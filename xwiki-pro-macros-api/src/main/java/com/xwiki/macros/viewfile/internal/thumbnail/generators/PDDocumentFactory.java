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

import java.io.InputStream;
import java.lang.reflect.Method;

import org.apache.pdfbox.pdmodel.PDDocument;

/**
 * TODO This class should be deleted once we upgrade to a XWiki parent version >= 17.10.1, which includes
 * XWIKI-21258:Upgrade to Apache PDFBox 3.0.6. This class should be then replaced by {org.apache.pdfbox.Loader}
 * Handles the loading of the PDF file. We use reflection because the newer versions of the platform (17+) upgraded the
 * PDF box to 3+ and in this version the load is done through a loader instead of the PDDocument.
 *
 * @version $Id$
 * @since 1.30.1
 */
public final class PDDocumentFactory
{
    private PDDocumentFactory()
    {
    }

    /**
     * @param content content of the file
     * @return a PDF file.
     */
    public static PDDocument load(InputStream content) throws Exception
    {
        try {
            Class<?> loaderClass = Class.forName("org.apache.pdfbox.Loader");

            Class<?> rarClass = Class.forName("org.apache.pdfbox.io.RandomAccessReadBuffer");
            Object rarInstance = rarClass.getConstructor(InputStream.class).newInstance(content);

            Class<?> rarInterface = Class.forName("org.apache.pdfbox.io.RandomAccessRead");

            Method loadMethod = loaderClass.getMethod("loadPDF", rarInterface);

            return (PDDocument) loadMethod.invoke(null, rarInstance);
        } catch (ClassNotFoundException e) {
            // PDFBox 2.x fallback
            return PDDocument.load(content);
        }
    }
}
