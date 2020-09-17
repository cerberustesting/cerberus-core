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

import org.cerberus.crud.dao.ITestCaseStepActionDAO;
import org.cerberus.crud.entity.TestCaseStepAction;
import org.cerberus.crud.entity.TestCaseStepActionControl;
import org.cerberus.crud.service.ITestCaseStepActionControlService;
import org.cerberus.engine.entity.MessageEvent;
import org.cerberus.enums.MessageEventEnum;
import org.cerberus.exception.CerberusException;
import org.cerberus.crud.service.ITestCaseStepActionService;
import org.cerberus.util.answer.Answer;
import org.cerberus.util.answer.AnswerList;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 *
 * @author bcivel
 */
@Service
public class TestCaseStepActionService implements ITestCaseStepActionService {

    private static final Logger LOG = LogManager.getLogger(TestCaseStepActionService.class);
    
    @Autowired
    private ITestCaseStepActionDAO testCaseStepActionDAO;
    @Autowired
    private ITestCaseStepActionControlService testCaseStepActionControlService;

    @Override
    public TestCaseStepAction findTestCaseStepActionbyKey(String test, String testCase, int step, int sequence) {
        return testCaseStepActionDAO.readByKey(test, testCase, step, sequence);
    }

    @Override
    public List<TestCaseStepAction> getListOfAction(String test, String testcase, int step) {
        return testCaseStepActionDAO.findActionByTestTestCaseStep(test, testcase, step);
    }

    @Override
    public void insertTestCaseStepAction(TestCaseStepAction testCaseStepAction) throws CerberusException {
        testCaseStepActionDAO.createTestCaseStepAction(testCaseStepAction);
    }

    @Override
    public boolean insertListTestCaseStepAction(List<TestCaseStepAction> testCaseStepActionList) {
        for (TestCaseStepAction tcsa : testCaseStepActionList) {
            try {
                insertTestCaseStepAction(tcsa);
            } catch (CerberusException ex) {
                LOG.warn(ex);
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean changeTestCaseStepActionSequence(String test, String testCase, int step, int oldSequence, int newSequence) {
        return testCaseStepActionDAO.changeTestCaseStepActionSequence(test, testCase, step, oldSequence, newSequence);
    }

    @Override
    public boolean updateTestCaseStepAction(TestCaseStepAction tcsa) {
        try {
            testCaseStepActionDAO.update(tcsa);
        } catch (CerberusException ex) {
            LOG.warn(ex);
            return false;
        }
        return true;
    }

    @Override
    public List<TestCaseStepAction> findTestCaseStepActionbyTestTestCase(String test, String testCase) throws CerberusException {
        return testCaseStepActionDAO.findTestCaseStepActionbyTestTestCase(test, testCase);

    }

    @Override
    public void deleteListTestCaseStepAction(List<TestCaseStepAction> tcsaToDelete) throws CerberusException {
        for (TestCaseStepAction tcsa : tcsaToDelete) {
            deleteTestCaseStepAction(tcsa);
        }
    }

    @Override
    public void deleteTestCaseStepAction(TestCaseStepAction tcsa) throws CerberusException {
        testCaseStepActionDAO.delete(tcsa);
    }

    @Override
    public void compareListAndUpdateInsertDeleteElements(List<TestCaseStepAction> newList, List<TestCaseStepAction> oldList, boolean duplicate) throws CerberusException {
        /**
         * Iterate on (TestCaseStepAction From Page - TestCaseStepAction From
         * Database) If TestCaseStepAction in Database has same key : Update and
         * remove from the list. If TestCaseStepAction in database does ot exist
         * : Insert it.
         */
        List<TestCaseStepAction> tcsaToUpdateOrInsert = new ArrayList<>(newList);
        tcsaToUpdateOrInsert.removeAll(oldList);
        List<TestCaseStepAction> tcsaToUpdateOrInsertToIterate = new ArrayList<>(tcsaToUpdateOrInsert);

        for (TestCaseStepAction tcsaDifference : tcsaToUpdateOrInsertToIterate) {
            for (TestCaseStepAction tcsaInDatabase : oldList) {
                if (tcsaDifference.hasSameKey(tcsaInDatabase)) {
                    this.updateTestCaseStepAction(tcsaDifference);
                    tcsaToUpdateOrInsert.remove(tcsaDifference);
                }
            }
        }

        /**
         * Iterate on (TestCaseStepAction From Database - TestCaseStepAction
         * From Page). If TestCaseStepAction in Page has same key : remove from
         * the list. Then delete the list of TestCaseStepAction
         */
        if (!duplicate) {
            List<TestCaseStepAction> tcsaToDelete = new ArrayList<>(oldList);
            tcsaToDelete.removeAll(newList);
            List<TestCaseStepAction> tcsaToDeleteToIterate = new ArrayList<>(tcsaToDelete);

            for (TestCaseStepAction tcsaDifference : tcsaToDeleteToIterate) {
                for (TestCaseStepAction tcsaInPage : newList) {
                    if (tcsaDifference.hasSameKey(tcsaInPage)) {
                        tcsaToDelete.remove(tcsaDifference);
                    }
                }
            }
            this.deleteListTestCaseStepAction(tcsaToDelete);
        }

        // We insert only at the end (after deletion of all potencial enreg - linked with #1281)
        this.insertListTestCaseStepAction(tcsaToUpdateOrInsert);
    }

    @Override
    public AnswerList<TestCaseStepAction> readByTestTestCase(String test, String testcase) {
        return testCaseStepActionDAO.readByTestTestCase(test, testcase);
    }

    @Override
    public AnswerList<TestCaseStepAction> readByVarious1WithDependency(String test, String testcase, int step) {
        AnswerList<TestCaseStepAction> actions = testCaseStepActionDAO.readByVarious1(test, testcase, step);
        AnswerList<TestCaseStepAction> response = null;
        List<TestCaseStepAction> tcsaList = new ArrayList<>();
        for (Object action : actions.getDataList()) {
            TestCaseStepAction tces = (TestCaseStepAction) action;
            AnswerList<TestCaseStepActionControl> controls = testCaseStepActionControlService.readByVarious1(test, testcase, step, tces.getSequence());
            tces.setControls((List<TestCaseStepActionControl>) controls.getDataList());
            tcsaList.add(tces);
        }
        response = new AnswerList<>(tcsaList, actions.getTotalRows(), new MessageEvent(MessageEventEnum.DATA_OPERATION_OK));
        return response;
    }

    @Override
    public Answer create(TestCaseStepAction object) {
        return testCaseStepActionDAO.create(object);
    }

    @Override
    public Answer createList(List<TestCaseStepAction> objectList) {
        Answer ans = new Answer(null);
        for (TestCaseStepAction objectToCreate : objectList) {
            ans = create(objectToCreate);
        }
        return ans;
    }

    @Override
    public Answer duplicateList(List<TestCaseStepAction> objectList, String targetTest, String targetTestCase) {
        Answer ans = new Answer(null);
        List<TestCaseStepAction> listToCreate = new ArrayList<>();
        for (TestCaseStepAction objectToDuplicate : objectList) {
            objectToDuplicate.setTest(targetTest);
            objectToDuplicate.setTestCase(targetTestCase);
            listToCreate.add(objectToDuplicate);
        }
        ans = createList(listToCreate);
        return ans;
    }
}
