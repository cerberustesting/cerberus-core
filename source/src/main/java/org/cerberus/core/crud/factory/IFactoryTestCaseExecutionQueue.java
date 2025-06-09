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
import java.util.Date;

import org.cerberus.core.crud.entity.TestCaseExecutionQueue;
import org.cerberus.core.exception.FactoryCreationException;

/**
 * Factories to create a {@link TestCaseExecutionQueue} instance
 *
 * @author abourdon
 */
public interface IFactoryTestCaseExecutionQueue {

    TestCaseExecutionQueue create(long id, String system, String test, String testCase, String country, String environment, String robot, String robotDecli, String robotIP, String robotPort, String browser,
            String browserVersion, String platform, String screenSize, int manualURL, String manualHost, String manualContextRoot, String manualLoginRelativeURL, String manualEnvData,
            String tag, int screenshot, int video, int verbose, String timeout, int pageSource, int robotLog, int consoleLog, long exeId, Integer retries,
            String manualExecution, String usrCreated, Timestamp dateCreated, String usrModif, Timestamp dateModif)
            throws FactoryCreationException;

    TestCaseExecutionQueue create(String system, String test, String testCase, String country, String environment, String robot, String robotDecli, String robotIP, String robotPort, String browser,
            String browserVersion, String platform, String screenSize, int manualURL, String manualHost, String manualContextRoot, String manualLoginRelativeURL, String manualEnvData,
            String tag, int screenshot, int video, int verbose, String timeout, int pageSource, int robotLog, int consoleLog, long exeId, Integer retries,
            String manualExecution, int priority, String usrCreated, Timestamp dateCreated, String usrModif, Timestamp dateModif)
            throws FactoryCreationException;

    TestCaseExecutionQueue create(long id, String system, String test, String testCase, String country, String environment, String robot, String robotDecli, String robotIP, String robotPort, String browser,
            String browserVersion, String platform, String screenSize, int manualURL, String manualHost, String manualContextRoot, String manualLoginRelativeURL, String manualEnvData,
            String tag, int screenshot, int video, int verbose, String timeout, int pageSource, int robotLog, int consoleLog, Date requestDate, TestCaseExecutionQueue.State state, int Priority, String comment, String DebugFlag, 
            Integer retries,Integer alreadyExecuted,
            String manualExecution, long exeId, String usrCreated, Timestamp dateCreated, String usrModif, Timestamp dateModif)
            throws FactoryCreationException;

}
