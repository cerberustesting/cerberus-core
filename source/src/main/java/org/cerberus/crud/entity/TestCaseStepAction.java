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
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;
import java.util.Objects;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * @author bcivel
 */
public class TestCaseStepAction {

    private static final Logger LOG = LogManager.getLogger(TestCaseStepAction.class);

    private String test;
    private String testcase;
    private int stepId;
    private int actionId;
    private int sort;
    private String conditionOperator;
    private String conditionValue1;
    private String conditionValue2;
    private String conditionValue3;
    private JSONArray conditionOptions;
    private String action;
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
     * From here are data outside database model.
     */
    List<TestCaseStepActionControl> controls;

    /**
     * Invariant ACTION String.
     */
    public static final String ACTION_UNKNOWN = "Unknown";
    public static final String ACTION_CLICK = "click";
    public static final String ACTION_LONGPRESS = "longPress";
    public static final String ACTION_MOUSELEFTBUTTONPRESS = "mouseLeftButtonPress";
    public static final String ACTION_MOUSELEFTBUTTONRELEASE = "mouseLeftButtonRelease";
    public static final String ACTION_MOUSEMOVE = "mouseMove";
    public static final String ACTION_DOUBLECLICK = "doubleClick";
    public static final String ACTION_RIGHTCLICK = "rightClick";
    public static final String ACTION_MOUSEOVER = "mouseOver";
    public static final String ACTION_FOCUSTOIFRAME = "focusToIframe";
    public static final String ACTION_FOCUSDEFAULTIFRAME = "focusDefaultIframe";
    public static final String ACTION_SWITCHTOWINDOW = "switchToWindow";
    public static final String ACTION_MANAGEDIALOG = "manageDialog";
    public static final String ACTION_MANAGEDIALOGKEYPRESS = "manageDialogKeypress";
    public static final String ACTION_OPENURLWITHBASE = "openUrlWithBase";
    public static final String ACTION_OPENURLLOGIN = "openUrlLogin";
    public static final String ACTION_OPENURL = "openUrl";
    public static final String ACTION_REFRESHCURRENTPAGE = "refreshCurrentPage";
    public static final String ACTION_EXECUTEJS = "executeJS";
    public static final String ACTION_EXECUTECOMMAND = "executeCommand";
    public static final String ACTION_EXECUTECERBERUSCOMMAND = "executeCerberusCommand";
    public static final String ACTION_OPENAPP = "openApp";
    public static final String ACTION_CLOSEAPP = "closeApp";
    public static final String ACTION_DRAGANDDROP = "dragAndDrop";
    public static final String ACTION_SELECT = "select";
    public static final String ACTION_KEYPRESS = "keypress";
    public static final String ACTION_TYPE = "type";
    public static final String ACTION_CLEARFIELD = "clearField";
    public static final String ACTION_HIDEKEYBOARD = "hideKeyboard";
    public static final String ACTION_SWIPE = "swipe";
    public static final String ACTION_SCROLLTO = "scrollTo";
    public static final String ACTION_INSTALLAPP = "installApp";
    public static final String ACTION_REMOVEAPP = "removeApp";
    public static final String ACTION_WAIT = "wait";
    public static final String ACTION_WAITVANISH = "waitVanish";
    public static final String ACTION_WAITNETWORKTRAFFICIDLE = "waitNetworkTrafficIdle";
    public static final String ACTION_CALLSERVICE = "callService";
    public static final String ACTION_EXECUTESQLUPDATE = "executeSqlUpdate";
    public static final String ACTION_EXECUTESQLSTOREPROCEDURE = "executeSqlStoredProcedure";
    public static final String ACTION_CALCULATEPROPERTY = "calculateProperty";
    public static final String ACTION_SETNETWORKTRAFFICCONTENT = "setNetworkTrafficContent";
    public static final String ACTION_INDEXNETWORKTRAFFIC = "indexNetworkTraffic";
    public static final String ACTION_SETCONSOLECONTENT = "setConsoleContent";
    public static final String ACTION_SETCONTENT = "setContent";
    public static final String ACTION_SETSERVICECALLCONTENT = "setServiceCallContent";
    public static final String ACTION_DONOTHING = "doNothing";

    // ??? TODO. Clean this unused action.
    public static final String ACTION_PERFORMEDITORACTION = "performEditorAction";

    // DEPRECATED
    public static final String ACTION_REMOVEDIFFERENCE = "removeDifference";
    public static final String ACTION_MOUSEOVERANDWAIT = "mouseOverAndWait";

    /**
     * Invariant FORCEEXESTATUS String.
     */
    public static final String FORCEEXESTATUS_PE = "PE";
    /**
     * Invariant CONDITIONOPERATOR String.
     */
    public static final String CONDITIONOPERATOR_ALWAYS = "always";
    public static final String CONDITIONOPERATOR_IFELEMENTPRESENT = "ifElementPresent";
    public static final String CONDITIONOPERATOR_IFELEMENTNOTPRESENT = "ifElementNotPresent";
    public static final String CONDITIONOPERATOR_IFELEMENTVISIBLE = "ifElementVisible";
    public static final String CONDITIONOPERATOR_IFELEMENTNOTVISIBLE = "ifElementNotVisible";
    public static final String CONDITIONOPERATOR_IFPROPERTYEXIST = "ifPropertyExist";
    public static final String CONDITIONOPERATOR_IFPROPERTYNOTEXIST = "ifPropertyNotExist";
    public static final String CONDITIONOPERATOR_IFNUMERICEQUAL = "ifNumericEqual";
    public static final String CONDITIONOPERATOR_IFNUMERICDIFFERENT = "ifNumericDifferent";
    public static final String CONDITIONOPERATOR_IFNUMERICGREATER = "ifNumericGreater";
    public static final String CONDITIONOPERATOR_IFNUMERICGREATEROREQUAL = "ifNumericGreaterOrEqual";
    public static final String CONDITIONOPERATOR_IFNUMERICMINOR = "ifNumericMinor";
    public static final String CONDITIONOPERATOR_IFNUMERICMINOROREQUAL = "ifNumericMinorOrEqual";
    public static final String CONDITIONOPERATOR_IFSTRINGEQUAL = "ifStringEqual";
    public static final String CONDITIONOPERATOR_IFSTRINGDIFFERENT = "ifStringDifferent";
    public static final String CONDITIONOPERATOR_IFSTRINGGREATER = "ifStringGreater";
    public static final String CONDITIONOPERATOR_IFSTRINGMINOR = "ifStringMinor";
    public static final String CONDITIONOPERATOR_IFSTRINGCONTAINS = "ifStringContains";
    public static final String CONDITIONOPERATOR_IFSTRINGNOTCONTAINS = "ifStringNotContains";
    public static final String CONDITIONOPERATOR_IFTEXTINELEMENT = "ifTextInElement";
    public static final String CONDITIONOPERATOR_IFTEXTNOTINELEMENT = "ifTextNotInElement";
    public static final String CONDITIONOPERATOR_NEVER = "never";

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

    public String getScreenshotFilename() {
        return screenshotFilename;
    }

    public void setScreenshotFilename(String screenshotFilename) {
        this.screenshotFilename = screenshotFilename;
    }

    public List<TestCaseStepActionControl> getControls() {
        return controls;
    }

    public void setControls(List<TestCaseStepActionControl> controls) {
        this.controls = controls;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public String getValue1() {
        return value1;
    }

    public void setValue1(String value1) {
        this.value1 = value1;
    }

    public String getValue2() {
        return value2;
    }

    public void setValue2(String value2) {
        this.value2 = value2;
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

    public int getSort() {
        return sort;
    }

    public void setSort(int sort) {
        this.sort = sort;
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

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getTestcase() {
        return testcase;
    }

    public void setTestcase(String testcase) {
        this.testcase = testcase;
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

    public boolean hasSameKey(TestCaseStepAction obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final TestCaseStepAction other = (TestCaseStepAction) obj;
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
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 97 * hash + Objects.hashCode(this.test);
        hash = 97 * hash + Objects.hashCode(this.testcase);
        hash = 97 * hash + this.stepId;
        hash = 97 * hash + this.actionId;
        hash = 97 * hash + this.sort;
        hash = 97 * hash + Objects.hashCode(this.conditionOperator);
        hash = 97 * hash + Objects.hashCode(this.conditionValue1);
        hash = 97 * hash + Objects.hashCode(this.conditionValue2);
        hash = 97 * hash + Objects.hashCode(this.conditionValue3);
        hash = 97 * hash + Objects.hashCode(this.action);
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
        final TestCaseStepAction other = (TestCaseStepAction) obj;
        if (this.stepId != other.stepId) {
            return false;
        }
        if (this.actionId != other.actionId) {
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
        if (!Objects.equals(this.action, other.action)) {
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
        if (!Objects.equals(this.options, other.options)) {
            return false;
        }
        if (!Objects.equals(this.conditionOptions, other.conditionOptions)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "TestCaseStepAction{" + "test=" + test + ", testcase=" + testcase + ", stepId=" + stepId + ", actionId=" + actionId + ", sort=" + sort + ", conditionOperator=" + conditionOperator + ", conditionValue1=" + conditionValue1 + ", conditionValue2=" + conditionValue2 + ", conditionValue3=" + conditionValue3 + ", action=" + action + ", value1=" + value1 + ", value2=" + value2 + ", value3=" + value3 + ", isFatal=" + isFatal + ", description=" + description + ", screenshotFilename=" + screenshotFilename + ", usrCreated=" + usrCreated + ", dateCreated=" + dateCreated + ", usrModif=" + usrModif + ", dateModif=" + dateModif + ", controls=" + controls + '}';
    }

    public JSONObject toJson() {
        JSONObject result = new JSONObject();
        try {
            result.put("sort", this.getSort());
            result.put("stepId", this.getStepId());
            result.put("actionId", this.getActionId());
            result.put("description", this.getDescription());
            result.put("action", this.getAction());
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

            JSONArray controlsJson = new JSONArray();
            if (this.getControls() != null) {
                for (TestCaseStepActionControl control : this.getControls()) {
                    controlsJson.put(control.toJson());
                }
            }
            result.put("controls", controlsJson);

        } catch (JSONException ex) {
            Logger LOG = LogManager.getLogger(TestCaseStepAction.class);
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
            result.put("description", this.getDescription());
            result.put("action", this.getAction());
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

            JSONArray controlsJson = new JSONArray();
            if (this.getControls() != null) {
                for (TestCaseStepActionControl control : this.getControls()) {
                    controlsJson.put(control.toJsonV001());
                }
            }
            result.put("controls", controlsJson);

        } catch (JSONException ex) {
            Logger LOG = LogManager.getLogger(TestCaseStepAction.class);
            LOG.warn(ex);
        }
        return result;
    }
}
