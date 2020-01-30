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
package org.cerberus.crud.dao;

import java.util.List;

import org.cerberus.crud.entity.TestCaseStepActionControl;
import org.cerberus.exception.CerberusException;
import org.cerberus.util.answer.Answer;
import org.cerberus.util.answer.AnswerList;

/**
 * {Insert class description here}
 *
 * @author Tiago Bernardes
 * @version 1.0, 31/12/2012
 * @since 2.0.0
 */
public interface ITestCaseStepActionControlDAO {

    TestCaseStepActionControl findTestCaseStepActionControlByKey(String test, String testcase, int stepNumber, int sequence, int control);

    List<TestCaseStepActionControl> findControlByTestTestCaseStepSequence(String test, String testcase, int stepNumber, int sequence);

    void insertTestCaseStepActionControl(TestCaseStepActionControl testCaseStepActionControl) throws CerberusException;

    List<TestCaseStepActionControl> findControlByTestTestCaseStep(String test, String testcase, int step);

    public void updateTestCaseStepActionControl(TestCaseStepActionControl control) throws CerberusException;

    public void deleteTestCaseStepActionControl(TestCaseStepActionControl tcsac) throws CerberusException;

    public List<TestCaseStepActionControl> findControlByTestTestCase(String test, String testCase) throws CerberusException;

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

    Answer create(TestCaseStepActionControl testCaseStepActionControl);
}
