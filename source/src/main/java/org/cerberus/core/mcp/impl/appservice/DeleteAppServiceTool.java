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
import org.cerberus.core.api.dto.appservice.AppServiceDTOV001;
import org.cerberus.core.api.dto.appservice.AppServiceMapperV001;
import org.cerberus.core.crud.entity.AppService;
import org.cerberus.core.crud.service.IAppServiceService;
import org.cerberus.core.mcp.MCPTool;
import org.cerberus.core.mcp.util.MCPLogUtils;
import org.cerberus.core.mcp.util.MCPToolUtils;
import org.cerberus.core.util.answer.Answer;
import org.cerberus.core.util.answer.AnswerItem;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

/**
 * MCP tool that deletes an existing {@link AppService} entity from Cerberus.
 *
 * <p>Exposed MCP tool name: {@code cerberus_appservice_delete}.</p>
 *
 * <p>Delegates to {@link IAppServiceService} for both the existence check
 * ({@code readByKey}) and the actual deletion ({@code delete}). The deletion
 * service method returns an {@link Answer} rather than throwing a checked exception,
 * so success is detected via the answer's message code.</p>
 *
 * <p>A DTO snapshot is captured before deletion so the confirmation response can
 * include the deleted service's data even though the entity is gone after the
 * {@link IAppServiceService#delete(AppService)} call.</p>
 */
@Component
public class DeleteAppServiceTool implements MCPTool {

    private static final String TOOL_NAME = "cerberus_appservice_delete";

    private final IAppServiceService appServiceService;
    private final AppServiceMapperV001 mapper;
    private final MCPLogUtils mcpLogUtils;

    public DeleteAppServiceTool(IAppServiceService appServiceService, AppServiceMapperV001 mapper, MCPLogUtils mcpLogUtils) {
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
     * Builds the MCP {@link McpSchema.Tool} descriptor for {@code cerberus_appservice_delete}.
     *
     * <p>Declares {@code service} as the only required parameter. Annotated with delete
     * semantics ({@code MCPToolUtils.deleteAnnotations}) so MCP clients can surface an
     * appropriate confirmation prompt before the call is executed.</p>
     *
     * @return the fully configured tool descriptor
     */
    private McpSchema.Tool createTool() {
        Map<String, Object> properties = Map.of(
                "service", Map.of(
                        "type", "string",
                        "description", "Exact name of the app service to delete (the service field, primary key)."
                )
        );

        return new McpSchema.Tool(
                TOOL_NAME,
                null,
                """
                Deletes an existing app service from Cerberus.
                Call this tool whenever the user asks to remove or delete a service.
                Before calling this tool, confirm the service name with the user.
                Use cerberus_appservice_list to find the exact service name before deleting.
                Do not call this tool when the user only asks to list, read, create, or update a service.
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
                MCPToolUtils.deleteAnnotations("Delete app service", false),
                null
        );
    }

    /**
     * Validates the argument, checks that the app service exists, captures a DTO snapshot,
     * deletes it, and returns a JSON confirmation payload.
     *
     * @param args raw MCP arguments map from the client request
     * @return a success JSON result containing the deleted service DTO, or an error text result
     */
    private McpSchema.CallToolResult execute(Map<String, Object> args) {
        String service = MCPToolUtils.getString(args, "service", "");

        mcpLogUtils.call(TOOL_NAME, "appservice_delete",
                String.format("MCP tool %s called with service=%s", TOOL_NAME, service));

        if (service.isBlank()) {
            return MCPToolUtils.errorText("Missing required parameter: service");
        }

        AnswerItem<AppService> readAnswer = appServiceService.readByKey(service);

        if (!readAnswer.isCodeStringEquals("OK") || readAnswer.getItem() == null) {
            return MCPToolUtils.errorText("App service does not exist: " + service);
        }

        AppService appService = readAnswer.getItem();
        // Snapshot the DTO before deletion — the entity is gone after the service call.
        AppServiceDTOV001 dto = mapper.toDTO(appService);

        Answer deleteAnswer = appServiceService.delete(appService);

        if (!deleteAnswer.isCodeStringEquals("OK")) {
            return MCPToolUtils.errorText("Unable to delete app service " + service + ": " + deleteAnswer.getMessageDescription());
        }

        return MCPToolUtils.successJson(Map.of(
                "status", "deleted",
                "service", dto
        ));
    }
}
