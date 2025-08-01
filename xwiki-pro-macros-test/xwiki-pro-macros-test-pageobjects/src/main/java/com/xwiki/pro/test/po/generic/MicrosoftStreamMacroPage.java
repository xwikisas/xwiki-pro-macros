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

import java.util.List;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.xwiki.test.ui.po.ViewPage;

public class MicrosoftStreamMacroPage extends ViewPage
{
    @FindBy(css = ".msStreamMacro")
    private List<WebElement> macros;

    public int getMstreamCount()
    {
        return macros.size();
    }

    private WebElement getIframe(int index)
    {
        return macros.get(index).findElement(By.tagName("iframe"));
    }

    public String getIframeWidth(int index)
    {
        return getIframe(index).getAttribute("width");
    }

    public String getIframeHeight(int index)
    {
        return getIframe(index).getAttribute("height");
    }

    public String getIframeSrc(int index)
    {
        return getIframe(index).getAttribute("src");
    }

    public boolean hasAutoplay(int index)
    {
        return getIframeSrc(index).contains("autoplay=true");
    }

    public boolean hasShowInfo(int index)
    {
        return getIframeSrc(index).contains("showinfo=true");
    }

    public String getAlignment(int index)
    {
        String classes = macros.get(index).getAttribute("class");
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

    public boolean hasStartTime(int index)
    {
        return getIframeSrc(index).contains("st=");
    }

    public int getStartTime(int index)
    {
        String src = getIframeSrc(index);
        for (String part : src.split("[?&]")) {
            if (part.startsWith("st=")) {
                return Integer.parseInt(part.substring(3));
            }
        }
        return -1;
    }

    public boolean hasCorrectURL(int index, String url)
    {
        return getIframeSrc(index).contains(url);
    }
}
