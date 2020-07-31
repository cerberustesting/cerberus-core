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
import org.cerberus.crud.dao.ITestCaseStepDAO;
import org.cerberus.engine.entity.MessageEvent;
import org.cerberus.crud.entity.TestCaseStep;
import org.cerberus.crud.entity.TestCaseStepAction;
import org.cerberus.exception.CerberusException;
import org.cerberus.crud.service.ITestCaseStepService;
import org.cerberus.enums.MessageEventEnum;
import org.cerberus.util.answer.Answer;
import org.cerberus.util.answer.AnswerList;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 *
 * @author bcivel
 */
@Service
public class TestCaseStepService implements ITestCaseStepService {

    @Autowired
    private ITestCaseStepDAO testCaseStepDAO;
    @Autowired
    private TestCaseStepActionService testCaseStepActionService;

    @Override
    public List<TestCaseStep> getListOfSteps(String test, String testcase) {
        return testCaseStepDAO.findTestCaseStepByTestCase(test, testcase);
    }

    @Override
    public TestCaseStep findTestCaseStep(String test, String testcase, Integer step) {
        return testCaseStepDAO.findTestCaseStep(test, testcase, step);
    }

    @Override
    public TestCaseStep modifyTestCaseStepDataFromUsedStep(TestCaseStep masterStep) {
        if (masterStep.getUseStep().equals("Y")) {
            TestCaseStep usedStep = findTestCaseStep(masterStep.getUseStepTest(), masterStep.getUseStepTestCase(), masterStep.getUseStepStep());
            // Copy the usedStep property to main step. Loop and conditionOperator are taken from used step.
            if (usedStep != null) {
                masterStep.setLoop(usedStep.getLoop());
                masterStep.setConditionOperator(usedStep.getConditionOperator());
                masterStep.setConditionVal1(usedStep.getConditionVal1());
                masterStep.setConditionVal2(usedStep.getConditionVal2());
                masterStep.setConditionVal3(usedStep.getConditionVal3());
            }
        }

        return masterStep;
    }

    @Override
    public void updateTestCaseStep(TestCaseStep tcs) throws CerberusException {
        testCaseStepDAO.updateTestCaseStep(tcs);
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
    public List<TestCaseStep> getTestCaseStepUsingStepInParamter(String test, String testCase, int step) throws CerberusException {
        return testCaseStepDAO.getTestCaseStepUsingStepInParamter(test, testCase, step);
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
                    List<TestCaseStep> tcsDependencyToUpd = new ArrayList<TestCaseStep>();
                    tcsDependencyToUpd.add(tcsDifference);
                    updateTestCaseStepUsingTestCaseStepInList(tcsDependencyToUpd);
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
            updateTestCaseStepUsingTestCaseStepInList(tcsToDelete);
            this.deleteListTestCaseStep(tcsToDelete);

        }

        // We insert only at the end (after deletion of all potencial enreg - linked with #1281)
        this.createList(tcsToUpdateOrInsert);
        updateTestCaseStepUsingTestCaseStepInList(tcsToUpdateOrInsert);
    }

    private void updateTestCaseStepUsingTestCaseStepInList(List<TestCaseStep> testCaseStepList) throws CerberusException {
        for (TestCaseStep tcsDifference : testCaseStepList) {
            if (tcsDifference.isIsStepInUseByOtherTestCase()) {
                List<TestCaseStep> tcsUsingStep = this.getTestCaseStepUsingStepInParamter(tcsDifference.getTest(), tcsDifference.getTestCase(), tcsDifference.getInitialStep());
                for (TestCaseStep tcsUS : tcsUsingStep) {
                    tcsUS.setUseStepStep(tcsDifference.getStep());
                    this.updateTestCaseStep(tcsUS);
                }
            }
        }
    }

    @Override
    public List<TestCaseStep> getTestCaseStepUsingTestCaseInParamter(String test, String testCase) throws CerberusException {
        return testCaseStepDAO.getTestCaseStepUsingTestCaseInParamter(test, testCase);
    }

    @Override
    public List<TestCaseStep> getTestCaseStepsUsingTestInParameter(final String test) throws CerberusException {
        return testCaseStepDAO.getTestCaseStepsUsingTestInParameter(test);
    }

    @Override
    public List<TestCaseStep> getStepUsedAsLibraryInOtherTestCaseByApplication(String application) throws CerberusException {
        return testCaseStepDAO.getStepUsedAsLibraryInOtherTestCaseByApplication(application);
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
    public AnswerList readByLibraryUsed(String test, String testcase, int step) {
        return testCaseStepDAO.readByLibraryUsed(test, testcase, step);
    }

    @Override
    public AnswerList<TestCaseStep> readByTestTestCaseStepsWithDependencies(String test, String testcase) {
        AnswerList<TestCaseStep> answerSteps = this.readByTestTestCase(test, testcase);
        AnswerList<TestCaseStep> response = null;
        AnswerList<TestCaseStepAction> actions;
        List<TestCaseStep> steps = new ArrayList<>();
        for (TestCaseStep step : answerSteps.getDataList()) {
            if (step.getUseStep().equals("Y")) {
                TestCaseStep usedStep = this.findTestCaseStep(step.getUseStepTest(), step.getUseStepTestCase(), step.getUseStepStep());
                step.setUseStepStepSort(usedStep.getSort());
                actions = testCaseStepActionService.readByVarious1WithDependency(step.getUseStepTest(), step.getUseStepTestCase(), step.getUseStepStep());
            } else {
                actions = testCaseStepActionService.readByVarious1WithDependency(test, testcase, step.getStep());
            }
            step.setActions(actions.getDataList());
            steps.add(step);
        }
        response = new AnswerList<>(steps, answerSteps.getTotalRows(), new MessageEvent(MessageEventEnum.DATA_OPERATION_OK));
        return response;
    }

    @Override
    public Answer duplicateList(List<TestCaseStep> listOfSteps, String targetTest, String targetTestCase) {
        Answer ans = new Answer(null);
        List<TestCaseStep> listToCreate = new ArrayList<>();
        for (TestCaseStep objectToDuplicate : listOfSteps) {
            objectToDuplicate.setTest(targetTest);
            objectToDuplicate.setTestCase(targetTestCase);
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

}
