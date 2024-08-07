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
package org.cerberus.core.crud.service.impl;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.cerberus.core.crud.dao.ITestCaseExecutionQueueDepDAO;
import org.cerberus.core.crud.entity.TestCaseExecution;
import org.cerberus.core.crud.entity.TestCaseExecutionQueue;
import org.cerberus.core.crud.entity.TestCaseExecutionQueueDep;
import org.cerberus.core.crud.service.ITestCaseExecutionQueueDepService;
import org.cerberus.core.crud.service.ITestCaseExecutionQueueService;
import org.cerberus.core.engine.entity.MessageGeneral;
import org.cerberus.core.enums.MessageEventEnum;
import org.cerberus.core.enums.MessageGeneralEnum;
import org.cerberus.core.exception.CerberusException;
import org.cerberus.core.util.answer.Answer;
import org.cerberus.core.util.answer.AnswerItem;
import org.cerberus.core.util.answer.AnswerList;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * @author bcivel
 */
@Service
public class TestCaseExecutionQueueDepService implements ITestCaseExecutionQueueDepService {

    @Autowired
    private ITestCaseExecutionQueueDepDAO testCaseExecutionQueueDepDAO;
    @Autowired
    private ITestCaseExecutionQueueService executionQueueService;

    private static final Logger LOG = LogManager.getLogger("TestCaseExecutionQueueDepService");

    private final String OBJECT_NAME = "Test Case Execution Queue Dependency";

    @Override
    public AnswerItem<Integer> insertFromTestCaseDep(long queueId, String env, String country, String tag, String test, String testcase) {
        return testCaseExecutionQueueDepDAO.insertFromTestCaseDep(queueId, env, country, tag, test, testcase);
    }

    @Override
    public AnswerItem<Integer> insertFromExeQueueIdDep(long queueId, long fromQueueId) {
        return testCaseExecutionQueueDepDAO.insertFromExeQueueIdDep(queueId, fromQueueId);
    }

    @Override
    public AnswerItem<Integer> updateStatusToRelease(String env, String Country, String tag, String type, String test, String testCase, String comment, long exeId, long queueId) {
        return testCaseExecutionQueueDepDAO.updateStatusToRelease(env, Country, tag, type, test, testCase, comment, exeId, queueId);
    }

    @Override
    public AnswerItem<Integer> updateStatusToRelease(long id) {
        return testCaseExecutionQueueDepDAO.updateStatusToRelease(id);
    }

    @Override
    public AnswerList<Long> readExeQueueIdByExeId(long exeId) {
        return testCaseExecutionQueueDepDAO.readExeQueueIdByExeId(exeId);
    }

    @Override
    public AnswerList<Long> readExeQueueIdByQueueId(long queueId) {
        return testCaseExecutionQueueDepDAO.readExeQueueIdByQueueId(queueId);
    }

    @Override
    public AnswerList<TestCaseExecutionQueueDep> readByExeQueueId(long exeQueueId) {
        return testCaseExecutionQueueDepDAO.readByExeQueueId(exeQueueId);
    }

    @Override
    public AnswerItem<Integer> readNbWaitingByExeQueueId(long exeQueueId) {
        return testCaseExecutionQueueDepDAO.readNbWaitingByExeQueueId(exeQueueId);
    }

    @Override
    public void loadDependenciesOnTestCaseExecution(List<TestCaseExecution> testCaseExecutions) throws CerberusException {
        HashMap<TestCaseExecution, List<TestCaseExecutionQueueDep>> dependenciesByTestCaseExecution = testCaseExecutionQueueDepDAO.readDependenciesByTestCaseExecution(testCaseExecutions);

        // modify directly the parameter variable
        for (Map.Entry<TestCaseExecution, List<TestCaseExecutionQueueDep>> entry : dependenciesByTestCaseExecution.entrySet()) {
            entry.getKey().setTestCaseExecutionQueueDepList(entry.getValue());
        }
    }

    @Override
    public AnswerItem<Integer> readNbReleasedWithNOKByExeQueueId(long exeQueueId) {
        return testCaseExecutionQueueDepDAO.readNbReleasedWithNOKByExeQueueId(exeQueueId);
    }

    @Override
    public AnswerList<TestCaseExecutionQueueDep> getWaitingDepReadytoRelease() {
        return testCaseExecutionQueueDepDAO.getWaitingDepReadytoRelease();
    }

    @Override
    public AnswerItem<Integer> insertNewTimingDep(String env, String Country, String tag, String test, String testCase, long exeID) {
        return testCaseExecutionQueueDepDAO.insertNewTimingDep(env, Country, tag, test, testCase, exeID);
    }

    @Override
    public void manageDependenciesEndOfExecution(TestCaseExecution tCExecution) {
        if (tCExecution != null) {
            LOG.debug("Release dependencies of Execution : " + tCExecution.getId() + ".");

            // Insert new dependency based on TIMING in case the initial dependency has a delay. Type TIMING Status WAITING
            // TODO
            AnswerItem ansNbDep = insertNewTimingDep(tCExecution.getEnvironment(), tCExecution.getCountry(), tCExecution.getTag(),
                    tCExecution.getTest(), tCExecution.getTestCase(), tCExecution.getId());

            // Updating all dependencies of type TCEEXEEND and tCExecution.getId() to RELEASED.
            ansNbDep = updateStatusToRelease(tCExecution.getEnvironment(), tCExecution.getCountry(), tCExecution.getTag(),
                    TestCaseExecutionQueueDep.TYPE_TCEXEEND, tCExecution.getTest(), tCExecution.getTestCase(), "", tCExecution.getId(), tCExecution.getQueueID());
            int nbdep = (int) ansNbDep.getItem();
            // Only check status of each Queue Entries if we RELEASED at least 1 entry.
            if (nbdep > 0) {
                // Getting the list of impacted Queue Entries where we released dependencies.
                List<Long> al = new ArrayList<>();
                AnswerList<Long> ansQueueId = readExeQueueIdByExeId(tCExecution.getId());
                al = ansQueueId.getDataList();
                // For each exequeue entry we just updated, we move status from QUWITHDEP to QUEUED in case there are no more WAITING dependency.
                for (Long long1 : al) {
                    executionQueueService.checkAndReleaseQueuedEntry(long1, tCExecution.getTag());
                }
            }
        }
    }

    @Override
    public void manageDependenciesEndOfQueueExecution(long idQueue) {
        LOG.debug("Release dependencies of Queue : " + idQueue + ".");

        try {
            //, String environment, String country, String tag, String test, String testCase
            TestCaseExecutionQueue queueEntry = executionQueueService.convert(executionQueueService.readByKey(idQueue, false));

            // Updating all dependencies of type TCEEXEEND and tCExecution.getId() to RELEASED.
            AnswerItem ansNbDep = updateStatusToRelease(queueEntry.getEnvironment(), queueEntry.getCountry(), queueEntry.getTag(),
                    TestCaseExecutionQueueDep.TYPE_TCEXEEND, queueEntry.getTest(), queueEntry.getTestCase(), "Queue Entry " + idQueue + " in ERROR.", 0, idQueue);
            int nbdep = (int) ansNbDep.getItem();
            // Only check status of each Queue Entries if we RELEASED at least 1 entry.
            if (nbdep > 0) {
                // Getting the list of impacted Queue Entries where we released dependencies.
                List<Long> al = new ArrayList<>();
                AnswerList<Long> ansQueueId = readExeQueueIdByQueueId(idQueue);
                al = ansQueueId.getDataList();
                // For each exequeue entry we just updated, we move status from QUWITHDEP to QUEUED in case there are no more WAITING dependency.
                for (Long long1 : al) {
                    executionQueueService.checkAndReleaseQueuedEntry(long1, queueEntry.getTag());
                }
            }
        } catch (CerberusException ex) {
            LOG.error("Exception when release dep from Queue Error.", ex);
        }
    }

    @Override
    public int manageDependenciesCheckTimingWaiting() {
        LOG.debug("Release dependencies based on TIMING");
        int nbReleased = 0;

        try {
            //, String environment, String country, String tag, String test, String testCase
            // Get list of queuedep to release.
            AnswerList<TestCaseExecutionQueueDep> depList = getWaitingDepReadytoRelease();
            if (depList.getTotalRows() > 0) {
                LOG.info(depList.getTotalRows() + " WAITING dependency(ies) is(are) now ready to be released.");

                List<TestCaseExecutionQueueDep> listToProcess = new ArrayList<>();
                listToProcess = depList.getDataList();

                // Release them by update the WAITING queuedep to RELEASED
                for (TestCaseExecutionQueueDep itemToProces : listToProcess) {
                    LOG.debug("Process Queue Entry dep id : " + itemToProces.getId());
                    updateStatusToRelease(itemToProces.getId());
                }

                // Loop on queue id and checkAndReleaseQueuedEntry on the tag
                for (TestCaseExecutionQueueDep itemToProces : listToProcess) {
                    if (executionQueueService.checkAndReleaseQueuedEntry(itemToProces.getExeQueueId(), itemToProces.getTag())) {
                        nbReleased++;
                    }
                }

            }

        } catch (Exception ex) {
            LOG.error("Exception when release TIMING dependencies.", ex);
        }
        return nbReleased;
    }

    @Override
    public TestCaseExecutionQueueDep convert(AnswerItem<TestCaseExecutionQueueDep> answerItem) throws CerberusException {
        if (answerItem.isCodeEquals(MessageEventEnum.DATA_OPERATION_OK.getCode())) {
            //if the service returns an OK message then we can get the item
            return answerItem.getItem();
        }
        throw new CerberusException(new MessageGeneral(MessageGeneralEnum.DATA_OPERATION_ERROR));
    }

    @Override
    public List<TestCaseExecutionQueueDep> convert(AnswerList<TestCaseExecutionQueueDep> answerList) throws CerberusException {
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
    public List<Long> enrichWithDependencies(List<Long> queueIdList) {

        try {
            // Loading list of labelId into a map in order to dedup it.
            HashMap<Long, Long> finalMap = new HashMap<>();
            HashMap<Long, Long> initMap = new HashMap<>();
            // Dedup list on a MAP
            for (Long queueId : queueIdList) {
                finalMap.put(queueId, Long.valueOf(0));
                initMap.put(queueId, Long.valueOf(0));
            }

            // Looping of each queueId and add the parent.
            Integer initSize = initMap.size();
            Integer finalSize = initSize;
            Integer i = 0;
            do {
                // Copy FinalMap to InitMap.
                for (Map.Entry<Long, Long> entry : finalMap.entrySet()) {
                    Long key = entry.getKey();
                    initMap.put(key, Long.valueOf(0));
                }
                // Save the size if InitMap
                initSize = initMap.size();
                // For each InitMap, we add the dependency.
                for (Map.Entry<Long, Long> entry : initMap.entrySet()) {
                    Long key = entry.getKey();
                    // Loading from database the list of links from parent to childs.
                    List<TestCaseExecutionQueueDep> queueIdLinkList = this.convert(this.readByExeQueueId(key));
                    // for each dependency found, we add the dependency to the FinalMap.
                    for (TestCaseExecutionQueueDep queueDepEntry : queueIdLinkList) {
                        finalMap.put(queueDepEntry.getQueueId(), Long.valueOf(0));
                    }
                }
                finalSize = finalMap.size();
                i++;
                LOG.debug(initSize + " " + finalSize);
            } while (!Objects.equals(finalSize, initSize) && i < 50);

            // Convert Map to List.
            List<Long> finalList = new ArrayList<>();
            for (Map.Entry<Long, Long> entry : finalMap.entrySet()) {
                Long key = entry.getKey();
                finalList.add(key);
            }
            return finalList;

        } catch (CerberusException ex) {
            LOG.error("Exception when enriching Labels with Child.", ex);
        }
        return new ArrayList<>();
    }

}
