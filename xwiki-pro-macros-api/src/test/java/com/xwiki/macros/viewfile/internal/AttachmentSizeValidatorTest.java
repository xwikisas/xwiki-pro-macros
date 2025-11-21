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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import javax.inject.Provider;

import org.apache.poi.util.IOUtils;
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
import com.xpn.xwiki.XWikiException;
import com.xpn.xwiki.doc.XWikiAttachment;
import com.xpn.xwiki.doc.XWikiDocument;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

/**
 * Unit test for {@link AttachmentSizeValidator}
 *
 * @version $Id$
 */
@ComponentTest
public class AttachmentSizeValidatorTest
{
    @InjectMockComponents
    private AttachmentSizeValidator attachmentSizeValidator;

    @MockComponent
    private Provider<XWikiContext> wikiContextProvider;

    @Mock
    private XWikiContext wikiContext;

    @Mock
    private XWiki wiki;

    @Mock
    private XWikiAttachment attachment;

    @Mock
    private XWikiDocument document;

    @RegisterExtension
    private LogCaptureExtension logCapture = new LogCaptureExtension(LogLevel.WARN);

    private DocumentReference documentReference = new DocumentReference("testWiki", "testSpace", "testPage");

    @BeforeEach
    void beforeEach() throws XWikiException
    {
        when(wikiContextProvider.get()).thenReturn(wikiContext);
        when(wikiContext.getWiki()).thenReturn(wiki);
        when(wiki.getDocument(documentReference, wikiContext)).thenReturn(document);
    }

    @Test
    void isAttachmentOversizeNull() throws IOException, XWikiException
    {
        String attachmentName = "test.pdf";
        AttachmentReference attachmentReference = new AttachmentReference(attachmentName, documentReference);
        when(document.getAttachment(attachmentName)).thenReturn(null);
        assertTrue(attachmentSizeValidator.isAttachmentOversize(attachmentReference));
    }

    @Test
    void isAttachmentOversizeOldOffice() throws IOException, XWikiException
    {
        String attachmentName = "test.doc";
        AttachmentReference attachmentReference = new AttachmentReference(attachmentName, documentReference);
        when(document.getAttachment(attachmentName)).thenReturn(attachment);
        assertFalse(attachmentSizeValidator.isAttachmentOversize(attachmentReference));
    }

    @Test
    void isAttachmentOversizeFalse() throws IOException, XWikiException
    {
        String attachmentName = "test.docx";
        AttachmentReference attachmentReference = new AttachmentReference(attachmentName, documentReference);
        when(document.getAttachment(attachmentName)).thenReturn(attachment);
        ByteArrayInputStream zipInput = getAttachmentInputStream(attachmentName, "some content");
        when(attachment.getContentInputStream(wikiContext)).thenReturn(zipInput);

        assertFalse(attachmentSizeValidator.isAttachmentOversize(attachmentReference));
    }

    @Test
    void isAttachmentOversizeTrue() throws IOException, XWikiException
    {
        String attachmentName = "test.docx";
        AttachmentReference attachmentReference = new AttachmentReference(attachmentName, documentReference);
        when(document.getAttachment(attachmentName)).thenReturn(attachment);
        ByteArrayInputStream zipInput = getAttachmentInputStream(attachmentName, "some content");
        when(attachment.getContentInputStream(wikiContext)).thenReturn(zipInput);
        int defaultValue = IOUtils.getMaxByteArrayInitSize();
        IOUtils.setMaxByteArrayInitSize(1);
        assertTrue(attachmentSizeValidator.isAttachmentOversize(attachmentReference));
        assertEquals("File entry size is larger then the maximum length for this record type set at [1].",
            logCapture.getMessage(0));
        IOUtils.setMaxByteArrayInitSize(defaultValue);
    }

    @Test
    void isAttachmentOversizeTrueParagraphsMeta() throws IOException, XWikiException
    {
        String attachmentName = "meta.xml";
        AttachmentReference attachmentReference = new AttachmentReference(attachmentName, documentReference);
        when(document.getAttachment(attachmentName)).thenReturn(attachment);
        ByteArrayInputStream zipInput = getAttachmentInputStream(attachmentName, "meta:paragraph-count=\"2000001\"");
        when(attachment.getContentInputStream(wikiContext)).thenReturn(zipInput);
        assertTrue(attachmentSizeValidator.isAttachmentOversize(attachmentReference));
        assertEquals("File oversize: too many paragraphs (2000001).", logCapture.getMessage(0));
    }

    @Test
    void isAttachmentOversizeTrueParagraphsDocProps() throws IOException, XWikiException
    {
        String attachmentName = "docProps/app.xml";
        AttachmentReference attachmentReference = new AttachmentReference(attachmentName, documentReference);
        when(document.getAttachment(attachmentName)).thenReturn(attachment);
        ByteArrayInputStream zipInput = getAttachmentInputStream(attachmentName, "<Paragraphs>20000000</Paragraphs>");
        when(attachment.getContentInputStream(wikiContext)).thenReturn(zipInput);
        assertTrue(attachmentSizeValidator.isAttachmentOversize(attachmentReference));
        assertEquals("File oversize: too many paragraphs (20000000).", logCapture.getMessage(0));
    }

    @Test
    void isAttachmentOversizeTrueParagraphsDocPropsFail() throws IOException, XWikiException
    {
        String attachmentName = "docProps/app.xml";
        AttachmentReference attachmentReference = new AttachmentReference(attachmentName, documentReference);
        when(document.getAttachment(attachmentName)).thenReturn(attachment);
        ByteArrayInputStream zipInput =
            getAttachmentInputStream(attachmentName, "<Paragraphs>999999999999999999999999</Paragraphs>");
        when(attachment.getContentInputStream(wikiContext)).thenReturn(zipInput);
        assertFalse(attachmentSizeValidator.isAttachmentOversize(attachmentReference));
        assertEquals("Failed to subtract the number of paragraphs. Root cause is: [NumberFormatException: For input "
            + "string: \"999999999999999999999999\"]", logCapture.getMessage(0));
    }

    private static ByteArrayInputStream getAttachmentInputStream(String attachmentName, String content)
        throws IOException
    {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ZipOutputStream zos = new ZipOutputStream(baos);
        zos.putNextEntry(new ZipEntry(attachmentName));
        zos.write(content.getBytes());
        zos.closeEntry();
        zos.close();
        return new ByteArrayInputStream(baos.toByteArray());
    }
}
