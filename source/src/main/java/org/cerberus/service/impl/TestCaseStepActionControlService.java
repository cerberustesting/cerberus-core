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

import java.util.ArrayList;
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
        return testCaseStepActionControlDao.findControlByTestTestCase(test, testCase);
    }

    @Override
    public void deleteListTestCaseStepActionControl(List<TestCaseStepActionControl> tcsacToDelete) throws CerberusException {
        for (TestCaseStepActionControl tcsac : tcsacToDelete) {
            deleteTestCaseStepActionControl(tcsac);
        }
    }

    @Override
    public void deleteTestCaseStepActionControl(TestCaseStepActionControl tcsac) throws CerberusException {
        testCaseStepActionControlDao.deleteTestCaseStepActionControl(tcsac);
    }

    @Override
    public void compareListAndUpdateInsertDeleteElements(List<TestCaseStepActionControl> newList, List<TestCaseStepActionControl> oldList) throws CerberusException {
        /**
         * Iterate on (TestCaseStepActionControl From Page -
         * TestCaseStepActionControl From Database) If TestCaseStepActionControl
         * in Database has same key : Update and remove from the list. If
         * TestCaseStepActionControl in database does ot exist : Insert it.
         */
        List<TestCaseStepActionControl> tcsacToUpdateOrInsert = new ArrayList(newList);
        tcsacToUpdateOrInsert.removeAll(oldList);
        List<TestCaseStepActionControl> tcsacToUpdateOrInsertToIterate = new ArrayList(tcsacToUpdateOrInsert);

        for (TestCaseStepActionControl tcsacDifference : tcsacToUpdateOrInsertToIterate) {
            for (TestCaseStepActionControl tcsacInDatabase : oldList) {
                if (tcsacDifference.hasSameKey(tcsacInDatabase)) {
                    this.updateTestCaseStepActionControl(tcsacDifference);
                    tcsacToUpdateOrInsert.remove(tcsacDifference);
                }
            }
        }
        this.insertListTestCaseStepActionControl(tcsacToUpdateOrInsert);

        /**
         * Iterate on (TestCaseStep From Database - TestCaseStep From Page). If
         * TestCaseStep in Page has same key : remove from the list. Then delete
         * the list of TestCaseStep
         */
        List<TestCaseStepActionControl> tcsacToDelete = new ArrayList(oldList);
        tcsacToDelete.removeAll(newList);
        List<TestCaseStepActionControl> tcsacToDeleteToIterate = new ArrayList(tcsacToDelete);

        for (TestCaseStepActionControl tcsacDifference : tcsacToDeleteToIterate) {
            for (TestCaseStepActionControl tcsacInPage : newList) {
                if (tcsacDifference.hasSameKey(tcsacInPage)) {
                    tcsacToDelete.remove(tcsacDifference);
                }
            }
        }
        this.deleteListTestCaseStepActionControl(tcsacToDelete);
    }
}
