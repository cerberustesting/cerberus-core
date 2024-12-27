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
package org.cerberus.core.api.dto.testcasecontrol;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonView;
import com.fasterxml.jackson.databind.JsonNode;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;
import lombok.extern.jackson.Jacksonized;
import org.cerberus.core.api.dto.views.View;

/**
 * @author mlombard
 */
@Data
@Builder
@Jacksonized
@ApiModel(value = "TestcaseControl")
public class TestcaseStepActionControlDTOV001 {

    @JsonView(value = {View.Public.GET.class, View.Public.PUT.class})
    @ApiModelProperty(position = 0)
    private String testFolderId;

    @JsonView(value = {View.Public.GET.class, View.Public.PUT.class})
    @ApiModelProperty(position = 1)
    private String testcaseId;

    @JsonView(value = {View.Public.GET.class, View.Public.PUT.class})
    @ApiModelProperty(position = 2)
    private int stepId;

    @JsonView(value = {View.Public.GET.class, View.Public.PUT.class})
    @ApiModelProperty(position = 3)
    private int actionId;

    @JsonView(value = {View.Public.GET.class, View.Public.PUT.class})
    @ApiModelProperty(position = 4)
    private int controlId;

    @NotNull
    @JsonView(value = {View.Public.GET.class, View.Public.PUT.class, View.Public.POST.class})
    @ApiModelProperty(position = 5)
    private int sort;

    @JsonView(value = {View.Public.GET.class, View.Public.PUT.class, View.Public.POST.class})
    @ApiModelProperty(position = 6)
    private String conditionOperator;

    @JsonView(value = {View.Public.GET.class, View.Public.PUT.class, View.Public.POST.class})
    @ApiModelProperty(position = 7)
    private String conditionValue1;

    @JsonView(value = {View.Public.GET.class, View.Public.PUT.class, View.Public.POST.class})
    @ApiModelProperty(position = 8)
    private String conditionValue2;

    @JsonView(value = {View.Public.GET.class, View.Public.PUT.class, View.Public.POST.class})
    @ApiModelProperty(position = 9)
    private String conditionValue3;

    @JsonView(value = {View.Public.GET.class, View.Public.PUT.class, View.Public.POST.class})
    @ApiModelProperty(position = 10)
    private JsonNode conditionOptions;

    @NotBlank(message = "Control is mandatory")
    @JsonView(value = {View.Public.GET.class, View.Public.PUT.class, View.Public.POST.class})
    @ApiModelProperty(position = 11)
    private String control;

    @JsonView(value = {View.Public.GET.class, View.Public.PUT.class, View.Public.POST.class})
    @ApiModelProperty(position = 12)
    private String value1;

    @JsonView(value = {View.Public.GET.class, View.Public.PUT.class, View.Public.POST.class})
    @ApiModelProperty(position = 16)
    private String value2;

    @JsonView(value = {View.Public.GET.class, View.Public.PUT.class, View.Public.POST.class})
    @ApiModelProperty(position = 14)
    private String value3;

    @JsonView(value = {View.Public.GET.class, View.Public.PUT.class, View.Public.POST.class})
    @ApiModelProperty(position = 15)
    private JsonNode options;

    @JsonView(value = {View.Public.GET.class, View.Public.PUT.class, View.Public.POST.class})
    @ApiModelProperty(position = 16)
    @JsonProperty("isFatal")
    private boolean isFatal;

    @NotBlank(message = "A description is mandatory")
    @JsonView(value = {View.Public.GET.class, View.Public.PUT.class, View.Public.POST.class})
    @ApiModelProperty(position = 17)
    private String description;

    @JsonView(value = {View.Public.GET.class, View.Public.PUT.class, View.Public.POST.class})
    @ApiModelProperty(position = 18)
    private boolean doScreenshotBefore;

    @JsonView(value = {View.Public.GET.class, View.Public.PUT.class, View.Public.POST.class})
    @ApiModelProperty(position = 19)
    private boolean doScreenshotAfter;

    @JsonView(value = {View.Public.GET.class, View.Public.PUT.class, View.Public.POST.class})
    @ApiModelProperty(position = 20)
    private int waitBefore;

    @JsonView(value = {View.Public.GET.class, View.Public.PUT.class, View.Public.POST.class})
    @ApiModelProperty(position = 21)
    private int waitAfter;

    @JsonView(value = {View.Public.GET.class, View.Public.PUT.class, View.Public.POST.class})
    @ApiModelProperty(position = 22)
    private String screenshotFilename;

    @JsonView(value = {View.Public.GET.class})
    @ApiModelProperty(position = 23)
    private String usrCreated;

    @JsonView(value = {View.Public.GET.class})
    @ApiModelProperty(position = 24)
    private String dateCreated;

    @JsonView(value = {View.Public.GET.class})
    @ApiModelProperty(position = 25)
    private String usrModif;

    @JsonView(value = {View.Public.GET.class})
    @ApiModelProperty(position = 26)
    private String dateModif;
}