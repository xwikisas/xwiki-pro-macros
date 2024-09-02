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
package com.xwiki.macros.confluence.internal;

import java.io.StringReader;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xwiki.component.manager.ComponentLookupException;
import org.xwiki.component.manager.ComponentManager;
import org.xwiki.rendering.block.Block;
import org.xwiki.rendering.block.MacroBlock;
import org.xwiki.rendering.block.XDOM;
import org.xwiki.rendering.macro.Macro;
import org.xwiki.rendering.macro.descriptor.ContentDescriptor;
import org.xwiki.rendering.parser.Parser;
import org.xwiki.rendering.syntax.Syntax;

/**
 * Internal XDOM utils.
 * @since 1.22.0
 * @version $Id$
 */
public final class XDOMUtils
{
    private static final Logger LOGGER = LoggerFactory.getLogger(XDOMUtils.class);

    private XDOMUtils()
    {

    }

    private static XDOM parse(ComponentManager componentManager, String text, String syntaxId)
    {
        XDOM result;
        try {
            Parser parser = componentManager.getInstance(Parser.class, syntaxId);
            result = parser.parse(new StringReader(text));
        } catch (Exception e) {
            result = null;
        }
        return result;
    }

    /**
     * @param componentManager a component manager to find the implementation of the macro
     * @param macroBlock the macro to parse
     * @param syntaxId the syntax of the document
     * @return return the XDOM content of the given macro
     * @throws ComponentLookupException if something goes wrong when looking up the macro
     * @since 1.22.0
     */
    public static XDOM getMacroXDOM(ComponentManager componentManager, MacroBlock macroBlock, String syntaxId)
        throws ComponentLookupException
    {
        if (componentManager.hasComponent(Macro.class, macroBlock.getId())) {
            ContentDescriptor macroContentDescriptor =
                ((Macro<?>) componentManager.getInstance(Macro.class, macroBlock.getId()))
                    .getDescriptor()
                    .getContentDescriptor();

            if (macroContentDescriptor != null && macroContentDescriptor.getType().equals(Block.LIST_BLOCK_TYPE)
                && StringUtils.isNotBlank(macroBlock.getContent()))
            {
                return parse(componentManager, macroBlock.getContent(), syntaxId);
            }
        } else if (StringUtils.isNotBlank(macroBlock.getContent())) {
            // Just assume that the macro content is wiki syntax if we don't know the macro.
            LOGGER.debug("Calling parse on unknown macro [{}] with syntax [{}]", macroBlock.getId(), syntaxId);
            return parse(componentManager, macroBlock.getContent(), syntaxId);
        }
        return null;
    }

    /**
     * @param componentManager a component manager to find the implementation of the macro
     * @param macroBlock the macro to parse
     * @param syntaxId the syntax of the document
     * @return return the XDOM content of the given macro
     * @throws ComponentLookupException if something goes wrong when looking up the macro
     * @since 1.22.0
     */
    public static XDOM getMacroXDOM(ComponentManager componentManager, MacroBlock macroBlock, Syntax syntaxId)
        throws ComponentLookupException
    {
        return getMacroXDOM(componentManager, macroBlock, syntaxId.toIdString());
    }
}
