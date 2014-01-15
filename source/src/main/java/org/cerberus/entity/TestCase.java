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
package org.cerberus.entity;

import java.util.List;

import org.cerberus.util.StringUtil;

/**
 * {Insert class description here}
 *
 * @author Tiago Bernardes
 * @version 1.0, 18/Dez/2012
 * @since 2.0.0
 */
public class TestCase {
    //Test Information
    private String test;
    private String testCase;
    //TestCaseInformation
    private String origin;
    private String refOrigin;
    private String creator;
    private String implementer;
    private String lastModifier;
    private String project;
    private String ticket;
    //TestCase Parameters
    private String application;
    private boolean runQA;
    private boolean runUAT;
    private boolean runPROD;
    private int priority;
    private String group;
    private String status;
    private List<Country> countriesList;
    private String shortDescription;
    private String description;
    private String howTo;
    //Activation Criterias
    private boolean active;
    private String fromSprint;
    private String fromRevision;
    private String toSprint;
    private String toRevision;
    private String lastExecutionStatus;
    private String bugID;
    private String targetSprint;
    private String targetRevision;
    private String comment;
    private List<String> countryList;

    public List<String> getCountryList() {
        return this.countryList;
    }

    public void setCountryList(List<String> countryList) {
        this.countryList = countryList;
    }

    public TestCase() {
    }

    public String getTest() {
        return this.test;
    }

    public void setTest(String tempTest) {
        this.test = tempTest;
    }

    public String getTestCase() {
        return this.testCase;
    }

    public void setTestCase(String tempTestCase) {
        this.testCase = tempTestCase;
    }

    public String getOrigin() {
        return this.origin;
    }

    public void setOrigin(String tempOrigin) {
        this.origin = tempOrigin;
    }

    public String getRefOrigin() {
        return this.refOrigin;
    }

    public void setRefOrigin(String tempRefOrigin) {
        this.refOrigin = tempRefOrigin;
    }

    public String getCreator() {
        return this.creator;
    }

    public void setCreator(String tempCreator) {
        this.creator = tempCreator;
    }

    public String getImplementer() {
        return this.implementer;
    }

    public void setImplementer(String tempImplementer) {
        this.implementer = tempImplementer;
    }

    public String getLastModifier() {
        return this.lastModifier;
    }

    public void setLastModifier(String tempLastModifier) {
        this.lastModifier = tempLastModifier;
    }

    public String getProject() {
        return this.project;
    }

    public void setProject(String tempProject) {
        this.project = tempProject;
    }

    public String getTicket() {
        return this.ticket;
    }

    public void setTicket(String tempTicket) {
        this.ticket = tempTicket;
    }

    public String getApplication() {
        return this.application;
    }

    public void setApplication(String tempApplication) {
        this.application = tempApplication;
    }

    public boolean isRunQA() {
        return this.runQA;
    }

    public void setRunQA(boolean tempRunQA) {
        this.runQA = tempRunQA;
    }

    public void setRunQA(String tempRunQA) {
        this.runQA = StringUtil.parseBoolean(tempRunQA);
    }

    public boolean isRunUAT() {
        return this.runUAT;
    }

    public void setRunUAT(boolean tempRunUAT) {
        this.runUAT = tempRunUAT;
    }

    public void setRunUAT(String tempRunUAT) {
        this.runUAT = StringUtil.parseBoolean(tempRunUAT);
    }

    public boolean isRunPROD() {
        return this.runPROD;
    }

    public void setRunPROD(boolean tempRunPROD) {
        this.runPROD = tempRunPROD;
    }

    public void setRunPROD(String tempRunPROD) {
        this.runPROD = StringUtil.parseBoolean(tempRunPROD);
    }

    public int getPriority() {
        return this.priority;
    }

    public void setPriority(int tempPriority) {
        this.priority = tempPriority;
    }

    public String getGroup() {
        return this.group;
    }

    public void setGroup(String tempGroup) {
        this.group = tempGroup;
    }

    public String getStatus() {
        return this.status;
    }

    public void setStatus(String tempStatus) {
        this.status = tempStatus;
    }

    public List<Country> getCountriesList() {
        return this.countriesList;
    }

    public void setCountriesList(List<Country> tempCountriesList) {
        this.countriesList = tempCountriesList;
    }

    public String getShortDescription() {
        return this.shortDescription;
    }

    public void setShortDescription(String tempShortDescription) {
        this.shortDescription = tempShortDescription;
    }

    public String getDescription() {
        return this.description;
    }

    public void setDescription(String tempDescription) {
        this.description = tempDescription;
    }

    public String getHowTo() {
        return this.howTo;
    }

    public void setHowTo(String tempHowTo) {
        this.howTo = tempHowTo;
    }

    public boolean isActive() {
        return this.active;
    }

    public void setActive(boolean tempActive) {
        this.active = tempActive;
    }

    public void setActive(String tempActive) {
        this.active = StringUtil.parseBoolean(tempActive);
    }

    public String getFromSprint() {
        return this.fromSprint;
    }

    public void setFromSprint(String tempFromSprint) {
        this.fromSprint = tempFromSprint;
    }

    public String getFromRevision() {
        return this.fromRevision;
    }

    public void setFromRevision(String tempFromRevision) {
        this.fromRevision = tempFromRevision;
    }

    public String getToSprint() {
        return this.toSprint;
    }

    public void setToSprint(String tempToSprint) {
        this.toSprint = tempToSprint;
    }

    public String getToRevision() {
        return this.toRevision;
    }

    public void setToRevision(String tempToRevision) {
        this.toRevision = tempToRevision;
    }

    public String getLastExecutionStatus() {
        return this.lastExecutionStatus;
    }

    public void setLastExecutionStatus(String tempLastExecutionStatus) {
        this.lastExecutionStatus = tempLastExecutionStatus;
    }

    public String getBugID() {
        return this.bugID;
    }

    public void setBugID(String tempBugID) {
        this.bugID = tempBugID;
    }

    public String getTargetSprint() {
        return this.targetSprint;
    }

    public void setTargetSprint(String tempTargetSprint) {
        this.targetSprint = tempTargetSprint;
    }

    public String getTargetRevision() {
        return this.targetRevision;
    }

    public void setTargetRevision(String tempTargetRevision) {
        this.targetRevision = tempTargetRevision;
    }

    public String getComment() {
        return this.comment;
    }

    public void setComment(String tempComment) {
        this.comment = tempComment;
    }
}
