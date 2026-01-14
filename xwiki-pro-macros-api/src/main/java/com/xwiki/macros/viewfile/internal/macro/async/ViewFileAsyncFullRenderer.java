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

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Provider;
import javax.servlet.http.HttpSession;

import com.xpn.xwiki.XWikiException;
import com.xpn.xwiki.doc.XWikiAttachment;
import com.xpn.xwiki.doc.XWikiAttachmentContent;
import com.xpn.xwiki.doc.XWikiDocument;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
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
import org.xwiki.rendering.block.TableCellBlock;
import org.xwiki.rendering.block.TableHeadCellBlock;
import org.xwiki.rendering.block.TableRowBlock;
import org.xwiki.rendering.block.TableBlock;
import org.xwiki.rendering.listener.reference.ResourceType;
import org.xwiki.rendering.macro.AbstractMacro;
import org.xwiki.rendering.macro.Macro;
import org.xwiki.rendering.macro.office.OfficeMacroParameters;
import org.xwiki.rendering.parser.ParseException;
import org.xwiki.rendering.parser.Parser;
import org.xwiki.rendering.syntax.Syntax;
import org.xwiki.rendering.transformation.MacroTransformationContext;

import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.web.XWikiRequest;
import com.xwiki.macros.viewfile.internal.macro.ViewFileMacro;
import com.xwiki.macros.viewfile.macro.async.AbstractViewFileAsyncRenderer;
import com.xwiki.pdfviewer.macro.PDFViewerMacroParameters;

import static com.xwiki.macros.viewfile.internal.macro.ViewFileMacroPrepareBlocks.CLASS;
import static com.xwiki.macros.viewfile.internal.macro.ViewFileMacroPrepareBlocks.CSV_FILE_EXTENSIONS;
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

    private boolean csvFirstLineIsHeader = true;

    private String csvDelimiter;

    private String csvFormat;

    @Inject
    private ComponentManager componentManager;

    @Inject
    private Provider<XWikiContext> wikiContextProvider;

    @Inject
    @Named("plain/1.0")
    private Parser plainTextParser;

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
        this.csvFirstLineIsHeader = Boolean.parseBoolean(parameters.getOrDefault("csvFirstLineIsHeader", "true"));
        this.csvDelimiter = parameters.get("csvDelimiter");
        this.csvFormat = parameters.get("csvFormat");
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
        }

        if (CSV_FILE_EXTENSIONS.contains(fileExtension)) {
            return prepareCSV();
        }

        if ("pdf".equals(fileExtension)) {
            return preparePDF();
        }

        return List.of();
    }

    private List<Block> prepareCSV() throws IOException, XWikiException, ParseException
    {
        CSVFormat format = CSVFormat.DEFAULT;
        if (StringUtils.isNotEmpty(this.csvFormat)) {
            format = CSVFormat.valueOf(this.csvFormat);
        }

        CSVFormat.Builder builder = format.builder().setTrim(true);
        if (StringUtils.isNotEmpty(this.csvDelimiter)) {
            builder = builder.setDelimiter(this.csvDelimiter);
        } else if ("tsv".equals(this.fileExtension)) {
            builder = builder.setDelimiter("\t");
        }

        if (this.csvFirstLineIsHeader) {
            builder.setHeader().setSkipHeaderRecord(true);
        }
        XWikiContext context = wikiContextProvider.get();
        XWikiDocument attachDoc = context.getWiki().getDocument(attachmentReference.getDocumentReference(), context);
        XWikiAttachment attachment = attachDoc.getAttachment(attachmentReference.getName());
        XWikiAttachmentContent attachmentContent = attachment.getAttachmentContent(context);
        InputStream attachmentContentIS = attachmentContent.getContentInputStream();
        CSVParser parser = builder.build().parse(new InputStreamReader(attachmentContentIS));
        List<String> headerNames = parser.getHeaderNames();
        List<Block> rows = new ArrayList<>();
        if (!headerNames.isEmpty()) {
            List<Block> headers = new ArrayList<>(headerNames.size());
            for (String header : headerNames) {
                headers.add(new TableHeadCellBlock(plainTextParser.parse(new StringReader(header)).getChildren()));
            }
            rows.add(new TableRowBlock(headers));
        }

        for (CSVRecord rec : parser) {
            List<Block> cells = new ArrayList<>(rec.size());
            for (String val : rec) {
                cells.add(new TableCellBlock(plainTextParser.parse(new StringReader(val)).getChildren()));
            }

            rows.add(new TableRowBlock(cells));
        }
        return wrapWithFullViewFormat(List.of(new TableBlock(rows)));
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
        // The office macro checks if a file is already saved in the cache, using the
        // DefaultTemporaryAttachmentSessionsManager which checks inside the session. In the thread, we are missing
        // a session/SessionManager as there is no request, so we wrap the original thread request and session to
        // avoid a NullPointerException in DefaultTemporaryAttachmentSessionsManager. To be removed once XWiki parent
        // version is >= 17.4.1.
        this.wikiContextProvider.get().setRequest(new AsyncRequest(wikiRequest, session));
        List<Block> officeMacroResult = displayerMacro.execute(macroParameters, "", transformationContext);
        this.wikiContextProvider.get().setRequest(wikiRequest);
        return wrapWithFullViewFormat(officeMacroResult);
    }

    private List<Block> wrapWithFullViewFormat(List<Block> wrapped)
    {
        String processedWidth = processDimensionsUnit(this.width, DEFAULT_WIDTH, true);
        String processedHeight = processDimensionsUnit(this.height, DEFAULT_HEIGHT + PX, true);
        String style = String.format("width:%s; height:%s; overflow:auto", processedWidth, processedHeight);
        String elementClass = "viewFileFull " + (PRESENTATION_FILE_EXTENSIONS.contains(fileExtension) ? "box" : "");
        Block groupBlock = new GroupBlock(wrapped, Map.of(CLASS, elementClass, STYLE, style));
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
