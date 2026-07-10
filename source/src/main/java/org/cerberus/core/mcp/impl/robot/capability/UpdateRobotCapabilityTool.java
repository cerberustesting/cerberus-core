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
import org.cerberus.core.util.answer.Answer;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * MCP tool that updates the value of an existing {@link RobotCapability}.
 *
 * <p>Exposed MCP tool name: {@code cerberus_robot_capability_update}.</p>
 *
 * <p>{@link IRobotCapabilityService} has no single-item lookup — this tool calls
 * {@link IRobotCapabilityService#readByRobot(String)} and filters in memory for the requested
 * capability key before applying the change and persisting via
 * {@link IRobotCapabilityService#update(RobotCapability)}.</p>
 */
@Component
public class UpdateRobotCapabilityTool implements MCPTool {

    private static final String TOOL_NAME = "cerberus_robot_capability_update";

    private final IRobotCapabilityService robotCapabilityService;
    private final RobotCapabilityMapperV001 mapper;
    private final MCPLogUtils mcpLogUtils;

    public UpdateRobotCapabilityTool(IRobotCapabilityService robotCapabilityService,
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
     * Builds the MCP {@link McpSchema.Tool} descriptor for {@code cerberus_robot_capability_update}.
     *
     * <p>The only mutable field is {@code value} — {@code robot} and {@code capability} form the
     * composite key and are not renameable through this tool.</p>
     *
     * @return the fully configured tool descriptor
     */
    private McpSchema.Tool createTool() {
        Map<String, Object> updateProperties = new LinkedHashMap<>();
        updateProperties.put("value", Map.of(
                "type", "string",
                "description", "New value for this capability."
        ));

        Map<String, Object> updatesSchema = new LinkedHashMap<>();
        updatesSchema.put("type", "object");
        updatesSchema.put("description", "Fields to update. Only 'value' is mutable.");
        updatesSchema.put("properties", updateProperties);
        // Reject unknown fields early so the switch in execute() never hits the default branch unexpectedly.
        updatesSchema.put("additionalProperties", false);

        Map<String, Object> properties = new LinkedHashMap<>();
        properties.put("robot", Map.of(
                "type", "string",
                "description", "Name of the robot the capability belongs to."
        ));
        properties.put("capability", Map.of(
                "type", "string",
                "description", "Exact capability key to update."
        ));
        properties.put("updates", updatesSchema);

        return new McpSchema.Tool(
                TOOL_NAME,
                null,
                """
                Updates the value of an existing WebDriver capability on a robot.

                Call this tool whenever the user asks to change the value of a robot's capability.
                Only the value can be changed — to rename a capability key, delete it and create a new one.

                Use cerberus_robot_capability_list to find the exact capability key before updating.

                Do not call this tool when the user only asks to list, read, create, or delete capabilities.
                """,
                new McpSchema.JsonSchema(
                        "object",
                        properties,
                        List.of("robot", "capability", "updates"),
                        null,
                        null,
                        null
                ),
                null,
                MCPToolUtils.updateAnnotations("Update robot capability", false),
                null
        );
    }

    /**
     * Validates input, locates the existing capability, applies the new value, persists the
     * update, and returns a JSON result with the updated DTO.
     *
     * @param args raw MCP arguments map from the client request
     * @return a success JSON result containing the updated capability DTO, or an error text result
     */
    @SuppressWarnings("unchecked")
    private McpSchema.CallToolResult execute(Map<String, Object> args) {
        String robot = MCPToolUtils.getString(args, "robot", "");
        String capability = MCPToolUtils.getString(args, "capability", "");

        mcpLogUtils.call(TOOL_NAME, "robot_capability_update",
                String.format("MCP tool %s called with robot=%s capability=%s", TOOL_NAME, robot, capability));

        if (robot.isBlank()) return MCPToolUtils.errorText("Missing required parameter: robot");
        if (capability.isBlank()) return MCPToolUtils.errorText("Missing required parameter: capability");

        Object updatesObject = args.get("updates");
        if (!(updatesObject instanceof Map)) {
            return MCPToolUtils.errorText("Missing or invalid required parameter: updates");
        }
        Map<String, Object> updates = (Map<String, Object>) updatesObject;
        if (updates.isEmpty()) {
            return MCPToolUtils.errorText("No field provided to update.");
        }

        List<RobotCapability> raw = robotCapabilityService.readByRobot(robot).getDataList();
        Optional<RobotCapability> match = raw.stream()
                .filter(c -> capability.equals(c.getCapability()))
                .findFirst();

        if (match.isEmpty()) {
            return MCPToolUtils.errorText("Robot capability does not exist: robot=" + robot + " capability=" + capability);
        }

        RobotCapability entity = match.get();

        try {
            for (Map.Entry<String, Object> entry : updates.entrySet()) {
                String field = entry.getKey();
                Object value = entry.getValue();
                switch (field) {
                    case "value":
                        entity.setValue(asString(value, field));
                        break;
                    default:
                        return MCPToolUtils.errorText("Unsupported field for robot capability update: " + field);
                }
            }
        } catch (IllegalArgumentException e) {
            return MCPToolUtils.errorText(e.getMessage());
        }

        Answer answer = robotCapabilityService.update(entity);

        if (!answer.isCodeStringEquals("OK")) {
            return MCPToolUtils.errorText("Unable to update robot capability: " + answer.getMessageDescription());
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

}