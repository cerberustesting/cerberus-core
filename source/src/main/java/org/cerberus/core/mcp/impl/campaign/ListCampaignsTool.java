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
import org.cerberus.core.util.answer.AnswerList;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * MCP tool that lists Cerberus {@link Campaign} entities, under the tool name
 * {@code cerberus_campaign_list}.
 *
 * <p>Delegates to {@link ICampaignService#readByCriteria(int, int, String, String, String, Map)}.
 * {@code search} matches the campaign name or description (server-side {@code LIKE}); {@code group1},
 * {@code group2} and {@code group3} apply exact filters on the campaign's grouping columns, which
 * Cerberus uses to organise campaigns (e.g. by team or product line).</p>
 *
 * <p>Use {@code cerberus_campaign_get} afterwards to inspect one campaign's full configuration and
 * its countries/environments/robots.</p>
 */
@Component
public class ListCampaignsTool implements MCPTool {

    private static final String TOOL_NAME = "cerberus_campaign_list";

    private static final int DEFAULT_LIMIT = 50;
    private static final int MAX_RESULTS = 200;

    private final ICampaignService campaignService;
    private final MCPLogUtils mcpLogUtils;

    public ListCampaignsTool(ICampaignService campaignService, MCPLogUtils mcpLogUtils) {
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
        Map<String, Object> properties = new LinkedHashMap<>();
        properties.put("search", Map.of(
                "type", "string",
                "description", "Optional text filter on the campaign name or description."
        ));
        properties.put("group1", Map.of("type", "string", "description", "Optional exact filter on the campaign's Group1 column."));
        properties.put("group2", Map.of("type", "string", "description", "Optional exact filter on the campaign's Group2 column."));
        properties.put("group3", Map.of("type", "string", "description", "Optional exact filter on the campaign's Group3 column."));
        properties.put("limit", Map.of(
                "type", "integer",
                "description", "Maximum number of campaigns to return. Defaults to " + DEFAULT_LIMIT
                        + ", capped at " + MAX_RESULTS + "."
        ));

        return new McpSchema.Tool(
                TOOL_NAME,
                null,
                """
                Lists Cerberus campaigns, optionally filtered by name/description or by group.

                Call this tool when the campaign name is unknown or when the user needs to browse
                or search campaigns. Use cerberus_campaign_get afterwards to see the full
                configuration (countries, environments, robots) of one specific campaign.
                """,
                new McpSchema.JsonSchema(
                        "object",
                        properties,
                        null,
                        null,
                        null,
                        null
                ),
                null,
                MCPToolUtils.readOnlyAnnotations("List campaigns", false),
                null
        );
    }

    private McpSchema.CallToolResult execute(Map<String, Object> args) {
        String search = MCPToolUtils.getString(args, "search", "");
        String group1 = MCPToolUtils.getString(args, "group1", "");
        String group2 = MCPToolUtils.getString(args, "group2", "");
        String group3 = MCPToolUtils.getString(args, "group3", "");
        int limit = Math.min(Math.max(MCPToolUtils.getInteger(args, "limit", DEFAULT_LIMIT), 1), MAX_RESULTS);

        mcpLogUtils.call(TOOL_NAME, "campaign_list",
                String.format("MCP tool %s called with search=%s group1=%s group2=%s group3=%s", TOOL_NAME, search, group1, group2, group3));

        Map<String, List<String>> individualSearch = new LinkedHashMap<>();
        if (!group1.isBlank()) {
            individualSearch.put("cpg.group1", List.of(group1));
        }
        if (!group2.isBlank()) {
            individualSearch.put("cpg.group2", List.of(group2));
        }
        if (!group3.isBlank()) {
            individualSearch.put("cpg.group3", List.of(group3));
        }

        AnswerList<Campaign> answer = campaignService.readByCriteria(0, limit, "cpg.campaign", "asc", search, individualSearch);

        List<Map<String, Object>> campaigns = answer.getDataList().stream()
                .map(this::toSummary)
                .toList();

        return MCPToolUtils.successJson(Map.of(
                "count", campaigns.size(),
                "campaigns", campaigns
        ));
    }

    private Map<String, Object> toSummary(Campaign campaign) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("campaign", MCPToolUtils.nullSafe(campaign.getCampaign()));
        map.put("description", MCPToolUtils.nullSafe(campaign.getDescription()));
        map.put("group1", MCPToolUtils.nullSafe(campaign.getGroup1()));
        map.put("group2", MCPToolUtils.nullSafe(campaign.getGroup2()));
        map.put("group3", MCPToolUtils.nullSafe(campaign.getGroup3()));
        map.put("tag", MCPToolUtils.nullSafe(campaign.getTag()));
        map.put("dateLastExecuted", campaign.getDateLastExecuted());
        return map;
    }

}