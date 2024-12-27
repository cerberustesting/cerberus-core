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
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import javax.persistence.Entity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.json.JSONException;
import org.json.JSONObject;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import org.cerberus.core.crud.entity.TestCaseExecution;
import org.json.JSONArray;

/**
 * @author bcivel
 */
@Getter
@Setter
@Builder
@EqualsAndHashCode
@AllArgsConstructor
@NoArgsConstructor
public class QueueStatus {

    // Websocket data content
    private HashMap<String, TestCaseExecution> executionHashMap;
    private int running;
    private int queueSize;
    private int globalLimit;

    private long lastWebsocketPush;

    /**
     * Not included in table.
     */
    private static final Logger LOG = LogManager.getLogger(QueueStatus.class);

    public void setQueueCounters(int globalLimit, int running, int queueSize) {
        this.globalLimit = globalLimit;
        this.running = running;
        this.queueSize = queueSize;
    }

    public HashMap<String, TestCaseExecution> getExecutionUUIDList() {
        return executionHashMap;
    }

    public void setExecutionUUID(String UUID, TestCaseExecution execution) {
        executionHashMap.put(UUID, execution);
    }

    public long getLastWebsocketPush() {
        return lastWebsocketPush;
    }

    public void setLastWebsocketPush(long lastWebsocketPush) {
        this.lastWebsocketPush = lastWebsocketPush;
    }

    public JSONObject toJson(boolean fatVersion) {
        JSONObject queueJson = new JSONObject();
        try {
            JSONObject queueStats = new JSONObject();
            queueStats.put("globalLimit", this.getGlobalLimit());
            queueStats.put("running", this.getRunning());
            queueStats.put("queueSize", this.getQueueSize());
            queueJson.put("queueStats", queueStats);
            queueJson.put("queueTotal", executionHashMap.size());

            List<JSONObject> executionArray = new ArrayList<>();
//            JSONArray executionArray = new JSONArray();
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
            Collections.sort(executionArray, new SortExecutions());
            JSONArray object1 = new JSONArray(executionArray);
            queueJson.put("runningExecutionsList", object1);

            //queueStats.queueSize
        } catch (JSONException ex) {
            LOG.error(ex.toString(), ex);
        }
        return queueJson;
    }

    class SortExecutions implements Comparator<JSONObject> {

        // Used for sorting Triggers 
        @Override
        public int compare(JSONObject a, JSONObject b) {

            if (a != null && b != null) {
                Date dateA;
                Date dateB;
                try {
                    dateA = (Date) a.get("start");
                    dateB = (Date) b.get("start");
                    if (dateA.equals(dateB)) {

                    } else {
                        return (dateA.compareTo(dateB));
                    }
                } catch (JSONException ex) {
                    LOG.error("Exception on JSON Parse.", ex);
                }

            } else {
                return 1;
            }

            return 1;
        }
    }

}
