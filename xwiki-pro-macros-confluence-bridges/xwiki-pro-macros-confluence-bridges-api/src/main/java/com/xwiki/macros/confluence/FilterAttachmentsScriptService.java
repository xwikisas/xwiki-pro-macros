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

import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import javax.inject.Named;
import javax.inject.Singleton;

import org.xwiki.component.annotation.Component;
import org.xwiki.model.reference.AttachmentReference;
import org.xwiki.script.service.ScriptService;
import org.xwiki.stability.Unstable;

/**
 * Script service for filtering attachments using regex expressions.
 *
 * @version $Id$
 * @since 1.26.18
 */
@Component
@Singleton
@Named("filterAttachments")
@Unstable
public class FilterAttachmentsScriptService implements ScriptService
{
    /**
     * Filters a list of AttachmentReferences by checking if their names match any of the given regex patterns.
     *
     * @param attachments the list of AttachmentReference objects to filter
     * @param regexes the list of regex patterns to match against attachment names
     * @return a list of AttachmentReferences whose names match at least one regex
     */
    public List<AttachmentReference> filterAttachmentResults(List<AttachmentReference> attachments,
        List<String> regexes)
    {
        if ((regexes != null && regexes.size() == 1 && regexes.get(0).isEmpty()) || regexes.isEmpty()) {
            return attachments;
        }

        List<Pattern> patterns = regexes.stream().map(Pattern::compile).collect(Collectors.toList());
        return attachments.stream().filter(
                attachment -> patterns.stream().anyMatch(pattern -> pattern.matcher(attachment.getName()).matches()))
            .collect(Collectors.toList());
    }
}
