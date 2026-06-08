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
package org.cerberus.core.mcp.impl.application.object;

import io.modelcontextprotocol.server.McpServerFeatures;
import io.modelcontextprotocol.spec.McpSchema;
import org.cerberus.core.api.dto.application.ApplicationObjectMapperV001;
import org.cerberus.core.crud.entity.ApplicationObject;
import org.cerberus.core.crud.service.IApplicationObjectService;
import org.cerberus.core.mcp.MCPTool;
import org.cerberus.core.mcp.util.MCPLogUtils;
import org.cerberus.core.mcp.util.MCPToolUtils;
import org.cerberus.core.util.answer.Answer;
import org.cerberus.core.util.answer.AnswerItem;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

/**
 * MCP tool that creates a new {@link ApplicationObject} entity in Cerberus.
 *
 * <p>Exposed MCP tool name: {@code cerberus_application_object_create}.</p>
 *
 * <p>Before persisting, verifies that no object with the same (application, object) key
 * already exists to prevent duplicate key errors at the database level.</p>
 *
 * <p>Delegates all persistence operations to {@link IApplicationObjectService}.</p>
 */
@Component
public class CreateApplicationObjectTool implements MCPTool {

    private static final String TOOL_NAME = "cerberus_application_object_create";

    private final IApplicationObjectService applicationObjectService;
    private final ApplicationObjectMapperV001 mapper;
    private final MCPLogUtils mcpLogUtils;

    public CreateApplicationObjectTool(IApplicationObjectService applicationObjectService,
                                       ApplicationObjectMapperV001 mapper,
                                       MCPLogUtils mcpLogUtils) {
        this.applicationObjectService = applicationObjectService;
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
     * Builds the MCP {@link McpSchema.Tool} descriptor for {@code cerberus_application_object_create}.
     *
     * <p>Declares {@code application}, {@code object}, and {@code value} as required parameters.
     * {@code xOffset} and {@code yOffset} are optional coordinate offsets for element interaction.</p>
     *
     * @return the fully configured tool descriptor
     */
    private McpSchema.Tool createTool() {
        Map<String, Object> properties = Map.of(
                "application", Map.of(
                        "type", "string",
                        "description", "Name of the application this object belongs to."
                ),
                "object", Map.of(
                        "type", "string",
                        "description", "Unique name for the new application object."
                ),
                "value", Map.of(
                        "type", "string",
                        "description", "Locator value for this object (e.g. XPath expression, CSS selector, element identifier)."
                ),
                "xOffset", Map.of(
                        "type", "string",
                        "description", "Optional horizontal offset for element coordinates."
                ),
                "yOffset", Map.of(
                        "type", "string",
                        "description", "Optional vertical offset for element coordinates."
                )
        );

        return new McpSchema.Tool(
                TOOL_NAME,
                null,
                """
                Creates a new application object (locator) for an application.

                Application objects are reusable element descriptors (XPath, CSS selectors, etc.)
                that can be referenced by name in testcase steps and actions.

                Call this tool whenever the user asks to create or add a new application object.
                Requires application name, object name, and a locator value.

                Do not call this tool when the user only asks to list, read, update, or delete objects.
                """,
                new McpSchema.JsonSchema(
                        "object",
                        properties,
                        List.of("application", "object", "value"),
                        null,
                        null,
                        null
                ),
                null,
                MCPToolUtils.createAnnotations("Create application object", false),
                null
        );
    }

    /**
     * Validates the arguments, checks for duplicates, builds the entity, and delegates
     * creation to {@link IApplicationObjectService#create(ApplicationObject)}.
     *
     * <p>Guard against duplicate (application, object) composite key before hitting the
     * DB unique constraint, so the MCP error is meaningful rather than a raw SQL exception.</p>
     *
     * @param args raw MCP arguments map from the client request
     * @return a success JSON result containing the created object DTO, or an error text result
     */
    private McpSchema.CallToolResult execute(Map<String, Object> args) {
        String application = MCPToolUtils.getString(args, "application", "");
        String object = MCPToolUtils.getString(args, "object", "");
        String value = MCPToolUtils.getString(args, "value", "");
        String xOffset = MCPToolUtils.getString(args, "xOffset", "");
        String yOffset = MCPToolUtils.getString(args, "yOffset", "");

        mcpLogUtils.call(TOOL_NAME, "application_object_create",
                String.format("MCP tool %s called with application=%s object=%s", TOOL_NAME, application, object));

        if (application.isBlank()) return MCPToolUtils.errorText("Missing required parameter: application");
        if (object.isBlank()) return MCPToolUtils.errorText("Missing required parameter: object");
        if (value.isBlank()) return MCPToolUtils.errorText("Missing required parameter: value");

        // Guard against duplicate (application, object) key before hitting the DB unique constraint.
        AnswerItem<ApplicationObject> existing = applicationObjectService.readByKey(application, object);
        if (existing.isCodeStringEquals("OK") && existing.getItem() != null) {
            return MCPToolUtils.errorText("Application object already exists: application=" + application + " object=" + object);
        }

        ApplicationObject appObject = new ApplicationObject();
        appObject.setApplication(application);
        appObject.setObject(object);
        appObject.setValue(value);
        appObject.setXOffset(xOffset);
        appObject.setYOffset(yOffset);
        // Tag the creator so audit trails distinguish MCP-driven creation from UI creation.
        appObject.setUsrCreated("MCP");

        Answer answer = applicationObjectService.create(appObject);

        if (!answer.isCodeStringEquals("OK")) {
            return MCPToolUtils.errorText("Unable to create application object: " + answer.getMessageDescription());
        }

        return MCPToolUtils.successJson(Map.of(
                "status", "created",
                "object", mapper.toDTO(appObject)
        ));
    }
}
