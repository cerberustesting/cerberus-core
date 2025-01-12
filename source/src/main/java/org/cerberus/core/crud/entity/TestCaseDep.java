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
package org.cerberus.core.crud.entity;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONException;
import org.json.JSONObject;

import java.sql.Timestamp;
import lombok.Builder;

/**
 * @author bcivel
 */
@Getter
@Setter
@EqualsAndHashCode
@ToString
@Builder
public class TestCaseDep {

    private long id;
    private String test;
    private String testcase;
    private String type;
    private String dependencyTest;
    private String dependencyTestcase;
    private String dependencyEvent;
    private Integer dependencyTCDelay;
    private boolean isActive;
    private String description;
    @EqualsAndHashCode.Exclude
    private String usrCreated;
    @EqualsAndHashCode.Exclude
    private Timestamp dateCreated;
    @EqualsAndHashCode.Exclude
    private String usrModif;
    @EqualsAndHashCode.Exclude
    private Timestamp dateModif;

    /**
     * Not included in table.
     */
    @EqualsAndHashCode.Exclude
    private String testcaseDescription;

    public static final String TYPE_TCEXEEND = "TCEXEEND"; // End of a testCase Execution any status.
    public static final String TYPE_TCEXEENDOK = "TCEXEENDOK"; // End of a testCase Execution only OK.
    public static final String TYPE_EVENT = "EVENT"; // Creation of an Event.
    public static final String TYPE_TIMING = "TIMING"; // Waiting for a specific timing.

    private static final Logger LOG = LogManager.getLogger(TestCaseDep.class);

    public boolean hasSameKey(TestCaseDep tcd) {
        return this.getTest().equals(tcd.getTest())
                && this.getTestcase().equals(tcd.getTestcase())
                && this.getDependencyTest().equals(tcd.getDependencyTest())
                && this.getDependencyTestcase().equals(tcd.getDependencyTestcase());
    }

    public JSONObject toJson() {
        JSONObject testCaseDependencyJson = new JSONObject();
        try {
            testCaseDependencyJson.put("id", this.getId());
            testCaseDependencyJson.put("dependencyTest", this.getDependencyTest());
            testCaseDependencyJson.put("dependencyTestcase", this.getDependencyTestcase());
            testCaseDependencyJson.put("dependencyTCDelay", this.getDependencyTCDelay());
            testCaseDependencyJson.put("type", this.getType());
            testCaseDependencyJson.put("isActive", this.isActive());
            testCaseDependencyJson.put("description", this.getDescription());
            testCaseDependencyJson.put("testcaseDescription", this.getTestcaseDescription());
            testCaseDependencyJson.put("dependencyEvent", this.getDependencyEvent());
        } catch (JSONException ex) {
            LOG.error(ex.toString(), ex);
        }
        return testCaseDependencyJson;
    }

    public JSONObject toJsonV001() {
        JSONObject testCaseDependencyJson = new JSONObject();
        try {
            testCaseDependencyJson.put("JSONVersion", "001");
            testCaseDependencyJson.put("id", this.getId());
            testCaseDependencyJson.put("dependencyTestFolder", this.getDependencyTest());
            testCaseDependencyJson.put("dependencyTestcase", this.getDependencyTestcase());
            testCaseDependencyJson.put("dependencyTCDelay", this.getDependencyTCDelay());
            testCaseDependencyJson.put("type", this.getType());
            testCaseDependencyJson.put("isActive", this.isActive());
            testCaseDependencyJson.put("description", this.getDescription());
            testCaseDependencyJson.put("dependencyEvent", this.getDependencyEvent());
        } catch (JSONException ex) {
            LOG.error(ex.toString(), ex);
        }
        return testCaseDependencyJson;
    }
}
