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
import org.cerberus.core.exception.CerberusException;
import org.cerberus.core.mcp.MCPTool;
import org.cerberus.core.mcp.util.MCPLogUtils;
import org.cerberus.core.mcp.util.MCPToolUtils;
import org.cerberus.core.util.answer.AnswerList;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * MCP tool that deletes a {@link TestCaseStepActionControl} (assertion/control) from a
 * testcase step action in Cerberus.
 *
 * <p>Exposes the MCP tool named {@value #TOOL_NAME}.</p>
 *
 * <p>Delegates persistence operations to {@link ITestCaseStepActionControlService}.
 * The mapper {@link TestcaseStepActionControlMapperV001} is used to capture a DTO snapshot
 * of the entity before it is removed from the database.</p>
 *
 * <p>No schema enum values are loaded from the invariant table or static JSON at startup;
 * all input parameters are plain scalars supplied by the MCP client at call time.</p>
 */
@Component
public class DeleteTestCaseStepActionControlTool implements MCPTool {

    private static final String TOOL_NAME = "cerberus_testcase_step_action_control_delete";

    private final ITestCaseStepActionControlService testCaseStepActionControlService;
    private final TestcaseStepActionControlMapperV001 mapper;
    private final MCPLogUtils mcpLogUtils;

    public DeleteTestCaseStepActionControlTool(ITestCaseStepActionControlService testCaseStepActionControlService, TestcaseStepActionControlMapperV001 mapper, MCPLogUtils mcpLogUtils) {
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
     * Builds the {@link McpSchema.Tool} descriptor for this MCP tool.
     *
     * <p>Input schema is defined statically (no invariant-table or static-JSON enum loading
     * at startup), so there is no startup-time risk from missing HTTP request context.</p>
     *
     * <p>All five parameters — {@code testFolder}, {@code testcase}, {@code stepId},
     * {@code actionId}, and {@code controlId} — are required.</p>
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
                "description", "ID of the control to delete."
        ));

        return new McpSchema.Tool(
                TOOL_NAME,
                null,
                """
                Deletes an existing control (assertion) from a testcase step action in Cerberus.

                Call this tool whenever the user asks to delete or remove a control from an action.
                Before calling this tool, confirm the control to delete with the user.

                Do not call this tool when the user only asks to list, read, create, or update controls.
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
                MCPToolUtils.deleteAnnotations("Delete testcase step action control", false),
                null
        );
    }

    /**
     * Handles the MCP tool invocation: validates inputs, locates the control, and deletes it.
     *
     * <p>The control is looked up via {@code readByVarious1} and filtered in memory by
     * {@code controlId}.</p>
     * <p>On success, returns the deleted control as a DTO together with a {@code "deleted"} status.</p>
     *
     * @param args raw MCP arguments map supplied by the client
     * @return a {@link McpSchema.CallToolResult} containing the deleted DTO or an error message
     */
    private McpSchema.CallToolResult execute(Map<String, Object> args) {
        String testFolder = MCPToolUtils.getString(args, "testFolder", "");
        String testcaseId = MCPToolUtils.getString(args, "testcase", "");
        int stepId = MCPToolUtils.getInteger(args, "stepId", 0);
        int actionId = MCPToolUtils.getInteger(args, "actionId", 0);
        int controlId = MCPToolUtils.getInteger(args, "controlId", 0);

        mcpLogUtils.call(TOOL_NAME, "testcase_step_action_control_delete",
                String.format("MCP tool %s called with testFolder=%s testcase=%s stepId=%d actionId=%d controlId=%d",
                        TOOL_NAME, testFolder, testcaseId, stepId, actionId, controlId));

        if (testFolder.isBlank()) return MCPToolUtils.errorText("Missing required parameter: testFolder");
        if (testcaseId.isBlank()) return MCPToolUtils.errorText("Missing required parameter: testcase");
        if (stepId <= 0) return MCPToolUtils.errorText("Missing or invalid required parameter: stepId");
        if (actionId <= 0) return MCPToolUtils.errorText("Missing or invalid required parameter: actionId");
        if (controlId <= 0) return MCPToolUtils.errorText("Missing or invalid required parameter: controlId");

        // No direct findByKey service method exists — load the full list then filter in memory.
        AnswerList<TestCaseStepActionControl> answer = testCaseStepActionControlService.readByVarious1(testFolder, testcaseId, stepId, actionId);

        TestCaseStepActionControl existing = answer.getDataList().stream()
                .filter(c -> c.getControlId() == controlId)
                .findFirst()
                .orElse(null);

        if (existing == null) {
            return MCPToolUtils.errorText("Control does not exist: testFolder=" + testFolder
                    + " testcase=" + testcaseId + " stepId=" + stepId
                    + " actionId=" + actionId + " controlId=" + controlId);
        }

        // Snapshot DTO before deletion — the entity is gone after the service call.
        TestcaseStepActionControlDTOV001 dto = mapper.toDTO(existing);

        try {
            // These service methods throw CerberusException on failure rather than returning an Answer.
            testCaseStepActionControlService.delete(existing);
        } catch (CerberusException e) {
            return MCPToolUtils.errorText("Unable to delete control: " + e.getMessage());
        }

        return MCPToolUtils.successJson(Map.of(
                "status", "deleted",
                "control", dto
        ));
    }

}
