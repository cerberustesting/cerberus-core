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
import org.cerberus.core.crud.entity.Invariant;
import org.cerberus.core.crud.entity.TestCaseCountry;
import org.cerberus.core.crud.service.IInvariantService;
import org.cerberus.core.crud.service.ITestCaseCountryService;
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
 * MCP tool that removes a country association from a testcase.
 *
 * <p>Exposes the MCP tool named {@code cerberus_testcase_country_delete}.
 * Delegates deletion to {@link ITestCaseCountryService} and resolves the
 * allowed country enum values at startup via {@link IInvariantService}
 * (invariant idName {@code "COUNTRY"}).
 *
 * <p>The tool first verifies the country-to-testcase association exists before
 * attempting deletion, returning a descriptive error if the record is absent.
 */
@Component
public class DeleteTestCaseCountryTool implements MCPTool {

    private static final String TOOL_NAME = "cerberus_testcase_country_delete";

    private final ITestCaseCountryService testCaseCountryService;
    private final IInvariantService invariantService;
    private final MCPLogUtils mcpLogUtils;

    public DeleteTestCaseCountryTool(
            ITestCaseCountryService testCaseCountryService,
            IInvariantService invariantService,
            MCPLogUtils mcpLogUtils) {
        this.testCaseCountryService = testCaseCountryService;
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
     * Builds the {@link McpSchema.Tool} descriptor registered with the MCP server.
     *
     * <p>The allowed country values are fetched at startup from the invariant table
     * (idName {@code "COUNTRY"}) and injected as a JSON {@code enum} on the
     * {@code country} parameter. If the invariant table is unavailable at startup
     * (e.g. no HTTP request context on the SSE thread), the enum is omitted and
     * any string is accepted.
     */
    private McpSchema.Tool createTool() {
        List<String> countryValues = loadInvariantValues("COUNTRY");

        Map<String, Object> countryProperty = new LinkedHashMap<>();
        countryProperty.put("type", "string");
        countryProperty.put("description", "Country code to remove from the testcase.");
        if (!countryValues.isEmpty()) countryProperty.put("enum", countryValues);

        Map<String, Object> properties = new LinkedHashMap<>();
        properties.put("testFolder", Map.of(
                "type", "string",
                "description", "Name of the test folder containing the testcase."
        ));
        properties.put("testcase", Map.of(
                "type", "string",
                "description", "Identifier of the testcase."
        ));
        properties.put("country", countryProperty);

        return new McpSchema.Tool(
                TOOL_NAME,
                null,
                """
                Removes a country association from a testcase.

                Call this tool whenever the user asks to remove or disable a country on a testcase.
                Before calling this tool, confirm the country to remove with the user.

                Removing a country also removes all properties scoped to that country.
                Use cerberus_testcase_country_list to check which countries are currently associated before deleting.

                Do not call this tool when the user only asks to list or add countries.
                """,
                new McpSchema.JsonSchema(
                        "object",
                        properties,
                        List.of("testFolder", "testcase", "country"),
                        null,
                        null,
                        null
                ),
                null,
                MCPToolUtils.deleteAnnotations("Delete testcase country", false),
                null
        );
    }

    /**
     * Executes the delete operation for a testcase-country association.
     *
     * <p>Validates required parameters, confirms the association exists via
     * {@link ITestCaseCountryService#readByKey}, then delegates deletion to
     * {@link ITestCaseCountryService#delete}. Returns a JSON success payload
     * or a descriptive error on any failure.
     */
    private McpSchema.CallToolResult execute(Map<String, Object> args) {
        String testFolder = MCPToolUtils.getString(args, "testFolder", "");
        String testcaseId = MCPToolUtils.getString(args, "testcase", "");
        String country = MCPToolUtils.getString(args, "country", "");

        mcpLogUtils.call(TOOL_NAME, "testcase_country_delete",
                String.format("MCP tool %s called with testFolder=%s testcase=%s country=%s",
                        TOOL_NAME, testFolder, testcaseId, country));

        if (testFolder.isBlank()) return MCPToolUtils.errorText("Missing required parameter: testFolder");
        if (testcaseId.isBlank()) return MCPToolUtils.errorText("Missing required parameter: testcase");
        if (country.isBlank()) return MCPToolUtils.errorText("Missing required parameter: country");

        // Verify the association exists before attempting deletion to return a clear error if absent.
        AnswerItem<TestCaseCountry> readAnswer = testCaseCountryService.readByKey(testFolder, testcaseId, country);

        if (!readAnswer.isCodeStringEquals("OK") || readAnswer.getItem() == null) {
            return MCPToolUtils.errorText("Country not associated: testFolder=" + testFolder
                    + " testcase=" + testcaseId + " country=" + country);
        }

        Answer answer = testCaseCountryService.delete(readAnswer.getItem());

        if (!answer.isCodeStringEquals("OK")) {
            return MCPToolUtils.errorText("Unable to remove country: " + answer.getMessageDescription());
        }

        return MCPToolUtils.successJson(Map.of(
                "status", "deleted",
                "testFolder", testFolder,
                "testcase", testcaseId,
                "country", country
        ));
    }

    /**
     * Loads the list of invariant values for a given {@code idName} from the database.
     *
     * <p>Used at tool-creation time to populate the {@code enum} constraint on
     * parameters such as {@code country}. Returns an empty list on failure so the
     * tool degrades gracefully instead of preventing startup.
     *
     * @param idName the invariant category identifier (e.g. {@code "COUNTRY"})
     * @return ordered list of valid values, or an empty list if unavailable
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
