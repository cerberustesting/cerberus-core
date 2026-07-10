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
package org.cerberus.core.service.ai.impl;

import com.anthropic.core.JsonValue;
import com.anthropic.models.messages.Tool;
import io.modelcontextprotocol.client.McpClient;
import io.modelcontextprotocol.client.McpSyncClient;
import io.modelcontextprotocol.client.transport.HttpClientStreamableHttpTransport;
import io.modelcontextprotocol.spec.McpSchema;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * Acts as an MCP client : connects to the configured MCP server over streamable
 * HTTP (authenticated with X-API-KEY), exposes its tools in the Anthropic format
 * and executes tool calls on behalf of Claude.
 */
@Service
public class AIMcpClientService {

    private static final Logger LOG = LogManager.getLogger(AIMcpClientService.class);

    private static final String API_KEY_HEADER = "X-API-KEY";
    private static final String MCP_ENDPOINT = "/mcp";

    /**
     * Name of the client-side-only pseudo-tool used to let Claude propose quick-reply
     * buttons phrased in whatever language the conversation is currently in. It is never
     * sent to the MCP server — {@link AIService} intercepts calls to it directly.
     */
    public static final String QUICK_REPLY_TOOL_NAME = "propose_quick_replies";

    @Autowired
    AIConfig aiConfig;

    // Reopening a client (TCP/TLS connection + MCP "initialize" handshake + tools/list) on every
    // chat message added multiple seconds of dead time before Claude even started streaming.
    // Instead, one client is kept alive per chat session and reused across messages; idle ones
    // are swept below.
    private static final Duration SESSION_CLIENT_IDLE_TIMEOUT = Duration.ofMinutes(30);
    private final Map<String, SessionMcpClient> clientsBySession = new ConcurrentHashMap<>();

    private static final class SessionMcpClient {
        final McpSyncClient client;
        final List<Tool> tools;
        volatile long lastUsedMillis;

        SessionMcpClient(McpSyncClient client, List<Tool> tools) {
            this.client = client;
            this.tools = tools;
            this.lastUsedMillis = System.currentTimeMillis();
        }
    }

    /**
     * Returns the MCP client for this chat session, opening and initializing one (plus listing
     * its tools) only on the first call. Subsequent calls for the same session reuse it.
     */
    public McpSyncClient getOrOpenSessionClient(String sessionID) {
        return sessionClientFor(sessionID).client;
    }

    /**
     * Anthropic tool definitions for this session's MCP client, fetched once when the client
     * was opened and cached alongside it.
     */
    public List<Tool> getSessionTools(String sessionID) {
        return sessionClientFor(sessionID).tools;
    }

    private SessionMcpClient sessionClientFor(String sessionID) {
        SessionMcpClient sessionClient = clientsBySession.computeIfAbsent(sessionID, id -> {
            McpSyncClient client = openClient();
            return new SessionMcpClient(client, listAnthropicTools(client));
        });
        sessionClient.lastUsedMillis = System.currentTimeMillis();
        return sessionClient;
    }

    /**
     * Discards and closes the cached client for this session, if any — e.g. after a failure,
     * so the next message opens a fresh connection instead of reusing a possibly broken one.
     */
    public void invalidateSessionClient(String sessionID) {
        SessionMcpClient sessionClient = clientsBySession.remove(sessionID);
        if (sessionClient != null) {
            try {
                sessionClient.client.closeGracefully();
            } catch (Exception ignored) {
            }
        }
    }

    @Scheduled(fixedRate = 5, timeUnit = java.util.concurrent.TimeUnit.MINUTES)
    void closeIdleSessionClients() {
        long now = System.currentTimeMillis();
        clientsBySession.forEach((sessionID, sessionClient) -> {
            if (now - sessionClient.lastUsedMillis >= SESSION_CLIENT_IDLE_TIMEOUT.toMillis()) {
                invalidateSessionClient(sessionID);
            }
        });
    }

    /**
     * Opens an initialized synchronous MCP client on the configured host.
     * The caller is responsible for closing it (try-with-resources or closeGracefully()).
     */
    public McpSyncClient openClient() {
        String host = aiConfig.mcpHost();
        if (host == null || host.isBlank()) {
            throw new IllegalStateException("cerberus_ai_mcp_host is not configured.");
        }

        // The transport resolves endpoint against the base URL : an endpoint starting
        // with "/" would drop the servlet context path. So we split the configured URL
        // into its origin (scheme://authority) and full path (e.g. /cerberus/mcp).
        URI uri = URI.create(host.trim());
        String baseUrl = uri.getScheme() + "://" + uri.getAuthority();
        String endpoint = (uri.getPath() == null || uri.getPath().isBlank()) ? MCP_ENDPOINT : uri.getPath();
        if (!endpoint.endsWith(MCP_ENDPOINT)) {
            endpoint = endpoint.replaceAll("/+$", "") + MCP_ENDPOINT;
        }

        String apiKey = aiConfig.mcpApiKey();

        HttpClientStreamableHttpTransport transport = HttpClientStreamableHttpTransport
                .builder(baseUrl)
                .endpoint(endpoint)
                .httpRequestCustomizer((requestBuilder, method, requestUri, body, context) ->
                        requestBuilder.header(API_KEY_HEADER, apiKey))
                .build();

        LOG.debug("Opening MCP client : base={}, endpoint={}", baseUrl, endpoint);

        McpSyncClient client = McpClient.sync(transport)
                .requestTimeout(Duration.ofSeconds(60))
                .build();
        client.initialize();
        return client;
    }

    /**
     * Lists the MCP server tools and converts them to Anthropic tool definitions.
     */
    public List<Tool> listAnthropicTools(McpSyncClient client) {
        return client.listTools().tools().stream()
                .map(this::toAnthropicTool)
                .collect(Collectors.toList());
    }

    private Tool toAnthropicTool(McpSchema.Tool mcpTool) {
        McpSchema.JsonSchema schema = mcpTool.inputSchema();

        Tool.InputSchema.Builder inputSchema = Tool.InputSchema.builder();
        if (schema != null) {
            if (schema.properties() != null) {
                inputSchema.properties(Tool.InputSchema.Properties.builder()
                        .additionalProperties(toJsonValueMap(schema.properties()))
                        .build());
            }
            if (schema.required() != null) {
                inputSchema.required(schema.required());
            }
        }

        return Tool.builder()
                .name(mcpTool.name())
                .description(mcpTool.description() == null ? "" : mcpTool.description())
                .inputSchema(inputSchema.build())
                .build();
    }

    private Map<String, JsonValue> toJsonValueMap(Map<String, Object> source) {
        return source.entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getKey, e -> JsonValue.from(e.getValue())));
    }

    /**
     * Builds the client-side {@code propose_quick_replies} tool definition. Claude calls it
     * to offer clickable quick-reply buttons after a tool result (e.g. confirm/cancel a
     * pending action, or a natural follow-up) — always phrased in the user's current language,
     * since only Claude (not the Java tool that produced the result) knows what that language is.
     */
    public Tool buildQuickReplyTool() {
        Map<String, Object> suggestionItemProperties = Map.of(
                "id", Map.of(
                        "type", "string",
                        "description", "Short stable identifier for this suggestion, e.g. \"confirm\" or \"cancel\"."
                ),
                "label", Map.of(
                        "type", "string",
                        "description", "Button text shown to the user. Must be written in the exact same language the user is currently chatting in."
                ),
                "value", Map.of(
                        "type", "string",
                        "description", "Message resent as the next user turn when this button is clicked, as if the user had typed it. Phrase it naturally, in the same language as label."
                ),
                "type", Map.of(
                        "type", "string",
                        "description", "Kind of suggestion.",
                        "enum", List.of("confirm", "cancel", "prompt", "action")
                )
        );

        Map<String, Object> properties = Map.of(
                "suggestions", Map.of(
                        "type", "array",
                        "description", "Ordered list of quick-reply buttons to show to the user right now.",
                        "items", Map.of(
                                "type", "object",
                                "properties", suggestionItemProperties,
                                "required", List.of("label", "value")
                        )
                )
        );

        Tool.InputSchema inputSchema = Tool.InputSchema.builder()
                .properties(Tool.InputSchema.Properties.builder()
                        .additionalProperties(toJsonValueMap(properties))
                        .build())
                .required(List.of("suggestions"))
                .build();

        return Tool.builder()
                .name(QUICK_REPLY_TOOL_NAME)
                .description("""
                        Call this right after a tool result, or at the end of your answer, whenever there is a
                        short list of obvious next steps for the user (confirming/cancelling a pending action, or
                        a natural follow-up). Skip it entirely when there is nothing meaningful to suggest.

                        Write "label" and "value" in the exact same language the user is currently chatting in —
                        never default to English or French. "value" is resent verbatim as the user's next message
                        when the button is clicked, so phrase it as something the user would naturally say.
                        """)
                .inputSchema(inputSchema)
                .build();
    }

    /**
     * Executes an MCP tool and returns its textual content (concatenated).
     */
    public String callTool(McpSyncClient client, String name, Map<String, Object> arguments) {
        McpSchema.CallToolResult result = client.callTool(new McpSchema.CallToolRequest(name, arguments));

        String text = result.content().stream()
                .filter(McpSchema.TextContent.class::isInstance)
                .map(content -> ((McpSchema.TextContent) content).text())
                .collect(Collectors.joining("\n"));

        if (Boolean.TRUE.equals(result.isError())) {
            LOG.warn("MCP tool '{}' returned an error : {}", name, text);
        }
        return text;
    }
}