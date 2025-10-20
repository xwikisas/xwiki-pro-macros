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
 * Represents a Tab Group macro and provides access to its attributes.
 *
 * @version $Id$
 * @since 1.28
 */
public class TabGroupMacro extends BaseElement
{
    private final WebElement tabGroup;

    public TabGroupMacro(String id)
    {
        this.tabGroup = getDriver().findElement(By.id(id));
    }

    public int getTabCount()
    {
        return getTabLinks().size();
    }

    public List<String> getTabLabels()
    {
        return getTabLinks().stream().map(WebElement::getText).collect(Collectors.toList());
    }

    public List<String> getTabIds()
    {
        return getTabLinks().stream().map(this::getTabId).collect(Collectors.toList());
    }

    public boolean isTabListFirst()
    {
        WebElement tabList = tabGroup.findElement(By.cssSelector("ul.nav-tabs"));
        WebElement firstChild = tabGroup.findElement(By.cssSelector(":scope > *:first-child"));
        return tabList.equals(firstChild);
    }

    public String getLocation()
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
        WebElement tabLink =
            getTabLinks().stream().filter(link -> getTabId(link).equals(tabId)).findFirst().orElseThrow();
        tabLink.click();
    }

    public int getNextAfter()
    {
        String attr = tabGroup.getAttribute("data-next-after");
        return Integer.parseInt(attr);
    }

    public boolean isLoopEnabled()
    {
        return Boolean.parseBoolean(tabGroup.getAttribute("data-loop-cards"));
    }

    public String getActiveTabId()
    {
        WebElement activeLi = tabGroup.findElement(By.cssSelector("ul.nav-tabs li.active a"));
        return getTabId(activeLi);
    }

    /*
        This method compares the tabGroup's and the tab's value of NextAfter.
     */
    public int getFinalNextAfter(TabMacro tab)
    {
        int tabNext = tab.getNextAfter();
        if (tabNext > 0) {
            return tabNext;
        }
        return getNextAfter();
    }

    private List<WebElement> getTabs()
    {
        return tabGroup.findElements(By.cssSelector("ul.nav-tabs li"));
    }

    private List<WebElement> getTabLinks()
    {
        return getTabs().stream().map(tab -> tab.findElement(By.tagName("a"))).collect(Collectors.toList());
    }

    private String getTabId(WebElement tabLink)
    {
        return TabMacro.getId(tabLink);
    }
}
