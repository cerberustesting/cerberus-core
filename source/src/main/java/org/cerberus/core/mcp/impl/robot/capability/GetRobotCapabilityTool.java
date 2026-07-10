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
import java.util.Optional;

/**
 * MCP tool that retrieves a single {@link RobotCapability} by its composite key
 * (robot + capability key).
 *
 * <p>Exposed MCP tool name: {@code cerberus_robot_capability_get}.</p>
 *
 * <p>{@link IRobotCapabilityService} has no single-item lookup — this tool calls
 * {@link IRobotCapabilityService#readByRobot(String)} and filters in memory for the
 * requested capability key.</p>
 */
@Component
public class GetRobotCapabilityTool implements MCPTool {

    private static final String TOOL_NAME = "cerberus_robot_capability_get";

    private final IRobotCapabilityService robotCapabilityService;
    private final RobotCapabilityMapperV001 mapper;
    private final MCPLogUtils mcpLogUtils;

    public GetRobotCapabilityTool(IRobotCapabilityService robotCapabilityService,
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
     * Builds the MCP {@link McpSchema.Tool} descriptor for {@code cerberus_robot_capability_get}.
     *
     * <p>Declares {@code robot} and {@code capability} as required parameters — together they
     * form the composite business key for a capability.</p>
     *
     * @return the fully configured tool descriptor
     */
    private McpSchema.Tool createTool() {
        Map<String, Object> properties = Map.of(
                "robot", Map.of(
                        "type", "string",
                        "description", "Name of the robot the capability belongs to."
                ),
                "capability", Map.of(
                        "type", "string",
                        "description", "Exact capability key to retrieve (e.g. 'browserName')."
                )
        );

        return new McpSchema.Tool(
                TOOL_NAME,
                null,
                """
                Retrieves the value of a specific WebDriver capability by key for a given robot.

                Call this tool whenever the user asks to inspect a specific capability of a robot.

                Use cerberus_robot_capability_list instead when the capability key is unknown or to
                browse all capabilities of a robot.
                Do not call this tool when the user asks to create, update, or delete a capability.
                """,
                new McpSchema.JsonSchema(
                        "object",
                        properties,
                        List.of("robot", "capability"),
                        null,
                        null,
                        null
                ),
                null,
                MCPToolUtils.readOnlyAnnotations("Get robot capability", true),
                null
        );
    }

    /**
     * Validates the arguments, loads the robot's capabilities, finds the requested key, and
     * returns the matching capability as a DTO.
     *
     * @param args raw MCP arguments map from the client request
     * @return a success JSON result containing the capability DTO, or an error text result
     */
    private McpSchema.CallToolResult execute(Map<String, Object> args) {
        String robot = MCPToolUtils.getString(args, "robot", "");
        String capability = MCPToolUtils.getString(args, "capability", "");

        mcpLogUtils.call(TOOL_NAME, "robot_capability_get",
                String.format("MCP tool %s called with robot=%s capability=%s", TOOL_NAME, robot, capability));

        if (robot.isBlank()) return MCPToolUtils.errorText("Missing required parameter: robot");
        if (capability.isBlank()) return MCPToolUtils.errorText("Missing required parameter: capability");

        List<RobotCapability> raw = robotCapabilityService.readByRobot(robot).getDataList();

        Optional<RobotCapability> match = raw.stream()
                .filter(c -> capability.equals(c.getCapability()))
                .findFirst();

        if (match.isEmpty()) {
            return MCPToolUtils.errorText("Robot capability does not exist: robot=" + robot + " capability=" + capability);
        }

        return MCPToolUtils.successJson(mapper.toDTO(match.get()));
    }

}