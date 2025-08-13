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

/**
 * Represents a UserProfile macro and provides access to its attributes.
 *
 * @version $Id$
 * @since 1.28
 */
public class UserProfileMacro extends BaseElement
{
    private final WebElement userProfile;

    public UserProfileMacro(WebElement container)
    {
        this.userProfile = container;
    }

    public boolean hasLinkImage(String username)
    {
        WebElement mediaLeft = userProfile.findElement(By.cssSelector(".media-left a"));
        String href = mediaLeft.getAttribute("href");
        return href != null && href.contains("/xwiki/bin/view/XWiki/" + username);
    }

    public boolean imageHasTitle(String expectedTitle)
    {
        WebElement image = userProfile.findElement(By.cssSelector(".media-left img.avatar"));
        String actualTitle = image.getAttribute("title");
        return expectedTitle.equals(actualTitle);
    }

    public boolean getProfileLink(String username)
    {
        String href = userProfile.findElement(By.cssSelector(".media-heading a")).getAttribute("href");
        return href != null && href.contains("/xwiki/bin/view/XWiki/" + username);
    }

    public String getLinkText()
    {
        return userProfile.findElement(By.cssSelector(".media-heading a")).getText();
    }

    public List<WebElement> getPropertiesList()
    {
        return userProfile.findElement(By.cssSelector(".xwiki-user-profile-body ul")).findElements(By.tagName("li"));
    }

    public int getPropertiesCount()
    {
        return getPropertiesList().size();
    }

    public List<String> getPropertiesText()
    {
        return getPropertiesList().stream().map(WebElement::getText).collect(Collectors.toList());
    }

    public boolean isEmailLinkCorrect(int propertyIndex, String expectedEmail)
    {
        WebElement emailElement = getPropertiesList().get(propertyIndex);
        WebElement emailLink = emailElement.findElement(By.tagName("a"));
        String href = emailLink.getAttribute("href");
        return href != null && href.equals("mailto:" + expectedEmail);
    }

    public String getComment()
    {
        return userProfile.findElement(By.cssSelector(".xwiki-user-profile-comment")).getText();
    }
}
