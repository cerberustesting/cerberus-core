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

import org.cerberus.crud.entity.TestCaseStep;
import org.cerberus.exception.CerberusException;
import org.cerberus.util.answer.Answer;
import org.cerberus.util.answer.AnswerList;

/**
 * {Insert class description here}
 *
 * @author Tiago Bernardes
 * @version 1.0, 29/12/2012
 * @since 2.0.0
 */
public interface ITestCaseStepDAO {


    List<TestCaseStep> findTestCaseStepByTestCase(String test, String testcase);

    /**
     *
     * @param test
     * @param testcase
     * @param Step
     * @return
     */
    TestCaseStep findTestCaseStep(String test, String testcase, Integer Step);

    public void deleteTestCaseStep(TestCaseStep tcs) throws CerberusException;

    public void updateTestCaseStep(TestCaseStep tcs) throws CerberusException;

    List<TestCaseStep> getTestCaseStepUsingStepInParamter(String test, String testCase, int step) throws CerberusException;

    List<TestCaseStep> getTestCaseStepUsingTestCaseInParamter(String test, String testCase) throws CerberusException;

    List<TestCaseStep> getTestCaseStepsUsingTestInParameter(final String test) throws CerberusException;

    List<TestCaseStep> getStepUsedAsLibraryInOtherTestCaseByApplication(String application) throws CerberusException;

    List<TestCaseStep> getStepLibraryBySystem(String system) throws CerberusException;

    List<TestCaseStep> getStepLibraryBySystemTest(String system, String test) throws CerberusException;

    List<TestCaseStep> getStepLibraryBySystemTestTestCase(String system, String test, String testCase) throws CerberusException;

    AnswerList<TestCaseStep> readByTestTestCase(String test, String testcase);

    AnswerList readByLibraryUsed(String test, String testcase, int step);

    /**
     *
     * @param testCaseStep
     * @return
     */
    Answer create(TestCaseStep testCaseStep);
}
