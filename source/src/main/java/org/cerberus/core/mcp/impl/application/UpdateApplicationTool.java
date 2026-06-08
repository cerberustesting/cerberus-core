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
package org.cerberus.core.mcp.impl.application;

import io.modelcontextprotocol.server.McpServerFeatures;
import io.modelcontextprotocol.spec.McpSchema;
import org.cerberus.core.api.dto.application.ApplicationMapperV001;
import org.cerberus.core.mcp.MCPTool;
import org.cerberus.core.mcp.util.MCPLogUtils;
import org.cerberus.core.mcp.util.MCPToolUtils;
import org.cerberus.core.crud.entity.Application;
import org.cerberus.core.crud.service.IApplicationService;
import org.cerberus.core.util.answer.Answer;
import org.cerberus.core.util.answer.AnswerItem;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * MCP tool that manages updates to existing {@link Application} entities in Cerberus.
 *
 * <p>Exposes the MCP tool name {@code cerberus_application_update}, which allows an AI agent
 * to mutate a subset of fields (description, type, system) on an application identified
 * by its unique name. Delegates persistence to {@link IApplicationService}.</p>
 *
 * <p>Only explicitly declared fields are accepted; any unrecognised field causes an immediate
 * error response, preventing unintended mutations.</p>
 */
@Component
public class UpdateApplicationTool implements MCPTool {

    private static final String TOOL_NAME = "cerberus_application_update";

    private final IApplicationService applicationService;
    private final ApplicationMapperV001 mapper;
    private final MCPLogUtils mcpLogUtils;

    public UpdateApplicationTool(IApplicationService applicationService, ApplicationMapperV001 mapper, MCPLogUtils mcpLogUtils) {
        this.applicationService = applicationService;
        this.mapper = mapper;
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
     * Builds the MCP {@link McpSchema.Tool} descriptor for this tool.
     *
     * <p>The input schema uses a nested {@code updates} object whose properties are
     * restricted to the supported mutable fields. {@code additionalProperties: false}
     * is enforced so the AI model cannot pass unknown fields that would silently be
     * ignored or misinterpreted.</p>
     *
     * @return the fully described tool specification ready for MCP server registration
     */
    private McpSchema.Tool createTool() {
        Map<String, Object> updateProperties = new LinkedHashMap<>();

        updateProperties.put("description", Map.of(
                "type", "string",
                "description", "New description of the application."
        ));

        updateProperties.put("type", Map.of(
                "type", "string",
                "description", "New type of the application."
        ));

        updateProperties.put("system", Map.of(
                "type", "string",
                "description", "System linked to the application."
        ));

        Map<String, Object> updatesSchema = new LinkedHashMap<>();
        updatesSchema.put("type", "object");
        updatesSchema.put("description", "Fields to update on the application. Only supported fields are allowed.");
        updatesSchema.put("properties", updateProperties);
        // Reject any field the switch-case does not handle, so the agent gets an explicit error
        updatesSchema.put("additionalProperties", false);

        Map<String, Object> properties = new LinkedHashMap<>();

        properties.put("application", Map.of(
                "type", "string",
                "description", "Name of the application to update."
        ));

        properties.put("updates", updatesSchema);

        return new McpSchema.Tool(
                TOOL_NAME,
                null,
                """
                Updates an existing application in Cerberus.

                Updates an existing application in Cerberus.

                Call this tool whenever the user asks to modify or change properties of an existing application.

                The application name is required.

                Only explicitly supported fields can be updated:
                - description
                - type
                - system

                Do not call this tool when the user only asks to display, list, read, or search an application.
                """,
                new McpSchema.JsonSchema(
                        "object",
                        properties,
                        List.of("application", "updates"),
                        null,
                        null,
                        null
                ),
                null,
                MCPToolUtils.updateAnnotations("Update application", false),
                null
        );
    }

    /**
     * Executes the update: reads the current {@link Application} record, applies the
     * requested field mutations, then persists the result via
     * {@link IApplicationService#update(String, Application)}.
     *
     * <p>The read-before-write pattern is intentional: it lets us return a clear
     * "does not exist" error rather than silently creating or corrupting data, and it
     * gives us the full current DTO so untouched fields keep their existing values.</p>
     *
     * @param args parsed MCP arguments map, must contain {@code application} and {@code updates}
     * @return a success JSON result listing updated fields, or an error text result
     */
    @SuppressWarnings("unchecked")
    private McpSchema.CallToolResult execute(Map<String, Object> args) {
        String applicationName = MCPToolUtils.getString(args, "application", "");

        if (applicationName.isBlank()) {
            return MCPToolUtils.errorText("Missing required parameter: application");
        }

        Object updatesObject = args.get("updates");

        if (!(updatesObject instanceof Map)) {
            return MCPToolUtils.errorText("Missing or invalid required parameter: updates");
        }

        Map<String, Object> updates = (Map<String, Object>) updatesObject;

        if (updates.isEmpty()) {
            return MCPToolUtils.errorText("No field provided to update.");
        }

        // Read the existing entity so unmodified fields retain their current values
        AnswerItem<Application> readAnswer = applicationService.readByKey(applicationName);

        if (!readAnswer.isCodeStringEquals("OK") || readAnswer.getItem() == null) {
            return MCPToolUtils.errorText("Application does not exist: " + applicationName);
        }

        Application application = readAnswer.getItem();

        try {
            for (Map.Entry<String, Object> entry : updates.entrySet()) {
                String field = entry.getKey();
                Object value = entry.getValue();

                switch (field) {
                    case "description":
                        application.setDescription(asString(value, field));
                        break;
                    case "type":
                        application.setType(asString(value, field));
                        break;
                    case "system":
                        application.setSystem(asString(value, field));
                        break;
                    default:
                        return MCPToolUtils.errorText("Unsupported field for application update: " + field);
                }
            }
        } catch (IllegalArgumentException e) {
            return MCPToolUtils.errorText(e.getMessage());
        }

        // Tag the modifier so audit trails identify MCP-originated changes.
        application.setUsrModif("MCP");

        Answer updateAnswer = applicationService.update(application.getApplication(), application);

        if (!updateAnswer.isCodeStringEquals("OK")) {
            return MCPToolUtils.errorText(
                    "Unable to update Application " + applicationName + ": "
                            + updateAnswer.getMessageDescription()
            );
        }

        return MCPToolUtils.successJson(Map.of(
                "status", "updated",
                "application", mapper.toDTO(application)
        ));
    }

    /**
     * Coerces a raw MCP argument value to a trimmed {@link String}.
     *
     * @param value the raw value from the MCP arguments map
     * @param field the field name, used in the exception message for clear diagnostics
     * @return the trimmed string, or an empty string when {@code value} is {@code null}
     * @throws IllegalArgumentException if {@code value} is non-null but not a {@link String}
     */
    private String asString(Object value, String field) {
        if (value == null) {
            return "";
        }

        if (!(value instanceof String)) {
            throw new IllegalArgumentException("Invalid value for field '" + field + "'. Expected string.");
        }

        return ((String) value).trim();
    }

}