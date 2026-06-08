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
import org.cerberus.core.api.dto.testcaseaction.TestcaseStepActionDTOV001;
import org.cerberus.core.api.dto.testcaseaction.TestcaseStepActionMapperV001;
import org.cerberus.core.crud.entity.TestCaseStepAction;
import org.cerberus.core.crud.service.ITestCaseStepActionService;
import org.cerberus.core.exception.CerberusException;
import org.cerberus.core.mcp.MCPTool;
import org.cerberus.core.mcp.util.MCPLogUtils;
import org.cerberus.core.mcp.util.MCPToolUtils;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

/**
 * MCP tool that permanently deletes a {@link TestCaseStepAction} from a testcase step.
 *
 * <p>Exposes the MCP tool named {@code cerberus_testcase_step_action_delete}.
 * Deletion is cascading: all child controls attached to the action are also removed.</p>
 *
 * <p>Delegates to {@link ITestCaseStepActionService} for persistence and uses
 * {@link TestcaseStepActionMapperV001} to capture a DTO snapshot of the entity
 * before it is erased from the database.</p>
 *
 * <p>Note: the service method {@code deleteTestCaseStepAction} throws
 * {@link CerberusException} on failure rather than returning an {@code Answer},
 * so the caller must handle it explicitly.</p>
 */
@Component
public class DeleteTestCaseStepActionTool implements MCPTool {

    private static final String TOOL_NAME = "cerberus_testcase_step_action_delete";

    private final ITestCaseStepActionService testCaseStepActionService;
    private final TestcaseStepActionMapperV001 mapper;
    private final MCPLogUtils mcpLogUtils;

    public DeleteTestCaseStepActionTool(ITestCaseStepActionService testCaseStepActionService, TestcaseStepActionMapperV001 mapper, MCPLogUtils mcpLogUtils) {
        this.testCaseStepActionService = testCaseStepActionService;
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
     * Builds the static MCP tool descriptor with its JSON Schema.
     *
     * <p>All parameter metadata (types, descriptions, required list) is defined
     * inline as a static map — no invariant table or external lookup is involved.
     * The tool is registered once at Spring startup; there is no startup-time risk
     * beyond normal bean initialisation.</p>
     *
     * @return the fully configured {@link McpSchema.Tool} descriptor
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
                        "description", "ID of the step containing the action."
                ),
                "actionId", Map.of(
                        "type", "integer",
                        "description", "ID of the action to delete."
                )
        );

        return new McpSchema.Tool(
                TOOL_NAME,
                null,
                """
                Deletes an existing action from a testcase step in Cerberus.

                WARNING: Deleting an action permanently removes all its controls as well.
                Call this tool whenever the user asks to delete or remove an action from a step.
                Before calling this tool, confirm the action to delete with the user.

                Do not call this tool when the user only asks to list, read, create, or update actions.
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
                MCPToolUtils.deleteAnnotations("Delete testcase step action", false),
                null
        );
    }

    /**
     * Validates arguments, fetches the existing action, snapshots it as a DTO,
     * then permanently deletes it via the service.
     *
     * <p>Returns a JSON result containing the deletion status and the snapshotted DTO,
     * or an error text if any argument is missing, the action is not found, or the
     * service throws {@link CerberusException}.</p>
     *
     * @param args raw MCP argument map extracted from the tool request
     * @return {@link McpSchema.CallToolResult} with success JSON or error text
     */
    private McpSchema.CallToolResult execute(Map<String, Object> args) {
        String testFolder = MCPToolUtils.getString(args, "testFolder", "");
        String testcaseId = MCPToolUtils.getString(args, "testcase", "");
        int stepId = MCPToolUtils.getInteger(args, "stepId", -1);
        int actionId = MCPToolUtils.getInteger(args, "actionId", -1);

        mcpLogUtils.call(TOOL_NAME, "testcase_step_action_delete",
                String.format("MCP tool %s called with testFolder=%s testcase=%s stepId=%d actionId=%d", TOOL_NAME, testFolder, testcaseId, stepId, actionId));

        if (testFolder.isBlank()) {
            return MCPToolUtils.errorText("Missing required parameter: testFolder");
        }

        if (testcaseId.isBlank()) {
            return MCPToolUtils.errorText("Missing required parameter: testcase");
        }

        if (stepId < 0) {
            return MCPToolUtils.errorText("Missing required parameter: stepId");
        }

        if (actionId < 0) {
            return MCPToolUtils.errorText("Missing required parameter: actionId");
        }

        TestCaseStepAction existing = testCaseStepActionService.findTestCaseStepActionbyKey(testFolder, testcaseId, stepId, actionId);
        if (existing == null) {
            return MCPToolUtils.errorText("Action does not exist: testFolder=" + testFolder + " testcase=" + testcaseId + " stepId=" + stepId + " actionId=" + actionId);
        }

        // Snapshot DTO before deletion — the entity is gone after the service call.
        TestcaseStepActionDTOV001 dto = mapper.toDTO(existing);

        try {
            // deleteTestCaseStepAction throws CerberusException on failure rather than returning an Answer.
            testCaseStepActionService.deleteTestCaseStepAction(existing);
        } catch (CerberusException e) {
            return MCPToolUtils.errorText("Unable to delete action: " + e.getMessage());
        }

        return MCPToolUtils.successJson(Map.of(
                "status", "deleted",
                "action", dto
        ));
    }

}
