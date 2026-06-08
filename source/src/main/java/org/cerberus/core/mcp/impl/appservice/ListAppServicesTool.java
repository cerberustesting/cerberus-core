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
import org.cerberus.core.util.answer.AnswerList;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

/**
 * MCP tool that exposes Cerberus {@link AppService} lookup under the tool name {@code cerberus_appservice_list}.
 *
 * <p>Allows AI agents to browse or search the app services defined in Cerberus.
 * Delegates to {@link IAppServiceService#readByLikeName(String, int)} with an optional
 * search filter and maps each result to {@link AppServiceDTOV001} via {@link AppServiceMapperV001}
 * before returning a JSON response to the MCP client.</p>
 *
 * <p>Use {@code cerberus_appservice_get} to retrieve the full details (including headers and
 * contents) of a specific service once its name is known.</p>
 */
@Component
public class ListAppServicesTool implements MCPTool {

    private static final String TOOL_NAME = "cerberus_appservice_list";

    private final IAppServiceService appServiceService;
    private final AppServiceMapperV001 mapper;
    private final MCPLogUtils mcpLogUtils;

    public ListAppServicesTool(IAppServiceService appServiceService,
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
     * Builds the MCP {@link McpSchema.Tool} descriptor for this tool.
     *
     * <p>Declares the optional {@code search} parameter so MCP clients can pass
     * a name filter directly to {@link IAppServiceService#readByLikeName(String, int)}.
     * Passing an empty or absent search string returns all services (up to 100 results).</p>
     *
     * @return a fully configured {@link McpSchema.Tool} ready for MCP registration
     */
    private McpSchema.Tool createTool() {
        Map<String, Object> properties = Map.of(
                "search", Map.of(
                        "type", "string",
                        "description", "Optional filter applied to the service name. Leave blank to list all services."
                )
        );

        return new McpSchema.Tool(
                TOOL_NAME,
                null,
                """
                Lists app services defined in Cerberus.
                Use this tool when the user asks to browse or find available services.
                Use the search parameter to filter by service name.
                Use cerberus_appservice_get to retrieve the full details including headers and contents of a specific service.
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
                MCPToolUtils.readOnlyAnnotations("List app services", false),
                null
        );
    }

    /**
     * Executes the list operation: queries the service layer with the optional search filter,
     * maps each {@link AppService} entity to a DTO, and returns the result as a JSON response.
     *
     * <p>When {@code search} is blank, an empty string is forwarded to
     * {@link IAppServiceService#readByLikeName(String, int)} which returns all services
     * (capped at 100 results). When non-blank, the service performs a name-based
     * {@code LIKE} search in the database before the results are returned.</p>
     *
     * @param args the raw MCP tool arguments supplied by the client (may be empty, never null)
     * @return a {@link McpSchema.CallToolResult} containing the serialised service list,
     *         or an error result if the service call fails
     */
    private McpSchema.CallToolResult execute(Map<String, Object> args) {
        String search = MCPToolUtils.getString(args, "search", "");

        mcpLogUtils.call(TOOL_NAME, "appservice_list",
                String.format("MCP tool %s called with search=%s", TOOL_NAME, search));

        AnswerList<AppService> answerList = appServiceService.readByLikeName(search, 100);

        List<AppServiceDTOV001> services = ((AnswerList) answerList).getDataList()
                .stream()
                .filter(AppService.class::isInstance)
                .map(AppService.class::cast)
                .map(mapper::toDTO)
                .toList();

        return MCPToolUtils.successJson(Map.of(
                "count", services.size(),
                "services", services
        ));
    }
}
