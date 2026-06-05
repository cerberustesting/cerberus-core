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
import org.cerberus.core.mcp.MCPTool;
import org.cerberus.core.mcp.util.MCPLogUtils;
import org.cerberus.core.mcp.util.MCPToolUtils;
import org.cerberus.core.crud.entity.Test;
import org.cerberus.core.crud.service.ITestService;
import org.cerberus.core.util.answer.Answer;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
public class DeleteTestFolderTool implements MCPTool {

    private static final String TOOL_NAME = "cerberus_test_folder_delete";

    private final ITestService testService;
    private final MCPLogUtils mcpLogUtils;

    public DeleteTestFolderTool(ITestService testService, MCPLogUtils mcpLogUtils) {
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
                        "description", "Name of the test folder to delete (the test field)."
                )
        );

        return new McpSchema.Tool(
                TOOL_NAME,
                null,
                """
                Deletes an existing test folder in Cerberus.

                Call this tool whenever the user asks to delete or remove a test folder.
                Requires the test folder name.
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
                MCPToolUtils.deleteAnnotations("Delete test folder", false),
                null
        );
    }

    private McpSchema.CallToolResult execute(Map<String, Object> args) {
        String testFolder = MCPToolUtils.getString(args, "testFolder", "");

        mcpLogUtils.call(TOOL_NAME, "test_folder_delete", String.format("MCP tool %s called with testFolder=%s", TOOL_NAME, testFolder));

        if (testFolder.isBlank()) {
            return MCPToolUtils.errorText("Missing required parameter: testFolder");
        }

        Test test = testService.readByKey(testFolder).getItem();

        if (test == null) {
            return MCPToolUtils.errorText("Test Folder does not exist: " + testFolder);
        }

        Answer answer = testService.delete(test);

        if (!answer.isCodeStringEquals("OK")) {
            return MCPToolUtils.errorText("Unable to delete Test Folder " + testFolder + ": " + answer.getMessageDescription());
        }

        return MCPToolUtils.successJson(Map.of(
                "status", "deleted",
                "testFolder", testFolder
        ));
    }

}