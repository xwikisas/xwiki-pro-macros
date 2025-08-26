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

public class TabGroupMacro extends BaseElement
{
    private WebElement tabGroup;

    public TabGroupMacro(String id)
    {
        this.tabGroup = getDriver().findElement(By.id(id));
    }

    public int getTabCount()
    {
        return tabGroup.findElements(By.cssSelector("ul.nav-tabs li a")).size();
    }

    public List<String> getTabLabels()
    {
        return tabGroup.findElements(By.cssSelector("ul.nav-tabs li a")).stream().map(WebElement::getText)
            .collect(Collectors.toList());
    }

    public List<String> getTabIds()
    {
        return tabGroup.findElements(By.cssSelector("ul.nav-tabs li a")).stream().map(e -> e.getAttribute("href"))
            .map(href -> href.substring(href.indexOf('#') + 1)).collect(Collectors.toList());
    }

    public boolean isTabListFirst()
    {
        WebElement tabList = tabGroup.findElement(By.cssSelector("ul.nav-tabs"));
        WebElement firstChild = tabGroup.findElement(By.cssSelector(":scope > *:first-child"));
        return tabList.equals(firstChild);
    }

    public String getTabLocation()
    {
        String classes = tabGroup.getAttribute("class");
        if (classes.contains("tabs-below")) {
            return "BOTTOM";
        }
        if (classes.contains("tabs-right")) {
            return "RIGHT";
        }
        if (classes.contains("tabs-left")) {
            return "LEFT";
        }
        return "TOP";
    }

    public boolean hasFadeEffect()
    {
        return tabGroup.findElements(By.cssSelector(".tab-pane")).stream()
            .allMatch(pane -> pane.getAttribute("class").contains("fade"));
    }

    public boolean hasCssClass(String cssClass)
    {
        return tabGroup.getAttribute("class").contains(cssClass);
    }

    public void clickTab(String tabId)
    {
        WebElement tabLink = tabGroup.findElement(By.cssSelector("ul[role='tablist'] a[href='#" + tabId + "']"));
        tabLink.click();
    }

    public int getNextAfter()
    {
        String attr = tabGroup.getAttribute("data-next-after");
        return Integer.parseInt(attr);
    }

    public boolean isLoopEnabled()
    {
        return "true".equalsIgnoreCase(tabGroup.getAttribute("data-loop-cards"));
    }

    public boolean isTabContentDisplayed(String tabId, String expectedText)
    {
        WebElement tab = tabGroup.findElement(By.cssSelector(".tab-pane#" + tabId));

        boolean visibleAndActive = tab.isDisplayed() && tab.getAttribute("class").contains("active");
        boolean correctText = tab.getText().equals(expectedText);

        return visibleAndActive && correctText;
    }

    public String getActiveTabId()
    {
        WebElement activeLi = tabGroup.findElement(By.cssSelector("ul.nav-tabs li.active a"));
        String href = activeLi.getAttribute("href");
        return href.substring(href.indexOf('#') + 1);
    }

    public int getEffectDuration(TabMacro tab)
    {
        String style = tab.getCssStyle();
        if (style != null && style.contains("transition-duration")) {
            String value = style.replaceAll(".*transition-duration:\\s*([0-9.]+)s.*", "$1");

            return Integer.parseInt(value.split("\\.")[0]);
        }
        return 0;
    }

    public int getFinalNextAfter(TabMacro tab)
    {
        String attr = tab.getNextAfter();

        if (attr != null && !attr.isEmpty() && !"0".equals(attr)) {
            return Integer.parseInt(attr);
        }
        String groupAttr = tabGroup.getAttribute("data-next-after");
        if (groupAttr != null && !groupAttr.isEmpty()) {
            return Integer.parseInt(groupAttr);
        }
        return 0;
    }
}
