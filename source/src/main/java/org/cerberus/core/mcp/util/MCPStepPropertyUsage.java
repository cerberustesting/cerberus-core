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

import org.cerberus.core.crud.entity.TestCaseCountryProperties;
import org.cerberus.core.crud.entity.TestCaseStepAction;
import org.cerberus.core.crud.entity.TestCaseStepActionControl;
import org.cerberus.core.crud.service.ITestCaseCountryPropertiesService;
import org.cerberus.core.crud.service.ITestCaseCountryService;
import org.cerberus.core.crud.service.ITestCaseStepActionControlService;
import org.cerberus.core.crud.service.ITestCaseStepActionService;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Works out which properties a step reads.
 *
 * <p>Needed whenever a step moves somewhere its properties do not exist yet. A property is defined
 * per testcase and country, so a step borrowed by another testcase reads names that testcase may
 * never have heard of, and the run then fails during property decoding — which reads as a broken
 * step rather than as a missing definition.</p>
 *
 * <p>Cerberus itself never parses these references: the engine walks the properties it knows and
 * replaces each occurrence by name, so there is no authoritative pattern to reuse. This class
 * recovers the names the other way round — it finds the {@code %property.…%} occurrences, then
 * resolves each against the names actually defined in the owning testcase. That resolution is what
 * makes the ambiguous forms safe: {@code %property.CUSTOMER.EMAIL%} is a property named
 * {@code CUSTOMER} read at sub-data {@code EMAIL} when {@code CUSTOMER} exists, and a property
 * named {@code CUSTOMER.EMAIL} when that one does.</p>
 */
@Component
public class MCPStepPropertyUsage {

    /**
     * An occurrence of the property syntax, capturing everything between the prefix and the closing
     * percent. What that text means is decided afterwards, against the real property names.
     */
    private static final Pattern PROPERTY_REFERENCE = Pattern.compile("%property\\.([^%]+)%");

    /** Where a property name ends when nothing is known to resolve it against. */
    private static final Pattern NAME_BOUNDARY = Pattern.compile("[.(]");

    private final ITestCaseStepActionService testCaseStepActionService;
    private final ITestCaseStepActionControlService testCaseStepActionControlService;
    private final ITestCaseCountryService testCaseCountryService;
    private final ITestCaseCountryPropertiesService testCaseCountryPropertiesService;

    public MCPStepPropertyUsage(ITestCaseStepActionService testCaseStepActionService,
                                ITestCaseStepActionControlService testCaseStepActionControlService,
                                ITestCaseCountryService testCaseCountryService,
                                ITestCaseCountryPropertiesService testCaseCountryPropertiesService) {
        this.testCaseStepActionService = testCaseStepActionService;
        this.testCaseStepActionControlService = testCaseStepActionControlService;
        this.testCaseCountryService = testCaseCountryService;
        this.testCaseCountryPropertiesService = testCaseCountryPropertiesService;
    }

    /**
     * The property names read by the actions and controls of one step, loading them from the
     * database.
     *
     * <p>Use this when the step's content is not already in hand. A caller that has the actions
     * loaded — with their controls — should use {@link #propertiesIn(List, Set)} instead, which
     * answers the same question without going back to the database once per step.</p>
     *
     * @return the names, in the order they were met, never {@code null}.
     */
    public Set<String> propertiesReadBy(String test, String testcase, int stepId) {
        List<TestCaseStepAction> actions = testCaseStepActionService.getListOfAction(test, testcase, stepId);
        if (actions == null) {
            return new LinkedHashSet<>();
        }
        for (TestCaseStepAction action : actions) {
            if (action.getControls() == null) {
                action.setControls(testCaseStepActionControlService
                        .findControlByTestTestCaseStepIdActionId(test, testcase, stepId, action.getActionId()));
            }
        }
        return propertiesIn(actions, declaredProperties(test, testcase));
    }

    /**
     * The property names read by actions already in hand, with their controls.
     *
     * @param actions  the actions to scan; their {@code controls} are scanned too when loaded.
     * @param declared the property names the owning testcase defines, from
     *                 {@link #declaredProperties(String, String)} — hoisted out so a caller walking
     *                 a whole testcase resolves them once instead of once per step.
     * @return the names, in the order they were met, never {@code null}.
     */
    public Set<String> propertiesIn(List<TestCaseStepAction> actions, Set<String> declared) {
        Set<String> found = new LinkedHashSet<>();
        if (actions == null) {
            return found;
        }

        for (TestCaseStepAction action : actions) {
            for (String field : valuesOf(action)) {
                collect(field, declared, found);
            }
            // The test case editor's own "use step" import matches a property by whole value2, a
            // convention older than the %property.…% syntax and still in use by the actions that
            // name a property directly. Kept so a step written that way imports as it always did.
            if (declared.contains(MCPToolUtils.nullSafe(action.getValue2()).trim())) {
                found.add(action.getValue2().trim());
            }

            if (action.getControls() == null) {
                continue;
            }
            for (TestCaseStepActionControl control : action.getControls()) {
                for (String field : valuesOf(control)) {
                    collect(field, declared, found);
                }
            }
        }

        return found;
    }

    /**
     * Pulls every property reference out of one field.
     */
    private void collect(String field, Set<String> declared, Set<String> found) {
        if (field == null || field.isEmpty()) {
            return;
        }
        Matcher matcher = PROPERTY_REFERENCE.matcher(field);
        while (matcher.find()) {
            found.add(resolve(matcher.group(1), declared));
        }
    }

    /**
     * Decides which property name a reference points at.
     *
     * <p>Prefers the longest declared name the reference starts with, so a name containing a dot
     * wins over the sub-data reading of the same text. Falls back to the text up to the first
     * separator when nothing is declared under that name — a reference to a property that does not
     * exist is exactly what the caller needs to be told about, so it is reported rather than
     * dropped.</p>
     */
    private String resolve(String reference, Set<String> declared) {
        String best = null;
        for (String candidate : declared) {
            if (!reference.equals(candidate)
                    && !reference.startsWith(candidate + ".")
                    && !reference.startsWith(candidate + "(")) {
                continue;
            }
            if (best == null || candidate.length() > best.length()) {
                best = candidate;
            }
        }
        if (best != null) {
            return best;
        }

        Matcher boundary = NAME_BOUNDARY.matcher(reference);
        return boundary.find() ? reference.substring(0, boundary.start()) : reference;
    }

    /**
     * Every property name the testcase defines, across all the countries it declares.
     *
     * <p>Public so a caller scanning several steps of the same testcase resolves it once.</p>
     */
    public Set<String> declaredProperties(String test, String testcase) {
        Set<String> names = new LinkedHashSet<>();
        List<String> countries = testCaseCountryService.findListOfCountryByTestTestCase(test, testcase);
        if (countries == null) {
            return names;
        }
        for (String country : countries) {
            List<TestCaseCountryProperties> properties = testCaseCountryPropertiesService
                    .findListOfPropertyPerTestTestCaseCountry(test, testcase, country);
            if (properties == null) {
                continue;
            }
            for (TestCaseCountryProperties property : properties) {
                if (property.getProperty() != null && !property.getProperty().isBlank()) {
                    names.add(property.getProperty());
                }
            }
        }
        return names;
    }

    /** The fields of an action the engine decodes before use. */
    private List<String> valuesOf(TestCaseStepAction action) {
        List<String> values = new ArrayList<>(6);
        values.add(action.getValue1());
        values.add(action.getValue2());
        values.add(action.getValue3());
        values.add(action.getConditionValue1());
        values.add(action.getConditionValue2());
        values.add(action.getConditionValue3());
        return values;
    }

    /** The fields of a control the engine decodes before use. */
    private List<String> valuesOf(TestCaseStepActionControl control) {
        List<String> values = new ArrayList<>(6);
        values.add(control.getValue1());
        values.add(control.getValue2());
        values.add(control.getValue3());
        values.add(control.getConditionValue1());
        values.add(control.getConditionValue2());
        values.add(control.getConditionValue3());
        return values;
    }
}
