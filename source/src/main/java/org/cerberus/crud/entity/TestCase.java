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
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.sql.Timestamp;
import java.util.List;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.cerberus.util.StringUtil;

/**
 * @author bcivel
 */
@Getter
@Setter
@EqualsAndHashCode
@ToString
public class TestCase {

    private static final Logger LOG = LogManager.getLogger(TestCase.class);

    private String test;
    private String testcase;
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
    private String conditionValue1;
    private String conditionValue2;
    private String conditionValue3;
    private JSONArray conditionOptions;
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
    private Timestamp dateCreated;
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
    private List<TestCaseLabel> testCaseLabels;
    private List<Label> labels;
    private List<TestCaseDep> dependencies;

    public static final String TESTCASE_TYPE_MANUAL = "MANUAL";
    public static final String TESTCASE_TYPE_AUTOMATED = "AUTOMATED";
    public static final String TESTCASE_TYPE_PRIVATE = "PRIVATE";

    @JsonIgnore
    public JSONArray getConditionOptions() {
        return conditionOptions;
    }

    @JsonIgnore
    public JSONArray getConditionOptionsActive() {
        JSONArray res = new JSONArray();
        for (int i = 0; i < conditionOptions.length(); i++) {
            try {
                JSONObject jo = conditionOptions.getJSONObject(i);
                if (jo.getBoolean("act")) {
                    res.put(jo);
                }
            } catch (JSONException ex) {
                LOG.error(ex);
            }
        }
        return res;
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

    public void appendTestCaseCountries(TestCaseCountry testCaseCountry) {
        this.testCaseCountries.add(testCaseCountry);
    }

    public void appendSteps(TestCaseStep step) {
        this.steps.add(step);
    }

    /**
     * Generate a unique key for a TestCase.
     *
     * @return
     */
    @JsonIgnore
    public String getKey() {
        return this.test + "##" + this.testcase;
    }

    public JSONObject toJson() {
        JSONObject testCaseJson = new JSONObject();
        try {
            testCaseJson.put("test", this.getTest());
            testCaseJson.put("testcase", this.getTestcase());
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
            testCaseJson.put("conditionValue1", this.getConditionValue1());
            testCaseJson.put("conditionValue2", this.getConditionValue2());
            testCaseJson.put("conditionValue3", this.getConditionValue3());
            testCaseJson.put("conditionOptions", this.getConditionOptions());
            testCaseJson.put("userAgent", this.getUserAgent());
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
                    if (country != null) {
                        countriesJson.put(country.toJson(true));
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

    public JSONObject toJsonV001(String cerberusURL, List<Invariant> prioritiesList) {
        JSONObject testCaseJson = new JSONObject();
        try {
            testCaseJson.put("JSONVersion", "001");
            cerberusURL = StringUtil.addSuffixIfNotAlready(cerberusURL, "/");
            testCaseJson.put("link", cerberusURL + "TestCaseScript.jsp?test=" + URLEncoder.encode(this.getTest(), "UTF-8") + "&testcase=" + URLEncoder.encode(this.getTestcase(), "UTF-8"));
            testCaseJson.put("testFolder", this.getTest());
            testCaseJson.put("testcase", this.getTestcase());
            testCaseJson.put("application", this.getApplication());
            testCaseJson.put("system", this.getSystem());
            testCaseJson.put("status", this.getStatus());
            testCaseJson.put("type", this.getType());

//            testCaseJson.put("priority", factoryInvariant.create(Invariant.IDNAME_PRIORITY, String.valueOf(this.getPriority()), 10, "", "", "", "", "", "", "", "", "", "", "").toJsonV001());
            testCaseJson.put("priority", this.getPriority());
            if (prioritiesList != null) {
                Invariant priorityLocal = prioritiesList.stream().filter(inv -> Integer.toString(this.getPriority()).equals(inv.getValue())).findAny().orElse(null);
                if (priorityLocal != null) {
                    testCaseJson.put("priority", priorityLocal.toJsonV001());
                }
            }

            testCaseJson.put("description", this.getDescription());
            testCaseJson.put("detailedDescription", this.getDetailedDescription());
            testCaseJson.put("isActive", this.isActive());
            testCaseJson.put("isActiveQA", this.isActiveQA());
            testCaseJson.put("isActiveUAT", this.isActiveUAT());
            testCaseJson.put("isActivePROD", this.isActivePROD());
            testCaseJson.put("bugs", this.getBugs());
            testCaseJson.put("comment", this.getComment());
            testCaseJson.put("implementer", this.getImplementer());
            testCaseJson.put("executor", this.getExecutor());
            testCaseJson.put("version", this.getVersion());
            testCaseJson.put("dateCreated", this.getDateCreated());
            testCaseJson.put("usrCreated", this.getUsrCreated());
            testCaseJson.put("dateModif", this.getDateModif());
            testCaseJson.put("usrModif", this.getUsrModif());
            testCaseJson.put("externalProvider", this.getOrigine());
            testCaseJson.put("externalReference", this.getRefOrigine());
            
            JSONArray stepsJson = new JSONArray();
            if (this.getSteps() != null) {
                for (TestCaseStep step : this.getSteps()) {
                    stepsJson.put(step.toJsonV001());
                }
            }
            testCaseJson.put("steps", stepsJson);

            JSONArray countriesJson = new JSONArray();
            if (this.getInvariantCountries() != null) {
                for (Invariant country : this.getInvariantCountries()) {
                    if (country != null) {
                        countriesJson.put(country.toJsonV001());
                    }
                }
            }
            testCaseJson.put("countries", countriesJson);

            JSONArray dependenciesJson = new JSONArray();
            if (this.getDependencies() != null) {
                for (TestCaseDep testCaseDependecy : this.getDependencies()) {
                    dependenciesJson.put(testCaseDependecy.toJsonV001());
                }
            }
            testCaseJson.put("dependencies", dependenciesJson);

            JSONArray labelsJson = new JSONArray();
            if (this.getLabels() != null) {
                for (Label label : this.getLabels()) {
                    labelsJson.put(label.toJsonV001());
                }
            }
            testCaseJson.put("labels", labelsJson);

            JSONObject propertiesJson = new JSONObject();
            JSONArray testCasePropertiesJson = new JSONArray();
            if (this.getTestCaseCountryProperties() != null) {
                for (TestCaseCountryProperties testCaseCountryProperties : this.getTestCaseCountryProperties()) {
                    testCasePropertiesJson.put(testCaseCountryProperties.toJsonV001());
                }
            }
            propertiesJson.put("testcaseProperties", testCasePropertiesJson);

            JSONArray testCaseInheritedPropertiesJson = new JSONArray();
            if (this.getTestCaseInheritedProperties() != null) {
                for (TestCaseCountryProperties testCaseCountryProperties : this.getTestCaseInheritedProperties()) {
                    testCaseInheritedPropertiesJson.put(testCaseCountryProperties.toJsonV001());
                }
            }
            propertiesJson.put("inheritedProperties", testCaseInheritedPropertiesJson);
            testCaseJson.put("properties", propertiesJson);
            
            

        } catch (JSONException | UnsupportedEncodingException ex) {
            LOG.error(ex.toString(), ex);
        }
        return testCaseJson;
    }

}
