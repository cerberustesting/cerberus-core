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
package org.cerberus.core.mcp.impl.test.testcase.step.action.control;

import io.modelcontextprotocol.server.McpServerFeatures;
import io.modelcontextprotocol.spec.McpSchema;
import org.cerberus.core.api.dto.testcasecontrol.TestcaseStepActionControlDTOV001;
import org.cerberus.core.api.dto.testcasecontrol.TestcaseStepActionControlMapperV001;
import org.cerberus.core.crud.entity.TestCaseStepActionControl;
import org.cerberus.core.crud.service.ITestCaseStepActionControlService;
import org.cerberus.core.mcp.MCPTool;
import org.cerberus.core.mcp.util.MCPLogUtils;
import org.cerberus.core.mcp.util.MCPToolUtils;
import org.cerberus.core.util.answer.AnswerList;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * MCP tool that retrieves a single {@link TestCaseStepActionControl} (assertion) identified
 * by its composite key: testFolder + testcase + stepId + actionId + controlId.
 *
 * <p>Exposes the MCP tool name {@code cerberus_testcase_step_action_control_get}.
 *
 * <p>Delegates persistence to {@link ITestCaseStepActionControlService} and converts the
 * resulting entity to a DTO via {@link TestcaseStepActionControlMapperV001}.
 *
 * <p>No direct findByKey service method exists — the full control list for the parent action
 * is loaded via {@code readByVarious1} and then filtered in memory by controlId.
 */
@Component
public class GetTestCaseStepActionControlTool implements MCPTool {

    private static final String TOOL_NAME = "cerberus_testcase_step_action_control_get";

    private final ITestCaseStepActionControlService testCaseStepActionControlService;
    private final TestcaseStepActionControlMapperV001 mapper;
    private final MCPLogUtils mcpLogUtils;

    public GetTestCaseStepActionControlTool(ITestCaseStepActionControlService testCaseStepActionControlService, TestcaseStepActionControlMapperV001 mapper, MCPLogUtils mcpLogUtils) {
        this.testCaseStepActionControlService = testCaseStepActionControlService;
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
     * The input schema is defined inline (no invariant table or static JSON lookup is needed for
     * this read-only tool — all parameters are plain scalars).
     */
    private McpSchema.Tool createTool() {
        Map<String, Object> properties = new LinkedHashMap<>();
        properties.put("testFolder", Map.of(
                "type", "string",
                "description", "Name of the test folder containing the testcase."
        ));
        properties.put("testcase", Map.of(
                "type", "string",
                "description", "Identifier of the testcase."
        ));
        properties.put("stepId", Map.of(
                "type", "integer",
                "description", "ID of the step containing the action."
        ));
        properties.put("actionId", Map.of(
                "type", "integer",
                "description", "ID of the action containing the control."
        ));
        properties.put("controlId", Map.of(
                "type", "integer",
                "description", "ID of the control to retrieve."
        ));

        return new McpSchema.Tool(
                TOOL_NAME,
                null,
                """
                Retrieves a single control (assertion) from a testcase step action in Cerberus.

                Call this tool whenever the user asks to get, inspect, or display the details of a specific control.

                Use cerberus_testcase_step_action_control_list instead when the controlId is unknown or when listing all controls on an action.
                Do not call this tool when the user asks to create, update, or delete a control.
                """,
                new McpSchema.JsonSchema(
                        "object",
                        properties,
                        List.of("testFolder", "testcase", "stepId", "actionId", "controlId"),
                        null,
                        null,
                        null
                ),
                null,
                MCPToolUtils.readOnlyAnnotations("Get testcase step action control", true),
                null
        );
    }

    /**
     * Handles the MCP tool invocation: validates input, fetches controls for the parent action,
     * filters by controlId, and returns the matching control as a JSON DTO.
     *
     * <p>No direct findByKey service method exists — load the full list then filter in memory.
     *
     * @param args raw MCP argument map extracted from the tool request
     * @return a {@link McpSchema.CallToolResult} with the serialised DTO on success, or an error message
     */
    private McpSchema.CallToolResult execute(Map<String, Object> args) {
        String testFolder = MCPToolUtils.getString(args, "testFolder", "");
        String testcaseId = MCPToolUtils.getString(args, "testcase", "");
        int stepId = MCPToolUtils.getInteger(args, "stepId", 0);
        int actionId = MCPToolUtils.getInteger(args, "actionId", 0);
        int controlId = MCPToolUtils.getInteger(args, "controlId", 0);

        mcpLogUtils.call(TOOL_NAME, "testcase_step_action_control_get",
                String.format("MCP tool %s called with testFolder=%s testcase=%s stepId=%d actionId=%d controlId=%d",
                        TOOL_NAME, testFolder, testcaseId, stepId, actionId, controlId));

        if (testFolder.isBlank()) return MCPToolUtils.errorText("Missing required parameter: testFolder");
        if (testcaseId.isBlank()) return MCPToolUtils.errorText("Missing required parameter: testcase");
        if (stepId <= 0) return MCPToolUtils.errorText("Missing or invalid required parameter: stepId");
        if (actionId <= 0) return MCPToolUtils.errorText("Missing or invalid required parameter: actionId");
        if (controlId <= 0) return MCPToolUtils.errorText("Missing or invalid required parameter: controlId");

        // No direct findByKey service method exists — load the full list then filter in memory.
        AnswerList<TestCaseStepActionControl> answer = testCaseStepActionControlService.readByVarious1(testFolder, testcaseId, stepId, actionId);

        TestCaseStepActionControl control = answer.getDataList().stream()
                .filter(c -> c.getControlId() == controlId)
                .findFirst()
                .orElse(null);

        if (control == null) {
            return MCPToolUtils.errorText("Control does not exist: testFolder=" + testFolder
                    + " testcase=" + testcaseId + " stepId=" + stepId
                    + " actionId=" + actionId + " controlId=" + controlId);
        }

        TestcaseStepActionControlDTOV001 dto = mapper.toDTO(control);
        return MCPToolUtils.successJson(dto);
    }
}
