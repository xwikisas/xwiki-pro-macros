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

import java.io.InputStream;

import javax.inject.Named;
import javax.inject.Singleton;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.xwiki.component.annotation.Component;
import org.xwiki.model.reference.AttachmentReference;

import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.doc.XWikiDocument;
import com.xwiki.macros.viewfile.thumbnail.generators.ThumbnailGenerator;

/**
 * {@link ThumbnailGenerator} implementation that handles the thumbnail generation for .pdf extension files.
 *
 * @version $Id$
 * @since 1.29.0
 */
@Component
@Singleton
@Named(PdfThumbnailGenerator.HINT)
public class PdfThumbnailGenerator extends AbstractOfficePdfThumbnailGenerator
{
    /**
     * Component hint.
     */
    public static final String HINT = "pdf";

    @Override
    public String generateThumbnail(AttachmentReference attachmentReference) throws Exception
    {
        XWikiContext wikiContext = wikiContextProvider.get();
        XWikiDocument document =
            wikiContext.getWiki().getDocument(attachmentReference.getDocumentReference(), wikiContext);
        InputStream is = document.getAttachment(attachmentReference.getName()).getContentInputStream(wikiContext);
        // Load the PDF document.
        PDDocument pdDoc = PDDocument.load(is);
        PDFRenderer pdfRenderer = new PDFRenderer(pdDoc);
        // Select the first page (index starts at 0).
        return saveThumbnail(pdfRenderer.renderImageWithDPI(0, 150), attachmentReference);
    }
}
