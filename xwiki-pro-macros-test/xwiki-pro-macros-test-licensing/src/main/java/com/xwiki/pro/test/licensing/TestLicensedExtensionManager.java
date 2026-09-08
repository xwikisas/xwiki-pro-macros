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

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.inject.Singleton;

import org.xwiki.component.annotation.Component;
import org.xwiki.extension.ExtensionId;
import org.xwiki.extension.InstalledExtension;

import com.xwiki.licensing.LicensedExtensionManager;
import com.xwiki.licensing.LicensedFeatureId;

/**
 * Registered with a higher priority than {@code DefaultLicensedExtensionManager} through
 * {@code META-INF/components.txt} to avoid requiring a real license in the tests.
 *
 * @version $Id$
 * @since 1.32.3
 */
@Component
@Singleton
public class TestLicensedExtensionManager implements LicensedExtensionManager
{
    private static final String TEST_VERSION = "999.0";

    @Override
    public Collection<ExtensionId> getLicensedExtensions()
    {
        return Collections.emptyList();
    }

    @Override
    public Collection<ExtensionId> getLicensedExtensions(String feature)
    {
        return List.of(new ExtensionId(feature, TEST_VERSION));
    }

    @Override
    public Collection<ExtensionId> getLicensedExtensions(LicensedFeatureId licensedFeatureId)
    {
        return List.of(new ExtensionId(licensedFeatureId.getId(), TEST_VERSION));
    }

    @Override
    public Collection<ExtensionId> getMandatoryLicensedExtensions()
    {
        return Collections.emptyList();
    }

    @Override
    public void invalidateMandatoryLicensedExtensionsCache()
    {
        // Nothing is cached.
    }

    @Override
    public Set<ExtensionId> getLicensedDependencies(InstalledExtension installedExtension, String namespace)
    {
        return Collections.emptySet();
    }

    @Override
    public Map<String, Set<com.xwiki.licensing.internal.LicensedDependenciesMap.LicensedExtensionParent>>
        getLicensedDependenciesMap()
    {
        return Collections.emptyMap();
    }

    @Override
    public void invalidateLicensedDependenciesMap()
    {
        // Nothing is cached.
    }
}
