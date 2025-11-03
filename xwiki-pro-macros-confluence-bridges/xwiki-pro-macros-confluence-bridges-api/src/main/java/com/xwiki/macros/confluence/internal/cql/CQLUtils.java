/*
 * See the NOTICE file distributed with this work for additional
 * information regarding copyright ownership.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package com.xwiki.macros.confluence.internal.cql;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.apache.commons.lang3.StringUtils;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;
import org.slf4j.Logger;
import org.xwiki.component.annotation.Component;
import org.xwiki.model.EntityType;
import org.xwiki.model.reference.EntityReference;
import org.xwiki.model.reference.EntityReferenceSerializer;
import org.xwiki.query.Query;
import org.xwiki.query.QueryException;
import org.xwiki.query.QueryManager;

import com.google.gson.Gson;
import com.xwiki.macros.confluence.internal.ConfluenceSpaceUtils;

/**
 * Basic utility for executing and computing CQLs.
 *
 * @version $Id$
 * @since 1.29.0
 */
@Component(roles = CQLUtils.class)
@Singleton
public class CQLUtils
{
    /**
     * By default, solr limits to 10 results, which doesn't correspond to the behavior of Confluence. INT_MAX causes
     * huge performances issues, so we limit to 1000 arbitrarily by default.
     */
    public static final int DEFAULT_MAX = 1000;

    private static final String CQL = "cql";

    private static final Gson STRING_ESCAPER = new Gson();

    private static final String DELIMITER = "\\s*,\\s*|\\s+";

    private static final String LABELS = "labels";

    private static final String OPERATOR = "operator";

    private static final String TYPE = "type";

    private static final String OPEN_BRACKET = "(";

    private static final String CLOSE_BRACKET = ")";

    private static final String SORT = "sort";

    private final String[] solrSpecialChars =
        { "+", "-", "&&", "||", "!", OPEN_BRACKET, CLOSE_BRACKET, "{", "}", "[", "]", "^", "\"", "~", "*", "?", ":",
            "/", "\\", " " };

    private final String[] escapedSolrSpecialChars =
        { "\\+", "\\-", "\\&&", "\\||", "\\!", "\\(", "\\)", "\\{", "\\}", "\\[", "\\]", "\\^", "\\\"", "\\~", "\\*",
            "\\?", "\\:", "\\/", "\\\\", "\\ " };

    @Inject
    private QueryManager queryManager;

    @Inject
    private ConfluenceSpaceUtils confluenceSpaceUtils;

    @Inject
    private EntityReferenceSerializer<String> serializer;

    @Inject
    private Logger logger;

    /**
     * Execute the cql parameter if provided, otherwise attempt to build a CQL query from the parameters and execute
     * it.
     *
     * @param macroParameters parameters of the macro.
     * @return list of SolrDocuments matching the CQL.
     */
    public List<SolrDocument> buildAndExecute(Map<String, Object> macroParameters)
    {

        String cql = (String) macroParameters.getOrDefault(CQL, "");
        String fq = "";
        // If the CQL is not already present as a parameter we try to build it.
        if (StringUtils.isBlank(cql)) {
            cql = buildQuery(macroParameters);
            fq = builderFilter((String) macroParameters.getOrDefault("spaces", ""));
        }

        // If no type is provided we use the default.
        String type = ((String) macroParameters.getOrDefault(TYPE, ""));
        if (StringUtils.isBlank(type)) {
            fq = fq + " type:DOCUMENT";
        }
        int limit = ((int) macroParameters.getOrDefault("max", DEFAULT_MAX));
        String sortField = determineSortField(macroParameters);
        String sortDirection = ((boolean) macroParameters.getOrDefault("reverse", false)) ? "desc" : "asc";
        List<SolrDocument> docs = new ArrayList<>();
        Set<String> alreadyFound = new HashSet<>();
        // Everything is prepared, and we attempt to execute the query.
        try {
            Query query = queryManager.createQuery(cql, CQL);
            if (StringUtils.isNotBlank(fq)) {
                query.bindValue("fq", fq.trim());
            }
            query.setLimit(limit);
            query.bindValue(SORT, String.format("%s %s", sortField, sortDirection));
            QueryResponse queryResponse = (QueryResponse) query.execute().get(0);
            SolrDocumentList documents = queryResponse.getResults();
            for (SolrDocument document : documents) {
                String fullRef = String.format("%s:%s", document.get("wiki"), document.get("fullname"));
                if (!alreadyFound.contains(fullRef)) {
                    docs.add(document);
                    alreadyFound.add(fullRef);
                }
            }
        } catch (QueryException e) {
            logger.error("Failed to execute the CQL", e);
        }

        return docs;
    }

    /**
     * Build a Solr facet filter using the provided spaces.
     *
     * @param spaces list of comma separated spaces
     * @return a facet filter made from the @param spaces.
     */
    public String builderFilter(String spaces)
    {
        StringBuilder fq = new StringBuilder();
        if (StringUtils.isNotBlank(spaces) && !spaces.contains("@all")) {
            String[] spacesList = spaces.split(DELIMITER);
            for (String space : spacesList) {
                EntityReference entiyRefernce = confluenceSpaceUtils.getSloppySpace(space);
                int facetNumber = entiyRefernce.getReversedReferenceChain().size() - 1;
                if (entiyRefernce.getRoot().getType().equals(EntityType.WIKI)) {
                    facetNumber = facetNumber - 1;
                    String serializedReference = serializer.serialize(entiyRefernce);
                    String solrFacet = String.format("%d/%s", facetNumber, serializedReference);
                    String solrEscapedFacet =
                        StringUtils.replaceEach(solrFacet, solrSpecialChars, escapedSolrSpecialChars);
                    fq.append(" space_facet:");
                    fq.append(solrEscapedFacet);
                }
            }
        }
        return fq.toString();
    }

    /**
     * Build a CQL query from the parameters. The parameters that are taken into account are: operator, label, labels
     * and type.
     *
     * @param parameters a map of parameters from which the query should be built.
     * @return a cql query.
     */
    public String buildQuery(Map<String, Object> parameters)
    {
        StringBuilder cql = new StringBuilder();
        String operator = ((String) parameters.getOrDefault(OPERATOR, "")).equalsIgnoreCase("and") ? " AND " : " OR ";
        String labels = (String) (parameters.containsKey(LABELS) ? parameters.getOrDefault(LABELS, "")
                                                                 : parameters.getOrDefault("label", ""));
        if (StringUtils.isNotBlank(labels)) {
            String[] labelList = labels.split(DELIMITER);
            String joinedLabels = Arrays.stream(labelList).map(l -> "label = " + STRING_ESCAPER.toJson(l))
                .collect(Collectors.joining(operator));
            cql.append(OPEN_BRACKET).append(joinedLabels).append(CLOSE_BRACKET);
        }
        String type = ((String) parameters.getOrDefault(TYPE, ""));
        if (StringUtils.isNotBlank(type)) {
            // We serialize the object directly.
            cql.append(" AND type = ").append(STRING_ESCAPER.toJson(parameters.get(TYPE)));
        }
        return cql.toString();
    }

    private String determineSortField(Map<String, Object> parameters)
    {
        switch (((String) parameters.getOrDefault(SORT, ""))) {
            case "modified":
                return "date";
            case "creation":
                return "creationDate";
            default:
                return "title_sort";
        }
    }
}
