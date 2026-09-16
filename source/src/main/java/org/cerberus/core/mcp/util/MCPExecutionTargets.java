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
package org.cerberus.core.mcp.util;

import org.cerberus.core.crud.entity.Application;
import org.cerberus.core.crud.entity.Robot;
import org.cerberus.core.crud.entity.TestCase;
import org.cerberus.core.crud.service.IApplicationService;
import org.cerberus.core.crud.service.ICountryEnvParamService;
import org.cerberus.core.crud.service.IRobotService;
import org.cerberus.core.crud.service.ITestCaseCountryService;
import org.cerberus.core.crud.service.ITestCaseService;
import org.cerberus.core.exception.CerberusException;
import org.cerberus.core.util.answer.AnswerItem;
import org.json.JSONObject;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Works out where a testcase can actually run: which countries it declares, which environments are
 * live for each, and which robots the engine would accept.
 *
 * <p>Shared by the tool that answers the question ({@code cerberus_testcase_execution_targets}) and
 * by the one that queues a run, so a run refused for a missing country or environment can say what
 * the valid ones are in the same breath instead of sending the caller off to ask. A second copy of
 * these rules would drift from the engine at a different rate than the first, and the two tools
 * would then disagree about what is runnable.</p>
 */
@Component
public class MCPExecutionTargets {

    private final ITestCaseService testCaseService;
    private final ITestCaseCountryService testCaseCountryService;
    private final IApplicationService applicationService;
    private final ICountryEnvParamService countryEnvParamService;
    private final IRobotService robotService;

    public MCPExecutionTargets(ITestCaseService testCaseService,
                               ITestCaseCountryService testCaseCountryService,
                               IApplicationService applicationService,
                               ICountryEnvParamService countryEnvParamService,
                               IRobotService robotService) {
        this.testCaseService = testCaseService;
        this.testCaseCountryService = testCaseCountryService;
        this.applicationService = applicationService;
        this.countryEnvParamService = countryEnvParamService;
        this.robotService = robotService;
    }

    /**
     * Everything known about where one testcase can run.
     *
     * @param error             {@code null} unless the testcase or its application cannot be read.
     * @param declaredCountries the countries the testcase itself declares.
     * @param countries         one entry per declared country, with its usable environments.
     * @param runnable          the flat country/environment pairs that would actually start.
     * @param robots            the robots the engine would accept; empty when none is needed.
     */
    public record Targets(String error,
                          TestCase testCase,
                          String application,
                          String system,
                          String applicationType,
                          List<String> declaredCountries,
                          List<Map<String, Object>> countries,
                          List<Map<String, Object>> runnable,
                          boolean robotRequired,
                          List<Map<String, Object>> robots) {

        public boolean failed() {
            return error != null;
        }

        static Targets of(String error) {
            return new Targets(error, null, "", "", "", List.of(), List.of(), List.of(), false, List.of());
        }

        /** The environments that appear for at least one country, deduplicated. */
        public List<String> environmentNames() {
            return runnable.stream()
                    .map(pair -> String.valueOf(pair.get("environment")))
                    .distinct()
                    .toList();
        }

        /** The robot names the engine would accept. */
        public List<String> robotNames() {
            return robots.stream().map(robot -> String.valueOf(robot.get("robot"))).toList();
        }
    }

    /**
     * Resolves the targets of a testcase.
     *
     * @return the targets; {@link Targets#failed()} when the testcase or its application is missing,
     * with an empty application handled as a successful result carrying nothing runnable — a
     * testcase without an application is a real state to report, not a lookup failure.
     */
    public Targets resolve(String testFolder, String testcase) {
        AnswerItem<TestCase> testCaseAnswer = testCaseService.readByKey(testFolder, testcase);
        if (!testCaseAnswer.isCodeStringEquals("OK") || testCaseAnswer.getItem() == null) {
            return Targets.of("Testcase does not exist: testFolder=" + testFolder + " testcase=" + testcase);
        }

        TestCase testCase = testCaseAnswer.getItem();
        String applicationName = MCPToolUtils.nullSafe(testCase.getApplication());
        if (applicationName.isBlank()) {
            return new Targets(null, testCase, "", "", "", List.of(), List.of(), List.of(), false, List.of());
        }

        AnswerItem<Application> applicationAnswer = applicationService.readByKey(applicationName);
        if (!applicationAnswer.isCodeStringEquals("OK") || applicationAnswer.getItem() == null) {
            return Targets.of("Application '" + applicationName
                    + "' referenced by the testcase does not exist.");
        }

        Application application = applicationAnswer.getItem();
        String system = MCPToolUtils.nullSafe(application.getSystem());
        String applicationType = MCPToolUtils.nullSafe(application.getType());

        List<String> declaredCountries = testCaseCountryService.findListOfCountryByTestTestCase(testFolder, testcase);
        if (declaredCountries == null) {
            declaredCountries = List.of();
        }

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

        boolean robotRequired = requiresRobot(applicationType);
        List<Map<String, Object>> robots = robotRequired ? resolveRobots(applicationType) : List.of();

        return new Targets(null, testCase, applicationName, system, applicationType,
                declaredCountries, countries, runnable, robotRequired, robots);
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
            map.put("environment", activeEnvironment.optString("environment", ""));
            map.put("build", activeEnvironment.optString("build", ""));
            map.put("revision", activeEnvironment.optString("revision", ""));
            map.put("url", activeEnvironment.optString("url", ""));
            environments.add(map);
        }

        return environments;
    }

    /**
     * Returns whether an application of this type needs a robot at all.
     *
     * <p>Mirrors the guard in {@code QueuedExecutionService.addToQueue}: only GUI, APK, IPA and FAT
     * applications are driven by a robot. For every other type the engine forces the robot to an
     * empty value, so offering a robot list would be misleading.</p>
     */
    public boolean requiresRobot(String applicationType) {
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
     * filter on the flag either: an inactive robot still queues. The flag is reported so the caller
     * can prefer an active one.</p>
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
