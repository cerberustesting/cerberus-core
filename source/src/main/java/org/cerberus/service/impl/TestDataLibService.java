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
import org.cerberus.dao.ITestDataLibDAO;
import org.cerberus.entity.TestDataLib;
import org.cerberus.exception.CerberusException;
import org.cerberus.service.ITestDataLibService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


@Service
public class TestDataLibService implements ITestDataLibService {

    @Autowired
    ITestDataLibDAO testDataLibDAO;

    @Override
    public void createTestDataLib(TestDataLib testDataLib) throws CerberusException {
        testDataLibDAO.createTestDataLib(testDataLib);
    }

    @Override
    public void updateTestDataLib(TestDataLib testDataLib) throws CerberusException {
        testDataLibDAO.updateTestDataLib(testDataLib);
    }

    @Override
    public void deleteTestDataLib(TestDataLib testDataLib) throws CerberusException {
        testDataLibDAO.deleteTestDataLib(testDataLib);
    }

    @Override
    public List<TestDataLib> findAllTestDataLib() {
        return testDataLibDAO.findAllTestDataLib();
    }

    @Override
    public List<TestDataLib> findTestDataLibListByCriteria(int start, int amount, String column, String dir, String searchTerm, String individualSearch) {
        return testDataLibDAO.findTestDataLibListByCriteria(start, amount, column, dir, searchTerm, individualSearch);
    }

    @Override
    public TestDataLib findTestDataLibByKey(String name, String system, String environment, String country) throws CerberusException {
        return testDataLibDAO.findTestDataLibByKey(name, system, environment, country);
    }

    @Override
    public Integer getNumberOfTestDataLibPerCriteria(String searchTerm, String inds) {
        return testDataLibDAO.getNumberOfTestDataLibPerCriteria(searchTerm, inds);
    }
    
}
