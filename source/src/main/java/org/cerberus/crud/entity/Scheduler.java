/*Cerberus Copyright (C) 2013 - 2017 cerberustesting
DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.

This file is part of Cerberus.

Cerberus is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

Cerberus is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without 

even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with Cerberus.  If not, see <http://www.gnu.org/licenses/>.

import java.sql.Timestamp;
 */
/**
 *
 * @author cdelage
 */
package org.cerberus.crud.entity;

import java.sql.Timestamp;

public class Scheduler {

    private Integer ID;
    private String type;
    private String name;
    private String cronDefinition;
    private String lastExecution;
    private String active;
    private String UsrCreated;
    private String DateCreated;
    private String UsrModif;
    private String DateModif;

    public Integer getID() {
        return ID;
    }

    public String getType() {
        return type;
    }

    public String getName() {
        return name;
    }

    public String getCronDefinition() {
        return cronDefinition;
    }

    public String getLastExecution() {
        return lastExecution;
    }

    public String getActive() {
        return active;
    }

    public String getUsrCreated() {
        return UsrCreated;
    }

    public String getDateCreated() {
        return DateCreated;
    }

    public String getUsrModif() {
        return UsrModif;
    }

    public String getDateModif() {
        return DateModif;
    }

    public void setID(Integer ID) {
        this.ID = ID;
    }

    public void setType(String type) {
        this.type = type;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setCronDefinition(String cronDefinition) {
        this.cronDefinition = cronDefinition;
    }

    public void setLastExecution(String lastExecution) {
        this.lastExecution = lastExecution;
    }

    public void setActive(String active) {
        this.active = active;
    }

    public void setUsrCreated(String UsrCreated) {
        this.UsrCreated = UsrCreated;
    }

    public void setDateCreated(String DateCreated) {
        this.DateCreated = DateCreated;
    }

    public void setUsrModif(String UsrModif) {
        this.UsrModif = UsrModif;
    }

    public void setDateModif(String DateModif) {
        this.DateModif = DateModif;
    }

}
