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
package org.cerberus.core.api.mcp.impl.test;

import io.modelcontextprotocol.server.McpServerFeatures;
import io.modelcontextprotocol.spec.McpSchema;
import org.cerberus.core.api.mcp.MCPTool;
import org.cerberus.core.api.mcp.util.MCPLogUtils;
import org.cerberus.core.api.mcp.util.MCPToolUtils;
import org.cerberus.core.crud.entity.LogEvent;
import org.cerberus.core.crud.entity.Test;
import org.cerberus.core.crud.service.ILogEventService;
import org.cerberus.core.crud.service.ITestService;
import org.cerberus.core.util.answer.Answer;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
public class CreateTestFolderTool implements MCPTool {

    private static final String TOOL_NAME = "cerberus_test_folder_create";

    private final ITestService testService;
    private final MCPLogUtils mcpLogUtils;

    public CreateTestFolderTool(ITestService testService, MCPLogUtils mcpLogUtils) {
        this.testService = testService;
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
                "testFolder", Map.of(
                        "type", "string",
                        "description", "Name of the new test folder."
                ),
                "description", Map.of(
                        "type", "string",
                        "description", "Optional description of the test folder."
                )
        );


        return new McpSchema.Tool(
                TOOL_NAME,
                null,
                """
                Creates a new test folder in Cerberus.

                Call this tool whenever the user asks to create, add, or organize a test folder.
                Requires a folder name.
                """,
                new McpSchema.JsonSchema(
                        "object",
                        properties,
                        List.of("testFolder"),
                        null,
                        null,
                        null
                ),
                null,
                MCPToolUtils.createAnnotations("Create test folder", false),
                null
        );
    }

    private McpSchema.CallToolResult execute(Map<String, Object> args) {
        String testFolder = MCPToolUtils.getString(args, "testFolder", "");
        String description = MCPToolUtils.getString(args, "description", "");

        mcpLogUtils.call(TOOL_NAME,"test_folder_create",  String.format("MCP tool %s called with testFolder=%s", TOOL_NAME, testFolder));

        if (testFolder.isBlank()) {
            return MCPToolUtils.errorText("Missing required parameter: testFolder");
        }

        if (testService.exist(testFolder)) {
            return MCPToolUtils.errorText("Test Folder already exists: " + testFolder);
        }

        Test test = new Test();
        test.setTest(testFolder);
        test.setDescription(description);
        test.setUsrCreated("MCP");
        test.setActive(true);

        Answer answer = testService.create(test);

        if (!answer.isCodeStringEquals("OK")) {
            return MCPToolUtils.errorText("Unable to create Test Folder " + testFolder + ": " + answer.getMessageDescription());
        }

        return MCPToolUtils.successJson(Map.of(
                "status", "created",
                "testFolder", testFolder
        ));
    }

}