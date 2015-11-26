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
package org.cerberus.dto.service.impl;

import java.util.List;
import org.cerberus.crud.dao.ICountryEnvironmentParametersDAO;
import org.cerberus.crud.dao.IInvariantDAO;
import org.cerberus.crud.dao.ITestCaseCountryDAO;
import org.cerberus.crud.entity.Invariant;
import org.cerberus.crud.entity.TestCaseExecution;
import org.cerberus.dto.ExecutionValidator;
import org.cerberus.dto.service.IExecutionValidatorService;
import org.cerberus.exception.CerberusException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 *
 * @author cerberus
 */
@Service
public class ExecutionValidatorService implements IExecutionValidatorService {

    @Autowired
    ICountryEnvironmentParametersDAO countryEnvironmentParametersDAO;

    @Autowired
    ITestCaseCountryDAO testCaseCountryDAO;
    
    @Autowired
    IInvariantDAO invariantDAO;
    
     private static final String TEST_CASE_COUNTRY_ERROR = "This Test Case is not defined for this country";
     private static final String COUNTRY_ENV_PARAM_APPLICATION_ERROR = "The application of the Test Case is not set for this environment";
     private static final String RUN_QA_NOT_DEFINED = "This Test Case is not defined to run on the QA environment";
     private static final String RUN_UAT_NOT_DEFINED = "This Test Case is not defined to run on the UAT environment";
     private static final String RUN_PROD_NOT_DEFINED = "This Test Case is not defined to run on the PROD environment";
     private static final String TEST_CASE_EXECUTION_OK = "This Test Case is set to run with this configuration";

    @Override
    public void validateExecution(ExecutionValidator execValidator, List<ExecutionValidator> notValidList) {
        TestCaseExecution execution = execValidator.getExecution();
        
        {
        }
        
        if (!this.checkInNotValidList(execValidator, notValidList)) {
            if (!this.checkTestCaseCountry(execution.getTest(), execution.getTestCase(), execution.getCountry())) {
                execValidator.setValid(false);
                execValidator.setMessage(TEST_CASE_COUNTRY_ERROR);
                notValidList.add(execValidator);
            } else if (!this.checkCountryEnvParam(execution.getApplication().getSystem(), execution.getCountry(), execution.getEnvironment(), execution.getApplication().getApplication())) {
                execValidator.setValid(false);
                execValidator.setMessage(COUNTRY_ENV_PARAM_APPLICATION_ERROR);
                notValidList.add(execValidator);
            } else if (!this.checkRunEnvironment(execution.getEnvironmentDataObj(), execValidator)) {
                execValidator.setValid(false);
                notValidList.add(execValidator);
            } else {
                execValidator.setValid(true);
                execValidator.setMessage(TEST_CASE_EXECUTION_OK);
            }
        }
    }

    private boolean checkTestCaseCountry(String test, String testCase, String country) {
        try {
            testCaseCountryDAO.findTestCaseCountryByKey(test, testCase, country);
            return true;
        } catch (CerberusException ce) {
            return false;
        }
    }

    private boolean checkCountryEnvParam(String system, String country, String environment, String application) {
        try {
            countryEnvironmentParametersDAO.findCountryEnvironmentParameterByKey(system, country, environment, application);
            return true;
        } catch (CerberusException ce) {
            return false;
        }
    }
    
    private boolean checkRunEnvironment(Invariant environment, ExecutionValidator execValidator) {
        if (environment.getGp1().equals("QA") && !execValidator.getRunQA()) {
            execValidator.setMessage(RUN_QA_NOT_DEFINED);
            return false;
        } else if (environment.getGp1().equals("UAT") && !execValidator.getRunUAT()) {
            execValidator.setMessage(RUN_UAT_NOT_DEFINED);
            return false;
        } else if (environment.getGp1().equals("PROD") && !execValidator.getRunPROD()) {
            execValidator.setMessage(RUN_PROD_NOT_DEFINED);
            return false;
        }
        return true;
    }

    private boolean checkInNotValidList(ExecutionValidator execValidator, List<ExecutionValidator> notValidList) {
        TestCaseExecution execution = execValidator.getExecution();
        
        for (ExecutionValidator notValid : notValidList) {
            TestCaseExecution execNotValid = notValid.getExecution();
            
            if (execution.getTest().equals(execNotValid.getTest())
                    && execution.getTestCase().equals(execNotValid.getTestCase())
                    && execution.getCountry().equals(execNotValid.getCountry())
                    && notValid.getMessage().equals(TEST_CASE_COUNTRY_ERROR)) {
                execValidator.setValid(false);
                execValidator.setMessage(TEST_CASE_COUNTRY_ERROR);
                return true;
            } else if (execution.getApplication().equals(execNotValid.getApplication())
                    && execution.getEnvironment().equals(execNotValid.getEnvironment())
                    && execution.getCountry().equals(execNotValid.getCountry())
                    && execution.getApplication().getSystem().equals(execNotValid.getApplication().getSystem())
                    && notValid.getMessage().equals(COUNTRY_ENV_PARAM_APPLICATION_ERROR)) {
                
                execValidator.setValid(false);
                execValidator.setMessage(COUNTRY_ENV_PARAM_APPLICATION_ERROR);
                return true;
            }
        }
        return false;
    }

}
