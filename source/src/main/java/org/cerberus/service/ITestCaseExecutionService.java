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
package org.cerberus.service;

import java.util.List;

import org.cerberus.entity.TestCaseExecution;
import org.cerberus.exception.CerberusException;

/**
 * @author bcivel
 */
public interface ITestCaseExecutionService {

    long insertTCExecution(TestCaseExecution tCExecution) throws CerberusException;

    void updateTCExecution(TestCaseExecution tCExecution) throws CerberusException;

    TestCaseExecution findLastTCExecutionByCriteria(String test, String testCase, String environment, String country,
                                              String build, String revision) throws CerberusException;

    TestCaseExecution findLastTCExecutionByCriteria(String test, String testCase, String environment, String country,
                                                    String build, String revision, String browser, String browserVersion,
                                                    String ip, String port, String tag);

    /**
     * @param dateLimitFrom The limit start date of the executions from which
     *                      the selection is done. Mandatory parameter.
     * @param test          filter on the test
     * @param testCase      filter on the testCase
     * @param application   filter on the application.
     * @param country       filter on the country
     * @param environment   filter on the environment
     * @param controlStatus filter on the control status (RC of the execution)
     * @param status        filter on the status (Status of the testCase when execution
     *                      was made)
     * @return a list of the testcaseExecution done after dateLimitFrom
     *         parameter and that match the other criteria.
     * @throws CerberusException when no Execution match the criteria.
     */
    List<TestCaseExecution> findTCExecutionbyCriteria1(String dateLimitFrom, String test, String testCase, String application, String country, String environment, String controlStatus, String status) throws CerberusException;

    long registerRunID(TestCaseExecution tCExecution) throws CerberusException;
}
