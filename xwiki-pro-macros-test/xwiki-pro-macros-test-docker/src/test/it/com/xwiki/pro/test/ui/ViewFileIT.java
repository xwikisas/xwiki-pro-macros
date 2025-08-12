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

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.xwiki.model.reference.DocumentReference;
import org.xwiki.test.docker.junit5.ExtensionOverride;
import org.xwiki.test.docker.junit5.UITest;
import org.xwiki.test.ui.TestUtils;

import com.xwiki.pro.test.po.confluence.viewfile.ViewFileViewPage;

import static org.junit.jupiter.api.Assertions.assertEquals;

@UITest(properties = { "xwikiCfgPlugins=com.xpn.xwiki.plugin" + ".tag.TagPlugin", }, extensionOverrides = {
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

    void createPage(TestUtils setup, String content, String pageName)
    {
        DocumentReference documentReference = new DocumentReference("xwiki", "viewfile", pageName);
        setup.createPage(documentReference, content);
    }

    @Test
    void invalidMacroCall(TestUtils setup)
    {
        createPage(setup, "{{view-file/}}", "invalidCall");
        ViewFileViewPage viewFileViewPage = new ViewFileViewPage();
        assertEquals(viewFileViewPage.getErrorMessage(), "Please provide a file to show in the name parameter.");
    }

    @Test
    void testInlineCalls(TestUtils setup){
        createPage(setup, "{{view-file name=\"test.pdf\"/}} {{view-file name=\"test.xls\"/}}", "inlineCalls");
        ViewFileViewPage viewFileViewPage = new ViewFileViewPage();
        assertEquals(viewFileViewPage.getInlineViewFilesCount(), 2);
    }

    @Test
    void testBlockCalls(TestUtils setup){
        createPage(setup, "{{view-file name=\"test.pdf\"/}}", "blockCalls");
        ViewFileViewPage viewFileViewPage = new ViewFileViewPage();
        assertEquals(viewFileViewPage.getBlockViewFiles(), 1);
    }
}
