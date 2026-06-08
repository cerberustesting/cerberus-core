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

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * MCP tool that updates an existing {@link TestCaseStep} entity within a Cerberus testcase.
 *
 * <p>Exposed MCP tool name: {@value TOOL_NAME}.</p>
 *
 * <p>Delegates persistence to {@link ITestCaseStepService#updateTestCaseStep(TestCaseStep)},
 * which throws {@link CerberusException} on failure rather than returning an Answer object.
 * The updated entity is mapped to {@link TestcaseStepDTOV001} via {@link TestcaseStepMapperV001}
 * and returned in the tool result.</p>
 *
 * <p>Note: only scalar step-level fields (description, loop, conditions, flags) are mutable
 * through this tool. Nested actions and controls must be modified via their own dedicated tools.</p>
 */
@Component
public class UpdateTestCaseStepTool implements MCPTool {

    private static final String TOOL_NAME = "cerberus_testcase_step_update";

    private final ITestCaseStepService testCaseStepService;
    private final TestcaseStepMapperV001 mapper;
    private final MCPLogUtils mcpLogUtils;

    public UpdateTestCaseStepTool(ITestCaseStepService testCaseStepService, TestcaseStepMapperV001 mapper, MCPLogUtils mcpLogUtils) {
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
     * Builds the {@link McpSchema.Tool} descriptor exposed to MCP clients.
     *
     * <p>Loop mode enum values are sourced from {@link TestCaseStep} constants (static Java
     * constants, not the invariant table), so no startup-time database call is needed and
     * there is no risk of NullPointerException from a missing HTTP request context.</p>
     *
     * <p>The {@code updates} sub-object uses {@code additionalProperties: false} to prevent
     * the LLM from inventing field names that are not handled by the switch in
     * {@link #execute(Map)}.</p>
     */
    private McpSchema.Tool createTool() {
        Map<String, Object> updateProperties = new LinkedHashMap<>();
        updateProperties.put("description", Map.of(
                "type", "string",
                "description", "New description of what the step does."
        ));
        updateProperties.put("loop", Map.of(
                "type", "string",
                "description", "Execution loop mode for this step.",
                // Enum sourced from TestCaseStep constants — kept in sync with the entity definition.
                "enum", List.of(
                        TestCaseStep.LOOP_ONCEIFCONDITIONTRUE,
                        TestCaseStep.LOOP_ONCEIFCONDITIONFALSE,
                        TestCaseStep.LOOP_DOWHILECONDITIONTRUE,
                        TestCaseStep.LOOP_DOWHILECONDITIONFALSE,
                        TestCaseStep.LOOP_WHILECONDITIONTRUEDO,
                        TestCaseStep.LOOP_WHILECONDITIONFALSEDO
                )
        ));
        updateProperties.put("conditionOperator", Map.of(
                "type", "string",
                "description", "Condition operator used to evaluate step execution."
        ));
        updateProperties.put("conditionValue1", Map.of(
                "type", "string",
                "description", "First condition value."
        ));
        updateProperties.put("conditionValue2", Map.of(
                "type", "string",
                "description", "Second condition value."
        ));
        updateProperties.put("conditionValue3", Map.of(
                "type", "string",
                "description", "Third condition value."
        ));
        updateProperties.put("isLibraryStep", Map.of(
                "type", "boolean",
                "description", "Set to true if this step is a reusable library step callable by other testcases."
        ));
        updateProperties.put("isExecutionForced", Map.of(
                "type", "boolean",
                "description", "Set to true to force the execution of this step regardless of conditions."
        ));

        Map<String, Object> updatesSchema = new LinkedHashMap<>();
        updatesSchema.put("type", "object");
        updatesSchema.put("description", "Fields to update. Only provide the fields that need to change.");
        updatesSchema.put("properties", updateProperties);
        // Reject unknown fields early to avoid silently ignoring them in the switch statement.
        updatesSchema.put("additionalProperties", false);

        Map<String, Object> properties = new LinkedHashMap<>();
        properties.put("testFolder", Map.of(
                "type", "string",
                "description", "Name of the test folder containing the testcase."
        ));
        properties.put("testcase", Map.of(
                "type", "string",
                "description", "Identifier of the testcase containing the step."
        ));
        properties.put("stepId", Map.of(
                "type", "integer",
                "description", "Identifier of the step to update."
        ));
        properties.put("updates", updatesSchema);

        return new McpSchema.Tool(
                TOOL_NAME,
                null,
                """
                Updates an existing step in a testcase in Cerberus.

                Call this tool whenever the user asks to modify, update, or change properties of an existing step.
                WARNING: Updating a step does not affect its actions and controls — use dedicated action/control tools for those.

                Only provide the fields that need to change in the updates object.

                Do not call this tool when the user only asks to list, read, create, or delete steps.
                """,
                new McpSchema.JsonSchema(
                        "object",
                        properties,
                        List.of("testFolder", "testcase", "stepId", "updates"),
                        null,
                        null,
                        null
                ),
                null,
                MCPToolUtils.updateAnnotations("Update testcase step", false),
                null
        );
    }

    /**
     * Fetches the existing step, applies only the fields present in {@code args["updates"]},
     * persists the change via the service, and returns the updated DTO.
     *
     * <p>Each recognized field is dispatched through a switch; an unknown field causes an
     * immediate error response rather than a silent no-op, which matches the
     * {@code additionalProperties: false} contract declared in {@link #createTool()}.</p>
     *
     * <p>The service call throws {@link CerberusException} on failure — there is no Answer
     * wrapper returned here.</p>
     */
    @SuppressWarnings("unchecked")
    private McpSchema.CallToolResult execute(Map<String, Object> args) {
        String testFolder = MCPToolUtils.getString(args, "testFolder", "");
        String testcaseId = MCPToolUtils.getString(args, "testcase", "");
        int stepId = MCPToolUtils.getInteger(args, "stepId", -1);

        mcpLogUtils.call(TOOL_NAME, "testcase_step_update",
                String.format("MCP tool %s called with testFolder=%s testcase=%s stepId=%d", TOOL_NAME, testFolder, testcaseId, stepId));

        if (testFolder.isBlank()) {
            return MCPToolUtils.errorText("Missing required parameter: testFolder");
        }

        if (testcaseId.isBlank()) {
            return MCPToolUtils.errorText("Missing required parameter: testcase");
        }

        if (stepId < 0) {
            return MCPToolUtils.errorText("Missing required parameter: stepId");
        }

        Object updatesObject = args.get("updates");
        if (!(updatesObject instanceof Map)) {
            return MCPToolUtils.errorText("Missing or invalid required parameter: updates");
        }

        Map<String, Object> updates = (Map<String, Object>) updatesObject;
        if (updates.isEmpty()) {
            return MCPToolUtils.errorText("No field provided to update.");
        }

        TestCaseStep existing = testCaseStepService.findTestCaseStep(testFolder, testcaseId, stepId);
        if (existing == null) {
            return MCPToolUtils.errorText("Step does not exist: testFolder=" + testFolder + " testcase=" + testcaseId + " stepId=" + stepId);
        }

        try {
            for (Map.Entry<String, Object> entry : updates.entrySet()) {
                String field = entry.getKey();
                Object value = entry.getValue();

                switch (field) {
                    case "description":
                        existing.setDescription(asString(value, field));
                        break;

                    case "loop":
                        existing.setLoop(asString(value, field));
                        break;

                    case "conditionOperator":
                        existing.setConditionOperator(asString(value, field));
                        break;

                    case "conditionValue1":
                        existing.setConditionValue1(asString(value, field));
                        break;

                    case "conditionValue2":
                        existing.setConditionValue2(asString(value, field));
                        break;

                    case "conditionValue3":
                        existing.setConditionValue3(asString(value, field));
                        break;

                    case "isLibraryStep":
                        existing.setLibraryStep(asBoolean(value, field));
                        break;

                    case "isExecutionForced":
                        existing.setExecutionForced(asBoolean(value, field));
                        break;

                    default:
                        return MCPToolUtils.errorText("Unsupported field for step update: " + field);
                }
            }
        } catch (IllegalArgumentException e) {
            return MCPToolUtils.errorText(e.getMessage());
        }

        // Tag the modifier so audit columns in the DB reflect the MCP origin.
        existing.setUsrModif("MCP");

        try {
            // updateTestCaseStep throws CerberusException on failure rather than returning an Answer.
            testCaseStepService.updateTestCaseStep(existing);
        } catch (CerberusException e) {
            return MCPToolUtils.errorText("Unable to update step: " + e.getMessage());
        }

        TestcaseStepDTOV001 dto = mapper.toDTO(existing);
        return MCPToolUtils.successJson(Map.of(
                "status", "updated",
                "step", dto
        ));
    }

    /**
     * Coerces a raw MCP argument value to a trimmed {@link String}.
     * Returns an empty string when the value is {@code null}.
     * Throws {@link IllegalArgumentException} if the value is not a {@link String}.
     */
    private String asString(Object value, String field) {
        if (value == null) return "";
        if (!(value instanceof String)) {
            throw new IllegalArgumentException("Invalid value for field '" + field + "'. Expected string.");
        }
        return ((String) value).trim();
    }

    /**
     * Coerces a raw MCP argument value to a primitive {@code boolean}.
     * Throws {@link IllegalArgumentException} if the value is not a {@link Boolean};
     * the MCP schema declares the field as boolean so this should not happen in practice.
     */
    private boolean asBoolean(Object value, String field) {
        if (!(value instanceof Boolean)) {
            throw new IllegalArgumentException("Invalid value for field '" + field + "'. Expected boolean.");
        }
        return (Boolean) value;
    }

}
