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
package com.xwiki.macros.confluence.internal.cql;

import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.apache.commons.lang3.StringUtils;
import org.xwiki.component.annotation.Component;
import org.xwiki.contrib.cql.aqlparser.ast.AQLAtomicClause;
import org.xwiki.contrib.cql.query.converters.ConversionException;
import org.xwiki.contrib.cql.query.converters.DefaultCQLToSolrAtomConverter;
import org.xwiki.query.Query;
import org.xwiki.query.QueryException;
import org.xwiki.query.QueryManager;

/**
 * Handle the metadatafield cql fields, used in the metadata-overview Confluence macro.
 * @since 1.30.0
 * @version $Id$
 */
@Component
@Singleton
@Named("*.metadatafield.*")
public class MetadataFieldCQLToSolrConverter extends DefaultCQLToSolrAtomConverter
{
    private static final Pattern HANDLED_FIELDS = Pattern.compile("[^.]+\\.metadatafield\\.[^.]+");

    @Inject
    private QueryManager queryManager;

    @Override
    public Pattern getHandledFields()
    {
        return HANDLED_FIELDS;
    }

    @Override
    protected List<String> getSolrFields(AQLAtomicClause atom) throws ConversionException
    {
        String[] elems = StringUtils.split(atom.getField(), '.');
        String spaceKey = elems[0].toLowerCase();
        String metadataSetField = elems[2].toLowerCase();
        List<Object> classNames;
        try {
            classNames = queryManager.createQuery(
                    "select setObj.name"
                        + " from BaseObject setObj, StringProperty spaceKeyProp, DBStringListProperty fieldsProp"
                        + " where fieldsProp.id.id = setObj.id"
                        + " and   fieldsProp.id.name = 'lowerFields'"
                        + " and   spaceKeyProp.id.id = setObj.id"
                        + " and   spaceKeyProp.id.name = 'lowerSpaceKey'"
                        + " and   :spaceKey = spaceKeyProp.value"
                        + " and   :field in elements(fieldsProp.list)",
                    Query.HQL)
                .bindValue("field", metadataSetField)
                .bindValue("spaceKey", spaceKey)
                .execute();
        } catch (QueryException e) {
            throw new ConversionException("Failed to query the involved metadata sets", e, atom.getParserState());
        }

        return classNames
            .stream()
            .map(className -> "property." + className + '.' + metadataSetField)
            .collect(Collectors.toList());
    }
}
