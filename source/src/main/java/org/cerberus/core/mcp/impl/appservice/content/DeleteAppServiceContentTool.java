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
import org.cerberus.core.util.answer.Answer;
import org.cerberus.core.util.answer.AnswerItem;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

/**
 * MCP tool that deletes a specific content parameter from an {@link AppServiceContent} entity.
 *
 * <p>Exposed MCP tool name: {@code cerberus_appservice_content_delete}.</p>
 *
 * <p>Delegates to {@link IAppServiceContentService} for both the existence check
 * ({@code readByKey}) and the actual deletion ({@code delete}). The deletion
 * service method returns an {@link Answer} rather than throwing a checked exception,
 * so success is detected via the answer's message code.</p>
 *
 * <p>A DTO snapshot is captured before deletion so the confirmation response can
 * include the deleted content parameter's data even though the entity is gone after
 * the {@link IAppServiceContentService#delete(AppServiceContent)} call.</p>
 */
@Component
public class DeleteAppServiceContentTool implements MCPTool {

    private static final String TOOL_NAME = "cerberus_appservice_content_delete";

    private final IAppServiceContentService appServiceContentService;
    private final AppServiceContentMapperV001 mapper;
    private final MCPLogUtils mcpLogUtils;

    public DeleteAppServiceContentTool(IAppServiceContentService appServiceContentService,
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

    /**
     * Builds the MCP {@link McpSchema.Tool} descriptor for {@code cerberus_appservice_content_delete}.
     *
     * <p>Declares {@code service} and {@code key} as required parameters. Annotated with delete
     * semantics ({@code MCPToolUtils.deleteAnnotations}) so MCP clients can surface an
     * appropriate confirmation prompt before the call is executed.</p>
     *
     * @return the fully configured tool descriptor
     */
    private McpSchema.Tool createTool() {
        Map<String, Object> properties = Map.of(
                "service", Map.of(
                        "type", "string",
                        "description", "Exact name of the app service owning the content parameter (the service field, primary key)."
                ),
                "key", Map.of(
                        "type", "string",
                        "description", "Exact key of the content parameter to delete (composite primary key with service)."
                )
        );

        return new McpSchema.Tool(
                TOOL_NAME,
                null,
                """
                Deletes a content parameter from an app service.
                Call this tool whenever the user asks to remove or delete a content parameter from a service.
                Before calling this tool, confirm the content key with the user.
                Use cerberus_appservice_content_list to find the exact key before deleting.
                """,
                new McpSchema.JsonSchema(
                        "object",
                        properties,
                        List.of("service", "key"),
                        null,
                        null,
                        null
                ),
                null,
                MCPToolUtils.deleteAnnotations("Delete app service content", false),
                null
        );
    }

    /**
     * Validates the arguments, checks that the content parameter exists, captures a DTO snapshot,
     * deletes it, and returns a JSON confirmation payload.
     *
     * @param args raw MCP arguments map from the client request
     * @return a success JSON result containing the deleted content DTO, or an error text result
     */
    private McpSchema.CallToolResult execute(Map<String, Object> args) {
        String service = MCPToolUtils.getString(args, "service", "");
        String key = MCPToolUtils.getString(args, "key", "");

        mcpLogUtils.call(TOOL_NAME, "appservice_content_delete",
                String.format("MCP tool %s called with service=%s, key=%s", TOOL_NAME, service, key));

        if (service.isBlank()) {
            return MCPToolUtils.errorText("Missing required parameter: service");
        }
        if (key.isBlank()) {
            return MCPToolUtils.errorText("Missing required parameter: key");
        }

        AnswerItem<AppServiceContent> readAnswer = appServiceContentService.readByKey(service, key);

        if (!readAnswer.isCodeStringEquals("OK") || readAnswer.getItem() == null) {
            return MCPToolUtils.errorText("App service content does not exist: service=" + service + ", key=" + key);
        }

        AppServiceContent content = readAnswer.getItem();
        // Snapshot the DTO before deletion — the entity is gone after the service call.
        AppServiceContentDTOV001 dto = mapper.toDTO(content);

        Answer deleteAnswer = appServiceContentService.delete(content);

        if (!deleteAnswer.isCodeStringEquals("OK")) {
            return MCPToolUtils.errorText("Unable to delete app service content " + key
                    + " from service " + service + ": " + deleteAnswer.getMessageDescription());
        }

        return MCPToolUtils.successJson(Map.of(
                "status", "deleted",
                "content", dto
        ));
    }
}
