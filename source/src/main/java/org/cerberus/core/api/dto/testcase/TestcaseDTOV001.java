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
package org.cerberus.core.api.dto.testcase;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonView;
import com.fasterxml.jackson.databind.JsonNode;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.util.List;
import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Positive;
import lombok.Builder;
import lombok.Data;
import lombok.extern.jackson.Jacksonized;
import org.cerberus.core.api.dto.invariant.InvariantDTOV001;
import org.cerberus.core.api.dto.testcasestep.TestcaseStepDTOV001;
import org.cerberus.core.api.dto.views.View;

/**
 * @author MorganLmd
 */
@Data
@Builder
@Jacksonized
@ApiModel(value = "Testcase")
public class TestcaseDTOV001 {

    @NotBlank(message = "Test folder id is mandatory")
    @JsonView(value = {View.Public.GET.class, View.Public.PUT.class, View.Public.POST.class})
    @ApiModelProperty(example = "Examples", position = 0)
    private String testFolderId;

    @JsonView(value = {View.Public.GET.class, View.Public.PUT.class})
    @ApiModelProperty(example = "0001A", position = 1)
    private String testcaseId;

    @NotBlank(message = "Application attribute is mandatory")
    @JsonView(value = {View.Public.GET.class, View.Public.PUT.class, View.Public.POST.class})
    @ApiModelProperty(example = "Google", position = 2)
    private String application;

    @NotBlank(message = "A description is mandatory")
    @JsonView(value = {View.Public.GET.class, View.Public.PUT.class, View.Public.POST.class})
    @ApiModelProperty(example = "Search for Cerberus Website", position = 3)
    private String description;

    @JsonView(value = {View.Public.GET.class, View.Public.PUT.class, View.Public.POST.class})
    @ApiModelProperty(example = "<p>This test case shows how to implement scenarios with basic actions.&nbsp;</p>", position = 4)
    private String detailedDescription;

    @Positive(message = "Priority must be a positive value")
    @JsonView(value = {View.Public.GET.class, View.Public.PUT.class, View.Public.POST.class})
    @ApiModelProperty(example = "1", position = 5)
    private int priority;

    @JsonView(value = {View.Public.GET.class, View.Public.PUT.class, View.Public.POST.class})
    @ApiModelProperty(example = "5", position = 6)
    private int version;

    @JsonView(value = {View.Public.GET.class, View.Public.PUT.class, View.Public.POST.class})
    @ApiModelProperty(example = "WORKING", position = 7)
    private String status;

    @JsonView(value = {View.Public.GET.class, View.Public.PUT.class, View.Public.POST.class})
    @ApiModelProperty(example = "true", position = 8)
    @JsonProperty("isActive")
    private boolean isActive;

    @JsonView(value = {View.Public.GET.class, View.Public.PUT.class, View.Public.POST.class})
    @ApiModelProperty(example = "true", position = 9)
    @JsonProperty("isActiveQA")
    private boolean isActiveQA;

    @JsonView(value = {View.Public.GET.class, View.Public.PUT.class, View.Public.POST.class})
    @ApiModelProperty(example = "true", position = 10)
    @JsonProperty("isActiveUAT")
    private boolean isActiveUAT;

    @JsonView(value = {View.Public.GET.class, View.Public.PUT.class, View.Public.POST.class})
    @ApiModelProperty(example = "true", position = 11)
    @JsonProperty("isActivePROD")
    private boolean isActivePROD;

    @JsonView(value = {View.Public.GET.class, View.Public.PUT.class, View.Public.POST.class})
    @ApiModelProperty(example = "always", position = 12)
    private String conditionOperator;

    @JsonView(value = {View.Public.GET.class, View.Public.PUT.class, View.Public.POST.class})
    @ApiModelProperty(example = "", position = 13)
    private String conditionValue1;

    @JsonView(value = {View.Public.GET.class, View.Public.PUT.class, View.Public.POST.class})
    @ApiModelProperty(example = "", position = 14)
    private String conditionValue2;

    @JsonView(value = {View.Public.GET.class, View.Public.PUT.class, View.Public.POST.class})
    @ApiModelProperty(example = "", position = 15)
    private String conditionValue3;

    @JsonView(value = {View.Public.GET.class, View.Public.PUT.class, View.Public.POST.class})
    @ApiModelProperty(example = "", position = 16)
    private JsonNode conditionOptions;

    @JsonView(value = {View.Public.GET.class, View.Public.PUT.class, View.Public.POST.class})
    @ApiModelProperty(example = "AUTOMATED", position = 17)
    private String type;

    @JsonView(value = {View.Public.GET.class, View.Public.PUT.class, View.Public.POST.class})
    @ApiModelProperty(example = "RX", position = 18)
    private String externalProvider;

    @JsonView(value = {View.Public.GET.class, View.Public.PUT.class, View.Public.POST.class})
    @ApiModelProperty(example = "", position = 19)
    private String externalReference;

    @JsonView(value = {View.Public.GET.class, View.Public.PUT.class, View.Public.POST.class})
    @ApiModelProperty(example = "", position = 20)
    private String comment;

    @JsonView(value = {View.Public.GET.class, View.Public.PUT.class, View.Public.POST.class})
    @ApiModelProperty(example = "", position = 21)
    private String fromMajor;

    @JsonView(value = {View.Public.GET.class, View.Public.PUT.class, View.Public.POST.class})
    @ApiModelProperty(example = "", position = 22)
    private String fromMinor;

    @JsonView(value = {View.Public.GET.class, View.Public.PUT.class, View.Public.POST.class})
    @ApiModelProperty(example = "", position = 23)
    private String toMajor;

    @JsonView(value = {View.Public.GET.class, View.Public.PUT.class, View.Public.POST.class})
    @ApiModelProperty(example = "", position = 24)
    private String toMinor;

    @JsonView(value = {View.Public.GET.class, View.Public.PUT.class, View.Public.POST.class})
    @ApiModelProperty(example = "", position = 25)
    private JsonNode bugs;

    @JsonView(value = {View.Public.GET.class, View.Public.PUT.class, View.Public.POST.class})
    @ApiModelProperty(example = "", position = 26)
    private String targetMajor;

    @JsonView(value = {View.Public.GET.class, View.Public.PUT.class, View.Public.POST.class})
    @ApiModelProperty(example = "", position = 27)
    private String targetMinor;

    @JsonView(value = {View.Public.GET.class, View.Public.PUT.class, View.Public.POST.class})
    @ApiModelProperty(example = "cerberus", position = 28)
    private String implementer;

    @JsonView(value = {View.Public.GET.class, View.Public.PUT.class, View.Public.POST.class})
    @ApiModelProperty(example = "", position = 29)
    private String executor;

    @JsonView(value = {View.Public.GET.class, View.Public.PUT.class, View.Public.POST.class})
    @ApiModelProperty(example = "", position = 30)
    private String userAgent;

    @JsonView(value = {View.Public.GET.class, View.Public.PUT.class, View.Public.POST.class})
    @ApiModelProperty(example = "", position = 31)
    private String screenSize;

    @Valid
    @JsonView(value = {View.Public.GET.class, View.Public.PUT.class, View.Public.POST.class})
    @ApiModelProperty(position = 36, required = false)
    private List<TestcaseStepDTOV001> steps;

    @Valid
    @JsonView(value = {View.Public.GET.class, View.Public.PUT.class, View.Public.POST.class})
    @ApiModelProperty(position = 37)
    private List<InvariantDTOV001> countries;

    @Valid
    @JsonView(value = {View.Public.GET.class, View.Public.PUT.class, View.Public.POST.class})
    @ApiModelProperty(position = 38)
    private List<TestcaseCountryPropertiesDTOV001> properties;

    @Valid
    @JsonView(value = {View.Public.GET.class, View.Public.PUT.class, View.Public.POST.class})
    @ApiModelProperty(position = 39)
    private List<TestcaseCountryPropertiesDTOV001> inheritedProperties;

    @Valid
    @JsonView(value = {View.Public.GET.class, View.Public.PUT.class, View.Public.POST.class})
    @ApiModelProperty(position = 40)
    private List<LabelDTOV001> labels;

    @Valid
    @JsonView(value = {View.Public.GET.class, View.Public.PUT.class, View.Public.POST.class})
    @ApiModelProperty(position = 41)
    private List<TestcaseDepDTOV001> dependencies;

    @JsonView(value = {View.Public.GET.class})
    @ApiModelProperty(example = "cerberus", position = 32)
    private String usrCreated;

    @JsonView(value = {View.Public.GET.class})
    @ApiModelProperty(example = "2012-06-19 09:56:40.0", position = 33)
    private String dateCreated;

    @JsonView(value = {View.Public.GET.class})
    @ApiModelProperty(example = "a03e2ae7-6fe6-42df-b2d6-6f7542ee3ed3", position = 34)
    private String usrModif;

    @JsonView(value = {View.Public.GET.class})
    @ApiModelProperty(example = "2019-04-06 10:15:09.0", position = 35)
    private String dateModif;

}
