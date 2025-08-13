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

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.Map;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.xwiki.model.reference.DocumentReference;
import org.xwiki.tag.test.po.AddTagsPane;
import org.xwiki.tag.test.po.TaggablePage;
import org.xwiki.test.docker.junit5.ExtensionOverride;
import org.xwiki.test.docker.junit5.UITest;
import org.xwiki.test.docker.junit5.servletengine.ServletEngine;
import org.xwiki.test.ui.TestUtils;
import org.xwiki.test.ui.po.ViewPage;
import org.xwiki.test.ui.po.editor.WikiEditPage;

import com.xwiki.pro.test.po.confluence.detailssummary.DetailsSummaryMacroViewPage;
import com.xwiki.pro.test.po.utils.SolrTestUtils;

import static org.junit.jupiter.api.Assertions.assertEquals;


/**
 * UI tests for the generic detailsSummary macros.
 *
 * @version $Id$
 * @since 1.27.2
 */
@UITest(
    servletEngine = ServletEngine.TOMCAT, forbiddenEngines = {
    // These tests need to have XWiki running inside a Docker container (we chose Tomcat since it's the most
    // used one), because they need LibreOffice to be installed, and we cannot guarantee that it is installed on the
    // host machine.
    ServletEngine.JETTY_STANDALONE },
    properties = {
        "xwikiCfgPlugins=com.xpn.xwiki.plugin.tag.TagPlugin",
    },
    extensionOverrides = {
        @ExtensionOverride(
            extensionId = "com.google.code.findbugs:jsr305",
            overrides = {
                "features=com.google.code.findbugs:annotations"
            }
        )
    })
public class DetailsSummaryIT
{
    @BeforeAll
    void setup(TestUtils setup)
    {
        setup.loginAsSuperAdmin();
    }

    void createPage(TestUtils setup, String tag, String pageName, String content)
    {
        DocumentReference documentReference = new DocumentReference("xwiki", "detailsSummary", pageName);
        // The content can be too big
        ViewPage viewPage = setup.createPage(documentReference, "");
        WikiEditPage wikiEditPage = viewPage.editWiki();
        wikiEditPage.setContent(content);
        wikiEditPage.clickSaveAndView();
        if (!tag.isEmpty()) {
            TaggablePage taggablePage = new TaggablePage();
            AddTagsPane tagsPane = taggablePage.addTags();
            tagsPane.setTags(tag);
            tagsPane.add();
        }
    }

    @Test
    @Order(1)
    void detailsSummaryWithMultipleDetailsMacros(TestUtils setup) throws Exception
    {

        String detailsMacroContent = readTestResourceFile("details/multipleMacrosOnTheSamePage");
        createPage(setup, "test1", "details_with_multiple_calls", detailsMacroContent);
        // Wait for the solr indexing
        SolrTestUtils solrTestUtils = new SolrTestUtils(setup);
        solrTestUtils.waitEmpyQueue();
        String detailsSummaryCall =
            buildMacroCall(Collections.singletonMap("cql", "\"space = " + "currentSpace ( )\""));
        createPage(setup, "", "detailsSummary", detailsSummaryCall);
        DetailsSummaryMacroViewPage detailsSummaryMacroViewPage = new DetailsSummaryMacroViewPage();
        assertEquals(10, detailsSummaryMacroViewPage.entriesCount());
    }

    private String buildMacroCall(Map<String, String> parameters)
    {
        StringBuilder builder = new StringBuilder();
        builder.append("{{confluence_detailssummary ");
        for (String parameter : parameters.keySet()) {
            builder.append(String.format("%s=%s", parameter, parameters.get(parameter)));
        }
        builder.append(" /}}");

        return builder.toString();
    }

    private String readTestResourceFile(String fileName)
    {
        try (InputStream is = getClass().getClassLoader().getResourceAsStream(fileName)) {
            String content = new String(is.readAllBytes(), StandardCharsets.UTF_8);
            return content;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
