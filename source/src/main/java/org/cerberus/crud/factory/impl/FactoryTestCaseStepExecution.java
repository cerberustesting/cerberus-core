/*
 * Cerberus  Copyright (C) 2013  vertigo17
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

import java.math.BigDecimal;
import org.cerberus.crud.entity.MessageEvent;
import org.cerberus.crud.entity.TestCaseExecution;
import org.cerberus.crud.entity.TestCaseStep;
import org.cerberus.crud.entity.TestCaseStepExecution;
import org.cerberus.crud.factory.IFactoryTestCaseStepExecution;
import org.springframework.stereotype.Service;

/**
 * @author bcivel
 */
@Service
public class FactoryTestCaseStepExecution implements IFactoryTestCaseStepExecution {

    @Override
    public TestCaseStepExecution create(long id, String test, String testCase, int step, String batNumExe, long start, long end, long fullStart, long fullEnd, BigDecimal timeElapsed,
            String returnCode, MessageEvent stepResultMessage, TestCaseStep testCaseStep, TestCaseExecution tCExecution, String useStep, String useStepTest, 
                                 String useStepTestCase, int useStepTestCaseStep, String description) {
        TestCaseStepExecution testCaseStepExecution = new TestCaseStepExecution();
        testCaseStepExecution.setBatNumExe(batNumExe);
        testCaseStepExecution.setEnd(end);
        testCaseStepExecution.setFullEnd(fullEnd);
        testCaseStepExecution.setFullStart(fullStart);
        testCaseStepExecution.setId(id);
        testCaseStepExecution.setReturnCode(returnCode);
        testCaseStepExecution.setStart(start);
        testCaseStepExecution.setStep(step);
        testCaseStepExecution.setTest(test);
        testCaseStepExecution.setTestCase(testCase);
        testCaseStepExecution.setTimeElapsed(timeElapsed);
        testCaseStepExecution.setStepResultMessage(stepResultMessage);
        testCaseStepExecution.setTestCaseStep(testCaseStep);
        testCaseStepExecution.settCExecution(tCExecution);
        testCaseStepExecution.setUseStep(useStep);
        testCaseStepExecution.setUseStepTest(useStepTest);
        testCaseStepExecution.setUseStepTestCase(useStepTestCase);
        testCaseStepExecution.setUseStepTestCaseStep(useStepTestCaseStep);
        testCaseStepExecution.setDescription(description);
        return testCaseStepExecution;
    }

    @Override
    public TestCaseStepExecution create(long id, String test, String testCase, 
            int step, String batNumExe, long start, long end, long fullStart, 
            long fullEnd, BigDecimal timeElapsed, String returnCode, 
            String returnMessage, String description) {
        TestCaseStepExecution testCaseStepExecution = new TestCaseStepExecution();
        testCaseStepExecution.setBatNumExe(batNumExe);
        testCaseStepExecution.setEnd(end);
        testCaseStepExecution.setFullEnd(fullEnd);
        testCaseStepExecution.setFullStart(fullStart);
        testCaseStepExecution.setId(id);
        testCaseStepExecution.setReturnCode(returnCode);
        testCaseStepExecution.setStart(start);
        testCaseStepExecution.setStep(step);
        testCaseStepExecution.setTest(test);
        testCaseStepExecution.setTestCase(testCase);
        testCaseStepExecution.setTimeElapsed(timeElapsed);
        testCaseStepExecution.setReturnMessage(returnMessage);
        testCaseStepExecution.setDescription(description);
        return testCaseStepExecution;
    }

}
