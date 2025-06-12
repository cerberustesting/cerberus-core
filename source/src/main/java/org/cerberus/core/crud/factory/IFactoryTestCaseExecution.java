/**
 * Cerberus Copyright (C) 2013 - 2025 cerberustesting
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
package org.cerberus.core.crud.factory;

import java.sql.Timestamp;
import java.util.List;
import org.cerberus.core.crud.entity.Application;
import org.cerberus.core.crud.entity.CountryEnvParam;
import org.cerberus.core.crud.entity.CountryEnvironmentParameters;
import org.cerberus.core.crud.entity.Robot;
import org.cerberus.core.crud.entity.TestCase;
import org.cerberus.core.crud.entity.TestCaseExecution;
import org.cerberus.core.crud.entity.TestCaseStepExecution;
import org.cerberus.core.engine.entity.MessageGeneral;

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
     * @param robot
     * @param robotExecutor
     * @param browser
     * @param version
     * @param platform
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
     * @param video
     * @param robotLog
     * @param synchroneous
     * @param consoleLog
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
     * @param robotObj
     * @param robotProvider
     * @param robotSessionId
     * @param conditionOperator
     * @param conditionVal1Init
     * @param conditionVal2Init
     * @param conditionVal3Init
     * @param conditionVal1
     * @param conditionVal2
     * @param conditionVal3
     * @param manualExecution
     * @param userAgent
     * @param testCaseVersion
     * @param testCasePriority
     * @param system
     * @param robotDecli
     * @param usrCreated
     * @param dateCreated
     * @param usrModif
     * @param dateModif
     * @return
     */
    TestCaseExecution create(long id, String test, String testCase, String description, String build, String revision, String environment, String country,
            String robot, String robotExecutor, String ip, String port, String robotDecli,
            String browser, String version, String platform, long start, long end, String controlStatus, String controlMessage,
            String application, Application applicationObj, String url, String tag, int verbose, int screenshot, int video, int pageSource, int robotLog, int consoleLog, boolean synchroneous, String timeout,
            String outputFormat, String status, String crbVersion, TestCase tCase, CountryEnvParam countryEnvParam,
            CountryEnvironmentParameters countryEnvironmentParameters, int manualURL, String myHost, String myContextRoot, String myLoginRelativeURL, String myEnvData,
            String seleniumIP, String seleniumPort, List<TestCaseStepExecution> testCaseStepExecution, MessageGeneral resultMessage,
            String executor, int numberOfRetries, String screenSize, Robot robotObj, String robotProvider, String robotSessionId,
            String conditionOperator, String conditionVal1Init, String conditionVal2Init, String conditionVal3Init, String conditionVal1, String conditionVal2, String conditionVal3, 
            String manualExecution, String userAgent, int testCaseVersion, int testCasePriority, String system,
            String usrCreated, Timestamp dateCreated, String usrModif, Timestamp dateModif);
}
