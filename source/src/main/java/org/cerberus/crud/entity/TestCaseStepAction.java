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

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * @author bcivel
 */
public class TestCaseStepAction {

    private String test;
    private String testCase;
    private int step;
    private int sequence;
    private int sort;
    private String conditionOperator;
    private String conditionVal1;
    private String conditionVal2;
    private String conditionVal3;
    private String action;
    private String value1;
    private String value2;
    private String value3;
    private String forceExeStatus;
    private String description;
    private String screenshotFilename;

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

    public String getForceExeStatus() {
        return forceExeStatus;
    }

    public void setForceExeStatus(String forceExeStatus) {
        this.forceExeStatus = forceExeStatus;
    }

    public int getSequence() {
        return sequence;
    }

    public void setSequence(int sequence) {
        this.sequence = sequence;
    }

    public int getSort() {
        return sort;
    }

    public void setSort(int sort) {
        this.sort = sort;
    }

    public int getStep() {
        return step;
    }

    public void setStep(int step) {
        this.step = step;
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

    public String getTestCase() {
        return testCase;
    }

    public void setTestCase(String testCase) {
        this.testCase = testCase;
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
        if ((this.testCase == null) ? (other.testCase != null) : !this.testCase.equals(other.testCase)) {
            return false;
        }
        if (this.step != other.step) {
            return false;
        }
        if (this.sequence != other.sequence) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 79 * hash + (this.test != null ? this.test.hashCode() : 0);
        hash = 79 * hash + (this.testCase != null ? this.testCase.hashCode() : 0);
        hash = 79 * hash + this.step;
        hash = 79 * hash + this.sequence;
        hash = 79 * hash + this.sort;
        hash = 79 * hash + (this.conditionOperator != null ? this.conditionOperator.hashCode() : 0);
        hash = 79 * hash + (this.conditionVal1 != null ? this.conditionVal1.hashCode() : 0);
        hash = 79 * hash + (this.conditionVal2 != null ? this.conditionVal2.hashCode() : 0);
        hash = 79 * hash + (this.conditionVal3 != null ? this.conditionVal3.hashCode() : 0);
        hash = 79 * hash + (this.action != null ? this.action.hashCode() : 0);
        hash = 79 * hash + (this.value1 != null ? this.value1.hashCode() : 0);
        hash = 79 * hash + (this.value2 != null ? this.value2.hashCode() : 0);
        hash = 79 * hash + (this.value3 != null ? this.value3.hashCode() : 0);
        hash = 79 * hash + (this.forceExeStatus != null ? this.forceExeStatus.hashCode() : 0);
        hash = 79 * hash + (this.description != null ? this.description.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
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
        if ((this.testCase == null) ? (other.testCase != null) : !this.testCase.equals(other.testCase)) {
            return false;
        }
        if (this.step != other.step) {
            return false;
        }
        if (this.sequence != other.sequence) {
            return false;
        }
        if (this.sort != other.sort) {
            return false;
        }
        if ((this.conditionOperator == null) ? (other.conditionOperator != null) : !this.conditionOperator.equals(other.conditionOperator)) {
            return false;
        }
        if ((this.conditionVal1 == null) ? (other.conditionVal1 != null) : !this.conditionVal1.equals(other.conditionVal1)) {
            return false;
        }
        if ((this.conditionVal2 == null) ? (other.conditionVal2 != null) : !this.conditionVal2.equals(other.conditionVal2)) {
            return false;
        }
        if ((this.conditionVal3 == null) ? (other.conditionVal3 != null) : !this.conditionVal3.equals(other.conditionVal3)) {
            return false;
        }
        if ((this.action == null) ? (other.action != null) : !this.action.equals(other.action)) {
            return false;
        }
        if ((this.value1 == null) ? (other.value1 != null) : !this.value1.equals(other.value1)) {
            return false;
        }
        if ((this.value2 == null) ? (other.value2 != null) : !this.value2.equals(other.value2)) {
            return false;
        }
        if ((this.value3 == null) ? (other.value3 != null) : !this.value3.equals(other.value3)) {
            return false;
        }
        if ((this.forceExeStatus == null) ? (other.forceExeStatus != null) : !this.forceExeStatus.equals(other.forceExeStatus)) {
            return false;
        }
        if ((this.description == null) ? (other.description != null) : !this.description.equals(other.description)) {
            return false;
        }
        if ((this.screenshotFilename == null) ? (other.screenshotFilename != null) : !this.screenshotFilename.equals(other.screenshotFilename)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "TestCaseStepAction{" + "test=" + test + ", testCase=" + testCase + ", step=" + step + ", sequence=" + sequence + ", action=" + action + ", object=" + value1 + ", property=" + value2 + ",value3= " + value3 + ", description=" + description + ", testCaseStepActionControl=" + controls + '}';
    }

    public JSONObject toJson() {
        JSONObject result = new JSONObject();
        try {
            result.put("sort", this.getSort());
            result.put("stepId", this.getStep());
            result.put("actionId", this.getSequence());
            result.put("description", this.getDescription());
            result.put("action", this.getAction());
            result.put("value1", this.getValue1());
            result.put("value2", this.getValue2());
            result.put("value3", this.getValue3());
            result.put("conditionOperator", this.getConditionOperator());
            result.put("conditionVal1", this.getConditionVal1());
            result.put("conditionVal2", this.getConditionVal2());
            result.put("conditionVal3", this.getConditionVal3());
            result.put("isFatal", this.getForceExeStatus());
            result.put("screenshotFilename", this.getScreenshotFilename());
            result.put("test", this.getTest());
            result.put("testcase", this.getTestCase());

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
}
