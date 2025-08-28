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
import java.util.HashMap;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.apache.commons.lang3.StringUtils;
import org.xwiki.component.annotation.Component;
import org.xwiki.model.EntityType;
import org.xwiki.model.reference.AttachmentReference;
import org.xwiki.model.reference.AttachmentReferenceResolver;
import org.xwiki.rendering.block.Block;
import org.xwiki.rendering.macro.MacroExecutionException;
import org.xwiki.rendering.syntax.Syntax;
import org.xwiki.rendering.syntax.SyntaxType;
import org.xwiki.rendering.transformation.MacroTransformationContext;
import org.xwiki.script.ScriptContextManager;
import org.xwiki.security.authorization.ContextualAuthorizationManager;
import org.xwiki.security.authorization.Right;

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
    protected ContextualAuthorizationManager contextualAuthorization;

    private boolean isOversize;

    private String base64;

    @Inject
    private ScriptContextManager scriptContextManager;

    @Inject
    @Named("current")
    private AttachmentReferenceResolver<String> attachmentReferenceResolver;

    @Inject
    private AttachmentSizeValidator attachmentSizeValidator;

    @Inject
    private ThumbnailGenerator thumbnailGenerator;

    @Inject
    private ViewFileMacroPrepareBlocks viewFileMacroPrepareBlocks;

    /**
     * Create and initialize the descriptor of the macro.
     */
    public ViewFileMacro()
    {
        super("View file", "Show a file using PDF Viewer Macro or Office Viewer.", ViewFileMacroParameters.class);
    }

    @Override
    public List<Block> internalExecute(ViewFileMacroParameters parameters, String content,
        MacroTransformationContext context) throws MacroExecutionException
    {
        try {
            String fileName = resolveFileName(parameters);

            if (StringUtils.isBlank(fileName)) {
                return viewFileMacroPrepareBlocks.errorMessage(context, "rendering.macro.viewFile.attachmentrequired");
            }

            AttachmentReference attachmentRef = createAttachmentReference(fileName);

            if (!userCanView(attachmentRef)) {
                return viewFileMacroPrepareBlocks.errorMessage(context, "rendering.macro.viewFile.norights");
            }

            boolean oversize = attachmentSizeValidator.isAttachmentOversize(attachmentRef);
            String base64Thumbnail = oversize ? null : generateThumbnailBase64(attachmentRef);

            return List.of(StaticBlockWrapperFactory.constructBlockWrapper(context.isInline(),
                viewFileMacroPrepareBlocks.prepareBlocks(parameters, context, attachmentRef, base64Thumbnail, oversize,
                    inEditMode(context)), new HashMap<>()));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public boolean supportsInlineMode()
    {
        return true;
    }

    private AttachmentReference createAttachmentReference(String fileName)
    {
        return new AttachmentReference(attachmentReferenceResolver.resolve(fileName, EntityType.ATTACHMENT));
    }

    private boolean userCanView(AttachmentReference attachmentRef)
    {
        return contextualAuthorization.hasAccess(Right.VIEW, attachmentRef);
    }

    private String generateThumbnailBase64(AttachmentReference attachmentRef)
    {
        byte[] thumbnailData = thumbnailGenerator.getThumbnailData(attachmentRef);
        return Base64.getEncoder().encodeToString(thumbnailData);
    }

    private String resolveFileName(ViewFileMacroParameters parameters)
    {
        if (StringUtils.isNotBlank(parameters.getName())) {
            return parameters.getName();
        }
        return parameters.getAttFilename();
    }

    private boolean inEditMode(MacroTransformationContext context)
    {
        boolean editMode;
        Syntax syntax = context.getTransformationContext().getTargetSyntax();
        // TODO remove after upgrade to 17.0.0+ https://jira.xwiki.org/browse/XWIKI-22738
        // Sadly in versions < 17.0.0 the syntax is not set in the context and to be able to handle different
        // displays for view and edit mode we have to use the scriptContextManger who has a variable in the
        // attributes that we can use to identify if we are in edit mode or not.
        if (syntax == null) {
            editMode = inEditModeFallBack();
        } else {
            SyntaxType targetSyntaxType = syntax.getType();
            editMode = SyntaxType.ANNOTATED_HTML.equals(targetSyntaxType) || SyntaxType.ANNOTATED_XHTML.equals(
                targetSyntaxType);
        }
        return editMode;
    }

    private boolean inEditModeFallBack()
    {
        String syntax = (String) scriptContextManager.getScriptContext().getAttribute("syntaxType");
        return (syntax != null) && (syntax.equals("annotatedhtml") || syntax.equals("annotatedxhtml"));
    }
}
