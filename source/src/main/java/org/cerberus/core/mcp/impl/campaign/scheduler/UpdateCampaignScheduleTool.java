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
import org.cerberus.core.crud.service.IMyVersionService;
import org.cerberus.core.crud.service.IScheduleEntryService;
import org.cerberus.core.engine.scheduler.SchedulerInit;
import org.cerberus.core.mcp.MCPTool;
import org.cerberus.core.mcp.util.MCPLogUtils;
import org.cerberus.core.mcp.util.MCPToolUtils;
import org.cerberus.core.util.answer.Answer;
import org.cerberus.core.util.answer.AnswerItem;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.cerberus.core.mcp.impl.campaign.scheduler.ListCampaignSchedulesTool.SCHEDULE_TYPE_CAMPAIGN;

/**
 * MCP tool that updates an existing campaign schedule, under the tool name
 * {@code cerberus_campaign_scheduler_update}.
 *
 * <p>Read-before-write on {@link IScheduleEntryService#readbykey(long)}, both to keep untouched
 * fields and to defensively verify the schedule actually belongs to the given campaign — the
 * {@code scheduleentry} table has no {@code campaign} foreign key, only a {@code name} column
 * shared with every schedule type. Delegates to {@link IScheduleEntryService#update(ScheduleEntry)}
 * (which validates the cron expression itself), then bumps {@code scheduler_version} and calls
 * {@link SchedulerInit#init()} so the change applies immediately.</p>
 */
@Component
public class UpdateCampaignScheduleTool implements MCPTool {

    private static final String TOOL_NAME = "cerberus_campaign_scheduler_update";
    private static final String SCHEDULER_VERSION_KEY = "scheduler_version";

    private final IScheduleEntryService scheduleEntryService;
    private final IMyVersionService myVersionService;
    private final SchedulerInit schedulerInit;
    private final MCPLogUtils mcpLogUtils;

    public UpdateCampaignScheduleTool(IScheduleEntryService scheduleEntryService, IMyVersionService myVersionService,
                                      SchedulerInit schedulerInit, MCPLogUtils mcpLogUtils) {
        this.scheduleEntryService = scheduleEntryService;
        this.myVersionService = myVersionService;
        this.schedulerInit = schedulerInit;
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
        updateProperties.put("cronDefinition", Map.of("type", "string", "description", "New Quartz cron expression."));
        updateProperties.put("active", Map.of("type", "boolean", "description", "Enable or disable the schedule."));
        updateProperties.put("description", Map.of("type", "string", "description", "New description."));

        Map<String, Object> updatesSchema = new LinkedHashMap<>();
        updatesSchema.put("type", "object");
        updatesSchema.put("description", "Fields to update on the schedule.");
        updatesSchema.put("properties", updateProperties);
        updatesSchema.put("additionalProperties", false);

        Map<String, Object> properties = new LinkedHashMap<>();
        properties.put("campaign", Map.of("type", "string", "description", "Name of the campaign the schedule belongs to."));
        properties.put("id", Map.of("type", "integer", "description", "Schedule id, as returned by cerberus_campaign_scheduler_list."));
        properties.put("updates", updatesSchema);

        return new McpSchema.Tool(
                TOOL_NAME,
                null,
                """
                Updates an existing cron schedule of a campaign (its cron expression, active flag, or description).

                Call this tool whenever the user asks to change, enable, disable, or reschedule an
                existing campaign schedule. Use cerberus_campaign_scheduler_list first to get the id.

                Do not call this tool to create a new schedule or to run the campaign immediately.
                """,
                new McpSchema.JsonSchema(
                        "object",
                        properties,
                        List.of("campaign", "id", "updates"),
                        null,
                        null,
                        null
                ),
                null,
                MCPToolUtils.updateAnnotations("Update campaign schedule", false),
                null
        );
    }

    @SuppressWarnings("unchecked")
    private McpSchema.CallToolResult execute(Map<String, Object> args) {
        String campaignName = MCPToolUtils.getString(args, "campaign", "");
        long id = MCPToolUtils.getLong(args, "id", 0L);

        mcpLogUtils.call(TOOL_NAME, "campaign_scheduler_update",
                String.format("MCP tool %s called with campaign=%s id=%d", TOOL_NAME, campaignName, id));

        if (campaignName.isBlank()) {
            return MCPToolUtils.errorText("Missing required parameter: campaign");
        }
        if (id <= 0) {
            return MCPToolUtils.errorText("Missing or invalid required parameter: id");
        }

        Object updatesObject = args.get("updates");
        if (!(updatesObject instanceof Map)) {
            return MCPToolUtils.errorText("Missing or invalid required parameter: updates");
        }
        Map<String, Object> updates = (Map<String, Object>) updatesObject;
        if (updates.isEmpty()) {
            return MCPToolUtils.errorText("No field provided to update.");
        }

        AnswerItem<ScheduleEntry> readAnswer = scheduleEntryService.readbykey(id);
        if (!readAnswer.isCodeStringEquals("OK") || readAnswer.getItem() == null) {
            return MCPToolUtils.errorText("Schedule does not exist: " + id);
        }

        ScheduleEntry entry = readAnswer.getItem();
        if (!SCHEDULE_TYPE_CAMPAIGN.equalsIgnoreCase(entry.getType()) || !campaignName.equalsIgnoreCase(entry.getName())) {
            return MCPToolUtils.errorText("Schedule " + id + " does not belong to campaign " + campaignName);
        }

        for (Map.Entry<String, Object> update : updates.entrySet()) {
            switch (update.getKey()) {
                case "cronDefinition":
                    entry.setCronDefinition(String.valueOf(update.getValue()));
                    break;
                case "active":
                    entry.setActive(Boolean.TRUE.equals(update.getValue()) ? "Y" : "N");
                    break;
                case "description":
                    entry.setDescription(String.valueOf(update.getValue()));
                    break;
                default:
                    return MCPToolUtils.errorText("Unsupported field for schedule update: " + update.getKey());
            }
        }
        entry.setUsrModif(MCPToolUtils.getString(args, "user", "MCP"));

        Answer updateAnswer = scheduleEntryService.update(entry);
        if (!updateAnswer.isCodeStringEquals("OK")) {
            return MCPToolUtils.errorText("Unable to update schedule " + id + ": " + updateAnswer.getMessageDescription());
        }

        myVersionService.updateMyVersionString(SCHEDULER_VERSION_KEY, String.valueOf(new Date()));
        schedulerInit.init();

        return MCPToolUtils.successJson(Map.of(
                "status", "updated",
                "id", id,
                "campaign", campaignName
        ));
    }
}