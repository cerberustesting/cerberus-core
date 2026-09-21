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
import java.util.List;
import java.util.Map;

import static org.cerberus.core.mcp.impl.campaign.scheduler.ListCampaignSchedulesTool.SCHEDULE_TYPE_CAMPAIGN;

/**
 * MCP tool that removes a cron schedule from a campaign, under the tool name
 * {@code cerberus_campaign_scheduler_delete}.
 *
 * <p>Delegates to {@link IScheduleEntryService#delete(ScheduleEntry)} after verifying the schedule
 * belongs to the given campaign, then bumps {@code scheduler_version} and calls
 * {@link SchedulerInit#init()} so the campaign stops being triggered immediately.</p>
 */
@Component
public class DeleteCampaignScheduleTool implements MCPTool {

    private static final String TOOL_NAME = "cerberus_campaign_scheduler_delete";
    private static final String SCHEDULER_VERSION_KEY = "scheduler_version";

    private final IScheduleEntryService scheduleEntryService;
    private final IMyVersionService myVersionService;
    private final SchedulerInit schedulerInit;
    private final MCPLogUtils mcpLogUtils;

    public DeleteCampaignScheduleTool(IScheduleEntryService scheduleEntryService, IMyVersionService myVersionService,
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
        Map<String, Object> properties = Map.of(
                "campaign", Map.of("type", "string", "description", "Name of the campaign the schedule belongs to."),
                "id", Map.of("type", "integer", "description", "Schedule id, as returned by cerberus_campaign_scheduler_list.")
        );

        return new McpSchema.Tool(
                TOOL_NAME,
                null,
                """
                Removes a cron schedule from a campaign, so it stops running automatically on that schedule.

                Call this tool whenever the user asks to remove, cancel, or stop a campaign's
                automatic schedule. Use cerberus_campaign_scheduler_list first to get the id.
                A campaign with several schedules keeps the others unless each is deleted individually.
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
                MCPToolUtils.deleteAnnotations("Delete campaign schedule", false),
                null
        );
    }

    private McpSchema.CallToolResult execute(Map<String, Object> args) {
        String campaignName = MCPToolUtils.getString(args, "campaign", "");
        long id = MCPToolUtils.getLong(args, "id", 0L);

        mcpLogUtils.call(TOOL_NAME, "campaign_scheduler_delete",
                String.format("MCP tool %s called with campaign=%s id=%d", TOOL_NAME, campaignName, id));

        if (campaignName.isBlank()) {
            return MCPToolUtils.errorText("Missing required parameter: campaign");
        }
        if (id <= 0) {
            return MCPToolUtils.errorText("Missing or invalid required parameter: id");
        }

        AnswerItem<ScheduleEntry> readAnswer = scheduleEntryService.readbykey(id);
        if (!readAnswer.isCodeStringEquals("OK") || readAnswer.getItem() == null) {
            return MCPToolUtils.errorText("Schedule does not exist: " + id);
        }

        ScheduleEntry entry = readAnswer.getItem();
        if (!SCHEDULE_TYPE_CAMPAIGN.equalsIgnoreCase(entry.getType()) || !campaignName.equalsIgnoreCase(entry.getName())) {
            return MCPToolUtils.errorText("Schedule " + id + " does not belong to campaign " + campaignName);
        }

        Answer deleteAnswer = scheduleEntryService.delete(entry);
        if (!deleteAnswer.isCodeStringEquals("OK")) {
            return MCPToolUtils.errorText("Unable to delete schedule " + id + ": " + deleteAnswer.getMessageDescription());
        }

        myVersionService.updateMyVersionString(SCHEDULER_VERSION_KEY, String.valueOf(new Date()));
        schedulerInit.init();

        return MCPToolUtils.successJson(Map.of(
                "status", "deleted",
                "id", id,
                "campaign", campaignName
        ));
    }
}