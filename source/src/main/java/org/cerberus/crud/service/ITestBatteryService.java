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
package org.cerberus.crud.service;

import java.util.List;
import java.util.Map;

import org.cerberus.crud.entity.TestBattery;
import org.cerberus.crud.entity.TestBatteryContent;
import org.cerberus.crud.entity.TestBatteryContentWithDescription;
import org.cerberus.exception.CerberusException;
import org.cerberus.util.answer.Answer;
import org.cerberus.util.answer.AnswerItem;
import org.cerberus.util.answer.AnswerList;

/**
 * @author memiks
 */
public interface ITestBatteryService {


    List<TestBattery> findAll() throws CerberusException;

    TestBattery findTestBatteryByKey(Integer testBatteryID) throws CerberusException;

    TestBatteryContent findTestBatteryContentByKey(Integer testBatteryContentID) throws CerberusException;

    TestBattery findTestBatteryByTestBatteryName(String testBattery) throws CerberusException;

    List<TestBatteryContent> findTestBatteryContentsByTestBatteryName(String testBattery) throws CerberusException;

    List<TestBatteryContentWithDescription> findTestBatteryContentsWithDescriptionByTestBatteryName(String testBattery) throws CerberusException;

    boolean updateTestBattery(TestBattery testBattery);

    boolean createTestBattery(TestBattery testBattery);

    boolean deleteTestBattery(TestBattery testBattery);

    boolean updateTestBatteryContent(TestBatteryContent testBatteryContent);

    boolean createTestBatteryContent(TestBatteryContent testBatteryContent);

    boolean deleteTestBatteryContent(TestBatteryContent testBatteryContent);

    List<TestBattery> findTestBatteryByCriteria(Integer testBatteryID, String testBattery, String Description) throws CerberusException;

    List<TestBatteryContent> findTestBatteryContentsByCriteria(Integer testBatteryContentID, String testBattery, String test, String testCase) throws CerberusException;

    List<TestBattery> findTestBatteriesByTestCase(String test, String testCase) throws CerberusException;

    public AnswerList readByCriteria(int startPosition, int length, String columnName, String sort, String searchParameter, String individualSearch);

    public AnswerList readByCriteria(int startPosition, int length, String columnName, String sort, String searchParameter, Map<String, List<String>> individualSearch);

    AnswerItem readByKey(String key);

    /**
     * @param searchParameter
     * @param individualSearch
     * @param columnName
     * @return
     */
    public AnswerList<String> readDistinctValuesByCriteria(String searchParameter, Map<String, List<String>> individualSearch, String columnName);

    Answer create(TestBattery tb);

    Answer update(TestBattery tb);

    Answer delete(TestBattery tb);
}
