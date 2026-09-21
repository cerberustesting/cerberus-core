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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Turns the most common mistake made against a Cerberus update tool into a message that carries
 * the fix.
 *
 * <p>Every update tool takes the fields to change inside an {@code updates} object rather than at
 * the top level. Nothing in the schema makes that obvious before the first attempt, so an agent
 * meeting one of these tools for the first time reliably sends the fields flat and gets back
 * "Missing or invalid required parameter: updates" — a message that says what is wrong and
 * nothing about what to send instead. One wasted round trip, once per tool, for more than twenty
 * tools.</p>
 *
 * <p>This check runs before the handler, reads the tool's own schema, and answers with the
 * corrected payload: which of the fields the caller sent belong inside {@code updates}, which
 * ones the tool accepts there, and a ready-to-send example built from the arguments actually
 * supplied. The agent's next call is the right one.</p>
 *
 * <p>It deliberately refuses to repair the payload itself. Accepting two shapes for the same tool
 * would teach whichever shape happened to work first, and the schema would stop describing the
 * contract — a bad trade for the one round trip it would save.</p>
 *
 * <p>Applied centrally from {@link org.cerberus.core.mcp.MCPToolRegistry}, so it covers the tools
 * that exist today and any update tool added later, without the author having to remember it.</p>
 */
public final class MCPUpdatesPreflight {

    /** The wrapper object every update tool expects its mutable fields inside. */
    private static final String UPDATES = "updates";

    /** How many accepted field names to spell out before truncating the list. */
    private static final int MAX_FIELDS_LISTED = 25;

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private MCPUpdatesPreflight() {
    }

    /**
     * Checks a call against its tool's schema.
     *
     * @param tool      the tool about to be invoked.
     * @param arguments the arguments received, possibly {@code null}.
     * @return {@code null} when the call may proceed — which is every call to a tool that does not
     * require an {@code updates} object, and every call that carries one; otherwise the error to
     * return to the caller in place of running the handler.
     */
    public static McpSchema.CallToolResult check(McpSchema.Tool tool, Map<String, Object> arguments) {
        if (tool == null || tool.inputSchema() == null) {
            return null;
        }
        Map<String, Object> properties = tool.inputSchema().properties();
        List<String> required = tool.inputSchema().required();

        // Only tools that actually demand the wrapper are in scope. A tool where updates is
        // optional (the data library ones take their fields at the top level) must keep working
        // exactly as its schema says.
        if (properties == null || required == null || !required.contains(UPDATES)) {
            return null;
        }
        // Only a wrapper the schema declares as an object: a future tool requiring an "updates" of
        // another shape would otherwise be answered with advice about a shape it does not want.
        Object updatesSchema = properties.get(UPDATES);
        if (!(updatesSchema instanceof Map) || !"object".equals(((Map<?, ?>) updatesSchema).get("type"))) {
            return null;
        }

        Map<String, Object> args = MCPToolUtils.argumentsOrEmpty(arguments);
        Object updates = args.get(UPDATES);
        if (updates instanceof Map) {
            return null;
        }

        List<String> accepted = acceptedFields(properties);
        // Fields the caller sent flat that the tool would have accepted inside the wrapper. This
        // is the part that names the actual mistake rather than describing the rule.
        List<String> misplaced = args.keySet().stream()
                .filter(accepted::contains)
                .filter(key -> !properties.containsKey(key))
                .toList();

        StringBuilder message = new StringBuilder();
        message.append("The tool '").append(tool.name()).append("' takes the fields to change inside an '")
                .append(UPDATES).append("' object, not at the top level.\n\n");

        if (updates != null) {
            message.append("You sent '").append(UPDATES).append("' as ")
                    .append(describeType(updates))
                    .append(". It must be a JSON object");
            if (updates instanceof String) {
                message.append(", not a string containing JSON");
            }
            message.append(".\n\n");
        } else if (!misplaced.isEmpty()) {
            message.append("You sent ").append(String.join(", ", quoted(misplaced)))
                    .append(" at the top level; ")
                    .append(misplaced.size() == 1 ? "it belongs" : "they belong")
                    .append(" inside '").append(UPDATES).append("'.\n\n");
        }

        message.append("Accepted inside '").append(UPDATES).append("': ")
                .append(summarise(accepted)).append(".\n\n");
        message.append("Send this instead:\n").append(example(properties, required, args, misplaced, accepted));

        return MCPToolUtils.errorText(message.toString());
    }

    /**
     * The field names the {@code updates} object declares.
     *
     * @return the accepted names, or an empty list when the wrapper carries no declared
     * properties — an update tool with a free-form wrapper is unusual but must not break here.
     */
    @SuppressWarnings("unchecked")
    private static List<String> acceptedFields(Map<String, Object> properties) {
        Object updatesSchema = properties.get(UPDATES);
        if (!(updatesSchema instanceof Map)) {
            return List.of();
        }
        Object nested = ((Map<String, Object>) updatesSchema).get("properties");
        if (!(nested instanceof Map)) {
            return List.of();
        }
        return new ArrayList<>(((Map<String, Object>) nested).keySet());
    }

    /**
     * Builds a payload the caller can send as-is.
     *
     * <p>The identifying arguments are reused verbatim when they were supplied, so the example is
     * about the call that just failed rather than about update tools in general. Fields the caller
     * misplaced are moved inside the wrapper with the values they already carried; when nothing
     * was misplaced the wrapper shows one accepted field as a placeholder, because an example with
     * an empty wrapper would only raise the next error.</p>
     */
    private static String example(Map<String, Object> properties, List<String> required,
                                  Map<String, Object> args, List<String> misplaced, List<String> accepted) {
        Map<String, Object> payload = new LinkedHashMap<>();

        for (String name : required) {
            if (UPDATES.equals(name)) {
                continue;
            }
            payload.put(name, args.containsKey(name) ? args.get(name) : placeholder(properties.get(name)));
        }

        Map<String, Object> wrapper = new LinkedHashMap<>();
        if (misplaced.isEmpty()) {
            if (!accepted.isEmpty()) {
                wrapper.put(accepted.get(0), "…");
            }
        } else {
            for (String name : misplaced) {
                wrapper.put(name, args.get(name));
            }
        }
        payload.put(UPDATES, wrapper);

        try {
            return OBJECT_MAPPER.writeValueAsString(payload);
        } catch (JsonProcessingException e) {
            // The example is a convenience; losing it must never cost the caller the diagnosis.
            return "{\"" + UPDATES + "\": { … }}";
        }
    }

    /**
     * A stand-in value of the right JSON type for a field the caller did not supply.
     */
    @SuppressWarnings("unchecked")
    private static Object placeholder(Object fieldSchema) {
        String type = fieldSchema instanceof Map
                ? String.valueOf(((Map<String, Object>) fieldSchema).getOrDefault("type", "string"))
                : "string";
        return switch (type) {
            case "integer", "number" -> 0;
            case "boolean" -> false;
            case "array" -> List.of();
            case "object" -> Map.of();
            default -> "…";
        };
    }

    /**
     * Names the JSON type of what was received, so "you sent a string" reads as a fact rather than
     * as a guess.
     */
    private static String describeType(Object value) {
        if (value instanceof String) return "a string";
        if (value instanceof Boolean) return "a boolean";
        if (value instanceof Number) return "a number";
        if (value instanceof List) return "an array";
        return "a " + value.getClass().getSimpleName();
    }

    /**
     * Renders the accepted field names, truncated when the tool has many so the message stays
     * readable — the example below it is what the caller acts on.
     */
    private static String summarise(List<String> accepted) {
        if (accepted.isEmpty()) {
            return "see the tool's input schema";
        }
        if (accepted.size() <= MAX_FIELDS_LISTED) {
            return String.join(", ", accepted);
        }
        return String.join(", ", accepted.subList(0, MAX_FIELDS_LISTED))
                + " and " + (accepted.size() - MAX_FIELDS_LISTED) + " more";
    }

    private static List<String> quoted(List<String> names) {
        return names.stream().map(name -> "'" + name + "'").toList();
    }
}
