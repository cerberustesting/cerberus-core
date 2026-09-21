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
package org.cerberus.core.mcp.impl.label;

import io.modelcontextprotocol.server.McpServerFeatures;
import io.modelcontextprotocol.spec.McpSchema;
import org.cerberus.core.crud.entity.CampaignLabel;
import org.cerberus.core.crud.entity.Label;
import org.cerberus.core.crud.service.ICampaignLabelService;
import org.cerberus.core.mcp.MCPTool;
import org.cerberus.core.mcp.util.MCPLogUtils;
import org.cerberus.core.mcp.util.MCPToolUtils;
import org.cerberus.core.util.answer.AnswerList;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * MCP tool that lists the labels attached to a campaign, under the tool name
 * {@code cerberus_label_list}.
 *
 * <p>These labels are not cosmetic tags on the campaign: {@code TestCaseService.findTestCaseByCampaignNameAndCountries}
 * selects the campaign's testcases by joining on {@code testcaselabel} against exactly this list of
 * label ids (enriched with their child labels), so a campaign's attached labels are the primary
 * mechanism deciding which testcases it runs — there is no direct testcase-to-campaign assignment.
 * The {@link org.cerberus.core.crud.entity.CampaignParameter} rows returned by
 * {@code cerberus_campaign_parameter_list} narrow that label-matched set further (STATUS, SYSTEM,
 * APPLICATION, PRIORITY, TYPE, TESTFOLDER) and pick the countries/environments/robots to run on.</p>
 *
 * <p>Delegates to {@link ICampaignLabelService#readByVarious(String)}, which joins the {@code label}
 * table so each row already carries the label's name and color.</p>
 */
@Component
public class ListLabelsTool implements MCPTool {

    private static final String TOOL_NAME = "cerberus_label_list";

    private final ICampaignLabelService campaignLabelService;
    private final MCPLogUtils mcpLogUtils;

    public ListLabelsTool(ICampaignLabelService campaignLabelService, MCPLogUtils mcpLogUtils) {
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
                "campaign", Map.of("type", "string", "description", "Name of the campaign whose labels should be listed.")
        );

        return new McpSchema.Tool(
                TOOL_NAME,
                null,
                """
                Lists the labels attached to a campaign — the labels a testcase must carry (one of
                these, or a child of one) to be selected when the campaign runs.

                Call this tool when the user asks what a campaign is tagged/labelled with, what
                testcases it will pick up, or before adding/removing a label to avoid duplicates.
                cerberus_campaign_parameter_list lists the other side: countries/environments/robots
                to run on, and further filters (status, system, application, priority, type, test folder).
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
                MCPToolUtils.readOnlyAnnotations("List campaign labels", false),
                null
        );
    }

    private McpSchema.CallToolResult execute(Map<String, Object> args) {
        String campaignName = MCPToolUtils.getString(args, "campaign", "");

        mcpLogUtils.call(TOOL_NAME, "label_list",
                String.format("MCP tool %s called with campaign=%s", TOOL_NAME, campaignName));

        if (campaignName.isBlank()) {
            return MCPToolUtils.errorText("Missing required parameter: campaign");
        }

        AnswerList<CampaignLabel> answer = campaignLabelService.readByVarious(campaignName);

        List<Map<String, Object>> labels = answer.getDataList().stream()
                .map(this::toSummary)
                .toList();

        return MCPToolUtils.successJson(Map.of(
                "campaign", campaignName,
                "count", labels.size(),
                "labels", labels
        ));
    }

    private Map<String, Object> toSummary(CampaignLabel campaignLabel) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("campaignLabelId", campaignLabel.getCampaignLabelID());
        map.put("labelId", campaignLabel.getLabelId());
        Label label = campaignLabel.getLabel();
        map.put("label", label != null ? MCPToolUtils.nullSafe(label.getLabel()) : "");
        map.put("color", label != null ? MCPToolUtils.nullSafe(label.getColor()) : "");
        map.put("type", label != null ? MCPToolUtils.nullSafe(label.getType()) : "");
        return map;
    }
}