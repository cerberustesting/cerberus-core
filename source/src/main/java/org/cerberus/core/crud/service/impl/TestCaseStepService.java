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
import org.cerberus.core.api.exceptions.EntityNotFoundException;
import org.cerberus.core.crud.dao.ITestCaseStepDAO;
import org.cerberus.core.crud.entity.TestCaseStep;
import org.cerberus.core.crud.entity.TestCaseStepAction;
import org.cerberus.core.crud.service.ITestCaseStepService;
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
public class TestCaseStepService implements ITestCaseStepService {

    @Autowired
    private ITestCaseStepDAO testCaseStepDAO;
    @Autowired
    private TestCaseStepActionService testCaseStepActionService;

    private static final Logger LOG = LogManager.getLogger(TestCaseStepService.class);

    @Override
    public List<TestCaseStep> getListOfSteps(String test, String testcase) {
        return testCaseStepDAO.findTestCaseStepByTestCase(test, testcase);
    }

    @Override
    public TestCaseStep findTestCaseStep(String test, String testcase, Integer stepId) {
        return testCaseStepDAO.findTestCaseStep(test, testcase, stepId);
    }

    @Override
    public TestCaseStep modifyTestCaseStepDataFromUsedStep(TestCaseStep masterStep) {
        if (masterStep.isUsingLibraryStep()) {
            TestCaseStep usedStep = findTestCaseStep(masterStep.getLibraryStepTest(), masterStep.getLibraryStepTestcase(), masterStep.getLibraryStepStepId());
            // Copy the usedStep property to main step. Loop and conditionOperator are taken from used step.
            if (usedStep != null) {
                masterStep.setLoop(usedStep.getLoop());
                masterStep.setConditionOperator(usedStep.getConditionOperator());
                masterStep.setConditionValue1(usedStep.getConditionValue1());
                masterStep.setConditionValue2(usedStep.getConditionValue2());
                masterStep.setConditionValue3(usedStep.getConditionValue3());
                masterStep.setLibraryStepSort(usedStep.getSort());
            }
        }

        return masterStep;
    }

    @Override
    public void updateTestCaseStep(TestCaseStep tcs) throws CerberusException {
        testCaseStepDAO.updateTestCaseStep(tcs);
    }

    @Override
    public void updateApplicationObject(String application, String oldObject, String newObject) {
        try {
            testCaseStepDAO.updateApplicationObject("ConditionValue1", application, oldObject, newObject);
            testCaseStepDAO.updateApplicationObject("ConditionValue2", application, oldObject, newObject);
            testCaseStepDAO.updateApplicationObject("ConditionValue3", application, oldObject, newObject);

            testCaseStepDAO.updateApplicationObject("Description", application, oldObject, newObject);
        } catch (CerberusException ex) {
            LOG.warn(ex, ex);
        }
    }

    @Override
    public void deleteListTestCaseStep(List<TestCaseStep> tcsToDelete) throws CerberusException {
        for (TestCaseStep tcs : tcsToDelete) {
            deleteTestCaseStep(tcs);
        }
    }

    @Override
    public void deleteTestCaseStep(TestCaseStep tcs) throws CerberusException {
        testCaseStepDAO.deleteTestCaseStep(tcs);
    }

    @Override
    public List<TestCaseStep> getTestCaseStepUsingStepInParamter(String test, String testCase, int stepId) throws CerberusException {
        return testCaseStepDAO.getTestCaseStepUsingStepInParamter(test, testCase, stepId);
    }

    @Override
    public void compareListAndUpdateInsertDeleteElements(List<TestCaseStep> newList, List<TestCaseStep> oldList, boolean duplicate) throws CerberusException {
        /**
         * Iterate on (TestCaseStep From Page - TestCaseStep From Database) If
         * TestCaseStep in Database has same key : Update and remove from the
         * list. If TestCaseStep in database does ot exist : Insert it.
         */
        List<TestCaseStep> tcsToUpdateOrInsert = new ArrayList<>(newList);
        tcsToUpdateOrInsert.removeAll(oldList);
        List<TestCaseStep> tcsToUpdateOrInsertToIterate = new ArrayList<>(tcsToUpdateOrInsert);

        for (TestCaseStep tcsDifference : tcsToUpdateOrInsertToIterate) {
            for (TestCaseStep tcsInDatabase : oldList) {
                if (tcsDifference.hasSameKey(tcsInDatabase)) {
                    this.updateTestCaseStep(tcsDifference);
                    tcsToUpdateOrInsert.remove(tcsDifference);
//                    List<TestCaseStep> tcsDependencyToUpd = new ArrayList<TestCaseStep>();
//                    tcsDependencyToUpd.add(tcsDifference);
//                    updateTestCaseStepUsingTestCaseStepInList(tcsDependencyToUpd);
                }
            }
        }

        /**
         * Iterate on (TestCaseStep From Database - TestCaseStep From Page). If
         * TestCaseStep in Page has same key : remove from the list. Then delete
         * the list of TestCaseStep
         */
        if (!duplicate) {
            List<TestCaseStep> tcsToDelete = new ArrayList<>(oldList);
            tcsToDelete.removeAll(newList);
            List<TestCaseStep> tcsToDeleteToIterate = new ArrayList<>(tcsToDelete);

            for (TestCaseStep tcsDifference : tcsToDeleteToIterate) {
                for (TestCaseStep tcsInPage : newList) {
                    if (tcsDifference.hasSameKey(tcsInPage)) {
                        tcsToDelete.remove(tcsDifference);
                    }
                }
            }
//            updateTestCaseStepUsingTestCaseStepInList(tcsToDelete);
            this.deleteListTestCaseStep(tcsToDelete);

        }

        // We insert only at the end (after deletion of all potencial enreg - linked with #1281)
        this.createList(tcsToUpdateOrInsert);
//        updateTestCaseStepUsingTestCaseStepInList(tcsToUpdateOrInsert);
    }

    //    private void updateTestCaseStepUsingTestCaseStepInList(List<TestCaseStep> testCaseStepList) throws CerberusException {
//        for (TestCaseStep tcsDifference : testCaseStepList) {
//            if (tcsDifference.isStepInUseByOtherTestcase()) {
//                List<TestCaseStep> tcsUsingStep = this.getTestCaseStepUsingStepInParamter(tcsDifference.getTest(), tcsDifference.getTestcase(), tcsDifference.getInitialStep());
//                for (TestCaseStep tcsUS : tcsUsingStep) {
//                    tcsUS.setLibraryStepStepId(tcsDifference.getStepId());
//                    this.updateTestCaseStep(tcsUS);
//                }
//            }
//        }
//    }
    @Override
    public List<TestCaseStep> getTestCaseStepUsingTestCaseInParamter(String test, String testCase) throws CerberusException {
        return testCaseStepDAO.getTestCaseStepUsingTestCaseInParamter(test, testCase);
    }

    @Override
    public List<TestCaseStep> getTestCaseStepsUsingTestInParameter(final String test) throws CerberusException {
        return testCaseStepDAO.getTestCaseStepsUsingTestInParameter(test);
    }

    @Override
    public List<TestCaseStep> getStepLibraryBySystem(String system) throws CerberusException {
        return testCaseStepDAO.getStepLibraryBySystem(system);
    }

    @Override
    public List<TestCaseStep> getStepLibraryBySystemTest(String system, String test) throws CerberusException {
        return testCaseStepDAO.getStepLibraryBySystemTest(system, test);
    }

    @Override
    public List<TestCaseStep> getStepLibraryBySystemTestTestCase(String system, String test, String testCase) throws CerberusException {
        return testCaseStepDAO.getStepLibraryBySystemTestTestCase(system, test, testCase);
    }

    @Override
    public AnswerList<TestCaseStep> readByTestTestCase(String test, String testcase) {
        return testCaseStepDAO.readByTestTestCase(test, testcase);

    }

    @Override
    public List<TestCaseStep> readByTestTestCaseAPI(String test, String testcase) {
        AnswerList<TestCaseStep> stepsAnswer = testCaseStepDAO.readByTestTestCase(test, testcase);
        if (stepsAnswer.getDataList() == null || stepsAnswer.getDataList().isEmpty()) {
            throw new EntityNotFoundException(TestCaseStep.class, "testFolderId", test, "testcaseId", testcase);
        }
        return stepsAnswer.getDataList();
    }

    @Override
    public AnswerList<TestCaseStep> readByLibraryUsed(String test, String testcase, int stepId) {
        return testCaseStepDAO.readByLibraryUsed(test, testcase, stepId);
    }

    @Override
    public AnswerList<TestCaseStep> readByTestTestCaseStepsWithDependencies(String test, String testcase) {
        LOG.debug("TEST = " + test + " | TESCASE = " + testcase);
        AnswerList<TestCaseStep> answerSteps = this.readByTestTestCase(test, testcase);
        AnswerList<TestCaseStep> response = null;
        AnswerList<TestCaseStepAction> actions;
        List<TestCaseStep> steps = new ArrayList<>();
        for (TestCaseStep step : answerSteps.getDataList()) {
            if (step.isUsingLibraryStep()) {
                //TODO changer pour readByLibraryUsed
                TestCaseStep usedStep = this.modifyTestCaseStepDataFromUsedStep(step);
                step = usedStep;
                actions = testCaseStepActionService.readByVarious1WithDependency(step.getLibraryStepTest(), step.getLibraryStepTestcase(), step.getLibraryStepStepId());
            } else {
                actions = testCaseStepActionService.readByVarious1WithDependency(test, testcase, step.getStepId());
            }
            step.setActions(actions.getDataList());
            steps.add(step);
        }
        response = new AnswerList<>(steps, answerSteps.getTotalRows(), new MessageEvent(MessageEventEnum.DATA_OPERATION_OK));
        return response;
    }

    @Override
    public TestCaseStep readTestcaseStepWithDependencies(String test, String testcase, int stepId) {
        TestCaseStep testcaseStep = this.findTestCaseStep(test, testcase, stepId);
        AnswerList<TestCaseStepAction> actions = testCaseStepActionService.readByVarious1WithDependency(test, testcase, testcaseStep.getStepId());
        testcaseStep.setActions(actions.getDataList());
        return testcaseStep;
    }

    @Override
    public TestCaseStep readTestcaseStepWithDependenciesAPI(String test, String testcase, int stepId) {
        TestCaseStep testcaseStep = this.findTestCaseStep(test, testcase, stepId);
        if (testcaseStep == null) {
            throw new EntityNotFoundException(TestCaseStep.class, "testFolderId", test, "testcaseId", testcase, "stepId", String.valueOf(stepId));
        }
        AnswerList<TestCaseStepAction> actions = testCaseStepActionService.readByVarious1WithDependency(test, testcase, testcaseStep.getStepId());
        testcaseStep.setActions(actions.getDataList());
        return testcaseStep;
    }

    @Override
    public Answer duplicateList(List<TestCaseStep> listOfSteps, String targetTest, String targetTestCase) {
        Answer ans = new Answer(null);
        List<TestCaseStep> listToCreate = new ArrayList<>();
        for (TestCaseStep objectToDuplicate : listOfSteps) {
            objectToDuplicate.setTest(targetTest);
            objectToDuplicate.setTestcase(targetTestCase);
            listToCreate.add(objectToDuplicate);
        }
        return createList(listToCreate);
    }

    @Override
    public Answer create(TestCaseStep object) {
        return testCaseStepDAO.create(object);
    }

    @Override
    public Answer createList(List<TestCaseStep> objectList) {
        Answer ans = new Answer(null);
        for (TestCaseStep objectToCreate : objectList) {
            ans = testCaseStepDAO.create(objectToCreate);
        }
        return ans;
    }

    @Override
    public int getMaxStepId(Collection<TestCaseStep> steps) {
        return steps
                .stream()
                .max(Comparator.comparing(TestCaseStep::getStepId))
                .map(TestCaseStep::getStepId)
                .orElse(0);
    }

}
