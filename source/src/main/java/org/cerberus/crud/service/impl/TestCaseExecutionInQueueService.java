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
package org.cerberus.crud.service.impl;

import org.cerberus.crud.dao.ITestCaseExecutionInQueueDAO;
import org.cerberus.crud.entity.Application;
import org.cerberus.crud.entity.TestCase;
import org.cerberus.crud.entity.TestCaseExecution;
import org.cerberus.crud.entity.TestCaseExecutionInQueue;
import org.cerberus.crud.factory.IFactoryTestCaseExecution;
import org.cerberus.crud.service.ITestCaseExecutionInQueueService;
import org.cerberus.exception.CerberusException;
import org.cerberus.util.answer.AnswerList;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * Default {@link ITestCaseExecutionInQueueService} implementation
 *
 * @author abourdon
 */
@Service
public class TestCaseExecutionInQueueService implements ITestCaseExecutionInQueueService {

    @Autowired
    private ITestCaseExecutionInQueueDAO testCaseExecutionInQueueDAO;

    @Autowired
    private IFactoryTestCaseExecution factoryTestCaseExecution;

    @Override
    public void insert(TestCaseExecutionInQueue inQueue) throws CerberusException {
        testCaseExecutionInQueueDAO.insert(inQueue);
    }

    @Override
    public void remove(long id) throws CerberusException {
        testCaseExecutionInQueueDAO.remove(id);
    }

    @Override
    public List<TestCaseExecutionInQueue> findTestCaseExecutionInQueuebyTag(String tag) throws CerberusException {
        return testCaseExecutionInQueueDAO.findTestCaseExecutionInQueuebyTag(tag);
    }

    @Override
    public TestCaseExecutionInQueue findByKey(long id) throws CerberusException {
        return testCaseExecutionInQueueDAO.findByKey(id);
    }

    @Override
    public void toWaiting(long id) throws CerberusException {
        testCaseExecutionInQueueDAO.toWaiting(id);
    }

    @Override
    public List<Long> toWaiting(final List<Long> ids) throws CerberusException {
        return testCaseExecutionInQueueDAO.toWaiting(ids);
    }

    @Override
    public List<TestCaseExecutionInQueue> toQueued() throws CerberusException {
        return testCaseExecutionInQueueDAO.toQueued(ITestCaseExecutionInQueueDAO.UNLIMITED_FETCH_SIZE);
    }

    @Override
    public List<TestCaseExecutionInQueue> toQueued(int maxFetchSize) throws CerberusException {
        return testCaseExecutionInQueueDAO.toQueued(maxFetchSize);
    }

    @Override
    public List<TestCaseExecutionInQueue> toQueued(final List<Long> ids) throws CerberusException {
        return testCaseExecutionInQueueDAO.toQueued(ids);
    }

    @Override
    public void toExecuting(long id) throws CerberusException {
        testCaseExecutionInQueueDAO.toExecuting(id);
    }

    @Override
    public void toError(long id, String comment) throws CerberusException {
        testCaseExecutionInQueueDAO.toError(id, comment);
    }

    @Override
    public void toCancelled(long id) throws CerberusException {
        testCaseExecutionInQueueDAO.toCancelled(id);
    }

    @Override
    public List<Long> toCancelled(final List<Long> ids) throws CerberusException {
        return testCaseExecutionInQueueDAO.toCancelled(ids);
    }

    @Override
    public List<TestCaseExecutionInQueue> findAll() throws CerberusException {
        return testCaseExecutionInQueueDAO.findAll();
    }

    @Override
    public AnswerList readByTagByCriteria(String tag, int start, int amount, String sort, String searchTerm, Map<String, List<String>> individualSearch) throws CerberusException {
        return testCaseExecutionInQueueDAO.readByTagByCriteria(tag, start, amount, sort, searchTerm, individualSearch);
    }
    
    @Override
    public AnswerList readByTag(String tag) throws CerberusException {
        return testCaseExecutionInQueueDAO.readByTag(tag);
    }

    @Override
    public AnswerList readByCriteria(int start, int amount, String column, String dir, String searchTerm, Map<String, List<String>> individualSearch) {
        return testCaseExecutionInQueueDAO.readByCriteria(start, amount, column, dir, searchTerm, individualSearch);
    }

    @Override
    public AnswerList readDistinctEnvCoutnryBrowserByTag(String tag) {
        return testCaseExecutionInQueueDAO.readDistinctEnvCoutnryBrowserByTag(tag);
    }

    @Override
    public AnswerList readDistinctColumnByTag(String tag, boolean env, boolean country, boolean browser, boolean app) {
        return testCaseExecutionInQueueDAO.readDistinctColumnByTag(tag, env, country, browser, app);
    }

    @Override
    public AnswerList readDistinctValuesByCriteria(String columnName, String sort, String searchParameter, Map<String, List<String>> individualSearch, String column) {
        return testCaseExecutionInQueueDAO.readDistinctValuesByCriteria(columnName, sort, searchParameter, individualSearch, column);
    }

    @Override
    public AnswerList findTagList(int tagnumber) {
        return testCaseExecutionInQueueDAO.findTagList(tagnumber);
    }

    @Override
    public AnswerList readBySystemByVarious(String system, List<String> testList, List<String> applicationList, List<String> projectList, List<String> tcstatusList, List<String> groupList, List<String> tcactiveList, List<String> priorityList, List<String> targetsprintList, List<String> targetrevisionList, List<String> creatorList, List<String> implementerList, List<String> buildList, List<String> revisionList, List<String> environmentList, List<String> countryList, List<String> browserList, List<String> tcestatusList, String ip, String port, String tag, String browserversion, String comment, String bugid, String ticket) {
        return testCaseExecutionInQueueDAO.readBySystemByVarious(system, testList, applicationList, projectList, tcstatusList, groupList, tcactiveList, priorityList, targetsprintList,
                targetrevisionList, creatorList, implementerList, buildList, revisionList, environmentList, countryList, browserList, tcestatusList,
                ip, port, tag, browserversion, comment, bugid, ticket);

    }

    @Override
    public TestCaseExecution convertToTestCaseExecution(TestCaseExecutionInQueue testCaseExecutionInQueue) {
        String test = testCaseExecutionInQueue.getTest();
        String testCase = testCaseExecutionInQueue.getTestCase();
        String environment = testCaseExecutionInQueue.getEnvironment();
        String country = testCaseExecutionInQueue.getCountry();
        String browser = testCaseExecutionInQueue.getBrowser();
        String version = testCaseExecutionInQueue.getBrowserVersion();
        String platform = testCaseExecutionInQueue.getPlatform();
        long start = testCaseExecutionInQueue.getRequestDate() != null ? testCaseExecutionInQueue.getRequestDate().getTime() : 0;
        long end = 0;
        String controlStatus = "NE";
        String controlMessage = "Not Executed";
        Application applicationObj = testCaseExecutionInQueue.getApplicationObj();
        String application = testCaseExecutionInQueue.getApplicationObj() != null ? testCaseExecutionInQueue.getApplicationObj().getApplication() : "";
        String ip = testCaseExecutionInQueue.getRobotIP();
        String port = testCaseExecutionInQueue.getRobotPort();
        String tag = testCaseExecutionInQueue.getTag();
        int verbose = testCaseExecutionInQueue.getVerbose();
        int screenshot = testCaseExecutionInQueue.getScreenshot();
        int pageSource = testCaseExecutionInQueue.getPageSource();
        int seleniumLog = testCaseExecutionInQueue.getSeleniumLog();
        boolean synchroneous = testCaseExecutionInQueue.isSynchroneous();
        String timeout = testCaseExecutionInQueue.getTimeout();
        String outputFormat = testCaseExecutionInQueue.getOutputFormat();
        TestCase tCase = testCaseExecutionInQueue.getTestCaseObj();
        boolean manualURL = testCaseExecutionInQueue.isManualURL();
        boolean manualExecution = testCaseExecutionInQueue.isManualExecution();
        String myHost = testCaseExecutionInQueue.getManualHost();
        String myContextRoot = testCaseExecutionInQueue.getManualContextRoot();
        String myLoginRelativeURL = testCaseExecutionInQueue.getManualLoginRelativeURL();
        String myEnvData = testCaseExecutionInQueue.getManualEnvData();
        String seleniumIP = testCaseExecutionInQueue.getRobotIP();
        String seleniumPort = testCaseExecutionInQueue.getRobotPort();
        TestCaseExecution result = factoryTestCaseExecution.create(0, test, testCase, ip, version, environment, country, browser, version, platform,
                browser, start, end, controlStatus, controlMessage, application, applicationObj, ip, tag, port, tag, browser, verbose, screenshot, pageSource,
                seleniumLog, synchroneous, timeout, outputFormat, tag, version, tCase, null, null, manualURL, myHost, myContextRoot, myLoginRelativeURL,
                myEnvData, seleniumIP, seleniumPort, null, null, null, 0, "", null, "", "", "", "", "", manualExecution, "");
        result.setIdFromQueue(testCaseExecutionInQueue.getId());
        result.setId(testCaseExecutionInQueue.getId());
        return result;
    }

}
