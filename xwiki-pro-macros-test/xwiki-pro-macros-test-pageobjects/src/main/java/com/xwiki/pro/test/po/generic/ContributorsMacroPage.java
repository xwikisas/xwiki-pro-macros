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
            String text = contributor.getText().trim();
            if (!text.isEmpty()) {
                result.add(text);
                continue;
            }

            boolean found = false;
            boolean nextIsTarget = false;

            for (WebElement link : allLinks) {
                if (nextIsTarget) {
                    String nextText = link.getText().trim();
                    if (!nextText.isEmpty()) {
                        result.add(nextText);
                        found = true;
                        break;
                    }
                }

                if (link.equals(contributor)) {
                    nextIsTarget = true;
                }
            }

            if (!found) {
                String href = contributor.getAttribute("href");
                if (href != null && href.contains("/")) {
                    result.add(href.substring(href.lastIndexOf("/") + 1));
                } else {
                    result.add("(unknown)");
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
        WebElement macro = contributors.get(index);
        return macro.findElements(By.cssSelector(".contributor-contribution-count")).size();
    }

    public List<Integer> getContributionCounts(int index)
    {
        WebElement macro = contributors.get(index);
        return macro.findElements(By.cssSelector(".contributor-contribution-count"))
            .stream()
            .map(el -> Integer.parseInt(el.getText().trim()))
            .collect(Collectors.toList());
    }

    public boolean hasLastModifiedDates(int index) {
        WebElement macro = contributors.get(index);
        return !macro.findElements(By.cssSelector(".contributor-last-contribution-date")).isEmpty();
    }

}
