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
package com.xwiki.pro.internal.resolvers;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Provider;
import javax.inject.Singleton;

import org.xwiki.component.annotation.Component;
import org.xwiki.contrib.confluence.resolvers.ConfluenceResolverException;
import org.xwiki.contrib.confluence.resolvers.ConfluenceSpaceKeyResolver;
import org.xwiki.contrib.confluence.resolvers.ConfluenceSpaceResolver;
import org.xwiki.model.EntityType;
import org.xwiki.model.reference.EntityReference;
import org.xwiki.model.reference.EntityReferenceResolver;
import org.xwiki.query.QueryException;
import org.xwiki.query.QueryManager;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.xpn.xwiki.XWikiContext;

import static org.xwiki.query.Query.HQL;

/**
 * Find a space with the given Confluence space key using the Confluence Migrator Pro's Link Mapping State.
 * @version $Id$
 * @since 1.19.0
 */

@Component
@Named("prolinkmapping")
@Singleton
public class ProConfluenceSpaceResolver implements ConfluenceSpaceResolver, ConfluenceSpaceKeyResolver
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

    private static final class ConfluenceSpace
    {
        private final String key;
        private final EntityReference ref;

        ConfluenceSpace(String key, EntityReference ref)
        {
            this.key = key;
            this.ref = ref;
        }
    }

    @Override
    public String getSpaceKey(EntityReference documentReference)
        throws ConfluenceResolverException
    {
        try {
            return getConfluenceSpaceKeyAndRef(documentReference).key;
        } catch (QueryException | JsonProcessingException e) {
            throw new ConfluenceResolverException(e);
        }
    }

    @Override
    public EntityReference getSpaceByKey(String spaceKey) throws ConfluenceResolverException
    {
        try {
            List<String> results = this.queryManager.createQuery(LINK_MAPPING_FOR_SPACE_KEY_HQL, HQL)
                .bindValue("spaceKey", spaceKey)
                .setLimit(1)
                .execute();

            if (results.isEmpty()) {
                return null;
            }

            return getSpaceByKey(spaceKey, results.get(0));
        } catch (JsonProcessingException | QueryException e) {
            throw new ConfluenceResolverException("Could not read the link mapping", e);
        }
    }

    @Override
    public EntityReference getSpace(EntityReference reference) throws ConfluenceResolverException
    {
        try {
            return getConfluenceSpaceKeyAndRef(reference).ref;
        } catch (QueryException | JsonProcessingException e) {
            throw new ConfluenceResolverException(e);
        }
    }

    private EntityReference getSpaceByKey(String spaceKey, String mapping)
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

    private ConfluenceSpace getConfluenceSpaceKeyAndRef(EntityReference documentReference)
        throws JsonProcessingException, QueryException
    {
        List<String> spaces = getSpaceSuffixedWithIDS(documentReference);

        List<Object[]> results;
        results = this.queryManager.createQuery(LINK_MAPPINGS_FOR_SPACES_HQL, HQL)
            .bindValue("spaceKeys", spaces)
            .execute();

        String spaceKey = null;
        EntityReference candidateSpace = null;
        for (Object[] result : results) {
            if (result.length == 2) {
                spaceKey = (String) result[0];
                spaceKey = spaceKey.substring(0, spaceKey.indexOf(':'));
                String mapping = (String) result[1];
                EntityReference space = getSpaceByKey(spaceKey, mapping);
                if (space != null && documentReference.hasParent(space) && (candidateSpace == null
                    || candidateSpace.size() < space.size())) {
                    candidateSpace = space;
                }
            }
        }

        return new ConfluenceSpace(spaceKey, candidateSpace);
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
