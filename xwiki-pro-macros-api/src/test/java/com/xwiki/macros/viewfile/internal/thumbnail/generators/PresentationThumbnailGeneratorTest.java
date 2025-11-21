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

import org.apache.poi.hslf.usermodel.HSLFSlideShow;
import org.apache.poi.openxml4j.exceptions.NotOfficeXmlFileException;
import org.apache.poi.xslf.usermodel.XMLSlideShow;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.mockito.Mock;
import org.xwiki.model.reference.AttachmentReference;
import org.xwiki.model.reference.DocumentReference;
import org.xwiki.test.LogLevel;
import org.xwiki.test.junit5.LogCaptureExtension;
import org.xwiki.test.junit5.mockito.ComponentTest;
import org.xwiki.test.junit5.mockito.InjectMockComponents;
import org.xwiki.test.junit5.mockito.MockComponent;

import com.xpn.xwiki.XWiki;
import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.doc.XWikiAttachment;
import com.xpn.xwiki.doc.XWikiDocument;
import com.xwiki.macros.viewfile.internal.thumbnail.TemporaryFileManager;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

/**
 * Unit test for {@link PresentationThumbnailGenerator}
 *
 * @version $Id$
 */
@ComponentTest
public class PresentationThumbnailGeneratorTest
{
    @InjectMockComponents
    private PresentationThumbnailGenerator thumbnailGenerator;

    @MockComponent
    private Provider<XWikiContext> wikiContextProvider;

    @MockComponent
    private TemporaryFileManager temporaryFileManager;

    @RegisterExtension
    private LogCaptureExtension logCapture = new LogCaptureExtension(LogLevel.WARN);

    @MockComponent
    private XWikiContext wikiContext;

    @Mock
    private XWiki wiki;

    @Mock
    private XWikiDocument document;

    @Mock
    private XWikiAttachment attachment;

    private DocumentReference documentReference = new DocumentReference("testWiki", "testSpace", "testPage");

    @BeforeEach
    void setUp()
    {
        when(wikiContextProvider.get()).thenReturn(wikiContext);
        when(wikiContext.getWiki()).thenReturn(wiki);
    }

    @Test
    void generateThumbnailDefault() throws Exception
    {
        String attachmentName = "test.pdf";
        AttachmentReference attachmentReference = new AttachmentReference(attachmentName, documentReference);
        when(wiki.getDocument(documentReference, wikiContext)).thenReturn(document);
        when(document.getAttachment(attachmentName)).thenReturn(attachment);
        assertEquals("", thumbnailGenerator.generateThumbnail(attachmentReference));
        assertEquals("Failed to identify the presentation file extension.", logCapture.getMessage(0));
    }

    @Test
    void generateThumbnailInvalid() throws Exception
    {
        String attachmentName = "test.pptx";
        AttachmentReference attachmentReference = new AttachmentReference(attachmentName, documentReference);
        when(wiki.getDocument(documentReference, wikiContext)).thenReturn(document);
        when(document.getAttachment(attachmentName)).thenReturn(attachment);
        when(attachment.getContentInputStream(wikiContext)).thenReturn(
            new ByteArrayInputStream("some content".getBytes()));
        NotOfficeXmlFileException exception = assertThrows(NotOfficeXmlFileException.class, () -> {
            this.thumbnailGenerator.generateThumbnail(attachmentReference);
        });
        assertEquals("No valid entries or contents found, this is not a valid OOXML (Office Open XML) file",
            exception.getMessage());
    }

    @Test
    void generateThumbnailPPTX() throws Exception
    {
        // Create a minimal valid PPTX file
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (XMLSlideShow ppt = new XMLSlideShow()) {
            ppt.createSlide();
            ppt.write(baos);
        }
        byte[] pptxBytes = baos.toByteArray();
        String attachmentName = "test.pptx";
        AttachmentReference attachmentReference = new AttachmentReference(attachmentName, documentReference);
        ByteArrayInputStream is = new ByteArrayInputStream(pptxBytes);
        when(wiki.getDocument(documentReference, wikiContext)).thenReturn(document);
        when(document.getAttachment(attachmentName)).thenReturn(attachment);
        when(attachment.getContentInputStream(wikiContext)).thenReturn(is);
        when(temporaryFileManager.createThumbnail(eq(attachmentReference), any(ByteArrayInputStream.class))).thenReturn(
            "test pptx result");
        assertEquals("test pptx result", thumbnailGenerator.generateThumbnail(attachmentReference));
    }

    @Test
    void generateThumbnailPPT() throws Exception
    {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (HSLFSlideShow ppt = new HSLFSlideShow()) {
            ppt.createSlide();
            ppt.write(baos);
        }
        byte[] pptBytes = baos.toByteArray();
        String attachmentName = "test.ppt";
        AttachmentReference attachmentReference = new AttachmentReference(attachmentName, documentReference);
        ByteArrayInputStream is = new ByteArrayInputStream(pptBytes);

        when(wiki.getDocument(documentReference, wikiContext)).thenReturn(document);
        when(document.getAttachment(attachmentName)).thenReturn(attachment);
        when(attachment.getContentInputStream(wikiContext)).thenReturn(is);
        when(temporaryFileManager.createThumbnail(eq(attachmentReference), any(ByteArrayInputStream.class))).thenReturn(
            "test ppt result");

        assertEquals("test ppt result", thumbnailGenerator.generateThumbnail(attachmentReference));
    }
}
