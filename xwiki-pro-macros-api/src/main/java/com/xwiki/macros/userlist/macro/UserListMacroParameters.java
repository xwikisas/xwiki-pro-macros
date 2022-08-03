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

import org.xwiki.properties.annotation.PropertyDescription;
import org.xwiki.properties.annotation.PropertyDisplayType;

import com.xwiki.macros.userlist.UserResourceReference;

public class UserListMacroParameters
{
    private String users;

    /**
     *
     * @return a list of users
     */
    public String getUsers()
    {
        return this.users;
    }

    /**
     * Sets a list of users. A user is represented by a using a serialized DocumentReference.
     * @param users list of user references
     */
    @PropertyDisplayType(UserResourceReference.class)
    @PropertyDescription("List of users")
    public void setUsers(String users)
    {
        this.users = users;
    }
}
