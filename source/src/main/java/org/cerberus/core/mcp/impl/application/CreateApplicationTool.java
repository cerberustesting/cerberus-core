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
import org.cerberus.core.api.dto.invariant.InvariantMapperV001;
import org.cerberus.core.mcp.MCPTool;
import org.cerberus.core.mcp.util.MCPLogUtils;
import org.cerberus.core.mcp.util.MCPToolUtils;
import org.cerberus.core.crud.entity.Application;
import org.cerberus.core.crud.entity.Invariant;
import org.cerberus.core.crud.service.IApplicationService;
import org.cerberus.core.crud.service.IInvariantService;
import org.cerberus.core.util.answer.Answer;
import org.cerberus.core.websocket.WebSocketEventSender;
import org.cerberus.core.websocket.WebSocketStatic;
import org.springframework.beans.factory.annotation.Autowired;
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
    private final IInvariantService invariantService;
    private final ApplicationMapperV001 applicationMapper;
    private final InvariantMapperV001 invariantMapper;
    private final MCPLogUtils mcpLogUtils;

    @Autowired
    private WebSocketEventSender webSocketEventSender;

    public CreateApplicationTool(IApplicationService applicationService, IInvariantService invariantService, ApplicationMapperV001 applicationMapper, InvariantMapperV001 invariantMapper, MCPLogUtils mcpLogUtils) {
        this.applicationService = applicationService;
        this.invariantService = invariantService;
        this.applicationMapper = applicationMapper;
        this.invariantMapper = invariantMapper;
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
     * <p>Declares {@code application}, {@code type} and {@code system} as required parameters.
     * If the given {@code system} does not exist as a SYSTEM invariant, it is created automatically.</p>
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
                        "description", "Type of the application.",
                        "enum", List.of(
                                Application.TYPE_GUI, Application.TYPE_BAT, Application.TYPE_SRV,
                                Application.TYPE_APK, Application.TYPE_IPA, Application.TYPE_FAT,
                                Application.TYPE_NONE
                        )
                ),
                "system", Map.of(
                        "type", "string",
                        "description", "System (workspace) linked to the application. If it does not exist as a SYSTEM invariant, the tool will ask for confirmation before creating it."
                ),
                "confirmSystemCreation", Map.of(
                        "type", "boolean",
                        "description", "Set to true to confirm the automatic creation of the SYSTEM invariant when it does not exist yet. Only needed when the system is new."
                )
        );

        return new McpSchema.Tool(
                TOOL_NAME,
                null,
                """
                Creates a new application in Cerberus.

                Call this tool whenever the user asks to create or add a new application.
                Requires an application name, a type, and a system.
                If the given system does not exist as a SYSTEM invariant, the tool returns an error.
                Ask the user to confirm, then re-call with confirmSystemCreation=true.
                """,
                new McpSchema.JsonSchema(
                        "object",
                        properties,
                        List.of("application", "type", "system"),
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
     * Validates the arguments, ensures the SYSTEM invariant exists (creating it if needed),
     * checks for a pre-existing application, builds the entity, and delegates creation
     * to {@link IApplicationService#create(Application)}.
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
        String appSessionID = MCPToolUtils.getString(args, "appSessionID", "");
        String user = MCPToolUtils.getString(args, "user", "MCPTool");
        boolean confirmSystemCreation = MCPToolUtils.getBoolean(args, "confirmSystemCreation", false);

        //Send tool start through Websocket if request provide from GUI
        if("".equals(appSessionID)){
            webSocketEventSender.sendToAppSession(appSessionID, WebSocketStatic.TYPE_TOOL_START, WebSocketStatic.CHANNEL_AI_CHAT,
                    Map.of("toolName", TOOL_NAME ));
        }

        mcpLogUtils.call(TOOL_NAME, "application_create", String.format("MCP tool %s called with application=%s type=%s system=%s", TOOL_NAME, applicationName, type, system));

        if (applicationName.isBlank()) {
            return MCPToolUtils.errorText("Missing required parameter: application");
        }
        if (type.isBlank()) {
            return MCPToolUtils.errorText("Missing required parameter: type");
        }
        if (system.isBlank()) {
            return MCPToolUtils.errorText("Missing required parameter: system");
        }
        if (!system.matches("[a-zA-Z0-9_\\-]+")) {
            return MCPToolUtils.errorText("Invalid system name '" + system + "': only letters, digits, hyphens and underscores are allowed.");
        }

        // If the SYSTEM invariant does not exist, require explicit user confirmation before creating it.
        if (!invariantService.isInvariantExist(Invariant.IDNAME_SYSTEM, system)) {
            if (!confirmSystemCreation) {
                return MCPToolUtils.errorText(
                        "System '" + system + "' does not exist as a SYSTEM invariant. " +
                        "Ask the user to confirm its creation, then re-call with confirmSystemCreation=true."
                );
            }
            Invariant systemInvariant = new Invariant();
            systemInvariant.setIdName(Invariant.IDNAME_SYSTEM);
            systemInvariant.setValue(system);
            systemInvariant.setDescription(system);
            systemInvariant.setSort(10);
            Answer invariantAnswer = invariantService.create(systemInvariant);
            if (!invariantAnswer.isCodeStringEquals("OK")) {
                return MCPToolUtils.errorText("Unable to create SYSTEM invariant '" + system + "': " + invariantAnswer.getMessageDescription());
            } else {
                Invariant invariantCreated = invariantService.readByKey(Invariant.IDNAME_SYSTEM, system).getItem();
                //Send tool end through Websocket if request provide from GUI
                if ("".equals(appSessionID)) {
                    webSocketEventSender.sendToAppSession(appSessionID, WebSocketStatic.TYPE_OBJECTCREATION_APPLICATION, WebSocketStatic.CHANNEL_AI_CHAT,
                            Map.of("toolName", TOOL_NAME, "invariant", invariantMapper.toDTO(invariantCreated)));
                    webSocketEventSender.sendToChannel(WebSocketStatic.CHANNEL_NOTIFICATION, WebSocketStatic.TYPE_OBJECTCREATION_APPLICATION,
                            Map.of("toolName", TOOL_NAME, "invariant", invariantMapper.toDTO(invariantCreated)));
                }
            }
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
        application.setUsrCreated(user);

        Answer answer = applicationService.create(application);

        if (!answer.isCodeStringEquals("OK")) {
            return MCPToolUtils.errorText("Unable to create Application " + applicationName + ": " + answer.getMessageDescription());
        }

        //Send tool end through Websocket if request provide from GUI
        if("".equals(appSessionID)){
            webSocketEventSender.sendToAppSession(appSessionID, WebSocketStatic.TYPE_OBJECTCREATION_APPLICATION, WebSocketStatic.CHANNEL_AI_CHAT,
                    Map.of("toolName", TOOL_NAME, "application", applicationMapper.toDTO(application) ));
            webSocketEventSender.sendToChannel(WebSocketStatic.CHANNEL_NOTIFICATION, WebSocketStatic.TYPE_OBJECTCREATION_APPLICATION,
                    Map.of("toolName", TOOL_NAME, "application", applicationMapper.toDTO(application) ));
        }


        //Send tool end through Websocket if request provide from GUI
        if("".equals(appSessionID)){
            webSocketEventSender.sendToAppSession(appSessionID, WebSocketStatic.TYPE_TOOL_END, WebSocketStatic.CHANNEL_AI_CHAT,
                    Map.of("toolName", TOOL_NAME ));
        }

        return MCPToolUtils.successJson(Map.of(
                "status", "created",
                "application", applicationMapper.toDTO(application)
        ));
    }

}