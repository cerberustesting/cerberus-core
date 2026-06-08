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
import org.cerberus.core.crud.entity.Invariant;
import org.cerberus.core.crud.entity.TestCaseStep;
import org.cerberus.core.crud.service.IInvariantService;
import org.cerberus.core.crud.service.ITestCaseStepService;
import org.cerberus.core.exception.CerberusException;
import org.cerberus.core.mcp.MCPTool;
import org.cerberus.core.mcp.util.MCPLogUtils;
import org.cerberus.core.mcp.util.MCPToolUtils;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * MCP tool that lists reusable library steps available in a Cerberus system.
 *
 * <p>Exposes the MCP tool {@code cerberus_step_library_list}. A library step is a
 * {@link TestCaseStep} flagged as reusable ({@code isLibraryStep=true}); it can be
 * referenced from other test cases via {@code isUsingLibraryStep=true}.
 *
 * <p>Delegates to {@link ITestCaseStepService} for data retrieval and to
 * {@link IInvariantService} to populate the {@code system} enum at startup time
 * from the {@code SYSTEM} invariant table.
 *
 * <p>The optional in-memory text filter ({@code search}) is applied after the service
 * call because the underlying service methods do not expose a search predicate.
 */
@Component
public class ListStepLibraryTool implements MCPTool {

    private static final String TOOL_NAME = "cerberus_step_library_list";

    private final ITestCaseStepService testCaseStepService;
    private final IInvariantService invariantService;
    private final MCPLogUtils mcpLogUtils;

    public ListStepLibraryTool(ITestCaseStepService testCaseStepService, IInvariantService invariantService, MCPLogUtils mcpLogUtils) {
        this.testCaseStepService = testCaseStepService;
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
     * Builds the MCP {@link McpSchema.Tool} descriptor, including the JSON schema for
     * the tool's input parameters.
     *
     * <p>The {@code system} enum values are loaded at startup from the {@code SYSTEM}
     * invariant table via {@link #loadInvariantValues}. If that call fails (e.g. because
     * the HTTP request context is not yet available on the MCP SSE thread), the enum is
     * omitted and any string value is accepted.
     *
     * @return a fully described MCP tool ready to be registered with the MCP server
     */
    private McpSchema.Tool createTool() {
        List<String> systemValues = loadInvariantValues("SYSTEM");

        Map<String, Object> systemProperty = new LinkedHashMap<>();
        systemProperty.put("type", "string");
        systemProperty.put("description", "System (workspace) to search library steps in.");
        if (!systemValues.isEmpty()) systemProperty.put("enum", systemValues);

        Map<String, Object> properties = new LinkedHashMap<>();
        properties.put("system", systemProperty);
        properties.put("testFolder", Map.of(
                "type", "string",
                "description", "Optional test folder to narrow down the search."
        ));
        properties.put("search", Map.of(
                "type", "string",
                "description", "Optional text filter on step description, testcase, or test folder."
        ));

        return new McpSchema.Tool(
                TOOL_NAME,
                null,
                """
                Returns the list of reusable library steps available in Cerberus for a given system.

                Call this tool whenever the user needs to find or select a library step to reuse in a testcase.
                Library steps are steps marked as reusable (isLibraryStep=true) that can be called from other testcases.

                Use testFolder to narrow the search to a specific folder.
                Use search to filter by description or testcase name.

                The returned stepId, testcase, and testFolder are the identifiers needed to reference a library step
                when creating a testcase step with isUsingLibraryStep=true.
                """,
                new McpSchema.JsonSchema(
                        "object",
                        properties,
                        List.of("system"),
                        null,
                        null,
                        null
                ),
                null,
                MCPToolUtils.createAnnotations("List step library", true),
                null
        );
    }

    /**
     * Executes the tool: retrieves library steps for the given system (and optional
     * test folder), applies the optional search filter in memory, and returns the
     * results as JSON.
     *
     * @param args the parsed MCP request arguments
     * @return a {@link McpSchema.CallToolResult} containing the matching steps or an
     *         error description
     */
    private McpSchema.CallToolResult execute(Map<String, Object> args) {
        String system = MCPToolUtils.getString(args, "system", "");
        String testFolder = MCPToolUtils.getString(args, "testFolder", "");
        String search = MCPToolUtils.getString(args, "search", "");

        mcpLogUtils.call(TOOL_NAME, "step_library_list",
                String.format("MCP tool %s called with system=%s testFolder=%s", TOOL_NAME, system, testFolder));

        if (system.isBlank()) {
            return MCPToolUtils.errorText("Missing required parameter: system");
        }

        List<TestCaseStep> steps;
        try {
            // Use the narrower service method when a test folder is specified to reduce data transfer.
            steps = testFolder.isBlank()
                    ? testCaseStepService.getStepLibraryBySystem(system)
                    : testCaseStepService.getStepLibraryBySystemTest(system, testFolder);
        } catch (CerberusException e) {
            return MCPToolUtils.errorText("Unable to retrieve library steps: " + e.getMessage());
        }

        List<Map<String, Object>> result = steps.stream()
                .filter(step -> matchesSearch(step, search))
                .map(this::toMap)
                .toList();

        return MCPToolUtils.successJson(Map.of(
                "system", system,
                "count", result.size(),
                "steps", result
        ));
    }

    /**
     * Converts a {@link TestCaseStep} to a lightweight map containing only the fields
     * relevant for identifying and selecting a library step.
     *
     * @param step the step entity to convert
     * @return an ordered map with {@code testFolder}, {@code testcase}, {@code stepId},
     *         {@code sort}, {@code description}, and {@code loop}
     */
    private Map<String, Object> toMap(TestCaseStep step) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("testFolder", step.getTest());
        map.put("testcase", step.getTestcase());
        map.put("stepId", step.getStepId());
        map.put("sort", step.getSort());
        map.put("description", MCPToolUtils.nullSafe(step.getDescription()));
        map.put("loop", MCPToolUtils.nullSafe(step.getLoop()));
        return map;
    }

    /**
     * Returns {@code true} when the step matches the given search string, performing a
     * case-insensitive substring check against the description, testcase name, and test
     * folder. A blank search string matches every step.
     *
     * @param step   the step to test
     * @param search the filter string; may be null or blank
     * @return {@code true} if the step matches or no filter was provided
     */
    private boolean matchesSearch(TestCaseStep step, String search) {
        if (search == null || search.isBlank()) return true;
        return MCPToolUtils.containsIgnoreCase(step.getDescription(), search)
                || MCPToolUtils.containsIgnoreCase(step.getTestcase(), search)
                || MCPToolUtils.containsIgnoreCase(step.getTest(), search);
    }

    /**
     * Loads invariant values for the given invariant name from the database at startup.
     *
     * // Catches Exception (not CerberusException): runs on MCP SSE thread at startup where RequestContextHolder
     * // has no bound HTTP request — service internals may throw NullPointerException before HTTP context is available.
     *
     * @param idName the invariant identifier (e.g. {@code "SYSTEM"})
     * @return the list of invariant string values, or an empty list if the load fails
     */
    private List<String> loadInvariantValues(String idName) {
        try {
            return invariantService.readByIdName(idName)
                    .stream()
                    .map(Invariant::getValue)
                    .toList();
        } catch (Exception e) {
            // Catches Exception (not CerberusException): runs on MCP SSE thread at startup where RequestContextHolder
            // has no bound HTTP request — service internals may throw NullPointerException before HTTP context is available.
            return Collections.emptyList();
        }
    }

}
