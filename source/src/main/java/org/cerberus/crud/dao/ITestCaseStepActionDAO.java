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

import org.cerberus.crud.entity.TestCaseStepAction;
import org.cerberus.exception.CerberusException;
import org.cerberus.util.answer.Answer;
import org.cerberus.util.answer.AnswerList;

import java.util.List;

/**
 * {Insert class description here}
 *
 * @author Tiago Bernardes
 * @version 1.0, 31/Dez/2012
 * @since 2.0.0
 */
public interface ITestCaseStepActionDAO {

    TestCaseStepAction readByKey(String test, String testcase, int stepId, int actionId);

    List<TestCaseStepAction> findActionByTestTestCaseStep(String test, String testcase, int stepId);

    void createTestCaseStepAction(TestCaseStepAction testCaseStepAction) throws CerberusException;

    boolean changeTestCaseStepActionActionId(String test, String testcase, int stepid, int oldActionId, int newActionId);

    void update(TestCaseStepAction tcsa) throws CerberusException;

    void updateService(String oldService, String service) throws CerberusException;

    void updateApplicationObject(String field, String application, String oldObject, String newObject) throws CerberusException;
            
    void delete(TestCaseStepAction tcsa) throws CerberusException;

    AnswerList<TestCaseStepAction> readByTestTestCase(String test, String testcase);

    AnswerList<TestCaseStepAction> readByVarious1(String test, String testcase, int stepId);

    Answer create(TestCaseStepAction testCaseStepAction);
}
