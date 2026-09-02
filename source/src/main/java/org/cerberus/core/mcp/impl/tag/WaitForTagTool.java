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
import org.cerberus.core.crud.entity.TestCaseExecutionQueue;
import org.cerberus.core.crud.service.ITagService;
import org.cerberus.core.crud.service.ITestCaseExecutionQueueService;
import org.cerberus.core.crud.service.ITestCaseExecutionService;
import org.cerberus.core.exception.CerberusException;
import org.cerberus.core.mcp.MCPTool;
import org.cerberus.core.mcp.util.MCPExecutionSignal;
import org.cerberus.core.mcp.util.MCPLogUtils;
import org.cerberus.core.mcp.util.MCPToolUtils;
import org.cerberus.core.util.answer.AnswerItem;
import org.cerberus.core.util.answer.AnswerList;
import org.springframework.stereotype.Component;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * MCP tool that waits for a run to finish and returns its verdict, under the tool name
 * {@code cerberus_tag_wait}.
 *
 * <p>Queuing a run and reading its result were two separate acts: the agent had to decide on its
 * own when to look again, which either wastes turns polling too eagerly or leaves the user waiting.
 * This tool holds the call open server-side instead, so from the agent's point of view it launches
 * a run, waits, and comes back with something to analyse.</p>
 *
 * <p>It works on the <em>tag</em>, which is what both {@code cerberus_testcase_execution_create}
 * and {@code cerberus_campaign_execution_create} return, so a single testcase run and a whole
 * campaign are followed exactly the same way.</p>
 *
 * <h2>How the waiting works</h2>
 *
 * <p>The call sleeps on {@link MCPExecutionSignal}, which the execution engine releases the moment
 * an execution completes, and re-reads the database each time it is woken. It also wakes on its own
 * every {@link #POLL_INTERVAL_MILLIS} milliseconds, because the signal cannot always arrive: on a
 * multi-instance Cerberus the run may execute in another JVM, and an entry that is cancelled or
 * fails to start never announces a completion. The signal is therefore an accelerator — best case
 * the answer comes back the instant the run ends, worst case this behaves like a poller.</p>
 *
 * <h2>Why it is bounded</h2>
 *
 * <p>An MCP client abandons a call it considers too slow — the Cerberus assistant hard-codes sixty
 * seconds — and a call cut mid-flight surfaces as a transport error, telling the agent nothing
 * about whether the run happened. The wait therefore stops before that, and says plainly that the
 * run is still going so the agent can call again. Calling again is cheap: no reasoning happens
 * between two waits.</p>
 */
@Component
public class WaitForTagTool implements MCPTool {

    private static final String TOOL_NAME = "cerberus_tag_wait";

    /**
     * Default and maximum wait. The ceiling stays under the sixty seconds hard-coded by the
     * Cerberus AI client, so the call always returns an answer rather than being cut off.
     */
    private static final int DEFAULT_TIMEOUT_SECONDS = 45;
    private static final int MAX_TIMEOUT_SECONDS = 55;

    /** Longest a waiter sleeps before re-reading on its own, when no signal arrives. */
    private static final long POLL_INTERVAL_MILLIS = 3_000L;

    private static final String DETAIL_SUMMARY = "summary";
    private static final String DETAIL_FAILURES = "failures";
    private static final String DETAIL_ALL = "all";

    private static final List<String> DETAIL_LEVELS = List.of(DETAIL_SUMMARY, DETAIL_FAILURES, DETAIL_ALL);

    /** Cap on the executions listed, so a large campaign cannot flood the agent's context. */
    private static final int MAX_LISTED_EXECUTIONS = 100;

    /**
     * Queue states in which an entry still owes a result. Anything else — DONE, CANCELLED, ERROR —
     * is settled, whether or not it produced an execution.
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

    /**
     * Control statuses that still owe a result, used to tell a genuine outcome from an execution
     * row that merely stands for something not finished yet.
     */
    private static final List<String> PENDING_CONTROL_STATUSES = List.of(
            TestCaseExecution.CONTROLSTATUS_PE,
            TestCaseExecution.CONTROLSTATUS_QU,
            TestCaseExecution.CONTROLSTATUS_PA);

    /**
     * Severity order used to summarise a testcase that ran several times in a campaign, most
     * alarming first: a test that could not run at all (FA) hides a problem more effectively than
     * one that ran and failed a control (KO).
     */
    private static final List<String> SEVERITY_ORDER = List.of(
            TestCaseExecution.CONTROLSTATUS_FA,
            TestCaseExecution.CONTROLSTATUS_KO,
            TestCaseExecution.CONTROLSTATUS_QE,
            TestCaseExecution.CONTROLSTATUS_NA,
            TestCaseExecution.CONTROLSTATUS_NE,
            TestCaseExecution.CONTROLSTATUS_CA,
            TestCaseExecution.CONTROLSTATUS_WE,
            TestCaseExecution.CONTROLSTATUS_PE,
            TestCaseExecution.CONTROLSTATUS_QU,
            TestCaseExecution.CONTROLSTATUS_PA,
            TestCaseExecution.CONTROLSTATUS_OK);

    private final ITagService tagService;
    private final ITestCaseExecutionService testCaseExecutionService;
    private final ITestCaseExecutionQueueService testCaseExecutionQueueService;
    private final MCPExecutionSignal mcpExecutionSignal;
    private final MCPLogUtils mcpLogUtils;

    public WaitForTagTool(ITagService tagService,
                          ITestCaseExecutionService testCaseExecutionService,
                          ITestCaseExecutionQueueService testCaseExecutionQueueService,
                          MCPExecutionSignal mcpExecutionSignal,
                          MCPLogUtils mcpLogUtils) {
        this.tagService = tagService;
        this.testCaseExecutionService = testCaseExecutionService;
        this.testCaseExecutionQueueService = testCaseExecutionQueueService;
        this.mcpExecutionSignal = mcpExecutionSignal;
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
     * @return the fully-described {@link McpSchema.Tool} for registration with the MCP server.
     */
    private McpSchema.Tool createTool() {
        Map<String, Object> properties = new LinkedHashMap<>();
        properties.put("tag", Map.of(
                "type", "string",
                "description", "Tag to wait on, as returned by cerberus_testcase_execution_create or "
                        + "cerberus_campaign_execution_create."
        ));
        properties.put("timeoutSeconds", Map.of(
                "type", "integer",
                "description", "How long to hold the call open. Defaults to " + DEFAULT_TIMEOUT_SECONDS
                        + ", capped at " + MAX_TIMEOUT_SECONDS + " so the call always returns before an MCP "
                        + "client gives up on it."
        ));
        properties.put("detail", Map.of(
                "type", "string",
                "description", """
                        How much to return once the run is over.
                        - summary: the verdict and the counts only.
                        - failures (default): adds the executions that did not pass, with the reason, and for a
                          campaign a per-testcase roll-up. This is what you want in order to analyse a run.
                        - all: adds every execution, passing ones included. Only useful to report exhaustively.
                        """,
                "enum", DETAIL_LEVELS
        ));

        return new McpSchema.Tool(
                TOOL_NAME,
                null,
                """
                Waits for a queued run to finish, then returns its verdict and what failed.

                Call this immediately after cerberus_testcase_execution_create or
                cerberus_campaign_execution_create, with the tag it returned. It holds the call open until the
                run is over, so you do not have to decide when to look again, and it comes back with enough
                detail to explain the outcome without a second call.

                It works the same for a single testcase and for a whole campaign. For a campaign you also get a
                per-testcase roll-up, which is the level to reason at when many testcases ran across several
                countries and environments.

                If the answer says stillRunning, the run is simply longer than one wait: call this tool again
                with the same tag. Nothing is lost between two waits, and no reasoning is spent while waiting.
                Do not fall back to polling cerberus_tag_get in a loop — that costs a turn each time.

                Use cerberus_testcase_execution_get on a failing execution id to go deeper, and
                cerberus_testcase_execution_file_get to look at a screenshot or a service response.
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
                MCPToolUtils.readOnlyAnnotations("Wait for a run to finish", false),
                null
        );
    }

    /**
     * Waits for the tag to settle, then builds the verdict.
     *
     * @param args tool arguments extracted from the MCP request (may be empty but never null).
     * @return a {@link McpSchema.CallToolResult} carrying the outcome, or an error.
     */
    private McpSchema.CallToolResult execute(Map<String, Object> args) {
        String tag = MCPToolUtils.getString(args, "tag", "").trim();
        int timeoutSeconds = Math.min(
                Math.max(MCPToolUtils.getInteger(args, "timeoutSeconds", DEFAULT_TIMEOUT_SECONDS), 1),
                MAX_TIMEOUT_SECONDS);
        String detail = MCPToolUtils.getString(args, "detail", DETAIL_FAILURES);

        mcpLogUtils.call(TOOL_NAME, "tag_wait",
                String.format("MCP tool %s called with tag=%s timeoutSeconds=%s detail=%s",
                        TOOL_NAME, tag, timeoutSeconds, detail));

        if (tag.isBlank()) {
            return MCPToolUtils.errorText("Missing required parameter: tag");
        }
        if (!DETAIL_LEVELS.contains(detail)) {
            return MCPToolUtils.errorText("Unsupported detail level: " + detail + ". Supported levels: " + DETAIL_LEVELS);
        }

        AnswerItem<Tag> tagAnswer = tagService.readByKey(tag);
        if (!tagAnswer.isCodeStringEquals("OK") || tagAnswer.getItem() == null) {
            return MCPToolUtils.errorText("Tag does not exist: " + tag
                    + ". Nothing has been queued under it, or it was misspelled.");
        }

        long start = System.currentTimeMillis();
        long deadline = start + TimeUnit.SECONDS.toMillis(timeoutSeconds);
        String wokenBy = "immediate";
        QueueState queueState;

        while (true) {
            try {
                queueState = readQueueState(tag);
            } catch (CerberusException e) {
                return MCPToolUtils.errorText("Unable to read the execution queue of tag " + tag + ": " + e.getMessage());
            }

            if (queueState.settled()) {
                break;
            }

            long remaining = deadline - System.currentTimeMillis();
            if (remaining <= 0) {
                wokenBy = "timeout";
                break;
            }

            // Registered before the wait and dropped straight after, so a tag nobody follows any
            // more leaves nothing behind in the signal registry.
            CountDownLatch latch = mcpExecutionSignal.register(tag);
            boolean signalled;
            try {
                signalled = latch.await(Math.min(remaining, POLL_INTERVAL_MILLIS), TimeUnit.MILLISECONDS);
            } catch (InterruptedException e) {
                // The client hung up or the server is shutting down : restore the flag and answer
                // with what is known rather than swallowing the interruption.
                Thread.currentThread().interrupt();
                wokenBy = "interrupted";
                break;
            } finally {
                mcpExecutionSignal.unregister(tag, latch);
            }

            wokenBy = signalled ? "event" : "poll";
        }

        long waitedMillis = System.currentTimeMillis() - start;
        return MCPToolUtils.successJson(buildResult(tag, detail, queueState, wokenBy, waitedMillis));
    }

    /**
     * Reads how much of the run is still owed, from the queue rather than from the tag counters.
     *
     * <p>The tag row is created with every counter at zero and only recomputed once executions
     * report back, so a tag read straight after queuing looks exactly like a finished one. The
     * queue does not have that blind spot: an entry exists from the moment the run is requested.</p>
     */
    private QueueState readQueueState(String tag) throws CerberusException {
        AnswerList<TestCaseExecutionQueue> all = testCaseExecutionQueueService.readByVarious1(tag, null, false);
        List<TestCaseExecutionQueue> entries = all.getDataList() == null ? List.of() : all.getDataList();

        long pending = entries.stream()
                .filter(entry -> entry.getState() != null && PENDING_QUEUE_STATES.contains(entry.getState().name()))
                .count();

        return new QueueState(entries.size(), pending);
    }

    /**
     * Assembles the answer: the verdict, then as much of the run as the requested detail asks for.
     */
    private Map<String, Object> buildResult(String tag, String detail, QueueState queueState,
                                            String wokenBy, long waitedMillis) {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("tag", tag);
        result.put("waitedMs", waitedMillis);
        result.put("wokenBy", wokenBy);

        // Re-read after the loop so the counters reflect the final state rather than the one that
        // happened to be current when the last execution reported.
        Tag tagRow = tagService.readByKey(tag).getItem();
        String campaign = tagRow == null ? "" : MCPToolUtils.nullSafe(tagRow.getCampaign());
        boolean isCampaign = !campaign.isBlank();

        result.put("isCampaign", isCampaign);
        if (isCampaign) {
            result.put("campaign", campaign);
        }

        boolean settled = queueState.settled();
        result.put("stillRunning", !settled);
        result.put("queueEntries", queueState.total());
        result.put("queuePending", queueState.pending());

        if (queueState.total() == 0) {
            result.put("message", "Nothing is queued under this tag. The run was never created, or its queue "
                    + "entries have already been purged. Check the response of the create tool: a tag is "
                    + "returned even when nbExecutions is 0.");
        } else if (!settled) {
            result.put("message", queueState.pending() + " of " + queueState.total()
                    + " queued entries have not finished after " + (waitedMillis / 1000)
                    + "s. Call this tool again with the same tag; the run continues meanwhile.");
        }

        if (tagRow != null) {
            result.put("counters", toCounters(tagRow));
            if (isCampaign) {
                result.put("ci", toCiVerdict(tagRow));
            }
            result.put("durationMs", tagRow.getDurationMs());
        }

        if (DETAIL_SUMMARY.equals(detail)) {
            return result;
        }

        List<TestCaseExecution> executions;
        try {
            executions = testCaseExecutionService.readLastExecutionAndExecutionInQueueByTag(tag);
        } catch (ParseException | CerberusException e) {
            result.put("executionsError", "Unable to read the executions of this tag: " + e.getMessage());
            return result;
        }
        if (executions == null) {
            executions = List.of();
        }

        result.put("totalExecutions", executions.size());

        List<TestCaseExecution> failures = executions.stream()
                .filter(this::isFailure)
                .sorted(Comparator.comparingInt(execution -> severityRank(execution.getControlStatus())))
                .toList();

        result.put("failureCount", failures.size());
        result.put("failures", project(failures));

        // A campaign runs the same testcases across countries and environments, so the useful
        // question is which testcases are unhealthy rather than which individual runs failed.
        if (isCampaign) {
            result.put("byTestcase", rollUpByTestcase(executions));
        }

        if (DETAIL_ALL.equals(detail)) {
            result.put("executions", project(executions));
        }

        if (settled && failures.isEmpty() && !executions.isEmpty()) {
            result.put("verdict", "All " + executions.size() + " execution(s) passed.");
        } else if (settled && !failures.isEmpty()) {
            result.put("verdict", failures.size() + " of " + executions.size()
                    + " execution(s) did not pass. Use cerberus_testcase_execution_get on an executionId "
                    + "below to see the failing step.");
        }

        return result;
    }

    /**
     * True when an execution has settled on something other than a pass.
     *
     * <p>Pending statuses are excluded on purpose: an execution still queued or running has not
     * failed, and counting it as a failure would make a partial answer look alarming.</p>
     */
    private boolean isFailure(TestCaseExecution execution) {
        String status = MCPToolUtils.nullSafe(execution.getControlStatus());
        return !status.isEmpty()
                && !TestCaseExecution.CONTROLSTATUS_OK.equals(status)
                && !PENDING_CONTROL_STATUSES.contains(status);
    }

    /**
     * Position of a status in {@link #SEVERITY_ORDER}; unknown statuses sort last.
     */
    private int severityRank(String status) {
        int rank = SEVERITY_ORDER.indexOf(MCPToolUtils.nullSafe(status));
        return rank < 0 ? SEVERITY_ORDER.size() : rank;
    }

    /**
     * Reduces executions to what identifies them and explains their outcome.
     */
    private List<Map<String, Object>> project(List<TestCaseExecution> executions) {
        List<Map<String, Object>> projected = new ArrayList<>();

        for (TestCaseExecution execution : executions.stream().limit(MAX_LISTED_EXECUTIONS).toList()) {
            Map<String, Object> map = new LinkedHashMap<>();
            map.put("executionId", execution.getId());
            map.put("controlStatus", MCPToolUtils.nullSafe(execution.getControlStatus()));
            map.put("testFolder", MCPToolUtils.nullSafe(execution.getTest()));
            map.put("testcase", MCPToolUtils.nullSafe(execution.getTestCase()));
            map.put("country", MCPToolUtils.nullSafe(execution.getCountry()));
            map.put("environment", MCPToolUtils.nullSafe(execution.getEnvironment()));
            map.put("robot", MCPToolUtils.nullSafe(execution.getRobot()));
            map.put("controlMessage", MCPToolUtils.nullSafe(execution.getControlMessage()));
            projected.add(map);
        }

        if (executions.size() > MAX_LISTED_EXECUTIONS) {
            Map<String, Object> note = new LinkedHashMap<>();
            note.put("truncated", true);
            note.put("shown", MAX_LISTED_EXECUTIONS);
            note.put("total", executions.size());
            projected.add(note);
        }

        return projected;
    }

    /**
     * Groups a campaign's executions by testcase — the level above the individual run.
     *
     * <p>Each entry says how the testcase fared overall and, when it failed, on which
     * country / environment pairs, which is what tells a broken test apart from a broken
     * environment.</p>
     */
    private List<Map<String, Object>> rollUpByTestcase(List<TestCaseExecution> executions) {
        Map<String, Map<String, Object>> byKey = new LinkedHashMap<>();

        for (TestCaseExecution execution : executions) {
            String testFolder = MCPToolUtils.nullSafe(execution.getTest());
            String testcase = MCPToolUtils.nullSafe(execution.getTestCase());
            String key = testFolder + " " + testcase;
            String status = MCPToolUtils.nullSafe(execution.getControlStatus());

            Map<String, Object> entry = byKey.computeIfAbsent(key, k -> {
                Map<String, Object> created = new LinkedHashMap<>();
                created.put("testFolder", testFolder);
                created.put("testcase", testcase);
                created.put("total", 0);
                created.put("passed", 0);
                created.put("failed", 0);
                created.put("worstStatus", "");
                created.put("failedOn", new ArrayList<String>());
                return created;
            });

            entry.put("total", (int) entry.get("total") + 1);
            if (TestCaseExecution.CONTROLSTATUS_OK.equals(status)) {
                entry.put("passed", (int) entry.get("passed") + 1);
            } else if (isFailure(execution)) {
                entry.put("failed", (int) entry.get("failed") + 1);
                @SuppressWarnings("unchecked")
                List<String> failedOn = (List<String>) entry.get("failedOn");
                failedOn.add(MCPToolUtils.nullSafe(execution.getCountry()) + "/"
                        + MCPToolUtils.nullSafe(execution.getEnvironment()) + " " + status);
            }

            String worst = (String) entry.get("worstStatus");
            if (worst.isEmpty() || severityRank(status) < severityRank(worst)) {
                entry.put("worstStatus", status);
            }
        }

        // Unhealthy testcases first, so the roll-up opens on what needs attention.
        return byKey.values().stream()
                .sorted(Comparator.comparingInt(entry -> severityRank((String) entry.get("worstStatus"))))
                .toList();
    }

    /**
     * The per-status counts Cerberus maintains on the tag row.
     */
    private Map<String, Object> toCounters(Tag tag) {
        Map<String, Object> counters = new LinkedHashMap<>();
        counters.put("OK", tag.getNbOK());
        counters.put("KO", tag.getNbKO());
        counters.put("FA", tag.getNbFA());
        counters.put("NA", tag.getNbNA());
        counters.put("NE", tag.getNbNE());
        counters.put("WE", tag.getNbWE());
        counters.put("PE", tag.getNbPE());
        counters.put("QU", tag.getNbQU());
        counters.put("QE", tag.getNbQE());
        counters.put("CA", tag.getNbCA());
        counters.put("total", tag.getNbExe());
        counters.put("flaky", tag.getNbFlaky());
        counters.put("muted", tag.getNbMuted());
        return counters;
    }

    /**
     * The CI verdict, which only a campaign carries.
     */
    private Map<String, Object> toCiVerdict(Tag tag) {
        Map<String, Object> ci = new LinkedHashMap<>();
        ci.put("result", MCPToolUtils.nullSafe(tag.getCiResult()));
        ci.put("score", tag.getCiScore());
        ci.put("threshold", tag.getCiScoreThreshold());
        ci.put("max", tag.getCiScoreMax());
        return ci;
    }

    /**
     * How much of the run the queue still owes.
     *
     * @param total   queue entries created under the tag, whatever their state.
     * @param pending those that have not settled yet.
     */
    private record QueueState(int total, long pending) {

        /**
         * A run is settled once the queue owes nothing more. A tag with no entry at all is also
         * settled — there is nothing to wait for — and the caller is told so explicitly rather than
         * being left to wait out the timeout.
         */
        boolean settled() {
            return pending == 0;
        }
    }
}
