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
import org.cerberus.core.mcp.MCPTool;
import org.cerberus.core.mcp.util.MCPToolUtils;
import org.cerberus.core.crud.entity.Application;
import org.cerberus.core.crud.service.IApplicationService;
import org.cerberus.core.util.answer.Answer;
import org.cerberus.core.util.answer.AnswerItem;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Component
public class UpdateApplicationTool implements MCPTool {

    private static final String TOOL_NAME = "cerberus_application_update";

    private final IApplicationService applicationService;

    public UpdateApplicationTool(IApplicationService applicationService) {
        this.applicationService = applicationService;
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

        updateProperties.put("active", Map.of(
                "type", "boolean",
                "description", "Whether the application is active."
        ));

        Map<String, Object> updatesSchema = new LinkedHashMap<>();
        updatesSchema.put("type", "object");
        updatesSchema.put("description", "Fields to update on the application. Only supported fields are allowed.");
        updatesSchema.put("properties", updateProperties);
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

                Call this tool whenever the user asks to modify, update, activate, deactivate,
                or change properties of an existing application.

                The application name is required.

                Only explicitly supported fields can be updated:
                - description
                - type
                - system
                - active

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
                null,
                null
        );
    }

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

        AnswerItem<Application> readAnswer = applicationService.readByKey(applicationName);

        if (!readAnswer.isCodeStringEquals("OK") || readAnswer.getItem() == null) {
            return MCPToolUtils.errorText("Application does not exist: " + applicationName);
        }

        Application application = readAnswer.getItem();

        Map<String, Object> modifiedFields = new LinkedHashMap<>();

        try {
            for (Map.Entry<String, Object> entry : updates.entrySet()) {
                String field = entry.getKey();
                Object value = entry.getValue();

                switch (field) {
                    case "description":
                        String description = asString(value, field);
                        application.setDescription(description);
                        modifiedFields.put(field, description);
                        break;

                    case "type":
                        String type = asString(value, field);
                        application.setType(type);
                        modifiedFields.put(field, type);
                        break;

                    case "system":
                        String system = asString(value, field);
                        application.setSystem(system);
                        modifiedFields.put(field, system);
                        break;

                    default:
                        return MCPToolUtils.errorText("Unsupported field for application update: " + field);
                }
            }
        } catch (IllegalArgumentException e) {
            return MCPToolUtils.errorText(e.getMessage());
        }

        if (modifiedFields.isEmpty()) {
            return MCPToolUtils.errorText("No valid field provided to update.");
        }

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
                "application", applicationName,
                "updatedFields", modifiedFields
        ));
    }

    private String asString(Object value, String field) {
        if (value == null) {
            return "";
        }

        if (!(value instanceof String)) {
            throw new IllegalArgumentException("Invalid value for field '" + field + "'. Expected string.");
        }

        return ((String) value).trim();
    }

    private Boolean asBoolean(Object value, String field) {
        if (!(value instanceof Boolean)) {
            throw new IllegalArgumentException("Invalid value for field '" + field + "'. Expected boolean.");
        }

        return (Boolean) value;
    }
}