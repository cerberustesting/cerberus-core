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
 * MCP tool that updates an existing {@link AppServiceContent} entry.
 *
 * <p>Exposed MCP tool name: {@code cerberus_appservice_content_update}.</p>
 *
 * <p>Uses a read-before-write pattern so unmodified fields retain their current values.
 * Delegates to {@link IAppServiceContentService#update(String, String, AppServiceContent)}.</p>
 *
 * <p>Note: {@link AppServiceContent} uses Lombok {@code @Data}. For boolean field {@code isActive},
 * the setter is {@code setActive(boolean)} — Lombok strips the {@code is} prefix.</p>
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

    /**
     * Builds the MCP tool descriptor for {@code cerberus_appservice_content_update}.
     *
     * <p>{@code additionalProperties: false} on the {@code updates} object prevents the AI model
     * from sending unrecognised fields that would be rejected by the switch in
     * {@link #execute(Map)}.</p>
     */
    private McpSchema.Tool createTool() {
        Map<String, Object> updateProperties = new LinkedHashMap<>();
        updateProperties.put("value", Map.of("type", "string", "description", "New parameter value."));
        updateProperties.put("sort", Map.of("type", "integer", "description", "New sort order."));
        updateProperties.put("isActive", Map.of("type", "boolean", "description", "Whether this content entry is active."));
        updateProperties.put("description", Map.of("type", "string", "description", "New description."));

        Map<String, Object> updatesSchema = new LinkedHashMap<>();
        updatesSchema.put("type", "object");
        updatesSchema.put("description", "Fields to update. Only provide the fields that need to change.");
        updatesSchema.put("properties", updateProperties);
        // Reject unknown fields so the switch never hits an unexpected default.
        updatesSchema.put("additionalProperties", false);

        Map<String, Object> properties = new LinkedHashMap<>();
        properties.put("service", Map.of("type", "string", "description", "Name of the app service."));
        properties.put("key", Map.of("type", "string", "description", "Key of the content parameter to update."));
        properties.put("updates", updatesSchema);

        return new McpSchema.Tool(
                TOOL_NAME,
                null,
                """
                Updates an existing content parameter for an app service.

                Call this tool when the user asks to modify the value, sort order, or active state of a content parameter.
                Only provide the fields that need to change in the updates object.

                Use cerberus_appservice_content_list to find the exact key before updating.
                Do not call this tool when the user only asks to list, read, create, or delete content.
                """,
                new McpSchema.JsonSchema("object", properties, List.of("service", "key", "updates"), null, null, null),
                null,
                MCPToolUtils.updateAnnotations("Update app service content", false),
                null
        );
    }

    @SuppressWarnings("unchecked")
    private McpSchema.CallToolResult execute(Map<String, Object> args) {
        String service = MCPToolUtils.getString(args, "service", "");
        String key = MCPToolUtils.getString(args, "key", "");

        mcpLogUtils.call(TOOL_NAME, "appservice_content_update",
                String.format("MCP tool %s called with service=%s key=%s", TOOL_NAME, service, key));

        if (service.isBlank()) return MCPToolUtils.errorText("Missing required parameter: service");
        if (key.isBlank()) return MCPToolUtils.errorText("Missing required parameter: key");

        Object updatesObj = args.get("updates");
        if (!(updatesObj instanceof Map)) return MCPToolUtils.errorText("Missing or invalid required parameter: updates");
        Map<String, Object> updates = (Map<String, Object>) updatesObj;
        if (updates.isEmpty()) return MCPToolUtils.errorText("No field provided to update.");

        // Read-before-write so unmodified fields retain their current values.
        AnswerItem<AppServiceContent> readAnswer = appServiceContentService.readByKey(service, key);
        if (!readAnswer.isCodeStringEquals("OK") || readAnswer.getItem() == null) {
            return MCPToolUtils.errorText("Content does not exist: service=" + service + " key=" + key);
        }

        AppServiceContent content = readAnswer.getItem();

        try {
            for (Map.Entry<String, Object> entry : updates.entrySet()) {
                String field = entry.getKey();
                Object value = entry.getValue();
                switch (field) {
                    case "value": content.setValue(asString(value, field)); break;
                    case "sort": content.setSort(((Number) value).intValue()); break;
                    // setActive(boolean) — Lombok strips 'is' prefix from isActive field name.
                    case "isActive": content.setActive((Boolean) value); break;
                    case "description": content.setDescription(asString(value, field)); break;
                    default: return MCPToolUtils.errorText("Unsupported field for content update: " + field);
                }
            }
        } catch (IllegalArgumentException e) {
            return MCPToolUtils.errorText(e.getMessage());
        }

        content.setUsrModif("MCP");

        Answer answer = appServiceContentService.update(service, key, content);
        if (!answer.isCodeStringEquals("OK")) {
            return MCPToolUtils.errorText("Unable to update content: " + answer.getMessageDescription());
        }

        return MCPToolUtils.successJson(Map.of("status", "updated", "content", mapper.toDTO(content)));
    }

    private String asString(Object value, String field) {
        if (value == null) return "";
        if (!(value instanceof String)) throw new IllegalArgumentException("Invalid value for '" + field + "'. Expected string.");
        return ((String) value).trim();
    }
}
