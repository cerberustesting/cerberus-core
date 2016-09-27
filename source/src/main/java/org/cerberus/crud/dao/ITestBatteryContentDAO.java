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
package org.cerberus.crud.dao;

import org.cerberus.crud.entity.TestBatteryContent;
import java.util.List;
import java.util.Map;

import org.cerberus.crud.entity.TestBatteryContentWithDescription;
import org.cerberus.exception.CerberusException;
import org.cerberus.util.answer.AnswerList;

/**
 *
 * @author memiks
@Entity
@Table(catalog = "cerberus", schema = "", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"testbattery", "Test", "TestCase"})})
@XmlRootElement
@NamedQueries({
    @NamedQuery(name = "TestBatteryContent.findAll", query = "SELECT t FROM testbatterycontent t"),
    @NamedQuery(name = "TestBatteryContent.findByTestbatterycontentID", query = "SELECT t FROM testbatterycontent t WHERE t.testbatterycontentID = :testbatterycontentID")})
 */
public interface ITestBatteryContentDAO {

    List<TestBatteryContent> findAll() throws CerberusException;

    TestBatteryContent findTestBatteryContentByKey(Integer testBatteryContentID) throws CerberusException;

    List<TestBatteryContent> findTestBatteryContentsByTestBatteryName(String testBattery) throws CerberusException;

    List<TestBatteryContentWithDescription> findTestBatteryContentsWithDescriptionByTestBatteryName(String testBattery) throws CerberusException;

    public AnswerList readByCampaignByCriteria(String campaign, int start, int amount, String columnName, String sortInformation, String searchTerm, Map<String, List<String>> individualSearch);

    /**
     *
     * @param searchParameter
     * @param individualSearch
     * @param columnName
     * @return
     */
    public AnswerList<String> readDistinctValuesByCampaignByCriteria(String campaign, String searchParameter, Map<String, List<String>> individualSearch, String columnName);

    boolean updateTestBatteryContent(TestBatteryContent testBatteryContent);

    boolean createTestBatteryContent(TestBatteryContent testBatteryContent);

    boolean deleteTestBatteryContent(TestBatteryContent testBatteryContent);

    List<TestBatteryContent> findTestBatteryContentsByCriteria(Integer testBatteryContentID, String testBattery, String test, String testCase) throws CerberusException;

    AnswerList readByTestBatteryByCriteria(String testBattery, int startPosition, int length, String columnName, String sort, String searchParameter, String string);
    
}
