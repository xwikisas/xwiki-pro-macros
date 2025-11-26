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

import java.util.List;
import java.util.Optional;

import javax.inject.Inject;
import javax.inject.Named;

import org.xwiki.model.EntityType;
import org.xwiki.model.reference.AttachmentReference;
import org.xwiki.model.reference.DocumentReference;
import org.xwiki.model.reference.EntityReference;
import org.xwiki.model.reference.EntityReferenceResolver;
import org.xwiki.model.reference.EntityReferenceSerializer;
import org.xwiki.rendering.block.MacroBlock;
import org.xwiki.rendering.macro.MacroRefactoring;
import org.xwiki.rendering.macro.MacroRefactoringException;

/**
 * Implementation of reference refactoring operation for the button macro.
 *
 * @version $Id$
 * @since 1.29
 */
public abstract class AbstractReferenceUpdateMacroRefactoring implements MacroRefactoring
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

    /**
     * Returns the list of parameter names in the macro that should be updated during reference refactoring.
     * @return the list of parameter names to update
     */
    public abstract List<String> getParametersToUpdate();

    @Override
    public Optional<MacroBlock> replaceReference(MacroBlock macroBlock, DocumentReference currentDocumentReference,
        DocumentReference sourceReference, DocumentReference targetReference, boolean relative)
        throws MacroRefactoringException
    {
        return refactorMacroBlock(macroBlock, currentDocumentReference, sourceReference, targetReference);
    }

    @Override
    public Optional<MacroBlock> replaceReference(MacroBlock macroBlock, DocumentReference currentDocumentReference,
        AttachmentReference sourceReference, AttachmentReference targetReference, boolean relative)
    {
        return refactorMacroBlock(macroBlock, currentDocumentReference, sourceReference, targetReference);
    }

    /**
     * Refactors a macro block by updating its parameters when they match a given source reference.
     *
     * @param <T> the type of entity reference
     * @param macroBlock the original macro block
     * @param currentDocumentReference the reference of the document where the macro resides
     * @param sourceReference the reference to be replaced
     * @param targetReference the reference that replaces the source
     * @return an optional containing the updated macro block, or empty if no parameter needed refactoring
     */
    public <T extends EntityReference> Optional<MacroBlock> refactorMacroBlock(MacroBlock macroBlock,
        DocumentReference currentDocumentReference, T sourceReference, T targetReference)
    {
        List<String> parametersToUpdate = getParametersToUpdate();
        if (!parametersToUpdate.isEmpty()) {
            MacroBlock newMacroBlock = (MacroBlock) macroBlock.clone();

            for (String parameterToUpdate : parametersToUpdate) {
                String stringMacroReference = macroBlock.getParameter(parameterToUpdate);
                EntityReference macroReference =
                    this.macroEntityReferenceResolver.resolve(stringMacroReference, EntityType.DOCUMENT, macroBlock,
                        sourceReference);

                boolean resolvedRelative = !isReferenceAbsolute(stringMacroReference, macroReference);

                if (macroReference.equals(sourceReference)) {
                    newMacroBlock.setParameter(parameterToUpdate,
                        serializeTargetReference(targetReference, currentDocumentReference, resolvedRelative));
                }
            }
            return Optional.of(newMacroBlock);
        }

        return Optional.empty();
    }

    /**
     * Serializes a target reference either as a relative or absolute reference,
     * depending on the provided flag.
     *
     * @param newTargetReference the reference to serialize
     * @param currentReference the current entity used as the serialization base
     * @param relative whether to serialize the reference in relative form
     * @return the serialized reference string
     */
    public String serializeTargetReference(EntityReference newTargetReference, EntityReference currentReference,
        boolean relative)
    {
        return relative
            ? this.compactEntityReferenceSerializer.serialize(newTargetReference, currentReference)
            : this.compactWikiEntityReferenceSerializer.serialize(newTargetReference,
            currentReference.extractReference(EntityType.WIKI));
    }

    /**
     * Determines whether the given reference representation is absolute by comparing it
     * with the fully serialized form of the resolved reference.
     *
     * @param referenceRepresentation the raw reference string found in the macro
     * @param absoluteEntityReference the resolved reference used for comparison
     * @return true if the reference string represents an absolute reference, false otherwise
     */
    public boolean isReferenceAbsolute(String referenceRepresentation, EntityReference absoluteEntityReference)
    {
        return this.compactWikiEntityReferenceSerializer.serialize(absoluteEntityReference,
            absoluteEntityReference.extractReference(EntityType.WIKI)).equals(referenceRepresentation);
    }
}
