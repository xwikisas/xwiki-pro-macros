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
package com.xwiki.macros.internal;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.xwiki.rendering.macro.wikibridge.WikiMacroParameters;
import org.xwiki.component.manager.ComponentLookupException;
import org.xwiki.component.manager.ComponentManager;
import org.xwiki.rendering.block.Block;
import org.xwiki.rendering.block.MacroBlock;
import org.xwiki.rendering.macro.Macro;
import org.xwiki.rendering.macro.MacroExecutionException;
import org.xwiki.rendering.macro.MacroId;
import org.xwiki.rendering.macro.descriptor.ContentDescriptor;
import org.xwiki.rendering.macro.descriptor.MacroDescriptor;
import org.xwiki.rendering.macro.descriptor.ParameterDescriptor;
import org.xwiki.rendering.transformation.MacroTransformationContext;

/**
 * Base class for old, legacy unprefixed confluence bridge macros.
 * @param <P> macro parameters
 */
abstract class AbstractUnprefixedConfluenceBridgeMacro<P> implements Macro<P>
{
    protected static final int PRIORITY = 900;

    @Inject
    private ComponentManager componentManager;

    @Inject
    private Logger logger;

    protected abstract String getId();

    protected String getConfluenceBridgeId()
    {
        return "confluence_" + getId();
    }
    /**
     * @return the priority
     */
    public int getPriority()
    {
        return PRIORITY;
    }

    @Override
    public MacroDescriptor getDescriptor()
    {
        Macro<P> macro = getMacro();

        final MacroDescriptor descriptor = macro == null ? null : macro.getDescriptor();

        final String id = getId();

        return new UnprefixedConfluenceBridgeMacroDescriptor(id, descriptor);
    }

    private Macro<P> getMacro()
    {
        Macro<P> macro;
        String confluenceBridgeId = getConfluenceBridgeId();
        try {
            macro = componentManager.getInstance(Macro.class, confluenceBridgeId);
        } catch (ComponentLookupException e) {
            logger.error("Could not find macro [{}]", confluenceBridgeId, e);
            return null;
        }
        return macro;
    }

    @Override
    public boolean supportsInlineMode()
    {
        Macro<P> macro = getMacro();
        if (macro == null) {
            return false;
        }
        return macro.supportsInlineMode();
    }

    @Override
    public List<Block> execute(P parameters, String content, MacroTransformationContext context)
        throws MacroExecutionException
    {
        Macro<P> macro = getMacro();
        if (macro == null) {
            return Collections.singletonList(new MacroBlock(
                "error",
                Collections.emptyMap(),
         "Could not find the Confluence bridge [" + getConfluenceBridgeId() + "].",
                context.isInline()
            ));
        }
        return macro.execute(parameters, content, context);
    }

    @Override
    public int compareTo(Macro<?> macro)
    {
        return this.getPriority() - macro.getPriority();
    }

    private static class UnprefixedConfluenceBridgeMacroDescriptor implements MacroDescriptor
    {
        private static final String DEPRECATED = "Deprecated";

        private static final String LEGACY_UNPREFIXED = "Legacy unprefixed ";

        private static final Set<String> DEFAULT_CATEGORIES = Collections.singleton(DEPRECATED);

        private final String id;

        private final MacroDescriptor descriptor;

        UnprefixedConfluenceBridgeMacroDescriptor(String id, MacroDescriptor descriptor)
        {
            this.id = id;
            this.descriptor = descriptor;
        }

        @Override
        public MacroId getId()
        {
            return new MacroId(id);
        }

        @Override
        public String getName()
        {
            return LEGACY_UNPREFIXED + (descriptor == null ? getFallbackName() : descriptor.getName());
        }

        private String getFallbackName()
        {
            return "Confluence bridge for " + id;
        }

        @Override
        public String getDescription()
        {
            return "Legacy Unprefixed " + (
                descriptor == null
                    ? (getFallbackName() + " (failed to get the description of this macro)")
                    : descriptor.getDescription());
        }

        @Override
        public Class<?> getParametersBeanClass()
        {
            return descriptor == null ? WikiMacroParameters.class : descriptor.getParametersBeanClass();
        }

        @Override
        public ContentDescriptor getContentDescriptor()
        {
            return descriptor == null ? null : descriptor.getContentDescriptor();
        }

        @Override
        public Map<String, ParameterDescriptor> getParameterDescriptorMap()
        {
            return descriptor == null ? Collections.emptyMap() : descriptor.getParameterDescriptorMap();
        }

        @Override
        public String getDefaultCategory()
        {
            return DEPRECATED;
        }

        /**
         * @return the default categories.
         * NOTE: please use @Override when moving to 14.10+ parent.
         */
        public Set<String> getDefaultCategories()
        {
            return DEFAULT_CATEGORIES;
        }
    }
}
