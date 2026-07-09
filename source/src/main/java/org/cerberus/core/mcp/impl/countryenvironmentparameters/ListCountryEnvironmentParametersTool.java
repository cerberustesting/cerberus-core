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
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * MCP tool that lists {@link CountryEnvironmentParameters} entries: the per-application
 * connection settings (endpoint, context root, credentials, ...) for a system/country/environment.
 *
 * <p>Exposed MCP tool name: {@code cerberus_country_environment_parameters_list}.</p>
 *
 * <p>A {@code CountryEnvironmentParameters} row is the child of both a {@code CountryEnvParam}
 * (see {@code cerberus_country_env_param_list}) and an application (see
 * {@code cerberus_application_list}) — it configures how a given application is reached on a
 * given system/country/environment.</p>
 *
 * <p>Delegates to {@link ICountryEnvironmentParametersService#readByVarious(String, String, String, String)}
 * and converts results via {@link CountryEnvironmentParametersMapperV001}.</p>
 */
@Component
public class ListCountryEnvironmentParametersTool implements MCPTool {

    private static final String TOOL_NAME = "cerberus_country_environment_parameters_list";

    private final ICountryEnvironmentParametersService countryEnvironmentParametersService;
    private final CountryEnvironmentParametersMapperV001 mapper;
    private final MCPLogUtils mcpLogUtils;

    public ListCountryEnvironmentParametersTool(ICountryEnvironmentParametersService countryEnvironmentParametersService,
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
     * Builds the MCP {@link McpSchema.Tool} descriptor for {@code cerberus_country_environment_parameters_list}.
     *
     * <p>Declares {@code system} as the only required parameter. {@code country}, {@code environment},
     * and {@code application} narrow the result down; {@code search} filters client-side.</p>
     *
     * @return the fully configured tool descriptor
     */
    private McpSchema.Tool createTool() {
        Map<String, Object> properties = new LinkedHashMap<>();
        properties.put("system", Map.of(
                "type", "string",
                "description", "Name of the system (workspace) whose per-application environment settings to list."
        ));
        properties.put("country", Map.of(
                "type", "string",
                "description", "Optional exact country code to filter on (e.g. 'FR')."
        ));
        properties.put("environment", Map.of(
                "type", "string",
                "description", "Optional exact environment name to filter on (e.g. 'DEV', 'QA', 'PROD')."
        ));
        properties.put("application", Map.of(
                "type", "string",
                "description", "Optional exact application name to filter on."
        ));
        properties.put("search", Map.of(
                "type", "string",
                "description", "Optional text filter applied on application, endpoint, domain, or context root (case-insensitive)."
        ));

        return new McpSchema.Tool(
                TOOL_NAME,
                null,
                """
                Lists the per-application environment settings (CountryEnvironmentParameters) defined
                for a system, such as endpoint (IP/host), context root, login URL, and pool size.

                Call this tool whenever the user asks to list, browse, or search how applications are
                configured on a specific system, optionally narrowed to a country, environment, or application.

                Use cerberus_country_environment_parameters_get to retrieve the full details of one specific entry.
                Do not call this tool when the user asks to create, update, or delete an entry.
                """,
                new McpSchema.JsonSchema(
                        "object",
                        properties,
                        List.of("system"),
                        null,
                        null,
                        null
                ),
                null,
                MCPToolUtils.readOnlyAnnotations("List country environment parameters (per application)", false),
                null
        );
    }

    /**
     * Loads all entries for the given system (optionally narrowed by country/environment/application),
     * applies an optional search filter, maps them to DTOs, and returns a JSON list.
     *
     * @param args raw MCP arguments map from the client request
     * @return a success JSON result containing the entry list, or an error text result
     */
    private McpSchema.CallToolResult execute(Map<String, Object> args) {
        String system = MCPToolUtils.getString(args, "system", "");
        String country = MCPToolUtils.getString(args, "country", "");
        String environment = MCPToolUtils.getString(args, "environment", "");
        String application = MCPToolUtils.getString(args, "application", "");
        String search = MCPToolUtils.getString(args, "search", "");

        mcpLogUtils.call(TOOL_NAME, "country_environment_parameters_list",
                String.format("MCP tool %s called with system=%s country=%s environment=%s application=%s", TOOL_NAME, system, country, environment, application));

        if (system.isBlank()) {
            return MCPToolUtils.errorText("Missing required parameter: system");
        }

        List<CountryEnvironmentParameters> raw = countryEnvironmentParametersService.readByVarious(system, country, environment, application).getDataList();

        List<Object> entries = raw.stream()
                .filter(cep -> matchesSearch(cep, search))
                .map(mapper::toDTO)
                .map(this::withoutSecrets)
                .map(Object.class::cast)
                .toList();

        return MCPToolUtils.successJson(Map.of(
                "system", system,
                "count", entries.size(),
                "entries", entries
        ));
    }

    /**
     * Returns {@code true} when application, IP/endpoint, domain, or URL contains the search term
     * (case-insensitive), or when no search term was provided.
     *
     * @param cep    the entry to test
     * @param search the search string; blank means "match all"
     * @return whether this entry passes the search filter
     */
    private boolean matchesSearch(CountryEnvironmentParameters cep, String search) {
        if (search.isBlank()) return true;
        return MCPToolUtils.containsIgnoreCase(cep.getApplication(), search)
                || MCPToolUtils.containsIgnoreCase(cep.getIp(), search)
                || MCPToolUtils.containsIgnoreCase(cep.getDomain(), search)
                || MCPToolUtils.containsIgnoreCase(cep.getUrl(), search);
    }

    /**
     * Strips the secret1/secret2 credential fields from a DTO before it is returned to the
     * MCP client.
     *
     * @param dto the DTO to sanitize
     * @return the same DTO instance, with secret1/secret2 cleared
     */
    private CountryEnvironmentParametersDTOV001 withoutSecrets(CountryEnvironmentParametersDTOV001 dto) {
        dto.setSecret1(null);
        dto.setSecret2(null);
        return dto;
    }

}