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

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * MCP tool that manages the update of existing {@link Invariant} entries in Cerberus.
 *
 * <p>Exposed MCP tool name: {@code cerberus_invariant_update}.</p>
 *
 * <p>Delegates all persistence operations to {@link IInvariantService}. Only a curated
 * subset of invariant types (COUNTRY, ENVIRONMENT, SYSTEM, etc.) and a restricted set
 * of fields (description, sort) are exposed to prevent unintentional changes to
 * Cerberus internal reference data.</p>
 */
@Component
public class UpdateInvariantTool implements MCPTool {

    private static final String TOOL_NAME = "cerberus_invariant_update";

    /**
     * Invariant types that are safe to expose for AI-driven updates.
     * Internal or system-managed types are intentionally excluded.
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

    public UpdateInvariantTool(IInvariantService invariantService, MCPLogUtils mcpLogUtils) {
        this.invariantService = invariantService;
        this.mcpLogUtils = mcpLogUtils;
    }

    /**
     * Builds and returns the synchronous MCP tool specification, binding the
     * JSON schema definition to the {@link #execute(Map)} handler.
     */
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
     * Constructs the {@link McpSchema.Tool} descriptor with the JSON schema that
     * constrains the AI to only the allowed invariant types and updatable fields.
     *
     * <p>The {@code updates} sub-object uses {@code additionalProperties: false} so the
     * MCP layer rejects any field the AI tries to set that is not explicitly declared.</p>
     */
    private McpSchema.Tool createTool() {
        Map<String, Object> updateProperties = new LinkedHashMap<>();
        updateProperties.put("description", Map.of(
                "type", "string",
                "description", "New human-readable description of the invariant value."
        ));
        updateProperties.put("sort", Map.of(
                "type", "integer",
                "description", "New sort order of the invariant value."
        ));

        Map<String, Object> updatesSchema = new LinkedHashMap<>();
        updatesSchema.put("type", "object");
        updatesSchema.put("description", "Fields to update. Only supported fields are allowed.");
        updatesSchema.put("properties", updateProperties);
        // Prevent the AI from injecting arbitrary fields into the Invariant entity.
        updatesSchema.put("additionalProperties", false);

        Map<String, Object> properties = new LinkedHashMap<>();
        properties.put("type", Map.of(
                "type", "string",
                "description", """
                        Type of invariant to update. Supported values:
                        - COUNTRY: update a country available for test execution.
                        - ENVIRONMENT: update an environment (e.g. DEV, QA, PROD).
                        - SYSTEM: update a system (also called workspace) grouping applications.
                        - BROWSER: update a supported browser.
                        - PRIORITY: update a test case priority level.
                        - ROBOT: update a robot (execution agent) type.
                        - CAPABILITY: update a robot capability.
                        - TCSTATUS: update a test case execution status.
                        """,
                "enum", SUPPORTED_TYPES
        ));
        properties.put("value", Map.of(
                "type", "string",
                "description", "Value (code) of the invariant entry to update (e.g. 'FR' for a country)."
        ));
        properties.put("updates", updatesSchema);

        return new McpSchema.Tool(
                TOOL_NAME,
                null,
                """
                Updates an existing invariant value in Cerberus.

                Call this tool whenever the user asks to modify or update a country, environment,
                system, browser, priority, robot, capability, or test case status.

                Do not call this tool when the user only asks to list, read, create, or delete invariant values.
                """,
                new McpSchema.JsonSchema(
                        "object",
                        properties,
                        List.of("type", "value", "updates"),
                        null,
                        null,
                        null
                ),
                null,
                MCPToolUtils.createAnnotations("Update invariant", false),
                null
        );
    }

    /**
     * Validates arguments, reads the existing {@link Invariant} from the database,
     * applies only the requested field changes, then persists the update via
     * {@link IInvariantService#update(String, String, Invariant)}.
     *
     * <p>The invariant is read before update so that unchanged fields retain their
     * current values — the service replaces the full row, not individual columns.</p>
     *
     * @param args raw MCP arguments map provided by the AI client
     * @return a success result with the list of modified fields, or an error result
     */
    @SuppressWarnings("unchecked")
    private McpSchema.CallToolResult execute(Map<String, Object> args) {
        String type = MCPToolUtils.getString(args, "type", "");
        String value = MCPToolUtils.getString(args, "value", "");

        mcpLogUtils.call(TOOL_NAME, "invariant_update", String.format("MCP tool %s called with type=%s value=%s", TOOL_NAME, type, value));

        if (type.isBlank()) {
            return MCPToolUtils.errorText("Missing required parameter: type");
        }

        if (value.isBlank()) {
            return MCPToolUtils.errorText("Missing required parameter: value");
        }

        // Secondary guard — the JSON schema enum should already block unsupported types.
        if (!SUPPORTED_TYPES.contains(type)) {
            return MCPToolUtils.errorText("Unsupported invariant type: " + type + ". Supported types: " + SUPPORTED_TYPES);
        }

        Object updatesObject = args.get("updates");
        if (!(updatesObject instanceof Map)) {
            return MCPToolUtils.errorText("Missing or invalid required parameter: updates");
        }

        Map<String, Object> updates = (Map<String, Object>) updatesObject;
        if (updates.isEmpty()) {
            return MCPToolUtils.errorText("No field provided to update.");
        }

        // Load the current state so we can mutate only the requested fields before persisting.
        AnswerItem<Invariant> existing = invariantService.readByKey(type, value);
        if (!existing.isCodeStringEquals("OK") || existing.getItem() == null) {
            return MCPToolUtils.errorText("Invariant does not exist: type=" + type + " value=" + value);
        }

        Invariant invariant = existing.getItem();
        Map<String, Object> modifiedFields = new LinkedHashMap<>();

        try {
            for (Map.Entry<String, Object> entry : updates.entrySet()) {
                switch (entry.getKey()) {
                    case "description":
                        String description = asString(entry.getValue(), "description");
                        invariant.setDescription(description);
                        modifiedFields.put("description", description);
                        break;

                    case "sort":
                        Integer sort = asInteger(entry.getValue(), "sort");
                        invariant.setSort(sort);
                        modifiedFields.put("sort", sort);
                        break;

                    default:
                        // Fail fast on unknown fields even if additionalProperties:false filtered most.
                        return MCPToolUtils.errorText("Unsupported field for invariant update: " + entry.getKey());
                }
            }
        } catch (IllegalArgumentException e) {
            return MCPToolUtils.errorText(e.getMessage());
        }

        Answer answer = invariantService.update(type, value, invariant);

        if (!answer.isCodeStringEquals("OK")) {
            return MCPToolUtils.errorText("Unable to update invariant type=" + type + " value=" + value + ": " + answer.getMessageDescription());
        }

        return MCPToolUtils.successJson(Map.of(
                "status", "updated",
                "type", type,
                "value", value,
                "updatedFields", modifiedFields
        ));
    }

    /**
     * Coerces a raw MCP argument value to a trimmed {@link String}.
     * Returns an empty string for null so callers can apply {@code isBlank()} uniformly.
     *
     * @param value the raw value from the arguments map
     * @param field field name used in the error message if the type is wrong
     * @return trimmed string value, never {@code null}
     * @throws IllegalArgumentException if the value is non-null but not a String
     */
    private String asString(Object value, String field) {
        if (value == null) return "";
        if (!(value instanceof String)) {
            throw new IllegalArgumentException("Invalid value for field '" + field + "'. Expected string.");
        }
        return ((String) value).trim();
    }

    /**
     * Coerces a raw MCP argument value to an {@link Integer}.
     * Handles both {@code Integer} and other {@link Number} subtypes because JSON
     * deserializers may produce {@code Long} or {@code Double} for numeric literals.
     *
     * @param value the raw value from the arguments map
     * @param field field name used in the error message if the type is wrong
     * @return integer value
     * @throws IllegalArgumentException if the value cannot be interpreted as an integer
     */
    private Integer asInteger(Object value, String field) {
        if (value instanceof Integer) return (Integer) value;
        if (value instanceof Number) return ((Number) value).intValue();
        throw new IllegalArgumentException("Invalid value for field '" + field + "'. Expected integer.");
    }

}