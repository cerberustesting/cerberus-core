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
package org.cerberus.core.crud.factory.impl;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import org.cerberus.core.engine.entity.MessageEvent;
import org.cerberus.core.crud.entity.TestCaseExecution;
import org.cerberus.core.crud.entity.TestCaseExecutionFile;
import org.cerberus.core.crud.entity.TestCaseStep;
import org.cerberus.core.crud.entity.TestCaseStepExecution;
import org.cerberus.core.crud.entity.TestCaseStepActionExecution;
import org.cerberus.core.crud.factory.IFactoryTestCaseStepExecution;
import org.springframework.stereotype.Service;

/**
 * @author bcivel
 */
@Service
public class FactoryTestCaseStepExecution implements IFactoryTestCaseStepExecution {

    @Override
    public TestCaseStepExecution create(long id, String test, String testCase, int stepId, int index, int sort, String loop, String conditionOperator, String conditionValue1Init,
            String conditionValue2Init, String conditionValue3Init, String conditionValue1, String conditionValue2, String conditionValue3,
            String batNumExe, long start, long end, long fullStart, long fullEnd, BigDecimal timeElapsed,
            String returnCode, String returnMessage, String description) {
        TestCaseStepExecution testCaseStepExecution = create(id, test, testCase, stepId, index, sort, loop, conditionOperator, conditionValue1Init, conditionValue2Init, conditionValue3Init, conditionValue1, conditionValue2, conditionValue3, batNumExe, start, end, fullStart, fullEnd, timeElapsed, returnCode, null, null, null, false, null, null, -1, description);
        testCaseStepExecution.setReturnMessage(returnMessage);
        return testCaseStepExecution;
    }

    @Override
    public TestCaseStepExecution create(long id, String test, String testCase, int stepId, int index, int sort, String loop, String conditionOperator, String conditionValue1Init,
            String conditionValue2Init, String conditionValue3Init, String conditionValue1, String conditionValue2, String conditionValue3, String batNumExe, long start, long end, long fullStart, long fullEnd, BigDecimal timeElapsed,
            String returnCode, MessageEvent stepResultMessage,
            TestCaseStep testCaseStep, TestCaseExecution tCExecution, boolean isUsingLibraryStep, String libraryStepTest, String libraryStepTestcase, int useStepTestCaseStep, String description) {
        TestCaseStepExecution testCaseStepExecution = new TestCaseStepExecution();
        testCaseStepExecution.setBatNumExe(batNumExe);
        testCaseStepExecution.setEnd(end);
        testCaseStepExecution.setFullEnd(fullEnd);
        testCaseStepExecution.setFullStart(fullStart);
        testCaseStepExecution.setId(id);
        testCaseStepExecution.setReturnCode(returnCode);
        testCaseStepExecution.setStart(start);
        testCaseStepExecution.setStepId(stepId);
        testCaseStepExecution.setIndex(index);
        testCaseStepExecution.setSort(sort);
        testCaseStepExecution.setLoop(loop);
        testCaseStepExecution.setConditionOperator(conditionOperator);
        testCaseStepExecution.setConditionValue1Init(conditionValue1Init);
        testCaseStepExecution.setConditionValue2Init(conditionValue2Init);
        testCaseStepExecution.setConditionValue3Init(conditionValue3Init);
        testCaseStepExecution.setConditionValue1(conditionValue1);
        testCaseStepExecution.setConditionValue2(conditionValue2);
        testCaseStepExecution.setConditionValue3(conditionValue3);
        testCaseStepExecution.setTest(test);
        testCaseStepExecution.setTestCase(testCase);
        testCaseStepExecution.setTimeElapsed(timeElapsed);
        testCaseStepExecution.setStepResultMessage(stepResultMessage);
        testCaseStepExecution.setTestCaseStep(testCaseStep);
        testCaseStepExecution.settCExecution(tCExecution);
        testCaseStepExecution.setUsingLibraryStep(isUsingLibraryStep);
        testCaseStepExecution.setLibraryStepTest(libraryStepTest);
        testCaseStepExecution.setLibraryStepTestcase(libraryStepTestcase);
        testCaseStepExecution.setUseStepTestCaseStep(useStepTestCaseStep);
        testCaseStepExecution.setDescription(description);
        // List objects
        List<TestCaseExecutionFile> objectFileList = new ArrayList<>();
        testCaseStepExecution.setFileList(objectFileList);
        List<TestCaseStepActionExecution> testCaseStepActionExecution = new ArrayList<>();
        testCaseStepExecution.setTestCaseStepActionExecutionList(testCaseStepActionExecution);

        return testCaseStepExecution;
    }

}
