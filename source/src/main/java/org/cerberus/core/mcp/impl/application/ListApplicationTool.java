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
import org.cerberus.core.mcp.util.MCPLogUtils;
import org.cerberus.core.mcp.util.MCPProjectionUtils;
import org.cerberus.core.mcp.util.MCPToolUtils;
import org.cerberus.core.crud.entity.Application;
import org.cerberus.core.crud.service.IApplicationService;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

/**
 * MCP tool that lists Cerberus {@link Application} entities accessible to the current user.
 *
 * <p>Exposes the MCP tool named {@value TOOL_NAME}, which returns a filtered and projected
 * list of applications. Delegates data retrieval to {@link IApplicationService#readAll()} and
 * relies on {@link ApplicationMapperV001} to convert entities to serialisable DTOs.</p>
 *
 * <p>The tool accepts an optional {@code intent} hint so the AI agent can signal its usage
 * context (e.g. selecting an application, creating a test case) and receive only the fields
 * that are relevant to that context by default.</p>
 */
@Component
public class ListApplicationTool implements MCPTool {

    private static final String TOOL_NAME = "list_applications";

    /** Exhaustive set of DTO field names that can be returned to the caller. */
    private static final List<String> ALL_FIELDS = List.of("application", "description", "sort", "type", "system", "subsystem", "svnurl",
            "bugTrackerUrl", "bugTrackerNewUrl", "poolSize", "deploytype", "mavengroupid",
            "bugTrackerConnector", "bugTrackerParam1", "bugTrackerParam2", "bugTrackerParam3",
            "environments", "usrCreated", "dateCreated", "usrModif", "dateModif");

    private final IApplicationService applicationService;
    private final ApplicationMapperV001 applicationMapper;
    private final MCPLogUtils mcpLogUtils;

    public ListApplicationTool(IApplicationService applicationService, ApplicationMapperV001 applicationMapper, MCPLogUtils mcpLogUtils) {
        this.applicationService = applicationService;
        this.applicationMapper = applicationMapper;
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
     * Builds the MCP {@link McpSchema.Tool} descriptor for {@value TOOL_NAME}.
     *
     * <p>Declares three optional input parameters:
     * <ul>
     *   <li>{@code intent} — usage context enum that drives the default field projection when
     *       {@code fields} is not explicitly provided.</li>
     *   <li>{@code search} — free-text filter applied against application name and description.</li>
     *   <li>{@code fields} — explicit allow-list of DTO fields to include in each result entry;
     *       takes precedence over the intent-driven defaults when present.</li>
     * </ul>
     * </p>
     *
     * @return the fully-configured tool descriptor
     */
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
                                // Constrain field names to the known DTO surface so the agent cannot request arbitrary keys.
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

    /**
     * Executes the tool: loads all applications, applies the optional search filter,
     * maps entities to DTOs, projects the requested fields, and returns a JSON result.
     *
     * @param args raw MCP argument map from the agent request
     * @return a {@link McpSchema.CallToolResult} containing the serialised application list
     */
    private McpSchema.CallToolResult execute(Map<String, Object> args) {
        String intent = MCPToolUtils.getString(args, "intent", "");
        String search = MCPToolUtils.getString(args, "search", "");

        // Explicit fields override intent-driven defaults when provided by the caller.
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
                // Reduce each DTO to only the caller-requested (or intent-default) fields to minimise payload size.
                .map(dto -> MCPProjectionUtils.project(dto, fields))
                .toList();

        return MCPToolUtils.successJson(Map.of(
                "intent", intent,
                "count", applications.size(),
                "applications", applications
        ));
    }

    /**
     * Returns the default field projection for a given intent value.
     *
     * <p>Each intent maps to the minimal set of fields needed for that usage context,
     * keeping the response payload small and focused for the AI agent.</p>
     *
     * @param intent the intent string provided by the caller (may be empty)
     * @return an ordered list of DTO field names to include in the response
     */
    private List<String> defaultFieldsForIntent(String intent) {
        return switch (intent) {
            // Only application name and type are needed when the agent is about to create a test case.
            case "create_testcase" -> List.of("application","type");
            // Full metadata when the user wants to inspect a specific application in detail.
            case "inspect_application" -> ALL_FIELDS;
            case "select_application" -> List.of("application","description","type","system");
            // Unrecognised or missing intent: return only the application name as a safe minimum.
            default -> List.of("application");
        };
    }

    /**
     * Returns {@code true} when the application name or description contains the search term
     * (case-insensitive), or when no search term was provided.
     *
     * @param app    the application entity to test
     * @param search the search string; blank or null means "match all"
     * @return whether this application passes the search filter
     */
    private boolean matchesSearch(Application app, String search) {
        if (search == null || search.isBlank()) {
            return true;
        }

        return MCPToolUtils.containsIgnoreCase(app.getApplication(), search)
                || MCPToolUtils.containsIgnoreCase(app.getDescription(), search);
    }

}