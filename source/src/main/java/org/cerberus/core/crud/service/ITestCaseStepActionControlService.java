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

import org.cerberus.core.crud.entity.TestCaseStepActionControl;
import org.cerberus.core.exception.CerberusException;
import org.cerberus.core.util.answer.Answer;
import org.cerberus.core.util.answer.AnswerList;

import java.util.Collection;
import java.util.List;

/**
 * @author bcivel
 */
public interface ITestCaseStepActionControlService {

    /**
     *
     * @param test
     * @param testcase
     * @param stepId
     * @param actionId
     * @param control
     * @return
     */
    TestCaseStepActionControl findTestCaseStepActionControlByKey(String test, String testcase, int stepId, int actionId, int control);

    /**
     *
     * @param test
     * @param testcase
     * @param stepId
     * @param actionId
     * @return
     */
    List<TestCaseStepActionControl> findControlByTestTestCaseStepIdActionId(String test, String testcase, int stepId, int actionId);

    /**
     *
     * @param test
     * @param testcase
     * @param stepId
     * @return
     */
    List<TestCaseStepActionControl> findControlByTestTestCaseStepId(String test, String testcase, int stepId);

    /**
     *
     * @param testCaseStepActionControl
     * @return
     */
    public boolean update(TestCaseStepActionControl testCaseStepActionControl);
    
    /**
     *
     * @param application
     * @param oldObject
     * @param newObject
     */
    public void updateApplicationObject(String application, String oldObject, String newObject);

    /**
     *
     * @param initialTest
     * @param initialTestCase
     * @return
     * @throws CerberusException
     */
    public List<TestCaseStepActionControl> findControlByTestTestCase(String initialTest, String initialTestCase) throws CerberusException;

    /**
     *
     * @param tcsacToDelete
     * @throws CerberusException
     */
    public void deleteList(List<TestCaseStepActionControl> tcsacToDelete) throws CerberusException;

    /**
     *
     * @param testCaseStepActionControl
     * @throws CerberusException
     */
    public void delete(TestCaseStepActionControl testCaseStepActionControl) throws CerberusException;

    /**
     *
     * @param newList
     * @param oldList
     * @param duplicate
     * @throws CerberusException
     */
    public void compareListAndUpdateInsertDeleteElements(List<TestCaseStepActionControl> newList, List<TestCaseStepActionControl> oldList, boolean duplicate) throws CerberusException;

    /**
     *
     * @param test
     * @param testcase
     * @return
     */
    public AnswerList<TestCaseStepActionControl> readByTestTestCase(String test, String testcase);

    /**
     * @param test
     * @param testcase
     * @param stepId
     * @param actionId
     * @return
     */
    public AnswerList<TestCaseStepActionControl> readByVarious1(String test, String testcase, int stepId, int actionId);

    /**
     *
     * @param testCaseStepActionControl
     * @return
     */
    Answer create(TestCaseStepActionControl testCaseStepActionControl);

    /**
     *
     * @param testCaseStepActionControls
     * @return
     */
    Answer createList(List<TestCaseStepActionControl> testCaseStepActionControls);

    /**
     *
     * @param testCaseStepActionControls
     * @param test
     * @param testcase
     * @return
     */
    Answer duplicateList(List<TestCaseStepActionControl> testCaseStepActionControls, String test, String testcase);

    /**
     * Get the highest controlId from the given controls
     *
     * @param controls a collection of controls from which get the highest controlId
     * @return the highest controlId from the given controls
     */
    int getMaxControlId(Collection<TestCaseStepActionControl> controls);
}
