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

import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Named;

import org.xwiki.component.annotation.Component;
import org.xwiki.localization.ContextualLocalizationManager;
import org.xwiki.model.reference.AttachmentReference;
import org.xwiki.rendering.RenderingException;
import org.xwiki.rendering.block.Block;
import org.xwiki.rendering.block.CompositeBlock;
import org.xwiki.rendering.block.FormatBlock;
import org.xwiki.rendering.block.ImageBlock;
import org.xwiki.rendering.block.WordBlock;
import org.xwiki.rendering.listener.Format;
import org.xwiki.rendering.listener.reference.ResourceReference;
import org.xwiki.rendering.listener.reference.ResourceType;
import org.xwiki.rendering.syntax.Syntax;
import org.xwiki.rendering.transformation.MacroTransformationContext;

import com.xwiki.macros.viewfile.internal.thumbnail.ThumbnailGenerator;
import com.xwiki.macros.viewfile.internal.macro.StaticBlockWrapperFactory;
import com.xwiki.macros.viewfile.internal.macro.ViewFileExternalBlockManager;
import com.xwiki.macros.viewfile.internal.macro.ViewFileMacro;
import com.xwiki.macros.viewfile.macro.async.AbstractViewFileAsyncRenderer;

/**
 * Async renderer for generating the thumbnail display {@link Block} for {@link ViewFileMacro}.
 *
 * @version $Id$
 * @since 1.29.0
 */
@Component(roles = AbstractViewFileAsyncRenderer.class)
@Named(ViewFileAsyncThumbnailRenderer.HINT)
public class ViewFileAsyncThumbnailRenderer extends AbstractViewFileAsyncRenderer
{
    /**
     * Component hint.
     */
    public static final String HINT = "thumbnail";

    private static final String CLASS = "class";

    @Inject
    private ContextualLocalizationManager contextLocalization;

    @Inject
    private ThumbnailGenerator thumbnailGenerator;

    @Inject
    private ViewFileExternalBlockManager viewFileExternalBlockManager;

    private AttachmentReference attachmentReference;

    private boolean isInline;

    private Syntax targetSyntax;

    private List<String> id;

    @Override
    public void initialize(MacroTransformationContext context, AttachmentReference attachmentReference,
        Map<String, String> parameters)
    {
        this.isInline = context.isInline();
        this.targetSyntax = context.getTransformationContext().getTargetSyntax();
        this.attachmentReference = attachmentReference;
        id =
            createId("rendering", "macro", "viewfile", HINT, String.valueOf(attachmentReference.toString().hashCode()));
    }

    @Override
    public boolean isInline()
    {
        return isInline;
    }

    @Override
    public Syntax getTargetSyntax()
    {
        return this.targetSyntax;
    }

    @Override
    public List<String> getId()
    {
        return this.id;
    }

    @Override
    public boolean isAsyncAllowed()
    {
        return true;
    }

    @Override
    public boolean isCacheAllowed()
    {
        return false;
    }

    @Override
    protected Block execute(boolean async, boolean cached) throws RenderingException
    {
        try {
            List<Block> result = List.of(getImageThumbnail(attachmentReference));
            return new CompositeBlock(result);
        } catch (Exception e) {
            throw new RenderingException("Failed to render asynchronously the work items displayer [{}].", e);
        }
    }

    private Block getImageThumbnail(AttachmentReference attachmentReference) throws Exception
    {
        String base64 = generateThumbnailBase64(attachmentReference);
        if (base64.isEmpty()) {
            return viewFileExternalBlockManager.getMimeTypeBlock(attachmentReference, isInline);
        }
        return getThumbnailBlock(base64);
    }

    private String generateThumbnailBase64(AttachmentReference attachmentRef)
    {
        return thumbnailGenerator.getThumbnailData(attachmentRef);
    }

    private Block getThumbnailBlock(String base64)
    {
        String imageAltTranslation =
            contextLocalization.getTranslationPlain("rendering.macro.viewFile.thumbnail.button.image.alt");
        String overlayTextTranslation =
            contextLocalization.getTranslationPlain("rendering.macro.viewFile.thumbnail.button.overlay");
        ResourceReference reference = new ResourceReference(base64, ResourceType.URL);
        Block imageBlock =
            new ImageBlock(reference, false, Map.of(CLASS, "viewfile-thumbnail-image", "alt", imageAltTranslation));
        Block overlayText =
            new FormatBlock(List.of(new WordBlock(overlayTextTranslation)), Format.NONE, Map.of(CLASS, "overlay-text"));
        Block overlay =
            StaticBlockWrapperFactory.constructBlockWrapper(isInline, List.of(overlayText), Map.of(CLASS, "overlay"));
        return StaticBlockWrapperFactory.constructBlockWrapper(isInline, List.of(imageBlock, overlay),
            Map.of(CLASS, "image-container"));
    }
}
