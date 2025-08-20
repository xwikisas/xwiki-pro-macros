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
import java.time.LocalTime;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
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

import com.xwiki.pro.test.po.generic.ButtonMacro;
import com.xwiki.pro.test.po.generic.MicrosoftStreamMacro;
import com.xwiki.pro.test.po.generic.MicrosoftStreamMacroPage;
import com.xwiki.pro.test.po.generic.PanelMacro;
import com.xwiki.pro.test.po.generic.PanelMacroPage;
import com.xwiki.pro.test.po.generic.RegisterMacro;
import com.xwiki.pro.test.po.generic.StatusMacro;
import com.xwiki.pro.test.po.generic.StatusMacroPage;
import com.xwiki.pro.test.po.generic.TagListMacro;
import com.xwiki.pro.test.po.generic.TagListMacroPage;
import com.xwiki.pro.test.po.generic.TeamMacro;
import com.xwiki.pro.test.po.generic.TeamMacroPage;
import com.xwiki.pro.test.po.generic.UserListItem;
import com.xwiki.pro.test.po.generic.UserListMacro;
import com.xwiki.pro.test.po.generic.UserListMacroPage;
import com.xwiki.pro.test.po.generic.UserProfileMacro;
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

    private String createContent(String filename)
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

    @Test
    @Order(1)
    void teamMacroTest(TestUtils setup, TestReference testReference)
    {
        setup.gotoPage("XWiki", "UserTest");
        TaggablePage taggablePage = new TaggablePage();
        AddTagsPane tagsPane = taggablePage.addTags();
        tagsPane.setTags("testTag");
        tagsPane.add();
        setup.createPage(testReference, createContent("team-macros.vm"), "TeamTest");
        TeamMacroPage page = new TeamMacroPage();

        TeamMacro team1 = page.getMacro(0);
        TeamMacro team2 = page.getMacro(1);
        TeamMacro team3 = page.getMacro(2);

        // There should be 3 team macros.
        assertEquals(3, page.getMacroCount());

        // The first team macro should display 3 users.
        assertEquals(3, team1.getUsers().size());
        String username = "xwiki:XWiki.UserTest";
        String username2 = "xwiki:XWiki.UserTest2";
        String username3 = "xwiki:XWiki.UserTest3";

        // Checks the user titles.
        assertEquals("UserTest", team1.getUserTitle(username));
        assertEquals("UserTest2", team1.getUserTitle(username2));
        assertEquals("UserTest3", team1.getUserTitle(username3));

        // Checks the users' profile links.
        assertTrue(team1.getProfileLink(username).endsWith("/xwiki/bin/view/XWiki/UserTest"));
        assertTrue(team1.getProfileLink(username2).endsWith("/xwiki/bin/view/XWiki/UserTest2"));
        assertTrue(team1.getProfileLink(username3).endsWith("/xwiki/bin/view/XWiki/UserTest3"));

        // Checks avatar attributes: initials, backgroundColor, fontColor, size, borderRadius.
        assertEquals("U", team1.getAvatarInitials(username));
        assertEquals("U", team1.getAvatarInitials(username2));
        assertEquals("U", team1.getAvatarInitials(username3));

        // Checks the default color.
        assertTrue(team1.getAvatarBackgroundColor(username).contains("rgb(0, 170, 102)"));
        assertTrue(team1.getAvatarBackgroundColor(username2).contains("rgb(0, 170, 102)"));
        assertTrue(team1.getAvatarBackgroundColor(username3).contains("rgb(0, 170, 102)"));

        // Checks the personalized color.
        assertEquals("rgb(204, 0, 255)", team1.getAvatarFontColor(username));
        assertEquals("rgb(204, 0, 255)", team1.getAvatarFontColor(username2));
        assertEquals("rgb(204, 0, 255)", team1.getAvatarFontColor(username3));

        assertTrue(team1.getAvatarSize(username).startsWith("60"));
        assertTrue(team1.getAvatarSize(username2).startsWith("60"));
        assertTrue(team1.getAvatarSize(username3).startsWith("60"));

        assertTrue(team1.getAvatarBorderRadius(username).startsWith("60"));
        assertTrue(team1.getAvatarBorderRadius(username2).startsWith("60"));
        assertTrue(team1.getAvatarBorderRadius(username3).startsWith("60"));

        // Checks the visibility of usernames.
        assertFalse(team1.isUsernameVisible(username));
        assertFalse(team1.isUsernameVisible(username2));
        assertFalse(team1.isUsernameVisible(username3));

        // Second team macro should display 1 user - the one with "testTag".
        assertEquals(1, team2.getUsers().size());

        assertEquals("UserTest", team2.getUserTitle(username));
        assertTrue(team2.getProfileLink(username).endsWith("/xwiki/bin/view/XWiki/UserTest"));
        // Checks that disableLetterAvatars="true" works.
        assertTrue(team2.getAvatarSize(username).startsWith("60"));

        // Checks that showUsernames="true" works.
        assertTrue(team2.isUsernameVisible(username));

        // Third team macro should display 0 users - none exist with tag "nonExistentTag".
        assertEquals(0, team3.getUsers().size());
        // Checks that if a team macro is empty (0 users), the message "There is nobody to show." appears.
        assertTrue(team3.hasEmptyTeamMessage());
    }

    @Test
    @Order(2)
    void buttonMacroTest(TestUtils setup, TestReference testReference)
    {
        setup.createPage(testReference, createContent("button-macros.vm"), "ButtonTest");

        ButtonMacro btn1 = new ButtonMacro("testbtn1");
        ButtonMacro btn2 = new ButtonMacro("testbtn2");
        ButtonMacro btn3 = new ButtonMacro("testbtn3");
        ButtonMacro btn4 = new ButtonMacro("testbtn4");
        ButtonMacro btn5 = new ButtonMacro("testbtn5");

        String link = "https://example.org/";

        // Checks the 1st button, with a personalized width, newTab="true" and an associated icon.
        assertEquals("100px", btn1.getWidth());
        assertEquals(Color.fromString("#ff66ff").asRgb(), btn1.getColor());
        assertEquals("test1", btn1.getLabel());
        assertTrue(btn1.hasLink(link));
        // Checks whether the link is opening in a new tab or not.
        assertEquals("_blank", btn1.getParentTarget());
        assertTrue(btn1.getCssClass().endsWith("-default"));
        assertTrue(btn1.hasIcon());

        // Checks the 2nd button, with type = "DANGER".
        assertEquals("test2", btn2.getLabel());
        assertTrue(btn2.hasLink(link));
        assertEquals("", btn2.getParentTarget());
        assertTrue(btn2.getCssClass().endsWith("-danger"));
        assertFalse(btn2.hasIcon());

        // Checks the 3rd button, with a personalized width, type="SUCCESS", newTab="false" and an associated icon.
        assertEquals("80px", btn3.getWidth());
        assertEquals(Color.fromString("#ff66ff").asRgb(), btn3.getColor());
        assertEquals("test3", btn3.getLabel());
        assertTrue(btn3.hasLink(link));
        assertEquals("", btn3.getParentTarget());
        assertTrue(btn3.getCssClass().endsWith("-success"));
        assertTrue(btn3.hasIcon());

        // Checks the 4th button with type="WARNING" and newTab="false".
        assertEquals(Color.fromString("#ff66ff").asRgb(), btn4.getColor());
        assertEquals("test4", btn4.getLabel());
        assertTrue(btn4.hasLink(link));
        assertEquals("", btn4.getParentTarget());
        assertTrue(btn4.getCssClass().endsWith("-warning"));
        assertFalse(btn4.hasIcon());

        // Tests for btn5 with newTab="false" and an associated icon.
        assertEquals("test5", btn5.getLabel());
        assertTrue(btn5.hasLink(link));
        assertEquals("", btn5.getParentTarget());
        assertTrue(btn5.getCssClass().endsWith("-default"));
        assertFalse(btn5.hasIcon());
    }

    @Test
    @Order(3)
    void statusMacroTest(TestUtils setup, TestReference testReference)
    {
        setup.createPage(testReference, createContent("status-macros.vm"), "StatusTest");

        StatusMacroPage page = new StatusMacroPage();

        // There should be 3 status macros.
        assertEquals(3, page.getMacroCount());

        StatusMacro status1 = page.getMacro(0);
        StatusMacro status2 = page.getMacro(1);
        StatusMacro status3 = page.getMacro(2);

        // Checks the 1st status macro, with default properties.
        assertEquals("test1", status1.getTitle());
        assertTrue(status1.hasColor("grey"));
        assertFalse(status1.isSubtle());

        // Checks the 2nd status macro, type Yellow and subtle="true".
        assertEquals("test2", status2.getTitle());
        assertTrue(status2.hasColor("yellow"));
        assertTrue(status2.isSubtle());

        // Checks the 3rd status macro, with special characters in the title, type Yellow and subtle="true".
        assertEquals("Title with double quotes: ?<>@!$%^&*(){}: |; ' , ./` in it .          .", status3.getTitle());
        assertTrue(status3.hasColor("yellow"));
        assertFalse(status3.isSubtle());
    }

    @Test
    @Order(4)
    void tagListMacroTest(TestUtils setup)
    {
        createPagesWithTags(setup);
        setup.createPage(pageWithTagListMacros, createContent("taglist-macros.vm"));

        TagListMacroPage page = new TagListMacroPage();
        TagListMacro tagList1 = page.getMacro(0);
        TagListMacro tagList2 = page.getMacro(1);
        TagListMacro tagList3 = page.getMacro(2);

        // There should be 3 tagList macros.
        assertEquals(3, page.getMacroCount());

        // Checks the 1st tagList macro, which should contain all the tags.
        List<String> expectedTitles1 = Arrays.asList("A-B", "G");
        assertEquals(expectedTitles1, tagList1.getGlossaryTitles());

        List<String> expectedTagNames1 = Arrays.asList("alpha", "beta", "gamma");
        assertEquals(expectedTagNames1, tagList1.getTagNames());

        // Checks the links associated with the tags.
        for (String i : expectedTagNames1) {
            assertTrue(tagList1.hasLink(i));
        }

        // Checks the 2nd tagList macro, which excludes the "beta" tag.
        List<String> expectedTitles2 = Arrays.asList("A-G");
        assertEquals(expectedTitles2, tagList2.getGlossaryTitles());
        List<String> expectedTagNames2 = Arrays.asList("alpha", "gamma");
        assertEquals(expectedTagNames2, tagList2.getTagNames());
        for (String i : expectedTagNames2) {
            assertTrue(tagList2.hasLink(i));
        }

        // Checks the 3rd tagList macro, which targets specific spaces and excludes the "gamma" tag.
        List<String> expectedTitles3 = Arrays.asList("A-X", "Y-Z");
        assertEquals(expectedTitles3, tagList3.getGlossaryTitles());

        List<String> expectedTagNames3 = Arrays.asList("alpha", "beta", "testTag", "x", "y", "z");
        assertEquals(expectedTagNames3, tagList3.getTagNames());

        for (String i : expectedTagNames3) {
            assertTrue(tagList3.hasLink(i));
        }
    }

    @Test
    @Order(5)
    void userProfileMacroTest(TestUtils setup, TestReference testReference)
    {
        setup.createPage(testReference, createContent("userprofile-macros.vm"), "UserProfileTest");

        UserProfileMacroPage page = new UserProfileMacroPage();

        UserProfileMacro user1 = page.getMacro(0);
        UserProfileMacro user2 = page.getMacro(1);
        UserProfileMacro user3 = page.getMacro(2);

        // There should be 3 userProfile macros.
        assertEquals(3, page.getMacroCount());

        // Checks the 1st userProfile macro, with the properties: company, email, phone, address.
        assertTrue(user1.hasAvatarWithLink("UserTest"));
        assertTrue(user1.hasProfileLink("UserTest"));

        // Checks the number of properties shown for each user.
        assertEquals(4, user1.getPropertiesList().size());

        // Checks the list of properties shown for each user.
        assertEquals(List.of("xwiki", "usertest@example.com", "07777777", "userTestAddress"), user1.getProperties());

        // Checks that the email link is valid.
        assertTrue(user1.isEmailLinkCorrect(1, "usertest@example.com"));

        // Checks the text from the "about" section for each user.
        assertEquals("test", user1.getComment());

        // Checks the 2nd userProfile macro, with the default properties.
        assertTrue(user2.hasAvatarWithLink("UserTest2"));
        assertTrue(user2.hasProfileLink("UserTest2"));

        assertEquals(4, user2.getPropertiesList().size());
        assertEquals(List.of("xwiki", "usertest2@example.com", "07777777", "userTestAddress2"), user2.getProperties());

        assertTrue(user2.isEmailLinkCorrect(1, "usertest2@example.com"));

        assertEquals("test2", user2.getComment());

        // Checks the 3rd userProfile macro, with the properties: blogfeed, email, blog.
        assertEquals(3, user3.getPropertiesList().size());
        assertEquals(List.of("https://example.com/", "usertest3@example.com", "https://example.com/"),
            user3.getProperties());

        assertTrue(user3.isEmailLinkCorrect(1, "usertest3@example.com"));

        assertEquals("test3", user3.getComment());
    }

    @Test
    @Order(6)
    void userListMacroTest(TestUtils setup, TestReference testReference)
    {
        setup.createPage(testReference, createContent("userlist-macros.vm"), "UserListTest");

        UserListMacroPage page = new UserListMacroPage();
        UserListMacro userList1 = page.getMacro(0);
        UserListMacro userList2 = page.getMacro(1);

        // There should be 2 userList macros.
        assertEquals(2, page.getMacroCount());

        // Checks the 1st userList macro, which should contain 2 users and show specific properties for each.
        List<UserListItem> users1 = userList1.getUsers();
        assertEquals(2, users1.size());

        for (UserListItem u : users1) {
            assertEquals(4, u.getProperties().size());
            assertEquals(List.of("avatar", "username", "phone", "email"), u.getPropertyTypes());
        }

        UserListItem u1 = users1.get(0);
        UserListItem u2 = users1.get(1);

        assertEquals("UserTest", u1.getUsername());
        assertEquals("UserTest2", u2.getUsername());

        assertEquals("UserTest", u1.getAvatarTitle());
        assertEquals("UserTest2", u2.getAvatarTitle());
        assertEquals("UserTest", u1.getAvatarAlt());
        assertEquals("UserTest2", u2.getAvatarAlt());

        assertEquals("07777777", u1.getPhone());
        assertEquals("07777777", u2.getPhone());

        assertEquals("usertest@example.com", u1.getEmail());
        assertEquals("usertest2@example.com", u2.getEmail());

        assertTrue(u1.isEmailLinkValid("usertest@example.com"));
        assertTrue(u2.isEmailLinkValid("usertest2@example.com"));

        assertTrue(u1.hasProfileLink("UserTest"));
        assertTrue(u2.hasProfileLink("UserTest2"));

        assertEquals(Set.of("UserTest", "UserTest2"), userList1.getUsernames());

        // Checks the 2nd userList macro, which should contain 3 users and show specific properties for each.
        List<UserListItem> users2 = userList2.getUsers();
        assertEquals(3, users2.size());
        assertTrue(userList2.hasFixedLayout());

        for (UserListItem u : users2) {
            assertEquals(6, u.getProperties().size());
            assertEquals(List.of("avatar", "username", "phone", "email", "address", "blogfeed"), u.getPropertyTypes());
        }

        UserListItem u3 = users2.get(2);

        assertEquals("UserTest3", u3.getUsername());
        assertEquals("usertest3@example.com", u3.getEmail());
        assertEquals("userTestAddress3", u3.getAddress());
        assertEquals("https://example.com/", u3.getBlogFeed());

        assertEquals(Set.of("UserTest", "UserTest2", "UserTest3"), userList2.getUsernames());
    }

    @Test
    @Order(7)
    void panelMacroTest(TestUtils setup, TestReference testReference)
    {
        setup.createPage(testReference, createContent("panel-macros.vm"), "PanelTest");

        PanelMacroPage page = new PanelMacroPage();
        PanelMacro panel1 = page.getPanel(0);
        PanelMacro panel2 = page.getPanel(1);
        PanelMacro panel3 = page.getPanel(2);

        // There should be 3 panel macros on the page.
        assertEquals(3, page.getPanelCount());

        // Panel container 1st panel.
        assertEquals("300px", panel1.getWidth());
        assertEquals("50%", panel1.getHeight());
        assertEquals("20px", panel1.getBorderRadius());
        assertEquals("2px dashed " + Color.fromString("#f536f5").asRgb(), panel1.getBorderStyle());
        // Title section 1st panel.
        assertEquals("PanelTestTitle", panel1.getTitleText());
        assertEquals(Color.fromString("#452fd4").asRgb(), panel1.getTitleBackgroundColor());
        assertEquals(Color.fromString("#74d927").asRgb(), panel1.getTitleColor());

        // Content section 1st panel.
        assertEquals("Content for PanelMacroTest\nContent2 for PanelMacroTest", panel1.getContentText());
        assertEquals(Color.fromString("#edfa34").asRgb(), panel1.getContentBackgroundColor());
        assertEquals("red", panel1.getContentColor());

        // Footer section 1st panel.
        assertEquals("PanelTestFooter", panel1.getFooterText());
        assertEquals(Color.fromString("#ac4de8").asRgb(), panel1.getFooterBackgroundColor());
        assertEquals(Color.fromString("#6beded").asRgb(), panel1.getFooterColor());

        // Nested Panels.

        // Panel container 2nd panel.
        assertEquals("solid " + Color.fromString("#f536f5").asRgb(), panel2.getBorderStyle());
        assertNull(panel2.getWidth());
        assertNull(panel2.getHeight());
        assertNull(panel2.getBorderRadius());

        // Title section 2nd panel.
        assertEquals("NestedPanelsTestTitle", panel2.getTitleText());
        assertNull(panel2.getTitleBackgroundColor());
        assertNull(panel2.getTitleColor());

        // Content section 2nd panel.
        assertEquals("Content3 for PanelMacroTest\nNestedPanelsTestTitle2\nContent4 for PanelMacroTest",
            panel2.getContentText());
        assertNull(panel2.getContentBackgroundColor());
        assertNull(panel2.getContentColor());

        // Checks the CSS class of the 3rd panel.
        assertTrue(panel3.getCssClass().contains("testCssClass"));

        // Panel container 3rd panel.
        assertEquals("groove rgb(153, 0, 153)", panel3.getBorderStyle());
        assertNull(panel3.getWidth());
        assertNull(panel3.getHeight());
        assertNull(panel3.getBorderRadius());

        // Title section 3rd panel.
        assertEquals("NestedPanelsTestTitle2", panel3.getTitleText());
        assertNull(panel3.getTitleBackgroundColor());
        assertNull(panel3.getTitleColor());

        // Content section 3rd panel.
        assertEquals("Content4 for PanelMacroTest", panel3.getContentText());
        assertNull(panel3.getContentBackgroundColor());
        assertNull(panel3.getContentColor());
    }

    @Test
    @Order(8)
    void microsoftStreamMacroTest(TestUtils setup, TestReference testReference)
    {
        setup.createPage(testReference, createContent("microsoftstream-macros.vm"), "MicrosoftStreamTest");

        MicrosoftStreamMacroPage page = new MicrosoftStreamMacroPage();

        MicrosoftStreamMacro mStream1 = page.getMacro(0);
        MicrosoftStreamMacro mStream2 = page.getMacro(1);
        MicrosoftStreamMacro mStream3 = page.getMacro(2);

        // There should be 3 MicrosoftStream macros.
        assertEquals(3, page.getMacroCount());

        // Checks the alignment of the macro.
        assertEquals("right", mStream1.getAlignment());

        // Checks the width of the macro, default = "500px".
        assertEquals("600px", mStream1.getWidth());

        // Checks the height of the macro, default = "300px".
        assertEquals("400px", mStream1.getHeight());

        // Checks the autoplay property, default = "false".
        assertTrue(mStream1.hasAutoplay());
        // Checks the showInfo property, default = "false".
        assertTrue(mStream1.hasShowInfo());
        // Checks the StartTime property existence.
        assertTrue(mStream1.hasStartTime());
        // Checks the StartTime property.
        assertEquals(LocalTime.parse("01:12:13").toSecondOfDay(), mStream1.getStartTime());
        // Checks the actual URL of the microsoftStream.
        assertTrue(mStream1.hasCorrectURL("www.stream.com"));

        // Checks the 2nd MicrosoftStream macro, with left alignment, personalized dimensions and set StartTime.
        assertEquals("left", mStream2.getAlignment());
        assertEquals("500px", mStream2.getWidth());
        assertEquals("300px", mStream2.getHeight());
        assertFalse(mStream2.hasAutoplay());
        assertFalse(mStream2.hasShowInfo());
        assertTrue(mStream2.hasStartTime());
        assertEquals(LocalTime.parse("03:12:13").toSecondOfDay(), mStream2.getStartTime());
        assertTrue(mStream2.hasCorrectURL("https://web.microsoftstream.com"));

        // Checks the 2nd MicrosoftStream macro, with center alignment
        assertEquals("center", mStream3.getAlignment());
        assertEquals("500px", mStream3.getWidth());
        assertEquals("300px", mStream3.getHeight());
        assertFalse(mStream3.hasAutoplay());
        assertFalse(mStream3.hasShowInfo());
        assertFalse(mStream3.hasStartTime());
        assertTrue(mStream3.hasCorrectURL("www.stream.com"));
    }
}
