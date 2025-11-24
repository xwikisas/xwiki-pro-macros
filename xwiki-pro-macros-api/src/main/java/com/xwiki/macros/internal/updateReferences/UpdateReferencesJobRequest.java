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

import java.util.Set;

import org.xwiki.job.AbstractRequest;
import org.xwiki.model.reference.DocumentReference;

/**
 * Update References Job Request.
 *
 * @version $Id$
 * @since 1.29
 */
public class UpdateReferencesJobRequest extends AbstractRequest
{
    /**
     * Reference to the page before rename operation.
     */
    private final DocumentReference currentDocRef;

    /**
     * Reference to the page after rename operation.
     */
    private final DocumentReference targetDocRef;

    /**
     * A Set containing the macros id's of whose parameters may reference other documents.
     */
    private final Set<String> macrosToUpdate;

    /**
     * Constructor.
     *
     * @param currentDocRef document reference before rename
     * @param targetDocRef document reference after rename
     * @param macrosToUpdate the macros to update Map
     */
    public UpdateReferencesJobRequest(DocumentReference currentDocRef, DocumentReference targetDocRef,
        Set<String> macrosToUpdate)
    {
        this.currentDocRef = currentDocRef;
        this.targetDocRef = targetDocRef;
        this.macrosToUpdate = macrosToUpdate;

        setId("update-references/"
            + (currentDocRef != null ? currentDocRef.toString() : "unknown"));
    }

    /**
     * @return the reference to the document before the rename
     */
    public DocumentReference getCurrentDocRef()
    {
        return currentDocRef;
    }

    /**
     * @return the reference to the document after the rename
     */
    public DocumentReference getTargetDocRef()
    {
        return targetDocRef;
    }

    /**
     * @return the set of macros where references should be updated
     */
    public Set<String> getMacrosToUpdate()
    {
        return macrosToUpdate;
    }
}
