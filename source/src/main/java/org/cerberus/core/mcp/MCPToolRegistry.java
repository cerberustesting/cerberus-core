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
package org.cerberus.core.mcp;

import io.modelcontextprotocol.server.McpServerFeatures;
import io.modelcontextprotocol.spec.McpSchema;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.cerberus.core.mcp.util.MCPLogUtils;
import org.cerberus.core.mcp.util.MCPToolUtils;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Collects every {@link MCPTool} bean and exposes their specifications to the MCP server.
 *
 * <p>Every handler is wrapped by {@link #guard} before registration, so an unexpected runtime
 * failure inside a tool becomes a normal MCP error result instead of escaping the handler.</p>
 */
@Component
public class MCPToolRegistry {

    private static final Logger LOG = LogManager.getLogger(MCPToolRegistry.class);

    private final List<MCPTool> tools;
    private final MCPLogUtils mcpLogUtils;

    public MCPToolRegistry(List<MCPTool> tools, MCPLogUtils mcpLogUtils) {
        this.tools = tools;
        this.mcpLogUtils = mcpLogUtils;
    }

    /**
     * Returns every registered tool specification, each with its handler wrapped by {@link #guard}.
     */
    public List<McpServerFeatures.SyncToolSpecification> listTools() {
        return tools.stream()
                .map(MCPTool::toToolSpecification)
                .map(this::guard)
                .collect(Collectors.toList());
    }

    /**
     * Wraps a tool handler so that an escaping exception is turned into an MCP error result.
     *
     * <p>Tool handlers validate their own inputs and return {@code errorText} for expected
     * failures, but nothing stops an unexpected one — a null field the entity never populated, a
     * DAO that lets a {@link NullPointerException} through, a malformed row. Unwrapped, such a
     * failure escapes the handler and surfaces as a bare JSON-RPC internal error: the calling
     * agent is told "the call failed" with no indication of which tool or why, and no audit entry
     * is written at all.</p>
     *
     * <p>Wrapping centrally rather than in each tool means the guarantee holds for every tool
     * already registered and for every tool added later, without relying on each author to
     * remember it.</p>
     *
     * <p>The message returned to the caller names the exception type and message but no stack
     * trace: the trace goes to the server log, where it belongs, while the agent gets enough to
     * decide whether to retry with different arguments or to report the problem.</p>
     *
     * @param specification the specification produced by a tool.
     * @return the same specification with a fail-safe handler.
     */
    private McpServerFeatures.SyncToolSpecification guard(McpServerFeatures.SyncToolSpecification specification) {
        McpSchema.Tool tool = specification.tool();
        String toolName = tool == null ? "unknown" : tool.name();

        return new McpServerFeatures.SyncToolSpecification(
                tool,
                (exchange, request) -> {
                    try {
                        return specification.callHandler().apply(exchange, request);
                    } catch (Exception | StackOverflowError e) {
                        // StackOverflowError is caught alongside Exception because the recursive
                        // step-library resolution can hit it on a self-referencing testcase, and
                        // that is a data problem the caller can act on, not a dead JVM.
                        LOG.error("MCP tool '{}' failed unexpectedly.", toolName, e);
                        mcpLogUtils.error(toolName, "tool_failure",
                                String.format("MCP tool %s failed unexpectedly : %s", toolName, describe(e)));
                        return MCPToolUtils.errorText(
                                "The Cerberus tool '" + toolName + "' failed unexpectedly: " + describe(e)
                                        + ". This is a server-side fault, not a problem with your arguments; "
                                        + "the full stack trace is in the Cerberus log.");
                    }
                }
        );
    }

    /**
     * Renders a throwable as a single readable line.
     *
     * <p>A bare {@link NullPointerException} has a null message, which would otherwise produce
     * "failed unexpectedly: null" — the least useful diagnostic possible. Falling back to the
     * class name keeps the report meaningful in exactly the case that needs it most.</p>
     */
    private String describe(Throwable throwable) {
        String message = throwable.getMessage();
        if (message == null || message.isBlank()) {
            return throwable.getClass().getSimpleName();
        }
        return throwable.getClass().getSimpleName() + " - " + message;
    }
}
