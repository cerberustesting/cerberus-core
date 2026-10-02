/**
 * Cerberus Copyright (C) 2013 - 2026 cerberustesting
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
package org.cerberus.core.mcpdelta.tools;

import com.fasterxml.jackson.databind.JsonNode;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/** One MCP tool. Answers are plain text: compact, readable, and never JSON the model has to unpack. */
public interface Tool {

    String name();

    String description();

    Map<String, Object> inputSchema();

    default boolean readOnly() {
        return false;
    }

    String call(JsonNode args);

    /** A refusal the model can act on: reported as an MCP tool error with this text. */
    final class ToolError extends RuntimeException {
        public ToolError(String message) {
            super(message);
        }
    }

    /** Reads arguments leniently: a single string where a list is expected is a list of one. */
    final class Args {
        private Args() {
        }

        public static String str(JsonNode args, String key, String def) {
            JsonNode n = args.get(key);
            if (n == null || n.isNull()) {
                return def;
            }
            return n.isTextual() ? n.asText() : n.toString();
        }

        public static boolean bool(JsonNode args, String key, boolean def) {
            JsonNode n = args.get(key);
            if (n == null || n.isNull()) {
                return def;
            }
            if (n.isBoolean()) {
                return n.asBoolean();
            }
            String s = n.asText().trim().toLowerCase();
            return s.equals("true") || s.equals("yes") || s.equals("1");
        }

        public static int integer(JsonNode args, String key, int def) {
            JsonNode n = args.get(key);
            if (n == null || n.isNull()) {
                return def;
            }
            if (n.isNumber()) {
                return n.asInt();
            }
            try {
                return Integer.parseInt(n.asText().trim());
            } catch (NumberFormatException e) {
                throw new ToolError(key + " must be a whole number");
            }
        }

        public static List<String> list(JsonNode args, String key) {
            JsonNode n = args.get(key);
            List<String> out = new ArrayList<>();
            if (n == null || n.isNull()) {
                return out;
            }
            if (n.isArray()) {
                for (JsonNode e : n) {
                    out.add(e.isTextual() ? e.asText() : e.toString());
                }
            } else if (n.isTextual()) {
                String s = n.asText().trim();
                // A JSON array sent as a string is still an array.
                if (s.startsWith("[") && s.endsWith("]")) {
                    try {
                        JsonNode parsed = new com.fasterxml.jackson.databind.ObjectMapper().readTree(s);
                        for (JsonNode e : parsed) {
                            out.add(e.isTextual() ? e.asText() : e.toString());
                        }
                        return out;
                    } catch (Exception ignored) {
                        // not JSON after all: a plain string
                    }
                }
                out.add(n.asText());
            } else {
                out.add(n.toString());
            }
            return out;
        }

        /** An object, or an object sent as a JSON string. */
        public static JsonNode object(JsonNode args, String key) {
            JsonNode n = args.get(key);
            if (n == null || n.isNull()) {
                return null;
            }
            if (n.isTextual()) {
                try {
                    return new com.fasterxml.jackson.databind.ObjectMapper().readTree(n.asText());
                } catch (Exception e) {
                    throw new ToolError(key + " must be an object");
                }
            }
            return n;
        }

        /** An array of objects, or one sent as a JSON string, or a single object. */
        public static List<JsonNode> objects(JsonNode args, String key) {
            JsonNode n = object(args, key);
            List<JsonNode> out = new ArrayList<>();
            if (n == null) {
                return out;
            }
            if (n.isArray()) {
                n.forEach(out::add);
            } else {
                out.add(n);
            }
            return out;
        }
    }
}
