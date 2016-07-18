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

import org.cerberus.crud.dao.ITestCaseExecutionDataDAO;
import org.cerberus.crud.entity.TestCaseExecutionData;
import org.cerberus.exception.CerberusException;
import org.cerberus.crud.service.ITestCaseExecutionDataService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 *
 * @author bcivel
 */
@Service
public class TestCaseExecutionDataService implements ITestCaseExecutionDataService {

    @Autowired
    ITestCaseExecutionDataDAO testCaseExecutionDataDao;

    @Override
    public TestCaseExecutionData findTestCaseExecutionDataByKey(long id, String property) {
        return testCaseExecutionDataDao.findTestCaseExecutionDataByKey(id, property);
    }

    @Override
    public void insertTestCaseExecutionData(TestCaseExecutionData testCaseExecutionData) throws CerberusException {
        testCaseExecutionDataDao.insertTestCaseExecutionData(testCaseExecutionData);
    }

    @Override
    public void updateTestCaseExecutionData(TestCaseExecutionData testCaseExecutionData) throws CerberusException {
        testCaseExecutionDataDao.updateTestCaseExecutionData(testCaseExecutionData);
    }

    @Override
    public List<TestCaseExecutionData> findTestCaseExecutionDataById(long id) {
        return testCaseExecutionDataDao.findTestCaseExecutionDataById(id);
    }

    @Override
    public List<TestCaseExecutionData> findTestCaseExecutionDataByCriteria1(String property, String value, String controlStatus, Integer nbMinutes) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void insertOrUpdateTestCaseExecutionData(TestCaseExecutionData testCaseExecutionData) throws CerberusException {
        if (findTestCaseExecutionDataByKey(testCaseExecutionData.getId(), testCaseExecutionData.getProperty()) != null) {
            updateTestCaseExecutionData(testCaseExecutionData);
        } else {
            insertTestCaseExecutionData(testCaseExecutionData);
        }

    }

}
