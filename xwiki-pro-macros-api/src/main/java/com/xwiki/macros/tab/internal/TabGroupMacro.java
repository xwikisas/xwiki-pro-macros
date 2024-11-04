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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Provider;
import javax.inject.Singleton;

import org.slf4j.Logger;
import org.xwiki.component.annotation.Component;
import org.xwiki.rendering.block.Block;
import org.xwiki.rendering.block.BulletedListBlock;
import org.xwiki.rendering.block.GroupBlock;
import org.xwiki.rendering.block.ListItemBlock;
import org.xwiki.rendering.block.RawBlock;
import org.xwiki.rendering.block.match.MacroBlockMatcher;
import org.xwiki.rendering.macro.MacroContentParser;
import org.xwiki.rendering.macro.MacroExecutionException;
import org.xwiki.rendering.macro.descriptor.DefaultContentDescriptor;
import org.xwiki.rendering.syntax.Syntax;
import org.xwiki.rendering.syntax.SyntaxType;
import org.xwiki.rendering.transformation.MacroTransformationContext;
import org.xwiki.skinx.SkinExtension;
import org.xwiki.text.StringUtils;
import org.xwiki.xml.XMLUtils;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.xpn.xwiki.XWikiContext;
import com.xwiki.macros.AbstractProMacro;
import com.xwiki.macros.tab.macro.TabGroupMacroParameters;
import com.xwiki.macros.tab.macro.TabMacroParameters;

/**
 * Tab-group macro: the main container macro for tab macro elements.
 *
 * @version $Id: $
 * @since 1.24.0
 */
@Component
@Named("tab-group")
@Singleton
public class TabGroupMacro extends AbstractProMacro<TabGroupMacroParameters>
{
    private static final String NAME = "Tab group";

    private static final String DESCRIPTION = "The main macro which group tab macro elements.";

    private static final String CONTENT_DESCRIPTION =
        "It's expected that the content of this macro are only tab macro. "
            + "Note all other element will be ignored. Only tab macro element will be rendered.";

    private static final String TRUE = "true";

    private static final String FALSE = "false";

    private static final String ID = "id";

    private static final String TAB_MACRO_ID = "tab";

    private static final String TAB_MACRO_PARAM_LABEL = "label";

    private static final String BLOCK_PARAM_CLASS = "class";

    private static final String BLOCK_PARAM_ROLE = "role";

    public static final String PARAM_NAME_NEXT_AFTER = "nextAfter";

    public static final String PARAM_NAME_EFFECT = "effectType";

    public static final String PARAM_NAME_EFFECT_DURATION = "effectDuration";

    @Inject
    private Provider<XWikiContext> xwikiContextProvider;

    @Inject
    private MacroContentParser contentParser;

    @Inject
    @Named("ssrx")
    private SkinExtension ssrx;

    @Inject
    @Named("jsrx")
    private SkinExtension jsrx;

    @Inject
    private Logger logger;

    /**
     * Create and initialize the descriptor of the macro.
     */
    public TabGroupMacro()
    {
        super(NAME, DESCRIPTION, new DefaultContentDescriptor(CONTENT_DESCRIPTION, true, Block.LIST_BLOCK_TYPE),
            TabGroupMacroParameters.class);
    }

    @Override
    public boolean supportsInlineMode()
    {
        return false;
    }

    private List<Block> renderView(TabGroupMacroParameters parameters, String content,
        MacroTransformationContext context) throws MacroExecutionException
    {
        ssrx.use("css/tabmacro.css");
        jsrx.use("js/tabmacro.js");
        String macroId = parameters.getId();

        if (StringUtils.isEmpty(macroId)) {
            macroId = context.getXDOM().getIdGenerator().generateUniqueId("tab-group");
        }

        Block contentBlocks = this.contentParser.parse(content, context, false, context.isInline());

        List<Block> ulBlocks = new ArrayList<>();
        List<Block> macroBlocks = contentBlocks.getBlocks(new MacroBlockMatcher(TAB_MACRO_ID), Block.Axes.CHILD);
        Optional<Block> defaultTabBlock = macroBlocks.stream()
            .filter(i -> TRUE.equals(i.getParameter(TabMacroParameters.PARAM_NAME_DEFAULT)))
            .findFirst();
        int defaultTabIndex = defaultTabBlock.isPresent() ? macroBlocks.indexOf(defaultTabBlock.get()) : 0;

        int inc = 0;
        for (Block mb : macroBlocks) {
            boolean isActive = inc == defaultTabIndex;
            mb.setParameter(TabMacroParameters.PARAM_NAME_DEFAULT, isActive ? TRUE : FALSE);
            String id = mb.getParameter(ID);
            if (id == null) {
                id = macroId + "_" + inc;
                mb.setParameter(ID, id);
            }
            // Set animation parameter for sub macro block
            if (parameters.getNextAfter() != 0 && mb.getParameter(PARAM_NAME_NEXT_AFTER) == null) {
                mb.setParameter(PARAM_NAME_NEXT_AFTER, Integer.toString(parameters.getNextAfter()));
            }
            if (parameters.getEffectType() != null && mb.getParameter(PARAM_NAME_EFFECT) == null) {
                mb.setParameter(PARAM_NAME_EFFECT, parameters.getEffectType().name());
            }
            if (parameters.getEffectDuration() != 0 && mb.getParameter(PARAM_NAME_EFFECT_DURATION) == null) {
                mb.setParameter(PARAM_NAME_EFFECT_DURATION, Integer.toString(parameters.getEffectDuration()));
            }

            Map<String, String> lbParam = new HashMap<>();
            lbParam.put(BLOCK_PARAM_ROLE, "presentation");
            if (isActive) {
                lbParam.put(BLOCK_PARAM_CLASS, "active");
            }
            // We use the raw block because the LinkBlock generate a span element which break bootstrap tabs CSS
            RawBlock linkBlock = new RawBlock(
                String.format("<a href=\"#%s\" aria-controls=\"%s\" role=\"tab\" data-toggle=\"tab\">%s</a>",
                    id, id, XMLUtils.escape(mb.getParameter(TAB_MACRO_PARAM_LABEL))),
                Syntax.HTML_5_0);
            ulBlocks.add(new ListItemBlock(Collections.singletonList(linkBlock), lbParam));
            inc++;
        }
        Map<String, String> mainBlockParam = new HashMap<>();
        StringBuilder style = new StringBuilder();
        StringBuilder mainDivClass = new StringBuilder("xwikitabmacro");
        if (!StringUtils.isEmpty(parameters.getId())) {
            mainBlockParam.put(ID, parameters.getId());
        }
        if (!StringUtils.isEmpty(parameters.getWidth())) {
            style.append("width: ").append(parameters.getWidth());
        }
        if (!StringUtils.isEmpty(parameters.getHeight())) {
            style.append("height: ").append(parameters.getWidth());
        }
        // FIXME I don't know if we need to apply the css class on the tab div or on the main div
        if (!StringUtils.isEmpty(parameters.getCssClass())) {
            mainDivClass.append(" ").append(parameters.getCssClass());
        }
        if (parameters.getTabLocation() != null
            && parameters.getTabLocation() != TabGroupMacroParameters.Location.TOP)
        {
            switch (parameters.getTabLocation()) {
                case RIGHT:
                    mainDivClass.append(" tabs-right");
                    break;
                case LEFT:
                    mainDivClass.append(" tabs-left");
                    break;
                case BOTTOM:
                    mainDivClass.append(" tabs-below");
                    break;
                case NONE:
                    mainDivClass.append(" tabs-none");
                    break;
                default:
                    break;
            }
        }
        mainBlockParam.put("style", style.toString());
        mainBlockParam.put(BLOCK_PARAM_CLASS, mainDivClass.toString());
        Block tabElementBlock = new BulletedListBlock(ulBlocks, Map.of(
            BLOCK_PARAM_CLASS, "nav nav-tabs",
            BLOCK_PARAM_ROLE, "tablist")
        );
        try {
            ObjectMapper jsonObjectMapper = new ObjectMapper();
            mainBlockParam.put("data-config", jsonObjectMapper.writeValueAsString(parameters));
        } catch (Exception ex) {
            logger.error("Failed to serialize parameter object", ex);
        }
        Block tabContentBlock = new GroupBlock(macroBlocks, Map.of(BLOCK_PARAM_CLASS, "tab-content"));
        List<Block> blockList;
        if (parameters.getTabLocation() == TabGroupMacroParameters.Location.BOTTOM) {
            blockList = Arrays.asList(tabContentBlock, tabElementBlock);
        } else {
            blockList = Arrays.asList(tabElementBlock, tabContentBlock);
        }
        Block result = new GroupBlock(blockList, mainBlockParam);
        return Collections.singletonList(result);
    }

    @Override
    protected List<Block> internalExecute(TabGroupMacroParameters parameters, String content,
        MacroTransformationContext context) throws MacroExecutionException
    {
        // Don't show tab in edit mode.
        Syntax syntax = context.getTransformationContext().getTargetSyntax();
        SyntaxType targetSyntaxType = syntax == null ? null : syntax.getType();
        if (SyntaxType.ANNOTATED_HTML.equals(targetSyntaxType) || SyntaxType.ANNOTATED_XHTML.equals(targetSyntaxType)) {
            return this.contentParser.parse(content, context, false, false).getChildren();
        } else {
            return renderView(parameters, content, context);
        }
    }
}
