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

import org.cerberus.crud.dao.ITestCaseStepExecutionDAO;
import org.cerberus.crud.entity.TestCaseStepExecution;
import org.cerberus.crud.service.ITestCaseStepExecutionService;
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
}
