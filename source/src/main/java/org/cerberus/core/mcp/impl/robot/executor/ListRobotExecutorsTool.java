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
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

/**
 * MCP tool that lists {@link RobotExecutor} entries (hosts able to run a robot).
 *
 * <p>Exposed MCP tool name: {@code cerberus_robot_executor_list}.</p>
 *
 * <p>A robot executor is a physical or virtual host (Selenium/Appium node) able to run a given
 * robot. A robot needs at least one active executor to be usable for test execution.</p>
 *
 * <p>Delegates to {@link IRobotExecutorService#readByRobot(String)} and converts results via
 * {@link RobotExecutorMapperV001}. The {@code hostPassword} credential is never included in the
 * response, regardless of what is stored.</p>
 */
@Component
public class ListRobotExecutorsTool implements MCPTool {

    private static final String TOOL_NAME = "cerberus_robot_executor_list";

    private final IRobotExecutorService robotExecutorService;
    private final RobotExecutorMapperV001 mapper;
    private final MCPLogUtils mcpLogUtils;

    public ListRobotExecutorsTool(IRobotExecutorService robotExecutorService,
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
     * Builds the MCP {@link McpSchema.Tool} descriptor for {@code cerberus_robot_executor_list}.
     *
     * <p>Declares {@code robot} as the only required parameter. {@code active} and {@code search}
     * filter the results client-side.</p>
     *
     * @return the fully configured tool descriptor
     */
    private McpSchema.Tool createTool() {
        Map<String, Object> properties = Map.of(
                "robot", Map.of(
                        "type", "string",
                        "description", "Name of the robot whose executors to list."
                ),
                "active", Map.of(
                        "type", "boolean",
                        "description", "Optional filter: when set, only returns executors whose active flag matches this value."
                ),
                "search", Map.of(
                        "type", "string",
                        "description", "Optional text filter applied on executor name, host, or description (case-insensitive)."
                )
        );

        return new McpSchema.Tool(
                TOOL_NAME,
                null,
                """
                Lists the executors (hosts) able to run a given robot.

                Call this tool whenever the user asks to list, browse, or search a robot's executors.

                Use cerberus_robot_executor_get to retrieve a specific executor by name.
                Do not call this tool when the user asks to create, update, or delete an executor.

                Note: for security, the hostPassword credential is never included in the response.
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
                MCPToolUtils.readOnlyAnnotations("List robot executors", false),
                null
        );
    }

    /**
     * Loads all executors for the given robot, applies optional client-side filters, maps them
     * to DTOs, and returns a JSON list. The {@code hostPassword} field is stripped from every
     * DTO before serialisation.
     *
     * @param args raw MCP arguments map from the client request
     * @return a success JSON result containing the executor list, or an error text result
     */
    private McpSchema.CallToolResult execute(Map<String, Object> args) {
        String robot = MCPToolUtils.getString(args, "robot", "");
        String search = MCPToolUtils.getString(args, "search", "");
        Boolean active = args.get("active") instanceof Boolean b ? b : null;

        mcpLogUtils.call(TOOL_NAME, "robot_executor_list",
                String.format("MCP tool %s called with robot=%s", TOOL_NAME, robot));

        if (robot.isBlank()) {
            return MCPToolUtils.errorText("Missing required parameter: robot");
        }

        List<RobotExecutor> raw = robotExecutorService.readByRobot(robot).getDataList();

        List<Object> executors = raw.stream()
                .filter(e -> active == null || e.isActive() == active)
                .filter(e -> matchesSearch(e, search))
                .map(mapper::toDTO)
                .map(this::withoutPassword)
                .map(Object.class::cast)
                .toList();

        return MCPToolUtils.successJson(Map.of(
                "robot", robot,
                "count", executors.size(),
                "executors", executors
        ));
    }

    /**
     * Returns {@code true} when the executor name, host, or description contains the search
     * term (case-insensitive), or when no search term was provided.
     *
     * @param executor the executor to test
     * @param search   the search string; blank means "match all"
     * @return whether this executor passes the search filter
     */
    private boolean matchesSearch(RobotExecutor executor, String search) {
        if (search.isBlank()) return true;
        return MCPToolUtils.containsIgnoreCase(executor.getExecutor(), search)
                || MCPToolUtils.containsIgnoreCase(executor.getHost(), search)
                || MCPToolUtils.containsIgnoreCase(executor.getDescription(), search);
    }

    /**
     * Strips the write-only {@code hostPassword} credential from a DTO before it is returned
     * to the MCP client.
     *
     * @param dto the DTO to sanitize
     * @return the same DTO instance, with {@code hostPassword} cleared
     */
    private RobotExecutorDTOV001 withoutPassword(RobotExecutorDTOV001 dto) {
        dto.setHostPassword(null);
        return dto;
    }

}