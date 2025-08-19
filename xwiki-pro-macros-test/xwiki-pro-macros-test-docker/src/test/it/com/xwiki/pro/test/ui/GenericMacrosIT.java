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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.xwiki.model.reference.DocumentReference;
import org.xwiki.tag.test.po.AddTagsPane;
import org.xwiki.tag.test.po.TaggablePage;
import org.xwiki.test.docker.junit5.ExtensionOverride;
import org.xwiki.test.docker.junit5.TestReference;
import org.xwiki.test.docker.junit5.UITest;
import org.xwiki.test.ui.TestUtils;

import com.xwiki.pro.test.po.generic.ContentReportTableMacro;
import com.xwiki.pro.test.po.generic.ContentReportTableMacroPage;
import com.xwiki.pro.test.po.generic.ExcerptIncludeMacro;
import com.xwiki.pro.test.po.generic.ExcerptIncludeMacroPage;
import com.xwiki.pro.test.po.generic.ExpandMacro;
import com.xwiki.pro.test.po.generic.ExpandMacroPage;
import com.xwiki.pro.test.po.generic.HideIfMacroPage;
import com.xwiki.pro.test.po.generic.ProfilePictureMacro;
import com.xwiki.pro.test.po.generic.ProfilePictureMacroPage;
import com.xwiki.pro.test.po.generic.RegisterMacro;
import com.xwiki.pro.test.po.generic.ShowIfMacroPage;
import com.xwiki.pro.test.po.generic.TabGroupMacro;
import com.xwiki.pro.test.po.generic.TabMacro;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * UI tests for the generic Pro Macros.
 *
 * @version $Id$
 * @since 1.25.2
 */
@UITest(
    properties = { "xwikiCfgPlugins=com.xpn.xwiki.plugin.tag.TagPlugin, "
            + "com.xpn.xwiki.plugin.skinx.JsResourceSkinExtensionPlugin, "
            + "com.xpn.xwiki.plugin.skinx.CssResourceSkinExtensionPlugin"
    },
    extensionOverrides = {
        @ExtensionOverride(
            extensionId = "com.google.code.findbugs:jsr305",
            overrides = {
                "features=com.google.code.findbugs:annotations"
            }
        ),
        // Right id of the Bouncy Castle package. Build fails since the wrong dependency is resolved. Check after XWiki
        // parent upgrade if this is still needed.
        @ExtensionOverride(
            extensionId = "org.bouncycastle:bcprov-jdk18on",
            overrides = {
                "features=org.bouncycastle:bcprov-jdk15on"
            }
        ),
        @ExtensionOverride(
            extensionId = "org.bouncycastle:bcpkix-jdk18on",
            overrides = {
                "features=org.bouncycastle:bcpkix-jdk15on"
            }
        ),
        @ExtensionOverride(
            extensionId = "org.bouncycastle:bcmail-jdk18on",
            overrides = {
                "features=org.bouncycastle:bcmail-jdk15on"
            }
        )
    })
public class GenericMacrosIT
{
    private final DocumentReference pageWithTeamMacros = new DocumentReference("xwiki", "Main", "TeamTest");


    private static final String PAGE_WITH_TEAM_MACROS_CONTENT =
        "{{team/}}\n" + "\n" + "{{team tag=\"testTag\" /}}\n" + "\n" + "{{team tag=\"nonExistentTag\" /}}";

    private static final List<String> BASE_XWIKI_MACRO_SPACE = List.of("XWiki", "Macros");

    private static final List<String> CONF_XWIKI_MACRO_SPACE = List.of("Confluence", "Macros");

    private void registerMacros()
    {
        RegisterMacro register = new RegisterMacro();
        register.registerMacro(BASE_XWIKI_MACRO_SPACE, "Button");
        register.registerMacro(BASE_XWIKI_MACRO_SPACE, "MicrosoftStream");
        register.registerMacro(BASE_XWIKI_MACRO_SPACE, "Panel");
        register.registerMacro(BASE_XWIKI_MACRO_SPACE, "Team");
        register.registerMacro(BASE_XWIKI_MACRO_SPACE, "Taglist");
        register.registerMacro(CONF_XWIKI_MACRO_SPACE, "RecentlyUpdated");
        register.registerMacro(CONF_XWIKI_MACRO_SPACE, "ContentReportTableMacro");
    }

    @BeforeAll
    void setup(TestUtils setup)
    {
        setup.loginAsSuperAdmin();
        setup.createUser("UserTest", "UserTest", "", "company", "xwiki", "phone", "07777777", "email",
            "usertest@example.com", "address", "userTestAddress", "comment", "test", "blog", "https://example.com/",
            "blogfeed", "https://example.com/");
        setup.createUser("UserTest2", "UserTest", "", "company", "xwiki", "phone", "07777777", "email",
            "usertest2@example.com", "address", "userTestAddress2", "comment", "test2");
        setup.createUser("UserTest3", "UserTest", "", "company", "xwiki", "phone", "07777777", "email",
            "usertest3@example.com", "address", "userTestAddress3", "comment", "test3", "blog",
            "https://example" + ".com/", "blogfeed", "https://example.com/");

        setup.setGlobalRights("XWiki.XWikiAllGroup", "", "comment", true);
        setup.setGlobalRights("XWiki.XWikiAllGroup", "", "edit", true);
        registerMacros();
    }

    public String createContent(String filename)
    {
        try (InputStream inputStream = getClass().getResourceAsStream("/macros/" + filename)) {
            if (inputStream == null) {
                throw new RuntimeException("Failed to load " + filename + " from resources.");
            }

            return new BufferedReader(new InputStreamReader(inputStream)).lines()
                .filter(line -> !line.trim().startsWith("##")).collect(Collectors.joining("\n"));
        } catch (IOException e) {
            throw new RuntimeException("Failed to read macro file: " + filename, e);
        }
    }

    private void createPagesWithTags(TestUtils setup)
    {
        final DocumentReference pageWithTags = new DocumentReference("xwiki", "Main", "pageWithTags");

        final DocumentReference pageWithTags2 = new DocumentReference("xwiki", "XWiki", "pageWithTags2");
        setup.createPage(pageWithTags, "Test content for tagging");
        setup.gotoPage(pageWithTags);
        TaggablePage taggablePage = new TaggablePage();
        AddTagsPane tagsPane = taggablePage.addTags();
        tagsPane.setTags("alpha, beta, gamma");
        tagsPane.add();

        setup.createPage(pageWithTags2, "Test content for tagging");
        setup.gotoPage(pageWithTags2);
        TaggablePage taggablePage2 = new TaggablePage();
        AddTagsPane tagsPane2 = taggablePage2.addTags();
        tagsPane2.setTags("z, x, y");
        tagsPane2.add();
    }



    /*@Test
    @Order(1)
    void teamMacroTest(TestUtils setup)
    {
        setup.gotoPage("XWiki", "UserTest");
        TaggablePage taggablePage = new TaggablePage();
        AddTagsPane tagsPane = taggablePage.addTags();
        tagsPane.setTags("testTag");
        tagsPane.add();

        setup.createPage(pageWithTeamMacros, PAGE_WITH_TEAM_MACROS_CONTENT);

        TeamMacroPage page = new TeamMacroPage();

        // There should be 3 team macros.
        assertEquals(3, page.getTeamMacrosCount());
        // First team macro should display 2 users (admin and the created user).
        assertEquals(3, page.getTeamMacroUsers(0).size());
        // Second team macro should display 1 user - the one with "testTag".
        assertEquals(1, page.getTeamMacroUsers(1).size());
        // Third team macro should display 0 users - none exist with tag "nonExistentTag".
        assertEquals(0, page.getTeamMacroUsers(2).size());
    }*/

    @Test
    @Order(1)
    void expandMacroTest(TestUtils setup, TestReference testReference)
    {
        setup.createPage(testReference, createContent("expand-macros.vm"), "ExpandTest");
        ExpandMacroPage page = new ExpandMacroPage();

        ExpandMacro expand1 = page.getMacro(0);
        ExpandMacro expand2 = page.getMacro(1);

        assertEquals(2, page.getMacroCount());

        // Nested Expand macros.
        // Checks the 1st Expand macro, with expanded = "true".
        assertEquals("ExpandTest1", expand1.getTitle());
        // Checking the icon.
        assertTrue(expand1.hasIcon());

        // Expanded = true.
        List<String> expectedContent = Arrays.asList("test0\ntest1", "test2");
        assertTrue(expand1.isExpanded());
        assertEquals(expectedContent, expand1.getTextContent());
        assertTrue(expand1.containsImage("example.jpg"));

        // Click -> closed macro, content not visible.
        expand1.toggle();
        assertFalse(expand1.isExpanded());
        assertNotEquals(expectedContent, expand1.getTextContent());
        assertTrue(expand1.containsImage("example.jpg"));

        // Click again -> open macro, content visible.
        expand1.toggle();
        assertTrue(expand1.isExpanded());
        assertEquals(expectedContent, expand1.getTextContent());
        assertTrue(expand1.containsImage("example.jpg"));

        // Checks the 2nd Expand macro, without expanded = "true".
        assertEquals("ExpandTest2", expand2.getTitle());
        assertTrue(expand2.hasIcon());

        List<String> expectedContent2 = Arrays.asList("test0\ntest1");
        // Closed macro, content not visible.
        assertFalse(expand2.isExpanded());
        assertNotEquals(expectedContent2, expand2.getTextContent());

        // Click -> opened macro, content visible.
        expand2.toggle();
        assertTrue(expand2.isExpanded());
        assertEquals(expectedContent2, expand2.getTextContent());

        // Click again -> closed macro, content not visible.
        expand2.toggle();
        assertFalse(expand2.isExpanded());
        assertNotEquals(expectedContent2, expand2.getTextContent());
    }

    void createUserWithPicture(TestUtils setup)
    {

        setup.loginAsSuperAdmin();
        setup.createUser("UserTest4", "UserTest", "", "avatar", "image1.png");
        try {
            setup.attachFile("XWiki", "UserTest4", "image1.png", getClass().getResourceAsStream("/macros/image1.png"),
                false);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    @Order(2)
    void profilePictureMacroTest(TestUtils setup, TestReference testReference)
    {
        createUserWithPicture(setup);
        setup.createPage(testReference, createContent("profilePicture-macros.vm"), "ProfilePictureTest");
        ProfilePictureMacroPage page = new ProfilePictureMacroPage();

        assertEquals(2, page.getMacroCount());

        ProfilePictureMacro picture1 = page.getMacro(0);
        ProfilePictureMacro picture2 = page.getMacro(1);

        // Checks the 1st profilePicture macro, with an actual profile image.
        assertEquals("UserTest4", picture1.getUserTitle());
        assertTrue(picture1.linkContainsUsername("UserTest4"));
        assertEquals("60px", picture1.getAvatarSize());
        assertTrue(picture1.hasProfileImage());
        assertFalse(picture1.hasAvatarInitials());

        // Checks the 2nd profilePicture macro, with personalized size.
        assertEquals("UserTest2", picture2.getUserTitle());
        assertTrue(picture2.linkContainsUsername("UserTest2"));
        assertEquals("100px", picture2.getAvatarSize());
        assertFalse(picture2.hasProfileImage());
        assertTrue(picture2.hasAvatarInitials());
    }

    @Test
    @Order(3)
    void showIfMacroTest(TestUtils setup, TestReference testReference)
    {
        setup.createPage(testReference, createContent("showIf-macros.vm"), "ShowIfTest");
        ShowIfMacroPage page = new ShowIfMacroPage();

        assertTrue(page.containsParagraph("Content for testing the show-If macro"));
        assertTrue(page.containsParagraph("Content for testing the show-If macro -2"));
        assertFalse(page.containsParagraph("Content for testing the show-If macro -3"));
        assertFalse(page.containsParagraph("Content for testing the show-If macro -4"));
    }

    @Test
    @Order(4)
    void hideIfMacroTest(TestUtils setup, TestReference testReference)
    {

        setup.createPage(testReference, createContent("hideIf-macros.vm"), "HideIfTest");
        HideIfMacroPage page = new HideIfMacroPage();

        assertTrue(page.containsParagraph("content1"));
        assertFalse(page.containsParagraph("content2"));
        assertTrue(page.containsParagraph("content3"));
        assertTrue(page.containsParagraph("content4"));
    }

    @Test
    @Order(5)
    void tabMacroTest(TestUtils setup, TestReference testReference)
    {
        setup.createPage(testReference, createContent("tab-macros.vm"), "TabTest");

        TabMacro tab0 = new TabMacro("tab_0");
        TabMacro tab1 = new TabMacro("tab_1");
        TabMacro tab2 = new TabMacro("tab_2");

        // Checks the tab_0 macro, with an added cssClass, transition duration, nextAfter and default="true".
        assertTrue(tab0.getCssClass().contains("tabCssClass"));
        assertTrue(tab0.getCssStyle().contains("transition-duration: 2s"));
        assertEquals("2", tab0.getNextAfter());
        assertTrue(tab0.getTextContent().contains("tab0-content"));
        assertTrue(tab0.isActive());
        assertTrue(tab0.isDisplayed());

        // Checks the tab_1 macro, with an added CSS values.
        assertFalse(tab1.isActive());
        assertTrue(tab1.getCssStyle().contains("background-color: yellow"));
        assertTrue(tab1.getCssStyle().contains("color: red"));
        assertTrue(tab1.getCssStyle().contains("border: 1px dashed " + hexToRgb("#ccc")));
        assertEquals("0", tab1.getNextAfter());
        assertTrue(tab1.getTextContent().contains("tab1-content"));
        assertTrue(tab1.isDisplayed());

        // Checks the tab_2 macro, with and added FADE effect nextAfter="3".
        assertTrue(tab2.getCssClass().contains("fade"));
        assertEquals("3", tab2.getNextAfter());
        assertFalse(tab2.isDisplayed());
    }

    @Test
    @Order(6)
    void tabGroupMacroTest(TestUtils setup, TestReference testReference)
    {
        setup.createPage(testReference, createContent("tabGroup-macros.vm"), "TabGroupTest");

        TabGroupMacro tabGroup0 = new TabGroupMacro("tabGroup_0");
        TabGroupMacro tabGroup1 = new TabGroupMacro("tabGroup_1");
        TabGroupMacro tabGroup2 = new TabGroupMacro("tabGroup_2");
        TabGroupMacro tabGroup3 = new TabGroupMacro("tabGroup_3");

        List<String> expectedLabels1 = Arrays.asList("tab0", "tab1", "tab2");
        List<String> expectedLabels2 = Arrays.asList("tab0", "tab1");

        // Checks the tabGroup_0 macro, with 3 tabs, tabLocation="TOP", fade effect, CSS class.
        assertEquals(3, tabGroup0.getTabCount());
        assertEquals(expectedLabels1, tabGroup0.getTabLabels());
        assertEquals(Arrays.asList("tab_0", "tab_1", "tab_2"), tabGroup0.getTabIds());

        assertEquals("TOP", new TabGroupMacro("tabGroup_0").getTabLocation());
        assertTrue(tabGroup0.isTabListFirst());
        assertTrue(tabGroup0.hasFadeEffect());

        assertTrue(tabGroup0.hasCssClass("CssTabGroup"));

        assertEquals(3, tabGroup0.getNextAfter());
        assertFalse(tabGroup0.isLoopEnabled());

        assertTrue(tabGroup0.isTabContentDisplayed("tab_0", "tab0-content"));

        // Checks the tabGroup_1 macro, with 3 tabs, tabLocation="BOTTOM", fade effect.
        assertEquals(3, tabGroup1.getTabCount());
        assertEquals(expectedLabels1, tabGroup1.getTabLabels());
        assertEquals(Arrays.asList("tab_3", "tab_4", "tab_5"), tabGroup1.getTabIds());

        assertEquals("BOTTOM", new TabGroupMacro("tabGroup_1").getTabLocation());
        assertFalse(tabGroup1.isTabListFirst());
        assertTrue(tabGroup1.hasFadeEffect());

        assertEquals(5, tabGroup1.getNextAfter());
        assertTrue(tabGroup1.isLoopEnabled());

        // Checks the tabGroup_2 macro, with 2 tabs, tabLocation="RIGHT".
        assertEquals(2, tabGroup2.getTabCount());
        assertEquals(expectedLabels2, tabGroup2.getTabLabels());
        assertEquals(Arrays.asList("tab_6", "tab_7"), tabGroup2.getTabIds());

        assertEquals("RIGHT", new TabGroupMacro("tabGroup_2").getTabLocation());
        assertFalse(tabGroup2.hasFadeEffect());

        assertEquals(1, tabGroup2.getNextAfter());
        assertTrue(tabGroup2.isLoopEnabled());

        // Checks the tabGroup_3 macro, with 2 tabs, tabLocation="LEFT".
        assertEquals(2, tabGroup3.getTabCount());
        assertEquals(expectedLabels2, tabGroup3.getTabLabels());
        assertEquals(Arrays.asList("tab_8", "tab_9"), tabGroup3.getTabIds());

        assertEquals("LEFT", new TabGroupMacro("tabGroup_3").getTabLocation());
        assertFalse(tabGroup3.hasFadeEffect());

        assertEquals(0, tabGroup3.getNextAfter());
        assertFalse(tabGroup3.isLoopEnabled());

        // Checks the active tab.
        assertTrue(tabGroup3.isTabContentDisplayed("tab_9", "tab1-content"));
        tabGroup3.clickTab("tab_8");
        assertTrue(tabGroup3.isTabContentDisplayed("tab_8", "tab0-content"));
    }

    @Test
    @Order(7)
    void excerptIncludeMacroTest(TestUtils setup, TestReference testReference)
    {
        createExcerptPage(setup);
        setup.createPage(testReference, createContent("excerptInclude-macros.vm"), "ExcerptIncludeTest");

        ExcerptIncludeMacroPage page = new ExcerptIncludeMacroPage();

        ExcerptIncludeMacro excerpt0 = page.getMacro(0);
        ExcerptIncludeMacro excerpt1 = page.getMacro(1);
        ExcerptIncludeMacro excerpt2 = page.getMacro(2);

        // Checks the 1st ExcerptInclude macro, with the Excerpt macro defined on the same page.
        assertEquals("ExcerptIncludeTest", excerpt0.getTitle());
        assertEquals("Content for excerpt macro -1", excerpt0.getContentText());

        // Checks the 2nd ExcerptInclude macro, with the Excerpt macro containing also a table.
        assertEquals("xwiki:Main.Excerpt", excerpt1.getTitle());
        assertEquals("Content for excerpt macro", excerpt1.getContentText());
        assertTrue(excerpt1.containsTable());

        // Checks the 3rd ExcerptInclude macro, with nested Excerpt macros and with inline mode.
        assertEquals("xwiki:Main.Excerpt", excerpt2.getTitle());
        assertEquals("Content for Excerpt macro -2\nContent for Excerpt macro -3", excerpt2.getContentText());

        // Checks an ExcerptInclude macro, without a panel and with inline mode.
        assertTrue(page.containsText("Content for Excerpt macro -2Content for Excerpt macro -3"));
    }

    @Test
    @Order(8)
    void contentReportTableMacroTest(TestUtils setup, TestReference testReference)
    {
        createPagesWithTags(setup);
        setup.createPage(testReference,createContent("contentReport-macros.vm"), "ContentReportTableTest");

        ContentReportTableMacroPage page = new ContentReportTableMacroPage();
        ContentReportTableMacro report0 = page.getMacro(0);
        ContentReportTableMacro report1 = page.getMacro(1);
        ContentReportTableMacro report2 = page.getMacro(2);
        ContentReportTableMacro report3 = page.getMacro(3);

        assertEquals(4,page.getMacroCount());

        // Checks the 1st content-report-table macro, with the tags "alpha,x" and the spaces "Main,XWiki".
        assertEquals(2,report0.getResultsCount());
        assertEquals(Arrays.asList("xwiki:XWiki.pageWithTags2","xwiki:Main.pageWithTags"),report0.getTitles());
        assertEquals(Arrays.asList("superadmin","superadmin"),report0.getCreators());
        assertEquals(2,report0.getModifiedDateCount());

        // Checks the 2nd content-report-table macro, with the tags "alpha,x", the spaces "Main,XWiki" and max = 1.
        assertEquals(1,report1.getResultsCount());
        assertEquals(Arrays.asList("xwiki:XWiki.pageWithTags2"),report1.getTitles());
        assertEquals(Arrays.asList("superadmin"),report1.getCreators());
        assertEquals(1,report1.getModifiedDateCount());

        // Checks the 3rd content-report-table macro, with the tag "x".
        assertEquals(1,report2.getResultsCount());
        assertEquals(Arrays.asList("xwiki:XWiki.pageWithTags2"),report2.getTitles());
        assertEquals(Arrays.asList("superadmin"),report2.getCreators());
        assertEquals(1,report2.getModifiedDateCount());

        // Checks the 4th content-report-table macro, with the tag "nonExistingTag".
        assertEquals(0,report3.getResultsCount());


    }
    private void createExcerptPage(TestUtils setup)
    {
        DocumentReference pageWithExcerptMacros = new DocumentReference("xwiki", "Main", "Excerpt");
        setup.createPage(pageWithExcerptMacros, createContent("excerpt-macros.vm"));
    }

    private String hexToRgb(String hex)
    {
        hex = hex.substring(1);
        if (hex.length() == 3) {
            hex = "" + hex.charAt(0) + hex.charAt(0) + hex.charAt(1) + hex.charAt(1) + hex.charAt(2) + hex.charAt(2);
        }

        int r = Integer.parseInt(hex.substring(0, 2), 16);
        int g = Integer.parseInt(hex.substring(2, 4), 16);
        int b = Integer.parseInt(hex.substring(4, 6), 16);

        return String.format("rgb(%d, %d, %d)", r, g, b);
    }
}
