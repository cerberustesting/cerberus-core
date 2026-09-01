/**
 * Cerberus Copyright (C) 2013 - 2025 cerberustesting
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This file is part of Cerberus.
 *
 * Cerberus is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Cerberus is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Cerberus.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.cerberus.core.mcp.impl.campaign.parameter;

import io.modelcontextprotocol.server.McpServerFeatures;
import io.modelcontextprotocol.spec.McpSchema;
import org.cerberus.core.crud.entity.CampaignParameter;
import org.cerberus.core.crud.service.ICampaignParameterService;
import org.cerberus.core.mcp.MCPTool;
import org.cerberus.core.mcp.util.MCPLogUtils;
import org.cerberus.core.mcp.util.MCPToolUtils;
import org.cerberus.core.util.answer.AnswerList;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * MCP tool that lists the {@link CampaignParameter} rows of a campaign, under the tool name
 * {@code cerberus_campaign_parameter_list}.
 *
 * <p>These rows play two different roles. COUNTRY, ENVIRONMENT and ROBOT are the defaults
 * {@code cerberus_campaign_execution_create} falls back to when the caller does not explicitly
 * override them for one run. STATUS, SYSTEM, APPLICATION, PRIORITY, TYPE, TESTFOLDER and BROWSER
 * are not overridable at execution time — they are permanent filters, applied together with the
 * campaign's labels ({@code cerberus_label_list}) every time
 * {@code TestCaseService.findTestCaseByCampaignNameAndCountries} resolves which testcases the
 * campaign selects.</p>
 *
 * <p>Delegates to {@link ICampaignParameterService#readByCampaign(String)}.</p>
 */
@Component
public class ListCampaignParametersTool implements MCPTool {

    private static final String TOOL_NAME = "cerberus_campaign_parameter_list";

    private final ICampaignParameterService campaignParameterService;
    private final MCPLogUtils mcpLogUtils;

    public ListCampaignParametersTool(ICampaignParameterService campaignParameterService, MCPLogUtils mcpLogUtils) {
        this.campaignParameterService = campaignParameterService;
        this.mcpLogUtils = mcpLogUtils;
    }

    @Override
    public McpServerFeatures.SyncToolSpecification toToolSpecification() {
        return new McpServerFeatures.SyncToolSpecification(
                createTool(),
                (exchange, request) -> {
                    Map<String, Object> args = MCPToolUtils.argumentsOrEmpty(request.arguments());
                    return execute(args);
                }
        );
    }

    private McpSchema.Tool createTool() {
        Map<String, Object> properties = Map.of(
                "campaign", Map.of(
                        "type", "string",
                        "description", "Name of the campaign whose parameters should be listed."
                )
        );

        return new McpSchema.Tool(
                TOOL_NAME,
                null,
                """
                Lists the countries/environments/robots a campaign runs on, and the other filters
                (status, system, application, priority, type, test folder) that narrow which
                labelled testcases it selects.

                Call this tool when the user asks what a campaign runs against or which testcases it
                will pick up, or before adding/removing a parameter to see the current values.
                Use cerberus_label_list alongside this one — labels decide which testcases match in
                the first place, these parameters filter and target that set.

                Do not call this tool to create, update, or delete a parameter.
                """,
                new McpSchema.JsonSchema(
                        "object",
                        properties,
                        List.of("campaign"),
                        null,
                        null,
                        null
                ),
                null,
                MCPToolUtils.readOnlyAnnotations("List campaign parameters", false),
                null
        );
    }

    private McpSchema.CallToolResult execute(Map<String, Object> args) {
        String campaignName = MCPToolUtils.getString(args, "campaign", "");

        mcpLogUtils.call(TOOL_NAME, "campaign_parameter_list",
                String.format("MCP tool %s called with campaign=%s", TOOL_NAME, campaignName));

        if (campaignName.isBlank()) {
            return MCPToolUtils.errorText("Missing required parameter: campaign");
        }

        AnswerList<CampaignParameter> answer = campaignParameterService.readByCampaign(campaignName);

        List<Map<String, Object>> parameters = new ArrayList<>();
        Map<String, List<String>> grouped = new LinkedHashMap<>();
        for (CampaignParameter parameter : answer.getDataList()) {
            Map<String, Object> map = new LinkedHashMap<>();
            map.put("parameter", MCPToolUtils.nullSafe(parameter.getParameter()));
            map.put("value", MCPToolUtils.nullSafe(parameter.getValue()));
            parameters.add(map);

            grouped.computeIfAbsent(MCPToolUtils.nullSafe(parameter.getParameter()), k -> new ArrayList<>())
                    .add(MCPToolUtils.nullSafe(parameter.getValue()));
        }

        return MCPToolUtils.successJson(Map.of(
                "campaign", campaignName,
                "count", parameters.size(),
                "parameters", parameters,
                "grouped", grouped
        ));
    }
}