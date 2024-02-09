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

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.xwiki.stability.Unstable;

import com.xpn.xwiki.util.AbstractXWikiRunnable;

/**
 * Base class for excerpt Runnable. It provides tools for working with excerptsQueue.
 *
 * @version $Id$
 * @since 1.14.4
 */
@Unstable
public abstract class AbstractExcerptIncludeRunnable extends AbstractXWikiRunnable
{
    /**
     * Stop runnable entry.
     */
    public static final ExcerptQueueEntry STOP_RUNNABLE_ENTRY = new ExcerptQueueEntry(null, null);

    /**
     * Entries to be processed by this thread.
     */
    private final BlockingQueue<ExcerptQueueEntry> excerptsQueue = new LinkedBlockingQueue<>();

    @Inject
    private Logger logger;

    /**
     * Add entries to the thread's queue.
     *
     * @param queueEntry the entry to be added
     */
    public void addToQueue(ExcerptQueueEntry queueEntry)
    {
        this.excerptsQueue.add(queueEntry);
    }

    /**
     * Process new excerpt entry of queue.
     *
     * @return queueEntry the new excerpt entry
     */
    public ExcerptQueueEntry getNextExcerptQueueEntry()
    {
        ExcerptQueueEntry queueEntry;

        try {
            queueEntry = this.excerptsQueue.take();
        } catch (InterruptedException e) {
            logger.warn("Excerpts update thread has been interrupted", e);
            queueEntry = STOP_RUNNABLE_ENTRY;
        }

        if (queueEntry == STOP_RUNNABLE_ENTRY) {
            this.excerptsQueue.clear();
        }

        return queueEntry;
    }
}
