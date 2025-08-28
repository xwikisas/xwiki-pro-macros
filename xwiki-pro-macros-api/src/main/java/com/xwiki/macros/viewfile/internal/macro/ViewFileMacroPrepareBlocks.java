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

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.inject.Singleton;

import org.apache.commons.lang3.StringUtils;
import org.xwiki.component.annotation.Component;
import org.xwiki.localization.ContextualLocalizationManager;
import org.xwiki.model.reference.AttachmentReference;
import org.xwiki.model.reference.DocumentReference;
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
import org.xwiki.rendering.transformation.MacroTransformationContext;

import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.XWikiException;
import com.xwiki.macros.viewfile.macro.ViewFileDisplay;
import com.xwiki.macros.viewfile.macro.ViewFileMacroParameters;

/**
 * Handles the preparation of the rendering blocks.
 *
 * @version $Id$
 * @since 1.27
 */
@Component(roles = ViewFileMacroPrepareBlocks.class)
@Singleton
public class ViewFileMacroPrepareBlocks
{
    private static final String PPT = "ppt";

    private static final String PPTX = "pptx";

    private static final String ODP = "odp";

    private static final Set<String> OFFICE_FILE_EXTENSIONS =
        Set.of(PPT, PPTX, ODP, "doc", "docx", "odt", "xls", "xlsx", "ods");

    private static final Set<String> PRESENTATION_FILE_EXTENSIONS = Set.of(PPT, PPTX, ODP);

    private static final LocalDocumentReference PDF_VIEWER_REFERENCE =
        new LocalDocumentReference(List.of("XWiki"), "PDFViewerMacro");

    private static final LocalDocumentReference COLLABORA_REFERENCE =
        new LocalDocumentReference(List.of("Collabora", "Code"), "Configuration");

    private static final String PDF = "pdf";

    private static final String PX = "px";

    private static final String CLASS = "class";

    private static final String STYLE = "style";

    private static final String DOWNLOAD = "download";

    private static final String DEFAULT_WIDTH = "100%";

    private static final String DEFAULT_HEIGHT = "1000";

    private static final String ERROR_MACRO_ID = "error";

    private boolean isOversize;

    private boolean inEditMode;

    private String base64;

    @Inject
    private ViewFileExternalBlockManager viewFileExternalBlockManager;

    @Inject
    private ViewFileResourceManager viewFileResourceManager;

    @Inject
    private ContextualLocalizationManager contextLocalization;

    @Inject
    private Provider<XWikiContext> contextProvider;

    @Inject
    private EntityReferenceSerializer<String> referenceSerializer;

    /**
     * Prepares the blocks of the view file macro.
     *
     * @param parameters view file macro parameters
     * @param context transformation context of the macro
     * @param inEditMode true if the macro is in edit mode, false otherwise
     * @param attachmentReference reference of the file that should be displayed
     * @param base64 representation of the preview if it exists.
     * @param isOversize if the file is too big and should not be displayed in full view.
     * @return a list of blocks that will render the macro.
     */
    public List<Block> prepareBlocks(ViewFileMacroParameters parameters, MacroTransformationContext context,
        AttachmentReference attachmentReference, String base64, boolean isOversize, boolean inEditMode) throws Exception
    {
        viewFileResourceManager.injectBaseResources();
        this.isOversize = isOversize;
        this.base64 = base64;
        this.inEditMode = inEditMode;

        if (parameters.getDisplay() == ViewFileDisplay.full) {
            return prepareFullDisplay(parameters, context, attachmentReference.getName(), attachmentReference);
        }

        return prepareCompactDisplay(parameters, context, attachmentReference.getName(), attachmentReference);
    }

    /**
     * Generates the blocks needed for an {{error}}key{{/error}} macro.
     *
     * @param context macro context
     * @param key a valid translation key
     * @return list of blocks that represents an error macro.
     */
    public List<Block> errorMessage(MacroTransformationContext context, String key)
    {
        // Inline Mod
        String message = contextLocalization.getTranslationPlain(key);
        return List.of(new MacroBlock(ERROR_MACRO_ID, new HashMap<>(), message, context.isInline()));
    }

    private List<Block> prepareCompactDisplay(ViewFileMacroParameters parameters, MacroTransformationContext context,
        String fileName, AttachmentReference attachmentReference) throws Exception
    {

        String fileExtension = getFileExtension(fileName);
        applyPresentationResourcesIfNeeded(fileExtension);

        boolean hasPreview = hasPreview(fileExtension);
        boolean forceCardView = shouldForceCardView(parameters.getDisplay(), context.isInline());
        boolean inLineElement = inEditMode || context.isInline();
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
        } else if (forceCardView && inEditMode) {
            style = "width: min-content; min-height: min-content;";
            thumbnailStyle = false;
        } else {
            thumbnailStyle = !forceCardView;
        }

        String thumbnailType = thumbnailStyle ? "Button" : "Card";
        String buttonClass = thumbnailStyle ? "button button-primary" : "";

        Map<String, String> rootElementParameters =
            Map.of(CLASS, String.format("viewFileThumbnail viewFileThumbnail%s", thumbnailType), STYLE, style,
                "data-preview", Boolean.toString(hasPreview), "data-ref",
                referenceSerializer.serialize(attachmentReference));

        Map<String, String> linkElementParameters = Map.of(DOWNLOAD, DOWNLOAD, CLASS, buttonClass, "title",
            contextLocalization.getTranslationPlain("rendering.macro.viewFile.thumbnail.button.title"));

        List<Block> innerContainer = getFileDisplayBlocks(thumbnailStyle, inLineElement, attachmentReference);
        ResourceReference reference =
            new ResourceReference(referenceSerializer.serialize(attachmentReference), ResourceType.ATTACHMENT);
        Block linkBlock = new LinkBlock(innerContainer, reference, false, linkElementParameters);
        List<Block> innerBlocks = new ArrayList<>();
        innerBlocks.add(linkBlock);

        // Check if Collabora is present.
        if (isApplicationInstalled(COLLABORA_REFERENCE)) {
            Block collaboraBlock = viewFileExternalBlockManager.getCollaboraBlock();
            innerBlocks.add(collaboraBlock);
        }
        Block wrapperBlock =
            StaticBlockWrapperFactory.constructBlockWrapper(inLineElement, innerBlocks, rootElementParameters);
        return List.of(wrapperBlock);
    }

    private Block getImageThumbnail(boolean isSpan)
    {

        String imageAltTranslation =
            contextLocalization.getTranslationPlain("rendering.macro.viewFile.thumbnail.button.image.alt");
        String overlayTextTranslation =
            contextLocalization.getTranslationPlain("rendering.macro.viewFile.thumbnail.button.overlay");
        String imageBase64 = "data:image/jpeg;base64," + base64;
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

    private List<Block> prepareFullDisplay(ViewFileMacroParameters parameters, MacroTransformationContext context,
        String fileName, AttachmentReference attachmentReference) throws Exception
    {
        if (inEditMode || isOversize || context.isInline()) {
            // If we are in edit, the file is too big to be displayed or the macro is inline we just show the thumbnail.
            return prepareCompactDisplay(parameters, context, fileName, attachmentReference);
        }
        String fileExtension = getFileExtension(fileName);

        if (OFFICE_FILE_EXTENSIONS.contains(fileExtension)) {
            return prepareOfficeFile(fileExtension, parameters, attachmentReference, "");
        } else if (fileExtension.equalsIgnoreCase(PDF) && isApplicationInstalled(PDF_VIEWER_REFERENCE)) {
            return preparePDF(fileName, parameters);
        }
        // Fallback if the file extension is not a known one.
        return prepareCompactDisplay(parameters, context, fileName, attachmentReference);
    }

    private List<Block> preparePDF(String fileName, ViewFileMacroParameters parameters)
    {
        String width = processDimensionsUnit(parameters.getWidth(), DEFAULT_WIDTH, false);
        String height = processDimensionsUnit(parameters.getHeight(), DEFAULT_HEIGHT, false);
        return List.of(new MacroBlock("pdfviewer", Map.of("file", fileName, "height", height, "width", width), false));
    }

    private List<Block> prepareOfficeFile(String fileExtension, ViewFileMacroParameters parameters,
        AttachmentReference attachmentReference, String content)
    {
        String width = processDimensionsUnit(parameters.getWidth(), DEFAULT_WIDTH, true);
        String height = processDimensionsUnit(parameters.getHeight(), DEFAULT_HEIGHT + PX, true);
        Block officeMacroBlock = new MacroBlock("office",
            Collections.singletonMap("reference", referenceSerializer.serialize(attachmentReference)), content, false);
        String style = String.format("width:%s; height:%s; overflow:auto", width, height);
        String elementClass = "viewFileFull " + (PRESENTATION_FILE_EXTENSIONS.contains(fileExtension) ? "box" : "");
        Block groupBlock = new GroupBlock(List.of(officeMacroBlock), Map.of(CLASS, elementClass, STYLE, style));
        return List.of(groupBlock);
    }

    private boolean isApplicationInstalled(LocalDocumentReference localDocumentReference) throws XWikiException
    {
        XWikiContext context = contextProvider.get();
        DocumentReference viewerWikiRef = new DocumentReference(localDocumentReference, context.getWikiReference());
        return context.getWiki().exists(viewerWikiRef, context);
    }

    private String getFileExtension(String fileName)
    {
        return fileName.substring(fileName.lastIndexOf(".") + 1).toLowerCase();
    }

    private boolean hasPreview(String fileExtension) throws XWikiException
    {
        return ((fileExtension.equals(PDF) && isApplicationInstalled(PDF_VIEWER_REFERENCE))
            || OFFICE_FILE_EXTENSIONS.contains(fileExtension) && !isOversize);
    }

    private boolean shouldForceCardView(ViewFileDisplay display, boolean context)
    {
        return display == ViewFileDisplay.thumbnail || display == ViewFileDisplay.full || display == null && !context;
    }

    private void applyPresentationResourcesIfNeeded(String fileExtension)
    {
        if (PRESENTATION_FILE_EXTENSIONS.contains(fileExtension)) {
            viewFileResourceManager.injectPresentationResources();
        }
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
        return viewFileExternalBlockManager.getMimeTypeBlock(attachmentReference, thumbnailStyle || isSpan);
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
}
