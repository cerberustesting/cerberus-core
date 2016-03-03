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

import java.util.ArrayList;
import java.util.List;

import org.cerberus.crud.dao.ITestCaseStepExecutionDAO;
import org.cerberus.crud.entity.TestCaseStepExecution;
import org.cerberus.crud.service.ITestCaseStepActionExecutionService;
import org.cerberus.crud.service.ITestCaseStepExecutionService;
import org.cerberus.util.answer.AnswerList;
import org.openqa.selenium.remote.Response;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 *
 * @author bcivel
 */
@Service
public class TestCaseStepExecutionService implements ITestCaseStepExecutionService {

    @Autowired
    ITestCaseStepExecutionDAO testCaseStepExecutionDao;
    @Autowired
    ITestCaseStepActionExecutionService testCaseStepActionExecutionService;

    @Override
    public void insertTestCaseStepExecution(TestCaseStepExecution testCaseStepExecution) {
        this.testCaseStepExecutionDao.insertTestCaseStepExecution(testCaseStepExecution);
    }

    @Override
    public void updateTestCaseStepExecution(TestCaseStepExecution testCaseStepExecution) {
        this.testCaseStepExecutionDao.updateTestCaseStepExecution(testCaseStepExecution);
    }

    @Override
    public List<TestCaseStepExecution> findTestCaseStepExecutionById(long id) {
        return testCaseStepExecutionDao.findTestCaseStepExecutionById(id);
    }

    @Override
    public AnswerList readByVarious1(long executionId, String test, String testcase) {
        return testCaseStepExecutionDao.readByVarious1(executionId, test, testcase);
    }

    @Override
    public AnswerList readByVarious1WithDependency(long executionId, String test, String testcase) {
        AnswerList steps = this.readByVarious1(executionId, test, testcase);
        AnswerList response = null;
        List<TestCaseStepExecution> tcseList = new ArrayList();
        for (Object step : steps.getDataList()) {
            TestCaseStepExecution tces = (TestCaseStepExecution) step;
            AnswerList actions = testCaseStepActionExecutionService.readByVarious1WithDependency(executionId, test, testcase, tces.getStep());
            tces.setTestCaseStepActionExecution(actions);
            tcseList.add(tces);
        }
        response = new AnswerList(tcseList, steps.getTotalRows());
        return response;
    }
}
