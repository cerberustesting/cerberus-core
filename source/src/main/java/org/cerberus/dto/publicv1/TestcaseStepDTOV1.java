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

@ApiModel(value = "TestcaseStep")
public class TestcaseStepDTOV1 {
    
    @ApiModelProperty(position = 0)
    private String testFolderId;
    
    @ApiModelProperty(position = 1)
    private String testcaseId;
    
    @ApiModelProperty(position = 2)
    private int stepId;
    
    @ApiModelProperty(position = 3)
    private int sort;
    
    @ApiModelProperty(position = 4)
    private String loop;
    
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
    private String description;
    
    @ApiModelProperty(position = 11)
    private boolean isUsingLibraryStep;
    
    @ApiModelProperty(position = 12)
    private String libraryStepTestFolderId;
    
    @ApiModelProperty(position = 13)
    private String libraryStepTestcaseId;
    
    @ApiModelProperty(position = 14)
    private Integer libraryStepStepId;
    
    @ApiModelProperty(position = 15)
    private boolean isStepInUseByOtherTestcase;
    
    @ApiModelProperty(position = 16)
    private int libraryStepSort;
    
    @ApiModelProperty(position = 17)
    private boolean isLibraryStep;
    
    @ApiModelProperty(position = 18)
    private boolean isExecutionForced;
    
    @ApiModelProperty(position = 19)
    private String usrCreated;
    
    @ApiModelProperty(position = 20)
    private String dateCreated;
    
    @ApiModelProperty(position = 21)
    private String usrModif;
    
    @ApiModelProperty(position = 22)
    private String dateModif;

    @ApiModelProperty(position = 23)
    private List<TestcaseStepActionDTOV1> actions;

    public TestcaseStepDTOV1() {
    }

    public TestcaseStepDTOV1(String testFolderId, String testcaseId, int stepId, int sort, 
            String loop, String conditionOperator, String conditionValue1, String conditionValue2, 
            String conditionValue3, String conditionOptions, String description, 
            boolean isUsingLibraryStep, String libraryStepTest, String libraryStepTestcase, 
            Integer libraryStepStepId, boolean isStepInUseByOtherTestcase, int libraryStepSort, 
            boolean isLibraryStep, boolean isExecutionForced, String usrCreated, String dateCreated, 
            String usrModif, String dateModif, List<TestcaseStepActionDTOV1> actions) {
        this.testFolderId = testFolderId;
        this.testcaseId = testcaseId;
        this.stepId = stepId;
        this.sort = sort;
        this.loop = loop;
        this.conditionOperator = conditionOperator;
        this.conditionValue1 = conditionValue1;
        this.conditionValue2 = conditionValue2;
        this.conditionValue3 = conditionValue3;
        this.conditionOptions = conditionOptions;
        this.description = description;
        this.isUsingLibraryStep = isUsingLibraryStep;
        this.libraryStepTestFolderId = libraryStepTest;
        this.libraryStepTestcaseId = libraryStepTestcase;
        this.libraryStepStepId = libraryStepStepId;
        this.isStepInUseByOtherTestcase = isStepInUseByOtherTestcase;
        this.libraryStepSort = libraryStepSort;
        this.isLibraryStep = isLibraryStep;
        this.isExecutionForced = isExecutionForced;
        this.usrCreated = usrCreated;
        this.dateCreated = dateCreated;
        this.usrModif = usrModif;
        this.dateModif = dateModif;
        this.actions = actions;
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

    public int getSort() {
        return sort;
    }

    public void setSort(int sort) {
        this.sort = sort;
    }

    public String getLoop() {
        return loop;
    }

    public void setLoop(String loop) {
        this.loop = loop;
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

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public boolean isIsUsingLibraryStep() {
        return isUsingLibraryStep;
    }

    public void setIsUsingLibraryStep(boolean isUsingLibraryStep) {
        this.isUsingLibraryStep = isUsingLibraryStep;
    }

    public String getLibraryStepTestFolderId() {
        return libraryStepTestFolderId;
    }

    public void setLibraryStepTestFolderId(String libraryStepTestFolderId) {
        this.libraryStepTestFolderId = libraryStepTestFolderId;
    }

    public String getLibraryStepTestcaseId() {
        return libraryStepTestcaseId;
    }

    public void setLibraryStepTestcaseId(String libraryStepTestcaseId) {
        this.libraryStepTestcaseId = libraryStepTestcaseId;
    }

    public Integer getLibraryStepStepId() {
        return libraryStepStepId;
    }

    public void setLibraryStepStepId(Integer libraryStepStepId) {
        this.libraryStepStepId = libraryStepStepId;
    }

    public boolean isIsStepInUseByOtherTestcase() {
        return isStepInUseByOtherTestcase;
    }

    public void setIsStepInUseByOtherTestcase(boolean isStepInUseByOtherTestcase) {
        this.isStepInUseByOtherTestcase = isStepInUseByOtherTestcase;
    }

    public int getLibraryStepSort() {
        return libraryStepSort;
    }

    public void setLibraryStepSort(int libraryStepSort) {
        this.libraryStepSort = libraryStepSort;
    }

    public boolean isIsLibraryStep() {
        return isLibraryStep;
    }

    public void setIsLibraryStep(boolean isLibraryStep) {
        this.isLibraryStep = isLibraryStep;
    }

    public boolean isIsExecutionForced() {
        return isExecutionForced;
    }

    public void setIsExecutionForced(boolean isExecutionForced) {
        this.isExecutionForced = isExecutionForced;
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

    public List<TestcaseStepActionDTOV1> getActions() {
        return actions;
    }

    public void setActions(List<TestcaseStepActionDTOV1> actions) {
        this.actions = actions;
    }
    
    
}
