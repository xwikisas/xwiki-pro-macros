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
package com.xwiki.macros.internal.updateReferences;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Provider;

import org.xwiki.component.annotation.Component;
import org.xwiki.job.AbstractJob;
import org.xwiki.model.reference.DocumentReference;
import org.xwiki.rendering.block.Block;
import org.xwiki.rendering.block.MacroBlock;
import org.xwiki.rendering.block.XDOM;
import org.xwiki.rendering.block.match.MacroBlockMatcher;
import org.xwiki.rendering.block.match.OrBlockMatcher;
import org.xwiki.rendering.macro.MacroRefactoring;
import org.xwiki.rendering.macro.MacroRefactoringException;

import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.XWikiException;
import com.xpn.xwiki.doc.XWikiDocument;

/**
 * Job that updates the value of a parameters that holds a reference, after the rename of a page.
 *
 * @version $Id$
 * @since 1.29
 */
@Component
@Named(UpdateReferencesJob.UPDATE_REFERENCES_JOB)
public class UpdateReferencesJob extends AbstractJob<UpdateReferencesJobRequest, UpdateReferencesJobStatus>
{
    /**
     * The unique identifier of the job used to update macro parameter references after a document rename. This value is
     * used when submitting the job through the {@link org.xwiki.job.JobExecutor}.
     */
    public static final String UPDATE_REFERENCES_JOB = "update-references-job";

    @Inject
    private Provider<XWikiContext> contextProvider;

    @Inject
    @Named("UpdateParametersRefactoring")
    private MacroRefactoring macroRefactoring;

    @Override
    public String getType()
    {
        return UPDATE_REFERENCES_JOB;
    }

    @Override
    protected void runInternal()
    {
        UpdateReferencesJobRequest request = getRequest();
        DocumentReference currentDocRef = request.getCurrentDocRef();
        DocumentReference targetDocRef = request.getTargetDocRef();
        Set<String> macrosToUpdate = request.getMacrosToUpdate();

        try {
            XWikiContext xContext = contextProvider.get();
            List<DocumentReference> backlinks =
                xContext.getWiki().getDocument(currentDocRef, xContext).getBackLinkedReferences(xContext);

            for (DocumentReference backlinkDocRef : backlinks) {
                XWikiDocument backlinkDoc = xContext.getWiki().getDocument(backlinkDocRef, xContext).clone();

                updateMacroReference(backlinkDoc, currentDocRef, targetDocRef, macrosToUpdate, xContext);
            }
        } catch (XWikiException | MacroRefactoringException e) {
            logger.warn("Update macro references for parameters interrupted", e);
        }
    }

    private void updateMacroReference(XWikiDocument document,
        DocumentReference currentReference, DocumentReference targetReference, Set<String> macrosToUpdate,
        XWikiContext xContext)
        throws XWikiException, MacroRefactoringException
    {
        XDOM backlinkDocXDOM = document.getXDOM();

        OrBlockMatcher orBlockMatcher =
            new OrBlockMatcher(macrosToUpdate
                .stream()
                .map(MacroBlockMatcher::new)
                .collect(Collectors.toList()));

        List<Block> macroBlocks = backlinkDocXDOM.getBlocks(orBlockMatcher, Block.Axes.DESCENDANT);

        boolean modified = false;

        for (Block macroBlock : macroBlocks) {
            if (macroBlock instanceof MacroBlock) {
                Optional<MacroBlock> updated = macroRefactoring.replaceReference(
                    (MacroBlock) macroBlock,
                    document.getDocumentReference(),
                    currentReference,
                    targetReference,
                    true
                );

                if (updated.isPresent()) {
                    modified = true;
                }
            }
        }

        if (modified) {
            document.setContent(backlinkDocXDOM);
            xContext.getWiki().saveDocument(document, "Updated macros references after page rename",
                xContext);
        }
    }
}
