/*
 * Cerberus  Copyright (C) 2013  vertigo17
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
package org.cerberus.refactor;

import org.apache.log4j.Level;
import org.cerberus.log.MyLogger;

public class Options {

    private Boolean active;
    private String application;
    private String build;
    private String chainNN;
    private String environment;
    private String executionTable;
    private String group;
    private String ip;
    private String port;
    private String priority;
    private String project;
    private Boolean activeQA;
    private Boolean activeUAT;
    private Boolean activePROD;
    private String revision;
    private StringBuilder sqlOpts;
    private String status;
    private String tag;
    private Boolean tcActive;
    private String test;
    private String testcase;
    private String targetBuild;
    private String targetRev;
    private String testcaseTable;
    private String testTable;

    public Options() {

        this.setTestTable("t");
        this.setTestcaseTable("tc");
        this.setExecutionTable("te");
    }

    private void appendBoolean(StringBuilder opts, String column, Boolean value) {

        try {
            if (value) {
                this.appendString(opts, column, "Y");
            } else {
                this.appendString(opts, column, "N");
            }

        } catch (NullPointerException e) {
            MyLogger.log(Options.class.getName(), Level.FATAL, "" + e);
        }

    }

    private void appendString(StringBuilder opts, String column, String value) {

        try {
            String s = " AND " + column.toString() + " = '" + value.toString() + "' ";
            opts.append(s);
        } catch (NullPointerException e) {
            MyLogger.log(Options.class.getName(), Level.FATAL, "String opts is null: " + e.toString());
        }
    }

    public String generateSQL(Boolean test, Boolean testcase, Boolean execution) {

        this.sqlOpts = new StringBuilder();

        if (test) {
            this.appendString(this.sqlOpts, this.getTestTable() + "." + "Test", this.test);
            this.appendBoolean(this.sqlOpts, this.getTestTable() + "." + "Active", this.active);
        }
        if (testcase) {
            this.appendString(this.sqlOpts, this.getTestcaseTable() + "." + "TestCase", this.testcase);
            this.appendBoolean(this.sqlOpts, this.getTestcaseTable() + "." + "TCActive", this.tcActive);
            this.appendBoolean(this.sqlOpts, this.getTestcaseTable() + "." + "activeQA", this.activeQA);
            this.appendBoolean(this.sqlOpts, this.getTestcaseTable() + "." + "activeUAT", this.activeUAT);
            this.appendBoolean(this.sqlOpts, this.getTestcaseTable() + "." + "activePROD", this.activePROD);
            this.appendString(this.sqlOpts, this.getTestcaseTable() + "." + "Priority", this.priority);
            this.appendString(this.sqlOpts, this.getTestcaseTable() + "." + "Project", this.project);
            this.appendString(this.sqlOpts, this.getTestcaseTable() + "." + "Application", this.application);
            this.appendString(this.sqlOpts, this.getTestcaseTable() + "." + "Status", this.status);
            this.appendString(this.sqlOpts, this.getTestcaseTable() + "." + "Group", this.group);
            this.appendString(this.sqlOpts, this.getTestcaseTable() + "." + "TargetBuild", this.targetBuild);
            this.appendString(this.sqlOpts, this.getTestcaseTable() + "." + "TargetRev", this.targetRev);
        }
        if (execution) {
            this.appendString(this.sqlOpts, this.getExecutionTable() + "." + "Ip", this.ip);
            this.appendString(this.sqlOpts, this.getExecutionTable() + "." + "Port", this.port);
            this.appendString(this.sqlOpts, this.getExecutionTable() + "." + "Environment", this.environment);
            this.appendString(this.sqlOpts, this.getExecutionTable() + "." + "Build", this.build);
            this.appendString(this.sqlOpts, this.getExecutionTable() + "." + "Revision", this.revision);
            this.appendString(this.sqlOpts, this.getExecutionTable() + "." + "Tag", this.tag);
        }

        return this.sqlOpts.toString();
    }

    public Boolean getActive() {

        return this.active;
    }

    public String getApplication() {

        return this.application;
    }

    public String getBuild() {

        return this.build;
    }

    public String getEnvironment() {

        return this.environment;
    }

    public String getExecutionTable() {

        return this.executionTable;
    }

    public String getGroup() {

        return this.group;
    }

    public String getIp() {

        return this.ip;
    }

    public String getPort() {

        return this.port;
    }

    public String getPriority() {

        return this.priority;
    }

    public String getProject() {

        return this.project;
    }

    public Boolean getActiveQA() {

        return this.activeQA;
    }

    public Boolean getActiveUAT() {

        return this.activeUAT;
    }

    public Boolean getActivePROD() {

        return this.activePROD;
    }

    public String getRevision() {

        return this.revision;
    }

    public StringBuilder getSqlOpts() {

        return this.sqlOpts;
    }

    public String getStatus() {

        return this.status;
    }

    public String getTag() {

        return this.tag;
    }

    public String getTargetBuild() {

        return this.targetBuild;
    }

    public String getTargetRev() {

        return this.targetRev;
    }

    public Boolean getTcActive() {

        return this.tcActive;
    }

    public String getTest() {

        return this.test;
    }

    public String getTestcase() {

        return this.testcase;
    }

    public String getTestcaseTable() {

        return this.testcaseTable;
    }

    public String getTestTable() {

        return this.testTable;
    }

    public void setActive(Boolean active) {

        this.active = active;
    }

    public void setActive(String active) {

        if (active.compareTo("Y") == 0) {
            this.active = true;
        } else {
            this.active = false;
        }
    }

    public void setApplication(String application) {

        this.application = application;
    }

    public void setBuild(String build) {

        this.build = build;
    }

    public void setEnvironment(String environment) {

        this.environment = environment;
    }

    public void setExecutionTable(String executionTable) {

        this.executionTable = executionTable;
    }

    public void setGroup(String group) {

        this.group = group;
    }

    public void setIp(String ip) {

        this.ip = ip;
    }

    public void setPort(String port) {

        this.port = port;
    }

    public void setPriority(String priority) {

        this.priority = priority;
    }

    public void setProject(String project) {

        this.project = project;
    }

    public void setTargetBuild(String targetBuild) {

        this.targetBuild = targetBuild;
    }

    public void setTargetRev(String targetRev) {

        this.targetRev = targetRev;
    }

    public void setActiveQA(Boolean activeQA) {
        this.activeQA = activeQA;
    }

    public void setActiveUAT(Boolean activeUAT) {
        this.activeUAT = activeUAT;
    }

    public void setActivePROD(Boolean activePROD) {
        this.activePROD = activePROD;
    }

    public void setActiveQA(String activeQA) {
        if (activeQA.compareTo("Y") == 0) {
            this.activeQA = true;
        } else {
            this.activeQA = false;
        }
    }

    public void setActiveUAT(String activeUAT) {
        if (activeUAT.compareTo("Y") == 0) {
            this.activeUAT = true;
        } else {
            this.activeUAT = false;
        }
    }

    public void setActivePROD(String activePROD) {
        if (activePROD.compareTo("Y") == 0) {
            this.activePROD = true;
        } else {
            this.activePROD = false;
        }
    }

    public void setRevision(String revision) {

        this.revision = revision;
    }

    public void setSqlOpts(StringBuilder sqlOpts) {

        this.sqlOpts = sqlOpts;
    }

    public void setStatus(String status) {

        this.status = status;
    }

    public void setTag(String tag) {

        this.tag = tag;
    }

    public void setTcActive(Boolean tcActive) {

        this.tcActive = tcActive;
    }

    public void setTcActive(String tcActive) {

        if (tcActive.compareTo("Y") == 0) {
            this.tcActive = true;
        } else {
            this.tcActive = false;
        }
    }

    public void setTest(String test) {

        this.test = test;
    }

    public void setTestcase(String testcase) {

        this.testcase = testcase;
    }

    public void setTestcaseTable(String testcaseTable) {

        this.testcaseTable = testcaseTable;
    }

    public void setTestTable(String testTable) {

        this.testTable = testTable;
    }
}
