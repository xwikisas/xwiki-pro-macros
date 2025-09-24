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
import org.xwiki.test.ui.po.BaseElement;

/**
 * Represents an Excerpt Include macro and provides access to its attributes.
 *
 * @version $Id$
 * @since 1.28
 */
public class ExcerptIncludeMacro extends BaseElement
{
    private final WebElement excerpt;

    public ExcerptIncludeMacro(WebElement excerpt)
    {
        this.excerpt = excerpt;
    }

    public String getTitle()
    {
        return excerpt.findElement(By.cssSelector(".macro-panel-title p")).getText();
    }

    public String getContent()
    {
        return excerpt.findElement(By.cssSelector(".macro-panel-content")).getText().trim();
    }

    public boolean isContentDisplayed()
    {
        return excerpt.findElement(By.cssSelector(".macro-panel-content")).isDisplayed();
    }
}
