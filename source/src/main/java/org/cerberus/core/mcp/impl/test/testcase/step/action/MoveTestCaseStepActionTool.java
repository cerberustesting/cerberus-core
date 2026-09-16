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
import org.cerberus.core.crud.entity.TestCaseStep;
import org.cerberus.core.crud.entity.TestCaseStepAction;
import org.cerberus.core.crud.service.ITestCaseStepActionService;
import org.cerberus.core.crud.service.ITestCaseStepService;
import org.cerberus.core.mcp.MCPTool;
import org.cerberus.core.mcp.util.MCPLogUtils;
import org.cerberus.core.mcp.util.MCPOrderingService;
import org.cerberus.core.mcp.util.MCPToolUtils;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * MCP tool that moves an action from one step to another, under the tool name
 * {@code cerberus_testcase_step_action_move}.
 *
 * <p>The operation the editor offers as cut-and-paste, and the one thing reordering could not do:
 * an action put in the wrong step, or a step that grew until part of it belongs somewhere else.
 * Without it the only route was to delete the action and rebuild it, which loses its controls —
 * so in practice it was not done, and the scenario kept the wrong shape.</p>
 *
 * <p>The move is a single update of the action's key columns. Its controls reference it under an
 * {@code ON UPDATE CASCADE} foreign key, so they travel with it in the same statement rather than
 * being re-created and possibly lost.</p>
 */
@Component
public class MoveTestCaseStepActionTool implements MCPTool {

    private static final String TOOL_NAME = "cerberus_testcase_step_action_move";

    /** Action ids advance in tens, as everywhere else. */
    private static final int ACTION_ID_INCREMENT = 10;

    private final ITestCaseStepService testCaseStepService;
    private final ITestCaseStepActionService testCaseStepActionService;
    private final MCPOrderingService orderingService;
    private final MCPLogUtils mcpLogUtils;

    public MoveTestCaseStepActionTool(ITestCaseStepService testCaseStepService,
                                      ITestCaseStepActionService testCaseStepActionService,
                                      MCPOrderingService orderingService,
                                      MCPLogUtils mcpLogUtils) {
        this.testCaseStepService = testCaseStepService;
        this.testCaseStepActionService = testCaseStepActionService;
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
                "description", "Testcase containing both steps."
        ));
        properties.put("stepId", Map.of(
                "type", "integer",
                "description", "Step the action is in today."
        ));
        properties.put("actionId", Map.of(
                "type", "integer",
                "description", "Action to move."
        ));
        properties.put("targetStepId", Map.of(
                "type", "integer",
                "description", "Step to move it into, in the same testcase."
        ));
        properties.put("position", Map.of(
                "type", "integer",
                "description", "Where it lands in the destination step, 1 being first. Omit it to append at "
                        + "the end."
        ));

        return new McpSchema.Tool(
                TOOL_NAME,
                null,
                """
                Moves an action, with its controls, from one step of a testcase to another.

                Use it when an action sits in the wrong step — a login action that belongs in the setup step,
                a check that drifted into the wrong block. Do not rebuild it by hand instead: deleting and
                re-creating an action loses every control attached to it, silently.

                The action keeps its content, its conditions, its options and its controls. Only its step and
                its position change; it takes a new action id in the destination step when its own is already
                taken there, which is reported back.

                Both steps must belong to the same testcase. To bring an action in from another testcase,
                copy the whole step with cerberus_testcase_step_duplicate, or recreate the action with
                cerberus_testcase_step_action_create.

                To move an action inside its own step, use cerberus_testcase_step_action_reorder.
                """,
                new McpSchema.JsonSchema(
                        "object",
                        properties,
                        List.of("testFolder", "testcase", "stepId", "actionId", "targetStepId"),
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
                                "to", Map.of("type", "object"),
                                "controlsFollowed", Map.of("type", "boolean")),
                        "required", List.of("status", "from", "to")),
                MCPToolUtils.updateAnnotations("Move an action to another step", false),
                null
        );
    }

    private McpSchema.CallToolResult execute(Map<String, Object> args) {
        String testFolder = MCPToolUtils.getString(args, "testFolder", "").trim();
        String testcase = MCPToolUtils.getString(args, "testcase", "").trim();
        int stepId = MCPToolUtils.getInteger(args, "stepId", -1);
        int actionId = MCPToolUtils.getInteger(args, "actionId", -1);
        int targetStepId = MCPToolUtils.getInteger(args, "targetStepId", -1);
        int position = MCPToolUtils.getInteger(args, "position", 0);

        mcpLogUtils.call(TOOL_NAME, "testcase_step_action_move",
                String.format("MCP tool %s called with %s/%s action %d of step %d to step %d",
                        TOOL_NAME, testFolder, testcase, actionId, stepId, targetStepId));

        if (testFolder.isBlank()) {
            return MCPToolUtils.errorText("Missing required parameter: testFolder");
        }
        if (testcase.isBlank()) {
            return MCPToolUtils.errorText("Missing required parameter: testcase");
        }
        if (stepId < 0 || actionId < 0 || targetStepId < 0) {
            return MCPToolUtils.errorText("stepId, actionId and targetStepId are all required, as whole "
                    + "numbers. Call cerberus_testcase_scenario_get to read them.");
        }
        if (stepId == targetStepId) {
            return MCPToolUtils.errorText("The action is already in step " + stepId
                    + ". To change its position inside that step, use cerberus_testcase_step_action_reorder.");
        }

        TestCaseStepAction action = testCaseStepActionService.findTestCaseStepActionbyKey(testFolder, testcase, stepId, actionId);
        if (action == null) {
            return MCPToolUtils.errorText("Action does not exist: testFolder=" + testFolder + " testcase="
                    + testcase + " stepId=" + stepId + " actionId=" + actionId);
        }

        TestCaseStep target = testCaseStepService.findTestCaseStep(testFolder, testcase, targetStepId);
        if (target == null) {
            return MCPToolUtils.errorText("Destination step does not exist: testFolder=" + testFolder
                    + " testcase=" + testcase + " stepId=" + targetStepId);
        }
        if (target.isUsingLibraryStep()) {
            // Such a step runs the actions of the library step it points at and ignores any of its
            // own, so the action would vanish from the run without any error.
            return MCPToolUtils.errorText("Step " + targetStepId + " runs the library step "
                    + MCPToolUtils.nullSafe(target.getLibraryStepTest()) + "/"
                    + MCPToolUtils.nullSafe(target.getLibraryStepTestcase()) + " step "
                    + target.getLibraryStepStepId() + ", so it never runs actions of its own. An action moved "
                    + "there would silently stop running. Move it to a step that owns its actions, or change "
                    + "the library step itself.");
        }

        List<TestCaseStepAction> targetActions =
                testCaseStepActionService.getListOfAction(testFolder, testcase, targetStepId);
        // The primary key is (test, testcase, stepId, actionId), so the action can keep its own id
        // only when nothing in the destination already uses it.
        boolean idTaken = targetActions != null && targetActions.stream()
                .anyMatch(existing -> existing.getActionId() == actionId);
        int newActionId = actionId;
        if (idTaken) {
            newActionId = testCaseStepActionService.getMaxActionId(targetActions) + ACTION_ID_INCREMENT;
        }

        // Lands at the end first; the placement below gives it the requested position. Doing it in
        // that order means a failure to place leaves the action present and running, not lost.
        int landingSort = targetActions == null ? 1 : targetActions.size() + 1;

        if (!testCaseStepActionService.moveTestCaseStepActionToStep(testFolder, testcase, stepId, actionId,
                targetStepId, newActionId, landingSort, "MCP")) {
            return MCPToolUtils.errorText("The action could not be moved. It is still in step " + stepId
                    + "; read it back with cerberus_testcase_scenario_get before trying anything else.");
        }

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("status", "moved");
        response.put("testFolder", testFolder);
        response.put("testcase", testcase);
        response.put("from", Map.of("stepId", stepId, "actionId", actionId));
        response.put("to", Map.of("stepId", targetStepId, "actionId", newActionId));
        response.put("controlsFollowed", true);
        if (idTaken) {
            response.put("note", "Action id " + actionId + " was already used in step " + targetStepId
                    + ", so the action is now " + newActionId + " there. Its controls followed it.");
        }

        // The source step now has a gap in its order and the destination one an extra entry: both are
        // renumbered so the two steps come out of this in the dense 1..n shape the editor writes.
        MCPOrderingService.Result placement = position > 0
                ? orderingService.placeAction(testFolder, testcase, targetStepId, newActionId, position)
                : orderingService.placeAction(testFolder, testcase, targetStepId, newActionId, landingSort);
        if (placement.failed()) {
            response.put("warning", "The action was moved but its position in step " + targetStepId
                    + " could not be set: " + placement.error()
                    + " Use cerberus_testcase_step_action_reorder to place it.");
        } else {
            response.put("actionIds", placement.order());
        }

        List<TestCaseStepAction> remaining =
                testCaseStepActionService.getListOfAction(testFolder, testcase, stepId);
        if (remaining != null && !remaining.isEmpty()) {
            orderingService.reorderActions(testFolder, testcase, stepId,
                    remaining.stream().map(TestCaseStepAction::getActionId).toList());
        }

        mcpLogUtils.success(TOOL_NAME, "testcase_step_action_move",
                String.format("Action %d moved from step %d to step %d of %s/%s as action %d",
                        actionId, stepId, targetStepId, testFolder, testcase, newActionId));

        return MCPToolUtils.successStructured(response);
    }
}
