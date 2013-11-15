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

/**
 * @author bcivel
 */
public class TCase {

    private String test;
    private String testCase;
    private String origin;
    private String refOrigin;
    private String creator;
    private String implementer;
    private String lastModifier;
    private String project;
    private String ticket;
    private String application;
    private String runQA;
    private String runUAT;
    private String runPROD;
    private int priority;
    private String group;
    private String status;
    private String shortDescription;
    private String description;
    private String howTo;
    private String active;
    private String fromSprint;
    private String fromRevision;
    private String toSprint;
    private String toRevision;
    private String lastExecutionStatus;
    private String bugID;
    private String targetSprint;
    private String targetRevision;
    private String comment;
    private List<TestCaseCountry> testCaseCountry;
    private List<TestCaseCountryProperties> testCaseCountryProperties;
    private List<TestCaseStep> testCaseStep;
    private List<TestCaseStepBatch> testCaseStepBatch;

    public List<TestCaseCountryProperties> getTestCaseCountryProperties() {
        return testCaseCountryProperties;
    }

    public void setTestCaseCountryProperties(List<TestCaseCountryProperties> testCaseCountryProperties) {
        this.testCaseCountryProperties = testCaseCountryProperties;
    }

    public List<TestCaseStepBatch> getTestCaseStepBatch() {
        return testCaseStepBatch;
    }

    public void setTestCaseStepBatch(List<TestCaseStepBatch> testCaseStepBatch) {
        this.testCaseStepBatch = testCaseStepBatch;
    }

    public String getActive() {
        return active;
    }

    public void setActive(String active) {
        this.active = active;
    }

    public String getApplication() {
        return application;
    }

    public void setApplication(String application) {
        this.application = application;
    }

    public String getBugID() {
        return bugID;
    }

    public void setBugID(String bugID) {
        this.bugID = bugID;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public String getCreator() {
        return creator;
    }

    public void setCreator(String creator) {
        this.creator = creator;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getFromRevision() {
        return fromRevision;
    }

    public void setFromRevision(String fromRevision) {
        this.fromRevision = fromRevision;
    }

    public String getFromSprint() {
        return fromSprint;
    }

    public void setFromSprint(String fromSprint) {
        this.fromSprint = fromSprint;
    }

    public String getGroup() {
        return group;
    }

    public void setGroup(String group) {
        this.group = group;
    }

    public String getHowTo() {
        return howTo;
    }

    public void setHowTo(String howTo) {
        this.howTo = howTo;
    }

    public String getImplementer() {
        return implementer;
    }

    public void setImplementer(String implementer) {
        this.implementer = implementer;
    }

    public String getLastExecutionStatus() {
        return lastExecutionStatus;
    }

    public void setLastExecutionStatus(String lastExecutionStatus) {
        this.lastExecutionStatus = lastExecutionStatus;
    }

    public String getLastModifier() {
        return lastModifier;
    }

    public void setLastModifier(String lastModifier) {
        this.lastModifier = lastModifier;
    }

    public String getOrigin() {
        return origin;
    }

    public void setOrigin(String origin) {
        this.origin = origin;
    }

    public int getPriority() {
        return priority;
    }

    public void setPriority(int priority) {
        this.priority = priority;
    }

    public String getProject() {
        return project;
    }

    public void setProject(String project) {
        this.project = project;
    }

    public String getRefOrigin() {
        return refOrigin;
    }

    public void setRefOrigin(String refOrigin) {
        this.refOrigin = refOrigin;
    }

    public String getRunPROD() {
        return runPROD;
    }

    public void setRunPROD(String runPROD) {
        this.runPROD = runPROD;
    }

    public String getRunQA() {
        return runQA;
    }

    public void setRunQA(String runQA) {
        this.runQA = runQA;
    }

    public String getRunUAT() {
        return runUAT;
    }

    public void setRunUAT(String runUAT) {
        this.runUAT = runUAT;
    }

    public String getShortDescription() {
        return shortDescription;
    }

    public void setShortDescription(String shortDescription) {
        this.shortDescription = shortDescription;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getTargetRevision() {
        return targetRevision;
    }

    public void setTargetRevision(String targetRevision) {
        this.targetRevision = targetRevision;
    }

    public String getTargetSprint() {
        return targetSprint;
    }

    public void setTargetSprint(String targetSprint) {
        this.targetSprint = targetSprint;
    }

    public String getTest() {
        return test;
    }

    public void setTest(String test) {
        this.test = test;
    }

    public String getTestCase() {
        return testCase;
    }

    public void setTestCase(String testCase) {
        this.testCase = testCase;
    }

    public List<TestCaseCountry> getTestCaseCountry() {
        return testCaseCountry;
    }

    public void setTestCaseCountry(List<TestCaseCountry> testCaseCountry) {
        this.testCaseCountry = testCaseCountry;
    }

    public List<TestCaseStep> getTestCaseStep() {
        return testCaseStep;
    }

    public void setTestCaseStep(List<TestCaseStep> testCaseStep) {
        this.testCaseStep = testCaseStep;
    }

    public String getTicket() {
        return ticket;
    }

    public void setTicket(String ticket) {
        this.ticket = ticket;
    }

    public String getToRevision() {
        return toRevision;
    }

    public void setToRevision(String toRevision) {
        this.toRevision = toRevision;
    }

    public String getToSprint() {
        return toSprint;
    }

    public void setToSprint(String toSprint) {
        this.toSprint = toSprint;
    }
}
