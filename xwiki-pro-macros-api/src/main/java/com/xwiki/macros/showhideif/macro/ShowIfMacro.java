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
package com.xwiki.macros.showhideif.macro;

import java.util.Collections;
import java.util.List;

import javax.inject.Named;
import javax.inject.Singleton;

import org.xwiki.component.annotation.Component;
import org.xwiki.rendering.block.Block;
import org.xwiki.rendering.macro.MacroExecutionException;
import org.xwiki.rendering.transformation.MacroTransformationContext;

import com.xwiki.macros.showhideif.internal.macro.AbstractShowHideIfMacro;

/**
 * Show if macro: Show the content if the constraint match.
 * @version $Id: $
 */
@Component
@Named("show-if")
@Singleton
public class ShowIfMacro extends AbstractShowHideIfMacro
{
    private static final String NAME = "Show if";

    private static final String DESCRIPTION = "Show if condition match parameters";

    /**
     * Create and initialize the descriptor of the macro.
     */
    public ShowIfMacro()
    {
        super(NAME, DESCRIPTION);
    }

    @Override
    public List<Block> execute(MacroParameter parameters, String content, MacroTransformationContext context)
        throws MacroExecutionException
    {
        if (doesMatch(parameters)) {
            return this.contentParser.parse(content, context, false, context.isInline()).getChildren();
        } else {
            return Collections.emptyList();
        }
    }
}
