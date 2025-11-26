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
package com.xwiki.macros.confluence.confluencepagetree.internal.macro;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Provider;
import javax.inject.Singleton;

import org.xwiki.component.annotation.Component;
import org.xwiki.model.EntityType;
import org.xwiki.model.reference.DocumentReference;
import org.xwiki.model.reference.DocumentReferenceResolver;
import org.xwiki.model.reference.EntityReference;
import org.xwiki.model.reference.EntityReferenceResolver;
import org.xwiki.model.reference.EntityReferenceSerializer;
import org.xwiki.model.reference.SpaceReference;
import org.xwiki.model.reference.SpaceReferenceResolver;
import org.xwiki.rendering.block.MacroBlock;
import org.xwiki.rendering.listener.reference.DocumentResourceReference;
import org.xwiki.rendering.listener.reference.ResourceReference;
import org.xwiki.text.StringUtils;

import com.xpn.xwiki.XWikiContext;
import com.xwiki.macros.internal.updateReferences.AbstractReferenceUpdateMacroRefactoring;

/**
 * Implementation of reference refactoring operation for the confluence_pagetree macro.
 *
 * @version $Id$
 * @since 1.29
 */
@Component
@Named("confluence_pagetree")
@Singleton
public class ConfluencePageTreeMacroParametersRefactoring extends AbstractReferenceUpdateMacroRefactoring
{
    private static final String ROOT_PARAMETER = "root";

    private static final String WEB_HOME = "WebHome";

    @Inject
    private DocumentReferenceResolver<String> documentReferenceResolver;

    @Inject
    private SpaceReferenceResolver<String> spaceReferenceResolver;

    @Inject
    @Named("compact")
    private EntityReferenceSerializer<String> entityReferenceSerializer;

    @Inject
    @Named("macro")
    private EntityReferenceResolver<String> macroEntityReferenceResolver;

    @Inject
    private Provider<XWikiContext> contextProvider;

    @Override
    public List<String> getParametersToUpdate()
    {
        return List.of(ROOT_PARAMETER);
    }

    @Override
    public Set<ResourceReference> extractReferences(MacroBlock macroBlock)
    {
        String rootValue = macroBlock.getParameter(ROOT_PARAMETER);

        Set<ResourceReference> resourceReferences = new HashSet<>();

        resourceReferences.add(new DocumentResourceReference(rootValue));

        if (!StringUtils.endsWith(rootValue, String.format(".%s", WEB_HOME))) {
            SpaceReference spaceRef = spaceReferenceResolver.resolve(rootValue);
            DocumentReference docRef = new DocumentReference(WEB_HOME, spaceRef);
            resourceReferences.add(new DocumentResourceReference(entityReferenceSerializer.serialize(docRef)));
        }

        return resourceReferences;
    }

    @Override
    public <T extends EntityReference> Optional<MacroBlock> refactorMacroBlock(MacroBlock macroBlock,
        DocumentReference currentDocumentReference, T sourceReference, T targetReference)
    {
        List<String> parametersToUpdate = getParametersToUpdate();

        if (parametersToUpdate.isEmpty()) {
            return Optional.empty();
        }

        boolean isModified = false;
        MacroBlock newMacroBlock = (MacroBlock) macroBlock.clone();

        for (String parameterToUpdate : parametersToUpdate) {
            String stringMacroReference = macroBlock.getParameter(parameterToUpdate);
            EntityReference macroReference =
                this.macroEntityReferenceResolver.resolve(stringMacroReference, EntityType.DOCUMENT, macroBlock,
                    sourceReference);

            boolean resolvedRelative = !isReferenceAbsolute(stringMacroReference, macroReference);

            String sourceReferenceParentStringReference =
                entityReferenceSerializer.serialize(sourceReference.getParent());

            EntityReference sourceReferenceParentReference =
                macroEntityReferenceResolver.resolve(sourceReferenceParentStringReference, EntityType.DOCUMENT,
                    macroBlock, sourceReference);

            if (macroReference.equals(sourceReference) || macroReference.equals(sourceReferenceParentReference)) {
                newMacroBlock.setParameter(parameterToUpdate,
                    serializeTargetReference(targetReference, currentDocumentReference, resolvedRelative));
                isModified = true;
            }
        }

        return isModified ? Optional.of(newMacroBlock) : Optional.empty();
    }
}
