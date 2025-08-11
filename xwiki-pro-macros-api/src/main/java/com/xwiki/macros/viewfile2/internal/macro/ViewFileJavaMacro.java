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
package com.xwiki.macros.viewfile2.internal.macro;

import java.io.IOException;
import java.util.Base64;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Provider;
import javax.inject.Singleton;

import org.apache.commons.lang3.StringUtils;
import org.xwiki.component.annotation.Component;
import org.xwiki.localization.ContextualLocalizationManager;
import org.xwiki.model.EntityType;
import org.xwiki.model.reference.AttachmentReference;
import org.xwiki.model.reference.AttachmentReferenceResolver;
import org.xwiki.model.reference.EntityReferenceSerializer;
import org.xwiki.model.reference.LocalDocumentReference;
import org.xwiki.rendering.block.Block;
import org.xwiki.rendering.block.FormatBlock;
import org.xwiki.rendering.block.GroupBlock;
import org.xwiki.rendering.block.ImageBlock;
import org.xwiki.rendering.block.LinkBlock;
import org.xwiki.rendering.block.MacroBlock;
import org.xwiki.rendering.block.WordBlock;
import org.xwiki.rendering.listener.Format;
import org.xwiki.rendering.listener.reference.ResourceReference;
import org.xwiki.rendering.listener.reference.ResourceType;
import org.xwiki.rendering.macro.AbstractMacro;
import org.xwiki.rendering.macro.MacroExecutionException;
import org.xwiki.rendering.syntax.Syntax;
import org.xwiki.rendering.syntax.SyntaxType;
import org.xwiki.rendering.transformation.MacroTransformationContext;
import org.xwiki.script.ScriptContextManager;
import org.xwiki.security.authorization.ContextualAuthorizationManager;
import org.xwiki.security.authorization.Right;

import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.XWikiException;
import com.xwiki.macros.viewfile.internal.AttachmentSizeValidator;
import com.xwiki.macros.viewfile.internal.ThumbnailGenerator;
import com.xwiki.macros.viewfile2.macro.ViewFileDisplay;
import com.xwiki.macros.viewfile2.macro.ViewFileJavaParameters;

/**
 * View File macro: Display a file in different rendering ways.
 *
 * @version $Id$
 * @since 1.27
 */
@Component
@Named("simpleViewFile")
@Singleton
public class ViewFileJavaMacro extends AbstractMacro<ViewFileJavaParameters>
{
    private static final String PPT = "ppt";

    private static final String PPTX = "pptx";

    private static final String ODP = "odp";

    private static final Set<String> OFFICE_FILE_EXTENSIONS =
        Set.of(PPT, PPTX, ODP, "doc", "docx", "odt", "xls", "xlsx", "ods");

    private static final Set<String> PRESENTATION_FILE_EXTENSIONS = Set.of(PPT, PPTX, ODP);

    private static final LocalDocumentReference PDF_VIEWER_REFERENCE =
        new LocalDocumentReference(List.of("XWiki"), "PDFViewerMacro");

    private static final String PDF = "pdf";

    private static final String PX = "px";

    private static final String CLASS = "class";

    private static final String STYLE = "style";

    private static final String DOWNLOAD = "download";

    private static final String DEFAULT_WIDTH = "100%";

    private static final String DEFAULT_HEIGHT = "1000";

    private static final String TRUE_STRING = "true";

    private static final String ERROR_MACRO_ID = "error";

    private static final String FALSE_STRING = "false";

    @Inject
    protected ContextualAuthorizationManager contextualAuthorization;

    private boolean isOversize;

    private String base64;

    @Inject
    private ViewFileExternalBlockManager viewFileExternalBlockManager;

    @Inject
    private ViewFileResourceManager viewFIleResourceManager;

    @Inject
    @Named("current")
    private AttachmentReferenceResolver<String> attachmentReferenceResolver;

    @Inject
    private ScriptContextManager scriptContextManager;

    @Inject
    private ContextualLocalizationManager contextLocalization;

    @Inject
    private Provider<XWikiContext> contextProvider;

    @Inject
    private EntityReferenceSerializer<String> referenceSerializer;

    @Inject
    private AttachmentSizeValidator attachmentSizeValidator;

    @Inject
    private ThumbnailGenerator thumbnailGenerator;

    /**
     * Create and initialize the descriptor of the macro.
     */
    public ViewFileJavaMacro()
    {
        super("simpleViewFile", "test description", ViewFileJavaParameters.class);
    }

    @Override
    public List<Block> execute(ViewFileJavaParameters parameters, String content, MacroTransformationContext context)
        throws MacroExecutionException
    {

        try {
            // Prerequisites
            String fileName = resolveFileName(parameters);
            // If the fileName is still empty then we don't have an attachment, and we can't continue with the macro.
            if (!StringUtils.isNotBlank(fileName)) {
                return errorMessage(context, "rendering.macro.viewFile.attachmentrequired");
            }

            AttachmentReference attachmentReference =
                new AttachmentReference(attachmentReferenceResolver.resolve(fileName, EntityType.ATTACHMENT));

            if (!contextualAuthorization.hasAccess(Right.VIEW, attachmentReference)) {
                return errorMessage(context, "rendering.macro.viewFile.norights");
            }
            viewFIleResourceManager.injectBaseResources();
            prepareAttachmentIMAGE(attachmentReference);

            return prepareBlocks(parameters, content, context, fileName, attachmentReference);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public boolean supportsInlineMode()
    {
        return true;
    }

    private void prepareAttachmentIMAGE(AttachmentReference attachmentReference) throws IOException, XWikiException
    {
        isOversize = attachmentSizeValidator.isAttachmentOversize(attachmentReference);
        if (!isOversize) {
            base64 = Base64.getEncoder().encodeToString(thumbnailGenerator.getThumbnailData(attachmentReference));
        }
    }

    private String resolveFileName(ViewFileJavaParameters parameters)
    {
        if (StringUtils.isNotBlank(parameters.getName())) {
            return parameters.getName();
        }
        return parameters.getAttFilename();
    }

    private List<Block> prepareBlocks(ViewFileJavaParameters parameters, String content,
        MacroTransformationContext context, String fileName, AttachmentReference attachmentReference) throws Exception
    {
        switch (parameters.getDisplay()) {
            case full:
                return prepareFullDisplay(parameters, content, context, fileName, attachmentReference);
            case button:
            case thumbnail:
                return prepareThumbnailButton(parameters, content, context, fileName, attachmentReference);
            default:
                errorMessage(context, "Error key");
        }
        return null;
    }

    private String getFileExtension(String fileName)
    {
        return fileName.substring(fileName.lastIndexOf(".") + 1).toLowerCase();
    }

    private boolean hasPreview(String fileExtension) throws XWikiException
    {
        return ((fileExtension.equals(PDF) && isPDFViewerMacroInstalled())
            || OFFICE_FILE_EXTENSIONS.contains(fileExtension) && !isOversize);
    }

    private boolean shouldForceCardView(ViewFileDisplay display, boolean context)
    {
        return display == ViewFileDisplay.thumbnail || display == ViewFileDisplay.full || display == null && !context;
    }

    private void applyPresentationResourcesIfNeeded(String fileExtension)
    {
        if (PRESENTATION_FILE_EXTENSIONS.contains(fileExtension)) {
            viewFIleResourceManager.injectPresentationResources();
        }
    }

    private List<Block> prepareThumbnailButton(ViewFileJavaParameters parameters, String content,
        MacroTransformationContext context, String fileName, AttachmentReference attachmentReference)
        throws Exception
    {

        String fileExtension = getFileExtension(fileName);
        applyPresentationResourcesIfNeeded(fileExtension);

        boolean inEdit = inEditMode(context);
        boolean hasPreview = hasPreview(fileExtension);
        boolean forceCardView = shouldForceCardView(parameters.getDisplay(), context.isInline());
        boolean inLineElement = inEdit || context.isInline();
        // False == card, true == Button
        boolean thumbnailStyle = false;

        String style = "";
        StringBuilder stringBuilder = new StringBuilder();

        if (StringUtils.isNotBlank(parameters.getWidth())) {
            stringBuilder.append(String.format(" width:%s;", parameters.getWidth()));
        }

        if (StringUtils.isNotBlank(parameters.getHeight())) {
            stringBuilder.append(String.format(" height:%s;", parameters.getHeight()));
        }

        if (stringBuilder.length() > 0) {
            style = stringBuilder.toString();
            thumbnailStyle = false;
        } else if (forceCardView && inEdit) {
            style = "width: min-content; min-height: min-content;";
            thumbnailStyle = false;
        } else {
            thumbnailStyle = !forceCardView;
        }

        String thumbnailType = thumbnailStyle ? "Button" : "Card";
        String buttonClass = thumbnailStyle ? "button button-primary" : "";

        Map<String, String> rootElementParameters =
            Map.of(CLASS, String.format("viewFileThumbnail viewFileThumbnail%s", thumbnailType), STYLE, style,
                "data-preview", hasPreview ? TRUE_STRING : FALSE_STRING, "data-ref",
                referenceSerializer.serialize(attachmentReference));

        String url = contextProvider.get().getWiki().getURL(attachmentReference, contextProvider.get());

        Map<String, String> linkElementParameters = Map.of("href", url, DOWNLOAD, DOWNLOAD, CLASS, buttonClass, "title",
            contextLocalization.getTranslationPlain("rendering.macro.viewFile.thumbnail.button.title"));

        List<Block> innerContainer = getFileDisplayBlocks(thumbnailStyle, inLineElement, attachmentReference);
        ResourceReference reference = new ResourceReference(url, ResourceType.ATTACHMENT);
        Block linkBlock = new LinkBlock(innerContainer, reference, false, linkElementParameters);
        Block wrapperBlock =
            StaticBlockWrapperFactory.constructBlockWrapper(inLineElement, List.of(linkBlock), rootElementParameters);
        return List.of(wrapperBlock);
    }

    private List<Block> getFileDisplayBlocks(boolean thumbnailStyle, boolean isSpan,
        AttachmentReference attachmentReference) throws Exception
    {
        Block thumbunalBlock = getThumbnail(thumbnailStyle, attachmentReference, isSpan);
        Block titleTextBlock = new WordBlock(attachmentReference.getName());
        Block titleBlock = new FormatBlock(List.of(titleTextBlock), Format.NONE, Map.of(CLASS, "viewFileName"));
        return List.of(thumbunalBlock, titleBlock);
    }

    private Block getThumbnail(boolean thumbnailStyle, AttachmentReference attachmentReference, boolean isSpan)
        throws Exception

    {
        if (base64 != null && (!base64.isEmpty() && !thumbnailStyle)) {
            return getImageThumbnail(isSpan);
        }
        // Generic thumbnail.
        return viewFileExternalBlockManager.getMimeTypeBlock(attachmentReference, thumbnailStyle);
    }

    private Block getImageThumbnail(boolean isSpan)
    {

        String imageAltTranslation =
            contextLocalization.getTranslationPlain("rendering.macro.viewFile.thumbnail.button.image.alt");
        String overlayTextTranslation =
            contextLocalization.getTranslationPlain("rendering.macro.viewFile.thumbnail.button.overlay");
        String imageBase64 = String.format("data:image/jpeg;base64,%s", base64);
        ResourceReference reference = new ResourceReference(imageBase64, ResourceType.DATA);
        Block imageBlock =
            new ImageBlock(reference, false, Map.of(CLASS, "viewfile-thumbnail-image", "alt", imageAltTranslation));
        Block overlayText =
            new FormatBlock(List.of(new WordBlock(overlayTextTranslation)), Format.NONE, Map.of(CLASS, "overlay-text"));

        Block overlay =
            StaticBlockWrapperFactory.constructBlockWrapper(isSpan, List.of(overlayText), Map.of(CLASS, "overlay"));

        Block imageContainer = StaticBlockWrapperFactory.constructBlockWrapper(isSpan, List.of(imageBlock, overlay),
            Map.of(CLASS, "image-container"));
        return imageContainer;
    }

    private List<Block> prepareFullDisplay(ViewFileJavaParameters parameters, String content,
        MacroTransformationContext context, String fileName, AttachmentReference attachmentReference) throws Exception
    {
        boolean isEdit = inEditMode(context);
        if (isEdit || isOversize) {
            // If we are in edit or the file is too big to be displayed we just show the thumbnail.
            return prepareThumbnailButton(parameters, content, context, fileName, attachmentReference);
        }
        String fileExtension = getFileExtension(fileName);

        if (OFFICE_FILE_EXTENSIONS.contains(fileExtension)) {
            prepareOfficeFile(fileExtension, parameters, attachmentReference, content);
        } else if (fileExtension.equalsIgnoreCase(PDF) && isPDFViewerMacroInstalled()) {
            preparePDF(fileName, parameters);
        }
        // Fallback if the file extension is not a known one.
        return prepareThumbnailButton(parameters, content, context, fileName, attachmentReference);
    }

    private List<Block> preparePDF(String fileName, ViewFileJavaParameters parameters)
    {
        String width = processDimensionsUnit(parameters.getWidth(), DEFAULT_WIDTH, false);
        String height = processDimensionsUnit(parameters.getHeight(), DEFAULT_HEIGHT, false);
        return List.of(new MacroBlock("pdfviewer", Map.of("file", fileName, "height", height, "width", width), false));
    }

    private List<Block> prepareOfficeFile(String fileExtension, ViewFileJavaParameters parameters,
        AttachmentReference attachmentReference, String content)
    {
        //TODO escape
        String width = processDimensionsUnit(parameters.getWidth(), DEFAULT_WIDTH, true);
        String height = processDimensionsUnit(parameters.getHeight(), DEFAULT_HEIGHT + PX, true);
        Block officeMacroBlock = new MacroBlock("office",
            Collections.singletonMap("reference", referenceSerializer.serialize(attachmentReference)), content, false);
        String style = String.format("width:%s; height:%s; overflow:auto", width, height);
        String elementClass = "viewFileFull " + (PRESENTATION_FILE_EXTENSIONS.contains(fileExtension) ? "box" : "");
        Block groupBlock = new GroupBlock(List.of(officeMacroBlock), Map.of(CLASS, elementClass, STYLE, style));
        return List.of(groupBlock);
    }

    private boolean isPDFViewerMacroInstalled() throws XWikiException
    {
        return !this.contextProvider.get().getWiki().getDocument(PDF_VIEWER_REFERENCE, contextProvider.get()).isNew();
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

    /**
     * Sadly, the office and pdfviewer are not using compatible units, and we have to make sure that we properly process
     * them.
     */
    private String processDimensionsUnit(String value, String defaultValue, boolean addUnit)
    {
        if (StringUtils.isNotBlank(value)) {
            if (addUnit) {
                boolean hasUnit = value.endsWith("%") || value.endsWith(PX);
                return hasUnit ? value : value + PX;
            } else {
                return value.endsWith(PX) ? StringUtils.removeEnd(value, PX) : value;
            }
        }
        return defaultValue;
    }

    /**
     * Genereates and {{error}}key{{/error}} macro.
     *
     * @param context macro context
     * @param key a valid translation key
     */
    private List<Block> errorMessage(MacroTransformationContext context, String key)
    {
        // Inline Mod
        String message = contextLocalization.getTranslationPlain(key);
        if (context.isInline()) {
            return List.of(new MacroBlock(ERROR_MACRO_ID, new HashMap<>(), message, true));
        } else {
            return List.of(new MacroBlock(ERROR_MACRO_ID, new HashMap<>(), message, false));
        }
    }

    private boolean inEditModeFallBack()
    {
        String syntax = (String) scriptContextManager.getScriptContext().getAttribute("syntaxType");
        return (syntax != null) && (syntax.equals("annotatedhtml") || syntax.equals("annotatedxhtml"));
    }
}
