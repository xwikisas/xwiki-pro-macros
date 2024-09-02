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

import org.xwiki.model.reference.DocumentReference;
import org.xwiki.properties.annotation.PropertyDescription;
import org.xwiki.properties.annotation.PropertyName;
import org.xwiki.stability.Unstable;

/**
 * confluence_outgoing-links parameters.
 * @since 1.22.0
 * @version $Id$
 */
@Unstable
public class ConfluenceOutgoingLinksMacroParameters
{
    private String spaces;

    /**
     * Retrieves the reference to the document from which the excerpt is to be included.
     * @since 1.14.5
     *
     * @return A {@link DocumentReference} object representing the document reference.
     */
    public String getSpaces()
    {
        return this.spaces;
    }

    /**
     * Set the spaces outgoing-links should restrict l  inks to.
     * @param spaces the spaces to set, separated by commas.
     */
    @PropertyDescription("Restrict displayed links to these spaces")
    @PropertyName("Spaces")
    public void setSpaces(String spaces)
    {
        this.spaces = spaces;
    }
}
