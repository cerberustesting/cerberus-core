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
package org.cerberus.core.mcp.impl.appservice.header;

import io.modelcontextprotocol.server.McpServerFeatures;
import io.modelcontextprotocol.spec.McpSchema;
import org.cerberus.core.mcp.MCPTool;
import org.cerberus.core.mcp.util.MCPLogUtils;
import org.cerberus.core.mcp.util.MCPToolUtils;
import org.cerberus.core.api.dto.appservice.AppServiceHeaderDTOV001;
import org.cerberus.core.api.dto.appservice.AppServiceHeaderMapperV001;
import org.cerberus.core.crud.entity.AppServiceHeader;
import org.cerberus.core.crud.service.IAppServiceHeaderService;
import org.cerberus.core.util.answer.Answer;
import org.cerberus.core.util.answer.AnswerItem;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * MCP tool that creates a new HTTP header for an existing {@link org.cerberus.core.crud.entity.AppService}.
 *
 * <p>Exposed MCP tool name: {@code cerberus_appservice_header_create}.</p>
 *
 * <p>Delegates duplicate-checking and persistence to {@link IAppServiceHeaderService}.
 * A duplicate check (via {@code readByKey}) is performed before creation because
 * {@link IAppServiceHeaderService#create} relies on a DB-level unique constraint;
 * surfacing a raw constraint violation to the LLM would produce a confusing error.</p>
 *
 * <p>Important: {@link AppServiceHeader} exposes a boolean field {@code isActive} whose
 * Lombok/manual setter is {@code setActive(boolean)} — NOT {@code setIsActive(boolean)}.
 * Always call {@code header.setActive(...)} to avoid a {@link NoSuchMethodError} at
 * runtime.</p>
 */
@Component
public class CreateAppServiceHeaderTool implements MCPTool {

    private static final String TOOL_NAME = "cerberus_appservice_header_create";

    private final IAppServiceHeaderService appServiceHeaderService;
    private final AppServiceHeaderMapperV001 mapper;
    private final MCPLogUtils mcpLogUtils;

    public CreateAppServiceHeaderTool(IAppServiceHeaderService appServiceHeaderService,
                                      AppServiceHeaderMapperV001 mapper,
                                      MCPLogUtils mcpLogUtils) {
        this.appServiceHeaderService = appServiceHeaderService;
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
     * Builds the MCP tool schema with an ordered properties map so that required
     * fields ({@code service}, {@code key}, {@code value}) appear before optional ones
     * in the LLM-visible JSON Schema.
     *
     * @return the fully described {@link McpSchema.Tool} ready for MCP registration.
     */
    private McpSchema.Tool createTool() {
        // LinkedHashMap preserves insertion order, keeping required fields first.
        Map<String, Object> properties = new LinkedHashMap<>();
        properties.put("service", Map.of(
                "type", "string",
                "description", "Name (primary key) of the AppService to which the header belongs."
        ));
        properties.put("key", Map.of(
                "type", "string",
                "description", "Header name (e.g. 'Content-Type', 'Authorization'). Must be unique within the service."
        ));
        properties.put("value", Map.of(
                "type", "string",
                "description", "Header value (e.g. 'application/json', 'Bearer <token>')."
        ));
        properties.put("sort", Map.of(
                "type", "integer",
                "description", "Display sort order. Defaults to 10 if not provided."
        ));
        properties.put("isActive", Map.of(
                "type", "boolean",
                "description", "Whether the header is active. Defaults to true if not provided."
        ));
        properties.put("description", Map.of(
                "type", "string",
                "description", "Optional human-readable description of the header."
        ));

        return new McpSchema.Tool(
                TOOL_NAME,
                null,
                """
                Creates a new HTTP header for an existing Cerberus AppService.

                Call this tool whenever the user asks to add a header to a service definition.
                The combination of (service, key) must be unique — if the header already exists
                this tool returns an error instead of overwriting it (use the update tool for that).

                Do not call this tool when the user only wants to list or read headers.
                """,
                new McpSchema.JsonSchema(
                        "object",
                        properties,
                        List.of("service", "key", "value"),
                        null,
                        null,
                        null
                ),
                null,
                MCPToolUtils.createAnnotations("Create app service header", false),
                null
        );
    }

    /**
     * Executes the header creation logic.
     *
     * <p>Steps:
     * <ol>
     *   <li>Extract and validate required arguments ({@code service}, {@code key}, {@code value}).</li>
     *   <li>Parse optional {@code sort} (Number-safe, default 10) and {@code isActive} (Boolean-safe, default true).</li>
     *   <li>Guard against duplicate entries via {@link IAppServiceHeaderService#readByKey}.</li>
     *   <li>Build and persist the {@link AppServiceHeader} entity.</li>
     *   <li>Return the created header as a DTO.</li>
     * </ol>
     * </p>
     *
     * @param args raw MCP argument map extracted from the tool call request.
     * @return a success JSON result containing the created header DTO,
     *         or an error result with a human-readable message.
     */
    private McpSchema.CallToolResult execute(Map<String, Object> args) {
        String service = MCPToolUtils.getString(args, "service", "");
        String key = MCPToolUtils.getString(args, "key", "");
        String value = MCPToolUtils.getString(args, "value", "");
        String description = MCPToolUtils.getString(args, "description", "");

        // Parse sort safely: the JSON layer may deliver an integer as a Number instance.
        Object sortObj = args.get("sort");
        int sort = (sortObj instanceof Number) ? ((Number) sortObj).intValue() : 10;

        // Parse isActive safely: the JSON layer delivers booleans as Boolean instances.
        Object activeObj = args.get("isActive");
        boolean isActive = (activeObj instanceof Boolean) ? (Boolean) activeObj : true;

        mcpLogUtils.call(TOOL_NAME, "appservice_header_create",
                String.format("MCP tool %s called with service=%s key=%s", TOOL_NAME, service, key));

        if (service.isBlank()) {
            return MCPToolUtils.errorText("Missing required parameter: service");
        }
        if (key.isBlank()) {
            return MCPToolUtils.errorText("Missing required parameter: key");
        }
        if (value.isBlank()) {
            return MCPToolUtils.errorText("Missing required parameter: value");
        }

        // Prevent creating a duplicate — readByKey returns OK + non-null item when the entry already exists.
        AnswerItem<AppServiceHeader> existing = appServiceHeaderService.readByKey(service, key);
        if (existing.isCodeStringEquals("OK") && existing.getItem() != null) {
            return MCPToolUtils.errorText(
                    "Header already exists for service='" + service + "' key='" + key
                    + "'. Use the update tool to modify an existing header.");
        }

        AppServiceHeader header = new AppServiceHeader();
        header.setService(service);
        header.setKey(key);
        header.setValue(value);
        header.setSort(sort);
        // NOTE: the setter is setActive(boolean), not setIsActive(boolean).
        header.setActive(isActive);
        header.setDescription(description);
        header.setUsrCreated("MCP");

        Answer answer = appServiceHeaderService.create(header);

        if (!answer.isCodeStringEquals("OK")) {
            return MCPToolUtils.errorText(
                    "Unable to create header service='" + service + "' key='" + key
                    + "': " + answer.getMessageDescription());
        }

        AppServiceHeaderDTOV001 dto = mapper.toDTO(header);
        return MCPToolUtils.successJson(Map.of(
                "status", "created",
                "header", dto
        ));
    }

}
