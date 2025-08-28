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
package com.xwiki.pro.test.po.generic;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.xwiki.test.ui.po.BaseElement;

/**
 * Represents a MicrosoftStream macro and provides access to its attributes.
 *
 * @version $Id$
 * @since 1.28
 */
public class MicrosoftStreamMacro extends BaseElement
{
    private final WebElement MSmacro;

    public MicrosoftStreamMacro(WebElement MSmacro)
    {
        this.MSmacro = MSmacro;
    }

    private WebElement getIframe()
    {
        return MSmacro.findElement(By.tagName("iframe"));
    }

    public String getWidth()
    {
        return getIframe().getAttribute("width");
    }

    public String getHeight()
    {
        return getIframe().getAttribute("height");
    }

    public String getSrc()
    {
        return getIframe().getAttribute("src");
    }

    public boolean hasAutoplay()
    {
        return getSrc().contains("autoplay=true");
    }

    public boolean hasShowInfo()
    {
        return getSrc().contains("showinfo=true");
    }

    public boolean hasStartTime()
    {
        return getSrc().contains("st=");
    }

    public int getStartTime()
    {
        for (String part : getSrc().split("[?&]")) {
            if (part.startsWith("st=")) {
                return Integer.parseInt(part.substring(3));
            }
        }
        return -1;
    }

    public boolean hasCorrectURL(String url)
    {
        return getSrc().contains(url);
    }

    public String getAlignment()
    {
        String classes = MSmacro.getAttribute("class");
        if (classes.contains("msStreamMacro-right")) {
            return "right";
        } else if (classes.contains("pull-left")) {
            return "left";
        } else if (classes.contains("msStreamMacro-center")) {
            return "center";
        } else {
            return "none";
        }
    }
}
