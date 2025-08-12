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
import java.util.NoSuchElementException;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.xwiki.test.ui.po.BaseElement;

// Represents a single instance of the Team macro and provides access to its attributes.

public class TeamMacro extends BaseElement
{
    private final WebElement team;

    public TeamMacro(WebElement team)
    {
        this.team = team;
    }

    public List<WebElement> getUsers()
    {
        return team.findElements(By.className("xwikiteam-user"));
    }

    public String getUserTitle(String username)
    {
        return getUser(username).getAttribute("title");
    }

    public String getProfileLink(String username)
    {
        return getUser(username).findElement(By.tagName("a")).getAttribute("href");
    }

    public String getAvatarInitials(String username)
    {
        return getUser(username).findElement(By.className("xwikiteam-avatar-initials-letters")).getText();
    }

    public String getAvatarBackgroundColor(String username)
    {
        return getAvatar(username).getCssValue("background-color");
    }

    public String getAvatarFontColor(String username)
    {
        return getUser(username).findElement(By.className("xwikiteam-avatar-initials-letters")).getCssValue("color");
    }

    public String getAvatarSize(String username)
    {
        return getAvatar(username).getCssValue("height");
    }

    public String getAvatarBorderRadius(String username)
    {
        return getAvatar(username).getCssValue("border-radius");
    }

    public boolean isUsernameHidden()
    {
        return team.getAttribute("class").contains("usernames-hidden");
    }

    public boolean hasEmptyTeamMessage()
    {
        return team.getText().equals("There is nobody to show.");
    }

    public boolean hasAvatarInitials(String username)
    {
        return !getUser(username).findElements(By.className("xwikiteam-avatar-initials")).isEmpty();
    }

    public boolean isUsernameVisible(String username)
    {
        WebElement usernameElement = getUser(username).findElement(By.className("xwikiteam-username"));
        return usernameElement.isDisplayed();
    }

    private WebElement getUser(String username)
    {
        return getUsers().stream().filter(user -> username.equals(user.getAttribute("data-username"))).findFirst()
            .orElseThrow(() -> new NoSuchElementException("User not found: " + username));
    }

    private WebElement getAvatar(String username)
    {
        return getUser(username).findElement(By.className("xwikiteam-avatar"));
    }
}
