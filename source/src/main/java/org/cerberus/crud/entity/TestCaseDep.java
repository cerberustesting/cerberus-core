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
package org.cerberus.crud.entity;

import java.sql.Timestamp;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * @author bcivel
 */
public class TestCaseDep {

    private long id;
    private String test;
    private String testcase;
    private String type;
    private String dependencyTest;
    private String dependencyTestcase;
    private String dependencyEvent;
    private boolean isActive;
    private String description;
    private String usrCreated;
    private Timestamp dateCreated;
    private String usrModif;
    private Timestamp dateModif;
    private String dependencyDescription;

    /**
     * Not included in table.
     */
    public static final String TYPE_TCEXEEND = "TCEXEEND"; // End of a testcase Execution.
    public static final String TYPE_EVENT = "EVENT"; // Creation of an Event.

    private static final Logger LOG = LogManager.getLogger(TestCaseDep.class);

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getTest() {
        return test;
    }

    public void setTest(String test) {
        this.test = test;
    }

    public String getTestcase() {
        return testcase;
    }

    public void setTestcase(String testcase) {
        this.testcase = testcase;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getDependencyTest() {
        return dependencyTest;
    }

    public void setDependencyTest(String dependencyTest) {
        this.dependencyTest = dependencyTest;
    }

    public String getDependencyTestcase() {
        return dependencyTestcase;
    }

    public void setDependencyTestcase(String dependencyTestcase) {
        this.dependencyTestcase = dependencyTestcase;
    }

    public String getDependencyEvent() {
        return dependencyEvent;
    }

    public void setDependencyEvent(String dependencyEvent) {
        this.dependencyEvent = dependencyEvent;
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean isActive) {
        this.isActive = isActive;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getUsrCreated() {
        return usrCreated;
    }

    public void setUsrCreated(String usrCreated) {
        this.usrCreated = usrCreated;
    }

    public Timestamp getDateCreated() {
        return dateCreated;
    }

    public void setDateCreated(Timestamp dateCreated) {
        this.dateCreated = dateCreated;
    }

    public String getUsrModif() {
        return usrModif;
    }

    public void setUsrModif(String usrModif) {
        this.usrModif = usrModif;
    }

    public Timestamp getDateModif() {
        return dateModif;
    }

    public void setDateModif(Timestamp dateModif) {
        this.dateModif = dateModif;
    }

    public String getDependencyDescription() {
        return dependencyDescription;
    }

    public void setDependencyDescription(String dependencyDescription) {
        this.dependencyDescription = dependencyDescription;
    }

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
            testCaseDependencyJson.put("type", this.getType());
            testCaseDependencyJson.put("isActive", this.isActive());
            testCaseDependencyJson.put("dependencyDescription", this.getDependencyDescription());
            testCaseDependencyJson.put("dependencyEvent", this.getDependencyEvent());
        } catch (JSONException ex) {
            LOG.error(ex.toString(), ex);
        }
        return testCaseDependencyJson;
    }

    @Override
    public String toString() {
        return "TestCaseDep{" + "id=" + id + ", test=" + test + ", testcase=" + testcase + ", type=" + type + ", dependencyTest=" + dependencyTest + ", dependencyTestcase=" + dependencyTestcase + ", dependencyEvent=" + dependencyEvent + ", isActive=" + isActive + ", description=" + description + ", usrCreated=" + usrCreated + ", dateCreated=" + dateCreated + ", usrModif=" + usrModif + ", dateModif=" + dateModif + ", dependencyDescription=" + dependencyDescription + '}';
    }
}
