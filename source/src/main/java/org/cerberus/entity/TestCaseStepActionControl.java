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
package org.cerberus.entity;

/**
 * @author bcivel
 */
public class TestCaseStepActionControl {

    private String test;
    private String testCase;
    private int step;
    private int sequence;
    private int control;
    private String type;
    private String controlValue;
    private String controlProperty;
    private String fatal;

    public int getControl() {
        return control;
    }

    public void setControl(int control) {
        this.control = control;
    }

    public String getControlProperty() {
        return controlProperty;
    }

    public void setControlProperty(String controlProperty) {
        this.controlProperty = controlProperty;
    }

    public String getControlValue() {
        return controlValue;
    }

    public void setControlValue(String controlValue) {
        this.controlValue = controlValue;
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

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    private static final String PROPERTY_EQUAL = "PropertyIsEqualTo";
    private static final String PROPERTY_DIFFERENT = "PropertyIsDifferentFrom";
    private static final String PROPERTY_GREATER = "PropertyIsGreaterThan";
    private static final String PROPERTY_MINOR = "PropertyIsMinorThan";
    private static final String VERIFY_ELEMENTPRESENT = "verifyElementPresent";
    private static final String VERIFY_ELEMENTNOTPRESENT = "verifyElementNotPresent";
    private static final String VERIFY_ELEMENTVISIBLE = "verifyElementVisible";
    private static final String VERIFY_TEXT = "verifyText";
    private static final String VERIFY_TEXTPRESENT = "verifyTextPresent";
    private static final String VERIFY_TITLE = "verifytitle";
    private static final String VERIFY_URL = "verifyurl";


    public boolean isVerifyPropertyDifferent() {
        return this.getType().equalsIgnoreCase(PROPERTY_DIFFERENT);
    }

    public boolean isVerifyPropertyEqual() {
        return this.getType().equalsIgnoreCase(PROPERTY_EQUAL);
    }

    public boolean isVerifyPropertyGreater() {
        return this.getType().equalsIgnoreCase(PROPERTY_GREATER);
    }

    public boolean isVerifyPropertyMinor() {
        return this.getType().equalsIgnoreCase(PROPERTY_MINOR);
    }

    public boolean isVerifyElementPresent() {
        return this.getType().equalsIgnoreCase(VERIFY_ELEMENTPRESENT);
    }

    public boolean isVerifyElementNotPresent() {
        return this.getType().equalsIgnoreCase(VERIFY_ELEMENTNOTPRESENT);
    }

    public boolean isVerifyElementVisible() {
        return this.getType().equalsIgnoreCase(VERIFY_ELEMENTVISIBLE);
    }

    public boolean isVerifyText() {
        return this.getType().equalsIgnoreCase(VERIFY_TEXT);
    }

    public boolean isVerifyTextPresent() {
        return this.getType().equalsIgnoreCase(VERIFY_TEXTPRESENT);
    }

    public boolean isVerifyTitle() {
        return this.getType().equalsIgnoreCase(VERIFY_TITLE);
    }

    public boolean isVerifyUrl() {
        return this.getType().equalsIgnoreCase(VERIFY_URL);
    }
}
