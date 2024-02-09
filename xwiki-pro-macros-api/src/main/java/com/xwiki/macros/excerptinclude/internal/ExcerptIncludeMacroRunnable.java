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
package com.xwiki.macros.excerptinclude.internal;

import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Provider;
import javax.inject.Singleton;

import org.slf4j.Logger;
import org.xwiki.component.annotation.Component;
import org.xwiki.model.reference.DocumentReference;
import org.xwiki.model.reference.EntityReferenceSerializer;
import org.xwiki.rendering.block.Block;
import org.xwiki.rendering.block.XDOM;
import org.xwiki.rendering.block.match.MacroBlockMatcher;
import org.xwiki.stability.Unstable;

import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.XWikiException;
import com.xpn.xwiki.doc.XWikiDocument;

/**
 * Updates the excerpt-include macro reference parameter after the rename of an excerpt page.
 *
 * @version $Id$
 * @since 1.14.4
 */
@Component(roles = ExcerptIncludeMacroRunnable.class)
@Singleton
@Unstable
public class ExcerptIncludeMacroRunnable extends AbstractExcerptIncludeRunnable
{
    private static final String MACRO_REFERENCE_PARAMETER = "0";

    @Inject
    private Provider<XWikiContext> contextProvider;

    @Inject
    @Named("compact")
    private EntityReferenceSerializer<String> compactEntityReferenceSerializer;

    @Inject
    private Logger logger;

    /**
     * @see com.xpn.xwiki.util.AbstractXWikiRunnable#runInternal()
     */
    @Override
    public void runInternal()
    {
        while (!Thread.interrupted()) {
            ExcerptQueueEntry queueEntry = getNextExcerptQueueEntry();

            if (queueEntry == STOP_RUNNABLE_ENTRY) {
                break;
            }

            XWikiContext xcontext = contextProvider.get();
            DocumentReference originalDocRef = queueEntry.originalDocRef;
            DocumentReference currentDocRef = queueEntry.currentDocRef;

            try {
                // We need to take backlinks from the original document because at this step they are not loaded on
                // the new document.
                List<DocumentReference> backlinks =
                    xcontext.getWiki().getDocument(originalDocRef, xcontext).getBackLinkedReferences(xcontext);

                for (DocumentReference backlinkDocRef : backlinks) {
                    XWikiDocument backlinkDoc = xcontext.getWiki().getDocument(backlinkDocRef, xcontext);

                    updateExcerptIncludeMacrosReferences(backlinkDoc, currentDocRef, originalDocRef, xcontext);
                }
            } catch (XWikiException e) {
                logger.warn("Update excerpt include macro reference parameter thread interrupted", e);
            }
        }
    }

    /**
     * Update a page excerpt include macro's old reference with the new one.
     *
     * @param document document that need to be updated with the new reference
     * @param newReference new excerpt reference
     * @param oldReference old excerpt reference
     * @param xcontext the XWikiContext
     * @throws XWikiException if updating the document fails
     */
    public void updateExcerptIncludeMacrosReferences(XWikiDocument document, DocumentReference newReference,
        DocumentReference oldReference, XWikiContext xcontext) throws XWikiException
    {
        XDOM backlinkDocXDOM = document.getXDOM();
        List<Block> macroBlocks = backlinkDocXDOM.getBlocks(new MacroBlockMatcher("excerpt-include"), Block.Axes.CHILD);

        String newReferenceString =
            compactEntityReferenceSerializer.serialize(newReference, document.getDocumentReference());
        String oldReferenceString =
            compactEntityReferenceSerializer.serialize(oldReference, document.getDocumentReference());

        boolean modified = false;
        for (Block macroBlock : macroBlocks) {
            String macroReference = macroBlock.getParameter(MACRO_REFERENCE_PARAMETER);

            if (!macroReference.equals(newReferenceString) && macroReference.equals(oldReferenceString)) {
                macroBlock.setParameter(MACRO_REFERENCE_PARAMETER, newReferenceString);
                modified = true;
            }
        }

        if (modified) {
            document.setContent(backlinkDocXDOM);
            xcontext.getWiki().saveDocument(document, "Updated excerpt-include macros references after excerpt page "
                    + "rename",
                xcontext);
        }
    }
}
