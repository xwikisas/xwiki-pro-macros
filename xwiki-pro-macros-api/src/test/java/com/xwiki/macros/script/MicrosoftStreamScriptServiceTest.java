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

import org.junit.jupiter.api.Test;
import org.xwiki.test.junit5.mockito.ComponentTest;
import org.xwiki.test.junit5.mockito.InjectMockComponents;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Unit tests for {@link MicrosoftStreamScriptService}.
 *
 * @version $Id$
 * @since 1.33.0
 */
@ComponentTest
public class MicrosoftStreamScriptServiceTest
{
    @InjectMockComponents
    private MicrosoftStreamScriptService service;

    @Test
    public void isTrustedURLShouldAcceptStreamClassic()
    {
        assertTrue(service.isTrustedURL("https://web.microsoftstream.com/"));
        assertTrue(service.isTrustedURL("https://web.microsoftstream.com/video/12345"));
        assertTrue(service.isTrustedURL("https://web.microsoftstream.com/video/12345?st=3600"));
    }

    @Test
    public void isTrustedURLShouldAcceptSharePoint()
    {
        assertTrue(service.isTrustedURL("https://mytenant.sharepoint.com/"));
        assertTrue(service.isTrustedURL("https://mytenant.sharepoint.com/:v:/g/xyz"));
        assertTrue(service.isTrustedURL("https://www.sharepoint.com/"));
    }

    @Test
    public void isTrustedURLShouldAcceptClipchamp()
    {
        assertTrue(service.isTrustedURL("https://app.clipchamp.com/"));
        assertTrue(service.isTrustedURL("https://app.clipchamp.com/videos/abc123"));
    }

    @Test
    public void isTrustedURLShouldRejectOtherDomains()
    {
        assertFalse(service.isTrustedURL("https://other.com/other"));
        assertFalse(service.isTrustedURL("https://www.stream.com/"));
        assertFalse(service.isTrustedURL("https://web.microsoftstream.com.other.com/"));
        assertFalse(service.isTrustedURL("https://othersharepoint.com/"));
        assertFalse(service.isTrustedURL("https://app.clipchamp.com.other.com/"));
    }

    @Test
    public void isTrustedURLShouldRejectMalformedInput()
    {
        assertFalse(service.isTrustedURL(null));
        assertFalse(service.isTrustedURL(""));
        assertFalse(service.isTrustedURL("not a url"));
        assertFalse(service.isTrustedURL("www.stream.com"));
    }
}