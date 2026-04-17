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
package com.xwiki.macros.tab.internal;

import java.io.StringReader;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.apache.commons.lang3.StringUtils;
import org.xwiki.component.annotation.Component;
import org.xwiki.model.validation.EntityNameValidation;
import org.xwiki.rendering.block.Block;
import org.xwiki.rendering.block.GroupBlock;
import org.xwiki.rendering.block.MetaDataBlock;
import org.xwiki.rendering.macro.MacroContentParser;
import org.xwiki.rendering.macro.MacroExecutionException;
import org.xwiki.rendering.macro.descriptor.DefaultContentDescriptor;
import org.xwiki.rendering.parser.Parser;
import org.xwiki.rendering.transformation.MacroTransformationContext;
import org.xwiki.skinx.SkinExtension;

import com.xwiki.macros.AbstractProMacro;
import com.xwiki.macros.tab.macro.TabMacroParameters;
import com.xwiki.macros.tab.macro.TransitionEffect;

/**
 * Tab macro: a tab element for the tab-group macro.
 *
 * @version $Id: $
 * @since 1.24.0
 */
@Component
@Named("tab")
@Singleton
public class TabMacro extends AbstractProMacro<TabMacroParameters>
{
    private static final String NAME = "Tab";

    private static final String DESCRIPTION =
        "Tab element for the tab group macro. This is expected to be put as the tab-group macro content.";

    private static final String CONTENT_DESCRIPTION = "The content to be displayed in the tab.";

    private static final String BLOCK_PARAM_CLASS = "class";

    @Inject
    protected MacroContentParser contentParser;

    @Inject
    @Named("SlugEntityNameValidation")
    private EntityNameValidation slugEntityNameValidation;

    @Inject
    @Named("ssrx")
    private SkinExtension ssrx;

    @Inject
    @Named("plain/1.0")
    private Parser plainTextParser;

    /**
     * Create and initialize the descriptor of the macro.
     */
    public TabMacro()
    {
        super(NAME, DESCRIPTION,
            new DefaultContentDescriptor(CONTENT_DESCRIPTION, true, Block.LIST_BLOCK_TYPE),
            TabMacroParameters.class);
    }

    @Override
    public boolean supportsInlineMode()
    {
        return false;
    }

    @Override
    protected List<Block> internalExecute(TabMacroParameters parameters, String content,
        MacroTransformationContext context) throws MacroExecutionException
    {
        // Don't show tab in edit mode.
        return isEditMode(context)
            ? executeEdit(parameters, content, context)
            : executeView(parameters, content, context);
    }

    private List<Block> executeView(TabMacroParameters parameters, String content, MacroTransformationContext context)
        throws MacroExecutionException
    {
        List<Block> macroContent = contentParser.parse(content, context, false, context.isInline()).getChildren();
        String divClass = getDivClass(parameters);

        Block groupBlock = new GroupBlock(macroContent, Map.of(
            "role", "tabpanel",
            BLOCK_PARAM_CLASS, divClass,
            "data-next-after", Integer.toString(parameters.getNextAfter()))
        );

        if (StringUtils.isNotEmpty(parameters.getId())) {
            groupBlock.setParameter("id", slugEntityNameValidation.transform(parameters.getId()));
        }

        setCSSStyle(parameters, groupBlock);

        return Collections.singletonList(groupBlock);
    }

    private static String getDivClass(TabMacroParameters parameters)
    {
        String divClass = "tab-pane";

        if (StringUtils.isNotEmpty(parameters.getCssClass())) {
            divClass += " " + parameters.getCssClass();
        }

        if (parameters.isShowByDefault()) {
            divClass += " active";
        }

        if (parameters.getEffectType() == TransitionEffect.FADE) {
            divClass += parameters.isShowByDefault() ? " fade in" : " fade";
        }

        return divClass;
    }

    private static void setCSSStyle(TabMacroParameters parameters, Block groupBlock)
    {
        StringBuilder cssStyle = new StringBuilder();
        if (parameters.getEffectDuration() != 0) {
            cssStyle.append("transition-duration: ");
            cssStyle.append(parameters.getEffectDuration());
            cssStyle.append("s;");
        }
        if (StringUtils.isNotEmpty(parameters.getCssStyle())) {
            cssStyle.append(parameters.getCssStyle().trim());
        }
        if (StringUtils.isNotEmpty(cssStyle.toString())) {
            groupBlock.setParameter("style", cssStyle.toString());
        }
    }

    private List<Block> executeEdit(TabMacroParameters parameters, String content, MacroTransformationContext context)
        throws MacroExecutionException
    {
        ssrx.use("css/tabmacro.css");
        List<Block> tabLabelBlock;
        try {
            tabLabelBlock = plainTextParser.parse(new StringReader(parameters.getLabel())).getChildren();
        } catch (Exception e) {
            throw new MacroExecutionException("Can't get tab label", e);
        }
        if (!tabLabelBlock.isEmpty()) {
            tabLabelBlock.get(0).setParameter(BLOCK_PARAM_CLASS, "tabs-edit-title");
        }
        List<Block> children = this.contentParser.parse(content, context, false, context.isInline()).getChildren();
        Block editableContent = new MetaDataBlock(children, getNonGeneratedContentMetaData());
        List<Block> result = new LinkedList<>();
        result.add(new GroupBlock(
            tabLabelBlock, Map.of(BLOCK_PARAM_CLASS, "tabs-edit-title-block")));
        result.add(editableContent);
        return result;
    }
}
