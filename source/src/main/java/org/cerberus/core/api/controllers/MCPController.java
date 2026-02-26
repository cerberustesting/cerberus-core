/*
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
package org.cerberus.core.api.controllers;


import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import org.cerberus.core.api.mcp.MCPRequest;
import org.cerberus.core.api.mcp.MCPTool;
import org.cerberus.core.api.mcp.MCPToolRegistry;
import org.cerberus.core.exception.CerberusException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * @author bcivel
 */
@Tag(name = "MCP", description = "Endpoints related to MCP")
@RestController
@RequestMapping("/mcp")
public class MCPController {

    private final MCPToolRegistry registry;

    public MCPController(MCPToolRegistry registry) {
        this.registry = registry;
    }

    @PostMapping
    public ResponseEntity<?> handle(@RequestBody MCPRequest request) {

        switch (request.getMethod()) {

            case "initialize":
                return ok(request, Map.of(
                        "protocolVersion", "2024-11-05",
                        "serverInfo", Map.of("name", "Cerberus MCP", "version", "1.0.0"),
                        "capabilities", Map.of("tools", Map.of())
                ));


            case "tools/list":
                return ok(request, Map.of("tools", registry.listTools()));

            case "tools/call":
                return handleToolCall(request);

            default:
                return error(request, -32601, "Method not found");
        }
    }

    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> health() {
        return ResponseEntity.ok(Map.of(
                "status", "UP",
                "server", "Cerberus MCP",
                "version", "1.0.0"
        ));
    }

    private ResponseEntity<?> handleToolCall(MCPRequest request) {
        MCPTool tool = registry.getTool(request.getToolName());

        if (tool == null) {
            return error(request, -32601, "Tool not found");
        }

        try {
            Object result = tool.execute(request);
            return ok(request, result);
        } catch (Exception e) {
            return error(request, -32000, e.getMessage());
        }
    }

    private ResponseEntity<?> ok(MCPRequest request, Object result) {
        return ResponseEntity.ok(Map.of(
                "jsonrpc", "2.0",
                "id", request.getId(),
                "result", result
        ));
    }

    private ResponseEntity<?> error(MCPRequest request, int code, String message) {
        return ResponseEntity.ok(Map.of(
                "jsonrpc", "2.0",
                "id", request.getId(),
                "error", Map.of(
                        "code", code,
                        "message", message
                )
        ));
    }
}
