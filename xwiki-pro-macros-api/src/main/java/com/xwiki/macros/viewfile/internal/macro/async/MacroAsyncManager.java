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

import java.util.Map;
import java.util.Set;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.slf4j.Logger;
import org.xwiki.component.annotation.Component;
import org.xwiki.component.manager.ComponentLookupException;
import org.xwiki.component.manager.ComponentManager;
import org.xwiki.model.reference.AttachmentReference;
import org.xwiki.rendering.async.internal.AsyncRendererConfiguration;
import org.xwiki.rendering.async.internal.AsyncRendererExecutor;
import org.xwiki.rendering.async.internal.AsyncRendererExecutorResponse;
import org.xwiki.rendering.async.internal.AsyncRendererResult;
import org.xwiki.rendering.transformation.MacroTransformationContext;

import com.xpn.xwiki.internal.context.XWikiContextContextStore;
import com.xwiki.macros.viewfile.internal.macro.ViewFileMacro;
import com.xwiki.macros.viewfile.macro.async.AbstractViewFileAsyncRenderer;

/**
 * Handles the async rendering of the {@link ViewFileMacro} display blocks.
 *
 * @version $Id$
 * @since 1.29.0
 */
@Component(roles = MacroAsyncManager.class)
@Singleton
public class MacroAsyncManager
{
    @Inject
    private ComponentManager componentManager;

    @Inject
    private AsyncRendererExecutor asyncRendererExecutor;

    @Inject
    private Logger logger;

    /**
     * Execute the async renderer corresponding to the given parameters and return a placeholder if the execution is not
     * finished.
     *
     * @param attachmentReference the reference of the attachment used in the async rendering execution
     * @param isInline if the context should be set in line or not
     * @param parameters additional parameters needed in the execution
     * @param element the placeholder block element type
     * @param hint the hint for the {@link AbstractViewFileAsyncRenderer} implementation to be used
     * @return return a placeholder if the execution is not finished
     */
    public String getViewFileAsyncBlock(AttachmentReference attachmentReference, boolean isInline,
        Map<String, String> parameters, String element, String hint)
    {
        try {
            AsyncRendererConfiguration configuration = new AsyncRendererConfiguration();
            configuration.setPlaceHolderForced(true);
            MacroTransformationContext context = new MacroTransformationContext();
            context.setInline(isInline);

            AbstractViewFileAsyncRenderer asyncRenderer =
                getAsyncRenderer(configuration, attachmentReference, parameters, hint, context);
            AsyncRendererExecutorResponse response = asyncRendererExecutor.render(asyncRenderer, configuration);
            AsyncRendererResult result = response.getStatus().getResult();

            if (result != null) {
                return result.getResult();
            } else {
                return String.format(
                    "<%s class=\"xwiki-async\" data-xwiki-async-id=\"%s\" data-xwiki-async-client-id=\"%s\"></%s>",
                    element, response.getJobIdHTTPPath(), response.getAsyncClientId(), element);
            }
        } catch (Exception e) {
            logger.error("There was an error while attempting to execute the async renderer.", e);
            throw new RuntimeException("Failed to execute the async renderer. Please check the logs for more info.");
        }
    }

    private AbstractViewFileAsyncRenderer getAsyncRenderer(AsyncRendererConfiguration configuration,
        AttachmentReference attachmentReference, Map<String, String> parameters, String hint,
        MacroTransformationContext context) throws ComponentLookupException
    {
        AbstractViewFileAsyncRenderer asyncRenderer =
            componentManager.getInstance(AbstractViewFileAsyncRenderer.class, hint);
        // Pass some properties that might be of interest for the executed async block.
        configuration.setContextEntries(Set.of(XWikiContextContextStore.PROP_USER, XWikiContextContextStore.PROP_WIKI,
            XWikiContextContextStore.PROP_ACTION, XWikiContextContextStore.PROP_LOCALE,
            XWikiContextContextStore.PREFIX_PROP_DOCUMENT, XWikiContextContextStore.PREFIX_PROP_REQUEST));
        asyncRenderer.initialize(context, attachmentReference, parameters);
        return asyncRenderer;
    }
}
