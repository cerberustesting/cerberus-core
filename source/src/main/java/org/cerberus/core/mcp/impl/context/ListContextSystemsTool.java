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
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

/**
 * MCP tool that reports the caller's current system context.
 *
 * <p>Exposed MCP tool name: {@code cerberus_context_system_list}.</p>
 *
 * <p>"System" and "workspace" name the same concept — the Cerberus UI calls its system selector
 * the "workspace selector" ({@code header2.js#workspaceSelector}/{@code ChangeWorkspace}) — so a
 * user asking to "see my workspaces" should route here just as much as one asking about
 * "systems".</p>
 *
 * <p>Returns two lists: {@code allowedSystems}, the full set of systems an administrator has
 * granted the caller ({@link org.cerberus.core.crud.entity.UserSystem}), and
 * {@code activeSystems}, the subset currently selected as the working context
 * ({@link User#getDefaultSystem()}). Other MCP tools filter reads to {@code activeSystems} and
 * reject writes to resources outside of it; {@link UpdateContextSystemsTool} changes it.</p>
 */
@Component
public class ListContextSystemsTool implements MCPTool {

    private static final String TOOL_NAME = "cerberus_context_system_list";

    private final MCPUserContextService userContext;
    private final MCPLogUtils mcpLogUtils;

    public ListContextSystemsTool(MCPUserContextService userContext, MCPLogUtils mcpLogUtils) {
        this.userContext = userContext;
        this.mcpLogUtils = mcpLogUtils;
    }

    @Override
    public McpServerFeatures.SyncToolSpecification toToolSpecification() {
        return new McpServerFeatures.SyncToolSpecification(
                createTool(),
                (exchange, request) -> execute(exchange)
        );
    }

    private McpSchema.Tool createTool() {
        return new McpSchema.Tool(
                TOOL_NAME,
                null,
                """
                Returns the caller's current Cerberus system context.

                In the Cerberus UI, a "system" is also called a "workspace" (the header's workspace
                selector). Use this tool when the user asks which systems or workspaces are active, or
                which ones are available to switch to.

                Call this tool first when you need to know which systems are currently active, or
                which systems are available to add to the context with cerberus_context_system_update.

                "activeSystems" is the working set: other MCP tools only read resources belonging to
                these systems, and refuse to create or update resources outside of it.
                "allowedSystems" is the full set an administrator has granted the caller; only systems
                from this list can be added to activeSystems.
                """,
                new McpSchema.JsonSchema("object", Map.of(), List.of(), null, null, null),
                null,
                MCPToolUtils.readOnlyAnnotations("List active system context", true),
                null
        );
    }

    private McpSchema.CallToolResult execute(McpSyncServerExchange exchange) {
        String login = userContext.getLogin(exchange);
        mcpLogUtils.call(TOOL_NAME, "context_system_list",
                String.format("MCP tool %s called", TOOL_NAME), login);

        if (login == null) {
            return MCPToolUtils.errorText("Unable to resolve the authenticated MCP user for this call.");
        }

        try {
            User user = userContext.getUser(login);
            List<String> active = userContext.getContextSystems(user);
            List<String> allowed = userContext.getAllowedSystems(login);

            return MCPToolUtils.successJson(Map.of(
                    "login", login,
                    "activeSystems", active,
                    "allowedSystems", allowed
            ));
        } catch (CerberusException e) {
            return MCPToolUtils.errorText(
                    "Unable to read system context for '" + login + "': " + e.getMessageError().getDescription());
        }
    }
}