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

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import org.cerberus.crud.entity.TestCaseStepActionExecution;
import org.cerberus.util.answer.AnswerItem;
import org.cerberus.util.answer.AnswerList;

/**
 * {Insert class description here}
 *
 * @author Tiago Bernardes
 * @version 1.0, 02/01/2013
 * @since 2.0.0
 */
public interface ITestCaseStepActionExecutionDAO {

    void updateTestCaseStepActionExecution(TestCaseStepActionExecution testCaseStepActionExecution);

    void insertTestCaseStepActionExecution(TestCaseStepActionExecution testCaseStepActionExecution);

    List<List<String>> getListOfSequenceDuration(String idList);

    List<TestCaseStepActionExecution> findTestCaseStepActionExecutionByCriteria(long id, String test, String testCase, int step, int index);

    public AnswerList readByVarious1(long executionId, String test, String testcase, int step, int index);

    public AnswerItem readByKey(long executionId, String test, String testcase, int step, int index, int sequence);

    public TestCaseStepActionExecution loadFromResultset(ResultSet resultSet) throws SQLException;
}
