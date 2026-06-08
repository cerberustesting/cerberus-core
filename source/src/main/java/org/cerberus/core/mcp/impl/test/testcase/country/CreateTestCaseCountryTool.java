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
import org.cerberus.core.crud.entity.TestCase;
import org.cerberus.core.crud.entity.TestCaseCountry;
import org.cerberus.core.crud.service.IInvariantService;
import org.cerberus.core.crud.service.ITestCaseCountryService;
import org.cerberus.core.crud.service.ITestCaseService;
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
 * MCP tool that associates a country with an existing testcase.
 *
 * <p>Exposes the {@code cerberus_testcase_country_create} tool to the AI agent.
 * The tool schema is built at registration time so country enum values are
 * populated from the invariant table and surfaced directly to the model.</p>
 */
@Component
public class CreateTestCaseCountryTool implements MCPTool {

    private static final String TOOL_NAME = "cerberus_testcase_country_create";

    private final ITestCaseService testCaseService;
    private final ITestCaseCountryService testCaseCountryService;
    private final IInvariantService invariantService;
    private final MCPLogUtils mcpLogUtils;

    public CreateTestCaseCountryTool(ITestCaseService testCaseService, ITestCaseCountryService testCaseCountryService, IInvariantService invariantService, MCPLogUtils mcpLogUtils) {
        this.testCaseService = testCaseService;
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
     * Builds the tool schema, including the live enum of valid country codes.
     *
     * <p>Country values are loaded from the invariant table at startup.
     * If the DB is not yet reachable (e.g. during context initialisation on
     * the MCP SSE thread), the enum is omitted and the field accepts any string.</p>
     */
    private McpSchema.Tool createTool() {
        List<String> countryValues = loadInvariantValues("COUNTRY");

        Map<String, Object> countryProperty = new LinkedHashMap<>();
        countryProperty.put("type", "string");
        countryProperty.put("description", "Country code to associate with the testcase (e.g. FR, US, DE).");
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
                Associates a country with a testcase in Cerberus.

                Call this tool whenever the user asks to add a country to a testcase or enable a testcase for a specific country.
                A testcase must be associated with at least one country to be executed.

                Do not call this tool when the user only asks to list or remove countries from a testcase.
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
                MCPToolUtils.createAnnotations("Create testcase country", false),
                null
        );
    }

    /**
     * Validates the inputs, checks for duplicates, and persists the association.
     *
     * <p>Three guard checks run before the insert:
     * <ol>
     *   <li>The testcase must exist — avoids orphan country rows.</li>
     *   <li>The country code must be a known invariant value — the model may
     *       hallucinate codes not in the enum when the schema was loaded without DB.</li>
     *   <li>The association must not already exist — the service {@code create}
     *       would silently succeed but leave a duplicate key constraint violation
     *       at the DAO level on some databases.</li>
     * </ol>
     * </p>
     */
    private McpSchema.CallToolResult execute(Map<String, Object> args) {
        String testFolder = MCPToolUtils.getString(args, "testFolder", "");
        String testcaseId = MCPToolUtils.getString(args, "testcase", "");
        String country = MCPToolUtils.getString(args, "country", "");

        mcpLogUtils.call(TOOL_NAME, "testcase_country_create",
                String.format("MCP tool %s called with testFolder=%s testcase=%s country=%s", TOOL_NAME, testFolder, testcaseId, country));

        if (testFolder.isBlank()) {
            return MCPToolUtils.errorText("Missing required parameter: testFolder");
        }

        if (testcaseId.isBlank()) {
            return MCPToolUtils.errorText("Missing required parameter: testcase");
        }

        if (country.isBlank()) {
            return MCPToolUtils.errorText("Missing required parameter: country");
        }

        // Guard 1 — testcase must exist
        AnswerItem<TestCase> testCaseAnswer = testCaseService.readByKey(testFolder, testcaseId);
        if (!testCaseAnswer.isCodeStringEquals("OK") || testCaseAnswer.getItem() == null) {
            return MCPToolUtils.errorText("Testcase does not exist: testFolder=" + testFolder + " testcase=" + testcaseId);
        }

        // Guard 2 — country must be a known invariant value
        AnswerItem<Invariant> countryInvariant = invariantService.readByKey("COUNTRY", country);
        if (!countryInvariant.isCodeStringEquals("OK") || countryInvariant.getItem() == null) {
            return MCPToolUtils.errorText("Invalid country: '" + country + "'. Call cerberus_invariant_list with type=COUNTRY to get valid values.");
        }

        // Guard 3 — association must not already exist
        if (testCaseCountryService.exist(testFolder, testcaseId, country)) {
            return MCPToolUtils.errorText("Country already associated: testcase=" + testcaseId + " country=" + country);
        }

        TestCaseCountry testCaseCountry = TestCaseCountry.builder()
                .test(testFolder)
                .testcase(testcaseId)
                .country(country)
                .usrCreated("MCP")
                .build();

        Answer answer = testCaseCountryService.create(testCaseCountry);

        if (!answer.isCodeStringEquals("OK")) {
            return MCPToolUtils.errorText("Unable to associate country: " + answer.getMessageDescription());
        }

        return MCPToolUtils.successJson(Map.of(
                "status", "created",
                "testFolder", testFolder,
                "testcase", testcaseId,
                "country", country
        ));
    }

    /**
     * Loads invariant values from the DB for use in the tool schema enum.
     *
     * <p>Catches {@link Exception} (not just {@link org.cerberus.core.exception.CerberusException})
     * because this method is called during Spring context initialisation on the MCP SSE thread,
     * where {@code RequestContextHolder} has no bound request and some service internals
     * throw {@link NullPointerException} before the HTTP context is available.</p>
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