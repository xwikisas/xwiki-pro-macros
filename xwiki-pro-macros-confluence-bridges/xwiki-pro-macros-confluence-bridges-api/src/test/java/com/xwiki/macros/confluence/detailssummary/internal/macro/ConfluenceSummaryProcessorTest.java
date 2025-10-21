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
import java.util.List;

import javax.inject.Named;
import javax.inject.Provider;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.xwiki.component.util.ReflectionUtils;
import org.xwiki.model.EntityType;
import org.xwiki.model.reference.EntityReference;
import org.xwiki.model.reference.EntityReferenceResolver;
import org.xwiki.rendering.block.Block;
import org.xwiki.rendering.block.TableCellBlock;
import org.xwiki.rendering.block.TableRowBlock;
import org.xwiki.rendering.block.WordBlock;
import org.xwiki.rendering.renderer.BlockRenderer;
import org.xwiki.security.authorization.ContextualAuthorizationManager;
import org.xwiki.security.authorization.Right;
import org.xwiki.test.junit5.mockito.ComponentTest;
import org.xwiki.test.junit5.mockito.InjectMockComponents;
import org.xwiki.test.junit5.mockito.MockComponent;

import com.xpn.xwiki.XWikiContext;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
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
    private Logger logger;

    @MockComponent
    @Named("plain/1.0")
    private BlockRenderer plainTextRenderer;

    @MockComponent
    private ContextualAuthorizationManager contextualAuthorization;

    @MockComponent
    private EntityReferenceResolver<String> resolver;

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
    void maybeSort()
    {
        // Arrange
        List<String> columnsLower = List.of("col1");
        List<Block> rows = new ArrayList<>();

        // the Row has the document name as an addition and the sort method takes into account that so we have to add a
        // placeholder.
        Block documentNamePlaceHolder = new TableCellBlock(List.of(new WordBlock("")));
        // Create row1: col1 = "b"
        TableCellBlock col1Row1 = new TableCellBlock(List.of(new WordBlock("b")));
        TableRowBlock row1 = new TableRowBlock(List.of(documentNamePlaceHolder, col1Row1));

        // Create row2: col1 = "a"
        TableCellBlock col1Row2 = new TableCellBlock(List.of(new WordBlock("a")));
        TableRowBlock row2 = new TableRowBlock(List.of(documentNamePlaceHolder, col1Row2));

        rows.add(row1);
        rows.add(row2);

        // Stub blockToString so it returns text from the WordBlock directly
        doReturn("b").when(confluenceSummaryProcessor).blockToString(col1Row1);
        doReturn("a").when(confluenceSummaryProcessor).blockToString(col1Row2);

        confluenceSummaryProcessor.maybeSort("col1", false, columnsLower, rows);
        assertEquals(List.of(row2, row1), rows);

        confluenceSummaryProcessor.maybeSort("col1", true, columnsLower, rows);
        assertEquals(List.of(row1, row2), rows);
    }

    @Test
    void makeSureThatWeTakeRightIntoAccount()
    {

        EntityReference entityReference = mock(EntityReference.class);
        ReflectionUtils.setFieldValue(this.confluenceSummaryProcessor, "logger", this.logger);
        when(contextProvider.get()).thenReturn(new XWikiContext());
        when(resolver.resolve("docTest", EntityType.DOCUMENT)).thenReturn(entityReference);
        when(contextualAuthorization.hasAccess(Right.VIEW, entityReference)).thenReturn(false);
        assertEquals(List.of(), confluenceSummaryProcessor.getDetails("", List.of(), List.of(), List.of(), "docTest"));
        verify(logger).warn("Tried to get [{}], but the user doesn't have view rights", entityReference);
    }
}
