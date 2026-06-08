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
import org.cerberus.core.util.answer.Answer;
import org.cerberus.core.util.answer.AnswerItem;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

/**
 * MCP tool that deletes an existing {@link Invariant} entry from Cerberus.
 *
 * <p>Exposes the MCP tool named {@code cerberus_invariant_delete}, which accepts a
 * {@code type} (e.g. COUNTRY, ENVIRONMENT) and a {@code value} (the invariant code)
 * and removes the matching invariant record via {@link IInvariantService}.
 *
 * <p>Only a curated subset of invariant types is allowed for deletion to prevent
 * accidental removal of system-critical invariants that are not user-managed.
 */
@Component
public class DeleteInvariantTool implements MCPTool {

    private static final String TOOL_NAME = "cerberus_invariant_delete";

    /**
     * Invariant types that are safe to expose for deletion via MCP.
     * Types not in this list are considered system-internal and are intentionally excluded.
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

    public DeleteInvariantTool(IInvariantService invariantService, MCPLogUtils mcpLogUtils) {
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
     * Builds the MCP {@link McpSchema.Tool} descriptor for {@code cerberus_invariant_delete}.
     *
     * <p>Declares {@code type} and {@code value} as required string parameters.
     * The {@code type} field is constrained to {@link #SUPPORTED_TYPES} via an enum schema
     * so the AI model only proposes valid type values.
     *
     * @return the fully configured tool specification
     */
    private McpSchema.Tool createTool() {
        Map<String, Object> properties = Map.of(
                "type", Map.of(
                        "type", "string",
                        "description", """
                                Type of invariant to delete. Supported values:
                                - COUNTRY: remove a country from test execution.
                                - ENVIRONMENT: remove an environment (e.g. DEV, QA, PROD).
                                - SYSTEM: remove a system (also called workspace) grouping applications.
                                - BROWSER: remove a supported browser.
                                - PRIORITY: remove a test case priority level.
                                - ROBOT: remove a robot (execution agent) type.
                                - CAPABILITY: remove a robot capability.
                                - TCSTATUS: remove a test case execution status.
                                """,
                        "enum", SUPPORTED_TYPES
                ),
                "value", Map.of(
                        "type", "string",
                        "description", "Value (code) of the invariant entry to delete (e.g. 'FR' for a country)."
                )
        );

        return new McpSchema.Tool(
                TOOL_NAME,
                null,
                """
                Deletes an existing invariant value in Cerberus.

                Call this tool whenever the user asks to delete or remove a country, environment,
                system, browser, priority, robot, capability, or test case status.

                Do not call this tool when the user only asks to list, read, or create invariant values.
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
                MCPToolUtils.createAnnotations("Delete invariant", false),
                null
        );
    }

    /**
     * Executes the invariant deletion for the given MCP arguments.
     *
     * <p>Validates that {@code type} and {@code value} are present and that {@code type}
     * belongs to {@link #SUPPORTED_TYPES}. Then performs an existence check via
     * {@link IInvariantService#readByKey(String, String)} before calling
     * {@link IInvariantService#delete(Invariant)} — the read-before-delete pattern
     * produces a user-friendly error message when the invariant is not found, rather than
     * relying on a generic service-layer exception.
     *
     * @param args the raw MCP tool arguments
     * @return a success JSON result with the deleted keys, or an error text result
     */
    private McpSchema.CallToolResult execute(Map<String, Object> args) {
        String type = MCPToolUtils.getString(args, "type", "");
        String value = MCPToolUtils.getString(args, "value", "");

        mcpLogUtils.call(TOOL_NAME, "invariant_delete", String.format("MCP tool %s called with type=%s value=%s", TOOL_NAME, type, value));

        if (type.isBlank()) {
            return MCPToolUtils.errorText("Missing required parameter: type");
        }

        if (value.isBlank()) {
            return MCPToolUtils.errorText("Missing required parameter: value");
        }

        if (!SUPPORTED_TYPES.contains(type)) {
            return MCPToolUtils.errorText("Unsupported invariant type: " + type + ". Supported types: " + SUPPORTED_TYPES);
        }

        // Load before delete so we can surface a clear "not found" message and pass the
        // entity instance to invariantService.delete(), which requires the full object.
        AnswerItem<Invariant> existing = invariantService.readByKey(type, value);
        if (!existing.isCodeStringEquals("OK") || existing.getItem() == null) {
            return MCPToolUtils.errorText("Invariant does not exist: type=" + type + " value=" + value);
        }

        Answer answer = invariantService.delete(existing.getItem());

        if (!answer.isCodeStringEquals("OK")) {
            return MCPToolUtils.errorText("Unable to delete invariant type=" + type + " value=" + value + ": " + answer.getMessageDescription());
        }

        return MCPToolUtils.successJson(Map.of(
                "status", "deleted",
                "type", type,
                "value", value
        ));
    }

}