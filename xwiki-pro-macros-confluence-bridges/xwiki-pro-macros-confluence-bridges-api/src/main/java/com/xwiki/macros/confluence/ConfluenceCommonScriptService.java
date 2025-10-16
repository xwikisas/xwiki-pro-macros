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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.apache.solr.common.SolrDocument;
import org.xwiki.component.annotation.Component;
import org.xwiki.query.QueryException;
import org.xwiki.rendering.macro.wikibridge.WikiMacroParameters;
import org.xwiki.script.service.ScriptService;
import org.xwiki.stability.Unstable;

import com.xwiki.macros.confluence.cql.CQLUtils;

/**
 * Script service that encapsulates common methods need across multiple confluence macros.
 *
 * @version $Id$
 * @sice 1.28.2
 */
@Component
@Singleton
@Named("confluence.common")
@Unstable
public class ConfluenceCommonScriptService implements ScriptService
{
    @Inject
    private CQLUtils cqlUtils;

    public List<SolrDocument> getCQLResult(Map<String, Object> macro) throws QueryException
    {

        // WikiMacroParameters doesn't allow setting defaults (like using a Map with getOrDefault),
        // so we convert the parameters into a map.
        Map<String, Object> macroParameters = convertXWikiParametersToMap(((WikiMacroParameters) macro.get("params")));
        return cqlUtils.buildAndExecute(macroParameters);
    }

    private Map<String, Object> convertXWikiParametersToMap(WikiMacroParameters macroParameters)
    {
        Map<String, Object> parameterMap = new HashMap<>();
        macroParameters.getParameterNames().forEach((key) -> parameterMap.put(key, macroParameters.get(key)));
        return parameterMap;
    }
}
