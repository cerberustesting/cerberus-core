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
import org.cerberus.core.util.answer.AnswerItem;
import org.cerberus.core.mcp.util.MCPToolUtils;
import org.cerberus.core.util.answer.Answer;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * MCP tool that updates an existing {@link TestCaseCountryProperties} entity (a testcase property
 * scoped to a specific country).
 *
 * <p>Exposed MCP tool name: {@code cerberus_testcase_country_property_update}.</p>
 *
 * <p>Delegates persistence to {@link ITestCaseCountryPropertiesService}. After a successful update,
 * the full property list for the (testFolder, testcase, property) key is reloaded so that the
 * response DTO contains the current country list — because {@link TestCaseCountryProperties#getInvariantCountries()}
 * is a transient field that must be populated manually before mapping.</p>
 *
 * <p>The enum values for {@code type} and {@code nature} are sourced directly from static constants
 * on {@link TestCaseCountryProperties}, not from the invariant table, so the schema is fixed at
 * compile time and carries no startup-time risk.</p>
 */
@Component
public class UpdateTestCaseCountryPropertyTool implements MCPTool {

    private static final String TOOL_NAME = "cerberus_testcase_country_property_update";

    /** Invariant holding the database names a property may point at, as the test case editor uses. */
    private static final String PROPERTY_DATABASE_INVARIANT = "PROPERTYDATABASE";

    private final ITestCaseCountryPropertiesService testCaseCountryPropertiesService;
    private final TestcaseCountryPropertiesMapperV001 mapper;
    private final MCPLogUtils mcpLogUtils;
    private final IInvariantService invariantService;

    public UpdateTestCaseCountryPropertyTool(
            ITestCaseCountryPropertiesService testCaseCountryPropertiesService,
            TestcaseCountryPropertiesMapperV001 mapper,
            MCPLogUtils mcpLogUtils,
            IInvariantService invariantService) {
        this.testCaseCountryPropertiesService = testCaseCountryPropertiesService;
        this.mapper = mapper;
        this.mcpLogUtils = mcpLogUtils;
        this.invariantService = invariantService;
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
     * Builds the {@link McpSchema.Tool} descriptor that the MCP runtime registers with AI clients.
     *
     * <p>The {@code type} and {@code nature} enums are sourced from static constants on
     * {@link TestCaseCountryProperties} (not from the invariant table), so the schema is
     * fully resolved at compile time with no startup-time database dependency.</p>
     *
     * <p>The {@code updates} sub-object uses {@code additionalProperties: false} to prevent
     * the AI model from sending unrecognised fields that would be rejected by the switch in
     * {@link #execute(Map)}.</p>
     *
     * @return a fully configured tool descriptor ready for MCP registration
     */
    private McpSchema.Tool createTool() {
        Map<String, Object> updateProperties = new LinkedHashMap<>();
        updateProperties.put("type", Map.of(
                "type", "string",
                "description", "New type of the property.",
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
        updateProperties.put("value1", Map.of(
                "type", "string",
                "description", "New primary value (static value, JSON path, SQL query, etc.)."
        ));
        updateProperties.put("value2", Map.of(
                "type", "string",
                "description", "New secondary value. Meaning depends on the type. It is NOT the database for "
                        + "type=getFromSql — that is the separate 'database' field."
        ));
        updateProperties.put("value3", Map.of(
                "type", "string",
                "description", "New third value. Meaning depends on the type."
        ));
        updateProperties.put("database", Map.of(
                "type", "string",
                "description", "Which database the query runs on, for type=getFromSql (and the SQL data "
                        + "libraries). A logical name Cerberus resolves per country and environment through "
                        + "the environment's database list. Change it to point an existing query at another "
                        + "source. Call cerberus_invariant_list with type=PROPERTYDATABASE for the valid names."
        ));
        updateProperties.put("length", Map.of(
                "type", "integer",
                "description", "Length of the generated value, for type=text with nature=RANDOM."
        ));
        updateProperties.put("rowLimit", Map.of(
                "type", "integer",
                "description", "Maximum number of rows to fetch. 0 means the system default."
        ));
        updateProperties.put("rank", Map.of(
                "type", "integer",
                "description", "Rank of this definition among several with the same property name."
        ));
        updateProperties.put("retryNb", Map.of(
                "type", "integer",
                "description", "How many times to retry when the property resolves to nothing."
        ));
        updateProperties.put("retryPeriod", Map.of(
                "type", "integer",
                "description", "Milliseconds between retries. Only meaningful with retryNb above 0."
        ));
        updateProperties.put("cacheExpire", Map.of(
                "type", "integer",
                "description", "Seconds the resolved value stays cached. 0 disables caching."
        ));
        updateProperties.put("description", Map.of(
                "type", "string",
                "description", "New description of the property."
        ));
        updateProperties.put("nature", Map.of(
                "type", "string",
                "description", "New value selection strategy.",
                "enum", List.of(
                        TestCaseCountryProperties.NATURE_STATIC,
                        TestCaseCountryProperties.NATURE_RANDOM,
                        TestCaseCountryProperties.NATURE_RANDOMNEW,
                        TestCaseCountryProperties.NATURE_NOTINUSE
                )
        ));

        Map<String, Object> updatesSchema = new LinkedHashMap<>();
        updatesSchema.put("type", "object");
        updatesSchema.put("description", "Fields to update. Only provide the fields that need to change.");
        updatesSchema.put("properties", updateProperties);
        // Reject unknown fields early so the switch in execute() never hits the default branch unexpectedly.
        updatesSchema.put("additionalProperties", false);

        Map<String, Object> properties = new LinkedHashMap<>();
        properties.put("testFolder", Map.of(
                "type", "string",
                "description", "Name of the test folder containing the testcase."
        ));
        properties.put("testcase", Map.of(
                "type", "string",
                "description", "Identifier of the testcase."
        ));
        properties.put("country", Map.of(
                "type", "string",
                "description", "Country the property is scoped to (use ALL for properties defined on all countries)."
        ));
        properties.put("property", Map.of(
                "type", "string",
                "description", "Name of the property to update."
        ));
        properties.put("updates", updatesSchema);

        return new McpSchema.Tool(
                TOOL_NAME,
                null,
                """
                Updates an existing property on a testcase for a specific country.

                Call this tool whenever the user asks to modify the value, type, or description of a testcase property.
                Only provide the fields that need to change in the updates object.

                Use cerberus_testcase_country_property_list to find the exact property name and country before updating.

                Do not call this tool when the user only asks to list, read, create, or delete properties.
                """,
                new McpSchema.JsonSchema(
                        "object",
                        properties,
                        List.of("testFolder", "testcase", "country", "property", "updates"),
                        null,
                        null,
                        null
                ),
                null,
                MCPToolUtils.updateAnnotations("Update testcase country property", false),
                null
        );
    }

    /**
     * Validates input arguments, applies the requested field changes to the existing entity,
     * persists the update, and returns a JSON result containing the refreshed property DTO.
     *
     * <p>After a successful update, all rows for the (testFolder, testcase, property) key are
     * reloaded so that every country entry can be aggregated into the response DTO. This reload
     * is necessary because the service's update path does not return the refreshed entity.</p>
     *
     * @param args raw MCP arguments map provided by the AI client
     * @return a {@link McpSchema.CallToolResult} containing either a success JSON payload or an error message
     */
    @SuppressWarnings("unchecked")
    private McpSchema.CallToolResult execute(Map<String, Object> args) {
        String testFolder = MCPToolUtils.getString(args, "testFolder", "");
        String testcaseId = MCPToolUtils.getString(args, "testcase", "");
        String country = MCPToolUtils.getString(args, "country", "");
        String property = MCPToolUtils.getString(args, "property", "");

        mcpLogUtils.call(TOOL_NAME, "testcase_country_property_update",
                String.format("MCP tool %s called with testFolder=%s testcase=%s country=%s property=%s",
                        TOOL_NAME, testFolder, testcaseId, country, property));

        if (testFolder.isBlank()) return MCPToolUtils.errorText("Missing required parameter: testFolder");
        if (testcaseId.isBlank()) return MCPToolUtils.errorText("Missing required parameter: testcase");
        if (country.isBlank()) return MCPToolUtils.errorText("Missing required parameter: country");
        if (property.isBlank()) return MCPToolUtils.errorText("Missing required parameter: property");

        Object updatesObject = args.get("updates");
        if (!(updatesObject instanceof Map)) {
            return MCPToolUtils.errorText("Missing or invalid required parameter: updates");
        }

        Map<String, Object> updates = (Map<String, Object>) updatesObject;
        if (updates.isEmpty()) {
            return MCPToolUtils.errorText("No field provided to update.");
        }

        // Load the existing entity so we can apply partial updates on top of its current state.
        TestCaseCountryProperties existing;
        try {
            existing = testCaseCountryPropertiesService.findTestCaseCountryPropertiesByKey(testFolder, testcaseId, country, property);
        } catch (CerberusException e) {
            return MCPToolUtils.errorText("Property does not exist: property=" + property + " country=" + country);
        }

        if (existing == null) {
            return MCPToolUtils.errorText("Property does not exist: property=" + property + " country=" + country);
        }

        try {
            for (Map.Entry<String, Object> entry : updates.entrySet()) {
                String field = entry.getKey();
                Object value = entry.getValue();

                switch (field) {
                    case "type":
                        existing.setType(asString(value, field));
                        break;
                    case "value1":
                        existing.setValue1(asString(value, field));
                        break;
                    case "value2":
                        existing.setValue2(asString(value, field));
                        break;
                    case "value3":
                        existing.setValue3(asString(value, field));
                        break;
                    case "database": {
                        String database = asString(value, field);
                        if (!database.isBlank()) {
                            AnswerItem<Invariant> known =
                                    invariantService.readByKey(PROPERTY_DATABASE_INVARIANT, database);
                            if (!known.isCodeStringEquals("OK") || known.getItem() == null) {
                                return MCPToolUtils.errorText("Unknown database '" + database
                                        + "'. Valid names come from the " + PROPERTY_DATABASE_INVARIANT
                                        + " invariant — call cerberus_invariant_list with type="
                                        + PROPERTY_DATABASE_INVARIANT + " to see them. Each one still has to "
                                        + "be mapped to a connection for the country and environment the "
                                        + "testcase runs in.");
                            }
                        }
                        existing.setDatabase(database);
                        break;
                    }
                    case "length":
                        // Stored as a string: a RANDOM text property reads it back with parseIntegerParam,
                        // which treats an empty column as zero.
                        existing.setLength(String.valueOf(asInteger(value, field)));
                        break;
                    case "rowLimit":
                        existing.setRowLimit(asInteger(value, field));
                        break;
                    case "rank":
                        existing.setRank(asInteger(value, field));
                        break;
                    case "retryNb":
                        existing.setRetryNb(asInteger(value, field));
                        break;
                    case "retryPeriod":
                        existing.setRetryPeriod(asInteger(value, field));
                        break;
                    case "cacheExpire":
                        existing.setCacheExpire(asInteger(value, field));
                        break;
                    case "description":
                        existing.setDescription(asString(value, field));
                        break;
                    case "nature":
                        existing.setNature(asString(value, field));
                        break;
                    default:
                        return MCPToolUtils.errorText("Unsupported field for property update: " + field);
                }
            }
        } catch (IllegalArgumentException e) {
            return MCPToolUtils.errorText(e.getMessage());
        }

        // Tag the modification source for audit purposes.
        existing.setUsrModif("MCP");

        Answer answer = testCaseCountryPropertiesService.update(existing);

        if (!answer.isCodeStringEquals("OK")) {
            return MCPToolUtils.errorText("Unable to update property: " + answer.getMessageDescription());
        }

        // Read the row back from the database rather than echoing the entity that was just written.
        // Two reasons, both of which produced wrong answers here:
        //
        //  - this response used to describe reloaded.get(0), an arbitrary row among the countries
        //    sharing this property name. Updating FR on a property also defined for DE reported DE's
        //    values as the outcome of the call;
        //  - TestCaseCountryPropertiesDAO, like its neighbours, logs a failed UPDATE and returns
        //    normally, so "updated" is not by itself evidence that anything was written.
        //
        // Re-reading the exact (testcase, country, property) row makes the answer the database's
        // state, which is the only thing worth reporting.
        TestCaseCountryProperties written;
        try {
            written = testCaseCountryPropertiesService
                    .findTestCaseCountryPropertiesByKey(testFolder, testcaseId, country, property);
        } catch (CerberusException e) {
            written = null;
        }
        if (written == null) {
            return MCPToolUtils.errorText("The update was accepted but the property could not be read back "
                    + "for country '" + country + "'. Check it with cerberus_testcase_country_property_get "
                    + "before relying on it.");
        }

        // Named so the caller sees which countries share this property name and were deliberately
        // left alone — the scoping is per country, and that is easy to forget.
        List<TestCaseCountryProperties> allCountries = testCaseCountryPropertiesService
                .findListOfPropertyPerTestTestCaseProperty(testFolder, testcaseId, property);
        List<String> otherCountries = new ArrayList<>();
        for (TestCaseCountryProperties row : allCountries) {
            if (!country.equals(row.getCountry())) {
                otherCountries.add(row.getCountry());
            }
        }

        // invariantCountries is transient and not populated by the service; the DTO carries the one
        // country this call actually touched, not every country of the property.
        written.setInvariantCountries(new ArrayList<>());
        Invariant touched = new Invariant();
        touched.setIdName("COUNTRY");
        touched.setValue(written.getCountry());
        written.getInvariantCountries().add(touched);

        TestcaseCountryPropertiesDTOV001 dto = mapper.toDTO(written);

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("status", "updated");
        response.put("country", country);
        response.put("updatedFields", new ArrayList<>(updates.keySet()));
        // Says plainly where the payload comes from, so it can be trusted without a second call.
        response.put("readBackFromDatabase", true);
        response.put("property", dto);
        if (!otherCountries.isEmpty()) {
            response.put("otherCountriesUnchanged", otherCountries);
        }
        return MCPToolUtils.successJson(response);
    }

    /**
     * Coerces an arbitrary MCP argument value to a trimmed {@link String}.
     *
     * <p>Returns an empty string for {@code null} values, matching the convention used by other
     * fields in the service layer where an absent optional string is treated as empty.</p>
     *
     * @param value the raw value from the MCP arguments map
     * @param field the field name, used solely for the error message
     * @return the trimmed string value, or {@code ""} if {@code value} is {@code null}
     * @throws IllegalArgumentException if {@code value} is non-null but not a {@link String}
     */
    /**
     * Coerces {@code value} to a primitive {@code int}.
     *
     * @throws IllegalArgumentException when the caller sent something that is not a whole number.
     */
    private int asInteger(Object value, String field) {
        if (value instanceof Integer intValue) {
            return intValue;
        }
        if (value instanceof Number number) {
            // JSON deserializers may hand back a Long or a Double for a whole number.
            return number.intValue();
        }
        throw new IllegalArgumentException("Invalid value for field '" + field + "'. Expected integer.");
    }

    private String asString(Object value, String field) {
        if (value == null) return "";
        if (!(value instanceof String)) {
            throw new IllegalArgumentException("Invalid value for field '" + field + "'. Expected string.");
        }
        return ((String) value).trim();
    }

}
