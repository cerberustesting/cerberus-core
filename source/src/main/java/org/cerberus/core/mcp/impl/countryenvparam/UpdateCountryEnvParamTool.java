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
package org.cerberus.core.mcp.impl.countryenvparam;

import io.modelcontextprotocol.server.McpServerFeatures;
import io.modelcontextprotocol.spec.McpSchema;
import org.cerberus.core.crud.entity.CountryEnvParam;
import org.cerberus.core.crud.service.ICountryEnvParamService;
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
 * MCP tool that updates an existing {@link CountryEnvParam} entry.
 *
 * <p>Exposed MCP tool name: {@code cerberus_country_env_param_update}.</p>
 *
 * <p>Applies a read-before-write pattern: the existing entry is loaded first so untouched
 * fields retain their current values. Only the fields provided in the {@code updates} map
 * are modified.</p>
 *
 * <p>Delegates persistence to {@link ICountryEnvParamService#update(CountryEnvParam)}.</p>
 */
@Component
public class UpdateCountryEnvParamTool implements MCPTool {

    private static final String TOOL_NAME = "cerberus_country_env_param_update";

    private final ICountryEnvParamService countryEnvParamService;
    private final MCPLogUtils mcpLogUtils;

    public UpdateCountryEnvParamTool(ICountryEnvParamService countryEnvParamService, MCPLogUtils mcpLogUtils) {
        this.countryEnvParamService = countryEnvParamService;
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
     * Builds the MCP {@link McpSchema.Tool} descriptor for {@code cerberus_country_env_param_update}.
     *
     * <p>The input schema uses a nested {@code updates} object with {@code additionalProperties: false}
     * so the AI model cannot send unrecognised fields.</p>
     *
     * @return the fully configured tool descriptor
     */
    private McpSchema.Tool createTool() {
        Map<String, Object> updateProperties = new LinkedHashMap<>();
        updateProperties.put("description", Map.of(
                "type", "string",
                "description", "New human-readable description of this environment configuration."
        ));
        updateProperties.put("build", Map.of(
                "type", "string",
                "description", "New build identifier currently deployed on this environment."
        ));
        updateProperties.put("revision", Map.of(
                "type", "string",
                "description", "New revision identifier currently deployed on this environment."
        ));
        updateProperties.put("chain", Map.of(
                "type", "string",
                "description", "New deployment chain / pipeline name."
        ));
        updateProperties.put("type", Map.of(
                "type", "string",
                "description", "New environment type code (e.g. 'STD')."
        ));
        updateProperties.put("distribList", Map.of(
                "type", "string",
                "description", "New comma-separated list of e-mail addresses notified about this environment."
        ));
        updateProperties.put("active", Map.of(
                "type", "boolean",
                "description", "New active flag for this environment."
        ));
        updateProperties.put("maintenanceAct", Map.of(
                "type", "boolean",
                "description", "New maintenance window active flag."
        ));
        updateProperties.put("maintenanceStr", Map.of(
                "type", "string",
                "description", "New maintenance window start time (HH:mm:ss)."
        ));
        updateProperties.put("maintenanceEnd", Map.of(
                "type", "string",
                "description", "New maintenance window end time (HH:mm:ss)."
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
        properties.put("updates", updatesSchema);

        return new McpSchema.Tool(
                TOOL_NAME,
                null,
                """
                Updates an existing environment configuration (CountryEnvParam) for a system.

                Call this tool whenever the user asks to modify build, revision, deployment chain,
                maintenance window, or activation status of an environment.
                Only provide the fields that need to change in the updates object.

                Use cerberus_country_env_param_list to find the exact system/country/environment
                combination before updating.

                Do not call this tool when the user only asks to list, read, create, or delete entries.
                """,
                new McpSchema.JsonSchema(
                        "object",
                        properties,
                        List.of("system", "country", "environment", "updates"),
                        null,
                        null,
                        null
                ),
                null,
                MCPToolUtils.updateAnnotations("Update country environment parameter", false),
                null
        );
    }

    /**
     * Validates input, loads the existing entry, applies the requested field changes,
     * persists the update, and returns a JSON result.
     *
     * @param args raw MCP arguments map from the client request
     * @return a success JSON result containing the updated entry, or an error text result
     */
    @SuppressWarnings("unchecked")
    private McpSchema.CallToolResult execute(Map<String, Object> args) {
        String system = MCPToolUtils.getString(args, "system", "");
        String country = MCPToolUtils.getString(args, "country", "");
        String environment = MCPToolUtils.getString(args, "environment", "");

        mcpLogUtils.call(TOOL_NAME, "country_env_param_update",
                String.format("MCP tool %s called with system=%s country=%s environment=%s", TOOL_NAME, system, country, environment));

        if (system.isBlank()) return MCPToolUtils.errorText("Missing required parameter: system");
        if (country.isBlank()) return MCPToolUtils.errorText("Missing required parameter: country");
        if (environment.isBlank()) return MCPToolUtils.errorText("Missing required parameter: environment");

        Object updatesObject = args.get("updates");
        if (!(updatesObject instanceof Map)) {
            return MCPToolUtils.errorText("Missing or invalid required parameter: updates");
        }
        Map<String, Object> updates = (Map<String, Object>) updatesObject;
        if (updates.isEmpty()) {
            return MCPToolUtils.errorText("No field provided to update.");
        }

        // Read the existing entity so unmodified fields retain their current values.
        AnswerItem<CountryEnvParam> readAnswer = countryEnvParamService.readByKey(system, country, environment);
        if (!readAnswer.isCodeStringEquals("OK") || readAnswer.getItem() == null) {
            return MCPToolUtils.errorText("Country environment parameter does not exist: system=" + system + " country=" + country + " environment=" + environment);
        }

        CountryEnvParam cep = readAnswer.getItem();
        Map<String, Object> modifiedFields = new LinkedHashMap<>();

        try {
            for (Map.Entry<String, Object> entry : updates.entrySet()) {
                String field = entry.getKey();
                Object value = entry.getValue();
                switch (field) {
                    case "description":
                        cep.setDescription(asString(value, field));
                        break;
                    case "build":
                        cep.setBuild(asString(value, field));
                        break;
                    case "revision":
                        cep.setRevision(asString(value, field));
                        break;
                    case "chain":
                        cep.setChain(asString(value, field));
                        break;
                    case "type":
                        cep.setType(asString(value, field));
                        break;
                    case "distribList":
                        cep.setDistribList(asString(value, field));
                        break;
                    case "active":
                        cep.setActive(asBoolean(value, field));
                        break;
                    case "maintenanceAct":
                        cep.setMaintenanceAct(asBoolean(value, field));
                        break;
                    case "maintenanceStr":
                        cep.setMaintenanceStr(asString(value, field));
                        break;
                    case "maintenanceEnd":
                        cep.setMaintenanceEnd(asString(value, field));
                        break;
                    default:
                        return MCPToolUtils.errorText("Unsupported field for country environment parameter update: " + field);
                }
                modifiedFields.put(field, value);
            }
        } catch (IllegalArgumentException e) {
            return MCPToolUtils.errorText(e.getMessage());
        }

        Answer answer = countryEnvParamService.update(cep);

        if (!answer.isCodeStringEquals("OK")) {
            return MCPToolUtils.errorText("Unable to update country environment parameter: " + answer.getMessageDescription());
        }

        return MCPToolUtils.successJson(Map.of(
                "status", "updated",
                "system", system,
                "country", country,
                "environment", environment,
                "updatedFields", modifiedFields
        ));
    }

    /**
     * Coerces a raw MCP argument value to a trimmed {@link String}.
     *
     * @param value the raw value from the raw MCP arguments map
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

}