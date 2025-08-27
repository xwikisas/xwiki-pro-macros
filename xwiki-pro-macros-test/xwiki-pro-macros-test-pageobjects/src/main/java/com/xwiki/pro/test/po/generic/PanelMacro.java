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
 * Represents a Panel macro and provides access to its attributes.
 *
 * @version $Id$
 * @since 1.28
 */
public class PanelMacro extends BaseElement
{
    private final WebElement panel;

    private final WebElement panelBorder;

    public PanelMacro(WebElement panel, WebElement panelBorder)
    {
        this.panel = panel;
        this.panelBorder = panelBorder;
    }

    // Container attributes.
    public String getWidth()
    {
        return getStyleAttribute(panel, "width");
    }

    public String getHeight()
    {
        return getStyleAttribute(panel, "height");
    }

    public String getBorderRadius()
    {
        return getStyleAttribute(panel, "border-radius");
    }

    public String getBorderStyle()
    {
        return getStyleAttribute(panelBorder, "border");
    }

    // Title.
    public String getTitleBackgroundColor()
    {
        return getStyleAttribute(getTitleElement(), "background-color");
    }

    public String getTitleColor()
    {
        return getStyleAttribute(getTitleElement(), "color");
    }

    public String getTitleText()
    {
        return getTitleElement().getText();
    }

    // Content.
    public String getContentBackgroundColor()
    {
        return getStyleAttribute(getContentElement(), "background-color");
    }

    public String getContentColor()
    {
        return getStyleAttribute(getContentElement(), "color");
    }

    public String getContentText()
    {
        return getContentElement().getText();
    }

    // Footer.
    public String getFooterBackgroundColor()
    {
        return getStyleAttribute(getFooterElement(), "background-color");
    }

    public String getFooterColor()
    {
        return getStyleAttribute(getFooterElement(), "color");
    }

    public String getFooterText()
    {
        return getFooterElement().getText();
    }

    public String getCssClass()
    {
        return panel.getAttribute("class");
    }

    private WebElement getTitleElement()
    {
        return panel.findElement(By.cssSelector(".macro-panel-title"));
    }

    private WebElement getContentElement()
    {
        return panel.findElement(By.cssSelector(".macro-panel-content"));
    }

    private WebElement getFooterElement()
    {
        return panel.findElement(By.cssSelector(".macro-panel-footer"));
    }

    private String getStyleAttribute(WebElement element, String attr)
    {
        String style = element.getAttribute("style");
        if (style == null) {
            return null;
        }

        for (String part : style.split(";")) {
            String[] keyValue = part.split(":");
            if (keyValue[0].trim().equals(attr)) {
                return keyValue[1].trim();
            }
        }
        return null;
    }
}
