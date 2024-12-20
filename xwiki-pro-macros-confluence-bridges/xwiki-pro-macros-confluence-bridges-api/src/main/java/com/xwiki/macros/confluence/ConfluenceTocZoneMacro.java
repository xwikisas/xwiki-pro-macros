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
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.xwiki.component.annotation.Component;
import org.xwiki.rendering.block.Block;
import org.xwiki.rendering.block.BulletedListBlock;
import org.xwiki.rendering.block.GroupBlock;
import org.xwiki.rendering.block.HeaderBlock;
import org.xwiki.rendering.block.LinkBlock;
import org.xwiki.rendering.block.ListBLock;
import org.xwiki.rendering.block.ListItemBlock;
import org.xwiki.rendering.block.MetaDataBlock;
import org.xwiki.rendering.block.ParagraphBlock;
import org.xwiki.rendering.block.SpaceBlock;
import org.xwiki.rendering.block.WordBlock;
import org.xwiki.rendering.block.match.AnyBlockMatcher;
import org.xwiki.rendering.listener.reference.ResourceReference;
import org.xwiki.rendering.listener.reference.ResourceType;
import org.xwiki.rendering.macro.MacroContentParser;
import org.xwiki.rendering.macro.MacroExecutionException;
import org.xwiki.rendering.macro.descriptor.DefaultContentDescriptor;
import org.xwiki.rendering.parser.Parser;
import org.xwiki.rendering.renderer.BlockRenderer;
import org.xwiki.rendering.renderer.printer.DefaultWikiPrinter;
import org.xwiki.rendering.renderer.printer.WikiPrinter;
import org.xwiki.rendering.renderer.reference.link.LinkLabelGenerator;
import org.xwiki.rendering.transformation.MacroTransformationContext;
import org.xwiki.stability.Unstable;

import com.xwiki.macros.AbstractProMacro;

/**
 * The Confluence outgoing-links bridge macro.
 * 
 * @since 1.23.0
 * @version $Id$
 */
@Component
@Named("confluence_toc-zone")
@Singleton
@Unstable
public class ConfluenceTocZoneMacro extends AbstractProMacro<ConfluenceTocZoneMacroParameters>
{
    private static final String SEMICOLUMN = ";";

    @Inject
    private MacroContentParser contentParser;

    @Inject
    @Named("plain/1.0")
    private BlockRenderer plainTextRenderer;

    @Inject
    @Named("plain/1.0")
    private Parser plainTextParser;

    @Inject
    private LinkLabelGenerator linkLabelGenerator;

    /**
     * Constructor.
     */
    public ConfluenceTocZoneMacro()
    {
        super("Confluence Toc Zone", "Confluence bridge macro for toc-zone.",
            new DefaultContentDescriptor("Content", true, Block.LIST_BLOCK_TYPE),
            ConfluenceTocZoneMacroParameters.class);
    }

    @Override
    public List<Block> internalExecute(ConfluenceTocZoneMacroParameters parameters, String content,
        MacroTransformationContext context) throws MacroExecutionException
    {

        // The parsed content of the macro.
        List<Block> contentXDOM = parseContent(content, context);

        // We need to do a first XDOM traversal to know the minimal heading level.
        int levelOffset = getLevelOffset(contentXDOM, parameters);

        // Generate the TOC.
        Block toc;
        if (parameters.getType().equals(ConfluenceTocZoneMacroTypeParameter.FLAT)) {
            toc = browseXDOMFlat(contentXDOM, levelOffset, context, parameters);
        } else {
            toc = browseXDOMList(contentXDOM, levelOffset, parameters);
        }

        // Wrap the TOC in a div when we need to add a class.
        StringBuilder classes = new StringBuilder("");
        if (!parameters.isPrintable()) {
            classes.append("hidden-print ");
        }
        classes.append(parameters.getCssClass());

        if (!"".equals(classes.toString())) {
            toc = new GroupBlock(Arrays.asList(toc), Collections.singletonMap("class", classes.toString()));
        }

        // Display the TOC somewhere around the editable content.
        List<Block> result = new ArrayList<>();
        switch (parameters.getLocation()) {
            case TOP:
                result.add(toc);
                result.addAll(contentXDOM);
                break;
            case BOTTOM:
                result.addAll(contentXDOM);
                result.add(toc);
                break;
            default:
                result.add(toc);
                result.addAll(contentXDOM);
                result.add(toc);
                break;
        }

        return result;
    }

    @Override
    public boolean supportsInlineMode()
    {
        return false;
    }

    private Block browseXDOMFlat(List<Block> body, int levelOffset, MacroTransformationContext context,
        ConfluenceTocZoneMacroParameters parameters) throws MacroExecutionException
    {
        Block toc = new ParagraphBlock(new ArrayList<>());

        // We keep track of the number of headings per level to display the outline when requested.
        List<Integer> outlineLevels = new ArrayList<>();

        // Set the separators according to the macro parameters.
        String separatorBefore = "";
        String separatorAfter = "";
        String separator = "";
        switch (parameters.getSeparator()) {
            case "brackets":
                separatorBefore = "[ ";
                separatorAfter = " ] ";
                break;
            case "braces":
                separatorBefore = "{ ";
                separatorAfter = " } ";
                break;
            case "parens":
                separatorBefore = "( ";
                separatorAfter = " ) ";
                break;
            case "pipe":
                separator = " | ";
                break;
            default:
                separator = parameters.getSeparator();
        }

        // Keep track of the last sperator's blocks to remove them after the last iteration.
        // The separator is only between the elements.
        List<Block> lastSeparator = Arrays.asList();

        // Traverse the XDOM and find all Headings, in order.
        for (Block block : body) {
            for (Block b : block.getBlocks(new AnyBlockMatcher(), Block.Axes.DESCENDANT)) {
                if (b instanceof HeaderBlock) {
                    HeaderBlock header = (HeaderBlock) b;

                    // Check if we should ignore this heading.
                    if (filterHeader(header, parameters)) {
                        continue;
                    }

                    // Compute the heading's relative level in this TOC.
                    int level = (header).getLevel().getAsInt() + levelOffset;

                    // We parse the separator text because it might contain more than just alphanumeric characters.
                    toc.addChildren(parseReadOnlyContent(separatorBefore, context));

                    // Add the outline if there is one.
                    toc.addChild(getOutline(level, outlineLevels, parameters));
                    toc.addChild(new SpaceBlock());

                    // Add the link to the heading.
                    toc.addChild(getHeaderLink(header));

                    toc.addChildren(parseReadOnlyContent(separatorAfter, context));

                    lastSeparator = parseReadOnlyContent(separator, context);
                    toc.addChildren(lastSeparator);
                }
            }
        }

        // Remove the last separator.
        for (Block block : lastSeparator) {
            toc.removeBlock(block);
        }

        return toc;
    }

    private Block browseXDOMList(List<Block> body, int levelOffset, ConfluenceTocZoneMacroParameters parameters)
    {
        // Keep track of the BulletedListBlocks that hold the different levels.
        List<ListBLock> levels = new ArrayList<>();

        // Keep track of the number of heading in each level to generate the outline.
        List<Integer> outlineLevels = new ArrayList<>();

        // Allow for setting the bullet style.
        Map<String, String> listParameters = Collections.emptyMap();

        StringBuilder style = new StringBuilder("");

        if (!"default".equals(parameters.getStyle())) {

            style.append("list-style-type: " + parameters.getStyle() + SEMICOLUMN);
        }

        if (!"".equals(parameters.getIndent())) {
            style.append("padding-inline-start: " + parameters.getIndent() + SEMICOLUMN);
        }

        if (!"".equals(style.toString())) {
            listParameters = Collections.singletonMap("style", style.toString());
        }
        // Traverse the XDOM and iterate over the headings, in order.
        for (Block block : body) {
            for (Block b : block.getBlocks(new AnyBlockMatcher(), Block.Axes.DESCENDANT)) {
                if (b instanceof HeaderBlock) {
                    HeaderBlock header = (HeaderBlock) b;

                    // Check if we should ignore this heading.
                    if (filterHeader(header, parameters)) {
                        continue;
                    }

                    // Compute the heading's relative level in this TOC.
                    int level = (header).getLevel().getAsInt() + levelOffset;

                    // Create the new TOC level.
                    getNewLevel(level, levels, listParameters)
                        // Add an entry with the outline and link.
                        .addChild(new ListItemBlock(Arrays.asList(getOutline(level, outlineLevels, parameters),
                            new SpaceBlock(), getHeaderLink(header))));
                }
            }
        }

        // The lowest level holds the whole TOC.
        if (levels.size() > 0) {
            return levels.get(0);
        } else {
            return new WordBlock("");
        }

    }

    private LinkBlock getHeaderLink(HeaderBlock headerBlock)
    {
        String idParameter = headerBlock.getParameter("id");
        if (idParameter == null) {
            idParameter = headerBlock.getId();
        }

        ConfluenceTocZoneBlockFilter confluenceTocZoneBlockFilter =
            new ConfluenceTocZoneBlockFilter(this.plainTextParser, this.linkLabelGenerator);

        return new LinkBlock((confluenceTocZoneBlockFilter.generateLabel(headerBlock)),
            new ResourceReference("#" + idParameter, ResourceType.URL), false);
    }

    private int getLevelOffset(List<Block> body, ConfluenceTocZoneMacroParameters parameters)
    {
        // When computing a min, we initialize with the maximum possible value.
        int minLevel = Integer.MAX_VALUE;

        // Iterate through the headings.
        for (Block block : body) {
            for (Block b : block.getBlocks(new AnyBlockMatcher(), Block.Axes.DESCENDANT)) {
                if (b instanceof HeaderBlock) {
                    HeaderBlock header = (HeaderBlock) b;

                    // Check if we should ignore the heading.
                    if (filterHeader(header, parameters)) {
                        continue;
                    }

                    // Update the minimum level.
                    int level = header.getLevel().getAsInt();
                    minLevel = Integer.min(level, minLevel);
                }
            }
        }

        // The offset is something we add.
        // This is why the value is the opposite of the minimum level.
        return -minLevel;
    }

    private boolean filterHeader(HeaderBlock header, ConfluenceTocZoneMacroParameters parameters)
    {
        int level = header.getLevel().getAsInt();

        // Skip heading if the level is below minimum value.
        if (level < parameters.getMinLevel()) {
            return true;
        }

        // Skip heading if the level is above maximum value.
        if (level > parameters.getMaxLevel()) {
            return true;
        }

        // In order to filter based on the text of the heading using a regex, we need to convert its XDOM to text.
        // we use the plainTextRenderer for this purpose.
        WikiPrinter printer = new DefaultWikiPrinter();
        plainTextRenderer.render(header, printer);

        // Skip heading if the parsed text does not match the include regex.
        Pattern includeRegex = Pattern.compile(parameters.getInclude());
        if (!includeRegex.matcher(printer.toString()).find()) {
            return true;
        }

        // Skip heading if the parsed text matches the exclude regex.
        Pattern excludeRegex = Pattern.compile(parameters.getExclude());
        if (excludeRegex.matcher(printer.toString()).find()) {
            return true;
        }

        // If all the checks passed, we can keep this heading.
        return false;
    }

    private Block getOutline(int level, List<Integer> levels, ConfluenceTocZoneMacroParameters parameters)
    {
        StringBuilder outline = new StringBuilder("");

        // We keep the levels List updated as we give the outline for the new heading.

        // When we have a heading with a lower level than the previous one (i.e. more important),
        // we need to get rid of the counters for higher levels.
        for (int i = levels.size() - 1; i > level; i--) {
            levels.remove(i);
        }

        // When we have a heading with a higher level than the previous one (i.e. less important),
        // we need to create all the sublevel's counters.
        for (int i = levels.size() - 1; i < level; i++) {
            levels.add(levels.size() + 1 != level ? 0 : 1);
        }

        // Update the counter for the current level.
        levels.set(level, levels.get(level) + 1);

        // Build the outline, if requested.
        if (parameters.isOutline()) {
            outline.append(levels.get(0));
            for (int i = 1; i <= level; i++) {
                outline.append(".");
                outline.append(levels.get(i));
            }
        }

        return new WordBlock(outline.toString());
    }

    private ListBLock getNewLevel(int level, List<ListBLock> levels, Map<String, String> listParameters)
    {
        // Initialize the levels list.
        if (levels.isEmpty()) {
            levels.add(new BulletedListBlock(new ArrayList<>(), listParameters));
        }

        // When we have a heading with a lower level than the previous one (i.e. more important),
        // we need to get rid of the lists for higher levels.
        for (int i = levels.size() - 1; i > level; i--) {
            levels.remove(i);
        }

        // When we have a heading with a higher level than the previous one (i.e. less important),
        // we need to create all the sublevel's counters.
        for (int i = levels.size() - 1; i < level; i++) {
            levels.add(new BulletedListBlock(new ArrayList<>(), listParameters));
            levels.get(i).addChild(levels.get(i + 1));
        }

        // Return the target level.
        return levels.get(level);

    }

    private List<Block> parseContent(String content, MacroTransformationContext context) throws MacroExecutionException
    {
        // Don't execute transformations explicitly. They'll be executed on the generated content later on.
        List<Block> children = contentParser.parse(content, context, false, context.isInline()).getChildren();

        return Collections.singletonList(new MetaDataBlock(children, this.getNonGeneratedContentMetaData()));
    }

    private List<Block> parseReadOnlyContent(String content, MacroTransformationContext context)
        throws MacroExecutionException
    {
        return contentParser.parse(content, context, true, true).getChildren();
    }
}
