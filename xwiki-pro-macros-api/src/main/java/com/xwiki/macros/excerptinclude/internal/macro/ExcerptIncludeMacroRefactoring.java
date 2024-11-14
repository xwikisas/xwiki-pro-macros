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
package com.xwiki.macros.excerptinclude.internal.macro;

import java.util.Collections;
import java.util.Optional;
import java.util.Set;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.xwiki.component.annotation.Component;
import org.xwiki.model.EntityType;
import org.xwiki.model.reference.AttachmentReference;
import org.xwiki.model.reference.DocumentReference;
import org.xwiki.model.reference.EntityReference;
import org.xwiki.model.reference.EntityReferenceResolver;
import org.xwiki.model.reference.EntityReferenceSerializer;
import org.xwiki.rendering.block.MacroBlock;
import org.xwiki.rendering.listener.reference.DocumentResourceReference;
import org.xwiki.rendering.listener.reference.ResourceReference;
import org.xwiki.rendering.macro.MacroRefactoring;
import org.xwiki.rendering.macro.MacroRefactoringException;
import org.apache.commons.lang3.StringUtils;

/**
 * Implementation of reference refactoring operation for the excerpt-include macro.
 *
 * @version $Id$
 * @since 1.14.6
 */
@Component
@Singleton
@Named("excerpt-include")
public class ExcerptIncludeMacroRefactoring implements MacroRefactoring
{
    @Inject
    @Named("compact")
    private EntityReferenceSerializer<String> compactEntityReferenceSerializer;

    @Inject
    @Named("compactwiki")
    private EntityReferenceSerializer<String> compactWikiEntityReferenceSerializer;

    @Inject
    @Named("macro")
    private EntityReferenceResolver<String> macroEntityReferenceResolver;

    @Override
    public Optional<MacroBlock> replaceReference(MacroBlock macroBlock, DocumentReference currentDocumentReference,
        DocumentReference sourceReference, DocumentReference targetReference, boolean relative)
        throws MacroRefactoringException
    {
        return getMacroBlock(macroBlock, currentDocumentReference, sourceReference, targetReference);
    }

    @Override
    public Optional<MacroBlock> replaceReference(MacroBlock macroBlock, DocumentReference currentDocumentReference,
        AttachmentReference sourceReference, AttachmentReference targetReference, boolean relative)
        throws MacroRefactoringException
    {
        return getMacroBlock(macroBlock, currentDocumentReference, sourceReference, targetReference);
    }

    @Override
    public Set<ResourceReference> extractReferences(MacroBlock macroBlock)
    {
        ResourceReference resourceReference = new DocumentResourceReference(macroBlock.getParameter("0"));
        return Collections.singleton(resourceReference);
    }

    private <T extends EntityReference> Optional<MacroBlock> getMacroBlock(MacroBlock macroBlock,
        DocumentReference currentDocumentReference, T sourceReference, T targetReference)
    {
        // Note: an empty string means a reference to the current page and thus a recursive include. Renaming the page
        // doesn't require changing the value (since it's still an empty string), thus, we skip it!
        String stringMacroReference = macroBlock.getParameter("0");
        if (StringUtils.isNotEmpty(stringMacroReference)) {
            MacroBlock newMacroBlock = (MacroBlock) macroBlock.clone();

            // Check if the macro block's reference parameter contains the same reference as the one being refactored.
            // Note: Make sure to pass the sourceReference in the resolver since the context document may not be set
            // in the refactoring job.
            EntityReference macroReference =
                this.macroEntityReferenceResolver.resolve(stringMacroReference, EntityType.DOCUMENT, macroBlock,
                    sourceReference);

            // TODO: Don't honor the "relative" parameter for now since its usage seems to be not correct or hazy at
            // best. Instead refactoring using a relative reference if the user was using a relative reference and
            // an absolute reference if the user was using an absolute reference.
            boolean resolvedRelative = !isReferenceAbsolute(stringMacroReference, macroReference);

            // Update the excerpt-include macro parameter when the excerpt document pointed to by the excerpt-include
            // macro parameter has been moved. In this case the passed sourceReference should be equal to that
            // parameter in the MacroBlock.
            if (macroReference.equals(sourceReference)) {
                newMacroBlock.setParameter("0",
                    serializeTargetReference(targetReference, currentDocumentReference, resolvedRelative));
                return Optional.of(newMacroBlock);
            }
        }
        return Optional.empty();
    }

    private String serializeTargetReference(EntityReference newTargetReference, EntityReference currentReference,
        boolean relative)
    {
        // Notes:
        // - If the wiki was specified by the user, it'll get removed if it's not needed.
        // - When relative is false, we still don't want to display the wiki if it's the same as the wiki of the
        //   current reference.
        return relative
            ? this.compactEntityReferenceSerializer.serialize(newTargetReference, currentReference)
            : this.compactWikiEntityReferenceSerializer.serialize(newTargetReference,
            currentReference.extractReference(EntityType.WIKI));
    }

    private boolean isReferenceAbsolute(String referenceRepresentation, EntityReference absoluteEntityReference)
    {
        // Serialize the entityReference and verify if it matches the string representation
        // Remove the wiki part since we want to consider that a reference without a wiki part can still be an absolute
        // one.
        return this.compactWikiEntityReferenceSerializer.serialize(absoluteEntityReference,
            absoluteEntityReference.extractReference(EntityType.WIKI)).equals(referenceRepresentation);
    }
}
