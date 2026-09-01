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
package org.cerberus.core.mcp.impl.execution;

import io.modelcontextprotocol.server.McpServerFeatures;
import io.modelcontextprotocol.spec.McpSchema;
import org.cerberus.core.crud.entity.TestCaseExecution;
import org.cerberus.core.crud.entity.TestCaseExecutionData;
import org.cerberus.core.crud.entity.TestCaseExecutionFile;
import org.cerberus.core.crud.entity.TestCaseStepActionControlExecution;
import org.cerberus.core.crud.entity.TestCaseStepActionExecution;
import org.cerberus.core.crud.entity.TestCaseStepExecution;
import org.cerberus.core.crud.service.ITestCaseExecutionDataService;
import org.cerberus.core.crud.service.ITestCaseExecutionService;
import org.cerberus.core.exception.CerberusException;
import org.cerberus.core.mcp.MCPTool;
import org.cerberus.core.mcp.util.MCPLogUtils;
import org.cerberus.core.mcp.util.MCPToolUtils;
import org.cerberus.core.util.answer.AnswerItem;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * MCP tool that returns the result of a single testcase execution, under the tool name
 * {@code cerberus_testcase_execution_get}.
 *
 * <p>This is the counterpart of {@link CreateTestCaseExecutionTool}: that tool queues a run,
 * this one tells the agent what the run actually did. Without it an agent can start a test but
 * has no way to know whether its script worked, which is the main feedback loop when authoring
 * or debugging a testcase.</p>
 *
 * <p>Three detail levels keep the response proportionate to the question being asked:</p>
 * <ul>
 *   <li>{@code summary} — execution header only (status, message, timing, environment). Cheap,
 *       and enough to answer "did it pass?".</li>
 *   <li>{@code steps} — adds one entry per executed step with its own status and message, which
 *       is enough to locate <em>where</em> a run broke.</li>
 *   <li>{@code full} — adds every action and control, each with the raw (…Init) and resolved
 *       values, plus the calculated properties and the recorded artefacts. This is the debug
 *       view: it shows what the engine actually sent and got back.</li>
 * </ul>
 *
 * <p>Delegation: {@link ITestCaseExecutionService#readByKey(long)} for {@code summary}, and
 * {@link ITestCaseExecutionService#readByKeyWithDependency(long)} for the two richer levels —
 * the latter eagerly loads steps, actions, controls, properties and file references.</p>
 */
@Component
public class GetTestCaseExecutionTool implements MCPTool {

    private static final String TOOL_NAME = "cerberus_testcase_execution_get";

    private static final String DETAIL_SUMMARY = "summary";
    private static final String DETAIL_STEPS = "steps";
    private static final String DETAIL_FULL = "full";

    private static final List<String> DETAIL_LEVELS = List.of(DETAIL_SUMMARY, DETAIL_STEPS, DETAIL_FULL);

    /**
     * Upper bound applied to every free-text field echoed back from an execution (control
     * messages, resolved values, property values). Engine messages can embed a whole HTTP
     * response or DOM fragment; without a cap a single failed control could take over the
     * agent's context window. Callers who need the untruncated payload should read the
     * recorded artefact with {@code cerberus_testcase_execution_file_get}.
     */
    private static final int MAX_TEXT_LENGTH = 2000;

    /**
     * Largest number of property rows returned. One data library call can resolve to hundreds of
     * rows, each carrying its own payload; past this point the response says how many there were
     * rather than trying to carry them all.
     */
    private static final int MAX_PROPERTY_ROWS = 100;

    private final ITestCaseExecutionService testCaseExecutionService;
    private final ITestCaseExecutionDataService testCaseExecutionDataService;
    private final MCPLogUtils mcpLogUtils;

    public GetTestCaseExecutionTool(ITestCaseExecutionService testCaseExecutionService,
                                    ITestCaseExecutionDataService testCaseExecutionDataService,
                                    MCPLogUtils mcpLogUtils) {
        this.testCaseExecutionService = testCaseExecutionService;
        this.testCaseExecutionDataService = testCaseExecutionDataService;
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
     * <p>{@code executionId} is the only required parameter; it is returned by
     * {@code cerberus_testcase_execution_list} and, once a queue entry has started, by the
     * queue entry itself.</p>
     *
     * @return the fully-described {@link McpSchema.Tool} for registration with the MCP server.
     */
    private McpSchema.Tool createTool() {
        Map<String, Object> properties = new LinkedHashMap<>();
        properties.put("executionId", Map.of(
                "type", "integer",
                "description", "Numeric execution id, as returned by cerberus_testcase_execution_list."
        ));
        properties.put("detail", Map.of(
                "type", "string",
                "description", """
                        How much of the execution to return.
                        - summary (default): status, message, environment and timing only.
                        - steps: adds each executed step with its own status and message. Use this to find where a run broke.
                        - full: adds every action and control with their raw and resolved values, the calculated
                          properties, and the list of recorded artefacts. Use this to debug a failing test.
                        Start with summary or steps; only ask for full once you know which execution you care about.
                        """,
                "enum", DETAIL_LEVELS
        ));

        return new McpSchema.Tool(
                TOOL_NAME,
                null,
                """
                Returns the result of one testcase execution: whether it passed, and if not, where and why it failed.

                Call this after cerberus_testcase_execution_create to check whether the run succeeded, and whenever
                the user asks about the outcome, the logs or the failure of a specific execution.

                Use cerberus_testcase_execution_list first to find the execution id from a tag or a testcase.
                Control status values: OK (passed), KO (a control failed — the application is at fault),
                FA (the test itself failed to run), NA (no data), NE (not executed), PE (still running),
                QU/QE (still queued), CA (cancelled), WE (waiting for manual testing).
                """,
                new McpSchema.JsonSchema(
                        "object",
                        properties,
                        List.of("executionId"),
                        null,
                        null,
                        null
                ),
                null,
                MCPToolUtils.readOnlyAnnotations("Get testcase execution", false),
                null
        );
    }

    /**
     * Validates the arguments, loads the execution at the requested depth, and returns it as JSON.
     *
     * @param args tool arguments extracted from the MCP request (may be empty but never null).
     * @return a {@link McpSchema.CallToolResult} carrying the execution, or an error description.
     */
    private McpSchema.CallToolResult execute(Map<String, Object> args) {
        long executionId = MCPToolUtils.getLong(args, "executionId", 0L);
        String detail = MCPToolUtils.getString(args, "detail", DETAIL_SUMMARY);

        mcpLogUtils.call(TOOL_NAME, "testcase_execution_get",
                String.format("MCP tool %s called with executionId=%s detail=%s", TOOL_NAME, executionId, detail));

        if (executionId <= 0) {
            return MCPToolUtils.errorText("Missing or invalid required parameter: executionId");
        }
        if (!DETAIL_LEVELS.contains(detail)) {
            return MCPToolUtils.errorText("Unsupported detail level: " + detail + ". Supported levels: " + DETAIL_LEVELS);
        }

        // readByKeyWithDependency dereferences the execution without a null check, so existence
        // is always established with the plain read first.
        AnswerItem<TestCaseExecution> answer = testCaseExecutionService.readByKey(executionId);
        if (!answer.isCodeStringEquals("OK") || answer.getItem() == null) {
            return MCPToolUtils.errorText("Execution does not exist: " + executionId);
        }

        if (DETAIL_SUMMARY.equals(detail)) {
            return MCPToolUtils.successJson(toSummary(answer.getItem()));
        }

        // ITestCaseExecutionService declares this one with a raw AnswerItem, so the item is
        // narrowed here rather than through an unchecked assignment of the whole answer.
        AnswerItem<?> withDependency = testCaseExecutionService.readByKeyWithDependency(executionId);
        TestCaseExecution execution = withDependency.getItem() instanceof TestCaseExecution loaded
                ? loaded
                : answer.getItem();

        Map<String, Object> result = toSummary(execution);
        result.put("steps", toSteps(execution, DETAIL_FULL.equals(detail)));

        if (DETAIL_FULL.equals(detail)) {
            result.put("properties", toProperties(executionId, result));
            // Execution-level artefacts only (the engine stores these with an empty level) ; the
            // per-step, per-action and per-control ones are reported alongside their own entry.
            result.put("files", toFiles(execution.getFileList()));
        }

        return MCPToolUtils.successJson(result);
    }

    /**
     * Converts the execution header into an ordered map.
     *
     * <p>{@code controlStatus} and {@code controlMessage} come first because they carry the
     * answer to "did it pass, and why not" — the rest is context for that verdict.</p>
     *
     * @param execution the execution to summarise.
     * @return an ordered map with the execution's header fields.
     */
    private Map<String, Object> toSummary(TestCaseExecution execution) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("executionId", execution.getId());
        map.put("controlStatus", MCPToolUtils.nullSafe(execution.getControlStatus()));
        map.put("controlMessage", truncate(execution.getControlMessage()));
        map.put("testFolder", MCPToolUtils.nullSafe(execution.getTest()));
        map.put("testcase", MCPToolUtils.nullSafe(execution.getTestCase()));
        map.put("description", MCPToolUtils.nullSafe(execution.getDescription()));
        map.put("system", MCPToolUtils.nullSafe(execution.getSystem()));
        map.put("application", MCPToolUtils.nullSafe(execution.getApplication()));
        map.put("country", MCPToolUtils.nullSafe(execution.getCountry()));
        map.put("environment", MCPToolUtils.nullSafe(execution.getEnvironment()));
        map.put("robot", MCPToolUtils.nullSafe(execution.getRobot()));
        map.put("robotExecutor", MCPToolUtils.nullSafe(execution.getRobotExecutor()));
        map.put("browser", MCPToolUtils.nullSafe(execution.getBrowser()));
        map.put("url", MCPToolUtils.nullSafe(execution.getUrl()));
        map.put("tag", MCPToolUtils.nullSafe(execution.getTag()));
        map.put("start", execution.getStart());
        map.put("end", execution.getEnd());
        map.put("durationMs", execution.getDurationMs());
        map.put("queueId", execution.getQueueID());
        map.put("queueState", MCPToolUtils.nullSafe(execution.getQueueState()));
        return map;
    }

    /**
     * Converts the executed steps, optionally descending into actions and controls.
     *
     * @param execution   the dependency-loaded execution.
     * @param withActions whether to include the action / control detail under each step.
     * @return one ordered map per executed step, in execution order.
     */
    private List<Map<String, Object>> toSteps(TestCaseExecution execution, boolean withActions) {
        List<Map<String, Object>> steps = new ArrayList<>();

        if (execution.getTestCaseStepExecutionList() == null) {
            return steps;
        }

        for (TestCaseStepExecution step : execution.getTestCaseStepExecutionList()) {
            Map<String, Object> map = new LinkedHashMap<>();
            map.put("stepId", step.getStepId());
            map.put("sort", step.getSort());
            map.put("index", step.getIndex());
            map.put("description", MCPToolUtils.nullSafe(step.getDescription()));
            map.put("returnCode", MCPToolUtils.nullSafe(step.getReturnCode()));
            map.put("returnMessage", truncate(step.getReturnMessage()));
            map.put("durationMs", step.getEnd() - step.getStart());

            // A step borrowed from the step library executes code that does not live in this
            // testcase; naming the source is what makes such a failure actionable.
            if (step.isUsingLibraryStep()) {
                map.put("libraryStepTestFolder", MCPToolUtils.nullSafe(step.getLibraryStepTest()));
                map.put("libraryStepTestcase", MCPToolUtils.nullSafe(step.getLibraryStepTestcase()));
            }

            if (withActions) {
                // Artefacts are attached at the level that produced them, not to the execution :
                // the screenshot that shows why a control failed hangs off that control. Listing
                // them here rather than only at execution level is what makes a failure
                // diagnosable without guessing which file belongs to which moment.
                map.put("files", toFiles(step.getFileList()));
                map.put("actions", toActions(step));
            }

            steps.add(map);
        }

        return steps;
    }

    /**
     * Converts the actions of one step, each with its controls.
     *
     * <p>Both {@code value1Init} and {@code value1} are returned: the first is what the testcase
     * declares, the second is what the engine actually used after property substitution. A
     * mismatch between the two is the single most common cause of a test failing for a reason
     * that is not visible in the testcase definition.</p>
     *
     * @param step the executed step to read actions from.
     * @return one ordered map per executed action, in execution order.
     */
    private List<Map<String, Object>> toActions(TestCaseStepExecution step) {
        List<Map<String, Object>> actions = new ArrayList<>();

        if (step.getTestCaseStepActionExecutionList() == null) {
            return actions;
        }

        for (TestCaseStepActionExecution action : step.getTestCaseStepActionExecutionList()) {
            Map<String, Object> map = new LinkedHashMap<>();
            map.put("sequence", action.getSequence());
            map.put("action", MCPToolUtils.nullSafe(action.getAction()));
            map.put("description", MCPToolUtils.nullSafe(action.getDescription()));
            map.put("value1Init", truncate(action.getValue1Init()));
            map.put("value1", truncate(action.getValue1()));
            map.put("value2Init", truncate(action.getValue2Init()));
            map.put("value2", truncate(action.getValue2()));
            map.put("value3Init", truncate(action.getValue3Init()));
            map.put("value3", truncate(action.getValue3()));
            map.put("returnCode", MCPToolUtils.nullSafe(action.getReturnCode()));
            map.put("returnMessage", truncate(action.getReturnMessage()));
            map.put("durationMs", action.getEnd() - action.getStart());
            map.put("files", toFiles(action.getFileList()));
            map.put("controls", toControls(action));
            actions.add(map);
        }

        return actions;
    }

    /**
     * Converts the controls attached to one action.
     *
     * @param action the executed action to read controls from.
     * @return one ordered map per executed control, in execution order.
     */
    private List<Map<String, Object>> toControls(TestCaseStepActionExecution action) {
        List<Map<String, Object>> controls = new ArrayList<>();

        if (action.getTestCaseStepActionControlExecutionList() == null) {
            return controls;
        }

        for (TestCaseStepActionControlExecution control : action.getTestCaseStepActionControlExecutionList()) {
            Map<String, Object> map = new LinkedHashMap<>();
            map.put("controlSequence", control.getControlSequence());
            map.put("control", MCPToolUtils.nullSafe(control.getControl()));
            map.put("description", MCPToolUtils.nullSafe(control.getDescription()));
            map.put("value1Init", truncate(control.getValue1Init()));
            map.put("value1", truncate(control.getValue1()));
            map.put("value2Init", truncate(control.getValue2Init()));
            map.put("value2", truncate(control.getValue2()));
            map.put("value3Init", truncate(control.getValue3Init()));
            map.put("value3", truncate(control.getValue3()));
            map.put("fatal", MCPToolUtils.nullSafe(control.getFatal()));
            map.put("returnCode", MCPToolUtils.nullSafe(control.getReturnCode()));
            map.put("returnMessage", truncate(control.getReturnMessage()));
            map.put("files", toFiles(control.getFileList()));
            controls.add(map);
        }

        return controls;
    }

    /**
     * Converts the properties calculated during the execution.
     *
     * <p>A property is where a testcase fetches the data it then acts on — a row from a database,
     * an entry from a data library, a value scraped out of the page. When a run does something
     * unexpected, the reason is very often that a property resolved to something other than what
     * the author assumed, so this is a full diagnostic view rather than a summary: both what the
     * testcase asked for ({@code value1Init}, before property substitution) and what the engine
     * actually ran ({@code value1}), where the data came from, and the raw payload it came back
     * with.</p>
     *
     * <p>The data is re-read through {@link ITestCaseExecutionDataService#readByIdWithDependency(long)}
     * rather than taken from {@code execution.getTestCaseExecutionDataMap()}. That map is keyed by
     * property name and populated only from rows whose index is 1, so a property that resolved to
     * several rows — any data library returning a set — would be reported as a single row and the
     * rest silently lost. Re-reading also brings in the artefacts recorded at property level.</p>
     *
     * <p>No masking is applied here, and none is needed: {@code TestCaseExecutionDataDAO} passes
     * every value, every {@code …Init}, the return message and {@code jsonResult} through
     * {@code StringUtil.secureFromSecrets} on the way in, so what the database holds is already
     * redacted. The runtime secrets map does not survive a read, so masking could not be redone
     * here in any case.</p>
     *
     * @param executionId the execution to read properties for.
     * @param result      the response being built, to which a truncation notice may be added.
     * @return one ordered map per calculated property row, ordered by property then index.
     */
    private List<Map<String, Object>> toProperties(long executionId, Map<String, Object> result) {
        List<Map<String, Object>> properties = new ArrayList<>();

        List<TestCaseExecutionData> dataList;
        try {
            dataList = testCaseExecutionDataService.readByIdWithDependency(executionId);
        } catch (CerberusException e) {
            result.put("propertiesError", "Unable to read the calculated properties: " + e.getMessage());
            return properties;
        }

        if (dataList == null) {
            return properties;
        }

        if (dataList.size() > MAX_PROPERTY_ROWS) {
            result.put("propertiesTruncated", true);
            result.put("propertiesTotal", dataList.size());
            result.put("propertiesNote", "This execution calculated " + dataList.size()
                    + " property rows; the first " + MAX_PROPERTY_ROWS + " are returned. A data library "
                    + "returning many rows produces one row per result.");
            dataList = dataList.subList(0, MAX_PROPERTY_ROWS);
        }

        for (TestCaseExecutionData data : dataList) {
            Map<String, Object> map = new LinkedHashMap<>();
            map.put("property", MCPToolUtils.nullSafe(data.getProperty()));
            // Index distinguishes the rows of a multi-row property; 1 is the only one the rest of
            // Cerberus surfaces by default.
            map.put("index", data.getIndex());
            map.put("type", MCPToolUtils.nullSafe(data.getType()));
            map.put("nature", MCPToolUtils.nullSafe(data.getNature()));
            map.put("description", truncate(data.getDescription()));
            map.put("value", truncate(data.getValue()));
            // …Init is what the testcase declares, the plain field is what the engine ran after
            // substituting other properties into it. A difference between the two explains most
            // "but my query is correct" failures.
            map.put("value1Init", truncate(data.getValue1Init()));
            map.put("value1", truncate(data.getValue1()));
            map.put("value2Init", truncate(data.getValue2Init()));
            map.put("value2", truncate(data.getValue2()));
            map.put("value3Init", truncate(data.getValue3Init()));
            map.put("value3", truncate(data.getValue3()));
            map.put("database", MCPToolUtils.nullSafe(data.getDatabase()));
            map.put("dataLib", MCPToolUtils.nullSafe(data.getDataLib()));
            // The raw payload the source returned, before Cerberus picked a value out of it.
            map.put("jsonResult", truncate(data.getJsonResult()));
            map.put("rowLimit", data.getRowLimit());
            map.put("retryNb", data.getRetryNb());
            map.put("retryPeriod", data.getRetryPeriod());
            map.put("fromCache", MCPToolUtils.nullSafe(data.getFromCache()));
            // A property is resolved per country and environment : when only one environment
            // fails, the answer is often that these three differ from what was expected.
            map.put("system", MCPToolUtils.nullSafe(data.getSystem()));
            map.put("environment", MCPToolUtils.nullSafe(data.getEnvironment()));
            map.put("country", MCPToolUtils.nullSafe(data.getCountry()));
            map.put("returnCode", MCPToolUtils.nullSafe(data.getRC()));
            map.put("returnMessage", truncate(data.getrMessage()));
            map.put("durationMs", data.getEnd() - data.getStart());
            map.put("files", toFiles(data.getFileList()));
            properties.add(map);
        }

        return properties;
    }

    /**
     * Lists the artefacts recorded during the execution (screenshots, page sources, service
     * responses, console and robot logs).
     *
     * <p>Only the references are returned. Text artefacts can then be read with
     * {@code cerberus_testcase_execution_file_get}, which keeps this response small whether the
     * run produced two files or two hundred.</p>
     *
     * @param files the recorded files, may be {@code null} when the execution produced none.
     * @return one ordered map per recorded artefact.
     */
    private List<Map<String, Object>> toFiles(List<TestCaseExecutionFile> files) {
        List<Map<String, Object>> result = new ArrayList<>();

        if (files == null) {
            return result;
        }

        for (TestCaseExecutionFile file : files) {
            Map<String, Object> map = new LinkedHashMap<>();
            map.put("fileId", file.getId());
            map.put("level", MCPToolUtils.nullSafe(file.getLevel()));
            map.put("fileDesc", MCPToolUtils.nullSafe(file.getFileDesc()));
            map.put("fileType", MCPToolUtils.nullSafe(file.getFileType()));
            map.put("fileName", MCPToolUtils.nullSafe(file.getFileName()));
            result.add(map);
        }

        return result;
    }

    /**
     * Caps a free-text execution field at {@link #MAX_TEXT_LENGTH} characters, appending an
     * explicit marker so the agent can tell a truncated value from a complete one.
     *
     * @param value the raw value, may be {@code null}.
     * @return the value, never {@code null}, never longer than the cap plus the marker.
     */
    private String truncate(String value) {
        String safe = MCPToolUtils.nullSafe(value);
        if (safe.length() <= MAX_TEXT_LENGTH) {
            return safe;
        }
        return safe.substring(0, MAX_TEXT_LENGTH) + "… [truncated, " + safe.length() + " characters total]";
    }

}
