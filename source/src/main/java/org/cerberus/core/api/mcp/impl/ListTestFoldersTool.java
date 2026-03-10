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

import com.fasterxml.jackson.core.JsonProcessingException;
import io.modelcontextprotocol.server.McpServerFeatures;
import io.modelcontextprotocol.spec.McpSchema;
import org.cerberus.core.api.mcp.MCPRequest;
import org.cerberus.core.api.mcp.MCPTool;
import org.cerberus.core.api.mcp.MCPToolMetadata;
import org.cerberus.core.crud.entity.Test;
import org.cerberus.core.crud.service.ITestService;
import org.cerberus.core.exception.CerberusException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class ListTestFoldersTool implements MCPTool {

    private final ITestService testService;

    public ListTestFoldersTool(ITestService testService) {
        this.testService = testService;
    }

    @Override
    public McpServerFeatures.SyncToolSpecification toToolSpecification() {
        return new McpServerFeatures.SyncToolSpecification(
                new McpSchema.Tool(
                        "list_test_folders",
                        null,
                        "Returns list of available Test Folders",
                        new McpSchema.JsonSchema("object", Map.of(), null, null, null, null),
                        null,
                        null,
                        null
                ),
                (exchange, args) -> {
                    List<String> folders = testService.readAll()
                            .getDataList()
                            .stream()
                            .map(test -> ((Test) test).getTest())
                            .collect(Collectors.toList());

                    String json = null;
                    try {
                        json = new com.fasterxml.jackson.databind.ObjectMapper()
                                .writeValueAsString(Map.of("folders", folders));
                    } catch (JsonProcessingException e) {
                        throw new RuntimeException(e);
                    }

                    return new McpSchema.CallToolResult(
                            List.of(new McpSchema.TextContent(null, json, null)),
                            false,
                            null,
                            null
                    );
                }
        );
    }
}