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

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.annotation.JsonView;
import com.fasterxml.jackson.databind.JsonNode;
import java.util.List;
import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Positive;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;
import lombok.ToString;
import lombok.extern.jackson.Jacksonized;
import org.cerberus.core.api.dto.invariant.InvariantDTOV001;
import org.cerberus.core.api.dto.testcasestep.TestcaseStepDTOV001;
import org.cerberus.core.api.dto.views.View;

/**
 * @author MorganLmd
 */
@ToString
@Data
@Builder
@Jacksonized
@JsonInclude(JsonInclude.Include.NON_EMPTY)
@JsonPropertyOrder({
        "testFolderId", "testcaseId", "application", "description", "detailedDescription",
        "priority", "version", "status", "isActive", "isActiveQA", "isActiveUAT", "isActivePROD",
        "conditionOperator", "conditionValue1", "conditionValue2", "conditionValue3", "conditionOptions",
        "type", "externalProvider", "externalReference", "comment", "fromMajor", "fromMinor",
        "toMajor", "toMinor", "bugs", "targetMajor", "targetMinor", "implementer", "executor",
        "userAgent", "screenSize", "usrCreated", "dateCreated", "usrModif", "dateModif",
        "steps", "countries", "properties", "inheritedProperties", "labels", "dependencies"
})
@Schema(name = "Testcase")
public class TestcaseDTOV001 {

    @NotBlank(message = "Test folder id is mandatory")
    @JsonView({View.Public.GET.class, View.Public.PUT.class, View.Public.POST.class})
    @Schema(description = "Test folder ID", example = "Examples", required = true)
    private String testFolderId;

    @JsonView({View.Public.GET.class, View.Public.PUT.class})
    @Schema(description = "Testcase ID", example = "0001A")
    private String testcaseId;

    @NotBlank(message = "Application attribute is mandatory")
    @JsonView({View.Public.GET.class, View.Public.PUT.class, View.Public.POST.class})
    @Schema(description = "Application", example = "Google", required = true)
    private String application;

    @NotBlank(message = "A description is mandatory")
    @JsonView({View.Public.GET.class, View.Public.PUT.class, View.Public.POST.class})
    @Schema(description = "Description", example = "Search for Cerberus Website", required = true)
    private String description;

    @JsonView({View.Public.GET.class, View.Public.PUT.class, View.Public.POST.class})
    @Schema(description = "Detailed description", example = "<p>This test case shows how to implement scenarios with basic actions.&nbsp;</p>")
    private String detailedDescription;

    @Positive(message = "Priority must be a positive value")
    @JsonView({View.Public.GET.class, View.Public.PUT.class, View.Public.POST.class})
    @Schema(description = "Priority", example = "1")
    private int priority;

    @JsonView({View.Public.GET.class, View.Public.PUT.class, View.Public.POST.class})
    @Schema(description = "Version", example = "5")
    private int version;

    @JsonView({View.Public.GET.class, View.Public.PUT.class, View.Public.POST.class})
    @Schema(description = "Status", example = "WORKING")
    private String status;

    @JsonView({View.Public.GET.class, View.Public.PUT.class, View.Public.POST.class})
    @JsonProperty("isActive")
    @Schema(description = "Active flag", example = "true")
    private boolean isActive;

    @JsonView({View.Public.GET.class, View.Public.PUT.class, View.Public.POST.class})
    @JsonProperty("isActiveQA")
    @Schema(description = "QA active flag", example = "true")
    private boolean isActiveQA;

    @JsonView({View.Public.GET.class, View.Public.PUT.class, View.Public.POST.class})
    @JsonProperty("isActiveUAT")
    @Schema(description = "UAT active flag", example = "true")
    private boolean isActiveUAT;

    @JsonView({View.Public.GET.class, View.Public.PUT.class, View.Public.POST.class})
    @JsonProperty("isActivePROD")
    @Schema(description = "PROD active flag", example = "true")
    private boolean isActivePROD;

    @JsonView({View.Public.GET.class, View.Public.PUT.class, View.Public.POST.class})
    @Schema(description = "Condition operator", example = "always")
    private String conditionOperator;

    @JsonView({View.Public.GET.class, View.Public.PUT.class, View.Public.POST.class})
    @Schema(description = "Condition value 1", example = "")
    private String conditionValue1;

    @JsonView({View.Public.GET.class, View.Public.PUT.class, View.Public.POST.class})
    @Schema(description = "Condition value 2", example = "")
    private String conditionValue2;

    @JsonView({View.Public.GET.class, View.Public.PUT.class, View.Public.POST.class})
    @Schema(description = "Condition value 3", example = "")
    private String conditionValue3;

    @JsonView({View.Public.GET.class, View.Public.PUT.class, View.Public.POST.class})
    @Schema(description = "Condition options")
    private JsonNode conditionOptions;

    @JsonView({View.Public.GET.class, View.Public.PUT.class, View.Public.POST.class})
    @Schema(description = "Type", example = "AUTOMATED")
    private String type;

    @JsonView({View.Public.GET.class, View.Public.PUT.class, View.Public.POST.class})
    @Schema(description = "External provider", example = "RX")
    private String externalProvider;

    @JsonView({View.Public.GET.class, View.Public.PUT.class, View.Public.POST.class})
    @Schema(description = "External reference", example = "")
    private String externalReference;

    @JsonView({View.Public.GET.class, View.Public.PUT.class, View.Public.POST.class})
    @Schema(description = "Comment", example = "")
    private String comment;

    @JsonView({View.Public.GET.class, View.Public.PUT.class, View.Public.POST.class})
    @Schema(description = "From major", example = "")
    private String fromMajor;

    @JsonView({View.Public.GET.class, View.Public.PUT.class, View.Public.POST.class})
    @Schema(description = "From minor", example = "")
    private String fromMinor;

    @JsonView({View.Public.GET.class, View.Public.PUT.class, View.Public.POST.class})
    @Schema(description = "To major", example = "")
    private String toMajor;

    @JsonView({View.Public.GET.class, View.Public.PUT.class, View.Public.POST.class})
    @Schema(description = "To minor", example = "")
    private String toMinor;

    @JsonView({View.Public.GET.class, View.Public.PUT.class, View.Public.POST.class})
    @Schema(description = "Bugs")
    private JsonNode bugs;

    @JsonView({View.Public.GET.class, View.Public.PUT.class, View.Public.POST.class})
    @Schema(description = "Target major", example = "")
    private String targetMajor;

    @JsonView({View.Public.GET.class, View.Public.PUT.class, View.Public.POST.class})
    @Schema(description = "Target minor", example = "")
    private String targetMinor;

    @JsonView({View.Public.GET.class, View.Public.PUT.class, View.Public.POST.class})
    @Schema(description = "Implementer", example = "cerberus")
    private String implementer;

    @JsonView({View.Public.GET.class, View.Public.PUT.class, View.Public.POST.class})
    @Schema(description = "Executor", example = "")
    private String executor;

    @JsonView({View.Public.GET.class, View.Public.PUT.class, View.Public.POST.class})
    @Schema(description = "User agent", example = "")
    private String userAgent;

    @JsonView({View.Public.GET.class, View.Public.PUT.class, View.Public.POST.class})
    @Schema(description = "Screen size", example = "")
    private String screenSize;

    @JsonView({View.Public.GET.class})
    @Schema(description = "User who created", example = "cerberus")
    private String usrCreated;

    @JsonView({View.Public.GET.class})
    @Schema(description = "Date created", example = "2012-06-19 09:56:40.0")
    private String dateCreated;

    @JsonView({View.Public.GET.class})
    @Schema(description = "User who modified", example = "a03e2ae7-6fe6-42df-b2d6-6f7542ee3ed3")
    private String usrModif;

    @JsonView({View.Public.GET.class})
    @Schema(description = "Date modified", example = "2019-04-06 10:15:09.0")
    private String dateModif;

    @Valid
    @JsonView({View.Public.GET.class, View.Public.PUT.class, View.Public.POST.class})
    @Schema(description = "Steps", required = false)
    private List<TestcaseStepDTOV001> steps;

    @Valid
    @JsonView({View.Public.GET.class, View.Public.PUT.class, View.Public.POST.class})
    @Schema(description = "Countries", required = false)
    private List<InvariantDTOV001> countries;

    @Valid
    @JsonView({View.Public.GET.class, View.Public.PUT.class, View.Public.POST.class})
    @Schema(description = "Properties", required = false)
    private List<TestcaseCountryPropertiesDTOV001> properties;

    @Valid
    @JsonView({View.Public.GET.class, View.Public.PUT.class, View.Public.POST.class})
    @Schema(description = "Inherited properties", required = false)
    private List<TestcaseCountryPropertiesDTOV001> inheritedProperties;

    @Valid
    @JsonView({View.Public.GET.class, View.Public.PUT.class, View.Public.POST.class})
    @Schema(description = "Labels", required = false)
    private List<LabelDTOV001> labels;

    @Valid
    @JsonView({View.Public.GET.class, View.Public.PUT.class, View.Public.POST.class})
    @Schema(description = "Dependencies", required = false)
    private List<TestcaseDepDTOV001> dependencies;
}