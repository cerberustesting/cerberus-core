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

import org.cerberus.engine.entity.MessageGeneral;
import org.cerberus.engine.entity.MessageEvent;
import java.math.BigDecimal;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.cerberus.util.answer.AnswerList;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * @author bcivel
 */
public class TestCaseStepExecution {

    private static final org.apache.log4j.Logger LOG = org.apache.log4j.Logger.getLogger(TestCaseStepExecution.class);
    
    private long id;
    private String test;
    private String testCase;
    private int step;
    private int index;
    private int sort;
    private String loop;
    private String conditionOper;
    private String conditionVal1Init;
    private String conditionVal2Init;
    private String conditionVal1;
    private String conditionVal2;
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
    private MessageEvent stepResultMessage;
    private MessageGeneral executionResultMessage;
    private boolean stopExecution;
    private String useStep;
    private String useStepTest;
    private String useStepTestCase;
    private int useStepTestCaseStep;
    private AnswerList testCaseStepActionExecutionList;

    public List<TestCaseExecutionFile> getFileList() {
        return fileList;
    }

    public void setFileList(List<TestCaseExecutionFile> fileList) {
        this.fileList = fileList;
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

    public String getConditionOper() {
        return conditionOper;
    }

    public void setConditionOper(String conditionOper) {
        this.conditionOper = conditionOper;
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

    public void setTestCaseStepActionExecution(AnswerList testCaseStepActionExecutionList) {
        this.testCaseStepActionExecutionList = testCaseStepActionExecutionList;
    }

    public AnswerList getTestCaseStepActionExecutionList() {
        return testCaseStepActionExecutionList;
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
            
            JSONArray array = new JSONArray();
            if (this.getTestCaseStepActionExecutionList() != null && this.getTestCaseStepActionExecutionList().getDataList() != null) {
                for (Object testCaseStepExecution : this.getTestCaseStepActionExecutionList().getDataList()) {
                    array.put(((TestCaseStepActionExecution) testCaseStepExecution).toJson());
                }
            }
            result.put("testCaseStepActionExecutionList", array);
            
            array = new JSONArray();
            if (this.getFileList()!= null) {
                for (Object fileList : this.getFileList()) {
                    array.put(((TestCaseExecutionFile) fileList).toJson());
                }
            }
            result.put("fileList", array);
            
        } catch (JSONException ex) {
            Logger.getLogger(TestCaseStepExecution.class.getName()).log(Level.SEVERE, null, ex);
        }
        return result;
    }
}
