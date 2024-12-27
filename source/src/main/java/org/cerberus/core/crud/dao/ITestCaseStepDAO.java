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
package org.cerberus.core.crud.dao;

import org.cerberus.core.crud.entity.TestCaseStep;
import org.cerberus.core.exception.CerberusException;
import org.cerberus.core.util.answer.Answer;
import org.cerberus.core.util.answer.AnswerList;

import java.util.List;

/**
 * {Insert class description here}
 *
 * @author Tiago Bernardes
 * @version 1.0, 29/12/2012
 * @since 2.0.0
 */
public interface ITestCaseStepDAO {

    /**
     *
     * @param test
     * @param testcase
     * @return
     */
    List<TestCaseStep> findTestCaseStepByTestCase(String test, String testcase);

    /**
     *
     * @return
     */
    List<TestCaseStep> findAllTestcaseSteps();

    /**
     *
     * @return
     */
    List<TestCaseStep> findAllLibrarySteps();

    /**
     *
     * @param testFolderId
     * @return
     */
    List<TestCaseStep> findTestcaseStepsByTestFolderId(String testFolderId);

    /**
     * @param test
     * @param testcase
     * @param StepId
     * @return
     */
    TestCaseStep findTestCaseStep(String test, String testcase, Integer StepId);

    /**
     *
     * @param tcs
     * @throws CerberusException
     */
    public void deleteTestCaseStep(TestCaseStep tcs) throws CerberusException;

    /**
     *
     * @param tcs
     * @throws CerberusException
     */
    public void updateTestCaseStep(TestCaseStep tcs) throws CerberusException;

    /**
     * Update @field on database replacing %object.oldObject% to
     * %object.newObject% on all lines that belong to @application
     *
     * @param field
     * @param application
     * @param oldObject
     * @param newObject
     * @throws CerberusException
     */
    void updateApplicationObject(String field, String application, String oldObject, String newObject) throws CerberusException;

    /**
     *
     * @param test
     * @param testCase
     * @param stepId
     * @return
     * @throws CerberusException
     */
    List<TestCaseStep> getTestCaseStepUsingStepInParamter(String test, String testCase, int stepId) throws CerberusException;

    /**
     *
     * @param test
     * @param testCase
     * @return
     * @throws CerberusException
     */
    List<TestCaseStep> getTestCaseStepUsingTestCaseInParamter(String test, String testCase) throws CerberusException;

    /**
     *
     * @param test
     * @return
     * @throws CerberusException
     */
    List<TestCaseStep> getTestCaseStepsUsingTestInParameter(final String test) throws CerberusException;

    /**
     *
     * @param system
     * @return
     * @throws CerberusException
     */
    List<TestCaseStep> getStepLibraryBySystem(String system) throws CerberusException;

    /**
     *
     * @param system
     * @param test
     * @return
     * @throws CerberusException
     */
    List<TestCaseStep> getStepLibraryBySystemTest(String system, String test) throws CerberusException;

    /**
     *
     * @param system
     * @param test
     * @param testCase
     * @return
     * @throws CerberusException
     */
    List<TestCaseStep> getStepLibraryBySystemTestTestCase(String system, String test, String testCase) throws CerberusException;

    /**
     *
     * @param test
     * @param testcase
     * @return
     */
    AnswerList<TestCaseStep> readByTestTestCase(String test, String testcase);

    /**
     *
     * @param test
     * @param testcase
     * @param stepId
     * @return
     */
    AnswerList<TestCaseStep> readByLibraryUsed(String test, String testcase, int stepId);

    /**
     * @param testCaseStep
     * @return
     */
    Answer create(TestCaseStep testCaseStep);
}
