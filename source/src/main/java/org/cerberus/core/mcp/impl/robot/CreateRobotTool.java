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
import org.cerberus.core.api.dto.robot.RobotMapperV001;
import org.cerberus.core.crud.entity.Robot;
import org.cerberus.core.crud.factory.IFactoryRobot;
import org.cerberus.core.crud.service.IRobotService;
import org.cerberus.core.exception.CerberusException;
import org.cerberus.core.mcp.MCPTool;
import org.cerberus.core.mcp.util.MCPLogUtils;
import org.cerberus.core.mcp.util.MCPToolUtils;
import org.cerberus.core.util.answer.Answer;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * MCP tool that creates a new {@link Robot} in Cerberus.
 *
 * <p>Exposed MCP tool name: {@code cerberus_robot_create}.</p>
 *
 * <p>Before persisting, verifies that no robot with the same name already exists to prevent
 * duplicate key errors at the database level.</p>
 *
 * <p>Builds the entity via {@link IFactoryRobot} and delegates persistence to
 * {@link IRobotService}. A freshly created robot has no capabilities or executors — use
 * {@code cerberus_robot_capability_create} and {@code cerberus_robot_executor_create} to add
 * them; a robot needs at least one active executor to be usable for test execution.</p>
 */
@Component
public class CreateRobotTool implements MCPTool {

    private static final String TOOL_NAME = "cerberus_robot_create";

    private final IRobotService robotService;
    private final IFactoryRobot factoryRobot;
    private final RobotMapperV001 mapper;
    private final MCPLogUtils mcpLogUtils;

    public CreateRobotTool(IRobotService robotService, IFactoryRobot factoryRobot, RobotMapperV001 mapper, MCPLogUtils mcpLogUtils) {
        this.robotService = robotService;
        this.factoryRobot = factoryRobot;
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
     * Builds the MCP {@link McpSchema.Tool} descriptor for {@code cerberus_robot_create}.
     *
     * <p>Declares {@code robot} and {@code platform} as required parameters, matching the
     * mandatory fields enforced by the existing Robot creation servlet.</p>
     *
     * @return the fully configured tool descriptor
     */
    private McpSchema.Tool createTool() {
        Map<String, Object> properties = new LinkedHashMap<>();
        properties.put("robot", Map.of(
                "type", "string",
                "description", "Unique name for the new robot (e.g. 'CHROME_LINUX')."
        ));
        properties.put("platform", Map.of(
                "type", "string",
                "description", "Platform the robot runs on (e.g. 'LINUX', 'WINDOWS', 'ANDROID', 'IOS')."
        ));
        properties.put("type", Map.of(
                "type", "string",
                "description", "Optional robot type (e.g. 'GUI', 'APK', 'IPA', 'SRV', 'BAT')."
        ));
        properties.put("browser", Map.of(
                "type", "string",
                "description", "Optional browser to use (e.g. 'chrome', 'firefox'). Leave empty for non-browser robots."
        ));
        properties.put("version", Map.of(
                "type", "string",
                "description", "Optional browser version."
        ));
        properties.put("active", Map.of(
                "type", "boolean",
                "description", "Whether this robot is active. Defaults to true."
        ));
        properties.put("userAgent", Map.of(
                "type", "string",
                "description", "Optional custom user agent string."
        ));
        properties.put("screenSize", Map.of(
                "type", "string",
                "description", "Optional screen resolution (e.g. '1920x1080')."
        ));
        properties.put("profileFolder", Map.of(
                "type", "string",
                "description", "Optional browser profile folder path."
        ));
        properties.put("extraParam", Map.of(
                "type", "string",
                "description", "Optional extra parameters (e.g. JSON of additional WebDriver options)."
        ));
        properties.put("acceptNotifications", Map.of(
                "type", "integer",
                "description", "Optional flag controlling browser notification handling. Defaults to 0."
        ));
        properties.put("acceptInsecureCerts", Map.of(
                "type", "boolean",
                "description", "Whether the robot should accept insecure/self-signed certificates. Defaults to false."
        ));
        properties.put("robotDecli", Map.of(
                "type", "string",
                "description", "Optional robot declination name, used to group similar robots."
        ));
        properties.put("lbexemethod", Map.of(
                "type", "string",
                "description", "Load-balancing method across this robot's executors. Defaults to 'ROUNDROBIN'.",
                "enum", List.of(Robot.LOADBALANCINGEXECUTORMETHOD_ROUNDROBIN, Robot.LOADBALANCINGEXECUTORMETHOD_BYRANKING)
        ));
        properties.put("description", Map.of(
                "type", "string",
                "description", "Optional human-readable description of the robot."
        ));
        properties.put("preloadScript", Map.of(
                "type", "string",
                "description", "Optional script to run before test execution starts on this robot."
        ));

        return new McpSchema.Tool(
                TOOL_NAME,
                null,
                """
                Creates a new robot (execution agent profile) in Cerberus.

                Call this tool whenever the user asks to create or add a new robot/execution agent.
                Requires a robot name and a platform.

                A freshly created robot has no capabilities or executors. Use
                cerberus_robot_capability_create to add WebDriver capabilities, and
                cerberus_robot_executor_create to add at least one host able to run this robot —
                without an active executor, the robot cannot be used for test execution.

                Do not call this tool when the user only asks to list, read, update, or delete robots.
                """,
                new McpSchema.JsonSchema(
                        "object",
                        properties,
                        List.of("robot", "platform"),
                        null,
                        null,
                        null
                ),
                null,
                MCPToolUtils.createAnnotations("Create robot", false),
                null
        );
    }

    /**
     * Validates the arguments, checks for duplicates, builds the entity via {@link IFactoryRobot},
     * and delegates creation to {@link IRobotService#create(Robot)}.
     *
     * @param args raw MCP arguments map from the client request
     * @return a success JSON result containing the created robot DTO, or an error text result
     */
    private McpSchema.CallToolResult execute(Map<String, Object> args) {
        String robot = MCPToolUtils.getString(args, "robot", "");
        String platform = MCPToolUtils.getString(args, "platform", "");

        mcpLogUtils.call(TOOL_NAME, "robot_create",
                String.format("MCP tool %s called with robot=%s platform=%s", TOOL_NAME, robot, platform));

        if (robot.isBlank()) return MCPToolUtils.errorText("Missing required parameter: robot");
        if (platform.isBlank()) return MCPToolUtils.errorText("Missing required parameter: platform");

        // Guard against duplicate robot names before hitting the DB unique constraint.
        Robot existing;
        try {
            existing = robotService.readByKey(robot);
        } catch (CerberusException e) {
            return MCPToolUtils.errorText("Unable to check existing robot " + robot + ": " + e.getMessage());
        }
        if (existing != null) {
            return MCPToolUtils.errorText("Robot already exists: " + robot);
        }

        Robot entity = factoryRobot.create(
                0,
                robot,
                platform,
                MCPToolUtils.getString(args, "browser", ""),
                MCPToolUtils.getString(args, "version", ""),
                MCPToolUtils.getBoolean(args, "active", true),
                MCPToolUtils.getString(args, "lbexemethod", Robot.LOADBALANCINGEXECUTORMETHOD_ROUNDROBIN),
                MCPToolUtils.getString(args, "description", ""),
                MCPToolUtils.getString(args, "userAgent", ""),
                MCPToolUtils.getString(args, "screenSize", ""),
                MCPToolUtils.getString(args, "profileFolder", ""),
                MCPToolUtils.getInteger(args, "acceptNotifications", 0),
                MCPToolUtils.getString(args, "extraParam", ""),
                MCPToolUtils.getBoolean(args, "acceptInsecureCerts", false),
                MCPToolUtils.getString(args, "robotDecli", ""),
                MCPToolUtils.getString(args, "type", ""),
                MCPToolUtils.getString(args, "preloadScript", "")
        );
        // Tag the creator so audit trails distinguish MCP-driven creation from UI creation.
        entity.setUsrCreated("MCP");

        Answer answer = robotService.create(entity);

        if (!answer.isCodeStringEquals("OK")) {
            return MCPToolUtils.errorText("Unable to create robot " + robot + ": " + answer.getMessageDescription());
        }

        return MCPToolUtils.successJson(Map.of(
                "status", "created",
                "entry", mapper.toDTO(entity)
        ));
    }

}