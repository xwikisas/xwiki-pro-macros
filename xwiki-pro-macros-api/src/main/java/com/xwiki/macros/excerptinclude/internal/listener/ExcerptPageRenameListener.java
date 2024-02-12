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
package com.xwiki.macros.excerptinclude.internal.listener;

import java.util.List;

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
import org.xwiki.rendering.block.Block;
import org.xwiki.rendering.block.XDOM;
import org.xwiki.rendering.block.match.MacroBlockMatcher;
import org.xwiki.stability.Unstable;

import com.xpn.xwiki.doc.XWikiDocument;
import com.xwiki.macros.excerptinclude.internal.AbstractExcerptIncludeRunnable;
import com.xwiki.macros.excerptinclude.internal.ExcerptIncludeMacroRunnable;
import com.xwiki.macros.excerptinclude.internal.ExcerptQueueEntry;

/**
 * Listens to rename of excerpt pages and starts a thread for updating references of the excerpt-include macro.
 *
 * @version $Id$
 * @since 1.14.4
 */
@Component
@Named(ExcerptPageRenameListener.ROLE_HINT)
@Singleton
@Unstable
public class ExcerptPageRenameListener extends AbstractEventListener implements Disposable
{
    /**
     * The role hint of the document.
     */
    protected static final String ROLE_HINT = "PageRenameEventListener";

    /**
     * Thread that will handle updating the reference of an excerpt-include macro after an excerpt rename.
     */
    private Thread excerptMacroThread;

    @Inject
    private ObservationContext observationContext;

    @Inject
    private JobContext jobContext;

    @Inject
    private Logger logger;

    @Inject
    private ExcerptIncludeMacroRunnable excerptIncludeMacroRunnable;

    /**
     * Constructor.
     */
    public ExcerptPageRenameListener()
    {
        super(ROLE_HINT, new DocumentCreatedEvent());
    }

    @Override
    public void onEvent(Event event, Object source, Object data)
    {
        if (this.excerptMacroThread == null) {
            startThreads();
        }

        if (observationContext.isIn(new JobStartedEvent("refactoring/rename"))) {
            XWikiDocument currentDoc = (XWikiDocument) source;
            XDOM backlinkDocXDOM = currentDoc.getXDOM();
            List<Block> excerptMacroBlocks = backlinkDocXDOM.getBlocks(new MacroBlockMatcher("excerpt"),
                Block.Axes.DESCENDANT);
            if (excerptMacroBlocks.isEmpty()) {
                return;
            }

            Job job = jobContext.getCurrentJob();
            DocumentReference destinationRef = job.getRequest().getProperty("destination");
            List<DocumentReference> references = job.getRequest().getProperty("entityReferences");
            if (references != null && !references.isEmpty()) {
                DocumentReference originalDocRef = references.get(0);
                DocumentReference currentDocRef = currentDoc.getDocumentReference();

                if (destinationRef.equals(currentDocRef)) {
                    startContentUpdating(currentDoc, originalDocRef);
                }
            }
        }
    }

    /**
     * Add entries in the queue of threads that update reference parameter of excerpt-include macro after renaming an
     * excerpt.
     *
     * @param currentDoc current document
     * @param originalDocRef the reference of the document before rename
     */
    public void startContentUpdating(XWikiDocument currentDoc, DocumentReference originalDocRef)
    {
        ExcerptQueueEntry queueEntry = new ExcerptQueueEntry(originalDocRef, currentDoc.getDocumentReference());
        this.excerptIncludeMacroRunnable.addToQueue(queueEntry);
    }

    /**
     * Multiple rename jobs could be started at very close dates at the moment when the threads were not initialized yet
     * (for example, at installation step) and we need to be sure that only a single instance of each thread is
     * created.
     */
    public synchronized void startThreads()
    {
        if (this.excerptMacroThread == null) {
            this.excerptMacroThread =
                startThread(this.excerptIncludeMacroRunnable, "Update Excerpt Include Macro Thread");
        }
    }

    /**
     * Actions for starting a thread.
     *
     * @param excerptIncludeRunnable runnable object that implements the run method
     * @param threadName name of the thread
     * @return thread that was started
     */
    public Thread startThread(AbstractExcerptIncludeRunnable excerptIncludeRunnable, String threadName)
    {
        Thread excerptThread = new Thread(excerptIncludeRunnable);
        excerptThread.setName(threadName);
        excerptThread.setDaemon(true);
        excerptThread.start();

        return excerptThread;
    }

    /**
     * Actions for closing a thread.
     *
     * @param excerptThread thread to be stopped
     * @param excerptIncludeRunnable runnable object of the thread
     * @throws InterruptedException if any thread has interrupted the current thread
     */
    public void stopThread(Thread excerptThread, AbstractExcerptIncludeRunnable excerptIncludeRunnable)
        throws InterruptedException
    {
        if (excerptThread != null) {
            excerptIncludeRunnable.addToQueue(AbstractExcerptIncludeRunnable.STOP_RUNNABLE_ENTRY);
            excerptThread.join();
        }
    }

    @Override
    public void dispose()
    {
        try {
            stopThread(this.excerptMacroThread, this.excerptIncludeMacroRunnable);
        } catch (InterruptedException e) {
            logger.warn("Excerpt backlinks update thread interrupted", e);
        }
    }
}
