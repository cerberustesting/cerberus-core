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
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.annotation.JsonView;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;
import lombok.ToString;
import lombok.extern.jackson.Jacksonized;
import org.cerberus.core.api.dto.views.View;
import jakarta.validation.constraints.NotEmpty;

@ToString
@Data
@Builder
@Jacksonized
@JsonInclude(JsonInclude.Include.ALWAYS)
@JsonPropertyOrder({
        "system", "country", "environment", "description", "type", "build", "revision",
        "isActive", "chain", "distribList", "isMaintenanceAct", "maintenanceStr", "maintenanceEnd",
        "eMailBodyRevision", "eMailBodyChain", "eMailBodyDisableEnvironment", "envGp"
})
@Schema(name = "CountryEnvParam")
public class CountryEnvParamDTOV001 {

    @NotEmpty
    @JsonView(value = {View.Public.GET.class})
    @Schema(description = "system")
    private String system;

    @NotEmpty
    @JsonView(value = {View.Public.GET.class, View.Public.PATCH.class, View.Public.PUT.class, View.Public.POST.class})
    @Schema(description = "country")
    private String country;

    @JsonView(value = {View.Public.GET.class, View.Public.PATCH.class, View.Public.PUT.class, View.Public.POST.class})
    @Schema(description = "environment")
    private String environment;

    @JsonView(value = {View.Public.GET.class, View.Public.PATCH.class, View.Public.PUT.class, View.Public.POST.class})
    @Schema(description = "description")
    private String description;

    @JsonView(value = {View.Public.GET.class, View.Public.PATCH.class, View.Public.PUT.class, View.Public.POST.class})
    @Schema(description = "type", example = "STD")
    private String type;

    @JsonView(value = {View.Public.GET.class, View.Public.PATCH.class, View.Public.PUT.class, View.Public.POST.class})
    @Schema(description = "build")
    private String build;

    @JsonView(value = {View.Public.GET.class, View.Public.PATCH.class, View.Public.PUT.class, View.Public.POST.class})
    @Schema(description = "revision")
    private String revision;

    @JsonView(value = {View.Public.GET.class, View.Public.PATCH.class, View.Public.PUT.class, View.Public.POST.class})
    @Schema(description = "isActive")
    @JsonProperty("isActive")
    private Boolean isActive;

    @JsonView(value = {View.Public.GET.class, View.Public.PATCH.class, View.Public.PUT.class, View.Public.POST.class})
    @Schema(description = "chain")
    private String chain;

    @JsonView(value = {View.Public.GET.class, View.Public.PATCH.class, View.Public.PUT.class, View.Public.POST.class})
    @Schema(description = "distribList")
    private String distribList;

    @JsonView(value = {View.Public.GET.class, View.Public.PATCH.class, View.Public.PUT.class, View.Public.POST.class})
    @Schema(description = "isMaintenanceAct")
    @JsonProperty("isMaintenanceAct")
    private Boolean isMaintenanceAct;

    @JsonView(value = {View.Public.GET.class, View.Public.PATCH.class, View.Public.PUT.class, View.Public.POST.class})
    @Schema(description = "maintenanceStr", example = "00:00:00")
    private String maintenanceStr;

    @JsonView(value = {View.Public.GET.class, View.Public.PATCH.class, View.Public.PUT.class, View.Public.POST.class})
    @Schema(description = "maintenanceEnd", example = "00:00:00")
    private String maintenanceEnd;

    @JsonView(value = {View.Public.GET.class, View.Public.PATCH.class, View.Public.PUT.class, View.Public.POST.class})
    @Schema(description = "eMailBodyRevision")
    private String eMailBodyRevision;

    @JsonView(value = {View.Public.GET.class, View.Public.PATCH.class, View.Public.PUT.class, View.Public.POST.class})
    @Schema(description = "eMailBodyChain")
    private String eMailBodyChain;

    @JsonView(value = {View.Public.GET.class, View.Public.PATCH.class, View.Public.PUT.class, View.Public.POST.class})
    @Schema(description = "eMailBodyDisableEnvironment")
    private String eMailBodyDisableEnvironment;

    @JsonView(value = {View.Public.GET.class})
    @Schema(description = "envGp")
    private String envGp;
}