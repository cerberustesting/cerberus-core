/**
 * Cerberus Copyright (C) 2013 - 2026 cerberustesting
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

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import org.cerberus.core.crud.entity.TestCaseExecutionLight;
import org.cerberus.core.crud.service.ITestCaseExecutionService;
import org.cerberus.core.exception.CerberusException;
import org.cerberus.core.util.StringUtil;

@Component
public class ExecutionMonitor {

    private static final String SEPARATOR = "-";
    private static final int MAXEXECUTIONEXELIST = 10;
    private static final Logger LOG = LogManager.getLogger(ExecutionMonitor.class);

    // Remplacement par ConcurrentHashMap pour supporter les accès concurrents
    private final Map<String, List<Long>> executionBoxHashMap = new ConcurrentHashMap<>();
    private final Map<Long, TestCaseExecutionLight> executionHashMap = new ConcurrentHashMap<>();

    private volatile long lastWebsocketPush;
    private volatile boolean needPush;

    @Autowired
    private ITestCaseExecutionService testCaseExecutionService;

    @PostConstruct
    public void init() {
        try {
            LOG.info("Monitor component build.");
            lastWebsocketPush = System.currentTimeMillis();
            needPush = false;

            LOG.debug("Loading last executions in order to init the monitor class component from oldest to newest.");
            List<TestCaseExecutionLight> lastExecutions = testCaseExecutionService.ReadLastExecutionForMonitor();
            if (lastExecutions != null && !lastExecutions.isEmpty()) {
                for (int i = lastExecutions.size(); i > 0; i--) {
                    this.addNewExecutionToMonitor(lastExecutions.get(i - 1));
                }
                this.setNeedPush(true);
            }
        } catch (CerberusException ex) {
            LOG.error(ex, ex);
        }
    }

    public Map<String, List<Long>> getExecutionBoxHashMap() {
        return executionBoxHashMap;
    }

    public Map<Long, TestCaseExecutionLight> getExecutionHashMap() {
        return executionHashMap;
    }

    public long getLastWebsocketPush() {
        return lastWebsocketPush;
    }

    public void setLastWebsocketPush(long lastWebsocketPush) {
        this.lastWebsocketPush = lastWebsocketPush;
    }

    public boolean isNeedPush() {
        return needPush;
    }

    public void setNeedPush(boolean needPush) {
        this.needPush = needPush;
    }

    public void updateExecutionToMonitor(long executionId, boolean isFalseNegative) {
        TestCaseExecutionLight exec = executionHashMap.get(executionId);
        if (exec != null) {
            exec.setFalseNegative(isFalseNegative);
        }
    }

    public synchronized void addNewExecutionToMonitor(TestCaseExecutionLight newexecution) {
        // Adding execution to main Map
        executionHashMap.put(newexecution.getId(), newexecution);

        // Calculate agregation keys
        String key = StringUtil.cleanFromSpecialCharacters(newexecution.getTest()) + SEPARATOR
                + StringUtil.cleanFromSpecialCharacters(newexecution.getTestCase()) + SEPARATOR
                + StringUtil.cleanFromSpecialCharacters(newexecution.getCountry()) + SEPARATOR
                + StringUtil.cleanFromSpecialCharacters(newexecution.getEnvironment()) + SEPARATOR
                + StringUtil.cleanFromSpecialCharacters(newexecution.getRobot());

        // Use computeIfAbsent in order to init the list in atomic way with CopyOnWriteArrayList
        List<Long> existingList = executionBoxHashMap.computeIfAbsent(key, k -> new CopyOnWriteArrayList<>());
        existingList.add(newexecution.getId());

        // Si la liste devient trop grande, suppression de la plus ancienne entrée
        if (existingList.size() > MAXEXECUTIONEXELIST) {
            Long removedId = existingList.remove(0);
            if (removedId != null) {
                executionHashMap.remove(removedId);
            }
        }
    }

    public JSONObject toJson(boolean fatVersion) {
        JSONObject result = new JSONObject();

        try {
            // Snapshots copies creation in order to instanciate the JSONObject without concurancy
            Map<Long, TestCaseExecutionLight> executionsSnapshot = new HashMap<>(executionHashMap);
            Map<String, List<Long>> boxesSnapshot = new HashMap<>();

            // Copie profonde des listes
            executionBoxHashMap.forEach((k, v) -> boxesSnapshot.put(k, new ArrayList<>(v)));

            result.put("executions", executionsSnapshot);
            result.put("executionBoxes", boxesSnapshot);

            JSONObject wsTiming = new JSONObject();
            wsTiming.put("lastPush", lastWebsocketPush);
            wsTiming.put("needPush", needPush);
            result.put("wsTiming", wsTiming);

        } catch (JSONException ex) {
            LOG.error(ex, ex);
        } catch (Exception ex) {
            LOG.error(ex, ex);
        }
        return result;
    }
}
