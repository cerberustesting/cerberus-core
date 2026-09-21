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
package org.cerberus.core.mcp.impl.campaign;

import io.modelcontextprotocol.server.McpServerFeatures;
import io.modelcontextprotocol.spec.McpSchema;
import org.cerberus.core.crud.entity.Campaign;
import org.cerberus.core.crud.service.ICampaignService;
import org.cerberus.core.mcp.MCPTool;
import org.cerberus.core.mcp.util.MCPLogUtils;
import org.cerberus.core.mcp.util.MCPToolUtils;
import org.cerberus.core.util.answer.Answer;
import org.cerberus.core.util.answer.AnswerItem;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * MCP tool that updates the configuration of an existing {@link Campaign}, under the tool name
 * {@code cerberus_campaign_update}.
 *
 * <p>Only the fields listed in the {@code updates} schema can be mutated — the campaign name
 * itself cannot be renamed through this tool. Countries, environments and robots are managed
 * separately as {@link org.cerberus.core.crud.entity.CampaignParameter} rows through
 * {@code cerberus_campaign_parameter_create} / {@code cerberus_campaign_parameter_delete}.</p>
 *
 * <p>Delegates to {@link ICampaignService#update(String, Campaign)} after a read-before-write
 * so untouched fields keep their current values.</p>
 */
@Component
public class UpdateCampaignTool implements MCPTool {

    private static final String TOOL_NAME = "cerberus_campaign_update";

    private static final List<String> MANUAL_EXECUTION_VALUES = List.of("N", "Y", "A");

    private final ICampaignService campaignService;
    private final MCPLogUtils mcpLogUtils;

    public UpdateCampaignTool(ICampaignService campaignService, MCPLogUtils mcpLogUtils) {
        this.campaignService = campaignService;
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
        updateProperties.put("description", Map.of("type", "string", "description", "New short description."));
        updateProperties.put("longDescription", Map.of("type", "string", "description", "New detailed description."));
        updateProperties.put("tag", Map.of("type", "string", "description", "New default execution tag."));
        updateProperties.put("manualExecution", Map.of("type", "string", "description", "New manual execution mode.", "enum", MANUAL_EXECUTION_VALUES));
        updateProperties.put("screenshot", Map.of("type", "string", "description", "New default screenshot capture level."));
        updateProperties.put("video", Map.of("type", "string", "description", "New default video capture level."));
        updateProperties.put("verbose", Map.of("type", "string", "description", "New default log verbosity level."));
        updateProperties.put("pageSource", Map.of("type", "string", "description", "New default page source capture (0/1)."));
        updateProperties.put("robotLog", Map.of("type", "string", "description", "New default robot log capture (0/1)."));
        updateProperties.put("consoleLog", Map.of("type", "string", "description", "New default browser console log capture (0/1)."));
        updateProperties.put("timeout", Map.of("type", "string", "description", "New default per-action timeout in milliseconds."));
        updateProperties.put("retries", Map.of("type", "string", "description", "New default number of retries on a non-OK result."));
        updateProperties.put("priority", Map.of("type", "string", "description", "New default queue priority."));
        updateProperties.put("ciScoreThreshold", Map.of("type", "string", "description", "New minimum success score (%) threshold."));
        updateProperties.put("group1", Map.of("type", "string", "description", "New Group1 value."));
        updateProperties.put("group2", Map.of("type", "string", "description", "New Group2 value."));
        updateProperties.put("group3", Map.of("type", "string", "description", "New Group3 value."));

        Map<String, Object> updatesSchema = new LinkedHashMap<>();
        updatesSchema.put("type", "object");
        updatesSchema.put("description", "Fields to update on the campaign. Only supported fields are allowed.");
        updatesSchema.put("properties", updateProperties);
        updatesSchema.put("additionalProperties", false);

        Map<String, Object> properties = new LinkedHashMap<>();
        properties.put("campaign", Map.of("type", "string", "description", "Name of the campaign to update."));
        properties.put("updates", updatesSchema);

        return new McpSchema.Tool(
                TOOL_NAME,
                null,
                """
                Updates the configuration of an existing campaign.

                Call this tool whenever the user asks to modify a campaign's settings (description,
                tag, execution defaults, grouping). The campaign name cannot be changed here.

                Use cerberus_campaign_parameter_create / cerberus_campaign_parameter_delete instead
                to add or remove countries, environments or robots.

                Do not call this tool when the user only asks to display, list, create, delete, or run a campaign.
                """,
                new McpSchema.JsonSchema(
                        "object",
                        properties,
                        List.of("campaign", "updates"),
                        null,
                        null,
                        null
                ),
                null,
                MCPToolUtils.updateAnnotations("Update campaign", false),
                null
        );
    }

    @SuppressWarnings("unchecked")
    private McpSchema.CallToolResult execute(Map<String, Object> args) {
        String campaignName = MCPToolUtils.getString(args, "campaign", "");

        mcpLogUtils.call(TOOL_NAME, "campaign_update",
                String.format("MCP tool %s called with campaign=%s", TOOL_NAME, campaignName));

        if (campaignName.isBlank()) {
            return MCPToolUtils.errorText("Missing required parameter: campaign");
        }

        Object updatesObject = args.get("updates");
        if (!(updatesObject instanceof Map)) {
            return MCPToolUtils.errorText("Missing or invalid required parameter: updates");
        }

        Map<String, Object> updates = (Map<String, Object>) updatesObject;
        if (updates.isEmpty()) {
            return MCPToolUtils.errorText("No field provided to update.");
        }

        AnswerItem<Campaign> readAnswer = campaignService.readByKey(campaignName);
        if (!readAnswer.isCodeStringEquals("OK") || readAnswer.getItem() == null) {
            return MCPToolUtils.errorText("Campaign does not exist: " + campaignName);
        }

        Campaign campaign = readAnswer.getItem();

        try {
            for (Map.Entry<String, Object> entry : updates.entrySet()) {
                String field = entry.getKey();
                Object value = entry.getValue();

                switch (field) {
                    case "description":
                        campaign.setDescription(asString(value, field));
                        break;
                    case "longDescription":
                        campaign.setLongDescription(asString(value, field));
                        break;
                    case "tag":
                        campaign.setTag(asString(value, field));
                        break;
                    case "manualExecution":
                        String manualExecution = asString(value, field);
                        if (!manualExecution.isBlank() && !MANUAL_EXECUTION_VALUES.contains(manualExecution)) {
                            return MCPToolUtils.errorText("Invalid manualExecution value '" + manualExecution + "'. Expected one of " + MANUAL_EXECUTION_VALUES);
                        }
                        campaign.setManualExecution(manualExecution);
                        break;
                    case "screenshot":
                        campaign.setScreenshot(asString(value, field));
                        break;
                    case "video":
                        campaign.setVideo(asString(value, field));
                        break;
                    case "verbose":
                        campaign.setVerbose(asString(value, field));
                        break;
                    case "pageSource":
                        campaign.setPageSource(asString(value, field));
                        break;
                    case "robotLog":
                        campaign.setRobotLog(asString(value, field));
                        break;
                    case "consoleLog":
                        campaign.setConsoleLog(asString(value, field));
                        break;
                    case "timeout":
                        campaign.setTimeout(asString(value, field));
                        break;
                    case "retries":
                        campaign.setRetries(asString(value, field));
                        break;
                    case "priority":
                        campaign.setPriority(asString(value, field));
                        break;
                    case "ciScoreThreshold":
                        campaign.setCIScoreThreshold(asString(value, field));
                        break;
                    case "group1":
                        campaign.setGroup1(asString(value, field));
                        break;
                    case "group2":
                        campaign.setGroup2(asString(value, field));
                        break;
                    case "group3":
                        campaign.setGroup3(asString(value, field));
                        break;
                    default:
                        return MCPToolUtils.errorText("Unsupported field for campaign update: " + field);
                }
            }
        } catch (IllegalArgumentException e) {
            return MCPToolUtils.errorText(e.getMessage());
        }

        campaign.setUsrModif("MCP");

        Answer updateAnswer = campaignService.update(campaignName, campaign);

        if (!updateAnswer.isCodeStringEquals("OK")) {
            return MCPToolUtils.errorText("Unable to update campaign " + campaignName + ": " + updateAnswer.getMessageDescription());
        }

        return MCPToolUtils.successJson(Map.of(
                "status", "updated",
                "campaign", campaignName
        ));
    }

    private String asString(Object value, String field) {
        if (value == null) {
            return "";
        }
        if (!(value instanceof String)) {
            throw new IllegalArgumentException("Invalid value for field '" + field + "'. Expected string.");
        }
        return ((String) value).trim();
    }

}