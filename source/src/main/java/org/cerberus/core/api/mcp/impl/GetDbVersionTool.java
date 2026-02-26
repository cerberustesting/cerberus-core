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
import org.cerberus.core.database.IDatabaseVersioningService;
import org.cerberus.core.exception.CerberusException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class GetDbVersionTool implements MCPTool {

    @Autowired
    private IDatabaseVersioningService databaseVersioningService;

    public GetDbVersionTool(IDatabaseVersioningService databaseVersioningService) {
        this.databaseVersioningService = databaseVersioningService;
    }

    @Override
    public MCPToolMetadata getMetadata() {
        return MCPToolMetadata.builder()
                .name("get_db_version")
                .description("Returns current Cerberus DB schema version")
                .category("database")
                .requiresAuth(true)
                .build();
    }

    @Override
    public Object execute(MCPRequest request) {
        return Map.of(
                "version", databaseVersioningService.getSqlVersion()
        );
    }
}
