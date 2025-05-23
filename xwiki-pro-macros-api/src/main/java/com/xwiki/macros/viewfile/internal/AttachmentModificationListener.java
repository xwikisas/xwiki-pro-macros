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

import java.io.File;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.slf4j.Logger;
import org.xwiki.component.annotation.Component;
import org.xwiki.environment.Environment;
import org.xwiki.observation.AbstractEventListener;
import org.xwiki.observation.event.Event;

import com.xpn.xwiki.doc.XWikiDocument;
import com.xpn.xwiki.internal.event.AbstractAttachmentEvent;
import com.xpn.xwiki.internal.event.AttachmentDeletedEvent;
import com.xpn.xwiki.internal.event.AttachmentUpdatedEvent;

/**
 * Listens to attachments delete and update events and attempt to remove the existing thumbnails if they exist.
 *
 * @version $Id$
 * @since 1.26.20
 */
@Component
@Named(AttachmentModificationListener.HINT)
@Singleton
public class AttachmentModificationListener extends AbstractEventListener
{
    /**
     * The hint for the component.
     */
    public static final String HINT = "ViewFileMacroAttachmentEventListener";

    @Inject
    private Logger logger;

    @Inject
    private Environment environment;

    /**
     * Creates an event-listener filtering for AttachmentDeletedEvent and AttachmentUpdatedEvent.
     */
    public AttachmentModificationListener()
    {
        super(HINT, new AttachmentDeletedEvent(), new AttachmentUpdatedEvent());
    }

    @Override
    public void onEvent(Event event, Object source, Object data)
    {
        if (event instanceof AttachmentUpdatedEvent || event instanceof AttachmentDeletedEvent) {
            XWikiDocument document = (XWikiDocument) source;
            if (document != null) {
                removeThumbnail(((AbstractAttachmentEvent) event).getName(), document);
            }
        }
    }

    private void removeThumbnail(String attachmentName, XWikiDocument document)
    {
        File tempDir = new File(environment.getTemporaryDirectory(),
            "viewfilemacro/thumbnails/" + document.getDocumentReference().toString());

        File thumbnail = new File(tempDir, attachmentName + ".jpg");
        if (thumbnail.exists()) {
            if (thumbnail.delete()) {
                logger.info("Successfully removed thumbnail at location: [{}]", thumbnail.getPath());
            } else {
                logger.warn("Failed to remove thumbnail at location: [{}]", thumbnail.getPath());
            }
        }
    }
}
