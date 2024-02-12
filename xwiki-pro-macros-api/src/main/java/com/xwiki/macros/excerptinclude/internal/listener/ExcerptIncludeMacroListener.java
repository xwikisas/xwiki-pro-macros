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
package com.xwiki.macros.excerptinclude.internal.listener;

import java.util.Arrays;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.hibernate.Session;
import org.slf4j.Logger;
import org.xwiki.bridge.event.DocumentCreatedEvent;
import org.xwiki.bridge.event.DocumentDeletedEvent;
import org.xwiki.bridge.event.DocumentUpdatedEvent;
import org.xwiki.component.annotation.Component;
import org.xwiki.model.reference.DocumentReference;
import org.xwiki.model.reference.DocumentReferenceResolver;
import org.xwiki.observation.AbstractEventListener;
import org.xwiki.observation.event.Event;
import org.xwiki.rendering.block.Block;
import org.xwiki.rendering.block.match.MacroBlockMatcher;
import org.xwiki.stability.Unstable;

import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.XWikiException;
import com.xpn.xwiki.doc.XWikiDocument;
import com.xpn.xwiki.store.XWikiHibernateBaseStore.HibernateCallback;
import com.xwiki.macros.excerptinclude.internal.StoreHandler;

/**
 * Listens to created, updated or deleted pages, checks for Excerpt Include Macro and adds/removes backlinks for the
 * excerpt macro page referenced in the excerpt-include macro. The backlinks are used to update the "0" parameter of the
 * excerpt-include macro in case of excerpt page rename.
 *
 * @version $Id$
 * @since 1.14.4
 */
@Component
@Named(ExcerptIncludeMacroListener.ROLE_HINT)
@Singleton
@Unstable
public class ExcerptIncludeMacroListener extends AbstractEventListener
{
    protected static final String ROLE_HINT = "ExcerptIncludeMacroListener";

    @Inject
    @Named("explicit")
    private DocumentReferenceResolver<String> explicitDocumentReferenceResolver;

    @Inject
    private Logger logger;

    @Inject
    private StoreHandler storeHandler;

    /**
     * Constructor.
     */
    public ExcerptIncludeMacroListener()
    {
        super(ROLE_HINT, Arrays.<Event>asList(new DocumentCreatedEvent(), new DocumentDeletedEvent(),
            new DocumentUpdatedEvent()));
    }

    @Override
    public void onEvent(Event event, Object source, Object data)
    {
        XWikiDocument document = (XWikiDocument) source;
        XWikiContext context = (XWikiContext) data;

        List<Block> macroBlocks =
            document.getXDOM().getBlocks(new MacroBlockMatcher("excerpt-include"), Block.Axes.DESCENDANT);

        if (!macroBlocks.isEmpty()) {
            try {
                // We need to delete existing links before saving the page's ones.
                storeHandler.deleteLinks(document.getId(), context);
                storeHandler.getStore().executeWrite(context, new HibernateCallback<Object>()
                {
                    @Override
                    public Object doInHibernate(Session session) throws XWikiException
                    {
                        // Is necessary to blank links from doc.
                        context.remove("links");

                        for (Block macroBlock : macroBlocks) {
                            DocumentReference excerptReference = explicitDocumentReferenceResolver
                                .resolve(macroBlock.getParameter("0"), document.getDocumentReference());
                            storeHandler.addXWikiLink(session, document, excerptReference, context);
                        }

                        return Boolean.TRUE;
                    }
                });
            } catch (Exception e) {
                logger.warn("Failed to update backlinks of excerpt macro", e);
            }
        }
    }
}
