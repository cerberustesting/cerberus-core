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

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * MCP tool that retrieves the details of a single campaign parameter row, under the tool name
 * {@code cerberus_campaign_parameter_get}.
 *
 * <p>{@link ICampaignParameterService} has no single-row read method — only
 * {@link ICampaignParameterService#readByCampaign(String)} for the whole campaign — so this tool
 * reads that list and picks out the row matching the exact (parameter, value) pair, the same way
 * {@code cerberus_campaign_parameter_create}'s duplicate check does.</p>
 */
@Component
public class GetCampaignParameterTool implements MCPTool {

    private static final String TOOL_NAME = "cerberus_campaign_parameter_get";

    private final ICampaignParameterService campaignParameterService;
    private final MCPLogUtils mcpLogUtils;

    public GetCampaignParameterTool(ICampaignParameterService campaignParameterService, MCPLogUtils mcpLogUtils) {
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
        Map<String, Object> properties = new LinkedHashMap<>();
        properties.put("campaign", Map.of("type", "string", "description", "Name of the campaign."));
        properties.put("parameter", Map.of("type", "string", "description", "Type of criterion (e.g. COUNTRY, ENVIRONMENT, ROBOT)."));
        properties.put("value", Map.of("type", "string", "description", "Exact value to look up (e.g. 'FR' for a COUNTRY parameter)."));

        return new McpSchema.Tool(
                TOOL_NAME,
                null,
                """
                Retrieves the details of one selection criterion attached to a campaign.

                Call this tool when the user asks to confirm whether a specific country, environment,
                robot, or other criterion is configured on a campaign, and needs its exact stored form.

                Use cerberus_campaign_parameter_list instead to browse every criterion at once.
                """,
                new McpSchema.JsonSchema(
                        "object",
                        properties,
                        List.of("campaign", "parameter", "value"),
                        null,
                        null,
                        null
                ),
                null,
                MCPToolUtils.readOnlyAnnotations("Get campaign parameter", true),
                null
        );
    }

    private McpSchema.CallToolResult execute(Map<String, Object> args) {
        String campaignName = MCPToolUtils.getString(args, "campaign", "");
        String parameterName = MCPToolUtils.getString(args, "parameter", "");
        String value = MCPToolUtils.getString(args, "value", "");

        mcpLogUtils.call(TOOL_NAME, "campaign_parameter_get",
                String.format("MCP tool %s called with campaign=%s parameter=%s value=%s", TOOL_NAME, campaignName, parameterName, value));

        if (campaignName.isBlank()) {
            return MCPToolUtils.errorText("Missing required parameter: campaign");
        }
        if (parameterName.isBlank()) {
            return MCPToolUtils.errorText("Missing required parameter: parameter");
        }
        if (value.isBlank()) {
            return MCPToolUtils.errorText("Missing required parameter: value");
        }

        AnswerList<CampaignParameter> answer = campaignParameterService.readByCampaign(campaignName);

        CampaignParameter found = answer.getDataList().stream()
                .filter(p -> parameterName.equalsIgnoreCase(p.getParameter()) && value.equalsIgnoreCase(p.getValue()))
                .findFirst()
                .orElse(null);

        if (found == null) {
            return MCPToolUtils.errorText("Campaign parameter does not exist: campaign='" + campaignName
                    + "' parameter='" + parameterName + "' value='" + value + "'.");
        }

        Map<String, Object> map = new LinkedHashMap<>();
        map.put("campaignParameterId", found.getCampaignparameterID());
        map.put("campaign", MCPToolUtils.nullSafe(found.getCampaign()));
        map.put("parameter", MCPToolUtils.nullSafe(found.getParameter()));
        map.put("value", MCPToolUtils.nullSafe(found.getValue()));

        return MCPToolUtils.successJson(map);
    }
}