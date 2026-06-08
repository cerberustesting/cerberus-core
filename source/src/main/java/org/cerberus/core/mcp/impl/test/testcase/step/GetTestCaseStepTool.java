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
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

/**
 * MCP tool that retrieves a single {@link TestCaseStep} by its composite key
 * (test folder, testcase ID, step ID).
 *
 * <p>Exposes the MCP tool name {@code cerberus_testcase_step_get}.
 *
 * <p>Delegates to {@link ITestCaseStepService#findTestCaseStep(String, String, int)}
 * and maps the result to {@link TestcaseStepDTOV001} via {@link TestcaseStepMapperV001}.
 *
 * <p>This tool is read-only; use the create/update/delete step tools for mutations.
 */
@Component
public class GetTestCaseStepTool implements MCPTool {

    private static final String TOOL_NAME = "cerberus_testcase_step_get";

    private final ITestCaseStepService testCaseStepService;
    private final TestcaseStepMapperV001 mapper;
    private final MCPLogUtils mcpLogUtils;

    public GetTestCaseStepTool(ITestCaseStepService testCaseStepService, TestcaseStepMapperV001 mapper, MCPLogUtils mcpLogUtils) {
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
     * Builds the static {@link McpSchema.Tool} descriptor registered with the MCP server.
     * The tool schema is fully static (no invariant-table or startup-time lookups are needed
     * for this read-only tool), so there is no startup-time risk of missing HTTP context.
     *
     * @return the tool descriptor with input schema and read-only annotations
     */
    private McpSchema.Tool createTool() {
        Map<String, Object> properties = Map.of(
                "testFolder", Map.of(
                        "type", "string",
                        "description", "Name of the test folder containing the testcase."
                ),
                "testcase", Map.of(
                        "type", "string",
                        "description", "Identifier of the testcase containing the step."
                ),
                "stepId", Map.of(
                        "type", "integer",
                        "description", "Identifier of the step to retrieve."
                )
        );

        return new McpSchema.Tool(
                TOOL_NAME,
                null,
                """
                Retrieves a single step from a testcase in Cerberus by test folder, testcase ID, and step ID.

                Call this tool whenever the user asks to get or inspect the details of a specific step.

                Use cerberus_testcase_step_list instead when the step ID is unknown or when listing multiple steps.
                Do not call this tool when the user asks to create, update, or delete a step.
                """,
                new McpSchema.JsonSchema(
                        "object",
                        properties,
                        List.of("testFolder", "testcase", "stepId"),
                        null,
                        null,
                        null
                ),
                null,
                MCPToolUtils.readOnlyAnnotations("Get testcase step", false),
                null
        );
    }

    /**
     * Validates the incoming arguments, looks up the step via the service, and returns
     * the mapped DTO as a JSON result.
     *
     * <p>Returns an error result if any required parameter is missing or blank, if
     * {@code stepId} is negative, or if no matching step is found in the database.
     *
     * @param args the raw MCP tool arguments map
     * @return a {@link McpSchema.CallToolResult} containing either the step JSON or an error message
     */
    private McpSchema.CallToolResult execute(Map<String, Object> args) {
        String testFolder = MCPToolUtils.getString(args, "testFolder", "");
        String testcaseId = MCPToolUtils.getString(args, "testcase", "");
        int stepId = MCPToolUtils.getInteger(args, "stepId", -1);

        mcpLogUtils.call(TOOL_NAME, "testcase_step_get",
                String.format("MCP tool %s called with testFolder=%s testcase=%s stepId=%d", TOOL_NAME, testFolder, testcaseId, stepId));

        if (testFolder.isBlank()) {
            return MCPToolUtils.errorText("Missing required parameter: testFolder");
        }

        if (testcaseId.isBlank()) {
            return MCPToolUtils.errorText("Missing required parameter: testcase");
        }

        // stepId defaults to -1 when absent; negative values are not valid step identifiers
        if (stepId < 0) {
            return MCPToolUtils.errorText("Missing required parameter: stepId");
        }

        TestCaseStep step = testCaseStepService.findTestCaseStep(testFolder, testcaseId, stepId);
        if (step == null) {
            return MCPToolUtils.errorText("Step does not exist: testFolder=" + testFolder + " testcase=" + testcaseId + " stepId=" + stepId);
        }

        TestcaseStepDTOV001 dto = mapper.toDTO(step);
        return MCPToolUtils.successJson(dto);
    }

}
