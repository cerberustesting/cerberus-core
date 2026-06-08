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
 * MCP tool that exposes the {@code list_available_conditions} operation.
 *
 * <p>Manages condition-operator metadata: the static catalogue of all Cerberus
 * condition operators (e.g. {@code conditionOperatorIfElementPresent},
 * {@code conditionOperatorIfPropertyContains}) that can be attached to a
 * test-case, step, action, or control to gate its execution.
 *
 * <p>The catalogue is read from the bundled classpath resource
 * {@code static/conditions.json}. No Cerberus service is involved — the data
 * is static and does not require a database round-trip. The tool filters the
 * full list down to entries whose {@code applicationType} array contains the
 * requested type or the sentinel value {@code "ALL"}.
 *
 * <p>Exposed MCP tool name: {@code list_available_conditions}.
 */
@Component
public class ListAvailableConditionsTool implements MCPTool {

    /** Application types recognised by Cerberus; used both for input validation and as enum hint in the schema. */
    private static final List<String> ALLOWED_APPLICATION_TYPES = List.of("GUI", "WEB", "SRV", "APK", "IPA", "FAT");

    private final ObjectMapper objectMapper;

    public ListAvailableConditionsTool() {
        this.objectMapper = new ObjectMapper();
    }

    /**
     * Builds the MCP {@link McpServerFeatures.SyncToolSpecification} for
     * {@code list_available_conditions}.
     *
     * <p>The specification declares a single required parameter
     * ({@code applicationType}) and wires it to an inline handler that:
     * <ol>
     *   <li>Validates the supplied application type against {@link #ALLOWED_APPLICATION_TYPES}.</li>
     *   <li>Reads {@code static/conditions.json} from the classpath.</li>
     *   <li>Filters the entries whose {@code applicationType} list contains the
     *       requested type or {@code "ALL"} (conditions that apply to every
     *       application type, regardless of the filter).</li>
     *   <li>Returns the filtered list as a JSON payload.</li>
     * </ol>
     *
     * @return a synchronous tool specification ready to be registered with the MCP server
     */
    @Override
    public McpServerFeatures.SyncToolSpecification toToolSpecification() {
        return new McpServerFeatures.SyncToolSpecification(
                new McpSchema.Tool(
                        "list_available_conditions",
                        null,
                        """
                        Returns the list of available Cerberus condition operators filtered by application type.

                        Conditions control whether a testcase, step, action, or control is executed.
                        Call this tool when the user wants to know which conditionOperator values are available,
                        or when setting up a conditional execution on any level of a testcase.

                        Conditions with applicationType "ALL" apply to every application type and are always included.
                        """,
                        new McpSchema.JsonSchema(
                                "object",
                                Map.of(
                                        "applicationType", Map.of(
                                                "type", "string",
                                                "description", "Application type used to filter available conditions (e.g. WEB, SRV, APK, IPA).",
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
                        ClassPathResource resource = new ClassPathResource("static/conditions.json");

                        try (InputStream inputStream = resource.getInputStream()) {
                            List<Map<String, Object>> conditions = objectMapper.readValue(
                                    inputStream,
                                    new TypeReference<List<Map<String, Object>>>() {}
                            );

                            List<Map<String, Object>> filtered = conditions.stream()
                                    .filter(condition -> {
                                        Object types = condition.get("applicationType");
                                        if (!(types instanceof List<?>)) return false;
                                        List<?> typeList = (List<?>) types;
                                        // Entries with applicationType=["ALL"] apply to every application type and are always included in the filtered result.
                                        return typeList.contains(applicationType) || typeList.contains("ALL");
                                    })
                                    .collect(Collectors.toList());

                            String json = objectMapper.writeValueAsString(
                                    Map.of(
                                            "applicationType", applicationType,
                                            "conditions", filtered
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
                        throw new RuntimeException("Unable to list available conditions", e);
                    }
                }
        );
    }

    /**
     * Builds a tool result that signals an error to the MCP caller.
     *
     * <p>The result carries {@code isError = true} so that MCP clients can
     * distinguish a domain-level validation failure from a successful (but
     * empty) response.
     *
     * @param message human-readable description of the error
     * @return a {@link McpSchema.CallToolResult} with the error payload and the error flag set
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
