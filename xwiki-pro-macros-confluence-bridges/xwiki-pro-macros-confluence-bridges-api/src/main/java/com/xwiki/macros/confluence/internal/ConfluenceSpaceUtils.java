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

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Provider;
import javax.inject.Singleton;

import org.slf4j.Logger;
import org.xwiki.component.annotation.Component;
import org.xwiki.contrib.confluence.resolvers.ConfluenceResolverException;
import org.xwiki.contrib.confluence.resolvers.ConfluenceSpaceKeyResolver;
import org.xwiki.contrib.confluence.resolvers.ConfluenceSpaceResolver;
import org.xwiki.model.EntityType;
import org.xwiki.model.reference.EntityReference;
import org.xwiki.model.reference.EntityReferenceResolver;
import org.xwiki.model.reference.EntityReferenceSerializer;
import org.xwiki.query.Query;
import org.xwiki.query.QueryException;
import org.xwiki.query.QueryManager;
import org.xwiki.stability.Unstable;

import com.xpn.xwiki.XWikiContext;

/**
 * Tools to manipulate migrated Confluence spaces.
 *
 * @version $Id$
 * @since 1.19.0
 */
@Component(roles = ConfluenceSpaceUtils.class)
@Singleton
@Unstable
public class ConfluenceSpaceUtils
{
    @Inject
    private Logger logger;

    @Inject
    private Provider<XWikiContext> contextProvider;

    @Inject
    private ConfluenceSpaceKeyResolver confluenceSpaceKeyResolver;

    @Inject
    private ConfluenceSpaceResolver confluenceSpaceResolver;

    @Inject
    private EntityReferenceResolver<String> resolver;

    @Inject
    private QueryManager queryManager;

    @Inject
    @Named("compact")
    private EntityReferenceSerializer<String> serializer;

    /**
     * This method is meant to handle macro parameters coming from Confluence that could contain both XWiki spaces or
     * Confluence (pseudo) space keys.
     *
     * @param spaceKeyOrRef the space key, or "@self", or a XWiki reference to the space.
     * @return the root of the Confluence space described by the parameter, or null if not found.
     */
    public EntityReference getSloppySpace(String spaceKeyOrRef)
    {
        try {
            if (spaceKeyOrRef.contains("@self")) {
                return confluenceSpaceResolver.getSpace(contextProvider.get().getDoc().getDocumentReference());
            }

            if (spaceKeyOrRef.startsWith("confluenceSpace:")) {
                // The spaceKeyOrRef might be prefixed with confluenceSpace: when at migration time we didn't know
                // where the space is. In the meantime it might have been migrated, and we should check if the space
                // is available in XWiki
                String unprefixedSpaceKey = spaceKeyOrRef.substring(spaceKeyOrRef.indexOf(':') + 1);
                return confluenceSpaceKeyResolver.getSpaceByKey(unprefixedSpaceKey);
            }
            EntityReference spaceRef = resolver.resolve(spaceKeyOrRef, EntityType.SPACE);
            Query checkSpaceExistence =
                queryManager.createQuery("SELECT count(reference) FROM XWikiSpace s where s.reference=:reference",
                    Query.HQL);
            checkSpaceExistence.bindValue("reference", serializer.serialize(spaceRef));
            long count = (long) checkSpaceExistence.execute().get(0);
            if (count > 0) {
                return spaceRef;
            }

            return confluenceSpaceKeyResolver.getSpaceByKey(spaceKeyOrRef);
        } catch (ConfluenceResolverException e) {
            logger.warn("Could not convert space [{}] to an entity reference", spaceKeyOrRef, e);
        } catch (QueryException e) {
            throw new RuntimeException(e);
        }
        return null;
    }
}


