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
package org.cerberus.crud.factory;

import java.util.Date;

import org.cerberus.crud.entity.TestCaseExecutionInQueue;
import org.cerberus.exception.FactoryCreationException;

/**
 * Factories to create a {@link TestCaseExecutionInQueue} instance
 * 
 * @author abourdon
 */
public interface IFactoryTestCaseExecutionInQueue {

	TestCaseExecutionInQueue create(long id, String test, String testCase, String country, String environment, String robot, String robotIP, String robotPort, String browser,
			String browserVersion, String platform, boolean manualURL, String manualHost, String manualContextRoot, String manualLoginRelativeURL, String manualEnvData,
			String tag, String outputFormat, int screenshot, int verbose, String timeout, boolean synchroneous, int pageSource, int seleniumLog, Date requestDate)
			throws FactoryCreationException;

	TestCaseExecutionInQueue create(String test, String testCase, String country, String environment, String robot, String robotIP, String robotPort, String browser,
			String browserVersion, String platform, boolean manualURL, String manualHost, String manualContextRoot, String manualLoginRelativeURL, String manualEnvData,
			String tag, String outputFormat, int screenshot, int verbose, String timeout, boolean synchroneous, int pageSource, int seleniumLog, Date requestDate, Integer retries, boolean manualExecution)
			throws FactoryCreationException;
        
        TestCaseExecutionInQueue create(long id, String test, String testCase, String country, String environment, String robot, String robotIP, String robotPort, String browser,
			String browserVersion, String platform, boolean manualURL, String manualHost, String manualContextRoot, String manualLoginRelativeURL, String manualEnvData,
			String tag, String outputFormat, int screenshot, int verbose, String timeout, boolean synchroneous, int pageSource, int seleniumLog, Date requestDate, String processed, String comment, Integer retries, boolean manualExecution)
			throws FactoryCreationException;

}
