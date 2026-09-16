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
import org.cerberus.core.mcp.MCPTool;
import org.cerberus.core.mcp.util.MCPLogUtils;
import org.cerberus.core.mcp.util.MCPOrderingService;
import org.cerberus.core.mcp.util.MCPToolUtils;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * MCP tool that sets the order in which the controls of an action run, under the tool name
 * {@code cerberus_testcase_step_action_control_reorder}.
 *
 * <p>Control order decides which check reports first, and a control marked fatal stops the
 * testcase where it sits — so moving one changes what the run reports, not just how it reads.</p>
 */
@Component
public class ReorderTestCaseStepActionControlsTool implements MCPTool {

    private static final String TOOL_NAME = "cerberus_testcase_step_action_control_reorder";

    private final MCPOrderingService orderingService;
    private final MCPLogUtils mcpLogUtils;

    public ReorderTestCaseStepActionControlsTool(MCPOrderingService orderingService, MCPLogUtils mcpLogUtils) {
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
                "description", "Step containing the action."
        ));
        properties.put("actionId", Map.of(
                "type", "integer",
                "description", "Action whose controls are being reordered."
        ));
        properties.put("controlIds", Map.of(
                "type", "array",
                "items", Map.of("type", "integer"),
                "description", "Every control id of the action, in the order they should run, first to "
                        + "last. The list must name each control exactly once — including the ones that do "
                        + "not move. Call cerberus_testcase_step_action_control_list first to read the "
                        + "current order."
        ));

        return new McpSchema.Tool(
                TOOL_NAME,
                null,
                """
                Sets the order in which the controls of an action run.

                Order matters here beyond readability: controls run in this order, and a control marked
                fatal ends the testcase where it sits, so anything after it never runs. Put the check that
                decides whether it is worth continuing first.

                Only the running order changes; control ids stay as they are.

                Send the complete list of control ids in the wanted order. A partial list is refused rather
                than completed by guesswork, and the error tells you the current order so you can resend.

                To place a new control rather than move an existing one, use the position argument of
                cerberus_testcase_step_action_control_create instead.
                """,
                new McpSchema.JsonSchema(
                        "object",
                        properties,
                        List.of("testFolder", "testcase", "stepId", "actionId", "controlIds"),
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
                                "actionId", Map.of("type", "integer"),
                                "controlIds", Map.of("type", "array", "items", Map.of("type", "integer")),
                                "controlsMoved", Map.of("type", "integer")),
                        "required", List.of("status", "controlIds", "controlsMoved")),
                // Idempotent: the same ordered list applied twice leaves the same order, so a client
                // may safely retry one that timed out.
                MCPToolUtils.annotations("Reorder action controls", false, false, true, false, false),
                null
        );
    }

    private McpSchema.CallToolResult execute(Map<String, Object> args) {
        String testFolder = MCPToolUtils.getString(args, "testFolder", "").trim();
        String testcase = MCPToolUtils.getString(args, "testcase", "").trim();
        int stepId = MCPToolUtils.getInteger(args, "stepId", -1);
        int actionId = MCPToolUtils.getInteger(args, "actionId", -1);
        List<Integer> controlIds = MCPToolUtils.getIntegerList(args, "controlIds");

        mcpLogUtils.call(TOOL_NAME, "testcase_step_action_control_reorder",
                String.format("MCP tool %s called with testFolder=%s testcase=%s stepId=%d actionId=%d controlIds=%s",
                        TOOL_NAME, testFolder, testcase, stepId, actionId, controlIds));

        if (testFolder.isBlank()) {
            return MCPToolUtils.errorText("Missing required parameter: testFolder");
        }
        if (testcase.isBlank()) {
            return MCPToolUtils.errorText("Missing required parameter: testcase");
        }
        if (stepId < 0) {
            return MCPToolUtils.errorText("Missing or invalid required parameter: stepId");
        }
        if (actionId < 0) {
            return MCPToolUtils.errorText("Missing or invalid required parameter: actionId");
        }
        if (controlIds == null) {
            return MCPToolUtils.errorText("Missing or invalid required parameter: controlIds. "
                    + "Send the control ids as a list of whole numbers, in the order they should run, "
                    + "for example [20, 10, 30].");
        }

        MCPOrderingService.Result result =
                orderingService.reorderControls(testFolder, testcase, stepId, actionId, controlIds);
        if (result.failed()) {
            return MCPToolUtils.errorText(result.error());
        }

        mcpLogUtils.success(TOOL_NAME, "testcase_step_action_control_reorder",
                String.format("Controls of %s/%s step %d action %d reordered to %s",
                        testFolder, testcase, stepId, actionId, result.order()));

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("status", "reordered");
        response.put("testFolder", testFolder);
        response.put("testcase", testcase);
        response.put("stepId", stepId);
        response.put("actionId", actionId);
        response.put("controlIds", result.order());
        response.put("controlsMoved", result.written());
        return MCPToolUtils.successStructured(response);
    }
}
