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

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import org.cerberus.core.engine.entity.MessageGeneral;
import org.cerberus.core.engine.entity.MessageEvent;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.List;
import org.cerberus.core.util.StringUtil;
import org.json.JSONArray;

/**
 *
 * @author bcivel
 */
public class TestCaseExecutionData {

    private long id;
    private String property;
    private int index;
    private String type;
    private String value;
    private String database;
    private String value1Init;
    private String value2Init;
    private String value3Init;
    private String value1;
    private String value2;
    private String value3;
    private String lengthInit;
    private String length;
    private String system;
    private String environment;
    private String country;
    private String dataLib;
    private String jsonResult;
    private String fromCache;
    private int rowLimit;
    private String nature;
    private int retryNb;
    private int retryPeriod;
    private int Rank;
    private long start;
    private long end;
    private long startLong;
    private long endLong;
    private String RC;
    private String rMessage;
    private String description;
    /**
     *
     */
    private List<TestCaseExecutionFile> fileList; // Host the list of the files stored at control level
    private TestCaseExecution tCExecution;
    private MessageEvent propertyResultMessage;
    private MessageGeneral executionResultMessage;
    private boolean stopExecution;
    private TestCaseCountryProperties testCaseCountryProperties;
    private List<HashMap<String, String>> dataLibRawData; // Have the raw data of all subdata when comming from testDataLibrary
    private TestDataLib dataLibObj;

    private static final Logger LOG = LogManager.getLogger(TestCaseExecutionData.class);

    public TestDataLib getDataLibObj() {
        return dataLibObj;
    }

    public void setDataLibObj(TestDataLib dataLibObj) {
        this.dataLibObj = dataLibObj;
    }

    public String getFromCache() {
        return fromCache;
    }

    public void setFromCache(String fromCache) {
        this.fromCache = fromCache;
    }

    public List<TestCaseExecutionFile> getFileList() {
        return fileList;
    }

    public void setFileList(List<TestCaseExecutionFile> fileList) {
        this.fileList = fileList;
    }

    public void addFileList(TestCaseExecutionFile file) {
        if (file != null) {
            this.fileList.add(file);
        }
    }

    public void addFileList(List<TestCaseExecutionFile> fileList) {
        if (fileList != null) {
            for (TestCaseExecutionFile testCaseExecutionFile : fileList) {
                this.fileList.add(testCaseExecutionFile);
            }
        }
    }

    public String getDatabase() {
        return database;
    }

    public void setDatabase(String database) {
        this.database = database;
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

    public String getLengthInit() {
        return lengthInit;
    }

    public void setLengthInit(String lengthInit) {
        this.lengthInit = lengthInit;
    }

    public String getLength() {
        return length;
    }

    public void setLength(String length) {
        this.length = length;
    }

    public String getSystem() {
        return system;
    }

    public void setSystem(String system) {
        this.system = system;
    }

    public String getEnvironment() {
        return environment;
    }

    public void setEnvironment(String environment) {
        this.environment = environment;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getDataLib() {
        return dataLib;
    }

    public void setDataLib(String dataLib) {
        this.dataLib = dataLib;
    }

    public String getJsonResult() {
        return jsonResult;
    }

    public void setJsonResult(String jsonResult) {
        this.jsonResult = jsonResult;
    }

    public int getRowLimit() {
        return rowLimit;
    }

    public void setRowLimit(int rowLimit) {
        this.rowLimit = rowLimit;
    }

    public String getNature() {
        return nature;
    }

    public void setNature(String nature) {
        this.nature = nature;
    }

    public int getRetryNb() {
        return retryNb;
    }

    public void setRetryNb(int retrynb) {
        this.retryNb = retrynb;
    }

    public int getRetryPeriod() {
        return retryPeriod;
    }

    public void setRetryPeriod(int retryperiod) {
        this.retryPeriod = retryperiod;
    }

    public int getRank() {
        return Rank;
    }

    public void setRank(int rank) {
        Rank = rank;
    }

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    public List<HashMap<String, String>> getDataLibRawData() {
        return dataLibRawData;
    }

    public void setDataLibRawData(List<HashMap<String, String>> dataLibRawData) {
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

    public String getValue3() {
        return value3;
    }

    public void setValue3(String value3) {
        this.value3 = value3;
    }

    public String getProperty() {
        return property;
    }

    public void setProperty(String property) {
        this.property = property;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
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
        return "TestCaseExecutionData{" + "id=" + id + ", property=" + property + ", value=" + value + ", type=" + type + ", value1=" + value1 + ", value2=" + value2 + ", value3=" + value3 + ", RC=" + RC + ", rMessage=" + rMessage + ", start=" + start + ", end=" + end + ", startLong=" + startLong + ", endLong=" + endLong + ", propertyResultMessage=" + propertyResultMessage + ", executionResultMessage=" + executionResultMessage + ", stopExecution=" + stopExecution + '}';
    }

    /**
     * Convert the current TestCaseExecutionData into JSON format Note that if
     * withChilds and withParents are both set to true, only the child will be
     * included to avoid loop.
     *
     * @param withChilds boolean that define if childs should be included
     * @param withParents boolean that define if parents should be included
     * @param secrets
     * @return TestCaseExecutionData in JSONObject format
     */
    public JSONObject toJson(boolean withChilds, boolean withParents, HashMap<String, String> secrets) {
        JSONObject result = new JSONObject();
        // Check if both parameter are not set to true
        if (withChilds == true && withParents == true) {
            withParents = false;
        }
        try {
            result.put("type", "testCaseExecutionData");
            result.put("id", this.getId());
            result.put("property", this.getProperty());
            result.put("index", this.getIndex());
            result.put("database", this.getDatabase());
            result.put("value", StringUtil.secureFromSecrets(this.getValue(), secrets));
            result.put("type", this.getType());
            result.put("rank", this.getRank());
            result.put("value1Init", StringUtil.secureFromSecrets(this.getValue1Init(), secrets));
            result.put("value2Init", StringUtil.secureFromSecrets(this.getValue2Init(), secrets));
            result.put("value3Init", StringUtil.secureFromSecrets(this.getValue3Init(), secrets));
            result.put("value1", StringUtil.secureFromSecrets(this.getValue1(), secrets));
            result.put("value2", StringUtil.secureFromSecrets(this.getValue2(), secrets));
            result.put("value3", StringUtil.secureFromSecrets(this.getValue3(), secrets));
            result.put("length", StringUtil.secureFromSecrets(this.getLength(), secrets));
            result.put("rowLimit", this.getRowLimit());
            result.put("nature", this.getNature());
            result.put("retryNb", this.getRetryNb());
            result.put("retryPeriod", this.getRetryPeriod());
            result.put("start", this.getStart());
            result.put("end", this.getEnd());
            result.put("startLong", this.getStartLong());
            result.put("endLong", this.getEndLong());
            result.put("RC", this.getRC());
            result.put("rMessage", StringUtil.secureFromSecrets(this.getrMessage(), secrets));
            result.put("description", StringUtil.secureFromSecrets(this.getDescription(), secrets));

            if (withChilds) {
                JSONArray array = new JSONArray();
                if (this.getFileList() != null) {
                    for (Object dataFileList : this.getFileList()) {
                        array.put(((TestCaseExecutionFile) dataFileList).toJson());
                    }
                }
                result.put("fileList", array);
            }

            if (withParents && this.gettCExecution() != null) {
                result.put("testCaseExecution", this.gettCExecution().toJson(false));
            }

        } catch (JSONException ex) {
            LOG.error(this.getId() + " - " + this.getProperty() + " - " + this.getIndex(), ex);
        } catch (Exception ex) {
            LOG.error(this.getId() + " - " + this.getProperty() + " - " + this.getIndex(), ex);
        }
        return result;
    }

}
