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
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

/**
 * MCP tool {@value TOOL_NAME} — retrieves a single content parameter of a Cerberus AppService
 * identified by its composite key ({@code service}, {@code key}).
 *
 * <p>Delegates to {@link IAppServiceContentService#readByKey(String, String)} to load the
 * {@link AppServiceContent} entity, then converts it to a DTO via
 * {@link AppServiceContentMapperV001} before returning the JSON result.</p>
 */
@Component
public class GetAppServiceContentTool implements MCPTool {

    private static final String TOOL_NAME = "cerberus_appservice_content_get";

    private final IAppServiceContentService appServiceContentService;
    private final AppServiceContentMapperV001 appServiceContentMapper;
    private final MCPLogUtils mcpLogUtils;

    public GetAppServiceContentTool(IAppServiceContentService appServiceContentService,
                                    AppServiceContentMapperV001 appServiceContentMapper,
                                    MCPLogUtils mcpLogUtils) {
        this.appServiceContentService = appServiceContentService;
        this.appServiceContentMapper = appServiceContentMapper;
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
     * Builds the MCP tool schema descriptor for {@value TOOL_NAME}.
     *
     * <p>Both {@code service} and {@code key} are required because they form the composite
     * primary key of an {@link AppServiceContent}. The tool is declared read-only because
     * it performs no mutations.</p>
     *
     * @return the fully populated {@link McpSchema.Tool} descriptor
     */
    private McpSchema.Tool createTool() {
        Map<String, Object> properties = Map.of(
                "service", Map.of(
                        "type", "string",
                        "description", "Name of the AppService that owns the content parameter."
                ),
                "key", Map.of(
                        "type", "string",
                        "description", "Content parameter key (name) to retrieve."
                )
        );

        return new McpSchema.Tool(
                TOOL_NAME,
                null,
                """
                Returns a single content parameter belonging to a Cerberus AppService, identified by
                its composite key (service, key).

                Use this tool when you need the full metadata for one specific content parameter of a
                known AppService.
                """,
                new McpSchema.JsonSchema(
                        "object",
                        properties,
                        List.of("service", "key"),
                        null,
                        null,
                        null
                ),
                null,
                MCPToolUtils.readOnlyAnnotations("Get app service content", true),
                null
        );
    }

    /**
     * Executes the tool: looks up the AppService content parameter by its composite key
     * ({@code service}, {@code key}) and returns its DTO.
     *
     * <p>Returns {@code found=false} (without an error) when either identifier is blank
     * or the content parameter does not exist, so the AI client can handle the "not found" case
     * gracefully.</p>
     *
     * @param args raw MCP arguments map; must contain non-blank {@code service} and {@code key} values
     * @return a JSON result containing a {@code found} flag and, when found, the content DTO
     */
    private McpSchema.CallToolResult execute(Map<String, Object> args) {
        String service = MCPToolUtils.getString(args, "service", "");
        String key = MCPToolUtils.getString(args, "key", "");

        mcpLogUtils.call(TOOL_NAME, "appservice_content_get",
                String.format("MCP tool %s called with service=%s, key=%s", TOOL_NAME, service, key));

        if (service.isBlank() || key.isBlank()) {
            return MCPToolUtils.successJson(Map.of(
                    "service", service,
                    "key", key,
                    "found", false
            ));
        }

        var answerItem = appServiceContentService.readByKey(service, key);

        if (!answerItem.isCodeStringEquals("OK") || answerItem.getItem() == null) {
            return MCPToolUtils.successJson(Map.of(
                    "service", service,
                    "key", key,
                    "found", false
            ));
        }

        AppServiceContentDTOV001 dto = appServiceContentMapper.toDTO(answerItem.getItem());

        return MCPToolUtils.successJson(Map.of(
                "found", true,
                "content", dto
        ));
    }
}
