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
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebElement;
import org.xwiki.test.ui.po.BaseElement;

/**
 * Represents a Button macro and provides access to its attributes.
 *
 * @version $Id$
 * @since 1.28
 */
public final class ButtonMacro extends BaseElement
{
    private WebElement button;

    public ButtonMacro(String id)
    {
        this.button = getDriver().findElement(By.id(id));
    }

    public String getLabel()
    {
        return button.getText();
    }

    public String getWidth()
    {
        return button.getCssValue("width");
    }

    public String getColor()
    {
        return button.getCssValue("background-color");
    }

    public String getCssClass()
    {
        return button.getAttribute("class");
    }

    public boolean hasIcon()
    {
        try {
            return button.findElement(By.xpath(".//img[@alt='Icon']")).isDisplayed();
        } catch (NoSuchElementException e) {
            return false;
        }
    }

    public String getParentTarget()
    {
        return getParentLink().getAttribute("target");
    }

    public boolean hasLink(String expectedUrl)
    {
        return expectedUrl.equals(getParentLink().getAttribute("href"));
    }

    private WebElement getParentLink()
    {
        return button.findElement(By.xpath("ancestor::a"));
    }
}
