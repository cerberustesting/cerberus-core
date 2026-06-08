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
package org.cerberus.core.mcp.impl;

import io.modelcontextprotocol.server.McpServerFeatures;
import io.modelcontextprotocol.spec.McpSchema;
import org.cerberus.core.mcp.MCPTool;
import org.cerberus.core.database.IDatabaseVersioningService;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

/**
 * MCP tool that exposes the current Cerberus database schema version.
 *
 * <p>Tool name: {@code get_db_version}</p>
 * <p>Delegates to {@link IDatabaseVersioningService#getSqlVersion()} to retrieve the
 * applied SQL migration version stored in the database.</p>
 *
 * <p>This tool takes no input parameters and always returns a plain-text response
 * containing the version number.</p>
 */
@Component
public class GetDbVersionTool implements MCPTool {

    private final IDatabaseVersioningService databaseVersioningService;

    public GetDbVersionTool(IDatabaseVersioningService databaseVersioningService) {
        this.databaseVersioningService = databaseVersioningService;
    }

    /**
     * Builds and returns the MCP {@code get_db_version} tool specification.
     *
     * <p>The tool accepts an empty JSON object as input (no parameters required)
     * and returns a single text content block with the current DB schema version.</p>
     *
     * @return a {@link McpServerFeatures.SyncToolSpecification} that can be registered
     *         with the MCP server
     */
    @Override
    public McpServerFeatures.SyncToolSpecification toToolSpecification() {
        return new McpServerFeatures.SyncToolSpecification(
                new McpSchema.Tool(
                        "get_db_version",           // name
                        null,                       // title
                        "Returns current Cerberus DB schema version", // description
                        // Empty object schema — no input parameters needed
                        new McpSchema.JsonSchema("object", Map.of(), null, null, null, null), // inputSchema
                        null,                       // outputSchema
                        null,                       // annotations
                        null                        // meta
                ),
                (exchange, args) -> {
                    // Convert to String explicitly in case getSqlVersion returns a numeric type
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
