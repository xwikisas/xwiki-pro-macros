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
import org.openqa.selenium.support.FindBy;
import org.xwiki.test.ui.po.ViewPage;

/**
 * Represents a page containing one or more Team macros.
 *
 * @version $Id$
 * @since 1.25.2
 */
public class TeamMacroPage extends ViewPage
{
    @FindBy(css = ".xwikiteam")
    private List<WebElement> teamMacros;

    public int getTeamMacrosCount()
    {
        return teamMacros.size();
    }

    public List<WebElement> getTeamMacroUsers(int i)
    {
        return teamMacros.get(i).findElements(By.className("xwikiteam-user"));
    }

    public String getUserTitle(int macroIndex, String dataUsername)
    {
        return getUserByUsername(macroIndex, dataUsername).getAttribute("title");
    }

    public String getProfileLink(int macroIndex, String dataUsername)
    {
        return getUserByUsername(macroIndex, dataUsername)
            .findElement(By.tagName("a")).getAttribute("href");
    }

    public String getAvatarInitials(int macroIndex, String dataUsername)
    {
        return getUserByUsername(macroIndex, dataUsername)
            .findElement(By.className("xwikiteam-avatar-initials-letters")).getText();
    }

    public String getAvatarBackgroundColor(int macroIndex, String dataUsername)
    {
        return getUserByUsername(macroIndex, dataUsername)
            .findElement(By.className("xwikiteam-avatar")).getCssValue("background-color");
    }

    public String getAvatarFontColor(int macroIndex, String dataUsername)
    {
        return getUserByUsername(macroIndex, dataUsername)
            .findElement(By.className("xwikiteam-avatar-initials-letters")).getCssValue("color");
    }

    public String getAvatarSize(int macroIndex, String dataUsername)
    {
        return getUserByUsername(macroIndex, dataUsername)
            .findElement(By.className("xwikiteam-avatar")).getCssValue("height");
    }

    public String getAvatarBorderRadius(int macroIndex, String dataUsername)
    {
        return getUserByUsername(macroIndex, dataUsername)
            .findElement(By.className("xwikiteam-avatar")).getCssValue("border-radius");
    }

    private WebElement getUserByUsername(int macroIndex, String dataUsername)
    {
        return teamMacros.get(macroIndex)
            .findElements(By.className("xwikiteam-user"))
            .stream()
            .filter(user -> dataUsername.equals(user.getAttribute("data-username")))
            .findFirst()
            .orElseThrow(() -> new NoSuchElementException("User not found: " + dataUsername));
    }

    public boolean isUsernameHidden(int macroIndex)
    {
        WebElement container = teamMacros.get(macroIndex);
        String classAttr = container.getAttribute("class");
        return classAttr.contains("usernames-hidden");
    }

    public boolean hasEmptyTeamMessage(int macroIndex)
    {
        return teamMacros.get(macroIndex).getText().equals("There is nobody to show.");
    }

    public boolean hasAvatarInitials(int macroIndex, String dataUsername)
    {
        WebElement user = getUserByUsername(macroIndex, dataUsername);
        return !user.findElements(By.className("xwikiteam-avatar-initials")).isEmpty();
    }

    public boolean isUsernameVisible(int macroIndex, String dataUsername)
    {
        WebElement user = getUserByUsername(macroIndex, dataUsername);
        WebElement usernameElement = user.findElement(By.className("xwikiteam-username"));
        return usernameElement.isDisplayed();
    }
}
