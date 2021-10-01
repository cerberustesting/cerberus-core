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
package org.cerberus.dto.publicv1;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.util.List;

/**
 *
 * @author mlombard
 */

@ApiModel(value = "TestcaseStepAction")
public class TestcaseStepActionDTOV1 {
    
    @ApiModelProperty(position = 0)
    private String testFolderId;
    
    @ApiModelProperty(position = 1)
    private String testcaseId;
    
    @ApiModelProperty(position = 2)
    private int stepId;
    
    @ApiModelProperty(position = 3)
    private int actionId;
    
    @ApiModelProperty(position = 4)
    private int sort;
    
    @ApiModelProperty(position = 5)
    private String conditionOperator;
    
    @ApiModelProperty(position = 6)
    private String conditionValue1;
    
    @ApiModelProperty(position = 7)
    private String conditionValue2;
    
    @ApiModelProperty(position = 8)
    private String conditionValue3;
    
    @ApiModelProperty(position = 9)
    private String conditionOptions;
    
    @ApiModelProperty(position = 10)
    private String action;
    
    @ApiModelProperty(position = 11)
    private String value1;
    
    @ApiModelProperty(position = 12)
    private String value2;
    
    @ApiModelProperty(position = 13)
    private String value3;
    
    @ApiModelProperty(position = 14)
    private String options;
    
    @ApiModelProperty(position = 15)
    private boolean isFatal;
    
    @ApiModelProperty(position = 16)
    private String description;
    
    @ApiModelProperty(position = 17)
    private String screenshotFilename;
    
    @ApiModelProperty(position = 18)
    private String usrCreated;
    
    @ApiModelProperty(position = 19)
    private String dateCreated;
    
    @ApiModelProperty(position = 20)
    private String usrModif;
    
    @ApiModelProperty(position = 21)
    private String dateModif;

    @ApiModelProperty(position = 22)
    List<TestcaseStepActionControlDTOV1> controls;

    public TestcaseStepActionDTOV1() {
    }

    public TestcaseStepActionDTOV1(String testFolderId, String testcaseId, 
            int stepId, int actionId, int sort, String conditionOperator, 
            String conditionValue1, String conditionValue2, String conditionValue3, 
            String conditionOptions, String action, String value1, String value2, 
            String value3, String options, boolean isFatal, String description, 
            String screenshotFilename, String usrCreated, String dateCreated, 
            String usrModif, String dateModif, List<TestcaseStepActionControlDTOV1> controls) {
        this.testFolderId = testFolderId;
        this.testcaseId = testcaseId;
        this.stepId = stepId;
        this.actionId = actionId;
        this.sort = sort;
        this.conditionOperator = conditionOperator;
        this.conditionValue1 = conditionValue1;
        this.conditionValue2 = conditionValue2;
        this.conditionValue3 = conditionValue3;
        this.conditionOptions = conditionOptions;
        this.action = action;
        this.value1 = value1;
        this.value2 = value2;
        this.value3 = value3;
        this.options = options;
        this.isFatal = isFatal;
        this.description = description;
        this.screenshotFilename = screenshotFilename;
        this.usrCreated = usrCreated;
        this.dateCreated = dateCreated;
        this.usrModif = usrModif;
        this.dateModif = dateModif;
        this.controls = controls;
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

    public int getStepId() {
        return stepId;
    }

    public void setStepId(int stepId) {
        this.stepId = stepId;
    }

    public int getActionId() {
        return actionId;
    }

    public void setActionId(int actionId) {
        this.actionId = actionId;
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

    public String getConditionOptions() {
        return conditionOptions;
    }

    public void setConditionOptions(String conditionOptions) {
        this.conditionOptions = conditionOptions;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
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

    public String getOptions() {
        return options;
    }

    public void setOptions(String options) {
        this.options = options;
    }

    public boolean isIsFatal() {
        return isFatal;
    }

    public void setIsFatal(boolean isFatal) {
        this.isFatal = isFatal;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getScreenshotFilename() {
        return screenshotFilename;
    }

    public void setScreenshotFilename(String screenshotFilename) {
        this.screenshotFilename = screenshotFilename;
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

    public List<TestcaseStepActionControlDTOV1> getControls() {
        return controls;
    }

    public void setControls(List<TestcaseStepActionControlDTOV1> controls) {
        this.controls = controls;
    }
}
