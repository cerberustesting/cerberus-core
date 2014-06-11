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
import org.cerberus.dao.ITestBatteryContentDAO;
import org.cerberus.dao.ITestBatteryDAO;
import org.cerberus.entity.TestBattery;
import org.cerberus.entity.TestBatteryContent;
import org.cerberus.entity.TestBatteryContentWithDescription;
import org.cerberus.exception.CerberusException;
import org.cerberus.service.ITestBatteryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 *
 * @author memiks
 */
@Service
public class TestBatteryService implements ITestBatteryService {

    @Autowired
    private ITestBatteryDAO testBatteryDAO;

    @Autowired
    private ITestBatteryContentDAO testBatteryContentDAO;

    @Override
    public List<TestBattery> findAll() throws CerberusException {
        return testBatteryDAO.findAll();
    }

    @Override
    public TestBattery findTestBatteryByKey(Integer testBatteryID) throws CerberusException {
        return testBatteryDAO.findTestBatteryByKey(testBatteryID);
    }

    @Override
    public TestBattery findTestBatteryByTestBatteryName(String testBattery) throws CerberusException {
        return testBatteryDAO.findTestBatteryByTestBatteryName(testBattery);
    }

    @Override
    public List<TestBatteryContent> findTestBatteryContentsByTestBatteryName(String testBattery) throws CerberusException {
        return testBatteryContentDAO.findTestBatteryContentsByTestBatteryName(testBattery);
    }

    @Override
    public List<TestBatteryContentWithDescription> findTestBatteryContentsWithDescriptionByTestBatteryName(String testBattery) throws CerberusException {
        return testBatteryContentDAO.findTestBatteryContentsWithDescriptionByTestBatteryName(testBattery);
    }

    @Override
    public boolean updateTestBattery(TestBattery testBattery) {
        return testBatteryDAO.updateTestBattery(testBattery);
    }

    @Override
    public boolean createTestBattery(TestBattery testBattery) {
        return testBatteryDAO.createTestBattery(testBattery);
    }

    @Override
    public boolean updateTestBatteryContent(TestBatteryContent testBatteryContent) {
        return testBatteryContentDAO.updateTestBatteryContent(testBatteryContent);
    }

    @Override
    public boolean createTestBatteryContent(TestBatteryContent testBatteryContent) {
        return testBatteryContentDAO.createTestBatteryContent(testBatteryContent);
    }

    @Override
    public List<TestBattery> findTestBatteryByCriteria(Integer testBatteryID, String testBattery, String Description) throws CerberusException {
        return testBatteryDAO.findTestBatteryByCriteria(testBatteryID, testBattery, Description);
    }

    @Override
    public List<TestBatteryContent> findTestBatteryContentsByCriteria(Integer testBatteryContentID, String testBattery, String test, String testCase) throws CerberusException {
        return testBatteryContentDAO.findTestBatteryContentsByCriteria(testBatteryContentID, testBattery, test, testCase);
    }

    @Override
    public TestBatteryContent findTestBatteryContentByKey(Integer testBatteryContentID) throws CerberusException {
        return testBatteryContentDAO.findTestBatteryContentByKey(testBatteryContentID);
    }

    @Override
    public boolean deleteTestBattery(TestBattery testBattery) {
        return testBatteryDAO.deleteTestBattery(testBattery);
    }

    @Override
    public boolean deleteTestBatteryContent(TestBatteryContent testBatteryContent) {
        return testBatteryContentDAO.deleteTestBatteryContent(testBatteryContent);
    }

}
