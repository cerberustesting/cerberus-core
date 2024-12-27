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

import org.cerberus.core.crud.entity.TestCaseStepAction;
import org.cerberus.core.exception.CerberusException;
import org.cerberus.core.util.answer.Answer;
import org.cerberus.core.util.answer.AnswerList;

import java.util.List;

/**
 * {Insert class description here}
 *
 * @author Tiago Bernardes
 * @version 1.0, 31/Dez/2012
 * @since 2.0.0
 */
public interface ITestCaseStepActionDAO {

    /**
     *
     * @param test
     * @param testcase
     * @param stepId
     * @param actionId
     * @return
     */
    TestCaseStepAction readByKey(String test, String testcase, int stepId, int actionId);

    /**
     *
     * @param test
     * @param testcase
     * @param stepId
     * @return
     */
    List<TestCaseStepAction> findActionByTestTestCaseStep(String test, String testcase, int stepId);

    /**
     *
     * @param testCaseStepAction
     * @throws CerberusException
     */
    void createTestCaseStepAction(TestCaseStepAction testCaseStepAction) throws CerberusException;

    /**
     *
     * @param test
     * @param testcase
     * @param stepid
     * @param oldActionId
     * @param newActionId
     * @return
     */
    boolean changeTestCaseStepActionActionId(String test, String testcase, int stepid, int oldActionId, int newActionId);

    /**
     *
     * @param tcsa
     * @throws CerberusException
     */
    void update(TestCaseStepAction tcsa) throws CerberusException;

    /**
     *
     * @param oldService
     * @param service
     * @throws CerberusException
     */
    void updateService(String oldService, String service) throws CerberusException;

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
     * @param tcsa
     * @throws CerberusException
     */
    void delete(TestCaseStepAction tcsa) throws CerberusException;

    /**
     *
     * @param test
     * @param testcase
     * @return
     */
    AnswerList<TestCaseStepAction> readByTestTestCase(String test, String testcase);

    /**
     *
     * @param test
     * @param testcase
     * @param stepId
     * @return
     */
    AnswerList<TestCaseStepAction> readByVarious1(String test, String testcase, int stepId);

    /**
     *
     * @param testCaseStepAction
     * @return
     */
    Answer create(TestCaseStepAction testCaseStepAction);
}
