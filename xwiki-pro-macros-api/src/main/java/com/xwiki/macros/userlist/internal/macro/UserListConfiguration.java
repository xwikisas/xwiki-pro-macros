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
package com.xwiki.macros.userlist.internal.macro;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.xwiki.component.annotation.Component;
import org.xwiki.configuration.ConfigurationSource;
import org.xwiki.security.authorization.ContextualAuthorizationManager;
import org.xwiki.security.authorization.Right;
import org.xwiki.stability.Unstable;

/**
 * The API needed to retrieve the configuration.
 *
 * @version $Id$
 * @since 1.31.4
 */
@Component(roles = UserListConfiguration.class)
@Singleton
@Unstable
public class UserListConfiguration
{
    @Inject
    @Named("userlist")
    private ConfigurationSource configurationSource;

    @Inject
    private ContextualAuthorizationManager authorizationManager;

    /**
     * @return set of banned fields, or an empty set if the current user has admin rights.
     */
    public Set<String> bannedFields()
    {
        if (this.authorizationManager.hasAccess(Right.ADMIN)) {
            return Collections.emptySet();
        }

        String stringField = this.configurationSource.getProperty("bannedFields", "");
        return new HashSet<>(List.of(stringField.split(",")));
    }
}
