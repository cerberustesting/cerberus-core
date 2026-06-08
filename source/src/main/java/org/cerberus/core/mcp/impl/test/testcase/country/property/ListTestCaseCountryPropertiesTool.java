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
package org.cerberus.core.mcp.impl.test.testcase.country.property;

import io.modelcontextprotocol.server.McpServerFeatures;
import io.modelcontextprotocol.spec.McpSchema;
import org.cerberus.core.api.dto.testcase.TestcaseCountryPropertiesDTOV001;
import org.cerberus.core.api.dto.testcase.TestcaseCountryPropertiesMapperV001;
import org.cerberus.core.crud.entity.Invariant;
import org.cerberus.core.crud.entity.TestCaseCountryProperties;
import org.cerberus.core.crud.service.IInvariantService;
import org.cerberus.core.crud.service.ITestCaseCountryPropertiesService;
import org.cerberus.core.exception.CerberusException;
import org.cerberus.core.mcp.MCPTool;
import org.cerberus.core.mcp.util.MCPLogUtils;
import org.cerberus.core.mcp.util.MCPToolUtils;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * MCP tool that lists the country-scoped properties defined on a testcase.
 *
 * <p>Exposes the MCP tool {@code cerberus_testcase_country_property_list}.
 * Delegates data retrieval to {@link ITestCaseCountryPropertiesService} and uses
 * {@link TestcaseCountryPropertiesMapperV001} to convert entities to DTOs.</p>
 *
 * <p>Architectural note: the database returns one row per (property, country) pair.
 * This tool groups those rows by property name and aggregates the countries into
 * {@code invariantCountries} before mapping, so the caller receives one DTO per
 * property with a consolidated countries list.</p>
 */
@Component
public class ListTestCaseCountryPropertiesTool implements MCPTool {

    private static final String TOOL_NAME = "cerberus_testcase_country_property_list";

    private final ITestCaseCountryPropertiesService testCaseCountryPropertiesService;
    private final IInvariantService invariantService;
    private final TestcaseCountryPropertiesMapperV001 mapper;
    private final MCPLogUtils mcpLogUtils;

    public ListTestCaseCountryPropertiesTool(
            ITestCaseCountryPropertiesService testCaseCountryPropertiesService,
            IInvariantService invariantService,
            TestcaseCountryPropertiesMapperV001 mapper,
            MCPLogUtils mcpLogUtils) {
        this.testCaseCountryPropertiesService = testCaseCountryPropertiesService;
        this.invariantService = invariantService;
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
     * Builds the MCP tool descriptor, including the JSON Schema for the tool's input parameters.
     *
     * <p>The {@code country} enum is populated at startup from the {@code COUNTRY} invariant table.
     * A synthetic {@code "ALL"} entry is prepended so callers can explicitly request all countries.
     * If the invariant query fails at startup (e.g. no HTTP context on the SSE thread), the enum
     * is omitted and the parameter accepts any free-form string.</p>
     *
     * @return a fully-described {@link McpSchema.Tool} ready for MCP registration
     */
    private McpSchema.Tool createTool() {
        List<String> countryValues = loadInvariantValues("COUNTRY");

        Map<String, Object> countryProperty = new LinkedHashMap<>();
        countryProperty.put("type", "string");
        countryProperty.put("description", "Optional filter to return only properties for this country.");
        if (!countryValues.isEmpty()) {
            List<String> values = new ArrayList<>(countryValues);
            // Prepend "ALL" so the caller can explicitly request all countries without filtering
            values.add(0, "ALL");
            countryProperty.put("enum", values);
        }

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
        properties.put("search", Map.of(
                "type", "string",
                "description", "Optional text filter on property name, description, or value."
        ));

        return new McpSchema.Tool(
                TOOL_NAME,
                null,
                """
                Returns the list of properties defined on a testcase, optionally filtered by country.

                Call this tool whenever the user needs to see, check, or select the properties (variables) of a testcase.
                Properties can be referenced in steps and actions using the syntax %property%.
                Each property is returned once, with the list of all countries it applies to.

                Omit the country filter to retrieve all properties across all countries.

                Do not call this tool when the user asks to create, update, or delete properties.
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
                MCPToolUtils.readOnlyAnnotations("List testcase country properties", true),
                null
        );
    }

    /**
     * Executes the tool: fetches the property rows, groups them by property name,
     * applies the optional search filter, and returns the mapped DTOs as JSON.
     *
     * <p>DB returns one row per (property, country) pair. Group by property name and aggregate countries
     * into invariantCountries so the mapper produces one DTO per property with a countries list.</p>
     *
     * @param args the raw MCP argument map provided by the caller
     * @return a {@link McpSchema.CallToolResult} containing the serialised property list or an error message
     */
    private McpSchema.CallToolResult execute(Map<String, Object> args) {
        String testFolder = MCPToolUtils.getString(args, "testFolder", "");
        String testcaseId = MCPToolUtils.getString(args, "testcase", "");
        String country = MCPToolUtils.getString(args, "country", "");
        String search = MCPToolUtils.getString(args, "search", "");

        mcpLogUtils.call(TOOL_NAME, "testcase_country_property_list",
                String.format("MCP tool %s called with testFolder=%s testcase=%s country=%s", TOOL_NAME, testFolder, testcaseId, country));

        if (testFolder.isBlank()) return MCPToolUtils.errorText("Missing required parameter: testFolder");
        if (testcaseId.isBlank()) return MCPToolUtils.errorText("Missing required parameter: testcase");

        List<TestCaseCountryProperties> rows;
        try {
            rows = country.isBlank()
                    ? testCaseCountryPropertiesService.findListOfPropertyPerTestTestCase(testFolder, testcaseId)
                    : testCaseCountryPropertiesService.findListOfPropertyPerTestTestCaseCountry(testFolder, testcaseId, country);
        } catch (CerberusException e) {
            return MCPToolUtils.errorText("Unable to retrieve properties: " + e.getMessage());
        }

        // Group rows by property name — one DB row per (property, country) pair
        Map<String, TestCaseCountryProperties> grouped = new LinkedHashMap<>();
        for (TestCaseCountryProperties row : rows) {
            if (!grouped.containsKey(row.getProperty())) {
                // invariantCountries is a transient field not auto-populated by the service — must be set manually before mapping.
                row.setInvariantCountries(new ArrayList<>());
                grouped.put(row.getProperty(), row);
            }
            Invariant inv = new Invariant();
            inv.setIdName("COUNTRY");
            inv.setValue(row.getCountry());
            grouped.get(row.getProperty()).getInvariantCountries().add(inv);
        }

        List<TestcaseCountryPropertiesDTOV001> result = grouped.values().stream()
                .filter(p -> matchesSearch(p, search))
                .map(mapper::toDTO)
                .toList();

        return MCPToolUtils.successJson(Map.of(
                "testFolder", testFolder,
                "testcase", testcaseId,
                "count", result.size(),
                "properties", result
        ));
    }

    /**
     * Returns {@code true} when the property matches the given search term.
     * The comparison is case-insensitive and covers the property name, description, and first value.
     *
     * @param p      the property entity to test
     * @param search the search term; {@code null} or blank means "match everything"
     * @return {@code true} if the property matches, {@code false} otherwise
     */
    private boolean matchesSearch(TestCaseCountryProperties p, String search) {
        if (search == null || search.isBlank()) return true;
        return MCPToolUtils.containsIgnoreCase(p.getProperty(), search)
                || MCPToolUtils.containsIgnoreCase(p.getDescription(), search)
                || MCPToolUtils.containsIgnoreCase(p.getValue1(), search);
    }

    /**
     * Queries the invariant table for all values belonging to the given {@code idName} category.
     *
     * <p>Used at tool-creation time to populate enum constraints in the JSON Schema.
     * // Catches Exception (not CerberusException): runs on MCP SSE thread at startup where RequestContextHolder
     * // has no bound HTTP request — service internals may throw NullPointerException before HTTP context is available.
     * On any failure an empty list is returned so tool registration still succeeds.</p>
     *
     * @param idName the invariant category name (e.g. {@code "COUNTRY"})
     * @return the list of invariant values, or an empty list if the query fails
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
