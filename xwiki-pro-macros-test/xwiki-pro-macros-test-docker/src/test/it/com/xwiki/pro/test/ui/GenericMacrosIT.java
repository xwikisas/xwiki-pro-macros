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
import org.openqa.selenium.support.Color;
import org.xwiki.model.reference.DocumentReference;
import org.xwiki.tag.test.po.AddTagsPane;
import org.xwiki.tag.test.po.TaggablePage;
import org.xwiki.test.docker.junit5.ExtensionOverride;
import org.xwiki.test.docker.junit5.TestReference;
import org.xwiki.test.docker.junit5.UITest;
import org.xwiki.test.ui.TestUtils;
import org.xwiki.test.ui.po.CommentsTab;

import com.xwiki.pro.test.po.generic.ContentReportTableMacro;
import com.xwiki.pro.test.po.generic.ContributorsMacro;
import com.xwiki.pro.test.po.generic.ExcerptIncludeMacro;
import com.xwiki.pro.test.po.generic.ExpandMacro;
import com.xwiki.pro.test.po.generic.GenericMacrosPage;
import com.xwiki.pro.test.po.generic.ProfilePictureMacro;
import com.xwiki.pro.test.po.generic.RecentlyUpdatedMacro;
import com.xwiki.pro.test.po.generic.RegisterMacro;
import com.xwiki.pro.test.po.generic.TabGroupMacro;
import com.xwiki.pro.test.po.generic.TabMacro;
import com.xwiki.pro.test.po.generic.TeamMacroPage;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
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
        register.registerMacro(CONF_XWIKI_MACRO_SPACE, "Contributors");
        register.registerMacro(CONF_XWIKI_MACRO_SPACE, "ContentReportTableMacro");
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

    @BeforeAll
    void setup(TestUtils setup)
    {
        setup.loginAsSuperAdmin();
        setup.createUser("UserTest", "UserTest", "", "company", "xwiki", "phone", "07777777", "email",
            "usertest@example.com", "address", "userTestAddress", "comment", "test", "blog", "https://example.com/",
            "blogfeed", "https://example.com/", "avatar", "image1.png");
        setup.createUser("UserTest2", "UserTest", "", "company", "xwiki", "phone", "07777777", "email",
            "usertest2@example.com", "address", "userTestAddress2", "comment", "test2");
        setup.createUser("UserTest3", "UserTest", "", "company", "xwiki", "phone", "07777777", "email",
            "usertest3@example.com", "address", "userTestAddress3", "comment", "test3", "blog",
            "https://example" + ".com/", "blogfeed", "https://example.com/");

        setup.setGlobalRights("XWiki.XWikiAllGroup", "", "comment", true);
        setup.setGlobalRights("XWiki.XWikiAllGroup", "", "edit", true);

        try {
            setup.attachFile("XWiki", "UserTest", "image1.png", getClass().getResourceAsStream("/macros/image1.png"),
                false);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        registerMacros();
    }

    @Test
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
    }

    @Test
    @Order(2)
    void expandMacroTest(TestUtils setup, TestReference testReference)
    {
        setup.createPage(testReference, createContent("expand-macros.vm"), "ExpandTest");
        GenericMacrosPage expandPage = new GenericMacrosPage();

        String css = "details.confluence-expand-macro.panel.panel-default";
        ExpandMacro expand0 = expandPage.getMacro(css, 0, ExpandMacro::new);
        ExpandMacro expand1 = expandPage.getMacro(css, 1, ExpandMacro::new);

        assertEquals(2, expandPage.getMacroCount(css));

        // Nested Expand macros.
        // Checks the 1st Expand macro, with expanded = "true".
        assertEquals("ExpandTest1", expand0.getTitle());
        // Checking the icon.
        assertTrue(expand0.hasIcon());

        // Expanded = true.
        List<String> expectedContent = Arrays.asList("test0\ntest1", "test2");
        assertTrue(expand0.isExpanded());
        assertEquals(expectedContent, expand0.getTextContent());
        assertTrue(expand0.containsImage("example.jpg"));

        // Click -> closed macro, content not visible.
        expand0.toggle();
        assertFalse(expand0.isExpanded());
        assertNotEquals(expectedContent, expand0.getTextContent());
        assertTrue(expand0.containsImage("example.jpg"));

        // Click again -> open macro, content visible.
        expand0.toggle();
        assertTrue(expand0.isExpanded());
        assertEquals(expectedContent, expand0.getTextContent());
        assertTrue(expand0.containsImage("example.jpg"));

        // Checks the 2nd Expand macro, without expanded = "true".
        assertEquals("ExpandTest2", expand1.getTitle());
        assertTrue(expand1.hasIcon());

        List<String> expectedContent2 = Arrays.asList("test0\ntest1");
        // Closed macro, content not visible.
        assertFalse(expand1.isExpanded());
        assertNotEquals(expectedContent2, expand1.getTextContent());

        // Click -> opened macro, content visible.
        expand1.toggle();
        assertTrue(expand1.isExpanded());
        assertEquals(expectedContent2, expand1.getTextContent());

        // Click again -> closed macro, content not visible.
        expand1.toggle();
        assertFalse(expand1.isExpanded());
        assertNotEquals(expectedContent2, expand1.getTextContent());
    }

    @Test
    @Order(3)
    void profilePictureMacroTest(TestUtils setup, TestReference testReference)
    {

        setup.createPage(testReference, createContent("profilePicture-macros.vm"), "ProfilePictureTest");
        GenericMacrosPage picturePage = new GenericMacrosPage();

        assertEquals(2, picturePage.getMacroCount(".xwikiteam"));

        ProfilePictureMacro picture0 = picturePage.getMacro(".xwikiteam", 0, ProfilePictureMacro::new);
        ProfilePictureMacro picture1 = picturePage.getMacro(".xwikiteam", 1, ProfilePictureMacro::new);

        // Checks the 1st profilePicture macro, with an actual profile image.
        assertEquals("UserTest", picture0.getUserTitle());
        assertTrue(picture0.linkContainsUsername("UserTest"));
        assertEquals("60px", picture0.getAvatarSize());
        assertTrue(picture0.hasProfileImage());
        assertFalse(picture0.hasAvatarInitials());

        // Checks the 2nd profilePicture macro, with personalized size.
        assertEquals("UserTest2", picture1.getUserTitle());
        assertTrue(picture1.linkContainsUsername("UserTest2"));
        assertEquals("100px", picture1.getAvatarSize());
        assertFalse(picture1.hasProfileImage());
        assertTrue(picture1.hasAvatarInitials());
    }

    @Test
    @Order(4)
    void showIfMacroTest(TestUtils setup, TestReference testReference)
    {
        setup.createPage(testReference, createContent("showIf-macros.vm"), "ShowIfTest");
        GenericMacrosPage page = new GenericMacrosPage();

        assertTrue(page.containsParagraph("Content for testing the show-If macro"));
        assertTrue(page.containsParagraph("Content for testing the show-If macro -2"));
        assertFalse(page.containsParagraph("Content for testing the show-If macro -3"));
        assertFalse(page.containsParagraph("Content for testing the show-If macro -4"));
    }

    @Test
    @Order(5)
    void hideIfMacroTest(TestUtils setup, TestReference testReference)
    {

        setup.createPage(testReference, createContent("hideIf-macros.vm"), "HideIfTest");
        GenericMacrosPage page = new GenericMacrosPage();

        assertTrue(page.containsParagraph("content1"));
        assertFalse(page.containsParagraph("content2"));
        assertTrue(page.containsParagraph("content3"));
        assertTrue(page.containsParagraph("content4"));
    }

    @Test
    @Order(6)
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
        assertTrue(tab0.isActive());
        assertTrue(tab0.isContentDisplayed("tab0-content"));

        // Checks the tab_1 macro, with an added CSS values.
        assertFalse(tab1.isActive());
        assertTrue(tab1.getCssStyle().contains("background-color: yellow"));
        assertTrue(tab1.getCssStyle().contains("color: red"));
        assertTrue(tab1.getCssStyle().contains("border: 1px dashed " + Color.fromString("#ccc").asRgb()));
        assertEquals("0", tab1.getNextAfter());
        assertTrue(tab1.isContentDisplayed("tab1-content"));

        // Checks the tab_2 macro, with and added FADE effect nextAfter="3".
        assertTrue(tab2.getCssClass().contains("fade"));
        assertEquals("3", tab2.getNextAfter());
        assertFalse(tab2.isContentDisplayed("tab2-content"));
    }

    @Test
    @Order(7)
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

        // Waits until tab_6 is active.
        setup.getDriver().waitUntilCondition(driver -> "tab_6".equals(tabGroup2.getActiveTabId()));
        assertTrue(tabGroup2.isTabContentDisplayed("tab_6", "tab0-content"));

        // Waits for 1 second until tab_7 is active.
        setup.getDriver().waitUntilCondition(driver -> "tab_7".equals(tabGroup2.getActiveTabId()), 1000);
        assertTrue(tabGroup2.isTabContentDisplayed("tab_7", "tab1-content"));

        // Waits until tab_6 is active again (loopCards="true").
        setup.getDriver().waitUntilCondition(driver -> "tab_6".equals(tabGroup2.getActiveTabId()), 1000);
        assertTrue(tabGroup2.isTabContentDisplayed("tab_6", "tab0-content"));

        TabMacro tab3 = new TabMacro("tab_3");
        TabMacro tab4 = new TabMacro("tab_4");
        TabMacro tab5 = new TabMacro("tab_5");

        // Waits until tab_5 is active, it's the set default tab.
        setup.getDriver().waitUntilCondition(driver -> "tab_5".equals(tabGroup1.getActiveTabId()));

        // Waits until the content is active, added "+100" for safety.
        setup.getDriver().waitUntilCondition(driver -> tab5.isContentDisplayed("tab2-content"),
            (tabGroup1.getEffectDuration(tab5) * 1000)+100);

        // Waits until tab_3 is active.
        setup.getDriver().waitUntilCondition(driver -> "tab_3".equals(tabGroup1.getActiveTabId()),
            (tabGroup1.getFinalNextAfter(tab5) * 1000));

        setup.getDriver().waitUntilCondition(driver -> tab3.isContentDisplayed("tab0-content"),
            (tabGroup1.getEffectDuration(tab3) * 1000));

        // Waits until tab_4 is active.
        setup.getDriver().waitUntilCondition(driver -> "tab_4".equals(tabGroup1.getActiveTabId()),
            (tabGroup1.getFinalNextAfter(tab3) * 1000));

        setup.getDriver().waitUntilCondition(driver -> tab4.isContentDisplayed("tab1-content"),
            (tabGroup1.getEffectDuration(tab4) * 1000));

        // Waits until tab_5 is active again (loopCards="true").
        setup.getDriver().waitUntilCondition(driver -> "tab_5".equals(tabGroup1.getActiveTabId()),
            (tabGroup1.getFinalNextAfter(tab4) * 1000));

        setup.getDriver().waitUntilCondition(driver -> tab5.isContentDisplayed("tab2-content"),
            (tabGroup1.getEffectDuration(tab5) * 1000) + 100);
    }

    @Test
    @Order(8)
    void excerptMacroTest(TestUtils setup)
    {
        createExcerptPage(setup);
        GenericMacrosPage page = new GenericMacrosPage();

        assertTrue(page.containsParagraph("Content for excerpt macro"));
        assertTrue(page.containsParagraph("Content for Excerpt macro -2"));
        assertTrue(page.containsParagraph("Content for Excerpt macro -3"));
        assertFalse(page.containsParagraph("Content for Excerpt macro -4"));
    }

    @Test
    @Order(9)
    void excerptIncludeMacroTest(TestUtils setup)
    {
        createExcerptPage(setup);
        DocumentReference pageWithExcerptMacros = new DocumentReference("xwiki", "Main", "ExcerptIncludeTest");
        setup.createPage(pageWithExcerptMacros, createContent("excerptInclude-macros.vm"));

        GenericMacrosPage includePage = new GenericMacrosPage();

        String css = "div.macro-panel.macro-excerpt-include";

        ExcerptIncludeMacro excerpt0 = includePage.getMacro(css, 0, ExcerptIncludeMacro::new);
        ExcerptIncludeMacro excerpt1 = includePage.getMacro(css, 1, ExcerptIncludeMacro::new);
        ExcerptIncludeMacro excerpt2 = includePage.getMacro(css, 2, ExcerptIncludeMacro::new);

        // Checks the 1st ExcerptInclude macro, with the Excerpt macro defined on the same page.
        assertEquals("xwiki:Main.ExcerptIncludeTest", excerpt0.getTitle());
        assertEquals("Content for excerpt macro -1", excerpt0.getContentText());

        // Checks the 2nd ExcerptInclude macro, with the Excerpt macro containing also a table.
        assertEquals("xwiki:Main.ExcerptTest", excerpt1.getTitle());
        assertEquals("Content for excerpt macro", excerpt1.getContentText());
        assertTrue(excerpt1.containsTable());

        // Checks the 3rd ExcerptInclude macro, with nested Excerpt macros and with inline mode.
        assertEquals("xwiki:Main.ExcerptTest", excerpt2.getTitle());
        assertEquals("Content for Excerpt macro -2\nContent for Excerpt macro -3", excerpt2.getContentText());

        // Checks an ExcerptInclude macro, without a panel and with inline mode.
        assertTrue(includePage.containsText("Content for Excerpt macro -2Content for Excerpt macro -3"));
    }

    @Test
    @Order(10)
    void contentReportTableMacroTest(TestUtils setup, TestReference testReference)
    {
        createPagesWithTags(setup);
        setup.createPage(testReference, createContent("contentReport-macros.vm"), "ContentReportTableTest");

        GenericMacrosPage reportPage = new GenericMacrosPage();

        String css = "#xwikicontent table";

        ContentReportTableMacro report0 = reportPage.getMacro(css, 0, ContentReportTableMacro::new);
        ContentReportTableMacro report1 = reportPage.getMacro(css, 1, ContentReportTableMacro::new);
        ContentReportTableMacro report2 = reportPage.getMacro(css, 2, ContentReportTableMacro::new);
        ContentReportTableMacro report3 = reportPage.getMacro(css, 3, ContentReportTableMacro::new);

        assertEquals(4, reportPage.getMacroCount(css));

        // Checks the 1st content-report-table macro, with the tags "alpha,x" and the spaces "Main, XWiki".
        assertEquals(2, report0.getResultsCount());
        assertEquals(Arrays.asList("xwiki:XWiki.pageWithTags2", "xwiki:Main.pageWithTags"), report0.getTitles());
        assertEquals(Arrays.asList("superadmin", "superadmin"), report0.getCreators());
        assertEquals(2, report0.getModifiedDateCount());

        // Checks the 2nd content-report-table macro, with the tags "alpha,x", the spaces "Main, XWiki" and max = 1.
        assertEquals(1, report1.getResultsCount());
        assertEquals(Arrays.asList("xwiki:XWiki.pageWithTags2"), report1.getTitles());
        assertEquals(Arrays.asList("superadmin"), report1.getCreators());
        assertEquals(1, report1.getModifiedDateCount());

        // Checks the 3rd content-report-table macro, with the tag "x".
        assertEquals(1, report2.getResultsCount());
        assertEquals(Arrays.asList("xwiki:XWiki.pageWithTags2"), report2.getTitles());
        assertEquals(Arrays.asList("superadmin"), report2.getCreators());
        assertEquals(1, report2.getModifiedDateCount());

        // Checks the 4th content-report-table macro, with the tag "nonExistingTag".
        assertEquals(0, report3.getResultsCount());
    }

    @Test
    @Order(11)
    void contributorsMacroTest(TestUtils setup, TestReference testReference)
    {

        createPagesWithTags(setup);
        createTestPages(setup);

        setup.createPage(testReference, createContent("contributors-macros.vm"), "ContributorsTest");
        CommentsTab commentsTab = setup.gotoPage(testReference).openCommentsDocExtraPane();
        commentsTab.postComment("test comment", true);

        GenericMacrosPage contribPage = new GenericMacrosPage();

        String css = ".confluence-contributors";
        ContributorsMacro contrib0 = contribPage.getMacro(css, 0, ContributorsMacro::new);
        ContributorsMacro contrib1 = contribPage.getMacro(css, 1, ContributorsMacro::new);
        ContributorsMacro contrib2 = contribPage.getMacro(css, 2, ContributorsMacro::new);
        ContributorsMacro contrib3 = contribPage.getMacro(css, 3, ContributorsMacro::new);
        ContributorsMacro contrib4 = contribPage.getMacro(css, 4, ContributorsMacro::new);
        ContributorsMacro contrib5 = contribPage.getMacro(css, 5, ContributorsMacro::new);
        ContributorsMacro contrib6 = contribPage.getMacro(css, 6, ContributorsMacro::new);

        // There should be 7 contributors macros on the page.
        assertEquals(7, contribPage.getMacroCount(css));

        // Checks the 1st macro, with default properties.
        assertEquals(1, contrib0.getNames().size());
        assertEquals(Arrays.asList("superadmin"), contrib0.getNames());
        assertFalse(contrib0.isListMode());
        assertFalse(contrib0.hasLastModifiedDates());
        assertFalse(contrib0.hasPages());
        assertFalse(contrib0.hasContributionCount());

        // Checks the 2nd macro, with default properties
        assertEquals(3, contrib1.getNames().size());
        assertEquals(Arrays.asList("superadmin", "UserTest3", "UserTest2"), contrib1.getNames());
        assertTrue(contrib1.isListMode());
        assertTrue(contrib1.hasLastModifiedDates());
        assertFalse(contrib1.hasPages());
        assertFalse(contrib1.hasContributionCount());

        assertEquals(3, contrib2.getNames().size());
        assertEquals(Arrays.asList("superadmin", "UserTest3", "UserTest2"), contrib1.getNames());
        assertFalse(contrib2.isListMode());
        assertFalse(contrib2.hasLastModifiedDates());
        assertFalse(contrib2.hasPages());
        assertTrue(contrib2.hasContributionCount());
        assertEquals(Arrays.asList(1, 1, 1), contrib2.getContributionCounts());

        assertEquals(1, contrib3.getNames().size());
        assertEquals(Arrays.asList("superadmin"), contrib3.getNames());
        assertTrue(contrib3.hasContributionCount());
        assertEquals(Arrays.asList(1), contrib3.getContributionCounts());
        assertTrue(contrib3.hasPages());
        assertEquals(Arrays.asList("Profile of UserTest", "Profile of UserTest2", "Profile of UserTest3",
            "xwiki:XWiki.pageWithTags2"), contrib3.getPages());

        assertEquals(0, contrib4.getNames().size());
        assertEquals("None found :(", contrib4.getNoneFoundMessage());
        assertTrue(contrib4.hasPages());
        assertEquals(Arrays.asList("(none)"), contrib4.getPages());

        assertEquals(1, contrib5.getNames().size());
        assertEquals(Arrays.asList("superadmin"), contrib5.getNames());
        assertTrue(contrib5.hasPages());
        assertEquals(Arrays.asList("ContributorsTest"), contrib5.getPages());

        assertEquals(1, contrib6.getNames().size());
        assertEquals(Arrays.asList("superadmin"), contrib5.getNames());
        assertTrue(contrib6.hasPages());
        assertEquals(Arrays.asList("ContributorsTest"), contrib6.getPages());
    }

    @Test
    @Order(12)
    void recentlyUpdatedMacroTest(TestUtils setup, TestReference testReference)
    {
        createPagesWithTags(setup);
        createTestPages(setup);

        setup.createPage(testReference, createContent("recentlyupdated-macros.vm"), "RecentlyUpdatedTest");

        GenericMacrosPage recentlyPage = new GenericMacrosPage();

        String css = ".recently-updated-macro";
        RecentlyUpdatedMacro recent0 = recentlyPage.getMacro(css, 0, RecentlyUpdatedMacro::new);
        RecentlyUpdatedMacro recent1 = recentlyPage.getMacro(css, 1, RecentlyUpdatedMacro::new);
        RecentlyUpdatedMacro recent2 = recentlyPage.getMacro(css, 2, RecentlyUpdatedMacro::new);
        RecentlyUpdatedMacro recent3 = recentlyPage.getMacro(css, 3, RecentlyUpdatedMacro::new);
        RecentlyUpdatedMacro recent4 = recentlyPage.getMacro(css, 4, RecentlyUpdatedMacro::new);
        RecentlyUpdatedMacro recent5 = recentlyPage.getMacro(css, 5, RecentlyUpdatedMacro::new);
        RecentlyUpdatedMacro recent6 = recentlyPage.getMacro(css, 6, RecentlyUpdatedMacro::new);
        RecentlyUpdatedMacro recent7 = recentlyPage.getMacro(css, 7, RecentlyUpdatedMacro::new);
        RecentlyUpdatedMacro recent8 = recentlyPage.getMacro(css, 8, RecentlyUpdatedMacro::new);

        assertEquals(4, recent0.getItemTitles().size());
        assertEquals(Arrays.asList("xwiki:Main.testPage2", "image1.png", "xwiki:Main.testPage2", "xwiki:Main.testPage"),
            recent0.getItemTitles());
        assertEquals("concise", recent0.getResultsTheme());
        assertTrue(recent0.themeStructureIsCorrect("concise"));
        assertEquals("250px", recent0.getMacroWidth());

        assertEquals(2, recent1.getItemTitles().size());
        assertEquals(Arrays.asList("xwiki:Main.testPage2", "xwiki:Main.testPage"), recent1.getItemTitles());
        assertTrue(recent1.hasShowMoreButton());
        recent1.clickShowMore();
        assertEquals(4, recent1.getItemTitles().size());
        assertEquals(Arrays.asList("xwiki:Main.testPage2", "xwiki:Main.testPage", "xwiki:XWiki.pageWithTags2",
            "xwiki" + ":Main.pageWithTags"), recent1.getItemTitles());
        assertEquals("concise", recent1.getResultsTheme());
        assertTrue(recent1.themeStructureIsCorrect("concise"));
        assertEquals("150", recent1.getMacroWidth());

        assertEquals(1, recent2.getItemTitles().size());
        assertEquals(Arrays.asList("xwiki:Main.testPage2"), recent2.getItemTitles());
        assertEquals("100", recent2.getMacroWidth());

        assertEquals(0, recent3.getItemTitles().size());

        assertEquals(0, recent4.getItemTitles().size());
        assertFalse(recent4.hasHeading());

        assertEquals(1, recent5.getItemTitles().size());
        assertEquals(Arrays.asList("xwiki:Main.pageWithTags"), recent5.getItemTitles());
        assertEquals("social", recent5.getResultsTheme());
        assertTrue(recent5.themeStructureIsCorrect("social"));
        assertEquals("superadmin", recent5.getAuthorName(0));
        assertFalse(recent5.hasAvatars());

        assertEquals(2, recent6.getItemTitles().size());
        assertEquals(Arrays.asList("xwiki:Main.testPage2", "xwiki:Main.testPage"), recent6.getItemTitles());
        assertTrue(recent6.hasAvatars());

        assertEquals(2, recent7.getItemTitles().size());
        assertEquals(Arrays.asList("xwiki:Main.testPage2", "xwiki:Main.testPage"), recent7.getItemTitles());
        assertEquals("sidebar", recent7.getResultsTheme());
        assertTrue(recent7.themeStructureIsCorrect("sidebar"));

        assertEquals(1, recent8.getItemTitles().size());
        assertEquals(Arrays.asList("xwiki:Main.testPage"), recent8.getItemTitles());
        assertEquals("social", recent8.getResultsTheme());
        assertTrue(recent8.themeStructureIsCorrect("social"));
        assertEquals("UserTest3", recent8.getAuthorName(0));
        assertTrue(recent8.hasAvatars());
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

    private void createExcerptPage(TestUtils setup)
    {
        DocumentReference pageWithExcerptMacros = new DocumentReference("xwiki", "Main", "ExcerptTest");
        setup.createPage(pageWithExcerptMacros, createContent("excerpt-macros.vm"));
    }

    private void createTestPages(TestUtils setup)
    {
        final DocumentReference testPage = new DocumentReference("xwiki", "Main", "testPage");

        final DocumentReference testPage2 = new DocumentReference("xwiki", "Main", "testPage2");

        setup.deletePage(testPage);
        setup.deletePage(testPage2);

        setup.login("UserTest3", "UserTest");
        setup.createPage(testPage, "test page 1");
        setup.gotoPage(testPage);
        TaggablePage taggablePage = new TaggablePage();
        AddTagsPane tagsPane = taggablePage.addTags();
        tagsPane.setTags("recent, recent2");
        tagsPane.add();

        setup.login("UserTest2", "UserTest");
        setup.createPage(testPage2, "test page 2");
        setup.gotoPage(testPage2);
        TaggablePage taggablePage2 = new TaggablePage();
        AddTagsPane tagsPane2 = taggablePage2.addTags();
        tagsPane.setTags("recent");
        tagsPane2.add();
        CommentsTab commentsTab = setup.gotoPage("Main", "testPage2").openCommentsDocExtraPane();
        commentsTab.postComment("test comment", true);
        try {
            setup.attachFile("Main", "testPage2", "image1.png", getClass().getResourceAsStream("/macros/image1.png"),
                false);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        setup.loginAsSuperAdmin();
    }
}
