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
 * MCP tool that updates an existing {@link Robot}.
 *
 * <p>Exposed MCP tool name: {@code cerberus_robot_update}.</p>
 *
 * <p>Applies a read-before-write pattern: the existing robot is loaded first so untouched
 * fields (and its capabilities/executors) retain their current values. Only the fields provided
 * in the {@code updates} map are modified. This tool does not touch capabilities or executors —
 * use the dedicated {@code cerberus_robot_capability_*} and {@code cerberus_robot_executor_*}
 * tools for those.</p>
 *
 * <p>Delegates persistence to {@link IRobotService#update(Robot, String)}.</p>
 */
@Component
public class UpdateRobotTool implements MCPTool {

    private static final String TOOL_NAME = "cerberus_robot_update";

    private final IRobotService robotService;
    private final RobotMapperV001 mapper;
    private final MCPLogUtils mcpLogUtils;

    public UpdateRobotTool(IRobotService robotService, RobotMapperV001 mapper, MCPLogUtils mcpLogUtils) {
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
     * Builds the MCP {@link McpSchema.Tool} descriptor for {@code cerberus_robot_update}.
     *
     * <p>The input schema uses a nested {@code updates} object with {@code additionalProperties: false}
     * so the AI model cannot send unrecognised fields.</p>
     *
     * @return the fully configured tool descriptor
     */
    private McpSchema.Tool createTool() {
        Map<String, Object> updateProperties = new LinkedHashMap<>();
        updateProperties.put("type", Map.of("type", "string", "description", "New robot type (e.g. 'GUI', 'APK', 'IPA', 'SRV', 'BAT')."));
        updateProperties.put("platform", Map.of("type", "string", "description", "New platform the robot runs on."));
        updateProperties.put("browser", Map.of("type", "string", "description", "New browser to use."));
        updateProperties.put("version", Map.of("type", "string", "description", "New browser version."));
        updateProperties.put("active", Map.of("type", "boolean", "description", "New active flag for this robot."));
        updateProperties.put("userAgent", Map.of("type", "string", "description", "New custom user agent string."));
        updateProperties.put("screenSize", Map.of("type", "string", "description", "New screen resolution."));
        updateProperties.put("profileFolder", Map.of("type", "string", "description", "New browser profile folder path."));
        updateProperties.put("extraParam", Map.of("type", "string", "description", "New extra parameters."));
        updateProperties.put("acceptNotifications", Map.of("type", "integer", "description", "New notification handling flag."));
        updateProperties.put("acceptInsecureCerts", Map.of("type", "boolean", "description", "New insecure-certificates flag."));
        updateProperties.put("robotDecli", Map.of("type", "string", "description", "New robot declination name."));
        updateProperties.put("lbexemethod", Map.of(
                "type", "string",
                "description", "New load-balancing method across this robot's executors.",
                "enum", List.of(Robot.LOADBALANCINGEXECUTORMETHOD_ROUNDROBIN, Robot.LOADBALANCINGEXECUTORMETHOD_BYRANKING)
        ));
        updateProperties.put("description", Map.of("type", "string", "description", "New human-readable description."));
        updateProperties.put("preloadScript", Map.of("type", "string", "description", "New pre-execution script."));

        Map<String, Object> updatesSchema = new LinkedHashMap<>();
        updatesSchema.put("type", "object");
        updatesSchema.put("description", "Fields to update. Only provide the fields that need to change.");
        updatesSchema.put("properties", updateProperties);
        // Reject unknown fields early so the switch in execute() never hits the default branch unexpectedly.
        updatesSchema.put("additionalProperties", false);

        Map<String, Object> properties = new LinkedHashMap<>();
        properties.put("robot", Map.of(
                "type", "string",
                "description", "Exact name of the robot to update."
        ));
        properties.put("updates", updatesSchema);

        return new McpSchema.Tool(
                TOOL_NAME,
                null,
                """
                Updates an existing robot (execution agent profile).

                Call this tool whenever the user asks to modify the platform, browser, activation
                status, or other scalar settings of a robot.
                Only provide the fields that need to change in the updates object.
                This tool does not modify capabilities or executors — use cerberus_robot_capability_update
                or cerberus_robot_executor_update for those.

                Use cerberus_robot_list to find the exact robot name before updating.

                Do not call this tool when the user only asks to list, read, create, or delete robots.
                """,
                new McpSchema.JsonSchema(
                        "object",
                        properties,
                        List.of("robot", "updates"),
                        null,
                        null,
                        null
                ),
                null,
                MCPToolUtils.updateAnnotations("Update robot", false),
                null
        );
    }

    /**
     * Validates input, loads the existing robot, applies the requested field changes, persists
     * the update, and returns a JSON result with the updated DTO.
     *
     * @param args raw MCP arguments map from the client request
     * @return a success JSON result containing the updated robot DTO, or an error text result
     */
    @SuppressWarnings("unchecked")
    private McpSchema.CallToolResult execute(Map<String, Object> args) {
        String robot = MCPToolUtils.getString(args, "robot", "");

        mcpLogUtils.call(TOOL_NAME, "robot_update",
                String.format("MCP tool %s called with robot=%s", TOOL_NAME, robot));

        if (robot.isBlank()) return MCPToolUtils.errorText("Missing required parameter: robot");

        Object updatesObject = args.get("updates");
        if (!(updatesObject instanceof Map)) {
            return MCPToolUtils.errorText("Missing or invalid required parameter: updates");
        }
        Map<String, Object> updates = (Map<String, Object>) updatesObject;
        if (updates.isEmpty()) {
            return MCPToolUtils.errorText("No field provided to update.");
        }

        // Read the existing entity so unmodified fields (and capabilities/executors) retain their current values.
        Robot entity;
        try {
            entity = robotService.readByKey(robot);
        } catch (CerberusException e) {
            return MCPToolUtils.errorText("Unable to retrieve robot " + robot + ": " + e.getMessage());
        }
        if (entity == null) {
            return MCPToolUtils.errorText("Robot does not exist: " + robot);
        }

        try {
            for (Map.Entry<String, Object> entry : updates.entrySet()) {
                String field = entry.getKey();
                Object value = entry.getValue();
                switch (field) {
                    case "type":
                        entity.setType(asString(value, field));
                        break;
                    case "platform":
                        entity.setPlatform(asString(value, field));
                        break;
                    case "browser":
                        entity.setBrowser(asString(value, field));
                        break;
                    case "version":
                        entity.setVersion(asString(value, field));
                        break;
                    case "active":
                        entity.setActive(asBoolean(value, field));
                        break;
                    case "userAgent":
                        entity.setUserAgent(asString(value, field));
                        break;
                    case "screenSize":
                        entity.setScreenSize(asString(value, field));
                        break;
                    case "profileFolder":
                        entity.setProfileFolder(asString(value, field));
                        break;
                    case "extraParam":
                        entity.setExtraParam(asString(value, field));
                        break;
                    case "acceptNotifications":
                        entity.setAcceptNotifications(asInteger(value, field));
                        break;
                    case "acceptInsecureCerts":
                        entity.setAcceptInsecureCerts(asBoolean(value, field));
                        break;
                    case "robotDecli":
                        entity.setRobotDecli(asString(value, field));
                        break;
                    case "lbexemethod":
                        entity.setLbexemethod(asString(value, field));
                        break;
                    case "description":
                        entity.setDescription(asString(value, field));
                        break;
                    case "preloadScript":
                        entity.setPreloadScript(asString(value, field));
                        break;
                    default:
                        return MCPToolUtils.errorText("Unsupported field for robot update: " + field);
                }
            }
        } catch (IllegalArgumentException e) {
            return MCPToolUtils.errorText(e.getMessage());
        }

        Answer answer = robotService.update(entity, "MCP");

        if (!answer.isCodeStringEquals("OK")) {
            return MCPToolUtils.errorText("Unable to update robot " + robot + ": " + answer.getMessageDescription());
        }

        return MCPToolUtils.successJson(Map.of(
                "status", "updated",
                "entry", mapper.toDTO(entity)
        ));
    }

    /**
     * Coerces a raw MCP argument value to a trimmed {@link String}.
     *
     * @param value the raw value from the arguments map
     * @param field the field name, used in the exception message
     * @return the trimmed string, or {@code ""} if {@code value} is {@code null}
     * @throws IllegalArgumentException if {@code value} is non-null but not a {@link String}
     */
    private String asString(Object value, String field) {
        if (value == null) return "";
        if (!(value instanceof String)) {
            throw new IllegalArgumentException("Invalid value for field '" + field + "'. Expected string.");
        }
        return ((String) value).trim();
    }

    /**
     * Coerces a raw MCP argument value to a {@link Boolean}.
     *
     * @param value the raw value from the arguments map
     * @param field the field name, used in the exception message
     * @return the boolean value
     * @throws IllegalArgumentException if {@code value} is not a {@link Boolean}
     */
    private boolean asBoolean(Object value, String field) {
        if (!(value instanceof Boolean)) {
            throw new IllegalArgumentException("Invalid value for field '" + field + "'. Expected boolean.");
        }
        return (Boolean) value;
    }

    /**
     * Coerces a raw MCP argument value to an {@link Integer}.
     *
     * @param value the raw value from the arguments map
     * @param field the field name, used in the exception message
     * @return integer value
     * @throws IllegalArgumentException if the value cannot be interpreted as an integer
     */
    private Integer asInteger(Object value, String field) {
        if (value instanceof Integer) return (Integer) value;
        if (value instanceof Number) return ((Number) value).intValue();
        throw new IllegalArgumentException("Invalid value for field '" + field + "'. Expected integer.");
    }

}