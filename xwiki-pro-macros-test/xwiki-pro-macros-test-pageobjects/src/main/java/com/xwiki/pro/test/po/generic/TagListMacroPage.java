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
import org.openqa.selenium.support.FindBy;
import org.xwiki.test.ui.po.ViewPage;

import java.util.ArrayList;
import java.util.List;

public class TagListMacroPage extends ViewPage
{
    @FindBy(css = ".glossaryListRoot")
    private List<WebElement> listRoots;

    public List<String> getGlossaryTitles(int rootIndex)
    {

        WebElement root = listRoots.get(rootIndex);
        List<WebElement> glossaryTitles = root.findElements(By.cssSelector(".glossaryBinTitle"));
        List<String> titles = new ArrayList<>();
        for (WebElement el : glossaryTitles) {
            titles.add(el.getText());
        }
        return titles;
    }

    public List<String> getTagNames(int rootIndex)
    {

        WebElement root = listRoots.get(rootIndex);
        List<WebElement> glossaryElements = root.findElements(By.cssSelector(".glossaryBinElement"));
        List<String> tagNames = new ArrayList<>();
        for (WebElement el : glossaryElements) {
            tagNames.add(el.getText());
        }
        return tagNames;
    }

    public String getHtmlTagForTagName(int rootIndex, String tagName)
    {

        WebElement root = listRoots.get(rootIndex);
        String xpath = ".//li[contains(@class, 'glossaryBinElement')]/a[text()='" + tagName + "']";
        List<WebElement> elements = root.findElements(By.xpath(xpath));

        return elements.get(0).getTagName();
    }
}
