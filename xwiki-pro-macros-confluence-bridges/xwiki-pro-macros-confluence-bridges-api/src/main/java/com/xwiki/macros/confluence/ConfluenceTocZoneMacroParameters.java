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
package com.xwiki.macros.confluence;

import org.xwiki.properties.annotation.PropertyDescription;
import org.xwiki.properties.annotation.PropertyId;
import org.xwiki.stability.Unstable;

/**
 * confluence_toc-zone parameters.
 * 
 * @since 1.22.4
 * @version $Id$
 */
@Unstable
public class ConfluenceTocZoneMacroParameters
{
    private ConfluenceTocZoneMacroLocationParameter location = ConfluenceTocZoneMacroLocationParameter.BOTH;

    private ConfluenceTocZoneMacroTypeParameter type = ConfluenceTocZoneMacroTypeParameter.LIST;

    private Boolean outline = false;

    private String style = "default";

    private String indent = "";

    private String separator = "brackets";

    private int minLevel = 1;

    private int maxLevel = 7;

    private String include = ".*";

    private String exclude = "\\b\\B";

    private boolean printable = true;

    private String cssClass = "";

    /**
     * Gets the location parameter.
     *
     * @return The location parameter.
     */
    public ConfluenceTocZoneMacroLocationParameter getLocation()
    {
        return this.location;
    }

    /**
     * Sets the location.
     * 
     * @param location location parameter.
     */
    @PropertyDescription("Defines where the table of contents should appear around the content.")
    public void setLocation(ConfluenceTocZoneMacroLocationParameter location)
    {
        this.location = location;
    }

    /**
     * Gets the type parameter.
     * 
     * @return The type parameter
     */
    public ConfluenceTocZoneMacroTypeParameter getType()
    {
        return this.type;
    }

    /**
     * Sets the type parameter.
     * 
     * @param type type parameter.
     */
    @PropertyDescription("Defines the layout of the table of contents.")
    public void setType(ConfluenceTocZoneMacroTypeParameter type)
    {
        this.type = type;
    }

    /**
     * Get the outline parameter.
     * 
     * @return the outline parameter.
     */
    public Boolean isOutline()
    {
        return this.outline;
    }

    /**
     * Set the outline parameter.
     * 
     * @param outline the outline parameter.
     */
    @PropertyDescription("Add numbering before the headings in the table of contents.")
    public void setOutline(Boolean outline)
    {
        this.outline = outline;
    }

    /**
     * Get the style parameter.
     * 
     * @return the style parameter.
     */
    public String getStyle()
    {
        return this.style;
    }

    /**
     * Set the style parameter.
     * 
     * @param style the style parameter.
     */
    @PropertyDescription("When in List layout, defines the CSS style of the bullets.")
    public void setStyle(String style)
    {
        this.style = style;
    }

    /**
     * Get the indent parameter.
     * 
     * @return the indent parameter.
     */
    public String getIndent()
    {
        return this.indent;
    }

    /**
     * Set the indent parameter.
     * 
     * @param indent the indent parameter.
     */
    @PropertyDescription("When in List layout, sets the amount by which to indent the headings."
        + " Must be a valid CSS size.")
    public void setIndent(String indent)
    {
        this.indent = indent;
    }

    /**
     * Get the separator parameter.
     * 
     * @return the separator parameter.
     */
    public String getSeparator()
    {
        return this.separator;
    }

    /**
     * Set the separator parameter.
     * 
     * @param separator the separator parameter.
     */
    @PropertyDescription("When in Flat layout, defines how the headings are separated.")
    public void setSeparator(String separator)
    {
        this.separator = separator;
    }

    /**
     * Get the minLevel parameter.
     * 
     * @return the minLevel parameter.
     */
    public int getMinLevel()
    {
        return this.minLevel;
    }

    /**
     * Set the minLevel parameter.
     * 
     * @param minLevel the minLevel parameter.
     */
    @PropertyDescription("Sets the highest level of importance a heading can have to appear in the table of contents.")
    public void setMinLevel(int minLevel)
    {
        this.minLevel = minLevel;
    }

    /**
     * Get the maxLevel parameter.
     * 
     * @return the maxLevel parameter.
     */
    public int getMaxLevel()
    {
        return this.maxLevel;
    }

    /**
     * Set the maxLevel parameter.
     * 
     * @param maxLevel the maxLevel parameter.
     */
    @PropertyDescription("Sets the lowest level of importance a heading can have to appear in the table of contents.")
    public void setMaxLevel(int maxLevel)
    {
        this.maxLevel = maxLevel;
    }

    /**
     * Get the include parameter.
     * 
     * @return the include parameter.
     */
    public String getInclude()
    {
        return this.include;
    }

    /**
     * Set the include parameter.
     * 
     * @param include the include parameter.
     */
    @PropertyDescription("Sets a regular expression that headings must match to appear in the table of contents.")
    public void setInclude(String include)
    {
        this.include = include;
    }

    /**
     * Get the exclude parameter.
     * 
     * @return the exclude parameter.
     */
    public String getExclude()
    {
        return this.exclude;
    }

    /**
     * Set the exclude parameter.
     * 
     * @param exclude the exclude parameter.
     */
    @PropertyDescription("Sets a regular expression that headings shouldn't match to appear in the table of contents.")
    public void setExclude(String exclude)
    {
        this.exclude = exclude;
    }

    /**
     * Get the printable parameter.
     * 
     * @return the printable parameter.
     */
    public boolean isPrintable()
    {
        return this.printable;
    }

    /**
     * Set the printable parameter.
     * 
     * @param printable the printable parameter.
     */
    @PropertyDescription("When unset, the table of content will not appear in printed versions of the page.")
    public void setPrintable(boolean printable)
    {
        this.printable = printable;
    }

    /**
     * Get the class parameter.
     * 
     * @return the class parameter.
     */
    public String getCssClass()
    {
        return this.cssClass;
    }

    /**
     * Set the class parameter.
     * 
     * @param cssClass the class parameter.
     */
    // class is a property of every Object.
    @PropertyId("class")
    @PropertyDescription("When set, a div with the specificied class will wrap the table of contents"
        + ", allowing for custom styling.")
    public void setCssClass(String cssClass)
    {
        this.cssClass = cssClass;
    }
}
