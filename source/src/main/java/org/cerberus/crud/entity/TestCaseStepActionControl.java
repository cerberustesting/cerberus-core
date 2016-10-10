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

/**
 * @author bcivel
 */
public class TestCaseStepActionControl {

    private String test;
    private String testCase;
    private int step;
    private int sequence;
    private int controlSequence;
    private int sort;
    private String control;
    private String value1;
    private String value2;
    private String fatal;
    private String description;
    private String screenshotFilename;

    /**
     * Invariant CONTROL TYPE String.
     */
    public static final String CONTROL_UNKNOWN = "Unknown";
    public static final String CONTROL_VERIFYSTRINGEQUAL = "verifyStringEqual";
    public static final String CONTROL_VERIFYSTRINGDIFFERENT = "verifyStringDifferent";
    public static final String CONTROL_VERIFYSTRINGGREATER = "verifyStringGreater";
    public static final String CONTROL_VERIFYSTRINGMINOR = "verifyStringMinor";
    public static final String CONTROL_VERIFYSTRINGCONTAINS = "verifyStringContains";
    public static final String CONTROL_VERIFYINTEGEREQUALS = "verifyIntegerEquals";
    public static final String CONTROL_VERIFYINTEGERDIFFERENT = "verifyIntegerDifferent";
    public static final String CONTROL_VERIFYINTEGERGREATER = "verifyIntegerGreater";
    public static final String CONTROL_VERIFYINTEGERMINOR = "verifyIntegerMinor";
    public static final String CONTROL_VERIFYELEMENTPRESENT = "verifyElementPresent";
    public static final String CONTROL_VERIFYELEMENTNOTPRESENT = "verifyElementNotPresent";
    public static final String CONTROL_VERIFYELEMENTVISIBLE = "verifyElementVisible";
    public static final String CONTROL_VERIFYELEMENTNOTVISIBLE = "verifyElementNotVisible";
    public static final String CONTROL_VERIFYELEMENTEQUALS = "verifyElementEquals";
    public static final String CONTROL_VERIFYELEMENTDIFFERENT = "verifyElementDifferent";
    public static final String CONTROL_VERIFYELEMENTINELEMENT = "verifyElementInElement";
    public static final String CONTROL_VERIFYELEMENTCLICKABLE = "verifyElementClickable";
    public static final String CONTROL_VERIFYELEMENTNOTCLICKABLE = "verifyElementNotClickable";
    public static final String CONTROL_VERIFYTEXTINELEMENT = "verifyTextInElement";
    public static final String CONTROL_VERIFYTEXTNOTINELEMENT = "verifyTextNotInElement";
    public static final String CONTROL_VERIFYREGEXINELEMENT = "verifyRegexInElement";
    public static final String CONTROL_VERIFYTEXTINPAGE = "verifyTextInPage";
    public static final String CONTROL_VERIFYTEXTNOTINPAGE = "verifyTextNotInPage";
    public static final String CONTROL_VERIFYTITLE = "verifyTitle";
    public static final String CONTROL_VERIFYURL = "verifyUrl";
    public static final String CONTROL_VERIFYTEXTINDIALOG = "verifyTextInDialog";
    public static final String CONTROL_VERIFYXMLTREESTRUCTURE = "verifyXmlTreeStructure";
    public static final String CONTROL_TAKESCREENSHOT = "takeScreenshot";
    public static final String CONTROL_GETPAGESOURCE = "getPageSource";
    public static final String CONTROL_SKIPCONTROL = "skipControl";
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

    public int getControlSequence() {
        return controlSequence;
    }

    public void setControlSequence(int control) {
        this.controlSequence = control;
    }

    public int getSort() {
        return sort;
    }

    public void setSort(int sort) {
        this.sort = sort;
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

    public String getFatal() {
        return fatal;
    }

    public void setFatal(String fatal) {
        this.fatal = fatal;
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

    public String getTestCase() {
        return testCase;
    }

    public void setTestCase(String testCase) {
        this.testCase = testCase;
    }

    public String getControl() {
        return control;
    }

    public void setControl(String type) {
        this.control = type;
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
        if ((this.testCase == null) ? (other.testCase != null) : !this.testCase.equals(other.testCase)) {
            return false;
        }
        if (this.step != other.step) {
            return false;
        }
        if (this.sequence != other.sequence) {
            return false;
        }
        if (this.controlSequence != other.controlSequence) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 67 * hash + (this.test != null ? this.test.hashCode() : 0);
        hash = 67 * hash + (this.testCase != null ? this.testCase.hashCode() : 0);
        hash = 67 * hash + this.step;
        hash = 67 * hash + this.sequence;
        hash = 67 * hash + this.controlSequence;
        hash = 67 * hash + this.sort;
        hash = 67 * hash + (this.control != null ? this.control.hashCode() : 0);
        hash = 67 * hash + (this.value1 != null ? this.value1.hashCode() : 0);
        hash = 67 * hash + (this.value2 != null ? this.value2.hashCode() : 0);
        hash = 67 * hash + (this.fatal != null ? this.fatal.hashCode() : 0);
        hash = 67 * hash + (this.description != null ? this.description.hashCode() : 0);
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
        final TestCaseStepActionControl other = (TestCaseStepActionControl) obj;
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
        if (this.controlSequence != other.controlSequence) {
            return false;
        }
        if (this.sort != other.sort) {
            return false;
        }
        if ((this.control == null) ? (other.control != null) : !this.control.equals(other.control)) {
            return false;
        }
        if ((this.value1 == null) ? (other.value1 != null) : !this.value1.equals(other.value1)) {
            return false;
        }
        if ((this.value2 == null) ? (other.value2 != null) : !this.value2.equals(other.value2)) {
            return false;
        }
        if ((this.fatal == null) ? (other.fatal != null) : !this.fatal.equals(other.fatal)) {
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
        return "TestCaseStepActionControl{" + "test=" + test + ", testCase=" + testCase + ", step=" + step + ", sequence=" + sequence + ", control=" + controlSequence + ", type=" + control + ", controlValue=" + value1 + ", controlProperty=" + value2 + ", fatal=" + fatal + ", description=" + description + '}';
    }

}
