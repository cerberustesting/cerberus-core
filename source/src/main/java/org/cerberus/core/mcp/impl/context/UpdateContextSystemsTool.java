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
package org.cerberus.core.mcp.impl.context;

import io.modelcontextprotocol.server.McpServerFeatures;
import io.modelcontextprotocol.server.McpSyncServerExchange;
import io.modelcontextprotocol.spec.McpSchema;
import org.cerberus.core.crud.entity.User;
import org.cerberus.core.exception.CerberusException;
import org.cerberus.core.mcp.MCPTool;
import org.cerberus.core.mcp.util.MCPLogUtils;
import org.cerberus.core.mcp.util.MCPToolUtils;
import org.cerberus.core.mcp.util.MCPUserContextService;
import org.cerberus.core.websocket.WebSocketEventSender;
import org.cerberus.core.websocket.WebSocketStatic;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * MCP tool that adds or removes systems ("workspaces" in the UI — {@code System} and
 * {@code workspace} name the same concept, see {@code header2.js#ChangeWorkspace}) from the
 * caller's active system context.
 *
 * <p>Exposed MCP tool name: {@code cerberus_context_system_update}.</p>
 *
 * <p>{@code action=add} is rejected for any system the caller is not granted in
 * {@link org.cerberus.core.crud.entity.UserSystem} — this is the enforcement point that keeps
 * the active context ({@link User#getDefaultSystem()}) a subset of what the caller is allowed
 * to touch. {@code action=remove} accepts any currently active system, no allow-list check
 * needed since it only narrows the context.</p>
 *
 * <p>On success, pushes {@link WebSocketStatic#CHANNEL_USERCONTEXT_UPDATE} to every session of
 * the caller ({@code sendToUser}, not {@code sendToAppSession} — the login is already known and
 * reaches every open tab, not just the one that issued the call), so the UI's workspace selector
 * can refresh without a full page reload when the context changes from a chat command rather
 * than the header dropdown itself. See {@code rightPanel.html}'s {@code handleObjectSideEffects}
 * for the receiving end.</p>
 */
@Component
public class UpdateContextSystemsTool implements MCPTool {

    private static final String TOOL_NAME = "cerberus_context_system_update";

    private final MCPUserContextService userContext;
    private final MCPLogUtils mcpLogUtils;

    @Autowired
    private WebSocketEventSender webSocketEventSender;

    public UpdateContextSystemsTool(MCPUserContextService userContext, MCPLogUtils mcpLogUtils) {
        this.userContext = userContext;
        this.mcpLogUtils = mcpLogUtils;
    }

    @Override
    public McpServerFeatures.SyncToolSpecification toToolSpecification() {
        return new McpServerFeatures.SyncToolSpecification(
                createTool(),
                (exchange, request) -> execute(MCPToolUtils.argumentsOrEmpty(request.arguments()), exchange)
        );
    }

    private McpSchema.Tool createTool() {
        Map<String, Object> properties = Map.of(
                "action", Map.of(
                        "type", "string",
                        "description", "Whether to add systems/workspaces to, or remove them from, the active context.",
                        "enum", List.of("add", "remove")
                ),
                "systems", Map.of(
                        "type", "array",
                        "items", Map.of("type", "string"),
                        "description", "One or more system (a.k.a. workspace) names to add or remove. When action "
                                + "is 'add', every name must already be present in the caller's allowed systems "
                                + "(see cerberus_context_system_list)."
                )
        );

        return new McpSchema.Tool(
                TOOL_NAME,
                null,
                """
                Adds systems to, or removes systems from, the caller's active Cerberus system context.

                In the Cerberus UI, a "system" is also called a "workspace" (the header's workspace
                selector). Use this tool whenever the user asks to switch, activate, add, or leave a
                workspace — not just when they literally say "system".

                Call cerberus_context_system_list first to see the current active systems/workspaces and
                the full list the caller is allowed to use.

                action=add is rejected if any requested system is not in the caller's allowed systems.
                action=remove accepts any currently active system.

                Other MCP tools immediately start filtering reads to, and restricting writes to, the
                resulting active context.
                """,
                new McpSchema.JsonSchema("object", properties, List.of("action", "systems"), null, null, null),
                null,
                MCPToolUtils.updateAnnotations("Update active system context", true),
                null
        );
    }

    private McpSchema.CallToolResult execute(Map<String, Object> args, McpSyncServerExchange exchange) {
        String action = MCPToolUtils.getString(args, "action", "").toLowerCase();
        List<String> requested = MCPToolUtils.getStringList(args, "systems", List.of());
        String login = userContext.getLogin(exchange);

        mcpLogUtils.call(TOOL_NAME, "context_system_update",
                String.format("MCP tool %s called with action=%s, systems=%s", TOOL_NAME, action, requested), login);

        if (!"add".equals(action) && !"remove".equals(action)) {
            return MCPToolUtils.errorText("Invalid 'action': must be 'add' or 'remove'.");
        }
        if (requested.isEmpty()) {
            return MCPToolUtils.errorText("Missing required parameter: systems (non-empty list of system names).");
        }

        if (login == null) {
            return MCPToolUtils.errorText("Unable to resolve the authenticated MCP user for this call.");
        }

        try {
            User user = userContext.getUser(login);
            List<String> current = new ArrayList<>(userContext.getContextSystems(user));

            if ("add".equals(action)) {
                List<String> allowed = userContext.getAllowedSystems(login);
                List<String> notAllowed = requested.stream()
                        .filter(requestedSystem -> allowed.stream().noneMatch(requestedSystem::equalsIgnoreCase))
                        .toList();
                if (!notAllowed.isEmpty()) {
                    return MCPToolUtils.errorText(
                            "Cannot add system(s) " + notAllowed + ": not in the caller's allowed systems "
                                    + allowed + ". Ask an administrator to grant access first.");
                }
                for (String requestedSystem : requested) {
                    // Resolve to the allow-list's exact casing so activeSystems values line up with
                    // allowedSystems values instead of drifting on caller-supplied casing.
                    String canonical = allowed.stream()
                            .filter(requestedSystem::equalsIgnoreCase)
                            .findFirst()
                            .orElse(requestedSystem);
                    if (current.stream().noneMatch(canonical::equalsIgnoreCase)) {
                        current.add(canonical);
                    }
                }
            } else {
                current.removeIf(activeSystem -> requested.stream().anyMatch(activeSystem::equalsIgnoreCase));
            }

            userContext.saveContextSystems(user, current);

            mcpLogUtils.success(TOOL_NAME, "context_system_update",
                    String.format("User %s ran action=%s on system(s) %s, active context is now %s",
                            login, action, requested, current), login);

            // Notify every open tab of this user (not just the one that made the call — a chat
            // command in one tab should still update the workspace selector in others) so it
            // reflects the change immediately, the same way rightPanel.html's
            // handleObjectSideEffects refreshes the cached user/workspace list when a SYSTEM
            // invariant is created. Shaped like ObjectChangeHistory's
            // {action, objectName, objectId, object} so the front-end's generic activity-event
            // handler (normalizeObjectActivityEvent) recognizes it; see the "DefaultSystem" case
            // added to handleObjectSideEffects. sendToUser (not sendToAppSession) because it only
            // needs the login we already resolved — no dependency on the calling appSessionID
            // matching whatever session the receiving tab registered under.
            webSocketEventSender.sendToUser(login, WebSocketStatic.CHANNEL_USERCONTEXT_UPDATE,
                    Map.of(
                            "action", "update",
                            "objectName", "DefaultSystem",
                            "objectId", login,
                            "object", Map.of("activeSystems", current)
                    ));

            return MCPToolUtils.successJson(Map.of(
                    "login", login,
                    "action", action,
                    "activeSystems", current
            ));
        } catch (CerberusException e) {
            return MCPToolUtils.errorText(
                    "Unable to update system context for '" + login + "': " + e.getMessageError().getDescription());
        }
    }
}