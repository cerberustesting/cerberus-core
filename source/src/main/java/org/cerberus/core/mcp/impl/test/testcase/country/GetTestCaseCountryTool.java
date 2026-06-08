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
import org.cerberus.core.util.answer.AnswerItem;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * MCP tool that retrieves a single {@link TestCaseCountry} association by its composite key
 * (testFolder + testcase + country).
 *
 * <p>Exposed MCP tool name: {@code cerberus_testcase_country_get}.
 *
 * <p>Delegates to {@link ITestCaseCountryService#readByKey} for the lookup and to
 * {@link IInvariantService} to populate the {@code country} parameter enum at startup.
 *
 * <p>Note: country enum values are resolved once at bean creation time via
 * {@link #loadInvariantValues(String)}. If the invariant table is not yet accessible during
 * startup (e.g. no HTTP request context on the MCP SSE thread), the enum silently falls back
 * to an unconstrained string field — the tool remains functional.
 */
@Component
public class GetTestCaseCountryTool implements MCPTool {

    private static final String TOOL_NAME = "cerberus_testcase_country_get";

    private final ITestCaseCountryService testCaseCountryService;
    private final IInvariantService invariantService;
    private final MCPLogUtils mcpLogUtils;

    public GetTestCaseCountryTool(
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
     * Builds the MCP tool descriptor, including the JSON Schema for the three required parameters.
     *
     * <p>The allowed values for {@code country} are loaded from the {@code COUNTRY} invariant table
     * at startup time. If the invariant table is unavailable on the MCP SSE thread, the enum is
     * omitted and the parameter accepts any string without validation.
     */
    private McpSchema.Tool createTool() {
        List<String> countryValues = loadInvariantValues("COUNTRY");

        Map<String, Object> countryProperty = new LinkedHashMap<>();
        countryProperty.put("type", "string");
        countryProperty.put("description", "Country code to check (e.g. FR, US, DE).");
        // Only inject the enum when values were successfully loaded — avoids an empty enum constraint.
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
                Checks whether a specific country is associated with a testcase and returns its details.

                Call this tool whenever the user asks to verify or inspect the association between a testcase and a country.

                Use cerberus_testcase_country_list instead to retrieve all countries for a testcase.
                Do not call this tool when the user asks to add or remove countries from a testcase.
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
                MCPToolUtils.readOnlyAnnotations("Get testcase country", true),
                null
        );
    }

    /**
     * Handles the tool invocation: validates arguments, calls the service, and returns the result.
     *
     * <p>Returns an error result if any required parameter is blank or if no matching
     * {@link TestCaseCountry} record exists for the given composite key.
     */
    private McpSchema.CallToolResult execute(Map<String, Object> args) {
        String testFolder = MCPToolUtils.getString(args, "testFolder", "");
        String testcaseId = MCPToolUtils.getString(args, "testcase", "");
        String country = MCPToolUtils.getString(args, "country", "");

        mcpLogUtils.call(TOOL_NAME, "testcase_country_get",
                String.format("MCP tool %s called with testFolder=%s testcase=%s country=%s",
                        TOOL_NAME, testFolder, testcaseId, country));

        if (testFolder.isBlank()) return MCPToolUtils.errorText("Missing required parameter: testFolder");
        if (testcaseId.isBlank()) return MCPToolUtils.errorText("Missing required parameter: testcase");
        if (country.isBlank()) return MCPToolUtils.errorText("Missing required parameter: country");

        AnswerItem<TestCaseCountry> answer = testCaseCountryService.readByKey(testFolder, testcaseId, country);

        // A non-OK answer code or a null item both indicate the association does not exist.
        if (!answer.isCodeStringEquals("OK") || answer.getItem() == null) {
            return MCPToolUtils.errorText("Country not associated: testFolder=" + testFolder
                    + " testcase=" + testcaseId + " country=" + country);
        }

        TestCaseCountry tcc = answer.getItem();
        return MCPToolUtils.successJson(toMap(tcc));
    }

    /**
     * Converts a {@link TestCaseCountry} entity into a plain ordered map suitable for JSON
     * serialisation in the MCP response.
     *
     * <p>Timestamp fields are serialised as ISO strings; {@code null} timestamps become empty
     * strings to avoid JSON {@code null} values in the response payload.
     */
    private Map<String, Object> toMap(TestCaseCountry tcc) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("testFolder", tcc.getTest());
        map.put("testcase", tcc.getTestcase());
        map.put("country", tcc.getCountry());
        map.put("usrCreated", MCPToolUtils.nullSafe(tcc.getUsrCreated()));
        map.put("dateCreated", tcc.getDateCreated() != null ? tcc.getDateCreated().toString() : "");
        map.put("usrModif", MCPToolUtils.nullSafe(tcc.getUsrModif()));
        map.put("dateModif", tcc.getDateModif() != null ? tcc.getDateModif().toString() : "");
        return map;
    }

    /**
     * Reads all invariant values for a given invariant identifier from the database.
     *
     * <p>Used at tool-creation time to populate enum constraints in the JSON Schema.
     *
     * // Catches Exception (not CerberusException): runs on MCP SSE thread at startup where RequestContextHolder
     * // has no bound HTTP request — service internals may throw NullPointerException before HTTP context is available.
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
}
