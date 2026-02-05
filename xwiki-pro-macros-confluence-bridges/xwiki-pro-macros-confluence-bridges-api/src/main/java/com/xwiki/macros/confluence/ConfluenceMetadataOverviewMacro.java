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
package com.xwiki.macros.confluence;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Provider;
import javax.inject.Singleton;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.message.BasicNameValuePair;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;
import org.xwiki.component.annotation.Component;
import org.xwiki.contrib.cql.aqlparser.AQLParser;
import org.xwiki.contrib.cql.aqlparser.ast.AQLAtomicClause;
import org.xwiki.contrib.cql.aqlparser.ast.AQLBooleanLiteral;
import org.xwiki.contrib.cql.aqlparser.ast.AQLClauseOperator;
import org.xwiki.contrib.cql.aqlparser.ast.AQLClauseWithNextOperator;
import org.xwiki.contrib.cql.aqlparser.ast.AQLDateLiteral;
import org.xwiki.contrib.cql.aqlparser.ast.AQLNumberLiteral;
import org.xwiki.contrib.cql.aqlparser.ast.AQLStatement;
import org.xwiki.contrib.cql.aqlparser.ast.AQLStringLiteral;
import org.xwiki.contrib.cql.aqlparser.ast.AbstractAQLClause;
import org.xwiki.contrib.cql.aqlparser.ast.AbstractAQLRightHandValue;
import org.xwiki.contrib.cql.aqlparser.exceptions.ParserException;
import org.xwiki.localization.ContextualLocalizationManager;
import org.xwiki.model.reference.DocumentReference;
import org.xwiki.model.reference.DocumentReferenceResolver;
import org.xwiki.query.Query;
import org.xwiki.query.QueryException;
import org.xwiki.query.QueryManager;
import org.xwiki.rendering.block.Block;
import org.xwiki.rendering.block.MacroBlock;
import org.xwiki.rendering.macro.MacroExecutionException;
import org.xwiki.rendering.script.RenderingScriptService;
import org.xwiki.rendering.transformation.MacroTransformationContext;
import org.xwiki.script.service.ScriptService;
import org.xwiki.stability.Unstable;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.xpn.xwiki.XWiki;
import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.XWikiException;
import com.xpn.xwiki.doc.XWikiDocument;
import com.xpn.xwiki.objects.BaseCollection;
import com.xpn.xwiki.objects.PropertyInterface;
import com.xpn.xwiki.objects.classes.BaseClass;
import com.xpn.xwiki.objects.classes.BooleanClass;
import com.xpn.xwiki.objects.classes.DateClass;
import com.xpn.xwiki.objects.classes.TextAreaClass;
import com.xwiki.macros.AbstractProMacro;

import static org.apache.commons.lang3.StringUtils.removeStart;

/**
 * Confluence bridge for metadata-overview.
 *
 * NOTE: this bridge should be heavily reworked and simplified as soon as XWIKI-23940 is implemented.
 *
 * @since 1.30.0
 * @version $Id$
 */
@Component
@Named("confluence_metadata-overview")
@Singleton
@Unstable
public class ConfluenceMetadataOverviewMacro extends AbstractProMacro<ConfluenceMetadataOverviewMacroParameters>
{
    private static final String FAILEDTOEXECUTECQL = "rendering.macro.metadataOverview.failedtoexecutecql";
    private static final String DOC_TITLE = "doc.title";
    private static final String DOC_URL = "doc.url";
    private static final String ID = "id";
    private static final String NAME = "name";
    private static final String VISIBLE = "visible";
    private static final String DISPLAYER = "displayer";

    private static final String DATE = "date";
    private static final String BOOLEAN = "boolean";

    private static final String[] SOLR_PROP_TYPES = { "string", DATE, BOOLEAN, "int", "long", "float", "double" };

    @Inject
private Provider<XWikiContext> contextProvider;

    @Inject
    private ContextualLocalizationManager localizationManager;

    @Inject
    private DocumentReferenceResolver<String> resolver;

    @Inject
    private QueryManager queryManager;

    @Inject
    @Named("rendering")
    private ScriptService rendering;

    /**
     * Constructor.
     */
    public ConfluenceMetadataOverviewMacro()
    {
        super("Confluence Metadata Overview", "Confluence bridge macro for metadata-overview",
            null, ConfluenceMetadataOverviewMacroParameters.class);
    }

    @Override
    public boolean supportsInlineMode()
    {
        return false;
    }

    @Override
    protected List<Block> internalExecute(ConfluenceMetadataOverviewMacroParameters parameters, String content,
        MacroTransformationContext context) throws MacroExecutionException
    {
        // This macro is quite complicated. We need to parse the CQL expression to understand which the data to
        // display uses. We expect the CQL expression to start with "metadataset = metadataset.myset and ...".
        // Then, we try hard displaying the data using a feature-full LiveData. But the CQL expression can be too
        // powerful for the simple parsing we can do here, in which case we fall back to a full CQL querying, and
        // render the result using a table. Hopefully at some point we'll be able to give LiveData a Solr or a CQL
        // query, and we won't need all this complexity anymore.

        String cql = parameters.getCql();
        if (cql.isEmpty()) {
            return warning("Parameter cql is mandatory");
        }

        String className = null;
        List<NameValuePair> filters = new ArrayList<>();

        // if this becomes true, we ought to fall back to a full CQL query handling.
        boolean fullCQLFallback = false;

        try {
            AQLStatement cqlAST = AQLParser.parse(cql);
            List<AQLClauseWithNextOperator> clausesWithNextOp = cqlAST.getClausesWithNextOp();
            for (AQLClauseWithNextOperator clauseWithNextOp : clausesWithNextOp) {
                // we loop over the simple clauses of the CQL expression
                AQLAtomicClause clause = getSimpleClauseInConjunction(clauseWithNextOp);
                if (clause == null) {
                    // if we notice the CQL expression is not a conjunction of simple clauses, we fall back
                    fullCQLFallback = true;
                } else {
                    String field = clause.getField();
                    String value = parseAtomicCQLValue(clause);
                    if (value == null) {
                        // we could not parse the simple value, let's fall back
                        fullCQLFallback = true;
                    } else if ("label".equals(field)) {
                        filters.add(new BasicNameValuePair("tags", value));
                    } else if ("metadataset".equals(field)) {
                        className = getClassNameFromMetadatasetValue(value);
                    } else {
                        fullCQLFallback = addFilterFromField(field, filters, value) || fullCQLFallback;
                    }
                }
            }
        } catch (ParserException | IOException | QueryException e) {
            throw new MacroExecutionException("rendering.macro.metadataOverview.failedtoparsecql", e);
        }

        return render(fullCQLFallback, cql, className, filters, parameters);
    }

    private static AQLAtomicClause getSimpleClauseInConjunction(AQLClauseWithNextOperator clauseWithNextOp)
    {
        AQLClauseOperator op = clauseWithNextOp.getNextOperator();
        if (op == null || (op.isAnd() && !op.isNot())) {
            // the next operator is a simple "and", or there is no next operator
            AbstractAQLClause clause = clauseWithNextOp.getClause();
            if (clause instanceof AQLAtomicClause) {
                return (AQLAtomicClause) clause;
            }
        }
        return null;
    }

    private boolean addFilterFromField(String field, List<NameValuePair> filters, String value)
    {
        String metadataField = getMetadataField(field);
        if (metadataField == null) {
            return true;
        }

        // this is a metadata field
        filters.add(new BasicNameValuePair(metadataField, value));
        return false;
    }

    private String getClassNameFromMetadatasetValue(String value) throws QueryException
    {
        List<Object> classNames = queryManager.createQuery(
                "select setObj.name from"
                    + " BaseObject setObj,"
                    + " StringProperty lowerKeyProp"
                    + " where lowerKeyProp.id.id = setObj.id"
                    + " and lowerKeyProp.id.name = 'lowerKey'"
                    + " and lowerKeyProp.value = :setName", Query.HQL)
            .bindValue("setName", removeStart(value, "metadataset."))
            .setLimit(0)
            .execute();
        return classNames.isEmpty() ? null : (String) classNames.get(0);
    }

    private List<Block> render(boolean fullCQLFallback, String cql, String className, List<NameValuePair> filters,
        ConfluenceMetadataOverviewMacroParameters parameters) throws MacroExecutionException
    {
        BaseClass xClass = getBaseClass(className);

        List<String> properties = new ArrayList<>();
        List<Block> warning = fillProperties(parameters, properties, xClass);
        if (warning != null) {
            return warning;
        }

        return List.of(new MacroBlock(
            "liveData",
            Map.of(
                "sourceParameters", "translationPrefix=rendering.macro.metadataOverview.&className=" + className,
                "properties", DOC_TITLE + (properties.isEmpty() ? "" : (',' + String.join(",", properties))),
                "source", fullCQLFallback ? "" : "liveTable",
                "filters", URLEncodedUtils.format(filters, StandardCharsets.UTF_8)
            ),
            fullCQLFallback ? getLDContentUsingCQL(cql, className, xClass, properties) : null,
            false
        ));
    }

    private BaseClass getBaseClass(String className) throws MacroExecutionException
    {
        BaseClass xClass = null;
        DocumentReference classRef = StringUtils.isEmpty(className) ? null : resolver.resolve(className);
        if (classRef != null) {
            try {
                XWikiContext xcontext = contextProvider.get();
                XWikiDocument classDoc = xcontext.getWiki().getDocument(classRef, xcontext);
                xClass = classDoc.getXClass();
            } catch (XWikiException e) {
                throw new MacroExecutionException(
                    localizationManager.getTranslationPlain(
                        "rendering.macro.metadataOverview.failedtogetproperties", classRef.getName()),
                    e);
            }
        }
        return xClass;
    }

    private String getLDContentUsingCQL(String cql, String className, BaseClass xClass,
        List<String> properties) throws MacroExecutionException
    {
        List<Map<String, Object>> entries = getEntries(cql, className, properties);
        List<Map<String, Object>> propertyDescriptors = getPropertyDescriptors(xClass, properties);

        try {
            return new ObjectMapper().writeValueAsString(
                Map.of(
                    "data", Map.of(
                        "count", entries.size(),
                        "entries", entries
                    ),
                    "meta", Map.of(
                        "propertyDescriptors", propertyDescriptors
                    )
                )
            );
        } catch (IOException e) {
            throw new MacroExecutionException(FAILEDTOEXECUTECQL, e);
        }
    }

    private List<Map<String, Object>> getEntries(String cql, String className, List<String> properties)
        throws MacroExecutionException
    {
        List<QueryResponse> res;
        try {
            res = queryManager.createQuery(cql, "cql").execute();
        } catch (QueryException e) {
            throw new MacroExecutionException(FAILEDTOEXECUTECQL, e);
        }

        if (res.isEmpty()) {
            throw new MacroExecutionException(FAILEDTOEXECUTECQL);
        }

        XWikiContext context = contextProvider.get();
        XWiki wiki = context.getWiki();

        SolrDocumentList results = res.get(0).getResults();
        List<Map<String, Object>> entries = new ArrayList<>(results.size());
        for (SolrDocument result : results) {
            Map<String, Object> entry = new HashMap<>(properties.size() + 2);
            String fullName = (String) result.get("fullname");
            String url = wiki.getURL(fullName, "view", null, context);
            entry.put(DOC_TITLE, result.get("title_"));
            entry.put(DOC_URL, url);
            for (String property : properties) {
                entry.put(property, getPropertyValueForLD(className, result, property));
            }
            entries.add(entry);
        }
        return entries;
    }

    private Object getPropertyValueForLD(String className, SolrDocument result, String property)
    {
        String solrKeyPrefix = "property." + className + '.' + property + '_';
        for (String type : SOLR_PROP_TYPES) {
            Object val = result.get(solrKeyPrefix + type);

            if (val instanceof Collection) {
                // Live Data don't seem to like lists, so we join them with commas.
                return ((Collection<?>) val).stream()
                    .map(this::propertyValueToString)
                    .collect(Collectors.joining(", "));
            }

            if (val != null) {
                return val;
            }
        }

        String val = (String) result.get(solrKeyPrefix + "sortString");
        if (val != null) {
            // sortString is xwiki-escaped
            return ((RenderingScriptService) rendering).render(
                ((RenderingScriptService) rendering).parse(val, "xwiki/2.1"),
                "xhtml/1.0");
        }

        return "";
    }

    private String propertyValueToString(Object val)
    {
        if (val instanceof Date) {
            XWikiContext context = contextProvider.get();
            return context.getWiki().formatDate((Date) val, null, context);
        }

        return val == null ? null : val.toString();
    }

    private List<Map<String, Object>> getPropertyDescriptors(BaseClass xClass, List<String> properties)
    {
        List<Map<String, Object>> propertyDescriptors = new ArrayList<>(properties.size() + 1);
        propertyDescriptors.add(Map.of(
            ID, DOC_TITLE,
            NAME, localizationManager.getTranslationPlain("rendering.macro.metadataOverview.doc.title"),
            VISIBLE, true,
            DISPLAYER, Map.of(ID, "link", "propertyHref", DOC_URL)
        ));
        for (String id : properties) {
            PropertyInterface p = xClass == null ? null : xClass.get(id);
            String prettyName = p instanceof BaseCollection ? ((BaseCollection<?>) p).getPrettyName() : null;
            String displayer = getPropertyDisplayerId(p);
            Map<String, Object> propertyDescriptor = new HashMap<>(displayer == null ? 3 : 4);
            propertyDescriptor.put(ID, id);
            propertyDescriptor.put(NAME, StringUtils.defaultString(prettyName, id));
            propertyDescriptor.put(VISIBLE, true);
            if (displayer != null) {
                propertyDescriptor.put(DISPLAYER, displayer);
            }
            propertyDescriptors.add(propertyDescriptor);
        }
        return propertyDescriptors;
    }

    private static String getPropertyDisplayerId(PropertyInterface p)
    {
        if (p instanceof BooleanClass) {
            return BOOLEAN;
        }

        if (p instanceof DateClass) {
            return DATE;
        }

        if (p instanceof TextAreaClass) {
            String contentType = ((TextAreaClass) p).getContentType();
            if (TextAreaClass.ContentType.WIKI_TEXT.toString().equalsIgnoreCase(contentType)) {
                return "html";
            }
        }

        return null;
    }

    private List<Block> fillProperties(ConfluenceMetadataOverviewMacroParameters parameters,
        List<String> properties, BaseClass xClass)
    {
        String filterFields = parameters.getFilterfields();
        if (filterFields != null) {
            for (String filterField : StringUtils.split(filterFields, ',')) {
                String metadataField = getMetadataField(filterField);
                if (metadataField == null) {
                    return warning(localizationManager.getTranslationPlain(
                        "rendering.macro.metadataOverview.unexpectedfilterfield",
                        filterField
                    ));
                }
                properties.add(metadataField);
            }
        }

        if (properties.isEmpty() && xClass != null) {
            properties.addAll(xClass.getPropertyList());
        }
        return null;
    }

    private String parseAtomicCQLValue(AQLAtomicClause clause)
    {
        AbstractAQLRightHandValue right = clause.getRight();
        if (right instanceof AQLBooleanLiteral) {
            return ((AQLBooleanLiteral) right).isTrue() ? "1" : "0";
        }

        if (right instanceof AQLDateLiteral) {
            return getDateFromCQL((AQLDateLiteral) right);
        }

        if (right instanceof AQLNumberLiteral) {
            return ((AQLNumberLiteral) right).getNumber();
        }

        if (right instanceof AQLStringLiteral) {
            return ((AQLStringLiteral) right).getString();
        }

        return null;
    }

    private String getMetadataField(String field)
    {
        // Fields are of the shape SPACEKEY.metadatafield.FIELDNAME
        // We only want to keep the FIELDNAME part.
        String[] fieldParts = StringUtils.split(field, '.');
        if (fieldParts.length != 3 || !"metadatafield".equals(fieldParts[1]) && !fieldParts[2].isEmpty()) {
            return null;
        }

        return fieldParts[2];
    }

    private String getDateFromCQL(AQLDateLiteral dateLiteral)
    {
        XWikiContext xcontext = contextProvider.get();
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.YEAR, dateLiteral.getYear());
        cal.set(Calendar.MONTH, dateLiteral.getMonth());
        cal.set(Calendar.DAY_OF_MONTH, dateLiteral.getDay());

        if (dateLiteral.getHours() != -1) {
            cal.set(Calendar.HOUR_OF_DAY, dateLiteral.getHours());
        }

        if (dateLiteral.getMinutes() != -1) {
            cal.set(Calendar.MINUTE, dateLiteral.getHours());
        }

        return xcontext.getWiki().formatDate(cal.getTime(), "", xcontext);
    }

    private List<Block> warning(String content)
    {
        return List.of(
            new MacroBlock("warning", Map.of(), content, false)
        );
    }
}
