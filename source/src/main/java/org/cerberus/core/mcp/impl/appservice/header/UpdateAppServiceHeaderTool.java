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
import org.cerberus.core.api.dto.appservice.AppServiceHeaderMapperV001;
import org.cerberus.core.crud.entity.AppServiceHeader;
import org.cerberus.core.crud.service.IAppServiceHeaderService;
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
 * MCP tool that manages updates to existing {@link AppServiceHeader} entities in Cerberus.
 *
 * <p>Exposes the MCP tool name {@code cerberus_appservice_header_update}, which allows an AI
 * agent to mutate a subset of fields (value, sort, isActive, description) on a header
 * identified by its composite key {@code (service, key)}. Delegates persistence to
 * {@link IAppServiceHeaderService}.</p>
 *
 * <p>The read-before-write pattern is applied: the existing record is fetched first so that
 * unmodified fields retain their current values and a clear "does not exist" error is returned
 * instead of silently creating or corrupting data.</p>
 *
 * <p>Note on the boolean active field: {@link AppServiceHeader} uses a manual getter/setter
 * pair named {@code isActive()} / {@code setActive(boolean)} — not {@code setIsActive}.
 * This is why the switch branch for {@code "isActive"} calls {@code header.setActive(...)}
 * rather than {@code header.setIsActive(...)}.</p>
 *
 * <p>Only explicitly declared fields are accepted; any unrecognised field causes an immediate
 * error response, preventing unintended mutations.</p>
 */
@Component
public class UpdateAppServiceHeaderTool implements MCPTool {

    private static final String TOOL_NAME = "cerberus_appservice_header_update";

    private final IAppServiceHeaderService appServiceHeaderService;
    private final AppServiceHeaderMapperV001 mapper;
    private final MCPLogUtils mcpLogUtils;

    public UpdateAppServiceHeaderTool(IAppServiceHeaderService appServiceHeaderService,
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

    private McpSchema.Tool createTool() {
        Map<String, Object> updateProperties = new LinkedHashMap<>();

        updateProperties.put("value", Map.of(
                "type", "string",
                "description", "New value of the header."
        ));

        updateProperties.put("sort", Map.of(
                "type", "integer",
                "description", "New sort order of the header."
        ));

        updateProperties.put("isActive", Map.of(
                "type", "boolean",
                "description", "Whether the header is active."
        ));

        updateProperties.put("description", Map.of(
                "type", "string",
                "description", "New description of the header."
        ));

        Map<String, Object> updatesSchema = new LinkedHashMap<>();
        updatesSchema.put("type", "object");
        updatesSchema.put("description", "Fields to update on the header. Only supported fields are allowed.");
        updatesSchema.put("properties", updateProperties);
        updatesSchema.put("additionalProperties", false);

        Map<String, Object> properties = new LinkedHashMap<>();

        properties.put("service", Map.of(
                "type", "string",
                "description", "Name (primary key) of the service the header belongs to."
        ));

        properties.put("key", Map.of(
                "type", "string",
                "description", "Key (part of composite primary key) of the header to update."
        ));

        properties.put("updates", updatesSchema);

        return new McpSchema.Tool(
                TOOL_NAME,
                null,
                """
                Updates an existing app service header in Cerberus.

                Call this tool whenever the user asks to modify or change properties of an existing service header.

                Both service and key are required to identify the header.

                Only explicitly supported fields can be updated:
                - value
                - sort        (integer)
                - isActive    (boolean)
                - description

                Do not call this tool when the user only asks to display, list, read, or search a header.
                """,
                new McpSchema.JsonSchema(
                        "object",
                        properties,
                        List.of("service", "key", "updates"),
                        null,
                        null,
                        null
                ),
                null,
                MCPToolUtils.updateAnnotations("Update app service header", false),
                null
        );
    }

    @SuppressWarnings("unchecked")
    private McpSchema.CallToolResult execute(Map<String, Object> args) {
        mcpLogUtils.call(TOOL_NAME, "update", "MCP tool cerberus_appservice_header_update called with args: " + args);

        String service = MCPToolUtils.getString(args, "service", "");
        String key = MCPToolUtils.getString(args, "key", "");

        if (service.isBlank()) {
            return MCPToolUtils.errorText("Missing required parameter: service");
        }

        if (key.isBlank()) {
            return MCPToolUtils.errorText("Missing required parameter: key");
        }

        Object updatesObject = args.get("updates");

        if (!(updatesObject instanceof Map)) {
            return MCPToolUtils.errorText("Missing or invalid required parameter: updates");
        }

        Map<String, Object> updates = (Map<String, Object>) updatesObject;

        if (updates.isEmpty()) {
            return MCPToolUtils.errorText("No field provided to update.");
        }

        AnswerItem<AppServiceHeader> readAnswer = appServiceHeaderService.readByKey(service, key);

        if (!readAnswer.isCodeStringEquals("OK") || readAnswer.getItem() == null) {
            return MCPToolUtils.errorText("App service header does not exist: service=" + service + ", key=" + key);
        }

        AppServiceHeader header = readAnswer.getItem();

        try {
            for (Map.Entry<String, Object> entry : updates.entrySet()) {
                String field = entry.getKey();
                Object value = entry.getValue();

                switch (field) {
                    case "value":
                        header.setValue(asString(value, field));
                        break;
                    case "sort":
                        if (!(value instanceof Number)) {
                            throw new IllegalArgumentException("Invalid value for field 'sort'. Expected integer.");
                        }
                        header.setSort(((Number) value).intValue());
                        break;
                    case "isActive":
                        if (!(value instanceof Boolean)) {
                            throw new IllegalArgumentException("Invalid value for field 'isActive'. Expected boolean.");
                        }
                        header.setActive((Boolean) value);
                        break;
                    case "description":
                        header.setDescription(asString(value, field));
                        break;
                    default:
                        return MCPToolUtils.errorText("Unsupported field: " + field);
                }
            }
        } catch (IllegalArgumentException e) {
            return MCPToolUtils.errorText(e.getMessage());
        }

        header.setUsrModif("MCP");

        Answer updateAnswer = appServiceHeaderService.update(service, key, header);

        if (!updateAnswer.isCodeStringEquals("OK")) {
            return MCPToolUtils.errorText(
                    "Unable to update app service header (service=" + service + ", key=" + key + "): "
                            + updateAnswer.getMessageDescription()
            );
        }

        return MCPToolUtils.successJson(Map.of(
                "status", "updated",
                "header", mapper.toDTO(header)
        ));
    }

    private String asString(Object value, String field) {
        if (value == null) {
            return "";
        }

        if (!(value instanceof String)) {
            throw new IllegalArgumentException("Invalid value for field '" + field + "'. Expected string.");
        }

        return ((String) value).trim();
    }

}
