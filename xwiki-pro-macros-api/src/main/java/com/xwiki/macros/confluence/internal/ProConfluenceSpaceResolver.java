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

import org.xwiki.component.annotation.Component;
import org.xwiki.contrib.cql.aqlparser.ast.AbstractAQLRightHandValue;
import org.xwiki.contrib.cql.query.converters.ConfluenceSpaceResolver;
import org.xwiki.contrib.cql.query.converters.ConversionException;
import org.xwiki.model.reference.EntityReference;
import org.xwiki.query.QueryException;
import org.xwiki.query.QueryManager;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.xpn.xwiki.XWikiContext;

/**
 * Find a space with the given Confluence space key using the Confluence Migrator Pro's Link Mapping State.
 * @version $Id$
 * @since 1.19.0
 */

@Component
@Named("prolinkmapping")
@Singleton
public class ProConfluenceSpaceResolver implements ConfluenceSpaceResolver
{
    @Inject
    private QueryManager queryManager;

    @Inject
    private Provider<XWikiContext> contextProvider;

    @Inject
    private ConfluenceSpaceUtils confluenceSpaceUtils;

    @Override
    public EntityReference getSpaceByKey(AbstractAQLRightHandValue node, String spaceKey) throws ConversionException
    {
        try {
            return confluenceSpaceUtils.getSpaceByKey(spaceKey);
        } catch (JsonProcessingException | QueryException e) {
            throw new ConversionException("Could not read the link mapping", e,
                node == null ? null : node.getParserState());
        }
    }

    @Override
    public EntityReference getCurrentConfluenceSpace(AbstractAQLRightHandValue node) throws ConversionException
    {
        EntityReference document = contextProvider.get().getDoc().getDocumentReference();
        try {
            return confluenceSpaceUtils.getConfluenceSpace(document);
        } catch (QueryException | JsonProcessingException e) {
            throw new ConversionException(e, node == null ? null : node.getParserState());
        }
    }
}
