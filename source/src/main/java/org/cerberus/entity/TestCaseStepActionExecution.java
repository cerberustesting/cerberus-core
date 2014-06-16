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
public class TestCaseStepActionExecution {

    private long id;
    private String test;
    private String testCase;
    private int step;
    private int sequence;
    private String returnCode;
    private String returnMessage;
    private String action;
    private String object;
    private String property;
    private long start;
    private long end;
    private long startLong;
    private long endLong;
    private String screenshotFilename;
    /**
     * From here are data outside database model.
     */
    private TestCaseStepAction testCaseStepAction;
    private TestCaseStepExecution testCaseStepExecution;
    private MessageEvent actionResultMessage;
    private MessageGeneral executionResultMessage;
    private String propertyName; // Property name is stored in order to keep track of the property name. property is replaced by the value of it.
    private boolean stopExecution;
    private List<TestCaseExecutionData> testCaseExecutionDataList; // Host the full list of data that was previously calculated and that will be used to calculate during the calculation of any property during the action.
    
    public String getPropertyName() {
        return propertyName;
    }

    public void setPropertyName(String propertyName) {
        this.propertyName = propertyName;
    }

    public List<TestCaseExecutionData> getTestCaseExecutionDataList() {
        return testCaseExecutionDataList;
    }

    public void setTestCaseExecutionDataList(List<TestCaseExecutionData> testCaseExecutionDataList) {
        this.testCaseExecutionDataList = testCaseExecutionDataList;
    }

    public MessageEvent getActionResultMessage() {
        return actionResultMessage;
    }

    public void setActionResultMessage(MessageEvent actionResultMessage) {
        this.actionResultMessage = actionResultMessage;
        if (actionResultMessage != null) {
            this.setReturnCode(actionResultMessage.getCodeString());
            this.setReturnMessage(actionResultMessage.getDescription());
        }
    }

    public MessageGeneral getExecutionResultMessage() {
        return executionResultMessage;
    }

    public void setExecutionResultMessage(MessageGeneral executionResultMessage) {
        this.executionResultMessage = executionResultMessage;
    }

    public boolean isStopExecution() {
        return stopExecution;
    }

    public void setStopExecution(boolean stopExecution) {
        this.stopExecution = stopExecution;
    }

    public TestCaseStepExecution getTestCaseStepExecution() {
        return testCaseStepExecution;
    }

    public void setTestCaseStepExecution(TestCaseStepExecution testCaseStepExecution) {
        this.testCaseStepExecution = testCaseStepExecution;
    }

    public TestCaseStepAction getTestCaseStepAction() {
        return testCaseStepAction;
    }

    public void setTestCaseStepAction(TestCaseStepAction testCaseStepAction) {
        this.testCaseStepAction = testCaseStepAction;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public long getEnd() {
        return end;
    }

    public void setEnd(long end) {
        this.end = end;
    }

    public long getEndLong() {
        return endLong;
    }

    public void setEndLong(long endLong) {
        this.endLong = endLong;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
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

    public String getReturnCode() {
        return returnCode;
    }

    public void setReturnCode(String returnCode) {
        this.returnCode = returnCode;
    }

    public String getReturnMessage() {
        return returnMessage;
    }

    public void setReturnMessage(String returnMessage) {
        this.returnMessage = returnMessage;
    }

    public String getScreenshotFilename() {
        return screenshotFilename;
    }

    public void setScreenshotFilename(String screenshotFilename) {
        this.screenshotFilename = screenshotFilename;
    }

    public int getSequence() {
        return sequence;
    }

    public void setSequence(int sequence) {
        this.sequence = sequence;
    }

    public long getStart() {
        return start;
    }

    public void setStart(long start) {
        this.start = start;
    }

    public long getStartLong() {
        return startLong;
    }

    public void setStartLong(long startLong) {
        this.startLong = startLong;
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
