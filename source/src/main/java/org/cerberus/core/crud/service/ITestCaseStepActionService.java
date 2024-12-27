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
package org.cerberus.core.crud.service;

import org.cerberus.core.crud.entity.TestCaseStepAction;
import org.cerberus.core.exception.CerberusException;
import org.cerberus.core.util.answer.Answer;
import org.cerberus.core.util.answer.AnswerList;

import java.util.Collection;
import java.util.List;

/**
 * @author bcivel
 */
public interface ITestCaseStepActionService {

    /**
     *
     * @param test
     * @param testCase
     * @param stepId
     * @param actionId
     * @return
     */
    TestCaseStepAction findTestCaseStepActionbyKey(String test, String testCase, int stepId, int actionId);

    /**
     *
     * @param test
     * @param testcase
     * @param stepId
     * @return
     */
    List<TestCaseStepAction> getListOfAction(String test, String testcase, int stepId);

    /**
     *
     * @param testCaseStepAction
     * @throws CerberusException
     */
    void insertTestCaseStepAction(TestCaseStepAction testCaseStepAction) throws CerberusException;

    /**
     *
     * @param testCaseStepActionList
     * @return
     */
    boolean insertListTestCaseStepAction(List<TestCaseStepAction> testCaseStepActionList);

    /**
     *
     * @param test
     * @param testCase
     * @param stepId
     * @param oldActionId
     * @param newActionId
     * @return
     */
    boolean changeTestCaseStepActionActionId(String test, String testCase, int stepId, int oldActionId, int newActionId);

    /**
     *
     * @param tcsa
     * @return
     */
    public boolean updateTestCaseStepAction(TestCaseStepAction tcsa);

    /**
     *
     * @param application
     * @param oldObject
     * @param newObject
     */
    public void updateApplicationObject(String application, String oldObject, String newObject);

    /**
     *
     * @param oldService
     * @param service
     * @throws CerberusException
     */
    public void updateService(String oldService, String service) throws CerberusException;

    /**
     *
     * @param tcsa
     * @throws CerberusException
     */
    public void deleteTestCaseStepAction(TestCaseStepAction tcsa) throws CerberusException;

    /**
     *
     * @param tcsaToDelete
     * @throws CerberusException
     */
    public void deleteListTestCaseStepAction(List<TestCaseStepAction> tcsaToDelete) throws CerberusException;

    /**
     *
     * @param newList
     * @param oldList
     * @param duplicate
     * @throws CerberusException
     */
    public void compareListAndUpdateInsertDeleteElements(List<TestCaseStepAction> newList, List<TestCaseStepAction> oldList, boolean duplicate) throws CerberusException;

    /**
     *
     * @param test
     * @param testcase
     * @return
     */
    public AnswerList<TestCaseStepAction> readByTestTestCase(String test, String testcase);

    /**
     *
     * @param test
     * @param testcase
     * @param stepId
     * @return
     */
    public AnswerList<TestCaseStepAction> readByVarious1WithDependency(String test, String testcase, int stepId);

    /**
     *
     * @param object
     * @return
     */
    Answer create(TestCaseStepAction object);

    /**
     *
     * @param objectList
     * @return
     */
    Answer createList(List<TestCaseStepAction> objectList);

    /**
     *
     * @param objectList
     * @param test
     * @param testCase
     * @return
     */
    Answer duplicateList(List<TestCaseStepAction> objectList, String test, String testCase);

    /**
     * Get the highest actionId from the given actions
     *
     * @param actions a collection of actions from which get the highest
     * actionId
     * @return the highest actionId from the given actions
     */
    int getMaxActionId(Collection<TestCaseStepAction> actions);
}
