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

import java.util.Collections;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import org.xwiki.component.annotation.Component;
import org.xwiki.localization.ContextualLocalizationManager;
import org.xwiki.model.reference.AttachmentReference;
import org.xwiki.rendering.RenderingException;
import org.xwiki.rendering.async.internal.block.AbstractBlockAsyncRenderer;
import org.xwiki.rendering.block.Block;
import org.xwiki.rendering.block.CompositeBlock;
import org.xwiki.rendering.block.FormatBlock;
import org.xwiki.rendering.block.ImageBlock;
import org.xwiki.rendering.block.MacroBlock;
import org.xwiki.rendering.block.MacroMarkerBlock;
import org.xwiki.rendering.block.WordBlock;
import org.xwiki.rendering.listener.Format;
import org.xwiki.rendering.listener.reference.ResourceReference;
import org.xwiki.rendering.listener.reference.ResourceType;
import org.xwiki.rendering.syntax.Syntax;
import org.xwiki.rendering.transformation.MacroTransformationContext;

import com.xwiki.macros.viewfile.internal.ThumbnailGenerator;
import com.xwiki.macros.viewfile.internal.macro.StaticBlockWrapperFactory;

@Component(roles = ViewFileAsyncThumbnailRenderer.class)
public class ViewFileAsyncThumbnailRenderer extends AbstractBlockAsyncRenderer
{
    public static final String HINT = "thumbnail";

    private static final String CLASS = "class";

    @Inject
    private ContextualLocalizationManager contextLocalization;

    @Inject
    private ThumbnailGenerator thumbnailGenerator;

    private AttachmentReference attachmentReference;

    private MacroTransformationContext transformationContext;

    private boolean isInline;

    private Syntax targetSyntax;

    private List<String> id;

    public void initialize(MacroTransformationContext context, AttachmentReference attachmentReference,
        boolean isInline)
    {
        this.isInline = isInline;
        this.transformationContext = context;
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
            List<Block> result = List.of(getImageThumbnail(isInline, attachmentReference));
            MacroBlock currentMacro = transformationContext.getCurrentMacroBlock();
            if (currentMacro != null) {
                result = Collections.singletonList(
                    new MacroMarkerBlock(
                        currentMacro.getId(),
                        currentMacro.getParameters(),
                        currentMacro.getContent(),
                        result,
                        currentMacro.isInline()
                    )
                );
            }
            return new CompositeBlock(result);
        } catch (Exception e) {
            throw new RenderingException("Failed to render asynchronously the work items displayer [{}].", e);
        }
    }

    private Block getImageThumbnail(boolean isSpan, AttachmentReference attachmentReference) throws Exception
    {
        String base64 = thumbnailGenerator.generateThumbnailBase64(attachmentReference);
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

        return StaticBlockWrapperFactory.constructBlockWrapper(isSpan, List.of(imageBlock, overlay),
            Map.of(CLASS, "image-container"));
    }
}
