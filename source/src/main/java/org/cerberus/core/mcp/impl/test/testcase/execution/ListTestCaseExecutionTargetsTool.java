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
import org.cerberus.core.crud.entity.Application;
import org.cerberus.core.crud.entity.Robot;
import org.cerberus.core.crud.entity.TestCase;
import org.cerberus.core.crud.service.IApplicationService;
import org.cerberus.core.crud.service.ICountryEnvParamService;
import org.cerberus.core.crud.service.IRobotService;
import org.cerberus.core.crud.service.ITestCaseCountryService;
import org.cerberus.core.crud.service.ITestCaseService;
import org.cerberus.core.exception.CerberusException;
import org.cerberus.core.mcp.MCPTool;
import org.cerberus.core.mcp.util.MCPLogUtils;
import org.cerberus.core.mcp.util.MCPToolUtils;
import org.cerberus.core.util.answer.AnswerItem;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * MCP tool that reports where a testcase can actually be executed, under the tool name
 * {@code cerberus_testcase_execution_targets}.
 *
 * <p>{@code cerberus_testcase_execution_create} requires a country, an environment and a robot, and
 * silently queues nothing when the combination does not exist. The three values are not free
 * choices: a country must be declared on the testcase, an environment only exists for a given
 * (system, country) pair <em>and</em> must be configured for the application, and a robot must
 * match the application type. Nothing in the create tool exposes those constraints, so an agent
 * has to guess — and the intuitive guess is wrong whenever a team hosts its environments under a
 * country code that is not the obvious one for the market being tested.</p>
 *
 * <p>This tool resolves the whole chain server-side and returns the combinations that will work,
 * so the caller picks from a list instead of guessing. When a testcase declares a country that has
 * no configured environment, that country is reported with an empty environment list rather than
 * omitted: knowing that a country is declared but unusable is what explains a run that queued
 * nothing.</p>
 *
 * <p>Delegation: {@link ITestCaseService}, {@link ITestCaseCountryService},
 * {@link ICountryEnvParamService#findActiveEnvironmentBySystemCountryApplication(String, String, String)}
 * and {@link IRobotService}.</p>
 */
@Component
public class ListTestCaseExecutionTargetsTool implements MCPTool {

    private static final String TOOL_NAME = "cerberus_testcase_execution_targets";

    private final ITestCaseService testCaseService;
    private final ITestCaseCountryService testCaseCountryService;
    private final IApplicationService applicationService;
    private final ICountryEnvParamService countryEnvParamService;
    private final IRobotService robotService;
    private final MCPLogUtils mcpLogUtils;

    public ListTestCaseExecutionTargetsTool(ITestCaseService testCaseService,
                                            ITestCaseCountryService testCaseCountryService,
                                            IApplicationService applicationService,
                                            ICountryEnvParamService countryEnvParamService,
                                            IRobotService robotService,
                                            MCPLogUtils mcpLogUtils) {
        this.testCaseService = testCaseService;
        this.testCaseCountryService = testCaseCountryService;
        this.applicationService = applicationService;
        this.countryEnvParamService = countryEnvParamService;
        this.robotService = robotService;
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
        properties.put("testFolder", Map.of(
                "type", "string",
                "description", "Test folder the testcase belongs to."
        ));
        properties.put("testcase", Map.of(
                "type", "string",
                "description", "Testcase identifier to resolve execution targets for."
        ));

        return new McpSchema.Tool(
                TOOL_NAME,
                null,
                """
                Returns the country / environment / robot combinations a testcase can actually be executed on.

                Call this before cerberus_testcase_execution_create, every time you do not already know the exact
                country, environment and robot to use. Do not guess them: a country code that looks right for the
                market under test is often not the one the environments are declared under, and queuing a run with
                an unconfigured combination silently creates nothing.

                The response gives, per country declared on the testcase, the environments configured for its
                application, plus the robots whose type matches that application. "runnable" lists the pairs that
                are ready to execute; a country with an empty environment list is declared on the testcase but has
                no environment configured, so it cannot be used.

                If several combinations are valid and the user has not said which one they want, ask them rather
                than picking one.
                """,
                new McpSchema.JsonSchema(
                        "object",
                        properties,
                        List.of("testFolder", "testcase"),
                        null,
                        null,
                        null
                ),
                null,
                MCPToolUtils.readOnlyAnnotations("List testcase execution targets", false),
                null
        );
    }

    /**
     * Resolves the testcase, its application, its declared countries and the matching robots.
     *
     * @param args tool arguments extracted from the MCP request (may be empty but never null).
     * @return a {@link McpSchema.CallToolResult} carrying the executable targets, or an error.
     */
    private McpSchema.CallToolResult execute(Map<String, Object> args) {
        String testFolder = MCPToolUtils.getString(args, "testFolder", "").trim();
        String testcaseId = MCPToolUtils.getString(args, "testcase", "").trim();

        mcpLogUtils.call(TOOL_NAME, "testcase_execution_targets",
                String.format("MCP tool %s called with testFolder=%s testcase=%s", TOOL_NAME, testFolder, testcaseId));

        if (testFolder.isBlank()) {
            return MCPToolUtils.errorText("Missing required parameter: testFolder");
        }
        if (testcaseId.isBlank()) {
            return MCPToolUtils.errorText("Missing required parameter: testcase");
        }

        AnswerItem<TestCase> testCaseAnswer = testCaseService.readByKey(testFolder, testcaseId);
        if (!testCaseAnswer.isCodeStringEquals("OK") || testCaseAnswer.getItem() == null) {
            return MCPToolUtils.errorText("Testcase does not exist: testFolder=" + testFolder + " testcase=" + testcaseId);
        }

        TestCase testCase = testCaseAnswer.getItem();
        String applicationName = MCPToolUtils.nullSafe(testCase.getApplication());

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("testFolder", testFolder);
        response.put("testcase", testcaseId);
        response.put("application", applicationName);
        response.put("testcaseStatus", MCPToolUtils.nullSafe(testCase.getStatus()));
        response.put("testcaseIsActive", testCase.isActive());

        if (applicationName.isBlank()) {
            response.put("countries", List.of());
            response.put("robots", List.of());
            response.put("runnable", List.of());
            response.put("message", "This testcase has no application attached, so no environment can be resolved. "
                    + "Attach an application to it before trying to execute it.");
            return MCPToolUtils.successJson(response);
        }

        AnswerItem<Application> applicationAnswer = applicationService.readByKey(applicationName);
        if (!applicationAnswer.isCodeStringEquals("OK") || applicationAnswer.getItem() == null) {
            return MCPToolUtils.errorText("Application '" + applicationName + "' referenced by the testcase does not exist.");
        }

        Application application = applicationAnswer.getItem();
        String system = MCPToolUtils.nullSafe(application.getSystem());
        String applicationType = MCPToolUtils.nullSafe(application.getType());

        response.put("system", system);
        response.put("applicationType", applicationType);

        List<String> declaredCountries = testCaseCountryService.findListOfCountryByTestTestCase(testFolder, testcaseId);
        List<Map<String, Object>> countries = new ArrayList<>();
        List<Map<String, Object>> runnable = new ArrayList<>();

        for (String country : declaredCountries) {
            List<Map<String, Object>> environments = resolveEnvironments(system, country, applicationName);

            Map<String, Object> countryEntry = new LinkedHashMap<>();
            countryEntry.put("country", country);
            countryEntry.put("environments", environments);
            countries.add(countryEntry);

            for (Map<String, Object> environment : environments) {
                Map<String, Object> pair = new LinkedHashMap<>();
                pair.put("country", country);
                pair.put("environment", environment.get("environment"));
                pair.put("url", environment.get("url"));
                runnable.add(pair);
            }
        }

        boolean requiresRobot = requiresRobot(applicationType);
        List<Map<String, Object>> robots = requiresRobot ? resolveRobots(applicationType) : List.of();

        response.put("countries", countries);
        response.put("robotRequired", requiresRobot);
        response.put("robots", robots);
        response.put("runnable", runnable);

        if (runnable.isEmpty()) {
            response.put("message", declaredCountries.isEmpty()
                    ? "No country is declared on this testcase. Add one with cerberus_testcase_country_create before executing it."
                    : "The countries declared on this testcase (" + declaredCountries + ") have no active environment "
                            + "configured for application '" + applicationName + "' in system '" + system + "'. "
                            + "Check which country the environments are actually declared under with "
                            + "cerberus_country_environment_parameters_list.");
        } else if (!requiresRobot) {
            response.put("message", "Application type '" + applicationType + "' is not driven by a robot, so the "
                    + "execution engine ignores the robot. cerberus_testcase_execution_create still requires the "
                    + "robots parameter: pass any existing robot name.");
        } else if (robots.isEmpty()) {
            response.put("message", "No robot matches application type '" + applicationType + "', so this testcase "
                    + "cannot be executed automatically. A robot matches when its type equals the application type "
                    + "exactly (case-sensitive) or is left empty. Create one with cerberus_robot_create, or run with "
                    + "manualExecution set to 'Y'.");
        }

        return MCPToolUtils.successJson(response);
    }

    /**
     * Lists the active environments configured for one (system, country, application) triplet.
     *
     * <p>An environment is only usable when it is active at both levels: the country/environment
     * itself, and the per-application parameters that carry the URL. The delegated service
     * intersects the two, which is exactly the condition the execution engine applies.</p>
     *
     * @return one ordered map per usable environment, possibly empty.
     */
    private List<Map<String, Object>> resolveEnvironments(String system, String country, String application) {
        List<Map<String, Object>> environments = new ArrayList<>();

        List<JSONObject> activeEnvironments;
        try {
            activeEnvironments = countryEnvParamService
                    .findActiveEnvironmentBySystemCountryApplication(system, country, application);
        } catch (CerberusException e) {
            // A country with no configuration is a normal, expected outcome here — reporting the
            // country with no environment is more useful than failing the whole lookup.
            return environments;
        }

        for (JSONObject activeEnvironment : activeEnvironments) {
            Map<String, Object> map = new LinkedHashMap<>();
            try {
                map.put("environment", activeEnvironment.optString("environment", ""));
                map.put("build", activeEnvironment.optString("build", ""));
                map.put("revision", activeEnvironment.optString("revision", ""));
                map.put("url", activeEnvironment.optString("url", ""));
            } catch (JSONException e) {
                continue;
            }
            environments.add(map);
        }

        return environments;
    }

    /**
     * Returns whether an application of this type needs a robot at all.
     *
     * <p>Mirrors the guard in
     * {@code QueuedExecutionService.addToQueue}: only GUI, APK, IPA and FAT applications are
     * driven by a robot. For every other type the engine forces the robot to an empty value, so
     * offering a robot list would be misleading.</p>
     */
    private boolean requiresRobot(String applicationType) {
        return Application.TYPE_GUI.equalsIgnoreCase(applicationType)
                || Application.TYPE_APK.equalsIgnoreCase(applicationType)
                || Application.TYPE_IPA.equalsIgnoreCase(applicationType)
                || Application.TYPE_FAT.equalsIgnoreCase(applicationType);
    }

    /**
     * Lists the robots the execution engine would accept for an application of the given type.
     *
     * <p>The matching rule is copied from {@code QueuedExecutionService.addToQueue}:
     * {@code "".equals(robot.getType()) || app.getType().equals(robot.getType())}. Two details of
     * that rule matter and are reproduced verbatim rather than rationalised:</p>
     * <ul>
     *   <li>a robot with an <em>empty</em> type is a wildcard that matches every application —
     *       excluding it would report "no robot available" for an instance whose only robot is
     *       untyped, which is a common setup;</li>
     *   <li>the comparison is case-sensitive, so a robot typed "gui" genuinely will not run a GUI
     *       application. Matching case-insensitively here would promise a run that the engine then
     *       refuses.</li>
     * </ul>
     *
     * <p>Inactive robots are listed rather than filtered out, because the queue lookup does not
     * filter on the flag either: an inactive robot still queues. The flag is reported so the
     * caller can prefer an active one.</p>
     *
     * @return one ordered map per robot the engine would accept, possibly empty.
     */
    private List<Map<String, Object>> resolveRobots(String applicationType) {
        List<Map<String, Object>> robots = new ArrayList<>();

        List<Robot> allRobots = robotService.readAll().getDataList();
        if (allRobots == null) {
            return robots;
        }

        for (Robot robot : allRobots) {
            String robotType = robot.getType();
            boolean accepted = "".equals(robotType) || applicationType.equals(robotType);
            if (!accepted) {
                continue;
            }

            Map<String, Object> map = new LinkedHashMap<>();
            map.put("robot", MCPToolUtils.nullSafe(robot.getRobot()));
            map.put("type", MCPToolUtils.nullSafe(robotType));
            map.put("active", robot.isActive());
            map.put("platform", MCPToolUtils.nullSafe(robot.getPlatform()));
            map.put("browser", MCPToolUtils.nullSafe(robot.getBrowser()));
            map.put("description", MCPToolUtils.nullSafe(robot.getDescription()));
            robots.add(map);
        }

        return robots;
    }

}
