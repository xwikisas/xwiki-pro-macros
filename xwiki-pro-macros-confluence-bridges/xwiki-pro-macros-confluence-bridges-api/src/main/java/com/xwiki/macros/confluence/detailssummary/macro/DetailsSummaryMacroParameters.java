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
package com.xwiki.macros.confluence.detailssummary.macro;

/**
 * Parameter bean class for the DetailsSummaryMacro. This class holds all the parameters that can be configured for the
 * macro.
 * @version $Id$
 * @since 1.28.3
 */
public class DetailsSummaryMacroParameters
{
    private String id = "";

    private String headings = "";

    private String cql = "";

    private String label = "";

    private String firstcolumn = "";

    private boolean showLastModified;

    private boolean showPageLabels;

    private boolean showCreator;

    private String operator = "OR";

    private int max = 1000;

    private String sort = "";

    private boolean reverse;

    /**
     * Returns the unique identifier for the macro instance.
     *
     * @return the id
     */
    public String getId()
    {
        return id;
    }

    /**
     * Sets the unique identifier for the macro instance.
     *
     * @param id the id to set
     */
    public void setId(String id)
    {
        this.id = id;
    }

    /**
     * Returns the headings to display in the macro output.
     *
     * @return the headings
     */
    public String getHeadings()
    {
        return headings;
    }

    /**
     * Sets the headings to display in the macro output.
     *
     * @param headings the headings to set
     */
    public void setHeadings(String headings)
    {
        this.headings = headings;
    }

    /**
     * Returns the Confluence Query Language (CQL) query string used to filter content.
     *
     * @return the CQL query
     */
    public String getCql()
    {
        return cql;
    }

    /**
     * Sets the Confluence Query Language (CQL) query string used to filter content.
     *
     * @param cql the CQL query to set
     */
    public void setCql(String cql)
    {
        this.cql = cql;
    }

    /**
     * Returns the label to filter pages or content by.
     *
     * @return the label
     */
    public String getLabel()
    {
        return label;
    }

    /**
     * Sets the label to filter pages or content by.
     *
     * @param label the label to set
     */
    public void setLabel(String label)
    {
        this.label = label;
    }

    /**
     * Returns the name of the first column to display in the output.
     *
     * @return the first column
     */
    public String getFirstcolumn()
    {
        return firstcolumn;
    }

    /**
     * Sets the name of the first column to display in the output.
     *
     * @param firstcolumn the first column to set
     */
    public void setFirstcolumn(String firstcolumn)
    {
        this.firstcolumn = firstcolumn;
    }

    /**
     * Indicates whether the last modified date should be displayed.
     *
     * @return true if last modified date should be shown, false otherwise
     */
    public boolean showLastModified()
    {
        return showLastModified;
    }

    /**
     * Sets whether the last modified date should be displayed.
     *
     * @param showLastModified true to show last modified date, false otherwise
     */
    public void setShowLastModified(boolean showLastModified)
    {
        this.showLastModified = showLastModified;
    }

    /**
     * Indicates whether page labels should be displayed.
     *
     * @return true if page labels should be shown, false otherwise
     */
    public boolean showPageLabels()
    {
        return showPageLabels;
    }

    /**
     * Sets whether page labels should be displayed.
     *
     * @param showPageLabels true to show page labels, false otherwise
     */
    public void setShowPageLabels(boolean showPageLabels)
    {
        this.showPageLabels = showPageLabels;
    }

    /**
     * Indicates whether the creator of the page should be displayed.
     *
     * @return true if creator should be shown, false otherwise
     */
    public boolean showCreator()
    {
        return showCreator;
    }

    /**
     * Sets whether the creator of the page should be displayed.
     *
     * @param showCreator true to show creator, false otherwise
     */
    public void setShowCreator(boolean showCreator)
    {
        this.showCreator = showCreator;
    }

    /**
     * Returns the operator used in filtering (e.g., AND, OR).
     *
     * @return the operator
     */
    public String getOperator()
    {
        return operator;
    }

    /**
     * Sets the operator used in filtering (e.g., AND, OR).
     *
     * @param operator the operator to set
     */
    public void setOperator(String operator)
    {
        this.operator = operator;
    }

    /**
     * Returns the maximum number of results to display.
     *
     * @return the max number of results
     */
    public int getMax()
    {
        return max;
    }

    /**
     * Sets the maximum number of results to display.
     *
     * @param max the max number of results
     */
    public void setMax(int max)
    {
        this.max = max;
    }

    /**
     * Returns the field by which results should be sorted.
     *
     * @return the sort field
     */
    public String getSort()
    {
        return sort;
    }

    /**
     * Sets the field by which results should be sorted.
     *
     * @param sort the sort field to set
     */
    public void setSort(String sort)
    {
        this.sort = sort;
    }

    /**
     * Indicates whether the sorting of the {@link #sort} field should be reversed.
     *
     * @return true if reverse sort, false otherwise
     */
    public boolean getReverse()
    {
        return reverse;
    }

    /**
     * Sets whether the sorting of the {@link #sort} field should be reversed.
     *
     * @param reverseSort true to reverse sort, false otherwise
     */
    public void setReverse(boolean reverseSort)
    {
        this.reverse = reverseSort;
    }
}
