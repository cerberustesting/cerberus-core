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
@ToString
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
     * From here are data outside database model.
     */
    @EqualsAndHashCode.Exclude
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
    public static final String ACTION_RETURNPREVIOUSPAGE = "returnPreviousPage";
    public static final String ACTION_FORWARDNEXTPAGE = "forwardNextPage";
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
    public static final String ACTION_CLEANROBOTFILE = "cleanRobotFile";
    public static final String ACTION_UPLOADROBOTFILE = "uploadRobotFile";
    public static final String ACTION_GETROBOTFILE = "getRobotFile";
    public static final String ACTION_CALCULATEPROPERTY = "calculateProperty";
    public static final String ACTION_SETNETWORKTRAFFICCONTENT = "setNetworkTrafficContent";
    public static final String ACTION_INDEXNETWORKTRAFFIC = "indexNetworkTraffic";
    public static final String ACTION_SETCONSOLECONTENT = "setConsoleContent";
    public static final String ACTION_SETCONTENT = "setContent";
    public static final String ACTION_SETSERVICECALLCONTENT = "setServiceCallContent";
    public static final String ACTION_SWITCHTOCONTEXT = "switchToContext";
    public static final String ACTION_LOCKDEVICE = "lockDevice";
    public static final String ACTION_UNLOCKDEVICE = "unlockDevice";
    public static final String ACTION_ROTATEDEVICE = "rotateDevice";
    public static final String ACTION_DONOTHING = "doNothing";

    // ??? TODO. Clean this unused action.
    public static final String ACTION_PERFORMEDITORACTION = "performEditorAction";

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

    public boolean hasSameKey(TestCaseStepAction obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final TestCaseStepAction other = obj;
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
            result.put("waitBefore", this.getWaitBefore());
            result.put("waitAfter", this.getWaitAfter());
            result.put("doScreenshotBefore", this.isDoScreenshotBefore());
            result.put("doScreenshotAfter", this.isDoScreenshotAfter());
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
            result.put("JSONVersion", "001");
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
            result.put("testFolder", this.getTest());
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
