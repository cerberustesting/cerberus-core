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
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * MCP tool that creates a new {@link CountryEnvParam} entry in Cerberus.
 *
 * <p>Exposed MCP tool name: {@code cerberus_country_env_param_create}.</p>
 *
 * <p>Before persisting, verifies that no entry with the same (system, country, environment) key
 * already exists to prevent duplicate key errors at the database level.</p>
 *
 * <p>The country and environment values referenced here must already exist as invariants
 * (types COUNTRY and ENVIRONMENT) — this tool does not create them.</p>
 *
 * <p>Delegates all persistence operations to {@link ICountryEnvParamService}.</p>
 */
@Component
public class CreateCountryEnvParamTool implements MCPTool {

    private static final String TOOL_NAME = "cerberus_country_env_param_create";

    private final ICountryEnvParamService countryEnvParamService;
    private final MCPLogUtils mcpLogUtils;

    public CreateCountryEnvParamTool(ICountryEnvParamService countryEnvParamService, MCPLogUtils mcpLogUtils) {
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
     * Builds the MCP {@link McpSchema.Tool} descriptor for {@code cerberus_country_env_param_create}.
     *
     * <p>Declares {@code system}, {@code country}, and {@code environment} as required parameters.
     * All other fields are optional and default to blank/inactive values.</p>
     *
     * @return the fully configured tool descriptor
     */
    private McpSchema.Tool createTool() {
        Map<String, Object> properties = new LinkedHashMap<>();
        properties.put("system", Map.of(
                "type", "string",
                "description", "Name of the system (workspace) this entry belongs to."
        ));
        properties.put("country", Map.of(
                "type", "string",
                "description", "Country code to activate for this system (e.g. 'FR'). Must already exist as a COUNTRY invariant."
        ));
        properties.put("environment", Map.of(
                "type", "string",
                "description", "Environment name to activate for this system (e.g. 'DEV', 'QA', 'PROD'). Must already exist as an ENVIRONMENT invariant."
        ));
        properties.put("description", Map.of(
                "type", "string",
                "description", "Optional human-readable description of this environment configuration."
        ));
        properties.put("build", Map.of(
                "type", "string",
                "description", "Optional build identifier currently deployed on this environment."
        ));
        properties.put("revision", Map.of(
                "type", "string",
                "description", "Optional revision identifier currently deployed on this environment."
        ));
        properties.put("chain", Map.of(
                "type", "string",
                "description", "Optional deployment chain / pipeline name associated with this environment."
        ));
        properties.put("type", Map.of(
                "type", "string",
                "description", "Optional environment type code (e.g. 'STD'). Defaults to 'STD' when not provided."
        ));
        properties.put("distribList", Map.of(
                "type", "string",
                "description", "Optional comma-separated list of e-mail addresses notified about this environment."
        ));
        properties.put("active", Map.of(
                "type", "boolean",
                "description", "Whether this environment is active. Defaults to true."
        ));
        properties.put("maintenanceAct", Map.of(
                "type", "boolean",
                "description", "Whether a maintenance window is currently active on this environment. Defaults to false."
        ));
        properties.put("maintenanceStr", Map.of(
                "type", "string",
                "description", "Optional maintenance window start time (HH:mm:ss)."
        ));
        properties.put("maintenanceEnd", Map.of(
                "type", "string",
                "description", "Optional maintenance window end time (HH:mm:ss)."
        ));

        return new McpSchema.Tool(
                TOOL_NAME,
                null,
                """
                Creates a new environment configuration (CountryEnvParam) activating a country +
                environment combination for a system.

                Call this tool whenever the user asks to enable, activate, or create a new environment
                (e.g. DEV, QA, PROD) for a given country within a system.

                Country and environment values must already exist as invariants (types COUNTRY and
                ENVIRONMENT). Use cerberus_invariant_list to check available values, or
                cerberus_invariant_create to register a new one, before calling this tool.

                This is step 3 of onboarding a brand-new application end-to-end (e.g. "create a test on
                https://qa.auchan.fr"):
                1. Verify/create the COUNTRY and ENVIRONMENT invariants.
                2. Create the application (cerberus_application_create).
                3. Activate the environment for that system (this tool) — typically type "STD".
                4. Configure the application on that environment (cerberus_country_environment_parameters_create).

                Do not call this tool when the user only asks to list, read, update, or delete entries.
                """,
                new McpSchema.JsonSchema(
                        "object",
                        properties,
                        List.of("system", "country", "environment"),
                        null,
                        null,
                        null
                ),
                null,
                MCPToolUtils.createAnnotations("Create country environment parameter", false),
                null
        );
    }

    /**
     * Validates the arguments, checks for duplicates, builds the entity, and delegates
     * creation to {@link ICountryEnvParamService#create(CountryEnvParam)}.
     *
     * @param args raw MCP arguments map from the client request
     * @return a success JSON result containing the created entry, or an error text result
     */
    private McpSchema.CallToolResult execute(Map<String, Object> args) {
        String system = MCPToolUtils.getString(args, "system", "");
        String country = MCPToolUtils.getString(args, "country", "");
        String environment = MCPToolUtils.getString(args, "environment", "");

        mcpLogUtils.call(TOOL_NAME, "country_env_param_create",
                String.format("MCP tool %s called with system=%s country=%s environment=%s", TOOL_NAME, system, country, environment));

        if (system.isBlank()) return MCPToolUtils.errorText("Missing required parameter: system");
        if (country.isBlank()) return MCPToolUtils.errorText("Missing required parameter: country");
        if (environment.isBlank()) return MCPToolUtils.errorText("Missing required parameter: environment");

        // Guard against duplicate (system, country, environment) key before hitting the DB unique constraint.
        if (countryEnvParamService.exist(system, country, environment)) {
            return MCPToolUtils.errorText("Country environment parameter already exists: system=" + system + " country=" + country + " environment=" + environment);
        }

        CountryEnvParam cep = new CountryEnvParam();
        cep.setSystem(system);
        cep.setCountry(country);
        cep.setEnvironment(environment);
        cep.setDescription(MCPToolUtils.getString(args, "description", ""));
        cep.setBuild(MCPToolUtils.getString(args, "build", ""));
        cep.setRevision(MCPToolUtils.getString(args, "revision", ""));
        cep.setChain(MCPToolUtils.getString(args, "chain", ""));
        cep.setType(MCPToolUtils.getString(args, "type", "STD"));
        cep.setDistribList(MCPToolUtils.getString(args, "distribList", ""));
        cep.setActive(MCPToolUtils.getBoolean(args, "active", true));
        cep.setMaintenanceAct(MCPToolUtils.getBoolean(args, "maintenanceAct", false));
        cep.setMaintenanceStr(MCPToolUtils.getString(args, "maintenanceStr", "00:00:00"));
        cep.setMaintenanceEnd(MCPToolUtils.getString(args, "maintenanceEnd", "00:00:00"));

        Answer answer = countryEnvParamService.create(cep);

        if (!answer.isCodeStringEquals("OK")) {
            return MCPToolUtils.errorText("Unable to create country environment parameter: " + answer.getMessageDescription());
        }

        return MCPToolUtils.successJson(Map.of(
                "status", "created",
                "system", system,
                "country", country,
                "environment", environment
        ));
    }

}