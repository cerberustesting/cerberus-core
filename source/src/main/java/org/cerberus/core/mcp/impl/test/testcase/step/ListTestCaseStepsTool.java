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
import org.cerberus.core.api.dto.testcasestep.TestcaseStepDTOV001;
import org.cerberus.core.api.dto.testcasestep.TestcaseStepMapperV001;
import org.cerberus.core.crud.entity.TestCaseStep;
import org.cerberus.core.crud.service.ITestCaseStepService;
import org.cerberus.core.mcp.MCPTool;
import org.cerberus.core.mcp.util.MCPLogUtils;
import org.cerberus.core.mcp.util.MCPToolUtils;
import org.cerberus.core.util.answer.AnswerList;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

/**
 * MCP tool that exposes the {@code cerberus_testcase_step_list} endpoint.
 *
 * <p>Manages {@link TestCaseStep} entities: retrieves all steps belonging to a
 * given (testFolder, testcase) pair and optionally filters them by description.
 *
 * <p>Delegates to {@link ITestCaseStepService#readByTestTestCase} for data
 * retrieval and uses {@link TestcaseStepMapperV001} to convert entities to
 * transport DTOs before serialising the response.
 *
 * <p>This is a read-only tool — it never mutates test-case steps. Create,
 * update, and delete operations are handled by dedicated sibling tools.
 */
@Component
public class ListTestCaseStepsTool implements MCPTool {

    private static final String TOOL_NAME = "cerberus_testcase_step_list";

    private final ITestCaseStepService testCaseStepService;
    private final TestcaseStepMapperV001 mapper;
    private final MCPLogUtils mcpLogUtils;

    public ListTestCaseStepsTool(ITestCaseStepService testCaseStepService, TestcaseStepMapperV001 mapper, MCPLogUtils mcpLogUtils) {
        this.testCaseStepService = testCaseStepService;
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
     * Builds the {@link McpSchema.Tool} descriptor registered with the MCP server at startup.
     *
     * <p>The schema is defined statically (no invariant table lookup), so there is
     * no startup-time risk of missing HTTP context. The {@code testFolder} and
     * {@code testcase} parameters are declared as required; {@code search} is optional.
     */
    private McpSchema.Tool createTool() {
        Map<String, Object> properties = Map.of(
                "testFolder", Map.of(
                        "type", "string",
                        "description", "Name of the test folder containing the testcase."
                ),
                "testcase", Map.of(
                        "type", "string",
                        "description", "Identifier of the testcase to list steps from."
                ),
                "search", Map.of(
                        "type", "string",
                        "description", "Optional text filter on step description."
                )
        );

        return new McpSchema.Tool(
                TOOL_NAME,
                null,
                """
                Returns the list of steps for a given testcase in Cerberus.

                Call this tool when the user needs to browse or inspect the steps of a testcase.
                Use search to filter steps by description.

                Do not call this tool when the user asks to create, update, or delete steps.
                """,
                new McpSchema.JsonSchema(
                        "object",
                        properties,
                        List.of("testFolder", "testcase"),
                        null,
                        null,
                        null
                ),
                null,
                MCPToolUtils.readOnlyAnnotations("List testcase steps", false),
                null
        );
    }

    /**
     * Executes the tool: loads all steps for the given testcase and applies an
     * optional description filter before returning the serialised DTO list.
     *
     * <p>Returns an error result immediately when either required parameter is blank,
     * avoiding an unnecessary service call with empty keys.
     */
    private McpSchema.CallToolResult execute(Map<String, Object> args) {
        String testFolder = MCPToolUtils.getString(args, "testFolder", "");
        String testcaseId = MCPToolUtils.getString(args, "testcase", "");
        String search = MCPToolUtils.getString(args, "search", "");

        mcpLogUtils.call(TOOL_NAME, "testcase_step_list",
                String.format("MCP tool %s called with testFolder=%s testcase=%s", TOOL_NAME, testFolder, testcaseId));

        if (testFolder.isBlank()) {
            return MCPToolUtils.errorText("Missing required parameter: testFolder");
        }

        if (testcaseId.isBlank()) {
            return MCPToolUtils.errorText("Missing required parameter: testcase");
        }

        AnswerList<TestCaseStep> stepsAnswer = testCaseStepService.readByTestTestCase(testFolder, testcaseId);
        List<TestCaseStep> steps = stepsAnswer.getDataList();

        // Guard against a null data list returned by the service when no steps exist.
        List<TestcaseStepDTOV001> dtos = steps == null ? List.of() : steps.stream()
                .filter(s -> matchesSearch(s, search))
                .map(mapper::toDTO)
                .toList();

        return MCPToolUtils.successJson(Map.of(
                "testFolder", testFolder,
                "testcase", testcaseId,
                "count", dtos.size(),
                "steps", dtos
        ));
    }

    /**
     * Returns {@code true} when {@code search} is blank (no filter) or when the
     * step description contains {@code search} (case-insensitive).
     */
    private boolean matchesSearch(TestCaseStep step, String search) {
        if (search == null || search.isBlank()) return true;
        return MCPToolUtils.containsIgnoreCase(step.getDescription(), search);
    }

}
