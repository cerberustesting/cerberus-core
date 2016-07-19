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

import java.util.List;

/**
 * @author bcivel
 */
public class TestCaseStepAction {

    private String test;
    private String testCase;
    private int step;
    private int sequence;
    private int sort;
    private String action;
    private String object;
    private String property;
    private String forceExeStatus;
    private String description;
    private String screenshotFilename;

    /**
     * From here are data outside database model.
     */
    List<TestCaseStepActionControl> testCaseStepActionControl;

    /**
     * Invariant String.
     */
    public static final String ACTION_CICKANDWAIT = "clickAndWait";
    public static final String ACTION_DOUBLECLICK = "doubleClick";
    public static final String ACTION_KEYPRESS = "keypress";
    public static final String ACTION_SWITCHTOWINDOW = "switchToWindow";
    public static final String ACTION_MANAGEDILOG = "manageDialog";
    public static final String ACTION_UNKNOWN = "Unknown";
    public static final String ACTION_HIDEKEYBOARD = "hideKeyboard";
    public static final String ACTION_SWIPE = "swipe";
    public static final String ACTION_CALCULATEPROPERTY = "calculateProperty";
    public static final String ACTION_SKIPACTION = "skipAction";
    public static final String ACTION_CLICK = "click";
    public static final String ACTION_MOUSELEFTBUTTONPRESS = "mouseLeftButtonPress";
    public static final String ACTION_MOUSELEFTBUTTONRELEASE = "mouseLeftButtonRelease";
    public static final String ACTION_ENTER = "enter";
    public static final String ACTION_FOCUSTOIFRAME = "focusToIframe";
    public static final String ACTION_FOCUSDEFAULTIFRAME = "focusDefaultIframe";
    public static final String ACTION_MOUSEOVER = "mouseOver";
    public static final String ACTION_MOUSEOVERANDWAIT = "mouseOverAndWait";
    public static final String ACTION_OPENURLWITHBASE = "openUrlWithBase";
    public static final String ACTION_OPENURLLOGIN = "openUrlLogin";
    public static final String ACTION_OPENURL = "openUrl";
    public static final String ACTION_SELECT = "select";
    public static final String ACTION_SELECTANDWAIT = "selectAndWait";
    public static final String ACTION_TYPE = "type";
    public static final String ACTION_WAIT = "wait";
    public static final String ACTION_CALLSOAP = "callSoap";
    public static final String ACTION_CALLSOAPWITHBASE = "callSoapWithBase";
    public static final String ACTION_GETPAGESOURCE = "getPageSource";
    public static final String ACTION_REMOVEDIFFERENCE = "removeDifference";
    public static final String ACTION_EXECUTESQLUPDATE = "executeSqlUpdate";
    public static final String ACTION_EXECUTESQLSTOREPROCEDURE = "executeSqlStoredProcedure";
    public static final String ACTION_DONOTHING = "doNothing";
    public static final String FORCEEXESTATUS_PE = "PE";
    
    public String getScreenshotFilename() {
        return screenshotFilename;
    }

    public void setScreenshotFilename(String screenshotFilename) {
        this.screenshotFilename = screenshotFilename;
    }

    public List<TestCaseStepActionControl> getTestCaseStepActionControl() {
        return testCaseStepActionControl;
    }

    public void setTestCaseStepActionControl(List<TestCaseStepActionControl> testCaseStepActionControl) {
        this.testCaseStepActionControl = testCaseStepActionControl;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public String getObject() {
        return object;
    }

    public void setObject(String object) {
        this.object = object;
    }

    public String getProperty() {
        return property;
    }

    public void setProperty(String property) {
        this.property = property;
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
        hash = 79 * hash + (this.action != null ? this.action.hashCode() : 0);
        hash = 79 * hash + (this.object != null ? this.object.hashCode() : 0);
        hash = 79 * hash + (this.property != null ? this.property.hashCode() : 0);
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
        if ((this.action == null) ? (other.action != null) : !this.action.equals(other.action)) {
            return false;
        }
        if ((this.object == null) ? (other.object != null) : !this.object.equals(other.object)) {
            return false;
        }
        if ((this.property == null) ? (other.property != null) : !this.property.equals(other.property)) {
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
        return "TestCaseStepAction{" + "test=" + test + ", testCase=" + testCase + ", step=" + step + ", sequence=" + sequence + ", action=" + action + ", object=" + object + ", property=" + property + ", description=" + description + ", testCaseStepActionControl=" + testCaseStepActionControl + '}';
    }

}
