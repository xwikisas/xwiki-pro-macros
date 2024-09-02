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

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Provider;
import javax.inject.Singleton;

import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.doc.XWikiDocument;
import com.xwiki.macros.AbstractProMacro;
import com.xwiki.macros.confluence.internal.ConfluenceSpaceUtils;
import com.xwiki.macros.confluence.internal.XDOMUtils;

import org.xwiki.component.annotation.Component;
import org.xwiki.component.manager.ComponentLookupException;
import org.xwiki.component.manager.ComponentManager;
import org.xwiki.localization.ContextualLocalizationManager;
import org.xwiki.model.reference.AttachmentReferenceResolver;
import org.xwiki.model.reference.DocumentReferenceResolver;
import org.xwiki.model.reference.EntityReference;
import org.xwiki.rendering.block.Block;
import org.xwiki.rendering.block.BulletedListBlock;
import org.xwiki.rendering.block.LinkBlock;
import org.xwiki.rendering.block.ListItemBlock;
import org.xwiki.rendering.block.MacroBlock;
import org.xwiki.rendering.block.XDOM;
import org.xwiki.rendering.block.match.AnyBlockMatcher;
import org.xwiki.rendering.listener.reference.ResourceReference;
import org.xwiki.rendering.listener.reference.ResourceType;
import org.xwiki.rendering.macro.MacroExecutionException;
import org.xwiki.rendering.syntax.Syntax;
import org.xwiki.rendering.transformation.MacroTransformationContext;
import org.xwiki.stability.Unstable;

/**
 * The Confluence outgoing-links bridge macro.
 * @since 1.22.0
 * @version $Id$
 */
@Component
@Named("confluence_outgoing-links")
@Singleton
@Unstable
public class ConfluenceOutgoingLinksMacro extends AbstractProMacro<ConfluenceOutgoingLinksMacroParameters>
{
    @Inject
    private Provider<ComponentManager> componentManagerProvider;

    @Inject
    private Provider<XWikiContext> contextProvider;

    @Inject
    private ContextualLocalizationManager localizationManager;

    @Inject
    private ConfluenceSpaceUtils confluenceSpaceUtils;

    @Inject
    private DocumentReferenceResolver<String> documentReferenceResolver;

    @Inject
    private AttachmentReferenceResolver<String> attachmentReferenceResolver;

    /**
     * Constructor.
     */
    public ConfluenceOutgoingLinksMacro()
    {
        super("Confluence Outgoing Links", "Confluence bridge macro for outgoing-links.",
            ConfluenceOutgoingLinksMacroParameters.class);
    }

    @Override
    public List<Block> internalExecute(ConfluenceOutgoingLinksMacroParameters parameters, String content,
        MacroTransformationContext context) throws MacroExecutionException
    {
        XWikiDocument document = contextProvider.get().getDoc();

        Set<LinkBlock> links = new LinkedHashSet<>();
        try {
            String spacesStr = parameters.getSpaces();
            String[] spaces = (spacesStr == null || spacesStr.isEmpty()) ? null : spacesStr.split(",");
            browseXDOM(document.getXDOM(), document.getSyntax(), links, spaces);
        } catch (ComponentLookupException e) {
            throw new MacroExecutionException("Could not compute outgoing links", e);
        }

        if (links.isEmpty()) {
            return Collections.singletonList(new MacroBlock(
                "info",
                Collections.emptyMap(),
                localizationManager.getTranslationPlain("rendering.macro.confluence_outgoinglinks.noresults"),
                context.isInline()
            ));
        }

        return Collections.singletonList(
            new BulletedListBlock(
                new ArrayList<>(
                    links.stream().map(link ->
                        new ListItemBlock(Collections.singletonList(link))).collect(Collectors.toList())
                )
            )
        );
    }

    @Override
    public boolean supportsInlineMode()
    {
        return true;
    }

    private void browseXDOM(Block block, Syntax syntaxId, Set<LinkBlock> links, String[] spaces)
        throws ComponentLookupException
    {
        for (Block b : block.getBlocks(new AnyBlockMatcher(), Block.Axes.DESCENDANT)) {
            if (b instanceof MacroBlock) {
                XDOM content = XDOMUtils.getMacroXDOM(this.componentManagerProvider.get(), (MacroBlock) b, syntaxId);
                if (content != null) {
                    browseXDOM(content, syntaxId, links, spaces);
                }
            } else {
                if (b instanceof LinkBlock) {
                    maybeAddLink(links, spaces, (LinkBlock) b);
                }
                browseXDOM(b, syntaxId, links, spaces);
            }
        }
    }

    private void maybeAddLink(Set<LinkBlock> links, String[] spaces, LinkBlock b)
    {
        if (spaces == null) {
            links.add(b);
        } else {
            ResourceReference reference = b.getReference();
            if (reference.getType() == ResourceType.DOCUMENT) {
                EntityReference currentDocRef = contextProvider.get().getDoc().getDocumentReference();
                EntityReference ref = documentReferenceResolver.resolve(reference.getReference(),
                    currentDocRef);
                if (isReferenceInSpaces(spaces, ref)) {
                    links.add(b);
                }
            } else if (reference.getType() == ResourceType.ATTACHMENT) {
                EntityReference currentDocRef = contextProvider.get().getDoc().getDocumentReference();
                EntityReference ref = attachmentReferenceResolver.resolve(reference.getReference(),
                    currentDocRef);
                if (isReferenceInSpaces(spaces, ref)) {
                    links.add(b);
                }
            }
        }
    }

    private boolean isReferenceInSpaces(String[] spaces, EntityReference reference)
    {
        for (String space : spaces) {
            EntityReference spaceRef = confluenceSpaceUtils.getSloppySpace(space);
            if (spaceRef != null && reference.hasParent(spaceRef)) {
                return true;
            }
        }
        return false;
    }
}
