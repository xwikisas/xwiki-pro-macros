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

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import javax.inject.Named;
import javax.inject.Singleton;

import org.xwiki.component.annotation.Component;
import org.xwiki.script.service.ScriptService;
import org.xwiki.stability.Unstable;

/**
 * Provides Scripting APIs for the Microsoft Stream macro.
 *
 * @version $Id$
 * @since 1.33.0
 */
@Component
@Singleton
@Named("msStream")
@Unstable
public class MicrosoftStreamScriptService implements ScriptService
{
    private static final List<String> WILDCARD_DOMAINS =
        Arrays.asList("microsoftstream.com", "sharepoint.com", "clipchamp.com");

    /**
     * Checks whether the given URL is hosted on a trusted Microsoft domain (Stream Classic, SharePoint or Clipchamp).
     *
     * @param url the URL to validate
     * @return {@code true} if the URL host is one of the allowed Microsoft domains, {@code false} otherwise
     */
    public boolean isTrustedURL(String url)
    {
        if (url == null || url.isEmpty()) {
            return false;
        }

        String host = extractHost(url);
        if (host == null) {
            return false;
        }

        String normalizedHost = host.toLowerCase(Locale.ROOT);
        return WILDCARD_DOMAINS.stream()
            // Only accept the domain itself or a subdomain of it.
            .anyMatch(domain -> normalizedHost.equals(domain) || normalizedHost.endsWith("." + domain));
    }

    private String extractHost(String url)
    {
        URI uri;
        try {
            uri = new URI(url);
        } catch (URISyntaxException e) {
            return null;
        }
        return uri.getHost();
    }
}
