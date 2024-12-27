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

/**
 *
 * @author bcivel
 */
public class CountryEnvParam {

    private String system;
    private String country;
    private String environment;
    
    private String description;
    private String build;
    private String revision;
    private String chain;
    private String distribList;
    private String eMailBodyRevision;
    private String type;
    private String eMailBodyChain;
    private String eMailBodyDisableEnvironment;
    private boolean active;
    private boolean maintenanceAct;
    private String maintenanceStr;
    private String maintenanceEnd;

    // Outside Database model
    private String envGp;
    
    
    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public String getBuild() {
        return build;
    }

    public void setBuild(String build) {
        this.build = build;
    }

    public String getChain() {
        return chain;
    }

    public void setChain(String chain) {
        this.chain = chain;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getDistribList() {
        return distribList;
    }

    public void setDistribList(String distribList) {
        this.distribList = distribList;
    }

    public String geteMailBodyChain() {
        return eMailBodyChain;
    }

    public void seteMailBodyChain(String eMailBodyChain) {
        this.eMailBodyChain = eMailBodyChain;
    }

    public String geteMailBodyDisableEnvironment() {
        return eMailBodyDisableEnvironment;
    }

    public void seteMailBodyDisableEnvironment(String eMailBodyDisableEnvironment) {
        this.eMailBodyDisableEnvironment = eMailBodyDisableEnvironment;
    }

    public String geteMailBodyRevision() {
        return eMailBodyRevision;
    }

    public void seteMailBodyRevision(String eMailBodyRevision) {
        this.eMailBodyRevision = eMailBodyRevision;
    }

    public String getEnvironment() {
        return environment;
    }

    public void setEnvironment(String environment) {
        this.environment = environment;
    }

    public boolean isMaintenanceAct() {
        return maintenanceAct;
    }

    public void setMaintenanceAct(boolean maintenanceAct) {
        this.maintenanceAct = maintenanceAct;
    }

    public String getMaintenanceEnd() {
        return maintenanceEnd;
    }

    public void setMaintenanceEnd(String maintenanceEnd) {
        this.maintenanceEnd = maintenanceEnd;
    }

    public String getMaintenanceStr() {
        return maintenanceStr;
    }

    public void setMaintenanceStr(String maintenanceStr) {
        this.maintenanceStr = maintenanceStr;
    }

    public String getRevision() {
        return revision;
    }

    public void setRevision(String revision) {
        this.revision = revision;
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

    public String getEnvGp() {
        return envGp;
    }

    public void setEnvGp(String envGp) {
        this.envGp = envGp;
    }
    
}
