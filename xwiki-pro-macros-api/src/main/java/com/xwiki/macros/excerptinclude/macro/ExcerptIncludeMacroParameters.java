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
package com.xwiki.macros.excerptinclude.macro;

import org.xwiki.model.reference.DocumentReference;
import org.xwiki.properties.annotation.PropertyDescription;
import org.xwiki.properties.annotation.PropertyId;
import org.xwiki.properties.annotation.PropertyMandatory;
import org.xwiki.properties.annotation.PropertyName;
import org.xwiki.stability.Unstable;

/**
 * Defines parameters for the {@link com.xwiki.macros.excerptinclude.internal.macro.ExcerptIncludeMacro} Macro.
 *
 * @version $Id$
 * @since 1.14.5
 */
@Unstable
public class ExcerptIncludeMacroParameters
{
    /**
     * @see #getReference()
     */
    private DocumentReference reference;

    /**
     * Retrieves the reference to the document from which the excerpt is to be included.
     *
     * @return A {@link DocumentReference} object representing the document reference.
     */
    public DocumentReference getReference()
    {
        return this.reference;
    }

    /**
     * Sets the reference to the document from which the excerpt is to be included.
     *
     * @param reference A {@link DocumentReference} object representing the document reference.
     */
    @PropertyDescription("The reference of the resource to display")
    @PropertyId("0")
    @PropertyMandatory
    @PropertyName("Reference")
    public void setReference(DocumentReference reference)
    {
        this.reference = reference;
    }
}
