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

import java.util.Date;

import org.cerberus.crud.entity.TestCaseExecutionInQueue;
import org.cerberus.exception.FactoryCreationException;
import org.cerberus.crud.factory.IFactoryTestCaseExecutionInQueue;
import org.springframework.stereotype.Service;

/**
 * Default {@link IFactoryTestCaseExecutionInQueue} implementation
 *
 * @author abourdon
 */
@Service
public class FactoryTestCaseExecutionInQueue implements IFactoryTestCaseExecutionInQueue {

    private static final long NEW_ENTRY_INDEX = -1;

    @Override
    public TestCaseExecutionInQueue create(long id, String test, String testCase, String country, String environment, String robot, String robotIP, String robotPort,
            String browser, String browserVersion, String platform, boolean manualURL, String manualHost, String manualContextRoot, String manualLoginRelativeURL,
            String manualEnvData, String tag, String outputFormat, int screenshot, int verbose, String timeout, boolean synchroneous, int pageSource, int seleniumLog,
            Date requestDate) throws FactoryCreationException {
        try {
            TestCaseExecutionInQueue inQueue = new TestCaseExecutionInQueue();
            inQueue.setId(id);
            inQueue.setTest(test);
            inQueue.setTestCase(testCase);
            inQueue.setCountry(country);
            inQueue.setEnvironment(environment);
            inQueue.setRobot(robot);
            inQueue.setRobotIP(robotIP);
            inQueue.setRobotPort(robotPort);
            inQueue.setBrowser(browser);
            inQueue.setBrowserVersion(browserVersion);
            inQueue.setPlatform(platform);
            inQueue.setManualURL(manualURL);
            inQueue.setManualHost(manualHost);
            inQueue.setManualContextRoot(manualContextRoot);
            inQueue.setManualLoginRelativeURL(manualLoginRelativeURL);
            inQueue.setManualEnvData(manualEnvData);
            inQueue.setTag(tag);
            inQueue.setOutputFormat(outputFormat);
            inQueue.setScreenshot(screenshot);
            inQueue.setVerbose(verbose);
            inQueue.setTimeout(timeout);
            inQueue.setSynchroneous(synchroneous);
            inQueue.setPageSource(pageSource);
            inQueue.setSeleniumLog(seleniumLog);
            inQueue.setRequestDate(requestDate);
            return inQueue;
        } catch (IllegalArgumentException iae) {
            throw new FactoryCreationException("Unable to create a TestCaseExecutionInQueue instance", iae);
        } catch (IllegalStateException ise) {
            throw new FactoryCreationException("Unable to create a TestCaseExecutionInQueue instance", ise);
        }
    }

    @Override
    public TestCaseExecutionInQueue create(String test, String testCase, String country, String environment, String robot, String robotIP, String robotPort, String browser,
            String browserVersion, String platform, boolean manualURL, String manualHost, String manualContextRoot, String manualLoginRelativeURL, String manualEnvData,
            String tag, String outputFormat, int screenshot, int verbose, String timeout, boolean synchroneous, int pageSource, int seleniumLog, Date requestDate, Integer retries, boolean manualExecution)
            throws FactoryCreationException {
        TestCaseExecutionInQueue inQueue = create(NEW_ENTRY_INDEX, test, testCase, country, environment, robot, robotIP, robotPort, browser, browserVersion, platform, manualURL, manualHost,
                manualContextRoot, manualLoginRelativeURL, manualEnvData, tag, outputFormat, screenshot, verbose, timeout, synchroneous, pageSource, seleniumLog, requestDate);
        inQueue.setRetries(retries);
        inQueue.setManualExecution(manualExecution);
        return inQueue;
    }

    @Override
    public TestCaseExecutionInQueue create(long id, String test, String testCase, String country, String environment, String robot, String robotIP, String robotPort, String browser,
            String browserVersion, String platform, boolean manualURL, String manualHost, String manualContextRoot, String manualLoginRelativeURL, String manualEnvData,
            String tag, String outputFormat, int screenshot, int verbose, String timeout, boolean synchroneous, int pageSource, int seleniumLog, Date requestDate, String processed, String comment, Integer retries, boolean manualExecution) throws FactoryCreationException {
        TestCaseExecutionInQueue inQueue;
        inQueue = this.create(id, test, testCase, country, environment, robot, robotIP, robotPort, browser, browserVersion, platform, manualURL, manualHost, manualContextRoot, manualLoginRelativeURL, manualEnvData, tag, outputFormat, screenshot, verbose, timeout, synchroneous, pageSource, seleniumLog, requestDate);
        inQueue.setProcessed(processed);
        inQueue.setComment(comment);
        inQueue.setRetries(retries);
        inQueue.setManualExecution(manualExecution);
        return inQueue;
    }
}
