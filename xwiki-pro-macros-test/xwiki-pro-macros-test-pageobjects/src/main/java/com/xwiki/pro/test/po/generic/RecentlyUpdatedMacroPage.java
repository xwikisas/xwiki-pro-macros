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
import org.openqa.selenium.support.FindBy;
import org.xwiki.test.ui.po.ViewPage;

public class RecentlyUpdatedMacroPage extends ViewPage
{
    @FindBy(css = ".recently-updated-macro")
    private List<WebElement> recentlyUpdatedMacros;

    public int getRecentlyUpdatedItemCount(int macroIndex)
    {
        WebElement macro = recentlyUpdatedMacros.get(macroIndex);

        List<WebElement> resultItems = macro.findElements(By.cssSelector(".result-item"));

        List<String> matchingTitles = resultItems.stream()
            .map(item -> item.findElement(By.cssSelector(".result-title a")))
            .map(WebElement::getText)
            .filter(text -> text != null && text.contains("xwiki:"))
            .collect(Collectors.toList());

        System.out.println("Found " + matchingTitles.size() + " matching result items in macro[" + macroIndex + "]:");
        for (String title : matchingTitles) {
            System.out.println(" - " + title);
        }

        return matchingTitles.size();
    }

    public boolean hasShowMoreButton(int macroIndex)
    {
        if (macroIndex >= recentlyUpdatedMacros.size()) {
            return false;
        }

        WebElement macro = recentlyUpdatedMacros.get(macroIndex);
        List<WebElement> buttons = macro.findElements(By.cssSelector("button.show-more"));

        return !buttons.isEmpty() && buttons.get(0).isDisplayed();
    }

    public void clickShowMore(int macroIndex)
    {

        WebElement showMoreButton =
            recentlyUpdatedMacros.get(macroIndex).findElement(By.cssSelector("button.show-more"));
        if (showMoreButton.isDisplayed() && showMoreButton.isEnabled()) {
            showMoreButton.click();
        }
    }

    public boolean macroHasHeading(int macroIndex)
    {

        List<WebElement> headings = recentlyUpdatedMacros.get(macroIndex).findElements(By.tagName("h2"));

        return !headings.isEmpty() && headings.get(0).isDisplayed();
    }

    public boolean macroHasAvatars(int macroIndex)
    {
        List<WebElement> macroResults = recentlyUpdatedMacros.get(macroIndex)
            .findElements(By.cssSelector("img.avatar"));

        return !macroResults.isEmpty();
    }

    public String getAuthorName(int macroIndex, int resultIndex)
    {

        List<WebElement> results = recentlyUpdatedMacros.get(macroIndex).findElements(By.cssSelector(".result-item"));
        WebElement authorElement = results.get(resultIndex).findElement(By.className("result-last-author-name"));

        return authorElement.getText().trim();
    }

    public String getResultsTheme(int macroIndex)
    {
        WebElement macro = recentlyUpdatedMacros.get(macroIndex);
        WebElement resultsList = macro.findElement(By.cssSelector("ul.results"));
        String classAttr = resultsList.getAttribute("class");

        for (String cls : classAttr.split(" ")) {
            if (cls.startsWith("theme-")) {
                return cls.substring("theme-".length());
            }
        }

        return null;
    }

    public boolean themeStructureIsCorrect(int macroIndex, String expectedTheme)
    {
        WebElement macro = recentlyUpdatedMacros.get(macroIndex);
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

    public String getMacroWidth(int macroIndex)
    {
        String style = recentlyUpdatedMacros.get(macroIndex).getAttribute("style");
        int pos = style.indexOf("--width");
        int colon = style.indexOf(":", pos);
        int semicolon = style.indexOf(";", colon);
        return style.substring(colon + 1, semicolon).trim();
    }
}


