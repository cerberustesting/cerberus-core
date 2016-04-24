/* DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
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

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 * @author vertigo
 */
@Entity
@Table(name = "application")
public class Application {

    @Id
    private String application;
    private String description;
    private int sort;
    private String type;
    private String system;
    private String subsystem;
    private String svnurl;
    private String bugTrackerUrl;
    private String bugTrackerNewUrl;
    private String deploytype;
    private String mavengroupid;

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

    @Override
    public String toString() {
        return application;
    }
}
