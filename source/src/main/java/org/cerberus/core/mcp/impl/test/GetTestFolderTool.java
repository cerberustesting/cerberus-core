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
package org.cerberus.core.mcp.impl.test;

import io.modelcontextprotocol.server.McpServerFeatures;
import io.modelcontextprotocol.spec.McpSchema;
import org.cerberus.core.api.dto.test.TestMapperV001;
import org.cerberus.core.mcp.MCPTool;
import org.cerberus.core.mcp.util.MCPLogUtils;
import org.cerberus.core.mcp.util.MCPProjectionUtils;
import org.cerberus.core.mcp.util.MCPToolUtils;
import org.cerberus.core.crud.entity.Test;
import org.cerberus.core.crud.service.ITestService;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
public class GetTestFolderTool implements MCPTool {

    private static final String TOOL_NAME = "cerberus_test_folder_get";

    private static final List<String> ALL_FIELDS = List.of("test", "description", "isActive", "parentTest",
            "usrCreated", "dateCreated", "usrModif", "dateModif");

    private final ITestService testService;
    private final TestMapperV001 testMapper;
    private final MCPLogUtils mcpLogUtils;

    public GetTestFolderTool(ITestService testService, TestMapperV001 testMapper,MCPLogUtils mcpLogUtils) {
        this.testService = testService;
        this.testMapper = testMapper;
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
        Map<String, Object> properties = Map.of(
                "test", Map.of(
                        "type", "string",
                        "description", "Identifier of the test folder to retrieve (the test field)."
                )
        );

        return new McpSchema.Tool(
                TOOL_NAME,
                null,
                """
                Returns a single Cerberus test folder with all its fields, identified by its test field.

                Use this tool when full metadata for a known test folder is needed.
                """,
                new McpSchema.JsonSchema(
                        "object",
                        properties,
                        List.of("test"),
                        null,
                        null,
                        null
                ),
                null,
                MCPToolUtils.readOnlyAnnotations("Get test folder", false),
                null
        );
    }

    private McpSchema.CallToolResult execute(Map<String, Object> args) {
        String testFolder = MCPToolUtils.getString(args, "test", "");

        mcpLogUtils.call(TOOL_NAME, "test_folder_get", String.format("MCP tool %s called with testFolder=%s", TOOL_NAME, testFolder));

        if (testFolder.isBlank()) {
            return MCPToolUtils.successJson(Map.of(
                    "testFolder", testFolder,
                    "found", false
            ));
        }

        Test folder = testService.readByKey(testFolder).getItem();

        if (folder == null) {
            return MCPToolUtils.successJson(Map.of(
                    "testFolder", testFolder,
                    "found", false
            ));
        }

        return MCPToolUtils.successJson(Map.of(
                "found", true,
                "testFolder", MCPProjectionUtils.project(testMapper.toDTO(folder), ALL_FIELDS)
        ));
    }

}