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
package com.xwiki.macros.note.internal.macro;

import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.xwiki.component.annotation.Component;
import org.xwiki.rendering.block.Block;
import org.xwiki.rendering.macro.Macro;
import org.xwiki.rendering.macro.MacroExecutionException;
import org.xwiki.rendering.macro.box.BoxMacroParameters;
import org.xwiki.rendering.macro.descriptor.DefaultContentDescriptor;
import org.xwiki.rendering.transformation.MacroTransformationContext;

import com.xwiki.macros.AbstractProMacro;
import com.xwiki.macros.note.macro.NoteMacroParameters;

/**
 * A wrapper for the warning macro.
 *
 * @version $Id$
 * @since 1.9.1
 */
@Component
@Named("note")
@Singleton
public class NoteMacro extends AbstractProMacro<NoteMacroParameters>
{
    // We want the note macro to have the exact same behaviour of the warning macro. We inject and execute it instead
    // of instantiating a new macro block with id "warning". Going with the latter means that we wrap the
    // content of the note macro with a warning macro. Subsequent calls to the note macro would add more and more
    // warning macro wraps.
    @Inject
    @Named("warning")
    private Macro<BoxMacroParameters> warningMacro;

    /**
     * Default constructor.
     */
    public NoteMacro()
    {
        super("Note", "Write a note.", new DefaultContentDescriptor("Content of the note.", true,
            Block.LIST_BLOCK_TYPE), NoteMacroParameters.class);
    }

    @Override
    public boolean supportsInlineMode()
    {
        return true;
    }

    @Override
    protected List<Block> internalExecute(NoteMacroParameters parameters, String content,
        MacroTransformationContext context) throws MacroExecutionException
    {
        BoxMacroParameters warningParams = new BoxMacroParameters();
        if (parameters.getTitle() != null)
        {
            warningParams.setTitle(parameters.getTitle());
        }
        return warningMacro.execute(warningParams, content, context);
    }
}
