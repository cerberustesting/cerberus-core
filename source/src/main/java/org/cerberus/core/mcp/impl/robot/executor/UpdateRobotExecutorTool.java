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
package org.cerberus.core.mcp.impl.robot.executor;

import io.modelcontextprotocol.server.McpServerFeatures;
import io.modelcontextprotocol.spec.McpSchema;
import org.cerberus.core.api.dto.robot.RobotExecutorDTOV001;
import org.cerberus.core.api.dto.robot.RobotExecutorMapperV001;
import org.cerberus.core.crud.entity.RobotExecutor;
import org.cerberus.core.crud.service.IRobotExecutorService;
import org.cerberus.core.mcp.MCPTool;
import org.cerberus.core.mcp.util.MCPLogUtils;
import org.cerberus.core.mcp.util.MCPToolUtils;
import org.cerberus.core.util.answer.Answer;
import org.cerberus.core.util.answer.AnswerItem;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * MCP tool that updates an existing {@link RobotExecutor}.
 *
 * <p>Exposed MCP tool name: {@code cerberus_robot_executor_update}.</p>
 *
 * <p>Applies a read-before-write pattern: the existing executor is loaded first so untouched
 * fields retain their current values. Only the fields provided in the {@code updates} map are
 * modified.</p>
 *
 * <p>Delegates persistence to {@link IRobotExecutorService#update(String, String, RobotExecutor)}.
 * The {@code hostPassword} credential is never included in the response, even if it was just
 * changed.</p>
 */
@Component
public class UpdateRobotExecutorTool implements MCPTool {

    private static final String TOOL_NAME = "cerberus_robot_executor_update";

    private static final List<String> PROXY_TYPES = List.of(
            RobotExecutor.PROXY_TYPE_NONE,
            RobotExecutor.PROXY_TYPE_MANUAL,
            RobotExecutor.PROXY_TYPE_NETWORKTRAFFIC,
            RobotExecutor.PROXY_TYPE_MITMPROXY
    );

    private final IRobotExecutorService robotExecutorService;
    private final RobotExecutorMapperV001 mapper;
    private final MCPLogUtils mcpLogUtils;

    public UpdateRobotExecutorTool(IRobotExecutorService robotExecutorService,
                                   RobotExecutorMapperV001 mapper,
                                   MCPLogUtils mcpLogUtils) {
        this.robotExecutorService = robotExecutorService;
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
     * Builds the MCP {@link McpSchema.Tool} descriptor for {@code cerberus_robot_executor_update}.
     *
     * <p>The input schema uses a nested {@code updates} object with {@code additionalProperties: false}
     * so the AI model cannot send unrecognised fields.</p>
     *
     * @return the fully configured tool descriptor
     */
    private McpSchema.Tool createTool() {
        Map<String, Object> updateProperties = new LinkedHashMap<>();
        updateProperties.put("active", Map.of("type", "boolean", "description", "New active flag for this executor."));
        updateProperties.put("rank", Map.of("type", "integer", "description", "New rank used for load balancing."));
        updateProperties.put("host", Map.of("type", "string", "description", "New host (IP address or hostname)."));
        updateProperties.put("port", Map.of("type", "string", "description", "New port of the executor node."));
        updateProperties.put("hostUser", Map.of("type", "string", "description", "New username used to connect to the host."));
        updateProperties.put("hostPassword", Map.of("type", "string", "description", "New password used to connect to the host. Never returned in tool responses."));
        updateProperties.put("executorProxyType", Map.of(
                "type", "string",
                "description", "New proxy type to configure on the browser for this executor.",
                "enum", PROXY_TYPES
        ));
        updateProperties.put("executorProxyServiceHost", Map.of("type", "string", "description", "New proxy service host."));
        updateProperties.put("executorProxyServicePort", Map.of("type", "integer", "description", "New proxy service port."));
        updateProperties.put("executorBrowserProxyHost", Map.of("type", "string", "description", "New manual browser proxy host."));
        updateProperties.put("executorBrowserProxyPort", Map.of("type", "integer", "description", "New manual browser proxy port."));
        updateProperties.put("executorExtensionPort", Map.of("type", "integer", "description", "New Cerberus browser extension port."));
        updateProperties.put("executorExtensionProxyPort", Map.of("type", "integer", "description", "New proxy port used to reach the extension."));
        updateProperties.put("deviceUdid", Map.of("type", "string", "description", "New mobile device UDID."));
        updateProperties.put("deviceName", Map.of("type", "string", "description", "New mobile device name."));
        updateProperties.put("devicePort", Map.of("type", "integer", "description", "New mobile device port."));
        updateProperties.put("isDeviceLockUnlock", Map.of("type", "boolean", "description", "New device lock/unlock flag."));
        updateProperties.put("description", Map.of("type", "string", "description", "New human-readable description."));

        Map<String, Object> updatesSchema = new LinkedHashMap<>();
        updatesSchema.put("type", "object");
        updatesSchema.put("description", "Fields to update. Only provide the fields that need to change.");
        updatesSchema.put("properties", updateProperties);
        // Reject unknown fields early so the switch in execute() never hits the default branch unexpectedly.
        updatesSchema.put("additionalProperties", false);

        Map<String, Object> properties = new LinkedHashMap<>();
        properties.put("robot", Map.of(
                "type", "string",
                "description", "Name of the robot the executor belongs to."
        ));
        properties.put("executor", Map.of(
                "type", "string",
                "description", "Exact name of the executor to update."
        ));
        properties.put("updates", updatesSchema);

        return new McpSchema.Tool(
                TOOL_NAME,
                null,
                """
                Updates an existing executor (host) of a robot.

                Call this tool whenever the user asks to modify the host, port, credentials, proxy
                settings, or activation status of an executor.
                Only provide the fields that need to change in the updates object.

                Use cerberus_robot_executor_list to find the exact executor name before updating.

                Do not call this tool when the user only asks to list, read, create, or delete executors.

                Note: for security, the hostPassword credential is never included in the response,
                even if it was just changed.
                """,
                new McpSchema.JsonSchema(
                        "object",
                        properties,
                        List.of("robot", "executor", "updates"),
                        null,
                        null,
                        null
                ),
                null,
                MCPToolUtils.updateAnnotations("Update robot executor", false),
                null
        );
    }

    /**
     * Validates input, loads the existing executor, applies the requested field changes,
     * persists the update, and returns a JSON result with the updated DTO.
     *
     * @param args raw MCP arguments map from the client request
     * @return a success JSON result containing the updated executor DTO (without its credential),
     *         or an error text result
     */
    @SuppressWarnings("unchecked")
    private McpSchema.CallToolResult execute(Map<String, Object> args) {
        String robot = MCPToolUtils.getString(args, "robot", "");
        String executor = MCPToolUtils.getString(args, "executor", "");

        mcpLogUtils.call(TOOL_NAME, "robot_executor_update",
                String.format("MCP tool %s called with robot=%s executor=%s", TOOL_NAME, robot, executor));

        if (robot.isBlank()) return MCPToolUtils.errorText("Missing required parameter: robot");
        if (executor.isBlank()) return MCPToolUtils.errorText("Missing required parameter: executor");

        Object updatesObject = args.get("updates");
        if (!(updatesObject instanceof Map)) {
            return MCPToolUtils.errorText("Missing or invalid required parameter: updates");
        }
        Map<String, Object> updates = (Map<String, Object>) updatesObject;
        if (updates.isEmpty()) {
            return MCPToolUtils.errorText("No field provided to update.");
        }

        // Read the existing entity so unmodified fields retain their current values.
        AnswerItem<RobotExecutor> readAnswer = robotExecutorService.readByKey(robot, executor);
        if (!readAnswer.isCodeStringEquals("OK") || readAnswer.getItem() == null) {
            return MCPToolUtils.errorText("Robot executor does not exist: robot=" + robot + " executor=" + executor);
        }

        RobotExecutor entity = readAnswer.getItem();

        try {
            for (Map.Entry<String, Object> entry : updates.entrySet()) {
                String field = entry.getKey();
                Object value = entry.getValue();
                switch (field) {
                    case "active":
                        entity.setIsActive(asBoolean(value, field));
                        break;
                    case "rank":
                        entity.setRank(asInteger(value, field));
                        break;
                    case "host":
                        entity.setHost(asString(value, field));
                        break;
                    case "port":
                        entity.setPort(asString(value, field));
                        break;
                    case "hostUser":
                        entity.setHostUser(asString(value, field));
                        break;
                    case "hostPassword":
                        entity.setHostPassword(asString(value, field));
                        break;
                    case "executorProxyType":
                        entity.setExecutorProxyType(asString(value, field));
                        break;
                    case "executorProxyServiceHost":
                        entity.setExecutorProxyServiceHost(asString(value, field));
                        break;
                    case "executorProxyServicePort":
                        entity.setExecutorProxyServicePort(asInteger(value, field));
                        break;
                    case "executorBrowserProxyHost":
                        entity.setExecutorBrowserProxyHost(asString(value, field));
                        break;
                    case "executorBrowserProxyPort":
                        entity.setExecutorBrowserProxyPort(asInteger(value, field));
                        break;
                    case "executorExtensionPort":
                        entity.setExecutorExtensionPort(asInteger(value, field));
                        break;
                    case "executorExtensionProxyPort":
                        entity.setExecutorExtensionProxyPort(asInteger(value, field));
                        break;
                    case "deviceUdid":
                        entity.setDeviceUdid(asString(value, field));
                        break;
                    case "deviceName":
                        entity.setDeviceName(asString(value, field));
                        break;
                    case "devicePort":
                        entity.setDevicePort(asInteger(value, field));
                        break;
                    case "isDeviceLockUnlock":
                        entity.setIsDeviceLockUnlock(asBoolean(value, field));
                        break;
                    case "description":
                        entity.setDescription(asString(value, field));
                        break;
                    default:
                        return MCPToolUtils.errorText("Unsupported field for robot executor update: " + field);
                }
            }
        } catch (IllegalArgumentException e) {
            return MCPToolUtils.errorText(e.getMessage());
        }

        // Tag the modifier so audit trails identify MCP-originated changes.
        entity.setUsrModif("MCP");

        // Service takes the original (robot, executor) key separately from the entity.
        Answer answer = robotExecutorService.update(robot, executor, entity);

        if (!answer.isCodeStringEquals("OK")) {
            return MCPToolUtils.errorText("Unable to update robot executor: " + answer.getMessageDescription());
        }

        RobotExecutorDTOV001 dto = mapper.toDTO(entity);
        // Strip the write-only credential before returning it to the MCP client.
        dto.setHostPassword(null);

        return MCPToolUtils.successJson(Map.of(
                "status", "updated",
                "entry", dto
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