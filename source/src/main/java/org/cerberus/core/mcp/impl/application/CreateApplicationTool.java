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
package org.cerberus.core.mcp.impl.application;

import io.modelcontextprotocol.server.McpServerFeatures;
import io.modelcontextprotocol.spec.McpSchema;
import org.cerberus.core.api.dto.application.ApplicationMapperV001;
import org.cerberus.core.mcp.MCPTool;
import org.cerberus.core.mcp.util.MCPLogUtils;
import org.cerberus.core.mcp.util.MCPToolUtils;
import org.cerberus.core.crud.entity.Application;
import org.cerberus.core.crud.service.IApplicationService;
import org.cerberus.core.util.answer.Answer;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

/**
 * MCP tool that creates a new {@link Application} entity in Cerberus.
 *
 * <p>Exposes the MCP tool named {@code cerberus_application_create}. Before
 * persisting, it verifies that no application with the same name already exists
 * to prevent duplicate key errors at the database level.</p>
 *
 * <p>Delegates all persistence operations to {@link IApplicationService}.</p>
 */
@Component
public class CreateApplicationTool implements MCPTool {

    private static final String TOOL_NAME = "cerberus_application_create";

    private final IApplicationService applicationService;
    private final ApplicationMapperV001 mapper;
    private final MCPLogUtils mcpLogUtils;

    public CreateApplicationTool(IApplicationService applicationService, ApplicationMapperV001 mapper, MCPLogUtils mcpLogUtils) {
        this.applicationService = applicationService;
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
     * Builds the MCP {@link McpSchema.Tool} descriptor that is advertised to MCP clients.
     *
     * <p>Declares {@code application} as the only required parameter; all other
     * fields ({@code description}, {@code type}, {@code system}) are optional and
     * default to an empty string when omitted.</p>
     *
     * @return the fully configured tool descriptor
     */
    private McpSchema.Tool createTool() {
        Map<String, Object> properties = Map.of(
                "application", Map.of(
                        "type", "string",
                        "description", "Name of the new application."
                ),
                "description", Map.of(
                        "type", "string",
                        "description", "Optional description of the application."
                ),
                "type", Map.of(
                        "type", "string",
                        "description", "Optional type of the application.",
                        "enum", List.of(
                                Application.TYPE_GUI, Application.TYPE_BAT, Application.TYPE_SRV,
                                Application.TYPE_APK, Application.TYPE_IPA, Application.TYPE_FAT,
                                Application.TYPE_NONE
                        )
                ),
                "system", Map.of(
                        "type", "string",
                        "description", "Optional system linked to the application."
                )
        );

        return new McpSchema.Tool(
                TOOL_NAME,
                null,
                """
                Creates a new application in Cerberus.

                Call this tool whenever the user asks to create or add a new application.
                Requires an application name.
                """,
                new McpSchema.JsonSchema(
                        "object",
                        properties,
                        List.of("application"),
                        null,
                        null,
                        null
                ),
                null,
                MCPToolUtils.createAnnotations("Create application", false),
                null
        );
    }

    /**
     * Validates the arguments, checks for a pre-existing application, builds the
     * entity, and delegates creation to {@link IApplicationService#create(Application)}.
     *
     * <p>The existence check uses {@link IApplicationService#exist(String)} rather
     * than relying on a database unique-constraint violation so that the error
     * returned to the MCP client is meaningful rather than a raw SQL exception.</p>
     *
     * @param args the raw MCP argument map from the client request
     * @return a success result containing {@code status} and {@code application},
     *         or an error result with a human-readable message
     */
    private McpSchema.CallToolResult execute(Map<String, Object> args) {
        String applicationName = MCPToolUtils.getString(args, "application", "");
        String description = MCPToolUtils.getString(args, "description", "");
        String type = MCPToolUtils.getString(args, "type", "");
        String system = MCPToolUtils.getString(args, "system", "");

        mcpLogUtils.call(TOOL_NAME, "application_create", String.format("MCP tool %s called with application=%s", TOOL_NAME, applicationName));

        if (applicationName.isBlank()) {
            return MCPToolUtils.errorText("Missing required parameter: application");
        }

        // Guard against duplicate names before hitting the DB unique constraint.
        if (applicationService.exist(applicationName)) {
            return MCPToolUtils.errorText("Application already exists: " + applicationName);
        }

        Application application = new Application();
        application.setApplication(applicationName);
        application.setDescription(description);
        application.setType(type);
        application.setSystem(system);
        application.setSubsystem("");
        // Tag the creator as "MCP" so audit trails distinguish tool-driven creation from UI creation.
        application.setUsrCreated("MCP");

        Answer answer = applicationService.create(application);

        if (!answer.isCodeStringEquals("OK")) {
            return MCPToolUtils.errorText("Unable to create Application " + applicationName + ": " + answer.getMessageDescription());
        }

        return MCPToolUtils.successJson(Map.of(
                "status", "created",
                "application", mapper.toDTO(application)
        ));
    }

}