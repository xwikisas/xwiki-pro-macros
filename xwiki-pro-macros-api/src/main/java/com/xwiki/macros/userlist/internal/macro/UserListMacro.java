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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.xwiki.component.annotation.Component;
import org.xwiki.displayer.HTMLDisplayerException;
import org.xwiki.displayer.HTMLDisplayerManager;
import org.xwiki.model.reference.DocumentReference;
import org.xwiki.model.reference.EntityReference;
import org.xwiki.model.reference.EntityReferenceSerializer;
import org.xwiki.query.Query;
import org.xwiki.query.QueryException;
import org.xwiki.query.QueryFilter;
import org.xwiki.query.QueryManager;
import org.xwiki.rendering.block.Block;
import org.xwiki.rendering.block.RawBlock;
import org.xwiki.rendering.macro.MacroExecutionException;
import org.xwiki.rendering.syntax.Syntax;
import org.xwiki.rendering.transformation.MacroTransformationContext;
import org.xwiki.text.StringUtils;
import org.xwiki.wiki.user.UserScope;
import org.xwiki.wiki.user.WikiUserManager;
import org.xwiki.wiki.user.WikiUserManagerException;

import com.xwiki.macros.AbstractProMacro;
import com.xwiki.macros.userlist.macro.GroupReferenceList;
import com.xwiki.macros.userlist.macro.UserListMacroParameters;
import com.xwiki.macros.userlist.macro.UserReferenceList;

/**
 * This macro displays a list of users with their name and avatar.
 *
 * @version $Id$
 */
@Component
@Named("userList")
@Singleton
public class UserListMacro extends AbstractProMacro<UserListMacroParameters>
{
    @Inject
    private HTMLDisplayerManager htmlDisplayerManager;

    @Inject
    private QueryManager queryManager;

    @Inject
    @Named("local")
    private EntityReferenceSerializer<String> localSerializer;

    @Inject
    private WikiUserManager wikiUserManager;

    @Inject
    @Named("document")
    private QueryFilter documentFilter;

    /**
     * Create and initialize the descriptor of the macro.
     */
    public UserListMacro()
    {
        super("User list", "Displays a custom list of users with their avatar",
            UserListMacroParameters.class);
    }

    private void addUsersFromWiki(UserReferenceList users, String wiki, List<String> groups)
        throws QueryException
    {
        Query query;

        if (groups.isEmpty()) {
            query = queryManager.createQuery(
                "select doc.fullName from Document doc, doc.object(XWiki.XWikiUsers) obj "
                    + "order by doc.fullName",
                Query.XWQL
            );
        } else {
            query = queryManager.createQuery(
                "select g.member from "
                    + "Document doc, "
                    + "doc.object(XWiki.XWikiGroups) g "
                    + "where doc.fullName in (:groups) and g.member <> ''"
                    + "order by g.member",
                Query.XWQL
            );
            query.bindValue("groups", groups);
        }
        query.addFilter(this.documentFilter);
        for (Object userReference : query.setWiki(wiki).execute()) {
            users.add((DocumentReference) userReference);
        }
    }

    @Override
    public List<Block> internalExecute(UserListMacroParameters parameters, String content,
        MacroTransformationContext context)
        throws MacroExecutionException
    {
        Map<String, String> params = new HashMap<>();
        try {
            params.put("properties", StringUtils.join(parameters.getProperties(), ','));
            params.put("fixedTableLayout", String.valueOf(parameters.isFixedTableLayout()));

            UserReferenceList users = parameters.getUsers();
            if (users == null) {
                users = new UserReferenceList();
            }

            GroupReferenceList groupReferences = parameters.getGroups();
            if (groupReferences == null) {
                groupReferences = new GroupReferenceList();
            }

            List<String> groups = new ArrayList<>();
            for (EntityReference group : groupReferences) {
                groups.add(localSerializer.serialize(group));
            }

            String mainWiki = getWikiDescriptorManager().getMainWikiId();
            String currentWiki = getWikiDescriptorManager().getCurrentWikiId();

            if (!groups.isEmpty() || users.isEmpty()) {
                // If no user is given or if at least one group is given, we add all users
                // (respectively from the wiki or from the given group(s)).
                if (mainWiki.equals(currentWiki)) {
                    // We are in the main wiki.
                    addUsersFromWiki(users, mainWiki, groups);
                } else {
                    // We are in a subwiki.
                    UserScope userScope = wikiUserManager.getUserScope(currentWiki);
                    switch (userScope) {
                        case GLOBAL_ONLY:
                            addUsersFromWiki(users, mainWiki, groups);
                            break;
                        case LOCAL_ONLY:
                            addUsersFromWiki(users, currentWiki, groups);
                            break;
                        default:
                            addUsersFromWiki(users, mainWiki, groups);
                            addUsersFromWiki(users, currentWiki, groups);
                    }
                }
            }

            String html = htmlDisplayerManager.display(UserReferenceList.class, users, params, "view");
            return Arrays.asList(new RawBlock(html, Syntax.HTML_5_0));
        } catch (HTMLDisplayerException | QueryException | WikiUserManagerException e) {
            throw new MacroExecutionException("Failed to render the userProfile viewer template.", e);
        }
    }

    @Override
    public boolean supportsInlineMode()
    {
        return false;
    }
}
