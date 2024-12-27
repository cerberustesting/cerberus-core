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
package org.cerberus.core.api.dto.testcasestep;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonView;
import com.fasterxml.jackson.databind.JsonNode;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.util.List;
import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;
import lombok.extern.jackson.Jacksonized;
import org.cerberus.core.api.dto.testcase.TestcaseCountryPropertiesDTOV001;
import org.cerberus.core.api.dto.testcaseaction.TestcaseStepActionDTOV001;
import org.cerberus.core.api.dto.views.View;

/**
 * @author MorganLmd
 */
@Data
@Builder
@Jacksonized
@ApiModel(value = "TestcaseStep")
public class TestcaseStepDTOV001 {

    @JsonView(value = {View.Public.GET.class, View.Public.PUT.class})
    @ApiModelProperty(position = 0)
    private String testFolderId;

    @JsonView(value = {View.Public.GET.class, View.Public.PUT.class})
    @ApiModelProperty(position = 1)
    private String testcaseId;

    @JsonView(value = {View.Public.GET.class, View.Public.PUT.class})
    @ApiModelProperty(position = 2)
    private int stepId;

    @NotNull
    @JsonView(value = {View.Public.GET.class, View.Public.PUT.class, View.Public.POST.class})
    @ApiModelProperty(position = 3)
    private int sort;

    @JsonView(value = {View.Public.GET.class, View.Public.PUT.class, View.Public.POST.class})
    @ApiModelProperty(position = 4)
    private String loop;

    @JsonView(value = {View.Public.GET.class, View.Public.PUT.class, View.Public.POST.class})
    @ApiModelProperty(position = 5)
    private String conditionOperator;

    @JsonView(value = {View.Public.GET.class, View.Public.PUT.class, View.Public.POST.class})
    @ApiModelProperty(position = 6)
    private String conditionValue1;

    @JsonView(value = {View.Public.GET.class, View.Public.PUT.class, View.Public.POST.class})
    @ApiModelProperty(position = 7)
    private String conditionValue2;

    @JsonView(value = {View.Public.GET.class, View.Public.PUT.class, View.Public.POST.class})
    @ApiModelProperty(position = 8)
    private String conditionValue3;

    @JsonView(value = {View.Public.GET.class, View.Public.PUT.class, View.Public.POST.class})
    @ApiModelProperty(position = 9)
    private JsonNode conditionOptions;

    @NotBlank(message = "A description is mandatory")
    @JsonView(value = {View.Public.GET.class, View.Public.PUT.class, View.Public.POST.class})
    @ApiModelProperty(position = 10)
    private String description;

    @JsonView(value = {View.Public.GET.class, View.Public.PUT.class, View.Public.POST.class})
    @ApiModelProperty(position = 11)
    @JsonProperty("isUsingLibraryStep")
    @Builder.Default
    private boolean isUsingLibraryStep = false;

    @JsonView(value = {View.Public.GET.class, View.Public.PUT.class, View.Public.POST.class})
    @ApiModelProperty(position = 12)
    private String libraryStepTestFolderId;

    @JsonView(value = {View.Public.GET.class, View.Public.PUT.class, View.Public.POST.class})
    @ApiModelProperty(position = 13)
    private String libraryStepTestcaseId;

    @JsonView(value = {View.Public.GET.class, View.Public.PUT.class, View.Public.POST.class})
    @ApiModelProperty(position = 14)
    private Integer libraryStepStepId;

    @JsonView(value = {View.Public.GET.class})
    @ApiModelProperty(position = 15)
    @JsonProperty("isStepInUseByOtherTestcase")
    private boolean isStepInUseByOtherTestcase;

    @JsonView(value = {View.Public.GET.class, View.Public.PUT.class, View.Public.POST.class})
    @ApiModelProperty(position = 16)
    private int libraryStepSort;

    @JsonView(value = {View.Public.GET.class, View.Public.PUT.class, View.Public.POST.class})
    @ApiModelProperty(name = "isLibraryStep", position = 17)
    @JsonProperty("isLibraryStep")
    @Builder.Default
    private boolean isLibraryStep = false;

    @JsonView(value = {View.Public.GET.class, View.Public.PUT.class, View.Public.POST.class})
    @ApiModelProperty(position = 18)
    @JsonProperty("isExecutionForced")
    @Builder.Default
    private boolean isExecutionForced = false;

    @Valid
    @JsonView(value = {View.Public.GET.class, View.Public.PUT.class, View.Public.POST.class})
    @ApiModelProperty(position = 23, required = false)
    private List<TestcaseStepActionDTOV001> actions;

    @Valid
    @JsonView(value = {View.Public.GET.class, View.Public.PUT.class, View.Public.POST.class})
    @ApiModelProperty(position = 24)
    private List<TestcaseCountryPropertiesDTOV001> properties;

    @JsonView(value = {View.Public.GET.class})
    @ApiModelProperty(position = 19)
    private String usrCreated;

    @JsonView(value = {View.Public.GET.class})
    @ApiModelProperty(position = 20)
    private String dateCreated;

    @JsonView(value = {View.Public.GET.class})
    @ApiModelProperty(position = 21)
    private String usrModif;

    @JsonView(value = {View.Public.GET.class})
    @ApiModelProperty(position = 22)
    private String dateModif;
}
