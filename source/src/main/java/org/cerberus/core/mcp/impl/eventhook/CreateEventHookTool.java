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
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * MCP tool that adds a notification hook to a campaign, under the tool name
 * {@code cerberus_eventhook_create}.
 *
 * <p>{@code eventReference} is restricted to the three campaign-scoped values Cerberus fires:
 * {@link EventHook#EVENTREFERENCE_CAMPAIGN_START}, {@link EventHook#EVENTREFERENCE_CAMPAIGN_END}
 * and {@link EventHook#EVENTREFERENCE_CAMPAIGN_END_CIKO} (end only when the CI gate failed).
 * Delegates to {@link IEventHookService#create(EventHook)} — there is no uniqueness constraint on
 * (campaign, eventReference, connector), so a campaign can have several hooks for the same event,
 * e.g. one Slack channel and one email recipient both on {@code CAMPAIGN_END}.</p>
 */
@Component
public class CreateEventHookTool implements MCPTool {

    private static final String TOOL_NAME = "cerberus_eventhook_create";

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

    public CreateEventHookTool(IEventHookService eventHookService, MCPLogUtils mcpLogUtils) {
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
        Map<String, Object> properties = new LinkedHashMap<>();
        properties.put("campaign", Map.of("type", "string", "description", "Name of the campaign to notify on."));
        properties.put("eventReference", Map.of(
                "type", "string",
                "description", "CAMPAIGN_START fires when the campaign launches. CAMPAIGN_END fires on every "
                        + "finished run. CAMPAIGN_END_CIKO fires only when the run ends with a failing CI score.",
                "enum", EVENT_REFERENCES
        ));
        properties.put("hookConnector", Map.of(
                "type", "string",
                "description", "Notification channel type.",
                "enum", HOOK_CONNECTORS
        ));
        properties.put("hookRecipient", Map.of(
                "type", "string",
                "description", "Recipient for the connector: an email address for EMAIL, a webhook URL for "
                        + "SLACK/TEAMS/GOOGLE-CHAT/GENERIC."
        ));
        properties.put("hookChannel", Map.of("type", "string", "description", "Optional channel name, used by some connectors (e.g. Slack channel)."));
        properties.put("description", Map.of("type", "string", "description", "Optional description of this hook."));
        properties.put("active", Map.of("type", "boolean", "description", "Whether the hook is enabled. Defaults to true."));

        return new McpSchema.Tool(
                TOOL_NAME,
                null,
                """
                Adds a notification hook (Slack, Teams, email, or generic webhook) that fires when a
                campaign starts or ends.

                Call this tool whenever the user asks to set up, add, or configure a notification for
                a campaign. A campaign can have several hooks, including several for the same event.

                Use CAMPAIGN_END_CIKO instead of CAMPAIGN_END if the user only wants to be notified on failure.
                """,
                new McpSchema.JsonSchema(
                        "object",
                        properties,
                        List.of("campaign", "eventReference", "hookConnector", "hookRecipient"),
                        null,
                        null,
                        null
                ),
                null,
                MCPToolUtils.createAnnotations("Add campaign notification hook", false),
                null
        );
    }

    private McpSchema.CallToolResult execute(Map<String, Object> args) {
        String campaignName = MCPToolUtils.getString(args, "campaign", "");
        String eventReference = MCPToolUtils.getString(args, "eventReference", "");
        String hookConnector = MCPToolUtils.getString(args, "hookConnector", "");
        String hookRecipient = MCPToolUtils.getString(args, "hookRecipient", "");
        String hookChannel = MCPToolUtils.getString(args, "hookChannel", "");
        String description = MCPToolUtils.getString(args, "description", "");
        boolean active = MCPToolUtils.getBoolean(args, "active", true);
        String user = MCPToolUtils.getString(args, "user", "MCP");

        mcpLogUtils.call(TOOL_NAME, "eventhook_create",
                String.format("MCP tool %s called with campaign=%s eventReference=%s hookConnector=%s",
                        TOOL_NAME, campaignName, eventReference, hookConnector));

        if (campaignName.isBlank()) {
            return MCPToolUtils.errorText("Missing required parameter: campaign");
        }
        if (!EVENT_REFERENCES.contains(eventReference)) {
            return MCPToolUtils.errorText("Invalid eventReference '" + eventReference + "'. Expected one of " + EVENT_REFERENCES);
        }
        if (!HOOK_CONNECTORS.contains(hookConnector)) {
            return MCPToolUtils.errorText("Invalid hookConnector '" + hookConnector + "'. Expected one of " + HOOK_CONNECTORS);
        }
        if (hookRecipient.isBlank()) {
            return MCPToolUtils.errorText("Missing required parameter: hookRecipient");
        }

        EventHook hook = new EventHook();
        hook.setEventReference(eventReference);
        hook.setObjectKey1(campaignName);
        hook.setObjectKey2("");
        hook.setActive(active);
        hook.setHookConnector(hookConnector);
        hook.setHookRecipient(hookRecipient);
        hook.setHookChannel(hookChannel);
        hook.setDescription(description);
        hook.setUsrCreated(user);

        Answer answer = eventHookService.create(hook);

        if (!answer.isCodeStringEquals("OK")) {
            return MCPToolUtils.errorText("Unable to create notification hook for campaign " + campaignName + ": " + answer.getMessageDescription());
        }

        return MCPToolUtils.successJson(Map.of(
                "status", "created",
                "campaign", campaignName,
                "eventReference", eventReference,
                "hookConnector", hookConnector
        ));
    }
}