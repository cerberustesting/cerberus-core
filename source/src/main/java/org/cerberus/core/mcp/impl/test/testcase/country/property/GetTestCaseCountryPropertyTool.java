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
import org.cerberus.core.api.dto.testcase.TestcaseCountryPropertiesMapperV001;
import org.cerberus.core.crud.entity.Invariant;
import org.cerberus.core.crud.entity.TestCaseCountryProperties;
import org.cerberus.core.crud.service.ITestCaseCountryPropertiesService;
import org.cerberus.core.mcp.MCPTool;
import org.cerberus.core.mcp.util.MCPLogUtils;
import org.cerberus.core.mcp.util.MCPToolUtils;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * MCP tool that retrieves a single {@link TestCaseCountryProperties} by property name for a given testcase.
 *
 * <p>Exposed MCP tool name: {@code cerberus_testcase_country_property_get}.</p>
 *
 * <p>Delegates to {@link ITestCaseCountryPropertiesService#findListOfPropertyPerTestTestCaseProperty}
 * to load all country rows for the requested property, then manually assembles the
 * {@code invariantCountries} list before handing off to {@link TestcaseCountryPropertiesMapperV001}.</p>
 *
 * <p>Architectural note: the DB returns one row per (property, country) pair, so grouping and
 * invariant population must be done in-process rather than relying on the service layer.</p>
 */
@Component
public class GetTestCaseCountryPropertyTool implements MCPTool {

    private static final String TOOL_NAME = "cerberus_testcase_country_property_get";

    private final ITestCaseCountryPropertiesService testCaseCountryPropertiesService;
    private final TestcaseCountryPropertiesMapperV001 mapper;
    private final MCPLogUtils mcpLogUtils;

    public GetTestCaseCountryPropertyTool(
            ITestCaseCountryPropertiesService testCaseCountryPropertiesService,
            TestcaseCountryPropertiesMapperV001 mapper,
            MCPLogUtils mcpLogUtils) {
        this.testCaseCountryPropertiesService = testCaseCountryPropertiesService;
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
     * Builds the {@link McpSchema.Tool} descriptor for this MCP tool.
     *
     * <p>The schema exposes {@code testFolder}, {@code testcase}, and {@code property} as required
     * parameters and {@code country} as an optional filter. No enum values are loaded from the
     * invariant table or static JSON at startup — all parameters are free-form strings.</p>
     *
     * @return the fully described MCP tool schema ready for registration
     */
    private McpSchema.Tool createTool() {
        Map<String, Object> properties = new LinkedHashMap<>();
        properties.put("testFolder", Map.of(
                "type", "string",
                "description", "Name of the test folder containing the testcase."
        ));
        properties.put("testcase", Map.of(
                "type", "string",
                "description", "Identifier of the testcase."
        ));
        properties.put("property", Map.of(
                "type", "string",
                "description", "Name of the property to retrieve."
        ));
        properties.put("country", Map.of(
                "type", "string",
                "description", "Optional country filter. If omitted, all countries for the property are returned."
        ));

        return new McpSchema.Tool(
                TOOL_NAME,
                null,
                """
                Retrieves a specific property from a testcase, with all its associated countries.

                Call this tool whenever the user asks to inspect or display the details of a specific property by name.
                Returns the property once, with the full list of countries it applies to.

                Use cerberus_testcase_country_property_list instead when the property name is unknown or to browse all properties.

                Do not call this tool when the user asks to create, update, or delete properties.
                """,
                new McpSchema.JsonSchema(
                        "object",
                        properties,
                        List.of("testFolder", "testcase", "property"),
                        null,
                        null,
                        null
                ),
                null,
                MCPToolUtils.readOnlyAnnotations("Get testcase country property", true),
                null
        );
    }

    /**
     * Executes the tool: loads all country rows for the requested property, optionally filters
     * by country, then assembles and maps a single DTO.
     *
     * <p>DB returns one row per (property, country) pair. Group by property name and aggregate
     * countries into invariantCountries so the mapper produces one DTO per property with a
     * countries list.</p>
     *
     * <p>invariantCountries is a transient field not auto-populated by the service — must be set
     * manually before mapping.</p>
     *
     * @param args raw MCP call arguments
     * @return a JSON result containing the property DTO, or an error text if not found
     */
    private McpSchema.CallToolResult execute(Map<String, Object> args) {
        String testFolder = MCPToolUtils.getString(args, "testFolder", "");
        String testcaseId = MCPToolUtils.getString(args, "testcase", "");
        String property = MCPToolUtils.getString(args, "property", "");
        String country = MCPToolUtils.getString(args, "country", "");

        mcpLogUtils.call(TOOL_NAME, "testcase_country_property_get",
                String.format("MCP tool %s called with testFolder=%s testcase=%s property=%s country=%s",
                        TOOL_NAME, testFolder, testcaseId, property, country));

        if (testFolder.isBlank()) return MCPToolUtils.errorText("Missing required parameter: testFolder");
        if (testcaseId.isBlank()) return MCPToolUtils.errorText("Missing required parameter: testcase");
        if (property.isBlank()) return MCPToolUtils.errorText("Missing required parameter: property");

        // No direct findByKey service method exists — load the full list then filter in memory.
        List<TestCaseCountryProperties> rows = testCaseCountryPropertiesService
                .findListOfPropertyPerTestTestCaseProperty(testFolder, testcaseId, property);

        if (rows.isEmpty()) {
            return MCPToolUtils.errorText("Property does not exist: property=" + property
                    + " testFolder=" + testFolder + " testcase=" + testcaseId);
        }

        if (!country.isBlank()) {
            // Narrow the row list to the single requested country before building the DTO.
            rows = rows.stream().filter(r -> country.equals(r.getCountry())).toList();
            if (rows.isEmpty()) {
                return MCPToolUtils.errorText("Property does not exist for country: property=" + property + " country=" + country);
            }
        }

        // Use the first row as the base entity; it carries all shared property fields.
        TestCaseCountryProperties base = rows.get(0);
        // invariantCountries is a transient field not auto-populated by the service — must be set manually before mapping.
        base.setInvariantCountries(new ArrayList<>());
        for (TestCaseCountryProperties row : rows) {
            // Build a minimal Invariant stub — only COUNTRY idName and value are needed by the mapper.
            Invariant inv = new Invariant();
            inv.setIdName("COUNTRY");
            inv.setValue(row.getCountry());
            base.getInvariantCountries().add(inv);
        }

        return MCPToolUtils.successJson(mapper.toDTO(base));
    }
}
