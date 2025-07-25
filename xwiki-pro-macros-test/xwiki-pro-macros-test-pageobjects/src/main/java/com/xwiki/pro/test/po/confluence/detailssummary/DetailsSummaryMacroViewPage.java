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
package com.xwiki.pro.test.po.confluence.detailssummary;

import java.util.List;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.xwiki.test.ui.po.ViewPage;

/**
 * Represents a details summary macro in view mode.
 *
 * @version $Id$
 * @since 1.27.2
 */
public class DetailsSummaryMacroViewPage extends ViewPage
{

    private static final String DETAILS_SUMMARY = ".details_summary";

    private WebElement detailsSummaryMacro;

    /**
     * Retrieves the details summary macro at the specified index on the page. Throws an error if the macro at the given
     * index is not found.
     *
     * @param index the zero-based index of the details summary macro to retrieve
     */
    public DetailsSummaryMacroViewPage(int index)
    {
        List<WebElement> detailsSummaryMacros = getDriver().findElements(By.cssSelector(DETAILS_SUMMARY));
        if (detailsSummaryMacros != null && detailsSummaryMacros.size() < index) {
            this.detailsSummaryMacro = detailsSummaryMacros.get(index);
        }
    }

    /**
     * Retrieves the first details summary macro on the page. Throws an error if no details summary macro is found.
     */
    public DetailsSummaryMacroViewPage()
    {
        this.detailsSummaryMacro = getDriver().findElement(By.cssSelector(DETAILS_SUMMARY));
    }

    /**
     * @return the number of entries(details blocks) that where found by the macro.
     */
    public int entriesCount(){
        // The table header is also a row so we always should have at least one row.
        return detailsSummaryMacro.findElements(By.cssSelector("tr")).size() -1;
    }


}


