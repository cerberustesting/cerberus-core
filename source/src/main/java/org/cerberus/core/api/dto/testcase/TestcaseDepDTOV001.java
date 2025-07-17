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
import java.sql.Timestamp;
import javax.validation.constraints.NotBlank;
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
        "dependencyTestFolderId",
        "dependencyTestcaseId",
        "dependencyEvent",
        "dependencyTCDelay",
        "type",
        "isActive",
        "description",
        "usrCreated",
        "dateCreated",
        "usrModif",
        "dateModif"
})
@Schema(name = "TestcaseDependency")
public class TestcaseDepDTOV001 {

    @JsonView({View.Public.GET.class, View.Public.PUT.class})
    @Schema(description = "Unique ID of the dependency")
    private long id;

    @NotBlank(message = "The test folder id this testcase is dependent of is mandatory")
    @JsonView({View.Public.GET.class, View.Public.PUT.class, View.Public.POST.class})
    @Schema(description = "The test folder id this testcase depends on", required = true)
    private String dependencyTestFolderId;

    @NotBlank(message = "The testcase id this testcase is dependent of is mandatory")
    @JsonView({View.Public.GET.class, View.Public.PUT.class, View.Public.POST.class})
    @Schema(description = "The testcase id this testcase depends on", required = true)
    private String dependencyTestcaseId;

    @JsonView({View.Public.GET.class, View.Public.PUT.class, View.Public.POST.class})
    @Schema(description = "Dependency event")
    private String dependencyEvent;

    @JsonView({View.Public.GET.class, View.Public.PUT.class, View.Public.POST.class})
    @Schema(description = "Delay for the testcase dependency")
    private Integer dependencyTCDelay;

    @NotNull(message = "Type is mandatory")
    @JsonView({View.Public.GET.class, View.Public.PUT.class, View.Public.POST.class})
    @Schema(description = "Type of the dependency", required = true)
    private String type;

    @JsonView({View.Public.GET.class, View.Public.PUT.class, View.Public.POST.class})
    @Schema(description = "Indicates if the dependency is active")
    @Builder.Default
    private boolean isActive = true;

    @JsonView({View.Public.GET.class, View.Public.PUT.class, View.Public.POST.class})
    @Schema(description = "Description of the dependency")
    private String description;

    @JsonView({View.Public.GET.class})
    @Schema(description = "User who created this dependency")
    private String usrCreated;

    @JsonView({View.Public.GET.class})
    @Schema(description = "Creation timestamp")
    private Timestamp dateCreated;

    @JsonView({View.Public.GET.class})
    @Schema(description = "User who last modified this dependency")
    private String usrModif;

    @JsonView({View.Public.GET.class})
    @Schema(description = "Last modification timestamp")
    private Timestamp dateModif;

}