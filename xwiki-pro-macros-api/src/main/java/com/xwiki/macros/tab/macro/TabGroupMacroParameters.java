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
package com.xwiki.macros.tab.macro;

import com.xwiki.macros.tab.internal.TabGroupMacro;

/**
 * Parameters for {@link TabGroupMacro}.
 *
 * @version $Id: $
 * @since 1.24.0
 */
public class TabGroupMacroParameters
{
    private String id;

    private int width;

    private int height;

    private String cssClass;

    /**
     * @return the id of the element.
     */
    public String getId()
    {
        return id;
    }

    /**
     * @param id the id of the element.
     */
    public void setId(String id)
    {
        this.id = id;
    }

    /**
     * @return with of the macro.
     */
    public int getWidth()
    {
        return width;
    }

    /**
     * @param width of the macro.
     */
    public void setWidth(int width)
    {
        this.width = width;
    }

    /**
     * @return height of the macro.
     */
    public int getHeight()
    {
        return height;
    }

    /**
     * @param height height of the macro.
     */
    public void setHeight(int height)
    {
        this.height = height;
    }

    /**
     * @return the css class to add on the main element.
     */
    public String getCssClass()
    {
        return cssClass;
    }

    /**
     * @param cssClass the css class to add on the main element.
     */
    public void setCssClass(String cssClass)
    {
        this.cssClass = cssClass;
    }
}
