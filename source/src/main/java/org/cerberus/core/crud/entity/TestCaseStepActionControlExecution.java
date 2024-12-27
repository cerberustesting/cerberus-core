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

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.cerberus.core.engine.entity.MessageGeneral;
import org.cerberus.core.engine.entity.MessageEvent;
import org.cerberus.core.util.DateUtil;
import org.cerberus.core.util.StringUtil;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

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
public class TestCaseStepActionControlExecution {

    private long id;
    private String test;
    private String testCase;
    private int stepId;
    private int index;
    private int sequence;
    private int controlSequence;
    private int sort;
    private String conditionOperator;
    private String conditionVal1Init;
    private String conditionVal2Init;
    private String conditionVal3Init;
    private String conditionVal1;
    private String conditionVal2;
    private String conditionVal3;
    private String control;
    private String value1Init;
    private String value2Init;
    private String value3Init;
    private String value1;
    private String value2;
    private String value3;
    private String fatal;
    private String description;
    private String returnCode;
    private String returnMessage;
    private long start;
    private long end;
    private long startLong;
    private long endLong;
    /**
     *
     */
    private TestCaseStepActionExecution testCaseStepActionExecution;
    private TestCaseStepActionControl testCaseStepActionControl;
    private List<TestCaseExecutionFile> fileList; // Host the list of the files stored at control level
    private MessageEvent controlResultMessage;
    private MessageGeneral executionResultMessage;
    private boolean stopExecution;
    private JSONArray conditionOptions;
    private JSONArray options;
    private boolean doScreenshotBefore;
    private boolean doScreenshotAfter;
    private int waitBefore;
    private int waitAfter;

    public JSONArray getConditionOptions() {
        return conditionOptions;
    }

    public void setConditionOptions(JSONArray conditionOptions) {
        this.conditionOptions = conditionOptions;
    }

    public JSONArray getOptions() {
        return options;
    }

    public void setOptions(JSONArray options) {
        this.options = options;
    }

    public List<TestCaseExecutionFile> getFileList() {
        return fileList;
    }

    public void setFileList(List<TestCaseExecutionFile> fileList) {
        this.fileList = fileList;
    }

    public void addFileList(TestCaseExecutionFile file) {
        this.fileList.add(file);
    }

    public void addFileList(List<TestCaseExecutionFile> fileList) {
        if (fileList != null) {
            for (TestCaseExecutionFile testCaseExecutionFile : fileList) {
                this.fileList.add(testCaseExecutionFile);
            }
        }
    }

    public MessageEvent getControlResultMessage() {
        return controlResultMessage;
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

    public int getControlId() {
        return controlSequence;
    }

    public void setControlId(int control) {
        this.controlSequence = control;
    }

    public int getSort() {
        return sort;
    }

    public void setSort(int sort) {
        this.sort = sort;
    }

    public String getConditionOperator() {
        return conditionOperator;
    }

    public void setConditionOperator(String conditionOperator) {
        this.conditionOperator = conditionOperator;
    }

    public String getConditionVal1() {
        return conditionVal1;
    }

    public String getConditionVal1Init() {
        return conditionVal1Init;
    }

    public void setConditionVal1Init(String conditionVal1Init) {
        this.conditionVal1Init = conditionVal1Init;
    }

    public String getConditionVal2Init() {
        return conditionVal2Init;
    }

    public void setConditionVal2Init(String conditionVal2Init) {
        this.conditionVal2Init = conditionVal2Init;
    }

    public String getConditionVal3Init() {
        return conditionVal3Init;
    }

    public void setConditionVal3Init(String conditionVal3Init) {
        this.conditionVal3Init = conditionVal3Init;
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

    public String getValue1() {
        return value1;
    }

    public void setValue1(String controlProperty) {
        this.value1 = controlProperty;
    }

    public String getValue3() {
        return value3;
    }

    public void setValue3(String value3) {
        this.value3 = value3;
    }

    public String getControl() {
        return control;
    }

    public void setControl(String controlType) {
        this.control = controlType;
    }

    public String getValue2() {
        return value2;
    }

    public void setValue2(String controlValue) {
        this.value2 = controlValue;
    }

    public long getEnd() {
        return end;
    }

    public void setEnd(long end) {
        this.end = end;
        DateFormat df = new SimpleDateFormat(DateUtil.DATE_FORMAT_TIMESTAMP);
        this.endLong = Long.valueOf(df.format(end));
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

    public int getActionId() {
        return sequence;
    }

    public void setActionId(int sequence) {
        this.sequence = sequence;
    }

    public long getStart() {
        return start;
    }

    public void setStart(long start) {
        this.start = start;
        DateFormat df = new SimpleDateFormat(DateUtil.DATE_FORMAT_TIMESTAMP);
        this.startLong = Long.valueOf(df.format(start));
    }

    public long getStartLong() {
        return startLong;
    }

    public void setStartLong(long startLong) {
        this.startLong = startLong;
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

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
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

    public String getValue1Init() {
        return value1Init;
    }

    public void setValue1Init(String value1Init) {
        this.value1Init = value1Init;
    }

    public String getValue2Init() {
        return value2Init;
    }

    public void setValue2Init(String value2Init) {
        this.value2Init = value2Init;
    }

    public String getValue3Init() {
        return value3Init;
    }

    public void setValue3Init(String value3Init) {
        this.value3Init = value3Init;
    }

    public TestCaseStepActionControl getTestCaseStepActionControl() {
        return testCaseStepActionControl;
    }

    public void setTestCaseStepActionControl(TestCaseStepActionControl testCaseStepActionControl) {
        this.testCaseStepActionControl = testCaseStepActionControl;
    }

    /**
     * Convert the current TestCaseStepActionControlExecution into JSON format
     * Note that if withChilds and withParents are both set to true, only the
     * child will be included to avoid loop.
     *
     * @param withChilds boolean that define if childs should be included
     * @param withParents boolean that define if parents should be included
     * @param secrets
     * @return TestCaseStepActionControlExecution in JSONObject format
     */
    public JSONObject toJson(boolean withChilds, boolean withParents, HashMap<String, String> secrets) {
        JSONObject result = new JSONObject();
        // Check if both parameter are not set to true
        if (withChilds == true && withParents == true) {
            withParents = false;
        }
        try {
            result.put("type", "testCaseStepActionControlExecution");
            result.put("id", this.getId());
            result.put("test", this.getTest());
            result.put("testcase", this.getTestCase());
            result.put("step", this.getStepId());
            result.put("index", this.getIndex());
            result.put("sequence", this.getActionId());
            result.put("control", this.getControlId());
            result.put("sort", this.getSort());
            result.put("conditionOperator", this.getConditionOperator());
            result.put("conditionVal1Init", StringUtil.secureFromSecrets(this.getConditionVal1Init(), secrets));
            result.put("conditionVal2Init", StringUtil.secureFromSecrets(this.getConditionVal2Init(), secrets));
            result.put("conditionVal3Init", StringUtil.secureFromSecrets(this.getConditionVal3Init(), secrets));
            result.put("conditionVal1", StringUtil.secureFromSecrets(this.getConditionVal1(), secrets));
            result.put("conditionVal2", StringUtil.secureFromSecrets(this.getConditionVal2(), secrets));
            result.put("conditionVal3", StringUtil.secureFromSecrets(this.getConditionVal3(), secrets));
            result.put("controlType", this.getControl());
            result.put("controlProperty", StringUtil.secureFromSecrets(this.getValue1(), secrets));
            result.put("controlValue", StringUtil.secureFromSecrets(this.getValue2(), secrets));
            result.put("controlValue3", StringUtil.secureFromSecrets(this.getValue3(), secrets));
            result.put("controlPropertyInit", StringUtil.secureFromSecrets(this.getValue1Init(), secrets));
            result.put("controlValueInit", StringUtil.secureFromSecrets(this.getValue2Init(), secrets));
            result.put("controlValue3Init", StringUtil.secureFromSecrets(this.getValue3Init(), secrets));
            result.put("fatal", this.getFatal());
            result.put("start", this.getStart());
            result.put("end", this.getEnd());
            result.put("startlong", this.getStartLong());
            result.put("endlong", this.getEndLong());
            result.put("description", StringUtil.secureFromSecrets(this.getDescription(), secrets));
            result.put("returnCode", this.getReturnCode());
            result.put("returnMessage", StringUtil.secureFromSecrets(this.getReturnMessage(), secrets));

            if (withChilds) {
                JSONArray array = new JSONArray();
                if (this.getFileList() != null) {
                    for (Object actionFileList : this.getFileList()) {
                        if (actionFileList != null) {
                            array.put(((TestCaseExecutionFile) actionFileList).toJson());
                        }
                    }
                }
                result.put("fileList", array);
            }

            if (withParents) {
                result.put("testCaseStepActionExecution", this.getTestCaseStepActionExecution().toJson(false, true, secrets));
            }

        } catch (JSONException ex) {
            Logger LOG = LogManager.getLogger(TestCaseStepActionControlExecution.class);
            LOG.warn(ex);
        }
        return result;
    }
}
