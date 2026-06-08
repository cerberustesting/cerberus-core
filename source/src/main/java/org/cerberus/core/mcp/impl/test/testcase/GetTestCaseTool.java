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
import org.cerberus.core.crud.entity.TestCase;
import org.cerberus.core.crud.entity.TestCaseStep;
import org.cerberus.core.crud.entity.TestCaseStepAction;
import org.cerberus.core.crud.entity.TestCaseStepActionControl;
import org.cerberus.core.crud.service.ITestCaseService;
import org.cerberus.core.mcp.MCPTool;
import org.cerberus.core.mcp.util.MCPLogUtils;
import org.cerberus.core.mcp.util.MCPToolUtils;
import org.cerberus.core.util.answer.AnswerItem;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * MCP tool that retrieves a single {@link TestCase} entity from Cerberus.
 *
 * <p>Exposes the MCP tool {@code cerberus_testcase_get}. Callers identify the
 * testcase by its (testFolder, testcase) primary key. An optional {@code withSteps}
 * flag causes the full step/action/control tree to be loaded and returned alongside
 * the testcase metadata.</p>
 *
 * <p>Delegates to {@link ITestCaseService}:
 * <ul>
 *   <li>{@code readByKey} — lightweight fetch of testcase header fields only.</li>
 *   <li>{@code readByKeyWithDependency} — eager fetch that also populates steps,
 *       actions and controls.</li>
 * </ul>
 * </p>
 */
@Component
public class GetTestCaseTool implements MCPTool {

    private static final String TOOL_NAME = "cerberus_testcase_get";

    private final ITestCaseService testCaseService;
    private final MCPLogUtils mcpLogUtils;

    public GetTestCaseTool(ITestCaseService testCaseService, MCPLogUtils mcpLogUtils) {
        this.testCaseService = testCaseService;
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
     * Builds the MCP tool descriptor, including its JSON Schema for input validation.
     *
     * <p>{@code withSteps} is intentionally optional (not listed in {@code required})
     * so callers that only need metadata do not pay the cost of loading the full tree.</p>
     *
     * @return the tool descriptor registered with the MCP server
     */
    private McpSchema.Tool createTool() {
        Map<String, Object> properties = Map.of(
                "testFolder", Map.of(
                        "type", "string",
                        "description", "Name of the test folder containing the testcase."
                ),
                "testcase", Map.of(
                        "type", "string",
                        "description", "Identifier of the testcase to retrieve."
                ),
                "withSteps", Map.of(
                        "type", "boolean",
                        "description", "If true, includes steps, actions and controls in the response. Defaults to false."
                )
        );

        return new McpSchema.Tool(
                TOOL_NAME,
                null,
                """
                Retrieves a single testcase from Cerberus by test folder and testcase ID.

                Call this tool whenever the user asks to get, inspect, or display the details of a specific testcase.
                Use withSteps=true to also retrieve the full step/action/control tree.

                Use cerberus_testcase_list instead when the testcase ID is unknown or when listing multiple testcases.
                Do not call this tool when the user asks to create, update, or delete a testcase.
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
                MCPToolUtils.createAnnotations("Get testcase", true),
                null
        );
    }

    /**
     * Executes the testcase retrieval.
     *
     * <p>Chooses between two service methods based on whether the caller needs steps:
     * <ul>
     *   <li>{@code readByKeyWithDependency} — when {@code withSteps=true}; loads the
     *       full step/action/control graph in a single call.</li>
     *   <li>{@code readByKey} — when {@code withSteps=false}; avoids the join-heavy
     *       dependency query and returns only header fields.</li>
     * </ul>
     * </p>
     *
     * @param args raw MCP argument map from the caller
     * @return a JSON result containing the testcase, or an error text on failure
     */
    private McpSchema.CallToolResult execute(Map<String, Object> args) {
        String testFolder = MCPToolUtils.getString(args, "testFolder", "");
        String testcaseId = MCPToolUtils.getString(args, "testcase", "");
        boolean withSteps = MCPToolUtils.getBoolean(args, "withSteps", false);

        mcpLogUtils.call(TOOL_NAME, "testcase_get",
                String.format("MCP tool %s called with testFolder=%s testcase=%s", TOOL_NAME, testFolder, testcaseId));

        if (testFolder.isBlank()) {
            return MCPToolUtils.errorText("Missing required parameter: testFolder");
        }

        if (testcaseId.isBlank()) {
            return MCPToolUtils.errorText("Missing required parameter: testcase");
        }

        if (withSteps) {
            // Use the dependency-aware read to eagerly load steps, actions and controls.
            AnswerItem<TestCase> answer = testCaseService.readByKeyWithDependency(testFolder, testcaseId);
            if (!answer.isCodeStringEquals("OK") || answer.getItem() == null) {
                return MCPToolUtils.errorText("Testcase does not exist: testFolder=" + testFolder + " testcase=" + testcaseId);
            }
            return MCPToolUtils.successJson(toMapWithSteps(answer.getItem()));
        }

        AnswerItem<TestCase> answer = testCaseService.readByKey(testFolder, testcaseId);
        if (!answer.isCodeStringEquals("OK") || answer.getItem() == null) {
            return MCPToolUtils.errorText("Testcase does not exist: testFolder=" + testFolder + " testcase=" + testcaseId);
        }
        return MCPToolUtils.successJson(toMap(answer.getItem()));
    }

    /**
     * Serialises a {@link TestCase} to a flat map of header fields, without steps.
     *
     * @param tc the testcase entity to convert
     * @return an ordered map of testcase header fields
     */
    private Map<String, Object> toMap(TestCase tc) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("testFolder", tc.getTest());
        map.put("testcase", tc.getTestcase());
        map.put("description", MCPToolUtils.nullSafe(tc.getDescription()));
        map.put("application", MCPToolUtils.nullSafe(tc.getApplication()));
        map.put("type", MCPToolUtils.nullSafe(tc.getType()));
        map.put("status", MCPToolUtils.nullSafe(tc.getStatus()));
        map.put("priority", tc.getPriority());
        map.put("isActive", tc.isActive());
        map.put("comment", MCPToolUtils.nullSafe(tc.getComment()));
        map.put("usrCreated", MCPToolUtils.nullSafe(tc.getUsrCreated()));
        map.put("usrModif", MCPToolUtils.nullSafe(tc.getUsrModif()));
        return map;
    }

    /**
     * Serialises a {@link TestCase} together with its full step/action/control tree.
     *
     * <p>Reuses {@link #toMap(TestCase)} for the header fields so the two output
     * shapes remain consistent regardless of the {@code withSteps} flag.</p>
     *
     * @param tc the testcase entity, already loaded with dependencies
     * @return an ordered map containing header fields and a {@code steps} list
     */
    private Map<String, Object> toMapWithSteps(TestCase tc) {
        Map<String, Object> map = toMap(tc);
        if (tc.getSteps() != null) {
            map.put("steps", tc.getSteps().stream().map(this::stepToMap).toList());
        }
        return map;
    }

    /**
     * Serialises a {@link TestCaseStep} to a map, including its nested actions.
     *
     * @param step the step entity to convert
     * @return an ordered map of step fields
     */
    private Map<String, Object> stepToMap(TestCaseStep step) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("stepId", step.getStepId());
        map.put("sort", step.getSort());
        map.put("description", MCPToolUtils.nullSafe(step.getDescription()));
        map.put("loop", MCPToolUtils.nullSafe(step.getLoop()));
        if (step.getActions() != null) {
            map.put("actions", step.getActions().stream().map(this::actionToMap).toList());
        }
        return map;
    }

    /**
     * Serialises a {@link TestCaseStepAction} to a map, including its nested controls.
     *
     * @param action the action entity to convert
     * @return an ordered map of action fields
     */
    private Map<String, Object> actionToMap(TestCaseStepAction action) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("actionId", action.getActionId());
        map.put("sort", action.getSort());
        map.put("action", MCPToolUtils.nullSafe(action.getAction()));
        map.put("value1", MCPToolUtils.nullSafe(action.getValue1()));
        map.put("value2", MCPToolUtils.nullSafe(action.getValue2()));
        map.put("value3", MCPToolUtils.nullSafe(action.getValue3()));
        map.put("description", MCPToolUtils.nullSafe(action.getDescription()));
        map.put("isFatal", action.isFatal());
        if (action.getControls() != null) {
            map.put("controls", action.getControls().stream().map(this::controlToMap).toList());
        }
        return map;
    }

    /**
     * Serialises a {@link TestCaseStepActionControl} to a flat map.
     *
     * @param control the control entity to convert
     * @return an ordered map of control fields
     */
    private Map<String, Object> controlToMap(TestCaseStepActionControl control) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("controlId", control.getControlId());
        map.put("sort", control.getSort());
        map.put("control", MCPToolUtils.nullSafe(control.getControl()));
        map.put("value1", MCPToolUtils.nullSafe(control.getValue1()));
        map.put("value2", MCPToolUtils.nullSafe(control.getValue2()));
        map.put("value3", MCPToolUtils.nullSafe(control.getValue3()));
        map.put("description", MCPToolUtils.nullSafe(control.getDescription()));
        map.put("isFatal", control.isFatal());
        return map;
    }

}
