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
package com.xwiki.macros.script;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import javax.inject.Named;
import javax.inject.Singleton;

import org.xwiki.component.annotation.Component;
import org.xwiki.script.service.ScriptService;
import org.xwiki.stability.Unstable;

/**
 * Tools to manipulate the data needed for the contributors macro.
 *
 * @version $Id$
 * @since 1.27.3
 */
@Component
@Named("contributors")
@Singleton
@Unstable
public class ContributorsScriptService implements ScriptService
{
    /**
     * Sort the list of contributors by the given key.
     *
     * @param contributors list of contributors
     * @param key which key to use when sorting
     * @param reverse if the list should be reversed
     * @param limit how many elements the list should have after sorting, if the parameter is missing the full list
     * is returned
     * @return list of sorted contributors
     */
    public List<Map<String, Object>> sortContributors(Collection<Map<String, Object>> contributors, String key,
        boolean reverse, int limit)
    {
        List<Map<String, Object>> result = new ArrayList<>(contributors);

        Collections.sort(result, new Comparator<Map<String, Object>>()
        {
            @Override
            public int compare(Map<String, Object> v1, Map<String, Object> v2)
            {

                int cmp = ((Comparable<Object>) v1.get(key)).compareTo(v2.get(key));
                return reverse ? -cmp : cmp;
            }
        });

        if (0 < limit) {
            return result.subList(0, limit < result.size() ? limit : result.size());
        }
        return result;
    }
}
