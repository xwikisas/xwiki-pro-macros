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
package com.xwiki.macros.viewfile.internal.macro.async;

import javax.servlet.http.HttpSession;

import com.xpn.xwiki.web.WrappingXWikiRequest;
import com.xpn.xwiki.web.XWikiRequest;

/**
 * Mock request for the async renderer used to safely retrieve the {@link HttpSession}. Can be removed after the parent
 * version is >= 17.4.1.
 *  TODO: discuss if this is the best implementation to bypass the NullPointerException thrown in
 *  DefaultTemporaryAttachmentSessionsManager.
 *
 * @version $Id$
 * @since 1.29.0
 */
public class AsyncRequest extends WrappingXWikiRequest
{
    private final HttpSession session;

    /**
     * @param originalRequest the main thread request.
     * @param session the main thread session.
     */
    public AsyncRequest(XWikiRequest originalRequest, HttpSession session)
    {
        super(originalRequest);
        this.session = session;
    }

    @Override
    public HttpSession getSession()
    {
        return this.session;
    }

    @Override
    public HttpSession getSession(boolean create)
    {
        return this.session;
    }
}
