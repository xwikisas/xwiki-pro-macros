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
package com.xwiki.macros.excerptinclude.internal.macro;

import java.io.StringReader;
import java.lang.reflect.ParameterizedType;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Provider;
import javax.inject.Singleton;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.xwiki.component.annotation.Component;
import org.xwiki.component.manager.ComponentLookupException;
import org.xwiki.component.manager.ComponentManager;
import org.xwiki.component.util.DefaultParameterizedType;
import org.xwiki.display.internal.DocumentDisplayer;
import org.xwiki.display.internal.DocumentDisplayerParameters;
import org.xwiki.localization.ContextualLocalizationManager;
import org.xwiki.model.reference.DocumentReference;
import org.xwiki.model.reference.DocumentReferenceResolver;
import org.xwiki.model.reference.EntityReferenceSerializer;
import org.xwiki.rendering.block.Block;
import org.xwiki.rendering.block.MacroBlock;
import org.xwiki.rendering.block.MacroMarkerBlock;
import org.xwiki.rendering.block.MetaDataBlock;
import org.xwiki.rendering.block.ParagraphBlock;
import org.xwiki.rendering.block.XDOM;
import org.xwiki.rendering.block.match.ClassBlockMatcher;
import org.xwiki.rendering.listener.MetaData;
import org.xwiki.rendering.macro.MacroExecutionException;
import org.xwiki.rendering.macro.MacroId;
import org.xwiki.rendering.macro.MacroLookupException;
import org.xwiki.rendering.macro.MacroManager;
import org.xwiki.rendering.macro.descriptor.ContentDescriptor;
import org.xwiki.rendering.parser.ParseException;
import org.xwiki.rendering.parser.Parser;
import org.xwiki.rendering.transformation.MacroTransformationContext;
import org.xwiki.security.authorization.ContextualAuthorizationManager;
import org.xwiki.security.authorization.Right;
import org.xwiki.stability.Unstable;

import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.XWikiException;
import com.xpn.xwiki.doc.XWikiDocument;
import com.xwiki.macros.AbstractProMacro;
import com.xwiki.macros.excerptinclude.macro.ExcerptIncludeMacroParameters;

/**
 * Implements the 'excerpt-include' macro functionality to include excerpts from other documents. This macro serves as a
 * bridge for the Confluence Excerpt Include macro.
 *
 * @version $Id$
 * @since 1.14.5
 */
@Component
@Named(ExcerptIncludeMacro.EXCERPT_INCLUDE)
@Singleton
@Unstable
public class ExcerptIncludeMacro extends AbstractProMacro<ExcerptIncludeMacroParameters>
{
    static final String EXCERPT_INCLUDE = "excerpt-include";
    private static final String NAME = "name";

    @Inject
    private ContextualLocalizationManager localizationManager;

    @Inject
    private ContextualAuthorizationManager contextualAuthorization;

    @Inject
    @Named("content")
    private DocumentDisplayer contentDisplayer;

    @Inject
    private Provider<XWikiContext> contextProvider;

    @Inject
    private MacroManager macroManager;

    @Inject
    private Provider<ComponentManager> cmProvider;

    @Inject
    private Logger logger;

    @Inject
    private EntityReferenceSerializer<String> entityReferenceSerializer;

    @Inject
    private DocumentReferenceResolver<String> documentReferenceResolver;

    /**
     * Constructs an instance of ExcerptIncludeMacro. Initializes the macro descriptor with a specific name,
     * description, and parameter class type.
     */
    public ExcerptIncludeMacro()
    {
        super("Excerpt include",
            "Includes excerpts from other documents into the current page. The included pages need to contain the "
                + "simple `excerpt` macro.",
            ExcerptIncludeMacroParameters.class);
    }

    @Override
    public boolean supportsInlineMode()
    {
        return true;
    }

    @Override
    protected List<Block> internalExecute(ExcerptIncludeMacroParameters parameters, String content,
        MacroTransformationContext context) throws MacroExecutionException
    {
        XWikiDocument document = getReferencedDocument(parameters);
        DocumentReference documentReference = document.getDocumentReference();

        if (recursionDetected(documentReference, parameters.getName(), context))  {
            return Collections.singletonList(getErrorXDOM(
                localizationManager.getTranslationPlain("rendering.macro.excerptinclude.recursiondetected",
                    documentReference), context));
        }

        boolean inline = context.isInline() || parameters.isInline();

        XDOM displayContent = document.isNew()
            ? getErrorXDOM(
                localizationManager.getTranslationPlain("rendering.macro.excerptinclude.referenceexcerptnotfound",
                documentReference), context)
            : getExcerpt(parameters.getName(), document, context, inline);

        return (inline || parameters.isNopanel())
            ? Collections.singletonList(displayContent)
            : renderPanel(document, displayContent, documentReference);
    }

    private boolean recursionDetected(DocumentReference includedDoc, String name, MacroTransformationContext context)
    {
        // Inspired of org.xwiki.rendering.internal.macro.include.IncludeMacro#checkRecursion
        MacroBlock currentMacroBlock = context.getCurrentMacroBlock();
        return recursionDetected(includedDoc, name, currentMacroBlock.getParent());
    }

    private boolean recursionDetected(DocumentReference includedDoc, String name, Block parentBlock)
    {
        if (parentBlock == null) {
            return false;
        }

        if (isSameIncludeExcerpt(includedDoc, name, parentBlock)) {
            return true;
        }

        return recursionDetected(includedDoc, name, parentBlock.getParent());
    }

    private boolean isSameIncludeExcerpt(DocumentReference includedDoc, String name, Block parentBlock)
    {
        if (!(parentBlock instanceof MacroMarkerBlock)) {
            return false;
        }

        MacroMarkerBlock parentMacro = (MacroMarkerBlock) parentBlock;
        if (
            EXCERPT_INCLUDE.equals(parentMacro.getId())
                && StringUtils.defaultString(parentMacro.getParameter(NAME)).equals(name)
        ) {
            String serializedRef = parentMacro.getParameter("0");

            DocumentReference ref = StringUtils.isEmpty(serializedRef)
                ? contextProvider.get().getDoc().getDocumentReference()
                : documentReferenceResolver.resolve(serializedRef);

            return ref.equals(includedDoc);
        }

        return false;
    }

    private XWikiDocument getReferencedDocument(ExcerptIncludeMacroParameters parameters) throws MacroExecutionException
    {
        DocumentReference reference = parameters.getReference();
        XWikiContext xcontext = contextProvider.get();
        try {
            return reference == null ? xcontext.getDoc() : xcontext.getWiki().getDocument(reference, xcontext);
        } catch (XWikiException e) {
            throw new MacroExecutionException(String.format("Failed to get document for reference [%s]", reference), e);
        }
    }

    private List<Block> renderPanel(XWikiDocument document, XDOM displayContent, DocumentReference reference)
    {
        Map<String, String> panelParameters = new HashMap<>(1);
        panelParameters.put("classes", "macro-excerpt-include");
        panelParameters.put("title", document.getTitle());

        MacroBlock panel = new MacroBlock("panel", panelParameters, renderExcerptContent(displayContent), false);
        String source = this.entityReferenceSerializer.serialize(reference);
        MetaData extraMeta = new MetaData();
        extraMeta.addMetaData(MetaData.SOURCE, source);
        extraMeta.addMetaData(MetaData.BASE, source);
        // We wrap the MacroBlock panel with the metadata to correctly render the relative references.
        MetaDataBlock metadataContent = new MetaDataBlock(
            Collections.<Block>singletonList(panel),
            extraMeta
        );

        return Collections.singletonList(metadataContent);
    }

    private String renderExcerptContent(XDOM displayContent)
    {
        try {
            XWikiDocument docForRendering = new XWikiDocument(null);
            docForRendering.setContent(displayContent);
            return docForRendering.getContent();
        } catch (XWikiException e) {
            return "{{error}}\n"
                + localizationManager.getTranslationPlain("rendering.macro.excerptinclude.renderfailure")
                + "\n{{/error}}";
        }
    }

    private XDOM getExcerpt(String name, XWikiDocument document, MacroTransformationContext context, boolean inline)
        throws MacroExecutionException
    {
        DocumentDisplayerParameters displayParameters = new DocumentDisplayerParameters();
        displayParameters.setContentTransformed(true);
        displayParameters.setExecutionContextIsolated(true);
        displayParameters.setTransformationContextIsolated(true);
        XDOM displayContent = getExcerptFromXDOM(name, document.getXDOM(), document, displayParameters, inline);

        if (displayContent == null) {
            if (StringUtils.isEmpty(name)) {
                // If there is no excerpt macro and a name was not provided, display the entire content.
                checkAccess(document.getDocumentReference(), contextProvider.get());
                displayContent = contentDisplayer.display(document, displayParameters);
            } else {
                displayContent = getErrorXDOM(
                    localizationManager.getTranslationPlain("rendering.macro.excerptinclude.namedexcerptnotfound",
                        name, document.getDocumentReference()), context);
            }
        }

        if (inline) {
            // I didn't figure out how to use org.xwiki.rendering.util.ParserUtils#convertToInline
            ClassBlockMatcher matcher = new ClassBlockMatcher(ParagraphBlock.class);
            for (Block p : displayContent.getBlocks(matcher, Block.Axes.DESCENDANT_OR_SELF)) {
                p.getParent().replaceChild(p.getChildren(), p);
            }
        }
        return displayContent;
    }

    private XDOM getExcerptFromXDOM(String name, XDOM xdom, XWikiDocument document,
        DocumentDisplayerParameters displayParameters, boolean inline) throws MacroExecutionException
    {
        List<Block> blocks = xdom.getBlocks(new ClassBlockMatcher(MacroBlock.class), Block.Axes.DESCENDANT);

        for (Block block : blocks) {
            MacroBlock macroBlock = (MacroBlock) block;
            String candidateName = macroBlock.getParameter(NAME);
            if (candidateName == null) {
                candidateName = "";
            }

            XDOM displayContent = ("excerpt".equals(macroBlock.getId()) && StringUtils.equals(candidateName, name))
                ? parseMatchingExcerpt(document, displayParameters, inline, macroBlock)
                : findMatchingExcerpt(name, document, displayParameters, inline, macroBlock);

            if (displayContent != null) {
                return displayContent;
            }
        }

        return null;
    }

    private XDOM findMatchingExcerpt(String name, XWikiDocument document,
        DocumentDisplayerParameters displayParameters, boolean inline, MacroBlock macroBlock)
        throws MacroExecutionException
    {
        // recurse into the macro content, but only if that content is of "WIKI" type
        MacroId macroId = new MacroId(macroBlock.getId(), document.getSyntax());
        try {
            ParameterizedType wikiContentType = new DefaultParameterizedType(null, List.class, Block.class);
            ContentDescriptor contentDescriptor = macroManager.getMacro(macroId).getDescriptor()
                    .getContentDescriptor();
            if (contentDescriptor != null && wikiContentType.equals(contentDescriptor.getType())) {
                ComponentManager cm = cmProvider.get();
                Parser macroContentParser = cm.getInstance(Parser.class, document.getSyntax().toIdString());
                XDOM macroContent = macroContentParser.parse(new StringReader(macroBlock.getContent()));
                return getExcerptFromXDOM(name, macroContent, document, displayParameters, inline);
            }
        } catch (MacroLookupException | NullPointerException e) {
            // someone used an unknown / unsupported macro: ignore
            logger.debug("No macro found for [{}]", macroId, e);
        } catch (ComponentLookupException e) {
            // looks like a serious installation problem
            logger.warn("Failed to find required component to find excerpt in [{}]",
                document.getDocumentReference());
            logger.debug("It is missing because:", e);
        } catch (ParseException e) {
            // this is likely nothing the current user can do something about
            logger.info("Failed to parse content of wiki macro [{}] in document [{}] to look for excerpt",
                    macroBlock.getId(), document.getDocumentReference());
            logger.debug("Reason:", e);
        }
        return null;
    }

    private XDOM parseMatchingExcerpt(XWikiDocument document, DocumentDisplayerParameters displayParameters,
        boolean inline, MacroBlock macroBlock) throws MacroExecutionException
    {
        XDOM displayContent;
        String unprivileged = macroBlock.getParameter("allowUnprivilegedInclude");
        boolean allowUnprivileged = "1".equals(unprivileged) || "true".equalsIgnoreCase(unprivileged);
        if (!allowUnprivileged) {
            checkAccess(document.getDocumentReference(), contextProvider.get());
        }

        // Use a document clone that has only the excerpt macro as content, to be displayed.
        XWikiDocument documentClone = document.clone();
        try {
            MacroBlock mb = macroBlock;
            mb.setParameter("hidden", "false");
            if (inline && !mb.isInline()) {
                mb = new MacroBlock(macroBlock.getId(), mb.getParameters(), mb.getContent(), true);
            }
            documentClone.setContent(new XDOM(Collections.singletonList(mb)));
        } catch (XWikiException e) {
            throw new MacroExecutionException("Could not include the excerpt", e);
        }
        displayContent = contentDisplayer.display(documentClone, displayParameters);
        return displayContent;
    }

    private XDOM getErrorXDOM(String message, MacroTransformationContext context)
    {
        return new XDOM(Collections.singletonList(new MacroBlock(
            "error", Collections.emptyMap(), message, context.isInline())));
    }

    private void checkAccess(DocumentReference reference, XWikiContext xcontext) throws MacroExecutionException
    {
        if (!this.contextualAuthorization.hasAccess(Right.VIEW, reference)) {
            throw new MacroExecutionException(
                String.format("Current user [%s] doesn't have view rights on document [%s]",
                    xcontext.getUserReference(), reference));
        }
    }
}
