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

import java.sql.Timestamp;

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
@ToString
public class TestCaseStepActionControl {

    private static final Logger LOG = LogManager.getLogger(TestCaseStepActionControl.class);

    private String test;
    private String testcase;
    private int stepId;
    private int actionId;
    private int controlId;
    private int sort;
    private String conditionOperator;
    private String conditionValue1;
    private String conditionValue2;
    private String conditionValue3;
    private JSONArray conditionOptions;
    private String control;
    private String value1;
    private String value2;
    private String value3;
    private JSONArray options;
    private boolean isFatal;
    private String description;
    private String screenshotFilename;
    private boolean doScreenshotBefore;
    private boolean doScreenshotAfter;
    private int waitBefore;
    private int waitAfter;
    @EqualsAndHashCode.Exclude
    private String usrCreated;
    @EqualsAndHashCode.Exclude
    private Timestamp dateCreated;
    @EqualsAndHashCode.Exclude
    private String usrModif;
    @EqualsAndHashCode.Exclude
    private Timestamp dateModif;

    /**
     * Invariant CONTROL TYPE String.
     */
    public static final String CONTROL_UNKNOWN = "Unknown";
    public static final String CONTROL_VERIFYSTRINGEQUAL = "verifyStringEqual";
    public static final String CONTROL_VERIFYSTRINGDIFFERENT = "verifyStringDifferent";
    public static final String CONTROL_VERIFYSTRINGGREATER = "verifyStringGreater";
    public static final String CONTROL_VERIFYSTRINGMINOR = "verifyStringMinor";
    public static final String CONTROL_VERIFYSTRINGCONTAINS = "verifyStringContains";
    public static final String CONTROL_VERIFYSTRINGNOTCONTAINS = "verifyStringNotContains";
    public static final String CONTROL_VERIFYNUMERICEQUALS = "verifyNumericEquals";
    public static final String CONTROL_VERIFYNUMERICDIFFERENT = "verifyNumericDifferent";
    public static final String CONTROL_VERIFYNUMERICGREATER = "verifyNumericGreater";
    public static final String CONTROL_VERIFYNUMERICGREATEROREQUAL = "verifyNumericGreaterOrEqual";
    public static final String CONTROL_VERIFYNUMERICMINOR = "verifyNumericMinor";
    public static final String CONTROL_VERIFYNUMERICMINOROREQUAL = "verifyNumericMinorOrEqual";
    public static final String CONTROL_VERIFYELEMENTPRESENT = "verifyElementPresent";
    public static final String CONTROL_VERIFYELEMENTNOTPRESENT = "verifyElementNotPresent";
    public static final String CONTROL_VERIFYELEMENTVISIBLE = "verifyElementVisible";
    public static final String CONTROL_VERIFYELEMENTNOTVISIBLE = "verifyElementNotVisible";
    public static final String CONTROL_VERIFYELEMENTCHECKED = "verifyElementChecked";
    public static final String CONTROL_VERIFYELEMENTNOTCHECKED = "verifyElementNotChecked";
    public static final String CONTROL_VERIFYELEMENTEQUALS = "verifyElementEquals";
    public static final String CONTROL_VERIFYELEMENTDIFFERENT = "verifyElementDifferent";
    public static final String CONTROL_VERIFYELEMENTINELEMENT = "verifyElementInElement";
    public static final String CONTROL_VERIFYELEMENTCLICKABLE = "verifyElementClickable";
    public static final String CONTROL_VERIFYELEMENTNOTCLICKABLE = "verifyElementNotClickable";
    public static final String CONTROL_VERIFYELEMENTTEXTEQUAL = "verifyElementTextEqual";
    public static final String CONTROL_VERIFYELEMENTTEXTDIFFERENT = "verifyElementTextDifferent";
    public static final String CONTROL_VERIFYELEMENTTEXTCONTAINS = "verifyElementTextContains";
    public static final String CONTROL_VERIFYELEMENTTEXTNOTCONTAINS = "verifyElementTextNotContains";
    public static final String CONTROL_VERIFYELEMENTTEXTMATCHREGEX = "verifyElementTextMatchRegex";
    public static final String CONTROL_VERIFYSTRINGARRAYCONTAINS = "verifyStringArrayContains";
    public static final String CONTROL_VERIFYNUMERICARRAYCONTAINS = "verifyNumericArrayContains";
    public static final String CONTROL_VERIFYELEMENTTEXTARRAYCONTAINS = "verifyElementTextArrayContains";
    public static final String CONTROL_VERIFYELEMENTNUMERICARRAYCONTAINS = "verifyElementNumericArrayContains";
    public static final String CONTROL_VERIFYELEMENTNUMERICEQUAL = "verifyElementNumericEqual";
    public static final String CONTROL_VERIFYELEMENTNUMERICDIFFERENT = "verifyElementNumericDifferent";
    public static final String CONTROL_VERIFYELEMENTNUMERICGREATER = "verifyElementNumericGreater";
    public static final String CONTROL_VERIFYELEMENTNUMERICGREATEROREQUAL = "verifyElementNumericGreaterOrEqual";
    public static final String CONTROL_VERIFYELEMENTNUMERICMINOR = "verifyElementNumericMinor";
    public static final String CONTROL_VERIFYELEMENTNUMERICMINOROREQUAL = "verifyElementNumericMinorOrEqual";
    public static final String CONTROL_VERIFYTEXTINPAGE = "verifyTextInPage";
    public static final String CONTROL_VERIFYTEXTNOTINPAGE = "verifyTextNotInPage";
    public static final String CONTROL_VERIFYTITLEEQUAL = "verifyTitleEqual";
    public static final String CONTROL_VERIFYTITLEDIFFERENT = "verifyTitleDifferent";
    public static final String CONTROL_VERIFYTITLECONTAINS = "verifyTitleContains";
    public static final String CONTROL_VERIFYTITLENOTCONTAINS = "verifyTitleNotContains";
    public static final String CONTROL_VERIFYTITLEMATCHREGEX = "verifyTitleMatchRegex";
    public static final String CONTROL_VERIFYURLEQUAL = "verifyUrlEqual";
    public static final String CONTROL_VERIFYURLDIFFERENT = "verifyUrlDifferent";
    public static final String CONTROL_VERIFYURLCONTAINS = "verifyUrlContains";
    public static final String CONTROL_VERIFYURLNOTCONTAINS = "verifyUrlNotContains";
    public static final String CONTROL_VERIFYURLMATCHREGEX = "verifyUrlMatchRegex";
    public static final String CONTROL_VERIFYTEXTINDIALOG = "verifyTextInDialog";
    public static final String CONTROL_VERIFYXMLTREESTRUCTURE = "verifyXmlTreeStructure";
    public static final String CONTROL_TAKESCREENSHOT = "takeScreenshot";
    public static final String CONTROL_GETPAGESOURCE = "getPageSource";

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
    public JSONArray getOptions() {
        return options;
    }

    @JsonIgnore
    public JSONArray getOptionsActive() {
        JSONArray res = new JSONArray();
        for (int i = 0; i < options.length(); i++) {
            try {
                JSONObject jo = options.getJSONObject(i);
                if (jo.getBoolean("act")) {
                    res.put(jo);
                }
            } catch (JSONException ex) {
                LOG.error(ex);
            }
        }
        return res;
    }

    public boolean hasSameKey(TestCaseStepActionControl obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final TestCaseStepActionControl other = obj;
        if ((this.test == null) ? (other.test != null) : !this.test.equals(other.test)) {
            return false;
        }
        if ((this.testcase == null) ? (other.testcase != null) : !this.testcase.equals(other.testcase)) {
            return false;
        }
        if (this.stepId != other.stepId) {
            return false;
        }
        if (this.actionId != other.actionId) {
            return false;
        }
        if (this.controlId != other.controlId) {
            return false;
        }
        return true;
    }

    public JSONObject toJson() {
        JSONObject result = new JSONObject();
        try {
            result.put("sort", this.getSort());
            result.put("stepId", this.getStepId());
            result.put("actionId", this.getActionId());
            result.put("controlId", this.getControlId());
            result.put("description", this.getDescription());
            result.put("control", this.getControl());
            result.put("value1", this.getValue1());
            result.put("value2", this.getValue2());
            result.put("value3", this.getValue3());
            result.put("options", this.getOptions());
            result.put("conditionOperator", this.getConditionOperator());
            result.put("conditionValue1", this.getConditionValue1());
            result.put("conditionValue2", this.getConditionValue2());
            result.put("conditionValue3", this.getConditionValue3());
            result.put("conditionOptions", this.getConditionOptions());
            result.put("isFatal", this.isFatal());
            result.put("screenshotFilename", this.getScreenshotFilename());
            result.put("waitBefore", this.getWaitBefore());
            result.put("waitAfter", this.getWaitAfter());
            result.put("doScreenshotBefore", this.isDoScreenshotBefore());
            result.put("doScreenshotAfter", this.isDoScreenshotAfter());
            result.put("test", this.getTest());
            result.put("testcase", this.getTestcase());

        } catch (JSONException ex) {
            LOG.warn(ex);
        }
        return result;
    }

    public JSONObject toJsonV001() {
        JSONObject result = new JSONObject();
        try {
            result.put("JSONVersion", "001");
            result.put("sort", this.getSort());
            result.put("stepId", this.getStepId());
            result.put("actionId", this.getActionId());
            result.put("controlId", this.getControlId());
            result.put("description", this.getDescription());
            result.put("control", this.getControl());
            result.put("value1", this.getValue1());
            result.put("value2", this.getValue2());
            result.put("value3", this.getValue3());
            result.put("options", this.getOptions());
            result.put("conditionOperator", this.getConditionOperator());
            result.put("conditionValue1", this.getConditionValue1());
            result.put("conditionValue2", this.getConditionValue2());
            result.put("conditionValue3", this.getConditionValue3());
            result.put("conditionOptions", this.getConditionOptions());
            result.put("isFatal", this.isFatal());
            result.put("screenshotFilename", this.getScreenshotFilename());
            result.put("testFolder", this.getTest());
            result.put("testcase", this.getTestcase());

        } catch (JSONException ex) {
            LOG.warn(ex);
        }
        return result;
    }
}
