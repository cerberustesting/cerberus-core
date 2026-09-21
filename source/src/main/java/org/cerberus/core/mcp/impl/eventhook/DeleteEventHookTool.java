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
import org.cerberus.core.util.answer.Answer;
import org.cerberus.core.util.answer.AnswerItem;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

/**
 * MCP tool that removes a notification hook from a campaign, under the tool name
 * {@code cerberus_eventhook_delete}.
 *
 * <p>Delegates to {@link IEventHookService#delete(EventHook)} after verifying the hook belongs to
 * the given campaign, the same defensive check used by the update tool.</p>
 */
@Component
public class DeleteEventHookTool implements MCPTool {

    private static final String TOOL_NAME = "cerberus_eventhook_delete";

    private static final List<String> EVENT_REFERENCES = List.of(
            EventHook.EVENTREFERENCE_CAMPAIGN_START,
            EventHook.EVENTREFERENCE_CAMPAIGN_END,
            EventHook.EVENTREFERENCE_CAMPAIGN_END_CIKO
    );

    private final IEventHookService eventHookService;
    private final MCPLogUtils mcpLogUtils;

    public DeleteEventHookTool(IEventHookService eventHookService, MCPLogUtils mcpLogUtils) {
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
                Removes a notification hook from a campaign, so it stops firing on that event.

                Call this tool whenever the user asks to remove, disable for good, or delete a
                campaign notification. Use cerberus_eventhook_list first to get the id.
                To temporarily stop a hook without deleting it, use cerberus_eventhook_update
                with active=false instead.
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
                MCPToolUtils.deleteAnnotations("Delete campaign notification hook", false),
                null
        );
    }

    private McpSchema.CallToolResult execute(Map<String, Object> args) {
        String campaignName = MCPToolUtils.getString(args, "campaign", "");
        int id = MCPToolUtils.getInteger(args, "id", 0);

        mcpLogUtils.call(TOOL_NAME, "eventhook_delete",
                String.format("MCP tool %s called with campaign=%s id=%d", TOOL_NAME, campaignName, id));

        if (campaignName.isBlank()) {
            return MCPToolUtils.errorText("Missing required parameter: campaign");
        }
        if (id <= 0) {
            return MCPToolUtils.errorText("Missing or invalid required parameter: id");
        }

        AnswerItem<EventHook> readAnswer = eventHookService.readByKey(id);
        if (!readAnswer.isCodeStringEquals("OK") || readAnswer.getItem() == null) {
            return MCPToolUtils.errorText("Notification hook does not exist: " + id);
        }

        EventHook hook = readAnswer.getItem();
        if (!campaignName.equalsIgnoreCase(hook.getObjectKey1()) || !EVENT_REFERENCES.contains(hook.getEventReference())) {
            return MCPToolUtils.errorText("Notification hook " + id + " does not belong to campaign " + campaignName);
        }

        Answer deleteAnswer = eventHookService.delete(hook);
        if (!deleteAnswer.isCodeStringEquals("OK")) {
            return MCPToolUtils.errorText("Unable to delete notification hook " + id + ": " + deleteAnswer.getMessageDescription());
        }

        return MCPToolUtils.successJson(Map.of(
                "status", "deleted",
                "id", id,
                "campaign", campaignName
        ));
    }
}