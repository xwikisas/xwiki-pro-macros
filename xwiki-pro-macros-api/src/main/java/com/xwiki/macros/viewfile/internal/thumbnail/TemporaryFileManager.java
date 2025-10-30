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
import java.io.InputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.xwiki.component.annotation.Component;
import org.xwiki.model.reference.AttachmentReference;
import org.xwiki.model.reference.EntityReference;
import org.xwiki.resource.ResourceReference;
import org.xwiki.resource.ResourceReferenceSerializer;
import org.xwiki.resource.SerializeResourceReferenceException;
import org.xwiki.resource.UnsupportedResourceReferenceException;
import org.xwiki.resource.temporary.TemporaryResourceReference;
import org.xwiki.resource.temporary.TemporaryResourceStore;
import org.xwiki.url.ExtendedURL;

/**
 * Helper class used to handle temporary files operations.
 *
 * @version $Id$
 * @since 1.29.0
 */
@Component(roles = TemporaryFileManager.class)
@Singleton
public class TemporaryFileManager
{
    private static final String JPG_EXTENSION = ".jpg";

    /**
     * Used to create and access the temporary files.
     */
    @Inject
    private TemporaryResourceStore temporaryResourceStore;

    /**
     * Used to obtain the URL that corresponds to a temporary resource.
     */
    @Inject
    private ResourceReferenceSerializer<ResourceReference, ExtendedURL> resourceReferenceSerializer;

    /**
     * Create a new thumbnail file for a given {@link AttachmentReference}.
     *
     * @param attachmentReference reference for which to create the temporary file
     * @param content the file content
     * @return the URL to the newly created temporary file
     * @throws IOException if any error occurs during the file creation
     * @throws SerializeResourceReferenceException if there was an error while serializing the XWiki Resource
     *     object
     * @throws UnsupportedResourceReferenceException if the passed representation points to an unsupported Resource
     *     Reference type that we don't know how to serialize
     */
    public String createThumbnail(AttachmentReference attachmentReference, InputStream content)
        throws IOException, SerializeResourceReferenceException, UnsupportedResourceReferenceException
    {
        TemporaryResourceReference temporaryResourceReference = getTemporaryResourceReference(attachmentReference);
        this.temporaryResourceStore.createTemporaryFile(temporaryResourceReference, content);
        return this.resourceReferenceSerializer.serialize(temporaryResourceReference).serialize();
    }

    /**
     * Get the associated thumbnail file.
     *
     * @param attachmentReference reference for which to search for the temporary file
     * @return a {@link File} representing the given {@link AttachmentReference} thumbnail
     * @throws IOException if any error occurs during the file retrieval
     */
    public File getThumbnailFile(AttachmentReference attachmentReference) throws IOException
    {
        TemporaryResourceReference temporaryResourceReference = getTemporaryResourceReference(attachmentReference);
        return this.temporaryResourceStore.getTemporaryFile(temporaryResourceReference);
    }

    /**
     * Check if a thumbnail file exists for the given {@link AttachmentReference}.
     *
     * @param attachmentReference reference for which to search for the temporary file
     * @return {@code true} if the file exists, {@code false} otherwise
     * @throws IOException if any error occurs during the file retrieval
     */
    public boolean thumbnailFileExists(AttachmentReference attachmentReference) throws IOException
    {
        return getThumbnailFile(attachmentReference).exists();
    }

    /**
     * Get the URL to a temporary file as a {@link String}.
     *
     * @param attachmentReference reference for which to search for the temporary file
     * @return the URL to the temporary file
     * @throws SerializeResourceReferenceException if there was an error while serializing the XWiki Resource
     *     object
     * @throws UnsupportedResourceReferenceException if the passed representation points to an unsupported Resource
     *     Reference type that we don't know how to serialize
     */
    public String getThumbnailURL(AttachmentReference attachmentReference)
        throws SerializeResourceReferenceException, UnsupportedResourceReferenceException
    {
        TemporaryResourceReference temporaryResourceReference = getTemporaryResourceReference(attachmentReference);
        return this.resourceReferenceSerializer.serialize(temporaryResourceReference).serialize();
    }

    private static TemporaryResourceReference getTemporaryResourceReference(AttachmentReference attachmentReference)
    {
        String encodedFileReference = URLEncoder.encode(attachmentReference.getName(), StandardCharsets.UTF_8);
        List<String> resourcePath = Arrays.asList("thumbnails", encodedFileReference + JPG_EXTENSION);
        EntityReference owningEntityReference = attachmentReference.getDocumentReference();
        return new TemporaryResourceReference("viewfilemacro", resourcePath, owningEntityReference);
    }
}
