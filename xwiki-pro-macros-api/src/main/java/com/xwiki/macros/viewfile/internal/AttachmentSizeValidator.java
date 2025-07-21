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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.inject.Singleton;

import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.poi.util.IOUtils;
import org.slf4j.Logger;
import org.xwiki.component.annotation.Component;
import org.xwiki.model.reference.AttachmentReference;

import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.XWikiException;
import com.xpn.xwiki.doc.XWikiAttachment;
import com.xpn.xwiki.doc.XWikiDocument;

/**
 * Checks if Office 2007 and ODF document attachments are oversize by checking the ZIP entries size or if an excessive
 * amount of paragraphs is present. This validator prevents documents from causing performance or memory issues during
 * processing (e.g., conversion or rendering), by applying constraints derived from Apache POI and LibreOffice
 * limitations.
 *
 * @version $Id$
 * @since 1.27.1
 */
@Component(roles = AttachmentSizeValidator.class)
@Singleton
public class AttachmentSizeValidator
{
    private static final int MAX_ENTRY_LENGTH = 100_000_000;

    private static final List<String> OLD_OFFICE_EXTENSIONS = List.of("doc", "xls", "ppt");

    private static final int MAX_PARAGRAPHS = 200_000;

    @Inject
    private Provider<XWikiContext> wikiContextProvider;

    @Inject
    private Logger logger;

    /**
     * Determines whether a given document attachment exceeds safe limits for processing. This method only applies to
     * Office 2007 and ODF formats attachments.
     *
     * @param attachRef The reference to the attachment to check
     * @return {@code true} if the attachment is oversize, or {@code false} otherwise
     * @throws XWikiException if an error occurs while attempting to retrieve the attachment and it's content
     * @throws IOException if an error occurs while reading or parsing the attachment
     */
    public boolean isAttachmentOversize(AttachmentReference attachRef) throws XWikiException, IOException
    {
        XWikiContext wikiContext = wikiContextProvider.get();
        XWikiDocument document = wikiContext.getWiki().getDocument(attachRef.getDocumentReference(), wikiContext);
        XWikiAttachment attachment = document.getAttachment(attachRef.getName());
        String extension = attachRef.getName().substring(attachRef.getName().lastIndexOf('.') + 1).toLowerCase();
        boolean isOversize = false;
        if (!OLD_OFFICE_EXTENSIONS.contains(extension)) {
            InputStream is = attachment.getContentInputStream(wikiContext);
            isOversize = containsOversizeEntry(is);
        }
        return isOversize;
    }

    private boolean containsOversizeEntry(InputStream file) throws IOException
    {
        int maxEntrySize = IOUtils.getMaxByteArrayInitSize();
        if (maxEntrySize == -1) {
            maxEntrySize = MAX_ENTRY_LENGTH;
        }
        try (ZipInputStream zis = new ZipInputStream(file)) {
            ZipEntry entry;
            while ((entry = zis.getNextEntry()) != null) {
                // Check the metadata of a DOCX file to verify if the number of paragraphs are above the limit.
                switch (entry.getName()) {
                    case "docProps/app.xml":
                        if (exceedsParagraphLimit(zis, "<Paragraphs>(\\d+)</Paragraphs>", "<Paragraphs>")) {
                            return true;
                        }
                        break;
                    case "meta.xml":
                        if (exceedsParagraphLimit(zis, "meta:paragraph-count\\s*=\\s*\"(\\d+)\"", "paragraph-count")) {
                            return true;
                        }
                        break;
                    default:
                        // Check if the size of the entry are above the Apache POI max record size.
                        long entrySize = entry.getSize();
                        if (entry.getSize() == -1) {
                            entrySize = getEntrySize(zis);
                        }
                        if (entrySize > maxEntrySize) {
                            logger.warn(
                                "File entry size is larger then the maximum length for this record type set at [{}].",
                                maxEntrySize);
                            return true;
                        }
                        break;
                }
                zis.closeEntry();
            }
        }
        return false;
    }

    private boolean exceedsParagraphLimit(ZipInputStream zis, String regex, String selector) throws IOException
    {
        BufferedReader reader = new BufferedReader(new InputStreamReader(zis));
        String line;
        while ((line = reader.readLine()) != null) {
            if (line.contains(selector)) {
                Pattern pattern = Pattern.compile(regex);
                Matcher matcher = pattern.matcher(line);
                if (matcher.find()) {
                    try {
                        int paragraphCount = Integer.parseInt(matcher.group(1));
                        if (paragraphCount > MAX_PARAGRAPHS) {
                            logger.warn("File oversize: too many paragraphs ({}).", paragraphCount);
                            return true;
                        }
                    } catch (NumberFormatException e) {
                        logger.error("Failed to subtract the number of paragraphs. Root cause is: [{}]",
                            ExceptionUtils.getRootCauseMessage(e));
                    }
                }
            }
        }
        return false;
    }

    private long getEntrySize(ZipInputStream zis) throws IOException
    {
        long size = 0;
        int read;
        byte[] buffer = new byte[8192];
        while ((read = zis.read(buffer)) != -1) {
            size += read;
        }
        return size;
    }
}
