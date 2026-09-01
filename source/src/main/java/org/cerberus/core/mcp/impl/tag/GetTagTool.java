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
import org.cerberus.core.crud.entity.Tag;
import org.cerberus.core.crud.entity.TestCaseExecution;
import org.cerberus.core.crud.service.ITagService;
import org.cerberus.core.crud.service.ITestCaseExecutionService;
import org.cerberus.core.exception.CerberusException;
import org.cerberus.core.mcp.MCPTool;
import org.cerberus.core.mcp.util.MCPLogUtils;
import org.cerberus.core.mcp.util.MCPToolUtils;
import org.cerberus.core.util.answer.AnswerItem;
import org.springframework.stereotype.Component;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * MCP tool that returns the aggregated result of a tag, under the tool name
 * {@code cerberus_tag_get}.
 *
 * <p>A tag groups a set of testcase executions launched together — most commonly a campaign run
 * ({@code cerberus_campaign_execution_create} returns one), but also a manual multi-testcase run
 * or a single relaunch. {@link Tag} is not just an identifier: Cerberus keeps its OK/KO/FA/...
 * counters and its CI score live-updated as executions finish
 * ({@code TagService.manageCampaignEndOfExecution}), so reading it is the cheap, authoritative way
 * to ask "did this run pass" — no manual counting required.</p>
 *
 * <p>This is deliberately not the same view as {@code cerberus_testcase_execution_list}: that tool
 * returns every execution row under a tag, including retries, which over-counts a testcase that
 * ran more than once. This tool instead exposes {@link Tag}'s own counters, and, when
 * {@code includeExecutions} is requested, the one-row-per-testcase view from
 * {@link ITestCaseExecutionService#readLastExecutionAndExecutionInQueueByTag(String)} — the same
 * de-duplication (last execution per test/testcase/country/environment/robot, with a
 * {@code flaky} flag when an earlier attempt failed) used by the campaign execution report.</p>
 */
@Component
public class GetTagTool implements MCPTool {

    private static final String TOOL_NAME = "cerberus_tag_get";

    private static final int MAX_TEXT_LENGTH = 2000;

    private final ITagService tagService;
    private final ITestCaseExecutionService testCaseExecutionService;
    private final MCPLogUtils mcpLogUtils;

    public GetTagTool(ITagService tagService, ITestCaseExecutionService testCaseExecutionService, MCPLogUtils mcpLogUtils) {
        this.tagService = tagService;
        this.testCaseExecutionService = testCaseExecutionService;
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
        Map<String, Object> properties = new LinkedHashMap<>();
        properties.put("tag", Map.of(
                "type", "string",
                "description", "Tag to look up — returned by cerberus_campaign_execution_create or cerberus_testcase_execution_create."
        ));
        properties.put("includeExecutions", Map.of(
                "type", "boolean",
                "description", "When true, also returns one row per testcase (deduplicated across retries, with a "
                        + "flaky flag) instead of just the aggregated counters. Defaults to false."
        ));

        return new McpSchema.Tool(
                TOOL_NAME,
                null,
                """
                Returns the aggregated result of a tag: how many executions passed, failed, are
                still running or queued, and the resulting CI score.

                Call this after cerberus_campaign_execution_create (or cerberus_testcase_execution_create
                with several testcases) to check whether the run passed overall. Prefer this tool over
                counting statuses yourself from cerberus_testcase_execution_list: a retried testcase
                produces several execution rows there, which this tool's counters and flaky detection
                already account for.

                The run is not finished while pending > 0 (still queued or running). Set includeExecutions
                to true to see which specific testcase failed, is flaky, or is still pending — then use
                cerberus_testcase_execution_get on its executionId for the full detail.

                If the tag does not exist yet, nothing has been queued under it (a run can return a tag
                with nbExecutions = 0 when no valid country/environment/robot/testcase combination matched).
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
                MCPToolUtils.readOnlyAnnotations("Get tag result", false),
                null
        );
    }

    private McpSchema.CallToolResult execute(Map<String, Object> args) {
        String tag = MCPToolUtils.getString(args, "tag", "").trim();
        boolean includeExecutions = MCPToolUtils.getBoolean(args, "includeExecutions", false);

        mcpLogUtils.call(TOOL_NAME, "tag_get",
                String.format("MCP tool %s called with tag=%s includeExecutions=%s", TOOL_NAME, tag, includeExecutions));

        if (tag.isBlank()) {
            return MCPToolUtils.errorText("Missing required parameter: tag");
        }

        AnswerItem<Tag> answer = tagService.readByKey(tag);
        if (!answer.isCodeStringEquals("OK") || answer.getItem() == null) {
            return MCPToolUtils.errorText("Tag does not exist: " + tag
                    + ". Nothing has been queued under it yet, or it was misspelled.");
        }

        Map<String, Object> result = toSummary(answer.getItem());

        if (includeExecutions) {
            try {
                List<TestCaseExecution> executions = testCaseExecutionService.readLastExecutionAndExecutionInQueueByTag(tag);
                result.put("executions", executions.stream().map(this::toExecutionSummary).toList());
            } catch (ParseException | CerberusException e) {
                result.put("executionsError", "Unable to read the per-testcase executions: " + e.getMessage());
            }
        }

        return MCPToolUtils.successJson(result);
    }

    private Map<String, Object> toSummary(Tag tag) {
        long pending = (long) tag.getNbPE() + tag.getNbQU() + tag.getNbQE() + tag.getNbPA();

        Map<String, Object> map = new LinkedHashMap<>();
        map.put("tag", MCPToolUtils.nullSafe(tag.getTag()));
        map.put("campaign", MCPToolUtils.nullSafe(tag.getCampaign()));
        map.put("description", MCPToolUtils.nullSafe(tag.getDescription()));
        map.put("pending", pending);
        map.put("runFinished", pending == 0);
        map.put("nbExecutions", tag.getNbExe());
        map.put("nbExecutionsUsefull", tag.getNbExeUsefull());
        map.put("nbOK", tag.getNbOK());
        map.put("nbKO", tag.getNbKO());
        map.put("nbFA", tag.getNbFA());
        map.put("nbNA", tag.getNbNA());
        map.put("nbNE", tag.getNbNE());
        map.put("nbWE", tag.getNbWE());
        map.put("nbPE", tag.getNbPE());
        map.put("nbQU", tag.getNbQU());
        map.put("nbPA", tag.getNbPA());
        map.put("nbQE", tag.getNbQE());
        map.put("nbCA", tag.getNbCA());
        map.put("nbFlaky", tag.getNbFlaky());
        map.put("nbMuted", tag.getNbMuted());
        map.put("ciScore", tag.getCiScore());
        map.put("ciScoreThreshold", tag.getCiScoreThreshold());
        map.put("ciScoreMax", tag.getCiScoreMax());
        map.put("ciResult", MCPToolUtils.nullSafe(tag.getCiResult()));
        map.put("falseNegative", tag.isFalseNegative());
        map.put("countryList", MCPToolUtils.nullSafe(tag.getCountryList()));
        map.put("environmentList", MCPToolUtils.nullSafe(tag.getEnvironmentList()));
        map.put("robotList", MCPToolUtils.nullSafe(tag.getRobotDecliList()));
        map.put("systemList", MCPToolUtils.nullSafe(tag.getSystemList()));
        map.put("applicationList", MCPToolUtils.nullSafe(tag.getApplicationList()));
        map.put("dateStartExe", tag.getDateStartExe());
        map.put("dateEndQueue", tag.getDateEndQueue());
        map.put("durationMs", tag.getDurationMs());
        return map;
    }

    private Map<String, Object> toExecutionSummary(TestCaseExecution execution) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("executionId", execution.getId());
        map.put("testFolder", MCPToolUtils.nullSafe(execution.getTest()));
        map.put("testcase", MCPToolUtils.nullSafe(execution.getTestCase()));
        map.put("country", MCPToolUtils.nullSafe(execution.getCountry()));
        map.put("environment", MCPToolUtils.nullSafe(execution.getEnvironment()));
        map.put("robot", MCPToolUtils.nullSafe(execution.getRobot()));
        map.put("controlStatus", MCPToolUtils.nullSafe(execution.getControlStatus()));
        map.put("controlMessage", truncate(execution.getControlMessage()));
        // >1 means this testcase needed a retry to reach its final status under this tag.
        map.put("nbExecutions", execution.getNbExecutions());
        map.put("flaky", execution.isFlaky());
        if (execution.getQueueID() > 0) {
            map.put("queueId", execution.getQueueID());
            map.put("queueState", MCPToolUtils.nullSafe(execution.getQueueState()));
        }
        map.put("start", execution.getStart());
        map.put("end", execution.getEnd());
        return map;
    }

    private String truncate(String value) {
        String safe = MCPToolUtils.nullSafe(value);
        if (safe.length() <= MAX_TEXT_LENGTH) {
            return safe;
        }
        return safe.substring(0, MAX_TEXT_LENGTH) + "… [truncated, " + safe.length() + " characters total]";
    }

}