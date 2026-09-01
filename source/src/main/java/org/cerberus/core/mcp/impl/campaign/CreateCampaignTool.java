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
import org.cerberus.core.crud.entity.CampaignParameter;
import org.cerberus.core.crud.service.ICampaignParameterService;
import org.cerberus.core.crud.service.ICampaignService;
import org.cerberus.core.mcp.MCPTool;
import org.cerberus.core.mcp.util.MCPLogUtils;
import org.cerberus.core.mcp.util.MCPToolUtils;
import org.cerberus.core.util.answer.Answer;
import org.cerberus.core.util.answer.AnswerItem;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * MCP tool that creates a new {@link Campaign} in Cerberus, under the tool name
 * {@code cerberus_campaign_create}.
 *
 * <p>Delegates persistence of the campaign row to {@link ICampaignService#create(Campaign)}.
 * As a convenience, when {@code countries}, {@code environments} or {@code robots} are supplied,
 * this tool also creates the corresponding {@link CampaignParameter} rows via
 * {@link ICampaignParameterService#createList(List)} — {@code ICampaignService#create} does not
 * cascade them, they are a separate table keyed on (campaign, parameter, value).</p>
 *
 * <p>Numeric-looking fields ({@code screenshot}, {@code retries}, ...) are stored as free-form
 * strings on {@link Campaign}, matching the legacy CRUD screen; leaving one blank means the
 * execution engine falls back to its own default when the campaign is run
 * (see {@code cerberus_campaign_execution_create}).</p>
 */
@Component
public class CreateCampaignTool implements MCPTool {

    private static final String TOOL_NAME = "cerberus_campaign_create";

    private static final List<String> MANUAL_EXECUTION_VALUES = List.of("N", "Y", "A");

    private final ICampaignService campaignService;
    private final ICampaignParameterService campaignParameterService;
    private final MCPLogUtils mcpLogUtils;

    public CreateCampaignTool(ICampaignService campaignService, ICampaignParameterService campaignParameterService, MCPLogUtils mcpLogUtils) {
        this.campaignService = campaignService;
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
        properties.put("campaign", Map.of("type", "string", "description", "Name (unique key) of the new campaign."));
        properties.put("description", Map.of("type", "string", "description", "Short description of the campaign."));
        properties.put("longDescription", Map.of("type", "string", "description", "Optional detailed description of the campaign."));
        properties.put("countries", Map.of(
                "type", "array",
                "items", Map.of("type", "string"),
                "description", "Countries this campaign runs against (e.g. ['FR']). Stored as COUNTRY campaign parameters."
        ));
        properties.put("environments", Map.of(
                "type", "array",
                "items", Map.of("type", "string"),
                "description", "Environments this campaign runs against (e.g. ['QA']). Stored as ENVIRONMENT campaign parameters."
        ));
        properties.put("robots", Map.of(
                "type", "array",
                "items", Map.of("type", "string"),
                "description", "Robots this campaign runs on. Stored as ROBOT campaign parameters. Omit for manual-only campaigns."
        ));
        properties.put("tag", Map.of(
                "type", "string",
                "description", "Optional default execution tag. Supports %TIMESTAMP%, %USER%, %REQCOUNTRYLIST%, %REQENVIRONMENTLIST% placeholders."
        ));
        properties.put("manualExecution", Map.of(
                "type", "string",
                "description", "Whether the campaign runs manually. 'N' = automated (default), 'Y' = manual, 'A' = automated with manual fallback.",
                "enum", MANUAL_EXECUTION_VALUES
        ));
        properties.put("screenshot", Map.of("type", "string", "description", "Default screenshot capture level (0=off, 1=on error, 2=always)."));
        properties.put("video", Map.of("type", "string", "description", "Default video capture level (0=off, 1=on error, 2=always)."));
        properties.put("verbose", Map.of("type", "string", "description", "Default log verbosity level."));
        properties.put("pageSource", Map.of("type", "string", "description", "Default page source capture (0/1)."));
        properties.put("robotLog", Map.of("type", "string", "description", "Default robot log capture (0/1)."));
        properties.put("consoleLog", Map.of("type", "string", "description", "Default browser console log capture (0/1)."));
        properties.put("timeout", Map.of("type", "string", "description", "Default per-action timeout in milliseconds."));
        properties.put("retries", Map.of("type", "string", "description", "Default number of retries on a non-OK result."));
        properties.put("priority", Map.of("type", "string", "description", "Default queue priority (lower runs first)."));
        properties.put("ciScoreThreshold", Map.of("type", "string", "description", "Minimum success score (%) below which a CI run is considered failed."));
        properties.put("group1", Map.of("type", "string", "description", "Optional grouping value (e.g. team or product line)."));
        properties.put("group2", Map.of("type", "string", "description", "Optional secondary grouping value."));
        properties.put("group3", Map.of("type", "string", "description", "Optional tertiary grouping value."));
        properties.put("user", Map.of("type", "string", "description", "Optional user name recorded as the creator. Defaults to 'MCP'."));

        return new McpSchema.Tool(
                TOOL_NAME,
                null,
                """
                Creates a new campaign in Cerberus — a named, reusable set of testcases plus the
                countries/environments/robots to run them on, launched as a single unit.

                Call this tool whenever the user asks to create or add a new campaign.
                Only the campaign name is required. Provide countries, environments and robots up
                front when known — cerberus_campaign_execution_create requires at least one of each
                (explicitly or from this configuration) before it can run anything.

                Do not call this tool when the user only asks to list, read, update, delete, or run a campaign.
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
                MCPToolUtils.createAnnotations("Create campaign", false),
                null
        );
    }

    private McpSchema.CallToolResult execute(Map<String, Object> args) {
        String campaignName = MCPToolUtils.getString(args, "campaign", "");
        String user = MCPToolUtils.getString(args, "user", "MCP");
        String manualExecution = MCPToolUtils.getString(args, "manualExecution", "");

        mcpLogUtils.call(TOOL_NAME, "campaign_create",
                String.format("MCP tool %s called with campaign=%s", TOOL_NAME, campaignName));

        if (campaignName.isBlank()) {
            return MCPToolUtils.errorText("Missing required parameter: campaign");
        }
        if (!manualExecution.isBlank() && !MANUAL_EXECUTION_VALUES.contains(manualExecution)) {
            return MCPToolUtils.errorText("Invalid manualExecution value '" + manualExecution + "'. Expected one of " + MANUAL_EXECUTION_VALUES);
        }

        AnswerItem<Campaign> existing = campaignService.readByKey(campaignName);
        if (existing.isCodeStringEquals("OK") && existing.getItem() != null) {
            return MCPToolUtils.errorText("Campaign already exists: " + campaignName);
        }

        Campaign campaign = new Campaign();
        campaign.setCampaign(campaignName);
        campaign.setDescription(MCPToolUtils.getString(args, "description", ""));
        campaign.setLongDescription(MCPToolUtils.getString(args, "longDescription", ""));
        campaign.setTag(MCPToolUtils.getString(args, "tag", ""));
        campaign.setManualExecution(manualExecution);
        campaign.setScreenshot(MCPToolUtils.getString(args, "screenshot", ""));
        campaign.setVideo(MCPToolUtils.getString(args, "video", ""));
        campaign.setVerbose(MCPToolUtils.getString(args, "verbose", ""));
        campaign.setPageSource(MCPToolUtils.getString(args, "pageSource", ""));
        campaign.setRobotLog(MCPToolUtils.getString(args, "robotLog", ""));
        campaign.setConsoleLog(MCPToolUtils.getString(args, "consoleLog", ""));
        campaign.setTimeout(MCPToolUtils.getString(args, "timeout", ""));
        campaign.setRetries(MCPToolUtils.getString(args, "retries", ""));
        campaign.setPriority(MCPToolUtils.getString(args, "priority", ""));
        campaign.setCIScoreThreshold(MCPToolUtils.getString(args, "ciScoreThreshold", ""));
        campaign.setGroup1(MCPToolUtils.getString(args, "group1", ""));
        campaign.setGroup2(MCPToolUtils.getString(args, "group2", ""));
        campaign.setGroup3(MCPToolUtils.getString(args, "group3", ""));
        campaign.setUsrCreated(user);

        Answer answer = campaignService.create(campaign);

        if (!answer.isCodeStringEquals("OK")) {
            return MCPToolUtils.errorText("Unable to create campaign " + campaignName + ": " + answer.getMessageDescription());
        }

        List<CampaignParameter> parameters = new ArrayList<>();
        addParameters(parameters, campaignName, CampaignParameter.COUNTRY_PARAMETER, MCPToolUtils.getStringList(args, "countries", List.of()));
        addParameters(parameters, campaignName, CampaignParameter.ENVIRONMENT_PARAMETER, MCPToolUtils.getStringList(args, "environments", List.of()));
        addParameters(parameters, campaignName, CampaignParameter.ROBOT_PARAMETER, MCPToolUtils.getStringList(args, "robots", List.of()));

        if (!parameters.isEmpty()) {
            Answer parametersAnswer = campaignParameterService.createList(parameters);
            if (!parametersAnswer.isCodeStringEquals("OK")) {
                return MCPToolUtils.successJson(Map.of(
                        "status", "created_with_warnings",
                        "message", "Campaign created, but not all parameters could be saved: " + parametersAnswer.getMessageDescription(),
                        "campaign", campaignName
                ));
            }
        }

        return MCPToolUtils.successJson(Map.of(
                "status", "created",
                "campaign", campaignName,
                "parametersCreated", parameters.size()
        ));
    }

    private void addParameters(List<CampaignParameter> target, String campaignName, String parameterName, List<String> values) {
        for (String value : values) {
            if (value == null || value.isBlank()) {
                continue;
            }
            CampaignParameter parameter = new CampaignParameter();
            parameter.setCampaign(campaignName);
            parameter.setParameter(parameterName);
            parameter.setValue(value);
            target.add(parameter);
        }
    }

}