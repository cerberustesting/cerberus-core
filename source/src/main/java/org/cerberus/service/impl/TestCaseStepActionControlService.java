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
package org.cerberus.service.impl;

import java.util.List;

import org.apache.log4j.Level;
import org.cerberus.dao.ITestCaseStepActionControlDAO;
import org.cerberus.entity.TestCaseStepActionControl;
import org.cerberus.exception.CerberusException;
import org.cerberus.log.MyLogger;
import org.cerberus.service.ITestCaseStepActionControlService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 *
 * @author bcivel
 */
@Service
public class TestCaseStepActionControlService implements ITestCaseStepActionControlService {

    @Autowired
    private ITestCaseStepActionControlDAO testCaseStepActionControlDao;

    @Override
    public TestCaseStepActionControl findTestCaseStepActionControlByKey(String test, String testcase, int stepNumber, int sequence, int control) {
        return testCaseStepActionControlDao.findTestCaseStepActionControlByKey(test, testcase, stepNumber, sequence, control);
    }

    @Override
    public List<TestCaseStepActionControl> findControlByTestTestCaseStepSequence(String test, String testcase, int stepNumber, int sequence) {
        return testCaseStepActionControlDao.findControlByTestTestCaseStepSequence(test, testcase, stepNumber, sequence);
    }

    @Override
    public void insertTestCaseStepActionControl(TestCaseStepActionControl testCaseStepActionControl) throws CerberusException {
        testCaseStepActionControlDao.insertTestCaseStepActionControl(testCaseStepActionControl);
    }

    @Override
    public boolean insertListTestCaseStepActionControl(List<TestCaseStepActionControl> testCaseStepActionControlList) {
        for (TestCaseStepActionControl tcs : testCaseStepActionControlList) {
            try {
                insertTestCaseStepActionControl(tcs);
            } catch (CerberusException ex) {
                MyLogger.log(TestCaseStepActionControlService.class.getName(), Level.FATAL, ex.toString());
                return false;
            }
        }
        return true;
    }

    @Override
    public List<TestCaseStepActionControl> findControlByTestTestCaseStep(String test, String testcase, int step) {
        return testCaseStepActionControlDao.findControlByTestTestCaseStep(test, testcase, step);
    }

    @Override
    public boolean updateTestCaseStepActionControl(TestCaseStepActionControl control) {
        try {
            testCaseStepActionControlDao.updateTestCaseStepActionControl(control);
        } catch (CerberusException ex) {
            MyLogger.log(TestCaseStepActionControlService.class.getName(), Level.FATAL, ex.toString());
            return false;
        }
        return true;
    }

    @Override
    public List<TestCaseStepActionControl> findControlByTestTestCase(String test, String testCase) throws CerberusException {
        return testCaseStepActionControlDao.findControlByTestTestCase(test,testCase);
    }

    @Override
    public void deleteListTestCaseStepActionControl(List<TestCaseStepActionControl> tcsacToDelete) throws CerberusException {
        for (TestCaseStepActionControl tcsac : tcsacToDelete){
        deleteTestCaseStepActionControl(tcsac);
        }
    }

    @Override
    public void deleteTestCaseStepActionControl(TestCaseStepActionControl tcsac) throws CerberusException{
        testCaseStepActionControlDao.deleteTestCaseStepActionControl(tcsac);
    }
}
