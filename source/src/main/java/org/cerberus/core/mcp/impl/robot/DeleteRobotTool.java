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
import org.cerberus.core.util.answer.Answer;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

/**
 * MCP tool that deletes an existing {@link Robot} from Cerberus.
 *
 * <p>Exposed MCP tool name: {@code cerberus_robot_delete}.</p>
 *
 * <p>Deleting a robot does not automatically remove its capabilities/executors at the service
 * layer covered by this tool set — if the underlying database enforces a foreign key
 * constraint on those child tables, the deletion may be rejected; the resulting error is
 * surfaced as-is.</p>
 *
 * <p>Delegates to {@link IRobotService} for both the existence check ({@code readByKey}) and
 * the deletion ({@code delete}). A DTO snapshot is captured before deletion via
 * {@link RobotMapperV001} so the confirmation response can include the deleted robot's data.</p>
 */
@Component
public class DeleteRobotTool implements MCPTool {

    private static final String TOOL_NAME = "cerberus_robot_delete";

    private final IRobotService robotService;
    private final RobotMapperV001 mapper;
    private final MCPLogUtils mcpLogUtils;

    public DeleteRobotTool(IRobotService robotService, RobotMapperV001 mapper, MCPLogUtils mcpLogUtils) {
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
     * Builds the MCP {@link McpSchema.Tool} descriptor for {@code cerberus_robot_delete}.
     *
     * <p>Declares {@code robot} as the only required parameter. Annotated with delete semantics
     * so MCP clients can surface a confirmation prompt.</p>
     *
     * @return the fully configured tool descriptor
     */
    private McpSchema.Tool createTool() {
        Map<String, Object> properties = Map.of(
                "robot", Map.of(
                        "type", "string",
                        "description", "Exact name of the robot to delete."
                )
        );

        return new McpSchema.Tool(
                TOOL_NAME,
                null,
                """
                Deletes a robot (execution agent profile) from Cerberus.

                Call this tool whenever the user asks to remove or delete a robot.
                Before calling this tool, confirm the robot name with the user, and consider
                removing its executors and capabilities first if the deletion is rejected due to
                existing child records.

                Use cerberus_robot_list to find the exact robot name before deleting.

                Do not call this tool when the user only asks to list, read, create, or update robots.
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
                MCPToolUtils.deleteAnnotations("Delete robot", false),
                null
        );
    }

    /**
     * Validates the arguments, checks the robot exists, captures a DTO snapshot, deletes it,
     * and returns a JSON confirmation payload.
     *
     * @param args raw MCP arguments map from the client request
     * @return a success JSON result containing the deleted robot DTO, or an error text result
     */
    private McpSchema.CallToolResult execute(Map<String, Object> args) {
        String robot = MCPToolUtils.getString(args, "robot", "");

        mcpLogUtils.call(TOOL_NAME, "robot_delete",
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

        // Snapshot the DTO before deletion — the entity is gone after the service call.
        RobotDTOV001 dto = mapper.toDTO(entity);

        Answer deleteAnswer = robotService.delete(entity);

        if (!deleteAnswer.isCodeStringEquals("OK")) {
            return MCPToolUtils.errorText("Unable to delete robot " + robot + ": " + deleteAnswer.getMessageDescription());
        }

        return MCPToolUtils.successJson(Map.of(
                "status", "deleted",
                "entry", dto
        ));
    }

}