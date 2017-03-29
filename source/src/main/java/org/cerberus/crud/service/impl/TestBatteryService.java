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
import java.util.Map;

import org.cerberus.crud.dao.ITestBatteryContentDAO;
import org.cerberus.crud.dao.ITestBatteryDAO;
import org.cerberus.crud.entity.TestBattery;
import org.cerberus.crud.entity.TestBatteryContent;
import org.cerberus.crud.entity.TestBatteryContentWithDescription;
import org.cerberus.exception.CerberusException;
import org.cerberus.crud.service.ITestBatteryService;
import org.cerberus.util.answer.Answer;
import org.cerberus.util.answer.AnswerItem;
import org.cerberus.util.answer.AnswerList;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
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

    @Override
    public List<TestBattery> findTestBatteriesByTestCase(String test, String testCase) throws CerberusException {
        return testBatteryDAO.findTestBatteriesByTestCase(test, testCase);
    }

    @Override
    public AnswerList readByCriteria(int startPosition, int length, String columnName, String sort, String searchParameter, String individualSearch) {
        return testBatteryDAO.readByCriteria(startPosition, length, columnName, sort, searchParameter, individualSearch);
    }

    @Override
    public AnswerList readByCriteria(int startPosition, int length, String columnName, String sort, String searchParameter, Map<String, List<String>> individualSearch) {
        return testBatteryDAO.readByCriteria(startPosition, length, columnName, sort, searchParameter, individualSearch);
    }

    @Override
    public AnswerItem readByKey(String key) {
        return testBatteryDAO.readByKey(key);
    }

    @Override
    public AnswerList<String> readDistinctValuesByCriteria(String searchParameter, Map<String, List<String>> individualSearch, String columnName) {
        return testBatteryDAO.readDistinctValuesByCriteria(searchParameter, individualSearch, columnName);
    }

    @Override
    public Answer create(TestBattery tb) {
        return testBatteryDAO.create(tb);
    }

    @Override
    public Answer update(TestBattery tb) {
        return testBatteryDAO.update(tb);
    }

    @Override
    public Answer delete(TestBattery tb) {
        return testBatteryDAO.delete(tb);
    }

}
