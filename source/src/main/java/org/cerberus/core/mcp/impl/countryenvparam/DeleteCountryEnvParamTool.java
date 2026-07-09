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
import org.cerberus.core.api.dto.application.CountryEnvParamDTOV001;
import org.cerberus.core.api.dto.application.CountryEnvParamMapperV001;
import org.cerberus.core.crud.entity.CountryEnvParam;
import org.cerberus.core.crud.service.ICountryEnvParamService;
import org.cerberus.core.mcp.MCPTool;
import org.cerberus.core.mcp.util.MCPLogUtils;
import org.cerberus.core.mcp.util.MCPToolUtils;
import org.cerberus.core.util.answer.Answer;
import org.cerberus.core.util.answer.AnswerItem;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

/**
 * MCP tool that deletes an existing {@link CountryEnvParam} entry from Cerberus.
 *
 * <p>Exposed MCP tool name: {@code cerberus_country_env_param_delete}.</p>
 *
 * <p>Deleting an environment configuration also removes the ability to run tests against it.
 * If per-application settings ({@code CountryEnvironmentParameters}) still reference this
 * system/country/environment combination, the underlying database foreign key constraint
 * may reject the deletion — the resulting error is surfaced as-is.</p>
 *
 * <p>Delegates to {@link ICountryEnvParamService} for both the existence check
 * ({@code readByKey}) and the deletion ({@code delete}). A DTO snapshot is captured before
 * deletion via {@link CountryEnvParamMapperV001} so the confirmation response can include the
 * deleted entry's data even though the entity is gone after the service call.</p>
 */
@Component
public class DeleteCountryEnvParamTool implements MCPTool {

    private static final String TOOL_NAME = "cerberus_country_env_param_delete";

    private final ICountryEnvParamService countryEnvParamService;
    private final CountryEnvParamMapperV001 mapper;
    private final MCPLogUtils mcpLogUtils;

    public DeleteCountryEnvParamTool(ICountryEnvParamService countryEnvParamService,
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
     * Builds the MCP {@link McpSchema.Tool} descriptor for {@code cerberus_country_env_param_delete}.
     *
     * <p>Declares {@code system}, {@code country}, and {@code environment} as required parameters.
     * Annotated with delete semantics so MCP clients can surface a confirmation prompt.</p>
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
                        "description", "Exact country code of the entry to delete."
                ),
                "environment", Map.of(
                        "type", "string",
                        "description", "Exact environment name of the entry to delete."
                )
        );

        return new McpSchema.Tool(
                TOOL_NAME,
                null,
                """
                Deletes an environment configuration (CountryEnvParam), deactivating a country +
                environment combination for a system.

                Call this tool whenever the user asks to remove or deactivate an environment for a system.
                Before calling this tool, confirm the system/country/environment with the user.

                Use cerberus_country_env_param_list to find the exact combination before deleting.

                Do not call this tool when the user only asks to list, read, create, or update entries.
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
                MCPToolUtils.deleteAnnotations("Delete country environment parameter", false),
                null
        );
    }

    /**
     * Validates the arguments, checks the entry exists, deletes it, and returns a JSON
     * confirmation payload.
     *
     * @param args raw MCP arguments map from the client request
     * @return a success JSON result confirming the deletion, or an error text result
     */
    private McpSchema.CallToolResult execute(Map<String, Object> args) {
        String system = MCPToolUtils.getString(args, "system", "");
        String country = MCPToolUtils.getString(args, "country", "");
        String environment = MCPToolUtils.getString(args, "environment", "");

        mcpLogUtils.call(TOOL_NAME, "country_env_param_delete",
                String.format("MCP tool %s called with system=%s country=%s environment=%s", TOOL_NAME, system, country, environment));

        if (system.isBlank()) return MCPToolUtils.errorText("Missing required parameter: system");
        if (country.isBlank()) return MCPToolUtils.errorText("Missing required parameter: country");
        if (environment.isBlank()) return MCPToolUtils.errorText("Missing required parameter: environment");

        AnswerItem<CountryEnvParam> readAnswer = countryEnvParamService.readByKey(system, country, environment);

        if (!readAnswer.isCodeStringEquals("OK") || readAnswer.getItem() == null) {
            return MCPToolUtils.errorText("Country environment parameter does not exist: system=" + system + " country=" + country + " environment=" + environment);
        }

        CountryEnvParam cep = readAnswer.getItem();
        // Snapshot the DTO before deletion — the entity is gone after the service call.
        CountryEnvParamDTOV001 dto = mapper.toDTO(cep);

        Answer deleteAnswer = countryEnvParamService.delete(cep);

        if (!deleteAnswer.isCodeStringEquals("OK")) {
            return MCPToolUtils.errorText("Unable to delete country environment parameter: " + deleteAnswer.getMessageDescription());
        }

        return MCPToolUtils.successJson(Map.of(
                "status", "deleted",
                "entry", dto
        ));
    }

}