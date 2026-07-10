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

import java.util.List;
import java.util.Map;

/**
 * MCP tool that deletes an existing {@link RobotExecutor} from Cerberus.
 *
 * <p>Exposed MCP tool name: {@code cerberus_robot_executor_delete}.</p>
 *
 * <p>Delegates to {@link IRobotExecutorService} for both the existence check
 * ({@code readByKey}) and the deletion ({@code delete}). A DTO snapshot is captured before
 * deletion so the confirmation response can include the deleted executor's data; its
 * {@code hostPassword} credential is stripped from that snapshot before it is returned.</p>
 */
@Component
public class DeleteRobotExecutorTool implements MCPTool {

    private static final String TOOL_NAME = "cerberus_robot_executor_delete";

    private final IRobotExecutorService robotExecutorService;
    private final RobotExecutorMapperV001 mapper;
    private final MCPLogUtils mcpLogUtils;

    public DeleteRobotExecutorTool(IRobotExecutorService robotExecutorService,
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
     * Builds the MCP {@link McpSchema.Tool} descriptor for {@code cerberus_robot_executor_delete}.
     *
     * <p>Declares {@code robot} and {@code executor} as required parameters.
     * Annotated with delete semantics so MCP clients can surface a confirmation prompt.</p>
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
                        "description", "Exact name of the executor to delete."
                )
        );

        return new McpSchema.Tool(
                TOOL_NAME,
                null,
                """
                Deletes an executor (host) from a robot.

                Call this tool whenever the user asks to remove a host/node from a robot.
                Before calling this tool, confirm the robot name and executor name with the user.
                If this is the robot's last active executor, warn the user that the robot will no
                longer be usable for test execution afterward.

                Use cerberus_robot_executor_list to find the exact executor name before deleting.

                Do not call this tool when the user only asks to list, read, create, or update executors.
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
                MCPToolUtils.deleteAnnotations("Delete robot executor", false),
                null
        );
    }

    /**
     * Validates the arguments, checks the executor exists, captures a DTO snapshot, deletes it,
     * and returns a JSON confirmation payload.
     *
     * @param args raw MCP arguments map from the client request
     * @return a success JSON result containing the deleted executor DTO (without its credential),
     *         or an error text result
     */
    private McpSchema.CallToolResult execute(Map<String, Object> args) {
        String robot = MCPToolUtils.getString(args, "robot", "");
        String executor = MCPToolUtils.getString(args, "executor", "");

        mcpLogUtils.call(TOOL_NAME, "robot_executor_delete",
                String.format("MCP tool %s called with robot=%s executor=%s", TOOL_NAME, robot, executor));

        if (robot.isBlank()) return MCPToolUtils.errorText("Missing required parameter: robot");
        if (executor.isBlank()) return MCPToolUtils.errorText("Missing required parameter: executor");

        AnswerItem<RobotExecutor> readAnswer = robotExecutorService.readByKey(robot, executor);

        if (!readAnswer.isCodeStringEquals("OK") || readAnswer.getItem() == null) {
            return MCPToolUtils.errorText("Robot executor does not exist: robot=" + robot + " executor=" + executor);
        }

        RobotExecutor entity = readAnswer.getItem();
        // Snapshot the DTO before deletion — the entity is gone after the service call.
        RobotExecutorDTOV001 dto = mapper.toDTO(entity);
        // Strip the write-only credential before returning it to the MCP client.
        dto.setHostPassword(null);

        Answer deleteAnswer = robotExecutorService.delete(entity);

        if (!deleteAnswer.isCodeStringEquals("OK")) {
            return MCPToolUtils.errorText("Unable to delete robot executor: " + deleteAnswer.getMessageDescription());
        }

        return MCPToolUtils.successJson(Map.of(
                "status", "deleted",
                "entry", dto
        ));
    }

}