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

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * MCP tool that updates an existing campaign notification hook, under the tool name
 * {@code cerberus_eventhook_update}.
 *
 * <p>Read-before-write on {@link IEventHookService#readByKey(Integer)}, and defensively verifies
 * the hook both belongs to the given campaign ({@code objectKey1}) and is one of the three
 * campaign-scoped events — the {@code eventhook} table has no foreign key tying {@code objectKey1}
 * to an actual campaign, and the same id space is shared with execution/testcase/environment hooks.</p>
 */
@Component
public class UpdateEventHookTool implements MCPTool {

    private static final String TOOL_NAME = "cerberus_eventhook_update";

    private static final List<String> EVENT_REFERENCES = List.of(
            EventHook.EVENTREFERENCE_CAMPAIGN_START,
            EventHook.EVENTREFERENCE_CAMPAIGN_END,
            EventHook.EVENTREFERENCE_CAMPAIGN_END_CIKO
    );

    private static final List<String> HOOK_CONNECTORS = List.of(
            EventHook.HOOKCONNECTOR_EMAIL,
            EventHook.HOOKCONNECTOR_SLACK,
            EventHook.HOOKCONNECTOR_TEAMS,
            EventHook.HOOKCONNECTOR_GOOGLECHAT,
            EventHook.HOOKCONNECTOR_GENERIC
    );

    private final IEventHookService eventHookService;
    private final MCPLogUtils mcpLogUtils;

    public UpdateEventHookTool(IEventHookService eventHookService, MCPLogUtils mcpLogUtils) {
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
        Map<String, Object> updateProperties = new LinkedHashMap<>();
        updateProperties.put("eventReference", Map.of("type", "string", "description", "New trigger event.", "enum", EVENT_REFERENCES));
        updateProperties.put("hookConnector", Map.of("type", "string", "description", "New notification channel type.", "enum", HOOK_CONNECTORS));
        updateProperties.put("hookRecipient", Map.of("type", "string", "description", "New recipient (email address or webhook URL)."));
        updateProperties.put("hookChannel", Map.of("type", "string", "description", "New channel name."));
        updateProperties.put("description", Map.of("type", "string", "description", "New description."));
        updateProperties.put("active", Map.of("type", "boolean", "description", "Enable or disable the hook."));

        Map<String, Object> updatesSchema = new LinkedHashMap<>();
        updatesSchema.put("type", "object");
        updatesSchema.put("description", "Fields to update on the notification hook.");
        updatesSchema.put("properties", updateProperties);
        updatesSchema.put("additionalProperties", false);

        Map<String, Object> properties = new LinkedHashMap<>();
        properties.put("campaign", Map.of("type", "string", "description", "Name of the campaign the hook belongs to."));
        properties.put("id", Map.of("type", "integer", "description", "Hook id, as returned by cerberus_eventhook_list."));
        properties.put("updates", updatesSchema);

        return new McpSchema.Tool(
                TOOL_NAME,
                null,
                """
                Updates an existing campaign notification hook (its trigger event, connector,
                recipient, channel, description, or active flag).

                Call this tool whenever the user asks to change or fix a campaign notification.
                Use cerberus_eventhook_list first to get the id.
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
                MCPToolUtils.updateAnnotations("Update campaign notification hook", false),
                null
        );
    }

    @SuppressWarnings("unchecked")
    private McpSchema.CallToolResult execute(Map<String, Object> args) {
        String campaignName = MCPToolUtils.getString(args, "campaign", "");
        int id = MCPToolUtils.getInteger(args, "id", 0);

        mcpLogUtils.call(TOOL_NAME, "eventhook_update",
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

        AnswerItem<EventHook> readAnswer = eventHookService.readByKey(id);
        if (!readAnswer.isCodeStringEquals("OK") || readAnswer.getItem() == null) {
            return MCPToolUtils.errorText("Notification hook does not exist: " + id);
        }

        EventHook hook = readAnswer.getItem();
        if (!campaignName.equalsIgnoreCase(hook.getObjectKey1()) || !EVENT_REFERENCES.contains(hook.getEventReference())) {
            return MCPToolUtils.errorText("Notification hook " + id + " does not belong to campaign " + campaignName);
        }

        for (Map.Entry<String, Object> update : updates.entrySet()) {
            switch (update.getKey()) {
                case "eventReference":
                    String eventReference = String.valueOf(update.getValue());
                    if (!EVENT_REFERENCES.contains(eventReference)) {
                        return MCPToolUtils.errorText("Invalid eventReference '" + eventReference + "'. Expected one of " + EVENT_REFERENCES);
                    }
                    hook.setEventReference(eventReference);
                    break;
                case "hookConnector":
                    String hookConnector = String.valueOf(update.getValue());
                    if (!HOOK_CONNECTORS.contains(hookConnector)) {
                        return MCPToolUtils.errorText("Invalid hookConnector '" + hookConnector + "'. Expected one of " + HOOK_CONNECTORS);
                    }
                    hook.setHookConnector(hookConnector);
                    break;
                case "hookRecipient":
                    hook.setHookRecipient(String.valueOf(update.getValue()));
                    break;
                case "hookChannel":
                    hook.setHookChannel(String.valueOf(update.getValue()));
                    break;
                case "description":
                    hook.setDescription(String.valueOf(update.getValue()));
                    break;
                case "active":
                    hook.setActive(Boolean.TRUE.equals(update.getValue()));
                    break;
                default:
                    return MCPToolUtils.errorText("Unsupported field for notification hook update: " + update.getKey());
            }
        }
        hook.setUsrModif(MCPToolUtils.getString(args, "user", "MCP"));

        Answer updateAnswer = eventHookService.update(hook);
        if (!updateAnswer.isCodeStringEquals("OK")) {
            return MCPToolUtils.errorText("Unable to update notification hook " + id + ": " + updateAnswer.getMessageDescription());
        }

        return MCPToolUtils.successJson(Map.of(
                "status", "updated",
                "id", id,
                "campaign", campaignName
        ));
    }
}