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
package org.cerberus.core.mcp.impl.test.testcase.step;

import io.modelcontextprotocol.server.McpServerFeatures;
import io.modelcontextprotocol.spec.McpSchema;
import org.cerberus.core.api.dto.testcasestep.TestcaseStepMapperV001;
import org.cerberus.core.crud.entity.TestCase;
import org.cerberus.core.crud.entity.TestCaseCountryProperties;
import org.cerberus.core.crud.entity.TestCaseStep;
import org.cerberus.core.crud.service.ITestCaseCountryPropertiesService;
import org.cerberus.core.crud.service.ITestCaseCountryService;
import org.cerberus.core.crud.service.ITestCaseService;
import org.cerberus.core.crud.service.ITestCaseStepService;
import org.cerberus.core.mcp.MCPTool;
import org.cerberus.core.mcp.util.MCPLogUtils;
import org.cerberus.core.mcp.util.MCPOrderingService;
import org.cerberus.core.mcp.util.MCPStepPropertyUsage;
import org.cerberus.core.mcp.util.MCPToolUtils;
import org.cerberus.core.util.answer.Answer;
import org.cerberus.core.util.answer.AnswerItem;
import org.cerberus.core.util.answer.AnswerList;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * MCP tool that adds a step running an existing library step, under the tool name
 * {@code cerberus_testcase_step_library_use}.
 *
 * <p>A Cerberus step either owns its actions or delegates them to a step defined elsewhere. The
 * second kind is how a shared sequence — signing in, creating a customer, accepting a cookie
 * banner — is written once and run from every testcase that needs it, and it is the only way to
 * fix such a sequence in one place.</p>
 *
 * <p>Until now the tools could only create the first kind. A testcase missing a shared prerequisite
 * could not be repaired as it should be — by inserting the shared step where it belongs — leaving
 * only workarounds elsewhere in the scenario that do not say what they are working around.</p>
 *
 * <p>Mirrors the {@code UseTestCaseStep} servlet the test case editor posts to, including its
 * option to bring over the properties the borrowed step consumes: a step that reads
 * {@code %property.LOGIN%} fails in a testcase where {@code LOGIN} is not defined, and that failure
 * reads as a broken step rather than as a missing property.</p>
 */
@Component
public class UseLibraryStepTool implements MCPTool {

    private static final String TOOL_NAME = "cerberus_testcase_step_library_use";

    /** Step ids advance in tens, as everywhere else, so there is room to renumber. */
    private static final int STEP_ID_INCREMENT = 10;

    private final ITestCaseService testCaseService;
    private final ITestCaseStepService testCaseStepService;
    private final ITestCaseCountryService testCaseCountryService;
    private final ITestCaseCountryPropertiesService testCaseCountryPropertiesService;
    private final MCPStepPropertyUsage stepPropertyUsage;
    private final MCPOrderingService orderingService;
    private final TestcaseStepMapperV001 mapper;
    private final MCPLogUtils mcpLogUtils;

    public UseLibraryStepTool(ITestCaseService testCaseService,
                              ITestCaseStepService testCaseStepService,
                              ITestCaseCountryService testCaseCountryService,
                              ITestCaseCountryPropertiesService testCaseCountryPropertiesService,
                              MCPStepPropertyUsage stepPropertyUsage,
                              MCPOrderingService orderingService,
                              TestcaseStepMapperV001 mapper,
                              MCPLogUtils mcpLogUtils) {
        this.testCaseService = testCaseService;
        this.testCaseStepService = testCaseStepService;
        this.testCaseCountryService = testCaseCountryService;
        this.testCaseCountryPropertiesService = testCaseCountryPropertiesService;
        this.stepPropertyUsage = stepPropertyUsage;
        this.orderingService = orderingService;
        this.mapper = mapper;
        this.mcpLogUtils = mcpLogUtils;
    }

    @Override
    public McpServerFeatures.SyncToolSpecification toToolSpecification() {
        return new McpServerFeatures.SyncToolSpecification(
                createTool(),
                (exchange, request) -> execute(MCPToolUtils.argumentsOrEmpty(request.arguments()))
        );
    }

    private McpSchema.Tool createTool() {
        Map<String, Object> properties = new LinkedHashMap<>();
        properties.put("testFolder", Map.of(
                "type", "string",
                "description", "Test folder of the testcase receiving the step."
        ));
        properties.put("testcase", Map.of(
                "type", "string",
                "description", "Testcase receiving the step."
        ));
        properties.put("libraryTestFolder", Map.of(
                "type", "string",
                "description", "Test folder of the library step to run. From cerberus_step_library_list."
        ));
        properties.put("libraryTestcase", Map.of(
                "type", "string",
                "description", "Testcase owning the library step to run. From cerberus_step_library_list."
        ));
        properties.put("libraryStepId", Map.of(
                "type", "integer",
                "description", "Step id of the library step to run. From cerberus_step_library_list."
        ));
        properties.put("description", Map.of(
                "type", "string",
                "description", "Description of the new step as it reads in this testcase. Defaults to the "
                        + "library step's own description."
        ));
        properties.put("position", Map.of(
                "type", "integer",
                "description", "Where the step goes in the testcase, 1 being first. Omit it to append at the "
                        + "end. A shared prerequisite usually belongs at the top, not at the bottom."
        ));
        properties.put("importProperties", Map.of(
                "type", "boolean",
                "description", "Copy the properties the library step reads into this testcase, for the "
                        + "countries this testcase declares. Defaults to true. A property that already exists "
                        + "here is left untouched, never overwritten. Turn this off only when you know this "
                        + "testcase already defines everything the step reads."
        ));

        return new McpSchema.Tool(
                TOOL_NAME,
                null,
                """
                Adds a step to a testcase that runs an existing library step, instead of actions of its own.

                Use this whenever a testcase needs a sequence that already exists somewhere else — signing
                in, creating a customer, dismissing a cookie banner. The step keeps pointing at the
                original, so fixing the original fixes every testcase using it. Copying the actions instead
                creates a second copy that will drift, and the next fix will miss it.

                Call cerberus_step_library_list first to find the library step and its exact coordinates.

                By default the properties the borrowed step reads are copied into this testcase for the
                countries it declares, because a step reading %property.X% fails where X is not defined and
                that failure looks like a broken step rather than a missing property. Properties already
                defined here are never overwritten.

                Pass position to put the step where it belongs rather than at the end; a prerequisite added
                last runs last, which is the same as not adding it.

                To create a step with its own actions, use cerberus_testcase_step_create instead.
                """,
                new McpSchema.JsonSchema(
                        "object",
                        properties,
                        List.of("testFolder", "testcase", "libraryTestFolder", "libraryTestcase", "libraryStepId"),
                        null,
                        null,
                        null
                ),
                null,
                MCPToolUtils.createAnnotations("Use a library step", false),
                null
        );
    }

    private McpSchema.CallToolResult execute(Map<String, Object> args) {
        String testFolder = MCPToolUtils.getString(args, "testFolder", "").trim();
        String testcaseId = MCPToolUtils.getString(args, "testcase", "").trim();
        String libraryTestFolder = MCPToolUtils.getString(args, "libraryTestFolder", "").trim();
        String libraryTestcase = MCPToolUtils.getString(args, "libraryTestcase", "").trim();
        int libraryStepId = MCPToolUtils.getInteger(args, "libraryStepId", -1);
        String description = MCPToolUtils.getString(args, "description", "").trim();
        int position = MCPToolUtils.getInteger(args, "position", 0);
        boolean importProperties = MCPToolUtils.getBoolean(args, "importProperties", true);

        mcpLogUtils.call(TOOL_NAME, "testcase_step_library_use",
                String.format("MCP tool %s called with testFolder=%s testcase=%s library=%s/%s/%d",
                        TOOL_NAME, testFolder, testcaseId, libraryTestFolder, libraryTestcase, libraryStepId));

        if (testFolder.isBlank()) {
            return MCPToolUtils.errorText("Missing required parameter: testFolder");
        }
        if (testcaseId.isBlank()) {
            return MCPToolUtils.errorText("Missing required parameter: testcase");
        }
        if (libraryTestFolder.isBlank() || libraryTestcase.isBlank() || libraryStepId < 0) {
            return MCPToolUtils.errorText("A library step is named by all three of libraryTestFolder, "
                    + "libraryTestcase and libraryStepId. Call cerberus_step_library_list to get them.");
        }

        if (testFolder.equals(libraryTestFolder) && testcaseId.equals(libraryTestcase)) {
            // The engine resolves a used step by reading the actions at the library coordinates, so a
            // testcase pointing at its own step would work, but nothing is shared and the scenario
            // becomes impossible to read. Refusing is kinder than letting it through.
            return MCPToolUtils.errorText("A testcase cannot use its own step as a library step. "
                    + "To repeat a sequence inside one testcase, add the actions to a step of its own.");
        }

        AnswerItem<TestCase> testCaseAnswer = testCaseService.readByKey(testFolder, testcaseId);
        if (!testCaseAnswer.isCodeStringEquals("OK") || testCaseAnswer.getItem() == null) {
            return MCPToolUtils.errorText("Testcase does not exist: testFolder=" + testFolder
                    + " testcase=" + testcaseId);
        }

        TestCaseStep libraryStep = testCaseStepService.findTestCaseStep(libraryTestFolder, libraryTestcase, libraryStepId);
        if (libraryStep == null) {
            return MCPToolUtils.errorText("Library step does not exist: " + libraryTestFolder + "/"
                    + libraryTestcase + " step " + libraryStepId
                    + ". Call cerberus_step_library_list to find the right coordinates.");
        }
        if (libraryStep.isUsingLibraryStep()) {
            // Resolution is one level deep: the engine reads the actions at the coordinates it is
            // given and does not follow a second hop, so this step would run nothing at all.
            return MCPToolUtils.errorText("Step " + libraryStepId + " of " + libraryTestFolder + "/"
                    + libraryTestcase + " is itself a step that uses another library step, and Cerberus does "
                    + "not follow that a second time — the new step would run no action. Point at the "
                    + "original instead: " + libraryStep.getLibraryStepTest() + "/"
                    + libraryStep.getLibraryStepTestcase() + " step " + libraryStep.getLibraryStepStepId() + ".");
        }

        AnswerList<TestCaseStep> stepsAnswer = testCaseStepService.readByTestTestCase(testFolder, testcaseId);
        List<TestCaseStep> existingSteps = stepsAnswer.getDataList();
        int nextStepId = existingSteps == null || existingSteps.isEmpty()
                ? STEP_ID_INCREMENT
                : testCaseStepService.getMaxStepId(existingSteps) + STEP_ID_INCREMENT;

        TestCaseStep step = TestCaseStep.builder()
                .test(testFolder)
                .testcase(testcaseId)
                .stepId(nextStepId)
                .sort(nextStepId)
                .description(description.isBlank() ? MCPToolUtils.nullSafe(libraryStep.getDescription()) : description)
                // loop and condition come from the library step at execution time
                // (TestCaseStepService.modifyTestCaseStepDataFromUsedStep), so setting them here would
                // record values the engine then ignores.
                .loop(TestCaseStep.LOOP_ONCEIFCONDITIONTRUE)
                .conditionOperator("always")
                .isLibraryStep(false)
                .isUsingLibraryStep(true)
                .libraryStepTest(libraryTestFolder)
                .libraryStepTestcase(libraryTestcase)
                .libraryStepStepId(libraryStepId)
                .isExecutionForced(false)
                .usrCreated("MCP")
                .build();

        Answer answer = testCaseStepService.create(step);
        if (!answer.isCodeStringEquals("OK")) {
            return MCPToolUtils.errorText("Unable to create the step: " + answer.getMessageDescription());
        }

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("status", "created");
        response.put("step", mapper.toDTO(step));
        response.put("runs", Map.of(
                "testFolder", libraryTestFolder,
                "testcase", libraryTestcase,
                "stepId", libraryStepId));

        List<String> notes = new ArrayList<>();
        if (!libraryStep.isLibraryStep()) {
            notes.add("Step " + libraryStepId + " of " + libraryTestFolder + "/" + libraryTestcase
                    + " is not flagged as a library step, so it does not appear in cerberus_step_library_list "
                    + "and whoever maintains it has no sign that it is reused. It will run correctly. Set "
                    + "isLibraryStep on it with cerberus_testcase_step_update if the reuse is intentional.");
        }

        if (position > 0) {
            MCPOrderingService.Result placement = orderingService.placeStep(testFolder, testcaseId, nextStepId, position);
            if (placement.failed()) {
                response.put("status", "created_but_not_positioned");
                notes.add("The step was created at the end of the testcase but could not be moved to position "
                        + position + ": " + placement.error()
                        + " Use cerberus_testcase_step_reorder to place it.");
            } else {
                response.put("stepIds", placement.order());
            }
        }

        if (importProperties) {
            response.put("properties", importProperties(testFolder, testcaseId, libraryStep, notes));
        } else {
            notes.add("Properties were not imported. If the library step reads a property this testcase does "
                    + "not define, the run fails on property decoding — call "
                    + "cerberus_testcase_country_property_list with includeInherited to check.");
        }

        if (!notes.isEmpty()) {
            response.put("notes", notes);
        }

        mcpLogUtils.success(TOOL_NAME, "testcase_step_library_use",
                String.format("Step %d of %s/%s now runs library step %s/%s/%d",
                        nextStepId, testFolder, testcaseId, libraryTestFolder, libraryTestcase, libraryStepId));

        return MCPToolUtils.successJson(response);
    }

    /**
     * Copies the properties the borrowed step reads into the receiving testcase.
     *
     * <p>Runs per country: a property is defined per testcase and country, and the receiving
     * testcase may declare countries the library's owner does not. Only the countries both declare
     * can be served, and the ones that cannot are reported rather than passed over — a step that
     * works in FR and fails in NL is exactly the kind of result that gets read as flaky.</p>
     */
    private Map<String, Object> importProperties(String testFolder, String testcaseId,
                                                 TestCaseStep libraryStep, List<String> notes) {
        Map<String, Object> report = new LinkedHashMap<>();

        List<String> targetCountries = testCaseCountryService.findListOfCountryByTestTestCase(testFolder, testcaseId);
        if (targetCountries == null || targetCountries.isEmpty()) {
            notes.add("This testcase declares no country, so no property could be imported. Add a country "
                    + "with cerberus_testcase_country_create, then import the properties by calling this "
                    + "tool's logic again — or define them with cerberus_testcase_country_property_create.");
            return report;
        }

        // libraryStep is the original: a step that itself used another one was refused above, so its
        // own coordinates are where the actions live.
        Set<String> used = stepPropertyUsage.propertiesReadBy(
                libraryStep.getTest(), libraryStep.getTestcase(), libraryStep.getStepId());
        report.put("readByTheStep", used);
        if (used.isEmpty()) {
            return report;
        }

        List<TestCaseCountryProperties> toInsert = new ArrayList<>();
        List<String> imported = new ArrayList<>();
        List<String> alreadyDefined = new ArrayList<>();
        List<String> notAvailable = new ArrayList<>();

        for (String country : targetCountries) {
            List<TestCaseCountryProperties> sourceProperties = testCaseCountryPropertiesService
                    .findListOfPropertyPerTestTestCaseCountry(libraryStep.getTest(), libraryStep.getTestcase(), country);
            List<TestCaseCountryProperties> targetProperties = testCaseCountryPropertiesService
                    .findListOfPropertyPerTestTestCaseCountry(testFolder, testcaseId, country);

            Set<String> definedHere = new java.util.HashSet<>();
            if (targetProperties != null) {
                targetProperties.forEach(property -> definedHere.add(property.getProperty()));
            }
            Set<String> definedThere = new java.util.HashSet<>();
            if (sourceProperties != null) {
                sourceProperties.forEach(property -> definedThere.add(property.getProperty()));
            }

            for (String name : used) {
                if (definedHere.contains(name)) {
                    alreadyDefined.add(name + " (" + country + ")");
                    continue;
                }
                if (!definedThere.contains(name)) {
                    notAvailable.add(name + " (" + country + ")");
                    continue;
                }
                sourceProperties.stream()
                        .filter(property -> name.equals(property.getProperty()))
                        .findFirst()
                        .ifPresent(property -> {
                            // The entity is rewritten onto the receiving testcase; every other field —
                            // type, database, values, retries — is carried over as-is so the property
                            // resolves the same way it does where it was written.
                            property.setTest(testFolder);
                            property.setTestcase(testcaseId);
                            toInsert.add(property);
                            imported.add(name + " (" + country + ")");
                        });
            }
        }

        if (!toInsert.isEmpty() && !testCaseCountryPropertiesService.insertListTestCaseCountryProperties(toInsert)) {
            notes.add("The step was created but its properties could not be imported. Define them with "
                    + "cerberus_testcase_country_property_create before running this testcase.");
            report.put("imported", List.of());
            return report;
        }

        report.put("imported", imported);
        if (!alreadyDefined.isEmpty()) {
            report.put("alreadyDefinedHere", alreadyDefined);
        }
        if (!notAvailable.isEmpty()) {
            report.put("notDefinedAnywhere", notAvailable);
            notes.add("These properties are read by the step but defined neither here nor in the library's "
                    + "testcase for those countries: " + notAvailable
                    + ". The run will fail on property decoding until they exist — create them with "
                    + "cerberus_testcase_country_property_create.");
        }
        return report;
    }
}
