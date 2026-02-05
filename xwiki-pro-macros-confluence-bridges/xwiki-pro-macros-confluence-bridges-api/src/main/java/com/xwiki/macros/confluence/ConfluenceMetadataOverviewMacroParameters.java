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

/**
 * confluence_metadata-overview parameters.
 * @since 1.30.0
 * @version $Id$
 */
public class ConfluenceMetadataOverviewMacroParameters
{
    private String cql;
    private String showfilter;
    private String filterfields;

    /**
     * @return the cql query
     */
    public String getCql()
    {
        return cql;
    }

    /**
     * @param cql the cql query to set
     */
    public void setCql(String cql)
    {
        this.cql = cql;
    }

    /**
     * @return the showfilter
     */
    public String getShowfilter()
    {
        return showfilter;
    }

    /**
     * @param showfilter the showfilter
     */
    public void setShowfilter(String showfilter)
    {
        this.showfilter = showfilter;
    }

    /**
     * @return the filterfields
     */
    public String getFilterfields()
    {
        return filterfields;
    }

    /**
     * @param filterfields the filterfields
     */
    public void setFilterfields(String filterfields)
    {
        this.filterfields = filterfields;
    }

}
