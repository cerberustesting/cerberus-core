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
import org.cerberus.core.exception.CerberusException;
import org.cerberus.core.mcp.MCPTool;
import org.cerberus.core.mcp.util.MCPLogUtils;
import org.cerberus.core.mcp.util.MCPToolUtils;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

/**
 * MCP tool that permanently deletes a {@link TestCaseStep} (and all its nested actions and controls)
 * from Cerberus.
 *
 * <p>Exposed MCP tool name: {@value TOOL_NAME}.</p>
 *
 * <p>Delegates to {@link ITestCaseStepService} for persistence.
 * {@code deleteTestCaseStep} throws {@link CerberusException} on failure rather than returning
 * an Answer, so the execute method catches it and converts it into an MCP error result.</p>
 *
 * <p>The step is looked up before deletion so that a DTO snapshot can be returned in the
 * success response, giving the caller confirmation of exactly what was removed.</p>
 */
@Component
public class DeleteTestCaseStepTool implements MCPTool {

    private static final String TOOL_NAME = "cerberus_testcase_step_delete";

    private final ITestCaseStepService testCaseStepService;
    private final TestcaseStepMapperV001 mapper;
    private final MCPLogUtils mcpLogUtils;

    public DeleteTestCaseStepTool(ITestCaseStepService testCaseStepService, TestcaseStepMapperV001 mapper, MCPLogUtils mcpLogUtils) {
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
     * Builds the static {@link McpSchema.Tool} descriptor for this MCP tool.
     *
     * <p>Input schema requires three fields: {@code testFolder}, {@code testcase}, and
     * {@code stepId}. No enum values are loaded from the invariant table or static JSON —
     * all parameter types are plain primitives, so there is no startup-time risk from
     * missing invariant data.</p>
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
                        "description", "Identifier of the step to delete."
                )
        );

        return new McpSchema.Tool(
                TOOL_NAME,
                null,
                """
                Deletes an existing step from a testcase in Cerberus.

                WARNING: Deleting a step permanently removes all its actions and controls as well.
                Call this tool whenever the user asks to delete or remove a step from a testcase.
                Before calling this tool, confirm the step to delete with the user.

                Do not call this tool when the user only asks to list, read, create, or update steps.
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
                MCPToolUtils.deleteAnnotations("Delete testcase step", false),
                null
        );
    }

    /**
     * Validates input arguments, fetches the existing step, snapshots it as a DTO, then
     * delegates the actual deletion to {@link ITestCaseStepService#deleteTestCaseStep}.
     *
     * <p>Returns a JSON success payload containing the deleted step DTO on success, or an
     * MCP error result if a parameter is missing, the step is not found, or the service
     * throws {@link CerberusException}.</p>
     *
     * @param args raw MCP argument map extracted from the tool request
     * @return {@link McpSchema.CallToolResult} describing success or failure
     */
    private McpSchema.CallToolResult execute(Map<String, Object> args) {
        String testFolder = MCPToolUtils.getString(args, "testFolder", "");
        String testcaseId = MCPToolUtils.getString(args, "testcase", "");
        int stepId = MCPToolUtils.getInteger(args, "stepId", -1);

        mcpLogUtils.call(TOOL_NAME, "testcase_step_delete",
                String.format("MCP tool %s called with testFolder=%s testcase=%s stepId=%d", TOOL_NAME, testFolder, testcaseId, stepId));

        if (testFolder.isBlank()) {
            return MCPToolUtils.errorText("Missing required parameter: testFolder");
        }

        if (testcaseId.isBlank()) {
            return MCPToolUtils.errorText("Missing required parameter: testcase");
        }

        // stepId defaults to -1 when absent or unparseable; any non-negative value is valid
        if (stepId < 0) {
            return MCPToolUtils.errorText("Missing required parameter: stepId");
        }

        TestCaseStep existing = testCaseStepService.findTestCaseStep(testFolder, testcaseId, stepId);
        if (existing == null) {
            return MCPToolUtils.errorText("Step does not exist: testFolder=" + testFolder + " testcase=" + testcaseId + " stepId=" + stepId);
        }

        // Snapshot DTO before deletion — the entity is gone after the service call.
        TestcaseStepDTOV001 dto = mapper.toDTO(existing);

        try {
            // deleteTestCaseStep throws CerberusException on failure rather than returning an Answer.
            testCaseStepService.deleteTestCaseStep(existing);
        } catch (CerberusException e) {
            return MCPToolUtils.errorText("Unable to delete step: " + e.getMessage());
        }

        return MCPToolUtils.successJson(Map.of(
                "status", "deleted",
                "step", dto
        ));
    }

}
