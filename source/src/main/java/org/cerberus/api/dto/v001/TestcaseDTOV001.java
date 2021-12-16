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

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.util.List;
import javax.validation.constraints.Pattern;
import lombok.Builder;
import lombok.Data;
import lombok.extern.jackson.Jacksonized;

/**
 * @author MorganLmd
 */
@Data
@Builder
@Jacksonized
@ApiModel(value = "Testcase")
public class TestcaseDTOV001 {

    @ApiModelProperty(example = "Examples", position = 0)
    private String testFolderId;

    @ApiModelProperty(example = "0001A", position = 1)
    @Pattern(regexp = "^[0-9]{4}[AB]$")
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
    @JsonProperty("isActive")
    private boolean isActive;

    @ApiModelProperty(example = "true", position = 9)
    @JsonProperty("isActiveQA")
    private boolean isActiveQA;

    @ApiModelProperty(example = "true", position = 10)
    @JsonProperty("isActiveUAT")
    private boolean isActiveUAT;

    @ApiModelProperty(example = "true", position = 11)
    @JsonProperty("isActivePROD")
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

    @ApiModelProperty( example = "RX", position = 18)
    private String externalProvider;

    @ApiModelProperty(example = "", position = 19)
    private String externalReference;

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
    
    @ApiModelProperty(position = 37, required = false)
    private List<InvariantDTOV001> countries;

    @ApiModelProperty(position = 38)
    private List<TestcaseCountryPropertiesDTOV001> properties;

    @ApiModelProperty(position = 39)
    private List<TestcaseCountryPropertiesDTOV001> inheritedProperties;

    @ApiModelProperty(position = 40)
    private List<LabelDTOV001> labels;

    @ApiModelProperty(position = 41)
    private List<TestcaseDepDTOV001> dependencies;

}
