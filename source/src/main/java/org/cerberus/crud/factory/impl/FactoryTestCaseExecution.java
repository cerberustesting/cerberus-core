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
package org.cerberus.crud.factory.impl;

import java.util.List;

import org.cerberus.crud.entity.Application;
import org.cerberus.crud.entity.CountryEnvParam;
import org.cerberus.crud.entity.CountryEnvironmentParameters;
import org.cerberus.crud.entity.MessageGeneral;
import org.cerberus.crud.entity.RobotCapability;
import org.cerberus.crud.entity.TestCaseExecution;
import org.cerberus.crud.entity.TCase;
import org.cerberus.crud.entity.TestCaseStepExecution;
import org.cerberus.crud.factory.IFactoryTestCaseExecution;
import org.springframework.stereotype.Service;

/**
 * @author bcivel
 */
@Service
public class FactoryTestCaseExecution implements IFactoryTestCaseExecution {

    @Override
    public TestCaseExecution create(long id, String test, String testCase, String build, String revision, String environment, String country, String browser, String version, String platform, String browserFullVersion, long start, long end, String controlStatus, String controlMessage, Application application, String ip, String url, String port, String tag, String finished, int verbose, int screenshot, int pageSource, int seleniumLog, boolean synchroneous, String timeout, String outputFormat, String status, String crbVersion, TCase tCase, CountryEnvParam countryEnvParam,
                              CountryEnvironmentParameters countryEnvironmentParameters, boolean manualURL, String myHost, String myContextRoot, String myLoginRelativeURL, String myEnvData,
                              String seleniumIP, String seleniumPort, List<TestCaseStepExecution> testCaseStepExecution,
            MessageGeneral resultMessage, String executor) {
        TestCaseExecution newTce = new TestCaseExecution();
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
        newTce.setFinished(finished);
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
        newTce.settCase(tCase);
        newTce.setCountryEnvParam(countryEnvParam);
        newTce.setCountryEnvironmentParameters(countryEnvironmentParameters);
        newTce.setManualURL(manualURL);
        newTce.setMyHost(myHost);
        newTce.setMyContextRoot(myContextRoot);
        newTce.setMyLoginRelativeURL(myLoginRelativeURL);
        newTce.setEnvironmentData(myEnvData);
        newTce.setSeleniumIP(seleniumIP);
        newTce.setSeleniumPort(seleniumPort);
        newTce.setTestCaseStepExecutionList(testCaseStepExecution);
        newTce.setResultMessage(resultMessage);
        newTce.setOutputFormat(outputFormat);
        newTce.setTimeout(timeout);
        newTce.setSynchroneous(synchroneous);
        newTce.setPageSource(pageSource);
        newTce.setSeleniumLog(seleniumLog);
        newTce.setExecutor(executor);
        return newTce;
    }

    @Override
    public TestCaseExecution create(long id, String test, String testCase, String build, String revision, String environment, String country, String browser, String version, String platform, String browserFullVersion, long start, long end, String controlStatus, String controlMessage, Application application, String ip, String url, String port, String tag, String finished, int verbose, int screenshot, int pageSource, int seleniumLog, boolean synchroneous, String timeout, String outputFormat, String status, String crbVersion, TCase tCase, CountryEnvParam countryEnvParam, CountryEnvironmentParameters countryEnvironmentParameters, boolean manualURL, String myHost, String myContextRoot, String myLoginRelativeURL, String myEnvData, String seleniumIP, String seleniumPort, List<TestCaseStepExecution> testCaseStepExecution, MessageGeneral resultMessage, String executor, int numberOfRetries) {
        TestCaseExecution newTce = this.create(id, test, testCase, build, revision, environment, country, browser, version, platform, browserFullVersion, start, end, controlStatus, controlMessage, application, ip, url, port, tag, finished, verbose, screenshot, pageSource, seleniumLog, synchroneous, timeout, outputFormat, status, crbVersion, tCase, countryEnvParam, countryEnvironmentParameters, manualURL, myHost, myContextRoot, myLoginRelativeURL, myEnvData, seleniumIP, seleniumPort, testCaseStepExecution, resultMessage, executor);
        newTce.setNumberOfRetries(numberOfRetries);
        return newTce;
    }

    @Override
    public TestCaseExecution create(long id, String test, String testCase, String build, String revision, String environment, String country, String browser, String version, String platform, String browserFullVersion, long start, long end, String controlStatus, String controlMessage, Application application, String ip, String url, String port, String tag, String finished, int verbose, int screenshot, int pageSource, int seleniumLog, boolean synchroneous, String timeout, String outputFormat, String status, String crbVersion, TCase tCase, CountryEnvParam countryEnvParam, CountryEnvironmentParameters countryEnvironmentParameters, boolean manualURL, String myHost, String myContextRoot, String myLoginRelativeURL, String myEnvData, String seleniumIP, String seleniumPort, List<TestCaseStepExecution> testCaseStepExecution, MessageGeneral resultMessage, String executor, int numberOfRetries, String screenSize) {
        TestCaseExecution newTce = this.create(id, test, testCase, build, revision, environment, country, browser, version, platform, browserFullVersion, start, end, controlStatus, controlMessage, application, ip, url, port, tag, finished, verbose, screenshot, pageSource, seleniumLog, synchroneous, timeout, outputFormat, status, crbVersion, tCase, countryEnvParam, countryEnvironmentParameters, manualURL, myHost, myContextRoot, myLoginRelativeURL, myEnvData, seleniumIP, seleniumPort, testCaseStepExecution, resultMessage, executor);
        newTce.setNumberOfRetries(numberOfRetries);
        newTce.setScreenSize(screenSize);
        return newTce;
    }

	@Override
	public TestCaseExecution create(long id, String test, String testCase, String build, String revision,
			String environment, String country, String browser, String version, String platform,
			String browserFullVersion, List<RobotCapability> capabilities, long start, long end, String controlStatus,
			String controlMessage, Application application, String ip, String url, String port, String tag,
			String finished, int verbose, int screenshot, int pageSource, int seleniumLog, boolean synchroneous,
			String timeout, String outputFormat, String status, String crbVersion, TCase tCase,
			CountryEnvParam countryEnvParam, CountryEnvironmentParameters countryEnvironmentParameters,
			boolean manualURL, String myHost, String myContextRoot, String myLoginRelativeURL, String myEnvData,
			String seleniumIP, String seleniumPort, List<TestCaseStepExecution> testCaseStepExecution,
			MessageGeneral resultMessage, String executor, int numberOfRetries, String screenSize) {
		TestCaseExecution newTce = create(id, test, testCase, build, revision, environment, country, browser, version, platform, browserFullVersion, start, end, controlStatus, controlMessage, application, ip, url, port, tag, finished, verbose, screenshot, pageSource, seleniumLog, synchroneous, timeout, outputFormat, status, crbVersion, tCase, countryEnvParam, countryEnvironmentParameters, manualURL, myHost, myContextRoot, myLoginRelativeURL, myEnvData, seleniumIP, seleniumPort, testCaseStepExecution, resultMessage, executor, numberOfRetries, screenSize);
		newTce.setCapabilities(capabilities);
		return newTce;
	}

}
