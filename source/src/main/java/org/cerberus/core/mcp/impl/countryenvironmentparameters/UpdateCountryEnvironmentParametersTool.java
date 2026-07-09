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
package org.cerberus.core.mcp.impl.countryenvironmentparameters;

import io.modelcontextprotocol.server.McpServerFeatures;
import io.modelcontextprotocol.spec.McpSchema;
import org.cerberus.core.api.dto.application.CountryEnvironmentParametersDTOV001;
import org.cerberus.core.api.dto.application.CountryEnvironmentParametersMapperV001;
import org.cerberus.core.crud.entity.CountryEnvironmentParameters;
import org.cerberus.core.crud.service.ICountryEnvironmentParametersService;
import org.cerberus.core.mcp.MCPTool;
import org.cerberus.core.mcp.util.MCPLogUtils;
import org.cerberus.core.mcp.util.MCPToolUtils;
import org.cerberus.core.util.answer.Answer;
import org.cerberus.core.util.answer.AnswerItem;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * MCP tool that updates an existing {@link CountryEnvironmentParameters} entry.
 *
 * <p>Exposed MCP tool name: {@code cerberus_country_environment_parameters_update}.</p>
 *
 * <p>Applies a read-before-write pattern: the existing entry is loaded first so untouched
 * fields retain their current values. Only the fields provided in the {@code updates} map
 * are modified.</p>
 *
 * <p>Delegates persistence to {@link ICountryEnvironmentParametersService#update(CountryEnvironmentParameters)}.</p>
 */
@Component
public class UpdateCountryEnvironmentParametersTool implements MCPTool {

    private static final String TOOL_NAME = "cerberus_country_environment_parameters_update";

    private final ICountryEnvironmentParametersService countryEnvironmentParametersService;
    private final CountryEnvironmentParametersMapperV001 mapper;
    private final MCPLogUtils mcpLogUtils;

    public UpdateCountryEnvironmentParametersTool(ICountryEnvironmentParametersService countryEnvironmentParametersService,
                                                  CountryEnvironmentParametersMapperV001 mapper,
                                                  MCPLogUtils mcpLogUtils) {
        this.countryEnvironmentParametersService = countryEnvironmentParametersService;
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
     * Builds the MCP {@link McpSchema.Tool} descriptor for {@code cerberus_country_environment_parameters_update}.
     *
     * <p>The input schema uses a nested {@code updates} object with {@code additionalProperties: false}
     * so the AI model cannot send unrecognised fields.</p>
     *
     * @return the fully configured tool descriptor
     */
    private McpSchema.Tool createTool() {
        Map<String, Object> updateProperties = new LinkedHashMap<>();
        updateProperties.put("isActive", Map.of(
                "type", "boolean",
                "description", "New active flag for this application on this environment."
        ));
        updateProperties.put("endPoint", Map.of(
                "type", "string",
                "description", "New endpoint (IP address or hostname) where the application is reachable."
        ));
        updateProperties.put("contextRoot", Map.of(
                "type", "string",
                "description", "New context root / base URL of the application."
        ));
        updateProperties.put("urlLogin", Map.of(
                "type", "string",
                "description", "New login URL of the application."
        ));
        updateProperties.put("domain", Map.of(
                "type", "string",
                "description", "New domain of the application."
        ));
        updateProperties.put("var1", Map.of("type", "string", "description", "New value for free-form variable 1."));
        updateProperties.put("var2", Map.of("type", "string", "description", "New value for free-form variable 2."));
        updateProperties.put("var3", Map.of("type", "string", "description", "New value for free-form variable 3."));
        updateProperties.put("var4", Map.of("type", "string", "description", "New value for free-form variable 4."));
        updateProperties.put("secret1", Map.of("type", "string", "description", "New secret value 1 (e.g. password)."));
        updateProperties.put("secret2", Map.of("type", "string", "description", "New secret value 2 (e.g. API key)."));
        updateProperties.put("mobileActivity", Map.of("type", "string", "description", "New mobile application activity (Android)."));
        updateProperties.put("mobilePackage", Map.of("type", "string", "description", "New mobile application package identifier."));
        updateProperties.put("poolSize", Map.of(
                "type", "integer",
                "description", "New maximum number of concurrent executions allowed for this application/environment."
        ));

        Map<String, Object> updatesSchema = new LinkedHashMap<>();
        updatesSchema.put("type", "object");
        updatesSchema.put("description", "Fields to update. Only provide the fields that need to change.");
        updatesSchema.put("properties", updateProperties);
        // Reject unknown fields early so the switch in execute() never hits the default branch unexpectedly.
        updatesSchema.put("additionalProperties", false);

        Map<String, Object> properties = new LinkedHashMap<>();
        properties.put("system", Map.of(
                "type", "string",
                "description", "Name of the system (workspace) the entry belongs to."
        ));
        properties.put("country", Map.of(
                "type", "string",
                "description", "Exact country code of the entry to update."
        ));
        properties.put("environment", Map.of(
                "type", "string",
                "description", "Exact environment name of the entry to update."
        ));
        properties.put("application", Map.of(
                "type", "string",
                "description", "Exact application name of the entry to update."
        ));
        properties.put("updates", updatesSchema);

        return new McpSchema.Tool(
                TOOL_NAME,
                null,
                """
                Updates an existing per-application environment configuration (CountryEnvironmentParameters).

                Call this tool whenever the user asks to modify the endpoint, context root, login URL,
                credentials variables, pool size, or activation status of an application on a specific
                system/country/environment.
                Only provide the fields that need to change in the updates object.

                Use cerberus_country_environment_parameters_list to find the exact
                system/country/environment/application combination before updating.

                Do not call this tool when the user only asks to list, read, create, or delete entries.
                """,
                new McpSchema.JsonSchema(
                        "object",
                        properties,
                        List.of("system", "country", "environment", "application", "updates"),
                        null,
                        null,
                        null
                ),
                null,
                MCPToolUtils.updateAnnotations("Update country environment parameter (per application)", false),
                null
        );
    }

    /**
     * Validates input, loads the existing entry, applies the requested field changes,
     * persists the update, and returns a JSON result with the updated DTO.
     *
     * @param args raw MCP arguments map from the client request
     * @return a success JSON result containing the updated entry DTO, or an error text result
     */
    @SuppressWarnings("unchecked")
    private McpSchema.CallToolResult execute(Map<String, Object> args) {
        String system = MCPToolUtils.getString(args, "system", "");
        String country = MCPToolUtils.getString(args, "country", "");
        String environment = MCPToolUtils.getString(args, "environment", "");
        String application = MCPToolUtils.getString(args, "application", "");

        mcpLogUtils.call(TOOL_NAME, "country_environment_parameters_update",
                String.format("MCP tool %s called with system=%s country=%s environment=%s application=%s", TOOL_NAME, system, country, environment, application));

        if (system.isBlank()) return MCPToolUtils.errorText("Missing required parameter: system");
        if (country.isBlank()) return MCPToolUtils.errorText("Missing required parameter: country");
        if (environment.isBlank()) return MCPToolUtils.errorText("Missing required parameter: environment");
        if (application.isBlank()) return MCPToolUtils.errorText("Missing required parameter: application");

        Object updatesObject = args.get("updates");
        if (!(updatesObject instanceof Map)) {
            return MCPToolUtils.errorText("Missing or invalid required parameter: updates");
        }
        Map<String, Object> updates = (Map<String, Object>) updatesObject;
        if (updates.isEmpty()) {
            return MCPToolUtils.errorText("No field provided to update.");
        }

        // Read the existing entity so unmodified fields retain their current values.
        AnswerItem<CountryEnvironmentParameters> readAnswer = countryEnvironmentParametersService.readByKey(system, country, environment, application);
        if (!readAnswer.isCodeStringEquals("OK") || readAnswer.getItem() == null) {
            return MCPToolUtils.errorText("Country environment parameter does not exist: system=" + system + " country=" + country + " environment=" + environment + " application=" + application);
        }

        CountryEnvironmentParameters cep = readAnswer.getItem();

        try {
            for (Map.Entry<String, Object> entry : updates.entrySet()) {
                String field = entry.getKey();
                Object value = entry.getValue();
                switch (field) {
                    case "isActive":
                        cep.setActive(asBoolean(value, field));
                        break;
                    case "endPoint":
                        cep.setIp(asString(value, field));
                        break;
                    case "contextRoot":
                        cep.setUrl(asString(value, field));
                        break;
                    case "urlLogin":
                        cep.setUrlLogin(asString(value, field));
                        break;
                    case "domain":
                        cep.setDomain(asString(value, field));
                        break;
                    case "var1":
                        cep.setVar1(asString(value, field));
                        break;
                    case "var2":
                        cep.setVar2(asString(value, field));
                        break;
                    case "var3":
                        cep.setVar3(asString(value, field));
                        break;
                    case "var4":
                        cep.setVar4(asString(value, field));
                        break;
                    case "secret1":
                        cep.setSecret1(asString(value, field));
                        break;
                    case "secret2":
                        cep.setSecret2(asString(value, field));
                        break;
                    case "mobileActivity":
                        cep.setMobileActivity(asString(value, field));
                        break;
                    case "mobilePackage":
                        cep.setMobilePackage(asString(value, field));
                        break;
                    case "poolSize":
                        cep.setPoolSize(asInteger(value, field));
                        break;
                    default:
                        return MCPToolUtils.errorText("Unsupported field for country environment parameter update: " + field);
                }
            }
        } catch (IllegalArgumentException e) {
            return MCPToolUtils.errorText(e.getMessage());
        }

        // Tag the modifier so audit trails identify MCP-originated changes.
        cep.setUsrModif("MCP");

        Answer answer = countryEnvironmentParametersService.update(cep);

        if (!answer.isCodeStringEquals("OK")) {
            return MCPToolUtils.errorText("Unable to update country environment parameter: " + answer.getMessageDescription());
        }

        CountryEnvironmentParametersDTOV001 dto = mapper.toDTO(cep);
        // Strip secret1/secret2 before returning the entry to the MCP client.
        dto.setSecret1(null);
        dto.setSecret2(null);

        return MCPToolUtils.successJson(Map.of(
                "status", "updated",
                "entry", dto
        ));
    }

    /**
     * Coerces a raw MCP argument value to a trimmed {@link String}.
     *
     * @param value the raw value from the arguments map
     * @param field the field name, used in the exception message
     * @return the trimmed string, or {@code ""} if {@code value} is {@code null}
     * @throws IllegalArgumentException if {@code value} is non-null but not a {@link String}
     */
    private String asString(Object value, String field) {
        if (value == null) return "";
        if (!(value instanceof String)) {
            throw new IllegalArgumentException("Invalid value for field '" + field + "'. Expected string.");
        }
        return ((String) value).trim();
    }

    /**
     * Coerces a raw MCP argument value to a {@link Boolean}.
     *
     * @param value the raw value from the arguments map
     * @param field the field name, used in the exception message
     * @return the boolean value
     * @throws IllegalArgumentException if {@code value} is not a {@link Boolean}
     */
    private boolean asBoolean(Object value, String field) {
        if (!(value instanceof Boolean)) {
            throw new IllegalArgumentException("Invalid value for field '" + field + "'. Expected boolean.");
        }
        return (Boolean) value;
    }

    /**
     * Coerces a raw MCP argument value to an {@link Integer}.
     * Handles both {@code Integer} and other {@link Number} subtypes because JSON
     * deserializers may produce {@code Long} or {@code Double} for numeric literals.
     *
     * @param value the raw value from the arguments map
     * @param field the field name, used in the exception message
     * @return integer value
     * @throws IllegalArgumentException if the value cannot be interpreted as an integer
     */
    private int asInteger(Object value, String field) {
        if (value instanceof Integer) return (Integer) value;
        if (value instanceof Number) return ((Number) value).intValue();
        throw new IllegalArgumentException("Invalid value for field '" + field + "'. Expected integer.");
    }

}