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
import org.cerberus.core.crud.entity.TestCaseStepAction;
import org.cerberus.core.crud.entity.TestCaseStepActionControl;
import org.cerberus.core.crud.service.ITestCaseCountryPropertiesService;
import org.cerberus.core.crud.service.ITestCaseCountryService;
import org.cerberus.core.crud.service.ITestCaseService;
import org.cerberus.core.crud.service.ITestCaseStepActionControlService;
import org.cerberus.core.crud.service.ITestCaseStepActionService;
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
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * MCP tool that copies a step, with its actions and controls, into a testcase, under the tool name
 * {@code cerberus_testcase_step_duplicate}.
 *
 * <p>The third thing maintaining a real scenario keeps asking for, next to inserting and
 * reordering: take this block and make another one like it. Rebuilding it action by action is a
 * dozen calls, and the copy silently diverges from the original in whatever the caller mistyped.</p>
 *
 * <p>Deliberately a copy and not a link. {@code cerberus_testcase_step_library_use} is the other
 * answer to "I need this sequence here too", and it is the better one when the two should stay in
 * step; this one is for when they have to diverge — a variant of a flow, a case the shared step
 * cannot express. The response says so, because choosing wrongly is what creates the second copy of
 * a defect nobody fixes.</p>
 *
 * <p>Mirrors the {@code ImportTestCaseStep} servlet the editor posts to, including its option to
 * bring over the properties the copied actions read.</p>
 */
@Component
public class DuplicateTestCaseStepTool implements MCPTool {

    private static final String TOOL_NAME = "cerberus_testcase_step_duplicate";

    /** Step ids advance in tens, as everywhere else, so there is room to renumber. */
    private static final int STEP_ID_INCREMENT = 10;

    private final ITestCaseService testCaseService;
    private final ITestCaseStepService testCaseStepService;
    private final ITestCaseStepActionService testCaseStepActionService;
    private final ITestCaseStepActionControlService testCaseStepActionControlService;
    private final ITestCaseCountryService testCaseCountryService;
    private final ITestCaseCountryPropertiesService testCaseCountryPropertiesService;
    private final MCPStepPropertyUsage stepPropertyUsage;
    private final MCPOrderingService orderingService;
    private final TestcaseStepMapperV001 mapper;
    private final MCPLogUtils mcpLogUtils;

    public DuplicateTestCaseStepTool(ITestCaseService testCaseService,
                                     ITestCaseStepService testCaseStepService,
                                     ITestCaseStepActionService testCaseStepActionService,
                                     ITestCaseStepActionControlService testCaseStepActionControlService,
                                     ITestCaseCountryService testCaseCountryService,
                                     ITestCaseCountryPropertiesService testCaseCountryPropertiesService,
                                     MCPStepPropertyUsage stepPropertyUsage,
                                     MCPOrderingService orderingService,
                                     TestcaseStepMapperV001 mapper,
                                     MCPLogUtils mcpLogUtils) {
        this.testCaseService = testCaseService;
        this.testCaseStepService = testCaseStepService;
        this.testCaseStepActionService = testCaseStepActionService;
        this.testCaseStepActionControlService = testCaseStepActionControlService;
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
                "description", "Test folder of the testcase receiving the copy."
        ));
        properties.put("testcase", Map.of(
                "type", "string",
                "description", "Testcase receiving the copy."
        ));
        properties.put("sourceTestFolder", Map.of(
                "type", "string",
                "description", "Test folder of the step to copy. Defaults to the receiving testcase's folder."
        ));
        properties.put("sourceTestcase", Map.of(
                "type", "string",
                "description", "Testcase owning the step to copy. Defaults to the receiving testcase, which is "
                        + "how you duplicate a step inside one testcase."
        ));
        properties.put("sourceStepId", Map.of(
                "type", "integer",
                "description", "Step id to copy."
        ));
        properties.put("description", Map.of(
                "type", "string",
                "description", "Description of the copy. Defaults to the source's, which leaves two steps "
                        + "reading identically — worth setting."
        ));
        properties.put("position", Map.of(
                "type", "integer",
                "description", "Where the copy goes, 1 being first. Omit it to append at the end."
        ));
        properties.put("importProperties", Map.of(
                "type", "boolean",
                "description", "Copy the properties the step's actions read into the receiving testcase, for "
                        + "the countries it declares. Defaults to true. Existing ones are never overwritten. "
                        + "Only relevant when copying from another testcase."
        ));

        return new McpSchema.Tool(
                TOOL_NAME,
                null,
                """
                Copies a step — with its actions and their controls — into a testcase.

                Use this to duplicate a block you are about to vary: a second variant of a flow, a case the
                original cannot express. The copy is independent from the moment it is created; changing one
                does not change the other.

                When the two should instead stay identical forever, do not copy: use
                cerberus_testcase_step_library_use, which points at the original so one fix repairs every
                testcase. Copying what should have been shared is how a defect ends up with copies nobody
                knows about.

                Leave sourceTestFolder and sourceTestcase out to duplicate a step inside the same testcase.
                Pass position to put the copy where it belongs rather than at the end.
                """,
                new McpSchema.JsonSchema(
                        "object",
                        properties,
                        List.of("testFolder", "testcase", "sourceStepId"),
                        null,
                        null,
                        null
                ),
                null,
                MCPToolUtils.createAnnotations("Duplicate a testcase step", false),
                null
        );
    }

    private McpSchema.CallToolResult execute(Map<String, Object> args) {
        String testFolder = MCPToolUtils.getString(args, "testFolder", "").trim();
        String testcaseId = MCPToolUtils.getString(args, "testcase", "").trim();
        String sourceTestFolder = MCPToolUtils.getString(args, "sourceTestFolder", "").trim();
        String sourceTestcase = MCPToolUtils.getString(args, "sourceTestcase", "").trim();
        int sourceStepId = MCPToolUtils.getInteger(args, "sourceStepId", -1);
        String description = MCPToolUtils.getString(args, "description", "").trim();
        int position = MCPToolUtils.getInteger(args, "position", 0);
        boolean importProperties = MCPToolUtils.getBoolean(args, "importProperties", true);

        if (testFolder.isBlank()) {
            return MCPToolUtils.errorText("Missing required parameter: testFolder");
        }
        if (testcaseId.isBlank()) {
            return MCPToolUtils.errorText("Missing required parameter: testcase");
        }
        if (sourceStepId < 0) {
            return MCPToolUtils.errorText("Missing or invalid required parameter: sourceStepId");
        }
        if (sourceTestFolder.isBlank()) {
            sourceTestFolder = testFolder;
        }
        if (sourceTestcase.isBlank()) {
            sourceTestcase = testcaseId;
        }

        mcpLogUtils.call(TOOL_NAME, "testcase_step_duplicate",
                String.format("MCP tool %s called with target=%s/%s source=%s/%s/%d",
                        TOOL_NAME, testFolder, testcaseId, sourceTestFolder, sourceTestcase, sourceStepId));

        AnswerItem<TestCase> testCaseAnswer = testCaseService.readByKey(testFolder, testcaseId);
        if (!testCaseAnswer.isCodeStringEquals("OK") || testCaseAnswer.getItem() == null) {
            return MCPToolUtils.errorText("Testcase does not exist: testFolder=" + testFolder
                    + " testcase=" + testcaseId);
        }

        TestCaseStep source = testCaseStepService.findTestCaseStep(sourceTestFolder, sourceTestcase, sourceStepId);
        if (source == null) {
            return MCPToolUtils.errorText("Step does not exist: " + sourceTestFolder + "/" + sourceTestcase
                    + " step " + sourceStepId);
        }

        List<String> notes = new ArrayList<>();
        if (source.isUsingLibraryStep()) {
            // The source owns no action of its own, so a copy of it would be an empty step. Pointing
            // at the same library is what the caller almost certainly meant.
            return MCPToolUtils.errorText("Step " + sourceStepId + " of " + sourceTestFolder + "/"
                    + sourceTestcase + " has no actions of its own — it runs the library step "
                    + source.getLibraryStepTest() + "/" + source.getLibraryStepTestcase() + " step "
                    + source.getLibraryStepStepId() + ". Copying it would create an empty step. Use "
                    + "cerberus_testcase_step_library_use to run the same library step here, or duplicate "
                    + "the library step itself to get a copy you can change.");
        }

        AnswerList<TestCaseStep> stepsAnswer = testCaseStepService.readByTestTestCase(testFolder, testcaseId);
        List<TestCaseStep> existingSteps = stepsAnswer.getDataList();
        int newStepId = existingSteps == null || existingSteps.isEmpty()
                ? STEP_ID_INCREMENT
                : testCaseStepService.getMaxStepId(existingSteps) + STEP_ID_INCREMENT;

        // Read before the copy is written: the source and the target can be the same testcase, and
        // the actions must be the source's as they stand now, not the ones that exist afterwards.
        List<TestCaseStepAction> sourceActions =
                testCaseStepActionService.getListOfAction(sourceTestFolder, sourceTestcase, sourceStepId);
        List<TestCaseStepActionControl> sourceControls = testCaseStepActionControlService
                .findControlByTestTestCaseStepId(sourceTestFolder, sourceTestcase, sourceStepId);
        Set<String> propertiesUsed = importProperties && !sourceTestcase.equals(testcaseId)
                ? readPropertiesUsed(sourceTestFolder, sourceTestcase, sourceStepId)
                : Set.of();

        TestCaseStep copy = TestCaseStep.builder()
                .test(testFolder)
                .testcase(testcaseId)
                .stepId(newStepId)
                .sort(newStepId)
                .description(description.isBlank()
                        ? MCPToolUtils.nullSafe(source.getDescription()) : description)
                .loop(MCPToolUtils.nullSafe(source.getLoop()))
                .conditionOperator(MCPToolUtils.nullSafe(source.getConditionOperator()))
                .conditionValue1(MCPToolUtils.nullSafe(source.getConditionValue1()))
                .conditionValue2(MCPToolUtils.nullSafe(source.getConditionValue2()))
                .conditionValue3(MCPToolUtils.nullSafe(source.getConditionValue3()))
                .conditionOptions(source.getConditionOptions())
                // A copy is never itself a library step: two steps offering themselves under the same
                // description is how the wrong one gets reused.
                .isLibraryStep(false)
                .isUsingLibraryStep(false)
                .libraryStepStepId(0)
                .isExecutionForced(source.isExecutionForced())
                .usrCreated("MCP")
                .build();

        Answer created = testCaseStepService.create(copy);
        if (!created.isCodeStringEquals("OK")) {
            return MCPToolUtils.errorText("Unable to create the step: " + created.getMessageDescription());
        }

        int actionsCopied = copyActions(sourceActions, testFolder, testcaseId, newStepId, notes);
        int controlsCopied = copyControls(sourceControls, testFolder, testcaseId, newStepId, notes);

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("status", "created");
        response.put("step", mapper.toDTO(copy));
        response.put("copiedFrom", Map.of(
                "testFolder", sourceTestFolder,
                "testcase", sourceTestcase,
                "stepId", sourceStepId));
        response.put("actionsCopied", actionsCopied);
        response.put("controlsCopied", controlsCopied);
        response.put("independent", "This copy no longer follows the original. If they should stay identical, "
                + "delete it and use cerberus_testcase_step_library_use instead.");

        if (position > 0) {
            MCPOrderingService.Result placement = orderingService.placeStep(testFolder, testcaseId, newStepId, position);
            if (placement.failed()) {
                response.put("status", "created_but_not_positioned");
                notes.add("The copy was created at the end of the testcase but could not be moved to position "
                        + position + ": " + placement.error()
                        + " Use cerberus_testcase_step_reorder to place it.");
            } else {
                response.put("stepIds", placement.order());
            }
        }

        if (!propertiesUsed.isEmpty()) {
            response.put("properties", importProperties(testFolder, testcaseId, sourceTestFolder, sourceTestcase,
                    propertiesUsed, notes));
        }

        if (!notes.isEmpty()) {
            response.put("notes", notes);
        }

        mcpLogUtils.success(TOOL_NAME, "testcase_step_duplicate",
                String.format("Step %s/%s/%d copied into %s/%s as step %d",
                        sourceTestFolder, sourceTestcase, sourceStepId, testFolder, testcaseId, newStepId));

        return MCPToolUtils.successJson(response);
    }

    /**
     * Rewrites the source actions onto the new step and inserts them.
     *
     * <p>Action ids are kept as they are: they are unique within their step, and preserving them is
     * what lets the copied controls stay attached to the right action without a mapping table.</p>
     */
    private int copyActions(List<TestCaseStepAction> sourceActions, String testFolder, String testcase,
                            int stepId, List<String> notes) {
        if (sourceActions == null || sourceActions.isEmpty()) {
            return 0;
        }
        List<TestCaseStepAction> toInsert = new ArrayList<>();
        for (TestCaseStepAction action : sourceActions) {
            action.setTest(testFolder);
            action.setTestcase(testcase);
            action.setStepId(stepId);
            action.setUsrCreated("MCP");
            toInsert.add(action);
        }
        if (!testCaseStepActionService.insertListTestCaseStepAction(toInsert)) {
            notes.add("The step was created but its actions could not be copied. Delete it and try again, or "
                    + "add the actions with cerberus_testcase_step_action_create.");
            return 0;
        }
        return toInsert.size();
    }

    /**
     * Rewrites the source controls onto the new step and inserts them.
     */
    private int copyControls(List<TestCaseStepActionControl> sourceControls, String testFolder, String testcase,
                             int stepId, List<String> notes) {
        if (sourceControls == null || sourceControls.isEmpty()) {
            return 0;
        }
        List<TestCaseStepActionControl> toInsert = new ArrayList<>();
        for (TestCaseStepActionControl control : sourceControls) {
            control.setTest(testFolder);
            control.setTestcase(testcase);
            control.setStepId(stepId);
            control.setUsrCreated("MCP");
            toInsert.add(control);
        }
        Answer answer = testCaseStepActionControlService.createList(toInsert);
        if (!answer.isCodeStringEquals("OK")) {
            notes.add("The actions were copied but their controls were not: " + answer.getMessageDescription()
                    + " Add them with cerberus_testcase_step_action_control_create.");
            return 0;
        }
        return toInsert.size();
    }

    /**
     * The property names the copied step reads, from the source testcase.
     */
    private Set<String> readPropertiesUsed(String sourceTestFolder, String sourceTestcase, int sourceStepId) {
        return stepPropertyUsage.propertiesReadBy(sourceTestFolder, sourceTestcase, sourceStepId);
    }

    /**
     * Copies the properties the step reads into the receiving testcase, per country.
     *
     * <p>Same rule as {@code cerberus_testcase_step_library_use}: a property already defined here is
     * left alone, and one defined nowhere is reported rather than passed over — a copied step whose
     * properties did not follow fails on decoding, which reads as a broken step.</p>
     */
    private Map<String, Object> importProperties(String testFolder, String testcaseId, String sourceTestFolder,
                                                 String sourceTestcase, Set<String> used, List<String> notes) {
        Map<String, Object> report = new LinkedHashMap<>();
        report.put("readByTheStep", used);

        List<String> targetCountries = testCaseCountryService.findListOfCountryByTestTestCase(testFolder, testcaseId);
        if (targetCountries == null || targetCountries.isEmpty()) {
            notes.add("This testcase declares no country, so no property could be copied with the step.");
            return report;
        }

        List<TestCaseCountryProperties> toInsert = new ArrayList<>();
        List<String> imported = new ArrayList<>();
        List<String> alreadyDefined = new ArrayList<>();
        List<String> notAvailable = new ArrayList<>();

        for (String country : targetCountries) {
            List<TestCaseCountryProperties> sourceProperties = testCaseCountryPropertiesService
                    .findListOfPropertyPerTestTestCaseCountry(sourceTestFolder, sourceTestcase, country);
            List<TestCaseCountryProperties> targetProperties = testCaseCountryPropertiesService
                    .findListOfPropertyPerTestTestCaseCountry(testFolder, testcaseId, country);

            Set<String> definedHere = new HashSet<>();
            if (targetProperties != null) {
                targetProperties.forEach(property -> definedHere.add(property.getProperty()));
            }

            for (String name : used) {
                if (definedHere.contains(name)) {
                    alreadyDefined.add(name + " (" + country + ")");
                    continue;
                }
                TestCaseCountryProperties match = sourceProperties == null ? null : sourceProperties.stream()
                        .filter(property -> name.equals(property.getProperty()))
                        .findFirst()
                        .orElse(null);
                if (match == null) {
                    notAvailable.add(name + " (" + country + ")");
                    continue;
                }
                match.setTest(testFolder);
                match.setTestcase(testcaseId);
                toInsert.add(match);
                imported.add(name + " (" + country + ")");
            }
        }

        if (!toInsert.isEmpty() && !testCaseCountryPropertiesService.insertListTestCaseCountryProperties(toInsert)) {
            notes.add("The step was copied but its properties were not. Define them with "
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
            notes.add("These properties are read by the copied step but defined neither here nor in the source "
                    + "for those countries: " + notAvailable + ". The run fails on property decoding until "
                    + "they exist.");
        }
        return report;
    }
}
