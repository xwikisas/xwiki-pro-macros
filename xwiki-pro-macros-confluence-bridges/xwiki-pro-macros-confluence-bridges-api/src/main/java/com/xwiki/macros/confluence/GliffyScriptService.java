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

import org.apache.commons.lang3.StringUtils;
import org.xwiki.component.annotation.Component;
import org.xwiki.contrib.confluence.resolvers.ConfluencePageIdResolver;
import org.xwiki.contrib.confluence.resolvers.ConfluenceResolverException;
import org.xwiki.model.reference.DocumentReference;
import org.xwiki.model.reference.DocumentReferenceResolver;
import org.xwiki.model.reference.EntityReference;
import org.xwiki.script.service.ScriptService;

/**
 * Gliffy bridge script service.
 *
 * @version $Id$
 * @since 1.26.20
 */
@Component
@Named("gliffyscript")
@Singleton
public class GliffyScriptService implements ScriptService
{
    @Inject
    private ConfluencePageIdResolver confluencePageIdResolver;

    @Inject
    private DocumentReferenceResolver<String> resolver;

    /**
     * @param id confluence if of the page
     * @return the document reference of the migrate page
     */
    public DocumentReference getReferenceFromConfluenceID(String id) throws ConfluenceResolverException
    {
        if (StringUtils.isNumeric(id)) {
            EntityReference entityReference = confluencePageIdResolver.getDocumentById(Integer.valueOf(id));
            return new DocumentReference(entityReference);
        }
        return resolver.resolve(id);
    }
}
