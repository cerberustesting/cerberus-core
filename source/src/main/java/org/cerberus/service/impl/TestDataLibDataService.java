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

import java.util.List;
import org.cerberus.dao.ITestDataLibDataDAO;
import org.cerberus.entity.TestDataLibData;
import org.cerberus.exception.CerberusException;
import org.cerberus.service.ITestDataLibDataService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class TestDataLibDataService implements ITestDataLibDataService {

    @Autowired
    ITestDataLibDataDAO testDataLibDataDAO;

    @Override
    public void createTestDataLibData(TestDataLibData testDataLibData) throws CerberusException {
        testDataLibDataDAO.createTestDataLibData(testDataLibData);
    }

    @Override
    public void updateTestDataLibData(TestDataLibData testDataLibData) throws CerberusException {
        testDataLibDataDAO.updateTestDataLibData(testDataLibData);
    }

    @Override
    public void deleteTestDataLibData(TestDataLibData testDataLibData) throws CerberusException {
        testDataLibDataDAO.deleteTestDataLibData(testDataLibData);
    }

    @Override
    public TestDataLibData findTestDataLibDataByKey(Integer testDataLibID, String subData) throws CerberusException {
        return testDataLibDataDAO.findTestDataLibDataByKey(testDataLibID, subData);
    }

    @Override
    public List<TestDataLibData> findAllTestDataLibData() {
        return testDataLibDataDAO.findAllTestDataLibData();
    }

    @Override
    public List<TestDataLibData> findTestDataLibDataListByTestDataLib(Integer testDataLibID) {
        return testDataLibDataDAO.findTestDataLibDataListByTestDataLib(testDataLibID);
    }

    @Override
    public List<TestDataLibData> findTestDataLibDataByCriteria(Integer testDataLibID, String subData, String value, String column, String parsingAnswer, String description) throws CerberusException {
        return testDataLibDataDAO.findTestDataLibDataByCriteria(testDataLibID, subData, value, column, parsingAnswer, description);
    }

}
