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
 * MCP tool that retrieves a single {@link TestCaseStepAction} by its composite key
 * (testFolder, testcaseId, stepId, actionId).
 *
 * <p>Exposes the MCP tool name {@code cerberus_testcase_step_action_get}.
 *
 * <p>Delegates lookups to {@link ITestCaseStepActionService#findTestCaseStepActionbyKey}
 * and maps the result to a {@link TestcaseStepActionDTOV001} via
 * {@link TestcaseStepActionMapperV001} before returning it as JSON.
 *
 * <p>This is a read-only tool; for mutations use the corresponding create/update/delete tools.
 */
@Component
public class GetTestCaseStepActionTool implements MCPTool {

    private static final String TOOL_NAME = "cerberus_testcase_step_action_get";

    private final ITestCaseStepActionService testCaseStepActionService;
    private final TestcaseStepActionMapperV001 mapper;
    private final MCPLogUtils mcpLogUtils;

    public GetTestCaseStepActionTool(ITestCaseStepActionService testCaseStepActionService, TestcaseStepActionMapperV001 mapper, MCPLogUtils mcpLogUtils) {
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
     * <p>Input schema parameters are defined as static JSON (no invariant table lookup),
     * so there is no startup-time risk from missing HTTP context or database availability.
     * Required parameters are: {@code testFolder}, {@code testcase}, {@code stepId}, {@code actionId}.
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
                        "description", "ID of the step containing the action."
                ),
                "actionId", Map.of(
                        "type", "integer",
                        "description", "ID of the action to retrieve."
                )
        );

        return new McpSchema.Tool(
                TOOL_NAME,
                null,
                """
                Retrieves a single action from a testcase step in Cerberus by test folder, testcase ID, step ID, and action ID.

                Call this tool whenever the user asks to get or inspect the details of a specific action.

                Use cerberus_testcase_step_action_list instead when the action ID is unknown or when listing multiple actions.
                Do not call this tool when the user asks to create, update, or delete an action.
                """,
                new McpSchema.JsonSchema(
                        "object",
                        properties,
                        List.of("testFolder", "testcase", "stepId", "actionId"),
                        null,
                        null,
                        null
                ),
                null,
                MCPToolUtils.readOnlyAnnotations("Get testcase step action", false),
                null
        );
    }

    /**
     * Validates all required arguments, looks up the action via the service, and returns
     * the mapped DTO as a JSON success result.
     *
     * <p>Returns an error result immediately if any required parameter is missing or invalid,
     * or if the service returns {@code null} (action not found).
     *
     * @param args tool arguments extracted from the MCP request
     * @return a {@link McpSchema.CallToolResult} containing the action DTO or an error message
     */
    private McpSchema.CallToolResult execute(Map<String, Object> args) {
        String testFolder = MCPToolUtils.getString(args, "testFolder", "");
        String testcaseId = MCPToolUtils.getString(args, "testcase", "");
        int stepId = MCPToolUtils.getInteger(args, "stepId", -1);
        int actionId = MCPToolUtils.getInteger(args, "actionId", -1);

        mcpLogUtils.call(TOOL_NAME, "testcase_step_action_get",
                String.format("MCP tool %s called with testFolder=%s testcase=%s stepId=%d actionId=%d", TOOL_NAME, testFolder, testcaseId, stepId, actionId));

        if (testFolder.isBlank()) {
            return MCPToolUtils.errorText("Missing required parameter: testFolder");
        }

        if (testcaseId.isBlank()) {
            return MCPToolUtils.errorText("Missing required parameter: testcase");
        }

        // stepId and actionId use -1 as sentinel for "not provided"
        if (stepId < 0) {
            return MCPToolUtils.errorText("Missing required parameter: stepId");
        }

        if (actionId < 0) {
            return MCPToolUtils.errorText("Missing required parameter: actionId");
        }

        TestCaseStepAction action = testCaseStepActionService.findTestCaseStepActionbyKey(testFolder, testcaseId, stepId, actionId);
        if (action == null) {
            return MCPToolUtils.errorText("Action does not exist: testFolder=" + testFolder + " testcase=" + testcaseId + " stepId=" + stepId + " actionId=" + actionId);
        }

        TestcaseStepActionDTOV001 dto = mapper.toDTO(action);
        return MCPToolUtils.successJson(dto);
    }

}
