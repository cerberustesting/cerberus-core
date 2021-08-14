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
import java.sql.Timestamp;
import java.util.Objects;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * @author bcivel
 */
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
    private String usrCreated;
    private Timestamp dateCreated;
    private String usrModif;
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
    public static final String CONTROL_VERIFYELEMENTEQUALS = "verifyElementEquals";
    public static final String CONTROL_VERIFYELEMENTDIFFERENT = "verifyElementDifferent";
    public static final String CONTROL_VERIFYELEMENTINELEMENT = "verifyElementInElement";
    public static final String CONTROL_VERIFYELEMENTCLICKABLE = "verifyElementClickable";
    public static final String CONTROL_VERIFYELEMENTNOTCLICKABLE = "verifyElementNotClickable";
    public static final String CONTROL_VERIFYELEMENTTEXTEQUAL = "verifyElementTextEqual";
    public static final String CONTROL_VERIFYELEMENTTEXTDIFFERENT = "verifyElementTextDifferent";
    public static final String CONTROL_VERIFYELEMENTTEXTMATCHREGEX = "verifyElementTextMatchRegex";
    public static final String CONTROL_VERIFYELEMENTNUMERICEQUAL = "verifyElementNumericEqual";
    public static final String CONTROL_VERIFYELEMENTNUMERICDIFFERENT = "verifyElementNumericDifferent";
    public static final String CONTROL_VERIFYELEMENTNUMERICGREATER = "verifyElementNumericGreater";
    public static final String CONTROL_VERIFYELEMENTNUMERICGREATEROREQUAL = "verifyElementNumericGreaterOrEqual";
    public static final String CONTROL_VERIFYELEMENTNUMERICMINOR = "verifyElementNumericMinor";
    public static final String CONTROL_VERIFYELEMENTNUMERICMINOROREQUAL = "verifyElementNumericMinorOrEqual";
    public static final String CONTROL_VERIFYTEXTINPAGE = "verifyTextInPage";
    public static final String CONTROL_VERIFYTEXTNOTINPAGE = "verifyTextNotInPage";
    public static final String CONTROL_VERIFYTITLE = "verifyTitle";
    public static final String CONTROL_VERIFYURL = "verifyUrl";
    public static final String CONTROL_VERIFYTEXTINDIALOG = "verifyTextInDialog";
    public static final String CONTROL_VERIFYXMLTREESTRUCTURE = "verifyXmlTreeStructure";
    public static final String CONTROL_TAKESCREENSHOT = "takeScreenshot";
    public static final String CONTROL_GETPAGESOURCE = "getPageSource";
    /**
     * Invariant CONTROL TYPE String.
     */
    public static final String FATAL_YES = "Y";
    public static final String FATAL_NO = "N";

    public String getScreenshotFilename() {
        return screenshotFilename;
    }

    public void setScreenshotFilename(String screenshotFilename) {
        this.screenshotFilename = screenshotFilename;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public int getControlId() {
        return controlId;
    }

    public void setControlId(int control) {
        this.controlId = control;
    }

    public int getSort() {
        return sort;
    }

    public void setSort(int sort) {
        this.sort = sort;
    }

    public String getConditionOperator() {
        return conditionOperator;
    }

    public void setConditionOperator(String conditionOperator) {
        this.conditionOperator = conditionOperator;
    }

    public String getConditionValue1() {
        return conditionValue1;
    }

    public void setConditionValue1(String conditionValue1) {
        this.conditionValue1 = conditionValue1;
    }

    public String getConditionValue2() {
        return conditionValue2;
    }

    public void setConditionValue2(String conditionValue2) {
        this.conditionValue2 = conditionValue2;
    }

    public String getConditionValue3() {
        return conditionValue3;
    }

    public void setConditionValue3(String conditionValue3) {
        this.conditionValue3 = conditionValue3;
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

    public void setConditionOptions(JSONArray conditionOptions) {
        this.conditionOptions = conditionOptions;
    }

    public String getValue2() {
        return value2;
    }

    public void setValue2(String controlProperty) {
        this.value2 = controlProperty;
    }

    public String getValue1() {
        return value1;
    }

    public void setValue1(String controlValue) {
        this.value1 = controlValue;
    }

    public String getValue3() {
        return value3;
    }

    public void setValue3(String value3) {
        this.value3 = value3;
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

    public void setOptions(JSONArray options) {
        this.options = options;
    }

    public boolean isFatal() {
        return isFatal;
    }

    public void setFatal(boolean isFatal) {
        this.isFatal = isFatal;
    }

    public int getActionId() {
        return actionId;
    }

    public void setActionId(int actionId) {
        this.actionId = actionId;
    }

    public int getStepId() {
        return stepId;
    }

    public void setStepId(int stepId) {
        this.stepId = stepId;
    }

    public String getTest() {
        return test;
    }

    public void setTest(String test) {
        this.test = test;
    }

    public String getTestcase() {
        return testcase;
    }

    public void setTestcase(String testcase) {
        this.testcase = testcase;
    }

    public String getControl() {
        return control;
    }

    public void setControl(String type) {
        this.control = type;
    }

    public String getUsrCreated() {
        return usrCreated;
    }

    public void setUsrCreated(String usrCreated) {
        this.usrCreated = usrCreated;
    }

    public Timestamp getDateCreated() {
        return dateCreated;
    }

    public void setDateCreated(Timestamp dateCreated) {
        this.dateCreated = dateCreated;
    }

    public String getUsrModif() {
        return usrModif;
    }

    public void setUsrModif(String usrModif) {
        this.usrModif = usrModif;
    }

    public Timestamp getDateModif() {
        return dateModif;
    }

    public void setDateModif(Timestamp dateModif) {
        this.dateModif = dateModif;
    }

    public boolean hasSameKey(TestCaseStepActionControl obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final TestCaseStepActionControl other = (TestCaseStepActionControl) obj;
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

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 97 * hash + Objects.hashCode(this.test);
        hash = 97 * hash + Objects.hashCode(this.testcase);
        hash = 97 * hash + this.stepId;
        hash = 97 * hash + this.actionId;
        hash = 97 * hash + this.controlId;
        hash = 97 * hash + this.sort;
        hash = 97 * hash + Objects.hashCode(this.conditionOperator);
        hash = 97 * hash + Objects.hashCode(this.conditionValue1);
        hash = 97 * hash + Objects.hashCode(this.conditionValue2);
        hash = 97 * hash + Objects.hashCode(this.conditionValue3);
        hash = 97 * hash + Objects.hashCode(this.control);
        hash = 97 * hash + Objects.hashCode(this.value1);
        hash = 97 * hash + Objects.hashCode(this.value2);
        hash = 97 * hash + Objects.hashCode(this.value3);
        hash = 97 * hash + (this.isFatal ? 1 : 0);
        hash = 97 * hash + Objects.hashCode(this.description);
        hash = 97 * hash + Objects.hashCode(this.screenshotFilename);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final TestCaseStepActionControl other = (TestCaseStepActionControl) obj;
        if (this.stepId != other.stepId) {
            return false;
        }
        if (this.actionId != other.actionId) {
            return false;
        }
        if (this.controlId != other.controlId) {
            return false;
        }
        if (this.sort != other.sort) {
            return false;
        }
        if (this.isFatal != other.isFatal) {
            return false;
        }
        if (!Objects.equals(this.test, other.test)) {
            return false;
        }
        if (!Objects.equals(this.testcase, other.testcase)) {
            return false;
        }
        if (!Objects.equals(this.conditionOperator, other.conditionOperator)) {
            return false;
        }
        if (!Objects.equals(this.conditionValue1, other.conditionValue1)) {
            return false;
        }
        if (!Objects.equals(this.conditionValue2, other.conditionValue2)) {
            return false;
        }
        if (!Objects.equals(this.conditionValue3, other.conditionValue3)) {
            return false;
        }
        if (!Objects.equals(this.control, other.control)) {
            return false;
        }
        if (!Objects.equals(this.value1, other.value1)) {
            return false;
        }
        if (!Objects.equals(this.value2, other.value2)) {
            return false;
        }
        if (!Objects.equals(this.value3, other.value3)) {
            return false;
        }
        if (!Objects.equals(this.description, other.description)) {
            return false;
        }
        if (!Objects.equals(this.screenshotFilename, other.screenshotFilename)) {
            return false;
        }
        if (!Objects.equals(this.conditionOptions, other.conditionOptions)) {
            return false;
        }
        if (!Objects.equals(this.options, other.options)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "TestCaseStepActionControl{" + "test=" + test + ", testcase=" + testcase + ", stepId=" + stepId + ", actionId=" + actionId + ", controlId=" + controlId + ", sort=" + sort + ", conditionOperator=" + conditionOperator + ", conditionValue1=" + conditionValue1 + ", conditionValue2=" + conditionValue2 + ", conditionValue3=" + conditionValue3 + ", control=" + control + ", value1=" + value1 + ", value2=" + value2 + ", value3=" + value3 + ", isFatal=" + isFatal + ", description=" + description + ", screenshotFilename=" + screenshotFilename + ", usrCreated=" + usrCreated + ", dateCreated=" + dateCreated + ", usrModif=" + usrModif + ", dateModif=" + dateModif + '}';
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
            result.put("JSONVersion", "V001");
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
            result.put("test", this.getTest());
            result.put("testcase", this.getTestcase());

        } catch (JSONException ex) {
            LOG.warn(ex);
        }
        return result;
    }
}
