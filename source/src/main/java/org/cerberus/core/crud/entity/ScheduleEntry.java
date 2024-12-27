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
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONException;
import org.json.JSONObject;

/**
 *
 * @author cdelage
 */
public class ScheduleEntry {

    private static final Logger LOG = LogManager.getLogger(ScheduleEntry.class);
    
    
    private long ID;
    private String type;
    private String name;
    private String cronDefinition;
    private Timestamp lastExecution;
    private String active;
    private String description;
    private String UsrCreated;
    private Timestamp DateCreated;
    private String UsrModif;
    private Timestamp DateModif;

    public long getID() {
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

    public Timestamp getLastExecution() {
        return lastExecution;
    }

    public String getActive() {
        return active;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getUsrCreated() {
        return UsrCreated;
    }

    public Timestamp getDateCreated() {
        return DateCreated;
    }

    public String getUsrModif() {
        return UsrModif;
    }

    public Timestamp getDateModif() {
        return DateModif;
    }

    public void setID(long ID) {
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

    public void setLastExecution(Timestamp lastExecution) {
        this.lastExecution = lastExecution;
    }

    public void setActive(String active) {
        this.active = active;
    }

    public void setUsrCreated(String UsrCreated) {
        this.UsrCreated = UsrCreated;
    }

    public void setDateCreated(Timestamp DateCreated) {
        this.DateCreated = DateCreated;
    }

    public void setUsrModif(String UsrModif) {
        this.UsrModif = UsrModif;
    }

    public void setDateModif(Timestamp DateModif) {
        this.DateModif = DateModif;
    }

    public boolean schedHasSameKey(ScheduleEntry obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }

        if (getID() != obj.getID()) {
            return false;
        }

        return true;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final ScheduleEntry other = (ScheduleEntry) obj;
        if ((this.name == null) ? (other.name != null) : !this.name.equals(other.name)) {
            return false;
        }
        if (this.ID != other.ID) {
            return false;
        }
        if ((this.cronDefinition == null) ? (other.cronDefinition != null) : !this.cronDefinition.equals(other.cronDefinition)) {
            return false;
        }
        if ((this.active == null) ? (other.active != null) : !this.active.equals(other.active)) {
            return false;
        }
        if ((this.description == null) ? (other.description != null) : !this.description.equals(other.description)) {
            return false;
        }
        if ((this.type == null) ? (other.type != null) : !this.type.equals(other.type)) {
            return false;
        }
        return true;
    }

    public JSONObject toJson() {
        JSONObject objJson = new JSONObject();
        try {
            objJson.put("id", this.getID());
            objJson.put("type", this.getType());
            objJson.put("name", this.getName());
            objJson.put("cronDefinition", this.getCronDefinition());
            objJson.put("isActive", this.getActive());
            objJson.put("lastExecution", this.getLastExecution());
            objJson.put("description", this.getDescription());
            objJson.put("usrCreated", this.getUsrCreated());
            objJson.put("dateCreated", this.getDateCreated());
            objJson.put("usrModif", this.getUsrModif());
            objJson.put("dateModif", this.getDateModif());
        } catch (JSONException ex) {
            LOG.error(ex.toString(), ex);
        }
        return objJson;
    }

}
