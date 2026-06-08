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
import org.cerberus.core.crud.entity.Test;
import org.cerberus.core.crud.entity.TestCase;
import org.cerberus.core.crud.service.IApplicationService;
import org.cerberus.core.crud.service.IInvariantService;
import org.cerberus.core.crud.service.ITestCaseService;
import org.cerberus.core.crud.service.ITestService;
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
 * MCP tool that manages the creation of {@link TestCase} entities in Cerberus.
 *
 * <p>Exposes the MCP tool named {@value TOOL_NAME}. When invoked, it validates
 * the supplied parameters against live Cerberus data (test folders, applications,
 * TCSTATUS and PRIORITY invariants), auto-assigns the testcase ID when none is
 * provided, and delegates the actual persistence to {@link ITestCaseService#create}.
 *
 * <p>The tool schema is populated at registration time with enum values fetched
 * from the database so that MCP clients can offer guided input.
 */
@Component
public class CreateTestCaseTool implements MCPTool {

    private static final String TOOL_NAME = "cerberus_testcase_create";

    private final ITestCaseService testCaseService;
    private final ITestService testService;
    private final IApplicationService applicationService;
    private final IInvariantService invariantService;
    private final MCPLogUtils mcpLogUtils;

    public CreateTestCaseTool(ITestCaseService testCaseService, ITestService testService, IApplicationService applicationService, IInvariantService invariantService, MCPLogUtils mcpLogUtils) {
        this.testCaseService = testCaseService;
        this.testService = testService;
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
     * Builds the MCP {@link McpSchema.Tool} descriptor for this tool.
     *
     * <p>Enum values for testFolder, application, status, and priority are loaded
     * from the database at registration time so that MCP clients receive a
     * constrained list. If any load fails, the property is left without an enum
     * and the LLM must infer a valid value from context.
     *
     * @return the fully described MCP tool schema.
     */
    private McpSchema.Tool createTool() {
        List<String> testFolderValues = loadTestFolderValues();
        List<String> applicationValues = loadApplicationValues();
        List<String> statusValues = loadInvariantValues("TCSTATUS");
        List<String> priorityValues = loadInvariantValues("PRIORITY");

        Map<String, Object> testFolderProperty = new LinkedHashMap<>();
        testFolderProperty.put("type", "string");
        testFolderProperty.put("description", "Name of the test folder that will contain this testcase.");
        if (!testFolderValues.isEmpty()) testFolderProperty.put("enum", testFolderValues);

        Map<String, Object> applicationProperty = new LinkedHashMap<>();
        applicationProperty.put("type", "string");
        applicationProperty.put("description", "Name of the application under test.");
        if (!applicationValues.isEmpty()) applicationProperty.put("enum", applicationValues);

        Map<String, Object> statusProperty = new LinkedHashMap<>();
        statusProperty.put("type", "string");
        statusProperty.put("description", "Lifecycle status of the testcase.");
        if (!statusValues.isEmpty()) statusProperty.put("enum", statusValues);

        Map<String, Object> priorityProperty = new LinkedHashMap<>();
        priorityProperty.put("type", "string");
        priorityProperty.put("description", "Priority of the testcase.");
        if (!priorityValues.isEmpty()) priorityProperty.put("enum", priorityValues);

        Map<String, Object> properties = new LinkedHashMap<>();
        properties.put("testFolder", testFolderProperty);
        properties.put("testcase", Map.of(
                "type", "string",
                "description", "Unique identifier of the testcase within the folder. If not provided, the next available ID is automatically assigned."
        ));
        properties.put("description", Map.of(
                "type", "string",
                "description", "Short description of what the testcase verifies."
        ));
        properties.put("application", applicationProperty);
        properties.put("type", Map.of(
                "type", "string",
                "description", "Type of testcase: MANUAL (executed by a human), AUTOMATED (executed by a robot), PRIVATE (library step, not directly executed).",
                "enum", List.of("MANUAL", "AUTOMATED", "PRIVATE")
        ));
        properties.put("status", statusProperty);
        properties.put("priority", priorityProperty);
        properties.put("comment", Map.of(
                "type", "string",
                "description", "Optional free-text comment about the testcase."
        ));

        return new McpSchema.Tool(
                TOOL_NAME,
                null,
                """
                Creates a new testcase in Cerberus inside a given test folder.

                Call this tool whenever the user asks to create, add, or define a new testcase or test scenario.
                Requires a test folder name and a description.

                Before calling this tool, propose the most coherent testFolder and application based on context,
                and confirm the full set of parameters with the user before proceeding.

                The testcase ID is auto-assigned if not provided.
                Use cerberus_testcase_step_create after this tool to add steps and actions.

                Do not call this tool when the user only asks to list, read, update, or delete testcases.
                """,
                new McpSchema.JsonSchema(
                        "object",
                        properties,
                        List.of("testFolder", "description"),
                        null,
                        null,
                        null
                ),
                null,
                MCPToolUtils.createAnnotations("Create testcase", false),
                null
        );
    }

    /**
     * Loads the names of all test folders available in Cerberus to populate the
     * {@code testFolder} enum in the tool schema.
     *
     * @return list of test folder names, or an empty list if the query fails.
     *
     * Catches Exception (not CerberusException) because this runs on the MCP SSE
     * thread at startup, where RequestContextHolder has no bound HTTP request —
     * some service internals throw NullPointerException before the HTTP context
     * is available.
     */
    private List<String> loadTestFolderValues() {
        try {
            return testService.convert(testService.readAll())
                    .stream()
                    .map(Test::getTest)
                    .toList();
        } catch (Exception e) {
            return Collections.emptyList();
        }
    }

    /**
     * Loads the names of all applications available in Cerberus to populate the
     * {@code application} enum in the tool schema.
     *
     * @return list of application names, or an empty list if the query fails.
     *
     * Catches Exception (not CerberusException) because this runs on the MCP SSE
     * thread at startup, where RequestContextHolder has no bound HTTP request —
     * some service internals throw NullPointerException before the HTTP context
     * is available.
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
     * Loads the allowed values for a given invariant type (e.g. {@code TCSTATUS},
     * {@code PRIORITY}) to populate enum fields in the tool schema.
     *
     * @param idName the invariant type identifier.
     * @return list of invariant values, or an empty list if the query fails.
     *
     * Catches Exception (not CerberusException) because this runs on the MCP SSE
     * thread at startup, where RequestContextHolder has no bound HTTP request —
     * some service internals throw NullPointerException before the HTTP context
     * is available.
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
     * Executes the testcase creation logic.
     *
     * <p>Validates the test folder, application, status, and priority before
     * building and persisting the {@link TestCase}. If no testcase ID is
     * supplied, the next available ID is automatically computed by the service.
     *
     * @param args the MCP tool arguments map.
     * @return a success JSON result with the created identifiers, or an error
     *         text result describing the first validation or persistence failure.
     */
    private McpSchema.CallToolResult execute(Map<String, Object> args) {
        String testFolder = MCPToolUtils.getString(args, "testFolder", "");
        String testcaseId = MCPToolUtils.getString(args, "testcase", "");
        String description = MCPToolUtils.getString(args, "description", "");
        String application = MCPToolUtils.getString(args, "application", "");
        String type = MCPToolUtils.getString(args, "type", TestCase.TESTCASE_TYPE_MANUAL);
        String status = MCPToolUtils.getString(args, "status", TestCase.TESTCASE_STATUS_WORKING);
        String priorityValue = MCPToolUtils.getString(args, "priority", "4");
        String comment = MCPToolUtils.getString(args, "comment", "");

        mcpLogUtils.call(TOOL_NAME, "testcase_create", String.format("MCP tool %s called with testFolder=%s", TOOL_NAME, testFolder));

        if (testFolder.isBlank()) {
            return MCPToolUtils.errorText("Missing required parameter: testFolder");
        }

        if (description.isBlank()) {
            return MCPToolUtils.errorText("Missing required parameter: description");
        }

        // Validate testFolder exists
        if (!testService.exist(testFolder)) {
            return MCPToolUtils.errorText("Test folder does not exist: '" + testFolder + "'.");
        }

        // Validate application exists
        if (!application.isBlank() && !applicationService.exist(application)) {
            return MCPToolUtils.errorText("Application does not exist: '" + application + "'.");
        }

        // Validate status against TCSTATUS invariants
        AnswerItem<Invariant> statusInvariant = invariantService.readByKey("TCSTATUS", status);
        if (!statusInvariant.isCodeStringEquals("OK") || statusInvariant.getItem() == null) {
            return MCPToolUtils.errorText("Invalid status: '" + status + "'. Call cerberus_invariant_list with type=TCSTATUS to get valid values.");
        }

        // Validate priority against PRIORITY invariants
        AnswerItem<Invariant> priorityInvariant = invariantService.readByKey("PRIORITY", priorityValue);
        if (!priorityInvariant.isCodeStringEquals("OK") || priorityInvariant.getItem() == null) {
            return MCPToolUtils.errorText("Invalid priority: '" + priorityValue + "'. Call cerberus_invariant_list with type=PRIORITY to get valid values.");
        }

        int priority;
        try {
            priority = Integer.parseInt(priorityValue);
        } catch (NumberFormatException e) {
            return MCPToolUtils.errorText("Priority must be a numeric value, got: '" + priorityValue + "'.");
        }

        if (testcaseId.isBlank()) {
            testcaseId = testCaseService.getNextAvailableTestcaseId(testFolder);
        } else {
            // Reject creation if a testcase with the same key already exists
            AnswerItem<TestCase> existing = testCaseService.readByKey(testFolder, testcaseId);
            if (existing.isCodeStringEquals("OK") && existing.getItem() != null) {
                return MCPToolUtils.errorText("Testcase already exists: testFolder=" + testFolder + " testcase=" + testcaseId);
            }
        }

        TestCase testCase = new TestCase();
        testCase.setTest(testFolder);
        testCase.setTestcase(testcaseId);
        testCase.setDescription(description);
        testCase.setApplication(application);
        testCase.setType(type);
        testCase.setStatus(status);
        testCase.setPriority(priority);
        testCase.setComment(comment);
        testCase.setActive(true);
        // Tag the creator as "MCP" so audit trails identify AI-originated records
        testCase.setUsrCreated("MCP");

        Answer answer = testCaseService.create(testCase);

        if (!answer.isCodeStringEquals("OK")) {
            return MCPToolUtils.errorText("Unable to create testcase: " + answer.getMessageDescription());
        }

        return MCPToolUtils.successJson(Map.of(
                "status", "created",
                "testFolder", testFolder,
                "testcase", testcaseId
        ));
    }

}