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
package org.cerberus.core.mcp.impl.test.testcase.country;

import io.modelcontextprotocol.server.McpServerFeatures;
import io.modelcontextprotocol.spec.McpSchema;
import org.cerberus.core.crud.entity.TestCaseCountry;
import org.cerberus.core.crud.service.ITestCaseCountryService;
import org.cerberus.core.mcp.MCPTool;
import org.cerberus.core.mcp.util.MCPLogUtils;
import org.cerberus.core.mcp.util.MCPToolUtils;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

/**
 * MCP tool that lists the countries associated with a given test case.
 *
 * <p>Exposes the MCP tool {@code cerberus_testcase_country_list}. A test case can only
 * be executed for countries it is explicitly linked to, so this tool is used to
 * inspect that set of countries.</p>
 *
 * <p>Delegates to {@link ITestCaseCountryService#findTestCaseCountryByTestTestCase}
 * to retrieve the country rows from the {@code testcasecountry} table.</p>
 *
 * <p>Note: the tool schema has no enum list for countries — it only accepts a
 * {@code testFolder} and {@code testcase} identifier and returns the linked countries
 * as a plain sorted list of country codes.</p>
 */
@Component
public class ListTestCaseCountriesTool implements MCPTool {

    private static final String TOOL_NAME = "cerberus_testcase_country_list";

    private final ITestCaseCountryService testCaseCountryService;
    private final MCPLogUtils mcpLogUtils;

    public ListTestCaseCountriesTool(ITestCaseCountryService testCaseCountryService, MCPLogUtils mcpLogUtils) {
        this.testCaseCountryService = testCaseCountryService;
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
     * Builds the {@link McpSchema.Tool} descriptor registered with the MCP server.
     *
     * <p>The input schema requires both {@code testFolder} and {@code testcase}.
     * No enum values are loaded from the invariant table — country validation is
     * performed downstream by the service rather than at the schema level.</p>
     *
     * <p>The tool is annotated as read-only because it only queries data.</p>
     */
    private McpSchema.Tool createTool() {
        Map<String, Object> properties = Map.of(
                "testFolder", Map.of(
                        "type", "string",
                        "description", "Name of the test folder containing the testcase."
                ),
                "testcase", Map.of(
                        "type", "string",
                        "description", "Identifier of the testcase."
                )
        );

        return new McpSchema.Tool(
                TOOL_NAME,
                null,
                """
                Returns the list of countries associated with a testcase.

                Call this tool whenever the user needs to see which countries are enabled on a testcase.
                A testcase can only be executed for countries it is associated with.

                Do not call this tool when the user asks to add or remove countries from a testcase.
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
                MCPToolUtils.readOnlyAnnotations("List testcase countries", true),
                null
        );
    }

    /**
     * Executes the tool: validates required arguments, fetches the country rows for
     * the given test case, and returns a sorted list of country codes as JSON.
     *
     * <p>Country codes are sorted alphabetically so that the result is deterministic
     * regardless of the DB row ordering.</p>
     *
     * @param args the raw MCP request arguments map
     * @return a {@link McpSchema.CallToolResult} containing the countries list or an error message
     */
    private McpSchema.CallToolResult execute(Map<String, Object> args) {
        String testFolder = MCPToolUtils.getString(args, "testFolder", "");
        String testcaseId = MCPToolUtils.getString(args, "testcase", "");

        mcpLogUtils.call(TOOL_NAME, "testcase_country_list",
                String.format("MCP tool %s called with testFolder=%s testcase=%s", TOOL_NAME, testFolder, testcaseId));

        if (testFolder.isBlank()) return MCPToolUtils.errorText("Missing required parameter: testFolder");
        if (testcaseId.isBlank()) return MCPToolUtils.errorText("Missing required parameter: testcase");

        List<TestCaseCountry> countries = testCaseCountryService.findTestCaseCountryByTestTestCase(testFolder, testcaseId);

        // Extract only the country code string from each entity; sort for stable output.
        List<String> countryCodes = countries.stream()
                .map(TestCaseCountry::getCountry)
                .sorted()
                .toList();

        return MCPToolUtils.successJson(Map.of(
                "testFolder", testFolder,
                "testcase", testcaseId,
                "count", countryCodes.size(),
                "countries", countryCodes
        ));
    }
}
