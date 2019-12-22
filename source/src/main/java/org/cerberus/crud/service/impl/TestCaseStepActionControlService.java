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
package org.cerberus.crud.service.impl;

import java.util.ArrayList;
import java.util.List;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import org.cerberus.crud.dao.ITestCaseStepActionControlDAO;
import org.cerberus.crud.entity.TestCaseStepActionControl;
import org.cerberus.exception.CerberusException;
import org.cerberus.crud.service.ITestCaseStepActionControlService;
import org.cerberus.util.answer.Answer;
import org.cerberus.util.answer.AnswerList;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 *
 * @author bcivel
 */
@Service
public class TestCaseStepActionControlService implements ITestCaseStepActionControlService {

    private static final Logger LOG = LogManager.getLogger(TestCaseStepActionControlService.class);
    
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
                LOG.warn(ex.toString());
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
            LOG.warn(ex.toString());
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
    public void compareListAndUpdateInsertDeleteElements(List<TestCaseStepActionControl> newList, List<TestCaseStepActionControl> oldList, boolean duplicate) throws CerberusException {
        /**
         * Iterate on (TestCaseStepActionControl From Page -
         * TestCaseStepActionControl From Database) If TestCaseStepActionControl
         * in Database has same key : Update and remove from the list. If
         * TestCaseStepActionControl in database does ot exist : Insert it.
         */
        List<TestCaseStepActionControl> tcsacToUpdateOrInsert = new ArrayList<>(newList);
        tcsacToUpdateOrInsert.removeAll(oldList);
        List<TestCaseStepActionControl> tcsacToUpdateOrInsertToIterate = new ArrayList<>(tcsacToUpdateOrInsert);

        for (TestCaseStepActionControl tcsacDifference : tcsacToUpdateOrInsertToIterate) {
            for (TestCaseStepActionControl tcsacInDatabase : oldList) {
                if (tcsacDifference.hasSameKey(tcsacInDatabase)) {
                    this.updateTestCaseStepActionControl(tcsacDifference);
                    tcsacToUpdateOrInsert.remove(tcsacDifference);
                }
            }
        }

        /**
         * Iterate on (TestCaseStep From Database - TestCaseStep From Page). If
         * TestCaseStep in Page has same key : remove from the list. Then delete
         * the list of TestCaseStep
         */
        if (!duplicate) {
            List<TestCaseStepActionControl> tcsacToDelete = new ArrayList<>(oldList);
            tcsacToDelete.removeAll(newList);
            List<TestCaseStepActionControl> tcsacToDeleteToIterate = new ArrayList<>(tcsacToDelete);

            for (TestCaseStepActionControl tcsacDifference : tcsacToDeleteToIterate) {
                for (TestCaseStepActionControl tcsacInPage : newList) {
                    if (tcsacDifference.hasSameKey(tcsacInPage)) {
                        tcsacToDelete.remove(tcsacDifference);
                    }
                }
            }
            this.deleteListTestCaseStepActionControl(tcsacToDelete);
        }

        // We insert only at the end (after deletion of all potencial enreg - linked with #1281)
        this.insertListTestCaseStepActionControl(tcsacToUpdateOrInsert);

    }

    @Override
    public AnswerList<TestCaseStepActionControl> readByTestTestCase(String test, String testcase) {
        return testCaseStepActionControlDao.readByTestTestCase(test, testcase);
    }

    @Override
    public AnswerList<TestCaseStepActionControl> readByVarious1(String test, String testcase, int step, int sequence) {
        return testCaseStepActionControlDao.readByVarious1(test, testcase, step, sequence);
    }

    @Override
    public Answer create(TestCaseStepActionControl object) {
        return testCaseStepActionControlDao.create(object);
    }

    @Override
    public Answer createList(List<TestCaseStepActionControl> objectList) {
        Answer ans = new Answer(null);
        for (TestCaseStepActionControl objectToCreate : objectList) {
            ans = create(objectToCreate);
        }
        return ans;
    }

    @Override
    public Answer duplicateList(List<TestCaseStepActionControl> objectList, String targetTest, String targetTestCase) {
        Answer ans = new Answer(null);
        List<TestCaseStepActionControl> listToCreate = new ArrayList<>();
        for (TestCaseStepActionControl objectToDuplicate : objectList) {
            objectToDuplicate.setTest(targetTest);
            objectToDuplicate.setTestCase(targetTestCase);
            listToCreate.add(objectToDuplicate);
        }
        ans = createList(listToCreate);
        return ans;
    }
}
