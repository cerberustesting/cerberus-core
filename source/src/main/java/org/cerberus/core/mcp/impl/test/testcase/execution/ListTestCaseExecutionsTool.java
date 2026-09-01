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
package org.cerberus.core.mcp.impl.test.testcase.execution;

import io.modelcontextprotocol.server.McpServerFeatures;
import io.modelcontextprotocol.spec.McpSchema;
import org.cerberus.core.crud.entity.TestCaseExecution;
import org.cerberus.core.crud.entity.TestCaseExecutionQueue;
import org.cerberus.core.crud.service.ITestCaseExecutionQueueService;
import org.cerberus.core.crud.service.ITestCaseExecutionService;
import org.cerberus.core.exception.CerberusException;
import org.cerberus.core.mcp.MCPTool;
import org.cerberus.core.mcp.util.MCPLogUtils;
import org.cerberus.core.mcp.util.MCPToolUtils;
import org.cerberus.core.util.answer.AnswerList;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * MCP tool that lists testcase executions, under the tool name
 * {@code cerberus_testcase_execution_list}.
 *
 * <p>This tool is the bridge between queuing a run and reading its result: it turns the tag
 * returned by {@code cerberus_testcase_execution_create} into execution ids usable with
 * {@code cerberus_testcase_execution_get}.</p>
 *
 * <p>It deliberately reports <em>two</em> sources at once:</p>
 * <ul>
 *   <li>the {@code testcaseexecution} rows — runs that have started, each with a result;</li>
 *   <li>the {@code testcaseexecutionqueue} rows — entries still queued, starting, running,
 *       cancelled or in error, which have no execution row yet.</li>
 * </ul>
 *
 * <p>Reading only the first source is what makes a freshly-launched run look like it never
 * happened: for the first seconds — or for as long as no robot is free — everything the caller
 * asked for lives exclusively in the queue. Merging both is what lets an agent answer "is it
 * done yet?" rather than "there is nothing there".</p>
 *
 * <p>Delegation: {@link ITestCaseExecutionService} and {@link ITestCaseExecutionQueueService}.</p>
 */
@Component
public class ListTestCaseExecutionsTool implements MCPTool {

    private static final String TOOL_NAME = "cerberus_testcase_execution_list";

    /**
     * Queue states that mean "this entry has not produced a final result yet". Used to decide
     * whether the caller should poll again rather than conclude the run is over.
     */
    private static final List<String> PENDING_QUEUE_STATES = List.of(
            TestCaseExecutionQueue.State.QUTEMP.name(),
            TestCaseExecutionQueue.State.QUWITHDEP.name(),
            TestCaseExecutionQueue.State.QUWITHDEP_PAUSED.name(),
            TestCaseExecutionQueue.State.QUEUED.name(),
            TestCaseExecutionQueue.State.QUEUED_PAUSED.name(),
            TestCaseExecutionQueue.State.WAITING.name(),
            TestCaseExecutionQueue.State.STARTING.name(),
            TestCaseExecutionQueue.State.EXECUTING.name()
    );

    /** Hard ceiling on returned executions, so a broad tag cannot flood the agent's context. */
    private static final int MAX_RESULTS = 200;

    private static final int DEFAULT_LIMIT = 50;

    private final ITestCaseExecutionService testCaseExecutionService;
    private final ITestCaseExecutionQueueService testCaseExecutionQueueService;
    private final MCPLogUtils mcpLogUtils;

    public ListTestCaseExecutionsTool(ITestCaseExecutionService testCaseExecutionService,
                                      ITestCaseExecutionQueueService testCaseExecutionQueueService,
                                      MCPLogUtils mcpLogUtils) {
        this.testCaseExecutionService = testCaseExecutionService;
        this.testCaseExecutionQueueService = testCaseExecutionQueueService;
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

    /**
     * Builds the MCP tool descriptor.
     *
     * <p>Either {@code tag} or the ({@code testFolder}, {@code testcase}) pair must be supplied;
     * neither is marked required in the schema because either one alone is a valid query, and a
     * JSON schema cannot express that choice. The constraint is enforced in {@link #execute(Map)}
     * with an explicit message.</p>
     *
     * @return the fully-described {@link McpSchema.Tool} for registration with the MCP server.
     */
    private McpSchema.Tool createTool() {
        Map<String, Object> properties = new LinkedHashMap<>();
        properties.put("tag", Map.of(
                "type", "string",
                "description", "Execution tag to look up — the tag returned by cerberus_testcase_execution_create. "
                        + "Returns every execution and queue entry launched under it."
        ));
        properties.put("testFolder", Map.of(
                "type", "string",
                "description", "Test folder of the testcase to look up. Use together with testcase, instead of tag, "
                        + "to find the most recent runs of one testcase."
        ));
        properties.put("testcase", Map.of(
                "type", "string",
                "description", "Testcase identifier to look up. Requires testFolder."
        ));
        properties.put("country", Map.of(
                "type", "string",
                "description", "Optional country filter (e.g. FR, NET)."
        ));
        properties.put("environment", Map.of(
                "type", "string",
                "description", "Optional environment filter (e.g. QA, INT, PROD)."
        ));
        properties.put("controlStatus", Map.of(
                "type", "string",
                "description", "Optional status filter. OK keeps only passing runs, KO only failing ones."
        ));
        properties.put("limit", Map.of(
                "type", "integer",
                "description", "Maximum number of executions to return. Defaults to " + DEFAULT_LIMIT
                        + ", capped at " + MAX_RESULTS + "."
        ));

        return new McpSchema.Tool(
                TOOL_NAME,
                null,
                """
                Lists testcase executions and pending queue entries, so you can find out what a run did.

                Call this after cerberus_testcase_execution_create with the tag it returned, to check whether the
                run has finished and to obtain the execution ids. Then call cerberus_testcase_execution_get on an
                id to see why a given execution failed.

                Query either by tag, or by testFolder + testcase to get the latest runs of one testcase.

                The response reports queued and running entries alongside finished ones. When "pending" is greater
                than zero the run is not over: wait a few seconds and call this tool again rather than concluding
                that the test did not run. Do not try to poll in a tight loop — a real test takes tens of seconds.
                """,
                new McpSchema.JsonSchema(
                        "object",
                        properties,
                        null,
                        null,
                        null,
                        null
                ),
                null,
                MCPToolUtils.readOnlyAnnotations("List testcase executions", false),
                null
        );
    }

    /**
     * Validates the arguments, gathers executions and queue entries, and returns the merged view.
     *
     * @param args tool arguments extracted from the MCP request (may be empty but never null).
     * @return a {@link McpSchema.CallToolResult} carrying the executions, or an error description.
     */
    private McpSchema.CallToolResult execute(Map<String, Object> args) {
        String tag = MCPToolUtils.getString(args, "tag", "").trim();
        String testFolder = MCPToolUtils.getString(args, "testFolder", "").trim();
        String testcase = MCPToolUtils.getString(args, "testcase", "").trim();
        String country = MCPToolUtils.getString(args, "country", "").trim();
        String environment = MCPToolUtils.getString(args, "environment", "").trim();
        String controlStatus = MCPToolUtils.getString(args, "controlStatus", "").trim();
        int limit = Math.min(Math.max(MCPToolUtils.getInteger(args, "limit", DEFAULT_LIMIT), 1), MAX_RESULTS);

        mcpLogUtils.call(TOOL_NAME, "testcase_execution_list",
                String.format("MCP tool %s called with tag=%s testFolder=%s testcase=%s", TOOL_NAME, tag, testFolder, testcase));

        if (tag.isBlank() && (testFolder.isBlank() || testcase.isBlank())) {
            return MCPToolUtils.errorText(
                    "Provide either tag, or both testFolder and testcase, to identify the executions to list.");
        }

        List<Map<String, Object>> executions = new ArrayList<>();
        List<Map<String, Object>> queueEntries = new ArrayList<>();

        if (!tag.isBlank()) {
            try {
                AnswerList<TestCaseExecution> answer = testCaseExecutionService.readByTag(tag);
                for (TestCaseExecution execution : answer.getDataList()) {
                    if (matchesFilters(execution, testFolder, testcase, country, environment, controlStatus)) {
                        executions.add(toSummary(execution));
                    }
                }
            } catch (CerberusException e) {
                return MCPToolUtils.errorText("Unable to read executions for tag " + tag + ": " + e.getMessage());
            }

            try {
                // stateList null means "every state" : entries that ended in CANCELLED or ERROR
                // never produce an execution row, and they are precisely the ones worth surfacing.
                AnswerList<TestCaseExecutionQueue> queueAnswer =
                        testCaseExecutionQueueService.readByVarious1(tag, null, false);
                for (TestCaseExecutionQueue entry : queueAnswer.getDataList()) {
                    if (matchesQueueFilters(entry, testFolder, testcase, country, environment)) {
                        queueEntries.add(toQueueSummary(entry));
                    }
                }
            } catch (CerberusException e) {
                return MCPToolUtils.errorText("Unable to read the execution queue for tag " + tag + ": " + e.getMessage());
            }
        } else {
            // No tag : return the recent runs of this testcase, newest first.
            //
            // readLastByCriteria is deliberately NOT used here even though its name fits. Its SQL
            // compares country, environment and tag with plain equality, so passing null for the
            // criteria the caller did not supply matches no row at all rather than every row —
            // the tool returned an empty list for a testcase that had executions.
            Map<String, List<String>> criteria = new LinkedHashMap<>();
            criteria.put("exe.test", List.of(testFolder));
            criteria.put("exe.testcase", List.of(testcase));
            if (!country.isBlank()) {
                criteria.put("exe.country", List.of(country));
            }
            if (!environment.isBlank()) {
                criteria.put("exe.environment", List.of(environment));
            }
            if (!controlStatus.isBlank()) {
                criteria.put("exe.controlstatus", List.of(controlStatus));
            }

            try {
                AnswerList<TestCaseExecution> answer = testCaseExecutionService.readByCriteria(
                        0, limit, "exe.id desc", null, criteria, null, null);
                for (TestCaseExecution execution : answer.getDataList()) {
                    executions.add(toSummary(execution));
                }
            } catch (CerberusException e) {
                return MCPToolUtils.errorText("Unable to read executions for testcase "
                        + testFolder + " / " + testcase + ": " + e.getMessage());
            }
        }

        boolean truncated = executions.size() > limit;
        if (truncated) {
            executions = executions.subList(0, limit);
        }

        long pending = queueEntries.stream()
                .filter(entry -> PENDING_QUEUE_STATES.contains(String.valueOf(entry.get("state"))))
                .count();

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("tag", tag);
        response.put("count", executions.size());
        response.put("pending", pending);
        response.put("runFinished", pending == 0);
        if (truncated) {
            response.put("truncated", true);
            response.put("message", "More executions matched than the requested limit of " + limit
                    + ". Narrow the query with country, environment or controlStatus, or raise limit.");
        }
        response.put("executions", executions);
        response.put("queueEntries", queueEntries);

        return MCPToolUtils.successJson(response);
    }

    /**
     * Applies the optional caller filters to a finished execution.
     *
     * @return {@code true} when the execution should be included in the response.
     */
    private boolean matchesFilters(TestCaseExecution execution, String testFolder, String testcase,
                                   String country, String environment, String controlStatus) {
        return matches(execution.getTest(), testFolder)
                && matches(execution.getTestCase(), testcase)
                && matches(execution.getCountry(), country)
                && matches(execution.getEnvironment(), environment)
                && matches(execution.getControlStatus(), controlStatus);
    }

    /**
     * Applies the optional caller filters to a queue entry.
     *
     * <p>{@code controlStatus} is deliberately not applied here: a queue entry has a state, not a
     * control status, and silently dropping every pending entry when the caller asks for "KO only"
     * would hide the fact that the run is still going.</p>
     *
     * @return {@code true} when the queue entry should be included in the response.
     */
    private boolean matchesQueueFilters(TestCaseExecutionQueue entry, String testFolder, String testcase,
                                        String country, String environment) {
        return matches(entry.getTest(), testFolder)
                && matches(entry.getTestCase(), testcase)
                && matches(entry.getCountry(), country)
                && matches(entry.getEnvironment(), environment);
    }

    /**
     * Case-insensitive equality that treats a blank filter as "match everything".
     */
    private boolean matches(String actual, String filter) {
        return filter.isBlank() || filter.equalsIgnoreCase(MCPToolUtils.nullSafe(actual));
    }

    /**
     * Converts a finished (or running) execution into a compact ordered map.
     *
     * <p>Only the fields needed to choose which execution to inspect further are returned; the
     * detail lives behind {@code cerberus_testcase_execution_get}.</p>
     */
    private Map<String, Object> toSummary(TestCaseExecution execution) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("executionId", execution.getId());
        map.put("controlStatus", MCPToolUtils.nullSafe(execution.getControlStatus()));
        map.put("controlMessage", MCPToolUtils.nullSafe(execution.getControlMessage()));
        map.put("testFolder", MCPToolUtils.nullSafe(execution.getTest()));
        map.put("testcase", MCPToolUtils.nullSafe(execution.getTestCase()));
        map.put("country", MCPToolUtils.nullSafe(execution.getCountry()));
        map.put("environment", MCPToolUtils.nullSafe(execution.getEnvironment()));
        map.put("robot", MCPToolUtils.nullSafe(execution.getRobot()));
        map.put("start", execution.getStart());
        map.put("end", execution.getEnd());
        map.put("durationMs", execution.getDurationMs());
        return map;
    }

    /**
     * Converts a queue entry into a compact ordered map.
     *
     * <p>{@code executionId} is included when the entry has already produced one, so the agent
     * can move straight to {@code cerberus_testcase_execution_get} without a second lookup.
     * {@code comment} carries the engine's reason for a CANCELLED or ERROR state — which is the
     * only place that reason is recorded.</p>
     */
    private Map<String, Object> toQueueSummary(TestCaseExecutionQueue entry) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("queueId", entry.getId());
        map.put("state", entry.getState() == null ? "" : entry.getState().name());
        map.put("testFolder", MCPToolUtils.nullSafe(entry.getTest()));
        map.put("testcase", MCPToolUtils.nullSafe(entry.getTestCase()));
        map.put("country", MCPToolUtils.nullSafe(entry.getCountry()));
        map.put("environment", MCPToolUtils.nullSafe(entry.getEnvironment()));
        map.put("robot", MCPToolUtils.nullSafe(entry.getRobot()));
        map.put("comment", MCPToolUtils.nullSafe(entry.getComment()));
        if (entry.getExeId() > 0) {
            map.put("executionId", entry.getExeId());
        }
        return map;
    }

}
