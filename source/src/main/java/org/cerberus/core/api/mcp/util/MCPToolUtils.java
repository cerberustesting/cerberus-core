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
package org.cerberus.core.api.mcp.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.modelcontextprotocol.spec.McpSchema;

import java.util.List;
import java.util.Map;

public final class MCPToolUtils {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private MCPToolUtils() {
    }

    public static Map<String, Object> argumentsOrEmpty(Map<String, Object> arguments) {
        return arguments == null ? Map.of() : arguments;
    }

    public static String getString(Map<String, Object> args, String key, String defaultValue) {
        Object value = args.get(key);
        return value instanceof String stringValue ? stringValue : defaultValue;
    }

    public static boolean getBoolean(Map<String, Object> args, String key, boolean defaultValue) {
        Object value = args.get(key);
        return value instanceof Boolean booleanValue ? booleanValue : defaultValue;
    }

    @SuppressWarnings("unchecked")
    public static List<String> getStringList(Map<String, Object> args, String key, List<String> defaultValue) {
        Object value = args.get(key);

        if (value instanceof List<?> list && list.stream().allMatch(String.class::isInstance)) {
            return (List<String>) list;
        }

        return defaultValue;
    }

    public static String nullSafe(String value) {
        return value == null ? "" : value;
    }

    public static boolean containsIgnoreCase(String source, String search) {
        if (search == null || search.isBlank()) {
            return true;
        }

        return nullSafe(source)
                .toLowerCase()
                .contains(search.toLowerCase());
    }

    public static McpSchema.CallToolResult errorText(String message) {
        return new McpSchema.CallToolResult(
                List.of(new McpSchema.TextContent(null, message, null)),
                true,
                null,
                null
        );
    }

    public static McpSchema.CallToolResult successJson(Object payload) {
        try {
            String json = OBJECT_MAPPER.writeValueAsString(payload);

            return new McpSchema.CallToolResult(
                    List.of(new McpSchema.TextContent(null, json, null)),
                    false,
                    null,
                    null
            );
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Unable to serialize MCP tool response.", e);
        }
    }


    public static McpSchema.ToolAnnotations annotations(
            String title, boolean readOnlyHint, boolean destructiveHint, boolean idempotentHint, boolean openWorldHint, boolean returnDirect
    ) {
        return new McpSchema.ToolAnnotations(title, readOnlyHint, destructiveHint, idempotentHint, openWorldHint, returnDirect);
    }

    public static McpSchema.ToolAnnotations readOnlyAnnotations(String title, boolean returnDirect) {
        return annotations(title, true, false, true, false, returnDirect);
    }

    public static McpSchema.ToolAnnotations createAnnotations(String title, boolean returnDirect) {
        return annotations(title, false, false, false, false, returnDirect);
    }

    public static McpSchema.ToolAnnotations updateAnnotations(String title, boolean returnDirect) {
        return annotations(title, false, false, false, false, returnDirect);
    }

    public static McpSchema.ToolAnnotations deleteAnnotations(String title, boolean returnDirect) {
        return annotations(title, false, true, false, false, returnDirect);
    }


    
}