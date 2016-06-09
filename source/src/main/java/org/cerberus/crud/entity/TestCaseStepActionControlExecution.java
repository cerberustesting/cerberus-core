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

import java.util.logging.Level;
import java.util.logging.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * @author bcivel
 */
public class TestCaseStepActionControlExecution {

    private long id;
    private String test;
    private String testCase;
    private int step;
    private int sequence;
    private int control;
    private String returnCode;
    private String returnMessage;
    private String controlType;
    private String controlProperty;
    private String controlValue;
    private String fatal;
    private long start;
    private long end;
    private long startLong;
    private long endLong;
    private String screenshotFilename;
    private String pageSourceFilename;
    private String description;
    /**
     *
     */
    private TestCaseStepActionExecution testCaseStepActionExecution;
    private MessageEvent controlResultMessage;
    private MessageGeneral executionResultMessage;
    private boolean stopExecution;

    public MessageEvent getControlResultMessage() {
        return controlResultMessage;
    }

    public String getPageSourceFilename() {
        return pageSourceFilename;
    }

    public void setPageSourceFilename(String pageSourceFilename) {
        this.pageSourceFilename = pageSourceFilename;
    }

    public void setControlResultMessage(MessageEvent controlResultMessage) {
        this.controlResultMessage = controlResultMessage;
        if (controlResultMessage != null) {
            this.setReturnCode(controlResultMessage.getCodeString());
            this.setReturnMessage(controlResultMessage.getDescription());
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

    public TestCaseStepActionExecution getTestCaseStepActionExecution() {
        return testCaseStepActionExecution;
    }

    public void setTestCaseStepActionExecution(TestCaseStepActionExecution testCaseStepActionExecution) {
        this.testCaseStepActionExecution = testCaseStepActionExecution;
    }

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

    public String getControlType() {
        return controlType;
    }

    public void setControlType(String controlType) {
        this.controlType = controlType;
    }

    public String getControlValue() {
        return controlValue;
    }

    public void setControlValue(String controlValue) {
        this.controlValue = controlValue;
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

    public String getFatal() {
        return fatal;
    }

    public void setFatal(String fatal) {
        this.fatal = fatal;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
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
    
    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public JSONObject toJson() {
        JSONObject result = new JSONObject();
        try {
            result.put("id", this.getId());
            result.put("test", this.getTest());
            result.put("testcase", this.getTestCase());
            result.put("step", this.getStep());
            result.put("sequence", this.getSequence());
            result.put("control", this.getControl());
            result.put("controlType", this.getControlType());
            result.put("controlProperty", this.getControlProperty());
            result.put("controlValue", this.getControlValue());
            result.put("fatal", this.getFatal());
            result.put("start", this.getStart());
            result.put("end", this.getEndLong());
            result.put("startlong", this.getStartLong());
            result.put("endlong", this.getEnd());
            result.put("screenshotFilename", this.getScreenshotFilename());
            result.put("pageSourceFilename", this.getPageSourceFilename());
            result.put("description", this.getDescription());
            result.put("returnCode", this.getReturnCode());
            result.put("returnMessage", this.getReturnMessage());
        } catch (JSONException ex) {
            Logger.getLogger(TestCaseStepExecution.class.getName()).log(Level.SEVERE, null, ex);
        }
        return result;
    }
}
