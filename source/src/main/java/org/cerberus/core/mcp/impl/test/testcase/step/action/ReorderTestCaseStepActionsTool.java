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
import org.cerberus.core.mcp.MCPTool;
import org.cerberus.core.mcp.util.MCPLogUtils;
import org.cerberus.core.mcp.util.MCPOrderingService;
import org.cerberus.core.mcp.util.MCPToolUtils;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * MCP tool that sets the order in which the actions of a step run, under the tool name
 * {@code cerberus_testcase_step_action_reorder}.
 *
 * <p>This is the tool to reach for whenever an action has to go somewhere other than the end of a
 * step. The alternative — creating actions at the end and shifting the content of every following
 * one down by hand — rewrites rows that should not have been touched at all, and one slip
 * overwrites a working action with the contents of its neighbour.</p>
 *
 * <p>Action ids are identity: the controls attached to an action point at it by {@code actionId},
 * and they follow it wherever it moves.</p>
 */
@Component
public class ReorderTestCaseStepActionsTool implements MCPTool {

    private static final String TOOL_NAME = "cerberus_testcase_step_action_reorder";

    private final MCPOrderingService orderingService;
    private final MCPLogUtils mcpLogUtils;

    public ReorderTestCaseStepActionsTool(MCPOrderingService orderingService, MCPLogUtils mcpLogUtils) {
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
                "description", "Testcase containing the step."
        ));
        properties.put("stepId", Map.of(
                "type", "integer",
                "description", "Step whose actions are being reordered."
        ));
        properties.put("actionIds", Map.of(
                "type", "array",
                "items", Map.of("type", "integer"),
                "description", "Every action id of the step, in the order they should run, first to last. "
                        + "The list must name each action exactly once — including the ones that do not "
                        + "move. Call cerberus_testcase_step_action_list first to read the current order."
        ));

        return new McpSchema.Tool(
                TOOL_NAME,
                null,
                """
                Sets the order in which the actions of a step run.

                Call this to move an action inside its step — a popup dismissal that has to happen between
                two existing clicks, a wait that has to come before the click rather than after it. Never
                simulate a move by rewriting the contents of several actions: that changes rows you did not
                mean to touch and loses whatever was in them.

                Only the running order changes. Action ids stay as they are, so each action keeps its own
                controls, conditions and options.

                Send the complete list of action ids in the wanted order. A partial list is refused rather
                than completed by guesswork, and the error tells you the current order so you can resend.

                To place a new action rather than move an existing one, use the position argument of
                cerberus_testcase_step_action_create instead.
                """,
                new McpSchema.JsonSchema(
                        "object",
                        properties,
                        List.of("testFolder", "testcase", "stepId", "actionIds"),
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
                                "stepId", Map.of("type", "integer"),
                                "actionIds", Map.of("type", "array", "items", Map.of("type", "integer")),
                                "actionsMoved", Map.of("type", "integer")),
                        "required", List.of("status", "actionIds", "actionsMoved")),
                // Idempotent: the same ordered list applied twice leaves the same order, so a client
                // may safely retry one that timed out.
                MCPToolUtils.annotations("Reorder step actions", false, false, true, false, false),
                null
        );
    }

    private McpSchema.CallToolResult execute(Map<String, Object> args) {
        String testFolder = MCPToolUtils.getString(args, "testFolder", "").trim();
        String testcase = MCPToolUtils.getString(args, "testcase", "").trim();
        int stepId = MCPToolUtils.getInteger(args, "stepId", -1);
        List<Integer> actionIds = MCPToolUtils.getIntegerList(args, "actionIds");

        mcpLogUtils.call(TOOL_NAME, "testcase_step_action_reorder",
                String.format("MCP tool %s called with testFolder=%s testcase=%s stepId=%d actionIds=%s",
                        TOOL_NAME, testFolder, testcase, stepId, actionIds));

        if (testFolder.isBlank()) {
            return MCPToolUtils.errorText("Missing required parameter: testFolder");
        }
        if (testcase.isBlank()) {
            return MCPToolUtils.errorText("Missing required parameter: testcase");
        }
        if (stepId < 0) {
            return MCPToolUtils.errorText("Missing or invalid required parameter: stepId");
        }
        if (actionIds == null) {
            return MCPToolUtils.errorText("Missing or invalid required parameter: actionIds. "
                    + "Send the action ids as a list of whole numbers, in the order they should run, "
                    + "for example [20, 10, 30].");
        }

        MCPOrderingService.Result result = orderingService.reorderActions(testFolder, testcase, stepId, actionIds);
        if (result.failed()) {
            return MCPToolUtils.errorText(result.error());
        }

        mcpLogUtils.success(TOOL_NAME, "testcase_step_action_reorder",
                String.format("Actions of %s/%s step %d reordered to %s",
                        testFolder, testcase, stepId, result.order()));

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("status", "reordered");
        response.put("testFolder", testFolder);
        response.put("testcase", testcase);
        response.put("stepId", stepId);
        response.put("actionIds", result.order());
        response.put("actionsMoved", result.written());
        return MCPToolUtils.successStructured(response);
    }
}
