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
package org.cerberus.core.api.mcp.impl;

import io.modelcontextprotocol.server.McpServerFeatures;
import io.modelcontextprotocol.spec.McpSchema;
import org.cerberus.core.api.mcp.MCPTool;
import org.cerberus.core.crud.entity.Test;
import org.cerberus.core.crud.service.ITestService;
import org.springframework.stereotype.Component;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.List;
import java.util.Map;

@Component
public class CreateTestFoldersTool implements MCPTool {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private final ITestService testService;

    public CreateTestFoldersTool(ITestService testService) {
        this.testService = testService;
    }

    @Override
    public McpServerFeatures.SyncToolSpecification toToolSpecification() {
        return new McpServerFeatures.SyncToolSpecification(
                new McpSchema.Tool(
                        "create_test_folder",
                        null,
                        "Creates a new test folder in Cerberus. Call this tool whenever the user asks to create, add, or organize a test folder. Requires a folder name.",
                        new McpSchema.JsonSchema(
                                "object",
                                Map.of(
                                        "testFolder", Map.of(
                                                "type", "string",
                                                "description", "Name of the new test folder"
                                        ),
                                        "description", Map.of(
                                                "type", "string",
                                                "description", "Optional description of the test folder"
                                        )
                                ),
                                List.of("testFolder"), // required
                                null,
                                null,
                                null
                        ),
                        null,
                        null,
                        null
                ),
                (exchange, args) -> {
                    try {
                        Map<String, Object> arguments = args.arguments();

                        if (arguments == null || !arguments.containsKey("testFolder")) {
                            return new McpSchema.CallToolResult(
                                    List.of(new McpSchema.TextContent(null, "Missing required parameter: testFolder", null)),
                                    true, null, null
                            );
                        }

                        String testFolder = String.valueOf(arguments.get("testFolder"));
                        String description = arguments.get("description") != null
                                ? String.valueOf(arguments.get("description"))
                                : "";

                        if (testService.exist(testFolder)) {
                            return new McpSchema.CallToolResult(
                                    List.of(new McpSchema.TextContent(null, "Test Folder already exists: " + testFolder, null)),
                                    true, null, null
                            );
                        }

                        Test test = new Test();
                        test.setTest(testFolder);
                        test.setDescription(description);
                        test.setUsrCreated("MCP");
                        test.setActive(true);

                        testService.create(test);

                        String json = OBJECT_MAPPER.writeValueAsString(Map.of(
                                "status", "created",
                                "testFolder", testFolder
                        ));

                        return new McpSchema.CallToolResult(
                                List.of(new McpSchema.TextContent(null, json, null)),
                                false, null, null
                        );

                    } catch (Exception e) {
                        return new McpSchema.CallToolResult(
                                List.of(new McpSchema.TextContent(null, "Error: " + e.getMessage(), null)),
                                true, null, null
                        );
                    }
                }
        );
    }
}