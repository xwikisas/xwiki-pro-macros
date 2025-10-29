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


import static com.xwiki.macros.confluence.internal.cql.CQLUtils.DEFAULT_MAX;

/**
 * Parameter bean class for the DetailsSummaryMacro. This class holds all the parameters that can be configured for the
 * macro.
 * @version $Id$
 * @since 1.29.0
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

    private int max = DEFAULT_MAX;

    private String sort = "";

    private boolean reverse;

    /**
     * @return the unique identifier for the macro instance.
     */
    public String getId()
    {
        return id;
    }

    /**
     * @param id the id to set
     */
    public void setId(String id)
    {
        this.id = id;
    }

    /**
     * @return the headings to display in the macro output.
     */
    public String getHeadings()
    {
        return headings;
    }

    /**
     * @param headings the headings to set
     */
    public void setHeadings(String headings)
    {
        this.headings = headings;
    }

    /**
     * @return the Confluence Query Language (CQL) query string used to filter content.
     */
    public String getCql()
    {
        return cql;
    }

    /**
     * @param cql the Confluence Query Language (CQL) query string used to filter content.
     */
    public void setCql(String cql)
    {
        this.cql = cql;
    }

    /**
     * @return the label to filter pages or content by.
     */
    public String getLabel()
    {
        return label;
    }

    /**
     * @param label the label to filter pages or content by.
     */
    public void setLabel(String label)
    {
        this.label = label;
    }

    /**
     * @return the name of the first column to display in the output.
     */
    public String getFirstcolumn()
    {
        return firstcolumn;
    }

    /**
     * @param firstcolumn the name of the first column to display in the output.
     */
    public void setFirstcolumn(String firstcolumn)
    {
        this.firstcolumn = firstcolumn;
    }

    /**
     * @return true if last modified date should be shown, false otherwise
     */
    public boolean showLastModified()
    {
        return showLastModified;
    }

    /**
     * @param showLastModified true to show last modified date, false otherwise
     */
    public void setShowLastModified(boolean showLastModified)
    {
        this.showLastModified = showLastModified;
    }

    /**
     * @return true if page labels should be shown, false otherwise
     */
    public boolean showPageLabels()
    {
        return showPageLabels;
    }

    /**
     * @param showPageLabels true to show page labels, false otherwise
     */
    public void setShowPageLabels(boolean showPageLabels)
    {
        this.showPageLabels = showPageLabels;
    }

    /**
     * @return true if creator should be shown, false otherwise
     */
    public boolean showCreator()
    {
        return showCreator;
    }

    /**
     * @param showCreator true to show creator, false otherwise
     */
    public void setShowCreator(boolean showCreator)
    {
        this.showCreator = showCreator;
    }

    /**
     * @return the operator used in filtering (e.g., AND, OR).
     */
    public String getOperator()
    {
        return operator;
    }

    /**
     * @param operator the operator to set
     */
    public void setOperator(String operator)
    {
        this.operator = operator;
    }

    /**
     * @return the max number of results
     */
    public int getMax()
    {
        return max;
    }

    /**
     * @param max the max number of results
     */
    public void setMax(int max)
    {
        this.max = max;
    }

    /**
     * @return the sort field
     */
    public String getSort()
    {
        return sort;
    }

    /**
     * @param sort the sort field to set
     */
    public void setSort(String sort)
    {
        this.sort = sort;
    }

    /**
     * @return true if reverse sort, false otherwise
     */
    public boolean getReverse()
    {
        return reverse;
    }

    /**
     * @param reverseSort true to reverse sort, false otherwise
     */
    public void setReverse(boolean reverseSort)
    {
        this.reverse = reverseSort;
    }
}
