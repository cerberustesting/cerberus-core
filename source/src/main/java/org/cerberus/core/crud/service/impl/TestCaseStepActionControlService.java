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
package org.cerberus.core.crud.service.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.cerberus.core.crud.dao.ITestCaseStepActionControlDAO;
import org.cerberus.core.crud.entity.TestCaseStepActionControl;
import org.cerberus.core.crud.service.ITestCaseStepActionControlService;
import org.cerberus.core.exception.CerberusException;
import org.cerberus.core.util.answer.Answer;
import org.cerberus.core.util.answer.AnswerList;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author bcivel
 */
@Service
public class TestCaseStepActionControlService implements ITestCaseStepActionControlService {

    private static final Logger LOG = LogManager.getLogger(TestCaseStepActionControlService.class);

    @Autowired
    private ITestCaseStepActionControlDAO testCaseStepActionControlDao;

    @Override
    public TestCaseStepActionControl findTestCaseStepActionControlByKey(String test, String testcase, int stepId, int actionId, int control) {
        return testCaseStepActionControlDao.findTestCaseStepActionControlByKey(test, testcase, stepId, actionId, control);
    }

    @Override
    public List<TestCaseStepActionControl> findControlByTestTestCaseStepIdActionId(String test, String testcase, int stepId, int actionId) {
        return testCaseStepActionControlDao.findControlByTestTestCaseStepIdActionId(test, testcase, stepId, actionId);
    }

    @Override
    public List<TestCaseStepActionControl> findControlByTestTestCaseStepId(String test, String testcase, int stepId) {
        return testCaseStepActionControlDao.findControlByTestTestCaseStepId(test, testcase, stepId);
    }

    @Override
    public boolean update(TestCaseStepActionControl control) {
        try {
            testCaseStepActionControlDao.updateTestCaseStepActionControl(control);
        } catch (CerberusException ex) {
            LOG.warn(ex.toString());
            return false;
        }
        return true;
    }

    @Override
    public void updateApplicationObject(String application, String oldObject, String newObject) {

        try {
            testCaseStepActionControlDao.updateApplicationObject("Value1", application, oldObject, newObject);
            testCaseStepActionControlDao.updateApplicationObject("Value2", application, oldObject, newObject);
            testCaseStepActionControlDao.updateApplicationObject("Value3", application, oldObject, newObject);

            testCaseStepActionControlDao.updateApplicationObject("ConditionValue1", application, oldObject, newObject);
            testCaseStepActionControlDao.updateApplicationObject("ConditionValue2", application, oldObject, newObject);
            testCaseStepActionControlDao.updateApplicationObject("ConditionValue3", application, oldObject, newObject);

            testCaseStepActionControlDao.updateApplicationObject("Description", application, oldObject, newObject);
        } catch (CerberusException ex) {
            LOG.warn(ex, ex);
        }
    }

    @Override
    public List<TestCaseStepActionControl> findControlByTestTestCase(String test, String testcase) throws CerberusException {
        return testCaseStepActionControlDao.findControlByTestTestCase(test, testcase);
    }

    @Override
    public void deleteList(List<TestCaseStepActionControl> tcsacToDelete) throws CerberusException {
        for (TestCaseStepActionControl tcsac : tcsacToDelete) {
            delete(tcsac);
        }
    }

    @Override
    public void delete(TestCaseStepActionControl tcsac) throws CerberusException {
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
                    this.update(tcsacDifference);
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
            this.deleteList(tcsacToDelete);
        }

        // We insert only at the end (after deletion of all potencial enreg - linked with #1281)
        this.createList(tcsacToUpdateOrInsert);

    }

    @Override
    public AnswerList<TestCaseStepActionControl> readByTestTestCase(String test, String testcase) {
        return testCaseStepActionControlDao.readByTestTestCase(test, testcase);
    }

    @Override
    public AnswerList<TestCaseStepActionControl> readByVarious1(String test, String testcase, int stepId, int actionId) {
        return testCaseStepActionControlDao.readByVarious1(test, testcase, stepId, actionId);
    }

    @Override
    public Answer create(TestCaseStepActionControl testCaseStepActionControl) {
        return testCaseStepActionControlDao.create(testCaseStepActionControl);
    }

    @Override
    public Answer createList(List<TestCaseStepActionControl> testCaseStepActionControls) {
        Answer ans = new Answer(null);
        for (TestCaseStepActionControl objectToCreate : testCaseStepActionControls) {
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
            objectToDuplicate.setTestcase(targetTestCase);
            listToCreate.add(objectToDuplicate);
        }
        ans = createList(listToCreate);
        return ans;
    }

    public int getMaxControlId(Collection<TestCaseStepActionControl> controls) {
        return controls
                .stream()
                .max(Comparator.comparing(TestCaseStepActionControl::getControlId))
                .map(TestCaseStepActionControl::getControlId)
                .orElse(0);
    }
}
