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

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.xwiki.test.ui.po.BaseElement;

/**
 * Represents a Recently Updated macro and provides access to its attributes.
 *
 * @version $Id$
 * @since 1.28
 */
public class RecentlyUpdatedMacro extends BaseElement
{
    private final WebElement macro;

    public RecentlyUpdatedMacro(WebElement macro)
    {
        this.macro = macro;
    }

    public List<String> getItemTitles()
    {
        List<WebElement> resultItems = macro.findElements(By.cssSelector(".result-item"));
        return resultItems.stream().map(item -> item.findElement(By.cssSelector(".result-title a")).getText().trim())
            .collect(Collectors.toList());
    }

    public boolean hasShowMoreButton()
    {
        return getShowMoreButton().isDisplayed();
    }

    public void clickShowMore()
    {
        getShowMoreButton().click();
    }

    public boolean hasHeading()
    {
        List<WebElement> headings = macro.findElements(By.tagName("h2"));
        return !headings.isEmpty() && headings.get(0).isDisplayed();
    }

    public boolean hasAvatars()
    {
        List<WebElement> macroResults = macro.findElements(By.cssSelector("img.avatar"));
        return !macroResults.isEmpty();
    }

    public String getAuthorName(int resultIndex)
    {
        List<WebElement> results = macro.findElements(By.cssSelector(".result-item, .result-container"));
        return results.get(resultIndex).findElement(By.cssSelector(".result-last-author-name")).getText();
    }

    public String getResultsTheme()
    {
        WebElement resultsList = macro.findElement(By.cssSelector("ul.results"));
        String classAttr = resultsList.getAttribute("class");

        for (String cls : classAttr.split(" ")) {
            if (cls.startsWith("theme-")) {
                return cls.substring("theme-".length());
            }
        }

        return null;
    }

    public boolean themeStructureIsCorrect(String expectedTheme)
    {
        WebElement resultsList = macro.findElement(By.cssSelector("ul.results"));

        List<String> expectedClasses;
        switch (expectedTheme) {
            case "concise":
                expectedClasses = Arrays.asList("result-title", "result-last-author-name", "result-date");
                break;
            case "social":
                expectedClasses = Arrays.asList("result-last-author-name", "result-title", "result-date");
                break;
            case "sidebar":
                expectedClasses = Arrays.asList("result-title", "result-date");
                break;
            default:
                return false;
        }

        List<WebElement> items = resultsList.findElements(By.cssSelector("li"));
        for (WebElement item : items) {
            String itemHtml = item.getAttribute("innerHTML");
            for (String expectedClass : expectedClasses) {
                if (!itemHtml.contains(expectedClass)) {
                    return false;
                }
            }
        }

        return true;
    }

    public String getMacroWidth()
    {
        return macro.getCssValue("--width");
    }

    private WebElement getShowMoreButton()
    {
        return macro.findElement(By.cssSelector("button.show-more"));
    }
}
