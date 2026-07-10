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
package org.cerberus.core.mcp.impl.robot;

import io.modelcontextprotocol.server.McpServerFeatures;
import io.modelcontextprotocol.spec.McpSchema;
import org.cerberus.core.api.dto.robot.RobotDTOV001;
import org.cerberus.core.api.dto.robot.RobotMapperV001;
import org.cerberus.core.crud.entity.Robot;
import org.cerberus.core.crud.service.IRobotService;
import org.cerberus.core.exception.CerberusException;
import org.cerberus.core.mcp.MCPTool;
import org.cerberus.core.mcp.util.MCPLogUtils;
import org.cerberus.core.mcp.util.MCPToolUtils;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

/**
 * MCP tool that retrieves a single {@link Robot} by its name.
 *
 * <p>Exposed MCP tool name: {@code cerberus_robot_get}.</p>
 *
 * <p>Delegates to {@link IRobotService#readByKey(String)}, which also loads the robot's
 * capabilities and executors, and converts the result via {@link RobotMapperV001}.</p>
 */
@Component
public class GetRobotTool implements MCPTool {

    private static final String TOOL_NAME = "cerberus_robot_get";

    private final IRobotService robotService;
    private final RobotMapperV001 mapper;
    private final MCPLogUtils mcpLogUtils;

    public GetRobotTool(IRobotService robotService, RobotMapperV001 mapper, MCPLogUtils mcpLogUtils) {
        this.robotService = robotService;
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
     * Builds the MCP {@link McpSchema.Tool} descriptor for {@code cerberus_robot_get}.
     *
     * <p>Declares {@code robot} as the only required parameter.</p>
     *
     * @return the fully configured tool descriptor
     */
    private McpSchema.Tool createTool() {
        Map<String, Object> properties = Map.of(
                "robot", Map.of(
                        "type", "string",
                        "description", "Exact name of the robot to retrieve."
                )
        );

        return new McpSchema.Tool(
                TOOL_NAME,
                null,
                """
                Retrieves the full details of a specific robot (execution agent) by name,
                including its capabilities and executors.

                Call this tool whenever the user asks to inspect or display a specific robot.

                Use cerberus_robot_list instead when the robot name is unknown or to browse all robots.
                Do not call this tool when the user asks to create, update, or delete a robot.
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
                MCPToolUtils.readOnlyAnnotations("Get robot", true),
                null
        );
    }

    /**
     * Validates the arguments, calls the service, and returns the robot DTO.
     *
     * @param args raw MCP arguments map from the client request
     * @return a success JSON result containing the robot DTO, or an error text result
     */
    private McpSchema.CallToolResult execute(Map<String, Object> args) {
        String robot = MCPToolUtils.getString(args, "robot", "");

        mcpLogUtils.call(TOOL_NAME, "robot_get",
                String.format("MCP tool %s called with robot=%s", TOOL_NAME, robot));

        if (robot.isBlank()) return MCPToolUtils.errorText("Missing required parameter: robot");

        Robot entity;
        try {
            entity = robotService.readByKey(robot);
        } catch (CerberusException e) {
            return MCPToolUtils.errorText("Unable to retrieve robot " + robot + ": " + e.getMessage());
        }

        if (entity == null) {
            return MCPToolUtils.errorText("Robot does not exist: " + robot);
        }

        RobotDTOV001 dto = mapper.toDTO(entity);
        return MCPToolUtils.successJson(dto);
    }

}