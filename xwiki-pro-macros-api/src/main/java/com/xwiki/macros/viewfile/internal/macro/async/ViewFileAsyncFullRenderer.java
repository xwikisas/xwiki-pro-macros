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
package com.xwiki.macros.viewfile.internal.macro.async;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Provider;
import javax.servlet.http.HttpSession;

import org.apache.commons.lang3.StringUtils;
import org.xwiki.component.annotation.Component;
import org.xwiki.component.manager.ComponentManager;
import org.xwiki.model.reference.AttachmentReference;
import org.xwiki.model.reference.EntityReferenceSerializer;
import org.xwiki.rendering.RenderingException;
import org.xwiki.rendering.block.Block;
import org.xwiki.rendering.block.CompositeBlock;
import org.xwiki.rendering.block.GroupBlock;
import org.xwiki.rendering.block.MacroBlock;
import org.xwiki.rendering.listener.reference.ResourceType;
import org.xwiki.rendering.macro.AbstractMacro;
import org.xwiki.rendering.macro.Macro;
import org.xwiki.rendering.macro.office.OfficeMacroParameters;
import org.xwiki.rendering.syntax.Syntax;
import org.xwiki.rendering.transformation.MacroTransformationContext;

import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.web.XWikiRequest;
import com.xwiki.macros.viewfile.internal.macro.ViewFileMacro;
import com.xwiki.macros.viewfile.macro.async.AbstractViewFileAsyncRenderer;
import com.xwiki.pdfviewer.macro.PDFViewerMacroParameters;

import static com.xwiki.macros.viewfile.internal.macro.ViewFileMacroPrepareBlocks.CLASS;
import static com.xwiki.macros.viewfile.internal.macro.ViewFileMacroPrepareBlocks.OFFICE_FILE_EXTENSIONS;
import static com.xwiki.macros.viewfile.internal.macro.ViewFileMacroPrepareBlocks.PRESENTATION_FILE_EXTENSIONS;
import static com.xwiki.macros.viewfile.internal.macro.ViewFileMacroPrepareBlocks.STYLE;

/**
 * Async renderer for generating the full view display {@link Block} for {@link ViewFileMacro}.
 *
 * @version $Id$
 * @since 1.29.0
 */
@Component(roles = AbstractViewFileAsyncRenderer.class)
@Named(ViewFileAsyncFullRenderer.HINT)
public class ViewFileAsyncFullRenderer extends AbstractViewFileAsyncRenderer
{
    /**
     * Component hint.
     */
    public static final String HINT = "fullview";

    private static final String PX = "px";

    private static final String DEFAULT_WIDTH = "100%";

    private static final String DEFAULT_HEIGHT = "1000";

    private static final String HEIGHT_KEY = "height";

    private static final String WIDTH_KEY = "width";

    private static final String OFFICE_HINT = "office";

    private static final String PDFVIEW_HINT = "pdfviewer";

    @Inject
    private EntityReferenceSerializer<String> referenceSerializer;

    private AttachmentReference attachmentReference;

    private String width;

    private String height;

    private String fileExtension;

    private MacroTransformationContext transformationContext;

    private Syntax targetSyntax;

    private List<String> id;

    private XWikiRequest wikiRequest;

    private HttpSession session;

    @Inject
    private ComponentManager componentManager;

    @Inject
    private Provider<XWikiContext> wikiContextProvider;

    @Override
    public void initialize(MacroTransformationContext context, AttachmentReference attachmentReference,
        Map<String, String> parameters)
    {
        XWikiContext wikiContext = this.wikiContextProvider.get();
        this.wikiRequest = wikiContext.getRequest();
        this.session = this.wikiRequest.getSession();
        this.height = parameters.getOrDefault(HEIGHT_KEY, DEFAULT_HEIGHT);
        this.width = parameters.getOrDefault(WIDTH_KEY, DEFAULT_WIDTH);
        this.fileExtension = parameters.get("fileExtension");
        this.transformationContext = context;
        this.targetSyntax = context.getTransformationContext().getTargetSyntax();
        this.attachmentReference = attachmentReference;
        id =
            createId("rendering", "macro", "viewfile", HINT, String.valueOf(attachmentReference.toString().hashCode()));
    }

    @Override
    public boolean isInline()
    {
        return false;
    }

    @Override
    public Syntax getTargetSyntax()
    {
        return this.targetSyntax;
    }

    @Override
    public List<String> getId()
    {
        return this.id;
    }

    @Override
    public boolean isAsyncAllowed()
    {
        return true;
    }

    @Override
    public boolean isCacheAllowed()
    {
        return true;
    }

    @Override
    protected Block execute(boolean async, boolean cached) throws RenderingException
    {
        try {
            List<Block> result = prepareFullDisplay();
            return new CompositeBlock(result);
        } catch (Exception e) {
            throw new RenderingException("Failed to render asynchronously the work items displayer.", e);
        }
    }

    private List<Block> prepareFullDisplay() throws Exception
    {
        if (OFFICE_FILE_EXTENSIONS.contains(fileExtension)) {
            return prepareOfficeFile();
        } else {
            return preparePDF();
        }
    }

    private List<Block> prepareOfficeFile() throws Exception
    {
        AbstractMacro<OfficeMacroParameters> displayerMacro = componentManager.getInstance(Macro.class, OFFICE_HINT);
        OfficeMacroParameters macroParameters = new OfficeMacroParameters();
        OfficeMacroParameters.OfficeResourceReference resourceReference =
            new OfficeMacroParameters.OfficeResourceReference(referenceSerializer.serialize(attachmentReference),
                ResourceType.ATTACHMENT);
        macroParameters.setReference(resourceReference);

        MacroBlock officeMacroBlock = new MacroBlock(OFFICE_HINT,
            Collections.singletonMap("reference", referenceSerializer.serialize(attachmentReference)), "", false);
        transformationContext.setCurrentMacroBlock(officeMacroBlock);
        /*
            TODO: Discuss if it is the best solution:
             The office macro checks if a file is already saved in the cache, using the
             DefaultTemporaryAttachmentSessionsManager which checks inside the session. In the thread, we are missing
              a session/SessionManager as there is no request. We can wrap the original thread request and session to
               avoid a NullPointerException in DefaultTemporaryAttachmentSessionsManager:
               https://github.com/xwiki/xwiki-platform/blob/xwiki-platform-14.10/xwiki-platform-core
               /xwiki-platform-store/xwiki-platform-store-filesystem-oldcore/src/main/java/org/xwiki/store/filesystem
               /internal/DefaultTemporaryAttachmentSessionsManager.java#L77-L78
        */
        this.wikiContextProvider.get().setRequest(new AsyncRequest(wikiRequest, session));
        List<Block> officeMacroResult = displayerMacro.execute(macroParameters, "", transformationContext);

        String processedWidth = processDimensionsUnit(this.width, DEFAULT_WIDTH, true);
        String processedHeight = processDimensionsUnit(this.height, DEFAULT_HEIGHT + PX, true);
        String style = String.format("width:%s; height:%s; overflow:auto", processedWidth, processedHeight);
        String elementClass = "viewFileFull " + (PRESENTATION_FILE_EXTENSIONS.contains(fileExtension) ? "box" : "");
        Block groupBlock = new GroupBlock(officeMacroResult, Map.of(CLASS, elementClass, STYLE, style));
        return List.of(groupBlock);
    }

    private List<Block> preparePDF() throws Exception
    {
        String processedWidth = processDimensionsUnit(this.width, DEFAULT_WIDTH, false);
        String processedHeight = processDimensionsUnit(this.height, DEFAULT_HEIGHT, false);

        AbstractMacro<PDFViewerMacroParameters> displayerMacro =
            componentManager.getInstance(Macro.class, PDFVIEW_HINT);
        PDFViewerMacroParameters macroParameters = new PDFViewerMacroParameters();
        macroParameters.setFile(referenceSerializer.serialize(attachmentReference));
        macroParameters.setHeight(Integer.parseInt(processedHeight));
        macroParameters.setWidth(processedWidth);

        MacroBlock pdfMacroBlock = new MacroBlock(PDFVIEW_HINT,
            Map.of("file", referenceSerializer.serialize(attachmentReference), HEIGHT_KEY, processedHeight, WIDTH_KEY,
                processedWidth), "", false);
        transformationContext.setCurrentMacroBlock(pdfMacroBlock);
        return displayerMacro.execute(macroParameters, "", transformationContext);
    }

    /**
     * Sadly, the office and pdfviewer are not using compatible units, and we have to make sure that we properly process
     * them.
     */
    private String processDimensionsUnit(String value, String defaultValue, boolean addUnit)
    {
        if (StringUtils.isNotBlank(value)) {
            if (addUnit) {
                boolean hasUnit = value.endsWith("%") || value.endsWith(PX);
                return hasUnit ? value : value + PX;
            } else {
                return value.endsWith(PX) ? StringUtils.removeEnd(value, PX) : value;
            }
        }
        return defaultValue;
    }
}
