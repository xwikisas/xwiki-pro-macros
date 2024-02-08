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

import org.xwiki.model.reference.DocumentReference;
import org.xwiki.stability.Unstable;

/**
 * Excerpt Queue Entry.
 *
 * @version $Id$
 * @since 1.14.4
 */
@Unstable
public class ExcerptQueueEntry
{
    /**
     * Reference to the page before rename operation.
     */
    public DocumentReference originalDocRef;

    /**
     * Reference to the page after rename operation.
     */
    public DocumentReference currentDocRef;

    /**
     * Constructor.
     *
     * @param originalDocRef document reference before rename
     * @param currentDocRef document reference after rename
     */
    public ExcerptQueueEntry(DocumentReference originalDocRef, DocumentReference currentDocRef)
    {
        this.originalDocRef = originalDocRef;
        this.currentDocRef = currentDocRef;
    }
}
