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
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.apache.commons.lang3.StringUtils;
import org.xwiki.component.annotation.Component;
import org.xwiki.contrib.cql.aqlparser.ast.AQLAtomicClause;
import org.xwiki.contrib.cql.aqlparser.ast.AQLStringLiteral;
import org.xwiki.contrib.cql.aqlparser.ast.AbstractAQLRightHandValue;
import org.xwiki.contrib.cql.query.converters.ConversionException;
import org.xwiki.contrib.cql.query.converters.DefaultCQLToSolrAtomConverter;
import org.xwiki.query.Query;
import org.xwiki.query.QueryException;
import org.xwiki.query.QueryManager;

import org.xwiki.contrib.cql.query.converters.Utils;

/**
 * Handle the metadataset cql field, used in the metadata-overview Confluence macro.
 * @since 1.30.0
 * @version $Id$
 */
@Component
@Singleton
@Named("metadataset")
public class MetadatasetCQLToSolrConverter extends DefaultCQLToSolrAtomConverter
{
    @Inject
    private QueryManager queryManager;

    @Override
    protected List<String> getSolrFields(AQLAtomicClause atom)
    {
        return List.of("class");
    }

    @Override
    protected String getSolrValue(AQLAtomicClause atom) throws ConversionException
    {
        AbstractAQLRightHandValue right = atom.getRight();
        if (right instanceof AQLStringLiteral) {
            String setName = StringUtils.removeStart(((AQLStringLiteral) right).getString(), "metadataset.");
            List<Object> classNames;
            try {
                classNames = queryManager.createQuery(
                        "select setObj.name"
                            + " from BaseObject setObj, StringProperty keyProp"
                            + " where keyProp.id.id = setObj.id"
                            + " and   keyProp.id.name = 'lowerKey'"
                            + " and   :key = keyProp.value",
                        Query.HQL)
                    .bindValue("key", setName.toLowerCase())
                    .execute();
            } catch (QueryException e) {
                throw new ConversionException("Failed to query the involved metadata sets", e, atom.getParserState());
            }

            return ("(" + classNames
                .stream()
                .map(Object::toString)
                .map(Utils::escapeSolr)
                .collect(Collectors.joining(" OR ")) + ")");
        }

        throw new ConversionException(
            "Unsupported metadataset clause. "
            + "Only clauses like metadataset = \"metadataset.someset\" (or using the IN operator) are supported",
            atom.getParserState());
    }
}
