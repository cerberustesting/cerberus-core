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
package org.cerberus.core.mcp.impl.invariant;

import io.modelcontextprotocol.server.McpServerFeatures;
import io.modelcontextprotocol.spec.McpSchema;
import org.cerberus.core.api.dto.invariant.InvariantMapperV001;
import org.cerberus.core.mcp.MCPTool;
import org.cerberus.core.mcp.util.MCPLogUtils;
import org.cerberus.core.mcp.util.MCPToolUtils;
import org.cerberus.core.crud.entity.Invariant;
import org.cerberus.core.crud.service.IInvariantService;
import org.cerberus.core.util.answer.Answer;
import org.cerberus.core.util.answer.AnswerItem;
import org.cerberus.core.websocket.WebSocketEventSender;
import org.cerberus.core.websocket.WebSocketStatic;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

/**
 * MCP tool that creates a new {@link Invariant} entry in Cerberus.
 *
 * <p>Exposed MCP tool name: {@code cerberus_invariant_create}.</p>
 *
 * <p>Delegates creation and duplicate-check to {@link IInvariantService}.
 * Only a curated subset of invariant types (COUNTRY, ENVIRONMENT, SYSTEM, …)
 * is exposed through this tool; internal or read-only invariant types are
 * intentionally excluded from {@link #SUPPORTED_TYPES} to prevent accidental
 * corruption of Cerberus configuration data.</p>
 */
@Component
public class CreateInvariantTool implements MCPTool {

    private static final String TOOL_NAME = "cerberus_invariant_create";

    /**
     * Invariant types that are safe to create via MCP.
     *
     * <p>Not all Cerberus invariant types are mutable through the public API —
     * some are managed internally. This allowlist restricts the tool to types
     * that represent user-configurable metadata.</p>
     */
    private static final List<String> SUPPORTED_TYPES = List.of(
            "COUNTRY",
            "ENVIRONMENT",
            "SYSTEM",
            "BROWSER",
            "PRIORITY",
            "ROBOT",
            "CAPABILITY",
            "TCSTATUS"
    );

    private final IInvariantService invariantService;
    private final InvariantMapperV001 invariantMapper;
    private final MCPLogUtils mcpLogUtils;

    @Autowired
    private WebSocketEventSender webSocketEventSender;

    public CreateInvariantTool(IInvariantService invariantService, InvariantMapperV001 invariantMapper, MCPLogUtils mcpLogUtils) {
        this.invariantService = invariantService;
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
     * Builds the MCP tool schema, including the input JSON Schema with
     * descriptions and the enum constraint for the {@code type} parameter.
     *
     * @return the fully described {@link McpSchema.Tool} ready for MCP registration.
     */
    private McpSchema.Tool createTool() {
        Map<String, Object> properties = Map.of(
                "type", Map.of(
                        "type", "string",
                        "description", """
                                Type of invariant to create. Supported values:
                                - COUNTRY: add a new country available for test execution.
                                - ENVIRONMENT: add a new environment (e.g. DEV, QA, PROD).
                                - SYSTEM: add a new system (also called workspace) grouping applications.
                                - BROWSER: add a new supported browser.
                                - PRIORITY: add a new test case priority level.
                                - ROBOT: add a new robot (execution agent) type.
                                - CAPABILITY: add a new robot capability.
                                - TCSTATUS: add a new test case execution status.
                                """,
                        // Enum drives LLM choices; only SUPPORTED_TYPES values are accepted.
                        "enum", SUPPORTED_TYPES
                ),
                "value", Map.of(
                        "type", "string",
                        "description", "Value (code) of the new invariant entry (e.g. 'FR' for a country)."
                ),
                "description", Map.of(
                        "type", "string",
                        "description", "Optional human-readable description of the invariant value."
                )
        );

        return new McpSchema.Tool(
                TOOL_NAME,
                null,
                """
                Creates a new invariant value in Cerberus.

                Call this tool whenever the user asks to create or add a new country, environment,
                system, browser, priority, robot, capability, or test case status.

                Do not call this tool when the user only asks to list or read invariant values.
                """,
                new McpSchema.JsonSchema(
                        "object",
                        properties,
                        List.of("type", "value"),
                        null,
                        null,
                        null
                ),
                null,
                MCPToolUtils.createAnnotations("Create invariant", false),
                null
        );
    }

    /**
     * Executes the invariant creation logic.
     *
     * <p>Guards against duplicate entries by calling {@link IInvariantService#readByKey}
     * before attempting creation. A duplicate check is necessary here because
     * {@link IInvariantService#create} does not enforce uniqueness at the service
     * layer — enforcement is at the DB level, and surfacing a DB constraint error
     * to the LLM would produce a confusing message.</p>
     *
     * @param args raw MCP argument map extracted from the tool call request.
     * @return a success JSON result containing the created invariant's type and value,
     *         or an error result with a human-readable message.
     */
    private McpSchema.CallToolResult execute(Map<String, Object> args) {
        String type = MCPToolUtils.getString(args, "type", "");
        String value = MCPToolUtils.getString(args, "value", "");
        String description = MCPToolUtils.getString(args, "description", "");
        String appSessionID = MCPToolUtils.getString(args, "appSessionID", "");
        String user = MCPToolUtils.getString(args, "user", "MCPTool");

        //Send tool start through Websocket if request provide from GUI
        if("".equals(appSessionID)){
            webSocketEventSender.sendToAppSession(appSessionID, WebSocketStatic.TYPE_TOOL_START, WebSocketStatic.CHANNEL_AI_CHAT,
                    Map.of("toolName", TOOL_NAME ));
        }
        mcpLogUtils.call(TOOL_NAME, "invariant_create", String.format("MCP tool %s called with type=%s value=%s", TOOL_NAME, type, value));

        if (type.isBlank()) {
            return MCPToolUtils.errorText("Missing required parameter: type");
        }

        if (value.isBlank()) {
            return MCPToolUtils.errorText("Missing required parameter: value");
        }

        // Reject types not in the allowlist before hitting the service layer.
        if (!SUPPORTED_TYPES.contains(type)) {
            return MCPToolUtils.errorText("Unsupported invariant type: " + type + ". Supported types: " + SUPPORTED_TYPES);
        }

        // Prevent creating a duplicate — readByKey returns OK + non-null item when the entry already exists.
        AnswerItem<Invariant> existing = invariantService.readByKey(type, value);
        if (existing.isCodeStringEquals("OK") && existing.getItem() != null) {
            return MCPToolUtils.errorText("Invariant already exists: type=" + type + " value=" + value);
        }

        Invariant invariant = new Invariant();
        invariant.setIdName(type);
        invariant.setValue(value);
        invariant.setDescription(description);
        invariant.setSort(10);

        Answer answer = invariantService.create(invariant);

        if (!answer.isCodeStringEquals("OK")) {
            return MCPToolUtils.errorText("Unable to create invariant type=" + type + " value=" + value + ": " + answer.getMessageDescription());
        } else {
            Invariant invariantCreated = invariantService.readByKey(type, value).getItem();
            //Send tool end through Websocket if request provide from GUI
            if ("".equals(appSessionID)) {
                webSocketEventSender.sendToAppSession(appSessionID, WebSocketStatic.TYPE_OBJECTCREATION_INVARIANT, WebSocketStatic.CHANNEL_AI_CHAT,
                        Map.of("toolName", TOOL_NAME, "invariant", invariantMapper.toDTO(invariantCreated)));
                webSocketEventSender.sendToChannel(WebSocketStatic.CHANNEL_NOTIFICATION, WebSocketStatic.TYPE_OBJECTCREATION_INVARIANT,
                        Map.of("toolName", TOOL_NAME, "invariant", invariantMapper.toDTO(invariantCreated)));
            }
        }



        return MCPToolUtils.successJson(Map.of(
                "status", "created",
                "type", type,
                "value", value
        ));

    }

}