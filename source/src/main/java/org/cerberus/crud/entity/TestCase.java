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

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
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
    private String ticket;
    private String description;
    private String detailedDescription;
    private int priority;
    private int version;
    private String status;
    private boolean isActive;
    private boolean isActiveQA;
    private boolean isActiveUAT;
    private boolean isActivePROD;
    private String conditionOperator;
    private String conditionVal1;
    private String conditionVal2;
    private String conditionVal3;
    private String type;
    private String origine;
    private String refOrigine;
    private String howTo;
    private String comment;
    private String fromMajor;
    private String fromMinor;
    private String toMajor;
    private String toMinor;
    private JSONArray bugID;
    private String targetMajor;
    private String targetMinor;
    private String implementer;
    private String executor;
    private String userAgent;
    private String screenSize;
    private String usrCreated;
    private String dateCreated;
    private String usrModif;
    private Timestamp dateModif;

    /**
     * Not included in table.
     */
    private String system;
    private String lastExecutionStatus;
    private List<TestCaseCountry> testCaseCountry;
    private List<TestCaseCountryProperties> testCaseCountryProperties;
    private List<TestCaseStep> testCaseStep;
    private List<TestCaseStepBatch> testCaseStepBatch;
    private List<TestCaseLabel> testCaseLabel;
    private List<TestCaseDep> testCaseDep;

    public static final String TESTCASE_TYPE_MANUAL = "MANUAL";
    public static final String TESTCASE_TYPE_AUTOMATED = "AUTOMATED";
    public static final String TESTCASE_TYPE_PRIVATE = "PRIVATE";

    private static final Logger LOG = LogManager.getLogger(TestCase.class);

    public String getScreenSize() {
        return screenSize;
    }

    public void setScreenSize(String screenSize) {
        this.screenSize = screenSize;
    }

    public String getSystem() {
        return system;
    }

    public void setSystem(String system) {
        this.system = system;
    }

    public String getDateCreated() {
        return dateCreated;
    }

    public void setDateCreated(String dateCreated) {
        this.dateCreated = dateCreated;
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

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean isActive) {
        this.isActive = isActive;
    }

    public String getConditionOperator() {
        return conditionOperator;
    }

    public void setConditionOperator(String conditionOperator) {
        this.conditionOperator = conditionOperator;
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
        this.conditionVal2 = conditionVal2;
    }

    public String getConditionVal3() {
        return conditionVal3;
    }

    public void setConditionVal3(String conditionVal3) {
        this.conditionVal3 = conditionVal3;
    }

    public String getApplication() {
        return application;
    }

    public void setApplication(String application) {
        this.application = application;
    }

    public String getExecutor() {
        return executor;
    }

    public void setExecutor(String executor) {
        this.executor = executor;
    }

    @JsonIgnore
    public JSONArray getBugID() {
        return bugID;
    }

    @JsonIgnore
    public JSONArray getBugIDActive() {
        JSONArray res = new JSONArray();
        for (int i = 0; i < bugID.length(); i++) {
            try {
                JSONObject jo = bugID.getJSONObject(i);
                if (jo.getBoolean("act")) {
                    res.put(jo);
                }
            } catch (JSONException ex) {
                LOG.error(ex);
            }
        }
        return res;
    }

    public void setBugID(JSONArray bugID) {
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

    public String getDetailedDescription() {
        return detailedDescription;
    }

    public void setDetailedDescription(String detailedDescription) {
        this.detailedDescription = detailedDescription;
    }

    public String getFromMinor() {
        return fromMinor;
    }

    public void setFromMinor(String fromMinor) {
        this.fromMinor = fromMinor;
    }

    public String getFromMajor() {
        return fromMajor;
    }

    public void setFromMajor(String fromMajor) {
        this.fromMajor = fromMajor;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
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

    public String getRefOrigine() {
        return refOrigine;
    }

    public void setRefOrigine(String refOrigin) {
        this.refOrigine = refOrigin;
    }

    public boolean isActiveQA() {
        return isActiveQA;
    }

    public void setActiveQA(boolean isActiveQA) {
        this.isActiveQA = isActiveQA;
    }

    public boolean isActiveUAT() {
        return isActiveUAT;
    }

    public void setActiveUAT(boolean isActiveUAT) {
        this.isActiveUAT = isActiveUAT;
    }

    public boolean isActivePROD() {
        return isActivePROD;
    }

    public void setActivePROD(boolean isActivePROD) {
        this.isActivePROD = isActivePROD;
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

    public String getTargetMinor() {
        return targetMinor;
    }

    public void setTargetMinor(String targetMinor) {
        this.targetMinor = targetMinor;
    }

    public String getTargetMajor() {
        return targetMajor;
    }

    public void setTargetMajor(String targetMajor) {
        this.targetMajor = targetMajor;
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

    public String getToMinor() {
        return toMinor;
    }

    public void setToMinor(String toMinor) {
        this.toMinor = toMinor;
    }

    public String getToMajor() {
        return toMajor;
    }

    public void setToMajor(String toMajor) {
        this.toMajor = toMajor;
    }

    public List<TestCaseLabel> getTestCaseLabel() {
        return testCaseLabel;
    }

    public void setTestCaseLabel(List<TestCaseLabel> testCaseLabel) {
        this.testCaseLabel = testCaseLabel;
    }

    public List<TestCaseDep> getTestCaseDep() {
        return testCaseDep;
    }

    public void setTestCaseDep(List<TestCaseDep> testCaseDep) {
        this.testCaseDep = testCaseDep;
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

    public int getVersion() {
        return version;
    }

    public void setVersion(int version) {
        this.version = version;
    }

    public JSONObject toJson() {
        JSONObject result = new JSONObject();
        try {
            result.put("test", this.getTest());
            result.put("testcase", this.getTestCase());
            result.put("application", this.getApplication());
            result.put("status", this.getStatus());
            result.put("type", this.getType());
            result.put("priority", this.getPriority());
            result.put("description", this.getDescription());
            result.put("detailedDescription", this.getDetailedDescription());
            result.put("howTo", this.getHowTo());
            result.put("isActive", this.isActive());
            result.put("isActiveQA", this.isActiveQA());
            result.put("isActiveUAT", this.isActiveUAT());
            result.put("isActivePROD", this.isActivePROD());
            result.put("fromMajor", this.getFromMajor());
            result.put("toMajor", this.getToMajor());
            result.put("targetMajor", this.getTargetMajor());
            result.put("fromMinor", this.getFromMinor());
            result.put("toMinor", this.getToMinor());
            result.put("targetMinor", this.getTargetMinor());
            result.put("conditionOperator", this.getConditionOperator());
            result.put("conditionValue1", this.getConditionVal1());
            result.put("conditionValue2", this.getConditionVal2());
            result.put("conditionValue3", this.getConditionVal3());
            result.put("usrAgent", this.getUserAgent());
            result.put("screenSize", this.getScreenSize());
            result.put("bugs", this.getBugID());
            result.put("comment", this.getComment());
            result.put("implementer", this.getImplementer());
            result.put("executor", this.getExecutor());
            result.put("version", this.getVersion());
            result.put("dateCreated", this.getDateCreated());
            result.put("usrCreated", this.getUsrCreated());
            result.put("dateModif", this.getDateModif());
            result.put("usrModif", this.getUsrModif());
            result.put("origine", this.getOrigine());
            result.put("refOrigine", this.getRefOrigine());

            JSONArray array = new JSONArray();
            if (this.getTestCaseStep() != null) {
                for (Object testCaseStepList : this.getTestCaseStep()) {
                    array.put(((TestCaseStep) testCaseStepList).toJson());
                }
            }
            result.put("testCaseStepList", array);
        } catch (JSONException ex) {
            LOG.error(ex.toString(), ex);
        }
        return result;
    }

}
