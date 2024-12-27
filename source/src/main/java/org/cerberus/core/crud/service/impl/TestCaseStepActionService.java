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

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.cerberus.core.crud.dao.ITestCaseStepActionDAO;
import org.cerberus.core.crud.entity.TestCaseStepAction;
import org.cerberus.core.crud.entity.TestCaseStepActionControl;
import org.cerberus.core.crud.service.ITestCaseStepActionControlService;
import org.cerberus.core.crud.service.ITestCaseStepActionService;
import org.cerberus.core.engine.entity.MessageEvent;
import org.cerberus.core.enums.MessageEventEnum;
import org.cerberus.core.exception.CerberusException;
import org.cerberus.core.util.answer.Answer;
import org.cerberus.core.util.answer.AnswerList;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;

/**
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
    public TestCaseStepAction findTestCaseStepActionbyKey(String test, String testcase, int stepId, int actionId) {
        return testCaseStepActionDAO.readByKey(test, testcase, stepId, actionId);
    }

    @Override
    public List<TestCaseStepAction> getListOfAction(String test, String testcase, int stepId) {
        return testCaseStepActionDAO.findActionByTestTestCaseStep(test, testcase, stepId);
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
    public boolean changeTestCaseStepActionActionId(String test, String testcase, int stepId, int oldActionId, int newActionId) {
        return testCaseStepActionDAO.changeTestCaseStepActionActionId(test, testcase, stepId, oldActionId, newActionId);
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
    public void updateApplicationObject(String application, String oldObject, String newObject) {
        try {
            testCaseStepActionDAO.updateApplicationObject("Value1", application, oldObject, newObject);
            testCaseStepActionDAO.updateApplicationObject("Value2", application, oldObject, newObject);
            testCaseStepActionDAO.updateApplicationObject("Value3", application, oldObject, newObject);

            testCaseStepActionDAO.updateApplicationObject("ConditionValue1", application, oldObject, newObject);
            testCaseStepActionDAO.updateApplicationObject("ConditionValue2", application, oldObject, newObject);
            testCaseStepActionDAO.updateApplicationObject("ConditionValue3", application, oldObject, newObject);

            testCaseStepActionDAO.updateApplicationObject("Description", application, oldObject, newObject);
        } catch (CerberusException ex) {
            LOG.warn(ex, ex);
        }
    }

    @Override
    public void updateService(String oldService, String service) throws CerberusException {
        testCaseStepActionDAO.updateService(oldService, service);
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
    public AnswerList<TestCaseStepAction> readByVarious1WithDependency(String test, String testcase, int stepId) {
        AnswerList<TestCaseStepAction> actions = testCaseStepActionDAO.readByVarious1(test, testcase, stepId);
        AnswerList<TestCaseStepAction> response = null;
        List<TestCaseStepAction> tcsaList = new ArrayList<>();
        for (Object action : actions.getDataList()) {
            TestCaseStepAction tces = (TestCaseStepAction) action;
            AnswerList<TestCaseStepActionControl> controls = testCaseStepActionControlService.readByVarious1(test, testcase, stepId, tces.getActionId());
            tces.setControls(controls.getDataList());
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
            objectToDuplicate.setTestcase(targetTestCase);
            listToCreate.add(objectToDuplicate);
        }
        ans = createList(listToCreate);
        return ans;
    }

    public int getMaxActionId(Collection<TestCaseStepAction> actions) {
        return actions
                .stream()
                .max(Comparator.comparing(TestCaseStepAction::getActionId))
                .map(TestCaseStepAction::getActionId)
                .orElse(0);
    }
}
