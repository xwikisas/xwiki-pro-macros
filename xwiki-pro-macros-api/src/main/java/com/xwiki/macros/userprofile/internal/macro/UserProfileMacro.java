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
package com.xwiki.macros.userprofile.internal.macro;

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
import org.xwiki.model.reference.EntityReferenceSerializer;
import org.xwiki.rendering.block.Block;
import org.xwiki.rendering.block.RawBlock;
import org.xwiki.rendering.macro.AbstractMacro;
import org.xwiki.rendering.macro.MacroExecutionException;
import org.xwiki.rendering.syntax.Syntax;
import org.xwiki.rendering.transformation.MacroTransformationContext;
import org.xwiki.text.StringUtils;

import com.xwiki.macros.userprofile.macro.UserProfileMacroParameters;
import com.xwiki.macros.userprofile.macro.UserReference;

/**
 * This macro displays a user profile.
 *
 * @version $Id$
 */
@Component
@Named("userProfile")
@Singleton
public class UserProfileMacro extends AbstractMacro<UserProfileMacroParameters>
{
    @Inject
    private HTMLDisplayerManager htmlDisplayerManager;

    @Inject
    @Named("default")
    private EntityReferenceSerializer<String> referenceSerializer;

    /**
     * Create and initialize the descriptor of the macro.
     */
    public UserProfileMacro()
    {
        super("User profile", "Displays a user profile with custom set of properties",
            UserProfileMacroParameters.class);
    }

    @Override
    public List<Block> execute(UserProfileMacroParameters parameters, String content,
        MacroTransformationContext context) throws MacroExecutionException
    {

        Map<String, String> params = new HashMap<>();
        try {
            String userReference = referenceSerializer.serialize(parameters.getReference());
            params.put("reference", userReference);
            params.put("properties", StringUtils.join(parameters.getProperties(), ','));
            String html = htmlDisplayerManager.display(UserReference.class, content, params, "view");
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
