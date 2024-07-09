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
package com.xwiki.macros.script;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.xwiki.component.annotation.Component;
import org.xwiki.extension.ExtensionId;
import org.xwiki.model.reference.EntityReference;
import org.xwiki.script.service.ScriptService;
import org.xwiki.stability.Unstable;

import com.xwiki.licensing.Licensor;

/**
 * Check licensing for macros in the pro-macro package.
 * This is a workaround for https://github.com/xwikisas/xwiki-pro-macros/issues/286, will possibly be removed when the
 * real fix is done.
 * @since 1.19.1
 * @version $Id$
 */
@Component
@Named("promacrolicensing")
@Singleton
@Unstable
public class ProMacroLicensingScriptService implements ScriptService
{
    private static final ExtensionId PRO_MACROS_EXT_ID = new ExtensionId("com.xwiki.pro:xwiki-pro-macros");

    @Inject
    private Licensor licensor;

    /**
     * @return whether a valid license matches the given document.
     * @param docRef the document to check
     */
    public boolean hasLicensureForEntity(EntityReference docRef)
    {
        return licensor.hasLicensure(docRef) || licensor.hasLicensure(PRO_MACROS_EXT_ID);
    }
}
