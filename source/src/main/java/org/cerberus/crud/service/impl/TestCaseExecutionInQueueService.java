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
package org.cerberus.crud.service.impl;

import java.util.List;
import java.util.Map;

import org.cerberus.crud.dao.ITestCaseCountryDAO;
import org.cerberus.crud.dao.ITestCaseExecutionInQueueDAO;
import org.cerberus.crud.entity.Application;
import org.cerberus.engine.entity.MessageGeneral;
import org.cerberus.crud.entity.TestCase;
import org.cerberus.crud.entity.TestCaseExecution;
import org.cerberus.enums.MessageGeneralEnum;
import org.cerberus.crud.entity.TestCaseExecutionInQueue;
import org.cerberus.crud.factory.IFactoryTestCaseExecution;
import org.cerberus.exception.CerberusException;
import org.cerberus.crud.service.ITestCaseExecutionInQueueService;
import org.cerberus.util.answer.Answer;
import org.cerberus.util.answer.AnswerList;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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
    private ITestCaseCountryDAO testCaseCountryDAO;
    @Autowired
    private IFactoryTestCaseExecution factoryTestCaseExecution;

    @Override
    public boolean canInsert(TestCaseExecutionInQueue inQueue) throws CerberusException {
        try {
            testCaseCountryDAO.findTestCaseCountryByKey(inQueue.getTest(), inQueue.getTestCase(), inQueue.getCountry());
            return true;
        } catch (CerberusException ce) {
            MessageGeneral messageGeneral = ce.getMessageError();
            if (messageGeneral == null || messageGeneral.getCode() != MessageGeneralEnum.NO_DATA_FOUND.getCode()) {
                throw ce;
            }
            return false;
        }
    }

    @Override
    public void insert(TestCaseExecutionInQueue inQueue) throws CerberusException {
        testCaseExecutionInQueueDAO.insert(inQueue);
    }

    @Override
    public TestCaseExecutionInQueue getNextAndProceed() throws CerberusException {
        return testCaseExecutionInQueueDAO.getNextAndProceed();
    }

    @Override
    public List<TestCaseExecutionInQueue> getProceededByTag(String tag) throws CerberusException {
        return testCaseExecutionInQueueDAO.getProceededByTag(tag);
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
    public List<TestCaseExecutionInQueue> findAllNotProcedeed() throws CerberusException {
        return testCaseExecutionInQueueDAO.getNotProceededAndProceed();
    }

    @Override
    public List<TestCaseExecutionInQueue> findAll() throws CerberusException {
        return testCaseExecutionInQueueDAO.findAll();
    }

    @Override
    public void setProcessedTo(Long l, String changeTo) throws CerberusException {
        testCaseExecutionInQueueDAO.setProcessedTo(l, changeTo);
    }

    @Override
    public void updateComment(Long queueId, String comment) throws CerberusException {
        testCaseExecutionInQueueDAO.updateComment(queueId, comment);
    }

    @Override
    public AnswerList readByTagByCriteria(String tag, int start, int amount, String sort, String searchTerm, Map<String, List<String>> individualSearch) throws CerberusException {
        return testCaseExecutionInQueueDAO.readByTagByCriteria(tag, start, amount, sort, searchTerm, individualSearch);
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
    public Answer create(TestCaseExecutionInQueue test) {
        return testCaseExecutionInQueueDAO.create(test);
    }

    @Override
    public Answer update(TestCaseExecutionInQueue test) {
        return testCaseExecutionInQueueDAO.update(test);
    }

    @Override
    public Answer delete(TestCaseExecutionInQueue test) {
        return testCaseExecutionInQueueDAO.delete(test);
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
        String myHost = testCaseExecutionInQueue.getManualHost();
        String myContextRoot = testCaseExecutionInQueue.getManualContextRoot();
        String myLoginRelativeURL = testCaseExecutionInQueue.getManualLoginRelativeURL();
        String myEnvData = testCaseExecutionInQueue.getManualEnvData();
        String seleniumIP = testCaseExecutionInQueue.getRobotIP();
        String seleniumPort = testCaseExecutionInQueue.getRobotPort();
        TestCaseExecution result = factoryTestCaseExecution.create(0, test, testCase, ip, version, environment, country, browser, version, platform,
                browser, start, end, controlStatus, controlMessage, applicationObj, ip, tag, port, tag, browser, verbose, screenshot, pageSource,
                seleniumLog, synchroneous, timeout, outputFormat, tag, version, tCase, null, null, manualURL, myHost, myContextRoot, myLoginRelativeURL,
                myEnvData, seleniumIP, seleniumPort, null, null, null, 0, "", null);
        result.setApplication(application);
        result.setIdFromQueue(testCaseExecutionInQueue.getId());
        result.setId(testCaseExecutionInQueue.getId());
        return result;
    }

}
