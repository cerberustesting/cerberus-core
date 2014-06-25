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
package org.cerberus.dao;

import java.util.List;

import org.cerberus.entity.TestCaseExecution;
import org.cerberus.exception.CerberusException;

/**
 * {Insert class description here}
 *
 * @author Tiago Bernardes
 * @version 1.0, 02/01/2013
 * @since 2.0.0
 */
public interface ITestCaseExecutionDAO {

    /**
     * @param tCExecution TestCaseExecution Object to insert in TestcaseExecution table
     * @throws org.cerberus.exception.CerberusException
     * @return execution id (long)
     */
    long insertTCExecution(TestCaseExecution tCExecution) throws CerberusException;

    void updateTCExecution(TestCaseExecution tCExecution) throws CerberusException;

    List<String> getIDListOfLastExecutions(String test, String testcase, String country);

    /**
     *
     * @param test Test Criteria 
     * @param testcase TestCase Criteria
     * @param environment Environment Criteria
     * @param country Country Criteria
     * @param build
     * @param revision
     * @return TestCaseExecution Object created only with attributes from database
     */
    TestCaseExecution findLastTCExecutionByCriteria(String test, String testcase, String environment, String country,
                                              String build, String revision) throws CerberusException;

    TestCaseExecution findLastTCExecutionByCriteria(String test, String testCase, String environment, String country,
                                                    String build, String revision, String browser, String browserVersion,
                                                    String ip, String port, String tag);

    /**
     * @param dateLimitFrom The limit start date of the executions from which the selection is done. Mandatory parameter.
     * @param test          filter on the test
     * @param testCase      filter on the testCase
     * @param application   filter on the application.
     * @param country       filter on the country
     * @param environment   filter on the environment
     * @param controlStatus filter on the control status (RC of the execution)
     * @param status        filter on the status (Status of the testCase when execution was made)
     * @return a list of testCaseExecution done after the dateLimitFrom parameter and following the other criteria.
     * @throws CerberusException when no executions can be found.
     */
    List<TestCaseExecution> findExecutionbyCriteria1(String dateLimitFrom, String test, String testCase, String application, String country, String environment, String controlStatus, String status) throws CerberusException;
}
