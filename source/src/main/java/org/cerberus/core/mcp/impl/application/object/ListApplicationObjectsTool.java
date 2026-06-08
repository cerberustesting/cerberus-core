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
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

/**
 * MCP tool that lists all {@link ApplicationObject} entities for a given application.
 *
 * <p>Exposed MCP tool name: {@code cerberus_application_object_list}.</p>
 *
 * <p>Application objects are reusable element descriptors (locators such as XPath or CSS selectors)
 * attached to an application. They can be referenced by name in testcase steps and actions
 * to avoid hard-coding locator strings.</p>
 *
 * <p>Delegates to {@link IApplicationObjectService#readByApplication(String)} and
 * converts results via {@link ApplicationObjectMapperV001}.</p>
 */
@Component
public class ListApplicationObjectsTool implements MCPTool {

    private static final String TOOL_NAME = "cerberus_application_object_list";

    private final IApplicationObjectService applicationObjectService;
    private final ApplicationObjectMapperV001 mapper;
    private final MCPLogUtils mcpLogUtils;

    public ListApplicationObjectsTool(IApplicationObjectService applicationObjectService,
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
     * Builds the MCP {@link McpSchema.Tool} descriptor for {@code cerberus_application_object_list}.
     *
     * <p>Declares {@code application} as the only required parameter.
     * An optional {@code search} parameter allows filtering by object name or value.</p>
     *
     * @return the fully configured tool descriptor
     */
    private McpSchema.Tool createTool() {
        Map<String, Object> properties = Map.of(
                "application", Map.of(
                        "type", "string",
                        "description", "Name of the application whose objects to list."
                ),
                "search", Map.of(
                        "type", "string",
                        "description", "Optional text filter applied on object name or value (case-insensitive)."
                )
        );

        return new McpSchema.Tool(
                TOOL_NAME,
                null,
                """
                Lists all application objects (locators) defined for a given application.

                Application objects are reusable element descriptors (XPath, CSS selectors, etc.)
                that can be referenced by name in testcase steps and actions.

                Call this tool whenever the user asks to list, browse, or search application objects for an application.

                Use cerberus_application_object_get to retrieve the full details of a specific object by name.
                Do not call this tool when the user asks to create, update, or delete an object.
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
                MCPToolUtils.readOnlyAnnotations("List application objects", false),
                null
        );
    }

    /**
     * Loads all objects for the given application, applies an optional search filter,
     * maps them to DTOs, and returns a JSON list.
     *
     * @param args raw MCP arguments map from the client request
     * @return a success JSON result containing the object list, or an error text result
     */
    private McpSchema.CallToolResult execute(Map<String, Object> args) {
        String application = MCPToolUtils.getString(args, "application", "");
        String search = MCPToolUtils.getString(args, "search", "");

        mcpLogUtils.call(TOOL_NAME, "application_object_list",
                String.format("MCP tool %s called with application=%s search=%s", TOOL_NAME, application, search));

        if (application.isBlank()) {
            return MCPToolUtils.errorText("Missing required parameter: application");
        }

        List<?> raw = applicationObjectService.readByApplication(application).getDataList();

        List<Object> objects = raw.stream()
                .filter(ApplicationObject.class::isInstance)
                .map(ApplicationObject.class::cast)
                .filter(o -> matchesSearch(o, search))
                .map(mapper::toDTO)
                .map(Object.class::cast)
                .toList();

        return MCPToolUtils.successJson(Map.of(
                "application", application,
                "count", objects.size(),
                "objects", objects
        ));
    }

    /**
     * Returns {@code true} when the object name or value contains the search term
     * (case-insensitive), or when no search term was provided.
     *
     * @param obj    the application object to test
     * @param search the search string; blank means "match all"
     * @return whether this object passes the search filter
     */
    private boolean matchesSearch(ApplicationObject obj, String search) {
        if (search.isBlank()) return true;
        return MCPToolUtils.containsIgnoreCase(obj.getObject(), search)
                || MCPToolUtils.containsIgnoreCase(obj.getValue(), search);
    }
}
