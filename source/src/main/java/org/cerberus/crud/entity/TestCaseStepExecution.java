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

import org.cerberus.engine.entity.MessageGeneral;
import org.cerberus.engine.entity.MessageEvent;
import java.math.BigDecimal;
import java.util.List;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * @author bcivel
 */
public class TestCaseStepExecution {

    private static final Logger LOG = LogManager.getLogger(TestCaseStepExecution.class);

    private long id;
    private String test;
    private String testCase;
    private int step;
    private int index;
    private int sort;
    private String loop;
    private String conditionOperator;
    private String conditionVal1Init;
    private String conditionVal2Init;
    private String conditionVal3Init;
    private String conditionVal1;
    private String conditionVal2;
    private String conditionVal3;
    private String batNumExe;
    private long start;
    private long end;
    private long fullStart;
    private long fullEnd;
    private BigDecimal timeElapsed;
    private String returnCode;
    private String returnMessage;
    private String description;
    /**
     * From here are data outside database model.
     */
    private TestCaseStep testCaseStep;
    private TestCaseExecution tCExecution;
    private List<TestCaseExecutionFile> fileList; // Host the list of the files stored at step level
    private List<TestCaseExecutionData> testCaseExecutionDataList; // Host the list of data calculated during the step execution.
    private List<TestCaseStepActionExecution> testCaseStepActionExecutionList;
    private MessageEvent stepResultMessage;
    private MessageGeneral executionResultMessage;
    private boolean stopExecution;
    private String useStep;
    private String useStepTest;
    private String useStepTestCase;
    private int useStepTestCaseStep;

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

    public String getLoop() {
        return loop;
    }

    public void setLoop(String loop) {
        this.loop = loop;
    }

    public String getReturnMessage() {
        return returnMessage;
    }

    public void setReturnMessage(String returnMessage) {
        this.returnMessage = returnMessage;
    }

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

    public int getUseStepTestCaseStep() {
        return useStepTestCaseStep;
    }

    public void setUseStepTestCaseStep(int useStepTestCaseStep) {
        this.useStepTestCaseStep = useStepTestCaseStep;
    }

    public List<TestCaseExecutionData> getTestCaseExecutionDataList() {
        return testCaseExecutionDataList;
    }

    public void setTestCaseExecutionDataList(List<TestCaseExecutionData> testCaseExecutionDataList) {
        this.testCaseExecutionDataList = testCaseExecutionDataList;
    }

    public MessageEvent getStepResultMessage() {
        return stepResultMessage;
    }

    public void setStepResultMessage(MessageEvent stepResultMessage) {
        this.stepResultMessage = stepResultMessage;
        if (stepResultMessage != null) {
            this.setReturnCode(stepResultMessage.getCodeString());
            this.setReturnMessage(stepResultMessage.getDescription());
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

    public TestCaseExecution gettCExecution() {
        return tCExecution;
    }

    public void settCExecution(TestCaseExecution tCExecution) {
        this.tCExecution = tCExecution;
    }

    public TestCaseStep getTestCaseStep() {
        return testCaseStep;
    }

    public void setTestCaseStep(TestCaseStep testCaseStep) {
        this.testCaseStep = testCaseStep;
    }

    public String getBatNumExe() {
        return batNumExe;
    }

    public void setBatNumExe(String batNumExe) {
        this.batNumExe = batNumExe;
    }

    public long getEnd() {
        return end;
    }

    public void setEnd(long end) {
        this.end = end;
    }

    public long getFullEnd() {
        return fullEnd;
    }

    public void setFullEnd(long fullEnd) {
        this.fullEnd = fullEnd;
    }

    public long getFullStart() {
        return fullStart;
    }

    public void setFullStart(long fullStart) {
        this.fullStart = fullStart;
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

    public long getStart() {
        return start;
    }

    public void setStart(long start) {
        this.start = start;
    }

    public int getStep() {
        return step;
    }

    public void setStep(int step) {
        this.step = step;
    }

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
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

    public BigDecimal getTimeElapsed() {
        return timeElapsed;
    }

    public void setTimeElapsed(BigDecimal timeElapsed) {
        this.timeElapsed = timeElapsed;
    }

    public List<TestCaseStepActionExecution> getTestCaseStepActionExecutionList() {
        return testCaseStepActionExecutionList;
    }

    public void setTestCaseStepActionExecutionList(List<TestCaseStepActionExecution> testCaseStepActionExecutionList) {
        this.testCaseStepActionExecutionList = testCaseStepActionExecutionList;
    }

    public void addTestCaseStepActionExecutionList(TestCaseStepActionExecution testCaseStepActionExecution) {
        if (testCaseStepActionExecution != null) {
            this.testCaseStepActionExecutionList.add(testCaseStepActionExecution);
        }
    }

    public void addTestCaseStepActionExecutionList(List<TestCaseStepActionExecution> testCaseStepActionExecutionList) {
        if (testCaseStepActionExecutionList != null) {
            for (TestCaseStepActionExecution testCaseStepActionExecution : testCaseStepActionExecutionList) {
                this.testCaseStepActionExecutionList.add(testCaseStepActionExecution);
            }
        }
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * Convert the current TestCaseStepExecution into JSON format Note that if
     * withChilds and withParents are both set to true, only the child will be
     * included to avoid loop.
     *
     * @param withChilds boolean that define if childs should be included
     * @param withParents boolean that define if parents should be included
     * @return TestCaseStepExecution in JSONObject format
     */
    public JSONObject toJson(boolean withChilds, boolean withParents) {
        JSONObject result = new JSONObject();
        // Check if both parameter are not set to true
        if (withChilds == true && withParents == true) {
            withParents = false;
        }
        try {
            result.put("type", "testCaseStepExecution");
            result.put("id", this.getId());
            result.put("test", this.getTest());
            result.put("testcase", this.getTestCase());
            result.put("step", this.getStep());
            result.put("index", this.getIndex());
            result.put("sort", this.getSort());
            result.put("batNumExe", this.getBatNumExe());
            result.put("start", this.getStart());
            result.put("end", this.getEnd());
            result.put("fullStart", this.getFullStart());
            result.put("fullEnd", this.getFullEnd());
            result.put("timeElapsed", this.getTimeElapsed());
            result.put("returnCode", this.getReturnCode());
            result.put("returnMessage", this.getReturnMessage());
            result.put("description", this.getDescription());
            result.put("useStep", this.getUseStep());
            result.put("useStepTest", this.getUseStepTest());
            result.put("useStepTestCase", this.getUseStepTestCase());
            result.put("useStepTestCaseStep", this.getUseStepTestCaseStep());
            result.put("loop", this.getLoop());
            result.put("conditionOperator", this.getConditionOperator());
            result.put("conditionVal1Init", this.getConditionVal1Init());
            result.put("conditionVal2Init", this.getConditionVal2Init());
            result.put("conditionVal3Init", this.getConditionVal3Init());
            result.put("conditionVal1", this.getConditionVal1());
            result.put("conditionVal2", this.getConditionVal2());
            result.put("conditionVal3", this.getConditionVal3());

            if (withChilds) {
                JSONArray array = new JSONArray();
                if (this.getTestCaseStepActionExecutionList() != null) {
                    for (Object testCaseStepExecution : this.getTestCaseStepActionExecutionList()) {
                        array.put(((TestCaseStepActionExecution) testCaseStepExecution).toJson(true, false));
                    }
                }
                result.put("testCaseStepActionExecutionList", array);

                array = new JSONArray();
                if (this.getFileList() != null) {
                    for (Object stepFileList : this.getFileList()) {
                        array.put(((TestCaseExecutionFile) stepFileList).toJson());
                    }
                }
                result.put("fileList", array);
            }

            if (withParents) {
                result.put("testCaseExecution", this.gettCExecution().toJson(false));
            }

        } catch (JSONException ex) {
            LOG.warn(ex);
        }
        return result;
    }
}
