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
package com.xwiki.macros.status.macro;

import com.xwiki.macros.status.internal.StatusMacro;

/**
 * Parameter bean class for {@link StatusMacro}.
 *
 * @version $Id$
 * @since 1.31.1
 */
public class StatusMacroParameters
{
    private String colour = "Grey";

    private String title;

    private boolean subtle;

    /**
     * The aspect of the status: either white text on dark color background ({@code false}) or dark color text on light
     * color background ({@code true}). Default value is {@code false}.
     *
     * @return {@code true} if the status is subtle, {@code false} otherwise
     */
    public boolean isSubtle()
    {
        return subtle;
    }

    /**
     * See {@link #isSubtle()}.
     *
     * @param subtle the aspect of the status.
     */
    public void setSubtle(boolean subtle)
    {
        this.subtle = subtle;
    }

    /**
     * The color of the status. The default value is "Grey".
     *
     * @return The color of the status.
     */
    public String getColour()
    {
        return this.colour;
    }

    /**
     * See {@link #getColour()}.
     *
     * @param colour the color of the status.
     */
    public void setColour(String colour)
    {
        this.colour = colour;
    }

    /**
     * The text of the status.
     *
     * @return The text of the status.
     */
    public String getTitle()
    {
        return this.title;
    }

    /**
     * See {@link #getTitle()}.
     *
     * @param title the text of the status.
     */
    public void setTitle(String title)
    {
        this.title = title;
    }
}
