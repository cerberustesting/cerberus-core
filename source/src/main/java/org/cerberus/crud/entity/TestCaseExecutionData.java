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

import java.util.HashMap;

/**
 *
 * @author bcivel
 */
public class TestCaseExecutionData {

    private long id;
    private String property;
    private String value;
    private String type;
    private String value1;
    private String value2;
    private String RC;
    private String rMessage;
    private long start;
    private long end;
    private long startLong;
    private long endLong;
    /**
     *
     */
    private TestCaseExecution tCExecution;
    private MessageEvent propertyResultMessage;
    private MessageGeneral executionResultMessage;
    private boolean stopExecution;
    private TestCaseCountryProperties testCaseCountryProperties;
    private HashMap<String, String> dataLibRawData; // Have the raw data of all subdata when comming from testDataLibrary

    public HashMap<String, String> getDataLibRawData() {
        return dataLibRawData;
    }

    public void setDataLibRawData(HashMap<String, String> dataLibRawData) {
        this.dataLibRawData = dataLibRawData;
    }

    public TestCaseCountryProperties getTestCaseCountryProperties() {
        return testCaseCountryProperties;
    }

    public void setTestCaseCountryProperties(TestCaseCountryProperties testCaseCountryProperties) {
        this.testCaseCountryProperties = testCaseCountryProperties;
    }

    public String getrMessage() {
        return rMessage;
    }

    public void setrMessage(String rMessage) {
        this.rMessage = rMessage;
    }

    public MessageEvent getPropertyResultMessage() {
        return propertyResultMessage;
    }

    public void setPropertyResultMessage(MessageEvent propertyResultMessage) {
        this.propertyResultMessage = propertyResultMessage;
        if (propertyResultMessage != null) {
            this.setRC(propertyResultMessage.getCodeString());
            this.setrMessage(propertyResultMessage.getDescription());
            this.executionResultMessage = new MessageGeneral(propertyResultMessage.getMessage());
            this.stopExecution = propertyResultMessage.isStopTest();
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

    public String getProperty() {
        return property;
    }

    public void setProperty(String property) {
        this.property = property;
    }

    public String getRC() {
        return RC;
    }

    public void setRC(String rc) {
        this.RC = rc;
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

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return "TestCaseExecutionData{" + "id=" + id + ", property=" + property + ", value=" + value + ", type=" + type + ", value1=" + value1 + ", value2=" + value2 + ", RC=" + RC + ", rMessage=" + rMessage + ", start=" + start + ", end=" + end + ", startLong=" + startLong + ", endLong=" + endLong + ", propertyResultMessage=" + propertyResultMessage.toString() + ", executionResultMessage=" + executionResultMessage + ", stopExecution=" + stopExecution + '}';
    }
    
    
}
