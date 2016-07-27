/*
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

import java.util.List;
import java.util.Map;
import org.cerberus.crud.entity.TCase;
import org.cerberus.crud.entity.TestCase;
import org.cerberus.exception.CerberusException;
import org.cerberus.util.answer.Answer;
import org.cerberus.util.answer.AnswerItem;
import org.cerberus.util.answer.AnswerList;

/**
 * {Insert class description here}
 *
 * @author Tiago Bernardes
 * @version 1.0, 18/Dez/2012
 * @since 0.9.0
 */
public interface ITestCaseDAO {

    List<TCase> findTestCaseByTest(String test);

    TCase findTestCaseByKey(String test, String testCase) throws CerberusException;

    boolean updateTestCaseInformation(TestCase testCase);

    boolean updateTestCaseInformationCountries(TestCase tc);

    boolean createTestCase(TCase testCase);

    List<TCase> findTestCaseByCriteria(String test, String application, String country, String active);

    /**
     * @param testCase
     * @param text
     * @param system
     * @return
     * @since 0.9.1
     */
    List<TCase> findTestCaseByCriteria(TCase testCase, String text, String system);

    List<String> findUniqueDataOfColumn(String column);

    /**
     *
     * @param testCase
     * @return true if delete is OK
     */
    boolean deleteTestCase(TCase testCase);

    /**
     *
     * @param tc
     * @param columnName Name of the column to update
     * @param value New value of the field columnName for the key name
     */
    void updateTestCaseField(TCase tc, String columnName, String value);

    /**
     *
     * @param tCase
     * @param system
     * @return
     * @since 1.0.2
     */
    List<TCase> findTestCaseByGroupInCriteria(TCase tCase, String system);

    /**
     *
     * @param campaign the campaign name
     * @return the list of TCase used in the campaign
     * @since 1.0.2
     */
    List<TCase> findTestCaseByCampaignName(String campaign);

    /**
     *
     * @param campaign the campaign name
     * @param countries arrays of country
     * @return the list of TCase used in the campaign
     * @since 1.0.2
     */
    List<TCase> findTestCaseByCampaignNameAndCountries(String campaign, String[] countries);

    public void updateTestCase(TCase tc) throws CerberusException;

    List<TCase> findTestCaseByTestSystems(String test, List<String> systems);

    String getMaxNumberTestCase(String test);

    public List<TCase> findTestCaseByTestSystem(String test, String system);

    List<TCase> findTestCaseByCriteria(String testClause, String projectClause, String appClause, String activeClause, String priorityClause, String statusClause, String groupClause, String targetBuildClause, String targetRevClause, String creatorClause, String implementerClause, String functionClause, String campaignClause, String batteryClause);

    public String findSystemOfTestCase(String test, String testcase) throws CerberusException;

    AnswerList readTestCaseByStepsInLibrary(String test);

    public AnswerList readByTestByCriteria(String system, String test, int start, int amount, String column, String dir, String searchTerm, Map<String, List<String>> individualSearch);

    public AnswerList readByVariousCriteria(String[] test, String[] idProject, String[] app, String[] creator, String[] implementer, String[] system,
                                            String[] testBattery, String[] campaign, String[] priority, String[] group, String[] status);

    public AnswerItem readByKey(String test, String testCase);
    
    public AnswerList<List<String>> readDistinctValuesByCriteria(String system, String test, String searchParameter, Map<String, List<String>> individualSearch, String columnName);
    
    public Answer update(TCase testCase);

    public Answer create(TCase testCase);

    public Answer delete(TCase testCase);
}
