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
import org.cerberus.core.api.dto.testcasecontrol.TestcaseStepActionControlMapperV001;
import org.cerberus.core.crud.entity.TestCaseStepAction;
import org.cerberus.core.crud.entity.TestCaseStepActionControl;
import org.cerberus.core.crud.service.ITestCaseStepActionControlService;
import org.cerberus.core.crud.service.ITestCaseStepActionService;
import org.cerberus.core.mcp.MCPTool;
import org.cerberus.core.mcp.util.MCPLogUtils;
import org.cerberus.core.mcp.util.MCPToolUtils;
import org.cerberus.core.util.answer.Answer;
import org.cerberus.core.util.answer.AnswerList;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * MCP tool that creates a {@link TestCaseStepActionControl} (assertion) and attaches it
 * to an existing action within a testcase step.
 *
 * <p>Exposes the MCP tool named {@code cerberus_testcase_step_action_control_create}.
 *
 * <p>Delegates persistence to {@link ITestCaseStepActionControlService} and uses
 * {@link ITestCaseStepActionService} only to verify that the target action exists before
 * creating the control.
 *
 * <p>The control type enum is provided as a static list of {@link TestCaseStepActionControl}
 * constants embedded directly in the tool schema (not loaded from the invariant table at
 * startup), so schema registration is safe even before an HTTP request context is available.
 */
@Component
public class CreateTestCaseStepActionControlTool implements MCPTool {

    private static final String TOOL_NAME = "cerberus_testcase_step_action_control_create";

    private static final int CONTROL_ID_INCREMENT = 10;

    private final ITestCaseStepActionService testCaseStepActionService;
    private final ITestCaseStepActionControlService testCaseStepActionControlService;
    private final TestcaseStepActionControlMapperV001 mapper;
    private final MCPLogUtils mcpLogUtils;

    public CreateTestCaseStepActionControlTool(ITestCaseStepActionService testCaseStepActionService, ITestCaseStepActionControlService testCaseStepActionControlService, TestcaseStepActionControlMapperV001 mapper, MCPLogUtils mcpLogUtils) {
        this.testCaseStepActionService = testCaseStepActionService;
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
     * <p>The {@code control} enum is populated from static {@link TestCaseStepActionControl}
     * constants (not from the invariant table), so this method is safe to call at Spring
     * startup without an active HTTP request context.
     *
     * <p>Required input parameters: {@code testFolder}, {@code testcase}, {@code stepId},
     * {@code actionId}, {@code control}, and {@code value1}.
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
                "description", "ID of the action to attach this control to."
        ));
        properties.put("control", Map.of(
                "type", "string",
                "description", """
                        Assertion to verify. Common values:
                        - verifyStringEqual: check value1 equals value2.
                        - verifyStringContains: check value1 contains value2.
                        - verifyStringDifferent: check value1 differs from value2.
                        - verifyNumericEquals / verifyNumericGreater / verifyNumericMinor: numeric comparisons.
                        - verifyElementPresent / verifyElementNotPresent: check element existence.
                        - verifyElementVisible / verifyElementNotVisible: check element visibility.
                        - verifyElementTextEqual: check element text equals value2.
                        - verifyElementTextContains: check element text contains value2.
                        Use cerberus_invariant_list with type=CONTROL to see all available controls.
                        """,
                "enum", List.of(
                        TestCaseStepActionControl.CONTROL_VERIFYSTRINGEQUAL,
                        TestCaseStepActionControl.CONTROL_VERIFYSTRINGDIFFERENT,
                        TestCaseStepActionControl.CONTROL_VERIFYSTRINGCONTAINS,
                        TestCaseStepActionControl.CONTROL_VERIFYSTRINGNOTCONTAINS,
                        TestCaseStepActionControl.CONTROL_VERIFYSTRINGGREATER,
                        TestCaseStepActionControl.CONTROL_VERIFYSTRINGMINOR,
                        TestCaseStepActionControl.CONTROL_VERIFYNUMERICEQUALS,
                        TestCaseStepActionControl.CONTROL_VERIFYNUMERICDIFFERENT,
                        TestCaseStepActionControl.CONTROL_VERIFYNUMERICGREATER,
                        TestCaseStepActionControl.CONTROL_VERIFYNUMERICGREATEROREQUAL,
                        TestCaseStepActionControl.CONTROL_VERIFYNUMERICMINOR,
                        TestCaseStepActionControl.CONTROL_VERIFYNUMERICMINOROREQUAL,
                        TestCaseStepActionControl.CONTROL_VERIFYELEMENTPRESENT,
                        TestCaseStepActionControl.CONTROL_VERIFYELEMENTNOTPRESENT,
                        TestCaseStepActionControl.CONTROL_VERIFYELEMENTVISIBLE,
                        TestCaseStepActionControl.CONTROL_VERIFYELEMENTNOTVISIBLE,
                        TestCaseStepActionControl.CONTROL_VERIFYELEMENTTEXTEQUAL,
                        TestCaseStepActionControl.CONTROL_VERIFYELEMENTTEXTDIFFERENT,
                        TestCaseStepActionControl.CONTROL_VERIFYELEMENTTEXTCONTAINS,
                        TestCaseStepActionControl.CONTROL_VERIFYELEMENTTEXTNOTCONTAINS,
                        TestCaseStepActionControl.CONTROL_VERIFYELEMENTTEXTMATCHREGEX,
                        TestCaseStepActionControl.CONTROL_VERIFYELEMENTCHECKED,
                        TestCaseStepActionControl.CONTROL_VERIFYELEMENTNOTCHECKED,
                        TestCaseStepActionControl.CONTROL_VERIFYELEMENTCLICKABLE,
                        TestCaseStepActionControl.CONTROL_VERIFYELEMENTNOTCLICKABLE,
                        TestCaseStepActionControl.CONTROL_VERIFYELEMENTEQUALS,
                        TestCaseStepActionControl.CONTROL_VERIFYELEMENTDIFFERENT,
                        TestCaseStepActionControl.CONTROL_VERIFYSTRINGARRAYCONTAINS,
                        TestCaseStepActionControl.CONTROL_VERIFYNUMERICARRAYCONTAINS,
                        TestCaseStepActionControl.CONTROL_VERIFYELEMENTTEXTARRAYCONTAINS,
                        TestCaseStepActionControl.CONTROL_UNKNOWN
                )
        ));
        properties.put("value1", Map.of(
                "type", "string",
                "description", "First value for the control (e.g. element locator, actual value, property name). Meaning depends on the control type."
        ));
        properties.put("value2", Map.of(
                "type", "string",
                "description", "Second value for the control (e.g. expected value). Meaning depends on the control type."
        ));
        properties.put("value3", Map.of(
                "type", "string",
                "description", "Third value for the control. Meaning depends on the control type."
        ));
        properties.put("description", Map.of(
                "type", "string",
                "description", "Optional description of what this control verifies."
        ));
        properties.put("isFatal", Map.of(
                "type", "boolean",
                "description", "If true, a failure on this control stops the testcase execution. Defaults to true."
        ));

        return new McpSchema.Tool(
                TOOL_NAME,
                null,
                """
                Adds a control (assertion) to an existing action in a Cerberus testcase step.

                Call this tool whenever the user asks to add a verification, assertion, or check on an action result.
                The control ID and sort order are auto-assigned after existing controls on the action.

                Controls are used to verify outcomes: HTTP status codes, response body values, element presence, text content, etc.

                Do not call this tool when the user only asks to list, read, update, or delete controls.
                """,
                new McpSchema.JsonSchema(
                        "object",
                        properties,
                        List.of("testFolder", "testcase", "stepId", "actionId", "control", "value1"),
                        null,
                        null,
                        null
                ),
                null,
                MCPToolUtils.createAnnotations("Create testcase step action control", false),
                null
        );
    }

    /**
     * Handles the MCP tool invocation: validates inputs, verifies the parent action exists,
     * computes the next available control ID, and persists the new control via the service.
     *
     * <p>The new control's {@code controlId} and {@code sort} are both set to
     * {@code maxExistingControlId + CONTROL_ID_INCREMENT} so controls retain a stable
     * ordering with gaps that allow future insertions.
     *
     * @param args raw MCP arguments map extracted from the incoming request
     * @return a {@link McpSchema.CallToolResult} containing the created control DTO on
     *         success, or an error message on validation/persistence failure
     */
    private McpSchema.CallToolResult execute(Map<String, Object> args) {
        String testFolder = MCPToolUtils.getString(args, "testFolder", "");
        String testcaseId = MCPToolUtils.getString(args, "testcase", "");
        int stepId = MCPToolUtils.getInteger(args, "stepId", 0);
        int actionId = MCPToolUtils.getInteger(args, "actionId", 0);
        String control = MCPToolUtils.getString(args, "control", "");
        String value1 = MCPToolUtils.getString(args, "value1", "");
        String value2 = MCPToolUtils.getString(args, "value2", "");
        String value3 = MCPToolUtils.getString(args, "value3", "");
        String description = MCPToolUtils.getString(args, "description", "");
        boolean isFatal = MCPToolUtils.getBoolean(args, "isFatal", true);

        mcpLogUtils.call(TOOL_NAME, "testcase_step_action_control_create",
                String.format("MCP tool %s called with testFolder=%s testcase=%s stepId=%d actionId=%d control=%s",
                        TOOL_NAME, testFolder, testcaseId, stepId, actionId, control));

        if (testFolder.isBlank()) {
            return MCPToolUtils.errorText("Missing required parameter: testFolder");
        }

        if (testcaseId.isBlank()) {
            return MCPToolUtils.errorText("Missing required parameter: testcase");
        }

        if (stepId <= 0) {
            return MCPToolUtils.errorText("Missing or invalid required parameter: stepId");
        }

        if (actionId <= 0) {
            return MCPToolUtils.errorText("Missing or invalid required parameter: actionId");
        }

        if (control.isBlank()) {
            return MCPToolUtils.errorText("Missing required parameter: control");
        }

        if (value1.isBlank()) {
            return MCPToolUtils.errorText("Missing required parameter: value1");
        }

        // No direct findByKey service method exists — load the full list then filter in memory.
        AnswerList<TestCaseStepAction> actions = testCaseStepActionService.readByVarious1WithDependency(testFolder, testcaseId, stepId);
        boolean actionExists = actions.getDataList().stream()
                .map(TestCaseStepAction.class::cast)
                .anyMatch(a -> a.getActionId() == actionId);

        if (!actionExists) {
            return MCPToolUtils.errorText("Action does not exist: testFolder=" + testFolder + " testcase=" + testcaseId + " stepId=" + stepId + " actionId=" + actionId);
        }

        AnswerList<TestCaseStepActionControl> existingControls = testCaseStepActionControlService.readByVarious1(testFolder, testcaseId, stepId, actionId);
        List<TestCaseStepActionControl> controlList = existingControls.getDataList()
                .stream()
                .map(TestCaseStepActionControl.class::cast)
                .toList();

        // Auto-assign controlId after the current maximum, leaving gaps for future insertions.
        int nextControlId = controlList.isEmpty()
                ? CONTROL_ID_INCREMENT
                : testCaseStepActionControlService.getMaxControlId(controlList) + CONTROL_ID_INCREMENT;

        TestCaseStepActionControl newControl = TestCaseStepActionControl.builder()
                .test(testFolder)
                .testcase(testcaseId)
                .stepId(stepId)
                .actionId(actionId)
                .controlId(nextControlId)
                .sort(nextControlId)
                .control(control)
                .value1(value1)
                .value2(value2)
                .value3(value3)
                .description(description)
                // Empty condition operator is treated as "always" at execution time, but setting it
                // explicitly avoids the field showing up blank in the UI.
                .conditionOperator("always")
                .isFatal(isFatal)
                .doScreenshotBefore(false)
                .doScreenshotAfter(false)
                .waitBefore(0)
                .waitAfter(0)
                .usrCreated("MCP")
                .build();

        Answer answer = testCaseStepActionControlService.create(newControl);

        if (!answer.isCodeStringEquals("OK")) {
            return MCPToolUtils.errorText("Unable to create control: " + answer.getMessageDescription());
        }

        return MCPToolUtils.successJson(Map.of(
                "status", "created",
                "control", mapper.toDTO(newControl)
        ));
    }

}
