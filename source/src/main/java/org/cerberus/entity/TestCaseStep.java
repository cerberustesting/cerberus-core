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

import java.util.List;

/**
 * @author bcivel
 */
public class TestCaseStep {

    private String test;
    private String testCase;
    private int step;
    private String description;
    private String useStep;  //  Y if the step use a step from another test 
    private String useStepTest; //  The test of the used step
    private String useStepTestCase;  // The testcase of the used step
    private Integer useStepStep;   //  the step used
    private List<TestCaseStepAction> testCaseStepAction;

    public String getUseStep() {
        return useStep;
    }

    public void setUseStep(String useStep) {
        this.useStep = useStep;
    }

    public String getUseStepTest() {
        return useStepTest;
    }

    public void setUseStepTest(String useStepTest) {
        this.useStepTest = useStepTest;
    }

    public String getUseStepTestCase() {
        return useStepTestCase;
    }

    public void setUseStepTestCase(String useStepTestCase) {
        this.useStepTestCase = useStepTestCase;
    }

    public Integer getUseStepStep() {
        return useStepStep;
    }

    public void setUseStepStep(Integer useStepStep) {
        this.useStepStep = useStepStep;
    }

    public List<TestCaseStepAction> getTestCaseStepAction() {
        return testCaseStepAction;
    }

    public void setTestCaseStepAction(List<TestCaseStepAction> testCaseStepAction) {
        this.testCaseStepAction = testCaseStepAction;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
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
}
