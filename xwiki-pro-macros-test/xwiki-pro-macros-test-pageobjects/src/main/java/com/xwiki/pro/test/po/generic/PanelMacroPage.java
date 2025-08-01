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

public class PanelMacroPage extends ViewPage
{
    @FindBy(css = ".macro-panel")
    private List<WebElement> panels;

    @FindBy(css = ".macro-border")
    private List<WebElement> panelBorders;

    public int getPanelCount()
    {
        return panels.size();
    }

    private WebElement getTitle(int index)
    {
        return panels.get(index).findElement(By.cssSelector(".macro-panel-title"));
    }

    private WebElement getContent(int index)
    {
        return panels.get(index).findElement(By.cssSelector(".macro-panel-content"));
    }

    private WebElement getFooter(int index)
    {
        return panels.get(index).findElement(By.cssSelector(".macro-panel-footer"));
    }

    private String getStyleAttribute(WebElement element, String attr)
    {
        String style = element.getAttribute("style");
        if (style == null) {
            return null;
        }

        for (String part : style.split(";")) {
            String[] keyValue = part.split(":", 2);
            if (keyValue.length == 2 && keyValue[0].trim().equals(attr)) {
                return keyValue[1].trim();
            }
        }
        return null;
    }

    // Panel container attributes.
    public String getPanelWidth(int index)
    {
        return getStyleAttribute(panels.get(index), "width");
    }

    public String getPanelHeight(int index)
    {
        return getStyleAttribute(panels.get(index), "height");
    }

    public String getPanelBorderStyle(int index)
    {
        return getStyleAttribute(panelBorders.get(index), "border");
    }

    public String getPanelBorderRadius(int index)
    {
        return getStyleAttribute(panels.get(index), "border-radius");
    }

    // Title.
    public String getTitleBackgroundColor(int index)
    {
        return getStyleAttribute(getTitle(index), "background-color");
    }

    public String getTitleColor(int index)
    {
        return getStyleAttribute(getTitle(index), "color");
    }

    //Content
    public String getContentBackgroundColor(int index)
    {
        return getStyleAttribute(getContent(index), "background-color");
    }

    public String getContentColor(int index)
    {
        return getStyleAttribute(getContent(index), "color");
    }

    // Footer.
    public String getFooterBackgroundColor(int index)
    {
        return getStyleAttribute(getFooter(index), "background-color");
    }

    public String getFooterColor(int index)
    {
        return getStyleAttribute(getFooter(index), "color");
    }

    // Text content.
    public String getTitleText(int index)
    {
        return getTitle(index).getText();
    }

    public String getContentText(int index)
    {
        return getContent(index).getText();
    }

    public String getFooterText(int index)
    {
        return getFooter(index).getText();
    }

    public String getPanelClass(int index)
    {
        return panels.get(index).getAttribute("class");
    }
}
