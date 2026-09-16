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
import org.cerberus.core.mcp.MCPTool;
import org.cerberus.core.mcp.util.MCPLogUtils;
import org.cerberus.core.mcp.util.MCPOrderingService;
import org.cerberus.core.mcp.util.MCPToolUtils;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * MCP tool that sets the order in which the steps of a testcase run, under the tool name
 * {@code cerberus_testcase_step_reorder}.
 *
 * <p>Step ids are identity, not position — a step created later can have to run first, and a step
 * reused as a library step is pointed at by its id from other testcases. Reordering therefore
 * moves nothing but the running order, and every reference into the testcase survives it.</p>
 *
 * <p>Pair it with the {@code position} argument of {@code cerberus_testcase_step_create} and
 * {@code cerberus_testcase_step_library_use}, which place a new step directly where it belongs
 * instead of at the end.</p>
 */
@Component
public class ReorderTestCaseStepsTool implements MCPTool {

    private static final String TOOL_NAME = "cerberus_testcase_step_reorder";

    private final MCPOrderingService orderingService;
    private final MCPLogUtils mcpLogUtils;

    public ReorderTestCaseStepsTool(MCPOrderingService orderingService, MCPLogUtils mcpLogUtils) {
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
                "description", "Testcase whose steps are being reordered."
        ));
        properties.put("stepIds", Map.of(
                "type", "array",
                "items", Map.of("type", "integer"),
                "description", "Every step id of the testcase, in the order they should run, first to last. "
                        + "The list must name each step exactly once — including the ones that do not move. "
                        + "Call cerberus_testcase_step_list first to read the current order."
        ));

        return new McpSchema.Tool(
                TOOL_NAME,
                null,
                """
                Sets the order in which the steps of a testcase run.

                Call this whenever a step is in the wrong place — a missing prerequisite that has to run
                first, a cleanup that has to run last. Only the running order changes: step ids stay as
                they are, so the actions, controls and properties attached to each step follow it, and any
                other testcase reusing one of these steps as a library step keeps working.

                Send the complete list of step ids in the wanted order. A partial list is refused rather
                than completed by guesswork, and the error tells you the current order so you can resend.

                To place a new step rather than move an existing one, use the position argument of
                cerberus_testcase_step_create or cerberus_testcase_step_library_use instead.
                """,
                new McpSchema.JsonSchema(
                        "object",
                        properties,
                        List.of("testFolder", "testcase", "stepIds"),
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
                                "stepIds", Map.of("type", "array", "items", Map.of("type", "integer")),
                                "stepsMoved", Map.of("type", "integer")),
                        "required", List.of("status", "stepIds", "stepsMoved")),
                // Idempotent: the same ordered list applied twice leaves the same order, so a client
                // may safely retry one that timed out.
                MCPToolUtils.annotations("Reorder testcase steps", false, false, true, false, false),
                null
        );
    }

    private McpSchema.CallToolResult execute(Map<String, Object> args) {
        String testFolder = MCPToolUtils.getString(args, "testFolder", "").trim();
        String testcase = MCPToolUtils.getString(args, "testcase", "").trim();
        List<Integer> stepIds = MCPToolUtils.getIntegerList(args, "stepIds");

        mcpLogUtils.call(TOOL_NAME, "testcase_step_reorder",
                String.format("MCP tool %s called with testFolder=%s testcase=%s stepIds=%s",
                        TOOL_NAME, testFolder, testcase, stepIds));

        if (testFolder.isBlank()) {
            return MCPToolUtils.errorText("Missing required parameter: testFolder");
        }
        if (testcase.isBlank()) {
            return MCPToolUtils.errorText("Missing required parameter: testcase");
        }
        if (stepIds == null) {
            return MCPToolUtils.errorText("Missing or invalid required parameter: stepIds. "
                    + "Send the step ids as a list of whole numbers, in the order they should run, "
                    + "for example [20, 10, 30].");
        }

        MCPOrderingService.Result result = orderingService.reorderSteps(testFolder, testcase, stepIds);
        if (result.failed()) {
            return MCPToolUtils.errorText(result.error());
        }

        mcpLogUtils.success(TOOL_NAME, "testcase_step_reorder",
                String.format("Steps of %s/%s reordered to %s", testFolder, testcase, result.order()));

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("status", "reordered");
        response.put("testFolder", testFolder);
        response.put("testcase", testcase);
        response.put("stepIds", result.order());
        response.put("stepsMoved", result.written());
        return MCPToolUtils.successStructured(response);
    }
}
