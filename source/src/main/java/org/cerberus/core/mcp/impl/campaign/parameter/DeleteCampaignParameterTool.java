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
import org.cerberus.core.util.answer.Answer;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * MCP tool that removes a single selection criterion from a campaign, under the tool name
 * {@code cerberus_campaign_parameter_delete}.
 *
 * <p>Delegates to {@link ICampaignParameterService#delete(CampaignParameter)}, which matches the
 * exact (campaign, parameter, value) tuple. The underlying delete is a no-op — reported as
 * success — when no row matches, since the SQL {@code DELETE} does not check the affected row
 * count.</p>
 */
@Component
public class DeleteCampaignParameterTool implements MCPTool {

    private static final String TOOL_NAME = "cerberus_campaign_parameter_delete";

    private final ICampaignParameterService campaignParameterService;
    private final MCPLogUtils mcpLogUtils;

    public DeleteCampaignParameterTool(ICampaignParameterService campaignParameterService, MCPLogUtils mcpLogUtils) {
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
        properties.put("campaign", Map.of("type", "string", "description", "Name of the campaign to remove the criterion from."));
        properties.put("parameter", Map.of("type", "string", "description", "Type of criterion to remove (e.g. COUNTRY, ENVIRONMENT, ROBOT)."));
        properties.put("value", Map.of("type", "string", "description", "Exact value to remove (e.g. 'FR' for a COUNTRY parameter)."));

        return new McpSchema.Tool(
                TOOL_NAME,
                null,
                """
                Removes one selection criterion (country, environment, robot, or other filter) from a campaign.

                Call this tool whenever the user asks to remove a country, environment or robot
                from a campaign. All three fields (campaign, parameter, value) must match exactly —
                use cerberus_campaign_parameter_list first to get the exact value to remove.

                This call succeeds even if no matching row exists.
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
                MCPToolUtils.deleteAnnotations("Remove campaign parameter", false),
                null
        );
    }

    private McpSchema.CallToolResult execute(Map<String, Object> args) {
        String campaignName = MCPToolUtils.getString(args, "campaign", "");
        String parameterName = MCPToolUtils.getString(args, "parameter", "");
        String value = MCPToolUtils.getString(args, "value", "");

        mcpLogUtils.call(TOOL_NAME, "campaign_parameter_delete",
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

        CampaignParameter parameter = new CampaignParameter();
        parameter.setCampaign(campaignName);
        parameter.setParameter(parameterName);
        parameter.setValue(value);

        Answer answer = campaignParameterService.delete(parameter);

        if (!answer.isCodeStringEquals("OK")) {
            return MCPToolUtils.errorText("Unable to delete campaign parameter: " + answer.getMessageDescription());
        }

        return MCPToolUtils.successJson(Map.of(
                "status", "deleted",
                "campaign", campaignName,
                "parameter", parameterName,
                "value", value
        ));
    }
}