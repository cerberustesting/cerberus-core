/**
 * Cerberus Copyright (C) 2013 - 2017 cerberustesting
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
package org.cerberus.crud.factory.impl;

import java.util.ArrayList;
import java.util.List;
import org.cerberus.crud.entity.TestCaseExecutionFile;
import org.cerberus.engine.entity.MessageEvent;
import org.cerberus.crud.entity.TestCaseStepAction;
import org.cerberus.crud.entity.TestCaseStepActionControlExecution;
import org.cerberus.crud.entity.TestCaseStepActionExecution;
import org.cerberus.crud.entity.TestCaseStepExecution;
import org.cerberus.crud.factory.IFactoryTestCaseStepActionExecution;
import org.springframework.stereotype.Service;

/**
 * @author bcivel
 */
@Service
public class FactoryTestCaseStepActionExecution implements IFactoryTestCaseStepActionExecution {

    @Override
    public TestCaseStepActionExecution create(long id, String test, String testCase, int step, int index, int sequence, int sort, String returnCode, String returnMessage,
            String conditionOperator, String conditionVal1Init, String conditionVal2Init, String conditionVal3Init, String conditionVal1, String conditionVal2, String conditionVal3,
            String action, String value1Init, String value2Init, String value3Init, String value1, String value2, String value3,
            String forceExeStatus, long start, long end, long startLong, long endLong, MessageEvent resultMessage, String description, TestCaseStepAction testCaseStepAction,
            TestCaseStepExecution testCaseStepExecution) {
        TestCaseStepActionExecution testCaseStepActionExecution = new TestCaseStepActionExecution();
        testCaseStepActionExecution.setAction(action);
        testCaseStepActionExecution.setEnd(end);
        testCaseStepActionExecution.setEndLong(endLong);
        testCaseStepActionExecution.setId(id);
        testCaseStepActionExecution.setConditionOperator(conditionOperator);
        testCaseStepActionExecution.setConditionVal1Init(conditionVal1Init);
        testCaseStepActionExecution.setConditionVal2Init(conditionVal2Init);
        testCaseStepActionExecution.setConditionVal3Init(conditionVal3Init);
        testCaseStepActionExecution.setConditionVal1(conditionVal1);
        testCaseStepActionExecution.setConditionVal2(conditionVal2);
        testCaseStepActionExecution.setConditionVal3(conditionVal3);
        testCaseStepActionExecution.setValue1(value1);
        testCaseStepActionExecution.setValue2(value2);
        testCaseStepActionExecution.setValue3(value3);
        testCaseStepActionExecution.setValue1Init(value1Init);
        testCaseStepActionExecution.setValue2Init(value2Init);
        testCaseStepActionExecution.setValue3Init(value3Init);
        testCaseStepActionExecution.setForceExeStatus(forceExeStatus);
        testCaseStepActionExecution.setReturnCode(returnCode);
        testCaseStepActionExecution.setReturnMessage(returnMessage);
        testCaseStepActionExecution.setSequence(sequence);
        testCaseStepActionExecution.setSort(sort);
        testCaseStepActionExecution.setStart(start);
        testCaseStepActionExecution.setStartLong(startLong);
        testCaseStepActionExecution.setStep(step);
        testCaseStepActionExecution.setIndex(index);
        testCaseStepActionExecution.setTest(test);
        testCaseStepActionExecution.setTestCase(testCase);
        testCaseStepActionExecution.setActionResultMessage(resultMessage);
        testCaseStepActionExecution.setTestCaseStepAction(testCaseStepAction);
        testCaseStepActionExecution.setTestCaseStepExecution(testCaseStepExecution);
        testCaseStepActionExecution.setDescription(description);
        // List objects
        List<TestCaseExecutionFile> objectFileList = new ArrayList<>();
        testCaseStepActionExecution.setFileList(objectFileList);
        List<TestCaseStepActionControlExecution> testCaseStepActionControlExecution = new ArrayList<>();
        testCaseStepActionExecution.setTestCaseStepActionControlExecutionList(testCaseStepActionControlExecution);
        return testCaseStepActionExecution;
    }

}
