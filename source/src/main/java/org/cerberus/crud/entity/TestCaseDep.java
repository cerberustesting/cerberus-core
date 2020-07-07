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
    private String testCase;
    private String type;
    private String depTest;
    private String depTestCase;
    private String depEvent;
    private String active;
    private String description;
    private String usrCreated;
    private Timestamp dateCreated;
    private String usrModif;
    private Timestamp dateModif;
    private String depDescription;

    /**
     * Not included in table.
     */
    public static final String TYPE_TCEXEEND = "TCEXEEND"; // End of a testCase Execution.
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

    public String getTestCase() {
        return testCase;
    }

    public void setTestCase(String testCase) {
        this.testCase = testCase;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getDepTest() {
        return depTest;
    }

    public void setDepTest(String depTest) {
        this.depTest = depTest;
    }

    public String getDepTestCase() {
        return depTestCase;
    }

    public void setDepTestCase(String depTestCase) {
        this.depTestCase = depTestCase;
    }

    public String getDepEvent() {
        return depEvent;
    }

    public void setDepEvent(String depEvent) {
        this.depEvent = depEvent;
    }

    public String getActive() {
        return active;
    }

    public void setActive(String active) {
        this.active = active;
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

    public String getDepDescription() {
        return depDescription;
    }

    public void setDepDescription(String depDescription) {
        this.depDescription = depDescription;
    }

    public boolean hasSameKey(TestCaseDep tcd) {
        return this.getTest().equals(tcd.getTest())
                && this.getTestCase().equals(tcd.getTestCase())
                && this.getDepTest().equals(tcd.getDepTest())
                && this.getDepTestCase().equals(tcd.getDepTestCase());
    }

    public JSONObject toJson() {
        JSONObject testCaseDependencyJson = new JSONObject();
        try {
            testCaseDependencyJson.put("id", this.getId());
            testCaseDependencyJson.put("dependencyTest", this.getDepTest());
            testCaseDependencyJson.put("dependencyTestCase", this.getDepTestCase());
            testCaseDependencyJson.put("type", this.getType());
            testCaseDependencyJson.put("active", "Y".equals(this.getActive()));
            testCaseDependencyJson.put("dependencyDescription", this.getDepDescription());
            testCaseDependencyJson.put("event", this.getDepEvent());
        } catch (JSONException ex) {
            LOG.error(ex.toString(), ex);
        }
        return testCaseDependencyJson;
    }
}
