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

public class UserListItem extends BaseElement
{
    private final WebElement item;

    public UserListItem(WebElement item)
    {
        this.item = item;
    }

    public String getAvatarTitle()
    {
        WebElement img = item.findElement(By.cssSelector("td.xwiki-userlist-user-avatar img"));
        return img.getAttribute("title");
    }

    public String getAvatarAlt()
    {
        WebElement img = item.findElement(By.cssSelector("td.xwiki-userlist-user-avatar img"));
        return img.getAttribute("alt");
    }

    public String getUsername()
    {
        WebElement link = item.findElement(By.cssSelector("td.xwiki-userlist-user-username a"));
        return link.getText();
    }

    public boolean hasProfileLink(String username)
    {
        WebElement link = item.findElement(By.cssSelector("td.xwiki-userlist-user-username a"));
        return link.getAttribute("href").contains("/xwiki/bin/view/XWiki/" + username);
    }

    public String getPhone()
    {
        return item.findElement(By.cssSelector("td.xwiki-userlist-user-phone")).getText();
    }

    public String getEmail()
    {
        return item.findElement(By.cssSelector("td.xwiki-userlist-user-email a")).getText();
    }

    public boolean isEmailLinkValid(String expectedEmail)
    {
        WebElement emailLink = item.findElement(By.cssSelector("td.xwiki-userlist-user-email a"));
        return ("mailto:" + expectedEmail).equals(emailLink.getAttribute("href"));
    }

    public String getAddress()
    {
        List<WebElement> cells = item.findElements(By.cssSelector("td.xwiki-userlist-user-address"));
        return cells.isEmpty() ? "" : cells.get(0).getText();
    }

    public String getBlogFeed()
    {
        List<WebElement> cells = item.findElements(By.cssSelector("td.xwiki-userlist-user-blogfeed"));
        return cells.isEmpty() ? "" : cells.get(0).getText();
    }

    public List<WebElement> getProperties()
    {
        return item.findElements(By.tagName("td"));
    }

    public List<String> getPropertyTypes()
    {
        return getProperties().stream().map(td -> {
            String classAttr = td.getAttribute("class");
            if (classAttr != null && classAttr.startsWith("xwiki-userlist-user-")) {
                return classAttr.substring("xwiki-userlist-user-".length());
            } else {
                return "";
            }
        }).collect(Collectors.toList());
    }
}
