/*
 * Cerberus Copyright (C) 2013 - 2026 cerberustesting
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
package org.cerberus.core.api.services;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import org.cerberus.core.crud.entity.TestCase;
import org.cerberus.core.crud.entity.TestCaseCountry;
import org.cerberus.core.crud.entity.TestCaseCountryProperties;
import org.cerberus.core.crud.entity.TestCaseStep;
import org.cerberus.core.crud.entity.TestCaseStepAction;
import org.cerberus.core.crud.entity.TestCaseStepActionControl;
import org.json.JSONArray;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

/**
 * Builds the GUI smoke testcase: sign in through the Keycloak login form, then
 * open every page listed in {@code bootstrap/smoke-gui-pages.json} and check its
 * title and its HTTP status.
 */
@Component
public class SmokeGuiTestcaseFactory {

    public static final String TEST_FOLDER = "Cerberus Smoke Tests";
    public static final String TESTCASE_ID = "0001A";
    public static final String USER_PROPERTY = "USER";

    private static final String PAGES_RESOURCE = "bootstrap/smoke-gui-pages.json";
    private static final String HTTP_JS = "return String(performance.getEntriesByType('navigation')[0].responseStatus);";
    private static final String TITLE_JS = "return document.title;";

    private final ObjectMapper mapper = new ObjectMapper();

    public TestCase build(String system, String application, String country, String dataLibName, String login) {
        JsonNode pages = readPages();

        List<TestCaseStep> steps = new ArrayList<>();
        List<TestCaseCountryProperties> properties = new ArrayList<>();
        steps.add(signInStep(login));

        int stepId = 10;
        for (JsonNode stepNode : pages.get("steps")) {
            stepId += 10;
            List<TestCaseStepAction> actions = new ArrayList<>();
            int actionId = 0;
            for (JsonNode page : stepNode.get("pages")) {
                actionId += 10;
                String key = propertyKey(page.get("path").asText());
                properties.add(property(country, "NAV_" + key + "_HTTP", HTTP_JS, login));
                properties.add(property(country, "NAV_" + key + "_TITLE", TITLE_JS, login));
                actions.add(pageAction(page, key, stepId, actionId, login));
            }
            steps.add(step(stepId, stepNode.get("description").asText(), actions, login));
        }
        properties.add(TestCaseCountryProperties.builder()
                .country(country).property(USER_PROPERTY).type("getFromDataLib").value1(dataLibName)
                .nature("STATIC").length("0").rowLimit(1).description("Smoke login data definition.")
                .usrCreated(login).build());

        TestCaseCountry testCaseCountry = TestCaseCountry.builder()
                .country(country).testCaseCountryProperty(properties).usrCreated(login).build();

        return TestCase.builder()
                .test(TEST_FOLDER).testcase(TESTCASE_ID).application(application).system(system)
                .description("Smoke - sign in, then open every menu page and verify its title and HTTP 200")
                .detailedDescription("Created by the instance bootstrap. Requires an administrator account.")
                .type("AUTOMATED").status("WORKING").priority(1)
                .isActive(true).isActiveQA(true).isActiveUAT(true).isActivePROD(true)
                .conditionOperator("always").conditionValue1("").conditionValue2("").conditionValue3("")
                .conditionOptions(new JSONArray()).bugs(new JSONArray())
                .origine("").refOrigine("").comment("").implementer("").executor("").userAgent("").screenSize("")
                .fromMajor("").fromMinor("").toMajor("").toMinor("").targetMajor("").targetMinor("")
                .usrCreated(login)
                .steps(steps)
                .testCaseCountries(List.of(testCaseCountry))
                .dependencies(new ArrayList<>()).testCaseLabels(new ArrayList<>())
                .build();
    }

    private TestCaseStep signInStep(String login) {
        List<TestCaseStepAction> actions = new ArrayList<>();
        actions.add(action(10, 10, "openUrlWithBase", "/", "", "Open login page", List.of(), login));
        actions.add(action(10, 20, "type", "id=username", "%property." + USER_PROPERTY + "%", "Enter username", List.of(), login));
        actions.add(action(10, 30, "type", "id=password", "%USER(Password)%", "Enter password", List.of(), login));
        actions.add(action(10, 40, "click", "id=kc-login", "", "Sign in", List.of(
                control(10, 40, 10, "verifyElementPresent", "css=meta[name=\"page\"][content=\"Homepage\"]", "",
                        "Wait until authenticated homepage is loaded", login)), login));
        return step(10, "Authentication - sign in to Cerberus", actions, login);
    }

    private TestCaseStepAction pageAction(JsonNode page, String key, int stepId, int actionId, String login) {
        boolean exact = page.path("exactTitle").asBoolean(false);
        String title = page.get("title").asText();
        List<TestCaseStepActionControl> controls = List.of(
                control(stepId, actionId, 10, exact ? "verifyStringEqual" : "verifyStringContains",
                        "%property.NAV_" + key + "_TITLE%", title, "Verify page title: " + title, login),
                control(stepId, actionId, 20, "verifyStringEqual",
                        "%property.NAV_" + key + "_HTTP%", "200", "Verify document HTTP status: 200", login));
        TestCaseStepAction action = action(stepId, actionId, "openUrlWithBase", page.get("path").asText(), "",
                page.get("label").asText(), controls, login);
        return action;
    }

    private TestCaseStep step(int stepId, String description, List<TestCaseStepAction> actions, String login) {
        return TestCaseStep.builder()
                .stepId(stepId).sort(stepId).description(description).loop("onceIfConditionTrue")
                .conditionOperator("always").conditionValue1("").conditionValue2("").conditionValue3("")
                .conditionOptions(new JSONArray())
                .libraryStepTest("").libraryStepTestcase("").libraryStepStepId(0)
                .usrCreated(login).actions(actions).build();
    }

    private TestCaseStepAction action(int stepId, int actionId, String action, String value1, String value2,
            String description, List<TestCaseStepActionControl> controls, String login) {
        TestCaseStepAction result = TestCaseStepAction.builder()
                .stepId(stepId).actionId(actionId).sort(actionId).action(action)
                .value1(value1).value2(value2).value3("").description(description)
                .conditionOperator("always").conditionValue1("").conditionValue2("").conditionValue3("")
                .conditionOptions(new JSONArray()).options(new JSONArray())
                .isFatal(true).usrCreated(login).build();
        result.setControls(new ArrayList<>(controls));
        return result;
    }

    private TestCaseStepActionControl control(int stepId, int actionId, int controlId, String control,
            String value1, String value2, String description, String login) {
        return TestCaseStepActionControl.builder()
                .stepId(stepId).actionId(actionId).controlId(controlId).sort(controlId).control(control)
                .value1(value1).value2(value2).value3("").description(description)
                .conditionOperator("always").conditionValue1("").conditionValue2("").conditionValue3("")
                .conditionOptions(new JSONArray()).options(new JSONArray())
                .isFatal(false).usrCreated(login).build();
    }

    private TestCaseCountryProperties property(String country, String name, String script, String login) {
        return TestCaseCountryProperties.builder()
                .country(country).property(name).type("getFromJS").value1(script).value2("").value3("")
                .nature("STATIC").length("0").rowLimit(0).description("").database("")
                .usrCreated(login).build();
    }

    /** "/documentation/D1/documentation_en.html" becomes "_documentation_D1_documentation_en_html". */
    private String propertyKey(String path) {
        return path.replaceAll("[^A-Za-z0-9]", "_");
    }

    private JsonNode readPages() {
        try (InputStream in = new ClassPathResource(PAGES_RESOURCE).getInputStream()) {
            return mapper.readTree(in);
        } catch (IOException ex) {
            throw new IllegalStateException("Unable to read " + PAGES_RESOURCE, ex);
        }
    }
}