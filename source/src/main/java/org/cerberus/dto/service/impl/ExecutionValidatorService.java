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
import org.cerberus.crud.dao.ITestCaseCountryDAO;
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
    
     private static final String TEST_CASE_COUNTRY_ERROR = "This Test Case is not defined for this country";
     private static final String COUNTRY_ENV_PARAM_APPLICATION_ERROR = "The application of the Test Case is not set for this environment";
     private static final String TEST_CASE_EXECUTION_OK = "This Test Case is set to run with this configuration";

    @Override
    public void validateExecution(ExecutionValidator execution, List<ExecutionValidator> notValidList) {
        if (!this.checkInNotValidList(execution, notValidList)) {
            if (!this.checkTestCaseCountry(execution.getTest(), execution.getTestCase(), execution.getCountry())) {
                execution.setValid(false);
                execution.setMessage(TEST_CASE_COUNTRY_ERROR);
                notValidList.add(execution);
            } else if (!this.checkCountryEnvParam(execution.getSystem(), execution.getCountry(), execution.getEnvironment(), execution.getApplication())) {
                execution.setValid(false);
                execution.setMessage(COUNTRY_ENV_PARAM_APPLICATION_ERROR);
                notValidList.add(execution);
            } else {
                execution.setValid(true);
                execution.setMessage(TEST_CASE_EXECUTION_OK);
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

    private boolean checkInNotValidList(ExecutionValidator execution, List<ExecutionValidator> notValidList) {
        for (ExecutionValidator notValid : notValidList) {
            if (execution.getTest().equals(notValid.getTest())
                    && execution.getTestCase().equals(notValid.getTestCase())
                    && execution.getCountry().equals(notValid.getCountry())
                    && notValid.getMessage().equals(TEST_CASE_COUNTRY_ERROR)) {
                execution.setValid(false);
                execution.setMessage(TEST_CASE_COUNTRY_ERROR);
                return true;
            } else if (execution.getApplication().equals(notValid.getApplication())
                    && execution.getEnvironment().equals(notValid.getEnvironment())
                    && execution.getCountry().equals(notValid.getCountry())
                    && execution.getSystem().equals(notValid.getSystem())
                    && notValid.getMessage().equals(COUNTRY_ENV_PARAM_APPLICATION_ERROR)) {
                
                execution.setValid(false);
                execution.setMessage(COUNTRY_ENV_PARAM_APPLICATION_ERROR);
                return true;
            }
        }
        return false;
    }

}
