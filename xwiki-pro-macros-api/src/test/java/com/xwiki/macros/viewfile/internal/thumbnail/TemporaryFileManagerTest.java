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

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.mockito.Mock;
import org.xwiki.model.reference.AttachmentReference;
import org.xwiki.model.reference.DocumentReference;
import org.xwiki.resource.ResourceReference;
import org.xwiki.resource.ResourceReferenceSerializer;
import org.xwiki.resource.SerializeResourceReferenceException;
import org.xwiki.resource.UnsupportedResourceReferenceException;
import org.xwiki.resource.temporary.TemporaryResourceReference;
import org.xwiki.resource.temporary.TemporaryResourceStore;
import org.xwiki.test.LogLevel;
import org.xwiki.test.junit5.LogCaptureExtension;
import org.xwiki.test.junit5.mockito.ComponentTest;
import org.xwiki.test.junit5.mockito.InjectMockComponents;
import org.xwiki.test.junit5.mockito.MockComponent;
import org.xwiki.url.ExtendedURL;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

/**
 * Unit test for {@link TemporaryFileManager}
 *
 * @version $Id$
 */
@ComponentTest
public class TemporaryFileManagerTest
{
    private static final String attachmentName = "test.docx";

    InputStream testContent = new ByteArrayInputStream("test".getBytes());

    @Mock
    ExtendedURL extendedURL;

    @InjectMockComponents
    private TemporaryFileManager temporaryFileManager;

    @MockComponent
    private TemporaryResourceStore temporaryResourceStore;

    @MockComponent
    private ResourceReferenceSerializer<ResourceReference, ExtendedURL> resourceReferenceSerializer;

    @RegisterExtension
    private LogCaptureExtension logCapture = new LogCaptureExtension(LogLevel.WARN);

    @Mock
    private File file;

    private DocumentReference documentReference = new DocumentReference("testWiki", "testSpace", "testPage");

    private AttachmentReference attachmentReference = new AttachmentReference(attachmentName, documentReference);

    @Test
    void createThumbnail()
        throws SerializeResourceReferenceException, IOException, UnsupportedResourceReferenceException
    {
        when(resourceReferenceSerializer.serialize(any(TemporaryResourceReference.class))).thenReturn(extendedURL);
        when(extendedURL.serialize()).thenReturn("serialized value");
        assertEquals("serialized value", temporaryFileManager.createThumbnail(attachmentReference, testContent));
    }

    @Test
    void getThumbnailFile() throws IOException
    {
        when(temporaryResourceStore.getTemporaryFile(any(TemporaryResourceReference.class))).thenReturn(file);
        assertEquals(file, temporaryFileManager.getThumbnailFile(attachmentReference));
    }

    @Test
    void thumbnailFileExistsTrue() throws IOException
    {
        when(temporaryResourceStore.getTemporaryFile(any(TemporaryResourceReference.class))).thenReturn(file);
        when(file.exists()).thenReturn(true);
        assertTrue(temporaryFileManager.thumbnailFileExists(attachmentReference));
    }

    @Test
    void thumbnailFileExistsFalse() throws IOException
    {
        when(temporaryResourceStore.getTemporaryFile(any(TemporaryResourceReference.class))).thenReturn(file);
        when(file.exists()).thenReturn(false);
        assertFalse(temporaryFileManager.thumbnailFileExists(attachmentReference));
    }

    @Test
    void getThumbnailURL()
        throws IOException, SerializeResourceReferenceException, UnsupportedResourceReferenceException
    {
        when(resourceReferenceSerializer.serialize(any(TemporaryResourceReference.class))).thenReturn(extendedURL);
        when(extendedURL.serialize()).thenReturn("serialized value");
        assertEquals("serialized value", temporaryFileManager.getThumbnailURL(attachmentReference));
    }
}
