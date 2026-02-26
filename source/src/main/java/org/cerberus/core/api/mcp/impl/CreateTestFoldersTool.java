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

import org.cerberus.core.api.mcp.MCPRequest;
import org.cerberus.core.api.mcp.MCPTool;
import org.cerberus.core.api.mcp.MCPToolMetadata;
import org.cerberus.core.crud.entity.Test;
import org.cerberus.core.crud.service.ITestService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class CreateTestFoldersTool implements MCPTool {

    @Autowired
    private ITestService testService;

    @Override
    public MCPToolMetadata getMetadata() {
        return MCPToolMetadata.builder()
                .name("create_test_folder")
                .description("Creates a new test folder in Cerberus. Call this tool whenever the user asks to create, add, or organize a test folder. Requires a folder name.")
                .category("test_folder")
                .requiresAuth(true)
                .inputSchema(
                        Map.of(
                                "type", "object",
                                "properties", Map.of(
                                        "testFolder", Map.of(
                                                "type", "string",
                                                "description", "Name of the new test folder"
                                        ),
                                        "description", Map.of(
                                                "type", "string",
                                                "description", "Optional description of the test folder"
                                        )
                                ),
                                "required", List.of("testFolder")
                        )
                )
                .build();
    }


    @Override
    public Object execute(MCPRequest request) {

        Map<String, Object> params = request.getParams();

        if (params == null || !params.containsKey("testFolder")) {
            throw new IllegalArgumentException("Missing required parameter: testFolder");
        }

        String testFolder = String.valueOf(params.get("testFolder"));
        String description = params.get("description") != null
                ? String.valueOf(params.get("description"))
                : "";

        if (testService.exist(testFolder)) {
            throw new IllegalStateException("Test Folder already exists: " + testFolder);
        }

        Test test = new Test();
        test.setTest(testFolder);
        test.setDescription(description);
        test.setUsrCreated("MCP");
        test.setActive(true);

        testService.create(test);

        return Map.of(
                "status", "created",
                "testFolder", testFolder
        );
    }

}
