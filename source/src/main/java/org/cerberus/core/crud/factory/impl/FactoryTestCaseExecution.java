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
package org.cerberus.core.crud.factory.impl;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.TreeMap;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.cerberus.core.crud.entity.Application;
import org.cerberus.core.crud.entity.CountryEnvParam;
import org.cerberus.core.crud.entity.CountryEnvironmentParameters;
import org.cerberus.core.crud.entity.Robot;
import org.cerberus.core.crud.entity.TestCase;
import org.cerberus.core.crud.entity.TestCaseExecution;
import org.cerberus.core.crud.entity.TestCaseExecutionData;
import org.cerberus.core.crud.entity.TestCaseExecutionFile;
import org.cerberus.core.crud.entity.TestCaseStepExecution;
import org.cerberus.core.crud.factory.IFactoryTestCaseExecution;
import org.cerberus.core.engine.entity.MessageGeneral;
import org.springframework.stereotype.Service;

/**
 * @author bcivel
 */
@Service
public class FactoryTestCaseExecution implements IFactoryTestCaseExecution {

    private static final Logger LOG = LogManager.getLogger(FactoryTestCaseExecution.class);

    @Override
    public TestCaseExecution create(long id, String test, String testCase, String description, String build, String revision, String environment, String country,
            String robot, String robotExecutor, String robotHost, String robotPort, String robotDecli,
            String browser, String version, String platform, long start, long end, String controlStatus, String controlMessage,
            String application, Application applicationObj, String url, String tag, int verbose, int screenshot, int video, int pageSource, int robotLog, int consoleLog,
            boolean synchroneous, String timeout, String outputFormat, String status, String crbVersion, TestCase tCase, CountryEnvParam countryEnvParam,
            CountryEnvironmentParameters countryEnvApplicationParam, int manualURL, String myHost, String myContextRoot, String myLoginRelativeURL, String myEnvData,
            String seleniumIP, String seleniumPort, List<TestCaseStepExecution> testCaseStepExecution, MessageGeneral resultMessage, String executor,
            int numberOfRetries, String screenSize, Robot robotObj, String robotProvider, String robotSessionId,
            String conditionOperator, String conditionVal1Init, String conditionVal2Init, String conditionVal3Init, String conditionVal1, String conditionVal2, String conditionVal3,
            String manualExecution, String userAgent, int testCaseVersion, int testCasePriority, String system,
            String usrCreated, Timestamp dateCreated, String usrModif, Timestamp dateModif) {
        TestCaseExecution newTce = new TestCaseExecution();
        newTce.setApplicationObj(applicationObj);
        newTce.setApplication(application);
        newTce.setBrowser(browser);
        newTce.setVersion(version);
        newTce.setPlatform(platform);
        newTce.setBuild(build);
        newTce.setControlMessage(controlMessage);
        newTce.setControlStatus(controlStatus);
        newTce.setCountry(country);
        newTce.setCrbVersion(crbVersion);
        newTce.setEnd(end);
        newTce.setEnvironment(environment);
        newTce.setEnvironmentData(myEnvData);
        newTce.setId(id);
        newTce.setRobot(robot);
        newTce.setRobotExecutor(robotExecutor);
        newTce.setRobotHost(robotHost);
        newTce.setRobotPort(robotPort);
        newTce.setRobotDecli(robotDecli);
        newTce.setRobotProvider(robotProvider);
        newTce.setRobotSessionID(robotSessionId);
        newTce.setRobotProviderSessionID("");
        newTce.setRevision(revision);
        newTce.setStart(start);
        newTce.setStatus(status);
        newTce.setTag(tag);
        newTce.setTest(test);
        newTce.setTestCase(testCase);
        newTce.setUrl(url);
        newTce.setVerbose(verbose);
        newTce.setScreenshot(screenshot);
        newTce.setVideo(video);
        newTce.setTestCaseObj(tCase);
        newTce.setCountryEnvParam(countryEnvParam);
        newTce.setCountryEnvApplicationParam(countryEnvApplicationParam);
        newTce.setManualURL(manualURL);
        newTce.setMyHost(myHost);
        newTce.setMyContextRoot(myContextRoot);
        newTce.setMyLoginRelativeURL(myLoginRelativeURL);
        newTce.setSeleniumIP(seleniumIP);
        newTce.setSeleniumPort(seleniumPort);
        if (testCaseStepExecution == null) {
            testCaseStepExecution = new ArrayList<>();
        }
        newTce.setTestCaseStepExecutionList(testCaseStepExecution);
        newTce.setResultMessage(resultMessage);
        newTce.setOutputFormat(outputFormat);
        newTce.setTimeout(timeout);
        newTce.setSynchroneous(synchroneous);
        newTce.setPageSource(pageSource);
        newTce.setRobotLog(robotLog);
        newTce.setConsoleLog(consoleLog);
        newTce.setExecutor(executor);
        newTce.setNumberOfRetries(numberOfRetries);
        newTce.setScreenSize(screenSize);
        newTce.setRobotObj(robotObj);
        newTce.setLastWebsocketPush(0);
        newTce.setConditionOperator(conditionOperator);
        newTce.setConditionVal1(conditionVal1);
        newTce.setConditionVal1Init(conditionVal1Init);
        newTce.setConditionVal2(conditionVal2);
        newTce.setConditionVal2Init(conditionVal2Init);
        newTce.setConditionVal3(conditionVal3);
        newTce.setConditionVal3Init(conditionVal3Init);
        newTce.setManualExecution(manualExecution);
        newTce.setUserAgent(userAgent);
        newTce.setDescription(description);
        newTce.setSystem(system);
        // List objects
        List<TestCaseExecutionFile> objectFileList = new ArrayList<>();
        newTce.setFileList(objectFileList);
        TreeMap<String, TestCaseExecutionData> hashTemp1 = new TreeMap<>();
        newTce.setTestCaseExecutionDataMap(hashTemp1);
        newTce.setNbExecutions(1);
        newTce.setTestCaseVersion(testCaseVersion);
        newTce.setTestCasePriority(testCasePriority);
        newTce.setUsrCreated(usrCreated);
        newTce.setDateCreated(dateCreated);
        newTce.setUsrModif(usrModif);
        newTce.setDateModif(dateModif);
        newTce.setNetworkTrafficIndexList(new ArrayList<>());
        newTce.setSecrets(new HashMap<>());
        newTce.setCountryEnvApplicationParams(new HashMap<>());

        return newTce;
    }

}
