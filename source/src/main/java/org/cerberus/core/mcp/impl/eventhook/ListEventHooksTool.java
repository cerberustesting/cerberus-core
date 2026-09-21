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
package org.cerberus.core.mcp.impl.eventhook;

import io.modelcontextprotocol.server.McpServerFeatures;
import io.modelcontextprotocol.spec.McpSchema;
import org.cerberus.core.crud.entity.EventHook;
import org.cerberus.core.crud.service.IEventHookService;
import org.cerberus.core.mcp.MCPTool;
import org.cerberus.core.mcp.util.MCPLogUtils;
import org.cerberus.core.mcp.util.MCPToolUtils;
import org.cerberus.core.util.answer.AnswerList;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * MCP tool that lists the notification hooks configured for a campaign, under the tool name
 * {@code cerberus_eventhook_list}.
 *
 * <p>Delegates to {@link IEventHookService#readByCampaign(String)}, which already filters to the
 * three campaign-scoped event references ({@code CAMPAIGN_START}, {@code CAMPAIGN_END},
 * {@code CAMPAIGN_END_CIKO}) with {@code objectKey1} equal to the campaign name.</p>
 */
@Component
public class ListEventHooksTool implements MCPTool {

    private static final String TOOL_NAME = "cerberus_eventhook_list";

    private final IEventHookService eventHookService;
    private final MCPLogUtils mcpLogUtils;

    public ListEventHooksTool(IEventHookService eventHookService, MCPLogUtils mcpLogUtils) {
        this.eventHookService = eventHookService;
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
                "campaign", Map.of("type", "string", "description", "Name of the campaign whose notification hooks should be listed.")
        );

        return new McpSchema.Tool(
                TOOL_NAME,
                null,
                """
                Lists the notification hooks (Slack/Teams/email/webhook) configured to fire when a
                campaign starts or ends.

                Call this tool when the user asks what notifications are set up for a campaign, or
                before adding/removing one to see what already exists and get the hook id needed by
                cerberus_eventhook_update / _delete.
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
                MCPToolUtils.readOnlyAnnotations("List campaign notification hooks", false),
                null
        );
    }

    private McpSchema.CallToolResult execute(Map<String, Object> args) {
        String campaignName = MCPToolUtils.getString(args, "campaign", "");

        mcpLogUtils.call(TOOL_NAME, "eventhook_list",
                String.format("MCP tool %s called with campaign=%s", TOOL_NAME, campaignName));

        if (campaignName.isBlank()) {
            return MCPToolUtils.errorText("Missing required parameter: campaign");
        }

        AnswerList<EventHook> answer = eventHookService.readByCampaign(campaignName);

        List<Map<String, Object>> hooks = answer.getDataList().stream()
                .map(this::toSummary)
                .toList();

        return MCPToolUtils.successJson(Map.of(
                "campaign", campaignName,
                "count", hooks.size(),
                "hooks", hooks
        ));
    }

    private Map<String, Object> toSummary(EventHook hook) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("id", hook.getId());
        map.put("eventReference", MCPToolUtils.nullSafe(hook.getEventReference()));
        map.put("active", hook.isActive());
        map.put("hookConnector", MCPToolUtils.nullSafe(hook.getHookConnector()));
        map.put("hookRecipient", MCPToolUtils.nullSafe(hook.getHookRecipient()));
        map.put("hookChannel", MCPToolUtils.nullSafe(hook.getHookChannel()));
        map.put("description", MCPToolUtils.nullSafe(hook.getDescription()));
        return map;
    }
}