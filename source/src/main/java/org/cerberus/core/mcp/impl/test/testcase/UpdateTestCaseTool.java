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
package org.cerberus.core.mcp.impl.test.testcase;

import io.modelcontextprotocol.server.McpServerFeatures;
import io.modelcontextprotocol.spec.McpSchema;
import org.cerberus.core.crud.entity.Application;
import org.cerberus.core.crud.entity.Invariant;
import org.cerberus.core.crud.entity.TestCase;
import org.cerberus.core.crud.service.IApplicationService;
import org.cerberus.core.crud.service.IInvariantService;
import org.cerberus.core.crud.service.ITestCaseService;
import org.cerberus.core.mcp.MCPTool;
import org.cerberus.core.mcp.util.MCPLogUtils;
import org.cerberus.core.mcp.util.MCPToolUtils;
import org.cerberus.core.util.answer.Answer;
import org.cerberus.core.util.answer.AnswerItem;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * MCP tool that updates an existing {@link TestCase} entity in Cerberus.
 *
 * <p>Exposed MCP tool name: {@value TOOL_NAME}.</p>
 *
 * <p>Delegates persistence to {@link ITestCaseService#update}, existence checks to
 * {@link IApplicationService} and {@link IInvariantService}, and enum population
 * (application list, TCSTATUS, PRIORITY) to those same services at tool-definition time.</p>
 *
 * <p>Only the fields explicitly provided in the {@code updates} map are applied to the
 * existing entity; all other fields are left unchanged (patch semantics).</p>
 */
@Component
public class UpdateTestCaseTool implements MCPTool {

    private static final String TOOL_NAME = "cerberus_testcase_update";

    private final ITestCaseService testCaseService;
    private final IApplicationService applicationService;
    private final IInvariantService invariantService;
    private final MCPLogUtils mcpLogUtils;

    public UpdateTestCaseTool(ITestCaseService testCaseService, IApplicationService applicationService, IInvariantService invariantService, MCPLogUtils mcpLogUtils) {
        this.testCaseService = testCaseService;
        this.applicationService = applicationService;
        this.invariantService = invariantService;
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
     * Builds the {@link McpSchema.Tool} descriptor for this tool, including the JSON schema
     * with enum constraints populated from live Cerberus data (applications, statuses, priorities).
     *
     * <p>Enum values are loaded eagerly at server startup so the MCP client receives a bounded
     * set of valid choices. If loading fails the enum constraint is simply omitted, leaving the
     * field as a free-form string.</p>
     *
     * @return the fully-described MCP tool ready to be registered on the server
     */
    private McpSchema.Tool createTool() {
        List<String> applicationValues = loadApplicationValues();
        List<String> statusValues = loadInvariantValues("TCSTATUS");
        List<String> priorityValues = loadInvariantValues("PRIORITY");

        Map<String, Object> applicationProperty = new LinkedHashMap<>();
        applicationProperty.put("type", "string");
        applicationProperty.put("description", "New application under test.");
        if (!applicationValues.isEmpty()) applicationProperty.put("enum", applicationValues);

        Map<String, Object> statusProperty = new LinkedHashMap<>();
        statusProperty.put("type", "string");
        statusProperty.put("description", "New lifecycle status of the testcase.");
        if (!statusValues.isEmpty()) statusProperty.put("enum", statusValues);

        Map<String, Object> priorityProperty = new LinkedHashMap<>();
        priorityProperty.put("type", "string");
        priorityProperty.put("description", "New priority of the testcase.");
        if (!priorityValues.isEmpty()) priorityProperty.put("enum", priorityValues);

        Map<String, Object> updateProperties = new LinkedHashMap<>();
        updateProperties.put("description", Map.of(
                "type", "string",
                "description", "New short description of the testcase."
        ));
        updateProperties.put("application", applicationProperty);
        updateProperties.put("type", Map.of(
                "type", "string",
                "description", "New type of the testcase.",
                "enum", List.of("MANUAL", "AUTOMATED", "PRIVATE")
        ));
        updateProperties.put("status", statusProperty);
        updateProperties.put("priority", priorityProperty);
        updateProperties.put("isActive", Map.of(
                "type", "boolean",
                "description", "Whether the testcase is active."
        ));
        updateProperties.put("comment", Map.of(
                "type", "string",
                "description", "New free-text comment."
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
                "description", "Identifier of the testcase to update."
        ));
        properties.put("updates", updatesSchema);

        return new McpSchema.Tool(
                TOOL_NAME,
                null,
                """
                Updates an existing testcase in Cerberus.

                Call this tool whenever the user asks to modify, update, activate, deactivate,
                or change properties of an existing testcase.

                Only provide the fields that need to change in the updates object.

                Do not call this tool when the user only asks to list, read, create, or delete a testcase.
                Do not call this tool to add or modify steps — use cerberus_testcase_step_create for that.
                """,
                new McpSchema.JsonSchema(
                        "object",
                        properties,
                        List.of("testFolder", "testcase", "updates"),
                        null,
                        null,
                        null
                ),
                null,
                MCPToolUtils.updateAnnotations("Update testcase", false),
                null
        );
    }

    /**
     * Validates the incoming arguments, applies each requested field change to the loaded
     * {@link TestCase} entity (patch semantics), then persists the result via
     * {@link ITestCaseService#update}.
     *
     * <p>The existing entity is loaded first so that only the fields listed in {@code updates}
     * are modified — unspecified fields keep their current database values.</p>
     *
     * <p>For {@code application} the service existence check is performed before mutating the
     * entity so that an invalid value never reaches the DAO. For {@code status} and
     * {@code priority} the invariant lookup acts as the validation guard.</p>
     *
     * @param args raw MCP argument map from the client request
     * @return a {@link McpSchema.CallToolResult} with a JSON success payload or an error message
     */
    @SuppressWarnings("unchecked")
    private McpSchema.CallToolResult execute(Map<String, Object> args) {
        String testFolder = MCPToolUtils.getString(args, "testFolder", "");
        String testcaseId = MCPToolUtils.getString(args, "testcase", "");

        mcpLogUtils.call(TOOL_NAME, "testcase_update",
                String.format("MCP tool %s called with testFolder=%s testcase=%s", TOOL_NAME, testFolder, testcaseId));

        if (testFolder.isBlank()) {
            return MCPToolUtils.errorText("Missing required parameter: testFolder");
        }

        if (testcaseId.isBlank()) {
            return MCPToolUtils.errorText("Missing required parameter: testcase");
        }

        Object updatesObject = args.get("updates");
        if (!(updatesObject instanceof Map)) {
            return MCPToolUtils.errorText("Missing or invalid required parameter: updates");
        }

        Map<String, Object> updates = (Map<String, Object>) updatesObject;
        if (updates.isEmpty()) {
            return MCPToolUtils.errorText("No field provided to update.");
        }

        // Load the existing entity first so patch semantics are respected.
        AnswerItem<TestCase> existing = testCaseService.readByKey(testFolder, testcaseId);
        if (!existing.isCodeStringEquals("OK") || existing.getItem() == null) {
            return MCPToolUtils.errorText("Testcase does not exist: testFolder=" + testFolder + " testcase=" + testcaseId);
        }

        TestCase testCase = existing.getItem();
        Map<String, Object> modifiedFields = new LinkedHashMap<>();

        try {
            for (Map.Entry<String, Object> entry : updates.entrySet()) {
                String field = entry.getKey();
                Object value = entry.getValue();

                switch (field) {
                    case "description":
                        String description = asString(value, field);
                        testCase.setDescription(description);
                        modifiedFields.put(field, description);
                        break;

                    case "application":
                        String application = asString(value, field);
                        // Guard against assigning a non-existent application before touching the entity.
                        if (!application.isBlank() && !applicationService.exist(application)) {
                            return MCPToolUtils.errorText("Application does not exist: '" + application + "'.");
                        }
                        testCase.setApplication(application);
                        modifiedFields.put(field, application);
                        break;

                    case "type":
                        String type = asString(value, field);
                        testCase.setType(type);
                        modifiedFields.put(field, type);
                        break;

                    case "status":
                        String status = asString(value, field);
                        // Validate against the TCSTATUS invariant table rather than a hard-coded list.
                        AnswerItem<Invariant> statusInvariant = invariantService.readByKey("TCSTATUS", status);
                        if (!statusInvariant.isCodeStringEquals("OK") || statusInvariant.getItem() == null) {
                            return MCPToolUtils.errorText("Invalid status: '" + status + "'.");
                        }
                        testCase.setStatus(status);
                        modifiedFields.put(field, status);
                        break;

                    case "priority":
                        String priorityValue = asString(value, field);
                        // Validate against the PRIORITY invariant table before parsing to int.
                        AnswerItem<Invariant> priorityInvariant = invariantService.readByKey("PRIORITY", priorityValue);
                        if (!priorityInvariant.isCodeStringEquals("OK") || priorityInvariant.getItem() == null) {
                            return MCPToolUtils.errorText("Invalid priority: '" + priorityValue + "'.");
                        }
                        testCase.setPriority(Integer.parseInt(priorityValue));
                        modifiedFields.put(field, priorityValue);
                        break;

                    case "isActive":
                        boolean isActive = asBoolean(value, field);
                        testCase.setActive(isActive);
                        modifiedFields.put(field, isActive);
                        break;

                    case "comment":
                        String comment = asString(value, field);
                        testCase.setComment(comment);
                        modifiedFields.put(field, comment);
                        break;

                    default:
                        return MCPToolUtils.errorText("Unsupported field for testcase update: " + field);
                }
            }
        } catch (IllegalArgumentException e) {
            return MCPToolUtils.errorText(e.getMessage());
        }

        // Tag the modification so audit columns reflect the MCP caller.
        testCase.setUsrModif("MCP");

        Answer answer = testCaseService.update(testFolder, testcaseId, testCase);

        if (!answer.isCodeStringEquals("OK")) {
            return MCPToolUtils.errorText("Unable to update testcase: " + answer.getMessageDescription());
        }

        return MCPToolUtils.successJson(Map.of(
                "status", "updated",
                "testFolder", testFolder,
                "testcase", testcaseId,
                "updatedFields", modifiedFields
        ));
    }

    /**
     * Loads all application names from the database for use as enum constraints in the tool schema.
     *
     * <p>Catches {@link Exception} (not {@code CerberusException}) because this runs on the MCP
     * SSE thread at startup, where {@code RequestContextHolder} has no bound HTTP request — some
     * service internals throw {@link NullPointerException} before the HTTP context is available.
     * On any failure the constraint is omitted and the field accepts a free-form string.</p>
     *
     * @return ordered list of application identifiers, or an empty list on failure
     */
    private List<String> loadApplicationValues() {
        try {
            return applicationService.convert(applicationService.readAll())
                    .stream()
                    .map(Application::getApplication)
                    .toList();
        } catch (Exception e) {
            return Collections.emptyList();
        }
    }

    /**
     * Loads the allowed values for a given invariant group (e.g. {@code "TCSTATUS"},
     * {@code "PRIORITY"}) for use as enum constraints in the tool schema.
     *
     * <p>Catches {@link Exception} (not {@code CerberusException}) because this runs on the MCP
     * SSE thread at startup, where {@code RequestContextHolder} has no bound HTTP request — some
     * service internals throw {@link NullPointerException} before the HTTP context is available.
     * On any failure the constraint is omitted and the field accepts a free-form string.</p>
     *
     * @param idName the invariant group identifier to load (e.g. {@code "TCSTATUS"})
     * @return ordered list of invariant values, or an empty list on failure
     */
    private List<String> loadInvariantValues(String idName) {
        try {
            return invariantService.readByIdName(idName)
                    .stream()
                    .map(Invariant::getValue)
                    .toList();
        } catch (Exception e) {
            return Collections.emptyList();
        }
    }

    /**
     * Coerces an argument value to a trimmed {@link String}, or returns an empty string for
     * {@code null}. Throws {@link IllegalArgumentException} if the value is not a {@link String},
     * which is caught in {@link #execute} and converted into an MCP error response.
     *
     * @param value the raw argument value
     * @param field the field name, used in the error message
     * @return trimmed string value, never {@code null}
     */
    private String asString(Object value, String field) {
        if (value == null) return "";
        if (!(value instanceof String)) {
            throw new IllegalArgumentException("Invalid value for field '" + field + "'. Expected string.");
        }
        return ((String) value).trim();
    }

    /**
     * Coerces an argument value to a primitive {@code boolean}. Throws
     * {@link IllegalArgumentException} if the value is not a {@link Boolean}, which is caught in
     * {@link #execute} and converted into an MCP error response.
     *
     * @param value the raw argument value
     * @param field the field name, used in the error message
     * @return the boolean value
     */
    private boolean asBoolean(Object value, String field) {
        if (!(value instanceof Boolean)) {
            throw new IllegalArgumentException("Invalid value for field '" + field + "'. Expected boolean.");
        }
        return (Boolean) value;
    }

}
