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
package com.xwiki.macros.showhideif.macro;

import java.util.Collections;
import java.util.List;

import org.xwiki.model.reference.DocumentReference;
import org.xwiki.properties.annotation.PropertyDescription;
import org.xwiki.user.UserReference;

/**
 * Parameters for {@link com.xwiki.macros.showhideif.macro.HideIfMacro}
 * and {@link com.xwiki.macros.showhideif.macro.ShowIfMacro} Macros.
 * @version $Id: $
 */
public class MacroParameter
{
    private MacroParamMatchUsing matchUsing = MacroParamMatchUsing.ANY;

    private MacroParamAuthenticationType authenticationType = MacroParamAuthenticationType.NONE;

    private java.util.List<org.xwiki.user.UserReference> users = Collections.emptyList();

    private java.util.List<org.xwiki.model.reference.DocumentReference> groups = Collections.emptyList();

    /**
     * @version $Id: $
     */
    public enum MacroParamMatchUsing
    {
        /**
         * Match any of macro parameters.
         */
        ANY,
        /**
         * Match all macro parameters.
         */
        ALL
    }

    /**
     * @version $Id: $
     */
    public enum MacroParamAuthenticationType
    {
        /**
         * Match in any case if the user is authenticated or not.
         */
        NONE,
        /**
         * Match only if user is authenticated.
         */
        AUTHENTICATED,
        /**
         * Match only if user is not authenticated.
         */
        ANONYMOUS
    }

    /**
     * @return the match using value.
     */
    public MacroParamMatchUsing getMatchUsing()
    {
        return matchUsing;
    }

    /**
     * @param matchUsing the match using value.
     */
    @PropertyDescription("Set if we must match all contraint or only one. If set to \"all\", then all items set must "
        + "match.")
    public void setMatchUsing(MacroParamMatchUsing matchUsing)
    {
        this.matchUsing = matchUsing;
    }

    /**
     * @return the authentication type.
     */
    public MacroParamAuthenticationType getAuthenticationType()
    {
        return authenticationType;
    }

    /**
     * @param authenticationType the authentication type.
     */
    @PropertyDescription("The type of user to match, using authentication type:\n"
        + "* None - No authentication type is chosen.\n"
        + "* Authenticated - The user is logged in.\n"
        + "* Anonymous - The user is not logged in.")
    public void setAuthenticationType(
        MacroParamAuthenticationType authenticationType)
    {
        this.authenticationType = authenticationType;
    }

    /**
     * @return the list of users which should match.
     */
    public List<UserReference> getUsers()
    {
        return users;
    }

    /**
     * @param users the list of users which should match.
     */
    @PropertyDescription("The list of user(s) to match.")
    public void setUsers(List<UserReference> users)
    {
        this.users = users;
    }

    /**
     * @return the list of groups which should match.
     */
    public List<DocumentReference> getGroups()
    {
        return groups;
    }

    /**
     * @param groups the list of groups which should match.
     */
    @PropertyDescription("The list of user group(s) to match.")
    public void setGroups(List<DocumentReference> groups)
    {
        this.groups = groups;
    }
}
