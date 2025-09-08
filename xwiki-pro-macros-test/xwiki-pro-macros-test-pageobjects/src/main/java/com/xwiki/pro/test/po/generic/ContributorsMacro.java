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
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.xwiki.test.ui.po.BaseElement;

/**
 * Represents a Contributors macro and provides access to its attributes.
 *
 * @version $Id$
 * @since 1.28
 */
public class ContributorsMacro extends BaseElement
{
    private final WebElement macro;

    public ContributorsMacro(WebElement macro)
    {
        this.macro = macro;
    }

    public List<String> getNames()
    {
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

    public boolean isListMode()
    {
        return !macro.findElements(By.tagName("ul")).isEmpty();
    }

    public List<Integer> getContributionCounts()
    {
        return macro.findElements(By.cssSelector(".contributor-contribution-count")).stream()
            .map(el -> Integer.parseInt(el.getText())).collect(Collectors.toList());
    }

    public boolean hasLastModifiedDates()
    {
        return !macro.findElements(By.cssSelector(".contributor-last-contribution-date")).isEmpty();
    }

    public String getNoneFoundMessage()
    {
        return macro.findElements(By.tagName("p")).stream().map(WebElement::getText)
            .filter(text -> !text.startsWith("Pages:")).findFirst().orElse("");
    }

    public boolean hasPages()
    {
        return macro.getText().contains("Pages:");
    }

    public boolean hasContributionCount()
    {
        return !getContributionCounts().isEmpty();
    }

    public List<String> getPages()
    {
        if (!hasPages()) {
            return Collections.emptyList();
        }

        WebElement paragraph =
            macro.findElements(By.tagName("p")).stream().filter(p -> p.getText().startsWith("Pages:")).findFirst()
                .orElse(null);

        if (paragraph == null) {
            return Collections.emptyList();
        }

        List<WebElement> links = paragraph.findElements(By.tagName("a"));
        if (!links.isEmpty()) {
            List<String> result = new ArrayList<>();
            for (WebElement link : links) {
                result.add(link.getText());
            }
            return result;
        }

        return Arrays.asList(paragraph.getText().substring("Pages:".length()).trim());
    }
}
