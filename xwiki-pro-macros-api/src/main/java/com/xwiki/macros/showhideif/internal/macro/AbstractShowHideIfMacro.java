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
package com.xwiki.macros.showhideif.internal.macro;

import java.util.Collection;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Provider;

import org.apache.commons.collections.CollectionUtils;
import org.xwiki.model.reference.DocumentReference;
import org.xwiki.rendering.block.Block;
import org.xwiki.rendering.macro.AbstractMacro;
import org.xwiki.rendering.macro.MacroContentParser;
import org.xwiki.rendering.macro.MacroExecutionException;
import org.xwiki.rendering.macro.descriptor.DefaultContentDescriptor;
import org.xwiki.user.UserReference;
import org.xwiki.user.group.GroupException;
import org.xwiki.user.group.GroupManager;
import org.xwiki.user.internal.document.DocumentUserReference;

import com.xpn.xwiki.XWikiContext;
import com.xwiki.macros.showhideif.macro.MacroParameter;

/**
 * Base class for macros hide-if and show-if. This is mainly used to define if the constraint match or not.
 *
 * @version $Id: $
 */
public abstract class AbstractShowHideIfMacro extends AbstractMacro<MacroParameter>
{
    private static final String CONTENT_DESCRIPTION = "the content to show conditionally";

    @Inject
    protected MacroContentParser contentParser;

    @Inject
    private Provider<XWikiContext> xwikiContextProvider;

    @Inject
    private GroupManager groupManager;

    /**
     * Create and initialize the descriptor of the macro.
     *
     * @param name name of the macro.
     * @param description description of the macro.
     */
    public AbstractShowHideIfMacro(String name, String description)
    {
        super(name, description,
            new DefaultContentDescriptor(CONTENT_DESCRIPTION, false, Block.LIST_BLOCK_TYPE),
            MacroParameter.class);
    }

    @Override
    public boolean supportsInlineMode()
    {
        return true;
    }

    protected boolean doesMatch(MacroParameter parameters)
        throws MacroExecutionException
    {
        boolean matchAnyRes = false;
        boolean matchAllRes = true;
        DocumentReference userReference = xwikiContextProvider.get().getUserReference();
        MacroParameter.MacroParamAuthenticationType authTypeParam = parameters.getAuthenticationType();
        if (userReference != null) {
            List<UserReference> usersParam = parameters.getUsers();
            if (!CollectionUtils.isEmpty(usersParam)) {
                boolean res = usersParam.stream()
                    .anyMatch(u -> ((DocumentUserReference) u).getReference().equals(userReference));
                matchAnyRes |= res;
                matchAllRes &= res;
            }
            List<DocumentReference> groupsParam = parameters.getGroups();
            if (!CollectionUtils.isEmpty(groupsParam)) {
                Collection<DocumentReference> userGroupsMember = null;
                try {
                    userGroupsMember = groupManager.getMembers(userReference, true);
                } catch (GroupException e) {
                    throw new MacroExecutionException(
                        "Can't check for group member", e);
                }
                boolean res = CollectionUtils.intersection(userGroupsMember, groupsParam).isEmpty();
                matchAnyRes |= res;
                matchAllRes &= res;
            }
        }
        switch (authTypeParam) {
            case AUTHENTICATED:
                if (userReference == null) {
                    matchAllRes = false;
                } else {
                    matchAnyRes = true;
                }
                break;
            case ANONYMOUS:
                if (userReference == null) {
                    matchAnyRes = true;
                } else {
                    matchAllRes = false;
                }
                break;
            default:
                break;
        }
        return (parameters.getMatchUsing() == MacroParameter.MacroParamMatchUsing.ANY && matchAnyRes)
            || (parameters.getMatchUsing() == MacroParameter.MacroParamMatchUsing.ALL && matchAllRes);
    }
}
