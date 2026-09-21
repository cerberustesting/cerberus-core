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

import java.util.List;
import java.util.Map;

/**
 * MCP tool that deletes an existing {@link Campaign} from Cerberus, under the tool name
 * {@code cerberus_campaign_delete}.
 *
 * <p>Delegates to {@link ICampaignService#delete(Campaign)}, which mirrors the legacy
 * {@code DeleteCampaign} servlet: it removes the campaign row and its event hooks, but does
 * <b>not</b> cascade-delete the campaign's {@link org.cerberus.core.crud.entity.CampaignParameter}
 * rows (countries/environments/robots). Remove those first with
 * {@code cerberus_campaign_parameter_delete} if a clean removal is required.</p>
 */
@Component
public class DeleteCampaignTool implements MCPTool {

    private static final String TOOL_NAME = "cerberus_campaign_delete";

    private final ICampaignService campaignService;
    private final MCPLogUtils mcpLogUtils;

    public DeleteCampaignTool(ICampaignService campaignService, MCPLogUtils mcpLogUtils) {
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
        Map<String, Object> properties = Map.of(
                "campaign", Map.of(
                        "type", "string",
                        "description", "Exact name of the campaign to delete."
                )
        );

        return new McpSchema.Tool(
                TOOL_NAME,
                null,
                """
                Deletes an existing campaign from Cerberus.

                Call this tool whenever the user asks to remove or delete a campaign.
                Before calling this tool, confirm the campaign name with the user.

                Use cerberus_campaign_list to find the exact campaign name before deleting.
                Note: the campaign's country/environment/robot parameters are not automatically
                removed by this tool; delete them separately with cerberus_campaign_parameter_delete
                if needed.

                Do not call this tool when the user only asks to list, read, create, update, or run a campaign.
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
                MCPToolUtils.deleteAnnotations("Delete campaign", false),
                null
        );
    }

    private McpSchema.CallToolResult execute(Map<String, Object> args) {
        String campaignName = MCPToolUtils.getString(args, "campaign", "");

        mcpLogUtils.call(TOOL_NAME, "campaign_delete",
                String.format("MCP tool %s called with campaign=%s", TOOL_NAME, campaignName));

        if (campaignName.isBlank()) {
            return MCPToolUtils.errorText("Missing required parameter: campaign");
        }

        AnswerItem<Campaign> readAnswer = campaignService.readByKey(campaignName);
        if (!readAnswer.isCodeStringEquals("OK") || readAnswer.getItem() == null) {
            return MCPToolUtils.errorText("Campaign does not exist: " + campaignName);
        }

        Campaign campaign = readAnswer.getItem();

        Answer deleteAnswer = campaignService.delete(campaign);

        if (!deleteAnswer.isCodeStringEquals("OK")) {
            return MCPToolUtils.errorText("Unable to delete campaign " + campaignName + ": " + deleteAnswer.getMessageDescription());
        }

        return MCPToolUtils.successJson(Map.of(
                "status", "deleted",
                "campaign", campaignName
        ));
    }
}