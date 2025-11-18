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

import org.xwiki.model.reference.DocumentReference;

/**
 * Update References Queue Entry.
 *
 * @version $Id$
 * @since 1.29
 */
public class UpdateReferencesQueueEntry
{
    /**
     * Reference to the page before rename operation.
     */
    private final DocumentReference originalDocRef;

    /**
     * Reference to the page after rename operation.
     */
    private final DocumentReference currentDocRef;

    /**
     * A map containing the macros whose parameters may reference other documents. The key is the macro name, and the
     * value is the list of parameter names that should be interpreted as document references and updated accordingly.
     */
    private final Map<String, List<String>> macrosToUpdate;

    /**
     * Constructor.
     *
     * @param originalDocRef document reference before rename
     * @param currentDocRef document reference after rename
     * @param macrosToUpdate the macros to update Map
     */
    public UpdateReferencesQueueEntry(DocumentReference originalDocRef, DocumentReference currentDocRef,
        Map<String, List<String>> macrosToUpdate)
    {
        this.originalDocRef = originalDocRef;
        this.currentDocRef = currentDocRef;
        this.macrosToUpdate = macrosToUpdate;
    }

    /**
     * @return the reference to the document before the rename
     */
    public DocumentReference getOriginalDocRef()
    {
        return originalDocRef;
    }

    /**
     * @return the reference to the document after the rename
     */
    public DocumentReference getCurrentDocRef()
    {
        return currentDocRef;
    }

    /**
     * @return the map of macros and their parameter names
     */
    public Map<String, List<String>> getMacrosToUpdate()
    {
        return macrosToUpdate;
    }
}
