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
package org.cerberus.core.crud.service.impl;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.cerberus.core.crud.dao.ITestCaseExecutionQueueDAO;
import org.cerberus.core.crud.entity.Application;
import org.cerberus.core.crud.entity.TestCase;
import org.cerberus.core.crud.entity.TestCaseExecution;
import org.cerberus.core.crud.entity.TestCaseExecutionQueue;
import org.cerberus.core.crud.entity.TestCaseExecutionQueueDep;
import org.cerberus.core.crud.factory.IFactoryTagSystem;
import org.cerberus.core.crud.factory.IFactoryTestCaseExecution;
import org.cerberus.core.crud.service.IParameterService;
import org.cerberus.core.crud.service.ITagService;
import org.cerberus.core.crud.service.ITagSystemService;
import org.cerberus.core.crud.service.ITestCaseExecutionQueueDepService;
import org.cerberus.core.crud.service.ITestCaseExecutionQueueService;
import org.cerberus.core.engine.entity.MessageGeneral;
import org.cerberus.core.engine.queuemanagement.entity.TestCaseExecutionQueueToTreat;
import org.cerberus.core.enums.MessageEventEnum;
import org.cerberus.core.enums.MessageGeneralEnum;
import org.cerberus.core.exception.CerberusException;
import org.cerberus.core.util.StringUtil;
import org.cerberus.core.util.answer.Answer;
import org.cerberus.core.util.answer.AnswerItem;
import org.cerberus.core.util.answer.AnswerList;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.cerberus.core.engine.entity.MessageEvent;

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
    private ITagSystemService tagSystemService;
    @Autowired
    private IFactoryTagSystem factoryTagSystem;
    @Autowired
    private IParameterService parameterService;
    @Autowired
    private IFactoryTestCaseExecution factoryTestCaseExecution;
    @Autowired
    private ITagService tagService;
    @Autowired
    private ITestCaseExecutionQueueDepService testCaseExecutionQueueDepService;

    private static final Logger LOG = LogManager.getLogger(TestCaseExecutionQueueService.class);

    @Override
    public AnswerItem<TestCaseExecutionQueue> readByKey(long queueId, boolean withDep) {
        AnswerItem<TestCaseExecutionQueue> result = testCaseExecutionInQueueDAO.readByKey(queueId);
        if (withDep && result.isCodeEquals(MessageEventEnum.DATA_OPERATION_OK.getCode()) && result.getItem() != null) {
            TestCaseExecutionQueue obj = result.getItem();
            AnswerList<TestCaseExecutionQueueDep> depAnsList = testCaseExecutionQueueDepService.readByExeQueueId(queueId);
            if (depAnsList.isCodeEquals(MessageEventEnum.DATA_OPERATION_OK.getCode())) {
                List<TestCaseExecutionQueueDep> depList = depAnsList.getDataList();
                obj.setTestcaseExecutionQueueDepList(depList);
                result.setItem(obj);
            }
        }
        return result;
    }

    @Override
    public AnswerList<TestCaseExecutionQueue> readMaxIdByTag(String tag) throws CerberusException {
        LOG.debug("Call Queue List readMaxIdByTag");
        AnswerList<Long> answer = testCaseExecutionInQueueDAO.readMaxIdListByTag(tag);
        LOG.debug(answer.getDataList().toString());
        if (answer.getDataList() != null && !answer.getDataList().isEmpty()) {
            return testCaseExecutionInQueueDAO.readByQueueIdList(answer.getDataList());
        } else {
            MessageEvent msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_OK);
            List<TestCaseExecutionQueue> testCaseExecutionInQueueList = new ArrayList<>();
            AnswerList<TestCaseExecutionQueue> answer1 = new AnswerList<>(testCaseExecutionInQueueList, testCaseExecutionInQueueList.size());
            answer1.setResultMessage(msg);
            return answer1;
        }
    }

    @Override
    public AnswerList<TestCaseExecutionQueue> readByVarious1(String tag, List<String> stateList, boolean withDependencies) throws CerberusException {
        return testCaseExecutionInQueueDAO.readByVarious1(tag, stateList, withDependencies);
    }

    @Override
    public AnswerList<TestCaseExecutionQueueToTreat> readQueueToTreat() throws CerberusException {
        List<String> stateList = new ArrayList<>();
        stateList.add(TestCaseExecutionQueue.State.QUEUED.name());
        return testCaseExecutionInQueueDAO.readByVarious2(stateList);
    }

    @Override
    public AnswerList<TestCaseExecutionQueueToTreat> readQueueRunning() throws CerberusException {
        List<String> stateList = new ArrayList<>();
        stateList.add(TestCaseExecutionQueue.State.WAITING.name());
        stateList.add(TestCaseExecutionQueue.State.STARTING.name());
        stateList.add(TestCaseExecutionQueue.State.EXECUTING.name());
        return testCaseExecutionInQueueDAO.readByVarious2(stateList);
    }

    @Override
    public AnswerList<TestCaseExecutionQueueToTreat> readQueueToTreatOrRunning() throws CerberusException {
        List<String> stateList = new ArrayList<>();
        stateList.add(TestCaseExecutionQueue.State.QUEUED.name());
        stateList.add(TestCaseExecutionQueue.State.WAITING.name());
        stateList.add(TestCaseExecutionQueue.State.STARTING.name());
        stateList.add(TestCaseExecutionQueue.State.EXECUTING.name());
        return testCaseExecutionInQueueDAO.readByVarious2(stateList);
    }

    @Override
    public AnswerList<TestCaseExecutionQueue> readQueueOpen(String tag) throws CerberusException {
        List<String> stateList = new ArrayList<>();
        stateList.add(TestCaseExecutionQueue.State.QUWITHDEP.name());
        stateList.add(TestCaseExecutionQueue.State.QUEUED.name());
        stateList.add(TestCaseExecutionQueue.State.QUWITHDEP_PAUSED.name());
        stateList.add(TestCaseExecutionQueue.State.QUEUED_PAUSED.name());
        stateList.add(TestCaseExecutionQueue.State.WAITING.name());
        stateList.add(TestCaseExecutionQueue.State.STARTING.name());
        stateList.add(TestCaseExecutionQueue.State.EXECUTING.name());
        return testCaseExecutionInQueueDAO.readByVarious1(tag, stateList, false);
    }

    @Override
    public AnswerList<TestCaseExecutionQueue> readByCriteria(int start, int amount, String column, String dir, String searchTerm, Map<String, List<String>> individualSearch) {
        return testCaseExecutionInQueueDAO.readByCriteria(start, amount, column, dir, searchTerm, individualSearch);
    }

    @Override
    public int getNbEntryToGo(long id, int prio) {
        return testCaseExecutionInQueueDAO.getNbEntryToGo(id, prio);
    }

    @Override
    public AnswerList<TestCaseExecutionQueue> readDistinctEnvCountryBrowserByTag(String tag) {
        return testCaseExecutionInQueueDAO.readDistinctEnvCountryBrowserByTag(tag);
    }

    @Override
    public AnswerList<String> readDistinctValuesByCriteria(String columnName, String sort, String searchParameter, Map<String, List<String>> individualSearch, String column) {
        return testCaseExecutionInQueueDAO.readDistinctValuesByCriteria(columnName, sort, searchParameter, individualSearch, column);
    }

    @Override
    public AnswerList findTagList(int tagnumber) {
        return testCaseExecutionInQueueDAO.findTagList(tagnumber);
    }

    @Override
    public AnswerList readBySystemByVarious(String system, List<String> testList, List<String> applicationList, List<String> tcstatusList, List<String> groupList, List<String> isActiveList, List<String> priorityList, List<String> targetMajorList, List<String> targetMinorList, List<String> creatorList, List<String> implementerList, List<String> buildList, List<String> revisionList, List<String> environmentList, List<String> countryList, List<String> browserList, List<String> tcestatusList, String ip, String port, String tag, String browserversion, String comment, String bugs, String ticket) {
        return testCaseExecutionInQueueDAO.readBySystemByVarious(system, testList, applicationList, tcstatusList, groupList, isActiveList, priorityList, targetMajorList,
                targetMinorList, creatorList, implementerList, buildList, revisionList, environmentList, countryList, browserList, tcestatusList,
                ip, port, tag, browserversion, comment, bugs, ticket);

    }

    @Override
    public String getUniqKey(String test, String testCase, String country, String env) {
        return "||" + test + "||" + testCase + "||" + country + "||" + env + "||";
    }

    @Override
    public AnswerItem<TestCaseExecutionQueue> create(TestCaseExecutionQueue object, boolean withNewDep, long exeQueueId, TestCaseExecutionQueue.State targetState, Map<String, TestCaseExecutionQueue> queueToInsert) {

        // We create the link between the tag and the system if it does not exist yet.
        tagSystemService.createIfNotExist(object.getTag(), object.getSystem(), object.getUsrCreated());

        AnswerItem<TestCaseExecutionQueue> ret;
        if (StringUtil.isEmptyOrNull(object.getTag())) {
            // If tag is not defined, we do not insert any dependencies.
            ret = testCaseExecutionInQueueDAO.create(object);
        } else {
//            if (withNewDep) {
            LOG.debug("Creating Queue entry : " + object.getId() + " targetState : " + targetState.toString());
            // Brand New execution Queue.
            // Inserting the record into the Queue forcing its state to QUWITHDEP (in order to secure it doesn't get triggered).
            object.setState(TestCaseExecutionQueue.State.QUWITHDEP);
            ret = testCaseExecutionInQueueDAO.create(object);
            // If insert was done correctly, we will try to add the dependencies.
            if (ret.getItem() != null) {
                // Get the QueueId Result from inserted record.
                long insertedQueueId = ret.getItem().getId();
                // Adding dependencies
                LOG.debug("With Dep : " + withNewDep);
                AnswerItem<Integer> retDep = testCaseExecutionQueueDepService.insertFromTestCaseDep(insertedQueueId, object.getEnvironment(), object.getCountry(),
                        object.getTag(), object.getTest(), object.getTestCase(), queueToInsert, withNewDep);
                LOG.debug("Dep inserted : " + retDep.getItem());
                if (retDep.getItem() < 1) {
                    // In case there are no dependencies, we release the execution moving to targetState State
                    updateToState(insertedQueueId, "", targetState);
                } else {
                    // In case there is at least 1 dependency, we leave the state to QUWITHDEP but move the prio to high so that when dependencies are released execution is triggered ASAP.
                    object.setPriority(TestCaseExecutionQueue.PRIORITY_WHENDEPENDENCY); // pass prio to 100 if it's a QUWITHDEP
                    updatePriority(insertedQueueId, TestCaseExecutionQueue.PRIORITY_WHENDEPENDENCY);
                }
            }
//            } else {
//                LOG.debug("Creating Queue entry : " + object.getId() + " From : " + exeQueueId + " targetState : " + targetState.toString());
//                // New execution Queue from an existing one (duplicated from an existing queue entry).
//                object.setState(targetState);
//                ret = testCaseExecutionInQueueDAO.create(object);
//                // We duplicate here the dependencies from the original exeQueue entry.
//                if (ret.getItem() != null) {
//                    // Get the QueueId Result from inserted record.
//                    long insertedQueueId = ret.getItem().getId();
//                    // Adding dependencies
//                    AnswerItem<Integer> retDep = testCaseExecutionQueueDepService.insertFromExeQueueIdDep(insertedQueueId, exeQueueId);
//                    LOG.debug("Dep inserted from old entries : " + retDep.getItem());
//                    // Move Original Queue entry to CANCELLED
//                    this.updateToCancelled(exeQueueId, "Cancelled because duplicated to new Queue entry " + insertedQueueId);
//                }
//            }
        }

        return ret;
    }

    @Override
    public boolean checkAndReleaseQueuedEntry(long exeQueueId, String tag) {
        boolean result = false;
        LOG.debug("Checking if we can move QUWITHDEP Queue entry to QUEUED of queue entry : " + exeQueueId);
        AnswerItem ansNbWaiting = testCaseExecutionQueueDepService.readNbWaitingByExeQueueId(exeQueueId);
        int nbwaiting = (int) ansNbWaiting.getItem();
        if (nbwaiting < 1) {
            // No more waiting dependencies.
            AnswerItem ansNbReleasedNOK = testCaseExecutionQueueDepService.readNbReleasedWithNOKByExeQueueId(exeQueueId);
            int nbReleasedNOK = (int) ansNbReleasedNOK.getItem();
            LOG.info("Queue entry " + exeQueueId + " can move from QUWITHDEP to QUEUED.");
            if (nbReleasedNOK <= 0) {
                // If all execution of RELEASED dep are OK, we update ExeQueue status from QUWITHDEP to QUEUED in order to allow queue entry to be executed.
                updateToQueuedFromQuWithDep(exeQueueId, "All Dependencies RELEASED.");
                result = true;
            } else {
                try {
                    // All dependencies of exeQueueId has been free but some with error that force to move the queue entry in ERROR status
                    String notExecutedMessage = nbReleasedNOK + " RELEASED dependency(ies) not OK.";
                    updateToErrorFromQuWithDep(exeQueueId, notExecutedMessage);
                    // We now trigger the check that the queue entry that move to ERROR status can release other dependencies
                    testCaseExecutionQueueDepService.manageDependenciesEndOfQueueExecution(exeQueueId);
                    // Tag execution could end here if that queue entry was the last one.
                    tagService.manageCampaignEndOfExecution(tag);
                } catch (CerberusException ex) {
                    LOG.error(ex.toString(), ex);
                }
            }
        }
        return result;
    }

    @Override
    public Answer update(TestCaseExecutionQueue object) {
        return testCaseExecutionInQueueDAO.update(object);
    }

    @Override
    public Answer updatePriority(long id, int priority) {
        return testCaseExecutionInQueueDAO.updatePriority(id, priority);
    }

    @Override
    public Answer updateComment(long id, String comment) {
        return testCaseExecutionInQueueDAO.updateComment(id, comment);
    }

    @Override
    public Answer updateToState(long id, String comment, TestCaseExecutionQueue.State targetState) {
        return testCaseExecutionInQueueDAO.updateToState(id, comment, targetState);
    }

    @Override
    public Answer updateToQueued(long id, String comment) {
        return testCaseExecutionInQueueDAO.updateToQueued(id, comment);
    }

    @Override
    public Answer updateAllTagToQueuedFromQuTemp(String tag, List<Long> queueIds) {
        return testCaseExecutionInQueueDAO.updateAllTagToQueuedFromQuTemp(tag, queueIds);
    }

    @Override
    public Answer updateToQueuedFromQuWithDep(long id, String comment) {
        return testCaseExecutionInQueueDAO.updateToQueuedFromQuWithDep(id, comment);
    }

    @Override
    public boolean updateToWaiting(final Long id) throws CerberusException {
        return testCaseExecutionInQueueDAO.updateToWaiting(id);
    }

    @Override
    public void updateToStarting(long id, String selectedRobot, String selectedRobotExt) throws CerberusException {
        testCaseExecutionInQueueDAO.updateToStarting(id, selectedRobot, selectedRobotExt);
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
    public void updateToErrorFromQuWithDep(long id, String comment) throws CerberusException {
        testCaseExecutionInQueueDAO.updateToErrorFromQuWithDep(id, comment);
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
    public Answer updateToErrorForce(long id, String comment) {
        return testCaseExecutionInQueueDAO.updateToErrorForce(id, comment);
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
    public void cancelRunningOldQueueEntries() {
        /**
         * Automatic Cancellation job. That Job force to CANCELLED queue entries
         * that still in Executing state and too old to be still running.
         */
        Integer timeout = parameterService.getParameterIntegerByKey("cerberus_automaticqueuecancellationjob_timeout", "", 3600);
        testCaseExecutionInQueueDAO.updateToCancelledOldRecord(timeout, "Cancelled by automatic job.");
    }

    @Override
    public AnswerItem<Integer> cancelPendingQueueEntries(String tag, String usrModif) {
        return testCaseExecutionInQueueDAO.updateToCancelledPendingRecord(tag, usrModif, "Cancelled by user " + usrModif);
    }

    @Override
    public AnswerItem<Integer> pausePendingQueueEntries(String tag, String usrModif) {
        return testCaseExecutionInQueueDAO.updateToPausedPendingRecord(tag, usrModif, "Paused by user " + usrModif);
    }

    @Override
    public AnswerItem<Integer> resumePausedQueueEntries(String tag, String usrModif) {
        return testCaseExecutionInQueueDAO.updateToNonPausedPendingRecord(tag, usrModif, "Resumed by user " + usrModif);
    }

    @Override
    public TestCaseExecutionQueue convert(AnswerItem<TestCaseExecutionQueue> answerItem) throws CerberusException {
        if (answerItem.isCodeEquals(MessageEventEnum.DATA_OPERATION_OK.getCode())) {
            //if the service returns an OK message then we can get the item
            return answerItem.getItem();
        }
        throw new CerberusException(new MessageGeneral(MessageGeneralEnum.DATA_OPERATION_ERROR));
    }

    @Override
    public List<TestCaseExecutionQueue> convert(AnswerList<TestCaseExecutionQueue> answerList) throws CerberusException {
        if (answerList.isCodeEquals(MessageEventEnum.DATA_OPERATION_OK.getCode())) {
            //if the service returns an OK message then we can get the item
            return answerList.getDataList();
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
        String robotDecli = testCaseExecutionInQueue.getRobotDecli();
        if (StringUtil.isEmptyOrNull(robotDecli)) {
            if (!StringUtil.isEmptyOrNull(browser)) {
                robotDecli = browser;
            } else {
                robotDecli = "";
            }
        }
        String version = testCaseExecutionInQueue.getBrowserVersion();
        String platform = testCaseExecutionInQueue.getPlatform();
        long start = testCaseExecutionInQueue.getRequestDate() != null ? testCaseExecutionInQueue.getRequestDate().getTime() : 0;
        long end = 0;
        String controlStatus = TestCaseExecution.CONTROLSTATUS_QU;
        String controlMessage = "Queued with State : " + testCaseExecutionInQueue.getState().name() + " - " + testCaseExecutionInQueue.getComment();
        if (testCaseExecutionInQueue.getState().name().equals(TestCaseExecutionQueue.State.WAITING.name())
                || testCaseExecutionInQueue.getState().name().equals(TestCaseExecutionQueue.State.QUEUED.name())
                || testCaseExecutionInQueue.getState().name().equals(TestCaseExecutionQueue.State.QUWITHDEP.name())
                || testCaseExecutionInQueue.getState().name().equals(TestCaseExecutionQueue.State.STARTING.name())) {
            controlStatus = TestCaseExecution.CONTROLSTATUS_QU;

        } else if (testCaseExecutionInQueue.getState().name().equals(TestCaseExecutionQueue.State.QUEUED_PAUSED.name())
                || testCaseExecutionInQueue.getState().name().equals(TestCaseExecutionQueue.State.QUWITHDEP_PAUSED.name())) {
            controlStatus = TestCaseExecution.CONTROLSTATUS_PA;

        } else {
            controlStatus = TestCaseExecution.CONTROLSTATUS_QE;
        }
        Application applicationObj = testCaseExecutionInQueue.getApplicationObj();
        String application = testCaseExecutionInQueue.getApplicationObj() != null ? testCaseExecutionInQueue.getApplicationObj().getApplication() : "";
        String robotHost = testCaseExecutionInQueue.getRobotIP();
        String robotPort = testCaseExecutionInQueue.getRobotPort();
        String tag = testCaseExecutionInQueue.getTag();
        int verbose = testCaseExecutionInQueue.getVerbose();
        int screenshot = testCaseExecutionInQueue.getScreenshot();
        int video = testCaseExecutionInQueue.getVideo();
        int pageSource = testCaseExecutionInQueue.getPageSource();
        int robotLog = testCaseExecutionInQueue.getRobotLog();
        int consoleLog = testCaseExecutionInQueue.getConsoleLog();
        int retry = testCaseExecutionInQueue.getRetries();
        boolean synchroneous = true;
        String timeout = testCaseExecutionInQueue.getTimeout();
        String outputFormat = "";
        TestCase tCase = testCaseExecutionInQueue.getTestCaseObj();
        int manualURL = (testCaseExecutionInQueue.getManualURL());
        String manualExecution = testCaseExecutionInQueue.getManualExecution();
        String myHost = testCaseExecutionInQueue.getManualHost();
        String myContextRoot = testCaseExecutionInQueue.getManualContextRoot();
        String myLoginRelativeURL = testCaseExecutionInQueue.getManualLoginRelativeURL();
        String myEnvData = testCaseExecutionInQueue.getManualEnvData();
        String seleniumIP = testCaseExecutionInQueue.getRobotIP();
        String seleniumPort = testCaseExecutionInQueue.getRobotPort();
        String description = "";
        if ((testCaseExecutionInQueue.getTestCaseObj() != null) && (testCaseExecutionInQueue.getTestCaseObj().getDescription() != null)) {
            description = testCaseExecutionInQueue.getTestCaseObj().getDescription();
        }
        TestCaseExecution result = factoryTestCaseExecution.create(0, test, testCase, description, null, null, environment, country, "", "", robotHost, robotPort, robotDecli,
                browser, version, platform,
                start, end, controlStatus, controlMessage, application, applicationObj, "", tag, verbose, screenshot, video, pageSource,
                robotLog, consoleLog, synchroneous, timeout, outputFormat, "", "", tCase, null, null, manualURL, myHost, myContextRoot, myLoginRelativeURL,
                myEnvData, seleniumIP, seleniumPort, null, null, null, retry, "", null, "", "", "", "", "", "", "", "", "", manualExecution, "", 0, 0, "", "", null, "", null);
        result.setQueueID(testCaseExecutionInQueue.getId());
        result.setQueueState(testCaseExecutionInQueue.getState().name());
        result.setId(testCaseExecutionInQueue.getExeId());
        return result;
    }

}
