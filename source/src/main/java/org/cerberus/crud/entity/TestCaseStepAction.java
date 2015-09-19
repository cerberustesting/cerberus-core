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
    private String action;
    private String object;
    private String property;
    private String description;
    private String screenshotFilename;

    public String getScreenshotFilename() {
        return screenshotFilename;
    }

    public void setScreenshotFilename(String screenshotFilename) {
        this.screenshotFilename = screenshotFilename;
    }
    
    List<TestCaseStepActionControl> testCaseStepActionControl;

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

    public int getSequence() {
        return sequence;
    }

    public void setSequence(int sequence) {
        this.sequence = sequence;
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
    private static final String SELENIUM_CLICK = "click";
    private static final String SELENIUM_CLICK_WAIT = "clickAndWait";
    private static final String SELENIUM_DOUBLECLICK = "doubleClick";
    private static final String SELENIUM_ENTER = "enter";
    private static final String SELENIUM_KEYPRESS = "keypress";
    private static final String SELENIUM_MOUSEOVER = "mouseOver";
    private static final String SELENIUM_MOUSEOVERANDWAIT = "mouseOverAndWait";
    private static final String SELENIUM_OPENURL = "openUrlWithBase";
    private static final String SELENIUM_SELECT = "select";
    private static final String SELENIUM_SELECTWAIT = "selectAndWait";
    private static final String SELENIUM_TYPE = "type";
    private static final String SELENIUM_URLLOGIN = "openUrlLogin";
    private static final String SELENIUM_WAIT = "wait";
    private static final String ACTION_CALCULATEPROPERTY = "calculateProperty";

    public boolean isSeleniumClick() {
        return this.getAction().equalsIgnoreCase(SELENIUM_CLICK);
    }

    public boolean isSeleniumClickAndWait() {
        return this.getAction().equalsIgnoreCase(SELENIUM_CLICK_WAIT);
    }

    public boolean isSeleniumDoubleClick() {
        return this.getAction().equalsIgnoreCase(SELENIUM_DOUBLECLICK);
    }

    public boolean isSeleniumEnter() {
        return this.getAction().equalsIgnoreCase(SELENIUM_ENTER);
    }

    public boolean isSeleniumKeypress() {
        return this.getAction().equalsIgnoreCase(SELENIUM_KEYPRESS);
    }

    public boolean isSeleniumMouseOver() {
        return this.getAction().equalsIgnoreCase(SELENIUM_MOUSEOVER);
    }

    public boolean isSeleniumMouseOverAndWait() {
        return this.getAction().equalsIgnoreCase(SELENIUM_MOUSEOVERANDWAIT);
    }

    public boolean isSeleniumOpenURL() {
        return this.getAction().equalsIgnoreCase(SELENIUM_OPENURL);
    }

    public boolean isSeleniumSelect() {
        return this.getAction().equalsIgnoreCase(SELENIUM_SELECT);
    }

    public boolean isSeleniumSelectAndWait() {
        return this.getAction().equalsIgnoreCase(SELENIUM_SELECTWAIT);
    }

    public boolean isSeleniumType() {
        return this.getAction().equalsIgnoreCase(SELENIUM_TYPE);
    }

    public boolean isSeleniumUrlLogin() {
        return this.getAction().equalsIgnoreCase(SELENIUM_URLLOGIN);
    }

    public boolean isSeleniumWait() {
        return this.getAction().equalsIgnoreCase(SELENIUM_WAIT);
    }

    public boolean isCalculateProperty() {
        return this.getAction().equalsIgnoreCase(ACTION_CALCULATEPROPERTY);
    }

    public boolean isSeleniumAction() {
        return this.getAction().equalsIgnoreCase(SELENIUM_CLICK) || this.getAction().equalsIgnoreCase(SELENIUM_CLICK_WAIT)
                || this.getAction().equalsIgnoreCase(SELENIUM_DOUBLECLICK) || this.getAction().equalsIgnoreCase(SELENIUM_ENTER)
                || this.getAction().equalsIgnoreCase(SELENIUM_KEYPRESS) || this.getAction().equalsIgnoreCase(SELENIUM_OPENURL)
                || this.getAction().equalsIgnoreCase(SELENIUM_MOUSEOVER) || this.getAction().equalsIgnoreCase(SELENIUM_MOUSEOVERANDWAIT)
                || this.getAction().equalsIgnoreCase(SELENIUM_TYPE) || this.getAction().equalsIgnoreCase(SELENIUM_WAIT)
                || this.getAction().equalsIgnoreCase(SELENIUM_SELECTWAIT) || this.getAction().equalsIgnoreCase(SELENIUM_URLLOGIN);
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
        hash = 79 * hash + (this.action != null ? this.action.hashCode() : 0);
        hash = 79 * hash + (this.object != null ? this.object.hashCode() : 0);
        hash = 79 * hash + (this.property != null ? this.property.hashCode() : 0);
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
        if ((this.action == null) ? (other.action != null) : !this.action.equals(other.action)) {
            return false;
        }
        if ((this.object == null) ? (other.object != null) : !this.object.equals(other.object)) {
            return false;
        }
        if ((this.property == null) ? (other.property != null) : !this.property.equals(other.property)) {
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
