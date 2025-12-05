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

import java.io.IOException;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.xwiki.component.manager.ComponentManager;
import org.xwiki.model.reference.AttachmentReference;
import org.xwiki.model.reference.DocumentReference;
import org.xwiki.resource.SerializeResourceReferenceException;
import org.xwiki.resource.UnsupportedResourceReferenceException;
import org.xwiki.test.LogLevel;
import org.xwiki.test.junit5.LogCaptureExtension;
import org.xwiki.test.junit5.mockito.ComponentTest;
import org.xwiki.test.junit5.mockito.InjectMockComponents;
import org.xwiki.test.junit5.mockito.MockComponent;

import com.xwiki.macros.viewfile.internal.thumbnail.generators.OfficeThumbnailGenerator;
import com.xwiki.macros.viewfile.thumbnail.generators.ThumbnailGenerator;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

/**
 * Unit test for {@link ThumbnailGeneratorManager}
 *
 * @version $Id$
 */
@ComponentTest
public class ThumbnailGeneratorManagerTest
{
    private static final String attachmentName = "test.doc";

    @MockComponent
    ThumbnailGenerator thumbnailGenerator;

    @InjectMockComponents
    private ThumbnailGeneratorManager thumbnailGeneratorManager;

    @MockComponent
    private TemporaryFileManager temporaryFileManager;

    @MockComponent
    private ComponentManager componentManager;

    @RegisterExtension
    private LogCaptureExtension logCapture = new LogCaptureExtension(LogLevel.WARN);

    private DocumentReference documentReference = new DocumentReference("testWiki", "testSpace", "testPage");

    private AttachmentReference attachmentReference = new AttachmentReference(attachmentName, documentReference);

    @Test
    void getThumbnailUrlFound()
        throws IOException, SerializeResourceReferenceException, UnsupportedResourceReferenceException
    {
        when(temporaryFileManager.thumbnailFileExists(attachmentReference)).thenReturn(true);
        when(temporaryFileManager.getThumbnailURL(attachmentReference)).thenReturn("attachUrl");
        assertEquals("attachUrl", thumbnailGeneratorManager.getThumbnailUrl(attachmentReference));
    }

    @Test
    void getThumbnailUrlFoundError()
        throws IOException, SerializeResourceReferenceException, UnsupportedResourceReferenceException
    {
        when(temporaryFileManager.thumbnailFileExists(attachmentReference)).thenReturn(true);
        when(temporaryFileManager.getThumbnailURL(attachmentReference)).thenThrow(
            new SerializeResourceReferenceException("test throw"));
        assertEquals("", thumbnailGeneratorManager.getThumbnailUrl(attachmentReference));
        assertEquals("There was an error while attempting to get the thumbnail URL. "
            + "Root cause is: [SerializeResourceReferenceException: test throw]", logCapture.getMessage(0));
    }

    @Test
    void getThumbnailUrlNotFound() throws Exception
    {
        when(temporaryFileManager.thumbnailFileExists(attachmentReference)).thenReturn(false);
        when(componentManager.getInstance(ThumbnailGenerator.class, OfficeThumbnailGenerator.HINT)).thenReturn(
            thumbnailGenerator);
        when(thumbnailGenerator.generateThumbnail(attachmentReference)).thenReturn("generated url for doc");
        assertEquals("generated url for doc", thumbnailGeneratorManager.getThumbnailUrl(attachmentReference));
    }
}
