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

import org.openqa.selenium.WebElement;
import org.xwiki.test.ui.po.ViewPage;

/**
 * Represents a generic base class for pages containing lists of macros.
 *
 * @version $Id$
 * @since 1.28
 *
 *  @param <T> the macro type to wrap each WebElement
 */
public abstract class AbstractGenericMacroPage<T> extends ViewPage
{
    private final Function<WebElement, T> constructor;

    public AbstractGenericMacroPage(Function<WebElement, T> constructor)
    {
        this.constructor = constructor;
    }

    public int getMacroCount()
    {
        return getElements().size();
    }

    public T getMacro(int index)
    {
        return constructor.apply(getElements().get(index));
    }

    protected abstract List<WebElement> getElements();
}
