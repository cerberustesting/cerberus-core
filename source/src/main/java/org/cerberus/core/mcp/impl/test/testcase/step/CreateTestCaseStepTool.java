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
package org.cerberus.core.mcp.impl.test.testcase.step;

import io.modelcontextprotocol.server.McpServerFeatures;
import io.modelcontextprotocol.spec.McpSchema;
import org.cerberus.core.api.dto.testcasestep.TestcaseStepMapperV001;
import org.cerberus.core.crud.entity.TestCase;
import org.cerberus.core.crud.entity.TestCaseStep;
import org.cerberus.core.crud.service.ITestCaseService;
import org.cerberus.core.crud.service.ITestCaseStepService;
import org.cerberus.core.mcp.MCPTool;
import org.cerberus.core.mcp.util.MCPLogUtils;
import org.cerberus.core.mcp.util.MCPOrderingService;
import org.cerberus.core.mcp.util.MCPToolUtils;
import org.cerberus.core.util.answer.Answer;
import org.cerberus.core.util.answer.AnswerItem;
import org.cerberus.core.util.answer.AnswerList;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * MCP tool that creates a new {@link TestCaseStep} inside an existing testcase.
 *
 * <p>Exposed MCP tool name: {@value TOOL_NAME}
 *
 * <p>Delegates to {@link ITestCaseService} to verify that the parent testcase
 * exists, and to {@link ITestCaseStepService} to fetch existing steps (for
 * step-ID auto-increment) and to persist the new step.
 *
 * <p>The loop-mode enum values are sourced from {@link TestCaseStep} constants
 * rather than the invariant table, so no startup-time invariant loading is
 * required and there is no risk of missing HTTP context during initialisation.
 *
 * <p>{@code libraryStepStepId} is always set to {@code 0} (not {@code null}):
 * // Must be 0 not null: DAO does getLibraryStepStepId() >= 0 which auto-unboxes the Integer — null causes NullPointerException.
 */
@Component
public class CreateTestCaseStepTool implements MCPTool {

    private static final String TOOL_NAME = "cerberus_testcase_step_create";

    private static final int STEP_ID_INCREMENT = 10;

    private final ITestCaseService testCaseService;
    private final ITestCaseStepService testCaseStepService;
    private final TestcaseStepMapperV001 mapper;
    private final MCPLogUtils mcpLogUtils;
    private final MCPOrderingService orderingService;

    public CreateTestCaseStepTool(ITestCaseService testCaseService, ITestCaseStepService testCaseStepService, TestcaseStepMapperV001 mapper, MCPLogUtils mcpLogUtils, MCPOrderingService orderingService) {
        this.testCaseService = testCaseService;
        this.testCaseStepService = testCaseStepService;
        this.mapper = mapper;
        this.mcpLogUtils = mcpLogUtils;
        this.orderingService = orderingService;
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
     * Builds the {@link McpSchema.Tool} descriptor registered with the MCP server.
     *
     * <p>The {@code loop} enum values are sourced directly from {@link TestCaseStep}
     * constants (not the invariant table), so no database call is needed at startup
     * and there is no risk of missing HTTP/request context during initialisation.
     *
     * @return the fully-described tool schema including parameter definitions and annotations
     */
    private McpSchema.Tool createTool() {
        Map<String, Object> properties = Map.of(
                "testFolder", Map.of(
                        "type", "string",
                        "description", "Name of the test folder containing the testcase."
                ),
                "testcase", Map.of(
                        "type", "string",
                        "description", "Identifier of the testcase to add the step to."
                ),
                "description", Map.of(
                        "type", "string",
                        "description", "Description of what this step does."
                ),
                "loop", Map.of(
                        "type", "string",
                        "description", "Execution loop mode for this step. Defaults to onceIfConditionTrue.",
                        "enum", List.of(
                                TestCaseStep.LOOP_ONCEIFCONDITIONTRUE,
                                TestCaseStep.LOOP_ONCEIFCONDITIONFALSE,
                                TestCaseStep.LOOP_DOWHILECONDITIONTRUE,
                                TestCaseStep.LOOP_DOWHILECONDITIONFALSE,
                                TestCaseStep.LOOP_WHILECONDITIONTRUEDO,
                                TestCaseStep.LOOP_WHILECONDITIONFALSEDO
                        )
                ),
                "isLibraryStep", Map.of(
                        "type", "boolean",
                        "description", "Set to true if this step is a reusable library step callable by other testcases. Defaults to false."
                ),
                "position", Map.of(
                        "type", "integer",
                        "description", "Where the step goes in the testcase, 1 being first. Omit it to append at "
                                + "the end. Use it whenever the step belongs somewhere other than last — a setup "
                                + "step the rest of the scenario depends on has to run before them. The steps "
                                + "already there shift down and keep their own actions, controls and properties."
                )
        );

        return new McpSchema.Tool(
                TOOL_NAME,
                null,
                """
                Adds a new step to an existing testcase in Cerberus.

                Call this tool whenever the user asks to add a step to a testcase.
                The step ID is auto-assigned. The step is appended at the end unless you pass position, which
                inserts it there instead — use cerberus_testcase_step_reorder to move a step that already exists.

                This tool creates a step with its own actions. To make the new step run an existing library
                step instead of actions of its own, use cerberus_testcase_step_library_use.

                Use cerberus_testcase_step_action_create after this tool to add actions inside the step.

                Do not call this tool when the user only asks to list, read, update, or delete steps.
                """,
                new McpSchema.JsonSchema(
                        "object",
                        properties,
                        List.of("testFolder", "testcase", "description"),
                        null,
                        null,
                        null
                ),
                null,
                MCPToolUtils.createAnnotations("Create testcase step", false),
                null
        );
    }

    /**
     * Validates arguments, resolves the next available step ID, creates the
     * {@link TestCaseStep} entity, persists it, and returns a JSON result.
     *
     * <p>Step ID and sort order are both set to {@code maxExistingStepId + STEP_ID_INCREMENT}
     * so new steps are appended at the end with a gap that allows future manual reordering.
     *
     * @param args the raw MCP request arguments map
     * @return a {@link McpSchema.CallToolResult} containing either the created step DTO or an error message
     */
    private McpSchema.CallToolResult execute(Map<String, Object> args) {
        String testFolder = MCPToolUtils.getString(args, "testFolder", "");
        String testcaseId = MCPToolUtils.getString(args, "testcase", "");
        String description = MCPToolUtils.getString(args, "description", "");
        String loop = MCPToolUtils.getString(args, "loop", TestCaseStep.LOOP_ONCEIFCONDITIONTRUE);
        boolean isLibraryStep = MCPToolUtils.getBoolean(args, "isLibraryStep", false);

        mcpLogUtils.call(TOOL_NAME, "testcase_step_create", String.format("MCP tool %s called with testFolder=%s testcase=%s", TOOL_NAME, testFolder, testcaseId));

        if (testFolder.isBlank()) {
            return MCPToolUtils.errorText("Missing required parameter: testFolder");
        }

        if (testcaseId.isBlank()) {
            return MCPToolUtils.errorText("Missing required parameter: testcase");
        }

        if (description.isBlank()) {
            return MCPToolUtils.errorText("Missing required parameter: description");
        }

        AnswerItem<TestCase> testCaseAnswer = testCaseService.readByKey(testFolder, testcaseId);
        if (!testCaseAnswer.isCodeStringEquals("OK") || testCaseAnswer.getItem() == null) {
            return MCPToolUtils.errorText("Testcase does not exist: testFolder=" + testFolder + " testcase=" + testcaseId);
        }

        AnswerList<TestCaseStep> stepsAnswer = testCaseStepService.readByTestTestCase(testFolder, testcaseId);
        List<TestCaseStep> existingSteps = stepsAnswer.getDataList();
        // Derive next step ID by incrementing beyond the current maximum, keeping gaps for reordering.
        int nextStepId = existingSteps == null || existingSteps.isEmpty()
                ? STEP_ID_INCREMENT
                : testCaseStepService.getMaxStepId(existingSteps) + STEP_ID_INCREMENT;

        TestCaseStep step = TestCaseStep.builder()
                .test(testFolder)
                .testcase(testcaseId)
                .stepId(nextStepId)
                .sort(nextStepId)
                .description(description)
                .loop(loop)
                // Empty condition operator is treated as "always" at execution time, but setting it
                // explicitly avoids the field showing up blank in the UI.
                .conditionOperator("always")
                .isLibraryStep(isLibraryStep)
                .isUsingLibraryStep(false)
                // Must be 0 not null: DAO does getLibraryStepStepId() >= 0 which auto-unboxes the Integer — null causes NullPointerException.
                .libraryStepStepId(0)
                .isExecutionForced(false)
                .usrCreated("MCP")
                .build();

        Answer answer = testCaseStepService.create(step);

        if (!answer.isCodeStringEquals("OK")) {
            return MCPToolUtils.errorText("Unable to create step: " + answer.getMessageDescription());
        }

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("status", "created");
        response.put("step", mapper.toDTO(step));

        // The row is always written at the end first, then moved: creating it in place would mean
        // renumbering the rest before it exists, which leaves the testcase in a broken order if the
        // insert then fails.
        int position = MCPToolUtils.getInteger(args, "position", 0);
        if (position > 0) {
            MCPOrderingService.Result placement =
                    orderingService.placeStep(testFolder, testcaseId, nextStepId, position);
            if (placement.failed()) {
                // The step exists; only its position is wrong. Saying so — rather than reporting a
                // failure — is what stops the caller from creating it a second time.
                response.put("status", "created_but_not_positioned");
                response.put("warning", "The step was created at the end of the testcase but could not be "
                        + "moved to position " + position + ": " + placement.error()
                        + " Use cerberus_testcase_step_reorder to place it.");
            } else {
                response.put("stepIds", placement.order());
            }
        }

        return MCPToolUtils.successJson(response);
    }

}
