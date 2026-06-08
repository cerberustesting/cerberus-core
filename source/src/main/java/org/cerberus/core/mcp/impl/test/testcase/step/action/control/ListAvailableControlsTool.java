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
package org.cerberus.core.mcp.impl.test.testcase.step.action.control;

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
 * MCP tool that lists the Cerberus controls (assertions) available for a given application type.
 *
 * <p>Exposes the MCP tool named {@code list_available_controls}. The control catalogue is read
 * directly from the static JSON file {@code resources/static/controls.json} bundled in the
 * classpath — no Cerberus service is injected. This makes the tool self-contained but also means
 * the returned data is fixed at build time and is not driven by the invariant table.
 *
 * <p>Architectural note: unlike sibling tools (e.g. {@code ListAvailableActionsTool}), this class
 * does not delegate to any Spring service. The entire control catalogue is embedded in the static
 * JSON, so there is no startup-time database dependency.
 *
 * // Entries with applicationType=["ALL"] apply to every application type and are always included.
 */
@Component
public class ListAvailableControlsTool implements MCPTool {

    private static final List<String> ALLOWED_APPLICATION_TYPES = List.of("GUI", "SRV", "APK", "IPA", "FAT");

    private final ObjectMapper objectMapper;

    public ListAvailableControlsTool() {
        this.objectMapper = new ObjectMapper();
    }

    /**
     * Builds and returns the MCP {@link McpServerFeatures.SyncToolSpecification} for
     * {@code list_available_controls}.
     *
     * <p>The tool definition is sourced entirely from the static JSON file
     * {@code resources/static/controls.json} — there is no invariant-table lookup and therefore
     * no startup-time database risk. The JSON schema advertises {@code applicationType} as a
     * required enum so the AI model is constrained to known values before the call is even made.
     *
     * @return a synchronous tool specification ready to be registered on the MCP server
     */
    @Override
    public McpServerFeatures.SyncToolSpecification toToolSpecification() {
        return new McpServerFeatures.SyncToolSpecification(
                new McpSchema.Tool(
                        "list_available_controls",
                        null,
                        """
                        Returns the list of available Cerberus controls (assertions) filtered by application type.

                        Call this tool when the user wants to know which controls can be used for a given application type,
                        or when choosing the right control type to add to a testcase step action.

                        Controls with applicationType "ALL" apply to every application type and are always included.
                        """,
                        new McpSchema.JsonSchema(
                                "object",
                                Map.of(
                                        "applicationType", Map.of(
                                                "type", "string",
                                                "description", "Application type used to filter available controls (e.g. GUI, SRV, APK, IPA, FAT).",
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

                    // Reject values not in the known set even if the schema enum already constrains them.
                    if (!ALLOWED_APPLICATION_TYPES.contains(applicationType)) {
                        return buildErrorResult("Invalid applicationType. Expected one of: " + ALLOWED_APPLICATION_TYPES);
                    }

                    try {
                        ClassPathResource resource = new ClassPathResource("static/controls.json");

                        try (InputStream inputStream = resource.getInputStream()) {
                            List<Map<String, Object>> controls = objectMapper.readValue(
                                    inputStream,
                                    new TypeReference<List<Map<String, Object>>>() {}
                            );

                            List<Map<String, Object>> filtered = controls.stream()
                                    .filter(control -> {
                                        Object types = control.get("applicationType");
                                        if (!(types instanceof List<?>)) return false;
                                        List<?> typeList = (List<?>) types;
                                        // "ALL" means the control applies to every application type
                                        return typeList.contains(applicationType) || typeList.contains("ALL");
                                    })
                                    .collect(Collectors.toList());

                            String json = objectMapper.writeValueAsString(
                                    Map.of(
                                            "applicationType", applicationType,
                                            "controls", filtered
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
                        throw new RuntimeException("Unable to list available controls", e);
                    }
                }
        );
    }

    /**
     * Serialises an error message into a JSON {@link McpSchema.CallToolResult} with
     * {@code isError=true} so the MCP client can distinguish tool failures from successful
     * (but empty) responses.
     *
     * @param message human-readable description of the error
     * @return a {@link McpSchema.CallToolResult} carrying the error payload
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
