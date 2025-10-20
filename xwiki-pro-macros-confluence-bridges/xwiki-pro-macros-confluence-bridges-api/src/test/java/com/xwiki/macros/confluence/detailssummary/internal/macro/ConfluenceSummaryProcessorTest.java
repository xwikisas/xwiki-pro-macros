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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Provider;

import org.apache.solr.common.SolrInputDocument;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.xwiki.model.reference.DocumentReference;
import org.xwiki.model.reference.DocumentReferenceResolver;
import org.xwiki.model.validation.EntityNameValidation;
import org.xwiki.model.validation.EntityNameValidationConfiguration;
import org.xwiki.model.validation.EntityNameValidationManager;
import org.xwiki.rendering.block.Block;
import org.xwiki.rendering.block.MacroBlock;
import org.xwiki.rendering.block.TableCellBlock;
import org.xwiki.rendering.block.TableRowBlock;
import org.xwiki.rendering.block.WordBlock;
import org.xwiki.rendering.block.XDOM;
import org.xwiki.rendering.renderer.BlockRenderer;
import org.xwiki.test.junit5.mockito.ComponentTest;
import org.xwiki.test.junit5.mockito.InjectMockComponents;
import org.xwiki.test.junit5.mockito.MockComponent;

import com.xpn.xwiki.XWiki;
import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.XWikiException;
import com.xpn.xwiki.doc.XWikiDocument;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link ConfluenceSummaryProcessor}
 */
@ComponentTest
public class ConfluenceSummaryProcessorTest
{
    @InjectMockComponents
    private ConfluenceSummaryProcessor confluenceSummaryProcessor;

    @MockComponent
    private Provider<XWikiContext> contextProvider;

    @MockComponent
    @Named("plain/1.0")
    private BlockRenderer plainTextRenderer;
    @BeforeEach
    void setUp()
    {
        this.confluenceSummaryProcessor = spy(this.confluenceSummaryProcessor);
    }

    @Test
    void parseHeadingsEmpty()
    {
        assertTrue(confluenceSummaryProcessor.parseHeadings("").isEmpty());
        assertTrue(confluenceSummaryProcessor.parseHeadings(null).isEmpty());
    }

    @Test
    void parseHeadingsSimple()
    {
        assertEquals(List.of("H1", "H2"), confluenceSummaryProcessor.parseHeadings("H1,H2"));
    }

    @Test
    void parseHeadingsWithQuotesAndEscapes()
    {
        assertEquals(List.of("H1,Part", "H2"), confluenceSummaryProcessor.parseHeadings("\"H1,Part\",H2"));
        assertEquals(List.of("A\"Quoted\"", "B"), confluenceSummaryProcessor.parseHeadings("\"A\\\"Quoted\\\"\",B"));
    }
    @Test
    void maybeSortAscending() {
        // Arrange
        List<String> columnsLower = List.of("col1");
        List<Block> rows = new ArrayList<>();

        // Create row1: col1 = "b"
        TableCellBlock col1Row1 = new TableCellBlock(List.of(new WordBlock("b")));
        TableRowBlock row1 = new TableRowBlock(List.of(col1Row1));

        // Create row2: col1 = "a"
        TableCellBlock col1Row2 = new TableCellBlock(List.of(new WordBlock("a")));
        TableRowBlock row2 = new TableRowBlock(List.of(col1Row2));

        rows.add(row1);
        rows.add(row2);

        // Stub blockToString so it returns text from the WordBlock directly
        doReturn("b").when(confluenceSummaryProcessor).blockToString(col1Row1);
        doReturn("a").when(confluenceSummaryProcessor).blockToString(col1Row2);

        // Act
        confluenceSummaryProcessor.maybeSort("col1", false, columnsLower, rows);

        // Assert
        assertEquals(List.of(row2, row1), rows);
    }
}
