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
import java.util.Set;
import java.util.stream.Collectors;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.xwiki.model.reference.DocumentReference;
import org.xwiki.test.docker.junit5.TestReference;
import org.xwiki.tag.test.po.AddTagsPane;
import org.xwiki.tag.test.po.TaggablePage;
import org.xwiki.test.docker.junit5.ExtensionOverride;
import org.xwiki.test.docker.junit5.UITest;
import org.xwiki.test.ui.TestUtils;

import com.xwiki.pro.test.po.generic.ButtonMacroPage;
import com.xwiki.pro.test.po.generic.MicrosoftStreamMacroPage;
import com.xwiki.pro.test.po.generic.PanelMacroPage;
import com.xwiki.pro.test.po.generic.RegisterMacro;
import com.xwiki.pro.test.po.generic.StatusMacroPage;
import com.xwiki.pro.test.po.generic.TagListMacroPage;
import com.xwiki.pro.test.po.generic.TeamMacroPage;
import com.xwiki.pro.test.po.generic.UserListMacroPage;
import com.xwiki.pro.test.po.generic.UserProfileMacroPage;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
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
    private static final List<String> BASE_XWIKI_MACRO_SPACE = List.of("XWiki", "Macros");

    private final DocumentReference pageWithTagListMacros = new DocumentReference("xwiki", "Main", "TagListTest");

    private final DocumentReference pageWithTags = new DocumentReference("xwiki", "Main", "pageWithTags");

    private final DocumentReference pageWithTags2 = new DocumentReference("xwiki", "XWiki", "pageWithTags2");

    private void registerMacros()
    {
        RegisterMacro register = new RegisterMacro();
        register.registerMacro(BASE_XWIKI_MACRO_SPACE, "Button");
        register.registerMacro(BASE_XWIKI_MACRO_SPACE, "MicrosoftStream");
        register.registerMacro(BASE_XWIKI_MACRO_SPACE, "Panel");
        register.registerMacro(BASE_XWIKI_MACRO_SPACE, "Team");
        register.registerMacro(BASE_XWIKI_MACRO_SPACE, "Taglist");
    }

    @BeforeAll
    void setup(TestUtils setup)
    {
        setup.loginAsSuperAdmin();
        setup.createUser("UserTest", "UserTest", "", "company", "xwiki", "phone", "07777777", "email"
            , "usertest@example.com", "address", "userTestAddress", "comment", "test", "blog", "https://example.com/",
            "blogfeed", "https://example.com/");
        setup.createUser("UserTest2", "UserTest", "", "company", "xwiki", "phone", "07777777", "email"
            , "usertest2@example.com", "address", "userTestAddress2", "comment", "test2");
        setup.createUser("UserTest3", "UserTest", "", "company", "xwiki", "phone", "07777777", "email"
            , "usertest3@example.com", "address", "userTestAddress3", "comment", "test3", "blog", "https://example"
                + ".com/", "blogfeed", "https://example.com/");

        setup.setGlobalRights("XWiki.XWikiAllGroup", "", "comment", true);
        setup.setGlobalRights("XWiki.XWikiAllGroup", "", "edit", true);
        registerMacros();
    }

    public void createPagesWithTags(TestUtils setup)
    {
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

    public String createContent(String filename)
    {
        try (InputStream inputStream = getClass().getResourceAsStream("/macros/" + filename)) {
            if (inputStream == null) {
                throw new RuntimeException("Failed to load " + filename + " from resources.");
            }

            return new BufferedReader(new InputStreamReader(inputStream))
                .lines()
                .filter(line -> !line.trim().startsWith("##"))
                .collect(Collectors.joining("\n"));
        } catch (IOException e) {
            throw new RuntimeException("Failed to read macro file: " + filename, e);
        }
    }

    @Test
    @Order(1)
    void teamMacroTest(TestUtils setup,TestReference testReference)
    {
        setup.gotoPage("XWiki", "UserTest");
        TaggablePage taggablePage = new TaggablePage();
        AddTagsPane tagsPane = taggablePage.addTags();
        tagsPane.setTags("testTag");
        tagsPane.add();
        setup.createPage(testReference, createContent("team-macros.vm"),"TeamTest");
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
        String username3 = "xwiki:XWiki.UserTest3";

        // Checks the users' titles.
        assertEquals("UserTest", page.getUserTitle(0, username));
        assertEquals("UserTest2", page.getUserTitle(0, username2));
        assertEquals("UserTest3", page.getUserTitle(0, username3));

        assertEquals("UserTest", page.getUserTitle(1, username));
        // Checks the users' profile links.
        assertTrue(page.getProfileLink(0, username).endsWith("/xwiki/bin/view/XWiki/UserTest"));
        assertTrue(page.getProfileLink(0, username2).endsWith("/xwiki/bin/view/XWiki/UserTest2"));
        assertTrue(page.getProfileLink(0, username3).endsWith("/xwiki/bin/view/XWiki/UserTest3"));

        assertTrue(page.getProfileLink(1, username).endsWith("/xwiki/bin/view/XWiki/UserTest"));

        // Checks the existence of avatar initials.
        assertTrue(page.hasAvatarInitials(0, "xwiki:XWiki.UserTest"));
        assertTrue(page.hasAvatarInitials(0, "xwiki:XWiki.UserTest2"));
        assertTrue(page.hasAvatarInitials(0, "xwiki:XWiki.UserTest3"));

        // Checking avatar attributes: initials, backgroundColor, fontColor, size, borderRadius.
        assertEquals("U", page.getAvatarInitials(0, username));
        assertEquals("U", page.getAvatarInitials(0, username2));
        assertEquals("U", page.getAvatarInitials(0, username3));

        // disableLetterAvatars="true".
        assertFalse(page.hasAvatarInitials(1, "xwiki:XWiki.UserTest"));

        // default color.
        assertTrue(page.getAvatarBackgroundColor(0, username).contains("rgb(0, 170, 102)"));
        assertTrue(page.getAvatarBackgroundColor(0, username2).contains("rgb(0, 170, 102)"));
        assertTrue(page.getAvatarBackgroundColor(0, username3).contains("rgb(0, 170, 102)"));

        // personalized color.
        assertEquals("rgb(204, 0, 255)", page.getAvatarFontColor(0, username));
        assertEquals("rgb(204, 0, 255)", page.getAvatarFontColor(0, username2));
        assertEquals("rgb(204, 0, 255)", page.getAvatarFontColor(0, username3));

        assertTrue(page.getAvatarSize(0, username).startsWith("60"));
        assertTrue(page.getAvatarSize(0, username2).startsWith("60"));
        assertTrue(page.getAvatarSize(0, username3).startsWith("60"));

        assertTrue(page.getAvatarSize(1, username).startsWith("60"));

        assertTrue(page.getAvatarBorderRadius(0, username).startsWith("60"));
        assertTrue(page.getAvatarBorderRadius(0, username2).startsWith("60"));
        assertTrue(page.getAvatarBorderRadius(0, username3).startsWith("60"));

        // Checks the property of hidden usernames.
        assertTrue(page.isUsernameHidden(0));
        assertFalse(page.isUsernameHidden(1));
        assertTrue(page.isUsernameHidden(2));

        // Checks the visibility of usernames.
        assertFalse(page.isUsernameVisible(0, username));
        assertFalse(page.isUsernameVisible(0, username2));
        assertFalse(page.isUsernameVisible(0, username3));
        assertTrue(page.isUsernameVisible(1, username));

        // Checks that if a team macro is empty (0 users), the message "There is nobody to show." appears.
        assertTrue(page.hasEmptyTeamMessage(2));
    }

    @Test
    @Order(2)
    void buttonMacroTest(TestUtils setup, TestReference testReference)
    {
        setup.createPage(testReference, createContent("button-macros.vm"),"ButtonTest");

        ButtonMacroPage page = new ButtonMacroPage();

        // Checks the width of the buttons.
        assertEquals("100px", page.getButtonWidth("testbtn1"));
        assertEquals("80px", page.getButtonWidth("testbtn3"));

        // Checks the color of the buttons.
        assertEquals(hexToRgb("#ff66ff"), page.getButtonColor("testbtn1"));
        assertEquals(hexToRgb("#ff66ff"), page.getButtonColor("testbtn3"));
        assertEquals(hexToRgb("#ff66ff"), page.getButtonColor("testbtn4"));

        // Checks the label of the buttons.
        assertEquals("test1", page.getButtonLabel("testbtn1"));
        assertEquals("test2", page.getButtonLabel("testbtn2"));
        assertEquals("test3", page.getButtonLabel("testbtn3"));
        assertEquals("test4", page.getButtonLabel("testbtn4"));
        assertEquals("test5", page.getButtonLabel("testbtn5"));

        // Checks the url of the buttons.
        assertTrue(page.hasLink("testbtn1", "https://dev.xwiki.org/xwiki/bin/view/Community/Testing/DockerTesting"));
        assertTrue(page.hasLink("testbtn2",
            "https://wiki.eniris.be/wiki/publicinformation/view/Help/Applications/Contributors/Charlie%20Chaplin"));
        assertTrue(page.hasLink("testbtn3",
            "https://wiki.eniris.be/wiki/publicinformation/view/Help/Applications/Contributors/Charlie%20Chaplin"));
        assertTrue(page.hasLink("testbtn4",
            "https://wiki.eniris.be/wiki/publicinformation/view/Help/Applications/Contributors/Charlie%20Chaplin"));
        assertTrue(page.hasLink("testbtn5", "https://dev.xwiki.org/xwiki/bin/view/Community/Testing/DockerTesting"));

        // Checks whether the link is opening in a new tab or not.
        assertEquals("_blank", page.getButtonParentTarget("testbtn1"));
        assertEquals("", page.getButtonParentTarget("testbtn2"));
        assertEquals("", page.getButtonParentTarget("testbtn3"));
        assertEquals("", page.getButtonParentTarget("testbtn4"));
        assertEquals("_blank", page.getButtonParentTarget("testbtn5"));

        // The type of the button (DEFAULT/ DANGER/ SUCCESS / WARNING).
        assertTrue(page.getButtonClass("testbtn1").endsWith("-default"));
        assertTrue(page.getButtonClass("testbtn2").endsWith("-danger"));
        assertTrue(page.getButtonClass("testbtn3").endsWith("-success"));
        assertTrue(page.getButtonClass("testbtn4").endsWith("-warning"));
        assertTrue(page.getButtonClass("testbtn5").endsWith("-default"));

        // The button has/ doesn't have an Icon assigned.
        assertTrue(page.hasIcon("testbtn1"));
        assertFalse(page.hasIcon("testbtn2"));
        assertTrue(page.hasIcon("testbtn3"));
        assertFalse(page.hasIcon("testbtn4"));
        assertTrue(page.hasIcon("testbtn5"));
    }

    @Test
    @Order(3)
    void statusMacroTest(TestUtils setup, TestReference testReference)
    {
        setup.createPage(testReference, createContent("status-macros.vm"),"StatusTest");

        StatusMacroPage page = new StatusMacroPage();

        // There should be 3 status macros.
        assertEquals(3, page.getStatusCount());

        // Checks the titles of the status macros.
        assertEquals("test1", page.getStatusTitle(0));
        assertEquals("test2", page.getStatusTitle(1));
        assertEquals("Title with double quotes: ?<>@!$%^&*(){}: |; ' , ./` in it .          .", page.getStatusTitle(2));

        // Checks the type of the status macros/ the color.
        assertTrue(page.hasColor(0,"grey"));
        assertTrue(page.hasColor(1,"yellow"));
        assertTrue(page.hasColor(2,"yellow"));

        // Checks the subtle property.
        assertFalse(page.isSubtle(0));
        assertTrue(page.isSubtle(1));
        assertFalse(page.isSubtle(2));
    }

    @Test
    @Order(4)
    void tagListMacroTest(TestUtils setup)
    {
        createPagesWithTags(setup);
        setup.gotoPage("XWiki", "TagListTest");
        setup.createPage(pageWithTagListMacros, createContent("taglist-macros.vm"));

        TagListMacroPage page = new TagListMacroPage();

        // There should be 3 tagList macros.
        assertEquals(3, page.getTagListCount());

        // Checks the ordered titles of the tags from the tagList.
        List<String> expectedTitles1 = Arrays.asList("A-B", "G");
        assertEquals(expectedTitles1, page.getGlossaryTitles(0));

        // Checks the ordered titles of the tags from the tagList.
        List<String> expectedTitles2 = Arrays.asList("A-G");
        assertEquals(expectedTitles2, page.getGlossaryTitles(1));

        // Checks the ordered names of the tags from the tagList.
        List<String> expectedTagNames1 = Arrays.asList("alpha", "beta", "gamma");
        assertEquals(expectedTagNames1, page.getTagNames(0));

        // Checks the ordered names of the tags from the tagList.
        List<String> expectedTagNames2 = Arrays.asList("alpha", "gamma");
        assertEquals(expectedTagNames2, page.getTagNames(1));

        // Checks the <a> tags of the tags from the tagList.
        for (String i : expectedTagNames1) {
            assertEquals("a", page.getHtmlTagForTagName(0, i));
        }

        // Checks the <a> tags of the tags from the tagList.
        for (String i : expectedTagNames2) {
            assertEquals("a", page.getHtmlTagForTagName(1, i));
        }

        // Checks the ordered titles of the tags from the tagList, multiple spaces.
        List<String> expectedTitles3 = Arrays.asList("A-X", "Y-Z");
        assertEquals(expectedTitles3, page.getGlossaryTitles(2));

        // Checks the ordered names of the tags from the tagList.
        List<String> expectedTagNames3 = Arrays.asList("alpha", "beta", "testTag", "x", "y", "z");
        assertEquals(expectedTagNames3, page.getTagNames(2));

        // Checks the <a> tags of the tags from the tagList.
        for (String i : expectedTagNames3) {
            assertEquals("a", page.getHtmlTagForTagName(2, i));
        }
    }

    @Test
    @Order(5)
    void userProfileMacroTest(TestUtils setup,TestReference testReference)
    {
        setup.createPage(testReference, createContent("userprofile-macros.vm"),"UserProfileTest");

        UserProfileMacroPage page = new UserProfileMacroPage();

        // There should be 3 userProfile macros.
        assertEquals(3, page.getUserProfileCount());

        // Checks the links of the avatar pictures for each user.
        assertTrue(page.linkImageProfile(0, "UserTest"));
        assertTrue(page.linkImageProfile(1, "UserTest2"));

        // Checks the titles of the avatar pictures for each user.
        assertTrue(page.imageHasTitle(0, "UserTest"));
        assertTrue(page.imageHasTitle(1, "UserTest2"));

        // Checks the validity of the profile link for each user.
        assertTrue(page.getProfileLinkHref(0, "UserTest"));
        assertTrue(page.getProfileLinkHref(1, "UserTest2"));

        // Checks the link for the profile.
        assertEquals("UserTest", page.getProfileLinkText(0));
        assertEquals("UserTest2", page.getProfileLinkText(1));

        // Checks the number of properties shown for each user.
        assertEquals(4, page.getPropertiesCount(0));
        assertEquals(4, page.getPropertiesCount(1));
        assertEquals(3, page.getPropertiesCount(2));

        // Checks the list of properties shown for each user.
        assertEquals(List.of("xwiki", "usertest@example.com", "07777777", "userTestAddress"),
            page.getPropertiesText(0));
        assertEquals(List.of("xwiki", "usertest2@example.com", "07777777", "userTestAddress2"),
            page.getPropertiesText(1));
        assertEquals(List.of("https://example.com/", "usertest3@example.com", "https://example.com/"),
            page.getPropertiesText(2));

        // Checks that the email link is valid.
        assertTrue(page.isEmailLinkCorrect(0, 1, "usertest@example.com"));
        assertTrue(page.isEmailLinkCorrect(1, 1, "usertest2@example.com"));
        assertTrue(page.isEmailLinkCorrect(1, 1, "usertest2@example.com"));

        // Checks the text from the "about" section for each user.
        assertEquals("test", page.getProfileComment(0));
        assertEquals("test2", page.getProfileComment(1));
        assertEquals("test3", page.getProfileComment(2));
    }

    @Test
    @Order(6)
    void userListMacroTest(TestUtils setup,TestReference testReference)
    {
        setup.createPage(testReference, createContent("userlist-macros.vm"),"UserListTest");

        UserListMacroPage page = new UserListMacroPage();

        // There should be 2 userList macros.
        assertEquals(2, page.getUserListsCount());

        // Checks the number of users in a list.
        assertEquals(2, page.getUserCountInList(0));
        for (int i = 0; i < 2; i++) {
            assertEquals(4, page.getUserPropertiesCount(0, i));
            assertEquals(List.of("avatar", "username", "phone", "email"), page.getUserPropertyTypes(0, i));
        }

        // Checks the avatar titles.
        assertEquals("UserTest", page.getUserAvatarTitle(0, 0));
        assertEquals("UserTest", page.getUserAvatarAlt(0, 0));

        // Checks the validity of the users' profile links.
        assertTrue(page.getUserLinkHref(0, 0, "UserTest"));

        // Checks the link, that has the correct username.
        assertEquals("UserTest", page.getUsernameLinkText(0, 0));

        // Checks the list of properties shown for each user.
        assertEquals(List.of("", "UserTest", "07777777", "usertest@example.com"), page.getUserPropertiesText(0, 0));

        // Checks that the email link is valid.
        assertTrue(page.isEmailLinkValid(0, 0, "usertest@example.com"));

        assertEquals("UserTest2", page.getUserAvatarTitle(0, 1));
        assertEquals("UserTest2", page.getUserAvatarAlt(0, 1));
        assertTrue(page.getUserLinkHref(0, 1, "UserTest2"));
        assertEquals("UserTest2", page.getUsernameLinkText(0, 1));
        assertEquals(List.of("", "UserTest2", "07777777", "usertest2@example.com"), page.getUserPropertiesText(0, 1));
        assertTrue(page.isEmailLinkValid(0, 1, "usertest2@example.com"));

        assertEquals(3, page.getUserCountInList(1));
        for (int i = 0; i < 3; i++) {
            assertEquals(6, page.getUserPropertiesCount(1, i));
            assertEquals(List.of("avatar", "username", "phone", "email", "address", "blogfeed"),
                page.getUserPropertyTypes(1,
                    i));
        }

        // Checks that fixedTableLayout="true" works.
        assertTrue(page.hasFixedLayout(1));

        // Check the users in the list.
        assertEquals(Set.of("UserTest", "UserTest2"), page.getUsernamesFromList(0));
        assertEquals(Set.of("UserTest", "UserTest2", "UserTest3"), page.getUsernamesFromList(1));
    }

    @Test
    @Order(7)
    void panelMacroTest(TestUtils setup,TestReference testReference)
    {
        setup.createPage(testReference, createContent("panel-macros.vm"),"PanelTest");

        PanelMacroPage page = new PanelMacroPage();

        // There should be 3 panel macros on the page.
        assertEquals(3, page.getPanelCount());

        // Panel container 1st panel.
        assertEquals("300px", page.getPanelWidth(0));
        assertEquals("50%", page.getPanelHeight(0));
        assertEquals("20px", page.getPanelBorderRadius(0));
        assertEquals("2px dashed " + hexToRgb("#f536f5"), page.getPanelBorderStyle(0));

        // Title section 1st panel.
        assertEquals("PanelTestTitle", page.getTitleText(0));
        assertEquals(hexToRgb("#452fd4"), page.getTitleBackgroundColor(0));
        assertEquals(hexToRgb("#74d927"), page.getTitleColor(0));

        // Content section 1st panel.
        assertEquals("Content for PanelMacroTest\nContent2 for PanelMacroTest", page.getContentText(0));
        assertEquals(hexToRgb("#edfa34"), page.getContentBackgroundColor(0));
        assertEquals("red", page.getContentColor(0));

        // Footer section 1st panel.
        assertEquals("PanelTestFooter", page.getFooterText(0));
        assertEquals(hexToRgb("#ac4de8"), page.getFooterBackgroundColor(0));
        assertEquals(hexToRgb("#6beded"), page.getFooterColor(0));

        // Nested Panels.

        // Panel container 2nd panel.
        assertEquals("solid " + hexToRgb("#f536f5"), page.getPanelBorderStyle(1));
        assertNull(page.getPanelWidth(1));
        assertNull(page.getPanelHeight(1));
        assertNull(page.getPanelBorderRadius(1));

        // Title section 2nd panel.
        assertEquals("NestedPanelsTestTitle", page.getTitleText(1));
        assertNull(page.getTitleBackgroundColor(1));
        assertNull(page.getTitleColor(1));

        // Content section 2nd panel.
        assertEquals("Content3 for PanelMacroTest\nNestedPanelsTestTitle2\nContent4 for PanelMacroTest",
            page.getContentText(1));
        assertNull(page.getContentBackgroundColor(1));
        assertNull(page.getContentColor(1));

        // Checks the CSS class of the 3rd panel.
        assertTrue(page.getPanelClass(2).contains("macro-panel"));
        assertTrue(page.getPanelClass(2).contains("testCssClass"));

        // Panel container 3rd panel.
        assertEquals("groove rgb(153, 0, 153)", page.getPanelBorderStyle(2));
        assertNull(page.getPanelWidth(2));
        assertNull(page.getPanelHeight(2));
        assertNull(page.getPanelBorderRadius(2));

        // Title section 3rd panel.
        assertEquals("NestedPanelsTestTitle2", page.getTitleText(2));
        assertNull(page.getTitleBackgroundColor(2));
        assertNull(page.getTitleColor(2));

        // Content section 3rd panel.
        assertEquals("Content4 for PanelMacroTest", page.getContentText(2));
        assertNull(page.getContentBackgroundColor(2));
        assertNull(page.getContentColor(2));
    }

    @Test
    @Order(8)
    void microsoftStreamMacroTest(TestUtils setup,TestReference testReference)
    {
        setup.createPage(testReference, createContent("microsoftstream-macros.vm"),"MicrosoftStreamTest");

        MicrosoftStreamMacroPage page = new MicrosoftStreamMacroPage();

        // There should be 3 MicrosoftStream macros.
        assertEquals(3, page.getMstreamCount());

        // Checks the alignment of the macro.
        assertEquals("right", page.getAlignment(0));

        // Checks the width of the macro, default = "500px".
        assertEquals("600px", page.getIframeWidth(0));

        // Checks the height of the macro, default = "300px".
        assertEquals("400px", page.getIframeHeight(0));

        // Checks the autoplay property, default = "false".
        assertTrue(page.hasAutoplay(0));
        // Checks the showInfo property, default = "false".
        assertTrue(page.hasShowInfo(0));
        // Checks the StartTime property existence.
        assertTrue(page.hasStartTime(0));
        // Checks the StartTime property.
        assertEquals(convertStartTime("01:12:13"), page.getStartTime(0));
        // Checks the actual URL of the microsoftStream.
        assertTrue(page.hasCorrectURL(0, "www.youtube.com"));

        assertEquals("left", page.getAlignment(1));
        assertEquals("500px", page.getIframeWidth(1));
        assertEquals("300px", page.getIframeHeight(1));
        assertFalse(page.hasAutoplay(1));
        assertFalse(page.hasShowInfo(1));
        assertTrue(page.hasStartTime(1));
        assertEquals(convertStartTime("03:12:13"), page.getStartTime(1));
        assertTrue(page.hasCorrectURL(1, "www.youtube.com"));

        assertEquals("center", page.getAlignment(2));
        assertEquals("500px", page.getIframeWidth(2));
        assertEquals("300px", page.getIframeHeight(2));
        assertFalse(page.hasAutoplay(2));
        assertFalse(page.hasShowInfo(2));
        assertFalse(page.hasStartTime(2));
        assertTrue(page.hasCorrectURL(2, "www.youtube.com"));
    }

    // Function to convert color codes from hex to rgb.
    private String hexToRgb(String hex)
    {
        hex = hex.substring(1);
        if (hex.length() == 3) {
            hex = "" + hex.charAt(0) + hex.charAt(0)
                + hex.charAt(1) + hex.charAt(1)
                + hex.charAt(2) + hex.charAt(2);
        }

        int r = Integer.parseInt(hex.substring(0, 2), 16);
        int g = Integer.parseInt(hex.substring(2, 4), 16);
        int b = Integer.parseInt(hex.substring(4, 6), 16);

        return String.format("rgb(%d, %d, %d)", r, g, b);
    }

    // Function to convert HH:MM:SS to seconds.
    public int convertStartTime(String time)
    {
        String[] parts = time.split(":");
        int h = Integer.parseInt(parts[0]);
        int m = Integer.parseInt(parts[1]);
        int s = Integer.parseInt(parts[2]);
        return h * 3600 + m * 60 + s;
    }
}
