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

import java.util.Base64;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import javax.script.ScriptContext;

import org.apache.commons.lang.exception.ExceptionUtils;
import org.xwiki.component.annotation.Component;
import org.xwiki.model.reference.AttachmentReference;
import org.xwiki.model.reference.AttachmentReferenceResolver;
import org.xwiki.rendering.block.Block;
import org.xwiki.rendering.macro.MacroExecutionException;
import org.xwiki.rendering.transformation.MacroTransformationContext;
import org.xwiki.script.ScriptContextManager;
import org.xwiki.template.Template;
import org.xwiki.template.TemplateManager;

import com.xwiki.macros.AbstractProMacro;
import com.xwiki.macros.viewfile.internal.AttachmentSizeValidator;
import com.xwiki.macros.viewfile.internal.ThumbnailGenerator;
import com.xwiki.macros.viewfile.macro.ViewFileMacroParameters;

/**
 * View File macro: Display a file in different rendering ways.
 *
 * @version $Id$
 * @since 1.27
 */
@Component
@Named("view-file")
@Singleton
public class ViewFileMacro extends AbstractProMacro<ViewFileMacroParameters>
{
    @Inject
    private TemplateManager templateManager;

    @Inject
    private ScriptContextManager scriptContextManager;

    @Inject
    private ThumbnailGenerator thumbnailGenerator;

    @Inject
    @Named("current")
    private AttachmentReferenceResolver<String> attachmentReferenceResolver;

    @Inject
    private AttachmentSizeValidator attachmentSizeValidator;

    /**
     * Create and initialize the descriptor of the macro.
     */
    public ViewFileMacro()
    {
        super("View file", "Show a file using PDF Viewer Macro or Office Viewer.", ViewFileMacroParameters.class);
    }

    @Override
    public boolean supportsInlineMode()
    {
        return true;
    }

    @Override
    protected List<Block> internalExecute(ViewFileMacroParameters parameters, String content,
        MacroTransformationContext context) throws MacroExecutionException
    {
        try {
            AttachmentReference attachRef = attachmentReferenceResolver.resolve(parameters.getName());
            boolean isOversize = attachmentSizeValidator.isAttachmentOversize(attachRef);
            byte[] thumbnailBytes = new byte[0];
            // Get the thumbnail data only if the display is thumbnail and the attachment is not oversize.
            if (!isOversize) {
                thumbnailBytes = thumbnailGenerator.getThumbnailData(attachRef);
            }
            Template customTemplate = this.templateManager.getTemplate("viewfile/viewFileTemplate.vm");
            ScriptContext scriptContext = scriptContextManager.getScriptContext();

            scriptContext.setAttribute("params", parameters, ScriptContext.ENGINE_SCOPE);
            scriptContext.setAttribute("isInline", context.isInline(), ScriptContext.ENGINE_SCOPE);
            String base64 = Base64.getEncoder().encodeToString(thumbnailBytes);
            scriptContext.setAttribute("thumbnailBase64", base64, ScriptContext.ENGINE_SCOPE);
            scriptContext.setAttribute("isOversize", isOversize, ScriptContext.ENGINE_SCOPE);
            if (context.getTransformationContext().getTargetSyntax() != null) {
                String targetSyntaxId = context.getTransformationContext().getTargetSyntax().getType().getId();
                scriptContext.setAttribute("targetSyntaxId", targetSyntaxId, ScriptContext.ENGINE_SCOPE);
            }
            return this.templateManager.execute(customTemplate).getChildren();
        } catch (Exception e) {
            throw new MacroExecutionException(ExceptionUtils.getRootCauseMessage(e));
        }
    }
}
