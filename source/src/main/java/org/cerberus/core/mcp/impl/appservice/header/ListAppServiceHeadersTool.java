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
package org.cerberus.core.mcp.impl.appservice.header;

import io.modelcontextprotocol.server.McpServerFeatures;
import io.modelcontextprotocol.spec.McpSchema;
import org.cerberus.core.api.dto.appservice.AppServiceHeaderDTOV001;
import org.cerberus.core.api.dto.appservice.AppServiceHeaderMapperV001;
import org.cerberus.core.crud.entity.AppServiceHeader;
import org.cerberus.core.crud.service.IAppServiceHeaderService;
import org.cerberus.core.mcp.MCPTool;
import org.cerberus.core.mcp.util.MCPLogUtils;
import org.cerberus.core.mcp.util.MCPToolUtils;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

/**
 * MCP tool that exposes {@link AppServiceHeader} listing under the tool name
 * {@code cerberus_appservice_header_list}.
 *
 * <p>Returns all headers defined for a given app service by delegating to
 * {@link IAppServiceHeaderService#readByVarious(String)}. Each header is mapped to a DTO
 * via {@link AppServiceHeaderMapperV001} before being returned as JSON.</p>
 */
@Component
public class ListAppServiceHeadersTool implements MCPTool {

    private static final String TOOL_NAME = "cerberus_appservice_header_list";

    private final IAppServiceHeaderService appServiceHeaderService;
    private final AppServiceHeaderMapperV001 mapper;
    private final MCPLogUtils mcpLogUtils;

    public ListAppServiceHeadersTool(IAppServiceHeaderService appServiceHeaderService,
                                     AppServiceHeaderMapperV001 mapper,
                                     MCPLogUtils mcpLogUtils) {
        this.appServiceHeaderService = appServiceHeaderService;
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
                        "description", "The name (primary key) of the app service whose headers should be listed."
                )
        );

        return new McpSchema.Tool(
                TOOL_NAME,
                null,
                """
                Lists all headers defined for a specific app service.
                Call this tool when the user asks to browse or inspect the headers of a service.
                Use cerberus_appservice_header_get to retrieve details of a specific header by key.
                Do not call this tool when the user asks to create, update, or delete a header.
                """,
                new McpSchema.JsonSchema(
                        "object",
                        properties,
                        List.of("service"),
                        null,
                        null,
                        null
                ),
                null,
                MCPToolUtils.readOnlyAnnotations("List app service headers", false),
                null
        );
    }

    private McpSchema.CallToolResult execute(Map<String, Object> args) {
        String service = MCPToolUtils.getString(args, "service", "");

        mcpLogUtils.call(TOOL_NAME, "appservice_header_list",
                String.format("MCP tool %s called with service=%s", TOOL_NAME, service));

        if (service.isBlank()) {
            return MCPToolUtils.errorText("Missing required parameter: service");
        }

        var answer = appServiceHeaderService.readByVarious(service);

        List<AppServiceHeaderDTOV001> list = answer.getDataList().stream()
                .filter(AppServiceHeader.class::isInstance)
                .map(AppServiceHeader.class::cast)
                .map(mapper::toDTO)
                .toList();

        return MCPToolUtils.successJson(Map.of(
                "service", service,
                "count", list.size(),
                "headers", list
        ));
    }
}
