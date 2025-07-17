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
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.annotation.JsonView;
import javax.validation.constraints.NotNull;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;
import lombok.ToString;
import lombok.extern.jackson.Jacksonized;
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
        "id",
        "system",
        "label",
        "type",
        "color",
        "parentLabelID",
        "requirementType",
        "requirementStatus",
        "requirementCriticality",
        "description",
        "detailedDescription",
        "usrCreated",
        "dateCreated",
        "usrModif",
        "dateModif"
})
@Schema(name = "Label")
public class LabelDTOV001 {

    @NotNull(message = "Id is mandatory")
    @JsonView({View.Public.GET.class, View.Public.PUT.class, View.Public.POST.class})
    @Schema(description = "Id", required = true)
    private Integer id;

    @JsonView({View.Public.GET.class, View.Public.PUT.class})
    @Schema(description = "System")
    private String system;

    @JsonView({View.Public.GET.class, View.Public.PUT.class})
    @Schema(description = "Label")
    private String label;

    @JsonView({View.Public.GET.class, View.Public.PUT.class})
    @Schema(description = "Type")
    private String type;

    @JsonView({View.Public.GET.class, View.Public.PUT.class})
    @Schema(description = "Color")
    private String color;

    @JsonView({View.Public.GET.class, View.Public.PUT.class})
    @Schema(description = "Parent Label ID")
    private Integer parentLabelID;

    @JsonView({View.Public.GET.class, View.Public.PUT.class})
    @Schema(description = "Requirement Type")
    private String requirementType;

    @JsonView({View.Public.GET.class, View.Public.PUT.class})
    @Schema(description = "Requirement Status")
    private String requirementStatus;

    @JsonView({View.Public.GET.class, View.Public.PUT.class})
    @Schema(description = "Requirement Criticality")
    private String requirementCriticality;

    @JsonView({View.Public.GET.class, View.Public.PUT.class})
    @Schema(description = "Description")
    private String description;

    @JsonView({View.Public.GET.class, View.Public.PUT.class})
    @Schema(description = "Detailed Description")
    private String detailedDescription;

    @JsonView({View.Public.GET.class})
    @Schema(description = "User who created the label")
    private String usrCreated;

    @JsonView({View.Public.GET.class})
    @Schema(description = "Creation date")
    private String dateCreated;

    @JsonView({View.Public.GET.class})
    @Schema(description = "User who last modified the label")
    private String usrModif;

    @JsonView({View.Public.GET.class})
    @Schema(description = "Modification date")
    private String dateModif;
}