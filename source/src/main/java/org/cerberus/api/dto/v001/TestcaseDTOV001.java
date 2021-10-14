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
package org.cerberus.api.dto.v001;

import com.fasterxml.jackson.databind.JsonNode;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.util.List;

/**
 *
 * @author mlombard
 */

@ApiModel(value = "Testcase")
public class TestcaseDTOV001 {
    
    @ApiModelProperty(example = "Examples", position = 0)
    private String testFolderId;
    
    @ApiModelProperty(example = "0001A", position = 1)
    private String testcaseId;
    
    @ApiModelProperty(example = "Google", position = 2)
    private String application;
        
    @ApiModelProperty(example = "Search for Cerberus Website", position = 3)
    private String description;
    
    @ApiModelProperty(example = "<p>This test case shows how to implement scenarios with basic actions.&nbsp;</p>", position = 4)
    private String detailedDescription;
    
    @ApiModelProperty(example = "1", position = 5)
    private int priority;
    
    @ApiModelProperty(example = "5", position = 6)
    private int version;
    
    @ApiModelProperty(example = "WORKING", position = 7)
    private String status;
    
    @ApiModelProperty(example = "true", position = 8)
    private boolean isActive;
    
    @ApiModelProperty(example = "true", position = 9)
    private boolean isActiveQA;
    
    @ApiModelProperty(example = "true", position = 10)
    private boolean isActiveUAT;
    
    @ApiModelProperty(example = "true", position = 11)
    private boolean isActivePROD;
    
    @ApiModelProperty(example = "always", position = 12)
    private String conditionOperator;
    
    @ApiModelProperty(example = "", position = 13)
    private String conditionValue1;
    
    @ApiModelProperty(example = "", position = 14)
    private String conditionValue2;
    
    @ApiModelProperty(example = "", position = 15)
    private String conditionValue3;
    
    @ApiModelProperty(example = "", position = 16)
    private JsonNode conditionOptions;
    
    @ApiModelProperty(example = "AUTOMATED", position = 17)
    private String type;
    
    @ApiModelProperty(name = "externalProvider", example = "RX", position = 18)
    private String origine;
    
    @ApiModelProperty(name = "externalReference", example = "", position = 19)
    private String refOrigine;
    
    @ApiModelProperty(example = "", position = 20)
    private String comment;
    
    @ApiModelProperty(example = "", position = 21)
    private String fromMajor;
    
    @ApiModelProperty(example = "", position = 22)
    private String fromMinor;
    
    @ApiModelProperty(example = "", position = 23)
    private String toMajor;
    
    @ApiModelProperty(example = "", position = 24)    
    private String toMinor;
    
    @ApiModelProperty(example = "", position = 25)
    private JsonNode bugs;
    
    @ApiModelProperty(example = "", position = 26)
    private String targetMajor;
    
    @ApiModelProperty(example = "", position = 27)
    private String targetMinor;
    
    @ApiModelProperty(example = "cerberus", position = 28)
    private String implementer;
    
    @ApiModelProperty(example = "", position = 29)
    private String executor;
    
    @ApiModelProperty(example = "", position = 30)
    private String userAgent;
    
    @ApiModelProperty(example = "", position = 31)
    private String screenSize;
    
    @ApiModelProperty(example = "cerberus", position = 32)
    private String usrCreated;
    
    @ApiModelProperty(example = "2012-06-19 09:56:40.0", position = 33)
    private String dateCreated;
    
    @ApiModelProperty(example = "a03e2ae7-6fe6-42df-b2d6-6f7542ee3ed3", position = 34)
    private String usrModif;
    
    @ApiModelProperty(example = "2019-04-06 10:15:09.0", position = 35)
    private String dateModif;

    @ApiModelProperty(position = 36, required = false)
    private List<TestcaseStepDTOV001> steps;

    public TestcaseDTOV001() {
    }

    public TestcaseDTOV001(String testFolderId, String testcaseId, String application, String description, String detailedDescription, int priority, int version, String status, boolean isActive, boolean isActiveQA, boolean isActiveUAT, boolean isActivePROD, String conditionOperator, String conditionValue1, String conditionValue2, String conditionValue3, JsonNode conditionOptions, String type, String origine, String refOrigine, String comment, String fromMajor, String fromMinor, String toMajor, String toMinor, JsonNode bugs, String targetMajor, String targetMinor, String implementer, String executor, String userAgent, String screenSize, String usrCreated, String dateCreated, String usrModif, String dateModif, List<TestcaseStepDTOV001> steps) {
        this.testFolderId = testFolderId;
        this.testcaseId = testcaseId;
        this.application = application;
        this.description = description;
        this.detailedDescription = detailedDescription;
        this.priority = priority;
        this.version = version;
        this.status = status;
        this.isActive = isActive;
        this.isActiveQA = isActiveQA;
        this.isActiveUAT = isActiveUAT;
        this.isActivePROD = isActivePROD;
        this.conditionOperator = conditionOperator;
        this.conditionValue1 = conditionValue1;
        this.conditionValue2 = conditionValue2;
        this.conditionValue3 = conditionValue3;
        this.conditionOptions = conditionOptions;
        this.type = type;
        this.origine = origine;
        this.refOrigine = refOrigine;
        this.comment = comment;
        this.fromMajor = fromMajor;
        this.fromMinor = fromMinor;
        this.toMajor = toMajor;
        this.toMinor = toMinor;
        this.bugs = bugs;
        this.targetMajor = targetMajor;
        this.targetMinor = targetMinor;
        this.implementer = implementer;
        this.executor = executor;
        this.userAgent = userAgent;
        this.screenSize = screenSize;
        this.usrCreated = usrCreated;
        this.dateCreated = dateCreated;
        this.usrModif = usrModif;
        this.dateModif = dateModif;
        this.steps = steps;
    }

    public String getTestFolderId() {
        return testFolderId;
    }

    public void setTestFolderId(String testFolderId) {
        this.testFolderId = testFolderId;
    }

    public String getTestcaseId() {
        return testcaseId;
    }

    public void setTestcaseId(String testcaseId) {
        this.testcaseId = testcaseId;
    }

    public String getApplication() {
        return application;
    }

    public void setApplication(String application) {
        this.application = application;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getDetailedDescription() {
        return detailedDescription;
    }

    public void setDetailedDescription(String detailedDescription) {
        this.detailedDescription = detailedDescription;
    }

    public int getPriority() {
        return priority;
    }

    public void setPriority(int priority) {
        this.priority = priority;
    }

    public int getVersion() {
        return version;
    }

    public void setVersion(int version) {
        this.version = version;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public boolean isIsActive() {
        return isActive;
    }

    public void setIsActive(boolean isActive) {
        this.isActive = isActive;
    }

    public boolean isIsActiveQA() {
        return isActiveQA;
    }

    public void setIsActiveQA(boolean isActiveQA) {
        this.isActiveQA = isActiveQA;
    }

    public boolean isIsActiveUAT() {
        return isActiveUAT;
    }

    public void setIsActiveUAT(boolean isActiveUAT) {
        this.isActiveUAT = isActiveUAT;
    }

    public boolean isIsActivePROD() {
        return isActivePROD;
    }

    public void setIsActivePROD(boolean isActivePROD) {
        this.isActivePROD = isActivePROD;
    }

    public String getConditionOperator() {
        return conditionOperator;
    }

    public void setConditionOperator(String conditionOperator) {
        this.conditionOperator = conditionOperator;
    }

    public String getConditionValue1() {
        return conditionValue1;
    }

    public void setConditionValue1(String conditionValue1) {
        this.conditionValue1 = conditionValue1;
    }

    public String getConditionValue2() {
        return conditionValue2;
    }

    public void setConditionValue2(String conditionValue2) {
        this.conditionValue2 = conditionValue2;
    }

    public String getConditionValue3() {
        return conditionValue3;
    }

    public void setConditionValue3(String conditionValue3) {
        this.conditionValue3 = conditionValue3;
    }

    public JsonNode getConditionOptions() {
        return conditionOptions;
    }

    public void setConditionOptions(JsonNode conditionOptions) {
        this.conditionOptions = conditionOptions;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getOrigine() {
        return origine;
    }

    public void setOrigine(String origine) {
        this.origine = origine;
    }

    public String getRefOrigine() {
        return refOrigine;
    }

    public void setRefOrigine(String refOrigine) {
        this.refOrigine = refOrigine;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public String getFromMajor() {
        return fromMajor;
    }

    public void setFromMajor(String fromMajor) {
        this.fromMajor = fromMajor;
    }

    public String getFromMinor() {
        return fromMinor;
    }

    public void setFromMinor(String fromMinor) {
        this.fromMinor = fromMinor;
    }

    public String getToMajor() {
        return toMajor;
    }

    public void setToMajor(String toMajor) {
        this.toMajor = toMajor;
    }

    public String getToMinor() {
        return toMinor;
    }

    public void setToMinor(String toMinor) {
        this.toMinor = toMinor;
    }

    public JsonNode getBugs() {
        return bugs;
    }

    public void setBugs(JsonNode bugs) {
        this.bugs = bugs;
    }

    public String getTargetMajor() {
        return targetMajor;
    }

    public void setTargetMajor(String targetMajor) {
        this.targetMajor = targetMajor;
    }

    public String getTargetMinor() {
        return targetMinor;
    }

    public void setTargetMinor(String targetMinor) {
        this.targetMinor = targetMinor;
    }

    public String getImplementer() {
        return implementer;
    }

    public void setImplementer(String implementer) {
        this.implementer = implementer;
    }

    public String getExecutor() {
        return executor;
    }

    public void setExecutor(String executor) {
        this.executor = executor;
    }

    public String getUserAgent() {
        return userAgent;
    }

    public void setUserAgent(String userAgent) {
        this.userAgent = userAgent;
    }

    public String getScreenSize() {
        return screenSize;
    }

    public void setScreenSize(String screenSize) {
        this.screenSize = screenSize;
    }

    public String getUsrCreated() {
        return usrCreated;
    }

    public void setUsrCreated(String usrCreated) {
        this.usrCreated = usrCreated;
    }

    public String getDateCreated() {
        return dateCreated;
    }

    public void setDateCreated(String dateCreated) {
        this.dateCreated = dateCreated;
    }

    public String getUsrModif() {
        return usrModif;
    }

    public void setUsrModif(String usrModif) {
        this.usrModif = usrModif;
    }

    public String getDateModif() {
        return dateModif;
    }

    public void setDateModif(String dateModif) {
        this.dateModif = dateModif;
    }

    public List<TestcaseStepDTOV001> getSteps() {
        return steps;
    }

    public void setSteps(List<TestcaseStepDTOV001> steps) {
        this.steps = steps;
    }

    
}
