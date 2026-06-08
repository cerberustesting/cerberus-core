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
 * MCP tool that lists {@link TestCaseStepActionControl} entities (assertions) attached to a
 * specific action inside a testcase step.
 *
 * <p>Exposes the MCP tool named {@code cerberus_testcase_step_action_control_list}.
 *
 * <p>Delegates all data retrieval to {@link ITestCaseStepActionControlService}, then maps
 * the results to {@link TestcaseStepActionControlDTOV001} via
 * {@link TestcaseStepActionControlMapperV001} before returning them to the MCP client.
 *
 * <p>This tool is read-only: it never mutates any entity. An optional {@code search} parameter
 * allows in-memory filtering on description or control type because no server-side text-search
 * method is exposed by the service.
 */
@Component
public class ListTestCaseStepActionControlsTool implements MCPTool {

    private static final String TOOL_NAME = "cerberus_testcase_step_action_control_list";

    private final ITestCaseStepActionControlService testCaseStepActionControlService;
    private final TestcaseStepActionControlMapperV001 mapper;
    private final MCPLogUtils mcpLogUtils;

    public ListTestCaseStepActionControlsTool(ITestCaseStepActionControlService testCaseStepActionControlService, TestcaseStepActionControlMapperV001 mapper, MCPLogUtils mcpLogUtils) {
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
     * Builds the static {@link McpSchema.Tool} descriptor registered with the MCP server at
     * startup.
     *
     * <p>Parameters are defined as a static JSON schema (no enum values are loaded from the
     * invariant table). Required fields are {@code testFolder}, {@code testcase}, {@code stepId},
     * and {@code actionId}; {@code search} is optional.
     *
     * @return the tool descriptor used by the MCP framework to expose this capability
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
                "description", "ID of the action whose controls to list."
        ));
        properties.put("search", Map.of(
                "type", "string",
                "description", "Optional text filter on control description or control type."
        ));

        return new McpSchema.Tool(
                TOOL_NAME,
                null,
                """
                Returns the list of controls (assertions) for a given action in a Cerberus testcase step.

                Call this tool when the user needs to browse or inspect the controls of an action, or when the controlId is unknown.
                Use search to filter controls by description or control type.

                Do not call this tool when the user asks to create, update, or delete controls.
                """,
                new McpSchema.JsonSchema(
                        "object",
                        properties,
                        List.of("testFolder", "testcase", "stepId", "actionId"),
                        null,
                        null,
                        null
                ),
                null,
                MCPToolUtils.readOnlyAnnotations("List testcase step action controls", true),
                null
        );
    }

    /**
     * Handles an MCP tool invocation: validates arguments, calls the service, applies optional
     * text filtering, and serialises the result as JSON.
     *
     * <p>No direct findByKey service method exists — the full control list for the given
     * (testFolder, testcase, stepId, actionId) tuple is loaded via {@code readByVarious1} and
     * filtered in memory.
     *
     * @param args raw arguments map extracted from the MCP request
     * @return a {@link McpSchema.CallToolResult} containing either the JSON payload or an error
     */
    private McpSchema.CallToolResult execute(Map<String, Object> args) {
        String testFolder = MCPToolUtils.getString(args, "testFolder", "");
        String testcaseId = MCPToolUtils.getString(args, "testcase", "");
        int stepId = MCPToolUtils.getInteger(args, "stepId", 0);
        int actionId = MCPToolUtils.getInteger(args, "actionId", 0);
        String search = MCPToolUtils.getString(args, "search", "");

        mcpLogUtils.call(TOOL_NAME, "testcase_step_action_control_list",
                String.format("MCP tool %s called with testFolder=%s testcase=%s stepId=%d actionId=%d",
                        TOOL_NAME, testFolder, testcaseId, stepId, actionId));

        if (testFolder.isBlank()) return MCPToolUtils.errorText("Missing required parameter: testFolder");
        if (testcaseId.isBlank()) return MCPToolUtils.errorText("Missing required parameter: testcase");
        if (stepId <= 0) return MCPToolUtils.errorText("Missing or invalid required parameter: stepId");
        if (actionId <= 0) return MCPToolUtils.errorText("Missing or invalid required parameter: actionId");

        // No direct findByKey service method exists — load the full list then filter in memory.
        AnswerList<TestCaseStepActionControl> answer = testCaseStepActionControlService.readByVarious1(testFolder, testcaseId, stepId, actionId);

        List<TestcaseStepActionControlDTOV001> dtos = answer.getDataList().stream()
                .filter(c -> matchesSearch(c, search))
                .map(mapper::toDTO)
                .toList();

        return MCPToolUtils.successJson(Map.of(
                "testFolder", testFolder,
                "testcase", testcaseId,
                "stepId", stepId,
                "actionId", actionId,
                "count", dtos.size(),
                "controls", dtos
        ));
    }

    /**
     * Returns {@code true} when the control matches the given search string, or when
     * {@code search} is blank (no filter applied).
     *
     * <p>Matching is case-insensitive and checks both the control description and the
     * control type field.
     *
     * @param control the entity to test
     * @param search  optional filter text; null or blank disables filtering
     * @return {@code true} if the control should be included in the result
     */
    private boolean matchesSearch(TestCaseStepActionControl control, String search) {
        if (search == null || search.isBlank()) return true;
        return MCPToolUtils.containsIgnoreCase(control.getDescription(), search)
                || MCPToolUtils.containsIgnoreCase(control.getControl(), search);
    }

}
