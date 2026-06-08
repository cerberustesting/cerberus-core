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
import org.cerberus.core.api.dto.testcaseaction.TestcaseStepActionMapperV001;
import org.cerberus.core.crud.entity.TestCaseStep;
import org.cerberus.core.crud.entity.TestCaseStepAction;
import org.cerberus.core.crud.service.ITestCaseStepActionService;
import org.cerberus.core.crud.service.ITestCaseStepService;
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
 * MCP tool that creates a new {@link TestCaseStepAction} inside an existing testcase step.
 *
 * <p>Exposed MCP tool name: {@value TOOL_NAME}.
 *
 * <p>Delegates to {@link ITestCaseStepActionService} for persistence and to
 * {@link ITestCaseStepService} to validate that the target step exists before inserting.
 *
 * <p>The action ID and sort order are auto-assigned by finding the current maximum action ID
 * in the step via {@link ITestCaseStepActionService#getMaxActionId} and adding
 * {@value ACTION_ID_INCREMENT}. This keeps actions spaced out so new actions can be inserted
 * between existing ones without renumbering.
 *
 * <p>The action enum values are hardcoded from {@link TestCaseStepAction} constants rather than
 * loaded from the invariant table, so no startup I/O is performed when the tool schema is built.
 */
@Component
public class CreateTestCaseStepActionTool implements MCPTool {

    private static final String TOOL_NAME = "cerberus_testcase_step_action_create";

    // Gap between consecutive action IDs, leaving room for future insertions between existing actions.
    private static final int ACTION_ID_INCREMENT = 10;

    private final ITestCaseStepService testCaseStepService;
    private final ITestCaseStepActionService testCaseStepActionService;
    private final TestcaseStepActionMapperV001 mapper;
    private final MCPLogUtils mcpLogUtils;

    public CreateTestCaseStepActionTool(ITestCaseStepService testCaseStepService, ITestCaseStepActionService testCaseStepActionService, TestcaseStepActionMapperV001 mapper, MCPLogUtils mcpLogUtils) {
        this.testCaseStepService = testCaseStepService;
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
     * Builds the MCP {@link McpSchema.Tool} descriptor for this tool.
     *
     * <p>The {@code action} enum is sourced from {@link TestCaseStepAction} string constants
     * (static, not from the invariant table), so no database access occurs here and there is
     * no startup-time risk of a missing HTTP request context.
     *
     * @return the fully described tool schema including all input properties and required fields
     */
    private McpSchema.Tool createTool() {
        Map<String, Object> properties = new LinkedHashMap<>();

        properties.put("testFolder", Map.of(
                "type", "string",
                "description", "Name of the test folder containing the testcase."
        ));
        properties.put("testcase", Map.of(
                "type", "string",
                "description", "Identifier of the testcase containing the step."
        ));
        properties.put("stepId", Map.of(
                "type", "integer",
                "description", "ID of the step to add the action to."
        ));
        properties.put("action", Map.of(
                "type", "string",
                "description", """
                        Action to execute. Common values:
                        - callService: call a REST/SOAP service. value1 = service name.
                        - openUrl: open a URL in the browser. value1 = URL.
                        - click: click on an element. value1 = element locator.
                        - type: type text into a field. value1 = element locator, value2 = text.
                        - wait: pause execution. value1 = duration in ms.
                        - executeJS: execute JavaScript. value1 = JS expression.
                        - doNothing: no operation, useful as a placeholder.
                        Use cerberus_invariant_list with type=ACTION to see all available actions.
                        """,
                "enum", List.of(
                        TestCaseStepAction.ACTION_CALLSERVICE,
                        TestCaseStepAction.ACTION_OPENURL,
                        TestCaseStepAction.ACTION_OPENURLWITHBASE,
                        TestCaseStepAction.ACTION_OPENURLLOGIN,
                        TestCaseStepAction.ACTION_CLICK,
                        TestCaseStepAction.ACTION_DOUBLECLICK,
                        TestCaseStepAction.ACTION_RIGHTCLICK,
                        TestCaseStepAction.ACTION_TYPE,
                        TestCaseStepAction.ACTION_CLEARFIELD,
                        TestCaseStepAction.ACTION_SELECT,
                        TestCaseStepAction.ACTION_KEYPRESS,
                        TestCaseStepAction.ACTION_WAIT,
                        TestCaseStepAction.ACTION_WAITVANISH,
                        TestCaseStepAction.ACTION_WAITNETWORKTRAFFICIDLE,
                        TestCaseStepAction.ACTION_EXECUTEJS,
                        TestCaseStepAction.ACTION_EXECUTECOMMAND,
                        TestCaseStepAction.ACTION_EXECUTECERBERUSCOMMAND,
                        TestCaseStepAction.ACTION_SCROLLTO,
                        TestCaseStepAction.ACTION_SWITCHTOWINDOW,
                        TestCaseStepAction.ACTION_FOCUSTOIFRAME,
                        TestCaseStepAction.ACTION_FOCUSDEFAULTIFRAME,
                        TestCaseStepAction.ACTION_REFRESHCURRENTPAGE,
                        TestCaseStepAction.ACTION_CALCULATEPROPERTY,
                        TestCaseStepAction.ACTION_SETCONTENT,
                        TestCaseStepAction.ACTION_SETSERVICECALLCONTENT,
                        TestCaseStepAction.ACTION_DONOTHING,
                        TestCaseStepAction.ACTION_UNKNOWN
                )
        ));
        properties.put("value1", Map.of(
                "type", "string",
                "description", "First value for the action (e.g. element locator, URL, service name, duration). Meaning depends on the action type."
        ));
        properties.put("value2", Map.of(
                "type", "string",
                "description", "Second value for the action (e.g. text to type, expected value). Meaning depends on the action type."
        ));
        properties.put("value3", Map.of(
                "type", "string",
                "description", "Third value for the action. Meaning depends on the action type."
        ));
        properties.put("description", Map.of(
                "type", "string",
                "description", "Optional description of what this action does."
        ));
        properties.put("isFatal", Map.of(
                "type", "boolean",
                "description", "If true, a failure on this action stops the testcase execution. Defaults to true."
        ));

        return new McpSchema.Tool(
                TOOL_NAME,
                null,
                """
                Adds a new action to an existing step in a Cerberus testcase.

                Call this tool whenever the user asks to add an action, instruction, or operation inside a testcase step.
                The action ID and sort order are auto-assigned after existing actions in the step.

                Use cerberus_testcase_step_action_control_create after this tool to add controls (assertions) on this action.

                Do not call this tool when the user only asks to list, read, update, or delete actions.
                """,
                new McpSchema.JsonSchema(
                        "object",
                        properties,
                        List.of("testFolder", "testcase", "stepId", "action"),
                        null,
                        null,
                        null
                ),
                null,
                MCPToolUtils.createAnnotations("Create testcase step action", false),
                null
        );
    }

    /**
     * Validates inputs, resolves the next available action ID, creates the {@link TestCaseStepAction}
     * entity, persists it, and returns the created DTO as JSON.
     *
     * <p>The step's existence is verified before any write so the caller gets a clear error message
     * instead of a DB constraint violation. The new action ID is set to
     * {@code max(existing action IDs) + ACTION_ID_INCREMENT}, falling back to
     * {@value ACTION_ID_INCREMENT} when the step has no actions yet.
     *
     * @param args raw arguments map from the MCP request
     * @return a {@link McpSchema.CallToolResult} containing the created action DTO on success,
     *         or an error description on failure
     */
    private McpSchema.CallToolResult execute(Map<String, Object> args) {
        String testFolder = MCPToolUtils.getString(args, "testFolder", "");
        String testcaseId = MCPToolUtils.getString(args, "testcase", "");
        int stepId = MCPToolUtils.getInteger(args, "stepId", 0);
        String action = MCPToolUtils.getString(args, "action", "");
        String value1 = MCPToolUtils.getString(args, "value1", "");
        String value2 = MCPToolUtils.getString(args, "value2", "");
        String value3 = MCPToolUtils.getString(args, "value3", "");
        String description = MCPToolUtils.getString(args, "description", "");
        boolean isFatal = MCPToolUtils.getBoolean(args, "isFatal", true);

        mcpLogUtils.call(TOOL_NAME, "testcase_step_action_create",
                String.format("MCP tool %s called with testFolder=%s testcase=%s stepId=%d action=%s", TOOL_NAME, testFolder, testcaseId, stepId, action));

        if (testFolder.isBlank()) {
            return MCPToolUtils.errorText("Missing required parameter: testFolder");
        }

        if (testcaseId.isBlank()) {
            return MCPToolUtils.errorText("Missing required parameter: testcase");
        }

        if (stepId <= 0) {
            return MCPToolUtils.errorText("Missing or invalid required parameter: stepId");
        }

        if (action.isBlank()) {
            return MCPToolUtils.errorText("Missing required parameter: action");
        }

        // Verify the target step exists before attempting to insert an action into it.
        TestCaseStep step = testCaseStepService.findTestCaseStep(testFolder, testcaseId, stepId);
        if (step == null) {
            return MCPToolUtils.errorText("Step does not exist: testFolder=" + testFolder + " testcase=" + testcaseId + " stepId=" + stepId);
        }

        // No direct findByKey service method exists — load the full list then filter in memory.
        AnswerList<TestCaseStepAction> existingActions = testCaseStepActionService.readByVarious1WithDependency(testFolder, testcaseId, stepId);
        List<TestCaseStepAction> actionList = existingActions.getDataList()
                .stream()
                .map(TestCaseStepAction.class::cast)
                .toList();

        // Derive the next action ID so new actions are always appended after existing ones.
        int nextActionId = actionList.isEmpty()
                ? ACTION_ID_INCREMENT
                : testCaseStepActionService.getMaxActionId(actionList) + ACTION_ID_INCREMENT;

        TestCaseStepAction newAction = TestCaseStepAction.builder()
                .test(testFolder)
                .testcase(testcaseId)
                .stepId(stepId)
                .actionId(nextActionId)
                // sort mirrors actionId so the UI displays actions in creation order by default.
                .sort(nextActionId)
                .action(action)
                .value1(value1)
                .value2(value2)
                .value3(value3)
                .description(description)
                .isFatal(isFatal)
                .doScreenshotBefore(false)
                .doScreenshotAfter(false)
                .waitBefore(0)
                .waitAfter(0)
                .usrCreated("MCP")
                .build();

        Answer answer = testCaseStepActionService.create(newAction);

        if (!answer.isCodeStringEquals("OK")) {
            return MCPToolUtils.errorText("Unable to create action: " + answer.getMessageDescription());
        }

        return MCPToolUtils.successJson(Map.of(
                "status", "created",
                "action", mapper.toDTO(newAction)
        ));
    }

}
