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
import org.cerberus.core.mcp.MCPTool;
import org.cerberus.core.mcp.util.MCPLogUtils;
import org.cerberus.core.mcp.util.MCPToolUtils;
import org.cerberus.core.api.dto.appservice.AppServiceMapperV001;
import org.cerberus.core.crud.entity.AppService;
import org.cerberus.core.crud.service.IAppServiceService;
import org.cerberus.core.util.answer.Answer;
import org.cerberus.core.util.answer.AnswerItem;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * MCP tool that creates a new {@link AppService} entity in Cerberus.
 *
 * <p>Exposed MCP tool name: {@code cerberus_appservice_create}</p>
 *
 * <p>Performs a duplicate check via {@link IAppServiceService#readByKey(String)} before
 * attempting to persist, returning a clear error when the service name is already taken.</p>
 *
 * <p>Delegates to {@link IAppServiceService} for persistence.</p>
 */
@Component
public class CreateAppServiceTool implements MCPTool {

    private static final String TOOL_NAME = "cerberus_appservice_create";

    private final IAppServiceService appServiceService;
    private final AppServiceMapperV001 mapper;
    private final MCPLogUtils mcpLogUtils;

    public CreateAppServiceTool(IAppServiceService appServiceService,
                                AppServiceMapperV001 mapper,
                                MCPLogUtils mcpLogUtils) {
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

    /**
     * Builds the MCP tool schema, declaring the tool name, description, and JSON input schema.
     *
     * @return the tool specification describing accepted parameters and their constraints
     */
    private McpSchema.Tool createTool() {
        Map<String, Object> properties = new LinkedHashMap<>();
        properties.put("service", Map.of(
                "type", "string",
                "description", "Unique name / identifier of the new app service."
        ));
        properties.put("type", Map.of(
                "type", "string",
                "description", "Service type. Allowed values: "
                        + AppService.TYPE_REST + ", "
                        + AppService.TYPE_SOAP + ", "
                        + AppService.TYPE_FTP + ", "
                        + AppService.TYPE_KAFKA + ", "
                        + AppService.TYPE_MONGODB + ".",
                "enum", List.of(
                        AppService.TYPE_REST,
                        AppService.TYPE_SOAP,
                        AppService.TYPE_FTP,
                        AppService.TYPE_KAFKA,
                        AppService.TYPE_MONGODB
                )
        ));
        properties.put("method", Map.of(
                "type", "string",
                "description", "HTTP / protocol method. Allowed values: "
                        + AppService.METHOD_HTTPGET + ", "
                        + AppService.METHOD_HTTPPOST + ", "
                        + AppService.METHOD_HTTPPUT + ", "
                        + AppService.METHOD_HTTPPATCH + ", "
                        + AppService.METHOD_HTTPDELETE + ", "
                        + AppService.METHOD_KAFKAPRODUCE + ", "
                        + AppService.METHOD_KAFKASEARCH + ", "
                        + AppService.METHOD_MONGODBFIND + ", "
                        + AppService.METHOD_MONGODBUPDATEONE + ", "
                        + AppService.METHOD_MONGODBINSERTONE + ", "
                        + AppService.METHOD_MONGODBREPLACEONE + ".",
                "enum", List.of(
                        AppService.METHOD_HTTPGET,
                        AppService.METHOD_HTTPPOST,
                        AppService.METHOD_HTTPPUT,
                        AppService.METHOD_HTTPPATCH,
                        AppService.METHOD_HTTPDELETE,
                        AppService.METHOD_KAFKAPRODUCE,
                        AppService.METHOD_KAFKASEARCH,
                        AppService.METHOD_MONGODBFIND,
                        AppService.METHOD_MONGODBUPDATEONE,
                        AppService.METHOD_MONGODBINSERTONE,
                        AppService.METHOD_MONGODBREPLACEONE
                )
        ));
        properties.put("application", Map.of(
                "type", "string",
                "description", "Optional application this service belongs to."
        ));
        properties.put("servicePath", Map.of(
                "type", "string",
                "description", "Optional URL / path of the service endpoint."
        ));
        properties.put("serviceRequest", Map.of(
                "type", "string",
                "description", "Optional default request body / payload."
        ));
        properties.put("bodyType", Map.of(
                "type", "string",
                "description", "Optional body type. Allowed values: "
                        + AppService.SRVBODYTYPE_NONE + ", "
                        + AppService.SRVBODYTYPE_RAW + ", "
                        + AppService.SRVBODYTYPE_FORMDATA + ", "
                        + AppService.SRVBODYTYPE_FORMURLENCODED + ".",
                "enum", List.of(
                        AppService.SRVBODYTYPE_NONE,
                        AppService.SRVBODYTYPE_RAW,
                        AppService.SRVBODYTYPE_FORMDATA,
                        AppService.SRVBODYTYPE_FORMURLENCODED
                )
        ));
        properties.put("description", Map.of(
                "type", "string",
                "description", "Optional human-readable description of the service."
        ));

        return new McpSchema.Tool(
                TOOL_NAME,
                null,
                """
                Creates a new app service in Cerberus.

                Call this tool whenever the user asks to create, add, or register a new service / API endpoint.
                Requires a unique service name, a type, and a method.
                """,
                new McpSchema.JsonSchema(
                        "object",
                        properties,
                        List.of("service", "type", "method"),
                        null,
                        null,
                        null
                ),
                null,
                MCPToolUtils.createAnnotations("Create app service", false),
                null
        );
    }

    /**
     * Validates input, checks for duplicates, creates the {@link AppService} entity, and returns the result.
     *
     * <p>The duplicate check with {@link IAppServiceService#readByKey(String)} prevents a redundant
     * DAO call and surfaces a clearer error message than letting the database throw a unique-key
     * violation.</p>
     *
     * @param args the MCP call arguments extracted from the request
     * @return a success result containing the created service DTO, or an error result on failure
     */
    private McpSchema.CallToolResult execute(Map<String, Object> args) {
        String service = MCPToolUtils.getString(args, "service", "");
        String type = MCPToolUtils.getString(args, "type", "");
        String method = MCPToolUtils.getString(args, "method", "");
        String application = MCPToolUtils.getString(args, "application", "");
        String servicePath = MCPToolUtils.getString(args, "servicePath", "");
        String serviceRequest = MCPToolUtils.getString(args, "serviceRequest", "");
        String bodyType = MCPToolUtils.getString(args, "bodyType", "");
        String description = MCPToolUtils.getString(args, "description", "");

        mcpLogUtils.call(TOOL_NAME, "appservice_create",
                String.format("MCP tool %s called with service=%s, type=%s, method=%s",
                        TOOL_NAME, service, type, method));

        if (service.isBlank()) {
            return MCPToolUtils.errorText("Missing required parameter: service");
        }
        if (type.isBlank()) {
            return MCPToolUtils.errorText("Missing required parameter: type");
        }
        if (method.isBlank()) {
            return MCPToolUtils.errorText("Missing required parameter: method");
        }

        AnswerItem<AppService> existingAnswer = appServiceService.readByKey(service);
        if (existingAnswer.isCodeStringEquals("OK") && existingAnswer.getItem() != null) {
            return MCPToolUtils.errorText("Service already exists: " + service);
        }

        AppService appService = new AppService();
        appService.setService(service);
        appService.setType(type);
        appService.setMethod(method);
        appService.setApplication(application);
        appService.setServicePath(servicePath);
        appService.setServiceRequest(serviceRequest);
        appService.setBodyType(bodyType);
        appService.setDescription(description);
        appService.setUsrCreated("MCP");

        Answer answer = appServiceService.create(appService);

        if (!answer.isCodeStringEquals("OK")) {
            return MCPToolUtils.errorText("Unable to create service " + service + ": " + answer.getMessageDescription());
        }

        return MCPToolUtils.successJson(Map.of(
                "status", "created",
                "service", mapper.toDTO(appService)
        ));
    }
}
