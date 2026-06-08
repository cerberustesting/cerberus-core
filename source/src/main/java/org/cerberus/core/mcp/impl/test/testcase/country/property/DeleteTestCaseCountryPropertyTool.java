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
import org.cerberus.core.crud.service.ITestCaseCountryPropertiesService;
import org.cerberus.core.exception.CerberusException;
import org.cerberus.core.mcp.MCPTool;
import org.cerberus.core.mcp.util.MCPLogUtils;
import org.cerberus.core.mcp.util.MCPToolUtils;
import org.cerberus.core.util.answer.Answer;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

/**
 * MCP tool that deletes a {@link TestCaseCountryProperties} entry — a single
 * property scoped to a (testFolder, testcase, country) triplet.
 *
 * <p>Exposed MCP tool name: {@code cerberus_testcase_country_property_delete}.</p>
 *
 * <p>Delegates to {@link ITestCaseCountryPropertiesService} for both the existence
 * check ({@code findTestCaseCountryPropertiesByKey}) and the actual deletion
 * ({@code delete}). The deletion service method returns an {@link Answer} rather
 * than throwing a checked exception, so success is detected via the answer's
 * message code.</p>
 *
 * <p>Note: {@code invariantCountries} is a transient field not auto-populated by
 * the service — it is set manually on the existing entity before mapping so the
 * mapper can produce a complete DTO for the confirmation response.</p>
 */
@Component
public class DeleteTestCaseCountryPropertyTool implements MCPTool {

    private static final String TOOL_NAME = "cerberus_testcase_country_property_delete";

    private final ITestCaseCountryPropertiesService testCaseCountryPropertiesService;
    private final TestcaseCountryPropertiesMapperV001 mapper;
    private final MCPLogUtils mcpLogUtils;

    public DeleteTestCaseCountryPropertyTool(
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
     * Builds the {@link McpSchema.Tool} descriptor registered with the MCP server.
     *
     * <p>Input schema has four required string parameters: {@code testFolder},
     * {@code testcase}, {@code country}, and {@code property}. No enum constraints
     * are applied here — valid country values are not loaded from the invariant table
     * at registration time, keeping startup lightweight.</p>
     *
     * <p>Annotated with delete semantics ({@code MCPToolUtils.deleteAnnotations}) so
     * MCP clients can surface an appropriate confirmation prompt to the user.</p>
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
                ),
                "country", Map.of(
                        "type", "string",
                        "description", "Country the property is scoped to (use ALL for properties defined on all countries)."
                ),
                "property", Map.of(
                        "type", "string",
                        "description", "Name of the property to delete."
                )
        );

        return new McpSchema.Tool(
                TOOL_NAME,
                null,
                """
                Deletes a property from a testcase for a specific country.

                Call this tool whenever the user asks to remove or delete a property (variable) from a testcase.
                Before calling this tool, confirm the property to delete with the user.

                Use cerberus_testcase_country_property_list to find the exact property name and country before deleting.

                Do not call this tool when the user only asks to list, read, or create properties.
                """,
                new McpSchema.JsonSchema(
                        "object",
                        properties,
                        List.of("testFolder", "testcase", "country", "property"),
                        null,
                        null,
                        null
                ),
                null,
                MCPToolUtils.deleteAnnotations("Delete testcase country property", false),
                null
        );
    }

    /**
     * Executes the delete operation for a single testcase country property.
     *
     * <p>Validates that all four required parameters are present, looks up the
     * property by its composite key, deletes it via the service, and returns a
     * JSON confirmation payload containing the deleted property DTO.</p>
     *
     * @param args tool invocation arguments extracted from the MCP request
     * @return a {@link McpSchema.CallToolResult} with either the deleted DTO on
     *         success or an error message describing the failure
     */
    private McpSchema.CallToolResult execute(Map<String, Object> args) {
        String testFolder = MCPToolUtils.getString(args, "testFolder", "");
        String testcaseId = MCPToolUtils.getString(args, "testcase", "");
        String country = MCPToolUtils.getString(args, "country", "");
        String property = MCPToolUtils.getString(args, "property", "");

        mcpLogUtils.call(TOOL_NAME, "testcase_country_property_delete",
                String.format("MCP tool %s called with testFolder=%s testcase=%s country=%s property=%s",
                        TOOL_NAME, testFolder, testcaseId, country, property));

        if (testFolder.isBlank()) return MCPToolUtils.errorText("Missing required parameter: testFolder");
        if (testcaseId.isBlank()) return MCPToolUtils.errorText("Missing required parameter: testcase");
        if (country.isBlank()) return MCPToolUtils.errorText("Missing required parameter: country");
        if (property.isBlank()) return MCPToolUtils.errorText("Missing required parameter: property");

        TestCaseCountryProperties existing;
        try {
            existing = testCaseCountryPropertiesService.findTestCaseCountryPropertiesByKey(testFolder, testcaseId, country, property);
        } catch (CerberusException e) {
            return MCPToolUtils.errorText("Property does not exist: property=" + property + " country=" + country);
        }

        if (existing == null) {
            return MCPToolUtils.errorText("Property does not exist: property=" + property + " country=" + country);
        }

        Answer answer = testCaseCountryPropertiesService.delete(existing);

        if (!answer.isCodeStringEquals("OK")) {
            return MCPToolUtils.errorText("Unable to delete property: " + answer.getMessageDescription());
        }

        // invariantCountries is a transient field not auto-populated by the service — must be set manually before mapping.
        Invariant inv = new Invariant();
        inv.setIdName("COUNTRY");
        inv.setValue(country);
        existing.setInvariantCountries(List.of(inv));

        // Snapshot DTO before deletion — the entity is gone after the service call.
        TestcaseCountryPropertiesDTOV001 dto = mapper.toDTO(existing);

        return MCPToolUtils.successJson(Map.of(
                "status", "deleted",
                "property", dto
        ));
    }

}
