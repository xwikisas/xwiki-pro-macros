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
package com.xwiki.macros.userlist.macro;

import java.util.Arrays;
import java.util.List;

import org.xwiki.properties.annotation.PropertyDescription;
import org.xwiki.properties.annotation.PropertyDisplayType;
import org.xwiki.properties.annotation.PropertyName;

public class UserListMacroParameters
{
    private UserReferenceList users;

    private List<String> properties = Arrays.asList("avatar", "username");

    /**
     * @return a list of users
     */
    public UserReferenceList getUsers()
    {
        return this.users;
    }

    /**
     * Sets a list of users.
     *
     * @param users list of user references
     */
    @PropertyDisplayType(UserReferenceList.class)
    @PropertyDescription("List of users")
    public void setUsers(UserReferenceList users)
    {
        this.users = users;
    }

    /**
     * @return list of properties to be displayed
     */
    public List<String> getProperties()
    {
        return this.properties;
    }

    /**
     * Sets a list of property names.
     *
     * @param properties list of user properties
     */
    @PropertyName("Properties")
    @PropertyDescription("List of user properties to be displayed")
    public void setProperties(List<String> properties)
    {
        this.properties = properties;
    }
}
