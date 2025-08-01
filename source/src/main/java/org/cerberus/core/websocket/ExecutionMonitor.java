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
package org.cerberus.core.websocket;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import javax.annotation.PostConstruct;
import org.json.JSONException;
import org.json.JSONObject;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import org.cerberus.core.crud.entity.TestCaseExecutionLight;
import org.springframework.stereotype.Component;

/**
 * @author vertigo17
 */
@Component
public class ExecutionMonitor {

    static private String SEPARATOR = "-";
    // Websocket data content
    private HashMap<String, List<TestCaseExecutionLight>> executionHashMap;
    private HashMap<String, JSONObject> testcaseMap;
    private HashMap<String, JSONObject> countryEnvRobotMap;

    private long lastWebsocketPush;

    private long initExeLoad;

    @PostConstruct
    public void init() {
        executionHashMap = new HashMap<>();
        testcaseMap = new HashMap<>();
        countryEnvRobotMap = new HashMap<>();
        lastWebsocketPush = 0;
        initExeLoad = 0;
        LOG.info("Monitor component build.");
    }

    /**
     * Not included in table.
     */
    private static final Logger LOG = LogManager.getLogger(ExecutionMonitor.class);

    public HashMap<String, List<TestCaseExecutionLight>> getExecutionHashMap() {
        return executionHashMap;
    }

    public void setExecutionHashMap(HashMap<String, List<TestCaseExecutionLight>> executionHashMap) {
        this.executionHashMap = executionHashMap;
    }

    public long getLastWebsocketPush() {
        return lastWebsocketPush;
    }

    public void setLastWebsocketPush(long lastWebsocketPush) {
        this.lastWebsocketPush = lastWebsocketPush;
    }

    public long getInitExeLoad() {
        return initExeLoad;
    }

    public void setInitExeLoad(long initExeLoad) {
        this.initExeLoad = initExeLoad;
    }

    public HashMap<String, JSONObject> getTestcaseMap() {
        return testcaseMap;
    }

    public void setTestcaseMap(HashMap<String, JSONObject> testcaseMap) {
        this.testcaseMap = testcaseMap;
    }

    public HashMap<String, JSONObject> getCountryEnvRobotMap() {
        return countryEnvRobotMap;
    }

    public void setCountryEnvRobotMap(HashMap<String, JSONObject> countryEnvRobotMap) {
        this.countryEnvRobotMap = countryEnvRobotMap;
    }

    public void addNewExecutionToMonitor(TestCaseExecutionLight newexecution) {
        // Calculate agregation keys
        String key = newexecution.getTest() + SEPARATOR + newexecution.getTestCase() + SEPARATOR + newexecution.getCountry() + SEPARATOR + newexecution.getEnvironment() + SEPARATOR + newexecution.getRobot();
        String keyTest = newexecution.getTest() + SEPARATOR + newexecution.getTestCase();
        String keyEnv = newexecution.getCountry() + SEPARATOR + newexecution.getEnvironment() + SEPARATOR + newexecution.getRobot();

        // Maintain Key TestCase
        JSONObject newTest;
        if (testcaseMap.containsKey(keyTest)) {
            newTest = testcaseMap.get(keyTest);
            newTest.put("nb", newTest.getInt("nb") + 1);
            testcaseMap.put(keyTest, newTest);
        } else {
            newTest = new JSONObject();
            newTest.put("nb", 1);
            newTest.put("test", newexecution.getTest());
            newTest.put("testcase", newexecution.getTestCase());
            newTest.put("application", newexecution.getApplication());
            newTest.put("description", newexecution.getDescription());
            newTest.put("system", newexecution.getSystem());
            testcaseMap.put(keyTest, newTest);
        }
        // Maintain Key Env
        JSONObject newEnv;
        if (countryEnvRobotMap.containsKey(keyEnv)) {
            newEnv = countryEnvRobotMap.get(keyEnv);
            newEnv.put("nb", newTest.getInt("nb") + 1);
            countryEnvRobotMap.put(keyEnv, newEnv);
        } else {
            newEnv = new JSONObject();
            newEnv.put("nb", 1);
            newEnv.put("env", newexecution.getEnvironment());
            newEnv.put("envData", newexecution.getEnvironmentData());
            newEnv.put("country", newexecution.getCountry());
            countryEnvRobotMap.put(keyEnv, newEnv);
        }

        // Maintain Execution tile
        if (this.getExecutionHashMap().containsKey(key)) {
            List<TestCaseExecutionLight> existingList = this.getExecutionHashMap().get(key);
            existingList.add(newexecution);
            // If old execution list is too big, we remove the oldest one.
            if (existingList.size() > 10) {
                existingList.remove(0);

                // Remove counter from countryEnvRobotMap
                newEnv = countryEnvRobotMap.get(keyEnv);
                newEnv.put("nb", newTest.getInt("nb") - 1);
                countryEnvRobotMap.put(keyEnv, newEnv);

                // Remove counter from testcaseMap
                newTest = testcaseMap.get(keyTest);
                newTest.put("nb", newTest.getInt("nb") - 1);
                testcaseMap.put(keyTest, newTest);

            }
            this.getExecutionHashMap().put(key, existingList);
        } else {
            List<TestCaseExecutionLight> newList = new ArrayList<>();
            newList.add(newexecution);
            this.getExecutionHashMap().put(key, newList);
        }
    }

    public JSONObject toJson(boolean fatVersion) {
        JSONObject result = new JSONObject();

        try {

            result.put("executions", executionHashMap);
            result.put("tests", testcaseMap);
            result.put("environments", countryEnvRobotMap);

            //queueStats.queueSize
        } catch (JSONException ex) {
            LOG.error(ex, ex);
        } catch (Exception ex) {
            LOG.error(ex, ex);
        }
        return result;
    }

//    class SortExecutions implements Comparator<JSONObject> {
//
//        // Used for sorting Triggers 
//        @Override
//        public int compare(JSONObject a, JSONObject b) {
//
//            if (a != null && b != null) {
//                Date dateA;
//                Date dateB;
//                try {
//                    dateA = (Date) a.get("start");
//                    dateB = (Date) b.get("start");
//                    if (dateA.equals(dateB)) {
//
//                    } else {
//                        return (dateA.compareTo(dateB));
//                    }
//                } catch (JSONException ex) {
//                    LOG.error("Exception on JSON Parse.", ex);
//                }
//
//            } else {
//                return 1;
//            }
//
//            return 1;
//        }
//    }
}
