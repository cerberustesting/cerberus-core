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
package org.cerberus.core.mcp.impl.campaign.scheduler;

import io.modelcontextprotocol.server.McpServerFeatures;
import io.modelcontextprotocol.spec.McpSchema;
import org.cerberus.core.crud.entity.ScheduleEntry;
import org.cerberus.core.crud.service.IScheduleEntryService;
import org.cerberus.core.mcp.MCPTool;
import org.cerberus.core.mcp.util.MCPLogUtils;
import org.cerberus.core.mcp.util.MCPToolUtils;
import org.cerberus.core.util.answer.AnswerList;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * MCP tool that lists the cron schedules configured for a campaign, under the tool name
 * {@code cerberus_campaign_scheduler_list}.
 *
 * <p>Delegates to {@link IScheduleEntryService#readByName(String)}. The underlying table is not
 * filtered by {@code type} in SQL, so this tool defensively keeps only rows whose
 * {@code type} is {@code CAMPAIGN} — the convention every existing caller in Cerberus already
 * relies on without enforcing it at the database level.</p>
 */
@Component
public class ListCampaignSchedulesTool implements MCPTool {

    private static final String TOOL_NAME = "cerberus_campaign_scheduler_list";
    static final String SCHEDULE_TYPE_CAMPAIGN = "CAMPAIGN";

    private final IScheduleEntryService scheduleEntryService;
    private final MCPLogUtils mcpLogUtils;

    public ListCampaignSchedulesTool(IScheduleEntryService scheduleEntryService, MCPLogUtils mcpLogUtils) {
        this.scheduleEntryService = scheduleEntryService;
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
                        "description", "Name of the campaign whose schedules should be listed."
                )
        );

        return new McpSchema.Tool(
                TOOL_NAME,
                null,
                """
                Lists the cron schedules (automatic recurring launches) configured for a campaign.

                Call this tool when the user asks whether/when a campaign runs automatically, or
                before adding or removing a schedule, to see what is already configured and get the
                schedule id needed by cerberus_campaign_scheduler_update / _delete.
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
                MCPToolUtils.readOnlyAnnotations("List campaign schedules", false),
                null
        );
    }

    private McpSchema.CallToolResult execute(Map<String, Object> args) {
        String campaignName = MCPToolUtils.getString(args, "campaign", "");

        mcpLogUtils.call(TOOL_NAME, "campaign_scheduler_list",
                String.format("MCP tool %s called with campaign=%s", TOOL_NAME, campaignName));

        if (campaignName.isBlank()) {
            return MCPToolUtils.errorText("Missing required parameter: campaign");
        }

        AnswerList<ScheduleEntry> answer = scheduleEntryService.readByName(campaignName);

        List<Map<String, Object>> schedules = answer.getDataList().stream()
                .filter(entry -> SCHEDULE_TYPE_CAMPAIGN.equalsIgnoreCase(entry.getType()))
                .map(this::toSummary)
                .toList();

        return MCPToolUtils.successJson(Map.of(
                "campaign", campaignName,
                "count", schedules.size(),
                "schedules", schedules
        ));
    }

    private Map<String, Object> toSummary(ScheduleEntry entry) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("id", entry.getID());
        map.put("cronDefinition", MCPToolUtils.nullSafe(entry.getCronDefinition()));
        map.put("active", "Y".equalsIgnoreCase(entry.getActive()));
        map.put("description", MCPToolUtils.nullSafe(entry.getDescription()));
        map.put("lastExecution", entry.getLastExecution());
        return map;
    }
}