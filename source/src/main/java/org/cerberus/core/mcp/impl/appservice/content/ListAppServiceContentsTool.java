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
package org.cerberus.core.mcp.impl.appservice.content;

import io.modelcontextprotocol.server.McpServerFeatures;
import io.modelcontextprotocol.spec.McpSchema;
import org.cerberus.core.api.dto.appservice.AppServiceContentDTOV001;
import org.cerberus.core.api.dto.appservice.AppServiceContentMapperV001;
import org.cerberus.core.crud.entity.AppServiceContent;
import org.cerberus.core.crud.service.IAppServiceContentService;
import org.cerberus.core.mcp.MCPTool;
import org.cerberus.core.mcp.util.MCPLogUtils;
import org.cerberus.core.mcp.util.MCPToolUtils;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

/**
 * MCP tool that exposes {@link AppServiceContent} listing under the tool name
 * {@code cerberus_appservice_content_list}.
 *
 * <p>Returns all content parameters defined for a given app service by delegating to
 * {@link IAppServiceContentService#readByVarious(String)}. Each content entry is mapped to a DTO
 * via {@link AppServiceContentMapperV001} before being returned as JSON.</p>
 */
@Component
public class ListAppServiceContentsTool implements MCPTool {

    private static final String TOOL_NAME = "cerberus_appservice_content_list";

    private final IAppServiceContentService appServiceContentService;
    private final AppServiceContentMapperV001 mapper;
    private final MCPLogUtils mcpLogUtils;

    public ListAppServiceContentsTool(IAppServiceContentService appServiceContentService,
                                      AppServiceContentMapperV001 mapper,
                                      MCPLogUtils mcpLogUtils) {
        this.appServiceContentService = appServiceContentService;
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
                        "description", "The name (primary key) of the app service whose content parameters should be listed."
                )
        );

        return new McpSchema.Tool(
                TOOL_NAME,
                null,
                """
                Lists all content parameters (form data, URL-encoded params, etc.) defined for a specific app service.
                Call this tool when the user asks to browse or inspect the content parameters of a service.
                Use cerberus_appservice_content_get to retrieve details of a specific content by key.
                Do not call this tool when the user asks to create, update, or delete content.
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
                MCPToolUtils.readOnlyAnnotations("List app service contents", false),
                null
        );
    }

    private McpSchema.CallToolResult execute(Map<String, Object> args) {
        String service = MCPToolUtils.getString(args, "service", "");

        mcpLogUtils.call(TOOL_NAME, "appservice_content_list",
                String.format("MCP tool %s called with service=%s", TOOL_NAME, service));

        if (service.isBlank()) {
            return MCPToolUtils.errorText("Missing required parameter: service");
        }

        var answer = appServiceContentService.readByVarious(service);

        List<AppServiceContentDTOV001> list = answer.getDataList().stream()
                .filter(AppServiceContent.class::isInstance)
                .map(AppServiceContent.class::cast)
                .map(mapper::toDTO)
                .toList();

        return MCPToolUtils.successJson(Map.of(
                "service", service,
                "count", list.size(),
                "contents", list
        ));
    }
}
