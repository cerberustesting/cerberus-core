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
package org.cerberus.crud.service.impl;

import java.util.ArrayList;
import java.util.List;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import org.cerberus.crud.dao.ITestCaseStepExecutionDAO;
import org.cerberus.crud.entity.TestCaseExecutionFile;
import org.cerberus.crud.entity.TestCaseStepActionExecution;
import org.cerberus.crud.entity.TestCaseStepExecution;
import org.cerberus.crud.service.ITestCaseExecutionFileService;
import org.cerberus.crud.service.ITestCaseStepActionExecutionService;
import org.cerberus.crud.service.ITestCaseStepExecutionService;
import org.cerberus.crud.service.ITestCaseStepService;
import org.cerberus.util.answer.AnswerList;
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
    ITestCaseStepService testCaseStepService;
    @Autowired
    ITestCaseStepActionExecutionService testCaseStepActionExecutionService;
    @Autowired
    ITestCaseExecutionFileService testCaseExecutionFileService;

    private static final Logger LOG = LogManager.getLogger(TestCaseStepExecutionService.class);
    
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
    public AnswerList<TestCaseStepExecution> readByVarious1(long executionId, String test, String testcase) {
        return testCaseStepExecutionDao.readByVarious1(executionId, test, testcase);
    }

    @Override
    public AnswerList<TestCaseStepExecution> readByVarious1WithDependency(long executionId, String test, String testcase) {
        AnswerList<TestCaseStepExecution> steps = this.readByVarious1(executionId, test, testcase);
        AnswerList<TestCaseStepExecution> response = null;
        List<TestCaseStepExecution> tcseList = new ArrayList<>();
        for (Object step : steps.getDataList()) {
            TestCaseStepExecution tces = (TestCaseStepExecution) step;

            AnswerList<TestCaseStepActionExecution> actions = testCaseStepActionExecutionService.readByVarious1WithDependency(executionId, tces.getTest(), tces.getTestCase(), tces.getStep(), tces.getIndex());
            tces.setTestCaseStepActionExecutionList((List<TestCaseStepActionExecution>) actions.getDataList());

            AnswerList<TestCaseExecutionFile> files = testCaseExecutionFileService.readByVarious(executionId, tces.getTest() + "-" + tces.getTestCase() + "-" + tces.getStep() + "-" + tces.getIndex());
            tces.setFileList((List<TestCaseExecutionFile>) files.getDataList());

            tcseList.add(tces);
        }
        response = new AnswerList<>(tcseList, steps.getTotalRows());
        return response;
    }
}
