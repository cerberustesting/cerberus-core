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

import org.cerberus.crud.dao.ITestCaseStepActionControlExecutionDAO;
import org.cerberus.crud.entity.TestCaseExecutionFile;
import org.cerberus.crud.entity.TestCaseStepActionControlExecution;
import org.cerberus.crud.service.ITestCaseExecutionFileService;
import org.cerberus.crud.service.ITestCaseStepActionControlExecutionService;
import org.cerberus.util.answer.AnswerItem;
import org.cerberus.util.answer.AnswerList;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 *
 * @author bcivel
 */
@Service
public class TestCaseStepActionControlExecutionService implements ITestCaseStepActionControlExecutionService {

    @Autowired
    private ITestCaseStepActionControlExecutionDAO testCaseStepActionControlExecutionDao;
    @Autowired
    ITestCaseExecutionFileService testCaseExecutionFileService;

    private static final Logger LOG = LogManager.getLogger(TestCaseStepActionControlExecutionService.class);

    @Override
    public void insertTestCaseStepActionControlExecution(TestCaseStepActionControlExecution testCaseStepActionControlExecution) {
        testCaseStepActionControlExecutionDao.insertTestCaseStepActionControlExecution(testCaseStepActionControlExecution);
    }

    @Override
    public void updateTestCaseStepActionControlExecution(TestCaseStepActionControlExecution testCaseStepActionControlExecution) {
        testCaseStepActionControlExecutionDao.updateTestCaseStepActionControlExecution(testCaseStepActionControlExecution);
    }

    @Override
    public List<TestCaseStepActionControlExecution> findTestCaseStepActionControlExecutionByCriteria(long id, String test, String testCase, int step, int index, int sequence) {
        return testCaseStepActionControlExecutionDao.findTestCaseStepActionControlExecutionByCriteria(id, test, testCase, step, index, sequence);
    }

    @Override
    public AnswerList<TestCaseStepActionControlExecution> readByVarious1(long executionId, String test, String testcase, int step, int index, int sequence) {
        return testCaseStepActionControlExecutionDao.readByVarious1(executionId, test, testcase, step, index, sequence);
    }

    @Override
    public AnswerItem<TestCaseStepActionControlExecution> readByKey(long executionId, String test, String testcase, int step, int index, int sequence, int controlSequence) {
        return testCaseStepActionControlExecutionDao.readByKey(executionId, test, testcase, step, index, sequence, controlSequence);
    }

    @Override
    public AnswerList<TestCaseStepActionControlExecution> readByVarious1WithDependency(long executionId, String test, String testcase, int step, int index, int sequence) {

        AnswerList<TestCaseStepActionControlExecution> controls = this.readByVarious1(executionId, test, testcase, step, index, sequence);
        AnswerList<TestCaseStepActionControlExecution> response = null;
        List<TestCaseStepActionControlExecution> tcsaceList = new ArrayList<>();
        for (Object control : controls.getDataList()) {

            TestCaseStepActionControlExecution tcsace = (TestCaseStepActionControlExecution) control;

            AnswerList<TestCaseExecutionFile> files = testCaseExecutionFileService.readByVarious(executionId, tcsace.getTest() + "-" + tcsace.getTestCase() + "-" + tcsace.getStep() + "-" + tcsace.getIndex() + "-" + tcsace.getSequence() + "-" + tcsace.getControlSequence());
            tcsace.setFileList((List<TestCaseExecutionFile>) files.getDataList());

            tcsaceList.add(tcsace);
        }
        response = new AnswerList<>(tcsaceList, controls.getTotalRows());
        return response;

    }

}
