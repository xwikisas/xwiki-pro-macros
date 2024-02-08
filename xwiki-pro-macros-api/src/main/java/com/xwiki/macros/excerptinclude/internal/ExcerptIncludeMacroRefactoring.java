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
package com.xwiki.macros.excerptinclude.internal;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.inject.Named;
import javax.inject.Singleton;

import org.xwiki.component.annotation.Component;
import org.xwiki.rendering.block.MacroBlock;
import org.xwiki.rendering.internal.macro.include.IncludeMacroRefactoring;
import org.xwiki.rendering.listener.reference.ResourceReference;
import org.xwiki.rendering.macro.MacroRefactoringException;
import org.xwiki.stability.Unstable;

/**
 * Implementation of reference refactoring operation for excerpt-include macro "0" parameter.
 *
 * @version $Id$
 * @since 1.14.4
 */
@Component
@Singleton
@Named("excerpt-include")
@Unstable
public class ExcerptIncludeMacroRefactoring extends IncludeMacroRefactoring
{
    @Override
    public Set<ResourceReference> extractReferences(MacroBlock macroBlock) throws MacroRefactoringException
    {
        Map<String, String> parameters = new HashMap<>(macroBlock.getParameters());
        parameters.put("reference", parameters.remove("0"));
        macroBlock.setParameters(parameters);
        return super.extractReferences(macroBlock);
    }
}
