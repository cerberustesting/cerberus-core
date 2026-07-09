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
import org.cerberus.core.util.answer.AnswerItem;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

/**
 * MCP tool that retrieves a single {@link CountryEnvParam} entry by its composite key
 * (system + country + environment).
 *
 * <p>Exposed MCP tool name: {@code cerberus_country_env_param_get}.</p>
 *
 * <p>Delegates to {@link ICountryEnvParamService#readByKey(String, String, String)} and converts
 * the result via {@link CountryEnvParamMapperV001}.</p>
 */
@Component
public class GetCountryEnvParamTool implements MCPTool {

    private static final String TOOL_NAME = "cerberus_country_env_param_get";

    private final ICountryEnvParamService countryEnvParamService;
    private final CountryEnvParamMapperV001 mapper;
    private final MCPLogUtils mcpLogUtils;

    public GetCountryEnvParamTool(ICountryEnvParamService countryEnvParamService,
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
     * Builds the MCP {@link McpSchema.Tool} descriptor for {@code cerberus_country_env_param_get}.
     *
     * <p>Declares {@code system}, {@code country}, and {@code environment} as required parameters —
     * together they form the composite business key for an entry.</p>
     *
     * @return the fully configured tool descriptor
     */
    private McpSchema.Tool createTool() {
        Map<String, Object> properties = Map.of(
                "system", Map.of(
                        "type", "string",
                        "description", "Name of the system (workspace) the entry belongs to."
                ),
                "country", Map.of(
                        "type", "string",
                        "description", "Exact country code of the entry to retrieve (e.g. 'FR')."
                ),
                "environment", Map.of(
                        "type", "string",
                        "description", "Exact environment name of the entry to retrieve (e.g. 'DEV', 'QA', 'PROD')."
                )
        );

        return new McpSchema.Tool(
                TOOL_NAME,
                null,
                """
                Retrieves the full details of a specific environment configuration (CountryEnvParam)
                by system, country, and environment.

                Call this tool whenever the user asks to inspect or display a specific environment
                configuration for a system.

                Use cerberus_country_env_param_list instead when the exact country/environment is unknown
                or to browse all entries for a system.
                Do not call this tool when the user asks to create, update, or delete an entry.
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
                MCPToolUtils.readOnlyAnnotations("Get country environment parameter", true),
                null
        );
    }

    /**
     * Validates the arguments, calls the service, and returns the entry as a JSON object.
     *
     * @param args raw MCP arguments map from the client request
     * @return a success JSON result containing the entry, or an error text result
     */
    private McpSchema.CallToolResult execute(Map<String, Object> args) {
        String system = MCPToolUtils.getString(args, "system", "");
        String country = MCPToolUtils.getString(args, "country", "");
        String environment = MCPToolUtils.getString(args, "environment", "");

        mcpLogUtils.call(TOOL_NAME, "country_env_param_get",
                String.format("MCP tool %s called with system=%s country=%s environment=%s", TOOL_NAME, system, country, environment));

        if (system.isBlank()) return MCPToolUtils.errorText("Missing required parameter: system");
        if (country.isBlank()) return MCPToolUtils.errorText("Missing required parameter: country");
        if (environment.isBlank()) return MCPToolUtils.errorText("Missing required parameter: environment");

        AnswerItem<CountryEnvParam> answer = countryEnvParamService.readByKey(system, country, environment);

        if (!answer.isCodeStringEquals("OK") || answer.getItem() == null) {
            return MCPToolUtils.errorText("Country environment parameter does not exist: system=" + system + " country=" + country + " environment=" + environment);
        }

        return MCPToolUtils.successJson(mapper.toDTO(answer.getItem()));
    }

}