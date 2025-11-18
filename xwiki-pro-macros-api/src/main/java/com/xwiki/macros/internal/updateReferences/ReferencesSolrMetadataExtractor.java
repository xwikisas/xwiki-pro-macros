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
package com.xwiki.macros.internal.updateReferences;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Provider;
import javax.inject.Singleton;

import org.apache.solr.common.SolrInputDocument;
import org.slf4j.Logger;
import org.xwiki.component.annotation.Component;
import org.xwiki.model.reference.DocumentReference;
import org.xwiki.model.reference.DocumentReferenceResolver;
import org.xwiki.model.validation.EntityNameValidationConfiguration;
import org.xwiki.model.validation.EntityNameValidationManager;
import org.xwiki.rendering.block.Block;
import org.xwiki.rendering.block.XDOM;
import org.xwiki.rendering.block.match.MacroBlockMatcher;
import org.xwiki.search.solr.SolrEntityMetadataExtractor;

import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.XWikiException;
import com.xpn.xwiki.doc.XWikiDocument;

/**
 * Handles the backlinks of the reference parameters of macros. Ensure that the parameters that are references always
 * have a valid reference even if the page is renamed or moved.
 *
 * @version $Id$
 * @since 1.29
 */
@Component
@Named("macrosReferencesUpdate")
@Singleton
public class ReferencesSolrMetadataExtractor implements SolrEntityMetadataExtractor<XWikiDocument>
{
    private static final Map<String, List<String>> MACROS_TO_SEARCH = Map.of(
        "button", List.of("url"),
        "confluence_pagetree", List.of("root")
    );

    @Inject
    @Named("explicit")
    private DocumentReferenceResolver<String> explicitDocumentReferenceResolver;

    @Inject
    private LinkRegistry linkRegistry;

    @Inject
    private Provider<XWikiContext> contextProvider;

    @Inject
    private EntityNameValidationManager entityNameValidationManager;

    @Inject
    private EntityNameValidationConfiguration entityNameValidationConfiguration;

    @Inject
    private Logger logger;

    @Override
    public boolean extract(XWikiDocument document, SolrInputDocument solrDocument)
    {
        XDOM xdom = document.getXDOM();
        boolean linksRegistered = false;

        for (Map.Entry<String, List<String>> macroToSearch : MACROS_TO_SEARCH.entrySet()) {
            String macroName = macroToSearch.getKey();
            List<String> parameters = macroToSearch.getValue();
            List<Block> macroBlocks = xdom.getBlocks(new MacroBlockMatcher(macroName), Block.Axes.DESCENDANT);

            if (macroBlocks != null && !macroBlocks.isEmpty() && !updateMacroReference(document, xdom,
                macroName, macroBlocks, parameters))
            {
                List<DocumentReference> macroReferences = new ArrayList<>();

                for (Block macroBlock : macroBlocks) {
                    for (String parameter : parameters) {
                        DocumentReference macroReference =
                            explicitDocumentReferenceResolver.resolve(macroBlock.getParameter(parameter),
                                document.getDocumentReference());
                        macroReferences.add(macroReference);
                    }
                }

                linksRegistered = linkRegistry.registerBacklinks(solrDocument, macroReferences);
            }
        }

        return linksRegistered;
    }

    /**
     * Checks and updates the references from macro parameters of all the macro calls to make sure that all of them
     * respect the current name strategy.
     *
     * @param document document with all the macro calls
     * @param xdom of the @document
     * @param macroName the name of the macro
     * @param macroBlocks list of all the button macro calls
     * @param parameters the parameters of the macro that are references
     * @return rue if any reference was invalid and has been updated, false if there weren't any invalid references or
     *     if an error occurred while updating the document
     */
    private boolean updateMacroReference(XWikiDocument document, XDOM xdom, String macroName, List<Block> macroBlocks,
        List<String> parameters)
    {
        try {
            XWikiContext context = contextProvider.get();
            boolean modified = false;

            for (Block macroBlock : macroBlocks) {
                for (String parameter : parameters) {
                    String referenceFromMacroName =
                        macroBlock.getParameter(parameter) != null ? macroBlock.getParameter(parameter) : macroName;
                    // For backwards compatibility we check if the page already exists so we won't modify it.
                    DocumentReference referenceFromMacro =
                        explicitDocumentReferenceResolver.resolve(referenceFromMacroName,
                            document.getDocumentReference());
                    boolean documentExists = context.getWiki().exists(referenceFromMacro, context);
                    if (!documentExists) {
                        // First we check if the name is valid in the current naming strategy.
                        boolean isValid = this.isValid(referenceFromMacroName);
                        // If the name is valid then we can use it, otherwise we transform the name in a valid
                        // one and update the macro block.
                        if (!isValid) {
                            String transformedName = this.transformName(referenceFromMacroName);
                            logger.debug("The reference [{}] was updated to [{}] to respect the current name strategy. "
                                    + "Document: [{}]", referenceFromMacroName, transformedName,
                                document.getDocumentReference());
                            macroBlock.setParameter(parameter, transformedName);
                            modified = true;
                        }
                    }
                }
            }

            if (modified) {
                document.setContent(xdom);
                context.getWiki()
                    .saveDocument(document, "Updated button macro references to respect the name strategy.", context);

                return modified;
            }
        } catch (XWikiException e) {
            logger.error("Failed to update button macro references of [{}] to respect the naming strategy.", document,
                e);
            return false;
        }
        return false;
    }

    private String transformName(String name)
    {
        // this.entityNameValidationConfiguration.useTransformation() is a property that MUST be set by the user in the
        // Administration -> Editing -> Name Strategies -> transform names automatically, if the property is disabled
        // the code will always return the original name and not the transformed one.
        if (this.entityNameValidationConfiguration.useTransformation()
            && this.entityNameValidationManager.getEntityReferenceNameStrategy() != null)
        {
            return this.entityNameValidationManager.getEntityReferenceNameStrategy().transform(name);
        } else {
            return name;
        }
    }

    private boolean isValid(String name)
    {
        // this.entityNameValidationConfiguration.useValidation() is a property that MUST be set by the user in the
        // Administration -> Editing -> Name Strategies -> validate names before saving, if the property is disabled
        // this code will always return false.
        if (this.entityNameValidationConfiguration.useValidation()
            && this.entityNameValidationManager.getEntityReferenceNameStrategy() != null)
        {
            return this.entityNameValidationManager.getEntityReferenceNameStrategy().isValid(name);
        } else {
            return true;
        }
    }
}
