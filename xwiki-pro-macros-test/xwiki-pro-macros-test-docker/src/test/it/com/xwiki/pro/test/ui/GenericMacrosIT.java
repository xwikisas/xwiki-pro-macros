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
import org.xwiki.test.docker.junit5.ExtensionOverride;
import org.xwiki.test.docker.junit5.TestReference;
import org.xwiki.test.docker.junit5.UITest;
import org.xwiki.test.ui.TestUtils;

import com.xwiki.pro.test.po.generic.ExpandMacro;
import com.xwiki.pro.test.po.generic.ExpandMacroPage;
import com.xwiki.pro.test.po.generic.ProfilePictureMacro;
import com.xwiki.pro.test.po.generic.ProfilePictureMacroPage;
import com.xwiki.pro.test.po.generic.RegisterMacro;

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
@UITest(properties = { "xwikiCfgPlugins=com.xpn.xwiki.plugin.tag.TagPlugin", }, extensionOverrides = {
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

        // Nested Expand macros - Checks the titles of the macros.
        assertEquals("ExpandTest1", expand1.getTitle());
        assertEquals("ExpandTest2", expand2.getTitle());

        // Checking the icon.
        assertTrue(expand1.hasIcon());
        assertTrue(expand2.hasIcon());

        List<String> expectedContent = Arrays.asList("test0\ntest1", "test2");
        // Expanded = true.
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

        List<String> expectedContent2 = Arrays.asList("test0\ntest1");
        // Expanded = false.
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

        assertEquals("UserTest4", picture1.getUserTitle());
        assertEquals("UserTest2", picture2.getUserTitle());

        assertTrue(picture1.linkContainsUsername("UserTest4"));
        assertTrue(picture2.linkContainsUsername("UserTest2"));

        assertEquals("60px", picture1.getAvatarSize());
        assertEquals("100px", picture2.getAvatarSize());

        assertTrue(picture1.hasProfileImage());
        assertFalse(picture2.hasProfileImage());

        assertFalse(picture1.hasAvatarInitials());
        assertTrue(picture2.hasAvatarInitials());
    }
}
