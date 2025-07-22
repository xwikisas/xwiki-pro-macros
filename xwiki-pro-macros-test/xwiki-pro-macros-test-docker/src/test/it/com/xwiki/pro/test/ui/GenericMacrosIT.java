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

import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.xwiki.model.reference.DocumentReference;
import org.xwiki.tag.test.po.AddTagsPane;
import org.xwiki.tag.test.po.TaggablePage;
import org.xwiki.test.docker.junit5.ExtensionOverride;
import org.xwiki.test.docker.junit5.UITest;
import org.xwiki.test.ui.TestUtils;

import com.xwiki.pro.test.po.generic.ButtonMacroPage;
import com.xwiki.pro.test.po.generic.ExpandMacroPage;
import com.xwiki.pro.test.po.generic.RegisterMacro;
import com.xwiki.pro.test.po.generic.StatusMacroPage;
import com.xwiki.pro.test.po.generic.TagListMacroPage;
import com.xwiki.pro.test.po.generic.TeamMacroPage;
import com.xwiki.pro.test.po.generic.UserProfileMacroPage;

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
    properties = {
        "xwikiCfgPlugins=com.xpn.xwiki.plugin.tag.TagPlugin",
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

    private final DocumentReference pageWithButtonMacros = new DocumentReference("xwiki", "Main", "ButtonTest");

    private final DocumentReference pageWithStatusMacros = new DocumentReference("xwiki", "Main", "StatusTest");

    private final DocumentReference pageWithTagListMacros = new DocumentReference("xwiki", "Main", "TagListTest");

    private final DocumentReference pageWithTags = new DocumentReference("xwiki", "Main", "pageWithTags");

    private final DocumentReference pageWithTags2 = new DocumentReference("xwiki", "XWiki", "pageWithTags2");

    private final DocumentReference pageWithExpandMacro = new DocumentReference("xwiki", "Main", "ExpandTest");

    private final DocumentReference pageWithUserProfileMacro = new DocumentReference("xwiki", "Main",
        "UserProfileTest");

    private static final String PAGE_WITH_TEAM_MACROS_CONTENT = "{{team/}}\n"
        + "\n"
        + "{{team tag=\"testTag\" /}}\n"
        + "\n"
        + "{{team tag=\"nonExistentTag\" /}}";

    private static final String PAGE_WITH_BUTTON_MACROS_CONTENT =
        "{{button id=\"testbtn1\" label=\"test1\" url=\"https://dev.xwiki.org/xwiki/bin/view/Community/Testing/DockerTesting\" color=\"#ff66ff\" newTab=\"true\" icon=\"check\" width=\"100px\"/}}\n"
            + "\n"
            + "{{button id=\"testbtn2\" label=\"test2\" url=\"https://wiki.eniris.be/wiki/publicinformation/view/Help/Applications/Contributors/Charlie%20Chaplin\" newTab=\"false\" type=\"DANGER\" width=\"60%\"/}}\n"
            + "{{button id=\"testbtn3\" label=\"test3\" color=\"#ff66ff\"  url=\"https://wiki.eniris.be/wiki/publicinformation/view/Help/Applications/Contributors/Charlie%20Chaplin\" newTab=\"false\" type=\"SUCCESS\" icon=\"check\"  width=\"80px\"/}}\n"
            + "{{button id=\"testbtn4\" label=\"test4\" url=\"https://wiki.eniris.be/wiki/publicinformation/view/Help/Applications/Contributors/Charlie%20Chaplin\" newTab=\"false\" type=\"WARNING\" color=\"#ff66ff\" width=\"80px\"/}}\n"
            + "{{button id=\"testbtn5\" label=\"test5\" url=\"https://dev.xwiki.org/xwiki/bin/view/Community/Testing/DockerTesting\" icon=\"page\"  newTab=\"true\"  width=\"100px\"/}}\n";

    private static final String PAGE_WITH_STATUS_MACROS_CONTENT = "{{status title =\"test1\" /}}"
        + "\n"
        + "{{status title =\"test2\" colour =\"Yellow\" subtle =\"true\"/}}"
        + "\n"
        + "{{status title =\"Title with double quotes: ?<>@!$%^&*(){}: |; ' , ./` in it .          \" colour =\"Yellow\" subtle=\"false\"/}}";

    private static final String PAGE_WITH_TAGLIST_MACROS_CONTENT = "{{tagList /}}\n"
        + "\n"
        + "{{tagList excludedTags=\"beta\"/}}\n"
        + "\n"
        + "{{tagList spaces =\"Main,XWiki\" excludedTags=\"gamma\"/}}\n";

    private static final String PAGE_WITH_EXPAND_MACROS_CONTENT =
        "{{expand title=\"ExpandTest1\" expanded=\"true\"}}\n"
            + "test0\n"
            + "test1\n"
            + "image:example.jpg\"\n"
            + "\n"
            + "{{expand title=\"ExpandTest2\"}}\n"
            + "test0\n"
            + "test1\n"
            + "\n"
            + "{{/expand}}\n"
            + "\n"
            + "test2\n"
            + "\n"
            + "{{/expand}}";

    private static final String PAGE_WITH_USER_PROFILE_MACROS_CONTENT =
        "{{userProfile reference=\"XWiki.UserTest\"/}}\n"
            + "\n";

    private static final String PAGE_WITH_USER_LIST_MACROS_CONTENT = "{{userList users=\"XWiki.UserTest\" /}}\n";
    private static final List<String> BASE_XWIKI_MACRO_SPACE = List.of("XWiki", "Macros");

    private void registerMacros()
    {
        RegisterMacro register = new RegisterMacro();
        register.registerMacro(BASE_XWIKI_MACRO_SPACE, "Button");
        register.registerMacro(BASE_XWIKI_MACRO_SPACE, "MicrosoftStream");
        register.registerMacro(BASE_XWIKI_MACRO_SPACE, "Panel");
        register.registerMacro(BASE_XWIKI_MACRO_SPACE, "Team");
        register.registerMacro(BASE_XWIKI_MACRO_SPACE, "Taglist");
        register.registerMacro(BASE_XWIKI_MACRO_SPACE, "Expand");
        //register.registerMacro(BASE_XWIKI_MACRO_SPACE, "UserProfile");
    }

    @BeforeAll
    void setup(TestUtils setup)
    {
        setup.loginAsSuperAdmin();
        setup.createUser("UserTest", "UserTest", "");
        setup.createUser("UserTest2", "UserTest", "");
        setup.createUser("UserTest3", "UserTest", "");

        setup.deletePage(pageWithTeamMacros);
        setup.deletePage(pageWithButtonMacros);
        setup.deletePage(pageWithStatusMacros);
        setup.deletePage(pageWithTagListMacros);
        setup.deletePage(pageWithExpandMacro);
        setup.deletePage(pageWithUserProfileMacro);

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

        String username = "xwiki:XWiki.UserTest";
        String username2 = "xwiki:XWiki.UserTest2";

        assertEquals("UserTest", page.getUserTitle(0, username));
        assertEquals("UserTest2", page.getUserTitle(0, username2));

        assertTrue(page.getProfileLink(0, username).endsWith("/xwiki/bin/view/XWiki/UserTest"));
        assertTrue(page.getProfileLink(0, username2).endsWith("/xwiki/bin/view/XWiki/UserTest2"));

        assertEquals("U", page.getAvatarInitials(0, username));
        assertEquals("U", page.getAvatarInitials(0, username2));

        assertTrue(page.getAvatarBackgroundColor(0, username).contains("rgb(0, 170, 102)"));
        assertTrue(page.getAvatarBackgroundColor(0, username2).contains("rgb(0, 170, 102)"));

        assertEquals("rgb(255, 255, 255)", page.getAvatarFontColor(0, username));
        assertEquals("rgb(255, 255, 255)", page.getAvatarFontColor(0, username2));

        assertTrue(page.getAvatarSize(0, username).startsWith("60"));
        assertTrue(page.getAvatarSize(0, username2).startsWith("60"));

        assertTrue(page.getAvatarBorderRadius(0, username).startsWith("60"));
        assertTrue(page.getAvatarBorderRadius(0, username2).startsWith("60"));
    }

    @Test
    @Order(2)
    void buttonMacroTest(TestUtils setup)
    {
        setup.deletePage(pageWithButtonMacros);
        setup.gotoPage("XWiki", "ButtonTest");
        setup.createPage(pageWithButtonMacros, PAGE_WITH_BUTTON_MACROS_CONTENT);

        ButtonMacroPage page = new ButtonMacroPage();

        //Checks the width of the buttons
        assertEquals("100px", page.getButtonWidth("testbtn1"));
        assertEquals("80px", page.getButtonWidth("testbtn3"));

        //Checks the color of the buttons
        assertEquals("rgb(255, 102, 255)", page.getButtonColor("testbtn1"));
        assertEquals("rgb(255, 102, 255)", page.getButtonColor("testbtn3"));
        assertEquals("rgb(255, 102, 255)", page.getButtonColor("testbtn4"));

        //Checks the label of the buttons
        assertEquals("test1", page.getButtonLabel("testbtn1"));
        assertEquals("test2", page.getButtonLabel("testbtn2"));
        assertEquals("test3", page.getButtonLabel("testbtn3"));
        assertEquals("test4", page.getButtonLabel("testbtn4"));
        assertEquals("test5", page.getButtonLabel("testbtn5"));

        //Checks the <a> tag of the parents' buttons
        assertEquals("a", page.getButtonParentTag("testbtn1"));
        assertEquals("a", page.getButtonParentTag("testbtn2"));
        assertEquals("a", page.getButtonParentTag("testbtn3"));
        assertEquals("a", page.getButtonParentTag("testbtn4"));
        assertEquals("a", page.getButtonParentTag("testbtn5"));

        //Checks the url of the parents' buttons
        assertEquals("https://dev.xwiki.org/xwiki/bin/view/Community/Testing/DockerTesting",
            page.getButtonParentUrl("testbtn1"));
        assertEquals(
            "https://wiki.eniris.be/wiki/publicinformation/view/Help/Applications/Contributors/Charlie%20Chaplin",
            page.getButtonParentUrl("testbtn2"));

        //Checks whether the url is opening in a new tab or not
        assertEquals("_blank", page.getButtonParentTarget("testbtn1"));
        assertEquals("", page.getButtonParentTarget("testbtn2"));

        //The type of the button (DEFAULT/ DANGER/ SUCCESS / WARNING)
        assertTrue(page.getButtonClass("testbtn1").endsWith("-default"));
        assertTrue(page.getButtonClass("testbtn2").endsWith("-danger"));
        assertTrue(page.getButtonClass("testbtn3").endsWith("-success"));
        assertTrue(page.getButtonClass("testbtn4").endsWith("-warning"));
        assertTrue(page.getButtonClass("testbtn5").endsWith("-default"));

        //The button has/ doesn't have an Icon assigned
        assertTrue(page.hasIcon("testbtn1"));
        assertFalse(page.hasIcon("testbtn2"));
        assertTrue(page.hasIcon("testbtn3"));
        assertFalse(page.hasIcon("testbtn4"));
        assertTrue(page.hasIcon("testbtn5"));
    }

    @Test
    @Order(3)
    void statusMacroTest(TestUtils setup)

    {
        setup.deletePage(pageWithButtonMacros);
        setup.gotoPage("XWiki", "StatusTest");
        setup.createPage(pageWithStatusMacros, PAGE_WITH_STATUS_MACROS_CONTENT);

        StatusMacroPage page = new StatusMacroPage();

        //Checks the titles of the status macros
        assertEquals("test1", page.getStatusTitle(0));
        assertEquals("test2", page.getStatusTitle(1));

        //assertEquals("Title with double quotes: ?<>@!$%^&*(){}:  |; ' , ./` in it .          ",
        //page.getStatusTitle(2));

        //Checks the type of the status macros/ the color
        assertEquals("grey", page.getStatusColor(0));
        assertEquals("yellow", page.getStatusColor(1));
        assertEquals("yellow", page.getStatusColor(2));

        //Checks the subtle property
        assertFalse(page.isSubtle(0));
        assertTrue(page.isSubtle(1));
        assertFalse(page.isSubtle(2));
    }

    @Test
    @Order(4)
    void tagListMacroTest(TestUtils setup)
    {
        setup.deletePage(pageWithTagListMacros);
        setup.gotoPage("XWiki", "TagListTest");
        setup.createPage(pageWithTagListMacros, PAGE_WITH_TAGLIST_MACROS_CONTENT);

        TagListMacroPage page = new TagListMacroPage();

        //Checks the ordered titles of the tags from the tagList
        List<String> expectedTitles1 = Arrays.asList("A-B", "G");
        assertEquals(expectedTitles1, page.getGlossaryTitles(0));

        //Checks the ordered titles of the tags from the tagList
        List<String> expectedTitles2 = Arrays.asList("A-G");
        assertEquals(expectedTitles2, page.getGlossaryTitles(1));

        //Checks the ordered names of the tags from the tagList
        List<String> expectedTagNames1 = Arrays.asList("alpha", "beta", "gamma");
        assertEquals(expectedTagNames1, page.getTagNames(0));

        //Checks the ordered names of the tags from the tagList
        List<String> expectedTagNames2 = Arrays.asList("alpha", "gamma");
        assertEquals(expectedTagNames2, page.getTagNames(1));

        //Checks the <a> tags of the tags from the tagList
        for (String i : expectedTagNames1) {
            assertEquals("a", page.getHtmlTagForTagName(0, i));
        }

        //Checks the <a> tags of the tags from the tagList
        for (String i : expectedTagNames2) {
            assertEquals("a", page.getHtmlTagForTagName(1, i));
        }

        //Checks the ordered titles of the tags from the tagList, multiple spaces
        List<String> expectedTitles3 = Arrays.asList("A-X", "Y-Z");
        assertEquals(expectedTitles3, page.getGlossaryTitles(2));

        //Checks the ordered names of the tags from the tagList
        List<String> expectedTagNames3 = Arrays.asList("alpha", "beta", "testTag", "x", "y", "z");
        assertEquals(expectedTagNames3, page.getTagNames(2));

        //Checks the <a> tags of the tags from the tagList
        for (String i : expectedTagNames3) {
            assertEquals("a", page.getHtmlTagForTagName(2, i));
        }
    }

    @Test
    @Order(5)
    void expandMacroTest(TestUtils setup)
    {
        setup.deletePage(pageWithExpandMacro);
        setup.gotoPage("Main", "ExpandMacroTest");
        setup.createPage(pageWithExpandMacro, PAGE_WITH_EXPAND_MACROS_CONTENT);

        ExpandMacroPage page = new ExpandMacroPage();

        //Nested Expand macros
        assertEquals("ExpandTest1", page.getTitleText(0));
        assertEquals("ExpandTest2", page.getTitleText(1));

        //Checking the icon
        assertTrue(page.hasIcon(0));
        assertTrue(page.hasIcon(1));

        List<String> expectedContent = Arrays.asList("test0\ntest1", "test2");
        //Expanded = true
        assertTrue(page.isExpanded(0));
        assertEquals(expectedContent, page.getParagraphs(0));
        assertTrue(page.containsImageWithSrc(0, "example.jpg"));

        //Click -> closed macro, content not visible
        page.toggleMacro(0);
        assertFalse(page.isExpanded(0));
        assertNotEquals(expectedContent, page.getParagraphs(0));
        assertTrue(page.containsImageWithSrc(0, "example.jpg"));

        //Click again -> open macro, content visible
        page.toggleMacro(0);
        assertTrue(page.isExpanded(0));
        assertEquals(expectedContent, page.getParagraphs(0));
        assertTrue(page.containsImageWithSrc(0, "example.jpg"));

        List<String> expectedContent2 = Arrays.asList("test0\ntest1");
        //Expanded = false
        //Closed macro, content not visible
        assertFalse(page.isExpanded(1));
        assertNotEquals(expectedContent2, page.getParagraphs(1));

        //Click -> opened macro, content visible
        page.toggleMacro(1);
        assertTrue(page.isExpanded(1));
        assertEquals(expectedContent2, page.getParagraphs(1));

        //Click again -> closed macro, content not visible
        page.toggleMacro(1);
        assertFalse(page.isExpanded(1));
        assertNotEquals(expectedContent2, page.getParagraphs(1));
    }

    @Test
    @Order(6)
    void userProfileMacroTest(TestUtils setup)
    {
        setup.deletePage(pageWithUserProfileMacro);
        setup.gotoPage("Main", "UserProfileTest");
        setup.createPage(pageWithUserProfileMacro, PAGE_WITH_USER_PROFILE_MACROS_CONTENT);
        //setup.createPage(pageWithUserProfileMacro, PAGE_WITH_USER_LIST_MACROS_CONTENT);

        UserProfileMacroPage page = new UserProfileMacroPage();
    }
}