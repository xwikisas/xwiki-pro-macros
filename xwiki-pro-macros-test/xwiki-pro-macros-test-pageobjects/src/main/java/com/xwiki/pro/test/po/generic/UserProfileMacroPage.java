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
import org.openqa.selenium.support.FindBy;
import org.xwiki.test.ui.po.ViewPage;

public class UserProfileMacroPage extends ViewPage
{
    @FindBy(css = ".xwiki-user-profile-box")
    private List<WebElement> userProfileBoxes;

    @FindBy(css = ".xwiki-user-profile-body")
    private List<WebElement> userProfileBody;

    @FindBy(css = ".xwiki-user-profile-comment")
    private List<WebElement> userProfileComment;

    public int getUserProfileCount()
    {
        return userProfileBoxes.size();
    }

    public boolean linkImageProfile(int index, String username)
    {
        WebElement profileBox = userProfileBoxes.get(index);
        WebElement mediaLeft = profileBox.findElement(By.cssSelector(".media-left a"));
        String href = mediaLeft.getAttribute("href");
        return href != null && href.contains("/xwiki/bin/view/XWiki/" + username);
    }

    public boolean imageHasTitle(int index, String expectedTitle)
    {
        WebElement profileBox = userProfileBoxes.get(index);
        WebElement image = profileBox.findElement(By.cssSelector(".media-left img.avatar"));
        String actualTitle = image.getAttribute("title");
        return expectedTitle.equals(actualTitle);
    }

    public boolean getProfileLinkHref(int index, String username)
    {
        WebElement profileBox = userProfileBoxes.get(index);
        String href = profileBox.findElement(By.cssSelector(".media-heading a")).getAttribute("href");
        return href != null && href.contains("/xwiki/bin/view/XWiki/" + username);
    }

    public String getProfileLinkText(int index)
    {
        WebElement profileBox = userProfileBoxes.get(index);
        return profileBox.findElement(By.cssSelector(".media-heading a")).getText();
    }

    public List<WebElement> getPropertiesList(int index)
    {
        return userProfileBody.get(index).findElement(By.tagName("ul")).findElements(By.tagName("li"));
    }

    public int getPropertiesCount(int index)
    {
        return getPropertiesList(index).size();
    }

    public List<String> getPropertiesText(int index)
    {
        return getPropertiesList(index).stream()
            .map(WebElement::getText)
            .collect(Collectors.toList());
    }

    public boolean isEmailLinkCorrect(int index, int propertyIndex, String expectedEmail)
    {
        WebElement emailElement = getPropertiesList(index).get(propertyIndex);
        WebElement emailLink = emailElement.findElement(By.tagName("a"));
        String href = emailLink.getAttribute("href");
        return href != null && href.equals("mailto:" + expectedEmail);
    }

    public String getProfileComment(int index)
    {
        return userProfileComment.get(index).getText();
    }
}
