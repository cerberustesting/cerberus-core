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
    private String comment;
    private String fromMajor;
    private String fromMinor;
    private String toMajor;
    private String toMinor;
    private JSONArray bugs;
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
    private List<TestCaseCountryProperties> testCaseCountryProperties;
    private List<TestCaseCountryProperties> testCaseInheritedProperties;
    private List<Invariant> invariantCountries;
    private List<TestCaseCountry> testCaseCountries;
    private List<TestCaseStep> steps;
    private List<TestCaseStepBatch> testCaseStepBatch;
    private List<TestCaseLabel> testCaseLabels;
    private List<Label> labels;
    private List<TestCaseDep> dependencies;

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
    public JSONArray getBugs() {
        return bugs;
    }

    @JsonIgnore
    public JSONArray getBugsActive() {
        JSONArray res = new JSONArray();
        for (int i = 0; i < bugs.length(); i++) {
            try {
                JSONObject jo = bugs.getJSONObject(i);
                if (jo.getBoolean("act")) {
                    res.put(jo);
                }
            } catch (JSONException ex) {
                LOG.error(ex);
            }
        }
        return res;
    }

    public void setBugs(JSONArray bugs) {
        this.bugs = bugs;
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

    public List<TestCaseCountryProperties> getTestCaseCountryProperties() {
        return testCaseCountryProperties;
    }

    public void setTestCaseCountryProperties(List<TestCaseCountryProperties> testCaseCountryProperties) {
        this.testCaseCountryProperties = testCaseCountryProperties;
    }

    public List<TestCaseCountryProperties> getTestCaseInheritedProperties() {
        return testCaseInheritedProperties;
    }

    public void setTestCaseInheritedProperties(List<TestCaseCountryProperties> testCaseInheritedProperties) {
        this.testCaseInheritedProperties = testCaseInheritedProperties;
    }

    public List<TestCaseCountry> getTestCaseCountries() {
        return testCaseCountries;
    }

    public void setTestCaseCountries(List<TestCaseCountry> testCaseCountries) {
        this.testCaseCountries = testCaseCountries;
    }

    public List<Invariant> getInvariantCountries() {
        return invariantCountries;
    }

    public void setInvariantCountries(List<Invariant> invariantCountries) {
        this.invariantCountries = invariantCountries;
    }

    public List<TestCaseStep> getSteps() {
        return steps;
    }

    public void setSteps(List<TestCaseStep> steps) {
        this.steps = steps;
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

    public List<TestCaseLabel> getTestCaseLabels() {
        return testCaseLabels;
    }

    public void setTestCaseLabels(List<TestCaseLabel> testCaseLabels) {
        this.testCaseLabels = testCaseLabels;
    }

    public List<Label> getLabels() {
        return labels;
    }

    public void setLabels(List<Label> labels) {
        this.labels = labels;
    }

    public List<TestCaseDep> getDependencies() {
        return dependencies;
    }

    public void setDependencies(List<TestCaseDep> dependencies) {
        this.dependencies = dependencies;
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

    @Override
    public String toString() {
        return "TestCase{" + "test=" + test + ", testCase=" + testCase + ", application=" + application + ", ticket=" + ticket + ", description=" + description + ", detailedDescription=" + detailedDescription + ", priority=" + priority + ", version=" + version + ", status=" + status + ", isActive=" + isActive + ", isActiveQA=" + isActiveQA + ", isActiveUAT=" + isActiveUAT + ", isActivePROD=" + isActivePROD + ", conditionOperator=" + conditionOperator + ", conditionVal1=" + conditionVal1 + ", conditionVal2=" + conditionVal2 + ", conditionVal3=" + conditionVal3 + ", type=" + type + ", origine=" + origine + ", refOrigine=" + refOrigine + ", comment=" + comment + ", fromMajor=" + fromMajor + ", fromMinor=" + fromMinor + ", toMajor=" + toMajor + ", toMinor=" + toMinor + ", bugs=" + bugs + ", targetMajor=" + targetMajor + ", targetMinor=" + targetMinor + ", implementer=" + implementer + ", executor=" + executor + ", userAgent=" + userAgent + ", screenSize=" + screenSize + ", usrCreated=" + usrCreated + ", dateCreated=" + dateCreated + ", usrModif=" + usrModif + ", dateModif=" + dateModif + ", system=" + system + ", lastExecutionStatus=" + lastExecutionStatus + ", testCaseCountryProperties=" + testCaseCountryProperties + ", invariantCountries=" + invariantCountries + ", testCaseCountries=" + testCaseCountries + ", steps=" + steps + ", testCaseStepBatch=" + testCaseStepBatch + ", testCaseLabels=" + testCaseLabels + ", labels=" + labels + ", dependencies=" + dependencies + '}';
    }

    public JSONObject toJson() {
        JSONObject testCaseJson = new JSONObject();
        try {
            testCaseJson.put("test", this.getTest());
            testCaseJson.put("testcase", this.getTestCase());
            testCaseJson.put("application", this.getApplication());
            testCaseJson.put("system", this.getSystem());
            testCaseJson.put("status", this.getStatus());
            testCaseJson.put("type", this.getType());
            testCaseJson.put("priority", this.getPriority());
            testCaseJson.put("description", this.getDescription());
            testCaseJson.put("detailedDescription", this.getDetailedDescription());
            testCaseJson.put("isActive", this.isActive());
            testCaseJson.put("isActiveQA", this.isActiveQA());
            testCaseJson.put("isActiveUAT", this.isActiveUAT());
            testCaseJson.put("isActivePROD", this.isActivePROD());
            testCaseJson.put("fromMajor", this.getFromMajor());
            testCaseJson.put("toMajor", this.getToMajor());
            testCaseJson.put("targetMajor", this.getTargetMajor());
            testCaseJson.put("fromMinor", this.getFromMinor());
            testCaseJson.put("toMinor", this.getToMinor());
            testCaseJson.put("targetMinor", this.getTargetMinor());
            testCaseJson.put("conditionOperator", this.getConditionOperator());
            testCaseJson.put("conditionValue1", this.getConditionVal1());
            testCaseJson.put("conditionValue2", this.getConditionVal2());
            testCaseJson.put("conditionValue3", this.getConditionVal3());
            testCaseJson.put("usrAgent", this.getUserAgent());
            testCaseJson.put("screenSize", this.getScreenSize());
            testCaseJson.put("bugs", this.getBugs());
            testCaseJson.put("comment", this.getComment());
            testCaseJson.put("implementer", this.getImplementer());
            testCaseJson.put("executor", this.getExecutor());
            testCaseJson.put("version", this.getVersion());
            testCaseJson.put("dateCreated", this.getDateCreated());
            testCaseJson.put("usrCreated", this.getUsrCreated());
            testCaseJson.put("dateModif", this.getDateModif());
            testCaseJson.put("usrModif", this.getUsrModif());
            testCaseJson.put("origine", this.getOrigine());
            testCaseJson.put("refOrigine", this.getRefOrigine());

            JSONArray stepsJson = new JSONArray();
            if (this.getSteps() != null) {
                for (TestCaseStep step : this.getSteps()) {
                    stepsJson.put(step.toJson());
                }
            }
            testCaseJson.put("steps", stepsJson);

            JSONArray countriesJson = new JSONArray();
            if (this.getInvariantCountries() != null) {
                for (Invariant country : this.getInvariantCountries()) {
                    if(country != null) {
                       countriesJson.put(country.toJson()); 
                    }
                }
            }
            testCaseJson.put("countries", countriesJson);

            JSONArray dependenciesJson = new JSONArray();
            if (this.getDependencies() != null) {
                for (TestCaseDep testCaseDependecy : this.getDependencies()) {
                    dependenciesJson.put(testCaseDependecy.toJson());
                }
            }
            testCaseJson.put("dependencies", dependenciesJson);

            JSONArray labelsJson = new JSONArray();
            if (this.getLabels() != null) {
                for (Label label : this.getLabels()) {
                    labelsJson.put(label.toJson());
                }
            }
            testCaseJson.put("labels", labelsJson);

            JSONObject propertiesJson = new JSONObject();
            JSONArray testCasePropertiesJson = new JSONArray();
            if (this.getTestCaseCountryProperties() != null) {
                for (TestCaseCountryProperties testCaseCountryProperties : this.getTestCaseCountryProperties()) {
                    testCasePropertiesJson.put(testCaseCountryProperties.toJson());
                }
            }
            propertiesJson.put("testCaseProperties", testCasePropertiesJson);

            JSONArray testCaseInheritedPropertiesJson = new JSONArray();
            if (this.getTestCaseInheritedProperties() != null) {
                for (TestCaseCountryProperties testCaseCountryProperties : this.getTestCaseInheritedProperties()) {
                    testCaseInheritedPropertiesJson.put(testCaseCountryProperties.toJson());
                }
            }
            propertiesJson.put("inheritedProperties", testCaseInheritedPropertiesJson);
            testCaseJson.put("properties", propertiesJson);

        } catch (JSONException ex) {
            LOG.error(ex.toString(), ex);
        }
        return testCaseJson;
    }

}
