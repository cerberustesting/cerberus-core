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
package org.cerberus.crud.factory.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.TreeMap;

import org.cerberus.crud.entity.Application;
import org.cerberus.crud.entity.CountryEnvParam;
import org.cerberus.crud.entity.CountryEnvironmentParameters;
import org.cerberus.engine.entity.MessageGeneral;
import org.cerberus.crud.entity.RobotCapability;
import org.cerberus.crud.entity.TestCaseExecution;
import org.cerberus.crud.entity.TestCase;
import org.cerberus.crud.entity.TestCaseExecutionData;
import org.cerberus.crud.entity.TestCaseExecutionFile;
import org.cerberus.crud.entity.TestCaseStepExecution;
import org.cerberus.crud.factory.IFactoryTestCaseExecution;
import org.springframework.stereotype.Service;

/**
 * @author bcivel
 */
@Service
public class FactoryTestCaseExecution implements IFactoryTestCaseExecution {

    @Override
    public TestCaseExecution create(long id, String test, String testCase, String description, String build, String revision, String environment, String country,
            String browser, String version, String platform, String browserFullVersion, long start, long end, String controlStatus, String controlMessage,
            String application, Application applicationObj, String ip, String url, String port, String tag, int verbose, int screenshot, int pageSource, int seleniumLog,
            boolean synchroneous, String timeout, String outputFormat, String status, String crbVersion, TestCase tCase, CountryEnvParam countryEnvParam,
            CountryEnvironmentParameters countryEnvironmentParameters, boolean manualURL, String myHost, String myContextRoot, String myLoginRelativeURL, String myEnvData,
            String seleniumIP, String seleniumPort, List<TestCaseStepExecution> testCaseStepExecution, MessageGeneral resultMessage, String executor,
            int numberOfRetries, String screenSize, List<RobotCapability> capabilities,
            String conditionOper, String conditionVal1Init, String conditionVal2Init, String conditionVal1, String conditionVal2, String manualExecution, String userAgent, int testCaseVersion) {
        TestCaseExecution newTce = new TestCaseExecution();
        newTce.setApplicationObj(applicationObj);
        newTce.setApplication(application);
        newTce.setBrowser(browser);
        newTce.setVersion(version);
        newTce.setPlatform(platform);
        newTce.setBrowserFullVersion(browserFullVersion);
        newTce.setBuild(build);
        newTce.setControlMessage(controlMessage);
        newTce.setControlStatus(controlStatus);
        newTce.setCountry(country);
        newTce.setCrbVersion(crbVersion);
        newTce.setEnd(end);
        newTce.setEnvironment(environment);
        newTce.setEnvironmentData(myEnvData);
        newTce.setId(id);
        newTce.setIp(ip);
        newTce.setPort(port);
        newTce.setRevision(revision);
        newTce.setStart(start);
        newTce.setStatus(status);
        newTce.setTag(tag);
        newTce.setTest(test);
        newTce.setTestCase(testCase);
        newTce.setUrl(url);
        newTce.setVerbose(verbose);
        newTce.setScreenshot(screenshot);
        newTce.setTestCaseObj(tCase);
        newTce.setCountryEnvParam(countryEnvParam);
        newTce.setCountryEnvironmentParameters(countryEnvironmentParameters);
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
        newTce.setSeleniumLog(seleniumLog);
        newTce.setExecutor(executor);
        newTce.setNumberOfRetries(numberOfRetries);
        newTce.setScreenSize(screenSize);
        newTce.setCapabilities(capabilities);
        newTce.setLastWebsocketPush(0);
        newTce.setConditionOper(conditionOper);
        newTce.setConditionVal1(conditionVal1);
        newTce.setConditionVal1Init(conditionVal1Init);
        newTce.setConditionVal2(conditionVal2);
        newTce.setConditionVal2Init(conditionVal2Init);
        newTce.setManualExecution(manualExecution);
        newTce.setUserAgent(userAgent);
        newTce.setDescription(description);
        // List objects
        List<TestCaseExecutionFile> objectFileList = new ArrayList<TestCaseExecutionFile>();
        newTce.setFileList(objectFileList);
        TreeMap<String, TestCaseExecutionData> hashTemp1 = new TreeMap<>();
        newTce.setTestCaseExecutionDataMap(hashTemp1);
        newTce.setNbExecutions(1);
        newTce.setTestCaseVersion(testCaseVersion);
        return newTce;
    }

}
