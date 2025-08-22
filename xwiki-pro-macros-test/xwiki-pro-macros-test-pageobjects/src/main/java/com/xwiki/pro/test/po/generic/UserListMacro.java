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
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.xwiki.test.ui.po.BaseElement;

/**
 * Represents a UserList macro and provides access to its attributes.
 *
 * @version $Id$
 * @since 1.28
 */
public class UserListMacro extends BaseElement
{
    private final WebElement userList;

    public UserListMacro(WebElement userList)
    {
        this.userList = userList;
    }

    public Map<String, UserListItem> getUsers()
    {
        return userList.findElements(By.cssSelector("tbody tr")).stream()
            .map(UserListItem::new)
            .collect(Collectors.toMap(
                UserListItem::getUsername,
                Function.identity()
            ));
    }
    /**
     * Note: This only checks the class attribute. The actual fixed-layout functionality cannot be verified.
     */
    public boolean hasFixedLayout()
    {
        String classAttr = userList.getAttribute("class");
        return classAttr != null && classAttr.contains("fixed-layout");
    }

    public Set<String> getUsernames()
    {
        return getUsers().values().stream()
            .map(UserListItem::getUsername)
            .collect(Collectors.toSet());
    }
}
