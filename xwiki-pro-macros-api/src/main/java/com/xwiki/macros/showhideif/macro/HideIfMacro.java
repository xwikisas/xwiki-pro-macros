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
import java.util.LinkedList;
import java.util.List;

import javax.inject.Named;
import javax.inject.Singleton;

import org.xwiki.component.annotation.Component;
import org.xwiki.rendering.block.Block;
import org.xwiki.rendering.block.MetaDataBlock;
import org.xwiki.rendering.macro.MacroExecutionException;
import org.xwiki.rendering.syntax.Syntax;
import org.xwiki.rendering.syntax.SyntaxType;
import org.xwiki.rendering.transformation.MacroTransformationContext;

import com.xwiki.macros.showhideif.internal.macro.AbstractShowHideIfMacro;

/**
 * Hide if macro: Hide the content if the constraint match.
 *
 * @version $Id: $
 * @since 1.23.0
 */
@Component
@Named("hide-if")
@Singleton
public class HideIfMacro extends AbstractShowHideIfMacro
{
    private static final String NAME = "Hide if";

    private static final String DESCRIPTION =
        "Hide the content of this macro if the condition set through the parameters is met.";

    /**
     * Create and initialize the descriptor of the macro.
     */
    public HideIfMacro()
    {
        super(NAME, DESCRIPTION);
    }

    @Override
    protected List<Block> internalExecute(ShowHideIfMacroParameters parameters, String content,
        MacroTransformationContext context)
        throws MacroExecutionException
    {
        Syntax syntax = context.getTransformationContext().getTargetSyntax();
        SyntaxType targetSyntaxType = syntax == null ? null : syntax.getType();
        if (SyntaxType.ANNOTATED_HTML.equals(targetSyntaxType) || SyntaxType.ANNOTATED_XHTML.equals(targetSyntaxType)) {
            List<Block> children = this.contentParser.parse(content, context, false, context.isInline()).getChildren();
            return Collections.singletonList(new MetaDataBlock(children, this.getNonGeneratedContentMetaData()));
        } else {
            List<Block> result = new LinkedList<>();
            maybeGetUnsupportedParameterErrorBlock(context).ifPresent(result::add);
            if (!doesMatch(parameters)) {
                result.addAll(this.contentParser.parse(content, context, false, context.isInline()).getChildren());
            }
            return result;
        }
    }
}
