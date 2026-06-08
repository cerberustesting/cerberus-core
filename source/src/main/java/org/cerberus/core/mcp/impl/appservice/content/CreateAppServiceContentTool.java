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
import org.cerberus.core.api.dto.appservice.AppServiceContentDTOV001;
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
 * MCP tool that creates a new {@link AppServiceContent} entry for an app service.
 *
 * <p>Exposed MCP tool name: {@code cerberus_appservice_content_create}.</p>
 *
 * <p>AppServiceContent entries represent form-data or URL-encoded parameters sent with a
 * service request. The composite key is (service, key).</p>
 *
 * <p>Note: {@link AppServiceContent} uses Lombok {@code @Data}, so the boolean field
 * {@code isActive} has getter {@code isActive()} and setter {@code setActive(boolean)} —
 * NOT {@code setIsActive()}.</p>
 */
@Component
public class CreateAppServiceContentTool implements MCPTool {

    private static final String TOOL_NAME = "cerberus_appservice_content_create";

    private final IAppServiceContentService appServiceContentService;
    private final AppServiceContentMapperV001 mapper;
    private final MCPLogUtils mcpLogUtils;

    public CreateAppServiceContentTool(IAppServiceContentService appServiceContentService,
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
     * Builds the MCP tool descriptor for {@code cerberus_appservice_content_create}.
     *
     * <p>Declares {@code service}, {@code key}, and {@code value} as required parameters.
     * {@code sort}, {@code isActive}, and {@code description} are optional.</p>
     */
    private McpSchema.Tool createTool() {
        Map<String, Object> properties = new LinkedHashMap<>();
        properties.put("service", Map.of("type", "string", "description", "Name of the app service this content belongs to."));
        properties.put("key", Map.of("type", "string", "description", "Parameter key (unique per service)."));
        properties.put("value", Map.of("type", "string", "description", "Parameter value."));
        properties.put("sort", Map.of("type", "integer", "description", "Sort order (default 10)."));
        properties.put("isActive", Map.of("type", "boolean", "description", "Whether this content entry is active (default true)."));
        properties.put("description", Map.of("type", "string", "description", "Human-readable description."));

        return new McpSchema.Tool(
                TOOL_NAME,
                null,
                """
                Creates a new content parameter (form data or URL-encoded param) for an app service.

                Call this tool when the user asks to add a content parameter to a service.
                Requires service name, key, and value.

                Use cerberus_appservice_content_list to browse existing content parameters.
                Do not call this tool when the user only asks to list, read, update, or delete content.
                """,
                new McpSchema.JsonSchema("object", properties, List.of("service", "key", "value"), null, null, null),
                null,
                MCPToolUtils.createAnnotations("Create app service content", false),
                null
        );
    }

    /**
     * Validates args, checks for duplicates, builds the entity, and delegates to
     * {@link IAppServiceContentService#create(AppServiceContent)}.
     */
    private McpSchema.CallToolResult execute(Map<String, Object> args) {
        String service = MCPToolUtils.getString(args, "service", "");
        String key = MCPToolUtils.getString(args, "key", "");
        String value = MCPToolUtils.getString(args, "value", "");
        String description = MCPToolUtils.getString(args, "description", "");
        Object sortObj = args.get("sort");
        int sort = (sortObj instanceof Number) ? ((Number) sortObj).intValue() : 10;
        Object activeObj = args.get("isActive");
        boolean isActive = (activeObj instanceof Boolean) ? (Boolean) activeObj : true;

        mcpLogUtils.call(TOOL_NAME, "appservice_content_create",
                String.format("MCP tool %s called with service=%s key=%s", TOOL_NAME, service, key));

        if (service.isBlank()) return MCPToolUtils.errorText("Missing required parameter: service");
        if (key.isBlank()) return MCPToolUtils.errorText("Missing required parameter: key");
        if (value.isBlank()) return MCPToolUtils.errorText("Missing required parameter: value");

        // Guard against duplicate (service, key) before hitting the DB unique constraint.
        AnswerItem<AppServiceContent> existing = appServiceContentService.readByKey(service, key);
        if (existing.isCodeStringEquals("OK") && existing.getItem() != null) {
            return MCPToolUtils.errorText("Content already exists: service=" + service + " key=" + key);
        }

        AppServiceContent content = new AppServiceContent();
        content.setService(service);
        content.setKey(key);
        content.setValue(value);
        content.setSort(sort);
        // setActive(boolean) — Lombok strips the 'is' prefix from the field name isActive.
        content.setActive(isActive);
        content.setDescription(description);
        content.setUsrCreated("MCP");

        Answer answer = appServiceContentService.create(content);
        if (!answer.isCodeStringEquals("OK")) {
            return MCPToolUtils.errorText("Unable to create content: " + answer.getMessageDescription());
        }

        AppServiceContentDTOV001 dto = mapper.toDTO(content);
        return MCPToolUtils.successJson(Map.of("status", "created", "content", dto));
    }
}
