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
public class TestCaseStepActionExecution {

    private long id;
    private String test;
    private String testCase;
    private int step;
    private int index;
    private int sequence;
    private int sort;
    private String conditionOper;
    private String conditionVal1Init;
    private String conditionVal2Init;
    private String conditionVal1;
    private String conditionVal2;
    private String action;
    private String value1Init;
    private String value2Init;
    private String value1;
    private String value2;
    private String forceExeStatus;
    private String description;
    private String returnCode;
    private String returnMessage;
    private long start;
    private long end;
    private long startLong;
    private long endLong;

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
    private AnswerList testCaseStepActionControlExecutionList;

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

    public String getForceExeStatus() {
        return forceExeStatus;
    }

    public void setForceExeStatus(String forceExeStatus) {
        this.forceExeStatus = forceExeStatus;
    }

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

    public String getValue1() {
        return value1;
    }

    public void setValue1(String value1) {
        this.value1 = value1;
    }

    public String getValue2() {
        return value2;
    }

    public void setValue2(String value2) {
        this.value2 = value2;
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

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
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

    public void setTestCaseStepActionControlExecutionList(AnswerList testCaseStepActionControlExecutionList) {
        this.testCaseStepActionControlExecutionList = testCaseStepActionControlExecutionList;
    }

    public AnswerList getTestCaseStepActionControlExecutionList() {
        return testCaseStepActionControlExecutionList;
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
            result.put("sequence", this.getSequence());
            result.put("sort", this.getSort());
            result.put("action", this.getAction());
            result.put("value1", this.getValue1());
            result.put("value2", this.getValue2());
            result.put("value1init", this.getValue1Init());
            result.put("value2init", this.getValue2Init());
            result.put("forceExeStatus", this.getForceExeStatus());
            result.put("start", this.getStart());
            result.put("end", this.getEnd());
            result.put("startlong", this.getStartLong());
            result.put("endlong", this.getEndLong());
            result.put("description", this.getDescription());
            result.put("returnCode", this.getReturnCode());
            result.put("returnMessage", this.getReturnMessage());
            JSONArray array = new JSONArray();
            if (this.getTestCaseStepActionControlExecutionList() != null && this.getTestCaseStepActionControlExecutionList().getDataList() != null) {
                for (Object testCaseStepActionControlExecution : this.getTestCaseStepActionControlExecutionList().getDataList()) {
                    array.put(((TestCaseStepActionControlExecution) testCaseStepActionControlExecution).toJson());
                }
            }
            result.put("testCaseStepActionControlExecutionList", array);
        } catch (JSONException ex) {
            Logger.getLogger(TestCaseStepActionExecution.class.getName()).log(Level.SEVERE, null, ex);
        }
        return result;
    }
}
