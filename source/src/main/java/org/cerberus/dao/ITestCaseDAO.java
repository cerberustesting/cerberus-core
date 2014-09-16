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

package org.cerberus.dao;

import java.util.List;

import org.cerberus.entity.TCase;
import org.cerberus.entity.TestCase;
import org.cerberus.exception.CerberusException;

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
    
    public void updateTestCase(TCase tc) throws CerberusException;
    
    List<TCase> findTestCaseByTestSystems(String test, List<String> systems);
}
