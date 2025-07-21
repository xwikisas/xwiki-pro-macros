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

import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.xwiki.test.ui.po.ViewPage;

public class StatusMacroPage extends ViewPage
{
    @FindBy(css = "span.statusBox")
    public List<WebElement> statusBox;

    public String getStatusTitle(int i)
    {
        return statusBox.get(i).getAttribute("title");
    }

    private String extractColorFromClass(String classAttr)
    {
        for (String className : classAttr.split("\\s+")) {
            if (className.endsWith("Status") && !className.equals("statusBox")) {
                return className.replace("Status", "");
            }
        }
        return "unknown";
    }

    public String getStatusColor(int i)
    {
        String classAttr = statusBox.get(i).getAttribute("class");
        return extractColorFromClass(classAttr);
    }

    public boolean isSubtle(int i)
    {
        return statusBox.get(i).getAttribute("class").contains("subtle");
    }
}

