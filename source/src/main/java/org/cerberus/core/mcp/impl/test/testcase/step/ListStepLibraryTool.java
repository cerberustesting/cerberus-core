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
import org.cerberus.core.crud.entity.TestCaseStepAction;
import org.cerberus.core.crud.service.ITestCaseStepActionService;
import org.cerberus.core.crud.service.ITestCaseStepService;
import org.cerberus.core.exception.CerberusException;
import org.cerberus.core.mcp.MCPTool;
import org.cerberus.core.mcp.util.MCPLogUtils;
import org.cerberus.core.mcp.util.MCPPagination;
import org.cerberus.core.mcp.util.MCPToolUtils;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.ArrayList;
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

    /** How many library steps a listing returns when the caller does not say. */
    private static final int DEFAULT_LIMIT = 50;

    private final ITestCaseStepService testCaseStepService;
    private final ITestCaseStepActionService testCaseStepActionService;
    private final IInvariantService invariantService;
    private final MCPLogUtils mcpLogUtils;

    public ListStepLibraryTool(ITestCaseStepService testCaseStepService,
                               ITestCaseStepActionService testCaseStepActionService,
                               IInvariantService invariantService, MCPLogUtils mcpLogUtils) {
        this.testCaseStepService = testCaseStepService;
        this.testCaseStepActionService = testCaseStepActionService;
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
        properties.put("detectDuplicates", Map.of(
                "type", "boolean",
                "description", "Group the library steps that do the same thing, so near-identical copies of "
                        + "one step show up as a group instead of as two unrelated entries. Two steps match "
                        + "when their actions have the same types and the same operands, whatever their "
                        + "descriptions say. Defaults to false because it reads the actions of every step "
                        + "listed."
        ));
        properties.put("search", Map.of(
                "type", "string",
                "description", "Optional text filter on step description, testcase, or test folder."
        ));

        // Copied because several of these tools build their properties with Map.of, which is
        // immutable; the copy keeps one insertion point for every listing.
        properties = new LinkedHashMap<>(properties);
        MCPPagination.declare(properties, DEFAULT_LIMIT, "library steps");

        return new McpSchema.Tool(
                TOOL_NAME,
                null,
                """
                Returns the list of reusable library steps available in Cerberus for a given system.

                Call this tool whenever the user needs to find or select a library step to reuse in a testcase.
                Library steps are steps marked as reusable (isLibraryStep=true) that can be called from other testcases.

                Use testFolder to narrow the search to a specific folder.
                Use search to filter by description or testcase name.

                This searches how steps are named, not what they do. To find steps by their content — a
                selector, a URL, a property they read — use cerberus_testcase_search_content instead.

                Once you have found the step you want, make a testcase run it with
                cerberus_testcase_step_library_use.

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

        List<TestCaseStep> matching = steps.stream()
                .filter(step -> matchesSearch(step, search))
                .toList();
        List<Map<String, Object>> result = matching.stream().map(this::toMap).toList();

        MCPPagination.Window window = MCPPagination.of(args, DEFAULT_LIMIT);
        List<Map<String, Object>> page = MCPPagination.slice(result, window);

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("system", system);
        response.put("count", page.size());
        MCPPagination.describe(response, window, result.size(), page.size(), "library steps");
        response.put("steps", page);

        if (MCPToolUtils.getBoolean(args, "detectDuplicates", false)) {
            List<Map<String, Object>> duplicates = findDuplicates(matching);
            response.put("duplicateGroups", duplicates);
            if (!duplicates.isEmpty()) {
                response.put("duplicateNote", "Each group below holds library steps whose actions are "
                        + "identical. They are separate steps, so a fix applied to one leaves the others "
                        + "wrong — which is how the same defect survives a repair. Keep one, point the "
                        + "testcases using the others at it with cerberus_testcase_step_library_use, then "
                        + "delete the copies. Check what still uses each one before deleting.");
            }
        }

        return MCPToolUtils.successJson(response);
    }

    /**
     * Groups the steps whose actions are identical.
     *
     * <p>A library exists to hold one copy of a shared sequence, and it drifts the other way on its
     * own: someone needing an existing step does not find it — the search only ever looked at
     * descriptions — and writes it again. The two copies then diverge, and a fix applied to one
     * silently leaves the other broken.</p>
     *
     * <p>Two steps are treated as the same when their actions line up on type and operands. The
     * descriptions are deliberately ignored: they are exactly what differs between two copies of one
     * sequence, and comparing them would hide every pair worth finding. Conditions and options are
     * ignored too — a copy that differs only by a timeout is still a copy.</p>
     */
    private List<Map<String, Object>> findDuplicates(List<TestCaseStep> steps) {
        Map<String, List<TestCaseStep>> bySignature = new LinkedHashMap<>();
        for (TestCaseStep step : steps) {
            String signature = signatureOf(step);
            if (signature == null) {
                // A step with no action has nothing to compare; reporting every empty step as a
                // duplicate of every other one would drown the real findings.
                continue;
            }
            bySignature.computeIfAbsent(signature, key -> new ArrayList<>()).add(step);
        }

        List<Map<String, Object>> groups = new ArrayList<>();
        for (List<TestCaseStep> group : bySignature.values()) {
            if (group.size() < 2) {
                continue;
            }
            Map<String, Object> described = new LinkedHashMap<>();
            described.put("actionCount", testCaseStepActionService
                    .getListOfAction(group.get(0).getTest(), group.get(0).getTestcase(), group.get(0).getStepId())
                    .size());
            described.put("steps", group.stream().map(step -> {
                Map<String, Object> one = new LinkedHashMap<>();
                one.put("testFolder", step.getTest());
                one.put("testcase", step.getTestcase());
                one.put("stepId", step.getStepId());
                one.put("description", MCPToolUtils.nullSafe(step.getDescription()));
                return one;
            }).toList());
            groups.add(described);
        }
        return groups;
    }

    /**
     * A fingerprint of what a step does.
     *
     * @return the fingerprint, or {@code null} when the step has no action to fingerprint.
     */
    private String signatureOf(TestCaseStep step) {
        List<TestCaseStepAction> actions =
                testCaseStepActionService.getListOfAction(step.getTest(), step.getTestcase(), step.getStepId());
        if (actions == null || actions.isEmpty()) {
            return null;
        }
        StringBuilder signature = new StringBuilder();
        for (TestCaseStepAction action : actions) {
            signature.append(MCPToolUtils.nullSafe(action.getAction())).append('\u0001')
                    .append(MCPToolUtils.nullSafe(action.getValue1())).append('\u0001')
                    .append(MCPToolUtils.nullSafe(action.getValue2())).append('\u0001')
                    .append(MCPToolUtils.nullSafe(action.getValue3())).append('\u0002');
        }
        return signature.toString();
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
