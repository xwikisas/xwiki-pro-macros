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

import java.util.Map;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.xwiki.component.annotation.Component;
import org.xwiki.component.manager.ComponentManager;
import org.xwiki.model.reference.AttachmentReference;

import com.xwiki.macros.viewfile.internal.thumbnail.generators.OfficeThumbnailGenerator;
import com.xwiki.macros.viewfile.internal.thumbnail.generators.PdfThumbnailGenerator;
import com.xwiki.macros.viewfile.internal.thumbnail.generators.PresentationThumbnailGenerator;
import com.xwiki.macros.viewfile.thumbnail.generators.ThumbnailGenerator;

/**
 * Generate a thumbnail image for office and PDF files.
 *
 * @version $Id$
 * @since 1.26.22
 */
@Component(roles = ThumbnailGeneratorManager.class)
@Singleton
public class ThumbnailGeneratorManager
{
    private static final Map<String, String> EXTENSION_HINT_MAP = Map.of(
        "odp", OfficeThumbnailGenerator.HINT,
        "doc", OfficeThumbnailGenerator.HINT,
        "docx", OfficeThumbnailGenerator.HINT,
        "odt", OfficeThumbnailGenerator.HINT,
        "xls", OfficeThumbnailGenerator.HINT,
        "xlsx", OfficeThumbnailGenerator.HINT,
        "ods", OfficeThumbnailGenerator.HINT,
        "ppt", PresentationThumbnailGenerator.HINT,
        "pptx", PresentationThumbnailGenerator.HINT,
        "pdf", PdfThumbnailGenerator.HINT
    );

    private static final String EMPTY_STRING = "";

    @Inject
    private Logger logger;

    @Inject
    private TemporaryFileManager temporaryFileManager;

    @Inject
    private ComponentManager componentManager;

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
            if (!temporaryFileManager.thumbnailFileExists(attachmentReference)) {
                return generateAndGetThumbnailUrlPath(attachmentReference);
            } else {
                return temporaryFileManager.getThumbnailURL(attachmentReference);
            }
        } catch (Exception e) {
            logger.error("There was an error while attempting to get the thumbnail URL. Root cause is: [{}]",
                ExceptionUtils.getRootCauseMessage(e));
            return EMPTY_STRING;
        }
    }

    private String generateAndGetThumbnailUrlPath(AttachmentReference attachmentReference) throws Exception
    {
        String extension = getExtension(attachmentReference.getName()).toLowerCase();
        String hint = EXTENSION_HINT_MAP.get(extension);
        if (hint == null) {
            logger.warn("Extension type [{}] not supported.", extension);
            return EMPTY_STRING;
        }
        ThumbnailGenerator generator = componentManager.getInstance(ThumbnailGenerator.class, hint);
        return generator.generateThumbnail(attachmentReference);
    }

    private String getExtension(String fileName)
    {
        return fileName.substring(fileName.lastIndexOf('.') + 1).toLowerCase();
    }
}
