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

import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.sql.Timestamp;
import java.util.List;

/**
 * @author bcivel
 */
public class TestCase {

    private String test;
    private String testCase;
    private String application;
    private String project;
    private String ticket;
    private String description;
    private String behaviorOrValueExpected;
    private int priority;
    private String status;
    private String tcActive;
    private String conditionOper;
    private String conditionVal1;
    private String conditionVal2;
    private String group;
    private String origine;
    private String refOrigine;
    private String howTo;
    private String comment;
    private String fromBuild;
    private String fromRev;
    private String toBuild;
    private String toRev;
    private String bugID;
    private String targetBuild;
    private String targetRev;
    private String implementer;
    private String activeQA;
    private String activeUAT;
    private String activePROD;
    private String function;
    private String userAgent;
    private String usrCreated;
    private String dateCreated;
    private String usrModif;
    private Timestamp dateModif;

    /**
     * Not included in table.
     */
    private String lastExecutionStatus;
    private List<TestCaseCountry> testCaseCountry;
    private List<TestCaseCountryProperties> testCaseCountryProperties;
    private List<TestCaseStep> testCaseStep;
    private List<TestCaseStepBatch> testCaseStepBatch;
    private List<TestCaseLabel> testCaseLabel;

    private static final Logger LOG = Logger.getLogger(TestCase.class);


    public String getDateCreated() {
        return dateCreated;
    }

    public void setDateCreated(String dateCreated) {
        this.dateCreated = dateCreated;
    }

    public String getFunction() {
        return function;
    }

    public void setFunction(String function) {
        this.function = function;
    }

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

    public String getTcActive() {
        return tcActive;
    }

    public void setTcActive(String active) {
        this.tcActive = active;
    }

    public String getConditionOper() {
        return conditionOper;
    }

    public void setConditionOper(String conditionOper) {
        this.conditionOper = conditionOper;
    }

    public String getConditionVal1() {
        return conditionVal1;
    }

    public void setConditionVal1(String conditionVal1) {
        this.conditionVal1 = conditionVal1;
    }

    public String getConditionVal2() {
        return conditionVal2;
    }

    public void setConditionVal2(String conditionVal2) {
        this.conditionVal1 = conditionVal2;
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

    public String getUsrCreated() {
        return usrCreated;
    }

    public void setUsrCreated(String creator) {
        this.usrCreated = creator;
    }

    public String getBehaviorOrValueExpected() {
        return behaviorOrValueExpected;
    }

    public void setBehaviorOrValueExpected(String behaviorOrValuexpected) {
        this.behaviorOrValueExpected = behaviorOrValuexpected;
    }

    public String getFromRev() {
        return fromRev;
    }

    public void setFromRev(String fromRevision) {
        this.fromRev = fromRevision;
    }

    public String getFromBuild() {
        return fromBuild;
    }

    public void setFromBuild(String fromSprint) {
        this.fromBuild = fromSprint;
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

    public String getUsrModif() {
        return usrModif;
    }

    public void setUsrModif(String lastModifier) {
        this.usrModif = lastModifier;
    }

    public String getOrigine() {
        return origine;
    }

    public void setOrigine(String origin) {
        this.origine = origin;
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

    public String getRefOrigine() {
        return refOrigine;
    }

    public void setRefOrigine(String refOrigin) {
        this.refOrigine = refOrigin;
    }

    public String getActivePROD() {
        return activePROD;
    }

    public void setActivePROD(String runPROD) {
        this.activePROD = runPROD;
    }

    public String getActiveQA() {
        return activeQA;
    }

    public void setActiveQA(String runQA) {
        this.activeQA = runQA;
    }

    public String getActiveUAT() {
        return activeUAT;
    }

    public void setActiveUAT(String runUAT) {
        this.activeUAT = runUAT;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getTargetRev() {
        return targetRev;
    }

    public void setTargetRev(String targetRevision) {
        this.targetRev = targetRevision;
    }

    public String getTargetBuild() {
        return targetBuild;
    }

    public void setTargetBuild(String targetSprint) {
        this.targetBuild = targetSprint;
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

    public String getToRev() {
        return toRev;
    }

    public void setToRev(String toRevision) {
        this.toRev = toRevision;
    }

    public String getToBuild() {
        return toBuild;
    }

    public void setToBuild(String toSprint) {
        this.toBuild = toSprint;
    }

    public List<TestCaseLabel> getTestCaseLabel() {
        return testCaseLabel;
    }

    public void setTestCaseLabel(List<TestCaseLabel> testCaseLabel) {
        this.testCaseLabel = testCaseLabel;
    }

    public String getUserAgent() {
        return userAgent;
    }

    public void setUserAgent(String userAgent) {
        this.userAgent = userAgent;
    }

    public Timestamp getDateModif() {
        return dateModif;
    }

    public void setDateModif(Timestamp dateModif) {
        this.dateModif = dateModif;
    }

    public JSONObject toJson(){
        JSONObject result = new JSONObject();
        try {
            result.put("test", this.getTest());
            result.put("testcase", this.getTestCase());
            result.put("application", this.getApplication());
            result.put("project", this.getProject());
            result.put("ticket", this.getTicket());
            result.put("description", this.getDescription());
            result.put("behaviourOrValueExpected", this.getBehaviorOrValueExpected());
            result.put("priority", this.getPriority());
            result.put("status", this.getStatus());
            result.put("tcActive", this.getTcActive());
            result.put("conditionOper", this.getConditionOper());
            result.put("conditionValue1", this.getConditionVal1());
            result.put("conditionValue2", this.getConditionVal1());
            result.put("group", this.getGroup());
            result.put("origine", this.getOrigine());
            result.put("refOrigine", this.getRefOrigine());
            result.put("howTo", this.getHowTo());
            result.put("comment", this.getComment());
            result.put("fromBuild", this.getFromBuild());
            result.put("fromRev", this.getFromRev());
            result.put("toBuild", this.getToBuild());
            result.put("toRev", this.getToRev());
            result.put("bugId", this.getBugID());
            result.put("targetBuild", this.getTargetBuild());
            result.put("targetRev", this.getTargetRev());
            result.put("implementer", this.getImplementer());
            result.put("activeQA", this.getActiveQA());
            result.put("activeUAT", this.getActiveUAT());
            result.put("activePROD", this.getActivePROD());
            result.put("function", this.getFunction());
            result.put("usrAgent", this.getUserAgent());
            result.put("usrCreated", this.getUsrCreated());
            result.put("dateCreated", this.getDateCreated());
            result.put("usrModif", this.getUsrModif());
            result.put("dateModif", this.getDateModif());
            JSONArray array = new JSONArray();
            if (this.getTestCaseStep() != null ) {
                for (Object testCaseStepExecution : this.getTestCaseStep()) {
                    array.put(((TestCaseStep) testCaseStepExecution).toJson());
                }
            }
            result.put("testCaseStepList", array);
        } catch (JSONException ex) {
            LOG.error(ex.toString());
        }
        return result;
    }

}
