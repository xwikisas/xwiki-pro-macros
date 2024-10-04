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

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.xwiki.component.annotation.Component;
import org.xwiki.contrib.confluence.resolvers.ConfluenceResolverException;
import org.xwiki.contrib.confluence.resolvers.ConfluenceSpaceResolver;
import org.xwiki.model.EntityType;
import org.xwiki.model.reference.EntityReference;
import org.xwiki.model.reference.EntityReferenceResolver;
import org.xwiki.script.service.ScriptService;
import org.xwiki.stability.Unstable;

import com.xpn.xwiki.api.Document;
import com.xwiki.macros.confluence.internal.ConfluenceSpaceUtils;

/**
 *  Script Service to manipulate Migrated Confluence spaces.
 * @since 1.19.0
 * @version $Id$
 */
@Component
@Singleton
@Named("confluence.spaces")
@Unstable
public class ConfluenceSpacesScriptService implements ScriptService
{
    @Inject
    private ConfluenceSpaceUtils confluenceSpaceUtils;

    @Inject
    private ConfluenceSpaceResolver confluenceSpaceResolver;

    @Inject
    private EntityReferenceResolver<String> resolver;

    /**
     * @return the root of the Confluence space in which the given document is.
     * @param documentReference the document of which the root of the Confluence space should be returned
     * @throws ConfluenceResolverException if something wrong happens
     */
    public EntityReference getConfluenceSpace(EntityReference documentReference) throws ConfluenceResolverException
    {
        return confluenceSpaceResolver.getSpace(documentReference);
    }

    /**
     * @return the root of the Confluence space in which the given document is.
     * @param documentReference the document of which the root of the Confluence space should be returned
     * @throws ConfluenceResolverException if something wrong happens
     */
    public EntityReference getConfluenceSpace(String documentReference) throws ConfluenceResolverException
    {
        return getConfluenceSpace(resolver.resolve(documentReference, EntityType.DOCUMENT));
    }

    /**
     * @return the root of the Confluence space in which the given document is.
     * @param document the document of which the root of the Confluence space should be returned
     * @throws ConfluenceResolverException if something wrong happens
     */
    public EntityReference getConfluenceSpace(Document document) throws ConfluenceResolverException
    {
        return getConfluenceSpace(document.getDocumentReference());
    }

    /**
     * @return the root of the Confluence space described by the parameter, or null if not found.
     * @param spaceKeyOrRef the space key, or "@self", or a XWiki reference to the space.
     */
    public EntityReference getSloppySpace(String spaceKeyOrRef)
    {
        return confluenceSpaceUtils.getSloppySpace(spaceKeyOrRef);
    }
}
