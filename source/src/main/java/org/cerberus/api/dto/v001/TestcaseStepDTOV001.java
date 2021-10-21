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
import lombok.Builder;
import lombok.Data;

/**
 *
 * @author mlombard
 */
@Data
@Builder
@ApiModel(value = "TestcaseStep")
public class TestcaseStepDTOV001 {
    
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
    private JsonNode conditionOptions;
    
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
    private List<TestcaseStepActionDTOV001> actions;
}
