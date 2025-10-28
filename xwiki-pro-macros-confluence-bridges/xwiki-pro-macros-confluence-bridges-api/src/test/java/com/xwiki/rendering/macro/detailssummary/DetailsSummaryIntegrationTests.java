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
package com.xwiki.rendering.macro.detailssummary;

import java.io.InputStream;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.inject.Provider;

import org.apache.solr.common.SolrDocument;
import org.mockito.stubbing.Answer;
import org.xwiki.component.util.DefaultParameterizedType;
import org.xwiki.localization.ContextualLocalizationManager;
import org.xwiki.model.EntityType;
import org.xwiki.model.reference.DocumentReference;
import org.xwiki.model.reference.EntityReference;
import org.xwiki.model.reference.EntityReferenceResolver;
import org.xwiki.observation.EventListener;
import org.xwiki.query.QueryManager;
import org.xwiki.refactoring.internal.ModelBridge;
import org.xwiki.refactoring.internal.ReferenceUpdater;
import org.xwiki.rendering.block.XDOM;
import org.xwiki.rendering.macro.wikibridge.WikiMacroFactory;
import org.xwiki.rendering.parser.Parser;
import org.xwiki.rendering.test.integration.RenderingTestSuite;
import org.xwiki.rendering.test.integration.junit5.RenderingTests;
import org.xwiki.search.solr.Solr;
import org.xwiki.security.authorization.AuthorizationManager;
import org.xwiki.security.authorization.ContextualAuthorizationManager;
import org.xwiki.security.authorization.Right;
import org.xwiki.security.internal.XWikiBridge;
import org.xwiki.template.TemplateManager;
import org.xwiki.test.TestEnvironment;
import org.xwiki.test.annotation.AllComponents;
import org.xwiki.test.mockito.MockitoComponentManager;
import org.xwiki.wiki.descriptor.WikiDescriptorManager;

import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.doc.XWikiDocument;
import com.xwiki.licensing.LicensedExtensionManager;
import com.xwiki.macros.confluence.internal.cql.CQLUtils;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@AllComponents
/**
 * Where the tests are located.
 */
@RenderingTestSuite.Scope(value = "macros.detailssummary")

public class DetailsSummaryIntegrationTests implements RenderingTests
{
    private XWikiContext xcontext;

    private ContextualAuthorizationManager authorizationManager;

    private EntityReferenceResolver<String> resolver;

    private Parser parser;

    @Initialized
    public void initialize(MockitoComponentManager componentManager) throws Exception
    {
        componentManager.registerComponent(TestEnvironment.class);
        componentManager.registerMockComponent(TemplateManager.class);
        componentManager.registerMockComponent(ModelBridge.class);
        componentManager.registerMockComponent(QueryManager.class);
        componentManager.registerMockComponent(ReferenceUpdater.class);
        componentManager.registerMockComponent(AuthorizationManager.class);
        componentManager.registerMockComponent(XWikiBridge.class);
        componentManager.registerMockComponent(WikiMacroFactory.class);

        // Don't need the LicensingScheduler in the test
        componentManager.registerMockComponent(LicensedExtensionManager.class);
        componentManager.registerMockComponent(EventListener.class, "LicensingSchedulerListener");
        // Don't need the actual solr
        componentManager.registerMockComponent(Solr.class, "embedded");
        // Mock the translation and register them
        ContextualLocalizationManager localizationManager =
            componentManager.registerMockComponent(ContextualLocalizationManager.class);
        mockTranslations(localizationManager);
        // Mock the reference resolver
        this.resolver = componentManager.registerMockComponent(
            new DefaultParameterizedType(null, EntityReferenceResolver.class, String.class));
        // Mock the contextualAuthorization so we can give view rights to only certain documents.
        this.authorizationManager = componentManager.registerMockComponent(ContextualAuthorizationManager.class);
        // Get the xwiki syntax parser
        this.parser = componentManager.getInstance(Parser.class, "xwiki/2.1");

        // XWiki Context Mock
        Provider<XWikiContext> mockXWikiContextProvider = componentManager.registerMockComponent(
            new DefaultParameterizedType(null, Provider.class, XWikiContext.class));
        // We want a deep mock so we can mock call chains.
        this.xcontext = mock(XWikiContext.class, RETURNS_DEEP_STUBS);
        // Since there is no real xwiki instance behind the test we fabricate the response with an actual date
        // formatter.
        when(xcontext.getWiki().formatDate(any(), any(), any())).thenAnswer((Answer<String>) invocation -> {
            Date firstArg = invocation.getArgument(0);
            DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
            return dateFormat.format(firstArg);
        });
        DocumentReference documentReference = new DocumentReference("wiki", "space", "page");
        XWikiDocument document = new XWikiDocument(documentReference);
        xcontext.setDoc(document);
        when(mockXWikiContextProvider.get()).thenReturn(xcontext);
        WikiDescriptorManager wikiDescriptorManager =
            componentManager.registerMockComponent(WikiDescriptorManager.class);
        when(wikiDescriptorManager.getCurrentWikiId()).thenReturn("wiki");
        // Mock de CQLQuery
        CQLUtils cqlUtils = componentManager.registerMockComponent(CQLUtils.class);

        // When there are no labels, just return an empty list of documents.
        when(cqlUtils.buildAndExecute(argThat(map -> map != null && map.get("label").equals("")))).thenReturn(
            List.of());
        // When we have the norights label we create a list with a document that has no viewing rights
        SolrDocument noViewingRightsSolrDoc =
            mockFullDoc(componentManager, "", "test", "afarcasi", 1009889257L, List.of("1", "2"), false);

        when(cqlUtils.buildAndExecute(argThat(map -> map != null && map.get("label").equals("norights")))).thenReturn(
            List.of(noViewingRightsSolrDoc));
        // When we have the normal label we create a list with documents that we have viewing rights over.
        SolrDocument normalDocumentWithViewRights =
            mockFullDoc(componentManager, "macros/detailssummary/content/singledetailmacro", "test2", "Jane Doe",
                1019889257L, List.of("test2", "normal"), true);
        when(cqlUtils.buildAndExecute(argThat(map -> map != null && map.get("label").equals("normal")))).thenReturn(
            List.of(normalDocumentWithViewRights));
        // When we have the multipledetailsonthesamepage label we create a list with a document that has multiple
        // details calls.
        SolrDocument multimpleDetailsCallsOnTheSamePage =
            mockFullDoc(componentManager, "macros/detailssummary/content/multipledetailmacrosonapage", "manga",
                "Mary Doe", 1119889257L, List.of("multiple"), true);
        when(cqlUtils.buildAndExecute(
            argThat(map -> map != null && map.get("label").equals("multipledetailsonthesamepage")))).thenReturn(
            List.of(multimpleDetailsCallsOnTheSamePage));
        // When we have the multi2 label we aggregate data from multiple documents.
        SolrDocument missingColumns =
            mockFullDoc(componentManager, "macros/detailssummary/content/missingcolumns", "manga-missing", "Toriko",
                2119889257L, List.of("multiple"), true);
        when(cqlUtils.buildAndExecute(argThat(map -> map != null && map.get("label").equals("multi2")))).thenReturn(
            List.of(normalDocumentWithViewRights, multimpleDetailsCallsOnTheSamePage, missingColumns));
    }

    private SolrDocument mockFullDoc(MockitoComponentManager componentManager, String contentFileName,
        String documentName, String author, long date, List<String> tags, boolean canView) throws Exception
    {
        SolrDocument solrDocument = new SolrDocument();

        String docFullname = String.format("wiki:%s", documentName);
        solrDocument.put("wiki", "wiki");
        solrDocument.put("fullname", documentName);
        solrDocument.put("date", new Date(date));
        String authorFullName = String.format("XWiki.%s", author);
        solrDocument.put("creator", authorFullName);

        EntityReference userRef = mockReference(authorFullName, "XWiki", author);
        // Mock the tags
        List<String> tagList = new ArrayList<>();
        tagList.addAll(tags);
        solrDocument.put("property.XWiki.TagClass.tags_string", tagList);
        mockReference("Main.Tags", "Main", "Tags");

        // Mock the document reference
        EntityReference entityReference = mockReference(docFullname, "test", documentName);

        when(authorizationManager.hasAccess(Right.VIEW, entityReference)).thenReturn(canView);
        // Deep stubs so we can mock method chains
        XWikiDocument mockedDoc = mock(XWikiDocument.class, RETURNS_DEEP_STUBS);
        when(xcontext.getWiki().getDocument(entityReference, xcontext)).thenReturn(mockedDoc);
        // We parse some xwiki syntax into a dom so we don't have to mock the blocks ourselves since that will be a pain.
        XDOM dom = parseFile(contentFileName);
        when(mockedDoc.getXDOM()).thenReturn(dom);
        when(mockedDoc.getSyntax().toIdString()).thenReturn("xwiki/2.1");
        return solrDocument;
    }

    private EntityReference mockReference(String docfullname, String space, String name)
    {
        EntityReference reference = new DocumentReference("wiki", space, name);
        when(resolver.resolve(docfullname, EntityType.DOCUMENT)).thenReturn(reference);
        when(resolver.resolve(eq(docfullname), eq(EntityType.DOCUMENT), any())).thenReturn(reference);
        return reference;
    }

    private XDOM parseFile(String fileName) throws Exception
    {
        if (fileName.isEmpty()) {
            return new XDOM(List.of());
        }

        InputStream inputStream = DetailsSummaryIntegrationTests.class.getClassLoader().getResourceAsStream(fileName);
        XDOM xdom = parser.parse(new StringReader(new String(inputStream.readAllBytes(), StandardCharsets.UTF_8)));
        return xdom;
    }

    private void mockTranslations(ContextualLocalizationManager localizationManager)
    {
        when(localizationManager.getTranslationPlain("rendering.macro.detailssummary.noresults")).thenReturn(
            "No rows found");
        when(localizationManager.getTranslationPlain("rendering.macro.detailssummary.firstcolumn")).thenReturn(
            "Document");
        when(localizationManager.getTranslationPlain("rendering.macro.detailssummary.tags")).thenReturn("tags");
        when(localizationManager.getTranslationPlain("rendering.macro.detailssummary.creator")).thenReturn("author");
        when(localizationManager.getTranslationPlain("rendering.macro.detailssummary.lastModified")).thenReturn(
            "lastModified");
    }
}
