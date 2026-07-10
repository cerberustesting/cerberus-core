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
import org.cerberus.core.crud.factory.IFactoryRobotExecutor;
import org.cerberus.core.crud.service.IRobotExecutorService;
import org.cerberus.core.mcp.MCPTool;
import org.cerberus.core.mcp.util.MCPLogUtils;
import org.cerberus.core.mcp.util.MCPToolUtils;
import org.cerberus.core.util.answer.Answer;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * MCP tool that creates a new {@link RobotExecutor} for a robot.
 *
 * <p>Exposed MCP tool name: {@code cerberus_robot_executor_create}.</p>
 *
 * <p>Before persisting, verifies that no executor with the same (robot, executor) key already
 * exists to prevent duplicate key errors at the database level.</p>
 *
 * <p>Builds the entity via {@link IFactoryRobotExecutor} and delegates persistence to
 * {@link IRobotExecutorService}. The {@code hostPassword} credential is never included in the
 * response, even though it was just supplied by the caller.</p>
 */
@Component
public class CreateRobotExecutorTool implements MCPTool {

    private static final String TOOL_NAME = "cerberus_robot_executor_create";

    private static final List<String> PROXY_TYPES = List.of(
            RobotExecutor.PROXY_TYPE_NONE,
            RobotExecutor.PROXY_TYPE_MANUAL,
            RobotExecutor.PROXY_TYPE_NETWORKTRAFFIC,
            RobotExecutor.PROXY_TYPE_MITMPROXY
    );

    private final IRobotExecutorService robotExecutorService;
    private final IFactoryRobotExecutor factoryRobotExecutor;
    private final RobotExecutorMapperV001 mapper;
    private final MCPLogUtils mcpLogUtils;

    public CreateRobotExecutorTool(IRobotExecutorService robotExecutorService,
                                   IFactoryRobotExecutor factoryRobotExecutor,
                                   RobotExecutorMapperV001 mapper,
                                   MCPLogUtils mcpLogUtils) {
        this.robotExecutorService = robotExecutorService;
        this.factoryRobotExecutor = factoryRobotExecutor;
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
     * Builds the MCP {@link McpSchema.Tool} descriptor for {@code cerberus_robot_executor_create}.
     *
     * <p>Declares {@code robot} and {@code executor} as required parameters. {@code host} is
     * strongly recommended — a robot cannot run tests via an executor with no host.</p>
     *
     * @return the fully configured tool descriptor
     */
    private McpSchema.Tool createTool() {
        Map<String, Object> properties = new LinkedHashMap<>();
        properties.put("robot", Map.of(
                "type", "string",
                "description", "Name of the robot this executor belongs to. Must already exist (see cerberus_robot_get / cerberus_robot_create)."
        ));
        properties.put("executor", Map.of(
                "type", "string",
                "description", "Unique name for the new executor within this robot."
        ));
        properties.put("active", Map.of(
                "type", "boolean",
                "description", "Whether this executor is active. Defaults to true."
        ));
        properties.put("rank", Map.of(
                "type", "integer",
                "description", "Optional rank used for load balancing across executors when the robot uses BYRANKING. Defaults to 1."
        ));
        properties.put("host", Map.of(
                "type", "string",
                "description", "Host (IP address or hostname) of the executor node. Required for the executor to be usable."
        ));
        properties.put("port", Map.of(
                "type", "string",
                "description", "Optional port of the executor node (e.g. Selenium/Appium node port)."
        ));
        properties.put("hostUser", Map.of(
                "type", "string",
                "description", "Optional username used to connect to the host."
        ));
        properties.put("hostPassword", Map.of(
                "type", "string",
                "description", "Optional password used to connect to the host. Never returned in tool responses."
        ));
        properties.put("executorProxyType", Map.of(
                "type", "string",
                "description", "Proxy type to configure on the browser for this executor. Defaults to 'NONE'.",
                "enum", PROXY_TYPES
        ));
        properties.put("executorProxyServiceHost", Map.of("type", "string", "description", "Optional proxy service host."));
        properties.put("executorProxyServicePort", Map.of("type", "integer", "description", "Optional proxy service port."));
        properties.put("executorBrowserProxyHost", Map.of("type", "string", "description", "Optional manual browser proxy host (used when executorProxyType is 'MANUAL')."));
        properties.put("executorBrowserProxyPort", Map.of("type", "integer", "description", "Optional manual browser proxy port (used when executorProxyType is 'MANUAL')."));
        properties.put("executorExtensionPort", Map.of("type", "integer", "description", "Optional Cerberus browser extension port."));
        properties.put("executorExtensionProxyPort", Map.of("type", "integer", "description", "Optional proxy port used to reach the extension when the node has a private IP."));
        properties.put("deviceUdid", Map.of("type", "string", "description", "Optional mobile device UDID (for mobile executors)."));
        properties.put("deviceName", Map.of("type", "string", "description", "Optional mobile device name (for mobile executors)."));
        properties.put("devicePort", Map.of("type", "integer", "description", "Optional mobile device port (for mobile executors)."));
        properties.put("isDeviceLockUnlock", Map.of("type", "boolean", "description", "Whether Cerberus should lock/unlock the mobile device around execution. Defaults to false."));
        properties.put("description", Map.of("type", "string", "description", "Optional human-readable description of the executor."));

        return new McpSchema.Tool(
                TOOL_NAME,
                null,
                """
                Adds a new executor (host) able to run a given robot.

                Call this tool whenever the user asks to add a host/node for a robot to run tests on.
                Requires the robot name and a new executor name; a host should also be provided since
                an executor without a host cannot run tests.

                Do not call this tool when the user only asks to list, read, update, or delete executors.

                Note: for security, the hostPassword credential is never included in the response,
                even though it was just supplied.
                """,
                new McpSchema.JsonSchema(
                        "object",
                        properties,
                        List.of("robot", "executor"),
                        null,
                        null,
                        null
                ),
                null,
                MCPToolUtils.createAnnotations("Create robot executor", false),
                null
        );
    }

    /**
     * Validates the arguments, checks for duplicates, builds the entity via
     * {@link IFactoryRobotExecutor}, and delegates creation to
     * {@link IRobotExecutorService#create(RobotExecutor)}.
     *
     * @param args raw MCP arguments map from the client request
     * @return a success JSON result containing the created executor DTO (without its credential),
     *         or an error text result
     */
    private McpSchema.CallToolResult execute(Map<String, Object> args) {
        String robot = MCPToolUtils.getString(args, "robot", "");
        String executor = MCPToolUtils.getString(args, "executor", "");

        mcpLogUtils.call(TOOL_NAME, "robot_executor_create",
                String.format("MCP tool %s called with robot=%s executor=%s", TOOL_NAME, robot, executor));

        if (robot.isBlank()) return MCPToolUtils.errorText("Missing required parameter: robot");
        if (executor.isBlank()) return MCPToolUtils.errorText("Missing required parameter: executor");

        // Guard against duplicate (robot, executor) key before hitting the DB unique constraint.
        if (robotExecutorService.exist(robot, executor)) {
            return MCPToolUtils.errorText("Robot executor already exists: robot=" + robot + " executor=" + executor);
        }

        RobotExecutor entity = factoryRobotExecutor.create(
                0,
                robot,
                executor,
                MCPToolUtils.getBoolean(args, "active", true),
                MCPToolUtils.getInteger(args, "rank", 1),
                MCPToolUtils.getString(args, "host", ""),
                MCPToolUtils.getString(args, "port", ""),
                MCPToolUtils.getString(args, "hostUser", ""),
                MCPToolUtils.getString(args, "hostPassword", ""),
                getOptionalInteger(args, "executorExtensionProxyPort"),
                MCPToolUtils.getString(args, "deviceUdid", ""),
                MCPToolUtils.getString(args, "deviceName", ""),
                getOptionalInteger(args, "devicePort"),
                MCPToolUtils.getBoolean(args, "isDeviceLockUnlock", false),
                MCPToolUtils.getString(args, "executorProxyServiceHost", ""),
                getOptionalInteger(args, "executorProxyServicePort"),
                MCPToolUtils.getString(args, "executorBrowserProxyHost", ""),
                getOptionalInteger(args, "executorBrowserProxyPort"),
                getOptionalInteger(args, "executorExtensionPort"),
                MCPToolUtils.getString(args, "executorProxyType", RobotExecutor.PROXY_TYPE_NONE),
                MCPToolUtils.getString(args, "description", ""),
                "MCP",
                null,
                null,
                null
        );

        Answer answer = robotExecutorService.create(entity);

        if (!answer.isCodeStringEquals("OK")) {
            return MCPToolUtils.errorText("Unable to create robot executor: " + answer.getMessageDescription());
        }

        RobotExecutorDTOV001 dto = mapper.toDTO(entity);
        // Strip the write-only credential before returning it to the MCP client.
        dto.setHostPassword(null);

        return MCPToolUtils.successJson(Map.of(
                "status", "created",
                "entry", dto
        ));
    }

    /**
     * Returns the integer value of {@code key} if present in {@code args}, or {@code null}
     * when absent — used for genuinely optional numeric fields where {@code 0} would be a
     * misleading default (e.g. a port number).
     *
     * @param args raw MCP arguments map
     * @param key  the argument name to look up
     * @return the integer value, or {@code null} if not provided
     */
    private Integer getOptionalInteger(Map<String, Object> args, String key) {
        Object value = args.get(key);
        if (value instanceof Integer i) return i;
        if (value instanceof Number n) return n.intValue();
        return null;
    }

}