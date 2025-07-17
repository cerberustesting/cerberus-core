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

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.annotation.JsonView;
import com.fasterxml.jackson.databind.JsonNode;
import java.util.List;
import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;
import lombok.ToString;
import lombok.extern.jackson.Jacksonized;
import org.cerberus.core.api.dto.testcase.TestcaseCountryPropertiesDTOV001;
import org.cerberus.core.api.dto.testcaseaction.TestcaseStepActionDTOV001;
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
        "testFolderId", "testcaseId", "stepId", "sort", "loop", "conditionOperator",
        "conditionValue1", "conditionValue2", "conditionValue3", "conditionOptions",
        "description", "isUsingLibraryStep", "libraryStepTestFolderId", "libraryStepTestcaseId",
        "libraryStepStepId", "isStepInUseByOtherTestcase", "libraryStepSort", "isLibraryStep",
        "isExecutionForced", "usrCreated", "dateCreated", "usrModif", "dateModif", "actions", "properties"
})
@Schema(name = "TestcaseStep")
public class TestcaseStepDTOV001 {

    @JsonView({View.Public.GET.class, View.Public.PUT.class})
    @Schema(description = "Test folder ID")
    private String testFolderId;

    @JsonView({View.Public.GET.class, View.Public.PUT.class})
    @Schema(description = "Testcase ID")
    private String testcaseId;

    @JsonView({View.Public.GET.class, View.Public.PUT.class})
    @Schema(description = "Step ID")
    private int stepId;

    @NotNull
    @JsonView({View.Public.GET.class, View.Public.PUT.class, View.Public.POST.class})
    @Schema(description = "Sort order", required = true)
    private int sort;

    @JsonView({View.Public.GET.class, View.Public.PUT.class, View.Public.POST.class})
    @Schema(description = "Loop value")
    private String loop;

    @JsonView({View.Public.GET.class, View.Public.PUT.class, View.Public.POST.class})
    @Schema(description = "Condition operator")
    private String conditionOperator;

    @JsonView({View.Public.GET.class, View.Public.PUT.class, View.Public.POST.class})
    @Schema(description = "Condition value 1")
    private String conditionValue1;

    @JsonView({View.Public.GET.class, View.Public.PUT.class, View.Public.POST.class})
    @Schema(description = "Condition value 2")
    private String conditionValue2;

    @JsonView({View.Public.GET.class, View.Public.PUT.class, View.Public.POST.class})
    @Schema(description = "Condition value 3")
    private String conditionValue3;

    @JsonView({View.Public.GET.class, View.Public.PUT.class, View.Public.POST.class})
    @Schema(description = "Condition options")
    private JsonNode conditionOptions;

    @NotBlank(message = "A description is mandatory")
    @JsonView({View.Public.GET.class, View.Public.PUT.class, View.Public.POST.class})
    @Schema(description = "Step description", required = true)
    private String description;

    @JsonView({View.Public.GET.class, View.Public.PUT.class, View.Public.POST.class})
    @JsonProperty("isUsingLibraryStep")
    @Schema(description = "Flag indicating usage of library step")
    @Builder.Default
    private boolean isUsingLibraryStep = false;

    @JsonView({View.Public.GET.class, View.Public.PUT.class, View.Public.POST.class})
    @Schema(description = "Library step test folder ID")
    private String libraryStepTestFolderId;

    @JsonView({View.Public.GET.class, View.Public.PUT.class, View.Public.POST.class})
    @Schema(description = "Library step testcase ID")
    private String libraryStepTestcaseId;

    @JsonView({View.Public.GET.class, View.Public.PUT.class, View.Public.POST.class})
    @Schema(description = "Library step step ID")
    private Integer libraryStepStepId;

    @JsonView({View.Public.GET.class})
    @JsonProperty("isStepInUseByOtherTestcase")
    @Schema(description = "Flag indicating if step is used by other testcase")
    private boolean isStepInUseByOtherTestcase;

    @JsonView({View.Public.GET.class, View.Public.PUT.class, View.Public.POST.class})
    @Schema(description = "Library step sort order")
    private int libraryStepSort;

    @JsonView({View.Public.GET.class, View.Public.PUT.class, View.Public.POST.class})
    @JsonProperty("isLibraryStep")
    @Schema(description = "Flag indicating if this is a library step")
    @Builder.Default
    private boolean isLibraryStep = false;

    @JsonView({View.Public.GET.class, View.Public.PUT.class, View.Public.POST.class})
    @JsonProperty("isExecutionForced")
    @Schema(description = "Flag indicating if execution is forced")
    @Builder.Default
    private boolean isExecutionForced = false;

    @Valid
    @JsonView({View.Public.GET.class, View.Public.PUT.class, View.Public.POST.class})
    @Schema(description = "List of actions", required = false)
    private List<TestcaseStepActionDTOV001> actions;

    @Valid
    @JsonView({View.Public.GET.class, View.Public.PUT.class, View.Public.POST.class})
    @Schema(description = "List of properties")
    private List<TestcaseCountryPropertiesDTOV001> properties;

    @JsonView({View.Public.GET.class})
    @Schema(description = "User who created")
    private String usrCreated;

    @JsonView({View.Public.GET.class})
    @Schema(description = "Date created")
    private String dateCreated;

    @JsonView({View.Public.GET.class})
    @Schema(description = "User who modified")
    private String usrModif;

    @JsonView({View.Public.GET.class})
    @Schema(description = "Date modified")
    private String dateModif;
}