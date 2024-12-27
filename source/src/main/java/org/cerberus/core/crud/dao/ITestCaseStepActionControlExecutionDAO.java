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

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;

import org.cerberus.core.crud.entity.TestCaseStepActionControlExecution;
import org.cerberus.core.util.answer.AnswerItem;
import org.cerberus.core.util.answer.AnswerList;

/**
 * {Insert class description here}
 *
 * @author Tiago Bernardes
 * @version 1.0, 02/01/2013
 * @since 2.0.0
 */
public interface ITestCaseStepActionControlExecutionDAO {

    /**
     *
     * @param testCaseStepActionControlExecution
     * @param secrets
     */
    void insertTestCaseStepActionControlExecution(TestCaseStepActionControlExecution testCaseStepActionControlExecution, HashMap<String, String> secrets);

    /**
     *
     * @param testCaseStepActionControlExecution
     * @param secrets
     */
    void updateTestCaseStepActionControlExecution(TestCaseStepActionControlExecution testCaseStepActionControlExecution, HashMap<String, String> secrets);

    /**
     *
     * @param id
     * @param test
     * @param testCase
     * @param stepId
     * @param index
     * @param sequence
     * @return
     */
    List<TestCaseStepActionControlExecution> findTestCaseStepActionControlExecutionByCriteria(long id, String test, String testCase, int stepId, int index, int sequence);

    /**
     *
     * @param executionId
     * @param test
     * @param testCase
     * @param stepId
     * @param index
     * @param sequence
     * @return
     */
    public AnswerList<TestCaseStepActionControlExecution> readByVarious1(long executionId, String test, String testCase, int stepId, int index, int sequence);

    /**
     *
     * @param executionId
     * @param test
     * @param testCase
     * @param stepId
     * @param index
     * @param sequence
     * @param controlSequence
     * @return
     */
    public AnswerItem<TestCaseStepActionControlExecution> readByKey(long executionId, String test, String testCase, int stepId, int index, int sequence, int controlSequence);

    /**
     *
     * @param resultSet
     * @return
     * @throws SQLException
     */
    public TestCaseStepActionControlExecution loadFromResultset(ResultSet resultSet) throws SQLException; 
}
