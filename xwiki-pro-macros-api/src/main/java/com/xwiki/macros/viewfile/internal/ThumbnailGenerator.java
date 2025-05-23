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
package com.xwiki.macros.viewfile.internal;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.List;

import javax.imageio.ImageIO;
import javax.inject.Inject;
import javax.inject.Provider;
import javax.inject.Singleton;

import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.apache.poi.hslf.usermodel.HSLFSlide;
import org.apache.poi.hslf.usermodel.HSLFSlideShow;
import org.apache.poi.xslf.usermodel.XMLSlideShow;
import org.apache.poi.xslf.usermodel.XSLFSlide;
import org.jodconverter.core.document.DefaultDocumentFormatRegistry;
import org.jodconverter.core.office.OfficeManager;
import org.jodconverter.local.LocalConverter;
import org.jodconverter.local.office.ExternalOfficeManager;
import org.slf4j.Logger;
import org.xwiki.component.annotation.Component;
import org.xwiki.environment.Environment;
import org.xwiki.model.reference.AttachmentReference;

import com.xpn.xwiki.XWiki;
import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.doc.XWikiDocument;

import net.coobird.thumbnailator.Thumbnails;

/**
 * Utility class used to generate a thumbnail image for office and pdf files.
 *
 * @version $Id$
 * @since 1.26.20
 */
@Component(roles = ThumbnailGenerator.class)
@Singleton
public class ThumbnailGenerator
{
    private static final List<String> OFFICE_EXTENSIONS = List.of("odp", "doc", "docx", "odt", "xls", "xlsx", "ods");

    private static final String PPT_EXTENSION = "ppt";

    private static final String PPTX_EXTENSION = "pptx";

    private static final List<String> PRESENTATION_EXTENSIONS = List.of(PPT_EXTENSION, PPTX_EXTENSION);

    private static final String THUMBNAILS_PATH = "viewfilemacro/thumbnails/%s";

    private static final String JPG_EXTENSION = ".jpg";

    private static final String JPG = "jpg";

    @Inject
    private Logger logger;

    @Inject
    private Environment environment;

    @Inject
    private Provider<XWikiContext> wikiContextProvider;

    /**
     * Checks if a thumbnail already exists for the given attachment reference, and if not, attempts to create a
     * thumbnail image and returns the byte array for it.
     *
     * @param attachmentReference the reference of the file for which a thumbnail is requested.
     * @return the thumbnail content as a byte array if the image was found or successfully created, or an empty byte
     *     array if an error occurs or if the file extension is not supported.
     */
    public byte[] getThumbnailData(AttachmentReference attachmentReference)
    {
        try {
            File tempDir = new File(environment.getTemporaryDirectory(),
                String.format(THUMBNAILS_PATH, attachmentReference.getDocumentReference().toString()));

            File thumbnail = new File(tempDir, attachmentReference.getName() + JPG_EXTENSION);
            if (!thumbnail.exists()) {
                return generateAndGetThumbnailBytes(attachmentReference);
            } else {
                return Files.readAllBytes(thumbnail.toPath());
            }
        } catch (Exception e) {
            logger.error("There was an error while attempting to get the thumbnail byte data. Root cause is: [{}]",
                ExceptionUtils.getRootCauseMessage(e));
            return new byte[0];
        }
    }

    private byte[] generateAndGetThumbnailBytes(AttachmentReference attachmentReference) throws Exception
    {
        String extension = getExtension(attachmentReference.getName());
        if (OFFICE_EXTENSIONS.contains(extension)) {
            return getOfficeThumbnailBytes(attachmentReference);
        } else if (PRESENTATION_EXTENSIONS.contains(extension)) {
            return getPresentationThumbnailBytes(attachmentReference, extension);
        } else if (extension.equals("pdf")) {
            return getPDFThumbnailBytes(attachmentReference);
        } else {
            logger.warn("Extension type not supported.");
            return new byte[0];
        }
    }

    private byte[] getPDFThumbnailBytes(AttachmentReference attachmentReference) throws Exception
    {
        XWikiContext wikiContext = wikiContextProvider.get();
        XWiki wiki = wikiContext.getWiki();
        XWikiDocument document = wiki.getDocument(attachmentReference.getDocumentReference(), wikiContext);
        InputStream is = document.getAttachment(attachmentReference.getName()).getContentInputStream(wikiContext);
        return generateThumbnail(new ByteArrayInputStream(is.readAllBytes()), attachmentReference);
    }

    private byte[] getOfficeThumbnailBytes(AttachmentReference attachmentReference) throws Exception
    {
        ByteArrayInputStream bais = getPDFContent(attachmentReference);
        return generateThumbnail(bais, attachmentReference);
    }

    private ByteArrayInputStream getPDFContent(AttachmentReference attachmentReference) throws Exception
    {
        XWikiContext wikiContext = wikiContextProvider.get();
        XWiki wiki = wikiContext.getWiki();
        XWikiDocument document = wiki.getDocument(attachmentReference.getDocumentReference(), wikiContext);
        try (InputStream is = document.getAttachment(attachmentReference.getName())
            .getContentInputStream(wikiContext); ByteArrayOutputStream baos = new ByteArrayOutputStream())
        {
            OfficeManager manager = ExternalOfficeManager.builder().portNumbers(8100).build();
            manager.start();
            LocalConverter.make(manager).convert(is).to(baos).as(DefaultDocumentFormatRegistry.PDF).execute();
            manager.stop();
            return new ByteArrayInputStream(baos.toByteArray());
        }
    }

    private byte[] generateThumbnail(ByteArrayInputStream inputStream, AttachmentReference attachmentReference)
        throws Exception
    {
        // Load the PDF document.
        PDDocument document = PDDocument.load(inputStream);
        PDFRenderer pdfRenderer = new PDFRenderer(document);
        // Select the first page (index starts at 0).
        BufferedImage bim = pdfRenderer.renderImageWithDPI(0, 150);
        BufferedImage resized = Thumbnails.of(bim).size(150, 212).asBufferedImage();
        File tempDir = new File(environment.getTemporaryDirectory(),
            String.format(THUMBNAILS_PATH, attachmentReference.getDocumentReference().toString()));
        // Create directories if they don't exist.
        if (!tempDir.exists()) {
            tempDir.mkdirs();
        }
        File thumbnailFile = new File(tempDir, attachmentReference.getName() + JPG_EXTENSION);
        ImageIO.write(resized, JPG, thumbnailFile);
        document.close();
        return Files.readAllBytes(thumbnailFile.toPath());
    }

    private byte[] getPresentationThumbnailBytes(AttachmentReference attachmentReference, String extension)
        throws Exception
    {
        XWikiContext wikiContext = wikiContextProvider.get();
        XWiki wiki = wikiContext.getWiki();
        XWikiDocument document = wiki.getDocument(attachmentReference.getDocumentReference(), wikiContext);

        try (InputStream is = document.getAttachment(attachmentReference.getName())
            .getContentInputStream(wikiContext))
        {
            switch (extension) {
                case PPT_EXTENSION:
                    HSLFSlideShow ppt = new HSLFSlideShow(is);
                    return getSlideThumbnail(ppt, attachmentReference);
                case PPTX_EXTENSION:
                    XMLSlideShow pptx = new XMLSlideShow(is);
                    return getSlideThumbnail(pptx, attachmentReference);
                default:
                    logger.warn("Failed to identify the presentation file extension.");
                    return new byte[0];
            }
        }
    }

    private byte[] getSlideThumbnail(HSLFSlideShow ppt, AttachmentReference attachmentReference) throws IOException
    {
        HSLFSlide slide = ppt.getSlides().get(0);
        Dimension pageSize = ppt.getPageSize();
        BufferedImage img = new BufferedImage(pageSize.width, pageSize.height, BufferedImage.TYPE_INT_RGB);
        Graphics2D graphics = img.createGraphics();
        graphics.setPaint(Color.white);
        graphics.fill(new Rectangle2D.Float(0, 0, pageSize.width, pageSize.height));
        slide.draw(graphics);

        return getSlideBytes(attachmentReference, img);
    }

    private byte[] getSlideThumbnail(XMLSlideShow ppt, AttachmentReference attachmentReference) throws IOException
    {
        XSLFSlide slide = ppt.getSlides().get(0);
        Dimension pageSize = ppt.getPageSize();
        BufferedImage img = new BufferedImage(pageSize.width, pageSize.height, BufferedImage.TYPE_INT_RGB);
        Graphics2D graphics = img.createGraphics();
        graphics.setPaint(Color.white);
        graphics.fill(new Rectangle2D.Float(0, 0, pageSize.width, pageSize.height));
        slide.draw(graphics);

        return getSlideBytes(attachmentReference, img);
    }

    private byte[] getSlideBytes(AttachmentReference attachmentReference, BufferedImage img) throws IOException
    {
        BufferedImage resized = Thumbnails.of(img).size(150, 212).asBufferedImage();
        File tempDir = new File(environment.getTemporaryDirectory(),
            String.format(THUMBNAILS_PATH, attachmentReference.getDocumentReference().toString()));
        if (!tempDir.exists()) {
            tempDir.mkdirs();
        }

        File outputFile = new File(tempDir, attachmentReference.getName() + JPG_EXTENSION);
        ImageIO.write(resized, JPG, outputFile);

        return Files.readAllBytes(outputFile.toPath());
    }

    private String getExtension(String fileName)
    {
        return fileName.substring(fileName.lastIndexOf('.') + 1).toLowerCase();
    }
}
