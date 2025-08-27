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
package com.xwiki.macros.viewfile.internal.macro;

import java.util.List;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.inject.Singleton;
import javax.script.ScriptContext;

import org.xwiki.component.annotation.Component;
import org.xwiki.model.reference.AttachmentReference;
import org.xwiki.rendering.block.Block;
import org.xwiki.rendering.block.FormatBlock;
import org.xwiki.rendering.listener.Format;
import org.xwiki.script.ScriptContextManager;
import org.xwiki.template.Template;
import org.xwiki.template.TemplateManager;

import com.xpn.xwiki.XWikiContext;

/**
 * Handles the creation of blocks that come from velocity defined macros.
 *
 * @version $Id$
 * @since 1.27
 */
@Component(roles = ViewFileExternalBlockManager.class)
@Singleton
public class ViewFileExternalBlockManager
{
    @Inject
    private TemplateManager templateManager;

    @Inject
    private ScriptContextManager scriptContextManager;

    @Inject
    private Provider<XWikiContext> contextProvider;

    /**
     * Returns the block that is used for displaying a generic thumbnail.
     *
     * @param attachmentReference reference of the attachment.
     * @param isButtonView is the view is button.
     * @return a block that represents a thumbnail.
     */
    public Block getMimeTypeBlock(AttachmentReference attachmentReference, boolean isButtonView) throws Exception
    {
        // False == card, true == Button
        String elementType = "div";
        if (isButtonView) {
            elementType = "span";
        }
        XWikiContext context = contextProvider.get();
        Template customTemplate = this.templateManager.getTemplate("viewfile/viewFileMimeType.vm");
        ScriptContext scriptContext = scriptContextManager.getScriptContext();
        scriptContext.setAttribute("attachment", context.getWiki().getDocument(attachmentReference.getParent(), context)
            .getAttachment(attachmentReference.getName()), ScriptContext.ENGINE_SCOPE);

        scriptContext.setAttribute("elem", elementType, ScriptContext.ENGINE_SCOPE);
        return this.templateManager.execute(customTemplate).getChildren().get(0);
    }

    /**
     * Handles the creation of the Collabora blocks.
     *
     * @return blocks needed for the Collabora integration.
     */
    public Block getCollaboraBlock() throws Exception
    {

        Template customTemplate = this.templateManager.getTemplate("viewfile/viewFileCollaboraIntegration.vm");
        Block callCollaboraVeloictyMacros = this.templateManager.execute(customTemplate).getChildren().get(0);
        return new FormatBlock(List.of(callCollaboraVeloictyMacros), Format.NONE);
    }
}
