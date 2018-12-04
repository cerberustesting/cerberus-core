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

public class TestCaseExecutionQueueDep {

    private long id;
    private long exeQueueId;
    private String environment;
    private String country;
    private String tag;
    private String type;
    private String depTest;
    private String depTestCase;
    private String depEvent;
    private String status;
    private Timestamp releaseDate;
    private String comment;
    private long exeId;
    private String usrCreated;
    private Timestamp dateCreated;
    private String usrModif;
    private Timestamp dateModif;

    /**
     * From here are data outside database model.
     */
    public static final String TYPE_TCEXEEND = "TCEXEEND"; // End of a testCase Execution.
    public static final String TYPE_EVENT = "EVENT"; // Creation of an Event.

    public static final String STATUS_WAITING = "WAITING"; // End of a testCase Execution.
    public static final String STATUS_RELEASED = "RELEASED"; // Creation of an Event.

    private static final Logger LOG = LogManager.getLogger(TestCaseExecutionQueueDep.class);

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getExeQueueId() {
        return exeQueueId;
    }

    public void setExeQueueId(long exeQueueId) {
        this.exeQueueId = exeQueueId;
    }

    public String getEnvironment() {
        return environment;
    }

    public void setEnvironment(String environment) {
        this.environment = environment;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
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

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Timestamp getReleaseDate() {
        return releaseDate;
    }

    public void setReleaseDate(Timestamp releaseDate) {
        this.releaseDate = releaseDate;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public long getExeId() {
        return exeId;
    }

    public void setExeId(long exeId) {
        this.exeId = exeId;
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

}
