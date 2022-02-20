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
package org.cerberus.crud.service;

import org.cerberus.crud.entity.TestCaseStepActionControl;
import org.cerberus.exception.CerberusException;
import org.cerberus.util.answer.Answer;
import org.cerberus.util.answer.AnswerList;

import java.util.Collection;
import java.util.List;

/**
 * @author bcivel
 */
public interface ITestCaseStepActionControlService {

    TestCaseStepActionControl findTestCaseStepActionControlByKey(String test, String testcase, int stepId, int actionId, int control);

    List<TestCaseStepActionControl> findControlByTestTestCaseStepIdActionId(String test, String testcase, int stepId, int actionId);

    List<TestCaseStepActionControl> findControlByTestTestCaseStepId(String test, String testcase, int stepId);

    public boolean update(TestCaseStepActionControl testCaseStepActionControl);

    public List<TestCaseStepActionControl> findControlByTestTestCase(String initialTest, String initialTestCase) throws CerberusException;

    public void deleteList(List<TestCaseStepActionControl> tcsacToDelete) throws CerberusException;

    public void delete(TestCaseStepActionControl testCaseStepActionControl) throws CerberusException;

    public void compareListAndUpdateInsertDeleteElements(List<TestCaseStepActionControl> newList, List<TestCaseStepActionControl> oldList, boolean duplicate) throws CerberusException;

    public AnswerList<TestCaseStepActionControl> readByTestTestCase(String test, String testcase);

    /**
     * @param test
     * @param testcase
     * @param stepId
     * @param actionId
     * @return
     */
    public AnswerList<TestCaseStepActionControl> readByVarious1(String test, String testcase, int stepId, int actionId);

    Answer create(TestCaseStepActionControl testCaseStepActionControl);

    Answer createList(List<TestCaseStepActionControl> testCaseStepActionControls);

    Answer duplicateList(List<TestCaseStepActionControl> testCaseStepActionControls, String test, String testcase);

    /**
     * Get the highest controlId from the given controls
     *
     * @param controls a collection of controls from which get the highest controlId
     * @return the highest controlId from the given controls
     */
    int getMaxControlId(Collection<TestCaseStepActionControl> controls);
}
