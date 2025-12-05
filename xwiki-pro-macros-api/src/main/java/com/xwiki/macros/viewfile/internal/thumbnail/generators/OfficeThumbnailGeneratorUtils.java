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
package com.xwiki.macros.viewfile.internal.thumbnail.generators;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.jodconverter.core.document.DefaultDocumentFormatRegistry;
import org.jodconverter.core.office.OfficeException;
import org.jodconverter.core.office.OfficeManager;
import org.jodconverter.local.LocalConverter;
import org.jodconverter.local.office.ExternalOfficeManager;
import org.xwiki.component.annotation.Component;
import org.xwiki.officeimporter.server.OfficeServer;
import org.xwiki.officeimporter.server.OfficeServerConfiguration;
import org.xwiki.stability.Unstable;

/**
 * Office thumbnail generator utility methods.
 *
 * @version $Id$
 * @since 1.29.0
 */
@Component(roles = OfficeThumbnailGeneratorUtils.class)
@Singleton
@Unstable
public class OfficeThumbnailGeneratorUtils
{
    /**
     * The office server configuration.
     */
    @Inject
    private OfficeServerConfiguration officeServerConfig;

    @Inject
    private OfficeServer officeServer;

    private OfficeManager officeManager;

    /**
     * Check if the office server is connected to the instance.
     *
     * @return {@code true} if the office server is connected, or {@code false} otherwise.
     */
    public boolean isOfficeServerConnected()
    {
        this.officeServer.refreshState();
        return this.officeServer.getState() == OfficeServer.ServerState.CONNECTED;
    }

    /**
     * Convert a given {@link InputStream} to a JPEG file, using LibreOffice.
     *
     * @param is content that needs to be converted
     * @return the result of the conversion to JPEG
     * @throws OfficeException if any error occurs during the conversion, or if the server is not connected.
     */
    public byte[] getImageBytes(InputStream is) throws OfficeException
    {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        OfficeManager manager = getOfficeManager();
        LocalConverter.make(manager).convert(is).to(baos).as(DefaultDocumentFormatRegistry.JPEG).execute();
        return baos.toByteArray();
    }

    private void initialize() throws OfficeException
    {
        if (isOfficeServerConnected()) {
            // Set an execution timeout equivalent to 10 seconds.
            officeManager = ExternalOfficeManager.builder().portNumbers(officeServerConfig.getServerPorts())
                .taskExecutionTimeout(10000L).build();
            try {
                officeManager.start();
            } catch (OfficeException e) {
                throw new RuntimeException(e);
            }
        } else {
            throw new OfficeException("Office server is not connected.");
        }
    }

    private OfficeManager getOfficeManager() throws OfficeException
    {
        if (officeManager == null || !officeManager.isRunning()) {
            initialize();
        }
        return officeManager;
    }
}
