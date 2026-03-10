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
import org.cerberus.core.database.IDatabaseVersioningService;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
public class GetDbVersionTool implements MCPTool {

    private final IDatabaseVersioningService databaseVersioningService;

    public GetDbVersionTool(IDatabaseVersioningService databaseVersioningService) {
        this.databaseVersioningService = databaseVersioningService;
    }

    @Override
    public McpServerFeatures.SyncToolSpecification toToolSpecification() {
        return new McpServerFeatures.SyncToolSpecification(
                new McpSchema.Tool(
                        "get_db_version",           // name
                        null,                       // title
                        "Returns current Cerberus DB schema version", // description
                        new McpSchema.JsonSchema("object", Map.of(), null, null, null, null), // inputSchema
                        null,                       // outputSchema
                        null,                       // annotations
                        null                        // meta
                ),
                (exchange, args) -> {
                    String version = String.valueOf(databaseVersioningService.getSqlVersion());
                    return new McpSchema.CallToolResult(
                            List.of(new McpSchema.TextContent(
                                    null,               // annotations
                                    "DB version: " + version,  // text
                                    null                // meta
                            )),
                            false,
                            null,
                            null
                    );
                }
        );
    }
}