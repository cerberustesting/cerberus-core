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

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.annotation.JsonView;
import com.fasterxml.jackson.databind.JsonNode;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;
import lombok.ToString;
import lombok.extern.jackson.Jacksonized;
import org.cerberus.core.api.dto.views.View;

/**
 * @author mlombard
 */
@ToString
@Data
@Builder
@Jacksonized
@JsonInclude(JsonInclude.Include.NON_EMPTY)
@JsonPropertyOrder({
        "testFolderId", "testcaseId", "stepId", "actionId", "controlId", "sort",
        "conditionOperator", "conditionValue1", "conditionValue2", "conditionValue3", "conditionOptions",
        "control", "value1", "value3", "options", "value2", "isFatal", "description",
        "doScreenshotBefore", "doScreenshotAfter", "waitBefore", "waitAfter", "screenshotFilename",
        "usrCreated", "dateCreated", "usrModif", "dateModif"
})
@Schema(name = "TestcaseControl")
public class TestcaseStepActionControlDTOV001 {

    @JsonView({View.Public.GET.class, View.Public.PUT.class})
    @Schema(description = "Test folder ID")
    private String testFolderId;

    @JsonView({View.Public.GET.class, View.Public.PUT.class})
    @Schema(description = "Testcase ID")
    private String testcaseId;

    @JsonView({View.Public.GET.class, View.Public.PUT.class})
    @Schema(description = "Step ID")
    private int stepId;

    @JsonView({View.Public.GET.class, View.Public.PUT.class})
    @Schema(description = "Action ID")
    private int actionId;

    @JsonView({View.Public.GET.class, View.Public.PUT.class})
    @Schema(description = "Control ID")
    private int controlId;

    @NotNull
    @JsonView({View.Public.GET.class, View.Public.PUT.class, View.Public.POST.class})
    @Schema(description = "Sort order", required = true)
    private int sort;

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

    @NotBlank(message = "Control is mandatory")
    @JsonView({View.Public.GET.class, View.Public.PUT.class, View.Public.POST.class})
    @Schema(description = "Control", required = true)
    private String control;

    @JsonView({View.Public.GET.class, View.Public.PUT.class, View.Public.POST.class})
    @Schema(description = "Value 1")
    private String value1;

    @JsonView({View.Public.GET.class, View.Public.PUT.class, View.Public.POST.class})
    @Schema(description = "Value 3")
    private String value3;

    @JsonView({View.Public.GET.class, View.Public.PUT.class, View.Public.POST.class})
    @Schema(description = "Options")
    private JsonNode options;

    @JsonView({View.Public.GET.class, View.Public.PUT.class, View.Public.POST.class})
    @Schema(description = "Value 2")
    private String value2;

    @JsonView({View.Public.GET.class, View.Public.PUT.class, View.Public.POST.class})
    @JsonProperty("isFatal")
    @Schema(description = "Is fatal")
    private boolean isFatal;

    @NotBlank(message = "A description is mandatory")
    @JsonView({View.Public.GET.class, View.Public.PUT.class, View.Public.POST.class})
    @Schema(description = "Description", required = true)
    private String description;

    @JsonView({View.Public.GET.class, View.Public.PUT.class, View.Public.POST.class})
    @Schema(description = "Do screenshot before")
    private boolean doScreenshotBefore;

    @JsonView({View.Public.GET.class, View.Public.PUT.class, View.Public.POST.class})
    @Schema(description = "Do screenshot after")
    private boolean doScreenshotAfter;

    @JsonView({View.Public.GET.class, View.Public.PUT.class, View.Public.POST.class})
    @Schema(description = "Wait before (ms)")
    private int waitBefore;

    @JsonView({View.Public.GET.class, View.Public.PUT.class, View.Public.POST.class})
    @Schema(description = "Wait after (ms)")
    private int waitAfter;

    @JsonView({View.Public.GET.class, View.Public.PUT.class, View.Public.POST.class})
    @Schema(description = "Screenshot filename")
    private String screenshotFilename;

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