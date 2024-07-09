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
package com.xwiki.macros.confluence.internal;

import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.xwiki.component.annotation.Component;
import org.xwiki.contrib.cql.aqlparser.ast.AbstractAQLRightHandValue;
import org.xwiki.contrib.cql.query.converters.ConfluenceIdResolver;
import org.xwiki.contrib.cql.query.converters.ConversionException;
import org.xwiki.model.EntityType;
import org.xwiki.model.reference.EntityReference;
import org.xwiki.model.reference.EntityReferenceResolver;
import org.xwiki.query.QueryException;
import org.xwiki.query.QueryManager;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import static org.xwiki.query.Query.HQL;

/**
 * Find a document with the given Confluence ID using the Confluence Migrator Pro's Link Mapping State.
 * @version $Id$
 * @since 1.19.0
 */
@Component
@Named("prolinkmapping")
@Singleton
public class ProConfluenceIdResolver implements ConfluenceIdResolver
{
    private static final TypeReference<Map<String, String>> TYPE_REF = new TypeReference<Map<String, String>>() { };

    // The below HQL query was converted from the following XWQL statement:
    // ----
    // select o.mapping from Document doc, doc.object(ConfluenceMigratorPro.Code.LinkMappingStateSpaceClass) o
    // where doc.name like '%:ids' and o.mapping like :idlike
    // ----
    // This is because XWQL requires ConfluenceMigratorPro.Code.LinkMappingStateSpaceClass to be present in the wiki
    // while the translated HQL does not, so people can uninstall Confluence Migrator Pro and this code still works.

    private static final String SPACE_LINK_MAPPING =  "select mappingProp.value from "
        + "XWikiDocument doc, "
        + "BaseObject o, "
        + "LargeStringProperty mappingProp where"
        + "(doc.name like '%:ids' and mappingProp.value like :idlike) and"
        + "doc.fullName = o.name and "
        + "o.className = 'ConfluenceMigratorPro.Code.LinkMappingStateSpaceClass' and "
        + "mappingProp.id.id = o.id and mappingProp.id.name = 'mapping'";

    @Inject
    private QueryManager queryManager;

    @Inject
    private EntityReferenceResolver<String> resolver;

    @Override
    public EntityReference getDocumentById(AbstractAQLRightHandValue node, long id) throws ConversionException
    {
        List<String> results;
        try {
            results = this.queryManager.createQuery(SPACE_LINK_MAPPING, HQL)
                .bindValue("idlike", "%\"" + id + "\"%")
                .execute();
        } catch (QueryException e) {
            throw new ConversionException(e, node == null ? null : node.getParserState());
        }

        ObjectMapper objectMapper = new ObjectMapper();

        for (String result : results) {
            try {
                String docRef = objectMapper.readValue(result, TYPE_REF).get("" + id);
                if (docRef != null) {
                    return resolver.resolve(docRef, EntityType.DOCUMENT);
                }
            } catch (JsonProcessingException e) {
                throw new ConversionException("Could not read the link mapping", e,
                    node == null ? null : node.getParserState());
            }
        }
        return null;
    }
}
