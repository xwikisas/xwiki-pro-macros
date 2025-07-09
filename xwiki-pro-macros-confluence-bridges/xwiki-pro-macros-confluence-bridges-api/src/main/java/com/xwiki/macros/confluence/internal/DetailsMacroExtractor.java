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
package com.xwiki.macros.confluence.internal;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.inject.Singleton;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.xwiki.component.annotation.Component;
import org.xwiki.component.manager.ComponentLookupException;
import org.xwiki.component.manager.ComponentManager;
import org.xwiki.model.reference.AttachmentReference;
import org.xwiki.model.reference.DocumentReference;
import org.xwiki.model.reference.DocumentReferenceResolver;
import org.xwiki.model.reference.EntityReferenceSerializer;
import org.xwiki.rendering.block.Block;
import org.xwiki.rendering.block.ImageBlock;
import org.xwiki.rendering.block.MacroBlock;
import org.xwiki.rendering.block.XDOM;
import org.xwiki.rendering.block.match.ClassBlockMatcher;
import org.xwiki.rendering.listener.reference.ResourceReference;
import org.xwiki.rendering.listener.reference.ResourceType;
import org.xwiki.stability.Unstable;

import static com.xwiki.macros.confluence.internal.XDOMUtils.getMacroXDOM;
import static org.apache.commons.lang3.StringUtils.defaultString;

/**
 * Handles the extraction and processing of the details macro xdoms.
 *
 * @version $Id$
 * @since 1.27.1
 */
@Singleton
@Unstable
@Component(roles = DetailsMacroExtractor.class)
public class DetailsMacroExtractor
{
    private static final ClassBlockMatcher MACRO_MATCHER = new ClassBlockMatcher(MacroBlock.class);

    private static final String ID = "id";

    @Inject
    private Provider<ComponentManager> componentManagerProvider;

    @Inject
    private EntityReferenceSerializer<String> entityReferenceSerializer;

    @Inject
    private DocumentReferenceResolver<String> documentReferenceResolver;

    @Inject
    private Logger logger;

    /**
     * Recursively extracts all details macros from a page.
     *
     * @param xdom the XDOM of the page from which to extract details macros
     * @param syntaxId the syntax of the page
     * @param id the ID of the details macro to include; if empty, includes all details macros
     * @return a list of XDOMs representing the extracted details macros
     */
    public List<XDOM> findDetailsMacros(XDOM xdom, String syntaxId, String id)
    {

        DocumentReference sourceDocument =
            documentReferenceResolver.resolve((String) xdom.getMetaData().getMetaData("source"));

        List<XDOM> results = new ArrayList<>(1);

        List<MacroBlock> macros = xdom.getBlocks(MACRO_MATCHER, Block.Axes.DESCENDANT_OR_SELF);
        for (MacroBlock macroBlock : macros) {
            try {
                if (StringUtils.equals("confluence_details", macroBlock.getId())) {
                    // If the id is blank we should display all the details macro, otherwise the id of the details macro
                    // should match the id given as a parameter.
                    if (!StringUtils.isNotBlank(id)
                        || StringUtils.equals(id, defaultString(macroBlock.getParameter(ID))))
                    {
                        XDOM detailXDOM = getMacroXDOM(componentManagerProvider.get(), macroBlock, syntaxId);
                        processImageBlocks(detailXDOM.getBlocks(new ClassBlockMatcher(ImageBlock.class),
                            Block.Axes.DESCENDANT_OR_SELF), sourceDocument);

                        results.add(detailXDOM);
                    }
                } else {
                    XDOM macroXDOM = getMacroXDOM(componentManagerProvider.get(), macroBlock, syntaxId);
                    if (macroXDOM != null) {
                        results.addAll(findDetailsMacros(macroXDOM, syntaxId, id));
                    }
                }
            } catch (ComponentLookupException e) {
                logger.error("Component lookup error trying to find the confluence_details macro", e);
            }
        }
        return results;
    }

    private void processImageBlocks(List<ImageBlock> imageBlocks, DocumentReference sourceDocument)
    {
        for (ImageBlock block : imageBlocks) {
            // If the image type is attachment then it means that is attached to a page and the XDOM parser will return
            // the reference as it is relative or not.
            if (block.getReference().getType() == ResourceType.ATTACHMENT) {
                // As per this documentation(https://www.xwiki.org/xwiki/bin/view/
                // ~Documentation/UserGuide/Features/XWikiSyntax/?syntax=2.1&section=Images)
                // if the reference doesn't contain an @ is relative and not absolute.
                if (!block.getReference().getReference().contains("@")) {
                    AttachmentReference attachmentReference =
                        new AttachmentReference(block.getReference().getReference(), sourceDocument);
                    String serializedRef = this.entityReferenceSerializer.serialize(attachmentReference);
                    ResourceReference resourceReference = new ResourceReference(serializedRef, ResourceType.ATTACHMENT);
                    ImageBlock newBlock =
                        new ImageBlock(resourceReference, block.isFreeStandingURI(), block.getParameters());
                    Block parent = block.getParent();
                    int index = parent.getChildren().indexOf(block);
                    parent.getChildren().set(index, newBlock);
                }
            }
        }
    }
}
