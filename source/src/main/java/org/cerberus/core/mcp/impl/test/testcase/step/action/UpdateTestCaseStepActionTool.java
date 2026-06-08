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
import org.cerberus.core.mcp.MCPTool;
import org.cerberus.core.mcp.util.MCPLogUtils;
import org.cerberus.core.mcp.util.MCPToolUtils;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * MCP tool that updates an existing {@link TestCaseStepAction} inside a testcase step.
 *
 * <p>Exposed MCP tool name: {@value #TOOL_NAME}.
 *
 * <p>Delegates persistence to {@link ITestCaseStepActionService#updateTestCaseStepAction(TestCaseStepAction)},
 * which returns a boolean rather than an {@code Answer} object — no exception is thrown on failure.
 *
 * <p>The tool accepts a partial {@code updates} map so that callers supply only the fields they
 * want to change; all other fields are preserved from the existing entity fetched by primary key.
 * Updating an action never cascades to its controls — use dedicated control tools for those.
 */
@Component
public class UpdateTestCaseStepActionTool implements MCPTool {

    private static final String TOOL_NAME = "cerberus_testcase_step_action_update";

    private final ITestCaseStepActionService testCaseStepActionService;
    private final TestcaseStepActionMapperV001 mapper;
    private final MCPLogUtils mcpLogUtils;

    public UpdateTestCaseStepActionTool(ITestCaseStepActionService testCaseStepActionService, TestcaseStepActionMapperV001 mapper, MCPLogUtils mcpLogUtils) {
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
     * Builds the {@link McpSchema.Tool} descriptor exposed to MCP clients.
     *
     * <p>The {@code action} enum is sourced directly from {@link TestCaseStepAction} constants
     * (static Java strings), not from the Cerberus invariant table, so no database call is needed
     * at startup and there is no risk of a missing HTTP request context.
     *
     * <p>All update fields are optional inside the {@code updates} object; only the provided
     * fields are applied to the existing entity.
     */
    private McpSchema.Tool createTool() {
        Map<String, Object> updateProperties = new LinkedHashMap<>();
        updateProperties.put("description", Map.of(
                "type", "string",
                "description", "New description of what this action does."
        ));
        updateProperties.put("action", Map.of(
                "type", "string",
                "description", "Action type to execute.",
                "enum", List.of(
                        TestCaseStepAction.ACTION_UNKNOWN,
                        TestCaseStepAction.ACTION_CLICK,
                        TestCaseStepAction.ACTION_LONGPRESS,
                        TestCaseStepAction.ACTION_MOUSELEFTBUTTONPRESS,
                        TestCaseStepAction.ACTION_MOUSELEFTBUTTONRELEASE,
                        TestCaseStepAction.ACTION_MOUSEMOVE,
                        TestCaseStepAction.ACTION_DOUBLECLICK,
                        TestCaseStepAction.ACTION_RIGHTCLICK,
                        TestCaseStepAction.ACTION_MOUSEOVER,
                        TestCaseStepAction.ACTION_FOCUSTOIFRAME,
                        TestCaseStepAction.ACTION_FOCUSDEFAULTIFRAME,
                        TestCaseStepAction.ACTION_SWITCHTOWINDOW,
                        TestCaseStepAction.ACTION_MANAGEDIALOG,
                        TestCaseStepAction.ACTION_MANAGEDIALOGKEYPRESS,
                        TestCaseStepAction.ACTION_OPENURLWITHBASE,
                        TestCaseStepAction.ACTION_OPENURLLOGIN,
                        TestCaseStepAction.ACTION_OPENURL,
                        TestCaseStepAction.ACTION_REFRESHCURRENTPAGE,
                        TestCaseStepAction.ACTION_RETURNPREVIOUSPAGE,
                        TestCaseStepAction.ACTION_FORWARDNEXTPAGE,
                        TestCaseStepAction.ACTION_EXECUTEJS,
                        TestCaseStepAction.ACTION_EXECUTECOMMAND,
                        TestCaseStepAction.ACTION_EXECUTECERBERUSCOMMAND,
                        TestCaseStepAction.ACTION_OPENAPP,
                        TestCaseStepAction.ACTION_CLOSEAPP,
                        TestCaseStepAction.ACTION_DRAGANDDROP,
                        TestCaseStepAction.ACTION_SELECT,
                        TestCaseStepAction.ACTION_KEYPRESS,
                        TestCaseStepAction.ACTION_TYPE,
                        TestCaseStepAction.ACTION_CLEARFIELD,
                        TestCaseStepAction.ACTION_HIDEKEYBOARD,
                        TestCaseStepAction.ACTION_SWIPE,
                        TestCaseStepAction.ACTION_SCROLLTO,
                        TestCaseStepAction.ACTION_INSTALLAPP,
                        TestCaseStepAction.ACTION_REMOVEAPP,
                        TestCaseStepAction.ACTION_WAIT,
                        TestCaseStepAction.ACTION_WAITVANISH,
                        TestCaseStepAction.ACTION_WAITNETWORKTRAFFICIDLE,
                        TestCaseStepAction.ACTION_CALLSERVICE,
                        TestCaseStepAction.ACTION_EXECUTESQLUPDATE,
                        TestCaseStepAction.ACTION_EXECUTESQLSTOREPROCEDURE,
                        TestCaseStepAction.ACTION_CLEANROBOTFILE,
                        TestCaseStepAction.ACTION_UPLOADROBOTFILE,
                        TestCaseStepAction.ACTION_GETROBOTFILE,
                        TestCaseStepAction.ACTION_CALCULATEPROPERTY,
                        TestCaseStepAction.ACTION_SETNETWORKTRAFFICCONTENT,
                        TestCaseStepAction.ACTION_INDEXNETWORKTRAFFIC,
                        TestCaseStepAction.ACTION_SETCONSOLECONTENT,
                        TestCaseStepAction.ACTION_SETCONTENT,
                        TestCaseStepAction.ACTION_SETSERVICECALLCONTENT
                )
        ));
        updateProperties.put("value1", Map.of(
                "type", "string",
                "description", "First value for the action. Meaning depends on the action type."
        ));
        updateProperties.put("value2", Map.of(
                "type", "string",
                "description", "Second value for the action. Meaning depends on the action type."
        ));
        updateProperties.put("value3", Map.of(
                "type", "string",
                "description", "Third value for the action. Meaning depends on the action type."
        ));
        updateProperties.put("conditionOperator", Map.of(
                "type", "string",
                "description", "Condition operator used to evaluate action execution."
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
                "description", "If true, a failure on this action stops the testcase execution."
        ));
        updateProperties.put("waitBefore", Map.of(
                "type", "integer",
                "description", "Wait time in milliseconds before executing the action."
        ));
        updateProperties.put("waitAfter", Map.of(
                "type", "integer",
                "description", "Wait time in milliseconds after executing the action."
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
                "description", "Identifier of the testcase containing the step."
        ));
        properties.put("stepId", Map.of(
                "type", "integer",
                "description", "ID of the step containing the action."
        ));
        properties.put("actionId", Map.of(
                "type", "integer",
                "description", "ID of the action to update."
        ));
        properties.put("updates", updatesSchema);

        return new McpSchema.Tool(
                TOOL_NAME,
                null,
                """
                Updates an existing action in a testcase step in Cerberus.

                Call this tool whenever the user asks to modify, update, or change properties of an existing action.
                WARNING: Updating an action does not affect its controls — use dedicated control tools for those.

                Only provide the fields that need to change in the updates object.

                Do not call this tool when the user only asks to list, read, create, or delete actions.
                """,
                new McpSchema.JsonSchema(
                        "object",
                        properties,
                        List.of("testFolder", "testcase", "stepId", "actionId", "updates"),
                        null,
                        null,
                        null
                ),
                null,
                MCPToolUtils.updateAnnotations("Update testcase step action", false),
                null
        );
    }

    /**
     * Resolves the target action by its composite key, applies each field in the {@code updates}
     * map to the existing entity, persists the change, and returns the updated DTO as JSON.
     *
     * <p>Validation of required parameters is performed before the service call. An
     * {@link IllegalArgumentException} thrown by the type-coercion helpers is caught and returned
     * as an error result without propagating.
     *
     * <p>// updateTestCaseStepAction returns boolean (true = success), not Answer — no exception thrown.
     */
    @SuppressWarnings("unchecked")
    private McpSchema.CallToolResult execute(Map<String, Object> args) {
        String testFolder = MCPToolUtils.getString(args, "testFolder", "");
        String testcaseId = MCPToolUtils.getString(args, "testcase", "");
        int stepId = MCPToolUtils.getInteger(args, "stepId", -1);
        int actionId = MCPToolUtils.getInteger(args, "actionId", -1);

        mcpLogUtils.call(TOOL_NAME, "testcase_step_action_update",
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

        Object updatesObject = args.get("updates");
        if (!(updatesObject instanceof Map)) {
            return MCPToolUtils.errorText("Missing or invalid required parameter: updates");
        }

        Map<String, Object> updates = (Map<String, Object>) updatesObject;
        if (updates.isEmpty()) {
            return MCPToolUtils.errorText("No field provided to update.");
        }

        // Load the existing entity so unmodified fields are preserved in the update.
        TestCaseStepAction existing = testCaseStepActionService.findTestCaseStepActionbyKey(testFolder, testcaseId, stepId, actionId);
        if (existing == null) {
            return MCPToolUtils.errorText("Action does not exist: testFolder=" + testFolder + " testcase=" + testcaseId + " stepId=" + stepId + " actionId=" + actionId);
        }

        try {
            // Apply only the fields supplied by the caller; unknown field names produce an immediate error.
            for (Map.Entry<String, Object> entry : updates.entrySet()) {
                String field = entry.getKey();
                Object value = entry.getValue();

                switch (field) {
                    case "description":
                        existing.setDescription(asString(value, field));
                        break;

                    case "action":
                        existing.setAction(asString(value, field));
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
                        return MCPToolUtils.errorText("Unsupported field for action update: " + field);
                }
            }
        } catch (IllegalArgumentException e) {
            return MCPToolUtils.errorText(e.getMessage());
        }

        // Tag the modifier so audit logs reflect the MCP origin of this change.
        existing.setUsrModif("MCP");

        // updateTestCaseStepAction returns boolean (true = success), not Answer — no exception thrown.
        boolean success = testCaseStepActionService.updateTestCaseStepAction(existing);
        if (!success) {
            return MCPToolUtils.errorText("Unable to update action: testFolder=" + testFolder + " testcase=" + testcaseId + " stepId=" + stepId + " actionId=" + actionId);
        }

        TestcaseStepActionDTOV001 dto = mapper.toDTO(existing);
        return MCPToolUtils.successJson(Map.of(
                "status", "updated",
                "action", dto
        ));
    }

    /**
     * Coerces {@code value} to a trimmed {@link String}.
     * Returns an empty string for null; throws {@link IllegalArgumentException} for non-string types.
     */
    private String asString(Object value, String field) {
        if (value == null) return "";
        if (!(value instanceof String)) {
            throw new IllegalArgumentException("Invalid value for field '" + field + "'. Expected string.");
        }
        return ((String) value).trim();
    }

    /**
     * Coerces {@code value} to a primitive {@code boolean}.
     * Throws {@link IllegalArgumentException} if the value is not a {@link Boolean} instance.
     */
    private boolean asBoolean(Object value, String field) {
        if (!(value instanceof Boolean)) {
            throw new IllegalArgumentException("Invalid value for field '" + field + "'. Expected boolean.");
        }
        return (Boolean) value;
    }

    /**
     * Coerces {@code value} to a primitive {@code int}.
     * Accepts any {@link Number} subtype (JSON parsers may produce {@code Long} or {@code Double});
     * throws {@link IllegalArgumentException} for non-numeric types.
     */
    private int asInteger(Object value, String field) {
        if (value instanceof Integer) {
            return (Integer) value;
        }
        if (value instanceof Number) {
            // JSON deserializers may return Long or Double for numeric values.
            return ((Number) value).intValue();
        }
        throw new IllegalArgumentException("Invalid value for field '" + field + "'. Expected integer.");
    }

}
