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
package com.xwiki.macros.viewfile.internal.macro;

import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import javax.script.ScriptContext;

import org.apache.commons.lang.exception.ExceptionUtils;
import org.xwiki.component.annotation.Component;
import org.xwiki.rendering.block.Block;
import org.xwiki.rendering.macro.MacroExecutionException;
import org.xwiki.rendering.transformation.MacroTransformationContext;
import org.xwiki.script.ScriptContextManager;
import org.xwiki.template.Template;
import org.xwiki.template.TemplateManager;

import com.xwiki.macros.AbstractProMacro;
import com.xwiki.macros.viewfile.macro.ViewFileMacroParameters;

/**
 * View File macro: Display a file in different rendering ways.
 *
 * @version $Id$
 * @since 1.27
 */
@Component
@Named("view-file")
@Singleton
public class ViewFileMacro extends AbstractProMacro<ViewFileMacroParameters>
{
    @Inject
    private TemplateManager templateManager;

    @Inject
    private ScriptContextManager scriptContextManager;

    /**
     * Create and initialize the descriptor of the macro.
     */
    public ViewFileMacro()
    {
        super("View file", "Show a file using PDF Viewer Macro or Office Viewer.", ViewFileMacroParameters.class);
    }

    @Override
    public boolean supportsInlineMode()
    {
        return true;
    }

    @Override
    protected List<Block> internalExecute(ViewFileMacroParameters parameters, String content,
        MacroTransformationContext context) throws MacroExecutionException
    {
        Template customTemplate = this.templateManager.getTemplate("viewfile/viewFileTemplate.vm");
        ScriptContext scriptContext = scriptContextManager.getScriptContext();

        scriptContext.setAttribute("params", parameters, ScriptContext.ENGINE_SCOPE);
        scriptContext.setAttribute("isInline", context.isInline(), ScriptContext.ENGINE_SCOPE);
        try {
            return this.templateManager.execute(customTemplate).getChildren();
        } catch (Exception e) {
            throw new MacroExecutionException(ExceptionUtils.getRootCauseMessage(e));
        }
    }
}
