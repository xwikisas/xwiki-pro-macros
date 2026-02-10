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
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.inject.Named;
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
import org.xwiki.rendering.script.RenderingScriptService;
import org.xwiki.rendering.transformation.MacroTransformationContext;
import org.xwiki.script.service.ScriptService;
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

    @Inject
    @Named("rendering")
    private ScriptService renderingScriptService;

    /**
     * Create and initialize the descriptor of the macro.
     *
     * @param name name of the macro.
     * @param description description of the macro.
     */
    protected AbstractShowHideIfMacro(String name, String description)
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

    private static final class ShowIfEvaluation
    {
        private boolean any;
        private boolean all = true;
    }

    protected boolean doesMatch(ShowHideIfMacroParameters parameters)
        throws MacroExecutionException
    {
        ShowIfEvaluation ev = new ShowIfEvaluation();
        DocumentReference userReference = xwikiContextProvider.get().getUserReference();
        evaluateUserAndGroupParams(parameters, userReference, ev);
        evaluateAuthParam(parameters, userReference, ev);
        evaluateDisplayTypeParam(parameters, ev);
        ShowHideIfMacroParameters.Matcher matchUsing = parameters.getMatchUsing();
        return (matchUsing == ShowHideIfMacroParameters.Matcher.ANY && ev.any)
            || (matchUsing == ShowHideIfMacroParameters.Matcher.ALL && ev.all);
    }

    private void evaluateDisplayTypeParam(ShowHideIfMacroParameters parameters, ShowIfEvaluation r)
    {
        if (parameters.getDisplayType() != null
            && parameters.getDisplayType() != ShowHideIfMacroParameters.DisplayType.NONE)
        {
            XWikiContext xcontext = xwikiContextProvider.get();
            boolean isExportPrintable = "export".equalsIgnoreCase(xcontext.getAction());
            switch (parameters.getDisplayType()) {
                case DEFAULT:
                    if (isExportPrintable) {
                        r.all = false;
                    } else {
                        r.any = true;
                    }
                    break;
                case PRINTABLE:
                    if (isExportPrintable) {
                        r.any = true;
                    } else {
                        r.all = false;
                    }
                    break;
                default:
                    break;
            }
        }
    }

    private static void evaluateAuthParam(ShowHideIfMacroParameters parameters, DocumentReference userReference,
        ShowIfEvaluation ev)
    {
        ShowHideIfMacroParameters.AuthType authTypeParam = parameters.getAuthenticationType();
        switch (authTypeParam) {
            case AUTHENTICATED:
                if (userReference == null) {
                    ev.all = false;
                } else {
                    ev.any = true;
                }
                break;
            case ANONYMOUS:
                if (userReference == null) {
                    ev.any = true;
                } else {
                    ev.all = false;
                }
                break;
            default:
                break;
        }
    }

    private void evaluateUserAndGroupParams(ShowHideIfMacroParameters parameters, DocumentReference userReference,
        ShowIfEvaluation ev) throws MacroExecutionException
    {
        if (userReference != null) {
            UserReferenceList usersParam = parameters.getUsers();
            if (!CollectionUtils.isEmpty(usersParam)) {
                boolean res = usersParam.stream()
                    .anyMatch(u -> u.equals(userReference));
                ev.any |= res;
                ev.all &= res;
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
                ev.any |= res;
                ev.all &= res;
            }
        }
    }

    /**
     * We need to check if all passed parameters are supported by XWiki because this macro could be imported from
     * confluence and on confluence a lot more parameters are supported. In case of the parameter is not supported it
     * could confuse the user why this macro don't work as expected.
     *
     * @param context the macro Transformation Context. Should come from the internalExecute method.
     * @return The error box block if an error should be shown.
     */
    protected Optional<Block> maybeGetUnsupportedParameterErrorBlock(MacroTransformationContext context)
    {
        // skip the parameters matching the name of the preserved unhandled parameters, to allow some parameters
        // to be kept as unhandled. This is hardcoded because there is no way to know this dynamically outside the
        // migration, so we only whitelist this prefix, as it's the most commonly used.
        String unhandledParametersPrefix = "confluence_";
        List<String> parametersWhiteList = List.of("atlassian-macro-output-type");
        Map<String, String> allParameters = context.getCurrentMacroBlock().getParameters();
        Set<String> parameterNames = allParameters.keySet();
        BeanDescriptor beanDescriptor = beanManager.getBeanDescriptor(ShowHideIfMacroParameters.class);
        List<String> beanPropertiesIds =
            beanDescriptor.getProperties().stream().map(PropertyDescriptor::getId).collect(Collectors.toList());
        List<String> unsupportedParameters = new LinkedList<>();
        for (String parameterName : parameterNames) {
            if (!beanPropertiesIds.contains(parameterName) && !parametersWhiteList.contains(parameterName)
                && !parameterName.startsWith(unhandledParametersPrefix)) {
                unsupportedParameters.add(parameterName);
            }
        }
        if (!unsupportedParameters.isEmpty()) {
            return Optional.of(
                new MacroBlock("error", Collections.emptyMap(),
                    // TODO XWikiSyntaxEscaper instead of RenderingScriptService
                    // after upgrading the parent of the app to >= 14.10.6
                    ((RenderingScriptService) renderingScriptService).escape(
                        "Unsupported parameter(s) for macro " + context.getCurrentMacroBlock().getId() + ": "
                        + String.join(", ", unsupportedParameters) + ".", context.getSyntax()) 
                        + " Due to this, the macro might have unexpected results.", false));
        }
        return Optional.empty();
    }
}
