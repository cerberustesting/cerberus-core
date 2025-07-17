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
import java.util.List;
import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;
import lombok.ToString;
import lombok.extern.jackson.Jacksonized;
import org.cerberus.core.api.dto.invariant.InvariantDTOV001;
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
        "testFolderId",
        "testcaseId",
        "property",
        "description",
        "type",
        "database",
        "value1",
        "value2",
        "value3",
        "length",
        "rowLimit",
        "nature",
        "rank",
        "usrCreated",
        "dateCreated",
        "usrModif",
        "dateModif",
        "countries"
})
@Schema(name = "TestcaseCountryProperties")
public class TestcaseCountryPropertiesDTOV001 {

    @JsonView({View.Public.GET.class, View.Public.PUT.class})
    @Schema(description = "Test folder ID")
    private String testFolderId;

    @JsonView({View.Public.GET.class, View.Public.PUT.class})
    @Schema(description = "Testcase ID")
    private String testcaseId;

    @NotBlank(message = "Property is mandatory")
    @JsonView({View.Public.GET.class, View.Public.PUT.class, View.Public.POST.class})
    @Schema(description = "Property name", required = true)
    private String property;

    @JsonView({View.Public.GET.class, View.Public.PUT.class, View.Public.POST.class})
    @Schema(description = "Property description")
    private String description;

    @NotBlank(message = "Message is mandatory")
    @JsonView({View.Public.GET.class, View.Public.PUT.class, View.Public.POST.class})
    @Schema(description = "Type", required = true)
    private String type;

    @JsonView({View.Public.GET.class, View.Public.PUT.class, View.Public.POST.class})
    @Schema(description = "Database")
    private String database;

    @JsonView({View.Public.GET.class, View.Public.PUT.class, View.Public.POST.class})
    @Schema(description = "Value 1")
    private String value1;

    @JsonView({View.Public.GET.class, View.Public.PUT.class, View.Public.POST.class})
    @Schema(description = "Value 2")
    private String value2;

    @JsonView({View.Public.GET.class, View.Public.PUT.class, View.Public.POST.class})
    @Schema(description = "Value 3")
    private String value3;

    @JsonView({View.Public.GET.class, View.Public.PUT.class, View.Public.POST.class})
    @Schema(description = "Length")
    private String length;

    @NotNull(message = "Row limit is mandatory")
    @JsonView({View.Public.GET.class, View.Public.PUT.class, View.Public.POST.class})
    @Schema(description = "Row limit", required = true)
    private int rowLimit;

    @NotNull(message = "Nature is mandatory")
    @JsonView({View.Public.GET.class, View.Public.PUT.class, View.Public.POST.class})
    @Schema(description = "Nature", required = true)
    private String nature;

    @JsonView({View.Public.GET.class, View.Public.PUT.class, View.Public.POST.class})
    @Schema(description = "Rank")
    private int rank;

    @JsonView({View.Public.GET.class})
    @Schema(description = "User who created this property")
    private String usrCreated;

    @JsonView({View.Public.GET.class})
    @Schema(description = "Creation date")
    private String dateCreated;

    @JsonView({View.Public.GET.class})
    @Schema(description = "User who last modified this property")
    private String usrModif;

    @JsonView({View.Public.GET.class})
    @Schema(description = "Modification date")
    private String dateModif;

    @Valid
    @JsonView({View.Public.GET.class, View.Public.PUT.class, View.Public.POST.class})
    @Schema(description = "List of countries associated with this property")
    private List<InvariantDTOV001> countries;
}