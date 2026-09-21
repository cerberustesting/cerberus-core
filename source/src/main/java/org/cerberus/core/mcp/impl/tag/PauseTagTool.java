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
package org.cerberus.core.mcp.impl.tag;

import io.modelcontextprotocol.server.McpServerFeatures;
import io.modelcontextprotocol.spec.McpSchema;
import org.cerberus.core.crud.service.ITagService;
import org.cerberus.core.exception.CerberusException;
import org.cerberus.core.mcp.MCPTool;
import org.cerberus.core.mcp.util.MCPLogUtils;
import org.cerberus.core.mcp.util.MCPToolUtils;
import org.cerberus.core.util.answer.AnswerItem;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

/**
 * MCP tool that pauses the not-yet-started executions grouped under a tag, under the tool name
 * {@code cerberus_tag_pause}.
 *
 * <p>A tag, not a campaign, is what groups executions together — {@code cerberus_campaign_execution_create}
 * is one way to produce one, but a manual multi-testcase run does too. This tool operates purely on
 * the tag, matching Cerberus's own model.</p>
 *
 * <p>Delegates to {@link ITagService#pauseAllExecutions(String, String)}, the same service the
 * {@code POST /campaignexecutions/{executionId}/pause} private REST endpoint calls (that path
 * segment, despite its name, is a tag string). Only entries still in {@code QUEUED} or
 * {@code QUWITHDEP} are moved to their {@code *_PAUSED} counterpart — whatever is already
 * {@code EXECUTING}/{@code STARTING}/{@code WAITING} keeps running, and nothing new starts until
 * {@code cerberus_tag_resume} is called.</p>
 */
@Component
public class PauseTagTool implements MCPTool {

    private static final String TOOL_NAME = "cerberus_tag_pause";

    private final ITagService tagService;
    private final MCPLogUtils mcpLogUtils;

    public PauseTagTool(ITagService tagService, MCPLogUtils mcpLogUtils) {
        this.tagService = tagService;
        this.mcpLogUtils = mcpLogUtils;
    }

    @Override
    public McpServerFeatures.SyncToolSpecification toToolSpecification() {
        return new McpServerFeatures.SyncToolSpecification(
                createTool(),
                (exchange, request) -> {
                    Map<String, Object> args = MCPToolUtils.argumentsOrEmpty(request.arguments());
                    return execute(args);
                }
        );
    }

    private McpSchema.Tool createTool() {
        Map<String, Object> properties = Map.of(
                "tag", Map.of("type", "string", "description", "Tag to pause, as returned by cerberus_campaign_execution_create or cerberus_testcase_execution_create."),
                "user", Map.of("type", "string", "description", "Optional user name recorded on the paused entries. Defaults to 'MCP'.")
        );

        return new McpSchema.Tool(
                TOOL_NAME,
                null,
                """
                Pauses the testcases under a tag that have not started yet, letting whatever is
                already executing finish normally, without cancelling anything.

                Call this tool whenever the user asks to pause or hold a run temporarily — a campaign
                run, or any tag grouping several testcase executions.
                Use cerberus_tag_resume to let it continue, or cerberus_tag_cancel to stop it for good.
                """,
                new McpSchema.JsonSchema(
                        "object",
                        properties,
                        List.of("tag"),
                        null,
                        null,
                        null
                ),
                null,
                MCPToolUtils.updateAnnotations("Pause tag executions", false),
                null
        );
    }

    private McpSchema.CallToolResult execute(Map<String, Object> args) {
        String tag = MCPToolUtils.getString(args, "tag", "").trim();
        String user = MCPToolUtils.getString(args, "user", "MCP");

        mcpLogUtils.call(TOOL_NAME, "tag_pause",
                String.format("MCP tool %s called with tag=%s", TOOL_NAME, tag));

        if (tag.isBlank()) {
            return MCPToolUtils.errorText("Missing required parameter: tag");
        }

        AnswerItem<Integer> answer;
        try {
            answer = tagService.pauseAllExecutions(tag, user);
        } catch (CerberusException e) {
            return MCPToolUtils.errorText("Unable to pause executions for tag " + tag + ": " + e.getMessage());
        }

        int paused = answer.getItem() == null ? 0 : answer.getItem();

        return MCPToolUtils.successJson(Map.of(
                "tag", tag,
                "pausedCount", paused,
                "message", paused > 0
                        ? paused + " queue entry(ies) paused."
                        : "No queue entries were paused. They were probably already running, finished, or the tag has no pending entries."
        ));
    }
}