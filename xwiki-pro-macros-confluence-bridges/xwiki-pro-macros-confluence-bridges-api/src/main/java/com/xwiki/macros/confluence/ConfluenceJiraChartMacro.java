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
package com.xwiki.macros.confluence;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.inject.Named;
import javax.inject.Singleton;

import org.apache.commons.lang.StringUtils;
import org.xwiki.component.annotation.Component;
import org.xwiki.rendering.block.Block;
import org.xwiki.rendering.block.MacroBlock;
import org.xwiki.rendering.macro.MacroExecutionException;
import org.xwiki.rendering.transformation.MacroTransformationContext;
import org.xwiki.stability.Unstable;

import com.xwiki.macros.AbstractProMacro;

/**
 * The Confluence jira chart bridge macro.
 *
 * @version $Id$
 * @since 1.27.0
 */
@Component
@Named("confluence_jirachart")
@Singleton
@Unstable
public class ConfluenceJiraChartMacro extends AbstractProMacro<ConfluenceJiraChartMacroParameters>
{
    private static final Map<String, String> VERSION_LABEL_MAP = Map.of(
        "all", "ALL",
        "major", "ONLY_MAJOR",
        "none", "NONE"
    );

    private static final String CHART_PARAM_STATUSES = "statuses";

    private static final String CHART_PARAM_ALL_FIXFOR = "allFixfor";

    private static final String CHART_PARAM_ASSIGNEES = "assignees";

    private static final String CHART_PARAM_PRIORITIES = "priorities";

    private static final String CHART_PARAM_COMPONENTS = "components";

    private static final String CHART_PARAM_ISSUETYPE = "issuetype";

    private static final String CHART_PARAM_STATUS = "STATUS";

    private static final String CHART_PARAM_FIX_VERSION = "FIX_VERSION";

    private static final String CHART_PARAM_ASSIGNEE = "ASSIGNEE";

    private static final String CHART_PARAM_PRIORITY = "PRIORITY";

    private static final String CHART_PARAM_COMPONENT = "COMPONENT";

    private static final Map<String, String> PIE_CHART_TYPE_PARAM = Map.of(
        CHART_PARAM_STATUSES, CHART_PARAM_STATUS,
        CHART_PARAM_ALL_FIXFOR, CHART_PARAM_FIX_VERSION,
        CHART_PARAM_ASSIGNEES, CHART_PARAM_ASSIGNEE,
        CHART_PARAM_PRIORITIES, CHART_PARAM_PRIORITY,
        CHART_PARAM_COMPONENTS, CHART_PARAM_COMPONENT,
        CHART_PARAM_ISSUETYPE, "ISSUE_TYPE"
    );

    private static final Map<String, String> BI_DIMENSIONAL_GRID_CHART_AXIS_PARAM = Map.of(
        CHART_PARAM_ASSIGNEES, CHART_PARAM_ASSIGNEE,
        "reporter", "REPORTER",
        CHART_PARAM_STATUSES, CHART_PARAM_STATUS,
        CHART_PARAM_ALL_FIXFOR, CHART_PARAM_FIX_VERSION,
        CHART_PARAM_COMPONENTS, CHART_PARAM_COMPONENT,
        CHART_PARAM_PRIORITIES, CHART_PARAM_PRIORITY,
        CHART_PARAM_ISSUETYPE, "TYPE");

    private static final Pattern JQL_FILTER_PATTERN = Pattern.compile("^filter\\s*=\\s*(\\d+)$");

    private static final String FALSE = "false";

    private static final String TRUE = "true";

    private static final String PIE = "pie";

    private static final String CREATEDVSRESOLVED = "createdvsresolved";

    private static final String TWODIMENSIONAL = "twodimensional";

    /**
     * Constructor.
     */
    public ConfluenceJiraChartMacro()
    {
        super("Confluence Jira chart", "Confluence bridge macro for jira chart.",
            ConfluenceJiraChartMacroParameters.class);
    }

    @Override
    public boolean supportsInlineMode()
    {
        return false;
    }

    @Override
    protected List<Block> internalExecute(ConfluenceJiraChartMacroParameters parameters, String content,
        MacroTransformationContext context) throws MacroExecutionException
    {
        Map<String, String> jiraChartMacroParameters = new HashMap<>();

        if (!StringUtils.isNotEmpty(parameters.getServer())) {
            throw new MacroExecutionException("server ID parameter is required");
        }
        jiraChartMacroParameters.put("id", parameters.getServer());

        if (!StringUtils.isNotEmpty(parameters.getJql())) {
            throw new MacroExecutionException("jql parameter is required");
        }
        String jqlValue = URLDecoder.decode(parameters.getJql(), StandardCharsets.UTF_8);

        Matcher jqlFilterMatcher = JQL_FILTER_PATTERN.matcher(jqlValue);
        if (jqlFilterMatcher.find()) {
            jiraChartMacroParameters.put("filterId", "filter-" + jqlFilterMatcher.group(1));
        } else {
            jiraChartMacroParameters.put("query", jqlValue);
        }

        String macroName;
        switch (parameters.getChartType()) {
            case PIE:
                macroName = "jiraPieChart";
                handlePieChartParameter(parameters, jiraChartMacroParameters);
                break;
            case CREATEDVSRESOLVED:
                macroName = "jiraCreatedVsResolvedChart";
                handleCreatedVsResolvedChartParameter(parameters, jiraChartMacroParameters);
                break;
            case TWODIMENSIONAL:
                macroName = "jiraBiDimensionalGridChart";
                handleTwoDimensionalChartParameter(parameters, jiraChartMacroParameters);
                break;
            default:
                throw new MacroExecutionException("Invalid chart type " + parameters.getChartType());
        }

        return List.of(new MacroBlock(macroName, jiraChartMacroParameters, false));
    }

    private static void handlePieChartParameter(ConfluenceJiraChartMacroParameters parameters,
        Map<String, String> jiraChartMacroParameters) throws MacroExecutionException
    {
        if (StringUtils.isNotEmpty(parameters.getStatType())) {
            if (!PIE_CHART_TYPE_PARAM.containsKey(parameters.getStatType())) {
                // https://jira.xwiki.org/browse/JIRA-71
                throw new MacroExecutionException(
                    String.format("State type parameter value '%s' is not supported",
                        parameters.getStatType()));
            }
            jiraChartMacroParameters.put("type", PIE_CHART_TYPE_PARAM.get(parameters.getStatType()));
        }
    }

    private static void handleCreatedVsResolvedChartParameter(ConfluenceJiraChartMacroParameters parameters,
        Map<String, String> jiraChartMacroParameters)
    {
        if (StringUtils.isNotEmpty(parameters.getDaysprevious())) {
            jiraChartMacroParameters.put("daysPreviously", parameters.getDaysprevious());
        }
        if (StringUtils.isNotEmpty(parameters.getPeriodName())) {
            jiraChartMacroParameters.put("period", parameters.getPeriodName().toUpperCase());
        }
        if (StringUtils.isNotEmpty(parameters.getIsCumulative())) {
            // Need to inverse the boolean
            String isCumulative = TRUE.equalsIgnoreCase(parameters.getIsCumulative()) ? FALSE : TRUE;
            jiraChartMacroParameters.put("count", isCumulative);
        }
        if (StringUtils.isNotEmpty(parameters.getShowUnresolvedTrend())) {
            jiraChartMacroParameters.put("displayTrend", parameters.getShowUnresolvedTrend());
        }
        if (StringUtils.isNotEmpty(parameters.getVersionLabel())) {
            String versionLabel =
                VERSION_LABEL_MAP.getOrDefault(parameters.getVersionLabel(), parameters.getVersionLabel());
            jiraChartMacroParameters.put("displayVersion", versionLabel);
        }
    }

    private static void handleTwoDimensionalChartParameter(ConfluenceJiraChartMacroParameters parameters,
        Map<String, String> jiraChartMacroParameters) throws MacroExecutionException
    {
        if (StringUtils.isNotEmpty(parameters.getXstattype())) {
            if (!BI_DIMENSIONAL_GRID_CHART_AXIS_PARAM.containsKey(parameters.getXstattype())) {
                // https://jira.xwiki.org/projects/JIRA/issues/JIRA-72
                throw new MacroExecutionException(
                    String.format("x stat type parameter value '%s' is not supported",
                        parameters.getXstattype()));
            }
            jiraChartMacroParameters.put("xAxisField",
                BI_DIMENSIONAL_GRID_CHART_AXIS_PARAM.get(parameters.getXstattype()));
        }
        if (StringUtils.isNotEmpty(parameters.getYstattype())) {
            if (!BI_DIMENSIONAL_GRID_CHART_AXIS_PARAM.containsKey(parameters.getYstattype())) {
                // https://jira.xwiki.org/projects/JIRA/issues/JIRA-72
                throw new MacroExecutionException(
                    String.format("y stat type parameter value '%s' is not supported",
                        parameters.getYstattype()));
            }
            jiraChartMacroParameters.put("yAxisField",
                BI_DIMENSIONAL_GRID_CHART_AXIS_PARAM.get(parameters.getYstattype()));
        }
        if (StringUtils.isNotEmpty(parameters.getNumberToShow())) {
            jiraChartMacroParameters.put("numberOfResults", parameters.getNumberToShow());
        }
    }
}
