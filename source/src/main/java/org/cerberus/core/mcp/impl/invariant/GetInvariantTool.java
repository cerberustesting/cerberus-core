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
import org.cerberus.core.mcp.MCPTool;
import org.cerberus.core.mcp.util.MCPLogUtils;
import org.cerberus.core.mcp.util.MCPToolUtils;
import org.cerberus.core.crud.entity.Invariant;
import org.cerberus.core.crud.service.IInvariantService;
import org.cerberus.core.util.answer.AnswerItem;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * MCP tool that retrieves a single {@link Invariant} entry by type and value.
 *
 * <p>Exposes the MCP tool named {@code cerberus_invariant_get}, which allows an AI agent to
 * look up the details of a specific invariant (e.g. a country code, environment name, browser
 * identifier) when both the type and exact value are already known.</p>
 *
 * <p>Delegates to {@link IInvariantService#readByKey(String, String)} for the actual lookup.
 * Use {@code cerberus_invariant_list} instead when the caller needs to enumerate all entries
 * of a given type.</p>
 */
@Component
public class GetInvariantTool implements MCPTool {

    private static final String TOOL_NAME = "cerberus_invariant_get";

    /**
     * Subset of invariant types exposed through this tool. Only types that are
     * meaningful for AI-driven test orchestration are included; internal or
     * rarely-used types are intentionally omitted.
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

    public GetInvariantTool(IInvariantService invariantService, MCPLogUtils mcpLogUtils) {
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
     * Builds the MCP tool schema that describes the tool name, description, and required
     * input parameters ({@code type} and {@code value}) to the MCP runtime.
     *
     * <p>The {@code type} parameter is constrained to {@link #SUPPORTED_TYPES} via an enum
     * restriction so that AI agents receive immediate validation feedback without a round-trip
     * to the Cerberus backend.</p>
     *
     * @return the fully configured {@link McpSchema.Tool} descriptor
     */
    private McpSchema.Tool createTool() {
        Map<String, Object> properties = Map.of(
                "type", Map.of(
                        "type", "string",
                        "description", """
                                Type of invariant to retrieve. Supported values:
                                - COUNTRY: get a country available for test execution.
                                - ENVIRONMENT: get an environment (e.g. DEV, QA, PROD).
                                - SYSTEM: get a system (also called workspace) grouping applications.
                                - BROWSER: get a supported browser.
                                - PRIORITY: get a test case priority level.
                                - ROBOT: get a robot (execution agent) type.
                                - CAPABILITY: get a robot capability.
                                - TCSTATUS: get a test case execution status.
                                """,
                        "enum", SUPPORTED_TYPES
                ),
                "value", Map.of(
                        "type", "string",
                        "description", "Value (code) of the invariant entry to retrieve (e.g. 'FR' for a country)."
                )
        );

        return new McpSchema.Tool(
                TOOL_NAME,
                null,
                """
                Retrieves a single invariant value from Cerberus by type and value.

                Call this tool whenever the user asks to get or inspect the details of a specific
                country, environment, system, browser, priority, robot, capability, or test case status.

                Use cerberus_invariant_list instead when the target value is unknown or when listing all entries of a type.
                Do not call this tool when the user asks to create, update, or delete an invariant value.
                """,
                new McpSchema.JsonSchema(
                        "object",
                        properties,
                        List.of("type", "value"),
                        null,
                        null,
                        null
                ),
                null,
                MCPToolUtils.createAnnotations("Get invariant", true),
                null
        );
    }

    /**
     * Validates the tool arguments, delegates to {@link IInvariantService#readByKey(String, String)},
     * and returns the invariant as a JSON object or a structured error.
     *
     * @param args raw MCP arguments map extracted from the tool call request
     * @return a {@link McpSchema.CallToolResult} containing either the invariant JSON or an error message
     */
    private McpSchema.CallToolResult execute(Map<String, Object> args) {
        String type = MCPToolUtils.getString(args, "type", "");
        String value = MCPToolUtils.getString(args, "value", "");

        mcpLogUtils.call(TOOL_NAME, "invariant_get", String.format("MCP tool %s called with type=%s value=%s", TOOL_NAME, type, value));

        if (type.isBlank()) {
            return MCPToolUtils.errorText("Missing required parameter: type");
        }

        if (value.isBlank()) {
            return MCPToolUtils.errorText("Missing required parameter: value");
        }

        // Guard against types that exist in the DB but are not intended for AI consumption.
        if (!SUPPORTED_TYPES.contains(type)) {
            return MCPToolUtils.errorText("Unsupported invariant type: " + type + ". Supported types: " + SUPPORTED_TYPES);
        }

        AnswerItem<Invariant> answer = invariantService.readByKey(type, value);

        // Treat both a service error and a null item as "not found" — the invariant may not exist.
        if (!answer.isCodeStringEquals("OK") || answer.getItem() == null) {
            return MCPToolUtils.errorText("Invariant does not exist: type=" + type + " value=" + value);
        }

        return MCPToolUtils.successJson(toMap(answer.getItem()));
    }

    /**
     * Converts an {@link Invariant} entity to a flat map suitable for JSON serialisation.
     *
     * <p>{@code veryShortDesc} is omitted when blank because many invariant types do not
     * populate that field, and including an empty string would add noise for the AI consumer.</p>
     *
     * @param inv the invariant entity to convert
     * @return an ordered map of invariant fields
     */
    private Map<String, Object> toMap(Invariant inv) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("type", inv.getIdName());
        map.put("value", inv.getValue());
        map.put("description", inv.getDescription());
        map.put("sort", inv.getSort());
        // Only include veryShortDesc when it carries meaningful content.
        if (inv.getVeryShortDesc() != null && !inv.getVeryShortDesc().isBlank()) {
            map.put("veryShortDesc", inv.getVeryShortDesc());
        }
        return map;
    }

}