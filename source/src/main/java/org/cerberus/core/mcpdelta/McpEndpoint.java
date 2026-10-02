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
package org.cerberus.core.mcpdelta;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.cerberus.core.mcpdelta.tools.Tool;
import org.cerberus.core.mcpdelta.tools.Tools;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * The MCP endpoint, independent of any HTTP server: one exchange of the Streamable HTTP transport (JSON-RPC,
 * plain JSON answers, no SSE needed since every call answers once) in, one response out. The standalone server
 * and the Cerberus webapp servlet both hand their requests to it. Deliberately small: the protocol is a door,
 * the value is behind it.
 */
public final class McpEndpoint {

    /** One HTTP answer. */
    public record Response(int status, String contentType, String body, Map<String, String> headers) {
    }

    /** What one tool call was, for an audit log: never its arguments, only their size. */
    public record CallRecord(String tool, String user, int argChars, int resultChars, boolean error, long millis) {
    }

    public static final String VERSION = "0.1.0-beta";
    private static final List<String> PROTOCOLS = List.of("2026-07-28", "2025-11-25", "2025-06-18", "2025-03-26", "2024-11-05");
    private static final ObjectMapper JSON = new ObjectMapper();

    private final Context ctx;
    private final Map<String, Tool> tools = new LinkedHashMap<>();
    private final Path callLog;

    private java.util.function.Consumer<CallRecord> listener = r -> { };

    public McpEndpoint(Context ctx) {
        this.ctx = ctx;
        for (Tool t : Tools.all(ctx)) {
            tools.put(t.name(), t);
        }
        this.callLog = Path.of(ctx.config.home, "calls.log");
    }

    /** Called after every tool call (the host's audit log). */
    public void onCall(java.util.function.Consumer<CallRecord> listener) {
        this.listener = listener;
    }

    public int toolCount() {
        return tools.size();
    }

    /**
     * @param httpMethod the HTTP method
     * @param body       the request body (POST)
     * @param header     request header by name (case-insensitive), null when absent
     */
    public Response handle(String httpMethod, String body, java.util.function.Function<String, String> header) {
        try {
            return switch (httpMethod) {
                case "POST" -> post(body, header);
                case "DELETE" -> new Response(200, "text/plain", "", Map.of());
                case "OPTIONS" -> new Response(204, "text/plain", "", Map.of());
                default -> new Response(405, "text/plain", "this server answers on POST; it has no event stream", Map.of("Allow", "POST, DELETE"));
            };
        } catch (RuntimeException e) {
            return new Response(500, "text/plain", e.toString(), Map.of());
        }
    }

    private Response post(String body, java.util.function.Function<String, String> header) {
        JsonNode msg;
        try {
            msg = JSON.readTree(body == null ? "" : body);
        } catch (IOException e) {
            return new Response(400, "application/json", error(null, -32700, "parse error").toString(), Map.of());
        }
        if (msg == null || !msg.isObject()) {
            return new Response(400, "application/json", error(null, -32600, "expected one JSON-RPC message").toString(), Map.of());
        }
        JsonNode id = msg.get("id");
        String method = msg.path("method").asText("");
        if (id == null || id.isNull()) {
            // Notifications (initialized, cancelled...) and client responses need no answer.
            return new Response(202, "text/plain", "", Map.of());
        }
        Map<String, String> headers = new LinkedHashMap<>();
        ObjectNode result;
        org.cerberus.core.mcpdelta.util.CallContext.set(header.apply("Mcp-Session-Id"));
        try {
            result = switch (method) {
                case "initialize" -> initialize(msg.path("params"), headers);
                case "ping" -> JSON.createObjectNode();
                case "tools/list" -> toolsList();
                case "tools/call" -> toolsCall(msg.path("params"));
                case "resources/list" -> JSON.createObjectNode().set("resources", JSON.createArrayNode());
                case "resources/templates/list" -> JSON.createObjectNode().set("resourceTemplates", JSON.createArrayNode());
                case "prompts/list" -> JSON.createObjectNode().set("prompts", JSON.createArrayNode());
                default -> null;
            };
        } catch (RuntimeException e) {
            return new Response(200, "application/json", error(id, -32603, e.getMessage()).toString(), headers);
        } finally {
            org.cerberus.core.mcpdelta.util.CallContext.set(null);
        }
        if (result == null) {
            return new Response(200, "application/json", error(id, -32601, "method not found: " + method).toString(), headers);
        }
        ObjectNode response = JSON.createObjectNode();
        response.put("jsonrpc", "2.0");
        response.set("id", id);
        response.set("result", result);
        try {
            return new Response(200, "application/json", JSON.writeValueAsString(response), headers);
        } catch (IOException e) {
            return new Response(500, "text/plain", e.toString(), headers);
        }
    }

    private ObjectNode initialize(JsonNode params, Map<String, String> headers) {
        String asked = params.path("protocolVersion").asText("");
        ObjectNode r = JSON.createObjectNode();
        r.put("protocolVersion", PROTOCOLS.contains(asked) ? asked : PROTOCOLS.get(1));
        ObjectNode caps = r.putObject("capabilities");
        caps.putObject("tools").put("listChanged", false);
        ObjectNode info = r.putObject("serverInfo");
        info.put("name", "cerberus-delta");
        info.put("title", "MCP Delta for Cerberus");
        info.put("version", VERSION);
        r.put("instructions", Guide.INSTRUCTIONS);
        headers.put("Mcp-Session-Id", UUID.randomUUID().toString());
        return r;
    }

    private ObjectNode toolsList() {
        ObjectNode r = JSON.createObjectNode();
        ArrayNode list = r.putArray("tools");
        for (Tool t : tools.values()) {
            ObjectNode n = list.addObject();
            n.put("name", t.name());
            n.put("description", t.description());
            n.set("inputSchema", JSON.valueToTree(t.inputSchema()));
            if (t.readOnly()) {
                n.putObject("annotations").put("readOnlyHint", true);
            }
        }
        return r;
    }

    private ObjectNode toolsCall(JsonNode params) {
        String name = params.path("name").asText("");
        Tool tool = tools.get(name);
        ObjectNode r = JSON.createObjectNode();
        ArrayNode content = r.putArray("content");
        String text;
        boolean isError = false;
        long t0 = System.nanoTime();
        if (tool == null) {
            text = "unknown tool " + name + "; tools: " + String.join(", ", tools.keySet());
            isError = true;
        } else {
            try {
                text = tool.call(params.path("arguments"));
            } catch (Tool.ToolError e) {
                text = e.getMessage();
                isError = true;
            } catch (org.cerberus.core.mcpdelta.doc.DocException e) {
                text = "nothing written:\n" + e.getMessage();
                isError = true;
            } catch (RuntimeException | LinkageError e) {
                text = "failed: " + e;
                isError = true;
            }
        }
        content.addObject().put("type", "text").put("text", text);
        r.put("isError", isError);
        long ms = (System.nanoTime() - t0) / 1_000_000;
        log(name, params.path("arguments").toString().length(), text.length(), isError, ms);
        try {
            listener.accept(new CallRecord(name, org.cerberus.core.mcpdelta.util.CallContext.user(), params.path("arguments").toString().length(),
                    text.length(), isError, ms));
        } catch (RuntimeException ignored) {
            // an audit log must never break a call
        }
        return r;
    }

    private void log(String tool, int argChars, int resultChars, boolean error, long ms) {
        try {
            Files.createDirectories(callLog.getParent());
            Files.writeString(callLog, Instant.now() + "\t" + tool + "\tuser=" + org.cerberus.core.mcpdelta.util.CallContext.user() + "\targs=" + argChars + "\tresult=" + resultChars
                    + "\t" + (error ? "error" : "ok") + "\t" + ms + "ms\n", StandardCharsets.UTF_8,
                    StandardOpenOption.CREATE, StandardOpenOption.APPEND);
        } catch (IOException ignored) {
            // logging must never break a call
        }
    }

    private static ObjectNode error(JsonNode id, int code, String message) {
        ObjectNode e = JSON.createObjectNode();
        e.put("jsonrpc", "2.0");
        e.set("id", id == null ? JSON.nullNode() : id);
        e.putObject("error").put("code", code).put("message", message == null ? "error" : message);
        return e;
    }
}
