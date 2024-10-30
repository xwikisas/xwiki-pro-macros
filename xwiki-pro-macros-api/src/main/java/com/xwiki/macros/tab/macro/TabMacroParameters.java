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
import org.xwiki.properties.annotation.PropertyId;
import org.xwiki.properties.annotation.PropertyMandatory;

import com.xwiki.macros.tab.internal.TabMacro;

/**
 * Parameters for {@link TabMacro}.
 *
 * @version $Id: $
 * @since 1.24.0
 */
public class TabMacroParameters
{
    /**
     * Parameter name default.
     */
    public static final String PARAM_NAME_DEFAULT = "default";

    private String label;

    private String id;

    private boolean showByDefault;

    private String cssClass;

    private String cssStyle;

    private int nextAfter;

    private TransitionEffect effect = TransitionEffect.NONE;

    private int effectDuration;

    /**
     * @return the label of this tab.
     */
    public String getLabel()
    {
        return label;
    }

    /**
     * @param label the label of this tab.
     */
    @PropertyDescription("Label for the tab")
    @PropertyMandatory
    public void setLabel(String label)
    {
        this.label = label;
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
    @PropertyDescription("A unique ID for this tab.")
    public void setId(String id)
    {
        this.id = id;
    }

    /**
     * @return define if it's the element to show by default.
     */
    public boolean isShowByDefault()
    {
        return showByDefault;
    }

    /**
     * @param showByDefault define if it's the element to show by default.
     */
    @PropertyDescription("If true, this tab will be the first show by default.")
    @PropertyId(PARAM_NAME_DEFAULT)
    public void setShowByDefault(boolean showByDefault)
    {
        this.showByDefault = showByDefault;
    }

    /**
     * @return the css class for this tab.
     */
    public String getCssClass()
    {
        return cssClass;
    }

    /**
     * @param cssClass the css class for this tab.
     */
    @PropertyDescription("The custom CSS class for the tab.")
    public void setCssClass(String cssClass)
    {
        this.cssClass = cssClass;
    }

    /**
     * @return custom css rules.
     */
    public String getCssStyle()
    {
        return cssStyle;
    }

    /**
     * @param cssStyle custom css rules.
     */
    @PropertyDescription("Custom CSS style values.")
    public void setCssStyle(String cssStyle)
    {
        this.cssStyle = cssStyle;
    }

    /**
     * @return The number of seconds this tab will stay visible before moving to the next one.
     */
    public int getNextAfter()
    {
        return nextAfter;
    }

    /**
     * @param nextAfter The number of seconds this tab will stay visible before moving to the next one.
     */
    @PropertyDescription("The number of seconds this tab will stay visible before moving to the next one. "
        + "If not set, no transition will be done automatically.")
    public void setNextAfter(int nextAfter)
    {
        this.nextAfter = nextAfter;
    }

    /**
     * @return The transition animation for this tab.
     */
    public TransitionEffect getEffect()
    {
        return effect;
    }

    /**
     * @param effect The transition animation for this tab.
     */
    @PropertyDescription("The transition animation for this tab.")
    public void setEffect(TransitionEffect effect)
    {
        this.effect = effect;
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
