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
package org.cerberus.core.mcp.impl.test.testcase;

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
 * MCP tool that exposes available Cerberus test-case property types filtered by application type.
 *
 * <p>Exposes the MCP tool name {@code list_available_property_types}. Property types describe how
 * a test-case property value is resolved at runtime (e.g. static text, SQL query, JSON path, OTP).
 *
 * <p>Unlike action/condition tools that may query the invariant table, this tool reads its data
 * exclusively from the static classpath resource {@code static/properties.json} — no Cerberus
 * service is injected. The JSON file is the single source of truth for property-type metadata and
 * is therefore safe to call before the database is reachable.
 *
 * <p>Architectural note: because the data source is a bundled JSON file rather than a live service,
 * there is no startup-time risk of a missing HTTP/request context (contrast with tools that call
 * {@code IInvariantService} during {@code createTool()}).
 * // Entries with applicationType=["ALL"] apply to every application type and are always included.
 */
@Component
public class ListAvailablePropertyTypesTool implements MCPTool {

    private static final List<String> ALLOWED_APPLICATION_TYPES = List.of("GUI", "WEB", "SRV", "APK", "IPA", "FAT");

    private final ObjectMapper objectMapper;

    public ListAvailablePropertyTypesTool() {
        this.objectMapper = new ObjectMapper();
    }

    /**
     * Builds the MCP {@link McpServerFeatures.SyncToolSpecification} for {@code list_available_property_types}.
     *
     * <p>The tool schema advertises {@code applicationType} as a required enum parameter. At call
     * time the handler reads {@code static/properties.json} from the classpath, filters entries
     * whose {@code applicationType} array contains the requested type or {@code "ALL"}, and returns
     * the matching subset as JSON.
     *
     * <p>Because the data comes from a static file rather than the invariant table, there is no
     * startup-time risk associated with a missing HTTP request context.
     *
     * @return a sync tool specification ready to be registered on the MCP server
     */
    @Override
    public McpServerFeatures.SyncToolSpecification toToolSpecification() {
        return new McpServerFeatures.SyncToolSpecification(
                new McpSchema.Tool(
                        "list_available_property_types",
                        null,
                        """
                        Returns the list of available Cerberus property types filtered by application type.

                        Property types define how a testcase property value is computed at runtime
                        (e.g. static text, SQL query, JSON path, OTP, etc.).
                        Call this tool when the user wants to know which type to use when creating or updating a property,
                        or to understand what parameters each type expects.

                        Property types with applicationType "ALL" apply to every application type and are always included.
                        """,
                        new McpSchema.JsonSchema(
                                "object",
                                Map.of(
                                        "applicationType", Map.of(
                                                "type", "string",
                                                "description", "Application type used to filter available property types (e.g. WEB, SRV, APK, IPA).",
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
                        return buildErrorResult("Invalid applicationType. Expected one of: " + ALLOWED_APPLICATION_TYPES);
                    }

                    try {
                        ClassPathResource resource = new ClassPathResource("static/properties.json");

                        try (InputStream inputStream = resource.getInputStream()) {
                            List<Map<String, Object>> propertyTypes = objectMapper.readValue(
                                    inputStream,
                                    new TypeReference<List<Map<String, Object>>>() {}
                            );

                            List<Map<String, Object>> filtered = propertyTypes.stream()
                                    .filter(pt -> {
                                        Object types = pt.get("applicationType");
                                        if (!(types instanceof List<?>)) return false;
                                        List<?> typeList = (List<?>) types;
                                        // Entries with applicationType=["ALL"] apply to every application type and are always included.
                                        return typeList.contains(applicationType) || typeList.contains("ALL");
                                    })
                                    .collect(Collectors.toList());

                            String json = objectMapper.writeValueAsString(
                                    Map.of(
                                            "applicationType", applicationType,
                                            "propertyTypes", filtered
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
                        throw new RuntimeException("Unable to list available property types", e);
                    }
                }
        );
    }

    /**
     * Serialises an error message into a {@link McpSchema.CallToolResult} with {@code isError=true}.
     *
     * <p>Wraps the message in a JSON object {@code {"error": "..."}} so callers receive a
     * structured response instead of a bare string, consistent with other MCP tool error handling.
     *
     * @param message human-readable description of what went wrong
     * @return a {@link McpSchema.CallToolResult} flagged as an error
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
