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

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;

import javax.imageio.ImageIO;
import javax.inject.Inject;
import javax.inject.Provider;

import org.xwiki.model.reference.AttachmentReference;

import com.xpn.xwiki.XWikiContext;
import com.xwiki.macros.viewfile.internal.thumbnail.TemporaryFileManager;
import com.xwiki.macros.viewfile.thumbnail.generators.ThumbnailGenerator;

import net.coobird.thumbnailator.Thumbnails;

/**
 * Abstract implementation of {@link ThumbnailGenerator} that adds a common method used by
 * {@link OfficeThumbnailGenerator} and {@link PdfThumbnailGenerator}.
 *
 * @version $Id$
 * @since 1.29.0
 */
public abstract class AbstractOfficePdfThumbnailGenerator implements ThumbnailGenerator
{
    @Inject
    protected Provider<XWikiContext> wikiContextProvider;

    @Inject
    private TemporaryFileManager temporaryFileManager;

    @Override
    public String generateThumbnail(AttachmentReference attachmentReference) throws Exception
    {
        return null;
    }

    protected String saveThumbnail(BufferedImage firstPage, AttachmentReference attachmentReference) throws Exception
    {
        // Resize to thumbnail
        BufferedImage resized = Thumbnails.of(firstPage).size(150, 212).asBufferedImage();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(resized, JPG, baos);
        try (InputStream is = new ByteArrayInputStream(baos.toByteArray())) {
            return this.temporaryFileManager.createThumbnail(attachmentReference, is);
        }
    }
}
