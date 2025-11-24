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
package com.xwiki.macros.internal.updateReferences;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.apache.solr.common.SolrInputDocument;
import org.xwiki.component.annotation.Component;
import org.xwiki.model.reference.DocumentReference;
import org.xwiki.model.reference.EntityReference;
import org.xwiki.model.reference.EntityReferenceSerializer;
import org.xwiki.search.solr.internal.api.FieldUtils;

/**
 * Handles the addition of backlinks to a solr document.
 *
 * @version $Id$
 * @since 1.29
 */
@Component(roles = LinkRegistry.class)
@Singleton
public class LinkRegistry
{
    private static final String ENTITY_PREFIX = "entity:";

    @Inject
    @Named("withtype/withparameters")
    private EntityReferenceSerializer<String> entitySerializer;

    /**
     * Associates the provided list of reference pages with the specified Solr document, establishing backlinks from
     * the document to each referenced page.
     *
     * @param solrDocument the document for which backlinks are being registered
     * @param references a list of pages that are linked within the solrDocument
     * @return {@code true} if the solrDocument was successfully updated with the backlinks,
     *         {@code false} otherwise
     */

    public boolean registerBacklinks(SolrInputDocument solrDocument, List<DocumentReference> references)
    {
        Set<String> links = new HashSet<>(references.size());
        Set<String> extendedLinks = new HashSet<>(references.size());
        boolean updated = false;

        for (DocumentReference reference : references) {
            EntityReference entityReference =
                new EntityReference(reference.getName(), reference.getType(), reference.getParent());
            String serializedEntity = entitySerializer.serialize(entityReference);
            // First we check if the Solr document already contains this link so we don't store duplicates.
            boolean alreadyExists =
                solrDocument.get(FieldUtils.LINKS) != null && solrDocument.get(FieldUtils.LINKS).getValues()
                    .contains(serializedEntity);
            if (!alreadyExists) {
                links.add(serializedEntity);
                extendedLinks.add(serializedEntity);
                this.extendLink(entityReference, extendedLinks);
                updated = true;
            }
        }

        for (String link : links) {
            solrDocument.addField(FieldUtils.LINKS, link);
        }
        for (String linkExtended : extendedLinks) {
            solrDocument.addField(FieldUtils.LINKS_EXTENDED, linkExtended);
        }
        return updated;
    }
    // Reference org.xwiki.search.solr.internal.metadata.SolrLinkSerializer
    private String serialize(EntityReference reference)
    {
        return ENTITY_PREFIX + this.entitySerializer.serialize(reference);
    }

    // Reference org.xwiki.search.solr.internal.metadata.AbstractSolrMetadataExtractor
    private void extendLink(EntityReference reference, Set<String> linksExtended)
    {
        // Ensures that the links are added with their parent to the extended link list so the links are properly
        // linked.
        for (EntityReference parent = reference.getParameters().isEmpty() ? reference
            : new EntityReference(reference.getName(), reference.getType(), reference.getParent(), null);
            parent != null; parent = parent.getParent()) {
            linksExtended.add(this.serialize(parent));
        }
    }
}
