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

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Provider;
import javax.inject.Singleton;

import org.apache.solr.common.SolrDocument;
import org.xwiki.component.annotation.Component;
import org.xwiki.job.JobException;
import org.xwiki.localization.ContextualLocalizationManager;
import org.xwiki.localization.LocalizationManager;
import org.xwiki.model.reference.EntityReferenceResolver;
import org.xwiki.rendering.RenderingException;
import org.xwiki.rendering.async.internal.block.BlockAsyncRendererConfiguration;
import org.xwiki.rendering.async.internal.block.BlockAsyncRendererExecutor;
import org.xwiki.rendering.block.Block;
import org.xwiki.rendering.block.LinkBlock;
import org.xwiki.rendering.block.MetaDataBlock;
import org.xwiki.rendering.block.TableBlock;
import org.xwiki.rendering.block.TableCellBlock;
import org.xwiki.rendering.block.TableHeadCellBlock;
import org.xwiki.rendering.block.TableRowBlock;
import org.xwiki.rendering.block.WordBlock;
import org.xwiki.rendering.listener.MetaData;
import org.xwiki.rendering.listener.reference.DocumentResourceReference;
import org.xwiki.rendering.listener.reference.ResourceReference;
import org.xwiki.rendering.listener.reference.ResourceType;
import org.xwiki.rendering.transformation.MacroTransformationContext;

import com.xpn.xwiki.XWikiContext;
import com.xwiki.macros.AbstractProMacro;
import com.xwiki.macros.confluence.cql.CQLUtils;
import com.xwiki.macros.confluence.detailssummary.macro.DetailsSummaryMacroParameters;

/**
 * Details summary macro: Display a list of details macro as a table.
 *
 * @version $Id$
 * @since 1.28.3
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
    private CQLUtils cqlUtils;

    @Inject
    private Provider<XWikiContext> contextProvider;

    @Inject
    private ConfluenceSummaryProcessor confluenceSummaryProcessor;

    @Inject
    private ContextualLocalizationManager localizationManager;

    @Inject
    private LocalizationManager localization;

    @Inject
    private EntityReferenceResolver<String> resolver;

    /**
     * Create amd initialize the descriptor of the macro.
     */
    public DetailsSummaryMacro()
    {
        super("Details Summary 2", "TODO FIX THIS", DetailsSummaryMacroParameters.class);
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
        List<Block> columns = headings.isEmpty() ? new ArrayList<>() : headings.stream()
            .map(heading -> new TableHeadCellBlock((List.of(new WordBlock(heading))))).collect(Collectors.toList());
        List<String> columnsLower = headings.isEmpty() ? new ArrayList<>() : headings.stream().map(String::toLowerCase)
            .collect(Collectors.toList());

        List<Block> tableRows = new ArrayList<>();
        for (SolrDocument document : documents) {
            String fullName = document.get("wiki") + ":" + document.get("fullname");
            List<List<Block>> rows =
                confluenceSummaryProcessor.getDetails(parameters.getId(), headings, columns, columnsLower, fullName);

            // Wrap each block with a metadata to make sure that relative references are resolved correctly.
            rows.forEach((row) -> {
                enhanceRow(parameters, document, row);
                TableRowBlock tableRowBlock = new TableRowBlock(row);
                MetaDataBlock metaDataBlock = new MetaDataBlock(List.of(tableRowBlock));
                metaDataBlock.getMetaData().addMetaData(MetaData.SOURCE, fullName);
                metaDataBlock.getMetaData().addMetaData(MetaData.BASE, fullName);
                BlockAsyncRendererConfiguration configuration =
                    new BlockAsyncRendererConfiguration(null, metaDataBlock);
                configuration.setInline(true);
                configuration.setDefaultSyntax(context.getSyntax());
                configuration.setTargetSyntax(context.getSyntax());
                configuration.setResricted(true);
                configuration.setTransformationId(context.getTransformationContext().getId());
                configuration.setAsyncAllowed(false);
                configuration.setCacheAllowed(false);
                try {
                    tableRows.add(executor.execute(configuration));
                } catch (JobException e) {
                    throw new RuntimeException(e);
                } catch (RenderingException e) {
                    throw new RuntimeException(e);
                }
            });
        }

        // Before adding the header row sort the rows.
        confluenceSummaryProcessor.maybeSort(parameters.getSort(), parameters.getReverse(), columnsLower, tableRows);
        enhanceHeader(parameters, columns);
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

    private List<Block> createTagsBlock(List<String> tagList)
    {
        List<Block> tags = new ArrayList<>();
        String baseRef = "Main.Tags";
        for (int i = 0; i < tagList.size(); i++) {
            String tag = tagList.get(i);
            DocumentResourceReference reference = new DocumentResourceReference(baseRef);
            reference.setQueryString(
                String.format("do=viewTag&tag=%s", URLEncoder.encode(tag, StandardCharsets.UTF_8)));
            tags.add(new LinkBlock(List.of(new WordBlock(tag)), reference, false));

            // Add ", " after each tag except the last
            if (i < tagList.size() - 1) {
                tags.add(new WordBlock(", "));
            }
        }
        return tags;
    }

    private void enhanceRow(DetailsSummaryMacroParameters parameters, SolrDocument document, List<Block> row)
    {
        if (parameters.showLastModified()) {
            Date date = (Date) document.get("date");
            DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
            row.add(new TableCellBlock(List.of(new WordBlock(dateFormat.format(date)))));
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
                block = new WordBlock(author);
            } else {
                ResourceReference userRef = new ResourceReference(author, ResourceType.DOCUMENT);
                block = new LinkBlock(List.of(), userRef, false);
            }

            row.add(new TableCellBlock(List.of(block)));
        }
    }

    private void rowsNotFound(List<Block> tableRows)
    {
        String message = localizationManager.getTranslationPlain("rendering.macro.detailssummary.noresults");
        Block row = new TableRowBlock(List.of(new TableCellBlock(List.of(new WordBlock(message)))));
        tableRows.add(row);
    }

    private void enhanceHeader(DetailsSummaryMacroParameters parameters, List<Block> header)
    {

        String titleColumnName = parameters.getFirstcolumn().equals("") ? localizationManager.getTranslationPlain(
            "rendering.macro.detailssummary.firstcolumn") : parameters.getFirstcolumn();
        header.add(0, new TableHeadCellBlock(List.of(new WordBlock(titleColumnName))));

        if (parameters.showLastModified()) {
            header.add(new TableHeadCellBlock(List.of(new WordBlock(
                localizationManager.getTranslationPlain("rendering.macro.detailssummary.lastModified")))));
        }

        if (parameters.showPageLabels()) {
            header.add(new TableHeadCellBlock(List.of(
                new WordBlock(localizationManager.getTranslationPlain("rendering.macro.detailssummary.tags")))));
        }

        if (parameters.showCreator()) {
            header.add(new TableHeadCellBlock(List.of(
                new WordBlock(localizationManager.getTranslationPlain("rendering.macro.detailssummary.creator")))));
        }
    }
}
