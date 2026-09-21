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
import org.cerberus.core.crud.entity.TestCaseStepAction;
import org.cerberus.core.crud.entity.TestCaseStepActionControl;
import org.cerberus.core.crud.service.ITestCaseStepActionControlService;
import org.cerberus.core.crud.service.ITestCaseStepActionService;
import org.cerberus.core.mcp.MCPTool;
import org.cerberus.core.mcp.util.MCPLogUtils;
import org.cerberus.core.mcp.util.MCPOrderingService;
import org.cerberus.core.mcp.util.MCPToolUtils;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * MCP tool that moves a control from one action to another, under the tool name
 * {@code cerberus_testcase_step_action_control_move}.
 *
 * <p>A control runs immediately after the action it hangs from, so attaching it to the wrong one
 * does not make it fail loudly — it makes it check too early. "The confirmation message is visible"
 * hung on the click that opens a form, rather than the click that submits it, fails on every run
 * while looking like a broken assertion rather than a misplaced one.</p>
 *
 * <p>Mechanically this is the action move one level down: the primary key
 * {@code (test, testcase, stepId, actionId, controlId)} is rewritten in a single update. It is
 * simpler than moving an action, because nothing in the database references a control — it is a
 * leaf, and moves alone.</p>
 */
@Component
public class MoveTestCaseStepActionControlTool implements MCPTool {

    private static final String TOOL_NAME = "cerberus_testcase_step_action_control_move";

    /** Control ids advance in tens, as everywhere else. */
    private static final int CONTROL_ID_INCREMENT = 10;

    private final ITestCaseStepActionService testCaseStepActionService;
    private final ITestCaseStepActionControlService testCaseStepActionControlService;
    private final MCPOrderingService orderingService;
    private final MCPLogUtils mcpLogUtils;

    public MoveTestCaseStepActionControlTool(ITestCaseStepActionService testCaseStepActionService,
                                             ITestCaseStepActionControlService testCaseStepActionControlService,
                                             MCPOrderingService orderingService,
                                             MCPLogUtils mcpLogUtils) {
        this.testCaseStepActionService = testCaseStepActionService;
        this.testCaseStepActionControlService = testCaseStepActionControlService;
        this.orderingService = orderingService;
        this.mcpLogUtils = mcpLogUtils;
    }

    @Override
    public McpServerFeatures.SyncToolSpecification toToolSpecification() {
        return new McpServerFeatures.SyncToolSpecification(
                createTool(),
                (exchange, request) -> execute(MCPToolUtils.argumentsOrEmpty(request.arguments()))
        );
    }

    private McpSchema.Tool createTool() {
        Map<String, Object> properties = new LinkedHashMap<>();
        properties.put("testFolder", Map.of(
                "type", "string",
                "description", "Test folder containing the testcase."
        ));
        properties.put("testcase", Map.of(
                "type", "string",
                "description", "Testcase containing both actions."
        ));
        properties.put("stepId", Map.of(
                "type", "integer",
                "description", "Step the control is in today."
        ));
        properties.put("actionId", Map.of(
                "type", "integer",
                "description", "Action the control hangs from today."
        ));
        properties.put("controlId", Map.of(
                "type", "integer",
                "description", "Control to move."
        ));
        properties.put("targetStepId", Map.of(
                "type", "integer",
                "description", "Step holding the destination action. Defaults to the control's current step, "
                        + "which is the usual case — moving a check onto the neighbouring action."
        ));
        properties.put("targetActionId", Map.of(
                "type", "integer",
                "description", "Action to hang the control from."
        ));
        properties.put("position", Map.of(
                "type", "integer",
                "description", "Where it lands among that action's controls, 1 being first. Omit it to append "
                        + "at the end. Order matters: a control marked fatal ends the testcase where it sits."
        ));

        return new McpSchema.Tool(
                TOOL_NAME,
                null,
                """
                Moves a control from one action to another, inside the same testcase.

                Use it when a check is hanging from the wrong action. That does not fail loudly — it makes the
                check run at the wrong moment, usually too early, so it reads as a broken assertion rather
                than a misplaced one. Moving it keeps its type, its values, its condition and its options.

                targetStepId defaults to the control's current step, so moving a check onto the neighbouring
                action only needs targetActionId.

                The control takes a new control id on the destination action when its own is already used
                there; the answer says so.

                To change a control's position on the action it already belongs to, use
                cerberus_testcase_step_action_control_reorder instead.
                """,
                new McpSchema.JsonSchema(
                        "object",
                        properties,
                        List.of("testFolder", "testcase", "stepId", "actionId", "controlId", "targetActionId"),
                        null,
                        null,
                        null
                ),
                // Declared so a client can validate the result and generate a typed call for it.
                // Only the always-present keys are required; the schema stays open, so an optional
                // diagnostic added later does not make a valid result fail validation.
                Map.of(
                        "type", "object",
                        "properties", Map.of(
                                "status", Map.of("type", "string"),
                                "testFolder", Map.of("type", "string"),
                                "testcase", Map.of("type", "string"),
                                "from", Map.of("type", "object"),
                                "to", Map.of("type", "object")),
                        "required", List.of("status", "from", "to")),
                MCPToolUtils.updateAnnotations("Move a control to another action", false),
                null
        );
    }

    private McpSchema.CallToolResult execute(Map<String, Object> args) {
        String testFolder = MCPToolUtils.getString(args, "testFolder", "").trim();
        String testcase = MCPToolUtils.getString(args, "testcase", "").trim();
        // 0 is a legitimate step, action and control id — Cerberus assigns these within their parent
        // rather than from a sequence, so the sentinel for "not supplied" has to be negative.
        int stepId = MCPToolUtils.getInteger(args, "stepId", -1);
        int actionId = MCPToolUtils.getInteger(args, "actionId", -1);
        int controlId = MCPToolUtils.getInteger(args, "controlId", -1);
        int targetActionId = MCPToolUtils.getInteger(args, "targetActionId", -1);
        int targetStepId = MCPToolUtils.getInteger(args, "targetStepId", stepId);
        int position = MCPToolUtils.getInteger(args, "position", 0);

        mcpLogUtils.call(TOOL_NAME, "testcase_step_action_control_move",
                String.format("MCP tool %s called with %s/%s control %d of step %d action %d to step %d action %d",
                        TOOL_NAME, testFolder, testcase, controlId, stepId, actionId, targetStepId, targetActionId));

        if (testFolder.isBlank()) {
            return MCPToolUtils.errorText("Missing required parameter: testFolder");
        }
        if (testcase.isBlank()) {
            return MCPToolUtils.errorText("Missing required parameter: testcase");
        }
        if (stepId < 0 || actionId < 0 || controlId < 0 || targetActionId < 0) {
            return MCPToolUtils.errorText("stepId, actionId, controlId and targetActionId are all required, as "
                    + "whole numbers. Call cerberus_testcase_scenario_get to read them.");
        }
        if (targetStepId == stepId && targetActionId == actionId) {
            return MCPToolUtils.errorText("The control already hangs from action " + actionId + " of step "
                    + stepId + ". To change its position there, use "
                    + "cerberus_testcase_step_action_control_reorder.");
        }

        TestCaseStepActionControl control = testCaseStepActionControlService
                .findTestCaseStepActionControlByKey(testFolder, testcase, stepId, actionId, controlId);
        if (control == null) {
            return MCPToolUtils.errorText("Control does not exist: testFolder=" + testFolder + " testcase="
                    + testcase + " stepId=" + stepId + " actionId=" + actionId + " controlId=" + controlId);
        }

        TestCaseStepAction target = testCaseStepActionService
                .findTestCaseStepActionbyKey(testFolder, testcase, targetStepId, targetActionId);
        if (target == null) {
            return MCPToolUtils.errorText("Destination action does not exist: testFolder=" + testFolder
                    + " testcase=" + testcase + " stepId=" + targetStepId + " actionId=" + targetActionId
                    + ". A control can only move inside its own testcase.");
        }

        List<TestCaseStepActionControl> targetControls = testCaseStepActionControlService
                .findControlByTestTestCaseStepIdActionId(testFolder, testcase, targetStepId, targetActionId);
        // The primary key is (test, testcase, stepId, actionId, controlId), so the control keeps its
        // own id only when nothing on the destination action already uses it.
        boolean idTaken = targetControls != null && targetControls.stream()
                .anyMatch(existing -> existing.getControlId() == controlId);
        int newControlId = idTaken
                ? testCaseStepActionControlService.getMaxControlId(targetControls) + CONTROL_ID_INCREMENT
                : controlId;

        // Lands at the end first; the placement below gives it the requested position. In that order,
        // a failure to place leaves the control attached and running, not lost.
        int landingSort = targetControls == null ? 1 : targetControls.size() + 1;

        if (!testCaseStepActionControlService.moveTestCaseStepActionControlToAction(testFolder, testcase,
                stepId, actionId, controlId, targetStepId, targetActionId, newControlId, landingSort, "MCP")) {
            return MCPToolUtils.errorText("The control could not be moved. It still hangs from action "
                    + actionId + " of step " + stepId + "; read it back with cerberus_testcase_scenario_get "
                    + "before trying anything else.");
        }

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("status", "moved");
        response.put("testFolder", testFolder);
        response.put("testcase", testcase);
        response.put("from", Map.of("stepId", stepId, "actionId", actionId, "controlId", controlId));
        response.put("to", Map.of("stepId", targetStepId, "actionId", targetActionId, "controlId", newControlId));
        if (idTaken) {
            response.put("note", "Control id " + controlId + " was already used on action " + targetActionId
                    + ", so the control is now " + newControlId + " there.");
        }

        // The source action now has a gap in its order and the destination one an extra entry: both
        // are renumbered so each comes out in the dense 1..n shape the editor writes.
        MCPOrderingService.Result placement = orderingService.placeControl(testFolder, testcase, targetStepId,
                targetActionId, newControlId, position > 0 ? position : landingSort);
        if (placement.failed()) {
            response.put("warning", "The control was moved but its position on action " + targetActionId
                    + " could not be set: " + placement.error()
                    + " Use cerberus_testcase_step_action_control_reorder to place it.");
        } else {
            response.put("controlIds", placement.order());
        }

        List<TestCaseStepActionControl> remaining = testCaseStepActionControlService
                .findControlByTestTestCaseStepIdActionId(testFolder, testcase, stepId, actionId);
        if (remaining != null && !remaining.isEmpty()) {
            orderingService.reorderControls(testFolder, testcase, stepId, actionId,
                    remaining.stream().map(TestCaseStepActionControl::getControlId).toList());
        }

        mcpLogUtils.success(TOOL_NAME, "testcase_step_action_control_move",
                String.format("Control %d moved from action %d to action %d of %s/%s as control %d",
                        controlId, actionId, targetActionId, testFolder, testcase, newControlId));

        return MCPToolUtils.successStructured(response);
    }
}
