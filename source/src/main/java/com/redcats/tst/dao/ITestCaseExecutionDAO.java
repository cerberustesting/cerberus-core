package com.redcats.tst.dao;

import com.redcats.tst.entity.TCExecution;
import com.redcats.tst.exception.CerberusException;

import java.util.List;

/**
 * {Insert class description here}
 *
 * @author Tiago Bernardes
 * @version 1.0, 02/01/2013
 * @since 2.0.0
 */
public interface ITestCaseExecutionDAO {

    /**
     * @param tCExecution TCExecution Object to insert in TestcaseExecution table
     */
    void insertTCExecution(TCExecution tCExecution) throws CerberusException;

    void updateTCExecution(TCExecution tCExecution) throws CerberusException;

    List<String> getIDListOfLastExecutions(String test, String testcase, String country);

    /**
     *
     * @param test Test Criteria 
     * @param testcase TestCase Criteria
     * @param environment Environment Criteria
     * @param country Country Criteria
     * @return TCExecution Object created only with attributes from database
     */
//    TCExecution findLastTCExecutionByCriteria(String test, String testcase, String environment, String country) throws CerberusException;

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
    List<TCExecution> findExecutionbyCriteria1(String dateLimitFrom, String test, String testCase, String application, String country, String environment, String controlStatus, String status) throws CerberusException;
}
