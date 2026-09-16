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
import org.cerberus.core.api.entity.ManualUrlParameters;
import org.cerberus.core.api.entity.QueuedExecution;
import org.cerberus.core.api.entity.QueuedExecutionResult;
import org.cerberus.core.api.entity.QueuedExecutionTestcase;
import org.cerberus.core.api.services.QueuedExecutionService;
import org.cerberus.core.crud.entity.Application;
import org.cerberus.core.crud.entity.TestCase;
import org.cerberus.core.crud.service.IApplicationService;
import org.cerberus.core.crud.service.ITestCaseService;
import org.cerberus.core.mcp.MCPTool;
import org.cerberus.core.mcp.util.MCPExecutionTargets;
import org.cerberus.core.mcp.util.MCPLogUtils;
import org.cerberus.core.mcp.util.MCPToolUtils;
import org.cerberus.core.util.answer.AnswerItem;
import org.springframework.stereotype.Component;

import java.security.Principal;
import java.util.LinkedHashMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * MCP tool that queues (or re-queues) the execution of a single testcase.
 *
 * <p>Exposed MCP tool name: {@code cerberus_testcase_execution_create}.</p>
 *
 * <p>Delegates to {@link QueuedExecutionService#addTestcasesToExecutionQueue(QueuedExecution, Principal)},
 * the same Spring service backing the {@code /public/queuedexecutions/} REST endpoint (and, before
 * it, the legacy {@code AddToExecutionQueuePrivate} servlet). It resolves the cartesian product of
 * the given testcase against the requested countries/environments/robots, inserts one
 * {@code TestCaseExecutionQueue} row per valid combination, and asynchronously triggers execution.</p>
 *
 * <p>"Relaunching" a testcase is simply calling this tool again with the same (or adjusted)
 * parameters — there is no separate rerun concept; a fresh queue entry is created each time.</p>
 */
@Component
public class CreateTestCaseExecutionTool implements MCPTool {

    private static final String TOOL_NAME = "cerberus_testcase_execution_create";

    private static final List<String> MANUAL_EXECUTION_VALUES = List.of("N", "Y", "A");

    /**
     * Placeholder handed to the execution service for applications it does not drive with a robot.
     * The service refuses an empty robots list, but never resolves the name for these application
     * types, so the value only has to exist — it is deliberately not a valid robot name.
     */
    private static final String ROBOT_NOT_APPLICABLE = "N/A";

    private final QueuedExecutionService queuedExecutionService;
    private final ITestCaseService testCaseService;
    private final IApplicationService applicationService;
    private final MCPExecutionTargets executionTargets;
    private final MCPLogUtils mcpLogUtils;

    public CreateTestCaseExecutionTool(QueuedExecutionService queuedExecutionService,
                                       ITestCaseService testCaseService,
                                       IApplicationService applicationService,
                                       MCPExecutionTargets executionTargets,
                                       MCPLogUtils mcpLogUtils) {
        this.queuedExecutionService = queuedExecutionService;
        this.testCaseService = testCaseService;
        this.applicationService = applicationService;
        this.executionTargets = executionTargets;
        this.mcpLogUtils = mcpLogUtils;
    }

    /**
     * Builds one message naming every missing parameter and the values this testcase would accept.
     *
     * <p>Resolving the targets costs a few queries on a call that is failing anyway, and it is the
     * difference between an answer the caller can act on and one that only says a field is empty.
     * When the resolution itself fails — no application, no live environment — that is the real
     * problem and it is reported instead of a list of parameters that could not be filled anyway.</p>
     */
    private String describeMissing(String testFolder, String testcase, List<String> missing, String applicationType) {
        StringBuilder message = new StringBuilder();
        message.append("Missing required parameter(s): ").append(String.join(", ", missing)).append(".\n\n");

        MCPExecutionTargets.Targets targets = executionTargets.resolve(testFolder, testcase);
        if (targets.failed()) {
            return message.append(targets.error()).toString();
        }
        if (targets.application().isBlank()) {
            return message.append("This testcase has no application attached, so no country or environment can "
                    + "be resolved. Attach one with cerberus_testcase_update before executing it.").toString();
        }
        if (targets.runnable().isEmpty()) {
            return message.append(targets.declaredCountries().isEmpty()
                    ? "This testcase declares no country. Add one with cerberus_testcase_country_create, then "
                      + "configure an environment for it."
                    : "The countries this testcase declares (" + targets.declaredCountries() + ") have no active "
                      + "environment for application '" + targets.application() + "'. Nothing can run until one "
                      + "is configured — check cerberus_country_environment_parameters_list.").toString();
        }

        message.append("This testcase can run on:\n");
        for (Map<String, Object> pair : targets.runnable()) {
            message.append("  country=").append(pair.get("country"))
                    .append(" environment=").append(pair.get("environment")).append("\n");
        }

        if (missing.contains("robots")) {
            if (targets.robots().isEmpty()) {
                message.append("\nNo robot matches application type '").append(applicationType)
                        .append("', so this testcase cannot run automatically. A robot matches when its type "
                                + "equals the application type exactly, or is left empty. Create one with "
                                + "cerberus_robot_create, or pass manualExecution='Y'.\n");
            } else {
                message.append("\nRobots it accepts: ").append(targets.robotNames()).append("\n");
            }
        } else if (!targets.robotRequired()) {
            message.append("\nApplication type '").append(applicationType)
                    .append("' is not driven by a robot, so robots is filled in for you.\n");
        }

        message.append("\nSend them all in one call, as lists: {\"testFolder\": \"").append(testFolder)
                .append("\", \"testcase\": \"").append(testcase)
                .append("\", \"countries\": [\"").append(targets.runnable().get(0).get("country"))
                .append("\"], \"environments\": [\"").append(targets.runnable().get(0).get("environment"))
                .append("\"]");
        if (missing.contains("robots") && !targets.robots().isEmpty()) {
            message.append(", \"robots\": [\"").append(targets.robotNames().get(0)).append("\"]");
        }
        message.append("}");

        return message.toString();
    }

    /**
     * Returns the application type behind a testcase, or an empty string when it cannot be
     * resolved.
     *
     * <p>A lookup failure is deliberately not an error: the execution service performs its own
     * validation and reports precisely what was wrong, so a testcase that does not exist should
     * produce that message rather than one invented here.</p>
     */
    private String resolveApplicationType(String testFolder, String testcase) {
        AnswerItem<TestCase> testCaseAnswer = testCaseService.readByKey(testFolder, testcase);
        if (!testCaseAnswer.isCodeStringEquals("OK") || testCaseAnswer.getItem() == null) {
            return "";
        }

        String applicationName = MCPToolUtils.nullSafe(testCaseAnswer.getItem().getApplication());
        if (applicationName.isBlank()) {
            return "";
        }

        AnswerItem<Application> applicationAnswer = applicationService.readByKey(applicationName);
        if (!applicationAnswer.isCodeStringEquals("OK") || applicationAnswer.getItem() == null) {
            return "";
        }

        return MCPToolUtils.nullSafe(applicationAnswer.getItem().getType());
    }

    /**
     * Mirrors the application-type guard in {@code QueuedExecutionService.addToQueue}: only these
     * four types make the engine look at the robot at all.
     */
    private boolean isRobotDriven(String applicationType) {
        return Application.TYPE_GUI.equalsIgnoreCase(applicationType)
                || Application.TYPE_APK.equalsIgnoreCase(applicationType)
                || Application.TYPE_IPA.equalsIgnoreCase(applicationType)
                || Application.TYPE_FAT.equalsIgnoreCase(applicationType);
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
     * Builds the MCP {@link McpSchema.Tool} descriptor for {@code cerberus_testcase_execution_create}.
     *
     * <p>Declares {@code testFolder}, {@code testcase}, {@code countries}, {@code environments},
     * and {@code robots} as required — {@link QueuedExecutionService#addTestcasesToExecutionQueue}
     * rejects the request if any of these three lists is empty, regardless of other settings.</p>
     *
     * @return the fully configured tool descriptor
     */
    private McpSchema.Tool createTool() {
        Map<String, Object> properties = new LinkedHashMap<>();
        properties.put("testFolder", Map.of(
                "type", "string",
                "description", "Test folder identifier the testcase belongs to."
        ));
        properties.put("testcase", Map.of(
                "type", "string",
                "description", "Testcase identifier to execute."
        ));
        properties.put("countries", Map.of(
                "type", "array",
                "items", Map.of("type", "string"),
                "description", "Countries to run the testcase against (e.g. ['FR']). Must already exist as COUNTRY invariants."
        ));
        properties.put("environments", Map.of(
                "type", "array",
                "items", Map.of("type", "string"),
                "description", "Environments to run the testcase against (e.g. ['QA']). Must already exist as ENVIRONMENT invariants."
        ));
        properties.put("robots", Map.of(
                "type", "array",
                "items", Map.of("type", "string"),
                "description", """
                        Robot names to execute on. Required for GUI, APK, IPA and FAT applications — the ones the
                        engine actually drives — including when manualExecution is 'Y' or 'A'.

                        Omit it for SRV, BAT and NONE applications: the engine discards the robot for those, and
                        this tool supplies the placeholder the underlying service needs. Do not invent a browser
                        robot for a service test — it would be ignored, and would make a later failure look like
                        a robot problem.

                        Use cerberus_testcase_execution_targets to get the robots valid for this testcase."""
        ));
        properties.put("tag", Map.of(
                "type", "string",
                "description", "Optional execution tag grouping these runs. Supports %TIMESTAMP%, %USER%, %REQCOUNTRYLIST%, %REQENVIRONMENTLIST% placeholders. Auto-generated when omitted."
        ));
        properties.put("manualExecution", Map.of(
                "type", "string",
                "description", "Whether to run in manual mode. 'N' = automated (default), 'Y' = manual, 'A' = automated with manual fallback.",
                "enum", MANUAL_EXECUTION_VALUES
        ));
        properties.put("screenshot", Map.of("type", "integer", "description", "Screenshot capture level (0=off, 1=on error, 2=always). Defaults to 1."));
        properties.put("video", Map.of("type", "integer", "description", "Video capture level (0=off, 1=on error, 2=always). Defaults to 1."));
        properties.put("verbose", Map.of("type", "integer", "description", "Log verbosity level. Defaults to 1."));
        properties.put("pageSource", Map.of("type", "integer", "description", "Whether to capture page source (0/1). Defaults to 1."));
        properties.put("robotLog", Map.of("type", "integer", "description", "Whether to capture robot logs (0/1). Defaults to 1."));
        properties.put("consoleLog", Map.of("type", "integer", "description", "Whether to capture browser console logs (0/1). Defaults to 1."));
        properties.put("timeout", Map.of("type", "string", "description", "Per-action timeout in milliseconds. Defaults to '30000'."));
        properties.put("retries", Map.of("type", "integer", "description", "Number of retries if the result is not OK. Defaults to 0."));
        properties.put("priority", Map.of("type", "integer", "description", "Queue priority (lower runs first). Defaults to 0."));
        properties.put("manualUrl", Map.of(
                "type", "integer",
                "description", "0 = use Cerberus-configured URL (default), 1 = force manual URL (requires manualHost and manualEnvData), 2 = override only the provided manual* fields.",
                "enum", List.of(0, 1, 2)
        ));
        properties.put("manualHost", Map.of("type", "string", "description", "Manual host override, used when manualUrl is 1 or 2."));
        properties.put("manualContextRoot", Map.of("type", "string", "description", "Manual context root override, used when manualUrl is 1 or 2."));
        properties.put("manualLoginRelativeUrl", Map.of("type", "string", "description", "Manual relative login URL override, used when manualUrl is 1 or 2."));
        properties.put("manualEnvData", Map.of("type", "string", "description", "Environment to source test data from when manualUrl is 1 or 2."));
        properties.put("user", Map.of("type", "string", "description", "Optional user name recorded as the execution trigger. Defaults to 'MCP'."));

        return new McpSchema.Tool(
                TOOL_NAME,
                null,
                """
                Queues the execution of a testcase against one or more countries/environments/robots,
                and immediately triggers it — equivalent to clicking "Run" in the Cerberus UI.

                Call this tool whenever the user asks to run, execute, launch, or relaunch a testcase.
                Relaunching is simply calling this tool again — there is no separate rerun action.

                Before calling this tool, resolve the country, environment and robot with
                cerberus_testcase_execution_targets unless the user already gave you all three. Do not guess
                them and do not assume the country matching the market under test: environments are frequently
                declared under a different country code, and an unconfigured combination queues nothing at all
                while still returning a tag. If several combinations are valid, ask the user which one to use.

                This tool only starts the run. Follow it immediately with cerberus_tag_wait on the tag it
                returns: that call holds until the run is over and comes back with the verdict, so you neither
                have to guess when to look nor spend a turn polling. Then use cerberus_testcase_execution_get
                on a failing execution id to see the step that broke. Always check the result after running a
                testcase you just created or modified — a queued run is not a passing run.

                A response with nbExecutions = 0 means nothing was queued: the accompanying lists say whether the
                testcase does not exist, is inactive, is not allowed on that environment, or the robot is missing.
                """,
                new McpSchema.JsonSchema(
                        "object",
                        properties,
                        List.of("testFolder", "testcase", "countries", "environments"),
                        null,
                        null,
                        null
                ),
                null,
                MCPToolUtils.createAnnotations("Run testcase", false),
                null
        );
    }

    /**
     * Validates the arguments, builds a {@link QueuedExecution} request, and delegates to
     * {@link QueuedExecutionService#addTestcasesToExecutionQueue(QueuedExecution, Principal)}.
     *
     * @param args raw MCP arguments map from the client request
     * @return a success JSON result summarizing the queued execution(s), or an error text result
     */
    private McpSchema.CallToolResult execute(Map<String, Object> args) {
        String testFolder = MCPToolUtils.getString(args, "testFolder", "");
        String testcase = MCPToolUtils.getString(args, "testcase", "");
        List<String> countries = MCPToolUtils.getStringList(args, "countries", List.of());
        List<String> environments = MCPToolUtils.getStringList(args, "environments", List.of());
        List<String> robots = MCPToolUtils.getStringList(args, "robots", List.of());
        String user = MCPToolUtils.getString(args, "user", "MCP");

        mcpLogUtils.call(TOOL_NAME, "testcase_execution_create",
                String.format("MCP tool %s called with testFolder=%s testcase=%s countries=%s environments=%s robots=%s",
                        TOOL_NAME, testFolder, testcase, countries, environments, robots));

        // The identity of the testcase has to be settled before anything else can be resolved, so
        // these two are the only checks that answer on their own.
        List<String> missingIdentity = new ArrayList<>();
        if (testFolder.isBlank()) missingIdentity.add("testFolder");
        if (testcase.isBlank()) missingIdentity.add("testcase");
        if (!missingIdentity.isEmpty()) {
            return MCPToolUtils.errorText("Missing required parameter(s): " + String.join(", ", missingIdentity)
                    + ". Call cerberus_testcase_list to find the testcase, then "
                    + "cerberus_testcase_execution_targets to get the countries, environments and robots it can "
                    + "run on.");
        }

        // Only GUI, APK, IPA and FAT applications are driven by a robot. For every other type the
        // engine discards whatever robot it is given and queues with an empty one — verified by
        // queuing an SRV testcase with a robot name that does not exist: the entry was still
        // created, with Robot empty. The underlying service nevertheless rejects an empty robots
        // list outright, which is why callers used to have to invent a value.
        String applicationType = resolveApplicationType(testFolder, testcase);
        boolean robotDriven = isRobotDriven(applicationType);
        boolean robotIgnored = !applicationType.isBlank() && !robotDriven;

        // Everything still missing is reported in one answer, with the values this testcase would
        // accept. Reporting them one at a time is what turned a first run into four calls: each
        // rejection named a single parameter and none of them said what to put in it.
        List<String> missing = new ArrayList<>();
        if (countries.isEmpty()) missing.add("countries");
        if (environments.isEmpty()) missing.add("environments");
        if (robots.isEmpty() && !robotIgnored) missing.add("robots");

        if (!missing.isEmpty()) {
            return MCPToolUtils.errorText(describeMissing(testFolder, testcase, missing, applicationType));
        }

        if (robots.isEmpty()) {
            // Satisfies the service-level guard without asking the caller for a value that would be
            // thrown away. The name is never resolved for these application types.
            robots = List.of(ROBOT_NOT_APPLICABLE);
        }

        ManualUrlParameters manualUrlParameters = ManualUrlParameters.builder()
                .host(MCPToolUtils.getString(args, "manualHost", ""))
                .contextRoot(MCPToolUtils.getString(args, "manualContextRoot", ""))
                .loginRelativeUrl(MCPToolUtils.getString(args, "manualLoginRelativeUrl", ""))
                .envData(MCPToolUtils.getString(args, "manualEnvData", ""))
                .build();

        QueuedExecution queuedExecution = QueuedExecution.builder()
                .testcases(List.of(QueuedExecutionTestcase.builder()
                        .testFolderId(testFolder)
                        .testcaseId(testcase)
                        .build()))
                .countries(countries)
                .environments(environments)
                .robots(robots)
                .tag(MCPToolUtils.getString(args, "tag", ""))
                .manualExecution(MCPToolUtils.getString(args, "manualExecution", "N"))
                .screenshot(MCPToolUtils.getInteger(args, "screenshot", 1))
                .video(MCPToolUtils.getInteger(args, "video", 1))
                .verbose(MCPToolUtils.getInteger(args, "verbose", 1))
                .pageSource(MCPToolUtils.getInteger(args, "pageSource", 1))
                .robotLog(MCPToolUtils.getInteger(args, "robotLog", 1))
                .consoleLog(MCPToolUtils.getInteger(args, "consoleLog", 1))
                .timeout(MCPToolUtils.getString(args, "timeout", "30000"))
                .retries(MCPToolUtils.getInteger(args, "retries", 0))
                .priority(MCPToolUtils.getInteger(args, "priority", 0))
                .manualUrl(MCPToolUtils.getInteger(args, "manualUrl", 0))
                .manualUrlParameters(manualUrlParameters)
                .build();

        Principal principal = () -> user;

        QueuedExecutionResult result;
        try {
            result = queuedExecutionService.addTestcasesToExecutionQueue(queuedExecution, principal);
        } catch (RuntimeException e) {
            return MCPToolUtils.errorText("Unable to queue testcase execution: " + e.getMessage());
        }

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("tag", result.getTag());
        response.put("nbExecutions", result.getNbExecutions());
        if (robotIgnored) {
            // Stated explicitly so a failing service test is never blamed on the robot that was
            // named for it : for these application types the engine never used it.
            response.put("robotIgnored", true);
            response.put("robotNote", "Application type '" + applicationType + "' is not driven by a robot. "
                    + "The execution engine ignored the robots parameter and queued with an empty robot, "
                    + "so the robot cannot be the cause if this run fails.");
        }
        response.put("queuedEntries", result.getQueuedEntries());
        if (result.getNbExecutions() == 0) {
            response.put("testcasesNotExist", result.getTestcasesNotExist());
            response.put("testcasesNotActive", result.getTestcasesNotActive());
            response.put("testcasesNotAllowedOnEnvironment", result.getTestcasesNotAllowedOnEnvironment());
            response.put("environmentsNotExistOrNotActive", result.getEnvironmentsNotExistOrNotActive());
            response.put("robotsMissing", result.getRobotsMissing());
        }
        response.put("messages", result.getMessages());

        return MCPToolUtils.successJson(response);
    }

}