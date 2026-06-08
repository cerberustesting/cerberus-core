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
import org.cerberus.core.crud.entity.Invariant;
import org.cerberus.core.crud.entity.TestCase;
import org.cerberus.core.crud.entity.TestCaseCountryProperties;
import org.cerberus.core.crud.service.IInvariantService;
import org.cerberus.core.crud.service.ITestCaseCountryPropertiesService;
import org.cerberus.core.crud.service.ITestCaseCountryService;
import org.cerberus.core.crud.service.ITestCaseService;
import org.cerberus.core.exception.CerberusException;
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
 * MCP tool that creates a {@link TestCaseCountryProperties} entry for a given testcase and country.
 *
 * <p>Exposed MCP tool name: {@value TOOL_NAME}</p>
 *
 * <p>Delegates to:
 * <ul>
 *   <li>{@link ITestCaseService} — validates that the parent testcase exists.</li>
 *   <li>{@link ITestCaseCountryService} — validates that the country is already associated with the testcase.</li>
 *   <li>{@link ITestCaseCountryPropertiesService} — checks for duplicates and performs the actual creation.</li>
 *   <li>{@link IInvariantService} — loads the COUNTRY enum values at startup to populate the tool schema,
 *       and validates the supplied country value at call time.</li>
 * </ul>
 * </p>
 *
 * <p>The special value {@code country=ALL} bypasses the per-country association check and creates the property
 * for every country linked to the testcase, as handled downstream by the service layer.</p>
 */
@Component
public class CreateTestCaseCountryPropertyTool implements MCPTool {

    private static final String TOOL_NAME = "cerberus_testcase_country_property_create";

    private final ITestCaseService testCaseService;
    private final ITestCaseCountryService testCaseCountryService;
    private final ITestCaseCountryPropertiesService testCaseCountryPropertiesService;
    private final IInvariantService invariantService;
    private final MCPLogUtils mcpLogUtils;

    public CreateTestCaseCountryPropertyTool(
            ITestCaseService testCaseService,
            ITestCaseCountryService testCaseCountryService,
            ITestCaseCountryPropertiesService testCaseCountryPropertiesService,
            IInvariantService invariantService,
            MCPLogUtils mcpLogUtils) {
        this.testCaseService = testCaseService;
        this.testCaseCountryService = testCaseCountryService;
        this.testCaseCountryPropertiesService = testCaseCountryPropertiesService;
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
     * Builds the MCP {@link McpSchema.Tool} descriptor, including the JSON schema for all input parameters.
     *
     * <p>Country enum values are fetched from the invariant table (idName={@code COUNTRY}) at Spring startup time
     * via {@link #loadInvariantValues(String)}. If the invariant service is unavailable at that point (e.g., no HTTP
     * request context on the MCP SSE thread), the country field will render as a plain string without an enum constraint.</p>
     *
     * <p>The {@code type} enum is populated from static constants on {@link TestCaseCountryProperties} rather than
     * the invariant table, so it is always complete regardless of startup timing.</p>
     *
     * @return a fully configured {@link McpSchema.Tool} ready to be registered on the MCP server.
     */
    private McpSchema.Tool createTool() {
        List<String> countryValues = loadInvariantValues("COUNTRY");

        Map<String, Object> countryProperty = new LinkedHashMap<>();
        countryProperty.put("type", "string");
        countryProperty.put("description", "Country this property applies to. Use ALL to apply to all countries associated with the testcase.");
        if (!countryValues.isEmpty()) {
            // Prepend "ALL" so the model sees it as the first (default) option.
            List<String> values = new java.util.ArrayList<>(countryValues);
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
        properties.put("property", Map.of(
                "type", "string",
                "description", "Name of the property (used as a variable in steps and actions, e.g. 'myUrl', 'expectedStatus')."
        ));
        properties.put("type", Map.of(
                "type", "string",
                "description", """
                        How the property value is obtained:
                        - text: static value defined in value1.
                        - getFromJson: extract from a JSON response using a path in value1.
                        - getFromSql: result of a SQL query in value1 on database in value2.
                        - getFromDataLib: fetch from a data library entry named in value1.
                        - getFromJS: evaluate a JavaScript expression in value1.
                        - getFromXml: extract from XML using XPath in value1.
                        - getFromNetworkTraffic: extract from captured network traffic.
                        - getOTP: generate a one-time password.
                        """,
                "enum", List.of(
                        TestCaseCountryProperties.TYPE_TEXT,
                        TestCaseCountryProperties.TYPE_GETFROMJSON,
                        TestCaseCountryProperties.TYPE_GETRAWFROMJSON,
                        TestCaseCountryProperties.TYPE_GETFROMSQL,
                        TestCaseCountryProperties.TYPE_GETFROMDATALIB,
                        TestCaseCountryProperties.TYPE_GETFROMJS,
                        TestCaseCountryProperties.TYPE_GETFROMXML,
                        TestCaseCountryProperties.TYPE_GETRAWFROMXML,
                        TestCaseCountryProperties.TYPE_GETFROMHTML,
                        TestCaseCountryProperties.TYPE_GETFROMHTMLVISIBLE,
                        TestCaseCountryProperties.TYPE_GETATTRIBUTEFROMHTML,
                        TestCaseCountryProperties.TYPE_GETFROMCOOKIE,
                        TestCaseCountryProperties.TYPE_GETFROMNETWORKTRAFFIC,
                        TestCaseCountryProperties.TYPE_GETFROMGROOVY,
                        TestCaseCountryProperties.TYPE_GETFROMCOMMAND,
                        TestCaseCountryProperties.TYPE_GETOTP,
                        TestCaseCountryProperties.TYPE_GETFROMEXECUTIONOBJECT
                )
        ));
        properties.put("value1", Map.of(
                "type", "string",
                "description", "Primary value for the property. For type=text: the static value. For type=getFromJson: the JSON path. For type=getFromSql: the SQL query."
        ));
        properties.put("value2", Map.of(
                "type", "string",
                "description", "Secondary value. For type=getFromSql: the database name. Meaning depends on type."
        ));
        properties.put("description", Map.of(
                "type", "string",
                "description", "Optional description of what this property represents."
        ));
        properties.put("nature", Map.of(
                "type", "string",
                "description", "Value selection strategy when multiple rows are returned. STATIC: always first row. RANDOM: random row. RANDOMNEW: random row not used in this execution. NOTINUSE: skip this property.",
                "enum", List.of(
                        TestCaseCountryProperties.NATURE_STATIC,
                        TestCaseCountryProperties.NATURE_RANDOM,
                        TestCaseCountryProperties.NATURE_RANDOMNEW,
                        TestCaseCountryProperties.NATURE_NOTINUSE
                )
        ));

        return new McpSchema.Tool(
                TOOL_NAME,
                null,
                """
                Creates a property on a testcase for a specific country (or all countries).

                Call this tool whenever the user asks to add a variable, property, or parameter to a testcase.
                Properties are named variables that can be referenced in steps and actions using the syntax %property%.

                A property is always scoped to a country. Use country=ALL to apply to all countries associated with the testcase.
                The property name must be unique per testcase and country combination.

                Do not call this tool when the user only asks to list, read, update, or delete properties.
                """,
                new McpSchema.JsonSchema(
                        "object",
                        properties,
                        List.of("testFolder", "testcase", "country", "property", "type", "value1"),
                        null,
                        null,
                        null
                ),
                null,
                MCPToolUtils.createAnnotations("Create testcase country property", false),
                null
        );
    }

    /**
     * Validates all inputs, checks pre-conditions, and delegates the actual persistence to
     * {@link ITestCaseCountryPropertiesService#create(TestCaseCountryProperties)}.
     *
     * <p>Pre-conditions verified in order:
     * <ol>
     *   <li>All required parameters are present and non-blank.</li>
     *   <li>The parent testcase exists.</li>
     *   <li>When {@code country != "ALL"}: the country is a valid invariant value and is already linked to the testcase.</li>
     *   <li>No property with the same (testFolder, testcase, country, property) key already exists.</li>
     * </ol>
     * </p>
     *
     * @param args raw MCP argument map extracted from the tool call request.
     * @return a {@link McpSchema.CallToolResult} containing either a JSON success payload or an error message.
     */
    private McpSchema.CallToolResult execute(Map<String, Object> args) {
        String testFolder = MCPToolUtils.getString(args, "testFolder", "");
        String testcaseId = MCPToolUtils.getString(args, "testcase", "");
        String country = MCPToolUtils.getString(args, "country", "");
        String property = MCPToolUtils.getString(args, "property", "");
        String type = MCPToolUtils.getString(args, "type", TestCaseCountryProperties.TYPE_TEXT);
        String value1 = MCPToolUtils.getString(args, "value1", "");
        String value2 = MCPToolUtils.getString(args, "value2", "");
        String description = MCPToolUtils.getString(args, "description", "");
        String nature = MCPToolUtils.getString(args, "nature", TestCaseCountryProperties.NATURE_STATIC);

        mcpLogUtils.call(TOOL_NAME, "testcase_country_property_create",
                String.format("MCP tool %s called with testFolder=%s testcase=%s country=%s property=%s",
                        TOOL_NAME, testFolder, testcaseId, country, property));

        if (testFolder.isBlank()) return MCPToolUtils.errorText("Missing required parameter: testFolder");
        if (testcaseId.isBlank()) return MCPToolUtils.errorText("Missing required parameter: testcase");
        if (country.isBlank()) return MCPToolUtils.errorText("Missing required parameter: country");
        if (property.isBlank()) return MCPToolUtils.errorText("Missing required parameter: property");
        if (value1.isBlank()) return MCPToolUtils.errorText("Missing required parameter: value1");

        AnswerItem<TestCase> testCaseAnswer = testCaseService.readByKey(testFolder, testcaseId);
        if (!testCaseAnswer.isCodeStringEquals("OK") || testCaseAnswer.getItem() == null) {
            return MCPToolUtils.errorText("Testcase does not exist: testFolder=" + testFolder + " testcase=" + testcaseId);
        }

        if (!"ALL".equals(country)) {
            // Validate the country value against the COUNTRY invariant before checking the testcase association.
            AnswerItem<Invariant> countryInvariant = invariantService.readByKey("COUNTRY", country);
            if (!countryInvariant.isCodeStringEquals("OK") || countryInvariant.getItem() == null) {
                return MCPToolUtils.errorText("Invalid country: '" + country + "'. Call cerberus_invariant_list with type=COUNTRY to get valid values.");
            }
            if (!testCaseCountryService.exist(testFolder, testcaseId, country)) {
                return MCPToolUtils.errorText("Country '" + country + "' is not associated with this testcase. Call cerberus_testcase_country_create first.");
            }
        }

        try {
            // findTestCaseCountryPropertiesByKey throws CerberusException when the record is not found,
            // so a non-null return value means a duplicate already exists.
            TestCaseCountryProperties existing = testCaseCountryPropertiesService.findTestCaseCountryPropertiesByKey(testFolder, testcaseId, country, property);
            if (existing != null) {
                return MCPToolUtils.errorText("Property already exists: property=" + property + " country=" + country);
            }
        } catch (CerberusException ignored) {
            // not found — proceed with creation
        }

        TestCaseCountryProperties prop = TestCaseCountryProperties.builder()
                .test(testFolder)
                .testcase(testcaseId)
                .country(country)
                .property(property)
                .description(description)
                .type(type)
                .value1(value1)
                .value2(value2)
                .value3("")
                .length("0")
                .rowLimit(0)
                .nature(nature)
                .cacheExpire(0)
                .retryNb(0)
                .retryPeriod(0)
                .rank(1)
                // Tag created-by as MCP so audit logs can distinguish AI-driven changes from UI changes.
                .usrCreated("MCP")
                .build();

        Answer answer = testCaseCountryPropertiesService.create(prop);

        if (!answer.isCodeStringEquals("OK")) {
            return MCPToolUtils.errorText("Unable to create property: " + answer.getMessageDescription());
        }

        return MCPToolUtils.successJson(Map.of(
                "status", "created",
                "testFolder", testFolder,
                "testcase", testcaseId,
                "country", country,
                "property", property,
                "type", type
        ));
    }

    /**
     * Fetches allowed values for a given invariant category from the database.
     *
     * <p>Used at bean-initialization time to populate enum constraints in the tool schema.
     * Returns an empty list on any failure so schema registration always succeeds.</p>
     *
     * // Catches Exception (not CerberusException): runs on MCP SSE thread at startup where RequestContextHolder
     * // has no bound HTTP request — service internals may throw NullPointerException before HTTP context is available.
     *
     * @param idName the invariant category identifier (e.g., {@code "COUNTRY"}).
     * @return list of invariant values, or an empty list if the invariant table is unreachable.
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
