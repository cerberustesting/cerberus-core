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
import org.cerberus.core.crud.service.ITestCaseService;
import org.cerberus.core.mcp.MCPTool;
import org.cerberus.core.mcp.util.MCPLogUtils;
import org.cerberus.core.mcp.util.MCPToolUtils;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * MCP tool that lists {@link TestCase} entities belonging to a given test folder.
 *
 * <p>Exposed MCP tool name: {@code cerberus_testcase_list}.</p>
 *
 * <p>Delegates to {@link ITestCaseService#findTestCaseByTest(String)} to fetch all test cases
 * for the requested folder, then applies optional in-memory filters (search, application,
 * status, type) before returning the result.</p>
 */
@Component
public class ListTestCasesTool implements MCPTool {

    private static final String TOOL_NAME = "cerberus_testcase_list";

    private final ITestCaseService testCaseService;
    private final MCPLogUtils mcpLogUtils;

    public ListTestCasesTool(ITestCaseService testCaseService, MCPLogUtils mcpLogUtils) {
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
     * Builds the MCP tool schema exposed to AI clients.
     *
     * <p>Declares {@code testFolder} as the only required parameter; all other filters
     * ({@code search}, {@code application}, {@code status}, {@code type}) are optional
     * and applied in-memory after fetching the full list from the service.</p>
     *
     * @return the fully configured {@link McpSchema.Tool} descriptor
     */
    private McpSchema.Tool createTool() {
        Map<String, Object> properties = Map.of(
                "testFolder", Map.of(
                        "type", "string",
                        "description", "Name of the test folder to list testcases from."
                ),
                "search", Map.of(
                        "type", "string",
                        "description", "Optional text filter on testcase ID, description, or application."
                ),
                "application", Map.of(
                        "type", "string",
                        "description", "Optional filter to return only testcases linked to this application."
                ),
                "status", Map.of(
                        "type", "string",
                        "description", "Optional filter to return only testcases with this status (e.g. WORKING)."
                ),
                "type", Map.of(
                        "type", "string",
                        "description", "Optional filter to return only testcases of this type (MANUAL, AUTOMATED, PRIVATE).",
                        "enum", List.of("MANUAL", "AUTOMATED", "PRIVATE")
                )
        );

        return new McpSchema.Tool(
                TOOL_NAME,
                null,
                """
                Returns the list of testcases in a given Cerberus test folder.

                Call this tool when the user needs to browse, search, or select a testcase within a folder.
                Use this tool before cerberus_testcase_get when the testcase ID is unknown.

                Use search to filter by description or application.
                Use application, status, or type to narrow down results.

                Do not call this tool when the user asks to create, update, or delete testcases.
                """,
                new McpSchema.JsonSchema(
                        "object",
                        properties,
                        List.of("testFolder"),
                        null,
                        null,
                        null
                ),
                null,
                MCPToolUtils.createAnnotations("List testcases", true),
                null
        );
    }

    /**
     * Fetches all test cases for the given folder and applies optional filters.
     *
     * <p>Filtering is done in-memory because {@link ITestCaseService#findTestCaseByTest(String)}
     * does not expose fine-grained query parameters — fetching the full list and streaming
     * predicates is the only available path.</p>
     *
     * @param args MCP call arguments extracted from the tool request
     * @return a {@link McpSchema.CallToolResult} containing the filtered list or an error message
     */
    private McpSchema.CallToolResult execute(Map<String, Object> args) {
        String testFolder = MCPToolUtils.getString(args, "testFolder", "");
        String search = MCPToolUtils.getString(args, "search", "");
        String filterApplication = MCPToolUtils.getString(args, "application", "");
        String filterStatus = MCPToolUtils.getString(args, "status", "");
        String filterType = MCPToolUtils.getString(args, "type", "");

        mcpLogUtils.call(TOOL_NAME, "testcase_list",
                String.format("MCP tool %s called with testFolder=%s", TOOL_NAME, testFolder));

        if (testFolder.isBlank()) {
            return MCPToolUtils.errorText("Missing required parameter: testFolder");
        }

        List<TestCase> testcases = testCaseService.findTestCaseByTest(testFolder)
                .stream()
                .filter(tc -> matchesSearch(tc, search))
                .filter(tc -> filterApplication.isBlank() || filterApplication.equalsIgnoreCase(tc.getApplication()))
                .filter(tc -> filterStatus.isBlank() || filterStatus.equalsIgnoreCase(tc.getStatus()))
                .filter(tc -> filterType.isBlank() || filterType.equalsIgnoreCase(tc.getType()))
                .toList();

        List<Map<String, Object>> result = testcases.stream()
                .map(this::toMap)
                .toList();

        return MCPToolUtils.successJson(Map.of(
                "testFolder", testFolder,
                "count", result.size(),
                "testcases", result
        ));
    }

    /**
     * Converts a {@link TestCase} entity into a flat map suitable for JSON serialisation.
     *
     * <p>Uses {@link LinkedHashMap} to preserve insertion order in the serialised output,
     * which makes responses more predictable for AI clients consuming the JSON.</p>
     *
     * @param tc the test case to convert
     * @return an ordered map of the test case's summary fields
     */
    private Map<String, Object> toMap(TestCase tc) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("testcase", tc.getTestcase());
        map.put("description", MCPToolUtils.nullSafe(tc.getDescription()));
        map.put("application", MCPToolUtils.nullSafe(tc.getApplication()));
        map.put("type", MCPToolUtils.nullSafe(tc.getType()));
        map.put("status", MCPToolUtils.nullSafe(tc.getStatus()));
        map.put("priority", tc.getPriority());
        map.put("isActive", tc.isActive());
        return map;
    }

    /**
     * Returns {@code true} when the test case matches the free-text search term.
     *
     * <p>Searches across testcase ID, description, and application so that an AI client
     * can locate a test case without knowing which field the term appears in.</p>
     *
     * @param tc     the test case to evaluate
     * @param search the search term; an empty or null value matches every test case
     * @return {@code true} if the test case matches or if {@code search} is blank
     */
    private boolean matchesSearch(TestCase tc, String search) {
        if (search == null || search.isBlank()) return true;
        return MCPToolUtils.containsIgnoreCase(tc.getTestcase(), search)
                || MCPToolUtils.containsIgnoreCase(tc.getDescription(), search)
                || MCPToolUtils.containsIgnoreCase(tc.getApplication(), search);
    }

}
