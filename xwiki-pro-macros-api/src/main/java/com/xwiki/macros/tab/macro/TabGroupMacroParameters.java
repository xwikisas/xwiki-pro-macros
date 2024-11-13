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
package com.xwiki.macros.tab.macro;

import org.xwiki.properties.annotation.PropertyDescription;

import com.xwiki.macros.tab.internal.TabGroupMacro;

/**
 * Parameters for {@link TabGroupMacro}.
 *
 * @version $Id: $
 * @since 1.24.0
 */
public class TabGroupMacroParameters
{
    private String id;

    private Location tabLocation;

    private String width;

    private String height;

    private String cssClass;

    private int nextAfter;

    private boolean loopCards;

    private TransitionEffect effectType = TransitionEffect.NONE;

    private int effectDuration;

    /**
     * @version $Id: $
     */
    public enum Location
    {
        /**
         * Top of the content.
         */
        TOP,
        /**
         * Bottom of the content.
         */
        BOTTOM,
        /**
         * Left of the content.
         */
        LEFT,
        /**
         * Right of the content.
         */
        RIGHT,
        /**
         * Don't show tap part.
         */
        NONE
    }

    /**
     * @return the id of the element.
     */
    public String getId()
    {
        return id;
    }

    /**
     * @param id the id of the element.
     */
    @PropertyDescription("The id of the element.")
    public void setId(String id)
    {
        this.id = id;
    }

    /**
     * @return the tabs location related to the macro content.
     */
    @PropertyDescription("The location of the element.")
    public Location getTabLocation()
    {
        return tabLocation;
    }

    /**
     * @param tabLocation the tabs location related to the macro content.
     */
    public void setTabLocation(Location tabLocation)
    {
        this.tabLocation = tabLocation;
    }

    /**
     * @return with of the macro.
     */
    @PropertyDescription("The width of the main element with the CSS unit.")
    public String getWidth()
    {
        return width;
    }

    /**
     * @param width of the macro.
     */
    public void setWidth(String width)
    {
        this.width = width;
    }

    /**
     * @return height of the macro.
     */
    public String getHeight()
    {
        return height;
    }

    /**
     * @param height height of the macro.
     */
    @PropertyDescription("The height of the main element with the CSS unit.")
    public void setHeight(String height)
    {
        this.height = height;
    }

    /**
     * @return the css class to add on the main element.
     */
    public String getCssClass()
    {
        return cssClass;
    }

    /**
     * @param cssClass the css class to add on the main element.
     */
    @PropertyDescription("A custom css class to decorate the tabs.")
    public void setCssClass(String cssClass)
    {
        this.cssClass = cssClass;
    }

    /**
     * @return The number of seconds the tab will stay visible before moving to the next one.
     */
    public int getNextAfter()
    {
        return nextAfter;
    }

    /**
     * @param nextAfter The number of seconds the tab will stay visible before moving to the next one.
     */
    @PropertyDescription("The number of seconds the tab will stay visible before moving to the next one. "
        + "If not set, no transition will be done automatically.")
    public void setNextAfter(int nextAfter)
    {
        this.nextAfter = nextAfter;
    }

    /**
     * @return If true, the tab will loop back to the beginning from the last tab.
     */
    public boolean isLoopCards()
    {
        return loopCards;
    }

    /**
     * @param loopCards If true, the tab will loop back to the beginning from the last tab.
     */
    @PropertyDescription("If true, the tab will loop back to the beginning from the last tab.")
    public void setLoopCards(boolean loopCards)
    {
        this.loopCards = loopCards;
    }

    /**
     * @return The transition animation between each tab.
     */
    public TransitionEffect getEffectType()
    {
        return effectType;
    }

    /**
     * @param effectType The transition animation between each tab.
     */
    @PropertyDescription("The transition animation between each tab.")
    public void setEffectType(TransitionEffect effectType)
    {
        this.effectType = effectType;
    }

    /**
     * @return The transition animation duration.
     */
    public int getEffectDuration()
    {
        return effectDuration;
    }

    /**
     * @param effectDuration The transition animation duration.
     */
    @PropertyDescription("The transition animation duration to apply on change of tab.")
    public void setEffectDuration(int effectDuration)
    {
        this.effectDuration = effectDuration;
    }
}
