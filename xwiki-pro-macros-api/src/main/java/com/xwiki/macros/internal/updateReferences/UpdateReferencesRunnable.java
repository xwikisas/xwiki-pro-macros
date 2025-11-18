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
package com.xwiki.macros.internal.updateReferences;

import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

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

import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.XWikiException;
import com.xpn.xwiki.doc.XWikiDocument;
import com.xpn.xwiki.util.AbstractXWikiRunnable;

/**
 * Updates the value of a parameters that holds a reference, after the rename of a page.
 *
 * @version $Id$
 * @since 1.29
 */
@Component(roles = UpdateReferencesRunnable.class)
@Singleton
public class UpdateReferencesRunnable extends AbstractXWikiRunnable
{
    /**
     * Stop runnable entry.
     */
    public static final UpdateReferencesQueueEntry STOP_RUNNABLE_ENTRY = new UpdateReferencesQueueEntry(
        null,
        null,
        Map.of()
    );

    @Inject
    private Provider<XWikiContext> contextProvider;

    @Inject
    @Named("compact")
    private EntityReferenceSerializer<String> compactEntityReferenceSerializer;

    @Inject
    private Logger logger;

    /**
     * Entries to be processed by this thread.
     */
    private final BlockingQueue<UpdateReferencesQueueEntry> referenceQueue =
        new LinkedBlockingQueue<>();

    /**
     * Add entries to the thread's queue.
     *
     * @param queueEntry the entry to be added
     */
    public void addToQueue(UpdateReferencesQueueEntry queueEntry)
    {
        this.referenceQueue.add(queueEntry);
    }

    @Override
    protected void runInternal()
    {
        while (!Thread.interrupted()) {
            UpdateReferencesQueueEntry queueEntry = getNextReferenceQueueEntry();

            if (queueEntry == STOP_RUNNABLE_ENTRY) {
                break;
            }

            XWikiContext xcontext = contextProvider.get();
            DocumentReference originalDocRef = queueEntry.getOriginalDocRef();
            DocumentReference currentDocRef = queueEntry.getCurrentDocRef();
            Map<String, List<String>> macrosToUpdate = queueEntry.getMacrosToUpdate();

            try {
                // We need to take backlinks from the original document because at this step they are not loaded on
                // the new document.
                List<DocumentReference> backlinks =
                    xcontext.getWiki().getDocument(originalDocRef, xcontext).getBackLinkedReferences(xcontext);

                for (DocumentReference backlinkDocRef : backlinks) {
                    XWikiDocument backlinkDoc = xcontext.getWiki().getDocument(backlinkDocRef, xcontext).clone();

                    updateMacroReference(backlinkDoc, currentDocRef, originalDocRef, macrosToUpdate, xcontext);
                }
            } catch (XWikiException e) {
                logger.warn("Update macro reference parameter thread interrupted", e);
            }
        }
    }

    private UpdateReferencesQueueEntry getNextReferenceQueueEntry()
    {
        UpdateReferencesQueueEntry queueEntry;

        try {
            queueEntry = this.referenceQueue.take();
        } catch (InterruptedException e) {
            logger.warn("Reference update thread has been interrupted", e);
            queueEntry = STOP_RUNNABLE_ENTRY;
        }

        if (queueEntry == STOP_RUNNABLE_ENTRY) {
            this.referenceQueue.clear();
        }

        return queueEntry;
    }

    private void updateMacroReference(XWikiDocument document, DocumentReference newReference,
        DocumentReference oldReference, Map<String, List<String>> macrosToUpdate, XWikiContext xcontext)
        throws XWikiException
    {
        for (Map.Entry<String, List<String>> macroToUpdate : macrosToUpdate.entrySet()) {
            XDOM backlinkDocXDOM = document.getXDOM();
            String macroName = macroToUpdate.getKey();
            List<String> macroParametersToUpdate = macroToUpdate.getValue();
            List<Block> macroBlocks =
                backlinkDocXDOM.getBlocks(new MacroBlockMatcher(macroName), Block.Axes.DESCENDANT);

            String newReferenceString =
                compactEntityReferenceSerializer.serialize(newReference, document.getDocumentReference());
            String oldReferenceString =
                compactEntityReferenceSerializer.serialize(oldReference, document.getDocumentReference());

            boolean modified = false;

            for (Block macroBlock : macroBlocks) {
                for (String macroParameter : macroParametersToUpdate) {
                    String macroReference = macroBlock.getParameter(macroParameter);

                    if (!macroReference.equals(newReferenceString) && macroReference.equals(oldReferenceString)) {
                        macroBlock.setParameter(macroParameter, newReferenceString);
                        modified = true;
                    }
                }
            }

            if (modified) {
                document.setContent(backlinkDocXDOM);
                xcontext.getWiki().saveDocument(document, "Updated macros references after page rename",
                    xcontext);
            }
        }
    }
}
