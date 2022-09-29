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
package com.xwiki.macros.userprofile.macro;

import java.util.Arrays;
import java.util.List;

import org.xwiki.properties.annotation.PropertyDescription;
import org.xwiki.properties.annotation.PropertyName;

public class UserProfileMacroParameters
{
    private List<String> properties = Arrays.asList("company", "email", "phone", "address");

    private UserReference user;

    /**
     * @return a user
     */
    public UserReference getReference()
    {
        return this.user;
    }

    /**
     * Sets a user.
     *
     * @param user list of user references
     */
    @PropertyName("Reference")
    @PropertyDescription("User reference")
    public void setReference(UserReference user)
    {
        this.user = user;
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
