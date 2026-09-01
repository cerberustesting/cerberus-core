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
import org.cerberus.core.util.answer.AnswerItem;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.cerberus.core.mcp.impl.campaign.scheduler.ListCampaignSchedulesTool.SCHEDULE_TYPE_CAMPAIGN;

/**
 * MCP tool that retrieves the details of a single campaign schedule, under the tool name
 * {@code cerberus_campaign_scheduler_get}.
 *
 * <p>Delegates to {@link IScheduleEntryService#readbykey(long)}, then defensively verifies the
 * schedule belongs to the given campaign — the {@code scheduleentry} table has no {@code campaign}
 * foreign key, only a {@code name} column shared with every schedule type.</p>
 */
@Component
public class GetCampaignScheduleTool implements MCPTool {

    private static final String TOOL_NAME = "cerberus_campaign_scheduler_get";

    private final IScheduleEntryService scheduleEntryService;
    private final MCPLogUtils mcpLogUtils;

    public GetCampaignScheduleTool(IScheduleEntryService scheduleEntryService, MCPLogUtils mcpLogUtils) {
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
                "campaign", Map.of("type", "string", "description", "Name of the campaign the schedule belongs to."),
                "id", Map.of("type", "integer", "description", "Schedule id, as returned by cerberus_campaign_scheduler_list.")
        );

        return new McpSchema.Tool(
                TOOL_NAME,
                null,
                """
                Retrieves the full details of a single campaign schedule (its cron expression,
                active flag, description, and last execution time).

                Call this tool when the user asks about a specific schedule on a campaign.
                Use cerberus_campaign_scheduler_list first to get the id.
                """,
                new McpSchema.JsonSchema(
                        "object",
                        properties,
                        List.of("campaign", "id"),
                        null,
                        null,
                        null
                ),
                null,
                MCPToolUtils.readOnlyAnnotations("Get campaign schedule", true),
                null
        );
    }

    private McpSchema.CallToolResult execute(Map<String, Object> args) {
        String campaignName = MCPToolUtils.getString(args, "campaign", "");
        long id = MCPToolUtils.getLong(args, "id", 0L);

        mcpLogUtils.call(TOOL_NAME, "campaign_scheduler_get",
                String.format("MCP tool %s called with campaign=%s id=%d", TOOL_NAME, campaignName, id));

        if (campaignName.isBlank()) {
            return MCPToolUtils.errorText("Missing required parameter: campaign");
        }
        if (id <= 0) {
            return MCPToolUtils.errorText("Missing or invalid required parameter: id");
        }

        AnswerItem<ScheduleEntry> answer = scheduleEntryService.readbykey(id);
        if (!answer.isCodeStringEquals("OK") || answer.getItem() == null) {
            return MCPToolUtils.errorText("Schedule does not exist: " + id);
        }

        ScheduleEntry entry = answer.getItem();
        if (!SCHEDULE_TYPE_CAMPAIGN.equalsIgnoreCase(entry.getType()) || !campaignName.equalsIgnoreCase(entry.getName())) {
            return MCPToolUtils.errorText("Schedule " + id + " does not belong to campaign " + campaignName);
        }

        return MCPToolUtils.successJson(toDetail(entry));
    }

    private Map<String, Object> toDetail(ScheduleEntry entry) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("id", entry.getID());
        map.put("campaign", MCPToolUtils.nullSafe(entry.getName()));
        map.put("cronDefinition", MCPToolUtils.nullSafe(entry.getCronDefinition()));
        map.put("active", "Y".equalsIgnoreCase(entry.getActive()));
        map.put("description", MCPToolUtils.nullSafe(entry.getDescription()));
        map.put("lastExecution", entry.getLastExecution());
        map.put("usrCreated", MCPToolUtils.nullSafe(entry.getUsrCreated()));
        map.put("dateCreated", entry.getDateCreated());
        map.put("usrModif", MCPToolUtils.nullSafe(entry.getUsrModif()));
        map.put("dateModif", entry.getDateModif());
        return map;
    }
}