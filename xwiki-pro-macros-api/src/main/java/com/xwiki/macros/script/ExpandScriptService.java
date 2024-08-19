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
package com.xwiki.macros.script;

import java.io.StringReader;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Provider;
import javax.inject.Singleton;

import org.slf4j.Logger;
import org.xwiki.component.annotation.Component;
import org.xwiki.component.manager.ComponentLookupException;
import org.xwiki.component.manager.ComponentManager;
import org.xwiki.rendering.block.Block;
import org.xwiki.rendering.block.HeaderBlock;
import org.xwiki.rendering.block.MacroBlock;
import org.xwiki.rendering.block.XDOM;
import org.xwiki.rendering.block.match.ClassBlockMatcher;
import org.xwiki.rendering.block.match.MacroBlockMatcher;
import org.xwiki.rendering.block.match.OrBlockMatcher;
import org.xwiki.rendering.parser.ParseException;
import org.xwiki.rendering.parser.Parser;
import org.xwiki.rendering.renderer.BlockRenderer;
import org.xwiki.rendering.renderer.printer.DefaultWikiPrinter;
import org.xwiki.rendering.renderer.printer.WikiPrinter;
import org.xwiki.script.service.ScriptService;
import org.xwiki.stability.Unstable;


/**
 * Expand script service.
 * @version $Id$
 * @since 1.21.0
 */
@Component
@Singleton
@Named("promacroexpand")
@Unstable
public class ExpandScriptService implements ScriptService
{
    private static final String PLAIN_1_0 = "plain/1.0";

    @Inject
    @Named("context")
    private Provider<ComponentManager> componentManagerProvider;

    @Inject
    private Logger logger;

    @Inject
    @Named(PLAIN_1_0)
    private BlockRenderer plainTextBlockRenderer;

    /**
     * @return the title of the expand macro from its content, or null if no title can be deduced from the content.
     * @param content the content of the macro
     * @param syntaxId the syntax identifier of the document containing the expand macro.
     * @since 1.21.0
     */
    public String getAutoTitle(String content, String syntaxId)
    {
        if (content == null || content.isEmpty()) {
            return null;
        }

        Parser parser;

        try {
            parser = this.componentManagerProvider.get().getInstance(Parser.class, syntaxId);
        } catch (ComponentLookupException e) {
            logger.error("Could not find a parser for syntax [{}]", syntaxId, e);
            return null;
        }

        XDOM xdom;
        try {
            xdom = parser.parse(new StringReader(content));
        } catch (ParseException e) {
            logger.error("Could not parse content", e);
            return null;
        }
        List<Block> candidates = xdom.getBlocks(
            new OrBlockMatcher(
                new ClassBlockMatcher(HeaderBlock.class),
                new MacroBlockMatcher("panel")
            ),
            Block.Axes.DESCENDANT
        );

        for (Block candidate : candidates) {
            if (candidate instanceof MacroBlock) {
                MacroBlock panel = (MacroBlock) candidate;
                String title = panel.getParameter("title");
                if (title == null) {
                    String panelContent = panel.getContent();
                    title = getAutoTitle(panelContent, syntaxId);
                }
                if (title != null) {
                    return title;
                }
            } else if (candidate instanceof HeaderBlock) {
                WikiPrinter printer = new DefaultWikiPrinter();
                try {
                    BlockRenderer renderer =
                        this.componentManagerProvider.get().getInstance(BlockRenderer.class, PLAIN_1_0);
                    renderer.render(candidate.getChildren(), printer);
                    return printer.toString();
                } catch (ComponentLookupException e) {
                    logger.error("Could not find the plain/1.0 renderer", e);
                }
                return null;
            } else {
                // should not happen
                return null;
            }
        }

        return null;
    }
}
