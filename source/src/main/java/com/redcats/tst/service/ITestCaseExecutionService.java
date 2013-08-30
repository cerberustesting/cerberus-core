/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.redcats.tst.service;

import com.redcats.tst.entity.TCExecution;
import com.redcats.tst.exception.CerberusException;

import java.util.List;

/**
 * @author bcivel
 */
public interface ITestCaseExecutionService {

    void insertTCExecution(TCExecution tCExecution) throws CerberusException;

    void updateTCExecution(TCExecution tCExecution) throws CerberusException;

//    TCExecution findLastTCExecutionByCriteria(String test, String testCase, String environment, String country) throws CerberusException;

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
    List<TCExecution> findTCExecutionbyCriteria1(String dateLimitFrom, String test, String testCase, String application, String country, String environment, String controlStatus, String status) throws CerberusException;

    long registerRunID(TCExecution tCExecution);
}
