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

import java.util.List;

import org.cerberus.crud.entity.TestCaseStepActionControl;
import org.cerberus.exception.CerberusException;
import org.cerberus.util.answer.Answer;
import org.cerberus.util.answer.AnswerList;

/**
 *
 * @author bcivel
 */
public interface ITestCaseStepActionControlService {

    TestCaseStepActionControl findTestCaseStepActionControlByKey(String test, String testcase, int stepNumber, int sequence, int control);

    List<TestCaseStepActionControl> findControlByTestTestCaseStepSequence(String test, String testcase, int stepNumber, int sequence);

    void insertTestCaseStepActionControl(TestCaseStepActionControl testCaseStepActionControl) throws CerberusException;

    boolean insertListTestCaseStepActionControl(List<TestCaseStepActionControl> testCaseStepActionControlList);

    List<TestCaseStepActionControl> findControlByTestTestCaseStep(String test, String testcase, int step);

    public boolean updateTestCaseStepActionControl(TestCaseStepActionControl control);

    public List<TestCaseStepActionControl> findControlByTestTestCase(String initialTest, String initialTestCase) throws CerberusException;

    public void deleteListTestCaseStepActionControl(List<TestCaseStepActionControl> tcsacToDelete) throws CerberusException;

    public void deleteTestCaseStepActionControl(TestCaseStepActionControl tcsac) throws CerberusException;

    public void compareListAndUpdateInsertDeleteElements(List<TestCaseStepActionControl> newList, List<TestCaseStepActionControl> oldList, boolean duplicate) throws CerberusException;

    public AnswerList<TestCaseStepActionControl> readByTestTestCase(String test, String testcase);

    /**
     *
     * @param test
     * @param testcase
     * @param step
     * @param sequence
     * @return
     */
    public AnswerList<TestCaseStepActionControl> readByVarious1(String test, String testcase, int step, int sequence);

    Answer create(TestCaseStepActionControl object);
    
    Answer createList(List<TestCaseStepActionControl> objectList);

    Answer duplicateList(List<TestCaseStepActionControl> objectList, String test, String testCase);
}
