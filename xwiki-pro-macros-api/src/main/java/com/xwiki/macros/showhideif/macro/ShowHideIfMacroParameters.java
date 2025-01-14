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

import org.xwiki.properties.annotation.PropertyDescription;

import com.xwiki.macros.internal.grouplist.GroupReferenceList;
import com.xwiki.macros.internal.userlist.UserReferenceList;

/**
 * Parameters for {@link com.xwiki.macros.showhideif.macro.HideIfMacro} and
 * {@link com.xwiki.macros.showhideif.macro.ShowIfMacro} Macros.
 *
 * @version $Id: $
 * @since 1.23.0
 */
public class ShowHideIfMacroParameters
{
    private Matcher matchUsing = Matcher.ANY;

    private AuthType authenticationType = AuthType.NONE;

    private UserReferenceList users;

    private GroupReferenceList groups;

    private DisplayType displayType;

    /**
     * @version $Id: $
     */
    public enum Matcher
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
    public enum AuthType
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
     * @version $Id: $
     * @since 1.26.0
     */
    public enum DisplayType
    {
        /**
         * Display in any cases.
         */
        NONE,
        /**
         * Dislay in general case except in PRINTABLE cases.
         */
        DEFAULT,
        /**
         * Display in printable case. This include ODT and PDF export and printing.
         */
        PRINTABLE,
    }

    /**
     * @return the match using value.
     */
    public Matcher getMatchUsing()
    {
        return matchUsing;
    }

    /**
     * @param matchUsing the match using value.
     */
    @PropertyDescription("Set if we must match all contraints or only one. If set to \"all\", then all items set must "
        + "match.")
    public void setMatchUsing(Matcher matchUsing)
    {
        this.matchUsing = matchUsing;
    }

    /**
     * @return the authentication type.
     */
    public AuthType getAuthenticationType()
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
        AuthType authenticationType)
    {
        this.authenticationType = authenticationType;
    }

    /**
     * @return the list of users which should match.
     */
    public UserReferenceList getUsers()
    {
        return users;
    }

    /**
     * @param users the list of users which should match.
     */
    @PropertyDescription("The list of user(s) to match.")
    public void setUsers(UserReferenceList users)
    {
        this.users = users;
    }

    /**
     * @return the list of groups which should match.
     */
    public GroupReferenceList getGroups()
    {
        return groups;
    }

    /**
     * @param groups the list of groups which should match.
     */
    @PropertyDescription("The list of user group(s) to match.")
    public void setGroups(GroupReferenceList groups)
    {
        this.groups = groups;
    }

    /**
     * @return the display type which should match.
     */
    public DisplayType getDisplayType()
    {
        return displayType;
    }

    /**
     * @param displayType the display type which should match.
     */
    @PropertyDescription("The type of display to show this content. I could be:\n"
        + "* None: don't take into account for this parameter.\n"
        + "* Default: Show on screen (when the content is not printed).\n"
        + "* Printable: Show when it's printed. Will be enabled for PDF or ODT export.")
    public void setDisplayType(DisplayType displayType)
    {
        this.displayType = displayType;
    }
}
