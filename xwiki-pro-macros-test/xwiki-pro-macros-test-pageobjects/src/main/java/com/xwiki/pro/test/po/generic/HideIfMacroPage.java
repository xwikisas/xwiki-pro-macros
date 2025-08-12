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

public class HideIfMacroPage
{
    @FindBy(id = "xwikicontent")
    private WebElement macroContent;

    public boolean isParagraphVisible(String paragraphText)
    {

        WebElement paragraph = macroContent.findElement(By.xpath(".//p[contains(text(), '" + paragraphText + "')]"));
        return paragraph.isDisplayed();
    }

    public boolean isImageVisible(String altText)
    {

        WebElement image = macroContent.findElement(By.xpath(".//img[@alt='" + altText + "']"));
        return image.isDisplayed();
    }

    public List<WebElement> getAllVisibleParagraphs()
    {
        return macroContent.findElements(By.tagName("p")).stream().filter(WebElement::isDisplayed)
            .collect(Collectors.toList());
    }

    public List<WebElement> getAllVisibleImages()
    {
        return macroContent.findElements(By.tagName("img")).stream().filter(WebElement::isDisplayed)
            .collect(Collectors.toList());
    }
}
