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
package com.xwiki.macros.viewfile.internal.macro;

import java.util.List;
import java.util.Map;

import org.xwiki.rendering.block.Block;
import org.xwiki.rendering.block.FormatBlock;
import org.xwiki.rendering.block.GroupBlock;
import org.xwiki.rendering.listener.Format;

/**
 * Handles the creation of block wrappers depending on the inline parameter. Spans for inline == true, divs otherwise.
 *
 * @version $Id$
 * @since 1.27.3
 */
final class StaticBlockWrapperFactory
{
    private StaticBlockWrapperFactory()
    {
    }

    /**
     * Creates a wrapper block depending on hte isInline parameter.
     *
     * @param isInline true if the macro is inline, false otherwise
     * @param content content of the block
     * @param parameters parameters of the block
     * @return a new block.
     */
    static Block constructBlockWrapper(boolean isInline, List<Block> content, Map<String, String> parameters)
    {
        if (isInline) {
            // This creates a span. I don't know if there is a better way to create one.
            return new FormatBlock(content, Format.NONE, parameters);
        } else {
            return new GroupBlock(content, parameters);
        }
    }
}
