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
import org.cerberus.core.util.answer.AnswerItem;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

/**
 * MCP tool that retrieves a single {@link ApplicationObject} by its composite key
 * (application + object name).
 *
 * <p>Exposed MCP tool name: {@code cerberus_application_object_get}.</p>
 *
 * <p>Delegates to {@link IApplicationObjectService#readByKey(String, String)} and converts
 * the result via {@link ApplicationObjectMapperV001}.</p>
 */
@Component
public class GetApplicationObjectTool implements MCPTool {

    private static final String TOOL_NAME = "cerberus_application_object_get";

    private final IApplicationObjectService applicationObjectService;
    private final ApplicationObjectMapperV001 mapper;
    private final MCPLogUtils mcpLogUtils;

    public GetApplicationObjectTool(IApplicationObjectService applicationObjectService,
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
     * Builds the MCP {@link McpSchema.Tool} descriptor for {@code cerberus_application_object_get}.
     *
     * <p>Declares {@code application} and {@code object} as required parameters.
     * Both together form the composite business key for an application object.</p>
     *
     * @return the fully configured tool descriptor
     */
    private McpSchema.Tool createTool() {
        Map<String, Object> properties = Map.of(
                "application", Map.of(
                        "type", "string",
                        "description", "Name of the application the object belongs to."
                ),
                "object", Map.of(
                        "type", "string",
                        "description", "Exact name of the application object to retrieve."
                )
        );

        return new McpSchema.Tool(
                TOOL_NAME,
                null,
                """
                Retrieves the full details of a specific application object by its name.

                Call this tool whenever the user asks to inspect or display a specific application object by name.

                Use cerberus_application_object_list instead when the object name is unknown or to browse all objects.
                Do not call this tool when the user asks to create, update, or delete an object.
                """,
                new McpSchema.JsonSchema(
                        "object",
                        properties,
                        List.of("application", "object"),
                        null,
                        null,
                        null
                ),
                null,
                MCPToolUtils.readOnlyAnnotations("Get application object", true),
                null
        );
    }

    /**
     * Validates the arguments, calls the service, and returns the application object DTO.
     *
     * @param args raw MCP arguments map from the client request
     * @return a success JSON result containing the object DTO, or an error text result
     */
    private McpSchema.CallToolResult execute(Map<String, Object> args) {
        String application = MCPToolUtils.getString(args, "application", "");
        String object = MCPToolUtils.getString(args, "object", "");

        mcpLogUtils.call(TOOL_NAME, "application_object_get",
                String.format("MCP tool %s called with application=%s object=%s", TOOL_NAME, application, object));

        if (application.isBlank()) return MCPToolUtils.errorText("Missing required parameter: application");
        if (object.isBlank()) return MCPToolUtils.errorText("Missing required parameter: object");

        AnswerItem<ApplicationObject> answer = applicationObjectService.readByKey(application, object);

        if (!answer.isCodeStringEquals("OK") || answer.getItem() == null) {
            return MCPToolUtils.errorText("Application object does not exist: application=" + application + " object=" + object);
        }

        return MCPToolUtils.successJson(mapper.toDTO(answer.getItem()));
    }
}
