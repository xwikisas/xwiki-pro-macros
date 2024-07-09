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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.inject.Singleton;

import org.xwiki.component.annotation.Component;
import org.slf4j.Logger;
import org.xwiki.model.EntityType;
import org.xwiki.model.reference.DocumentReference;
import org.xwiki.model.reference.EntityReference;
import org.xwiki.model.reference.EntityReferenceResolver;
import org.xwiki.query.QueryException;
import org.xwiki.query.QueryManager;
import org.xwiki.stability.Unstable;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.doc.XWikiDocument;

import static org.xwiki.query.Query.HQL;

/**
 * Tools to manipulate migrated Confluence spaces.
 * @version $Id$
 * @since 1.19.0
 */
@Component (roles = ConfluenceSpaceUtils.class)
@Singleton
@Unstable
public class ConfluenceSpaceUtils
{
    private static final TypeReference<Map<String, String>> TYPE_REF = new TypeReference<Map<String, String>>() { };

    private static final String LINK_MAPPING_HQL_TPL = "select %smappingProp.value from "
        + "XWikiDocument doc, "
        + "BaseObject o, "
        + "StringProperty spaceKeyProp, "
        + "LargeStringProperty mappingProp "
        + "where %s and "
        + "doc.fullName = o.name and "
        + "o.className = 'ConfluenceMigratorPro.Code.LinkMappingStateSpaceClass' and "
        + "spaceKeyProp.id.id = o.id and "
        + "spaceKeyProp.id.name = 'spaceKey' and "
        + "mappingProp.id.id = o.id and "
        + "mappingProp.id.name = 'mapping'";

    // The below HQL query was converted from the following XWQL statement:
    // ----
    // select o.mapping from Document doc, doc.object(ConfluenceMigratorPro.Code.LinkMappingStateSpaceClass) o "
    // where o.spaceKey = concat(:spaceKey, ':ids') or o.spaceKey = :spaceKey"
    // ----
    // This is because XWQL requires ConfluenceMigratorPro.Code.LinkMappingStateSpaceClass to be present in the wiki
    // while the translated HQL does not, so people can uninstall Confluence Migrator Pro and this code still works.
    private static final String LINK_MAPPING_FOR_SPACE_KEY_HQL = String.format(LINK_MAPPING_HQL_TPL,
        "", "(spaceKeyProp.value = concat (:spaceKey , ':ids') or spaceKeyProp.value = :spaceKey)");


    // The below HQL query was converted from the following XWQL statement:
    // ---
    // select o.spaceKey, o.mapping
    // from Document doc, doc.object(ConfluenceMigratorPro.Code.LinkMappingStateSpaceClass) o
    // where o.spaceKey in (:spaceKeys)
    // ---
    private static final String LINK_MAPPINGS_FOR_SPACES_HQL =  String.format(LINK_MAPPING_HQL_TPL,
        "spaceKeyProp.value, ", "spaceKeyProp.value in (:spaceKeys)");

    @Inject
    private EntityReferenceResolver<String> resolver;

    @Inject
    private QueryManager queryManager;

    @Inject
    private Provider<XWikiContext> contextProvider;

    @Inject
    private Logger logger;

    /**
     * @return the root of the Confluence space described by the parameter, or null if not found.
     * @param spaceKeyOrRef the space key, or "@self", or a XWiki reference to the space.
     */
    public EntityReference getSloppySpace(String spaceKeyOrRef)
    {
        try {
            if (spaceKeyOrRef.contains("@self")) {
                return getConfluenceSpace(contextProvider.get().getDoc().getDocumentReference());
            }

            if (spaceKeyOrRef.indexOf(':') != -1 || spaceKeyOrRef.indexOf('.') != -1) {
                // This is a XWiki reference
                EntityReference spaceRef = resolver.resolve(spaceKeyOrRef, EntityType.SPACE);
                EntityReference webHome = new EntityReference("WebHome", EntityType.DOCUMENT, spaceRef);
                if (!new XWikiDocument(new DocumentReference(webHome)).isNew()) {
                    // the home page of this space exists
                    return spaceRef;
                }
            }

            return getSpaceByKey(spaceKeyOrRef);
        } catch (QueryException | JsonProcessingException e) {
            logger.warn("Could not convert space [{}] to an entity reference", spaceKeyOrRef, e);
        }

        return null;
    }

    EntityReference getSpaceByKey(String spaceKey) throws QueryException, JsonProcessingException
    {
        List<String> results = this.queryManager.createQuery(LINK_MAPPING_FOR_SPACE_KEY_HQL, HQL)
            .bindValue("spaceKey", spaceKey)
            .setLimit(1)
            .execute();

        if (results.isEmpty()) {
            return null;
        }

        return getSpaceByKey(spaceKey, results.get(0));
    }

    EntityReference getSpaceByKey(String spaceKey, String mapping)
        throws JsonProcessingException
    {
        ObjectMapper objectMapper = new ObjectMapper();
        Map<String, String> refsById = objectMapper.readValue(mapping, TYPE_REF);
        Collection<String> refs = refsById.values();
        if (refs.isEmpty()) {
            return null;
        }

        EntityReference space = resolver.resolve(refs.iterator().next(), EntityType.DOCUMENT);
        EntityReference found = null;
        do {
            space = space.getParent();
            if (space != null && spaceKey.equals(space.getName())) {
                if (found != null) {
                    // The space name was found twice in the reference, find the space root the slow way
                    return commonRoot(refsById.values());
                }
                found = space;
            }
        } while (space != null && EntityType.SPACE.equals(space.getType()));

        return found == null ? commonRoot(refs) : found;
    }

    private EntityReference commonRoot(Collection<?> references)
    {
        String root = null;
        for (Object refObj : references) {
            if (refObj instanceof String) {
                root = root == null ? (String) refObj : commonRoot(root, (String) refObj);
                if (root.isEmpty()) {
                    return null;
                }
            }
        }
        return root == null ? null : resolver.resolve(root, EntityType.SPACE);
    }

    private static String commonRoot(String a, String b)
    {
        int i = 0;
        while (i < a.length() && i < b.length() && a.charAt(i) == b.charAt(i)) {
            i++;
        }

        return a.substring(0, i);
    }

    /**
     *
     * @return the space of the given document
     * @param documentReference the document for which to get the spacespace
     * @throws QueryException if something wrong happens
     * @throws JsonProcessingException if something wrong happens
     */
    public EntityReference getConfluenceSpace(EntityReference documentReference)
        throws QueryException, JsonProcessingException
    {
        List<String> spaces = getSpaceSuffixedWithIDS(documentReference);

        List<Object[]> results;
        results = this.queryManager.createQuery(LINK_MAPPINGS_FOR_SPACES_HQL, HQL)
            .bindValue("spaceKeys", spaces)
            .execute();

        EntityReference candidateSpace = null;
        for (Object[] result : results) {
            if (result.length == 2) {
                String spaceKey = (String) result[0];
                spaceKey = spaceKey.substring(0, spaceKey.indexOf(':'));
                String mapping = (String) result[1];
                EntityReference space = getSpaceByKey(spaceKey, mapping);
                if (space != null && documentReference.hasParent(space) && (candidateSpace == null
                    || candidateSpace.size() < space.size())) {
                    candidateSpace = space;
                }
            }
        }

        return candidateSpace;
    }

    private static List<String> getSpaceSuffixedWithIDS(EntityReference document)
    {
        EntityReference space = document.getParent();
        List<String> spaces = new ArrayList<>(document.size());
        while (space != null && EntityType.SPACE.equals(space.getType())) {
            spaces.add(space.getName() + ":ids");
            space = space.getParent();
        }
        return spaces;
    }
}


