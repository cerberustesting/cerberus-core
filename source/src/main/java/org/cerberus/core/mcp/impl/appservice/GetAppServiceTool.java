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
import org.cerberus.core.util.answer.AnswerItem;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

/**
 * MCP tool implementation for {@code cerberus_appservice_get}.
 *
 * <p>Retrieves the full details of a single {@link AppService} by its exact service name,
 * including all associated headers and content parameters.</p>
 *
 * <p>Delegates to {@link IAppServiceService#readByKeyWithDependency(String)}, which populates
 * both the {@code headerList} and {@code contentList} transient fields on the returned entity.
 * The result is then mapped to a DTO via {@link AppServiceMapperV001}.</p>
 */
@Component
public class GetAppServiceTool implements MCPTool {

    private static final String TOOL_NAME = "cerberus_appservice_get";

    private final IAppServiceService appServiceService;
    private final AppServiceMapperV001 mapper;
    private final MCPLogUtils mcpLogUtils;

    public GetAppServiceTool(IAppServiceService appServiceService, AppServiceMapperV001 mapper, MCPLogUtils mcpLogUtils) {
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
        Map<String, Object> properties = Map.of(
                "service", Map.of(
                        "type", "string",
                        "description", "Exact name of the app service to retrieve."
                )
        );

        return new McpSchema.Tool(
                TOOL_NAME,
                null,
                """
                Retrieves the full details of a specific app service by its exact name, including all headers and content parameters.
                Call this tool when the user asks to inspect or display a specific service.
                Use cerberus_appservice_list when the service name is unknown.
                Do not call this tool when the user asks to create, update, or delete a service.""",
                new McpSchema.JsonSchema(
                        "object",
                        properties,
                        List.of("service"),
                        null,
                        null,
                        null
                ),
                null,
                MCPToolUtils.readOnlyAnnotations("Get app service", true),
                null
        );
    }

    private McpSchema.CallToolResult execute(Map<String, Object> args) {
        String service = MCPToolUtils.getString(args, "service", "");

        mcpLogUtils.call(TOOL_NAME, "appservice_get", String.format("MCP tool %s called with service=%s", TOOL_NAME, service));

        if (service.isBlank()) {
            return MCPToolUtils.errorText("Parameter 'service' is required and must not be blank.");
        }

        AnswerItem<AppService> answer = appServiceService.readByKeyWithDependency(service);

        if (!answer.isCodeStringEquals("OK") || answer.getItem() == null) {
            return MCPToolUtils.errorText("App service not found: " + service);
        }

        return MCPToolUtils.successJson(mapper.toDTO(answer.getItem()));
    }

}
