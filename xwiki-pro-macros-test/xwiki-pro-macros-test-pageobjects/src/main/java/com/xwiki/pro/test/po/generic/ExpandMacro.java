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
import org.xwiki.test.ui.po.BaseElement;

public class ExpandMacro extends BaseElement
{
    private final WebElement expand;

    public ExpandMacro(WebElement expand)
    {
        this.expand = expand;
    }

    public boolean hasIcon()
    {
        WebElement icon = expand.findElement(By.cssSelector(".glyphicon.glyphicon-menu-right"));
        return icon.isDisplayed();
    }

    public String getTitle()
    {
        return expand.findElement(By.cssSelector(".title-text")).getText().trim();
    }

    public WebElement getPanel()
    {
        return expand.findElement(By.cssSelector(".panel-body"));
    }

    public List<WebElement> getImages()
    {
        return getPanel().findElements(By.tagName("img"));
    }

    public List<String> getTextContent()
    {
        List<WebElement> paragraphs = getPanel().findElements(By.tagName("p"));
        return paragraphs.stream().map(WebElement::getText).filter(s -> !s.isEmpty()).collect(Collectors.toList());
    }

    public boolean containsImage(String partialSrc)
    {
        return getImages().stream().anyMatch(img -> img.getAttribute("src").contains(partialSrc));
    }

    public boolean isExpanded()
    {
        return expand.getAttribute("open") != null;
    }

    public void toggle()
    {
        expand.findElement(By.tagName("summary")).click();
    }
}
