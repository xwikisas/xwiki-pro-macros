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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

import javax.inject.Provider;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.xwiki.model.reference.AttachmentReference;
import org.xwiki.model.reference.DocumentReference;
import org.xwiki.test.junit5.mockito.ComponentTest;
import org.xwiki.test.junit5.mockito.InjectMockComponents;
import org.xwiki.test.junit5.mockito.MockComponent;

import com.xpn.xwiki.XWiki;
import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.doc.XWikiAttachment;
import com.xpn.xwiki.doc.XWikiDocument;
import com.xwiki.macros.viewfile.internal.thumbnail.TemporaryFileManager;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

/**
 * Unit test for {@link PdfThumbnailGenerator}
 *
 * @version $Id$
 */
@ComponentTest
class PdfThumbnailGeneratorTest
{
    private static final String FILENAME = "test.pdf";

    @InjectMockComponents
    PdfThumbnailGenerator pdfThumbnailGenerator;

    @MockComponent
    private Provider<XWikiContext> wikiContextProvider;

    @MockComponent
    private XWikiContext wikiContext;

    @MockComponent
    private TemporaryFileManager temporaryFileManager;

    @Mock
    private XWiki wiki;

    @Mock
    private XWikiDocument document;

    @Mock
    private XWikiAttachment attachment;

    private DocumentReference documentReference = new DocumentReference("testWiki", "testSpace", "testPage");

    AttachmentReference attachmentReference = new AttachmentReference(FILENAME, documentReference);

    @Test
    void generateThumbnail() throws Exception
    {
        when(wikiContextProvider.get()).thenReturn(wikiContext);
        when(wikiContext.getWiki()).thenReturn(wiki);
        when(wiki.getDocument(documentReference, wikiContext)).thenReturn(document);
        when(temporaryFileManager.createThumbnail(eq(attachmentReference), any(ByteArrayInputStream.class))).thenReturn(
            "content");
        // Create a real valid PDF with PDFBox.
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (PDDocument pdf = new PDDocument()) {
            PDPage page = new PDPage();
            pdf.addPage(page);
            PDPageContentStream cs = new PDPageContentStream(pdf, page);
            cs.beginText();
            cs.setFont(PDType1Font.HELVETICA, 12);
            cs.newLineAtOffset(50, 700);
            cs.showText("test content");
            cs.endText();
            cs.close();
            pdf.save(baos);
        }
        byte[] pdfBytes = baos.toByteArray();

        when(document.getAttachment(FILENAME)).thenReturn(attachment);
        when(attachment.getContentInputStream(wikiContext)).thenReturn(new ByteArrayInputStream(pdfBytes));
        assertEquals("content", pdfThumbnailGenerator.generateThumbnail(attachmentReference));
    }
}
