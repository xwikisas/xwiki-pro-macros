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
import java.util.function.Function;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.xwiki.test.ui.po.ViewPage;

/**
 * Represents a generic base class for pages containing lists of macros.
 *
 * @version $Id$
 * @since 1.28
 */
public class GenericMacrosPage extends ViewPage
{
    public List<WebElement> getElements(String cssSelector)
    {
        return getDriver().findElements(By.cssSelector(cssSelector));
    }

    public int getMacroCount(String cssSelector)
    {
        return getElements(cssSelector).size();
    }

    public <T> T getMacro(String cssSelector, int index, Function<WebElement, T> constructor)
    {
        return constructor.apply(getElements(cssSelector).get(index));
    }
}
