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
import org.xwiki.properties.annotation.PropertyName;

/**
 * confluence_jirachart parameters.
 * @since 1.27.0
 * @version $Id$
 */
public class ConfluenceJiraChartMacroParameters
{
    private String chartType;

    private String server;

    private String jql;

    // Pie Chart
    private String statType;

    // Created Vs Resolved Chart
    private String daysprevious;

    private String periodName;

    private String isCumulative;

    private String showUnresolvedTrend;

    private String versionLabel;

    // Bi Dimensional Grid Chart
    private String xstattype;

    private String ystattype;

    private String numberToShow;

    private String sortDirection;

    private String sortBy;

    /**
     * Get the chart type to show.
     * @return the chart type to show.
     */
    public String getChartType()
    {
        return chartType;
    }

    /**
     * Set the chart type to show.
     * @param chartType the chart type to show.
     */
    @PropertyDescription("The cart type to show")
    @PropertyName("Chart type")
    public void setChartType(String chartType)
    {
        this.chartType = chartType;
    }

    /**
     * Get the server ID from which the info is retrieved.
     * @return the server ID from which the info is retrieved.
     */
    public String getServer()
    {
        return server;
    }

    /**
     * Set the server ID from which the info is retrieved.
     * @param server the server ID from which the info is retrieved.
     */
    @PropertyDescription("The server ID from which the info is retrieved.")
    @PropertyName("Server ID")
    public void setServer(String server)
    {
        this.server = server;
    }

    /**
     * Get the JQL request used to get the info from Jira.
     * @return the JQL request.
     */
    public String getJql()
    {
        return jql;
    }

    /**
     * Set the JQL request used to get the info from Jira.
     * @param jql the JQL request.
     */
    @PropertyDescription("The JQL request used to get the info from Jira")
    @PropertyName("JQL")
    public void setJql(String jql)
    {
        this.jql = jql;
    }

    /**
     * Get the pie chart stat type.
     * @return the stat type.
     */
    public String getStatType()
    {
        return statType;
    }

    /**
     * Set the pie chart stat type.
     * @param statType the stat type.
     */
    @PropertyDescription("The pie chart stat type")
    @PropertyName("Stat type")
    public void setStatType(String statType)
    {
        this.statType = statType;
    }

    /**
     * On the Created vs Resolved Chart, get the days previous.
     * @return the days previous
     */
    public String getDaysprevious()
    {
        return daysprevious;
    }

    /**
     * On the Created vs Resolved Chart, set the days previous.
     * @param daysprevious the days previous
     */
    @PropertyDescription("On the Created vs Resolved Chart, the number of previous days to show the chart")
    @PropertyName("Previous days")
    public void setDaysprevious(String daysprevious)
    {
        this.daysprevious = daysprevious;
    }

    /**
     * On the Created vs Resolved Chart, get the period name.
     * @return the period name.
     */
    public String getPeriodName()
    {
        return periodName;
    }

    /**
     * On the Created vs Resolved Chart, set the period name.
     * @param periodName the period name.
     */
    @PropertyDescription("On the Created vs Resolved Chart, the period name to show")
    @PropertyName("Period name")
    public void setPeriodName(String periodName)
    {
        this.periodName = periodName;
    }

    /**
     * On the Created vs Resolved Chart, get if the chart is cumulative.
     * @return true, if the chart is cumulative.
     */
    public String getIsCumulative()
    {
        return isCumulative;
    }

    /**
     * On the Created vs Resolved Chart, set if the chart is cumulative.
     * @param isCumulative if the chart is cumulative.
     */
    @PropertyDescription("On the Created vs Resolved Chart, if the chart is cumulative")
    @PropertyName("Is cumulative")
    public void setIsCumulative(String isCumulative)
    {
        this.isCumulative = isCumulative;
    }

    /**
     * On the Created vs Resolved Chart, get if the chart show the unresolved trend.
     * @return true, if the chart show the unresolved trend.
     */
    public String getShowUnresolvedTrend()
    {
        return showUnresolvedTrend;
    }

    /**
     * On the Created vs Resolved Chart, set if the chart show the unresolved trend.
     * @param showUnresolvedTrend if the chart show the unresolved trend.
     */
    @PropertyDescription("On the Created vs Resolved Chart, show unresolved trend")
    @PropertyName("Show unresolved tren")
    public void setShowUnresolvedTrend(String showUnresolvedTrend)
    {
        this.showUnresolvedTrend = showUnresolvedTrend;
    }

    /**
     * On the Created vs Resolved Chart, get the version label type.
     * @return the version label type.
     */
    public String getVersionLabel()
    {
        return versionLabel;
    }

    /**
     * On the Created vs Resolved Chart, set the version label type.
     * @param versionLabel the version label type.
     */
    @PropertyDescription("On the Created vs Resolved Chart, the version label type")
    @PropertyName("Version label type")
    public void setVersionLabel(String versionLabel)
    {
        this.versionLabel = versionLabel;
    }

    /**
     * On the Bi Dimensional Grid Chart, get x-axis stat type.
     * @return the x-axis stat type
     */
    public String getXstattype()
    {
        return xstattype;
    }

    /**
     * On the Bi Dimensional Grid Chart, set x-axis stat type.
     * @param xstattype the x-axis stat type
     */
    @PropertyDescription("On the Bi Dimensional Grid Chart, the x-axis stat type")
    @PropertyName("x-axis stat type")
    public void setXstattype(String xstattype)
    {
        this.xstattype = xstattype;
    }

    /**
     * On the Bi Dimensional Grid Chart, get y-axis stat type.
     * @return y-axis stat type.
     */
    public String getYstattype()
    {
        return ystattype;
    }

    /**
     * On the Bi Dimensional Grid Chart, get y-axis stat type.
     * @param ystattype y-axis stat type.
     */
    @PropertyDescription("On the Bi Dimensional Grid Chart, the y-axis stat type")
    @PropertyName("y-axis stat type")
    public void setYstattype(String ystattype)
    {
        this.ystattype = ystattype;
    }

    /**
     * On the Bi Dimensional Grid Chart, get the number of result to show.
     * @return the number of result to show.
     */
    public String getNumberToShow()
    {
        return numberToShow;
    }

    /**
     * On the Bi Dimensional Grid Chart, set the number of result to show.
     * @param numberToShow the number of result to show.
     */
    @PropertyDescription("On the Bi Dimensional Grid Chart, the number of result to show.")
    @PropertyName("Number of result to show")
    public void setNumberToShow(String numberToShow)
    {
        this.numberToShow = numberToShow;
    }

    /**
     * On the Bi Dimensional Grid Chart, get the sort direction.
     * @return the sort direction.
     */
    public String getSortDirection()
    {
        return sortDirection;
    }

    /**
     * On the Bi Dimensional Grid Chart, set the sort direction.
     * @param sortDirection the sort direction.
     */
    @PropertyDescription("On the Bi Dimensional Grid Chart, the sort direction")
    @PropertyName("Sort direction")
    public void setSortDirection(String sortDirection)
    {
        this.sortDirection = sortDirection;
    }

    /**
     * On the Bi Dimensional Grid Chart, get the sort criteria.
     * @return the sort criteria.
     */
    public String getSortBy()
    {
        return sortBy;
    }

    /**
     * On the Bi Dimensional Grid Chart, set the sort criteria.
     * @param sortBy the sort criteria.
     */
    @PropertyDescription("On the Bi Dimensional Grid Chart, the sort criteria")
    @PropertyName("Sort by")
    public void setSortBy(String sortBy)
    {
        this.sortBy = sortBy;
    }
}
