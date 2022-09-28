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
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.xwiki.component.annotation.Component;
import org.xwiki.displayer.HTMLDisplayerException;
import org.xwiki.displayer.HTMLDisplayerManager;
import org.xwiki.model.reference.EntityReferenceSerializer;
import org.xwiki.rendering.block.Block;
import org.xwiki.rendering.block.RawBlock;
import org.xwiki.rendering.macro.AbstractMacro;
import org.xwiki.rendering.macro.MacroExecutionException;
import org.xwiki.rendering.syntax.Syntax;
import org.xwiki.rendering.transformation.MacroTransformationContext;
import org.xwiki.text.StringUtils;

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
public class UserListMacro extends AbstractMacro<UserListMacroParameters>
{
    @Inject
    private HTMLDisplayerManager htmlDisplayerManager;

    @Inject
    private EntityReferenceSerializer<String> referenceSerializer;

    /**
     * Create and initialize the descriptor of the macro.
     */
    public UserListMacro()
    {
        super("User list", "Displays a custom list of users with their avatar",
            UserListMacroParameters.class);
    }

    @Override
    public List<Block> execute(UserListMacroParameters parameters, String content, MacroTransformationContext context)
        throws MacroExecutionException
    {

        Map<String, String> params = new HashMap<>();
        try {
            List<String> userReferences = new ArrayList<>();
            if (parameters.getUsers() != null) {
                userReferences =
                    parameters.getUsers().stream().map(reference -> referenceSerializer.serialize(reference)).collect(
                        Collectors.toList());
            }
            params.put("properties", StringUtils.join(parameters.getProperties(), ','));
            String html =
                htmlDisplayerManager.display(UserReferenceList.class, StringUtils.join(userReferences, ','), params,
                    "view");
            return Arrays.asList(new RawBlock(html, Syntax.HTML_5_0));
        } catch (HTMLDisplayerException e) {
            throw new MacroExecutionException("Failed to render the userProfile viewer template.", e);
        }
    }

    @Override
    public boolean supportsInlineMode()
    {
        return false;
    }
}
