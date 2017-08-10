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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.cerberus.crud.dao.ITestCaseExecutionQueueDAO;
import org.cerberus.crud.entity.Application;
import org.cerberus.crud.entity.TestCase;
import org.cerberus.crud.entity.TestCaseExecution;
import org.cerberus.crud.entity.TestCaseExecutionQueue;
import org.cerberus.crud.factory.IFactoryTestCaseExecution;
import org.cerberus.crud.service.ITestCaseExecutionQueueService;
import org.cerberus.engine.entity.MessageGeneral;
import org.cerberus.enums.MessageEventEnum;
import org.cerberus.enums.MessageGeneralEnum;
import org.cerberus.exception.CerberusException;
import org.cerberus.util.answer.Answer;
import org.cerberus.util.answer.AnswerItem;
import org.cerberus.util.answer.AnswerList;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Default {@link ITestCaseExecutionQueueService} implementation
 *
 * @author abourdon
 */
@Service
public class TestCaseExecutionQueueService implements ITestCaseExecutionQueueService {

    @Autowired
    private ITestCaseExecutionQueueDAO testCaseExecutionInQueueDAO;

    @Autowired
    private IFactoryTestCaseExecution factoryTestCaseExecution;

    @Override
    public AnswerItem<TestCaseExecutionQueue> readByKey(long queueId) {
        return testCaseExecutionInQueueDAO.readByKey(queueId);
    }

    @Override
    public List<TestCaseExecutionQueue> findTestCaseExecutionInQueuebyTag(String tag) throws CerberusException {
        return testCaseExecutionInQueueDAO.findTestCaseExecutionInQueuebyTag(tag);
    }

    @Override
    public AnswerList readByTagByCriteria(String tag, int start, int amount, String sort, String searchTerm, Map<String, List<String>> individualSearch) throws CerberusException {
        return testCaseExecutionInQueueDAO.readByTagByCriteria(tag, start, amount, sort, searchTerm, individualSearch);
    }

    @Override
    public AnswerList readByVarious1(String tag, List<String> stateList) throws CerberusException {
        return testCaseExecutionInQueueDAO.readByVarious1(tag, stateList);
    }

    @Override
    public AnswerList readQueueToTreat() throws CerberusException {
        List<String> stateList = new ArrayList<>();
        stateList.add(TestCaseExecutionQueue.State.QUEUED.name());
        return testCaseExecutionInQueueDAO.readQueueToTreatOrRunning(stateList);
    }

    @Override
    public AnswerList readQueueRunning() throws CerberusException {
        List<String> stateList = new ArrayList<>();
        stateList.add(TestCaseExecutionQueue.State.WAITING.name());
        stateList.add(TestCaseExecutionQueue.State.STARTING.name());
        stateList.add(TestCaseExecutionQueue.State.EXECUTING.name());
        return testCaseExecutionInQueueDAO.readQueueToTreatOrRunning(stateList);
    }

    @Override
    public AnswerList readByCriteria(int start, int amount, String column, String dir, String searchTerm, Map<String, List<String>> individualSearch) {
        return testCaseExecutionInQueueDAO.readByCriteria(start, amount, column, dir, searchTerm, individualSearch);
    }

    @Override
    public AnswerList readDistinctEnvCountryBrowserByTag(String tag) {
        return testCaseExecutionInQueueDAO.readDistinctEnvCountryBrowserByTag(tag);
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
    public Answer create(TestCaseExecutionQueue object) {
        return testCaseExecutionInQueueDAO.create(object);
    }

    @Override
    public Answer update(TestCaseExecutionQueue object) {
        return testCaseExecutionInQueueDAO.update(object);
    }

    @Override
    public Answer updateComment(long id, String comment) {
        return testCaseExecutionInQueueDAO.updateComment(id, comment);
    }

    @Override
    public Answer updateToQueued(long id, String comment) {
        return testCaseExecutionInQueueDAO.updateToQueued(id, comment);
    }

    @Override
    public boolean updateToWaiting(final Long id) throws CerberusException {
        return testCaseExecutionInQueueDAO.updateToWaiting(id);
    }

    @Override
    public void updateToStarting(long id) throws CerberusException {
        testCaseExecutionInQueueDAO.updateToStarting(id);
    }

    @Override
    public void updateToExecuting(long id, String comment, long exeId) throws CerberusException {
        testCaseExecutionInQueueDAO.updateToExecuting(id, comment, exeId);
    }

    @Override
    public void updateToError(long id, String comment) throws CerberusException {
        testCaseExecutionInQueueDAO.updateToError(id, comment);
    }

    @Override
    public void updateToDone(long id, String comment, long exeId) throws CerberusException {
        testCaseExecutionInQueueDAO.updateToDone(id, comment, exeId);
    }

    @Override
    public Answer updateToCancelled(long id, String comment) {
        return testCaseExecutionInQueueDAO.updateToCancelled(id, comment);
    }

    @Override
    public Answer updateToCancelledForce(long id, String comment) {
        return testCaseExecutionInQueueDAO.updateToCancelledForce(id, comment);
    }

    @Override
    public Answer delete(TestCaseExecutionQueue object) {
        return testCaseExecutionInQueueDAO.delete(object);
    }

    @Override
    public Answer delete(Long id) {
        return testCaseExecutionInQueueDAO.delete(id);
    }

    @Override
    public TestCaseExecutionQueue convert(AnswerItem answerItem) throws CerberusException {
        if (answerItem.isCodeEquals(MessageEventEnum.DATA_OPERATION_OK.getCode())) {
            //if the service returns an OK message then we can get the item
            return (TestCaseExecutionQueue) answerItem.getItem();
        }
        throw new CerberusException(new MessageGeneral(MessageGeneralEnum.DATA_OPERATION_ERROR));
    }

    @Override
    public List<TestCaseExecutionQueue> convert(AnswerList answerList) throws CerberusException {
        if (answerList.isCodeEquals(MessageEventEnum.DATA_OPERATION_OK.getCode())) {
            //if the service returns an OK message then we can get the item
            return (List<TestCaseExecutionQueue>) answerList.getDataList();
        }
        throw new CerberusException(new MessageGeneral(MessageGeneralEnum.DATA_OPERATION_ERROR));
    }

    @Override
    public void convert(Answer answer) throws CerberusException {
        if (answer.isCodeEquals(MessageEventEnum.DATA_OPERATION_OK.getCode())) {
            //if the service returns an OK message then we can get the item
            return;
        }
        throw new CerberusException(new MessageGeneral(MessageGeneralEnum.DATA_OPERATION_ERROR));
    }

    @Override
    public TestCaseExecution convertToTestCaseExecution(TestCaseExecutionQueue testCaseExecutionInQueue) {
        String test = testCaseExecutionInQueue.getTest();
        String testCase = testCaseExecutionInQueue.getTestCase();
        String environment = testCaseExecutionInQueue.getEnvironment();
        String country = testCaseExecutionInQueue.getCountry();
        String browser = testCaseExecutionInQueue.getBrowser();
        String version = testCaseExecutionInQueue.getBrowserVersion();
        String platform = testCaseExecutionInQueue.getPlatform();
        long start = testCaseExecutionInQueue.getRequestDate() != null ? testCaseExecutionInQueue.getRequestDate().getTime() : 0;
        long end = 0;
        String controlStatus = TestCaseExecution.CONTROLSTATUS_QU;
        String controlMessage = "Queued with State : " + testCaseExecutionInQueue.getState().name() + " - " + testCaseExecutionInQueue.getComment();
        Application applicationObj = testCaseExecutionInQueue.getApplicationObj();
        String application = testCaseExecutionInQueue.getApplicationObj() != null ? testCaseExecutionInQueue.getApplicationObj().getApplication() : "";
        String ip = testCaseExecutionInQueue.getRobotIP();
        String port = testCaseExecutionInQueue.getRobotPort();
        String tag = testCaseExecutionInQueue.getTag();
        int verbose = testCaseExecutionInQueue.getVerbose();
        int screenshot = testCaseExecutionInQueue.getScreenshot();
        int pageSource = testCaseExecutionInQueue.getPageSource();
        int seleniumLog = testCaseExecutionInQueue.getSeleniumLog();
        int retry = testCaseExecutionInQueue.getRetries();
        boolean synchroneous = true;
        String timeout = testCaseExecutionInQueue.getTimeout();
        String outputFormat = "";
        TestCase tCase = testCaseExecutionInQueue.getTestCaseObj();
        boolean manualURL = (testCaseExecutionInQueue.getManualURL() >= 1);
        String manualExecution = testCaseExecutionInQueue.getManualExecution();
        String myHost = testCaseExecutionInQueue.getManualHost();
        String myContextRoot = testCaseExecutionInQueue.getManualContextRoot();
        String myLoginRelativeURL = testCaseExecutionInQueue.getManualLoginRelativeURL();
        String myEnvData = testCaseExecutionInQueue.getManualEnvData();
        String seleniumIP = testCaseExecutionInQueue.getRobotIP();
        String seleniumPort = testCaseExecutionInQueue.getRobotPort();
        TestCaseExecution result = factoryTestCaseExecution.create(0, test, testCase, null, null, null, environment, country, browser, version, platform,
                browser, start, end, controlStatus, controlMessage, application, applicationObj, ip, "", port, tag, verbose, screenshot, pageSource,
                seleniumLog, synchroneous, timeout, outputFormat, "", "", tCase, null, null, manualURL, myHost, myContextRoot, myLoginRelativeURL,
                myEnvData, seleniumIP, seleniumPort, null, null, null, retry, "", null, "", "", "", "", "", manualExecution, "");
        result.setQueueID(testCaseExecutionInQueue.getId());
        result.setId(testCaseExecutionInQueue.getExeId());
        return result;
    }

}
