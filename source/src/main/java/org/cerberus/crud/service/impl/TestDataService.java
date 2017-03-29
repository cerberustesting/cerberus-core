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

import java.util.List;
import org.cerberus.crud.dao.ITestDataDAO;
import org.cerberus.crud.entity.TestData;
import org.cerberus.exception.CerberusException;
import org.cerberus.crud.service.ITestDataService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 *
 * @author bcivel
 */
@Service
public class TestDataService implements ITestDataService {

    @Autowired
    ITestDataDAO TestDataDAO;

    @Override
    public void createTestData(TestData testData) throws CerberusException {
        TestDataDAO.createTestData(testData);
    }

    @Override
    public void updateTestData(TestData testData) throws CerberusException {
        TestDataDAO.updateTestData(testData);
    }

    @Override
    public void deleteTestData(TestData testData) throws CerberusException {
        TestDataDAO.deleteTestData(testData);
    }

    @Override
    public List<TestData> findAllTestData() {
        return TestDataDAO.findAllTestData();
    }

    @Override
    public List<TestData> findTestDataListByCriteria(int start, int amount, String column, String dir, String searchTerm, String individualSearch) {
        return TestDataDAO.findTestDataListByCriteria(start, amount, column, dir, searchTerm, individualSearch);
    }

    @Override
    public TestData findTestDataByKey(String key, String application, String environment, String country) throws CerberusException {
        return TestDataDAO.findTestDataByKey(key, application, environment, country);
    }

    @Override
    public Integer getNumberOfTestDataPerCriteria(String searchTerm, String inds) {
        return TestDataDAO.getNumberOfTestDataPerCriteria(searchTerm, inds);
    }
}
