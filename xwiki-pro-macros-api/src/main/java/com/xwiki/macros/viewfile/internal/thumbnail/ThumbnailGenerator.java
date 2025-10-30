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
package com.xwiki.macros.viewfile.internal.thumbnail;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import javax.imageio.ImageIO;
import javax.inject.Inject;
import javax.inject.Provider;
import javax.inject.Singleton;
import javax.swing.ImageIcon;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.apache.poi.hslf.usermodel.HSLFSlide;
import org.apache.poi.hslf.usermodel.HSLFSlideShow;
import org.apache.poi.xslf.usermodel.XMLSlideShow;
import org.apache.poi.xslf.usermodel.XSLFSlide;
import org.jodconverter.core.document.DefaultDocumentFormatRegistry;
import org.jodconverter.core.office.OfficeException;
import org.jodconverter.core.office.OfficeManager;
import org.jodconverter.local.LocalConverter;
import org.jodconverter.local.office.ExternalOfficeManager;
import org.slf4j.Logger;
import org.xwiki.component.annotation.Component;
import org.xwiki.component.phase.Initializable;
import org.xwiki.component.phase.InitializationException;
import org.xwiki.model.reference.AttachmentReference;
import org.xwiki.officeimporter.server.OfficeServer;
import org.xwiki.officeimporter.server.OfficeServerConfiguration;

import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.doc.XWikiDocument;

import net.coobird.thumbnailator.Thumbnails;

/**
 * Generate a thumbnail image for office and PDF files.
 *
 * @version $Id$
 * @since 1.26.22
 */
@Component(roles = ThumbnailGenerator.class)
@Singleton
public class ThumbnailGenerator implements Initializable
{
    private static final List<String> OFFICE_EXTENSIONS = List.of("odp", "doc", "docx", "odt", "xls", "xlsx", "ods");

    private static final String PPT_EXTENSION = "ppt";

    private static final String PPTX_EXTENSION = "pptx";

    private static final List<String> PRESENTATION_EXTENSIONS = List.of(PPT_EXTENSION, PPTX_EXTENSION);

    private static final String JPG = "jpg";

    private static final String EMPTY_STRING = "";

    @Inject
    private Logger logger;

    @Inject
    private Provider<XWikiContext> wikiContextProvider;

    /**
     * The office server configuration.
     */
    @Inject
    private OfficeServerConfiguration officeServerConfig;

    @Inject
    private OfficeServer officeServer;

    @Inject
    private TemporaryFileManager temporaryFileManager;

    private OfficeManager officeManager;

    /**
     * Checks if a thumbnail already exists for the given attachment reference, and if not, attempts to create a
     * thumbnail image and returns the path to it.
     *
     * @param attachmentReference the reference of the file for which a thumbnail is requested.
     * @return the url to the thumbnail as a {@link String} if the image was found or successfully created, or an empty
     *     String if an error occurs or if the file extension is not supported.
     */
    public String getThumbnailUrl(AttachmentReference attachmentReference)
    {
        try {
            if (isOfficeServerConnected()) {
                if (!temporaryFileManager.thumbnailFileExists(attachmentReference)) {
                    return generateAndGetThumbnailUrlPath(attachmentReference);
                } else {
                    return temporaryFileManager.getThumbnailURL(attachmentReference);
                }
            } else {
                logger.warn("Unable to generate thumbnail. Office server is not connected.");
                return EMPTY_STRING;
            }
        } catch (Exception e) {
            logger.error("There was an error while attempting to get the thumbnail URL. Root cause is: [{}]",
                ExceptionUtils.getRootCauseMessage(e));
            return EMPTY_STRING;
        }
    }

    @Override
    public void initialize() throws InitializationException
    {
        // Set an execution timeout equivalent to 10 seconds.
        officeManager = ExternalOfficeManager.builder().portNumbers(officeServerConfig.getServerPorts())
            .taskExecutionTimeout(10000L).build();
        try {
            officeManager.start();
        } catch (OfficeException e) {
            throw new RuntimeException(e);
        }
    }

    private OfficeManager getOfficeManager() throws InitializationException
    {
        if (officeManager == null || !officeManager.isRunning()) {
            initialize();
        }
        return officeManager;
    }

    private String generateAndGetThumbnailUrlPath(AttachmentReference attachmentReference) throws Exception
    {
        String extension = getExtension(attachmentReference.getName());
        if (OFFICE_EXTENSIONS.contains(extension)) {
            return getOfficeThumbnailUrlPath(attachmentReference);
        } else if (PRESENTATION_EXTENSIONS.contains(extension)) {
            return getPresentationThumbnailUrlPath(attachmentReference, extension);
        } else if (extension.equals("pdf")) {
            return getPDFThumbnailUrlPath(attachmentReference);
        } else {
            logger.warn("Extension type not supported.");
            return EMPTY_STRING;
        }
    }

    private String getPDFThumbnailUrlPath(AttachmentReference attachmentReference) throws Exception
    {
        XWikiContext wikiContext = wikiContextProvider.get();
        XWikiDocument document =
            wikiContext.getWiki().getDocument(attachmentReference.getDocumentReference(), wikiContext);
        InputStream is = document.getAttachment(attachmentReference.getName()).getContentInputStream(wikiContext);
        // Load the PDF document.
        PDDocument pdDoc = PDDocument.load(is);
        PDFRenderer pdfRenderer = new PDFRenderer(pdDoc);
        // Select the first page (index starts at 0).
        return generateThumbnail(pdfRenderer.renderImageWithDPI(0, 150), attachmentReference);

    }

    private String getOfficeThumbnailUrlPath(AttachmentReference attachmentReference) throws Exception
    {
        byte[] bais = getJPEGContent(attachmentReference);
        return generateThumbnail(getBufferedImage(bais), attachmentReference);
    }

    private byte[] getJPEGContent(AttachmentReference attachmentReference) throws Exception
    {
        XWikiContext wikiContext = wikiContextProvider.get();
        XWikiDocument document =
            wikiContext.getWiki().getDocument(attachmentReference.getDocumentReference(), wikiContext);
        try (InputStream is = document.getAttachment(attachmentReference.getName()).getContentInputStream(wikiContext);
             ByteArrayOutputStream baos = new ByteArrayOutputStream())
        {
            OfficeManager manager = getOfficeManager();
            LocalConverter.make(manager).convert(is).to(baos).as(DefaultDocumentFormatRegistry.JPEG).execute();
            return baos.toByteArray();
        }
    }

    private String generateThumbnail(BufferedImage firstPage, AttachmentReference attachmentReference)
        throws Exception
    {
        // Resize to thumbnail
        BufferedImage resized = Thumbnails.of(firstPage).size(150, 212).asBufferedImage();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(resized, JPG, baos);
        try (InputStream is = new ByteArrayInputStream(baos.toByteArray())) {
            return this.temporaryFileManager.createThumbnail(attachmentReference, is);
        }
    }

    private BufferedImage getBufferedImage(byte[] result) throws IOException
    {
        byte[] imageBytes;

        if (isZip(result)) {
            try (ByteArrayInputStream bis = new ByteArrayInputStream(result);
                 ZipInputStream zis = new ZipInputStream(bis)) {
                ZipEntry entry = zis.getNextEntry();
                if (entry == null) {
                    throw new IOException("No image entries found in ZIP result");
                }
                // Read first image entry bytes
                imageBytes = zis.readAllBytes();
            }
        } else {
            imageBytes = result;
        }

        Image image = new ImageIcon(imageBytes).getImage();

        BufferedImage buffered = new BufferedImage(
            image.getWidth(null), image.getHeight(null), BufferedImage.TYPE_INT_RGB);
        Graphics2D g = buffered.createGraphics();
        g.drawImage(image, 0, 0, null);
        g.dispose();

        return buffered;
    }

    private boolean isZip(byte[] data)
    {
        return data.length >= 4 && data[0] == 0x50 && data[1] == 0x4B;
    }
    private String getPresentationThumbnailUrlPath(AttachmentReference attachmentReference, String extension)
        throws Exception
    {
        XWikiContext wikiContext = wikiContextProvider.get();
        XWikiDocument document =
            wikiContext.getWiki().getDocument(attachmentReference.getDocumentReference(), wikiContext);

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
                    return EMPTY_STRING;
            }
        }
    }

    private String getSlideThumbnailURL(HSLFSlideShow ppt, AttachmentReference attachmentReference) throws Exception
    {
        HSLFSlide slide = ppt.getSlides().get(0);
        int width = ppt.getPageSize().width;
        int height = ppt.getPageSize().height;
        BufferedImage img = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D graphics = img.createGraphics();
        graphics.setPaint(Color.white);
        graphics.fill(new Rectangle2D.Float(0, 0, width, height));
        slide.draw(graphics);

        return createSlideThumbnail(attachmentReference, img);
    }

    private String getSlideThumbnailURL(XMLSlideShow ppt, AttachmentReference attachmentReference) throws Exception
    {
        XSLFSlide slide = ppt.getSlides().get(0);
        int width = ppt.getPageSize().width;
        int height = ppt.getPageSize().height;
        BufferedImage img = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D graphics = img.createGraphics();
        graphics.setPaint(Color.white);
        graphics.fill(new Rectangle2D.Float(0, 0, width, height));
        slide.draw(graphics);

        return createSlideThumbnail(attachmentReference, img);
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

    private boolean isOfficeServerConnected()
    {
        this.officeServer.refreshState();
        return this.officeServer.getState() == OfficeServer.ServerState.CONNECTED;
    }
}
