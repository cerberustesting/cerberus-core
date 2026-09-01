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
import org.quartz.CronExpression;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.cerberus.core.mcp.impl.campaign.scheduler.ListCampaignSchedulesTool.SCHEDULE_TYPE_CAMPAIGN;

/**
 * MCP tool that adds a cron schedule to a campaign, under the tool name
 * {@code cerberus_campaign_scheduler_create}.
 *
 * <p>Delegates persistence to {@link IScheduleEntryService#create(ScheduleEntry)}, which — unlike
 * {@code createListSched} — does not validate the cron expression or reload the scheduler on its
 * own; this tool does both itself: cron syntax is checked with {@link CronExpression#isValidExpression(String)}
 * before saving, and after a successful save it bumps the {@code scheduler_version} row via
 * {@link IMyVersionService} and calls {@link SchedulerInit#init()} — the same sequence
 * {@code CampaignService.delete} uses — so the new schedule takes effect immediately instead of
 * waiting for the next unrelated scheduler reload.</p>
 */
@Component
public class CreateCampaignScheduleTool implements MCPTool {

    private static final String TOOL_NAME = "cerberus_campaign_scheduler_create";
    private static final String SCHEDULER_VERSION_KEY = "scheduler_version";

    private final IScheduleEntryService scheduleEntryService;
    private final IMyVersionService myVersionService;
    private final SchedulerInit schedulerInit;
    private final MCPLogUtils mcpLogUtils;

    public CreateCampaignScheduleTool(IScheduleEntryService scheduleEntryService, IMyVersionService myVersionService,
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
        Map<String, Object> properties = new LinkedHashMap<>();
        properties.put("campaign", Map.of("type", "string", "description", "Name of the campaign to schedule."));
        properties.put("cronDefinition", Map.of(
                "type", "string",
                "description", "Quartz cron expression (e.g. '0 0 6 * * ?' for every day at 6am). "
                        + "Six or seven fields: seconds minutes hours day-of-month month day-of-week [year]."
        ));
        properties.put("active", Map.of("type", "boolean", "description", "Whether the schedule is enabled. Defaults to true."));
        properties.put("description", Map.of("type", "string", "description", "Optional description of this schedule."));

        return new McpSchema.Tool(
                TOOL_NAME,
                null,
                """
                Adds a cron schedule to a campaign, so it runs automatically on a recurring basis.

                Call this tool whenever the user asks to schedule, automate, or set up a recurring
                run for a campaign. A campaign can have several schedules.

                cronDefinition must be a valid Quartz cron expression, not a standard 5-field crontab
                expression — Quartz expects seconds first (6 or 7 fields total).
                """,
                new McpSchema.JsonSchema(
                        "object",
                        properties,
                        List.of("campaign", "cronDefinition"),
                        null,
                        null,
                        null
                ),
                null,
                MCPToolUtils.createAnnotations("Schedule campaign", false),
                null
        );
    }

    private McpSchema.CallToolResult execute(Map<String, Object> args) {
        String campaignName = MCPToolUtils.getString(args, "campaign", "");
        String cronDefinition = MCPToolUtils.getString(args, "cronDefinition", "");
        boolean active = MCPToolUtils.getBoolean(args, "active", true);
        String description = MCPToolUtils.getString(args, "description", "");
        String user = MCPToolUtils.getString(args, "user", "MCP");

        mcpLogUtils.call(TOOL_NAME, "campaign_scheduler_create",
                String.format("MCP tool %s called with campaign=%s cronDefinition=%s", TOOL_NAME, campaignName, cronDefinition));

        if (campaignName.isBlank()) {
            return MCPToolUtils.errorText("Missing required parameter: campaign");
        }
        if (cronDefinition.isBlank()) {
            return MCPToolUtils.errorText("Missing required parameter: cronDefinition");
        }
        if (!CronExpression.isValidExpression(cronDefinition)) {
            return MCPToolUtils.errorText("'" + cronDefinition + "' is not a valid Quartz cron expression.");
        }

        ScheduleEntry entry = new ScheduleEntry();
        entry.setType(SCHEDULE_TYPE_CAMPAIGN);
        entry.setName(campaignName);
        entry.setCronDefinition(cronDefinition);
        entry.setActive(active ? "Y" : "N");
        entry.setDescription(description);
        entry.setUsrCreated(user);

        Answer answer = scheduleEntryService.create(entry);

        if (!answer.isCodeStringEquals("OK")) {
            return MCPToolUtils.errorText("Unable to create schedule for campaign " + campaignName + ": " + answer.getMessageDescription());
        }

        myVersionService.updateMyVersionString(SCHEDULER_VERSION_KEY, String.valueOf(new Date()));
        schedulerInit.init();

        Map<String, Object> response = new LinkedHashMap<>();
        if (answer instanceof AnswerItem<?> answerItem && answerItem.getItem() instanceof Integer generatedId) {
            response.put("id", generatedId);
        }
        response.put("status", "created");
        response.put("campaign", campaignName);
        response.put("cronDefinition", cronDefinition);
        response.put("active", active);

        return MCPToolUtils.successJson(response);
    }
}