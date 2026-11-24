/**
 * Cerberus Copyright (C) 2013 - 2026 cerberustesting
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
package org.cerberus.core.mcp.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.modelcontextprotocol.server.McpServerFeatures;
import io.modelcontextprotocol.server.McpSyncServerExchange;
import io.modelcontextprotocol.spec.McpSchema;
import org.cerberus.core.config.cerberus.Property;
import org.cerberus.core.crud.service.IParameterService;
import org.cerberus.core.mcp.MCPTool;
import org.cerberus.core.mcp.util.MCPLogUtils;
import org.cerberus.core.mcp.util.MCPToolUtils;
import org.cerberus.core.mcp.util.MCPUserContextService;
import org.springframework.stereotype.Component;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * MCP tool that explains how to install the Cerberus MCP server in an external AI client
 * (Claude Code, Claude.ai / Claude Desktop, ChatGPT, Cursor, VS Code).
 *
 * <p>Exposed MCP tool name: {@code cerberus_mcp_client_setup}.</p>
 *
 * <p>Mirrors the "Configuration par client" documentation of the MCP Inspector page
 * ({@code McpInspector.js}): same server definition, same authentication rules. The instance
 * decides the authentication mode: OAuth when Keycloak is enabled and a dedicated MCP client id is
 * configured ({@code org.cerberus.keycloak.mcpclient}), API key otherwise.</p>
 *
 * <p>The endpoint address comes from the {@code cerberus_gui_url} parameter (falling back on
 * {@code cerberus_url}) because an MCP tool call carries no browser URL. A real API key is never
 * returned: snippets carry a placeholder the user replaces with their own key.</p>
 */
@Component
public class McpClientSetupTool implements MCPTool {

    private static final String TOOL_NAME = "cerberus_mcp_client_setup";
    private static final String API_KEY_PLACEHOLDER = "<votre clé API>";

    private static final String CLIENT_CLAUDE_CODE = "claude_code";
    private static final String CLIENT_CLAUDE_AI = "claude_ai";
    private static final String CLIENT_CHATGPT = "chatgpt";
    private static final String CLIENT_CURSOR = "cursor";
    private static final String CLIENT_VSCODE = "vscode";
    private static final List<String> CLIENTS = List.of(
            CLIENT_CLAUDE_CODE, CLIENT_CLAUDE_AI, CLIENT_CHATGPT, CLIENT_CURSOR, CLIENT_VSCODE);

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private final IParameterService parameterService;
    private final MCPUserContextService userContext;
    private final MCPLogUtils mcpLogUtils;

    public McpClientSetupTool(IParameterService parameterService, MCPUserContextService userContext, MCPLogUtils mcpLogUtils) {
        this.parameterService = parameterService;
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
                "client", Map.of(
                        "type", "string",
                        "description", "The external AI tool to configure. Omit it to get the instructions for all of them.",
                        "enum", CLIENTS
                )
        );

        return new McpSchema.Tool(
                TOOL_NAME,
                null,
                """
                Explains how to install the Cerberus MCP server in an EXTERNAL AI tool, so that tool can
                drive this Cerberus instance (Claude Code, Claude.ai / Claude Desktop, ChatGPT, Cursor,
                VS Code).

                Use this tool whenever the user asks how to connect, install, add or configure Cerberus
                (or "the MCP", "the MCP server") in Claude, ChatGPT, Cursor, VS Code or any other AI tool.
                If the user did not say which tool they use, ask them first, or call this tool without
                'client' to get everything.

                Returns the MCP endpoint URL of this instance, the authentication mode this instance
                requires (OAuth with an imposed client id, or API key), and for each client a ready-to-use
                command, link or step-by-step instructions. Present those to the user as they are:
                do not invent URLs, client ids or commands.

                A real API key is never returned: when authentication is by API key, the snippets contain
                the placeholder "<votre clé API>" that the user must replace with their own API key.
                Claude.ai and ChatGPT cannot register a server by command: they are configured by hand in
                their interface, with the URL (and client id) returned here.
                """,
                new McpSchema.JsonSchema("object", properties, List.of(), null, null, null),
                null,
                MCPToolUtils.readOnlyAnnotations("MCP client setup help", true),
                null
        );
    }

    private McpSchema.CallToolResult execute(Map<String, Object> args, McpSyncServerExchange exchange) {
        String client = MCPToolUtils.getString(args, "client", "").trim().toLowerCase();
        String login = userContext.getLogin(exchange);

        mcpLogUtils.call(TOOL_NAME, "mcp_client_setup",
                String.format("MCP tool %s called with client=%s", TOOL_NAME, client), login);

        if (!client.isEmpty() && !CLIENTS.contains(client)) {
            return MCPToolUtils.errorText("Invalid 'client': must be one of " + CLIENTS + " (or omitted for all).");
        }

        try {
            String endpointUrl = endpointUrl();
            String mcpClientId = Property.isKeycloak() ? blankToNull(System.getProperty(Property.KEYCLOAKMCPCLIENT)) : null;
            boolean oauth = mcpClientId != null;

            Map<String, Object> serverDef = serverDefinition(endpointUrl, oauth, mcpClientId);

            Map<String, Object> result = new LinkedHashMap<>();
            result.put("endpointUrl", endpointUrl);
            result.put("authentication", oauth ? "oauth" : "apiKey");
            if (oauth) {
                result.put("oauthClientId", mcpClientId);
            }
            List<String> notes = new ArrayList<>();
            notes.add(oauth
                    ? "OAuth: no header to configure. The AI tool discovers the Keycloak login itself from the 401 answer of the endpoint."
                    : "API key: the AI tool must send the header X-API-KEY with the user's own Cerberus API key. Replace the placeholder "
                    + API_KEY_PLACEHOLDER + " by it; never share or paste it anywhere else.");
            if (endpointUrl.contains("localhost") || endpointUrl.contains("127.0.0.1")) {
                notes.add("The configured Cerberus URL (cerberus_gui_url / cerberus_url) points to localhost: if the AI tool does not run on "
                        + "the same machine, or the user reaches Cerberus through another address, replace the host part of the URL.");
            }
            result.put("notes", notes);

            Map<String, Object> clients = new LinkedHashMap<>();
            if (client.isEmpty() || CLIENT_CLAUDE_CODE.equals(client)) {
                clients.put(CLIENT_CLAUDE_CODE, claudeCode(serverDef));
            }
            if (client.isEmpty() || CLIENT_CLAUDE_AI.equals(client)) {
                clients.put(CLIENT_CLAUDE_AI, claudeAi(endpointUrl, oauth, mcpClientId));
            }
            if (client.isEmpty() || CLIENT_CHATGPT.equals(client)) {
                clients.put(CLIENT_CHATGPT, chatGpt(endpointUrl, oauth, mcpClientId));
            }
            if (client.isEmpty() || CLIENT_CURSOR.equals(client)) {
                clients.put(CLIENT_CURSOR, cursor(serverDef));
            }
            if (client.isEmpty() || CLIENT_VSCODE.equals(client)) {
                clients.put(CLIENT_VSCODE, vsCode(serverDef));
            }
            result.put("clients", clients);

            return MCPToolUtils.successJson(result);
        } catch (JsonProcessingException e) {
            return MCPToolUtils.errorText("Unable to build the MCP client configuration: " + e.getMessage());
        }
    }

    /** Public address of the MCP endpoint: cerberus_gui_url (or cerberus_url) + /mcp, context path included. */
    private String endpointUrl() {
        String base = parameterService.getParameterStringByKey("cerberus_gui_url", "", "");
        if (base == null || base.isBlank()) {
            base = parameterService.getParameterStringByKey("cerberus_url", "", "");
        }
        base = base == null ? "" : base.trim();
        while (base.endsWith("/")) {
            base = base.substring(0, base.length() - 1);
        }
        return base + "/mcp";
    }

    /** Same definition as McpInspector.js#connectionServerDef. */
    private Map<String, Object> serverDefinition(String endpointUrl, boolean oauth, String mcpClientId) {
        Map<String, Object> server = new LinkedHashMap<>();
        server.put("type", "http");
        server.put("url", endpointUrl);
        if (oauth) {
            server.put("oauth", Map.of("clientId", mcpClientId));
        } else {
            server.put("headers", Map.of("X-API-KEY", API_KEY_PLACEHOLDER));
        }
        return server;
    }

    private Map<String, Object> claudeCode(Map<String, Object> serverDef) throws JsonProcessingException {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("kind", "command");
        m.put("where", "Terminal, where Claude Code is installed.");
        m.put("command", "claude mcp add-json cerberus '" + OBJECT_MAPPER.writeValueAsString(serverDef) + "'");
        return m;
    }

    private Map<String, Object> cursor(Map<String, Object> serverDef) throws JsonProcessingException {
        Map<String, Object> def = new LinkedHashMap<>(serverDef);
        def.remove("type");
        String config = Base64.getEncoder().encodeToString(OBJECT_MAPPER.writeValueAsString(def).getBytes(StandardCharsets.UTF_8));
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("kind", "link");
        m.put("where", "Open the link in a browser on the machine where Cursor is installed; Cursor asks to confirm the install.");
        m.put("link", "cursor://anysphere.cursor-deeplink/mcp/install?name=cerberus&config=" + config);
        return m;
    }

    private Map<String, Object> vsCode(Map<String, Object> serverDef) throws JsonProcessingException {
        Map<String, Object> def = new LinkedHashMap<>();
        def.put("name", "cerberus");
        def.putAll(serverDef);
        String encoded = URLEncoder.encode(OBJECT_MAPPER.writeValueAsString(def), StandardCharsets.UTF_8).replace("+", "%20");
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("kind", "link");
        m.put("where", "Open the link in a browser on the machine where VS Code is installed; VS Code asks to confirm the install.");
        m.put("link", "vscode:mcp/install?" + encoded);
        return m;
    }

    private Map<String, Object> claudeAi(String endpointUrl, boolean oauth, String mcpClientId) {
        List<String> steps = new ArrayList<>();
        steps.add("Settings → Connectors → Add custom connector.");
        steps.add("Name: Cerberus, URL: " + endpointUrl);
        if (oauth) {
            steps.add("Advanced settings → OAuth Client ID: " + mcpClientId);
        } else {
            steps.add("Web connectors cannot send an X-API-KEY header: use OAuth (not enabled on this instance), "
                    + "or Claude Code / Claude Desktop with a local configuration instead.");
        }
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("kind", "manual");
        m.put("where", "Claude.ai or Claude Desktop interface (no command or API exists to register the server).");
        m.put("steps", steps);
        return m;
    }

    private Map<String, Object> chatGpt(String endpointUrl, boolean oauth, String mcpClientId) {
        List<String> steps = new ArrayList<>();
        steps.add("Settings → Apps & Connectors → Advanced → enable developer mode.");
        steps.add("Create: URL " + endpointUrl + ", authentication OAuth.");
        if (oauth) {
            steps.add("Client ID: " + mcpClientId);
        } else {
            steps.add("OAuth is not enabled on this Cerberus instance, and ChatGPT connectors cannot send an X-API-KEY header: "
                    + "ChatGPT cannot be connected to this instance until OAuth (Keycloak MCP client) is configured.");
        }
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("kind", "manual");
        m.put("where", "ChatGPT interface (no command or API exists to register the server).");
        m.put("steps", steps);
        return m;
    }

    private static String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value;
    }
}