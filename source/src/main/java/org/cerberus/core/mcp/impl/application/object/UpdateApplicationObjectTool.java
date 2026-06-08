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
package org.cerberus.core.mcp.impl.application.object;

import io.modelcontextprotocol.server.McpServerFeatures;
import io.modelcontextprotocol.spec.McpSchema;
import org.cerberus.core.api.dto.application.ApplicationObjectMapperV001;
import org.cerberus.core.crud.entity.ApplicationObject;
import org.cerberus.core.crud.service.IApplicationObjectService;
import org.cerberus.core.mcp.MCPTool;
import org.cerberus.core.mcp.util.MCPLogUtils;
import org.cerberus.core.mcp.util.MCPToolUtils;
import org.cerberus.core.util.answer.Answer;
import org.cerberus.core.util.answer.AnswerItem;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * MCP tool that updates an existing {@link ApplicationObject} entity.
 *
 * <p>Exposed MCP tool name: {@code cerberus_application_object_update}.</p>
 *
 * <p>Applies a read-before-write pattern: the existing object is loaded first so
 * untouched fields retain their current values. Only the fields provided in the
 * {@code updates} map are modified.</p>
 *
 * <p>Delegates persistence to {@link IApplicationObjectService#update(String, String, ApplicationObject)}.
 * The service takes the original (application, object) key pair separately from the entity
 * to support potential renames — since we do not rename here, both are the same.</p>
 */
@Component
public class UpdateApplicationObjectTool implements MCPTool {

    private static final String TOOL_NAME = "cerberus_application_object_update";

    private final IApplicationObjectService applicationObjectService;
    private final ApplicationObjectMapperV001 mapper;
    private final MCPLogUtils mcpLogUtils;

    public UpdateApplicationObjectTool(IApplicationObjectService applicationObjectService,
                                       ApplicationObjectMapperV001 mapper,
                                       MCPLogUtils mcpLogUtils) {
        this.applicationObjectService = applicationObjectService;
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
     * Builds the MCP {@link McpSchema.Tool} descriptor for {@code cerberus_application_object_update}.
     *
     * <p>The input schema uses a nested {@code updates} object. {@code additionalProperties: false}
     * prevents the AI model from sending unrecognised fields that would be rejected by the switch
     * in {@link #execute(Map)}.</p>
     *
     * @return the fully configured tool descriptor
     */
    private McpSchema.Tool createTool() {
        Map<String, Object> updateProperties = new LinkedHashMap<>();
        updateProperties.put("value", Map.of(
                "type", "string",
                "description", "New locator value (e.g. XPath expression, CSS selector, element identifier)."
        ));
        updateProperties.put("xOffset", Map.of(
                "type", "string",
                "description", "New horizontal offset for element coordinates."
        ));
        updateProperties.put("yOffset", Map.of(
                "type", "string",
                "description", "New vertical offset for element coordinates."
        ));

        Map<String, Object> updatesSchema = new LinkedHashMap<>();
        updatesSchema.put("type", "object");
        updatesSchema.put("description", "Fields to update. Only provide the fields that need to change.");
        updatesSchema.put("properties", updateProperties);
        // Reject unknown fields early so the switch in execute() never hits the default branch unexpectedly.
        updatesSchema.put("additionalProperties", false);

        Map<String, Object> properties = new LinkedHashMap<>();
        properties.put("application", Map.of(
                "type", "string",
                "description", "Name of the application the object belongs to."
        ));
        properties.put("object", Map.of(
                "type", "string",
                "description", "Exact name of the application object to update."
        ));
        properties.put("updates", updatesSchema);

        return new McpSchema.Tool(
                TOOL_NAME,
                null,
                """
                Updates an existing application object (locator) for an application.

                Call this tool whenever the user asks to modify the locator value or offsets of an application object.
                Only provide the fields that need to change in the updates object.

                Use cerberus_application_object_list to find the exact object name before updating.

                Do not call this tool when the user only asks to list, read, create, or delete objects.
                """,
                new McpSchema.JsonSchema(
                        "object",
                        properties,
                        List.of("application", "object", "updates"),
                        null,
                        null,
                        null
                ),
                null,
                MCPToolUtils.updateAnnotations("Update application object", false),
                null
        );
    }

    /**
     * Validates input, loads the existing object, applies the requested field changes,
     * persists the update, and returns a JSON result with the updated DTO.
     *
     * @param args raw MCP arguments map from the client request
     * @return a success JSON result containing the updated object DTO, or an error text result
     */
    @SuppressWarnings("unchecked")
    private McpSchema.CallToolResult execute(Map<String, Object> args) {
        String application = MCPToolUtils.getString(args, "application", "");
        String object = MCPToolUtils.getString(args, "object", "");

        mcpLogUtils.call(TOOL_NAME, "application_object_update",
                String.format("MCP tool %s called with application=%s object=%s", TOOL_NAME, application, object));

        if (application.isBlank()) return MCPToolUtils.errorText("Missing required parameter: application");
        if (object.isBlank()) return MCPToolUtils.errorText("Missing required parameter: object");

        Object updatesObject = args.get("updates");
        if (!(updatesObject instanceof Map)) {
            return MCPToolUtils.errorText("Missing or invalid required parameter: updates");
        }
        Map<String, Object> updates = (Map<String, Object>) updatesObject;
        if (updates.isEmpty()) {
            return MCPToolUtils.errorText("No field provided to update.");
        }

        // Read the existing entity so unmodified fields retain their current values.
        AnswerItem<ApplicationObject> readAnswer = applicationObjectService.readByKey(application, object);
        if (!readAnswer.isCodeStringEquals("OK") || readAnswer.getItem() == null) {
            return MCPToolUtils.errorText("Application object does not exist: application=" + application + " object=" + object);
        }

        ApplicationObject appObject = readAnswer.getItem();

        try {
            for (Map.Entry<String, Object> entry : updates.entrySet()) {
                String field = entry.getKey();
                Object value = entry.getValue();
                switch (field) {
                    case "value":
                        appObject.setValue(asString(value, field));
                        break;
                    case "xOffset":
                        appObject.setXOffset(asString(value, field));
                        break;
                    case "yOffset":
                        appObject.setYOffset(asString(value, field));
                        break;
                    default:
                        return MCPToolUtils.errorText("Unsupported field for application object update: " + field);
                }
            }
        } catch (IllegalArgumentException e) {
            return MCPToolUtils.errorText(e.getMessage());
        }

        // Tag the modifier so audit trails identify MCP-originated changes.
        appObject.setUsrModif("MCP");

        // Service takes the original (application, object) key separately from the entity.
        Answer answer = applicationObjectService.update(application, object, appObject);

        if (!answer.isCodeStringEquals("OK")) {
            return MCPToolUtils.errorText("Unable to update application object: " + answer.getMessageDescription());
        }

        return MCPToolUtils.successJson(Map.of(
                "status", "updated",
                "object", mapper.toDTO(appObject)
        ));
    }

    /**
     * Coerces a raw MCP argument value to a trimmed {@link String}.
     *
     * @param value the raw value from the MCP arguments map
     * @param field the field name, used in the exception message
     * @return the trimmed string, or {@code ""} if {@code value} is {@code null}
     * @throws IllegalArgumentException if {@code value} is non-null but not a {@link String}
     */
    private String asString(Object value, String field) {
        if (value == null) return "";
        if (!(value instanceof String)) {
            throw new IllegalArgumentException("Invalid value for field '" + field + "'. Expected string.");
        }
        return ((String) value).trim();
    }
}
