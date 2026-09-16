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

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.cerberus.core.crud.entity.TestCaseStep;
import org.cerberus.core.crud.entity.TestCaseStepAction;
import org.cerberus.core.crud.entity.TestCaseStepActionControl;
import org.cerberus.core.crud.service.ITestCaseStepActionControlService;
import org.cerberus.core.crud.service.ITestCaseStepActionService;
import org.cerberus.core.crud.service.ITestCaseStepService;
import org.cerberus.core.exception.CerberusException;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Function;

/**
 * Changes the order of the steps, actions and controls of a testcase.
 *
 * <p>Cerberus orders a scenario by the {@code sort} column alone — every read of a step, an action
 * or a control is {@code ORDER BY sort}. The identifiers next to it ({@code stepId},
 * {@code actionId}, {@code controlId}) are identity, not position: a control points at its action
 * by {@code actionId}, and a step that reuses a library step points at it by {@code stepId}.
 * Reordering therefore means rewriting {@code sort} and nothing else, which no reference in the
 * database follows.</p>
 *
 * <p>Without this, inserting one action in the middle of an existing sequence — adding a popup
 * dismissal between two clicks — had no expression in the tools at all. The only way through was
 * to append empty actions and shift the content of every following action down by hand, which is
 * a dozen calls, and a data-loss risk at every one of them: the operation that should be "put this
 * here" became "rewrite everything after here".</p>
 *
 * <p>Order is normalised to a dense {@code 1..n} sequence, the same convention the test case
 * editor writes when it saves, so a scenario touched from both sides keeps one convention.</p>
 */
@Component
public class MCPOrderingService {

    private static final Logger LOG = LogManager.getLogger(MCPOrderingService.class);

    /** Actor recorded on rows this service rewrites, matching the other MCP write tools. */
    private static final String MCP_ACTOR = "MCP";

    private final ITestCaseStepService testCaseStepService;
    private final ITestCaseStepActionService testCaseStepActionService;
    private final ITestCaseStepActionControlService testCaseStepActionControlService;

    public MCPOrderingService(ITestCaseStepService testCaseStepService,
                              ITestCaseStepActionService testCaseStepActionService,
                              ITestCaseStepActionControlService testCaseStepActionControlService) {
        this.testCaseStepService = testCaseStepService;
        this.testCaseStepActionService = testCaseStepActionService;
        this.testCaseStepActionControlService = testCaseStepActionControlService;
    }

    /**
     * The outcome of an ordering change.
     *
     * @param error   {@code null} on success, otherwise a message naming what the caller should
     *                send instead.
     * @param order   the identifiers in their new order, top to bottom.
     * @param written how many rows actually had their {@code sort} rewritten; rows already in
     *                place are left untouched so an ordering call does not stamp the whole
     *                scenario as modified.
     */
    public record Result(String error, List<Integer> order, int written) {

        public boolean failed() {
            return error != null;
        }

        static Result of(String error) {
            return new Result(error, List.of(), 0);
        }
    }

    // ------------------------------------------------------------------ steps

    /**
     * Sets the order of every step of a testcase.
     *
     * @param orderedStepIds the complete list of step ids, top to bottom.
     */
    public Result reorderSteps(String test, String testcase, List<Integer> orderedStepIds) {
        List<TestCaseStep> steps = testCaseStepService.getListOfSteps(test, testcase);
        return apply(steps, TestCaseStep::getStepId, orderedStepIds, "step", this::persistStep);
    }

    /**
     * Moves one step to a position, shifting the others around it.
     *
     * @param position 1-based position among the steps; anything past the end lands last.
     */
    public Result placeStep(String test, String testcase, int stepId, int position) {
        List<TestCaseStep> steps = testCaseStepService.getListOfSteps(test, testcase);
        List<Integer> target = moveTo(ids(steps, TestCaseStep::getStepId), stepId, position);
        if (target == null) {
            return Result.of("Step " + stepId + " does not belong to " + test + "/" + testcase + ".");
        }
        return apply(steps, TestCaseStep::getStepId, target, "step", this::persistStep);
    }

    private String persistStep(TestCaseStep step) {
        try {
            testCaseStepService.updateTestCaseStep(step);
            return null;
        } catch (CerberusException e) {
            LOG.error("Unable to reorder step {}/{}/{}.", step.getTest(), step.getTestcase(), step.getStepId(), e);
            return "step " + step.getStepId() + " (" + e.getMessageError().getDescription() + ")";
        }
    }

    // ---------------------------------------------------------------- actions

    /**
     * Sets the order of every action of a step.
     *
     * @param orderedActionIds the complete list of action ids, top to bottom.
     */
    public Result reorderActions(String test, String testcase, int stepId, List<Integer> orderedActionIds) {
        List<TestCaseStepAction> actions = testCaseStepActionService.getListOfAction(test, testcase, stepId);
        return apply(actions, TestCaseStepAction::getActionId, orderedActionIds, "action", this::persistAction);
    }

    /**
     * Moves one action to a position within its step, shifting the others around it.
     *
     * @param position 1-based position among the actions of the step; anything past the end lands
     *                 last.
     */
    public Result placeAction(String test, String testcase, int stepId, int actionId, int position) {
        List<TestCaseStepAction> actions = testCaseStepActionService.getListOfAction(test, testcase, stepId);
        List<Integer> target = moveTo(ids(actions, TestCaseStepAction::getActionId), actionId, position);
        if (target == null) {
            return Result.of("Action " + actionId + " does not belong to step " + stepId
                    + " of " + test + "/" + testcase + ".");
        }
        return apply(actions, TestCaseStepAction::getActionId, target, "action", this::persistAction);
    }

    private String persistAction(TestCaseStepAction action) {
        if (testCaseStepActionService.updateTestCaseStepAction(action)) {
            return null;
        }
        LOG.error("Unable to reorder action {}/{}/{}/{}.",
                action.getTest(), action.getTestcase(), action.getStepId(), action.getActionId());
        return "action " + action.getActionId();
    }

    // --------------------------------------------------------------- controls

    /**
     * Sets the order of every control of an action.
     *
     * @param orderedControlIds the complete list of control ids, top to bottom.
     */
    public Result reorderControls(String test, String testcase, int stepId, int actionId,
                                  List<Integer> orderedControlIds) {
        List<TestCaseStepActionControl> controls =
                testCaseStepActionControlService.findControlByTestTestCaseStepIdActionId(test, testcase, stepId, actionId);
        return apply(controls, TestCaseStepActionControl::getControlId, orderedControlIds, "control", this::persistControl);
    }

    /**
     * Moves one control to a position within its action, shifting the others around it.
     *
     * @param position 1-based position among the controls of the action; anything past the end
     *                 lands last.
     */
    public Result placeControl(String test, String testcase, int stepId, int actionId, int controlId, int position) {
        List<TestCaseStepActionControl> controls =
                testCaseStepActionControlService.findControlByTestTestCaseStepIdActionId(test, testcase, stepId, actionId);
        List<Integer> target = moveTo(ids(controls, TestCaseStepActionControl::getControlId), controlId, position);
        if (target == null) {
            return Result.of("Control " + controlId + " does not belong to action " + actionId
                    + " of step " + stepId + " in " + test + "/" + testcase + ".");
        }
        return apply(controls, TestCaseStepActionControl::getControlId, target, "control", this::persistControl);
    }

    private String persistControl(TestCaseStepActionControl control) {
        if (testCaseStepActionControlService.update(control)) {
            return null;
        }
        LOG.error("Unable to reorder control {}/{}/{}/{}/{}.", control.getTest(), control.getTestcase(),
                control.getStepId(), control.getActionId(), control.getControlId());
        return "control " + control.getControlId();
    }

    // ----------------------------------------------------------------- shared

    /**
     * Validates the requested order against what exists, then writes it.
     *
     * <p>The requested list has to name every existing element exactly once. A partial list is
     * refused rather than completed with a guess: "the ones you did not mention keep their
     * relative order" and "the ones you did not mention go to the end" are both defensible, and an
     * agent that assumed the other one would silently rearrange a scenario it believed it had left
     * alone.</p>
     *
     * @param elements  the elements as they currently stand, already in {@code sort} order.
     * @param idOf      reads an element's identifier.
     * @param requested the identifiers in the wanted order.
     * @param label     what one element is called, for the messages.
     * @param persist   writes one element back, returning {@code null} on success or a short
     *                  description of the failure.
     */
    private <T> Result apply(List<T> elements, Function<T, Integer> idOf, List<Integer> requested,
                             String label, Function<T, String> persist) {
        if (elements == null || elements.isEmpty()) {
            return Result.of("There is no " + label + " to reorder here.");
        }

        List<Integer> existing = ids(elements, idOf);
        String problem = validatePermutation(requested, existing, label);
        if (problem != null) {
            return Result.of(problem);
        }

        List<String> failures = new ArrayList<>();
        int written = 0;
        for (int index = 0; index < requested.size(); index++) {
            int sort = index + 1;
            T element = elements.stream()
                    .filter(candidate -> requested.get(sort - 1).equals(idOf.apply(candidate)))
                    .findFirst()
                    .orElseThrow();

            if (currentSort(element) == sort) {
                continue;
            }
            setSort(element, sort);
            stampModifier(element);
            String failure = persist.apply(element);
            if (failure != null) {
                failures.add(failure);
            } else {
                written++;
            }
        }

        if (!failures.isEmpty()) {
            // Reordering writes row by row, so a partial failure leaves a real, observable state:
            // say which rows are wrong rather than reporting a clean failure the caller would
            // wrongly read as "nothing happened".
            return new Result("The new order was only partially written. These could not be saved: "
                    + String.join(", ", failures) + ". Read the " + label
                    + "s back before changing anything else.", requested, written);
        }
        return new Result(null, requested, written);
    }

    /**
     * Checks that the requested identifiers are exactly the existing ones, in some order.
     *
     * @return {@code null} when they are, otherwise a message naming precisely what is wrong —
     * which identifiers are unknown, which were left out, which appear twice.
     */
    private String validatePermutation(List<Integer> requested, List<Integer> existing, String label) {
        if (requested == null || requested.isEmpty()) {
            return "Give the complete ordered list of " + label + " ids. Current order: " + existing + ".";
        }

        Set<Integer> seen = new LinkedHashSet<>();
        List<Integer> duplicated = new ArrayList<>();
        for (Integer id : requested) {
            if (!seen.add(id)) {
                duplicated.add(id);
            }
        }
        if (!duplicated.isEmpty()) {
            return "These " + label + " ids appear more than once: " + duplicated
                    + ". Each one must be listed exactly once. Current order: " + existing + ".";
        }

        List<Integer> unknown = requested.stream().filter(id -> !existing.contains(id)).toList();
        if (!unknown.isEmpty()) {
            return "These " + label + " ids do not exist here: " + unknown
                    + ". Existing ones, in their current order: " + existing + ".";
        }

        List<Integer> missing = existing.stream().filter(id -> !requested.contains(id)).toList();
        if (!missing.isEmpty()) {
            return "The list must name every " + label + ", and these are missing: " + missing
                    + ". Current order: " + existing
                    + ". Send the full list in the order you want, including the ones that do not move.";
        }

        return null;
    }

    /**
     * Places one identifier at a position and keeps the others in their current relative order.
     *
     * @return the resulting order, or {@code null} when the identifier is not part of the list.
     */
    private List<Integer> moveTo(List<Integer> existing, int id, int position) {
        if (!existing.contains(id)) {
            return null;
        }
        List<Integer> target = new ArrayList<>(existing);
        target.remove(Integer.valueOf(id));
        // A position below the first or past the last is clamped rather than refused: "put it
        // first" and "put it last" are what position 0 and position 999 mean to a caller, and
        // failing the call would only cost a round trip to learn the bounds.
        int index = Math.max(0, Math.min(position - 1, target.size()));
        target.add(index, id);
        return target;
    }

    private <T> List<Integer> ids(List<T> elements, Function<T, Integer> idOf) {
        return elements.stream().map(idOf).toList();
    }

    private int currentSort(Object element) {
        if (element instanceof TestCaseStep step) return step.getSort();
        if (element instanceof TestCaseStepAction action) return action.getSort();
        if (element instanceof TestCaseStepActionControl control) return control.getSort();
        throw new IllegalArgumentException("Unsupported element: " + element.getClass());
    }

    /**
     * Records the change against the MCP actor, as every other MCP write does — a reorder is a
     * modification of the row like any other, and an audit trail that omits it would show the
     * scenario changing shape with no author.
     */
    private void stampModifier(Object element) {
        if (element instanceof TestCaseStep step) {
            step.setUsrModif(MCP_ACTOR);
        } else if (element instanceof TestCaseStepAction action) {
            action.setUsrModif(MCP_ACTOR);
        } else if (element instanceof TestCaseStepActionControl control) {
            control.setUsrModif(MCP_ACTOR);
        }
    }

    private void setSort(Object element, int sort) {
        if (element instanceof TestCaseStep step) {
            step.setSort(sort);
        } else if (element instanceof TestCaseStepAction action) {
            action.setSort(sort);
        } else if (element instanceof TestCaseStepActionControl control) {
            control.setSort(sort);
        } else {
            throw new IllegalArgumentException("Unsupported element: " + element.getClass());
        }
    }
}
