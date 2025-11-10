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
package com.xwiki.macros.confluence.detailssummary.internal.macro;

import java.util.List;

import org.apache.solr.common.SolrDocument;
import org.xwiki.rendering.block.Block;

/**
 * Represents the contextual information for a single table row of the details summary macro.
 *
 * @version $Id$
 * @since 1.29.0
 */
class RowContext
{
    private final SolrDocument document;

    private final String fullName;

    private final List<Block> row;

    RowContext(SolrDocument document, String fullName, List<Block> row)
    {
        this.document = document;
        this.fullName = fullName;
        this.row = row;
    }

    public SolrDocument getDocument()
    {
        return document;
    }

    public String getFullName()
    {
        return fullName;
    }

    public List<Block> getRow()
    {
        return row;
    }
}
