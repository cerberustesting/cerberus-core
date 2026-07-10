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
import org.cerberus.core.util.answer.AnswerItem;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

/**
 * MCP tool that retrieves a single {@link RobotExecutor} by its composite key
 * (robot + executor name).
 *
 * <p>Exposed MCP tool name: {@code cerberus_robot_executor_get}.</p>
 *
 * <p>Delegates to {@link IRobotExecutorService#readByKey(String, String)} and converts the
 * result via {@link RobotExecutorMapperV001}. The {@code hostPassword} credential is never
 * included in the response, regardless of what is stored.</p>
 */
@Component
public class GetRobotExecutorTool implements MCPTool {

    private static final String TOOL_NAME = "cerberus_robot_executor_get";

    private final IRobotExecutorService robotExecutorService;
    private final RobotExecutorMapperV001 mapper;
    private final MCPLogUtils mcpLogUtils;

    public GetRobotExecutorTool(IRobotExecutorService robotExecutorService,
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
     * Builds the MCP {@link McpSchema.Tool} descriptor for {@code cerberus_robot_executor_get}.
     *
     * <p>Declares {@code robot} and {@code executor} as required parameters — together they
     * form the composite business key for an executor.</p>
     *
     * @return the fully configured tool descriptor
     */
    private McpSchema.Tool createTool() {
        Map<String, Object> properties = Map.of(
                "robot", Map.of(
                        "type", "string",
                        "description", "Name of the robot the executor belongs to."
                ),
                "executor", Map.of(
                        "type", "string",
                        "description", "Exact name of the executor to retrieve."
                )
        );

        return new McpSchema.Tool(
                TOOL_NAME,
                null,
                """
                Retrieves the full details of a specific executor (host) of a robot.

                Call this tool whenever the user asks to inspect a specific executor.

                Use cerberus_robot_executor_list instead when the executor name is unknown or to
                browse all executors of a robot.
                Do not call this tool when the user asks to create, update, or delete an executor.

                Note: for security, the hostPassword credential is never included in the response.
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
                MCPToolUtils.readOnlyAnnotations("Get robot executor", true),
                null
        );
    }

    /**
     * Validates the arguments, calls the service, and returns the executor DTO with its
     * credential stripped.
     *
     * @param args raw MCP arguments map from the client request
     * @return a success JSON result containing the executor DTO, or an error text result
     */
    private McpSchema.CallToolResult execute(Map<String, Object> args) {
        String robot = MCPToolUtils.getString(args, "robot", "");
        String executor = MCPToolUtils.getString(args, "executor", "");

        mcpLogUtils.call(TOOL_NAME, "robot_executor_get",
                String.format("MCP tool %s called with robot=%s executor=%s", TOOL_NAME, robot, executor));

        if (robot.isBlank()) return MCPToolUtils.errorText("Missing required parameter: robot");
        if (executor.isBlank()) return MCPToolUtils.errorText("Missing required parameter: executor");

        AnswerItem<RobotExecutor> answer = robotExecutorService.readByKey(robot, executor);

        if (!answer.isCodeStringEquals("OK") || answer.getItem() == null) {
            return MCPToolUtils.errorText("Robot executor does not exist: robot=" + robot + " executor=" + executor);
        }

        RobotExecutorDTOV001 dto = mapper.toDTO(answer.getItem());
        // Strip the write-only credential before returning it to the MCP client.
        dto.setHostPassword(null);

        return MCPToolUtils.successJson(dto);
    }

}