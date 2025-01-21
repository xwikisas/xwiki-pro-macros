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
     * @see #getName()
     */
    private String name = "";

    /**
     * @see #isNopanel()
     */
    private boolean nopanel;

    /**
     * @see #isInline()
     */
    private boolean inline;

    /**
     * Retrieves the reference to the document from which the excerpt is to be included.
     * @since 1.14.5
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
    @PropertyDescription("The reference of the document containing the excerpt to display")
    @PropertyId("0")
    @PropertyMandatory
    @PropertyName("Reference")
    public void setReference(DocumentReference reference)
    {
        this.reference = reference;
    }

    /**
     * @return the name of the excerpt to be included.
     * @since 1.18.0
     */
    public String getName()
    {
        return this.name;
    }

    /**
     * Sets the name of the excerpt to be included.
     *
     * @param name the name to set.
     * @since 1.18.0
     */
    @PropertyDescription("The name of the excerpt to be displayed")
    @PropertyName("Name")
    public void setName(String name)
    {
        this.name = name == null ? "" : name;
    }


    /**
     * @return the name of the excerpt to be included.
     * @since 1.19.0
     */
    public boolean isNopanel()
    {
        return this.nopanel;
    }

    /**
     * Sets the name of the excerpt to be included.
     *
     * @param nopanel the name to set.
     * @since 1.19.0
     */
    @PropertyDescription("Disable the panel")
    @PropertyName("no panel")
    public void setNopanel(boolean nopanel)
    {
        this.nopanel = nopanel;
    }


    /**
     * @return whether the macro is explicitly in inline mode.
     * @since 1.25.1
     */
    public boolean isInline()
    {
        return this.inline;
    }

    /**
     * Explicitly sets inline mode. Note that if the macro is used inline, it will be inline.
     *
     * @param inline the name to set.
     * @since 1.25.1
     */
    @PropertyDescription("Use the macro in inline mode. When in inline mode, the panel parameter is ignored.' What "
        + "do you think")
    @PropertyName("inline")
    public void setInline(boolean inline)
    {
        this.inline = inline;
    }
}
