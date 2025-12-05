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
package com.xwiki.pro.test.ui;

import java.io.File;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.xwiki.administration.test.po.AdministrationPage;
import org.xwiki.flamingo.skin.test.po.AttachmentsPane;
import org.xwiki.flamingo.skin.test.po.AttachmentsViewPage;
import org.xwiki.model.reference.DocumentReference;
import org.xwiki.officeimporter.test.po.OfficeServerAdministrationSectionPage;
import org.xwiki.test.docker.junit5.ExtensionOverride;
import org.xwiki.test.docker.junit5.TestConfiguration;
import org.xwiki.test.docker.junit5.UITest;
import org.xwiki.test.docker.junit5.servletengine.ServletEngine;
import org.xwiki.test.ui.TestUtils;
import org.xwiki.test.ui.po.ViewPage;

import com.xwiki.pro.test.po.confluence.viewfile.ViewFileViewPage;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@UITest(office = true, servletEngine = ServletEngine.TOMCAT, servletEngineTag = "8", forbiddenEngines = {
    // These tests need to have XWiki running inside a Docker container (we chose Tomcat since it's the most
    // used one), because they need LibreOffice to be installed, and we cannot guarantee that it is installed on the
    // host machine.
    ServletEngine.JETTY_STANDALONE }, properties = {
    // Add the FileUploadPlugin which is needed by the test to upload attachment files
    "xwikiCfgPlugins=com.xpn.xwiki.plugin.fileupload.FileUploadPlugin" }, extensionOverrides = {
    @ExtensionOverride(extensionId = "com.google.code.findbugs:jsr305", overrides = {
        "features=com.google.code.findbugs:annotations" }),
    // Right id of the Bouncy Castle package. Build fails since the wrong dependency is resolved. Check after XWiki
    // parent upgrade if this is still needed.
    @ExtensionOverride(extensionId = "org.bouncycastle:bcprov-jdk18on", overrides = {
        "features=org.bouncycastle:bcprov-jdk15on" }),
    @ExtensionOverride(extensionId = "org.bouncycastle:bcpkix-jdk18on", overrides = {
        "features=org.bouncycastle:bcpkix-jdk15on" }),
    @ExtensionOverride(extensionId = "org.bouncycastle:bcmail-jdk18on", overrides = {
        "features=org.bouncycastle:bcmail-jdk15on" }) })
public class ViewFileIT
{
    @BeforeAll
    void setup(TestUtils setup)
    {
        setup.loginAsSuperAdmin();
    }

    @Test
    @Order(1)
    void invalidMacroCall(TestUtils setup)
    {
        // We don't actually need to have the attachment because we just test that the macro shows an error when no
        // file is provided.
        createPage(setup, "{{view-file/}}", "invalidCall");
        ViewFileViewPage viewFileViewPage = new ViewFileViewPage();
        assertEquals(viewFileViewPage.getErrorMessage(), "Please provide a file to show in the name parameter.");
    }

    @Test
    @Order(2)
    void testInlineCalls(TestUtils setup)
    {
        // We don't actually need to have the attachment because we just test that the macro is displayed when we use
        // it inline.
        createPage(setup, "{{view-file name=\"test.pdf\"/}} {{view-file att--filename=\"test.xls\"/}}", "inlineCalls");
        ViewFileViewPage viewFileViewPage = new ViewFileViewPage();
        assertEquals(2, viewFileViewPage.getInlineViewFilesCount());
    }

    @Test
    @Order(3)
    void testBlockCalls(TestUtils setup)
    {
        // We don't actually need to have the attachment because we just test that the macro is displayed when we use
        // it as a block.
        createPage(setup, "{{view-file name=\"test.pdf\"/}}", "blockCalls");
        ViewFileViewPage viewFileViewPage = new ViewFileViewPage();
        assertEquals(1, viewFileViewPage.getBlockViewFiles());
    }

    @Test
    @Order(4)
    void actualAttachmentWithGenericThumbnail(TestUtils setup, TestConfiguration testConfiguration)
    {
        createPage(setup, "{{view-file name=\"Test.ppt\"/}}", "actualAttachment");
        uploadFile("Test.ppt", testConfiguration);
        ViewFileViewPage viewFileViewPage = new ViewFileViewPage();
        // Is displayed
        assertEquals(1, viewFileViewPage.getBlockViewFiles());
        // Check that the mime type was identified.
        assertTrue(viewFileViewPage.isGenericThumbnail());
    }

    @Test
    @Order(5)
    void actualAttachmentWithGeneratedThumbnail(TestUtils setup, TestConfiguration testConfiguration)
    {
        // We first enable the office extension.
        enableOfficeServer();

        ViewPage currentPage = createPage(setup, "{{view-file name=\"Test.ppt\"/}}", "actualAttachmentPreview");
        uploadFile("Test.ppt", testConfiguration);
        currentPage.reloadPage();
        ViewFileViewPage viewFileViewPage = new ViewFileViewPage();
        // Is displayed
        assertEquals(1, viewFileViewPage.getBlockViewFiles());
        // Check that the mime type was identified.
        assertTrue(viewFileViewPage.isPreviewThumbnail());
    }

    @Test
    @Order(6)
    void testFullView(TestUtils setup, TestConfiguration testConfiguration)
    {
        ViewPage currentPage =
            createPage(setup, "{{view-file display=\"full\" name=\"TestPDF.pdf\"/}}", "fullViewPage");
        // We use a PDF file instead of an Office document for this test because only the newer LibreOffice versions
        // are available on the official stable download location. Newer LibreOffice builds cannot be executed in the
        // test Docker environment, as it is missing libraries. The PDF view uses the same view-file async full
        // display, so it can reliably test the feature. This can be replaced once XWiki parent version is >= 16.6.
        uploadFile("TestPDF.pdf", testConfiguration);
        currentPage.reloadPage();
        ViewFileViewPage viewFileViewPage = new ViewFileViewPage();
        assertTrue(viewFileViewPage.hasLoadedInFullViewMode());
    }

    private ViewPage createPage(TestUtils setup, String content, String pageName)
    {
        DocumentReference documentReference = new DocumentReference("xwiki", "viewfile", pageName);
        return setup.createPage(documentReference, content);
    }

    private void uploadFile(String attachmentName, TestConfiguration testConfiguration)
    {
        String attachmentPath = new File(new File(testConfiguration.getBrowser().getTestResourcesPath(), "viewfile"),
            attachmentName).getAbsolutePath();
        AttachmentsPane sourceAttachmentsPane = new AttachmentsViewPage().openAttachmentsDocExtraPane();
        sourceAttachmentsPane.setFileToUpload(attachmentPath);
        sourceAttachmentsPane.waitForUploadToFinish(attachmentName);
    }

    private void enableOfficeServer()
    {
        AdministrationPage administrationPage = AdministrationPage.gotoPage();
        administrationPage.clickSection("Content", "Office Server");
        OfficeServerAdministrationSectionPage officeServerAdministrationSectionPage =
            new OfficeServerAdministrationSectionPage();
        if (!"Connected".equals(officeServerAdministrationSectionPage.getServerState())) {
            officeServerAdministrationSectionPage.startServer();
        }
    }
}
