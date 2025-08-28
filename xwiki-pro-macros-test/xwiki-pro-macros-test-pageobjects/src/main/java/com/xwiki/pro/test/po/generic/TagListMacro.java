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

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.xwiki.test.ui.po.BaseElement;

/**
 * Represents a TagList macro and provides access to its attributes.
 *
 * @version $Id$
 * @since 1.28
 */
public class TagListMacro extends BaseElement
{
    private final WebElement tagList;

    public TagListMacro(WebElement tagList)
    {
        this.tagList = tagList;
    }

    public List<String> getGlossaryTitles()
    {
        List<WebElement> glossaryTitles = tagList.findElements(By.cssSelector(".glossaryBinTitle"));
        List<String> titles = new ArrayList<>();
        for (WebElement el : glossaryTitles) {
            titles.add(el.getText());
        }
        return titles;
    }

    public List<String> getTagNames()
    {
        return tagList.findElements(By.cssSelector(".glossaryBinElement")).stream().map(WebElement::getText)
            .collect(Collectors.toList());
    }

    public boolean hasLink(String tagName)
    {
        String xpath = ".//li[contains(@class, 'glossaryBinElement')]/a[text()='" + tagName + "']";
        return tagList.findElements(By.xpath(xpath)).stream().map(WebElement::getTagName)
            .anyMatch(tag -> tag.equals("a"));
    }
}
