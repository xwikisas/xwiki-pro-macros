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
import java.util.Set;
import java.util.stream.Collectors;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.xwiki.test.ui.po.BaseElement;

// Represents a single instance of the UserList macro and provides access to its attributes.

public class UserListMacro extends BaseElement
{
    private final WebElement userList;

    public UserListMacro(WebElement userList)
    {
        this.userList = userList;
    }

    public int getUserCount()
    {
        return userList.findElements(By.cssSelector("tbody tr")).size();
    }

    public WebElement getUserRow(int userIndex)
    {
        return userList.findElements(By.cssSelector("tbody tr")).get(userIndex);
    }

    public int getUserPropertiesCount(int userIndex)
    {
        return getUserRow(userIndex).findElements(By.tagName("td")).size();
    }

    public List<WebElement> getUserProperties(int userIndex)
    {
        return getUserRow(userIndex).findElements(By.tagName("td"));
    }

    public List<String> getUserPropertiesText(int userIndex)
    {
        return getUserProperties(userIndex).stream().map(WebElement::getText).collect(Collectors.toList());
    }

    public List<String> getUserPropertyTypes(int userIndex)
    {
        List<WebElement> tds = getUserProperties(userIndex);
        List<String> types = new ArrayList<>();

        for (WebElement td : tds) {
            String classAttr = td.getAttribute("class");
            if (classAttr != null && classAttr.startsWith("xwiki-userlist-user-")) {
                types.add(classAttr.substring("xwiki-userlist-user-".length()));
            } else {
                types.add("");
            }
        }

        return types;
    }

    public boolean isEmailLinkValid(int userIndex, String expectedEmail)
    {
        WebElement emailTd = getUserProperties(userIndex).get(3);
        WebElement emailLink = emailTd.findElement(By.tagName("a"));
        String href = emailLink.getAttribute("href");
        return href != null && href.equals("mailto:" + expectedEmail);
    }

    public String getUserAvatarTitle(int userIndex)
    {
        WebElement avatarTd = getUserProperties(userIndex).get(0);
        WebElement img = avatarTd.findElement(By.tagName("img"));
        return img.getAttribute("title");
    }

    public String getUserAvatarAlt(int userIndex)
    {
        WebElement avatarTd = getUserProperties(userIndex).get(0);
        WebElement img = avatarTd.findElement(By.tagName("img"));
        return img.getAttribute("alt");
    }

    public boolean getUserLinkHref(int userIndex, String username)
    {
        WebElement usernameTd = getUserProperties(userIndex).get(1);
        WebElement link = usernameTd.findElement(By.tagName("a"));
        return link.getAttribute("href").contains("/xwiki/bin/view/XWiki/" + username);
    }

    public String getUsernameLinkText(int userIndex)
    {
        WebElement usernameTd = getUserProperties(userIndex).get(1);
        WebElement link = usernameTd.findElement(By.tagName("a"));
        return link.getText();
    }

    public boolean hasFixedLayout()
    {
        String classAttr = userList.getAttribute("class");
        return classAttr != null && classAttr.contains("fixed-layout");
    }

    public Set<String> getUsernames()
    {
        return userList.findElements(By.cssSelector("tbody tr")).stream()
            .map(row -> row.findElement(By.cssSelector(".xwiki-userlist-user-username a")).getText())
            .collect(Collectors.toSet());
    }
}
