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
import org.cerberus.core.api.dto.application.ApplicationObjectDTOV001;
import org.cerberus.core.api.dto.application.ApplicationObjectMapperV001;
import org.cerberus.core.crud.entity.ApplicationObject;
import org.cerberus.core.crud.service.IApplicationObjectService;
import org.cerberus.core.mcp.MCPTool;
import org.cerberus.core.mcp.util.MCPLogUtils;
import org.cerberus.core.mcp.util.MCPToolUtils;
import org.cerberus.core.util.answer.Answer;
import org.cerberus.core.util.answer.AnswerItem;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

/**
 * MCP tool that deletes an existing {@link ApplicationObject} entity from Cerberus.
 *
 * <p>Exposed MCP tool name: {@code cerberus_application_object_delete}.</p>
 *
 * <p>Delegates to {@link IApplicationObjectService} for both the existence check
 * ({@code readByKey}) and the deletion ({@code delete}). Both methods return an
 * {@link org.cerberus.core.util.answer.Answer} rather than throwing a checked exception.</p>
 *
 * <p>A DTO snapshot is captured before deletion so the confirmation response can include
 * the deleted object's data even though the entity is gone after the service call.</p>
 */
@Component
public class DeleteApplicationObjectTool implements MCPTool {

    private static final String TOOL_NAME = "cerberus_application_object_delete";

    private final IApplicationObjectService applicationObjectService;
    private final ApplicationObjectMapperV001 mapper;
    private final MCPLogUtils mcpLogUtils;

    public DeleteApplicationObjectTool(IApplicationObjectService applicationObjectService,
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
     * Builds the MCP {@link McpSchema.Tool} descriptor for {@code cerberus_application_object_delete}.
     *
     * <p>Declares {@code application} and {@code object} as required parameters.
     * Annotated with delete semantics so MCP clients can surface a confirmation prompt.</p>
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
                        "description", "Exact name of the application object to delete."
                )
        );

        return new McpSchema.Tool(
                TOOL_NAME,
                null,
                """
                Deletes an application object (locator) from an application.

                Call this tool whenever the user asks to remove or delete an application object.
                Before calling this tool, confirm the object name with the user.

                Use cerberus_application_object_list to find the exact object name before deleting.

                Do not call this tool when the user only asks to list, read, create, or update objects.
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
                MCPToolUtils.deleteAnnotations("Delete application object", false),
                null
        );
    }

    /**
     * Validates the arguments, checks the object exists, captures a DTO snapshot,
     * deletes it, and returns a JSON confirmation payload.
     *
     * @param args raw MCP arguments map from the client request
     * @return a success JSON result containing the deleted object DTO, or an error text result
     */
    private McpSchema.CallToolResult execute(Map<String, Object> args) {
        String application = MCPToolUtils.getString(args, "application", "");
        String object = MCPToolUtils.getString(args, "object", "");

        mcpLogUtils.call(TOOL_NAME, "application_object_delete",
                String.format("MCP tool %s called with application=%s object=%s", TOOL_NAME, application, object));

        if (application.isBlank()) return MCPToolUtils.errorText("Missing required parameter: application");
        if (object.isBlank()) return MCPToolUtils.errorText("Missing required parameter: object");

        AnswerItem<ApplicationObject> readAnswer = applicationObjectService.readByKey(application, object);

        if (!readAnswer.isCodeStringEquals("OK") || readAnswer.getItem() == null) {
            return MCPToolUtils.errorText("Application object does not exist: application=" + application + " object=" + object);
        }

        ApplicationObject appObject = readAnswer.getItem();
        // Snapshot the DTO before deletion — the entity is gone after the service call.
        ApplicationObjectDTOV001 dto = mapper.toDTO(appObject);

        Answer deleteAnswer = applicationObjectService.delete(appObject);

        if (!deleteAnswer.isCodeStringEquals("OK")) {
            return MCPToolUtils.errorText("Unable to delete application object: " + deleteAnswer.getMessageDescription());
        }

        return MCPToolUtils.successJson(Map.of(
                "status", "deleted",
                "object", dto
        ));
    }
}
