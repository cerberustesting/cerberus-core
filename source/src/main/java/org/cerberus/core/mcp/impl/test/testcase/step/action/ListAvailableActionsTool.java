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
package org.cerberus.core.mcp.impl.test.testcase.step.action;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.modelcontextprotocol.server.McpServerFeatures;
import io.modelcontextprotocol.spec.McpSchema;
import org.cerberus.core.mcp.MCPTool;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * MCP tool that exposes the list of Cerberus step actions available for a given application type.
 *
 * <p>Exposes the MCP tool name {@code list_available_actions}. It does not delegate to a Cerberus
 * service; instead it reads action definitions from the static classpath resource
 * {@code static/actions.json} and filters entries by the requested {@code applicationType}.
 *
 * <p>Because the data source is a static JSON file bundled in the WAR, no database or HTTP context
 * is required at call time. Entries whose {@code applicationType} array contains {@code "ALL"} apply
 * to every application type and are therefore always included when the array also contains the
 * requested type — see the filter lambda in {@link #toToolSpecification()}.
 */
@Component
public class ListAvailableActionsTool implements MCPTool {

    // Exhaustive list of application types the MCP client may request; used for input validation.
    private static final List<String> ALLOWED_APPLICATION_TYPES = List.of("GUI", "SRV", "APK", "IPA");

    private final ObjectMapper objectMapper;

    public ListAvailableActionsTool() {
        this.objectMapper = new ObjectMapper();
    }

    /**
     * Builds the MCP tool specification for {@code list_available_actions}.
     *
     * <p>The tool schema declares {@code applicationType} as a required enum parameter so that MCP
     * clients can present a constrained selection to the end user. Action data is sourced entirely
     * from the static JSON file {@code static/actions.json}; no invariant table lookup is performed.
     *
     * <p>The inline handler lambda validates the input, reads and filters {@code actions.json}, and
     * returns a JSON payload containing the matched actions.
     * // Entries with applicationType=["ALL"] apply to every application type and are always included.
     *
     * @return a {@link McpServerFeatures.SyncToolSpecification} ready to be registered with the MCP server
     */
    @Override
    public McpServerFeatures.SyncToolSpecification toToolSpecification() {
        return new McpServerFeatures.SyncToolSpecification(
                new McpSchema.Tool(
                        "list_available_actions",
                        null,
                        "Returns list of available Cerberus actions filtered by application type",
                        new McpSchema.JsonSchema(
                                "object",
                                Map.of(
                                        "applicationType", Map.of(
                                                "type", "string",
                                                "description", "Application type used to filter available actions",
                                                "enum", ALLOWED_APPLICATION_TYPES
                                        )
                                ),
                                List.of("applicationType"),
                                null,
                                null,
                                null
                        ),
                        null,
                        null,
                        null
                ),
                (exchange, request) -> {
                    Map<String, Object> arguments = request.arguments();

                    String applicationType = arguments != null
                            ? (String) arguments.get("applicationType")
                            : null;

                    if (applicationType == null || applicationType.isBlank()) {
                        return buildErrorResult("Missing required parameter: applicationType");
                    }

                    if (!ALLOWED_APPLICATION_TYPES.contains(applicationType)) {
                        return buildErrorResult("Invalid applicationType. Expected one of: GUI, SRV, APK, IPA");
                    }

                    try {
                        ClassPathResource resource = new ClassPathResource("static/actions.json");

                        try (InputStream inputStream = resource.getInputStream()) {
                            List<Map<String, Object>> actions = objectMapper.readValue(
                                    inputStream,
                                    new TypeReference<List<Map<String, Object>>>() {
                                    }
                            );

                            // Keep only actions whose applicationType list contains the requested type.
                            // Entries with applicationType=["ALL"] apply to every application type and are always included.
                            List<Map<String, Object>> filteredActions = actions.stream()
                                    .filter(action -> {
                                        Object applicationTypes = action.get("applicationType");

                                        if (!(applicationTypes instanceof List<?>)) {
                                            return false;
                                        }

                                        return ((List<?>) applicationTypes).contains(applicationType);
                                    })
                                    .collect(Collectors.toList());

                            String json = objectMapper.writeValueAsString(
                                    Map.of(
                                            "applicationType", applicationType,
                                            "actions", filteredActions
                                    )
                            );

                            return new McpSchema.CallToolResult(
                                    List.of(new McpSchema.TextContent(null, json, null)),
                                    false,
                                    null,
                                    null
                            );
                        }
                    } catch (Exception e) {
                        throw new RuntimeException("Unable to list available actions", e);
                    }
                }
        );
    }

    /**
     * Serialises an error message into a {@link McpSchema.CallToolResult} with {@code isError=true}.
     *
     * <p>Returns a well-formed MCP result so the client receives structured JSON rather than an
     * unhandled exception. The {@code isError} flag signals to the caller that the invocation failed.
     *
     * @param message human-readable error description to embed in the JSON payload
     * @return a {@link McpSchema.CallToolResult} carrying the error JSON and the error flag set to {@code true}
     */
    private McpSchema.CallToolResult buildErrorResult(String message) {
        try {
            String json = objectMapper.writeValueAsString(Map.of("error", message));

            return new McpSchema.CallToolResult(
                    List.of(new McpSchema.TextContent(null, json, null)),
                    true,
                    null,
                    null
            );
        } catch (Exception e) {
            throw new RuntimeException("Unable to build error response", e);
        }
    }
}
