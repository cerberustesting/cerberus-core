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
import org.springframework.stereotype.Service;

import java.net.URI;
import java.time.Duration;
import java.util.List;
import java.util.Map;
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

    @Autowired
    AIConfig aiConfig;

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