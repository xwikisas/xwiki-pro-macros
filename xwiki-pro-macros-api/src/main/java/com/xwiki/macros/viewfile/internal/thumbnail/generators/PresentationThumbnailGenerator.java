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
package com.xwiki.macros.viewfile.internal.thumbnail.generators;

import java.awt.Graphics2D;
import java.awt.Color;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;

import javax.imageio.ImageIO;
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Provider;
import javax.inject.Singleton;

import org.apache.poi.hslf.usermodel.HSLFSlideShow;
import org.apache.poi.sl.usermodel.Slide;
import org.apache.poi.sl.usermodel.SlideShow;
import org.apache.poi.xslf.usermodel.XMLSlideShow;
import org.slf4j.Logger;
import org.xwiki.component.annotation.Component;
import org.xwiki.model.reference.AttachmentReference;

import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.doc.XWikiDocument;
import com.xwiki.macros.viewfile.internal.thumbnail.TemporaryFileManager;
import com.xwiki.macros.viewfile.thumbnail.generators.ThumbnailGenerator;

import net.coobird.thumbnailator.Thumbnails;

/**
 * {@link ThumbnailGenerator} implementation that handles the thumbnail generation for .ppt and .pptx extension files.
 *
 * @version $Id$
 * @since 1.29.0
 */
@Component
@Singleton
@Named(PresentationThumbnailGenerator.HINT)
public class PresentationThumbnailGenerator implements ThumbnailGenerator
{
    /**
     * Component hint.
     */
    public static final String HINT = "presentation";

    private static final String PPT_EXTENSION = "ppt";

    private static final String PPTX_EXTENSION = "pptx";

    @Inject
    private Provider<XWikiContext> wikiContextProvider;

    @Inject
    private TemporaryFileManager temporaryFileManager;

    @Inject
    private Logger logger;

    @Override
    public String generateThumbnail(AttachmentReference attachmentReference) throws Exception
    {

        XWikiContext wikiContext = wikiContextProvider.get();
        XWikiDocument document =
            wikiContext.getWiki().getDocument(attachmentReference.getDocumentReference(), wikiContext);
        String extension = getExtension(attachmentReference.getName());
        try (InputStream is = document.getAttachment(attachmentReference.getName())
            .getContentInputStream(wikiContext))
        {
            switch (extension) {
                case PPT_EXTENSION:
                    HSLFSlideShow ppt = new HSLFSlideShow(is);
                    return getSlideThumbnailURL(ppt, attachmentReference);
                case PPTX_EXTENSION:
                    XMLSlideShow pptx = new XMLSlideShow(is);
                    return getSlideThumbnailURL(pptx, attachmentReference);
                default:
                    logger.warn("Failed to identify the presentation file extension.");
                    return "";
            }
        }
    }

    private String getSlideThumbnailURL(SlideShow<?, ?> pptx, AttachmentReference attachmentReference) throws Exception
    {
        Slide<?, ?> slide = pptx.getSlides().get(0);
        int width = pptx.getPageSize().width;
        int height = pptx.getPageSize().height;
        BufferedImage img = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D graphics = getGraphics2D(img, width, height);
        slide.draw(graphics);

        return createSlideThumbnail(attachmentReference, img);
    }

    private static Graphics2D getGraphics2D(BufferedImage img, int width, int height)
    {
        Graphics2D graphics = img.createGraphics();
        graphics.setPaint(Color.white);
        graphics.fill(new Rectangle2D.Float(0, 0, width, height));
        return graphics;
    }

    private String createSlideThumbnail(AttachmentReference attachmentReference, BufferedImage img) throws Exception
    {
        BufferedImage resized = Thumbnails.of(img).size(150, 212).asBufferedImage();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(resized, JPG, baos);
        try (InputStream is = new ByteArrayInputStream(baos.toByteArray())) {
            return this.temporaryFileManager.createThumbnail(attachmentReference, is);
        }
    }

    private String getExtension(String fileName)
    {
        return fileName.substring(fileName.lastIndexOf('.') + 1).toLowerCase();
    }
}
