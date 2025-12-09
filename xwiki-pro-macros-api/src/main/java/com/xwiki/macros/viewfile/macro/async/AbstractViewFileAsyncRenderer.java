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
package com.xwiki.macros.viewfile.macro.async;

import java.util.Map;

import org.xwiki.model.reference.AttachmentReference;
import org.xwiki.rendering.async.internal.block.AbstractBlockAsyncRenderer;
import org.xwiki.rendering.transformation.MacroTransformationContext;
import org.xwiki.stability.Unstable;

/**
 * Abstract class used to add a default initialization for {@link AbstractBlockAsyncRenderer}.
 *
 * @version $Id$
 * @since 1.29.0
 */
@Unstable
public abstract class AbstractViewFileAsyncRenderer extends AbstractBlockAsyncRenderer
{
    /**
     * Initialize the resources needed in the execution of the async renderer.
     * @param context the content passed to the execution of the async renderer
     * @param attachmentRef the reference of the attachment used in the async rendering execution
     * @param parameters additional parameters needed in the execution
     */
    public void initialize(MacroTransformationContext context, AttachmentReference attachmentRef,
        Map<String, String> parameters)
    {
    }
}
