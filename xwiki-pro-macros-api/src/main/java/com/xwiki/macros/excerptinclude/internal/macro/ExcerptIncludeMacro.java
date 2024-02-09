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
package com.xwiki.macros.excerptinclude.internal.macro;

import java.util.Collections;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Provider;
import javax.inject.Singleton;

import org.xwiki.component.annotation.Component;
import org.xwiki.display.internal.DocumentDisplayer;
import org.xwiki.display.internal.DocumentDisplayerParameters;
import org.xwiki.model.reference.DocumentReference;
import org.xwiki.rendering.block.Block;
import org.xwiki.rendering.block.MacroBlock;
import org.xwiki.rendering.block.TableBlock;
import org.xwiki.rendering.block.TableCellBlock;
import org.xwiki.rendering.block.TableHeadCellBlock;
import org.xwiki.rendering.block.TableRowBlock;
import org.xwiki.rendering.block.WordBlock;
import org.xwiki.rendering.block.XDOM;
import org.xwiki.rendering.block.match.ClassBlockMatcher;
import org.xwiki.rendering.macro.MacroExecutionException;
import org.xwiki.rendering.transformation.MacroTransformationContext;
import org.xwiki.security.authorization.ContextualAuthorizationManager;
import org.xwiki.security.authorization.Right;
import org.xwiki.stability.Unstable;

import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.XWikiException;
import com.xpn.xwiki.doc.XWikiDocument;
import com.xwiki.macros.AbstractProMacro;
import com.xwiki.macros.excerptinclude.macro.ExcerptIncludeMacroParameters;

/**
 * Implements the 'excerpt-include' macro functionality to include excerpts from other documents.
 * This macro serves as a bridge for the Confluence Excerpt Include macro.
 *
 * @version $Id$
 * @since 1.14.4
 */
@Component
@Named("excerpt-include")
@Singleton
@Unstable
public class ExcerptIncludeMacro extends AbstractProMacro<ExcerptIncludeMacroParameters>
{
    @Inject
    private ContextualAuthorizationManager contextualAuthorization;

    @Inject
    @Named("content")
    private DocumentDisplayer contentDisplayer;

    @Inject
    private Provider<XWikiContext> contextProvider;

    /**
     * Constructs an instance of ExcerptIncludeMacro. Initializes the macro descriptor with a specific name,
     * description, and parameter class type.
     */
    public ExcerptIncludeMacro()
    {
        super("Excerpt include",
            "Bridge for the Confluence Excerpt Include macro, includes excerpts from other document.",
            ExcerptIncludeMacroParameters.class);
    }

    @Override
    public boolean supportsInlineMode()
    {
        return false;
    }

    @Override
    protected List<Block> internalExecute(ExcerptIncludeMacroParameters parameters, String content,
        MacroTransformationContext context) throws MacroExecutionException
    {
        DocumentReference reference = parameters.getReference();
        XWikiContext xcontext = contextProvider.get();
        XWikiDocument document;
        try {
            document = xcontext.getWiki().getDocument(reference, xcontext);
        } catch (XWikiException e) {
            throw new MacroExecutionException(String.format("Failed to get document for reference [%s]", reference), e);
        }

        if (!this.contextualAuthorization.hasAccess(Right.VIEW, reference)) {
            throw new MacroExecutionException(
                String.format("Current user [%s] doesn't have view rights on document [%s]",
                    xcontext.getUserReference(), reference));
        }

        List<Block> blocks =
            document.getXDOM().getBlocks(new ClassBlockMatcher(MacroBlock.class), Block.Axes.DESCENDANT);

        XDOM displayContent = null;
        DocumentDisplayerParameters displayParameters = new DocumentDisplayerParameters();
        displayParameters.setContentTransformed(true);
        displayParameters.setExecutionContextIsolated(true);
        for (Block block : blocks) {
            MacroBlock macroBlock = (MacroBlock) block;
            if ("excerpt".equals(macroBlock.getId())) {
                // Use a document clone that has only the excerpt macro as content, to be displayed.
                XWikiDocument documentClone = document.clone();
                try {
                    documentClone.setContent(new XDOM(Collections.singletonList(macroBlock)));
                } catch (XWikiException e) {
                    throw new RuntimeException(e);
                }
                displayContent = contentDisplayer.display(documentClone, displayParameters);
            }
        }
        // If there is no excerpts macro, display the entire content.
        if (displayContent == null) {
            displayContent = contentDisplayer.display(document, displayParameters);
        }

        TableBlock tableBlock = new TableBlock(List.of(
            new TableRowBlock(List.of(new TableHeadCellBlock(List.of(new WordBlock(document.getTitle()))))),
            new TableRowBlock(List.of(new TableCellBlock(Collections.singletonList(displayContent))))));

        return List.of(tableBlock);
    }
}
