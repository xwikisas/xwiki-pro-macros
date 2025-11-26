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
package com.xwiki.macros.button.internal.macro;

import java.util.Collections;
import java.util.List;
import java.util.Set;

import javax.inject.Named;
import javax.inject.Singleton;

import org.xwiki.component.annotation.Component;
import org.xwiki.rendering.block.MacroBlock;
import org.xwiki.rendering.listener.reference.DocumentResourceReference;
import org.xwiki.rendering.listener.reference.ResourceReference;
import org.xwiki.rendering.macro.MacroRefactoringException;

import com.xwiki.macros.internal.updateReferences.AbstractReferenceUpdateMacroRefactoring;

/**
 * Implementation of reference refactoring operation for the button macro.
 *
 * @version $Id$
 * @since 1.29
 */
@Component
@Named("button")
@Singleton
public class ButtonMacroParametersRefactoring extends AbstractReferenceUpdateMacroRefactoring
{
    private static final String URL_PARAMETER = "url";

    @Override
    public Set<ResourceReference> extractReferences(MacroBlock macroBlock) throws MacroRefactoringException
    {
        ResourceReference resourceReference = new DocumentResourceReference(macroBlock.getParameter(URL_PARAMETER));
        return Collections.singleton(resourceReference);
    }

    @Override
    public List<String> getParametersToUpdate()
    {
        return List.of(URL_PARAMETER);
    }
}
