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

import java.util.HashMap;
import java.util.List;

import org.cerberus.core.crud.entity.TestCaseStepActionExecution;
import org.cerberus.core.util.answer.AnswerItem;
import org.cerberus.core.util.answer.AnswerList;
import org.json.JSONArray;

/**
 *
 * @author bcivel
 */
public interface ITestCaseStepActionExecutionService {

    /**
     *
     * @param testCaseStepActionExecution
     * @param secrets
     */
    void insertTestCaseStepActionExecution(TestCaseStepActionExecution testCaseStepActionExecution, HashMap<String, String> secrets);

    /**
     *
     * @param testCaseStepActionExecution
     * @param secrets
     */
    void updateTestCaseStepActionExecution(TestCaseStepActionExecution testCaseStepActionExecution, HashMap<String, String> secrets);

    /**
     *
     * @param id
     * @param test
     * @param testCase
     * @param stepId
     * @param index
     * @return List of testCaseStepExecution that correspond to the Id.
     */
    List<TestCaseStepActionExecution> findTestCaseStepActionExecutionByCriteria(long id, String test, String testCase, int stepId, int index);
    
    
    /**
     * 
     * @param test
     * @param testcase
     * @param country
     * @return 
     */
    JSONArray lastActionExecutionDuration(String test, String testcase, String country);

    /**
     *
     * @param executionId
     * @param test
     * @param testcase
     * @param stepId
     * @param index
     * @return
     */
    public AnswerList<TestCaseStepActionExecution> readByVarious1(long executionId, String test, String testcase, int stepId, int index);

    /**
     *
     * @param executionId
     * @param test
     * @param testcase
     * @param stepId
     * @param index
     * @param sequence
     * @return
     */
    public AnswerItem<TestCaseStepActionExecution> readByKey(long executionId, String test, String testcase, int stepId, int index, int sequence);

    /**
     *
     * @param executionId
     * @param test
     * @param testcase
     * @param stepId
     * @param index
     * @return
     */
    public AnswerList<TestCaseStepActionExecution> readByVarious1WithDependency(long executionId, String test, String testcase, int stepId, int index);
}
