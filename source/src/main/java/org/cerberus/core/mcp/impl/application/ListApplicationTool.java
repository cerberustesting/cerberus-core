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
import org.cerberus.core.mcp.util.MCPProjectionUtils;
import org.cerberus.core.mcp.util.MCPToolUtils;
import org.cerberus.core.crud.entity.Application;
import org.cerberus.core.crud.service.IApplicationService;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
public class ListApplicationTool implements MCPTool {

    private static final String TOOL_NAME = "list_applications";

    private static final List<String> ALL_FIELDS = List.of("application", "description", "sort", "type", "system", "subsystem", "svnurl",
            "bugTrackerUrl", "bugTrackerNewUrl", "poolSize", "deploytype", "mavengroupid",
            "bugTrackerConnector", "bugTrackerParam1", "bugTrackerParam2", "bugTrackerParam3",
            "environments", "usrCreated", "dateCreated", "usrModif", "dateModif");

    private final IApplicationService applicationService;
    private final ApplicationMapperV001 applicationMapper;

    public ListApplicationTool(IApplicationService applicationService, ApplicationMapperV001 applicationMapper) {
        this.applicationService = applicationService;
        this.applicationMapper = applicationMapper;
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
                "intent", Map.of(
                        "type", "string",
                        "description", """
                                Optional usage context used to optimize the returned fields when fields is not provided.
                
                                Use:
                                - select_application when the user needs to choose an application.
                                  Default fields: application, description, type, system.
                
                                - create_testcase before creating a testcase when the application is unknown.
                                  Default fields: application, type.
                
                                - inspect_application when detailed application metadata is needed.
                                  Default fields: ALL_FIELDS.
                
                                If fields is provided, fields takes precedence over intent defaults.
                                Default: select_application.
                                """,
                        "enum", List.of(
                                "select_application",
                                "create_testcase",
                                "inspect_application"
                        )
                ),
                "search", Map.of(
                        "type", "string",
                        "description", "Optional text filter on application name or description."
                ),
                "fields", Map.of(
                        "type", "array",
                        "description", "Optional list of fields to return.",
                        "items", Map.of(
                                "type", "string",
                                "enum", ALL_FIELDS
                        )
                )
        );

        return new McpSchema.Tool(
                TOOL_NAME,
                null,
                """
                Returns the list of Cerberus applications available to the user.

                Use this tool when the target application is unknown or when the user needs to select an application.
                This tool can be used before creating a testcase, or retrieving application-specific capabilities.

                Use intent to describe the current usage context.
                Use fields to reduce the returned data to what is useful for the current task.
                """,
                new McpSchema.JsonSchema(
                        "object",
                        properties,
                        null,
                        null,
                        null,
                        null
                ),
                null,
                null,
                null
        );
    }

    private McpSchema.CallToolResult execute(Map<String, Object> args) {
        String intent = MCPToolUtils.getString(args, "intent", "");
        String search = MCPToolUtils.getString(args, "search", "");

        List<String> fields = MCPToolUtils.getStringList(
                args,
                "fields",
                defaultFieldsForIntent(intent)
        );

        List<Map<String, Object>> applications = applicationService.readAll()
                .getDataList()
                .stream()
                .map(Application.class::cast)
                .filter(app -> matchesSearch(app, search))
                .map(applicationMapper::toDTO)
                .map(dto -> MCPProjectionUtils.project(dto, fields))
                .toList();

        return MCPToolUtils.successJson(Map.of(
                "intent", intent,
                "count", applications.size(),
                "applications", applications
        ));
    }

    private List<String> defaultFieldsForIntent(String intent) {
        return switch (intent) {
            case "create_testcase" -> List.of("application","type");
            case "inspect_application" -> ALL_FIELDS;
            case "select_application" -> List.of("application","description","type","system");
            default -> List.of("application");
        };
    }

    private boolean matchesSearch(Application app, String search) {
        if (search == null || search.isBlank()) {
            return true;
        }

        return MCPToolUtils.containsIgnoreCase(app.getApplication(), search)
                || MCPToolUtils.containsIgnoreCase(app.getDescription(), search);
    }

}