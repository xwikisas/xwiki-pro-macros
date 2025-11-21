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
import java.io.IOException;
import java.io.InputStream;

import javax.imageio.ImageIO;
import javax.inject.Provider;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.mockito.Mock;
import org.xwiki.component.phase.InitializationException;
import org.xwiki.model.reference.AttachmentReference;
import org.xwiki.model.reference.DocumentReference;
import org.xwiki.resource.SerializeResourceReferenceException;
import org.xwiki.resource.UnsupportedResourceReferenceException;
import org.xwiki.test.LogLevel;
import org.xwiki.test.junit5.LogCaptureExtension;
import org.xwiki.test.junit5.mockito.ComponentTest;
import org.xwiki.test.junit5.mockito.InjectMockComponents;
import org.xwiki.test.junit5.mockito.MockComponent;

import com.xpn.xwiki.XWiki;
import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.XWikiException;
import com.xpn.xwiki.doc.XWikiAttachment;
import com.xpn.xwiki.doc.XWikiDocument;
import com.xwiki.macros.viewfile.internal.thumbnail.TemporaryFileManager;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

/**
 * Unit test for {@link OfficeThumbnailGenerator}
 *
 * @version $Id$
 */
@ComponentTest
public class OfficeThumbnailGeneratorTest
{
    private static final String FILENAME = "test.docx";

    @InjectMockComponents
    OfficeThumbnailGenerator officeThumbnailGenerator;

    @MockComponent
    private Provider<XWikiContext> wikiContextProvider;

    @RegisterExtension
    private LogCaptureExtension logCapture = new LogCaptureExtension(LogLevel.WARN);

    @MockComponent
    private XWikiContext wikiContext;

    @MockComponent
    private TemporaryFileManager temporaryFileManager;

    @MockComponent
    private OfficeManagerWrapper officeManagerWrapper;

    @Mock
    private XWiki wiki;

    @Mock
    private XWikiDocument document;

    @Mock
    private XWikiAttachment attachment;

    private DocumentReference documentReference = new DocumentReference("testWiki", "testSpace", "testPage");

    AttachmentReference attachmentReference = new AttachmentReference(FILENAME, documentReference);

    @BeforeEach
    void setUp()
        throws XWikiException, SerializeResourceReferenceException, IOException, UnsupportedResourceReferenceException,
        InitializationException
    {
        when(wikiContextProvider.get()).thenReturn(wikiContext);
        when(wikiContext.getWiki()).thenReturn(wiki);
        when(wiki.getDocument(documentReference, wikiContext)).thenReturn(document);
        when(document.getAttachment(FILENAME)).thenReturn(attachment);
        when(temporaryFileManager.createThumbnail(eq(attachmentReference), any(ByteArrayInputStream.class))).thenReturn(
            "content");
    }

    @Test
    void generateThumbnailOfficeNotConnected() throws Exception
    {
        when(officeManagerWrapper.isOfficeServerConnected()).thenReturn(false);
        assertEquals("", officeThumbnailGenerator.generateThumbnail(attachmentReference));
        assertEquals("Unable to generate thumbnail for office file [Attachment testWiki:testSpace.testPage@test.docx]"
            + ". Office server is not connected.", logCapture.getMessage(0));
    }

    @Test
    void generateThumbnailOffice() throws Exception
    {
        when(officeManagerWrapper.isOfficeServerConnected()).thenReturn(true);
        InputStream is = new ByteArrayInputStream("test".getBytes());
        when(attachment.getContentInputStream(wikiContext)).thenReturn(is);
        when(officeManagerWrapper.getImageBytes(is)).thenReturn(createTestPng());
        assertEquals("content", officeThumbnailGenerator.generateThumbnail(attachmentReference));
    }

    private byte[] createTestPng() throws IOException
    {
        BufferedImage img = new BufferedImage(1, 1, BufferedImage.TYPE_INT_RGB);
        img.setRGB(0, 0, 0xFFFFFF);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(img, "png", baos);
        return baos.toByteArray();
    }
}
