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
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.inject.Provider;

import org.apache.commons.collections.CollectionUtils;
import org.xwiki.model.reference.DocumentReference;
import org.xwiki.properties.BeanDescriptor;
import org.xwiki.properties.PropertyDescriptor;
import org.xwiki.rendering.block.Block;
import org.xwiki.rendering.block.MacroBlock;
import org.xwiki.rendering.macro.MacroContentParser;
import org.xwiki.rendering.macro.MacroExecutionException;
import org.xwiki.rendering.macro.descriptor.DefaultContentDescriptor;
import org.xwiki.rendering.transformation.MacroTransformationContext;
import org.xwiki.user.group.GroupException;
import org.xwiki.user.group.GroupManager;
import org.xwiki.user.group.WikiTarget;

import com.xpn.xwiki.XWikiContext;
import com.xwiki.macros.AbstractProMacro;
import com.xwiki.macros.internal.grouplist.GroupReferenceList;
import com.xwiki.macros.internal.userlist.UserReferenceList;
import com.xwiki.macros.showhideif.macro.ShowHideIfMacroParameters;

/**
 * Base class for macros hide-if and show-if. This is mainly used to define if the constraint match or not.
 *
 * @version $Id: $
 * @since 1.23.0
 */
public abstract class AbstractShowHideIfMacro extends AbstractProMacro<ShowHideIfMacroParameters>
{
    private static final String CONTENT_DESCRIPTION = "The content to be displayed conditionally.";

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
            ShowHideIfMacroParameters.class);
    }

    @Override
    public boolean supportsInlineMode()
    {
        return true;
    }

    protected boolean doesMatch(ShowHideIfMacroParameters parameters)
        throws MacroExecutionException
    {
        boolean matchAnyRes = false;
        boolean matchAllRes = true;
        DocumentReference userReference = xwikiContextProvider.get().getUserReference();
        ShowHideIfMacroParameters.AuthType authTypeParam = parameters.getAuthenticationType();
        if (userReference != null) {
            UserReferenceList usersParam = parameters.getUsers();
            if (!CollectionUtils.isEmpty(usersParam)) {
                boolean res = usersParam.stream()
                    .anyMatch(u -> u.equals(userReference));
                matchAnyRes |= res;
                matchAllRes &= res;
            }
            GroupReferenceList groupsParam = parameters.getGroups();
            if (!CollectionUtils.isEmpty(groupsParam)) {
                Collection<DocumentReference> userGroupsMember = null;
                try {
                    userGroupsMember = groupManager.getGroups(userReference, WikiTarget.ALL, true);
                } catch (GroupException e) {
                    throw new MacroExecutionException(
                        "Can't check for group member", e);
                }
                boolean res = !CollectionUtils.intersection(userGroupsMember, groupsParam).isEmpty();
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
        return (parameters.getMatchUsing() == ShowHideIfMacroParameters.Matcher.ANY && matchAnyRes)
            || (parameters.getMatchUsing() == ShowHideIfMacroParameters.Matcher.ALL && matchAllRes);
    }

    /**
     * We need to check if all passed parameter are supported by XWiki because this macro could be imported from
     * confluence and on confluence a lot more parameter are supported. In case of the parameter is not supported it
     * could confuse the user thy this macro don't work as expected.
     *
     * @param context
     * @return
     */
    protected Optional<Block> maybeGetUnsupportedParameterErrorBlock(MacroTransformationContext context)
    {
        List<String> parametersWhiteList = List.of("atlassian-macro-output-type");
        java.util.Map<String, String> allParameters = context.getCurrentMacroBlock().getParameters();
        Set<String> parameterNames = allParameters.keySet();
        BeanDescriptor beanDescriptor = beanManager.getBeanDescriptor(ShowHideIfMacroParameters.class);
        List<String> beanPropertiesIds =
            beanDescriptor.getProperties().stream().map(PropertyDescriptor::getId).collect(Collectors.toList());
        List<String> unsupportedParameters = new LinkedList<>();
        for (String parameterName : parameterNames) {
            if (!beanPropertiesIds.contains(parameterName) && !parametersWhiteList.contains(parameterName)) {
                unsupportedParameters.add(parameterName);
            }
        }
        if (!unsupportedParameters.isEmpty()) {
            return Optional.of(
                new MacroBlock("error", Collections.emptyMap(),
                    // TODO escape content but seem that XWikiSyntaxEscaper
                    //  is only available since XWiki-platform 14.10.6
                    "Unsupported parameter for macro: " + String.join(", ", unsupportedParameters), false));
        }
        return Optional.empty();
    }
}
