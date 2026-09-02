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
import org.cerberus.core.crud.entity.TestCase;
import org.cerberus.core.crud.entity.TestCaseCountryProperties;
import org.cerberus.core.crud.service.IInvariantService;
import org.cerberus.core.crud.service.IParameterService;
import org.cerberus.core.crud.service.ITestCaseCountryPropertiesService;
import org.cerberus.core.crud.service.ITestCaseService;
import org.cerberus.core.util.answer.AnswerItem;
import org.cerberus.core.exception.CerberusException;
import org.cerberus.core.mcp.MCPTool;
import org.cerberus.core.mcp.util.MCPLogUtils;
import org.cerberus.core.mcp.util.MCPToolUtils;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
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

    /**
     * Parameter deciding whether a property inherited from a used step or a pre-test counts for a
     * country the testcase itself does not define it for. Its value changes the answer to "can I
     * use this property here", which is why it is reported alongside the inherited list.
     */
    private static final String COUNTRY_LEVEL_HERITAGE = "cerberus_property_countrylevelheritage";

    private final ITestCaseCountryPropertiesService testCaseCountryPropertiesService;
    private final ITestCaseService testCaseService;
    private final IInvariantService invariantService;
    private final IParameterService parameterService;
    private final TestcaseCountryPropertiesMapperV001 mapper;
    private final MCPLogUtils mcpLogUtils;

    public ListTestCaseCountryPropertiesTool(
            ITestCaseCountryPropertiesService testCaseCountryPropertiesService,
            ITestCaseService testCaseService,
            IInvariantService invariantService,
            IParameterService parameterService,
            TestcaseCountryPropertiesMapperV001 mapper,
            MCPLogUtils mcpLogUtils) {
        this.testCaseCountryPropertiesService = testCaseCountryPropertiesService;
        this.testCaseService = testCaseService;
        this.invariantService = invariantService;
        this.parameterService = parameterService;
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
        properties.put("includeInherited", Map.of(
                "type", "boolean",
                "description", """
                        Also return the properties this testcase inherits from the library steps it uses.
                        Defaults to false.

                        Set it to true whenever you are about to write a %property.NAME% in a step, or when a run
                        failed on an unresolved property: a testcase can legitimately use a property it does not
                        define itself, and looking only at its own properties is what makes that property look
                        missing when it is not.
                        """
        ));

        return new McpSchema.Tool(
                TOOL_NAME,
                null,
                """
                Returns the properties a testcase can use, written in steps as %property.NAME%.

                Call this whenever you need to see, check or choose the properties of a testcase — and always
                before writing a %property.NAME% into an action or control, so you reference one that exists.

                Set includeInherited to true to also get the properties coming from the library steps the
                testcase uses. A testcase very often relies on properties it does not define itself, so the
                default view alone can make an existing property look missing.

                Each property is returned once with every country it applies to, and, when inherited, the
                testcase it comes from and whether it can be edited here.

                For the variables that do not come from the testcase — %system.…%, %object.…%, %datalib.…% —
                call cerberus_variable_list instead.

                Do not call this tool to create, update or delete a property.
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
        boolean includeInherited = MCPToolUtils.getBoolean(args, "includeInherited", false);

        mcpLogUtils.call(TOOL_NAME, "testcase_country_property_list",
                String.format("MCP tool %s called with testFolder=%s testcase=%s country=%s includeInherited=%s",
                        TOOL_NAME, testFolder, testcaseId, country, includeInherited));

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

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("testFolder", testFolder);
        response.put("testcase", testcaseId);
        response.put("count", result.size());
        response.put("properties", result);

        if (includeInherited) {
            addInherited(testFolder, testcaseId, search, grouped.keySet(), response);
        } else if (!result.isEmpty()) {
            response.put("note", "Own properties only. Call again with includeInherited=true to also see the "
                    + "properties coming from the library steps this testcase uses — a testcase commonly "
                    + "references properties it does not define itself.");
        }

        return MCPToolUtils.successJson(response);
    }

    /**
     * Adds the properties the testcase inherits from the library steps it uses.
     *
     * <p>Delegates to {@link ITestCaseCountryPropertiesService#findDistinctInheritedPropertiesOfTestCase},
     * the same resolution the test script screen performs, which walks the steps flagged as using a
     * library step and collects the properties of the testcases they come from.</p>
     *
     * <p>Two things are reported alongside the list because they decide whether an inherited property
     * is actually usable, and neither is visible in the property row itself:</p>
     * <ul>
     *   <li>the source testcase, since an inherited property is edited there and not here;</li>
     *   <li>whether a name is defined in both places, in which case the engine keeps the definition
     *       of this testcase — the inherited one is shadowed, so changing it has no effect.</li>
     * </ul>
     *
     * @param ownNames names already defined by the testcase itself, used to flag shadowing.
     */
    private void addInherited(String testFolder, String testcaseId, String search,
                              java.util.Set<String> ownNames, Map<String, Object> response) {
        TestCase testCase;
        try {
            // The resolution walks the steps, so the testcase must be loaded with them.
            AnswerItem<TestCase> answer = testCaseService.readByKeyWithDependency(testFolder, testcaseId);
            testCase = answer.getItem();
        } catch (RuntimeException e) {
            response.put("inheritedError", "Unable to load the testcase steps: " + e.getMessage());
            return;
        }

        if (testCase == null) {
            response.put("inheritedError", "Testcase does not exist: testFolder=" + testFolder
                    + " testcase=" + testcaseId);
            return;
        }

        List<TestCaseCountryProperties> inherited;
        try {
            @SuppressWarnings("unchecked")
            HashMap<String, Invariant> countryInvariants =
                    (HashMap<String, Invariant>) invariantService.readByIdNameToHash("COUNTRY");
            inherited = testCaseCountryPropertiesService
                    .findDistinctInheritedPropertiesOfTestCase(testCase, countryInvariants);
        } catch (CerberusException e) {
            response.put("inheritedError", "Unable to resolve the inherited properties: " + e.getMessage());
            return;
        }

        List<Map<String, Object>> rows = new ArrayList<>();
        for (TestCaseCountryProperties property : inherited) {
            if (!matchesSearch(property, search)) {
                continue;
            }
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("property", MCPToolUtils.nullSafe(property.getProperty()));
            row.put("type", MCPToolUtils.nullSafe(property.getType()));
            row.put("nature", MCPToolUtils.nullSafe(property.getNature()));
            row.put("value1", MCPToolUtils.nullSafe(property.getValue1()));
            row.put("value2", MCPToolUtils.nullSafe(property.getValue2()));
            row.put("description", MCPToolUtils.nullSafe(property.getDescription()));
            // Where it lives : editing it means editing that testcase, not this one.
            row.put("fromTestFolder", MCPToolUtils.nullSafe(property.getTest()));
            row.put("fromTestcase", MCPToolUtils.nullSafe(property.getTestcase()));
            row.put("editableHere", false);
            row.put("countries", property.getInvariantCountries() == null
                    ? List.of()
                    : property.getInvariantCountries().stream()
                            .map(invariant -> MCPToolUtils.nullSafe(invariant.getValue()))
                            .toList());
            if (ownNames.contains(property.getProperty())) {
                row.put("shadowedByOwnProperty", true);
            }
            rows.add(row);
        }

        response.put("inheritedCount", rows.size());
        response.put("inherited", rows);
        response.put("resolutionOrder", List.of(
                "At execution the engine loads properties from four sources, each overriding the previous one: "
                        + "pre-testing testcases, post-testing testcases, the library steps used, then this "
                        + "testcase. The testcase's own definition therefore always wins.",
                "This list covers the library steps only. Properties coming from pre and post testing are "
                        + "selected at run time from the application, country, system, build and revision, so "
                        + "they cannot be resolved from the testcase alone.",
                "A property must exist for the country being run. The "
                        + COUNTRY_LEVEL_HERITAGE + " parameter is currently '"
                        + parameterService.getParameterStringByKey(COUNTRY_LEVEL_HERITAGE, "", "N")
                        + "'. With N, a property the testcase defines for other countries but not for the one "
                        + "being run counts as missing there, even if a library step defines it for that "
                        + "country. With Y, the inherited definition is enough."));
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
