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
import javax.inject.Singleton;

import org.xwiki.component.annotation.Component;
import org.xwiki.model.reference.AttachmentReference;
import org.xwiki.model.reference.DocumentReference;
import org.xwiki.model.reference.EntityReferenceSerializer;
import org.xwiki.rendering.block.MacroBlock;
import org.xwiki.rendering.macro.MacroRefactoring;
import org.xwiki.rendering.macro.MacroRefactoringException;

import com.xwiki.macros.internal.updateReferences.listeners.PageRenameMacroParametersUpdateListener;

/**
 * Macro refactoring component that updates macro parameters when a referenced document is moved.
 *
 * @version $Id$
 * @since 1.29
 */
@Component
@Named("UpdateParametersRefactoring")
@Singleton
public class UpdateParametersRefactoring implements MacroRefactoring
{
    @Inject
    @Named("compact")
    private EntityReferenceSerializer<String> compactEntityReferenceSerializer;

    @Override
    public Optional<MacroBlock> replaceReference(MacroBlock macroBlock, DocumentReference currentDocumentReference,
        DocumentReference sourceReference, DocumentReference targetReference, boolean relative)
        throws MacroRefactoringException
    {
        String sourceReferenceString =
            compactEntityReferenceSerializer.serialize(sourceReference, currentDocumentReference);
        String targetReferenceString =
            compactEntityReferenceSerializer.serialize(targetReference, currentDocumentReference);

        boolean modified = false;

        List<String> parameters =
            PageRenameMacroParametersUpdateListener.MACROS_TO_SEARCH.get(macroBlock.getId());

        for (String parameter : parameters) {
            String value = macroBlock.getParameters().get(parameter);

            if (value != null && value.equals(sourceReferenceString)) {
                macroBlock.setParameter(parameter, targetReferenceString);
                modified = true;
            }
        }
        if (modified) {
            return Optional.of(macroBlock);
        }

        return Optional.empty();
    }

    @Override
    public Optional<MacroBlock> replaceReference(MacroBlock macroBlock, DocumentReference currentDocumentReference,
        AttachmentReference sourceReference, AttachmentReference targetReference, boolean relative)
        throws MacroRefactoringException
    {
        return Optional.empty();
    }
}
