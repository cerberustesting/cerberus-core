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
package org.cerberus.core.engine.entity;

import java.sql.Timestamp;
import java.util.HashMap;
import javax.annotation.PostConstruct;
import org.cerberus.core.crud.entity.TestCaseExecution;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.stereotype.Component;

/**
 *
 * @author bcivel
 */
@Component
public class ExecutionUUID {

    private HashMap<String, TestCaseExecution> executionHashMap;
    private int running;
    private int queueSize;
    private int globalLimit;

    public int getRunning() {
        return running;
    }

    public int getQueueSize() {
        return queueSize;
    }

    public int getGlobalLimit() {
        return globalLimit;
    }

    public void setQueueCounters(int globalLimit, int running, int queueSize) {
        this.globalLimit = globalLimit;
        this.running = running;
        this.queueSize = queueSize;
    }

    @PostConstruct
    public void init() {
        executionHashMap = new HashMap<>();
        running = 0;
        queueSize = 0;
        globalLimit = 0;
    }

    public HashMap<String, TestCaseExecution> getExecutionUUIDList() {
        return executionHashMap;
    }

    public void setExecutionUUID(String UUID, TestCaseExecution execution) {
        executionHashMap.put(UUID, execution);
    }

    public void removeExecutionUUID(String uuid) {
        executionHashMap.remove(uuid);
    }

    public long getExecutionID(String uuid) {
        TestCaseExecution t = executionHashMap.get(uuid);
        return t.getId();
    }

    public TestCaseExecution getTestCaseExecution(String uuid) {
        return executionHashMap.get(uuid);
    }

    public int size() {
        return executionHashMap.size();
    }

    public JSONObject getRunningStatus() {
        JSONObject jsonResponse = new JSONObject();

        try {

            JSONArray executionArray = new JSONArray();
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
                executionArray.put(object);
            }
            jsonResponse.put("runningExecutionsList", executionArray);

            JSONObject queueStatus = new JSONObject();
            queueStatus.put("queueSize", queueSize);
            queueStatus.put("globalLimit", globalLimit);
            queueStatus.put("running", running);
            jsonResponse.put("queueStats", queueStatus);

            return jsonResponse;

        } catch (Exception ex) {
//            LOG.warn(ex, ex);
        }
        return jsonResponse;
    }
}
