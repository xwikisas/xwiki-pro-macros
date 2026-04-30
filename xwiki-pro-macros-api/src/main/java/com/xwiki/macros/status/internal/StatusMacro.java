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
package com.xwiki.macros.status.internal;

import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.xwiki.component.annotation.Component;
import org.xwiki.rendering.block.Block;
import org.xwiki.rendering.block.CompositeBlock;
import org.xwiki.rendering.block.FormatBlock;
import org.xwiki.rendering.block.MacroBlock;
import org.xwiki.rendering.block.ParagraphBlock;
import org.xwiki.rendering.listener.Format;
import org.xwiki.rendering.macro.MacroContentParser;
import org.xwiki.rendering.macro.MacroExecutionException;
import org.xwiki.rendering.macro.MacroPreparationException;
import org.xwiki.rendering.transformation.MacroTransformationContext;
import org.xwiki.skinx.SkinExtension;

import com.xwiki.macros.AbstractProMacro;
import com.xwiki.macros.status.macro.StatusMacroParameters;

/**
 * Status macro: Display a text status of a given color, with the option to have a subtle aspect.
 *
 * @version $Id$
 * @since 1.31.1
 */
@Component
@Named("status")
@Singleton
public class StatusMacro extends AbstractProMacro<StatusMacroParameters>
{
    private static final String SKIN_RESOURCES_DOCUMENT_REFERENCE = "XWiki.Macros.Status";

    private static final String NAME = "Status";

    private static final String DESCRIPTION = "Display a status with a specific color and title.";

    @Inject
    @Named("ssx")
    private SkinExtension ssx;

    /**
     * Used to parse the content of the footnote.
     */
    @Inject
    private MacroContentParser contentParser;

    /**
     * Create and initialize the descriptor of the macro.
     */
    public StatusMacro()
    {
        super(NAME, DESCRIPTION, StatusMacroParameters.class);
    }

    @Override
    public boolean supportsInlineMode()
    {
        return true;
    }

    @Override
    public void prepare(MacroBlock macroBlock) throws MacroPreparationException
    {
        this.contentParser.prepareContentWiki(macroBlock);
    }

    @Override
    protected List<Block> internalExecute(StatusMacroParameters parameters, String content,
        MacroTransformationContext context) throws MacroExecutionException
    {
        try {
            if (!context.isInline() && isEditMode(context)) {
                // Construct a new macro block to replace the old one with the inline flag set to true.
                MacroBlock oldMacroBlock = context.getCurrentMacroBlock();
                MacroBlock replacement =
                    new MacroBlock(oldMacroBlock.getId(), oldMacroBlock.getParameters(), oldMacroBlock.getContent(),
                        true);
                oldMacroBlock.getParent().replaceChild(new ParagraphBlock(List.of(replacement)), oldMacroBlock);

                // Add a parent to the old macro block as otherwise the MacroTransformation fails in XWiki < 16.5.0.
                Block mockParent = new CompositeBlock();
                mockParent.addChild(oldMacroBlock);

                return List.of();
            }

            StringBuilder cssClass = prepareCSS(parameters);

            Map<String, String> blockParameters = Map.of("class", cssClass.toString());
            List<Block> blocks = this.contentParser.parse(getTitle(parameters), context, false, true).getChildren();
            blocks = List.of(new FormatBlock(blocks, Format.NONE, blockParameters));

            return blocks;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private String getTitle(StatusMacroParameters parameters)
    {
        String title = parameters.getTitle();
        if (title == null || title.isEmpty()) {
            title = parameters.getColour();
        }
        return title;
    }

    private StringBuilder prepareCSS(StatusMacroParameters parameters)
    {
        ssx.use(SKIN_RESOURCES_DOCUMENT_REFERENCE);
        StringBuilder cssClass = new StringBuilder(parameters.getColour().toLowerCase());
        cssClass.append("Status statusBox");
        if (parameters.isSubtle()) {
            cssClass.append(" subtle");
        }
        return cssClass;
    }
}
