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
package com.xwiki.macros.viewfile.internal.macro.async;

import java.util.Set;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.xwiki.component.annotation.Component;
import org.xwiki.component.manager.ComponentLookupException;
import org.xwiki.component.manager.ComponentManager;
import org.xwiki.job.JobException;
import org.xwiki.model.reference.AttachmentReference;
import org.xwiki.rendering.RenderingException;
import org.xwiki.rendering.async.internal.AsyncRendererConfiguration;
import org.xwiki.rendering.async.internal.AsyncRendererExecutor;
import org.xwiki.rendering.async.internal.AsyncRendererExecutorResponse;
import org.xwiki.rendering.async.internal.AsyncRendererResult;
import org.xwiki.rendering.transformation.MacroTransformationContext;

import com.xpn.xwiki.internal.context.XWikiContextContextStore;

@Component(roles = MacroAsyncManager.class)
@Singleton
public class MacroAsyncManager
{
    @Inject
    private ComponentManager componentManager;

    @Inject
    private AsyncRendererExecutor asyncRendererExecutor;

    public String getThumbnailAsyncBlock(AttachmentReference attachmentReference, boolean isSpan)
        throws ComponentLookupException, JobException, RenderingException
    {
        AsyncRendererConfiguration configuration = new AsyncRendererConfiguration();
        configuration.setPlaceHolderForced(true);
        ViewFileAsyncThumbnailRenderer asyncRenderer = getAsyncRenderer(configuration, attachmentReference, isSpan,
            ViewFileAsyncThumbnailRenderer.HINT);
        AsyncRendererExecutorResponse response = asyncRendererExecutor.render(asyncRenderer, configuration);
        AsyncRendererResult result = response.getStatus().getResult();
        if (result != null && !configuration.isPlaceHolderForced()) {
            return result.getResult();
        } else {
            return String.format("<span class=\"xwiki-async\" data-xwiki-async-id=\"%s\" "
                    + "data-xwiki-async-client-id=\"%s\"></span>", response.getJobIdHTTPPath(),
                response.getAsyncClientId());
        }
    }

    private ViewFileAsyncThumbnailRenderer getAsyncRenderer(AsyncRendererConfiguration configuration,
        AttachmentReference attachmentReference, boolean isSpan, String hint) throws ComponentLookupException
    {
        ViewFileAsyncThumbnailRenderer asyncRenderer =
            componentManager.getInstance(ViewFileAsyncThumbnailRenderer.class);
        // Pass some properties that might be of interest the executed async block.
        configuration.setContextEntries(Set.of(XWikiContextContextStore.PROP_USER, XWikiContextContextStore.PROP_WIKI,
            XWikiContextContextStore.PROP_ACTION, XWikiContextContextStore.PROP_LOCALE,
            XWikiContextContextStore.PREFIX_PROP_DOCUMENT));

        MacroTransformationContext context = new MacroTransformationContext();
        asyncRenderer.initialize(context, attachmentReference, isSpan);
        return asyncRenderer;
    }
}
