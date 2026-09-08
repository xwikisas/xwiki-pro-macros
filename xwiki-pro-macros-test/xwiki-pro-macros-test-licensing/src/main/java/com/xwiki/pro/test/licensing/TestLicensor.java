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
package com.xwiki.pro.test.licensing;

import javax.inject.Singleton;

import org.xwiki.component.annotation.Component;
import org.xwiki.extension.ExtensionId;
import org.xwiki.model.reference.EntityReference;

import com.xwiki.licensing.License;
import com.xwiki.licensing.Licensor;

/**
 * A {@link Licensor} implementation for the functional tests that considers every extension and every entity to be
 * covered by a valid, non-expiring license.
 * <p>
 * It is packaged in this module's JAR, deployed into {@code WEB-INF/lib} of the test XWiki, and registered with a
 * higher priority than {@code DefaultLicensor}, so that the Pro Macros render without a real (or trial) license
 * being installed.
 *
 * @version $Id$
 * @since 1.32.3
 */
@Component
@Singleton
public class TestLicensor implements Licensor
{
    private final License license = createLicense();

    @Override
    public License getLicense()
    {
        return this.license;
    }

    @Override
    public License getLicense(ExtensionId extensionId)
    {
        return this.license;
    }

    @Override
    public License getLicense(EntityReference reference)
    {
        return this.license;
    }

    @Override
    public boolean hasLicensure()
    {
        return true;
    }

    @Override
    public boolean hasLicensure(EntityReference reference)
    {
        return true;
    }

    @Override
    public boolean hasLicensure(ExtensionId extensionId)
    {
        return true;
    }

    @Override
    public boolean isLicenseExpiring(ExtensionId extensionId)
    {
        return false;
    }

    private static License createLicense()
    {
        License license = new License();
        license.setExpirationDate(Long.MAX_VALUE);
        license.setMaxUserCount(Long.MAX_VALUE);
        return license;
    }
}
