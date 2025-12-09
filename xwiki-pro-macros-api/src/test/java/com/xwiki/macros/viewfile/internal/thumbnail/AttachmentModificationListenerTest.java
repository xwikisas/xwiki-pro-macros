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

import java.io.File;
import java.io.IOException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.mockito.Mock;
import org.xwiki.component.phase.InitializationException;
import org.xwiki.environment.Environment;
import org.xwiki.model.reference.AttachmentReference;
import org.xwiki.model.reference.AttachmentReferenceResolver;
import org.xwiki.model.reference.DocumentReference;
import org.xwiki.observation.event.Event;
import org.xwiki.test.LogLevel;
import org.xwiki.test.junit5.LogCaptureExtension;
import org.xwiki.test.junit5.XWikiTempDir;
import org.xwiki.test.junit5.mockito.ComponentTest;
import org.xwiki.test.junit5.mockito.InjectMockComponents;
import org.xwiki.test.junit5.mockito.MockComponent;

import com.xpn.xwiki.doc.XWikiDocument;
import com.xpn.xwiki.internal.event.AttachmentUpdatedEvent;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@ComponentTest
public class AttachmentModificationListenerTest
{
    @InjectMockComponents
    AttachmentModificationListener attachmentModificationListener;

    @MockComponent
    Environment environment;

    @MockComponent
    private AttachmentReferenceResolver<String> attachmentResolver;

    @MockComponent
    private TemporaryFileManager temporaryFileManager;

    @RegisterExtension
    private LogCaptureExtension logCapture = new LogCaptureExtension(LogLevel.INFO);

    @XWikiTempDir
    private File tmpDir;

    private File file1;

    private File folder1;

    @Mock
    private XWikiDocument document;

    @Mock
    private AttachmentReference attachmentReference;

    @Mock
    private DocumentReference testRef;

    @BeforeEach
    void setUp() throws IOException
    {
        tmpDir.mkdir();
        tmpDir.deleteOnExit();
        folder1 = new File(tmpDir, "viewfilemacro");
        folder1.mkdir();
        file1 = new File(folder1, "some file");
        file1.createNewFile();
    }

    @Test
    void initialize() throws InitializationException
    {
        when(environment.getTemporaryDirectory()).thenReturn(tmpDir);

        assertTrue(file1.exists());
        assertTrue(folder1.exists());
        attachmentModificationListener.initialize();
        assertFalse(folder1.exists());
        assertFalse(file1.exists());
    }

    @Test
    void onEventAttachmentDeletedEvent() throws IOException
    {
        Event event = new AttachmentUpdatedEvent("test doc", "testName");
        when(document.getDocumentReference()).thenReturn(testRef);
        when(attachmentResolver.resolve("testName", testRef)).thenReturn(attachmentReference);
        when(temporaryFileManager.getThumbnailFile(attachmentReference)).thenReturn(file1);
        assertTrue(file1.exists());
        attachmentModificationListener.onEvent(event, document, null);
        assertFalse(file1.exists());
        assertTrue(logCapture.getMessage(0).contains("Successfully removed thumbnail at location: ["));
    }

    @Test
    void onEventAttachmentUpdatedEvent() throws IOException
    {
        Event event = new AttachmentUpdatedEvent("test doc", "testName");
        when(document.getDocumentReference()).thenReturn(testRef);
        when(attachmentResolver.resolve("testName", testRef)).thenReturn(attachmentReference);
        when(temporaryFileManager.getThumbnailFile(attachmentReference)).thenReturn(file1);
        assertTrue(file1.exists());
        attachmentModificationListener.onEvent(event, document, null);
        assertFalse(file1.exists());
        assertTrue(logCapture.getMessage(0).contains("Successfully removed thumbnail at location: ["));
    }

    @Test
    void onEventFail() throws IOException
    {
        Event event = new AttachmentUpdatedEvent("test doc", "testName");
        when(document.getDocumentReference()).thenReturn(testRef);
        when(attachmentResolver.resolve("testName", testRef)).thenReturn(attachmentReference);
        when(temporaryFileManager.getThumbnailFile(attachmentReference)).thenThrow(new IOException("test error"));
        attachmentModificationListener.onEvent(event, document, null);
        assertEquals("Failed to remove thumbnail at attachment modification.", logCapture.getMessage(0));
    }
}
