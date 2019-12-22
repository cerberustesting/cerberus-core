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

/**
 * @author vertigo
 */
public class Application {

    private String application;
    private int sort;
    private String type;
    private String system;
    private String subsystem;
    private String svnurl;
    private String bugTrackerUrl;
    private String bugTrackerNewUrl;
    private int poolSize;
    private String deploytype;
    private String mavengroupid;
    private String description;
    private String UsrCreated;
    private Timestamp DateCreated;
    private String UsrModif;
    private Timestamp DateModif;

    /**
     * Invariant PROPERTY TYPE String.
     */
    public static final String TYPE_GUI = "GUI";
    public static final String TYPE_BAT = "BAT";
    public static final String TYPE_SRV = "SRV";
    public static final String TYPE_APK = "APK";
    public static final String TYPE_IPA = "IPA";
    public static final String TYPE_FAT = "FAT";
    public static final String TYPE_NONE = "NONE";

    public int getPoolSize() {
        return poolSize;
    }

    public void setPoolSize(int poolSize) {
        this.poolSize = poolSize;
    }

    public String getUsrCreated() {
        return UsrCreated;
    }

    public void setUsrCreated(String UsrCreated) {
        this.UsrCreated = UsrCreated;
    }

    public Timestamp getDateCreated() {
        return DateCreated;
    }

    public void setDateCreated(Timestamp DateCreated) {
        this.DateCreated = DateCreated;
    }

    public String getUsrModif() {
        return UsrModif;
    }

    public void setUsrModif(String UsrModif) {
        this.UsrModif = UsrModif;
    }

    public Timestamp getDateModif() {
        return DateModif;
    }

    public void setDateModif(Timestamp DateModif) {
        this.DateModif = DateModif;
    }

    public String getBugTrackerNewUrl() {
        return bugTrackerNewUrl;
    }

    public void setBugTrackerNewUrl(String bugTrackerNewUrl) {
        this.bugTrackerNewUrl = bugTrackerNewUrl;
    }

    public String getBugTrackerUrl() {
        return bugTrackerUrl;
    }

    public void setBugTrackerUrl(String bugTrackerUrl) {
        this.bugTrackerUrl = bugTrackerUrl;
    }

    public String getApplication() {
        return application;
    }

    public void setApplication(String application) {
        this.application = application;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public int getSort() {
        return sort;
    }

    public void setSort(int sort) {
        this.sort = sort;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getSystem() {
        return system;
    }

    public void setSystem(String system) {
        this.system = system;
    }

    public String getSubsystem() {
        return subsystem;
    }

    public void setSubsystem(String subsystem) {
        this.subsystem = subsystem;
    }

    public String getSvnurl() {
        return svnurl;
    }

    public void setSvnurl(String svnurl) {
        this.svnurl = svnurl;
    }

    public String getDeploytype() {
        return deploytype;
    }

    public void setDeploytype(String deploytype) {
        this.deploytype = deploytype;
    }

    public String getMavengroupid() {
        return mavengroupid;
    }

    public void setMavengroupid(String mavengroupid) {
        this.mavengroupid = mavengroupid;
    }

    public boolean hasSameKey(Application obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }

        final Application other = (Application) obj;
        if ((this.application == null) ? (other.application != null) : !this.application.equals(other.application)) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {

        int hash = 3;
        hash = 67 * hash + (this.application != null ? this.application.hashCode() : 0);
        hash = 67 * hash + (this.description != null ? this.description.hashCode() : 0);
        hash = 67 * hash + this.sort;
        hash = 67 * hash + (this.type != null ? this.type.hashCode() : 0);
        hash = 67 * hash + (this.system != null ? this.system.hashCode() : 0);
        hash = 67 * hash + (this.subsystem != null ? this.subsystem.hashCode() : 0);
        hash = 67 * hash + (this.svnurl != null ? this.svnurl.hashCode() : 0);
        hash = 67 * hash + (this.bugTrackerUrl != null ? this.bugTrackerUrl.hashCode() : 0);
        hash = 67 * hash + (this.bugTrackerNewUrl != null ? this.bugTrackerNewUrl.hashCode() : 0);
        hash = 67 * hash + (this.deploytype != null ? this.deploytype.hashCode() : 0);
        hash = 67 * hash + (this.mavengroupid != null ? this.mavengroupid.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {

        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final Application other = (Application) obj;
        if ((this.application == null) ? (other.application != null) : !this.application.equals(other.application)) {
            return false;
        }
        if ((this.description == null) ? (other.description != null) : !this.description.equals(other.description)) {
            return false;
        }
        if (this.sort != other.sort) {
            return false;
        }
        if ((this.type == null) ? (other.type != null) : !this.type.equals(other.type)) {
            return false;
        }
        if ((this.system == null) ? (other.system != null) : !this.system.equals(other.system)) {
            return false;
        }
        if ((this.subsystem == null) ? (other.subsystem != null) : !this.subsystem.equals(other.subsystem)) {
            return false;
        }
        if ((this.svnurl == null) ? (other.svnurl != null) : !this.svnurl.equals(other.svnurl)) {
            return false;
        }
        if ((this.bugTrackerUrl == null) ? (other.bugTrackerUrl != null) : !this.bugTrackerUrl.equals(other.bugTrackerUrl)) {
            return false;
        }
        if ((this.bugTrackerNewUrl == null) ? (other.bugTrackerNewUrl != null) : !this.bugTrackerNewUrl.equals(other.bugTrackerNewUrl)) {
            return false;
        }
        if ((this.deploytype == null) ? (other.deploytype != null) : !this.deploytype.equals(other.deploytype)) {
            return false;
        }
        if ((this.mavengroupid == null) ? (other.mavengroupid != null) : !this.mavengroupid.equals(other.mavengroupid)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return application;
    }
}
