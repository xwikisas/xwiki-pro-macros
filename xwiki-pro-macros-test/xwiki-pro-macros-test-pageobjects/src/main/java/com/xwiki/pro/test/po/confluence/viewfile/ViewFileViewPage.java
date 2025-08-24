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
package com.xwiki.pro.test.po.confluence.viewfile;

import java.util.List;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.xwiki.test.ui.po.ViewPage;

public class ViewFileViewPage extends ViewPage
{
    public String getErrorMessage()
    {
        return getDriver().findElement(By.cssSelector(".box.errormessage")).getText();
    }

    /**
     * @return count of inline calls of view file.
     */
    public int getInlineViewFilesCount()
    {
        return getDriver().findElements(By.cssSelector("span.viewFileThumbnail")).size();
    }

    /**
     * @return count of block calls of view file.
     */
    public int getBlockViewFiles()
    {
        return getDriver().findElements(By.cssSelector("div.viewFileThumbnail")).size();
    }

    /**
     * Preview if the value is created using the office server, generic otherwise.
     *
     * @return
     */
    public String getViewFileThumbnailType()
    {
        List<WebElement> thumbnails = getDriver().findElements(By.cssSelector(".viewfile-thumbnail-image"));

        if (thumbnails.size() == 1) {
            return "preview";
        }
        return "generic";
    }

    public boolean hasLoadedInFullViewMode()
    {
        List<WebElement> fullView = getDriver().findElements(By.cssSelector(".viewFileFull"));
        return fullView.size() > 0;
    }
}
