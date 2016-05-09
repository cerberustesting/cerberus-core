/* DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
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

import org.cerberus.crud.entity.TestCaseExecutionData;
import org.cerberus.exception.CerberusException;

/**
 * {Insert class description here}
 *
 * @author Tiago Bernardes
 * @version 1.0, 02/01/2013
 * @since 2.0.0
 */
public interface ITestCaseExecutionDataDAO {

    TestCaseExecutionData findTestCaseExecutionDataByKey(long id, String property);

    void insertTestCaseExecutionData(TestCaseExecutionData testCaseExecutionData) throws CerberusException;

    void updateTestCaseExecutionData(TestCaseExecutionData testCaseExecutionData) throws CerberusException;

    /**
     *
     * @param propName
     * @param test
     * @param testCase
     * @param build
     * @param environment
     * @param country
     * @return
     */
    List<String> getPastValuesOfProperty(String propName, String test, String testCase, String build, String environment, String country);

    /**
     * We get the list of values currently in used in the given COUNTRY,
     * ENVIRONMENT, PROPERTY only STATUS = PE execution wil be used. Nb of
     * seconds in timeout will be used to remove too old executions.
     *
     * @param propName
     * @param environment
     * @param timeoutInSecond
     * @param country
     * @return
     */
    List<String> getInUseValuesOfProperty(String propName, String environment, String country, Integer timeoutInSecond);

    /**
     *
     * @param id
     * @return List of TestCaseExecutionData that match the Id.
     */
    List<TestCaseExecutionData> findTestCaseExecutionDataById(long id);

}
