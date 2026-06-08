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
import org.cerberus.core.crud.entity.Application;
import org.cerberus.core.crud.service.IApplicationService;
import org.cerberus.core.mcp.MCPTool;
import org.cerberus.core.mcp.util.MCPLogUtils;
import org.cerberus.core.mcp.util.MCPToolUtils;
import org.cerberus.core.util.answer.AnswerItem;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

/**
 * MCP tool that retrieves a single {@link Application} entity by its unique name.
 *
 * <p>Exposed MCP tool name: {@code cerberus_application_get}.</p>
 *
 * <p>Delegates to {@link IApplicationService#readByKey(String)} for the lookup and
 * converts the result to a full DTO via {@link ApplicationMapperV001}.</p>
 *
 * <p>Use {@code list_applications} instead when the application name is unknown or
 * when browsing all available applications.</p>
 */
@Component
public class GetApplicationTool implements MCPTool {

    private static final String TOOL_NAME = "cerberus_application_get";

    private final IApplicationService applicationService;
    private final ApplicationMapperV001 mapper;
    private final MCPLogUtils mcpLogUtils;

    public GetApplicationTool(IApplicationService applicationService, ApplicationMapperV001 mapper, MCPLogUtils mcpLogUtils) {
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
     * Builds the MCP {@link McpSchema.Tool} descriptor for {@code cerberus_application_get}.
     *
     * <p>Declares {@code application} as the only required parameter.
     * No enum constraint is applied — the application name is a free-form string.</p>
     *
     * @return the fully configured tool descriptor
     */
    private McpSchema.Tool createTool() {
        Map<String, Object> properties = Map.of(
                "application", Map.of(
                        "type", "string",
                        "description", "Exact name of the application to retrieve."
                )
        );

        return new McpSchema.Tool(
                TOOL_NAME,
                null,
                """
                Retrieves the full details of a specific Cerberus application by its exact name.

                Call this tool whenever the user asks to inspect or display a specific application by name.

                Use list_applications instead when the application name is unknown or to browse all applications.
                Do not call this tool when the user asks to create, update, or delete an application.
                """,
                new McpSchema.JsonSchema(
                        "object",
                        properties,
                        List.of("application"),
                        null,
                        null,
                        null
                ),
                null,
                MCPToolUtils.readOnlyAnnotations("Get application", true),
                null
        );
    }

    /**
     * Validates the argument, calls the service, and returns the application DTO.
     *
     * @param args raw MCP arguments map from the client request
     * @return a success JSON result containing the application DTO, or an error text result
     */
    private McpSchema.CallToolResult execute(Map<String, Object> args) {
        String applicationName = MCPToolUtils.getString(args, "application", "");

        mcpLogUtils.call(TOOL_NAME, "application_get",
                String.format("MCP tool %s called with application=%s", TOOL_NAME, applicationName));

        if (applicationName.isBlank()) {
            return MCPToolUtils.errorText("Missing required parameter: application");
        }

        AnswerItem<Application> answer = applicationService.readByKey(applicationName);

        if (!answer.isCodeStringEquals("OK") || answer.getItem() == null) {
            return MCPToolUtils.errorText("Application does not exist: " + applicationName);
        }

        return MCPToolUtils.successJson(mapper.toDTO(answer.getItem()));
    }
}
