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
package com.xwiki.macros.viewfile.internal.macro;

import java.util.Collections;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.xwiki.component.annotation.Component;
import org.xwiki.skinx.SkinExtension;

/**
 * Utility class for handling resources for the {@link ViewFileMacro}.
 *
 * @version $Id$
 * @since 1.27
 */
@Component(roles = ViewFileResourceManager.class)
@Singleton
public class ViewFileResourceManager
{
    private static final String CONFLUENCE_MACROS_VIEW_FILE = "Confluence.Macros.ViewFile";

    @Inject
    @Named("jsx")
    private SkinExtension jsx;

    @Inject
    @Named("ssx")
    private SkinExtension ssx;

    @Inject
    @Named("jsfx")
    private SkinExtension jsfx;

    @Inject
    @Named("ssfx")
    private SkinExtension ssfx;

    /**
     * Inject base styles and JavaScript needed by the ViewFile macro.
     */
    public void injectBaseResources()
    {
        jsx.use(CONFLUENCE_MACROS_VIEW_FILE);
        ssx.use(CONFLUENCE_MACROS_VIEW_FILE);
    }

    /**
     * Inject resources needed for presentation files.
     */
    public void injectPresentationResources()
    {
        jsfx.use("uicomponents/widgets/gallery/gallery.js", Collections.singletonMap("forceSkinAction", true));
        ssfx.use("uicomponents/widgets/gallery/gallery.css");
    }
}
