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
import java.util.stream.Collectors;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.xwiki.test.ui.po.ViewPage;

public class ExpandMacroPage extends ViewPage
{
    @FindBy(css = "details.confluence-expand-macro.panel.panel-default")
    private List<WebElement> expandMacros;

    @FindBy(css = ".glyphicon.glyphicon-menu-right")
    private List<WebElement> icons;

    @FindBy(css = "details.confluence-expand-macro .title-text")
    private List<WebElement> titles;

    @FindBy(css = "details.confluence-expand-macro .panel-body")
    private List<WebElement> panels;

    public boolean hasIcon(int index)
    {
        return icons.get(index).isDisplayed();
    }

    public String getTitleText(int index)
    {
        return titles.get(index).getText();
    }

    public WebElement getPanel(int index)
    {
        return panels.get(index);
    }

    public int getExpandCount()
    {
        return expandMacros.size();
    }

    // Get all <img> tags inside the panel
    public List<WebElement> getImages(int index)
    {
        return getPanel(index).findElements(By.tagName("img"));
    }

    //Gel all test from paragraphs (<p> tag) inside panel body
    public List<String> getParagraphs(int index)
    {
        WebElement panel = expandMacros.get(index).findElement(By.cssSelector(".panel-body"));

        List<WebElement> paragraphs = panel.findElements(By.xpath("./p"));

        return paragraphs.stream()
            .map(WebElement::getText)
            .collect(Collectors.toList());
    }

    // Checks if an <img> with a certain src is present
    public boolean containsImageWithSrc(int index, String partialSrc)
    {
        return getImages(index).stream()
            .anyMatch(img -> img.getAttribute("src").contains(partialSrc));
    }

    //Checks whether the macro is expanded or not
    public boolean isExpanded(int index)
    {
        WebElement macro = expandMacros.get(index);
        return macro.getAttribute("open") != null;
    }

    //Clicks the macro to open/ close it
    public void toggleMacro(int index)
    {
        WebElement macro = expandMacros.get(index);
        WebElement summary = macro.findElement(By.tagName("summary"));
        summary.click();
    }
}