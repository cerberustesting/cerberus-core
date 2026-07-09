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
import org.cerberus.core.api.dto.application.CountryEnvParamMapperV001;
import org.cerberus.core.crud.entity.CountryEnvParam;
import org.cerberus.core.crud.service.ICountryEnvParamService;
import org.cerberus.core.mcp.MCPTool;
import org.cerberus.core.mcp.util.MCPLogUtils;
import org.cerberus.core.mcp.util.MCPToolUtils;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * MCP tool that lists {@link CountryEnvParam} entries (environment configuration for a
 * system/country/environment combination).
 *
 * <p>Exposed MCP tool name: {@code cerberus_country_env_param_list}.</p>
 *
 * <p>A {@code CountryEnvParam} row activates an environment (e.g. DEV, QA, PROD) for a given
 * country within a system, and carries environment-wide metadata such as build, revision,
 * deployment chain, and maintenance window. It is the parent of the per-application
 * {@code CountryEnvironmentParameters} rows (see {@code cerberus_country_environment_parameters_list}).</p>
 *
 * <p>Delegates to {@link ICountryEnvParamService#readByVarious(String, String, String, String, String, String)}
 * and converts results via {@link CountryEnvParamMapperV001}.</p>
 */
@Component
public class ListCountryEnvParamsTool implements MCPTool {

    private static final String TOOL_NAME = "cerberus_country_env_param_list";

    private final ICountryEnvParamService countryEnvParamService;
    private final CountryEnvParamMapperV001 mapper;
    private final MCPLogUtils mcpLogUtils;

    public ListCountryEnvParamsTool(ICountryEnvParamService countryEnvParamService,
                                    CountryEnvParamMapperV001 mapper,
                                    MCPLogUtils mcpLogUtils) {
        this.countryEnvParamService = countryEnvParamService;
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
     * Builds the MCP {@link McpSchema.Tool} descriptor for {@code cerberus_country_env_param_list}.
     *
     * <p>Declares {@code system} as the only required parameter. {@code country} and
     * {@code environment} narrow the result down; {@code active} and {@code search} filter
     * the results client-side.</p>
     *
     * @return the fully configured tool descriptor
     */
    private McpSchema.Tool createTool() {
        Map<String, Object> properties = new LinkedHashMap<>();
        properties.put("system", Map.of(
                "type", "string",
                "description", "Name of the system (workspace) whose environment configurations to list."
        ));
        properties.put("country", Map.of(
                "type", "string",
                "description", "Optional exact country code to filter on (e.g. 'FR'). Must be a value already registered as a COUNTRY invariant."
        ));
        properties.put("environment", Map.of(
                "type", "string",
                "description", "Optional exact environment name to filter on (e.g. 'DEV', 'QA', 'PROD'). Must be a value already registered as an ENVIRONMENT invariant."
        ));
        properties.put("active", Map.of(
                "type", "boolean",
                "description", "Optional filter: when set, only returns entries whose active flag matches this value."
        ));
        properties.put("search", Map.of(
                "type", "string",
                "description", "Optional text filter applied on description, build, revision, or chain (case-insensitive)."
        ));

        return new McpSchema.Tool(
                TOOL_NAME,
                null,
                """
                Lists the environment configurations (CountryEnvParam) defined for a system.

                Each entry activates a country + environment combination for the system and carries
                environment-wide metadata (build, revision, deployment chain, maintenance window).

                Call this tool whenever the user asks to list, browse, or search the environments
                configured for a system, optionally narrowed down to a specific country or environment.

                Country and environment values must already exist as invariants (types COUNTRY and
                ENVIRONMENT). Use cerberus_invariant_list to check available values, or
                cerberus_invariant_create to register a new one, before creating a new entry here.

                Use cerberus_country_env_param_get to retrieve the full details of one specific entry.
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
                MCPToolUtils.readOnlyAnnotations("List country environment parameters", false),
                null
        );
    }

    /**
     * Loads all entries for the given system (optionally narrowed by country/environment),
     * applies optional client-side filters, and returns a JSON list.
     *
     * @param args raw MCP arguments map from the client request
     * @return a success JSON result containing the entry list, or an error text result
     */
    private McpSchema.CallToolResult execute(Map<String, Object> args) {
        String system = MCPToolUtils.getString(args, "system", "");
        String country = MCPToolUtils.getString(args, "country", "");
        String environment = MCPToolUtils.getString(args, "environment", "");
        String search = MCPToolUtils.getString(args, "search", "");
        Boolean active = args.get("active") instanceof Boolean b ? b : null;

        mcpLogUtils.call(TOOL_NAME, "country_env_param_list",
                String.format("MCP tool %s called with system=%s country=%s environment=%s", TOOL_NAME, system, country, environment));

        if (system.isBlank()) {
            return MCPToolUtils.errorText("Missing required parameter: system");
        }

        List<CountryEnvParam> raw = countryEnvParamService.readByVarious(system, country, environment, "", "", "").getDataList();

        List<Object> entries = raw.stream()
                .filter(cep -> active == null || cep.isActive() == active)
                .filter(cep -> matchesSearch(cep, search))
                .map(mapper::toDTO)
                .map(Object.class::cast)
                .toList();

        return MCPToolUtils.successJson(Map.of(
                "system", system,
                "count", entries.size(),
                "entries", entries
        ));
    }

    /**
     * Returns {@code true} when description, build, revision, or chain contains the search term
     * (case-insensitive), or when no search term was provided.
     *
     * @param cep    the entry to test
     * @param search the search string; blank means "match all"
     * @return whether this entry passes the search filter
     */
    private boolean matchesSearch(CountryEnvParam cep, String search) {
        if (search.isBlank()) return true;
        return MCPToolUtils.containsIgnoreCase(cep.getDescription(), search)
                || MCPToolUtils.containsIgnoreCase(cep.getBuild(), search)
                || MCPToolUtils.containsIgnoreCase(cep.getRevision(), search)
                || MCPToolUtils.containsIgnoreCase(cep.getChain(), search);
    }

}
