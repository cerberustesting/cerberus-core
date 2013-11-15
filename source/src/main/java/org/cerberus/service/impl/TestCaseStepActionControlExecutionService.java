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
package org.cerberus.service.impl;

import org.cerberus.dao.ITestCaseStepActionControlExecutionDAO;
import org.cerberus.entity.TestCaseStepActionControlExecution;
import org.cerberus.service.ITestCaseStepActionControlExecutionService;
import java.util.List;
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
}
