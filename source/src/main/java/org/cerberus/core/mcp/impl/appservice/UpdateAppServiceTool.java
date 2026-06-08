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
package org.cerberus.core.mcp.impl.appservice;

import io.modelcontextprotocol.server.McpServerFeatures;
import io.modelcontextprotocol.spec.McpSchema;
import org.cerberus.core.api.dto.appservice.AppServiceMapperV001;
import org.cerberus.core.crud.entity.AppService;
import org.cerberus.core.crud.service.IAppServiceService;
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
 * MCP tool that manages updates to existing {@link AppService} entities in Cerberus.
 *
 * <p>Exposes the MCP tool name {@code cerberus_appservice_update}, which allows an AI agent
 * to mutate a subset of fields (description, type, method, application, servicePath,
 * serviceRequest, bodyType) on a service identified by its unique name. Delegates
 * persistence to {@link IAppServiceService}.</p>
 *
 * <p>The read-before-write pattern is applied: the existing record is fetched first so
 * that unmodified fields retain their current values and a clear "does not exist" error
 * is returned instead of silently creating or corrupting data.</p>
 *
 * <p>Only explicitly declared fields are accepted; any unrecognised field causes an
 * immediate error response, preventing unintended mutations.</p>
 */
@Component
public class UpdateAppServiceTool implements MCPTool {

    private static final String TOOL_NAME = "cerberus_appservice_update";

    private final IAppServiceService appServiceService;
    private final AppServiceMapperV001 mapper;
    private final MCPLogUtils mcpLogUtils;

    public UpdateAppServiceTool(IAppServiceService appServiceService, AppServiceMapperV001 mapper, MCPLogUtils mcpLogUtils) {
        this.appServiceService = appServiceService;
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

    private McpSchema.Tool createTool() {
        Map<String, Object> updateProperties = new LinkedHashMap<>();

        updateProperties.put("description", Map.of(
                "type", "string",
                "description", "New description of the service."
        ));

        updateProperties.put("type", Map.of(
                "type", "string",
                "enum", List.of("SOAP", "REST", "FTP", "KAFKA", "MONGODB"),
                "description", "New type of the service."
        ));

        updateProperties.put("method", Map.of(
                "type", "string",
                "enum", List.of("POST", "GET", "DELETE", "PUT", "PATCH", "PRODUCE", "SEARCH", "FIND", "UPDATEONE", "INSERTONE", "REPLACEONE"),
                "description", "New HTTP or protocol method of the service."
        ));

        updateProperties.put("application", Map.of(
                "type", "string",
                "description", "Application linked to the service."
        ));

        updateProperties.put("servicePath", Map.of(
                "type", "string",
                "description", "New path or URL of the service."
        ));

        updateProperties.put("serviceRequest", Map.of(
                "type", "string",
                "description", "New request body or payload template of the service."
        ));

        updateProperties.put("bodyType", Map.of(
                "type", "string",
                "enum", List.of("none", "raw", "form-data", "form-urlencoded"),
                "description", "New body type of the service request."
        ));

        Map<String, Object> updatesSchema = new LinkedHashMap<>();
        updatesSchema.put("type", "object");
        updatesSchema.put("description", "Fields to update on the service. Only supported fields are allowed.");
        updatesSchema.put("properties", updateProperties);
        updatesSchema.put("additionalProperties", false);

        Map<String, Object> properties = new LinkedHashMap<>();

        properties.put("service", Map.of(
                "type", "string",
                "description", "Name (primary key) of the service to update."
        ));

        properties.put("updates", updatesSchema);

        return new McpSchema.Tool(
                TOOL_NAME,
                null,
                """
                Updates an existing app service in Cerberus.

                Call this tool whenever the user asks to modify or change properties of an existing service.

                The service name is required.

                Only explicitly supported fields can be updated:
                - description
                - type  (SOAP, REST, FTP, KAFKA, MONGODB)
                - method  (POST, GET, DELETE, PUT, PATCH, PRODUCE, SEARCH, FIND, UPDATEONE, INSERTONE, REPLACEONE)
                - application
                - servicePath
                - serviceRequest
                - bodyType  (none, raw, form-data, form-urlencoded)

                Do not call this tool when the user only asks to display, list, read, or search a service.
                """,
                new McpSchema.JsonSchema(
                        "object",
                        properties,
                        List.of("service", "updates"),
                        null,
                        null,
                        null
                ),
                null,
                MCPToolUtils.updateAnnotations("Update app service", false),
                null
        );
    }

    @SuppressWarnings("unchecked")
    private McpSchema.CallToolResult execute(Map<String, Object> args) {
        mcpLogUtils.call(TOOL_NAME, "update", "MCP tool cerberus_appservice_update called with args: " + args);

        String service = MCPToolUtils.getString(args, "service", "");

        if (service.isBlank()) {
            return MCPToolUtils.errorText("Missing required parameter: service");
        }

        Object updatesObject = args.get("updates");

        if (!(updatesObject instanceof Map)) {
            return MCPToolUtils.errorText("Missing or invalid required parameter: updates");
        }

        Map<String, Object> updates = (Map<String, Object>) updatesObject;

        if (updates.isEmpty()) {
            return MCPToolUtils.errorText("No field provided to update.");
        }

        AnswerItem<AppService> readAnswer = appServiceService.readByKey(service);

        if (!readAnswer.isCodeStringEquals("OK") || readAnswer.getItem() == null) {
            return MCPToolUtils.errorText("App service does not exist: " + service);
        }

        AppService appService = readAnswer.getItem();

        try {
            for (Map.Entry<String, Object> entry : updates.entrySet()) {
                String field = entry.getKey();
                Object value = entry.getValue();

                switch (field) {
                    case "description":
                        appService.setDescription(asString(value, field));
                        break;
                    case "type":
                        appService.setType(asString(value, field));
                        break;
                    case "method":
                        appService.setMethod(asString(value, field));
                        break;
                    case "application":
                        appService.setApplication(asString(value, field));
                        break;
                    case "servicePath":
                        appService.setServicePath(asString(value, field));
                        break;
                    case "serviceRequest":
                        appService.setServiceRequest(asString(value, field));
                        break;
                    case "bodyType":
                        appService.setBodyType(asString(value, field));
                        break;
                    default:
                        return MCPToolUtils.errorText("Unsupported field: " + field);
                }
            }
        } catch (IllegalArgumentException e) {
            return MCPToolUtils.errorText(e.getMessage());
        }

        appService.setUsrModif("MCP");

        Answer updateAnswer = appServiceService.update(service, appService);

        if (!updateAnswer.isCodeStringEquals("OK")) {
            return MCPToolUtils.errorText(
                    "Unable to update app service " + service + ": "
                            + updateAnswer.getMessageDescription()
            );
        }

        return MCPToolUtils.successJson(Map.of(
                "status", "updated",
                "service", mapper.toDTO(appService)
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

}
