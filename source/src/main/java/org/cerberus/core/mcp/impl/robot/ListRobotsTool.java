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
import org.cerberus.core.mcp.MCPTool;
import org.cerberus.core.mcp.util.MCPLogUtils;
import org.cerberus.core.mcp.util.MCPToolUtils;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * MCP tool that lists {@link Robot} entries (execution agent definitions).
 *
 * <p>Exposed MCP tool name: {@code cerberus_robot_list}.</p>
 *
 * <p>A {@code Robot} is a reusable execution agent profile (browser/platform/version combination)
 * that testcases run against. It is the parent of {@code RobotCapability} rows (see
 * {@code cerberus_robot_capability_list}) and {@code RobotExecutor} rows (see
 * {@code cerberus_robot_executor_list}), which respectively define WebDriver capabilities and the
 * physical/virtual hosts able to run this robot.</p>
 *
 * <p>Delegates to {@link IRobotService#readByCriteria(boolean, boolean, int, int, String, String, String, Map)}
 * and converts results via {@link RobotMapperV001}.</p>
 */
@Component
public class ListRobotsTool implements MCPTool {

    private static final String TOOL_NAME = "cerberus_robot_list";

    private final IRobotService robotService;
    private final RobotMapperV001 mapper;
    private final MCPLogUtils mcpLogUtils;

    public ListRobotsTool(IRobotService robotService, RobotMapperV001 mapper, MCPLogUtils mcpLogUtils) {
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
     * Builds the MCP {@link McpSchema.Tool} descriptor for {@code cerberus_robot_list}.
     *
     * <p>All parameters are optional; without any filter, all robots are returned.</p>
     *
     * @return the fully configured tool descriptor
     */
    private McpSchema.Tool createTool() {
        Map<String, Object> properties = new LinkedHashMap<>();
        properties.put("type", Map.of(
                "type", "string",
                "description", "Optional exact robot type to filter on (e.g. 'GUI', 'APK', 'IPA', 'SRV', 'BAT')."
        ));
        properties.put("active", Map.of(
                "type", "boolean",
                "description", "Optional filter: when set, only returns robots whose active flag matches this value."
        ));
        properties.put("search", Map.of(
                "type", "string",
                "description", "Optional text filter applied on robot name, platform, browser, or description (case-insensitive)."
        ));

        return new McpSchema.Tool(
                TOOL_NAME,
                null,
                """
                Lists the execution agents (Robot) defined in Cerberus.

                A robot is a reusable browser/platform/version profile that testcases run against.
                Its physical/virtual hosts are defined separately as executors
                (cerberus_robot_executor_list) and its WebDriver capabilities as capabilities
                (cerberus_robot_capability_list).

                Call this tool whenever the user asks to list, browse, or search execution agents/robots.

                Use cerberus_robot_get to retrieve the full details of one specific robot, including
                its capabilities and executors.
                Do not call this tool when the user asks to create, update, or delete a robot.
                """,
                new McpSchema.JsonSchema(
                        "object",
                        properties,
                        List.of(),
                        null,
                        null,
                        null
                ),
                null,
                MCPToolUtils.readOnlyAnnotations("List robots", false),
                null
        );
    }

    /**
     * Loads all robots, applies optional client-side filters, maps them to DTOs, and returns
     * a JSON list. Capabilities and executors are not embedded here to keep the payload small —
     * use cerberus_robot_get for a single robot's full detail.
     *
     * @param args raw MCP arguments map from the client request
     * @return a success JSON result containing the robot list, or an error text result
     */
    private McpSchema.CallToolResult execute(Map<String, Object> args) {
        String type = MCPToolUtils.getString(args, "type", "");
        String search = MCPToolUtils.getString(args, "search", "");
        Boolean active = args.get("active") instanceof Boolean b ? b : null;

        mcpLogUtils.call(TOOL_NAME, "robot_list",
                String.format("MCP tool %s called with type=%s search=%s", TOOL_NAME, type, search));

        List<Robot> raw = robotService.readByCriteria(false, false, 0, 0, null, null, search, null).getDataList();

        List<Object> robots = raw.stream()
                .filter(r -> type.isBlank() || type.equalsIgnoreCase(r.getType()))
                .filter(r -> active == null || r.isActive() == active)
                .map(mapper::toDTO)
                .map(Object.class::cast)
                .toList();

        return MCPToolUtils.successJson(Map.of(
                "count", robots.size(),
                "robots", robots
        ));
    }

}