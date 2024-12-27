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

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import javax.persistence.Entity;
import javax.persistence.Id;
import java.sql.Timestamp;
import java.util.List;

/**
 * @author bcivel
 */
@Data
@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class TestCaseStep {

    @Id
    private String test;
    @Id
    private String testcase;
    @Id
    private int stepId;
    private int sort;
    private String loop;
    private String conditionOperator;
    private String conditionValue1;
    private String conditionValue2;
    private String conditionValue3;
    private JSONArray conditionOptions;
    private String description;
    private boolean isUsingLibraryStep;  //  true if the stepId use a stepId from another test
    private String libraryStepTest; //  The test of the used stepId
    private String libraryStepTestcase;  // The testcase of the used stepId
    private Integer libraryStepStepId;   //  the stepId of the original step
    private boolean isLibraryStep;
    private boolean isExecutionForced;
    private String usrCreated;
    @EqualsAndHashCode.Exclude
    private String dateCreated;
    @EqualsAndHashCode.Exclude
    private String usrModif;
    @EqualsAndHashCode.Exclude
    private Timestamp dateModif;

    /**
     * Not included in table.
     */
    @EqualsAndHashCode.Exclude
    private List<TestCaseStepAction> actions;
    @EqualsAndHashCode.Exclude
    private boolean isStepInUseByOtherTestcase;

    // Properties belonging to the testcase owning the step. Used by the public API
    @EqualsAndHashCode.Exclude
    private List<TestCaseCountryProperties> properties;

    //    private int initialStep;
    @EqualsAndHashCode.Exclude
    private TestCase testcaseObj;
    @EqualsAndHashCode.Exclude
    private int libraryStepSort; // Contain the sort information of the library.

    /**
     * Invariant PROPERTY TYPE String.
     */
    public static final String LOOP_ONCEIFCONDITIONTRUE = "onceIfConditionTrue";
    public static final String LOOP_ONCEIFCONDITIONFALSE = "onceIfConditionFalse";
    public static final String LOOP_DOWHILECONDITIONTRUE = "doWhileConditionTrue";
    public static final String LOOP_DOWHILECONDITIONFALSE = "doWhileConditionFalse";
    public static final String LOOP_WHILECONDITIONTRUEDO = "whileConditionTrueDo";
    public static final String LOOP_WHILECONDITIONFALSEDO = "whileConditionFalseDo";

    private static final Logger LOG = LogManager.getLogger(TestCaseStep.class);

    public void appendActions(TestCaseStepAction action) {
        this.actions.add(action);
    }

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

    public boolean hasSameKey(TestCaseStep obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        if ((this.test == null) ? (obj.test != null) : !this.test.equals(obj.test)) {
            return false;
        }
        if ((this.testcase == null) ? (obj.testcase != null) : !this.testcase.equals(obj.testcase)) {
            return false;
        }
        return this.stepId == obj.stepId;
    }

    public JSONObject toJson() {
        JSONObject stepJson = new JSONObject();
        try {
            stepJson.put("sort", this.getSort());
            stepJson.put("stepId", this.getStepId());
            stepJson.put("description", this.getDescription());
            stepJson.put("isExecutionForced", this.isExecutionForced());
            stepJson.put("loop", this.getLoop());
            stepJson.put("conditionOperator", this.getConditionOperator());
            stepJson.put("conditionValue1", this.getConditionValue1());
            stepJson.put("conditionValue2", this.getConditionValue2());
            stepJson.put("conditionValue3", this.getConditionValue3());
            stepJson.put("conditionOptions", this.getConditionOptions());
            stepJson.put("isUsingLibraryStep", this.isUsingLibraryStep());
            stepJson.put("isLibraryStep", this.isLibraryStep());
            stepJson.put("libraryStepTest", this.getLibraryStepTest());
            stepJson.put("libraryStepTestCase", this.getLibraryStepTestcase());
            stepJson.put("libraryStepStepId", this.getLibraryStepStepId());
            stepJson.put("libraryStepSort", this.getLibraryStepSort());
            stepJson.put("isStepInUseByOtherTestCase", this.isStepInUseByOtherTestcase());
            stepJson.put("test", this.getTest());
            stepJson.put("testcase", this.getTestcase());
//            stepJson.put("initialStep", this.getInitialStep());
            stepJson.put("usrCreated", this.usrCreated);
            stepJson.put("dateCreated", this.dateCreated);
            stepJson.put("usrModif", this.usrModif);
            stepJson.put("dateModif", this.dateModif);
            if (this.testcaseObj != null) {
                stepJson.put("testcaseObj", this.testcaseObj.toJson());
            }
            JSONArray stepsJson = new JSONArray();
            if (this.getActions() != null) {
                for (TestCaseStepAction action : this.getActions()) {
                    stepsJson.put(action.toJson());
                }
            }
            stepJson.put("actions", stepsJson);

        } catch (JSONException ex) {
            LOG.warn(ex);
        }
        return stepJson;
    }

    public JSONObject toJsonV001() {
        JSONObject stepJson = new JSONObject();
        try {
            stepJson.put("JSONVersion", "001");
            stepJson.put("sort", this.getSort());
            stepJson.put("stepId", this.getStepId());
            stepJson.put("description", this.getDescription());
            stepJson.put("isExecutionForced", this.isExecutionForced());
            stepJson.put("loop", this.getLoop());
            stepJson.put("conditionOperator", this.getConditionOperator());
            stepJson.put("conditionValue1", this.getConditionValue1());
            stepJson.put("conditionValue2", this.getConditionValue2());
            stepJson.put("conditionValue3", this.getConditionValue3());
            stepJson.put("conditionOptions", this.getConditionOptions());
            stepJson.put("isUsingLibraryStep", this.isUsingLibraryStep());
            stepJson.put("isLibraryStep", this.isLibraryStep());
            stepJson.put("libraryStepTestFolder", this.getLibraryStepTest());
            stepJson.put("libraryStepTestcase", this.getLibraryStepTestcase());
            stepJson.put("libraryStepStepId", this.getLibraryStepStepId());
            stepJson.put("libraryStepSort", this.getLibraryStepSort());
            stepJson.put("isStepInUseByOtherTestcase", this.isStepInUseByOtherTestcase());
            stepJson.put("testFolder", this.getTest());
            stepJson.put("testcase", this.getTestcase());
//            stepJson.put("initialStep", this.getInitialStep());
            stepJson.put("usrCreated", this.usrCreated);
            stepJson.put("dateCreated", this.dateCreated);
            stepJson.put("usrModif", this.usrModif);
            stepJson.put("dateModif", this.dateModif);

            JSONArray stepsJson = new JSONArray();
            if (this.getActions() != null) {
                for (TestCaseStepAction action : this.getActions()) {
                    stepsJson.put(action.toJsonV001());
                }
            }
            stepJson.put("actions", stepsJson);

        } catch (JSONException ex) {
            LOG.warn(ex);
        }
        return stepJson;
    }

}
