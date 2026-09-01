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
import org.cerberus.core.crud.entity.CampaignLabel;
import org.cerberus.core.crud.entity.Label;
import org.cerberus.core.crud.service.ICampaignLabelService;
import org.cerberus.core.crud.service.ICampaignParameterService;
import org.cerberus.core.crud.service.ICampaignService;
import org.cerberus.core.mcp.MCPTool;
import org.cerberus.core.mcp.util.MCPLogUtils;
import org.cerberus.core.mcp.util.MCPToolUtils;
import org.cerberus.core.util.answer.AnswerItem;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * MCP tool that retrieves a single {@link Campaign} entity by its exact name, under the tool name
 * {@code cerberus_campaign_get}.
 *
 * <p>There is no "campaign execution" object: a campaign is only a saved selection, and this tool
 * assembles the whole picture from three sources. {@link ICampaignService#readByKey(String)} gives
 * the campaign's own settings (defaults for screenshot/video/timeout/... and its default tag).
 * {@link ICampaignLabelService#readByVarious(String)} gives the labels a testcase must carry (one
 * of these, or a child of one) to be selected — the primary mechanism deciding which testcases the
 * campaign runs, there is no direct testcase-to-campaign assignment.
 * {@link ICampaignParameterService#parseParametersByCampaign(String)} gives the countries/
 * environments/robots to run on plus the further status/system/application/priority/type/testFolder
 * filters applied on top of the label match. Running the campaign
 * ({@code cerberus_campaign_execution_create}) re-derives testcases from labels and parameters
 * exactly the same way, and groups the resulting executions under a freshly generated tag.</p>
 *
 * <p>Use {@code cerberus_campaign_list} instead when the campaign name is unknown.</p>
 */
@Component
public class GetCampaignTool implements MCPTool {

    private static final String TOOL_NAME = "cerberus_campaign_get";

    private final ICampaignService campaignService;
    private final ICampaignParameterService campaignParameterService;
    private final ICampaignLabelService campaignLabelService;
    private final MCPLogUtils mcpLogUtils;

    public GetCampaignTool(ICampaignService campaignService, ICampaignParameterService campaignParameterService,
                           ICampaignLabelService campaignLabelService, MCPLogUtils mcpLogUtils) {
        this.campaignService = campaignService;
        this.campaignParameterService = campaignParameterService;
        this.campaignLabelService = campaignLabelService;
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
                        "description", "Exact name of the campaign to retrieve."
                )
        );

        return new McpSchema.Tool(
                TOOL_NAME,
                null,
                """
                Retrieves the full configuration of a specific Cerberus campaign by its exact name:
                its settings, the labels deciding which testcases it selects, and the countries/
                environments/robots/filters it runs them with.

                Call this tool whenever the user asks to inspect or display a specific campaign, or
                to understand what testcases it will run and where.
                Use cerberus_campaign_list instead when the campaign name is unknown.
                Do not call this tool when the user asks to create, update, delete, or run a campaign.
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
                MCPToolUtils.readOnlyAnnotations("Get campaign", true),
                null
        );
    }

    private McpSchema.CallToolResult execute(Map<String, Object> args) {
        String campaignName = MCPToolUtils.getString(args, "campaign", "");

        mcpLogUtils.call(TOOL_NAME, "campaign_get",
                String.format("MCP tool %s called with campaign=%s", TOOL_NAME, campaignName));

        if (campaignName.isBlank()) {
            return MCPToolUtils.errorText("Missing required parameter: campaign");
        }

        AnswerItem<Campaign> answer = campaignService.readByKey(campaignName);

        if (!answer.isCodeStringEquals("OK") || answer.getItem() == null) {
            return MCPToolUtils.errorText("Campaign does not exist: " + campaignName);
        }

        Campaign campaign = answer.getItem();
        Map<String, Object> result = toDetail(campaign);

        AnswerItem<Map<String, List<String>>> parsedParameters = campaignParameterService.parseParametersByCampaign(campaignName);
        result.put("parameters", parsedParameters.isCodeStringEquals("OK") && parsedParameters.getItem() != null
                ? parsedParameters.getItem()
                : Map.of());

        result.put("labels", campaignLabelService.readByVarious(campaignName).getDataList().stream()
                .map(this::toLabelSummary)
                .toList());

        return MCPToolUtils.successJson(result);
    }

    private Map<String, Object> toLabelSummary(CampaignLabel campaignLabel) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("labelId", campaignLabel.getLabelId());
        Label label = campaignLabel.getLabel();
        map.put("label", label != null ? MCPToolUtils.nullSafe(label.getLabel()) : "");
        return map;
    }

    private Map<String, Object> toDetail(Campaign campaign) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("campaign", MCPToolUtils.nullSafe(campaign.getCampaign()));
        map.put("description", MCPToolUtils.nullSafe(campaign.getDescription()));
        map.put("longDescription", MCPToolUtils.nullSafe(campaign.getLongDescription()));
        map.put("group1", MCPToolUtils.nullSafe(campaign.getGroup1()));
        map.put("group2", MCPToolUtils.nullSafe(campaign.getGroup2()));
        map.put("group3", MCPToolUtils.nullSafe(campaign.getGroup3()));
        map.put("tag", MCPToolUtils.nullSafe(campaign.getTag()));
        map.put("ciScoreThreshold", MCPToolUtils.nullSafe(campaign.getCIScoreThreshold()));
        map.put("verbose", MCPToolUtils.nullSafe(campaign.getVerbose()));
        map.put("screenshot", MCPToolUtils.nullSafe(campaign.getScreenshot()));
        map.put("video", MCPToolUtils.nullSafe(campaign.getVideo()));
        map.put("pageSource", MCPToolUtils.nullSafe(campaign.getPageSource()));
        map.put("robotLog", MCPToolUtils.nullSafe(campaign.getRobotLog()));
        map.put("consoleLog", MCPToolUtils.nullSafe(campaign.getConsoleLog()));
        map.put("timeout", MCPToolUtils.nullSafe(campaign.getTimeout()));
        map.put("retries", MCPToolUtils.nullSafe(campaign.getRetries()));
        map.put("priority", MCPToolUtils.nullSafe(campaign.getPriority()));
        map.put("manualExecution", MCPToolUtils.nullSafe(campaign.getManualExecution()));
        map.put("dateLastExecuted", campaign.getDateLastExecuted());
        map.put("usrCreated", MCPToolUtils.nullSafe(campaign.getUsrCreated()));
        map.put("dateCreated", campaign.getDateCreated());
        map.put("usrModif", MCPToolUtils.nullSafe(campaign.getUsrModif()));
        map.put("dateModif", campaign.getDateModif());
        return map;
    }
}