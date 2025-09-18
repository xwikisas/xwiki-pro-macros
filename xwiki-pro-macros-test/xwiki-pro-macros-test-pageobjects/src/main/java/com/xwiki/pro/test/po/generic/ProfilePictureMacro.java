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
 * Represents a Profile Picture macro and provides access to its attributes.
 *
 * @version $Id$
 * @since 1.28
 */
public class ProfilePictureMacro extends BaseElement
{
    private final WebElement profilePicture;

    public ProfilePictureMacro(WebElement profilePicture)
    {
        this.profilePicture = profilePicture;
    }

    public String getUserTitle()
    {
        return profilePicture.findElement(By.cssSelector(".xwikiteam-user")).getAttribute("title");
    }

    public boolean linkContainsUsername(String expectedUsername)
    {
        String href = profilePicture.findElement(By.cssSelector(".xwikiteam-user a")).getAttribute("href");
        return href != null && href.contains("/xwiki/bin/view/XWiki/" + expectedUsername);
    }

    public String getAvatarSize()
    {
        WebElement avatarElement = profilePicture.findElement(By.cssSelector(".xwikiteam-avatar"));
        return avatarElement.getCssValue("height");
    }

    public boolean hasProfileImage()
    {
        WebElement avatarElement = profilePicture.findElement(By.cssSelector(".xwikiteam-avatar"));
        return "img".equals(avatarElement.getTagName());
    }

    public boolean hasAvatarInitials()
    {

        WebElement avatarElement = profilePicture.findElement(By.cssSelector(".xwikiteam-avatar"));
        return avatarElement.getAttribute("class").contains("xwikiteam-avatar-initials");
    }
}
