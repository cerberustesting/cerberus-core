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
package org.cerberus.core.mcp.impl.invariant;

import io.modelcontextprotocol.server.McpServerFeatures;
import io.modelcontextprotocol.spec.McpSchema;
import org.cerberus.core.exception.CerberusException;
import org.cerberus.core.mcp.MCPTool;
import org.cerberus.core.mcp.util.MCPLogUtils;
import org.cerberus.core.mcp.util.MCPToolUtils;
import org.cerberus.core.crud.entity.Invariant;
import org.cerberus.core.crud.service.IInvariantService;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * MCP tool that exposes Cerberus {@link Invariant} lookup under the tool name {@code cerberus_invariant_list}.
 *
 * <p>An invariant is a controlled-vocabulary entry (e.g. COUNTRY, ENVIRONMENT, BROWSER) stored in the
 * {@code invariant} table and managed by {@link IInvariantService}. This tool lets an AI agent retrieve
 * the valid values for a given invariant category, optionally filtered by a free-text search term.</p>
 *
 * <p>Delegation: {@link IInvariantService#readByIdName(String)} is called to fetch all entries for the
 * requested type; in-memory filtering is then applied before the result is returned as JSON.</p>
 */
@Component
public class ListInvariantsTool implements MCPTool {

    private static final String TOOL_NAME = "cerberus_invariant_list";

    /**
     * Whitelist of invariant category identifiers exposed through this tool.
     * Only these types are accepted to avoid leaking internal or sensitive invariant categories.
     */
    private static final List<String> SUPPORTED_TYPES = List.of(
            "COUNTRY",
            "ENVIRONMENT",
            "SYSTEM",
            "BROWSER",
            "PRIORITY",
            "ROBOT",
            "CAPABILITY",
            "TCSTATUS"
    );

    private final IInvariantService invariantService;
    private final MCPLogUtils mcpLogUtils;

    public ListInvariantsTool(IInvariantService invariantService, MCPLogUtils mcpLogUtils) {
        this.invariantService = invariantService;
        this.mcpLogUtils = mcpLogUtils;
    }

    @Override
    public McpServerFeatures.SyncToolSpecification toToolSpecification() {
        return new McpServerFeatures.SyncToolSpecification(
                createTool(),
                (exchange, request) -> {
                    Map<String, Object> args = MCPToolUtils.argumentsOrEmpty(request.arguments());
                    return execute(args);
                }
        );
    }

    /**
     * Builds the MCP tool descriptor, including the JSON schema that constrains accepted inputs.
     *
     * <p>The {@code type} parameter is declared as an {@code enum} so MCP clients can present a
     * structured picker rather than a free-text field, reducing the chance of invalid values reaching
     * {@link #execute(Map)}.</p>
     *
     * @return the fully-described {@link McpSchema.Tool} for registration with the MCP server.
     */
    private McpSchema.Tool createTool() {
        Map<String, Object> properties = Map.of(
                "type", Map.of(
                        "type", "string",
                        "description", """
                                Type of invariant to retrieve. Supported values:
                                - COUNTRY: list of countries available for test execution.
                                - ENVIRONMENT: list of environments (e.g. DEV, QA, PROD).
                                - SYSTEM: list of systems grouping applications. Also called workspaces in the application
                                - BROWSER: list of supported browsers.
                                - PRIORITY: list of test case priority levels.
                                - ROBOT: list of robot (execution agent) types.
                                - CAPABILITY: list of robot capabilities.
                                - TCSTATUS: list of test case execution statuses.
                                """,
                        "enum", SUPPORTED_TYPES
                ),
                "search", Map.of(
                        "type", "string",
                        "description", "Optional text filter on value or description."
                )
        );

        return new McpSchema.Tool(
                TOOL_NAME,
                null,
                """
                Returns the list of Cerberus invariant values for a given type.

                Call this tool whenever the user needs to list countries, environments, systems, workspaces,
                browsers, priorities, robots, capabilities, or statuses available in Cerberus.

                Use type to specify which category of invariants to retrieve.
                Use search to filter results by value or description.
                """,
                new McpSchema.JsonSchema(
                        "object",
                        properties,
                        List.of("type"),
                        null,
                        null,
                        null
                ),
                null,
                MCPToolUtils.createAnnotations("List invariants", true),
                null
        );
    }

    /**
     * Validates the caller-supplied arguments, queries the invariant service, applies the optional
     * search filter, and returns a JSON result to the MCP client.
     *
     * @param args tool arguments extracted from the MCP request (may be empty but never null).
     * @return a {@link McpSchema.CallToolResult} containing either the matching invariants or an
     *         error description if validation or the service call failed.
     */
    private McpSchema.CallToolResult execute(Map<String, Object> args) {
        String type = MCPToolUtils.getString(args, "type", "");
        String search = MCPToolUtils.getString(args, "search", "");

        mcpLogUtils.call(TOOL_NAME, "invariant_list", String.format("MCP tool %s called with type=%s", TOOL_NAME, type));

        if (type.isBlank()) {
            return MCPToolUtils.errorText("Missing required parameter: type");
        }

        // Guard against types not in the whitelist before hitting the database.
        if (!SUPPORTED_TYPES.contains(type)) {
            return MCPToolUtils.errorText("Unsupported invariant type: " + type + ". Supported types: " + SUPPORTED_TYPES);
        }

        List<Invariant> invariants;
        try {
            invariants = invariantService.readByIdName(type);
        } catch (CerberusException e) {
            return MCPToolUtils.errorText("Unable to retrieve invariants for type " + type + ": " + e.getMessage());
        }

        List<Map<String, Object>> result = invariants.stream()
                .filter(inv -> matchesSearch(inv, search))
                .map(this::toMap)
                .toList();

        return MCPToolUtils.successJson(Map.of(
                "type", type,
                "count", result.size(),
                "invariants", result
        ));
    }

    /**
     * Converts an {@link Invariant} entity into a plain map suitable for JSON serialisation.
     *
     * <p>{@link LinkedHashMap} is used to preserve insertion order so that {@code value} always
     * appears before {@code description} in the serialised output, making responses easier to read.</p>
     * <p>{@code veryShortDesc} is omitted when blank because most invariant types do not populate it
     * and including an empty field adds noise for the AI consumer.</p>
     *
     * @param inv the invariant to convert.
     * @return an ordered map with the invariant's public fields.
     */
    private Map<String, Object> toMap(Invariant inv) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("value", inv.getValue());
        map.put("description", inv.getDescription());
        if (inv.getVeryShortDesc() != null && !inv.getVeryShortDesc().isBlank()) {
            map.put("veryShortDesc", inv.getVeryShortDesc());
        }
        return map;
    }

    /**
     * Returns {@code true} when the invariant's value or description contains the search term
     * (case-insensitive), or when no search term was supplied.
     *
     * @param inv    the invariant to test.
     * @param search the caller-supplied filter string; {@code null} or blank means "match all".
     * @return {@code true} if the invariant should be included in the result.
     */
    private boolean matchesSearch(Invariant inv, String search) {
        if (search == null || search.isBlank()) {
            return true;
        }
        return MCPToolUtils.containsIgnoreCase(inv.getValue(), search)
                || MCPToolUtils.containsIgnoreCase(inv.getDescription(), search);
    }

}