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
import org.openqa.selenium.support.FindBy;
import org.xwiki.test.ui.po.ViewPage;

public class ContributorsMacroPage extends ViewPage
{
    @FindBy(css = ".confluence-contributors")
    private List<WebElement> contributors;

    public int getContributorNameCount(int index)
    {
        WebElement macro = contributors.get(index);
        return macro.findElements(By.cssSelector(".contributor-name")).size();
    }

    public List<String> getContributorNames(int index)
    {
        WebElement macro = contributors.get(index);
        List<WebElement> nameLinks = macro.findElements(By.cssSelector(".contributor-name"));
        List<WebElement> allLinks = macro.findElements(By.tagName("a"));

        List<String> result = new ArrayList<>();

        for (WebElement contributor : nameLinks) {
            String text = contributor.getText();
            if (!text.isEmpty()) {
                result.add(text);
                continue;
            }
            boolean nextIsTarget = false;
            for (WebElement link : allLinks) {
                if (nextIsTarget) {
                    String nextText = link.getText();
                    if (!nextText.isEmpty()) {
                        result.add(nextText);
                    }
                    break;
                }
                if (link.equals(contributor)) {
                    nextIsTarget = true;
                }
            }
        }
        return result;
    }

    public boolean isListMode(int index)
    {

        List<WebElement> uls = contributors.get(index).findElements(By.tagName("ul"));
        return !uls.isEmpty();
    }

    public int getContributorCount(int index)
    {

        return contributors.get(index).findElements(By.cssSelector(".contributor-contribution-count")).size();
    }

    public List<Integer> getContributionCounts(int index)
    {

        return contributors.get(index).findElements(By.cssSelector(".contributor-contribution-count"))
            .stream()
            .map(el -> Integer.parseInt(el.getText()))
            .collect(Collectors.toList());
    }

    public boolean hasLastModifiedDates(int index)
    {
        return !contributors.get(index).findElements(By.cssSelector(".contributor-last-contribution-date")).isEmpty();
    }

    public String getNoneFoundMessage(int index)
    {

        List<WebElement> paragraphs = contributors.get(index).findElements(By.tagName("p"));

        return paragraphs.get(0).getText();
    }

    public boolean hasPages(int index)
    {

        return contributors.get(index).getText().contains("Pages:");
    }
}
