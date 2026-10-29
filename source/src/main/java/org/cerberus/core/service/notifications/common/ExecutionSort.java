/**
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
package org.cerberus.core.service.notifications.common;

import java.util.Comparator;
import org.cerberus.core.crud.entity.TestCaseExecution;

/**
 * Sorts the executions of the end of campaign notifications : by priority (unknown priorities last), then test,
 * testcase, environment, country and robot.
 */
public class ExecutionSort implements Comparator<TestCaseExecution> {

    @Override
    public int compare(TestCaseExecution a, TestCaseExecution b) {
        if (a != null && b != null) {
            int aPrio = a.getTestCasePriority();
            if (a.getTestCasePriority() < 1 || a.getTestCasePriority() > 5) {
                aPrio = 999 + a.getTestCasePriority();
            }
            int bPrio = b.getTestCasePriority();
            if (b.getTestCasePriority() < 1 || b.getTestCasePriority() > 5) {
                bPrio = 999 + b.getTestCasePriority();
            }

            if (aPrio == bPrio) {
                if (a.getTest().equals(b.getTest())) {
                    if (a.getTestCase().equals(b.getTestCase())) {
                        if (a.getEnvironment().equals(b.getEnvironment())) {
                            if (a.getCountry().equals(b.getCountry())) {
                                return a.getRobotDecli().compareToIgnoreCase(b.getRobotDecli());
                            } else {
                                return a.getCountry().compareToIgnoreCase(b.getCountry());
                            }
                        } else {
                            return a.getEnvironment().compareToIgnoreCase(b.getEnvironment());
                        }
                    } else {
                        return a.getTestCase().compareToIgnoreCase(b.getTestCase());
                    }
                } else {
                    return a.getTest().compareToIgnoreCase(b.getTest());
                }
            } else {
                return aPrio - bPrio;
            }
        } else {
            return 1;
        }
    }
}
