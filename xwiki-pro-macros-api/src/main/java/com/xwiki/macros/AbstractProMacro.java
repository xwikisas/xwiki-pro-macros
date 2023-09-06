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
package com.xwiki.macros;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import javax.inject.Inject;

import org.xwiki.bridge.DocumentAccessBridge;
import org.xwiki.localization.ContextualLocalizationManager;
import org.xwiki.model.reference.DocumentReference;
import org.xwiki.model.reference.LocalDocumentReference;
import org.xwiki.model.reference.WikiReference;
import org.xwiki.rendering.block.Block;
import org.xwiki.rendering.block.MacroBlock;
import org.xwiki.rendering.macro.AbstractMacro;
import org.xwiki.rendering.macro.MacroExecutionException;
import org.xwiki.rendering.macro.descriptor.ContentDescriptor;
import org.xwiki.rendering.transformation.MacroTransformationContext;
import org.xwiki.stability.Unstable;
import org.xwiki.wiki.descriptor.WikiDescriptorManager;

import com.xwiki.licensing.Licensor;

/**
 * Helper to implement a Pro macro.
 *
 * @param <P> the type of the macro parameters bean.
 * @version $Id$
 * @since 1.9.1
 */
@Unstable
public abstract class AbstractProMacro<P> extends AbstractMacro<P>
{
    private static final LocalDocumentReference APP_WEBHOME = new LocalDocumentReference(Arrays.asList("Confluence",
        "Macros"), "WebHome");

    private static final LocalDocumentReference XWIKI_PREFERENCES = new LocalDocumentReference("XWiki",
        "XWikiPreferences");

    @Inject
    private ContextualLocalizationManager localizationManager;

    @Inject
    private Licensor licensor;

    @Inject
    private DocumentAccessBridge accessBridge;

    @Inject
    private WikiDescriptorManager wikiDescriptorManager;

    /**
     * Creates a new macro instance.
     *
     * @param name the name of the macro.
     * @param description the description of the macro.
     * @param contentDescriptor the content descriptor of the macro.
     * @param parametersBeanClass the class of the parameters bean of this class.
     */
    public AbstractProMacro(String name, String description,
        ContentDescriptor contentDescriptor, Class<?> parametersBeanClass)
    {
        super(name, description, contentDescriptor, parametersBeanClass);
    }

    /**
     * Creates a new macro instance.
     *
     * @param name the name of the macro.
     * @param description the description of the macro.
     * @param parametersBeanClass the class of the parameters bean of this class.
     */
    public AbstractProMacro(String name, String description, Class<?> parametersBeanClass)
    {
        super(name, description, parametersBeanClass);
    }

    /**
     * Executes the macro.
     *
     * @param parameters the macro parameters in the form of a bean defined by the
     *     {@link org.xwiki.rendering.macro.Macro} implementation
     * @param content the content of the macro
     * @param context the context of the macros transformation process
     * @return the result of the macro execution as a list of Block elements
     * @throws MacroExecutionException when an error occurs during the execution process
     */
    @Override
    public List<Block> execute(P parameters, String content, MacroTransformationContext context)
        throws MacroExecutionException
    {
        if (licensor.hasLicensure(
            new DocumentReference(APP_WEBHOME, new WikiReference(wikiDescriptorManager.getCurrentWikiId()))))
        {
            return internalExecute(parameters, content, context);
        } else {
            String extensionName = localizationManager.getTranslationPlain("proMacros.extension.name");
            String licenseAdminURL = accessBridge.getDocumentURL(
                new DocumentReference(XWIKI_PREFERENCES, new WikiReference(wikiDescriptorManager.getMainWikiId())),
                "admin", "editor=globaladmin&section=Licenses", "");
            return Collections.singletonList(new MacroBlock(
                "error",
                Collections.emptyMap(),
                localizationManager.getTranslationPlain("licensor.missingLicense", extensionName, "[[",
                    ">>path:" + licenseAdminURL + "]]"),
                context.isInline())
            );
        }
    }

    protected abstract List<Block> internalExecute(P parameters, String content, MacroTransformationContext context)
        throws MacroExecutionException;

    /**
     * @return the wiki descriptor manager
     */
    public WikiDescriptorManager getWikiDescriptorManager()
    {
        return this.wikiDescriptorManager;
    }
}
