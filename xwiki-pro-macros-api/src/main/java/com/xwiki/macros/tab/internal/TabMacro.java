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

import java.util.Collections;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Provider;
import javax.inject.Singleton;

import org.slf4j.Logger;
import org.xwiki.component.annotation.Component;
import org.xwiki.rendering.block.Block;
import org.xwiki.rendering.block.GroupBlock;
import org.xwiki.rendering.macro.MacroContentParser;
import org.xwiki.rendering.macro.MacroExecutionException;
import org.xwiki.rendering.macro.descriptor.DefaultContentDescriptor;
import org.xwiki.rendering.transformation.MacroTransformationContext;
import org.xwiki.text.StringUtils;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.xpn.xwiki.XWikiContext;
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

    @Inject
    protected MacroContentParser contentParser;

    @Inject
    private Provider<XWikiContext> xwikiContextProvider;

    @Inject
    private Logger logger;

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
        if (!"view".equals(xwikiContextProvider.get().getAction())) {
            return this.contentParser.parse(content, context, false, false).getChildren();
        } else {
            List<Block> macroContent = contentParser.parse(content, context, false, context.isInline()).getChildren();
            String divClass = "tab-pane"
                + (StringUtils.isEmpty(parameters.getCssClass()) ? "" : " " + parameters.getCssClass())
                + (parameters.isShowByDefault() ? " active" : "")
                + (parameters.getEffect() == TransitionEffect.FADE
                ? (parameters.isShowByDefault() ? " fade in" : " fade") : "");
            String configSerialized = "";
            try {
                ObjectMapper jsonObjectMapper = new ObjectMapper();
                configSerialized = jsonObjectMapper.writeValueAsString(parameters);
            } catch (Exception ex) {
                logger.error("Failed to serialize parameter object", ex);
            }
            Block groupBlock = new GroupBlock(macroContent, Map.of(
                "role", "tabpanel",
                "class", divClass,
                "data-config", configSerialized)
            );
            if (!StringUtils.isEmpty(parameters.getId())) {
                groupBlock.setParameter("id", parameters.getId());
            }
            StringBuilder cssStyle = new StringBuilder();
            if (!StringUtils.isEmpty(parameters.getCssStyle())) {
                cssStyle.append(parameters.getCssStyle());
            }
            if (parameters.getEffectDuration() != 0) {
                cssStyle.append("transition-duration: ");
                cssStyle.append(parameters.getEffectDuration());
                cssStyle.append("s;");
            }
            if (!StringUtils.isEmpty(cssStyle.toString())) {
                groupBlock.setParameter("style", cssStyle.toString());
            }
            return Collections.singletonList(groupBlock);
        }
    }
}
