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

/**
 * Represents a Content Report Table macro and provides access to its attributes.
 *
 * @version $Id$
 * @since 1.28
 */
public class ContentReportTableMacro extends BaseElement
{
    private final WebElement report;

    public ContentReportTableMacro(WebElement report)
    {
        this.report = report;
    }

    public int getResultsCount()
    {
        List<WebElement> rows = report.findElements(By.cssSelector("tbody tr"));
        return (int) rows.stream().filter(r -> !r.findElements(By.tagName("td")).isEmpty()).count();
    }

    public List<String> getTitles()
    {
        return report.findElements(By.cssSelector("tbody tr td:nth-child(1) a")).stream().map(WebElement::getText)
            .collect(Collectors.toList());
    }

    public List<String> getCreators()
    {
        return report.findElements(By.cssSelector("tbody tr td:nth-child(2)")).stream().map(WebElement::getText)
            .collect(Collectors.toList());
    }

    public int getModifiedDateCount()
    {
        return (int) report.findElements(By.cssSelector("tbody tr td:nth-child(3)")).stream().map(WebElement::getText)
            .filter(s -> !s.isEmpty() && !s.equals("Modified")).count();
    }
}
