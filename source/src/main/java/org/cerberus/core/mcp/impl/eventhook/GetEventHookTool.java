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
import org.cerberus.core.util.answer.AnswerItem;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * MCP tool that retrieves the details of a single campaign notification hook, under the tool name
 * {@code cerberus_eventhook_get}.
 *
 * <p>Delegates to {@link IEventHookService#readByKey(Integer)}, then defensively verifies the hook
 * belongs to the given campaign and is one of the three campaign-scoped events — the
 * {@code eventhook} table has no foreign key tying {@code objectKey1} to an actual campaign, and
 * the same id space is shared with execution/testcase/environment hooks.</p>
 */
@Component
public class GetEventHookTool implements MCPTool {

    private static final String TOOL_NAME = "cerberus_eventhook_get";

    private static final List<String> EVENT_REFERENCES = List.of(
            EventHook.EVENTREFERENCE_CAMPAIGN_START,
            EventHook.EVENTREFERENCE_CAMPAIGN_END,
            EventHook.EVENTREFERENCE_CAMPAIGN_END_CIKO
    );

    private final IEventHookService eventHookService;
    private final MCPLogUtils mcpLogUtils;

    public GetEventHookTool(IEventHookService eventHookService, MCPLogUtils mcpLogUtils) {
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
                "campaign", Map.of("type", "string", "description", "Name of the campaign the hook belongs to."),
                "id", Map.of("type", "integer", "description", "Hook id, as returned by cerberus_eventhook_list.")
        );

        return new McpSchema.Tool(
                TOOL_NAME,
                null,
                """
                Retrieves the full details of a single campaign notification hook (trigger event,
                connector, recipient, channel, active flag, and who last changed it).

                Call this tool when the user asks about a specific notification hook on a campaign.
                Use cerberus_eventhook_list first to get the id.
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
                MCPToolUtils.readOnlyAnnotations("Get campaign notification hook", true),
                null
        );
    }

    private McpSchema.CallToolResult execute(Map<String, Object> args) {
        String campaignName = MCPToolUtils.getString(args, "campaign", "");
        int id = MCPToolUtils.getInteger(args, "id", 0);

        mcpLogUtils.call(TOOL_NAME, "eventhook_get",
                String.format("MCP tool %s called with campaign=%s id=%d", TOOL_NAME, campaignName, id));

        if (campaignName.isBlank()) {
            return MCPToolUtils.errorText("Missing required parameter: campaign");
        }
        if (id <= 0) {
            return MCPToolUtils.errorText("Missing or invalid required parameter: id");
        }

        AnswerItem<EventHook> answer = eventHookService.readByKey(id);
        if (!answer.isCodeStringEquals("OK") || answer.getItem() == null) {
            return MCPToolUtils.errorText("Notification hook does not exist: " + id);
        }

        EventHook hook = answer.getItem();
        if (!campaignName.equalsIgnoreCase(hook.getObjectKey1()) || !EVENT_REFERENCES.contains(hook.getEventReference())) {
            return MCPToolUtils.errorText("Notification hook " + id + " does not belong to campaign " + campaignName);
        }

        return MCPToolUtils.successJson(toDetail(hook));
    }

    private Map<String, Object> toDetail(EventHook hook) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("id", hook.getId());
        map.put("campaign", MCPToolUtils.nullSafe(hook.getObjectKey1()));
        map.put("eventReference", MCPToolUtils.nullSafe(hook.getEventReference()));
        map.put("active", hook.isActive());
        map.put("hookConnector", MCPToolUtils.nullSafe(hook.getHookConnector()));
        map.put("hookRecipient", MCPToolUtils.nullSafe(hook.getHookRecipient()));
        map.put("hookChannel", MCPToolUtils.nullSafe(hook.getHookChannel()));
        map.put("description", MCPToolUtils.nullSafe(hook.getDescription()));
        map.put("usrCreated", MCPToolUtils.nullSafe(hook.getUsrCreated()));
        map.put("dateCreated", hook.getDateCreated());
        map.put("usrModif", MCPToolUtils.nullSafe(hook.getUsrModif()));
        map.put("dateModif", hook.getDateModif());
        return map;
    }
}