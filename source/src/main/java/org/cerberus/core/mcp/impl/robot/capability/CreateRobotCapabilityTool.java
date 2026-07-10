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
import org.cerberus.core.crud.factory.IFactoryRobotCapability;
import org.cerberus.core.crud.service.IRobotCapabilityService;
import org.cerberus.core.mcp.MCPTool;
import org.cerberus.core.mcp.util.MCPLogUtils;
import org.cerberus.core.mcp.util.MCPToolUtils;
import org.cerberus.core.util.answer.Answer;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

/**
 * MCP tool that creates a new {@link RobotCapability} for a robot.
 *
 * <p>Exposed MCP tool name: {@code cerberus_robot_capability_create}.</p>
 *
 * <p>Before persisting, verifies that no capability with the same (robot, capability) key
 * already exists to prevent duplicate key errors at the database level.</p>
 *
 * <p>Delegates all persistence operations to {@link IRobotCapabilityService}.</p>
 */
@Component
public class CreateRobotCapabilityTool implements MCPTool {

    private static final String TOOL_NAME = "cerberus_robot_capability_create";

    private final IRobotCapabilityService robotCapabilityService;
    private final IFactoryRobotCapability factoryRobotCapability;
    private final RobotCapabilityMapperV001 mapper;
    private final MCPLogUtils mcpLogUtils;

    public CreateRobotCapabilityTool(IRobotCapabilityService robotCapabilityService,
                                     IFactoryRobotCapability factoryRobotCapability,
                                     RobotCapabilityMapperV001 mapper,
                                     MCPLogUtils mcpLogUtils) {
        this.robotCapabilityService = robotCapabilityService;
        this.factoryRobotCapability = factoryRobotCapability;
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
     * Builds the MCP {@link McpSchema.Tool} descriptor for {@code cerberus_robot_capability_create}.
     *
     * <p>Declares {@code robot}, {@code capability}, and {@code value} as required parameters.</p>
     *
     * @return the fully configured tool descriptor
     */
    private McpSchema.Tool createTool() {
        Map<String, Object> properties = Map.of(
                "robot", Map.of(
                        "type", "string",
                        "description", "Name of the robot this capability belongs to. Must already exist (see cerberus_robot_get / cerberus_robot_create)."
                ),
                "capability", Map.of(
                        "type", "string",
                        "description", "WebDriver capability key (e.g. 'browserName')."
                ),
                "value", Map.of(
                        "type", "string",
                        "description", "Value for this capability (e.g. 'chrome')."
                )
        );

        return new McpSchema.Tool(
                TOOL_NAME,
                null,
                """
                Adds a new WebDriver capability (key/value pair) to a robot.

                Call this tool whenever the user asks to add or configure a capability on a robot.
                Requires the robot name, the capability key, and its value.

                Do not call this tool when the user only asks to list, read, update, or delete capabilities.
                """,
                new McpSchema.JsonSchema(
                        "object",
                        properties,
                        List.of("robot", "capability", "value"),
                        null,
                        null,
                        null
                ),
                null,
                MCPToolUtils.createAnnotations("Create robot capability", false),
                null
        );
    }

    /**
     * Validates the arguments, checks for duplicates, builds the entity via
     * {@link IFactoryRobotCapability}, and delegates creation to
     * {@link IRobotCapabilityService#create(RobotCapability)}.
     *
     * @param args raw MCP arguments map from the client request
     * @return a success JSON result containing the created capability DTO, or an error text result
     */
    private McpSchema.CallToolResult execute(Map<String, Object> args) {
        String robot = MCPToolUtils.getString(args, "robot", "");
        String capability = MCPToolUtils.getString(args, "capability", "");
        String value = MCPToolUtils.getString(args, "value", "");

        mcpLogUtils.call(TOOL_NAME, "robot_capability_create",
                String.format("MCP tool %s called with robot=%s capability=%s", TOOL_NAME, robot, capability));

        if (robot.isBlank()) return MCPToolUtils.errorText("Missing required parameter: robot");
        if (capability.isBlank()) return MCPToolUtils.errorText("Missing required parameter: capability");
        if (value.isBlank()) return MCPToolUtils.errorText("Missing required parameter: value");

        // Guard against duplicate (robot, capability) key before hitting the DB unique constraint.
        boolean alreadyExists = robotCapabilityService.readByRobot(robot).getDataList().stream()
                .anyMatch(c -> capability.equals(c.getCapability()));
        if (alreadyExists) {
            return MCPToolUtils.errorText("Robot capability already exists: robot=" + robot + " capability=" + capability);
        }

        RobotCapability entity = factoryRobotCapability.create(0, robot, capability, value);

        Answer answer = robotCapabilityService.create(entity);

        if (!answer.isCodeStringEquals("OK")) {
            return MCPToolUtils.errorText("Unable to create robot capability: " + answer.getMessageDescription());
        }

        return MCPToolUtils.successJson(Map.of(
                "status", "created",
                "entry", mapper.toDTO(entity)
        ));
    }

}