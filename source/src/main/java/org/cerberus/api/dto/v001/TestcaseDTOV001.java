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
import com.fasterxml.jackson.annotation.JsonView;
import com.fasterxml.jackson.databind.JsonNode;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;
import lombok.extern.jackson.Jacksonized;
import org.cerberus.api.dto.views.View;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Positive;
import java.util.List;

/**
 * @author MorganLmd
 */
@Data
@Builder
@Jacksonized
@Schema(name = "Testcase")
public class TestcaseDTOV001 {

    @NotBlank(message = "Test folder id is mandatory")
    @JsonView(value = {View.Public.GET.class, View.Public.PUT.class, View.Public.POST.class})
    private String testFolderId;

    @JsonView(value = {View.Public.GET.class, View.Public.PUT.class})
    @Pattern(regexp = "^[0-9]{4}[AB]$")
    private String testcaseId;

    @NotBlank(message = "Application attribute is mandatory")
    @JsonView(value = {View.Public.GET.class, View.Public.PUT.class, View.Public.POST.class})
    private String application;

    @NotBlank(message = "A description is mandatory")
    @JsonView(value = {View.Public.GET.class, View.Public.PUT.class, View.Public.POST.class})
    private String description;

    @JsonView(value = {View.Public.GET.class, View.Public.PUT.class, View.Public.POST.class})
    private String detailedDescription;

    @Positive(message = "Priority must be a positive value")
    @JsonView(value = {View.Public.GET.class, View.Public.PUT.class, View.Public.POST.class})
    private int priority;

    @JsonView(value = {View.Public.GET.class, View.Public.PUT.class, View.Public.POST.class})
    private int version;

    @JsonView(value = {View.Public.GET.class, View.Public.PUT.class, View.Public.POST.class})
    private String status;

    @JsonView(value = {View.Public.GET.class, View.Public.PUT.class, View.Public.POST.class})
    @JsonProperty("isActive")
    private boolean isActive;

    @JsonView(value = {View.Public.GET.class, View.Public.PUT.class, View.Public.POST.class})
    @JsonProperty("isActiveQA")
    private boolean isActiveQA;

    @JsonView(value = {View.Public.GET.class, View.Public.PUT.class, View.Public.POST.class})
    @JsonProperty("isActiveUAT")
    private boolean isActiveUAT;

    @JsonView(value = {View.Public.GET.class, View.Public.PUT.class, View.Public.POST.class})
    @JsonProperty("isActivePROD")
    private boolean isActivePROD;

    @JsonView(value = {View.Public.GET.class, View.Public.PUT.class, View.Public.POST.class})
    private String conditionOperator;

    @JsonView(value = {View.Public.GET.class, View.Public.PUT.class, View.Public.POST.class})
    private String conditionValue1;

    @JsonView(value = {View.Public.GET.class, View.Public.PUT.class, View.Public.POST.class})
    private String conditionValue2;

    @JsonView(value = {View.Public.GET.class, View.Public.PUT.class, View.Public.POST.class})
    private String conditionValue3;

    @JsonView(value = {View.Public.GET.class, View.Public.PUT.class, View.Public.POST.class})
    private JsonNode conditionOptions;

    @JsonView(value = {View.Public.GET.class, View.Public.PUT.class, View.Public.POST.class})
    private String type;

    @JsonView(value = {View.Public.GET.class, View.Public.PUT.class, View.Public.POST.class})
    private String externalProvider;

    @JsonView(value = {View.Public.GET.class, View.Public.PUT.class, View.Public.POST.class})
    private String externalReference;

    @JsonView(value = {View.Public.GET.class, View.Public.PUT.class, View.Public.POST.class})
    private String comment;

    @JsonView(value = {View.Public.GET.class, View.Public.PUT.class, View.Public.POST.class})
    private String fromMajor;

    @JsonView(value = {View.Public.GET.class, View.Public.PUT.class, View.Public.POST.class})
    private String fromMinor;

    @JsonView(value = {View.Public.GET.class, View.Public.PUT.class, View.Public.POST.class})
    private String toMajor;

    @JsonView(value = {View.Public.GET.class, View.Public.PUT.class, View.Public.POST.class})
    private String toMinor;

    @JsonView(value = {View.Public.GET.class, View.Public.PUT.class, View.Public.POST.class})
    private JsonNode bugs;

    @JsonView(value = {View.Public.GET.class, View.Public.PUT.class, View.Public.POST.class})
    private String targetMajor;

    @JsonView(value = {View.Public.GET.class, View.Public.PUT.class, View.Public.POST.class})
    private String targetMinor;

    @JsonView(value = {View.Public.GET.class, View.Public.PUT.class, View.Public.POST.class})
    private String implementer;

    @JsonView(value = {View.Public.GET.class, View.Public.PUT.class, View.Public.POST.class})
    private String executor;

    @JsonView(value = {View.Public.GET.class, View.Public.PUT.class, View.Public.POST.class})
    private String userAgent;

    @JsonView(value = {View.Public.GET.class, View.Public.PUT.class, View.Public.POST.class})
    private String screenSize;

    @Valid
    @JsonView(value = {View.Public.GET.class, View.Public.PUT.class, View.Public.POST.class})
    private List<TestcaseStepDTOV001> steps;

    @Valid
    @JsonView(value = {View.Public.GET.class, View.Public.PUT.class, View.Public.POST.class})
    private List<InvariantDTOV001> countries;

    @Valid
    @JsonView(value = {View.Public.GET.class, View.Public.PUT.class, View.Public.POST.class})
    private List<TestcaseCountryPropertiesDTOV001> properties;

    @Valid
    @JsonView(value = {View.Public.GET.class, View.Public.PUT.class, View.Public.POST.class})
    private List<TestcaseCountryPropertiesDTOV001> inheritedProperties;

    @Valid
    @JsonView(value = {View.Public.GET.class, View.Public.PUT.class, View.Public.POST.class})
    private List<LabelDTOV001> labels;

    @Valid
    @JsonView(value = {View.Public.GET.class, View.Public.PUT.class, View.Public.POST.class})
    private List<TestcaseDepDTOV001> dependencies;

    @JsonView(value = {View.Public.GET.class})
    private String usrCreated;

    @JsonView(value = {View.Public.GET.class})
    private String dateCreated;

    @JsonView(value = {View.Public.GET.class})
    private String usrModif;

    @JsonView(value = {View.Public.GET.class})
    private String dateModif;

}
