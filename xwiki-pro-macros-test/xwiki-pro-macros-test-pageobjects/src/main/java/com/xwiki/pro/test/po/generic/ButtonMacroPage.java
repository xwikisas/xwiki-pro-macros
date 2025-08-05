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
import org.xwiki.test.ui.po.ViewPage;

public class ButtonMacroPage extends ViewPage
{
    public WebElement getButtonById(String id)
    {
        return getDriver().findElement(By.id(id));
    }

    public String getButtonWidth(String id)
    {
        return getButtonById(id).getCssValue("width");
    }

    public String getButtonColor(String id)
    {
        return getButtonById(id).getCssValue("background-color");
    }

    public String getButtonLabel(String id)
    {
        return getButtonById(id).getText();
    }

    public boolean hasLink(String id, String expectedUrl)
    {
        WebElement link = getButtonById(id).findElement(By.xpath("ancestor::a"));
        return expectedUrl.equals(link.getAttribute("href"));
    }

    public String getButtonParentTarget(String id)
    {
        WebElement parent = getButtonById(id).findElement(By.xpath("ancestor::a"));
        String target = parent.getAttribute("target");
        return target;
    }

    public String getButtonClass(String id)
    {

        return getButtonById(id).getAttribute("class");
    }

    public boolean hasIcon(String buttonId)
    {
        WebElement button = getButtonById(buttonId);
        try {
            WebElement icon = button.findElement(By.xpath(".//img[@alt='Icon']"));
            return icon.isDisplayed();
        } catch (NoSuchElementException e) {
            return false;
        }
    }
}
