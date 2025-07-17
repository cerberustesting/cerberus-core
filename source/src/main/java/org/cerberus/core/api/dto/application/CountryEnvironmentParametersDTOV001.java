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
import javax.validation.constraints.NotEmpty;

@ToString
@Data
@Builder
@Jacksonized
@JsonInclude(JsonInclude.Include.ALWAYS)
@JsonPropertyOrder({
        "system", "country", "environment", "application", "isActive", "endPoint",
        "contextRoot", "urlLogin", "domain", "var1", "var2", "var3", "var4",
        "secret1", "secret2", "poolSize", "mobileActivity", "mobilePackage",
        "usrCreated", "dateCreated", "usrModif", "dateModif"
})
@Schema(name = "ApplicationEnvironments")
public class CountryEnvironmentParametersDTOV001 {

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
    @Schema(description = "application")
    private String application;

    @JsonView(value = {View.Public.GET.class, View.Public.PATCH.class, View.Public.PUT.class, View.Public.POST.class})
    @Schema(description = "isActive")
    @JsonProperty("isActive")
    private Boolean isActive;

    @JsonView(value = {View.Public.GET.class, View.Public.PATCH.class, View.Public.PUT.class, View.Public.POST.class})
    @Schema(description = "endPoint")
    private String endPoint;

    @JsonView(value = {View.Public.GET.class, View.Public.PATCH.class, View.Public.PUT.class, View.Public.POST.class})
    @Schema(description = "contextRoot")
    private String contextRoot;

    @JsonView(value = {View.Public.GET.class, View.Public.PATCH.class, View.Public.PUT.class, View.Public.POST.class})
    @Schema(description = "urlLogin")
    private String urlLogin;

    @JsonView(value = {View.Public.GET.class, View.Public.PATCH.class, View.Public.PUT.class, View.Public.POST.class})
    @Schema(description = "domain")
    private String domain;

    @JsonView(value = {View.Public.GET.class, View.Public.PATCH.class, View.Public.PUT.class, View.Public.POST.class})
    @Schema(description = "var1")
    private String var1;

    @JsonView(value = {View.Public.GET.class, View.Public.PATCH.class, View.Public.PUT.class, View.Public.POST.class})
    @Schema(description = "var2")
    private String var2;

    @JsonView(value = {View.Public.GET.class, View.Public.PATCH.class, View.Public.PUT.class, View.Public.POST.class})
    @Schema(description = "var3")
    private String var3;

    @JsonView(value = {View.Public.GET.class, View.Public.PATCH.class, View.Public.PUT.class, View.Public.POST.class})
    @Schema(description = "var4")
    private String var4;

    @JsonView(value = {View.Public.GET.class, View.Public.PATCH.class, View.Public.PUT.class, View.Public.POST.class})
    @Schema(description = "secret1")
    private String secret1;

    @JsonView(value = {View.Public.GET.class, View.Public.PATCH.class, View.Public.PUT.class, View.Public.POST.class})
    @Schema(description = "secret2")
    private String secret2;

    @JsonView(value = {View.Public.GET.class, View.Public.PATCH.class, View.Public.PUT.class, View.Public.POST.class})
    @Schema(description = "poolSize")
    private Integer poolSize;

    @JsonView(value = {View.Public.GET.class, View.Public.PATCH.class, View.Public.PUT.class, View.Public.POST.class})
    @Schema(description = "mobileActivity")
    private String mobileActivity;

    @JsonView(value = {View.Public.GET.class, View.Public.PATCH.class, View.Public.PUT.class, View.Public.POST.class})
    @Schema(description = "mobilePackage")
    private String mobilePackage;

    @JsonView(value = {View.Public.GET.class})
    @Schema(description = "usrCreated", example = "cerberus")
    private String usrCreated;

    @JsonView(value = {View.Public.GET.class})
    @Schema(description = "dateCreated", example = "2012-06-19 09:56:40.0")
    private String dateCreated;

    @JsonView(value = {View.Public.GET.class})
    @Schema(description = "usrModif", example = "cerberus")
    private String usrModif;

    @JsonView(value = {View.Public.GET.class})
    @Schema(description = "dateModif", example = "2019-04-06 10:15:09.0")
    private String dateModif;
}
