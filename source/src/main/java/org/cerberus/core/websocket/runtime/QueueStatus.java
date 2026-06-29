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
package org.cerberus.core.websocket.runtime;

import jakarta.annotation.PostConstruct;
import lombok.Getter;
import lombok.Setter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.cerberus.core.crud.entity.TestCaseExecution;
import org.cerberus.core.crud.service.ITestCaseExecutionQueueService;
import org.cerberus.core.crud.service.impl.ParameterService;
import org.cerberus.core.engine.queuemanagement.entity.TestCaseExecutionQueueToTreat;
import org.cerberus.core.exception.CerberusException;
import org.cerberus.core.util.answer.AnswerList;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.sql.Timestamp;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Component
@Getter
@Setter
public class QueueStatus {

    private static final Logger LOG = LogManager.getLogger(QueueStatus.class);

    private final ITestCaseExecutionQueueService testCaseExecutionQueueService;

    @Autowired
    ParameterService parameterService;

    // Websocket data content / runtime state
    private List<TestCaseExecutionQueueToTreat> queueToTreat = Collections.emptyList();
    private Map<String, TestCaseExecution> executionHashMap = new ConcurrentHashMap<>();
    private int running;
    private int queueSize;
    private int globalLimit;

    public QueueStatus(ITestCaseExecutionQueueService testCaseExecutionQueueService) {
        this.testCaseExecutionQueueService = testCaseExecutionQueueService;
    }

    public int getRunning(){
        return executionHashMap.size();
    }

    @PostConstruct
    public void init() {

        refreshQueueToTreat();
        globalLimit = parameterService.getParameterIntegerByKey("cerberus_queueexecution_global_threadpoolsize", "", 12);
        queueSize = queueToTreat.size();
    }

    public void refreshQueueToTreat() {
        try {
            LOG.info("Retrieve tests in queue.");

            AnswerList<TestCaseExecutionQueueToTreat> answer =
                    testCaseExecutionQueueService.readQueueToTreat();

            if (answer == null || answer.getDataList() == null) {
                queueToTreat = Collections.emptyList();
            } else {
                queueToTreat = answer.getDataList();
            }

            queueSize = queueToTreat.size();

        } catch (CerberusException ex) {
            LOG.error(ex.toString(), ex);
            queueToTreat = Collections.emptyList();
            queueSize = 0;
        }
    }

    public List<TestCaseExecutionQueueToTreat> myQueueToTreat(String user){
        return queueToTreat
                .stream()
                .filter(execution -> Objects.equals(user, execution.getUsrCreated()))
                .collect(Collectors.toList());
    }

    public void updateRunning(int running) {
        this.running = running;
    }

    public JSONObject toJson(boolean fatVersion) {
        JSONObject queueJson = new JSONObject();

        try {
            JSONObject queueStats = new JSONObject();
            queueStats.put("globalLimit", globalLimit);
            queueStats.put("running", running);
            queueStats.put("queueSize", queueToTreat.size());

            queueJson.put("queueStats", queueStats);
            queueJson.put("queueTotal", queueToTreat.size());

            JSONArray executionsInQueue = new JSONArray();
            if (queueToTreat != null) {
                for (TestCaseExecutionQueueToTreat ex : queueToTreat) {
                    JSONObject object = new JSONObject();
                    object.put("id", ex.getId());
                    object.put("tag", ex.getTag());
                    object.put("test", ex.getTest());
                    object.put("testCase", ex.getTestCase());
                    object.put("system", ex.getSystem());
                    object.put("application", ex.getApplication());
                    object.put("environment", ex.getEnvironment());
                    object.put("country", ex.getCountry());
                    object.put("robotIP", ex.getQueueRobotHost());
                    object.put("usrCreated", ex.getUsrCreated());
                    object.put("queuePosition", ex.getQueuePosition());
                    object.put("nbEntryBefore", ex.getNbEntryBefore());
                    executionsInQueue.put(object);
                }
            }

            List<JSONObject> executionArray = new ArrayList<>();
            if (executionHashMap != null) {
                for (Object ex : executionHashMap.values()) {
                    TestCaseExecution execution = (TestCaseExecution) ex;
                    JSONObject object = new JSONObject();
                    object.put("id", execution.getId());
                    object.put("test", execution.getTest());
                    object.put("testcase", execution.getTestCase());
                    object.put("system", execution.getApplicationObj().getSystem());
                    object.put("application", execution.getApplication());
                    object.put("environment", execution.getEnvironmentData());
                    object.put("country", execution.getCountry());
                    object.put("robotIP", execution.getSeleniumIP());
                    object.put("tag", execution.getTag());
                    object.put("start", new Timestamp(execution.getStart()));
                    executionArray.add(object);
                }
            }
            queueJson.put("runningExecutionsList", executionArray);

        } catch (JSONException ex) {
            LOG.error(ex.toString(), ex);
        }

        return queueJson;
    }
}