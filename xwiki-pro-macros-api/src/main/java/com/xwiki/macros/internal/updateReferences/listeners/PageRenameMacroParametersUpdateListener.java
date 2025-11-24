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
package com.xwiki.macros.internal.updateReferences.listeners;

import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.slf4j.Logger;
import org.xwiki.bridge.event.DocumentCreatedEvent;
import org.xwiki.component.annotation.Component;
import org.xwiki.job.Job;
import org.xwiki.job.JobContext;
import org.xwiki.job.JobException;
import org.xwiki.job.JobExecutor;
import org.xwiki.job.event.JobStartedEvent;
import org.xwiki.model.reference.DocumentReference;
import org.xwiki.observation.AbstractEventListener;
import org.xwiki.observation.ObservationContext;
import org.xwiki.observation.event.Event;

import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.XWikiException;
import com.xpn.xwiki.doc.XWikiDocument;
import com.xwiki.macros.internal.updateReferences.UpdateReferencesJobRequest;

import static com.xwiki.macros.internal.updateReferences.UpdateReferencesJob.UPDATE_REFERENCES_JOB;

/**
 * Listens to rename of pages. Also, for pages that contains macros with references it will start a thread for updating
 * references of the macros parameters that are a document reference.
 *
 * @version $Id$
 * @since 1.29
 */
@Component
@Named(PageRenameMacroParametersUpdateListener.ROLE_HINT)
@Singleton
public class PageRenameMacroParametersUpdateListener extends AbstractEventListener
{
    /**
     * Map that defines which macro names should be inspected when searching for references inside macro parameters. The
     * key represents the macro name, while the value is the list of parameter names that may contain document or
     * attachment references and therefore need to be checked during refactoring operations.
     */
    public static final Map<String, List<String>> MACROS_TO_SEARCH = Map.of(
        "button", List.of("url"),
        "confluence_pagetree", List.of("root")
    );

    protected static final String ROLE_HINT = "PageRenameMacroParametersUpdateListener";

    @Inject
    protected JobContext jobContext;

    @Inject
    protected ObservationContext observationContext;

    @Inject
    private JobExecutor jobExecutor;

    @Inject
    private Logger logger;

    /**
     * Constructor.
     */
    public PageRenameMacroParametersUpdateListener()
    {
        super(ROLE_HINT, new DocumentCreatedEvent());
    }

    @Override
    public void onEvent(Event event, Object source, Object data)
    {
        if (observationContext.isIn(new JobStartedEvent("refactoring/rename"))) {
            Job job = jobContext.getCurrentJob();
            DocumentReference destinationRef = job.getRequest().getProperty("destination");
            List<DocumentReference> references = job.getRequest().getProperty("entityReferences");

            if (references != null && !references.isEmpty()) {
                XWikiDocument currentDoc = (XWikiDocument) source;

                DocumentReference originalDocRef = references.get(0);
                DocumentReference currentDocRef = currentDoc.getDocumentReference();

                if (destinationRef.equals(currentDocRef)) {
                    startUpdatingReferences((XWikiContext) data, currentDoc, originalDocRef);
                }
            }
        }
    }

    private void startUpdatingReferences(XWikiContext context, XWikiDocument currentDoc,
        DocumentReference originalDocRef)
    {
        UpdateReferencesJobRequest updateReferencesJobRequest = new UpdateReferencesJobRequest(
            originalDocRef,
            currentDoc.getDocumentReference(),
            MACROS_TO_SEARCH.keySet()
        );

        try {
            if (!context.getWiki().getDocument(originalDocRef, context).getBackLinkedReferences(context).isEmpty()) {
                jobExecutor.execute(UPDATE_REFERENCES_JOB, updateReferencesJobRequest);
            }
        } catch (XWikiException e) {
            logger.warn("Error when getting backlinks of renamed document", e);
        } catch (JobException e) {
            throw new RuntimeException(e);
        }
    }
}
