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
package org.cerberus.core.mcp.impl.test.testcase.step.action;

import io.modelcontextprotocol.server.McpServerFeatures;
import io.modelcontextprotocol.spec.McpSchema;
import org.cerberus.core.api.dto.testcaseaction.TestcaseStepActionDTOV001;
import org.cerberus.core.api.dto.testcaseaction.TestcaseStepActionMapperV001;
import org.cerberus.core.crud.entity.TestCaseStepAction;
import org.cerberus.core.crud.service.ITestCaseStepActionService;
import org.cerberus.core.mcp.MCPTool;
import org.cerberus.core.mcp.util.MCPLogUtils;
import org.cerberus.core.mcp.util.MCPToolUtils;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

/**
 * MCP tool that lists the actions belonging to a specific step of a Cerberus test case.
 *
 * <p>Exposed MCP tool name: {@value #TOOL_NAME}</p>
 *
 * <p>Delegates to {@link ITestCaseStepActionService#getListOfAction(String, String, int)} to
 * retrieve all {@link TestCaseStepAction} entities for the given (testFolder, testcase, stepId)
 * triplet, then maps each entity to {@link TestcaseStepActionDTOV001} via
 * {@link TestcaseStepActionMapperV001}.</p>
 *
 * <p>This is a read-only tool; create/update/delete operations are handled by dedicated tools.</p>
 */
@Component
public class ListTestCaseStepActionsTool implements MCPTool {

    private static final String TOOL_NAME = "cerberus_testcase_step_action_list";

    private final ITestCaseStepActionService testCaseStepActionService;
    private final TestcaseStepActionMapperV001 mapper;
    private final MCPLogUtils mcpLogUtils;

    public ListTestCaseStepActionsTool(ITestCaseStepActionService testCaseStepActionService, TestcaseStepActionMapperV001 mapper, MCPLogUtils mcpLogUtils) {
        this.testCaseStepActionService = testCaseStepActionService;
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
     * Builds the static {@link McpSchema.Tool} descriptor registered with the MCP server at startup.
     *
     * <p>Input schema parameters are defined inline as a plain {@link Map} rather than being
     * sourced from the invariant table — no external data is loaded here, so there is no
     * startup-time risk of a missing HTTP request context.</p>
     *
     * <p>The {@code search} parameter is optional; all other parameters are required.</p>
     */
    private McpSchema.Tool createTool() {
        Map<String, Object> properties = Map.of(
                "testFolder", Map.of(
                        "type", "string",
                        "description", "Name of the test folder containing the testcase."
                ),
                "testcase", Map.of(
                        "type", "string",
                        "description", "Identifier of the testcase containing the step."
                ),
                "stepId", Map.of(
                        "type", "integer",
                        "description", "ID of the step to list actions from."
                ),
                "search", Map.of(
                        "type", "string",
                        "description", "Optional text filter on action description or action type."
                )
        );

        return new McpSchema.Tool(
                TOOL_NAME,
                null,
                """
                Returns the list of actions for a given step in a Cerberus testcase.

                Call this tool when the user needs to browse or inspect the actions of a step.
                Use search to filter actions by description or action type.

                Do not call this tool when the user asks to create, update, or delete actions.
                """,
                new McpSchema.JsonSchema(
                        "object",
                        properties,
                        List.of("testFolder", "testcase", "stepId"),
                        null,
                        null,
                        null
                ),
                null,
                MCPToolUtils.readOnlyAnnotations("List testcase step actions", false),
                null
        );
    }

    /**
     * Validates inputs, fetches all actions for the given step from the service, applies the
     * optional {@code search} filter, and returns the serialised DTO list as a JSON result.
     *
     * <p>Returns an error result immediately if any required parameter is missing or invalid,
     * avoiding unnecessary service calls.</p>
     *
     * @param args raw MCP argument map from the client request
     * @return a {@link McpSchema.CallToolResult} containing either the action list or an error message
     */
    private McpSchema.CallToolResult execute(Map<String, Object> args) {
        String testFolder = MCPToolUtils.getString(args, "testFolder", "");
        String testcaseId = MCPToolUtils.getString(args, "testcase", "");
        int stepId = MCPToolUtils.getInteger(args, "stepId", -1);
        String search = MCPToolUtils.getString(args, "search", "");

        mcpLogUtils.call(TOOL_NAME, "testcase_step_action_list",
                String.format("MCP tool %s called with testFolder=%s testcase=%s stepId=%d", TOOL_NAME, testFolder, testcaseId, stepId));

        if (testFolder.isBlank()) {
            return MCPToolUtils.errorText("Missing required parameter: testFolder");
        }

        if (testcaseId.isBlank()) {
            return MCPToolUtils.errorText("Missing required parameter: testcase");
        }

        // stepId defaults to -1 when absent or unparseable, so any negative value means it was not provided
        if (stepId < 0) {
            return MCPToolUtils.errorText("Missing required parameter: stepId");
        }

        List<TestCaseStepAction> actions = testCaseStepActionService.getListOfAction(testFolder, testcaseId, stepId);

        // Guard against a null return from the service (e.g. step not found)
        List<TestcaseStepActionDTOV001> dtos = actions == null ? List.of() : actions.stream()
                .filter(a -> matchesSearch(a, search))
                .map(mapper::toDTO)
                .toList();

        return MCPToolUtils.successJson(Map.of(
                "testFolder", testFolder,
                "testcase", testcaseId,
                "stepId", stepId,
                "count", dtos.size(),
                "actions", dtos
        ));
    }

    /**
     * Returns {@code true} when {@code action} matches the given search string.
     *
     * <p>Matching is case-insensitive and checks both the action description and the action type.
     * A blank or null {@code search} value is treated as a wildcard that matches every action.</p>
     *
     * @param action the action entity to test
     * @param search the filter string provided by the client (may be blank or null)
     * @return {@code true} if the action should be included in the result
     */
    private boolean matchesSearch(TestCaseStepAction action, String search) {
        if (search == null || search.isBlank()) return true;
        return MCPToolUtils.containsIgnoreCase(action.getDescription(), search)
                || MCPToolUtils.containsIgnoreCase(action.getAction(), search);
    }

}
