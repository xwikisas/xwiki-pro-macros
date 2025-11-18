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
package com.xwiki.macros.internal.updateReferences.listeners;

import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.slf4j.Logger;
import org.xwiki.bridge.event.DocumentCreatedEvent;
import org.xwiki.component.annotation.Component;
import org.xwiki.component.phase.Disposable;
import org.xwiki.job.Job;
import org.xwiki.job.JobContext;
import org.xwiki.job.event.JobStartedEvent;
import org.xwiki.model.reference.DocumentReference;
import org.xwiki.observation.AbstractEventListener;
import org.xwiki.observation.ObservationContext;
import org.xwiki.observation.event.Event;

import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.XWikiException;
import com.xpn.xwiki.doc.XWikiDocument;
import com.xwiki.macros.internal.updateReferences.UpdateReferencesQueueEntry;
import com.xwiki.macros.internal.updateReferences.UpdateReferencesRunnable;

/**
 * Listens to rename of pages. Also, for pages that contains macros with references it will start a thread for updating
 * references of the macros parameters that are a document reference.
 *
 * @version $Id$
 * @since 1.29
 */
@Component
@Named(PageRenameMacroParametersUpdateListener.ROLE_HINT)
@Singleton
public class PageRenameMacroParametersUpdateListener extends AbstractEventListener implements Disposable
{
    protected static final String ROLE_HINT = "PageRenameMacroParametersUpdateListener";

    private static final Map<String, List<String>> MACROS_TO_UPDATE = Map.of(
        "button", List.of("url"),
        "confluence_pagetree", List.of("root")
        );

    /**
     * Thread that will handle updating the reference of a macro parameter after a page rename.
     */
    public Thread referencesMacroThread;

    @Inject
    protected JobContext jobContext;

    @Inject
    protected ObservationContext observationContext;

    @Inject
    private UpdateReferencesRunnable referencesMacroRunnable;

    @Inject
    private Logger logger;

    /**
     * Constructor.
     */
    public PageRenameMacroParametersUpdateListener()
    {
        super(ROLE_HINT, new DocumentCreatedEvent());
    }

    @Override
    public void onEvent(Event event, Object source, Object data)
    {
        if (this.referencesMacroThread == null) {
            startThreads();
        }

        if (observationContext.isIn(new JobStartedEvent("refactoring/rename"))) {
            Job job = jobContext.getCurrentJob();
            DocumentReference destinationRef = job.getRequest().getProperty("destination");
            List<DocumentReference> references = job.getRequest().getProperty("entityReferences");

            if (references != null && !references.isEmpty()) {
                XWikiDocument currentDoc = (XWikiDocument) source;

                DocumentReference originalDocRef = references.get(0);
                DocumentReference currentDocRef = currentDoc.getDocumentReference();

                if (destinationRef.equals(currentDocRef)) {
                    startUpdatingReferences((XWikiContext) data, currentDoc, originalDocRef);
                }
            }
        }
    }

    @Override
    public void dispose()
    {
        try {
            stopThread(this.referencesMacroThread, this.referencesMacroRunnable);
        } catch (InterruptedException e) {
            logger.warn("References backlinks update thread interrupted", e);
        }
    }

    private void startUpdatingReferences(XWikiContext context, XWikiDocument currentDoc,
        DocumentReference originalDocRef)
    {
        UpdateReferencesQueueEntry updateReferencesQueueEntry = new UpdateReferencesQueueEntry(
            originalDocRef,
            currentDoc.getDocumentReference(),
            MACROS_TO_UPDATE
        );

        try {
            // Restrain the number of documents added to queue to only those that have backlinks. We need to take
            // backlinks from the original document because at this step they are not loaded to the new document.
            if (!context.getWiki().getDocument(originalDocRef, context).getBackLinkedReferences(context).isEmpty()) {
                this.referencesMacroRunnable.addToQueue(updateReferencesQueueEntry);
            }
        } catch (XWikiException e) {
            logger.warn("Error when getting backlinks of renamed document");
        }
    }

    private synchronized void startThreads()
    {
        if (this.referencesMacroThread == null) {
            Thread referencesThread = new Thread(this.referencesMacroRunnable);
            referencesThread.setName("Update Button Links Thread");
            referencesThread.setDaemon(true);
            referencesThread.start();
        }
    }

    private void stopThread(Thread referencesThread, UpdateReferencesRunnable referenceRunnable)
        throws InterruptedException
    {
        if (referencesThread != null) {
            referenceRunnable.addToQueue(UpdateReferencesRunnable.STOP_RUNNABLE_ENTRY);
            referencesThread.join();
        }
    }
}
