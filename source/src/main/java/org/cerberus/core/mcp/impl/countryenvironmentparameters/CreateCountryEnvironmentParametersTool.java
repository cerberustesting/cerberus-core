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
 * MCP tool that creates a new {@link CountryEnvironmentParameters} entry in Cerberus.
 *
 * <p>Exposed MCP tool name: {@code cerberus_country_environment_parameters_create}.</p>
 *
 * <p>Before persisting, verifies that no entry with the same (system, country, environment,
 * application) key already exists to prevent duplicate key errors at the database level.</p>
 *
 * <p>If no {@code CountryEnvParam} exists yet for the (system, country, environment) combination,
 * {@link ICountryEnvironmentParametersService#create(CountryEnvironmentParameters)} creates one
 * automatically with default settings — so calling {@code cerberus_country_env_param_create}
 * beforehand is optional but recommended for full control over the environment metadata.</p>
 *
 * <p>Delegates all persistence operations to {@link ICountryEnvironmentParametersService}.</p>
 */
@Component
public class CreateCountryEnvironmentParametersTool implements MCPTool {

    private static final String TOOL_NAME = "cerberus_country_environment_parameters_create";

    private final ICountryEnvironmentParametersService countryEnvironmentParametersService;
    private final CountryEnvironmentParametersMapperV001 mapper;
    private final MCPLogUtils mcpLogUtils;

    public CreateCountryEnvironmentParametersTool(ICountryEnvironmentParametersService countryEnvironmentParametersService,
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
     * Builds the MCP {@link McpSchema.Tool} descriptor for {@code cerberus_country_environment_parameters_create}.
     *
     * <p>Declares {@code system}, {@code country}, {@code environment}, and {@code application} as
     * required parameters. All connection settings are optional.</p>
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
                "description", "Country code for this entry (e.g. 'FR'). Must already exist as a COUNTRY invariant."
        ));
        properties.put("environment", Map.of(
                "type", "string",
                "description", "Environment name for this entry (e.g. 'DEV', 'QA', 'PROD'). Must already exist as an ENVIRONMENT invariant."
        ));
        properties.put("application", Map.of(
                "type", "string",
                "description", "Name of the application this entry configures. Must already exist (see cerberus_application_get / cerberus_application_create)."
        ));
        properties.put("isActive", Map.of(
                "type", "boolean",
                "description", "Whether this application is active on this environment. Defaults to true."
        ));
        properties.put("endPoint", Map.of(
                "type", "string",
                "description", "Optional endpoint (IP address or hostname) where the application is reachable."
        ));
        properties.put("contextRoot", Map.of(
                "type", "string",
                "description", "Optional context root / base URL of the application."
        ));
        properties.put("urlLogin", Map.of(
                "type", "string",
                "description", "Optional login URL of the application."
        ));
        properties.put("domain", Map.of(
                "type", "string",
                "description", "Optional domain of the application."
        ));
        properties.put("var1", Map.of("type", "string", "description", "Optional free-form variable 1, usable in testcase steps."));
        properties.put("var2", Map.of("type", "string", "description", "Optional free-form variable 2, usable in testcase steps."));
        properties.put("var3", Map.of("type", "string", "description", "Optional free-form variable 3, usable in testcase steps."));
        properties.put("var4", Map.of("type", "string", "description", "Optional free-form variable 4, usable in testcase steps."));
        properties.put("secret1", Map.of("type", "string", "description", "Optional secret value 1 (e.g. password), usable in testcase steps."));
        properties.put("secret2", Map.of("type", "string", "description", "Optional secret value 2 (e.g. API key), usable in testcase steps."));
        properties.put("mobileActivity", Map.of("type", "string", "description", "Optional mobile application activity (Android)."));
        properties.put("mobilePackage", Map.of("type", "string", "description", "Optional mobile application package identifier."));
        properties.put("poolSize", Map.of(
                "type", "integer",
                "description", "Optional maximum number of concurrent executions allowed for this application/environment. Defaults to 10."
        ));

        return new McpSchema.Tool(
                TOOL_NAME,
                null,
                """
                Creates a new per-application environment configuration (CountryEnvironmentParameters),
                defining how a given application is reached on a given system/country/environment
                (endpoint, context root, login URL, credentials variables, pool size, ...).

                Call this tool whenever the user asks to configure, connect, or activate an application
                on a specific country/environment.

                The application must already exist — use cerberus_application_get or
                cerberus_application_create beforehand. Country and environment values must already exist
                as invariants (types COUNTRY and ENVIRONMENT) — use cerberus_invariant_list or
                cerberus_invariant_create to manage them.

                If no environment configuration (CountryEnvParam) exists yet for this
                system/country/environment, one is created automatically with default settings.

                This is step 4 (the final step) of onboarding a brand-new application end-to-end
                (e.g. "create a test on https://qa.auchan.fr"):
                1. Verify/create the COUNTRY and ENVIRONMENT invariants.
                2. Create the application (cerberus_application_create).
                3. Activate the environment for that system (cerberus_country_env_param_create).
                4. Configure the application on that environment (this tool) — set endPoint/contextRoot
                   to the application's URL on that environment (e.g. "https://qa.auchan.fr"), and a
                   small poolSize (e.g. 3) for non-production environments.
                Only after all four layers exist should test cases targeting this application/environment
                be created.

                Do not call this tool when the user only asks to list, read, update, or delete entries.
                """,
                new McpSchema.JsonSchema(
                        "object",
                        properties,
                        List.of("system", "country", "environment", "application"),
                        null,
                        null,
                        null
                ),
                null,
                MCPToolUtils.createAnnotations("Create country environment parameter (per application)", false),
                null
        );
    }

    /**
     * Validates the arguments, checks for duplicates, builds the entity, and delegates
     * creation to {@link ICountryEnvironmentParametersService#create(CountryEnvironmentParameters)}.
     *
     * @param args raw MCP arguments map from the client request
     * @return a success JSON result containing the created entry DTO, or an error text result
     */
    private McpSchema.CallToolResult execute(Map<String, Object> args) {
        String system = MCPToolUtils.getString(args, "system", "");
        String country = MCPToolUtils.getString(args, "country", "");
        String environment = MCPToolUtils.getString(args, "environment", "");
        String application = MCPToolUtils.getString(args, "application", "");

        mcpLogUtils.call(TOOL_NAME, "country_environment_parameters_create",
                String.format("MCP tool %s called with system=%s country=%s environment=%s application=%s", TOOL_NAME, system, country, environment, application));

        if (system.isBlank()) return MCPToolUtils.errorText("Missing required parameter: system");
        if (country.isBlank()) return MCPToolUtils.errorText("Missing required parameter: country");
        if (environment.isBlank()) return MCPToolUtils.errorText("Missing required parameter: environment");
        if (application.isBlank()) return MCPToolUtils.errorText("Missing required parameter: application");

        // Guard against duplicate (system, country, environment, application) key before hitting the DB unique constraint.
        AnswerItem<CountryEnvironmentParameters> existing = countryEnvironmentParametersService.readByKey(system, country, environment, application);
        if (existing.isCodeStringEquals("OK") && existing.getItem() != null) {
            return MCPToolUtils.errorText("Country environment parameter already exists: system=" + system + " country=" + country + " environment=" + environment + " application=" + application);
        }

        CountryEnvironmentParameters cep = new CountryEnvironmentParameters(system, country, environment, application);
        cep.setActive(MCPToolUtils.getBoolean(args, "isActive", true));
        cep.setIp(MCPToolUtils.getString(args, "endPoint", ""));
        cep.setUrl(MCPToolUtils.getString(args, "contextRoot", ""));
        cep.setUrlLogin(MCPToolUtils.getString(args, "urlLogin", ""));
        cep.setDomain(MCPToolUtils.getString(args, "domain", ""));
        cep.setVar1(MCPToolUtils.getString(args, "var1", ""));
        cep.setVar2(MCPToolUtils.getString(args, "var2", ""));
        cep.setVar3(MCPToolUtils.getString(args, "var3", ""));
        cep.setVar4(MCPToolUtils.getString(args, "var4", ""));
        cep.setSecret1(MCPToolUtils.getString(args, "secret1", ""));
        cep.setSecret2(MCPToolUtils.getString(args, "secret2", ""));
        cep.setMobileActivity(MCPToolUtils.getString(args, "mobileActivity", ""));
        cep.setMobilePackage(MCPToolUtils.getString(args, "mobilePackage", ""));
        cep.setPoolSize(MCPToolUtils.getInteger(args, "poolSize", CountryEnvironmentParameters.DEFAULT_POOLSIZE));
        // Tag the creator so audit trails distinguish MCP-driven creation from UI creation.
        cep.setUsrCreated("MCP");

        Answer answer = countryEnvironmentParametersService.create(cep);

        if (!answer.isCodeStringEquals("OK")) {
            return MCPToolUtils.errorText("Unable to create country environment parameter: " + answer.getMessageDescription());
        }

        return MCPToolUtils.successJson(Map.of(
                "status", "created",
                "entry", mapper.toDTO(cep)
        ));
    }

}