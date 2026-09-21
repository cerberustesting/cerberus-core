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
import org.cerberus.core.util.answer.AnswerList;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * MCP tool that changes the value of an existing campaign parameter row, under the tool name
 * {@code cerberus_campaign_parameter_update}.
 *
 * <p>{@code CampaignParameterDAO.update} runs {@code UPDATE campaignparameter SET value = ? WHERE
 * campaign = ? AND parameter = ?} — it matches on (campaign, parameter) only, <b>not</b> on the
 * current value. For a campaign with several rows sharing the same parameter name (e.g. two
 * COUNTRY values), calling the raw service method would silently collapse all of them to the same
 * new value instead of renaming just one. This tool refuses to update unless the (campaign,
 * parameter) pair currently resolves to exactly one row, so it can never have that effect; use
 * {@code cerberus_campaign_parameter_delete} + {@code cerberus_campaign_parameter_create} to
 * change one value out of several sharing a parameter name.</p>
 */
@Component
public class UpdateCampaignParameterTool implements MCPTool {

    private static final String TOOL_NAME = "cerberus_campaign_parameter_update";

    private final ICampaignParameterService campaignParameterService;
    private final MCPLogUtils mcpLogUtils;

    public UpdateCampaignParameterTool(ICampaignParameterService campaignParameterService, MCPLogUtils mcpLogUtils) {
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
        Map<String, Object> updateProperties = new LinkedHashMap<>();
        updateProperties.put("value", Map.of("type", "string", "description", "New value to set."));

        Map<String, Object> updatesSchema = new LinkedHashMap<>();
        updatesSchema.put("type", "object");
        updatesSchema.put("description", "The new value for this parameter.");
        updatesSchema.put("properties", updateProperties);
        updatesSchema.put("additionalProperties", false);

        Map<String, Object> properties = new LinkedHashMap<>();
        properties.put("campaign", Map.of("type", "string", "description", "Name of the campaign."));
        properties.put("parameter", Map.of("type", "string", "description", "Type of criterion to update (e.g. COUNTRY, ENVIRONMENT, ROBOT)."));
        properties.put("value", Map.of("type", "string", "description", "Current exact value, to identify the row (as returned by cerberus_campaign_parameter_list)."));
        properties.put("updates", updatesSchema);

        return new McpSchema.Tool(
                TOOL_NAME,
                null,
                """
                Changes the value of an existing campaign parameter (a country, environment, robot,
                or other criterion), in place.

                Call this tool whenever the user asks to correct, rename, or replace the value of a
                campaign parameter — e.g. change a COUNTRY from 'FR' to 'BE'.

                Only works when the campaign has exactly one row for that parameter name. If several
                values share the parameter (e.g. multiple COUNTRY entries), this tool refuses to
                update, to avoid silently overwriting all of them to the same value — use
                cerberus_campaign_parameter_delete then cerberus_campaign_parameter_create instead to
                change one value out of several.
                """,
                new McpSchema.JsonSchema(
                        "object",
                        properties,
                        List.of("campaign", "parameter", "value", "updates"),
                        null,
                        null,
                        null
                ),
                null,
                MCPToolUtils.updateAnnotations("Update campaign parameter", false),
                null
        );
    }

    @SuppressWarnings("unchecked")
    private McpSchema.CallToolResult execute(Map<String, Object> args) {
        String campaignName = MCPToolUtils.getString(args, "campaign", "");
        String parameterName = MCPToolUtils.getString(args, "parameter", "");
        String currentValue = MCPToolUtils.getString(args, "value", "");

        mcpLogUtils.call(TOOL_NAME, "campaign_parameter_update",
                String.format("MCP tool %s called with campaign=%s parameter=%s value=%s", TOOL_NAME, campaignName, parameterName, currentValue));

        if (campaignName.isBlank()) {
            return MCPToolUtils.errorText("Missing required parameter: campaign");
        }
        if (parameterName.isBlank()) {
            return MCPToolUtils.errorText("Missing required parameter: parameter");
        }
        if (currentValue.isBlank()) {
            return MCPToolUtils.errorText("Missing required parameter: value");
        }

        Object updatesObject = args.get("updates");
        if (!(updatesObject instanceof Map)) {
            return MCPToolUtils.errorText("Missing or invalid required parameter: updates");
        }
        Map<String, Object> updates = (Map<String, Object>) updatesObject;
        String newValue = MCPToolUtils.getString(updates, "value", "");
        if (newValue.isBlank()) {
            return MCPToolUtils.errorText("Missing required parameter: updates.value");
        }

        AnswerList<CampaignParameter> answer = campaignParameterService.readByCampaign(campaignName);

        List<CampaignParameter> matchingParameter = answer.getDataList().stream()
                .filter(p -> parameterName.equalsIgnoreCase(p.getParameter()))
                .toList();

        if (matchingParameter.isEmpty()) {
            return MCPToolUtils.errorText("Campaign parameter does not exist: campaign='" + campaignName
                    + "' parameter='" + parameterName + "' value='" + currentValue + "'.");
        }

        if (matchingParameter.size() > 1) {
            return MCPToolUtils.errorText("Campaign '" + campaignName + "' has " + matchingParameter.size()
                    + " values for parameter '" + parameterName + "'. Updating would overwrite all of them to the "
                    + "same value, so this tool refuses. Use cerberus_campaign_parameter_delete then "
                    + "cerberus_campaign_parameter_create to change just one.");
        }

        CampaignParameter existing = matchingParameter.get(0);
        if (!currentValue.equalsIgnoreCase(existing.getValue())) {
            return MCPToolUtils.errorText("Campaign parameter does not exist: campaign='" + campaignName
                    + "' parameter='" + parameterName + "' value='" + currentValue + "'. Current value is '"
                    + existing.getValue() + "'.");
        }

        existing.setValue(newValue);

        Answer updateAnswer = campaignParameterService.update(existing);
        if (!updateAnswer.isCodeStringEquals("OK")) {
            return MCPToolUtils.errorText("Unable to update campaign parameter: " + updateAnswer.getMessageDescription());
        }

        return MCPToolUtils.successJson(Map.of(
                "status", "updated",
                "campaign", campaignName,
                "parameter", parameterName,
                "value", newValue
        ));
    }
}