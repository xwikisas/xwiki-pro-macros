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

import java.awt.Image;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import javax.swing.ImageIcon;

import org.slf4j.Logger;
import org.xwiki.component.annotation.Component;
import org.xwiki.model.reference.AttachmentReference;

import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.doc.XWikiDocument;
import com.xwiki.macros.viewfile.thumbnail.generators.ThumbnailGenerator;

/**
 * {@link ThumbnailGenerator} implementation that handles the thumbnail generation for .odp, .doc, .docx, .odt, .xls,
 * .xlsx, .ods extension files.
 *
 * @version $Id$
 * @since 1.29.0
 */
@Component
@Singleton
@Named(OfficeThumbnailGenerator.HINT)
public class OfficeThumbnailGenerator extends AbstractOfficePdfThumbnailGenerator
{
    /**
     * Component hint.
     */
    public static final String HINT = "office";

    @Inject
    private OfficeThumbnailGeneratorUtils officeUtils;

    @Inject
    private Logger logger;

    @Override
    public String generateThumbnail(AttachmentReference attachmentReference) throws Exception
    {
        if (officeUtils.isOfficeServerConnected()) {
            byte[] bais = getJPEGContent(attachmentReference);
            return saveThumbnail(getBufferedImage(bais), attachmentReference);
        } else {
            logger.warn("Unable to generate thumbnail for office file [{}]. Office server is not connected.",
                attachmentReference.toString());
            return "";
        }
    }

    private byte[] getJPEGContent(AttachmentReference attachmentReference) throws Exception
    {
        XWikiContext wikiContext = wikiContextProvider.get();
        XWikiDocument document =
            wikiContext.getWiki().getDocument(attachmentReference.getDocumentReference(), wikiContext);
        try (InputStream is = document.getAttachment(attachmentReference.getName()).getContentInputStream(wikiContext))
        {
            return officeUtils.getImageBytes(is);
        }
    }

    private BufferedImage getBufferedImage(byte[] result) throws Exception
    {
        byte[] imageBytes = getImageBytes(result);
        Image image = new ImageIcon(imageBytes).getImage();
        BufferedImage buffered =
            new BufferedImage(image.getWidth(null), image.getHeight(null), BufferedImage.TYPE_INT_RGB);
        Graphics2D graphics = buffered.createGraphics();
        graphics.drawImage(image, 0, 0, null);
        graphics.dispose();
        return buffered;
    }

    private byte[] getImageBytes(byte[] result) throws IOException
    {
        byte[] imageBytes;

        if (isZip(result)) {
            try (ByteArrayInputStream bis = new ByteArrayInputStream(result); ZipInputStream zis = new ZipInputStream(
                bis))
            {
                ZipEntry entry = zis.getNextEntry();
                if (entry == null) {
                    throw new RuntimeException("No image entries found in ZIP result");
                }
                // Read first image entry bytes
                imageBytes = zis.readAllBytes();
            }
        } else {
            imageBytes = result;
        }
        return imageBytes;
    }

    private boolean isZip(byte[] data)
    {
        return data.length >= 4 && data[0] == 0x50 && data[1] == 0x4B;
    }
}
