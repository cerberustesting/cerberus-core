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

import java.sql.Timestamp;

/**
 *
 * @author cdelage
 */
public class ScheduledExecution {

    private long ID; // to convert in long
    private long schedulerId;
    private String scheduleName;
    private String status;
    private String comment;
    private String usrCreated;
    private String usrModif;

    private Timestamp scheduledDate;
    private Timestamp scheduleFireTime;
    private Timestamp dateCreated;
    private Timestamp dateModif;

    /**
     * Invariant PROPERTY TYPE String.
     */
    public static final String STATUS_TOLAUCH = "TOLAUCH";
    public static final String STATUS_IGNORED = "IGNORED";
    public static final String STATUS_TRIGGERED = "TRIGGERED";
    public static final String STATUS_ERROR = "ERROR";

    // PRIVATE STATIC INVARIANTS STATUS : TOLAUNCH / IGNORED / TRIGGERED / ERROR
    public long getID() {
        return ID;
    }

    public long getSchedulerId() {
        return schedulerId;
    }

    public String getScheduleName() {
        return scheduleName;
    }

    public String getStatus() {
        return status;
    }

    public String getComment() {
        return comment;
    }

    public String getUsrCreated() {
        return usrCreated;
    }

    public String getUsrModif() {
        return usrModif;
    }

    public Timestamp getScheduledDate() {
        return scheduledDate;
    }

    public Timestamp getScheduleFireTime() {
        return scheduleFireTime;
    }

    public Timestamp getDateCreated() {
        return dateCreated;
    }

    public Timestamp getDateModif() {
        return dateModif;
    }

    public void setID(long ID) {
        this.ID = ID;
    }

    public void setSchedulerId(long schedulerId) {
        this.schedulerId = schedulerId;
    }

    public void setScheduleName(String scheduleName) {
        this.scheduleName = scheduleName;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public void setUsrCreated(String usrCreated) {
        this.usrCreated = usrCreated;
    }

    public void setUsrModif(String usrModif) {
        this.usrModif = usrModif;
    }

    public void setScheduledDate(Timestamp scheduledDate) {
        this.scheduledDate = scheduledDate;
    }

    public void setScheduleFireTime(Timestamp scheduleFireTime) {
        this.scheduleFireTime = scheduleFireTime;
    }

    public void setDateCreated(Timestamp dateCreated) {
        this.dateCreated = dateCreated;
    }

    public void setDateModif(Timestamp dateModif) {
        this.dateModif = dateModif;
    }

}
