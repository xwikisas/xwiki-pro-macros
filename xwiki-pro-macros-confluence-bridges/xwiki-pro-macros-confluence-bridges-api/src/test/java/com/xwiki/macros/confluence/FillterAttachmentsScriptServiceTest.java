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
package com.xwiki.macros.confluence;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.xwiki.model.reference.AttachmentReference;
import org.xwiki.model.reference.DocumentReference;
import org.xwiki.test.junit5.mockito.ComponentTest;
import org.xwiki.test.junit5.mockito.InjectMockComponents;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link FilterAttachmentsScriptService}
 */
@ComponentTest
public class FillterAttachmentsScriptServiceTest
{

    private AttachmentReference attachmentReference1;

    private AttachmentReference attachmentReference2;

    @InjectMockComponents
    private FilterAttachmentsScriptService filterAttachmentsScriptService;

    @Mock
    private DocumentReference documentReference;
    @BeforeEach
    void setup()
    {
        // Can't mock the getName method because it is final, and I chose to instantiate the objects.
        this.attachmentReference1 = new AttachmentReference("test.pdf", documentReference);
        this.attachmentReference2 = new AttachmentReference("test.doc", documentReference);
    }

    @Test
    void multipleFilters()
    {
        List<AttachmentReference> attachmentReferenceList = List.of(attachmentReference1, attachmentReference2);
        assertEquals(2,
            filterAttachmentsScriptService.filterAttachmentResults(attachmentReferenceList, List.of(".*pdf", ".*doc"))
                .size());
    }

    @Test
    void singleFilter()
    {
        List<AttachmentReference> attachmentReferenceList = List.of(attachmentReference1, attachmentReference2);
        assertEquals(1,
            filterAttachmentsScriptService.filterAttachmentResults(attachmentReferenceList, List.of(".*pdf"))
                .size());
    }

    @Test
    void emptyFilter()
    {
        List<AttachmentReference> attachmentReferenceList = List.of(attachmentReference1, attachmentReference2);
        assertEquals(2,
            filterAttachmentsScriptService.filterAttachmentResults(attachmentReferenceList, List.of())
                .size());
    }

    @Test
    void emptyStringFilter()
    {
        List<AttachmentReference> attachmentReferenceList = List.of(attachmentReference1, attachmentReference2);
        assertEquals(2,
            filterAttachmentsScriptService.filterAttachmentResults(attachmentReferenceList, List.of(""))
                .size());
    }
}
