/*
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
package org.cerberus.core.api.dto.application;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.annotation.JsonView;
import java.util.List;
import javax.validation.constraints.NotEmpty;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;
import lombok.ToString;
import lombok.extern.jackson.Jacksonized;
import org.cerberus.core.api.dto.views.View;

@ToString
@Data
@Builder
@Jacksonized
@JsonInclude(JsonInclude.Include.NON_EMPTY)
@JsonPropertyOrder({
        "application", "description", "sort", "type", "system", "subsystem", "svnurl",
        "bugTrackerUrl", "bugTrackerNewUrl", "poolSize", "deploytype", "mavengroupid",
        "bugTrackerConnector", "bugTrackerParam1", "bugTrackerParam2", "bugTrackerParam3",
        "environments", "usrCreated", "dateCreated", "usrModif", "dateModif"
})
@Schema(name = "Application")
public class ApplicationDTOV001 {

    @NotEmpty
    @JsonView({View.Public.GET.class, View.Public.PATCH.class, View.Public.PUT.class, View.Public.POST.class})
    @Schema(description = "Application name")
    private String application;

    @JsonView({View.Public.GET.class, View.Public.PATCH.class, View.Public.PUT.class, View.Public.POST.class})
    @Schema(description = "Description")
    private String description;

    @JsonView({View.Public.GET.class, View.Public.PATCH.class, View.Public.PUT.class, View.Public.POST.class})
    @Schema(description = "Sort order")
    private Integer sort;

    @JsonView({View.Public.GET.class, View.Public.PATCH.class, View.Public.PUT.class, View.Public.POST.class})
    @Schema(description = "Type")
    private String type;

    @JsonView({View.Public.GET.class, View.Public.PATCH.class, View.Public.PUT.class, View.Public.POST.class})
    @Schema(description = "System")
    private String system;

    @JsonView({View.Public.GET.class, View.Public.PATCH.class, View.Public.PUT.class, View.Public.POST.class})
    @Schema(description = "Subsystem")
    private String subsystem;

    @JsonView({View.Public.GET.class, View.Public.PATCH.class, View.Public.PUT.class, View.Public.POST.class})
    @Schema(description = "SVN URL")
    private String svnurl;

    @JsonView({View.Public.GET.class, View.Public.PATCH.class, View.Public.PUT.class, View.Public.POST.class})
    @Schema(description = "Bug tracker URL")
    private String bugTrackerUrl;

    @JsonView({View.Public.GET.class, View.Public.PATCH.class, View.Public.PUT.class, View.Public.POST.class})
    @Schema(description = "Bug tracker new URL")
    private String bugTrackerNewUrl;

    @JsonView({View.Public.GET.class, View.Public.PATCH.class, View.Public.PUT.class, View.Public.POST.class})
    @Schema(description = "Bug tracker connector")
    private String bugTrackerConnector;

    @JsonView({View.Public.GET.class, View.Public.PATCH.class, View.Public.PUT.class, View.Public.POST.class})
    @Schema(description = "Bug tracker param 1")
    private String bugTrackerParam1;

    @JsonView({View.Public.GET.class, View.Public.PATCH.class, View.Public.PUT.class, View.Public.POST.class})
    @Schema(description = "Bug tracker param 2")
    private String bugTrackerParam2;

    @JsonView({View.Public.GET.class, View.Public.PATCH.class, View.Public.PUT.class, View.Public.POST.class})
    @Schema(description = "Bug tracker param 3")
    private String bugTrackerParam3;

    @JsonView({View.Public.GET.class, View.Public.PATCH.class, View.Public.PUT.class, View.Public.POST.class})
    @Schema(description = "Pool size")
    private Integer poolSize;

    @JsonView({View.Public.GET.class, View.Public.PATCH.class, View.Public.PUT.class, View.Public.POST.class})
    @Schema(description = "Deploy type")
    private String deploytype;

    @JsonView({View.Public.GET.class, View.Public.PATCH.class, View.Public.PUT.class, View.Public.POST.class})
    @Schema(description = "Maven group ID")
    private String mavengroupid;

    @JsonView({View.Public.GET.class, View.Public.PATCH.class, View.Public.PUT.class, View.Public.POST.class})
    @Schema(description = "Environments")
    private List<CountryEnvironmentParametersDTOV001> environments;

    @JsonView({View.Public.GET.class, View.Public.PATCH.class, View.Public.PUT.class, View.Public.POST.class})
    @Schema(description = "User who created")
    private String usrCreated;

    @JsonView(View.Public.GET.class)
    @Schema(description = "Date created")
    private String dateCreated;

    @JsonView({View.Public.GET.class, View.Public.PATCH.class, View.Public.PUT.class, View.Public.POST.class})
    @Schema(description = "User who modified")
    private String usrModif;

    @JsonView(View.Public.GET.class)
    @Schema(description = "Date modified")
    private String dateModif;
}