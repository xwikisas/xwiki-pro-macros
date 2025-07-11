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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Provider;
import javax.inject.Singleton;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.xwiki.component.annotation.Component;
import org.xwiki.component.manager.ComponentLookupException;
import org.xwiki.component.manager.ComponentManager;
import org.xwiki.model.EntityType;
import org.xwiki.model.reference.EntityReference;
import org.xwiki.model.reference.EntityReferenceResolver;
import org.xwiki.rendering.block.Block;
import org.xwiki.rendering.block.MacroBlock;
import org.xwiki.rendering.block.TableCellBlock;
import org.xwiki.rendering.block.TableRowBlock;
import org.xwiki.rendering.block.XDOM;
import org.xwiki.rendering.block.match.BlockMatcher;
import org.xwiki.rendering.block.match.ClassBlockMatcher;
import org.xwiki.rendering.renderer.BlockRenderer;
import org.xwiki.rendering.renderer.printer.DefaultWikiPrinter;
import org.xwiki.rendering.syntax.Syntax;
import org.xwiki.script.service.ScriptService;
import org.xwiki.stability.Unstable;

import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.XWikiException;
import com.xpn.xwiki.doc.XWikiDocument;

import static com.xwiki.macros.confluence.internal.XDOMUtils.getMacroXDOM;
import static org.apache.commons.lang3.StringUtils.defaultString;

/**
 * Confluence details script services. Mostly to implement the detailssummary macro.
 * @since 1.19.0
 * @version $Id$
 */
@Component
@Singleton
@Named("confluence.details")
@Unstable
public class ConfluenceDetailsScriptService implements ScriptService
{
    private static final BlockMatcher CELL_MATCHER = new ClassBlockMatcher(TableCellBlock.class);
    private static final BlockMatcher ROW_MATCHER = new ClassBlockMatcher(TableRowBlock.class);
    private static final ClassBlockMatcher MACRO_MATCHER = new ClassBlockMatcher(MacroBlock.class);
    private static final String ID = "id";

    @Inject
    private Provider<XWikiContext> contextProvider;

    @Inject
    private EntityReferenceResolver<String> resolver;

    @Inject
    @Named("plain/1.0")
    private BlockRenderer plainTextRenderer;

    @Inject
    @Named("xwiki/2.1")
    private BlockRenderer xwikiSyntaxRenderer;

    @Inject
    private Provider<ComponentManager> componentManagerProvider;

    @Inject
    private Logger logger;


    private List<XDOM> findDetailsMacros(XDOM xdom, String syntaxId, String id)
    {
        List<XDOM> results = new ArrayList<>(1);
        List<MacroBlock> macros = xdom.getBlocks(MACRO_MATCHER, Block.Axes.DESCENDANT_OR_SELF);
        for (MacroBlock macroBlock : macros) {
            try {
                if (StringUtils.equals("confluence_details", macroBlock.getId())) {
                    if (StringUtils.isBlank(id) || StringUtils.equals(id, defaultString(macroBlock.getParameter(ID)))) {
                        results.add(getMacroXDOM(componentManagerProvider.get(), macroBlock, syntaxId));
                    }
                } else {
                    XDOM macroXDOM = getMacroXDOM(componentManagerProvider.get(), macroBlock, syntaxId);
                    if (macroXDOM != null) {
                        results.addAll(findDetailsMacros(macroXDOM, syntaxId, id));
                    }
                }
            } catch (ComponentLookupException e) {
                logger.error("Component lookup error trying to find the confluence_details macro", e);
            }
        }
        return results;
    }

    /**
     * @return the rows to display in the detailssummary macro given the provided details id, the headings parameter
     *          and the results
     * @param id the id of the details macros to consider
     * @param headingsParam the headings confluence parameter
     * @param results the results of the CQL query of the detailsummary macro
     */
    public List<List<String>> getDetails(String id, String headingsParam, List<Map<String, Object>> results)
    {
        return getDetails(id, headingsParam, results, "", false);
    }

    /**
     * @return the rows to display in the detailssummary macro given the provided details id, the headings parameter
     *          and the results
     * @param id the id of the details macros to consider
     * @param headingsParam the headings confluence parameter
     * @param results the results of the CQL query of the detailsummary macro
     * @param sortBy the column to sort against
     * @param reverseSort whether to reverse the sort
     */
    public List<List<String>> getDetails(String id, String headingsParam, List<Map<String, Object>> results,
        String sortBy, boolean reverseSort)
    {
        List<String> headings = parseHeadings(headingsParam);
        List<String> columns = headings.isEmpty() ? new ArrayList<>() : headings;
        List<String> columnsLower = headings.isEmpty()
            ? new ArrayList<>()
            : headings.stream().map(String::toLowerCase).collect(Collectors.toList());

        List<List<String>> rows = new ArrayList<>(results.size() + 1);
        for (Map<String, Object> response : results) {
            String fullName = response.get("wiki").toString() + ':' + response.get("fullname").toString();
            EntityReference docRef = resolver.resolve(fullName, EntityType.DOCUMENT);
            XWikiContext context = contextProvider.get();
            XWikiDocument doc;
            try {
                doc = context.getWiki().getDocument(docRef, context);
            } catch (XWikiException e) {
                logger.error("Could not get the document", e);
                continue;
            }

            List<XDOM> details = findDetailsMacros(doc.getXDOM(), doc.getSyntax().toIdString(),
                StringUtils.defaultString(id));
            if (CollectionUtils.isEmpty(details)) {
                continue;
            }
            List<String> row = getRow(details, headings, columns, columnsLower, doc.getSyntax());
            row.add(0, fullName);
            rows.add(row);
        }

        maybeSort(sortBy, reverseSort, columnsLower, rows);
        rows.add(0, columns);
        return rows;
    }

    private static void maybeSort(String sortBy, boolean reverseSort, List<String> columnsLower,
        List<List<String>> rows)
    {
        boolean alreadyReversedIfNeeded = false;
        if (StringUtils.isNotEmpty(sortBy)) {
            int i = columnsLower.indexOf(sortBy.toLowerCase());
            if (i != -1) {
                alreadyReversedIfNeeded = true;
                rows.sort((l1, l2) -> {
                    String v1 = (i + 1 < l1.size()) ? l1.get(i + 1) : "";
                    String v2 = (i + 1 < l2.size()) ? l2.get(i + 1) : "";

                    // FIXME: technically requires parsing the XWiki syntax
                    int r = Objects.compare(v1, v2, Comparator.comparing(String::toString));
                    if (reverseSort) {
                        return -r;
                    }

                    return r;
                });
            }
        }

        if (reverseSort && !alreadyReversedIfNeeded) {
            Collections.reverse(rows);
        }
    }

    private List<TableRowBlock> findRows(XDOM xdom, Syntax syntax)
    {
        List<TableRowBlock> xdomRows = xdom.getBlocks(ROW_MATCHER, Block.Axes.DESCENDANT_OR_SELF);
        if (!xdomRows.isEmpty()) {
            return xdomRows;
        }

        List<MacroBlock> macroBlocks = xdom.getBlocks(MACRO_MATCHER, Block.Axes.DESCENDANT);
        for (MacroBlock macroBlock : macroBlocks) {
            XDOM macroContent;
            try {
                macroContent = getMacroXDOM(componentManagerProvider.get(), macroBlock, syntax);
            } catch (ComponentLookupException e) {
                logger.error("Failed to parse macro content for [{}]", macroBlock.getId(), e);
                continue;
            }
            if (macroContent != null) {
                xdomRows = findRows(macroContent, syntax);
                if (!xdomRows.isEmpty()) {
                    return xdomRows;
                }
            }
        }

        return Collections.emptyList();
    }

    private List<TableRowBlock> findRows(List<XDOM> xdoms, Syntax syntax)
    {
        if (xdoms.isEmpty()) {
            return Collections.emptyList();
        }

        if (xdoms.size() == 1) {
            return findRows(xdoms.get(0), syntax);
        }

        List<TableRowBlock> xdomRows = new ArrayList<>();
        for (XDOM xdom : xdoms) {
            xdomRows.addAll(findRows(xdom, syntax));
        }
        return xdomRows;
    }

    private List<String> getRow(List<XDOM> xdomDetails, List<String> headings, List<String> columns,
        List<String> columnsLower, Syntax syntax)
    {
        List<TableRowBlock> xdomRows = findRows(xdomDetails, syntax);
        List<String> row = new ArrayList<>(1 + (headings.isEmpty()
            ? Math.max(columns.size(), xdomRows.size())
            : headings.size()));
        for (TableRowBlock xdomRow : xdomRows) {
            List<TableCellBlock> cells = xdomRow.getBlocks(CELL_MATCHER, Block.Axes.DESCENDANT_OR_SELF);
            if (cells.size() < 2) {
                continue;
            }
            DefaultWikiPrinter printer = new DefaultWikiPrinter();
            plainTextRenderer.render(cells.get(0), printer);
            String key = printer.toString().trim();

            printer.clear();
            xwikiSyntaxRenderer.render(cells.get(1).getChildren(), printer);

            String value = printer.toString().trim();

            String keyLower = key.toLowerCase();
            int index = columnsLower.indexOf(keyLower);
            if (index == -1) {
                if (!headings.isEmpty()) {
                    // if the headings was specified by the user, don't add columns
                    continue;
                }
                index = columns.size();
                columns.add(key);
                columnsLower.add(keyLower);
            }
            while (index >= row.size()) {
                row.add("");
            }
            row.set(index, value);
        }
        return row;
    }

    private List<String> parseHeadings(String headingsParam)
    {
        if (StringUtils.isEmpty(headingsParam)) {
            return Collections.emptyList();
        }

        List<String> headings = new ArrayList<>(StringUtils.countMatches(headingsParam, ','));
        int i = 0;
        int len = headingsParam.length();
        StringBuilder heading = new StringBuilder();
        while (i < len) {
            char c = headingsParam.charAt(i);
            if (c == '"') {
                i++;
                while (i < len) {
                    c = headingsParam.charAt(i);
                    if (c == '"') {
                        break;
                    }

                    if (c == '\\') {
                        i++;
                        c = headingsParam.charAt(i);
                    }
                    heading.append(c);
                    i++;
                }
            } else if (c == ',') {
                headings.add(heading.toString().trim());
                heading.setLength(0);
            } else {
                heading.append(c);
            }
            i++;
        }

        if (heading.length() > 0) {
            headings.add(heading.toString().trim());
        }

        return headings;
    }
}
