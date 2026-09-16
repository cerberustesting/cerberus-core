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
package org.cerberus.core.mcp.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.modelcontextprotocol.spec.McpSchema;

import java.util.ArrayList;
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

    public static int getInteger(Map<String, Object> args, String key, int defaultValue) {
        Object value = args.get(key);
        if (value instanceof Integer intValue) return intValue;
        if (value instanceof Number number) return number.intValue();
        return defaultValue;
    }

    /**
     * Reads a 64-bit identifier from the tool arguments.
     *
     * <p>Execution ids exceed the int range on long-lived Cerberus instances, and JSON has no
     * integer width: a client may send the same id as a Long, an Integer or — some MCP clients
     * quote large numbers to avoid float precision loss — as a String. All three are accepted.</p>
     *
     * @param args         the tool arguments.
     * @param key          the argument name to read.
     * @param defaultValue the value returned when the argument is absent or unparseable.
     * @return the identifier, or {@code defaultValue}.
     */
    public static long getLong(Map<String, Object> args, String key, long defaultValue) {
        Object value = args.get(key);
        if (value instanceof Number number) return number.longValue();
        if (value instanceof String stringValue) {
            try {
                return Long.parseLong(stringValue.trim());
            } catch (NumberFormatException e) {
                return defaultValue;
            }
        }
        return defaultValue;
    }

    @SuppressWarnings("unchecked")
    public static List<String> getStringList(Map<String, Object> args, String key, List<String> defaultValue) {
        Object value = args.get(key);

        if (value instanceof List<?> list && list.stream().allMatch(String.class::isInstance)) {
            return (List<String>) list;
        }

        return defaultValue;
    }

    /**
     * Reads a list of identifiers from the tool arguments.
     *
     * <p>Accepts numbers and numeric strings alike: JSON has no integer type of its own and clients
     * differ on how they render one, so refusing {@code ["10","20"]} would fail a call that is
     * unambiguous.</p>
     *
     * @return the identifiers, or {@code null} when the argument is absent or holds an element that
     * is not a whole number — the two cases a caller must be told apart from an empty list.
     */
    public static List<Integer> getIntegerList(Map<String, Object> args, String key) {
        Object value = args.get(key);
        if (!(value instanceof List<?> list)) {
            return null;
        }
        List<Integer> result = new ArrayList<>(list.size());
        for (Object element : list) {
            if (element instanceof Number number) {
                result.add(number.intValue());
            } else if (element instanceof String stringValue) {
                try {
                    result.add(Integer.valueOf(stringValue.trim()));
                } catch (NumberFormatException e) {
                    return null;
                }
            } else {
                return null;
            }
        }
        return result;
    }

    /**
     * Reads a nested object from the tool arguments.
     *
     * <p>Returns an empty map rather than null for anything that is not an object, so a caller
     * that sent the wrong shape is handled by the field checks that follow instead of by a null
     * check at every use site.</p>
     */
    @SuppressWarnings("unchecked")
    public static Map<String, Object> getMap(Map<String, Object> args, String key) {
        Object value = args.get(key);
        return value instanceof Map ? (Map<String, Object>) value : Map.of();
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


    /**
     * Returns a payload both as text and as structured content.
     *
     * <p>Use this only for a tool that declares an {@code outputSchema}: the specification requires
     * a tool returning structured content to produce results conforming to its declared schema, and
     * a client is entitled to validate them. The two travel together or not at all.</p>
     *
     * <p>The serialized JSON stays in the text block, as the specification asks for backwards
     * compatibility — and because the in-app MCP Inspector parses exactly that block. The cost is
     * that the payload is carried twice, which is why this is reserved for small, bounded results:
     * on a listing it would double what a caller has to read back, working against the size limits
     * a tool result has to respect.</p>
     */
    public static McpSchema.CallToolResult successStructured(Object payload) {
        try {
            return new McpSchema.CallToolResult(
                    List.of(new McpSchema.TextContent(null, OBJECT_MAPPER.writeValueAsString(payload), null)),
                    false,
                    payload,
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