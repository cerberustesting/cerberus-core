/**
 * Cerberus Copyright (C) 2013 - 2017 cerberustesting
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
package org.cerberus.crud.factory;

import java.util.List;

import org.cerberus.crud.entity.Application;
import org.cerberus.crud.entity.CountryEnvParam;
import org.cerberus.crud.entity.CountryEnvironmentParameters;
import org.cerberus.engine.entity.MessageGeneral;
import org.cerberus.crud.entity.RobotCapability;
import org.cerberus.crud.entity.TestCaseExecution;
import org.cerberus.crud.entity.TestCase;
import org.cerberus.crud.entity.TestCaseStepExecution;

/**
 * @author bcivel
 */
public interface IFactoryTestCaseExecution {

    /**
     *
     * @param id
     * @param test
     * @param testCase
     * @param description
     * @param build
     * @param revision
     * @param environment
     * @param country
     * @param browser
     * @param version
     * @param platform
     * @param browserFullVersion
     * @param start
     * @param end
     * @param controlStatus
     * @param controlMessage
     * @param application
     * @param applicationObj
     * @param ip
     * @param url
     * @param port
     * @param tag
     * @param verbose
     * @param screenshot
     * @param pageSource
     * @param seleniumLog
     * @param synchroneous
     * @param timeout
     * @param outputFormat
     * @param status
     * @param crbVersion
     * @param tCase
     * @param countryEnvParam
     * @param countryEnvironmentParameters
     * @param manualURL
     * @param myHost
     * @param myContextRoot
     * @param myLoginRelativeURL
     * @param myEnvData
     * @param seleniumIP
     * @param seleniumPort
     * @param testCaseStepExecution
     * @param resultMessage
     * @param executor
     * @param numberOfRetries
     * @param screenSize
     * @param capabilities
     * @param conditionOper
     * @param conditionVal1Init
     * @param conditionVal2Init
     * @param conditionVal1
     * @param conditionVal2
     * @param manualExecution
     * @param userAgent
     * @return
     */
    TestCaseExecution create(long id, String test, String testCase, String description, String build, String revision, String environment,
            String country, String browser, String version, String platform, String browserFullVersion, long start, long end, String controlStatus, String controlMessage,
            String application, Application applicationObj, String ip, String url, String port, String tag, int verbose, int screenshot, int pageSource, int seleniumLog, boolean synchroneous, String timeout,
            String outputFormat, String status, String crbVersion, TestCase tCase, CountryEnvParam countryEnvParam,
            CountryEnvironmentParameters countryEnvironmentParameters, boolean manualURL, String myHost, String myContextRoot, String myLoginRelativeURL, String myEnvData,
            String seleniumIP, String seleniumPort, List<TestCaseStepExecution> testCaseStepExecution, MessageGeneral resultMessage, 
            String executor, int numberOfRetries, String screenSize, List<RobotCapability> capabilities,
            String conditionOper, String conditionVal1Init, String conditionVal2Init, String conditionVal1, String conditionVal2, String manualExecution, String userAgent);
}
