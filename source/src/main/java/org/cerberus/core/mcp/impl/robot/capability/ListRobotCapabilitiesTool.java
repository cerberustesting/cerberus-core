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
package org.cerberus.core.mcp.impl.robot.capability;

import io.modelcontextprotocol.server.McpServerFeatures;
import io.modelcontextprotocol.spec.McpSchema;
import org.cerberus.core.api.dto.robot.RobotCapabilityMapperV001;
import org.cerberus.core.crud.entity.RobotCapability;
import org.cerberus.core.crud.service.IRobotCapabilityService;
import org.cerberus.core.mcp.MCPTool;
import org.cerberus.core.mcp.util.MCPLogUtils;
import org.cerberus.core.mcp.util.MCPToolUtils;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

/**
 * MCP tool that lists {@link RobotCapability} entries (WebDriver capabilities) for a given robot.
 *
 * <p>Exposed MCP tool name: {@code cerberus_robot_capability_list}.</p>
 *
 * <p>A robot capability is a key/value WebDriver capability (e.g. {@code browserName}=
 * {@code chrome}) attached to a robot.</p>
 *
 * <p>Delegates to {@link IRobotCapabilityService#readByRobot(String)} and converts results
 * via {@link RobotCapabilityMapperV001}.</p>
 */
@Component
public class ListRobotCapabilitiesTool implements MCPTool {

    private static final String TOOL_NAME = "cerberus_robot_capability_list";

    private final IRobotCapabilityService robotCapabilityService;
    private final RobotCapabilityMapperV001 mapper;
    private final MCPLogUtils mcpLogUtils;

    public ListRobotCapabilitiesTool(IRobotCapabilityService robotCapabilityService,
                                     RobotCapabilityMapperV001 mapper,
                                     MCPLogUtils mcpLogUtils) {
        this.robotCapabilityService = robotCapabilityService;
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
     * Builds the MCP {@link McpSchema.Tool} descriptor for {@code cerberus_robot_capability_list}.
     *
     * <p>Declares {@code robot} as the only required parameter.
     * An optional {@code search} parameter allows filtering by capability key or value.</p>
     *
     * @return the fully configured tool descriptor
     */
    private McpSchema.Tool createTool() {
        Map<String, Object> properties = Map.of(
                "robot", Map.of(
                        "type", "string",
                        "description", "Name of the robot whose capabilities to list."
                ),
                "search", Map.of(
                        "type", "string",
                        "description", "Optional text filter applied on capability key or value (case-insensitive)."
                )
        );

        return new McpSchema.Tool(
                TOOL_NAME,
                null,
                """
                Lists the WebDriver capabilities (key/value pairs) configured for a given robot.

                Call this tool whenever the user asks to list, browse, or search a robot's capabilities.

                Use cerberus_robot_capability_get to retrieve a specific capability by key.
                Do not call this tool when the user asks to create, update, or delete a capability.
                """,
                new McpSchema.JsonSchema(
                        "object",
                        properties,
                        List.of("robot"),
                        null,
                        null,
                        null
                ),
                null,
                MCPToolUtils.readOnlyAnnotations("List robot capabilities", false),
                null
        );
    }

    /**
     * Loads all capabilities for the given robot, applies an optional search filter, maps
     * them to DTOs, and returns a JSON list.
     *
     * @param args raw MCP arguments map from the client request
     * @return a success JSON result containing the capability list, or an error text result
     */
    private McpSchema.CallToolResult execute(Map<String, Object> args) {
        String robot = MCPToolUtils.getString(args, "robot", "");
        String search = MCPToolUtils.getString(args, "search", "");

        mcpLogUtils.call(TOOL_NAME, "robot_capability_list",
                String.format("MCP tool %s called with robot=%s search=%s", TOOL_NAME, robot, search));

        if (robot.isBlank()) {
            return MCPToolUtils.errorText("Missing required parameter: robot");
        }

        List<RobotCapability> raw = robotCapabilityService.readByRobot(robot).getDataList();

        List<Object> capabilities = raw.stream()
                .filter(c -> matchesSearch(c, search))
                .map(mapper::toDTO)
                .map(Object.class::cast)
                .toList();

        return MCPToolUtils.successJson(Map.of(
                "robot", robot,
                "count", capabilities.size(),
                "capabilities", capabilities
        ));
    }

    /**
     * Returns {@code true} when the capability key or value contains the search term
     * (case-insensitive), or when no search term was provided.
     *
     * @param capability the capability to test
     * @param search     the search string; blank means "match all"
     * @return whether this capability passes the search filter
     */
    private boolean matchesSearch(RobotCapability capability, String search) {
        if (search.isBlank()) return true;
        return MCPToolUtils.containsIgnoreCase(capability.getCapability(), search)
                || MCPToolUtils.containsIgnoreCase(capability.getValue(), search);
    }

}