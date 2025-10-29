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

import java.io.StringReader;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Provider;
import javax.inject.Singleton;

import org.apache.solr.common.SolrDocument;
import org.slf4j.Logger;
import org.xwiki.component.annotation.Component;
import org.xwiki.localization.ContextualLocalizationManager;
import org.xwiki.rendering.async.internal.block.BlockAsyncRendererConfiguration;
import org.xwiki.rendering.async.internal.block.BlockAsyncRendererExecutor;
import org.xwiki.rendering.block.Block;
import org.xwiki.rendering.block.LinkBlock;
import org.xwiki.rendering.block.MetaDataBlock;
import org.xwiki.rendering.block.SpaceBlock;
import org.xwiki.rendering.block.TableBlock;
import org.xwiki.rendering.block.TableCellBlock;
import org.xwiki.rendering.block.TableHeadCellBlock;
import org.xwiki.rendering.block.TableRowBlock;
import org.xwiki.rendering.listener.MetaData;
import org.xwiki.rendering.listener.reference.DocumentResourceReference;
import org.xwiki.rendering.listener.reference.ResourceReference;
import org.xwiki.rendering.listener.reference.ResourceType;
import org.xwiki.rendering.parser.ParseException;
import org.xwiki.rendering.parser.Parser;
import org.xwiki.rendering.transformation.MacroTransformationContext;

import com.xpn.xwiki.XWikiContext;
import com.xwiki.macros.AbstractProMacro;
import com.xwiki.macros.confluence.detailssummary.macro.DetailsSummaryMacroParameters;
import com.xwiki.macros.confluence.internal.cql.CQLUtils;

/**
 * Details summary macro: Display a list of details macro as a table.
 *
 * @version $Id$
 * @since 1.29.0
 */
@Component
@Named(DetailsSummaryMacro.MACRO_HINT)
@Singleton
public class DetailsSummaryMacro extends AbstractProMacro<DetailsSummaryMacroParameters>
{
    /**
     * THe name of the macro.
     */
    public static final String MACRO_HINT = "confluence_detailssummary";

    @Inject
    protected BlockAsyncRendererExecutor executor;

    @Inject
    @Named("plain/1.0")
    private Parser plainParser;

    @Inject
    private CQLUtils cqlUtils;

    @Inject
    private Provider<XWikiContext> contextProvider;

    @Inject
    private ConfluenceSummaryProcessor confluenceSummaryProcessor;

    @Inject
    private ContextualLocalizationManager localizationManager;

    @Inject
    private Logger logger;

    /**
     * Create and initialize the descriptor of the macro.
     */
    public DetailsSummaryMacro()
    {

        super("Confluence bridge for Details Summary", "Confluence bridge for the Details Summary "
                + "(Page Properties Report) macro to display properties attached to a page using the Details macro.",
            DetailsSummaryMacroParameters.class);
    }

    @Override
    public boolean supportsInlineMode()
    {
        return false;
    }

    @Override
    protected List<Block> internalExecute(DetailsSummaryMacroParameters parameters, String content,
        MacroTransformationContext context)
    {

        List<SolrDocument> documents = cqlUtils.buildAndExecute(buildQueryMap(parameters));
        List<String> headings = confluenceSummaryProcessor.parseHeadings(parameters.getHeadings());
        // We create the columns here and give the object as a parameter so we can collect the column names as we go in
        // case the user didn't provide them already.
        List<Block> columns = new ArrayList<>();
        // The first column will always be the document name, but it may be translated to a diffrent name.
        String titleColumnName = parameters.getFirstcolumn().isEmpty() ? localizationManager.getTranslationPlain(
            "rendering.macro.detailssummary.firstcolumn") : parameters.getFirstcolumn();
        columns.add(0, new TableHeadCellBlock(parsePlainText(titleColumnName)));
        if (!headings.isEmpty()) {
            for (String heading : headings) {
                Block cell = new TableHeadCellBlock(parsePlainText(heading));
                columns.add(cell);
            }
        }
        List<String> columnsLower = new ArrayList<>();
        // Always add the name of the column that holds the document name.
        columnsLower.add(titleColumnName.toLowerCase());
        if (!headings.isEmpty()) {
            for (String heading : headings) {
                columnsLower.add(heading.toLowerCase());
            }
        }

        List<Block> tableRows = new ArrayList<>();
        List<RowContext> rawRows = new ArrayList<>();
        for (SolrDocument document : documents) {
            String fullName = document.get("wiki") + ":" + document.get("fullname");
            List<List<Block>> rows =
                confluenceSummaryProcessor.getDetails(parameters.getId(), headings, columns, columnsLower, fullName);

            rows.forEach(row -> rawRows.add(new RowContext(document, fullName, row)));
        }

        // Wrap each block with a metadata to make sure that relative references are resolved correctly.
        rawRows.forEach((rowContext) -> {
            enhanceRow(parameters, rowContext.getDocument(), rowContext.getRow(), columnsLower.size());
            MetaDataBlock metaDataBlock = setMetaDataBlock(rowContext.getRow(), rowContext.getFullName());
            BlockAsyncRendererConfiguration configuration = getBlockAsyncRendererConfiguration(context, metaDataBlock);
            try {
                tableRows.add(executor.execute(configuration));
            } catch (Exception e) {
                logger.warn("Failed to render the row with the permissions of the author.", e);
            }
        });

        enhanceHeader(parameters, columns, columnsLower);
        // Before adding the header row sort the rows.
        confluenceSummaryProcessor.maybeSort(parameters.getSort(), parameters.getReverse(), columnsLower, tableRows);
        tableRows.add(0, new TableRowBlock(columns));
        // If the table rows has only one row then it means that there were no details macro found, and we should add
        // a message to make this clear to the user.
        if (tableRows.size() == 1) {
            rowsNotFound(tableRows);
        }

        return List.of(new TableBlock(tableRows));
    }

    protected Map<String, Object> buildQueryMap(DetailsSummaryMacroParameters parameters)
    {
        Map<String, Object> map = new HashMap<>();
        map.put("label", parameters.getLabel());
        map.put("cql", parameters.getCql());
        map.put("operator", parameters.getOperator());
        map.put("max", parameters.getMax());
        map.put("reverse", parameters.getReverse());
        return map;
    }

    private static MetaDataBlock setMetaDataBlock(List<Block> row, String fullName)
    {
        TableRowBlock tableRowBlock = new TableRowBlock(row);
        MetaDataBlock metaDataBlock = new MetaDataBlock(List.of(tableRowBlock));
        metaDataBlock.getMetaData().addMetaData(MetaData.SOURCE, fullName);
        metaDataBlock.getMetaData().addMetaData(MetaData.BASE, fullName);
        return metaDataBlock;
    }

    private BlockAsyncRendererConfiguration getBlockAsyncRendererConfiguration(MacroTransformationContext context,
        MetaDataBlock metaDataBlock)
    {
        BlockAsyncRendererConfiguration configuration = new BlockAsyncRendererConfiguration(null, metaDataBlock);
        configuration.setInline(true);
        configuration.setDefaultSyntax(context.getSyntax());
        configuration.setTargetSyntax(context.getSyntax());
        configuration.setResricted(true);
        configuration.setTransformationId(context.getTransformationContext().getId());
        configuration.setAsyncAllowed(false);
        configuration.setCacheAllowed(false);
        return configuration;
    }

    private List<Block> createTagsBlock(List<String> tagList)
    {
        List<Block> tags = new ArrayList<>();
        String baseRef = "Main.Tags";
        List<Block> separatorAsBlocks = parsePlainText(", ");
        for (int i = 0; i < tagList.size(); i++) {
            String tag = tagList.get(i);
            DocumentResourceReference reference = new DocumentResourceReference(baseRef);
            reference.setQueryString(
                String.format("do=viewTag&tag=%s", URLEncoder.encode(tag, StandardCharsets.UTF_8)));
            List<Block> tagAsBlocks = parsePlainText(tag);
            tags.add(new LinkBlock(tagAsBlocks, reference, false));

            // Add ", " after each tag except the last
            if (i < tagList.size() - 1) {
                tags.addAll(separatorAsBlocks);
            }
        }
        return tags;
    }

    private void enhanceRow(DetailsSummaryMacroParameters parameters, SolrDocument document, List<Block> row,
        int baseLength)
    {

        if (row.size() < baseLength) {
            for (int i = 0; i < baseLength - row.size(); i++) {
                row.add(new TableCellBlock(List.of(new SpaceBlock())));
            }
        }

        if (parameters.showLastModified()) {
            Date date = (Date) document.get("date");
            XWikiContext context = contextProvider.get();
            String formattedDate = context.getWiki().formatDate(date, null, context);
            row.add(new TableCellBlock(parsePlainText(formattedDate)));
        }

        if (parameters.showPageLabels()) {
            List<String> tagList = (ArrayList<String>) document.get("property.XWiki.TagClass.tags_string");
            List<Block> tags = createTagsBlock(tagList);
            row.add(new TableCellBlock(tags));
        }

        if (parameters.showCreator()) {

            String author = ((String) document.get("creator"));
            Block block;
            if (author.equals("XWiki.superadmin")) {
                block = new TableCellBlock(parsePlainText(author));
            } else {
                ResourceReference userRef = new ResourceReference(author, ResourceType.DOCUMENT);
                block = new TableCellBlock(List.of(new LinkBlock(List.of(), userRef, false)));
            }

            row.add(block);
        }
    }

    private void rowsNotFound(List<Block> tableRows)
    {
        String message = localizationManager.getTranslationPlain("rendering.macro.detailssummary.noresults");
        Block row = new TableRowBlock(List.of(new TableCellBlock(parsePlainText(message))));
        tableRows.add(row);
    }

    private void enhanceHeader(DetailsSummaryMacroParameters parameters, List<Block> header, List<String> columnsLower)
    {

        if (parameters.showLastModified()) {
            String translation = localizationManager.getTranslationPlain("rendering.macro.detailssummary.lastModified");
            header.add(new TableHeadCellBlock(parsePlainText(translation)));
            columnsLower.add(translation.toLowerCase());
        }

        if (parameters.showPageLabels()) {
            String translation = localizationManager.getTranslationPlain("rendering.macro.detailssummary.tags");
            header.add(new TableHeadCellBlock(parsePlainText(translation)));
            columnsLower.add(translation.toLowerCase());
        }

        if (parameters.showCreator()) {
            String translation = localizationManager.getTranslationPlain("rendering.macro.detailssummary.creator");
            header.add(new TableHeadCellBlock(parsePlainText(translation)));
            columnsLower.add(translation.toLowerCase());
        }
    }

    private List<Block> parsePlainText(String text)
    {
        try {
            // Since this is parsed from plain text everything is wrapped in a Paragraph, but this is not needed and
            // cand break the UI.
            return plainParser.parse(new StringReader(text)).getChildren().get(0).getChildren();
        } catch (ParseException e) {
            logger.error("Failed to parse plain text: [{}]", text, e);
            return List.of(new SpaceBlock());
        }
    }
}
