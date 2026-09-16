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
package org.cerberus.core.mcp.impl.test.testcase;

import io.modelcontextprotocol.server.McpServerFeatures;
import io.modelcontextprotocol.spec.McpSchema;
import org.cerberus.core.crud.entity.TestCase;
import org.cerberus.core.crud.entity.TestCaseStep;
import org.cerberus.core.crud.entity.TestCaseStepAction;
import org.cerberus.core.crud.entity.TestCaseStepActionControl;
import org.cerberus.core.crud.service.ILoadTestCaseService;
import org.cerberus.core.crud.service.ITestCaseService;
import org.cerberus.core.mcp.MCPTool;
import org.cerberus.core.mcp.util.MCPActionOptions;
import org.cerberus.core.mcp.util.MCPLogUtils;
import org.cerberus.core.mcp.util.MCPStepPropertyUsage;
import org.cerberus.core.mcp.util.MCPToolUtils;
import org.cerberus.core.util.answer.AnswerItem;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * MCP tool that returns the sequence a testcase actually runs, under the tool name
 * {@code cerberus_testcase_scenario_get}.
 *
 * <p>Reading a scenario out of the per-level tools takes one call for the steps, then one more for
 * every step that turns out to borrow its actions from a library step, and the answer still has to
 * be assembled by hand. That is three or four calls before any diagnosis can start, on every
 * testcase looked at.</p>
 *
 * <p>This resolves the whole thing in one call, through
 * {@link ILoadTestCaseService#loadTestCaseStep}, which is the loader the engine itself uses at the
 * start of a run — so what comes back is the sequence that will execute, not a reconstruction of
 * it.</p>
 *
 * <p>That resolution rewrites a borrowed action onto the coordinates of the step running it, which
 * is right for execution and misleading for editing: the action exists once, in the library's
 * testcase, and changing it changes every testcase that borrows it. Each step therefore reports
 * where its actions are really defined and whether they can be edited in place, so a fix lands on
 * the right row and its blast radius is known before it is written.</p>
 */
@Component
public class GetTestCaseScenarioTool implements MCPTool {

    private static final String TOOL_NAME = "cerberus_testcase_scenario_get";

    private static final String DETAIL_OUTLINE = "outline";
    private static final String DETAIL_FULL = "full";

    private final ITestCaseService testCaseService;
    private final ILoadTestCaseService loadTestCaseService;
    private final MCPStepPropertyUsage stepPropertyUsage;
    private final MCPLogUtils mcpLogUtils;

    public GetTestCaseScenarioTool(ITestCaseService testCaseService,
                                   ILoadTestCaseService loadTestCaseService,
                                   MCPStepPropertyUsage stepPropertyUsage,
                                   MCPLogUtils mcpLogUtils) {
        this.testCaseService = testCaseService;
        this.loadTestCaseService = loadTestCaseService;
        this.stepPropertyUsage = stepPropertyUsage;
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
                "description", "Test folder containing the testcase."
        ));
        properties.put("testcase", Map.of(
                "type", "string",
                "description", "Testcase to resolve."
        ));
        properties.put("detail", Map.of(
                "type", "string",
                "description", "How much to return. 'full' gives every value, condition, option and control "
                        + "— use it to diagnose. 'outline' gives the shape only: steps, action types and "
                        + "descriptions — use it to get oriented in a long testcase before asking for the "
                        + "part that matters. Defaults to full.",
                "enum", List.of(DETAIL_FULL, DETAIL_OUTLINE)
        ));
        properties.put("includeProperties", Map.of(
                "type", "boolean",
                "description", "Report, per step, the properties its actions and controls read. Defaults to "
                        + "true: an unresolved property is the most common reason a step fails, and this "
                        + "names the ones to check. Turn it off on a very large testcase to keep the answer "
                        + "small."
        ));

        return new McpSchema.Tool(
                TOOL_NAME,
                null,
                """
                Returns the sequence a testcase actually runs: every step in order, every action in order,
                every control, with library steps resolved to the actions they contribute.

                Call this first whenever you need to understand, diagnose or repair a testcase. It answers in
                one call what otherwise takes a step list plus one more call per library step, and it is built
                on the same loader the engine uses to start a run, so it shows what will execute rather than a
                reconstruction.

                Each step reports where its actions are defined. A step marked source=library runs actions
                that live in another testcase: editing them there changes every testcase borrowing that step,
                and the coordinates to edit are given in definedIn — the ones shown next to the actions
                themselves belong to this testcase and are what the execution records under.

                Use cerberus_testcase_step_action_update and its neighbours to change an action, targeting the
                definedIn coordinates for a borrowed one.
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
                MCPToolUtils.readOnlyAnnotations("Get resolved testcase scenario", false),
                null
        );
    }

    private McpSchema.CallToolResult execute(Map<String, Object> args) {
        String testFolder = MCPToolUtils.getString(args, "testFolder", "").trim();
        String testcaseId = MCPToolUtils.getString(args, "testcase", "").trim();
        String detail = MCPToolUtils.getString(args, "detail", DETAIL_FULL).trim();
        boolean includeProperties = MCPToolUtils.getBoolean(args, "includeProperties", true);

        mcpLogUtils.call(TOOL_NAME, "testcase_scenario_get",
                String.format("MCP tool %s called with testFolder=%s testcase=%s detail=%s",
                        TOOL_NAME, testFolder, testcaseId, detail));

        if (testFolder.isBlank()) {
            return MCPToolUtils.errorText("Missing required parameter: testFolder");
        }
        if (testcaseId.isBlank()) {
            return MCPToolUtils.errorText("Missing required parameter: testcase");
        }
        if (!DETAIL_FULL.equals(detail) && !DETAIL_OUTLINE.equals(detail)) {
            return MCPToolUtils.errorText("Unknown detail '" + detail + "'. Use 'full' or 'outline'.");
        }

        AnswerItem<TestCase> testCaseAnswer = testCaseService.readByKey(testFolder, testcaseId);
        if (!testCaseAnswer.isCodeStringEquals("OK") || testCaseAnswer.getItem() == null) {
            return MCPToolUtils.errorText("Testcase does not exist: testFolder=" + testFolder
                    + " testcase=" + testcaseId);
        }
        TestCase testCase = testCaseAnswer.getItem();

        List<TestCaseStep> steps = loadTestCaseService.loadTestCaseStep(testCase);
        boolean full = DETAIL_FULL.equals(detail);

        List<Map<String, Object>> renderedSteps = new ArrayList<>();
        // One entry per testcase that owns steps in this scenario — the testcase itself, plus each
        // library it borrows from.
        Map<String, Set<String>> declaredByTestcase = new LinkedHashMap<>();
        int actionCount = 0;
        int controlCount = 0;
        int borrowedSteps = 0;

        int position = 0;
        for (TestCaseStep step : steps) {
            position++;
            Map<String, Object> rendered = new LinkedHashMap<>();
            rendered.put("position", position);
            rendered.put("stepId", step.getStepId());
            rendered.put("description", MCPToolUtils.nullSafe(step.getDescription()));

            boolean borrowed = step.isUsingLibraryStep();
            if (borrowed) {
                borrowedSteps++;
            }
            rendered.put("source", borrowed ? "library" : "local");
            rendered.put("definedIn", Map.of(
                    "testFolder", borrowed ? MCPToolUtils.nullSafe(step.getLibraryStepTest()) : step.getTest(),
                    "testcase", borrowed ? MCPToolUtils.nullSafe(step.getLibraryStepTestcase()) : step.getTestcase(),
                    "stepId", borrowed ? step.getLibraryStepStepId() : step.getStepId()));
            if (borrowed) {
                rendered.put("editWarning", "These actions live in "
                        + MCPToolUtils.nullSafe(step.getLibraryStepTest()) + "/"
                        + MCPToolUtils.nullSafe(step.getLibraryStepTestcase()) + " step " + step.getLibraryStepStepId()
                        + " and are shared. Editing one changes every testcase using that step. To change "
                        + "only this testcase, replace this step with one of its own actions.");
            }
            if (step.isLibraryStep()) {
                rendered.put("isLibraryStep", true);
            }

            if (full) {
                rendered.put("loop", MCPToolUtils.nullSafe(step.getLoop()));
                putIfPresent(rendered, "condition", condition(step.getConditionOperator(),
                        step.getConditionValue1(), step.getConditionValue2(), step.getConditionValue3()));
                Map<String, String> conditionOptions = MCPActionOptions.active(step.getConditionOptions());
                if (!conditionOptions.isEmpty()) {
                    rendered.put("conditionOptions", conditionOptions);
                }
            }

            List<Map<String, Object>> renderedActions = new ArrayList<>();
            List<TestCaseStepAction> actions = step.getActions() == null ? List.of() : step.getActions();
            // loadTestCaseStep already attached each action's controls, so nothing below needs a
            // second read.

            int actionPosition = 0;
            for (TestCaseStepAction action : actions) {
                actionPosition++;
                actionCount++;
                Map<String, Object> renderedAction = new LinkedHashMap<>();
                renderedAction.put("position", actionPosition);
                renderedAction.put("actionId", action.getActionId());
                renderedAction.put("action", MCPToolUtils.nullSafe(action.getAction()));
                renderedAction.put("description", MCPToolUtils.nullSafe(action.getDescription()));

                if (full) {
                    renderedAction.put("value1", MCPToolUtils.nullSafe(action.getValue1()));
                    renderedAction.put("value2", MCPToolUtils.nullSafe(action.getValue2()));
                    renderedAction.put("value3", MCPToolUtils.nullSafe(action.getValue3()));
                    renderedAction.put("isFatal", action.isFatal());
                    putIfPresent(renderedAction, "condition", condition(action.getConditionOperator(),
                            action.getConditionValue1(), action.getConditionValue2(), action.getConditionValue3()));
                    Map<String, String> options = MCPActionOptions.active(action.getOptions());
                    if (!options.isEmpty()) {
                        renderedAction.put("options", options);
                    }
                    Map<String, String> conditionOptions = MCPActionOptions.active(action.getConditionOptions());
                    if (!conditionOptions.isEmpty()) {
                        renderedAction.put("conditionOptions", conditionOptions);
                    }
                    if (action.getWaitBefore() > 0) {
                        renderedAction.put("waitBefore", action.getWaitBefore());
                    }
                    if (action.getWaitAfter() > 0) {
                        renderedAction.put("waitAfter", action.getWaitAfter());
                    }
                }

                List<TestCaseStepActionControl> controls =
                        action.getControls() == null ? List.of() : action.getControls();
                List<Map<String, Object>> renderedControls = new ArrayList<>();
                int controlPosition = 0;
                for (TestCaseStepActionControl control : controls) {
                    controlPosition++;
                    controlCount++;
                    Map<String, Object> renderedControl = new LinkedHashMap<>();
                    renderedControl.put("position", controlPosition);
                    renderedControl.put("controlId", control.getControlId());
                    renderedControl.put("control", MCPToolUtils.nullSafe(control.getControl()));
                    renderedControl.put("description", MCPToolUtils.nullSafe(control.getDescription()));
                    if (full) {
                        renderedControl.put("value1", MCPToolUtils.nullSafe(control.getValue1()));
                        renderedControl.put("value2", MCPToolUtils.nullSafe(control.getValue2()));
                        renderedControl.put("value3", MCPToolUtils.nullSafe(control.getValue3()));
                        renderedControl.put("isFatal", control.isFatal());
                        putIfPresent(renderedControl, "condition", condition(control.getConditionOperator(),
                                control.getConditionValue1(), control.getConditionValue2(),
                                control.getConditionValue3()));
                        Map<String, String> options = MCPActionOptions.active(control.getOptions());
                        if (!options.isEmpty()) {
                            renderedControl.put("options", options);
                        }
                        Map<String, String> conditionOptions = MCPActionOptions.active(control.getConditionOptions());
                        if (!conditionOptions.isEmpty()) {
                            renderedControl.put("conditionOptions", conditionOptions);
                        }
                    }
                    renderedControls.add(renderedControl);
                }
                if (!renderedControls.isEmpty()) {
                    renderedAction.put("controls", renderedControls);
                }

                renderedActions.add(renderedAction);
            }
            rendered.put("actions", renderedActions);

            if (includeProperties) {
                // Scanned from the actions already loaded above, not re-read from the database: a
                // testcase of twenty steps would otherwise cost hundreds of queries to answer a
                // question its own content already carries.
                //
                // Resolved against the testcase that owns the actions — the library's, for a borrowed
                // step — because that is where the names are declared and where they mean something.
                // The declared names are cached per owning testcase, which in practice is one or two
                // lookups for a whole scenario.
                // A step flagged as borrowed whose library columns are empty is broken data, not a
                // crash: it resolves against nothing and its properties come back empty.
                String ownerTest = MCPToolUtils.nullSafe(borrowed ? step.getLibraryStepTest() : step.getTest());
                String ownerTestcase =
                        MCPToolUtils.nullSafe(borrowed ? step.getLibraryStepTestcase() : step.getTestcase());
                Set<String> declared = declaredByTestcase.computeIfAbsent(ownerTest + "/" + ownerTestcase,
                        key -> stepPropertyUsage.declaredProperties(ownerTest, ownerTestcase));
                Set<String> read = stepPropertyUsage.propertiesIn(actions, declared);
                if (!read.isEmpty()) {
                    rendered.put("propertiesRead", read);
                }
            }

            renderedSteps.add(rendered);
        }

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("testFolder", testFolder);
        response.put("testcase", testcaseId);
        response.put("description", MCPToolUtils.nullSafe(testCase.getDescription()));
        response.put("application", MCPToolUtils.nullSafe(testCase.getApplication()));
        response.put("detail", detail);
        response.put("stepCount", renderedSteps.size());
        response.put("actionCount", actionCount);
        response.put("controlCount", controlCount);
        if (borrowedSteps > 0) {
            response.put("librarySteps", borrowedSteps);
        }
        response.put("steps", renderedSteps);
        if (includeProperties) {
            response.put("propertiesNote", "propertiesRead names what each step reads, not what is defined. "
                    + "Call cerberus_testcase_country_property_list with includeInherited to see what this "
                    + "testcase actually resolves, per country.");
        }

        return MCPToolUtils.successJson(response);
    }

    /** Keeps an absent condition out of the payload rather than rendering it as a null field. */
    private void putIfPresent(Map<String, Object> target, String key, Map<String, Object> value) {
        if (value != null) {
            target.put(key, value);
        }
    }

    /**
     * Renders a condition as one object, or omits it when the element always runs.
     *
     * @return the condition, or {@code null} for the "always" case so an unconditional action does
     * not carry three empty fields.
     */
    private Map<String, Object> condition(String operator, String value1, String value2, String value3) {
        String resolved = MCPToolUtils.nullSafe(operator);
        if (resolved.isBlank() || "always".equalsIgnoreCase(resolved)) {
            return null;
        }
        Map<String, Object> condition = new LinkedHashMap<>();
        condition.put("operator", resolved);
        condition.put("value1", MCPToolUtils.nullSafe(value1));
        condition.put("value2", MCPToolUtils.nullSafe(value2));
        condition.put("value3", MCPToolUtils.nullSafe(value3));
        return condition;
    }
}
