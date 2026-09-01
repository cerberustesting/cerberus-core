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
import org.cerberus.core.engine.queuemanagement.IExecutionThreadPoolService;
import org.cerberus.core.exception.CerberusException;
import org.cerberus.core.mcp.MCPTool;
import org.cerberus.core.mcp.util.MCPLogUtils;
import org.cerberus.core.mcp.util.MCPToolUtils;
import org.cerberus.core.util.answer.AnswerItem;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

/**
 * MCP tool that resumes a paused tag, under the tool name {@code cerberus_tag_resume}.
 *
 * <p>A tag, not a campaign, is what groups executions together — {@code cerberus_campaign_execution_create}
 * is one way to produce one, but a manual multi-testcase run does too. This tool operates purely on
 * the tag, matching Cerberus's own model.</p>
 *
 * <p>Delegates to {@link ITagService#resumeAllExecutions(String, String)}, the same service the
 * {@code POST /campaignexecutions/{executionId}/resume} private REST endpoint calls (that path
 * segment, despite its name, is a tag string): it strips the {@code _PAUSED} suffix off any
 * {@code QUEUED_PAUSED}/{@code QUWITHDEP_PAUSED} entry for the tag. Only makes sense after
 * {@code cerberus_tag_pause} — if nothing is paused, zero entries are affected.</p>
 *
 * <p>The REST endpoint kicks the queue runner after a successful resume
 * ({@code IExecutionThreadPoolService#executeNextInQueueAsynchroneously}); this tool does the same
 * so the resumed entries actually start rather than waiting for an unrelated trigger.</p>
 */
@Component
public class ResumeTagTool implements MCPTool {

    private static final String TOOL_NAME = "cerberus_tag_resume";

    private final ITagService tagService;
    private final IExecutionThreadPoolService executionThreadPoolService;
    private final MCPLogUtils mcpLogUtils;

    public ResumeTagTool(ITagService tagService, IExecutionThreadPoolService executionThreadPoolService, MCPLogUtils mcpLogUtils) {
        this.tagService = tagService;
        this.executionThreadPoolService = executionThreadPoolService;
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
                "tag", Map.of("type", "string", "description", "Tag to resume, as returned by cerberus_campaign_execution_create or cerberus_testcase_execution_create."),
                "user", Map.of("type", "string", "description", "Optional user name recorded on the resumed entries. Defaults to 'MCP'.")
        );

        return new McpSchema.Tool(
                TOOL_NAME,
                null,
                """
                Resumes a run previously paused with cerberus_tag_pause, letting its queued
                testcases start again.

                Call this tool whenever the user asks to resume, unpause, or continue a paused run.
                Has no effect if nothing is currently paused for the tag.
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
                MCPToolUtils.updateAnnotations("Resume tag executions", false),
                null
        );
    }

    private McpSchema.CallToolResult execute(Map<String, Object> args) {
        String tag = MCPToolUtils.getString(args, "tag", "").trim();
        String user = MCPToolUtils.getString(args, "user", "MCP");

        mcpLogUtils.call(TOOL_NAME, "tag_resume",
                String.format("MCP tool %s called with tag=%s", TOOL_NAME, tag));

        if (tag.isBlank()) {
            return MCPToolUtils.errorText("Missing required parameter: tag");
        }

        AnswerItem<Integer> answer;
        try {
            answer = tagService.resumeAllExecutions(tag, user);
        } catch (CerberusException e) {
            return MCPToolUtils.errorText("Unable to resume executions for tag " + tag + ": " + e.getMessage());
        }

        int resumed = answer.getItem() == null ? 0 : answer.getItem();

        if (resumed > 0) {
            try {
                executionThreadPoolService.executeNextInQueueAsynchroneously(false);
            } catch (CerberusException e) {
                // The entries are already resumed in the queue; the periodic queue runner will
                // eventually pick them up even if this immediate kick fails.
                mcpLogUtils.warning(TOOL_NAME, "tag_resume",
                        "Resumed " + resumed + " entries for tag " + tag + " but failed to trigger immediate execution: " + e.getMessage());
            }
        }

        return MCPToolUtils.successJson(Map.of(
                "tag", tag,
                "resumedCount", resumed,
                "message", resumed > 0
                        ? resumed + " queue entry(ies) resumed."
                        : "No queue entries were resumed. No paused entries were found for this tag."
        ));
    }
}