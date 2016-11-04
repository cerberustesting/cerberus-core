/*
 * Cerberus  Copyright (C) 2013  vertigo17
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
package org.cerberus.crud.service.impl;

import java.util.List;

import org.cerberus.crud.dao.ITestCaseStepActionControlExecutionDAO;
import org.cerberus.crud.entity.TestCaseStepActionControlExecution;
import org.cerberus.crud.service.ITestCaseStepActionControlExecutionService;
import org.cerberus.websocket.TestCaseExecutionEndPoint;
import org.cerberus.util.answer.AnswerList;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 *
 * @author bcivel
 */
@Service
public class TestCaseStepActionControlExecutionService implements ITestCaseStepActionControlExecutionService{

    @Autowired
    private ITestCaseStepActionControlExecutionDAO testCaseStepActionControlExecutionDao;
    @Autowired
    TestCaseExecutionService testCaseExecutionService;
    
    @Override
    public void insertTestCaseStepActionControlExecution(TestCaseStepActionControlExecution testCaseStepActionControlExecution) {
        testCaseStepActionControlExecutionDao.insertTestCaseStepActionControlExecution(testCaseStepActionControlExecution);
    }
    
    @Override
    public void updateTestCaseStepActionControlExecution(TestCaseStepActionControlExecution testCaseStepActionControlExecution) {
        testCaseStepActionControlExecutionDao.updateTestCaseStepActionControlExecution(testCaseStepActionControlExecution);
    }
    
    @Override
    public List<TestCaseStepActionControlExecution> findTestCaseStepActionControlExecutionByCriteria(long id, String test, String testCase, int step, int sequence) {
        return testCaseStepActionControlExecutionDao.findTestCaseStepActionControlExecutionByCriteria( id,  test,  testCase,  step,  sequence);
    }

    @Override
    public AnswerList readByVarious1(long executionId, String test, String testcase, int step, int sequence) {
        return testCaseStepActionControlExecutionDao.readByVarious1(executionId, test, testcase, step, sequence);
    }
}
