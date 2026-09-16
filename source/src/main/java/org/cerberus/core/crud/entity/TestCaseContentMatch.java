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
package org.cerberus.core.crud.entity;

/**
 * One place where a searched string was found inside a testcase.
 *
 * <p>Carries enough coordinates to go straight to the row and change it: which testcase, which step,
 * which action or control, and which field of it. A match that only said "testcase 0001A contains
 * this" would still leave the caller to find it.</p>
 *
 * @param scope      what kind of row matched.
 * @param system     the system the testcase belongs to.
 * @param test       the test folder.
 * @param testcase   the testcase.
 * @param stepId     the step, or 0 when the match is not inside a step.
 * @param actionId   the action, or 0 when the match is not inside an action.
 * @param controlId  the control, or 0 when the match is not inside a control.
 * @param country    the country, for a property match; empty otherwise.
 * @param name       the property name, the action type or the control type, depending on the scope.
 * @param field      the column that matched, as the tools name it (value1, conditionValue2, …).
 * @param value      the full content of that field.
 */
public record TestCaseContentMatch(Scope scope,
                                   String system,
                                   String test,
                                   String testcase,
                                   int stepId,
                                   int actionId,
                                   int controlId,
                                   String country,
                                   String name,
                                   String field,
                                   String value) {

    /** The kinds of text a search can cover. */
    public enum Scope {
        /** Action operands, conditions and descriptions. */
        ACTION,
        /** Control operands, conditions and descriptions. */
        CONTROL,
        /** Step descriptions and conditions. */
        STEP,
        /** Property values — where a data library or a SQL query is named. */
        PROPERTY
    }
}
