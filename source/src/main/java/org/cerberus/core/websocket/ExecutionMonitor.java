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
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import javax.annotation.PostConstruct;
import org.json.JSONException;
import org.json.JSONObject;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import org.cerberus.core.crud.entity.TestCaseExecutionLight;
import org.cerberus.core.util.StringUtil;
import org.springframework.stereotype.Component;

/**
 * @author vertigo17
 */
@Component
public class ExecutionMonitor {

    private static final String SEPARATOR = "-";
    private static final int MAXEXECUTIONEXELIST = 10;
    // Websocket data content
    private HashMap<String, List<Long>> executionBoxHashMap;
    private HashMap<Long, TestCaseExecutionLight> executionHashMap;

    private long lastWebsocketPush;
    private boolean needPush;

    @PostConstruct
    public void init() {
        executionHashMap = new HashMap<>();
        executionBoxHashMap = new HashMap<>();
        lastWebsocketPush = new Date().getTime();
        needPush = false;
        LOG.info("Monitor component build.");
    }

    /**
     * Not included in table.
     */
    private static final Logger LOG = LogManager.getLogger(ExecutionMonitor.class);

    public HashMap<String, List<Long>> getExecutionBoxHashMap() {
        return executionBoxHashMap;
    }

    public void setExecutionBoxHashMap(HashMap<String, List<Long>> executionBoxHashMap) {
        this.executionBoxHashMap = executionBoxHashMap;
    }

    public HashMap<Long, TestCaseExecutionLight> getExecutionHashMap() {
        return executionHashMap;
    }

    public void setExecutionHashMap(HashMap<Long, TestCaseExecutionLight> executionHashMap) {
        this.executionHashMap = executionHashMap;
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
        executionHashMap.get(executionId).setFalseNegative(isFalseNegative);
    }

    public void addNewExecutionToMonitor(TestCaseExecutionLight newexecution) {

        // Adding execution to main Map
        executionHashMap.put(newexecution.getId(), newexecution);

        // Calculate agregation keys
        String key = StringUtil.cleanFromSpecialCharacters(newexecution.getTest()) + SEPARATOR
                + StringUtil.cleanFromSpecialCharacters(newexecution.getTestCase()) + SEPARATOR
                + StringUtil.cleanFromSpecialCharacters(newexecution.getCountry()) + SEPARATOR
                + StringUtil.cleanFromSpecialCharacters(newexecution.getEnvironment()) + SEPARATOR
                + StringUtil.cleanFromSpecialCharacters(newexecution.getRobot());
        String keyTest = StringUtil.cleanFromSpecialCharacters(newexecution.getTest()) + SEPARATOR
                + StringUtil.cleanFromSpecialCharacters(newexecution.getTestCase());
        String keyEnv = StringUtil.cleanFromSpecialCharacters(newexecution.getCountry()) + SEPARATOR
                + StringUtil.cleanFromSpecialCharacters(newexecution.getEnvironment()) + SEPARATOR
                + StringUtil.cleanFromSpecialCharacters(newexecution.getRobot());

        // Maintain Execution tile
        if (this.getExecutionBoxHashMap().containsKey(key)) {
            List<Long> existingList = this.getExecutionBoxHashMap().get(key);
            existingList.add(newexecution.getId());
            // If old execution list is too big, we remove the oldest one.
            if (existingList.size() > MAXEXECUTIONEXELIST) {
                executionHashMap.remove(existingList.get(0));
                existingList.remove(0);

            }
            this.getExecutionBoxHashMap().put(key, existingList);
        } else {
            List<Long> newList = new ArrayList<>();
            newList.add(newexecution.getId());
            this.getExecutionBoxHashMap().put(key, newList);
        }
    }

    public JSONObject toJson(boolean fatVersion) {
        JSONObject result = new JSONObject();

        try {

            result.put("executions", executionHashMap);
            result.put("executionBoxes", executionBoxHashMap);
            JSONObject wsTiming = new JSONObject();
            wsTiming.put("lastPush", lastWebsocketPush);
            wsTiming.put("needPush", needPush);
            result.put("wsTiming", wsTiming);

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
