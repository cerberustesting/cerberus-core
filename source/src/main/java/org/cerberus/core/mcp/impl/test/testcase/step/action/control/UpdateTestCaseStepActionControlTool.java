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
 * MCP tool that updates an existing {@link TestCaseStepActionControl} (control/assertion) within a
 * testcase step action in Cerberus.
 *
 * <p>Exposes the MCP tool named {@code cerberus_testcase_step_action_control_update}.</p>
 *
 * <p>Delegates persistence to {@link ITestCaseStepActionControlService}. The control to update is
 * resolved by loading all controls for the given (testFolder, testcase, stepId, actionId) tuple via
 * {@code readByVarious1} and then filtering in memory by {@code controlId}, because no direct
 * find-by-key service method exists.</p>
 *
 * <p>The control enum values embedded in the tool schema are sourced from static constants on
 * {@link TestCaseStepActionControl} rather than the invariant table, so they are fixed at
 * compile time and do not require a startup database query.</p>
 */
@Component
public class UpdateTestCaseStepActionControlTool implements MCPTool {

    private static final String TOOL_NAME = "cerberus_testcase_step_action_control_update";

    private final ITestCaseStepActionControlService testCaseStepActionControlService;
    private final TestcaseStepActionControlMapperV001 mapper;
    private final MCPLogUtils mcpLogUtils;

    public UpdateTestCaseStepActionControlTool(ITestCaseStepActionControlService testCaseStepActionControlService, TestcaseStepActionControlMapperV001 mapper, MCPLogUtils mcpLogUtils) {
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
     * Builds the {@link McpSchema.Tool} descriptor exposed to MCP clients.
     *
     * <p>The {@code control} enum is built from static constants on {@link TestCaseStepActionControl}
     * (not the invariant table), so the list is fixed at compile time with no startup database risk.
     * All other field schemas are declared inline as static JSON.</p>
     *
     * @return the fully described tool schema including required parameters and the nested
     *         {@code updates} object with all updatable fields.
     */
    private McpSchema.Tool createTool() {
        Map<String, Object> updateProperties = new LinkedHashMap<>();
        updateProperties.put("description", Map.of(
                "type", "string",
                "description", "New description of what this control verifies."
        ));
        updateProperties.put("control", Map.of(
                "type", "string",
                "description", "Control type (assertion) to execute.",
                "enum", List.of(
                        TestCaseStepActionControl.CONTROL_UNKNOWN,
                        TestCaseStepActionControl.CONTROL_VERIFYSTRINGEQUAL,
                        TestCaseStepActionControl.CONTROL_VERIFYSTRINGDIFFERENT,
                        TestCaseStepActionControl.CONTROL_VERIFYSTRINGGREATER,
                        TestCaseStepActionControl.CONTROL_VERIFYSTRINGMINOR,
                        TestCaseStepActionControl.CONTROL_VERIFYSTRINGCONTAINS,
                        TestCaseStepActionControl.CONTROL_VERIFYSTRINGNOTCONTAINS,
                        TestCaseStepActionControl.CONTROL_VERIFYNUMERICEQUALS,
                        TestCaseStepActionControl.CONTROL_VERIFYNUMERICDIFFERENT,
                        TestCaseStepActionControl.CONTROL_VERIFYNUMERICGREATER,
                        TestCaseStepActionControl.CONTROL_VERIFYNUMERICGREATEROREQUAL,
                        TestCaseStepActionControl.CONTROL_VERIFYNUMERICMINOR,
                        TestCaseStepActionControl.CONTROL_VERIFYNUMERICMINOROREQUAL,
                        TestCaseStepActionControl.CONTROL_VERIFYELEMENTPRESENT,
                        TestCaseStepActionControl.CONTROL_VERIFYELEMENTNOTPRESENT,
                        TestCaseStepActionControl.CONTROL_VERIFYELEMENTVISIBLE,
                        TestCaseStepActionControl.CONTROL_VERIFYELEMENTNOTVISIBLE,
                        TestCaseStepActionControl.CONTROL_VERIFYELEMENTCHECKED,
                        TestCaseStepActionControl.CONTROL_VERIFYELEMENTNOTCHECKED,
                        TestCaseStepActionControl.CONTROL_VERIFYELEMENTEQUALS,
                        TestCaseStepActionControl.CONTROL_VERIFYELEMENTDIFFERENT,
                        TestCaseStepActionControl.CONTROL_VERIFYELEMENTINELEMENT,
                        TestCaseStepActionControl.CONTROL_VERIFYELEMENTCLICKABLE,
                        TestCaseStepActionControl.CONTROL_VERIFYELEMENTNOTCLICKABLE,
                        TestCaseStepActionControl.CONTROL_VERIFYELEMENTTEXTEQUAL,
                        TestCaseStepActionControl.CONTROL_VERIFYELEMENTTEXTDIFFERENT,
                        TestCaseStepActionControl.CONTROL_VERIFYELEMENTTEXTCONTAINS,
                        TestCaseStepActionControl.CONTROL_VERIFYELEMENTTEXTNOTCONTAINS,
                        TestCaseStepActionControl.CONTROL_VERIFYELEMENTTEXTMATCHREGEX,
                        TestCaseStepActionControl.CONTROL_VERIFYSTRINGARRAYCONTAINS,
                        TestCaseStepActionControl.CONTROL_VERIFYNUMERICARRAYCONTAINS,
                        TestCaseStepActionControl.CONTROL_VERIFYELEMENTTEXTARRAYCONTAINS,
                        TestCaseStepActionControl.CONTROL_VERIFYELEMENTNUMERICARRAYCONTAINS,
                        TestCaseStepActionControl.CONTROL_VERIFYELEMENTNUMERICEQUAL,
                        TestCaseStepActionControl.CONTROL_VERIFYELEMENTNUMERICDIFFERENT,
                        TestCaseStepActionControl.CONTROL_VERIFYELEMENTNUMERICGREATER,
                        TestCaseStepActionControl.CONTROL_VERIFYELEMENTNUMERICGREATEROREQUAL,
                        TestCaseStepActionControl.CONTROL_VERIFYELEMENTNUMERICMINOR,
                        TestCaseStepActionControl.CONTROL_VERIFYELEMENTNUMERICMINOROREQUAL,
                        TestCaseStepActionControl.CONTROL_VERIFYTEXTINPAGE
                )
        ));
        updateProperties.put("value1", Map.of(
                "type", "string",
                "description", "First value for the control. Meaning depends on the control type."
        ));
        updateProperties.put("value2", Map.of(
                "type", "string",
                "description", "Second value for the control. Meaning depends on the control type."
        ));
        updateProperties.put("value3", Map.of(
                "type", "string",
                "description", "Third value for the control. Meaning depends on the control type."
        ));
        updateProperties.put("conditionOperator", Map.of(
                "type", "string",
                "description", "Condition operator used to evaluate control execution."
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
        updateProperties.put("isFatal", Map.of(
                "type", "boolean",
                "description", "If true, a failure on this control stops the testcase execution."
        ));
        updateProperties.put("waitBefore", Map.of(
                "type", "integer",
                "description", "Wait time in milliseconds before executing the control."
        ));
        updateProperties.put("waitAfter", Map.of(
                "type", "integer",
                "description", "Wait time in milliseconds after executing the control."
        ));

        Map<String, Object> updatesSchema = new LinkedHashMap<>();
        updatesSchema.put("type", "object");
        updatesSchema.put("description", "Fields to update. Only provide the fields that need to change.");
        updatesSchema.put("properties", updateProperties);
        updatesSchema.put("additionalProperties", false);

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
                "description", "ID of the action containing the control."
        ));
        properties.put("controlId", Map.of(
                "type", "integer",
                "description", "ID of the control to update."
        ));
        properties.put("updates", updatesSchema);

        return new McpSchema.Tool(
                TOOL_NAME,
                null,
                """
                Updates an existing control (assertion) in a testcase step action in Cerberus.

                Call this tool whenever the user asks to modify, update, or change properties of an existing control.

                Only provide the fields that need to change in the updates object.

                Do not call this tool when the user only asks to list, read, create, or delete controls.
                """,
                new McpSchema.JsonSchema(
                        "object",
                        properties,
                        List.of("testFolder", "testcase", "stepId", "actionId", "controlId", "updates"),
                        null,
                        null,
                        null
                ),
                null,
                MCPToolUtils.updateAnnotations("Update testcase step action control", false),
                null
        );
    }

    /**
     * Executes the update operation for a single control.
     *
     * <p>Resolves the existing entity via {@code readByVarious1} + in-memory filter by
     * {@code controlId} (no direct find-by-key service method exists), applies each field from the
     * {@code updates} map, then persists via {@code update()}.
     *
     * <p>Note: {@code update(TestCaseStepActionControl)} returns a boolean (true = success), not an
     * Answer — no exception is thrown on failure.</p>
     *
     * @param args raw MCP arguments map; must contain testFolder, testcase, stepId, actionId,
     *             controlId and a non-empty updates map.
     * @return a success result with the updated DTO, or an error result describing the failure.
     */
    @SuppressWarnings("unchecked")
    private McpSchema.CallToolResult execute(Map<String, Object> args) {
        String testFolder = MCPToolUtils.getString(args, "testFolder", "");
        String testcaseId = MCPToolUtils.getString(args, "testcase", "");
        int stepId = MCPToolUtils.getInteger(args, "stepId", 0);
        int actionId = MCPToolUtils.getInteger(args, "actionId", 0);
        int controlId = MCPToolUtils.getInteger(args, "controlId", 0);

        mcpLogUtils.call(TOOL_NAME, "testcase_step_action_control_update",
                String.format("MCP tool %s called with testFolder=%s testcase=%s stepId=%d actionId=%d controlId=%d",
                        TOOL_NAME, testFolder, testcaseId, stepId, actionId, controlId));

        if (testFolder.isBlank()) return MCPToolUtils.errorText("Missing required parameter: testFolder");
        if (testcaseId.isBlank()) return MCPToolUtils.errorText("Missing required parameter: testcase");
        if (stepId <= 0) return MCPToolUtils.errorText("Missing or invalid required parameter: stepId");
        if (actionId <= 0) return MCPToolUtils.errorText("Missing or invalid required parameter: actionId");
        if (controlId <= 0) return MCPToolUtils.errorText("Missing or invalid required parameter: controlId");

        Object updatesObject = args.get("updates");
        if (!(updatesObject instanceof Map)) {
            return MCPToolUtils.errorText("Missing or invalid required parameter: updates");
        }

        Map<String, Object> updates = (Map<String, Object>) updatesObject;
        if (updates.isEmpty()) {
            return MCPToolUtils.errorText("No field provided to update.");
        }

        // No direct findByKey service method exists — load the full list then filter in memory.
        AnswerList<TestCaseStepActionControl> answer = testCaseStepActionControlService.readByVarious1(testFolder, testcaseId, stepId, actionId);

        TestCaseStepActionControl existing = answer.getDataList().stream()
                .filter(c -> c.getControlId() == controlId)
                .findFirst()
                .orElse(null);

        if (existing == null) {
            return MCPToolUtils.errorText("Control does not exist: testFolder=" + testFolder
                    + " testcase=" + testcaseId + " stepId=" + stepId
                    + " actionId=" + actionId + " controlId=" + controlId);
        }

        try {
            for (Map.Entry<String, Object> entry : updates.entrySet()) {
                String field = entry.getKey();
                Object value = entry.getValue();

                switch (field) {
                    case "description":
                        existing.setDescription(asString(value, field));
                        break;

                    case "control":
                        existing.setControl(asString(value, field));
                        break;

                    case "value1":
                        existing.setValue1(asString(value, field));
                        break;

                    case "value2":
                        existing.setValue2(asString(value, field));
                        break;

                    case "value3":
                        existing.setValue3(asString(value, field));
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

                    case "isFatal":
                        existing.setFatal(asBoolean(value, field));
                        break;

                    case "waitBefore":
                        existing.setWaitBefore(asInteger(value, field));
                        break;

                    case "waitAfter":
                        existing.setWaitAfter(asInteger(value, field));
                        break;

                    default:
                        return MCPToolUtils.errorText("Unsupported field for control update: " + field);
                }
            }
        } catch (IllegalArgumentException e) {
            return MCPToolUtils.errorText(e.getMessage());
        }

        // Tag the modifier as MCP so audit columns reflect the source of the change.
        existing.setUsrModif("MCP");

        // update() returns boolean (true = success), not Answer — no exception thrown.
        boolean success = testCaseStepActionControlService.update(existing);
        if (!success) {
            return MCPToolUtils.errorText("Unable to update control: testFolder=" + testFolder
                    + " testcase=" + testcaseId + " stepId=" + stepId
                    + " actionId=" + actionId + " controlId=" + controlId);
        }

        TestcaseStepActionControlDTOV001 dto = mapper.toDTO(existing);
        return MCPToolUtils.successJson(Map.of(
                "status", "updated",
                "control", dto
        ));
    }

    /**
     * Coerces an MCP argument value to a trimmed String.
     *
     * <p>Returns an empty string for {@code null}; throws {@link IllegalArgumentException} if the
     * value is present but not a String instance.</p>
     *
     * @param value the raw argument value from the MCP request.
     * @param field the field name, used only for the error message.
     * @return trimmed string value, never {@code null}.
     */
    private String asString(Object value, String field) {
        if (value == null) return "";
        if (!(value instanceof String)) {
            throw new IllegalArgumentException("Invalid value for field '" + field + "'. Expected string.");
        }
        return ((String) value).trim();
    }

    /**
     * Coerces an MCP argument value to a primitive boolean.
     *
     * <p>Throws {@link IllegalArgumentException} if the value is not a {@link Boolean} instance,
     * since JSON booleans are always deserialised as {@code Boolean} by the MCP framework.</p>
     *
     * @param value the raw argument value from the MCP request.
     * @param field the field name, used only for the error message.
     * @return the boolean value.
     */
    private boolean asBoolean(Object value, String field) {
        if (!(value instanceof Boolean)) {
            throw new IllegalArgumentException("Invalid value for field '" + field + "'. Expected boolean.");
        }
        return (Boolean) value;
    }

    /**
     * Coerces an MCP argument value to a primitive int.
     *
     * <p>Accepts both {@link Integer} and any other {@link Number} subtype (e.g. {@code Long} when
     * the JSON payload is large) by delegating to {@link Number#intValue()}. Throws
     * {@link IllegalArgumentException} for non-numeric types.</p>
     *
     * @param value the raw argument value from the MCP request.
     * @param field the field name, used only for the error message.
     * @return the integer value.
     */
    private int asInteger(Object value, String field) {
        if (value instanceof Integer) {
            return (Integer) value;
        }
        if (value instanceof Number) {
            return ((Number) value).intValue();
        }
        throw new IllegalArgumentException("Invalid value for field '" + field + "'. Expected integer.");
    }

}
