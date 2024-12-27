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
package org.cerberus.core.crud.factory;

import java.math.BigDecimal;
import org.cerberus.core.engine.entity.MessageEvent;
import org.cerberus.core.crud.entity.TestCaseExecution;
import org.cerberus.core.crud.entity.TestCaseStep;
import org.cerberus.core.crud.entity.TestCaseStepExecution;

/**
 * @author bcivel
 */
public interface IFactoryTestCaseStepExecution {

    TestCaseStepExecution create(long id, String test, String testCase, int stepId, int index, int sort, String loop,
            String conditionOperator, String conditionValue1Init, String conditionValue2Init, String conditionValue3Init, String conditionValue1, String conditionValue2, String conditionValue3,
            String batNumExe, long start, long end, long fullStart, long fullEnd, BigDecimal timeElapsed, String returnCode,
            String returnMessage, String description);

    TestCaseStepExecution create(long id, String test, String testCase, int stepId, int index, int sort, String loop,
            String conditionOperator, String conditionValue1Init, String conditionValue2Init, String conditionValue3Init, String conditionValue1, String conditionValue2, String conditionValue3,
            String batNumExe, long start, long end, long fullStart, long fullEnd, BigDecimal timeElapsed, String returnCode,
            MessageEvent stepResultMessage, TestCaseStep testCaseStep, TestCaseExecution tCExecution, boolean isUsingLibraryStep, String libraryStepTest,
            String libraryStepTestcase, int useStepTestCaseStep, String description);
}
