/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.redcats.tst.entity;

/**
 *
 * @author bcivel
 */
public class CountryEnvParam {
    
  private String country;
  private String environment;
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
  
  
}
