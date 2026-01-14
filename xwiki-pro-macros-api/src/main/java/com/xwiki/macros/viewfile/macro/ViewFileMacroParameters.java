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
package com.xwiki.macros.viewfile.macro;

import org.apache.commons.lang3.StringUtils;
import org.xwiki.model.reference.DocumentReference;
import org.xwiki.properties.annotation.PropertyDescription;
import org.xwiki.properties.annotation.PropertyDisplayHidden;
import org.xwiki.properties.annotation.PropertyDisplayType;
import org.xwiki.properties.annotation.PropertyId;
import org.xwiki.properties.annotation.PropertyName;

import com.xwiki.macros.viewfile.ViewFileResourceReference;

/**
 * Parameter bean class for {@link com.xwiki.macros.viewfile.internal.macro.ViewFileMacro}.
 *
 * @version $Id$
 * @since 1.27
 */
public class ViewFileMacroParameters
{
    private String name;

    private String page;

    private ViewFileDisplay display = ViewFileDisplay.thumbnail;

    private String width;

    private String height;

    private String attFilename;

    private String csvFormat;

    private String csvDelimiter;

    private boolean csvFirstLineIsHeader = true;

    /**
     * @return the name of the attachment.
     */
    public String getName()
    {
        return name;
    }

    /**
     * Set the attachment name.
     *
     * @param name the name of the attachment.
     */
    @PropertyDisplayType(ViewFileResourceReference.class)
    @PropertyName("File name")
    public void setName(String name)
    {
        this.name = name;
    }

    /**
     * @return the source document of the attachments.
     */
    public String getPage()
    {
        return page;
    }

    /**
     * Set the source document for the attachments.
     *
     * @param page the source document of the attachments.
     */
    @PropertyDisplayType(DocumentReference.class)
    public void setPage(String page)
    {
        this.page = page;
    }

    /**
     * @return the display type.
     */
    public ViewFileDisplay getDisplay()
    {
        return display;
    }

    /**
     * Set the display type.
     *
     * @param display the display type.
     */
    public void setDisplay(ViewFileDisplay display)
    {
        this.display = display;
    }

    /**
     * @return the width of the display window.
     */
    public String getWidth()
    {
        return width != null ? width : "";
    }

    /**
     * Set the width of the display window.
     *
     * @param width the width of the display window.
     */
    public void setWidth(String width)
    {
        this.width = width;
    }

    /**
     * @return the height of the display window.
     */
    public String getHeight()
    {
        return height != null ? height : "";
    }

    /**
     * Set the height of the display window.
     *
     * @param height the height of the display window.
     */
    public void setHeight(String height)
    {
        this.height = height;
    }

    /**
     * @return the attachment filename.
     */
    public String getAttFilename()
    {
        return attFilename;
    }

    /**
     * Alias of File name used for compatibility reasons.
     *
     * @param attFilename the attachment filename.
     */
    @PropertyId("att--filename")
    @PropertyName("att--filename")
    @PropertyDisplayHidden
    @PropertyDescription("Alias of name (here for compatibility reasons)")
    public void setAttFilename(String attFilename)
    {
        this.attFilename = attFilename;
    }

    /**
     * Set the CSV format to use for CSV files.
     * @param csvFormat the CSV format
     * @since 1.30.0
     */
    @PropertyId("csvFormat")
    @PropertyName("CSV Format")
    @PropertyDescription("Which format to use when displaying a CSV file (possible values: "
        + "Default, Excel, InformixUnload, InformixUnloadCsv, MongoDBCsv, "
        + "MongoDBTsv, MySQL, Oracle, PostgreSQLCsv, PostgreSQLText, RFC4180, TDF"
        + ")")
    public void setCSVFormat(String csvFormat)
    {
        this.csvFormat = csvFormat;
    }

    /**
     * @return the format to use for CSV files
     * @since 1.30.0
     */
    public String getCSVFormat()
    {
        return StringUtils.defaultString(csvFormat);
    }

    /**
     * @return the delimiter to use for CSV files
     * @since 1.30.0
     */
    public String getCSVDelimiter()
    {
        return StringUtils.defaultString(csvDelimiter);
    }

    /**
     * Set the CSV delimiter to use for CSV files.
     * @param csvDelimiter the CSV delimiter
     * @since 1.30.0
     */
    @PropertyId("csvDelimiter")
    @PropertyName("CSV Delimiter")
    @PropertyDescription("Which delimiter to use when displaying a CSV file. "
            + "Overrides the CSV format if provided as well. If nothing is provided, ',' is used.")
    public void setCSVDelimiter(String csvDelimiter)
    {
        this.csvDelimiter = csvDelimiter;
    }

    /**
     * @return whether the first line is to be considered the header for CSV files
     * @since 1.30.0
     */
    public boolean getCSVFirstLineIsHeader()
    {
        return this.csvFirstLineIsHeader;
    }

    /**
     * Set whether the first line is to be considered the header for CSV files.
     * @param csvFirstLineIsHeader the CSV delimiter
     * @since 1.30.0
     */
    @PropertyId("csvFirstLineIsHeader")
    @PropertyName("CSV first line is header")
    @PropertyDescription("Whether the first line is to be considered the header when displaying a CSV file")
    public void setCSVFirstLineIsHeader(boolean csvFirstLineIsHeader)
    {
        this.csvFirstLineIsHeader = csvFirstLineIsHeader;
    }
}
