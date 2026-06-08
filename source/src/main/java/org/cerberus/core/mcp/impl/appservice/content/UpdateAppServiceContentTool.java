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
package org.cerberus.core.mcp.impl.appservice.content;

import io.modelcontextprotocol.server.McpServerFeatures;
import io.modelcontextprotocol.spec.McpSchema;
import org.cerberus.core.api.dto.appservice.AppServiceContentMapperV001;
import org.cerberus.core.crud.entity.AppServiceContent;
import org.cerberus.core.crud.service.IAppServiceContentService;
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
 * MCP tool that manages updates to existing {@link AppServiceContent} entities in Cerberus.
 *
 * <p>Exposes the MCP tool name {@code cerberus_appservice_content_update}, which allows an AI
 * agent to mutate a subset of fields (value, sort, isActive, description) on a content parameter
 * identified by its composite key {@code (service, key)}. Delegates persistence to
 * {@link IAppServiceContentService}.</p>
 *
 * <p>The read-before-write pattern is applied: the existing record is fetched first so that
 * unmodified fields retain their current values and a clear "does not exist" error is returned
 * instead of silently creating or corrupting data.</p>
 *
 * <p>Note on the boolean active field: {@link AppServiceContent} uses Lombok {@code @Data}, which
 * generates the getter/setter pair as {@code isActive()} / {@code setActive(boolean)} — stripping
 * the {@code is-} prefix from the setter name. This is why the switch branch for {@code "isActive"}
 * calls {@code content.setActive(...)} rather than {@code content.setIsActive(...)}.</p>
 *
 * <p>Only explicitly declared fields are accepted; any unrecognised field causes an immediate
 * error response, preventing unintended mutations.</p>
 */
@Component
public class UpdateAppServiceContentTool implements MCPTool {

    private static final String TOOL_NAME = "cerberus_appservice_content_update";

    private final IAppServiceContentService appServiceContentService;
    private final AppServiceContentMapperV001 mapper;
    private final MCPLogUtils mcpLogUtils;

    public UpdateAppServiceContentTool(IAppServiceContentService appServiceContentService,
                                       AppServiceContentMapperV001 mapper,
                                       MCPLogUtils mcpLogUtils) {
        this.appServiceContentService = appServiceContentService;
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
                "description", "New value of the content parameter."
        ));

        updateProperties.put("sort", Map.of(
                "type", "integer",
                "description", "New sort order of the content parameter."
        ));

        updateProperties.put("isActive", Map.of(
                "type", "boolean",
                "description", "Whether the content parameter is active."
        ));

        updateProperties.put("description", Map.of(
                "type", "string",
                "description", "New description of the content parameter."
        ));

        Map<String, Object> updatesSchema = new LinkedHashMap<>();
        updatesSchema.put("type", "object");
        updatesSchema.put("description", "Fields to update on the content parameter. Only supported fields are allowed.");
        updatesSchema.put("properties", updateProperties);
        updatesSchema.put("additionalProperties", false);

        Map<String, Object> properties = new LinkedHashMap<>();

        properties.put("service", Map.of(
                "type", "string",
                "description", "Name (primary key) of the service the content parameter belongs to."
        ));

        properties.put("key", Map.of(
                "type", "string",
                "description", "Key (part of composite primary key) of the content parameter to update."
        ));

        properties.put("updates", updatesSchema);

        return new McpSchema.Tool(
                TOOL_NAME,
                null,
                """
                Updates an existing app service content parameter in Cerberus.

                Call this tool whenever the user asks to modify or change properties of an existing service content parameter.

                Both service and key are required to identify the content parameter.

                Only explicitly supported fields can be updated:
                - value
                - sort        (integer)
                - isActive    (boolean)
                - description

                Do not call this tool when the user only asks to display, list, read, or search a content parameter.
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
                MCPToolUtils.updateAnnotations("Update app service content", false),
                null
        );
    }

    @SuppressWarnings("unchecked")
    private McpSchema.CallToolResult execute(Map<String, Object> args) {
        mcpLogUtils.call(TOOL_NAME, "appservice_content_update", "MCP tool cerberus_appservice_content_update called with args: " + args);

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

        AnswerItem<AppServiceContent> readAnswer = appServiceContentService.readByKey(service, key);

        if (!readAnswer.isCodeStringEquals("OK") || readAnswer.getItem() == null) {
            return MCPToolUtils.errorText("App service content does not exist: service=" + service + ", key=" + key);
        }

        AppServiceContent content = readAnswer.getItem();

        try {
            for (Map.Entry<String, Object> entry : updates.entrySet()) {
                String field = entry.getKey();
                Object value = entry.getValue();

                switch (field) {
                    case "value":
                        content.setValue(asString(value, field));
                        break;
                    case "sort":
                        if (!(value instanceof Number)) {
                            throw new IllegalArgumentException("Invalid value for field 'sort'. Expected integer.");
                        }
                        content.setSort(((Number) value).intValue());
                        break;
                    case "isActive":
                        if (!(value instanceof Boolean)) {
                            throw new IllegalArgumentException("Invalid value for field 'isActive'. Expected boolean.");
                        }
                        content.setActive((Boolean) value);
                        break;
                    case "description":
                        content.setDescription(asString(value, field));
                        break;
                    default:
                        return MCPToolUtils.errorText("Unsupported field: " + field);
                }
            }
        } catch (IllegalArgumentException e) {
            return MCPToolUtils.errorText(e.getMessage());
        }

        content.setUsrModif("MCP");

        Answer updateAnswer = appServiceContentService.update(service, key, content);

        if (!updateAnswer.isCodeStringEquals("OK")) {
            return MCPToolUtils.errorText(
                    "Unable to update app service content (service=" + service + ", key=" + key + "): "
                            + updateAnswer.getMessageDescription()
            );
        }

        return MCPToolUtils.successJson(Map.of(
                "status", "updated",
                "content", mapper.toDTO(content)
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
