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
import org.cerberus.core.crud.entity.Label;
import org.cerberus.core.crud.entity.TestCase;
import org.cerberus.core.crud.service.IApplicationService;
import org.cerberus.core.crud.service.ILabelService;
import org.cerberus.core.crud.service.ITestCaseService;
import org.cerberus.core.exception.CerberusException;
import org.cerberus.core.util.answer.AnswerItem;
import org.cerberus.core.util.answer.AnswerList;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Turns the label names a caller knows into the identifiers the database stores.
 *
 * <p>Labels are keyed by a numeric id, but nobody refers to them that way — a campaign selects
 * testcases by label name, and so does anyone describing what a testcase is for. Forcing every call
 * to carry ids would mean listing all the labels of the system first, every time, which is the kind
 * of detour that makes a tool go unused.</p>
 *
 * <p>Names are scoped to a system, which a testcase reaches through its application. Resolution
 * therefore starts from the testcase, and an ambiguous name — the same label defined twice under
 * different types — is refused with both candidates rather than resolved by picking one.</p>
 */
@Component
public class MCPLabelResolver {

    private final ITestCaseService testCaseService;
    private final IApplicationService applicationService;
    private final ILabelService labelService;

    public MCPLabelResolver(ITestCaseService testCaseService,
                            IApplicationService applicationService,
                            ILabelService labelService) {
        this.testCaseService = testCaseService;
        this.applicationService = applicationService;
        this.labelService = labelService;
    }

    /** What a resolution produced: the labels found, or the reason it could not be done. */
    public record Resolution(String error, List<Label> labels) {

        public boolean failed() {
            return error != null;
        }

        static Resolution of(String error) {
            return new Resolution(error, List.of());
        }
    }

    /**
     * The system a testcase belongs to, through its application.
     *
     * @return the system name, or {@code null} when the testcase or its application cannot be read.
     */
    public String systemOf(TestCase testCase) {
        if (testCase == null || testCase.getApplication() == null || testCase.getApplication().isBlank()) {
            return null;
        }
        try {
            Application application = applicationService.convert(
                    applicationService.readByKey(testCase.getApplication()));
            return application == null ? null : application.getSystem();
        } catch (CerberusException e) {
            return null;
        }
    }

    /**
     * Reads a testcase, or explains why it could not be read.
     */
    public AnswerItem<TestCase> readTestCase(String test, String testcase) {
        return testCaseService.readByKey(test, testcase);
    }

    /**
     * Finds the labels named by a caller, by name or by id.
     *
     * @param system    the system whose labels are in scope; may be {@code null}, in which case only
     *                  ids can be resolved.
     * @param names     label names, possibly empty.
     * @param ids       label ids, possibly empty.
     * @return the labels, or the reason resolution failed, naming exactly what could not be found.
     */
    public Resolution resolve(String system, List<String> names, List<Integer> ids) {
        if ((names == null || names.isEmpty()) && (ids == null || ids.isEmpty())) {
            return Resolution.of("Name at least one label, either by name in 'labels' or by id in 'labelIds'.");
        }

        List<Label> available = labelsOf(system);
        List<Label> resolved = new ArrayList<>();
        List<String> unknown = new ArrayList<>();
        List<String> ambiguous = new ArrayList<>();

        if (ids != null) {
            for (Integer id : ids) {
                AnswerItem<Label> answer = labelService.readByKey(id);
                if (!answer.isCodeStringEquals("OK") || answer.getItem() == null) {
                    unknown.add("id " + id);
                } else {
                    resolved.add(answer.getItem());
                }
            }
        }

        if (names != null && !names.isEmpty()) {
            if (system == null) {
                return Resolution.of("Labels can only be resolved by name inside a system, and the system of "
                        + "this testcase could not be determined — its application may be missing. Pass "
                        + "'labelIds' instead, or fix the testcase's application.");
            }
            Map<String, List<Label>> byName = new LinkedHashMap<>();
            for (Label label : available) {
                byName.computeIfAbsent(label.getLabel(), key -> new ArrayList<>()).add(label);
            }
            for (String name : names) {
                List<Label> candidates = byName.get(name);
                if (candidates == null || candidates.isEmpty()) {
                    unknown.add("'" + name + "'");
                } else if (candidates.size() > 1) {
                    ambiguous.add("'" + name + "' (ids " + candidates.stream().map(Label::getId).toList() + ")");
                } else {
                    resolved.add(candidates.get(0));
                }
            }
        }

        if (!ambiguous.isEmpty()) {
            return Resolution.of("These label names exist more than once in system '" + system + "': "
                    + String.join(", ", ambiguous) + ". Name them by id in 'labelIds' instead.");
        }
        if (!unknown.isEmpty()) {
            return Resolution.of("These labels do not exist" + (system == null ? "" : " in system '" + system + "'")
                    + ": " + String.join(", ", unknown)
                    + ". Call cerberus_system_label_list to see what exists, or cerberus_system_label_create "
                    + "to define one.");
        }

        return new Resolution(null, resolved);
    }

    /**
     * Every label defined in a system.
     */
    public List<Label> labelsOf(String system) {
        if (system == null) {
            return List.of();
        }
        // readBySystemByCriteria appends the empty system to the list it is given, to fold in the
        // labels declared with no system — so it has to be handed a mutable list. An immutable one
        // fails at runtime, inside the DAO, with nothing pointing back here.
        List<String> systems = new ArrayList<>();
        systems.add(system);
        AnswerList<Label> answer = labelService.readBySystem(systems);
        return answer.getDataList() == null ? List.of() : answer.getDataList();
    }

    /**
     * Renders a label for a tool response.
     */
    public static Map<String, Object> describe(Label label) {
        Map<String, Object> described = new LinkedHashMap<>();
        described.put("labelId", label.getId());
        described.put("label", MCPToolUtils.nullSafe(label.getLabel()));
        described.put("type", MCPToolUtils.nullSafe(label.getType()));
        if (label.getColor() != null && !label.getColor().isBlank()) {
            described.put("color", label.getColor());
        }
        if (label.getDescription() != null && !label.getDescription().isBlank()) {
            described.put("description", label.getDescription());
        }
        return described;
    }
}
