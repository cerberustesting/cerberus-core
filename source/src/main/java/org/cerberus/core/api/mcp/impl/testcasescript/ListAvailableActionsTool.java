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
package org.cerberus.core.api.mcp.impl.testcasescript;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.modelcontextprotocol.server.McpServerFeatures;
import io.modelcontextprotocol.spec.McpSchema;
import org.cerberus.core.api.mcp.MCPTool;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class ListAvailableActionsTool implements MCPTool {

    private static final List<String> ALLOWED_APPLICATION_TYPES = List.of("GUI", "SRV", "APK", "IPA");

    private final ObjectMapper objectMapper;

    public ListAvailableActionsTool() {
        this.objectMapper = new ObjectMapper();
    }

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