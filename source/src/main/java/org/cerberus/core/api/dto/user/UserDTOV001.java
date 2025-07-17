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

package org.cerberus.core.api.dto.user;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.annotation.JsonView;
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
        "login", "name", "email", "team", "language",
        "attribute01", "attribute02", "attribute03", "attribute04", "attribute05",
        "comment", "usrCreated", "dateCreated", "usrModif", "dateModif"
})
@Schema(name = "User")
public class UserDTOV001 {

    @NotEmpty
    @JsonView({View.Public.GET.class, View.Public.POST.class, View.Public.PUT.class})
    @Schema(description = "User login", required = true)
    private String login;

    @JsonView({View.Public.GET.class, View.Public.PUT.class, View.Public.POST.class})
    @Schema(description = "User name")
    private String name;

    @NotEmpty
    @JsonView({View.Public.GET.class, View.Public.PUT.class, View.Public.POST.class})
    @Schema(description = "User email", required = true)
    private String email;

    @NotEmpty
    @JsonView({View.Public.GET.class, View.Public.PUT.class, View.Public.POST.class})
    @Schema(description = "User team", required = true)
    private String team;

    @JsonView({View.Public.GET.class, View.Public.PUT.class, View.Public.POST.class})
    @Schema(description = "User language")
    private String language;

    @JsonView({View.Public.GET.class, View.Public.PUT.class, View.Public.POST.class})
    @Schema(description = "Custom attribute 01")
    private String attribute01;

    @JsonView({View.Public.GET.class, View.Public.PUT.class, View.Public.POST.class})
    @Schema(description = "Custom attribute 02")
    private String attribute02;

    @JsonView({View.Public.GET.class, View.Public.PUT.class, View.Public.POST.class})
    @Schema(description = "Custom attribute 03")
    private String attribute03;

    @JsonView({View.Public.GET.class, View.Public.PUT.class, View.Public.POST.class})
    @Schema(description = "Custom attribute 04")
    private String attribute04;

    @JsonView({View.Public.GET.class, View.Public.PUT.class, View.Public.POST.class})
    @Schema(description = "Custom attribute 05")
    private String attribute05;

    @JsonView({View.Public.GET.class, View.Public.PUT.class, View.Public.POST.class})
    @Schema(description = "User comment")
    private String comment;

    @JsonView({View.Public.GET.class, View.Public.PUT.class, View.Public.POST.class})
    @Schema(description = "User who created")
    private String usrCreated;

    @JsonView(View.Public.GET.class)
    @Schema(description = "Date created")
    private String dateCreated;

    @JsonView({View.Public.GET.class, View.Public.PUT.class, View.Public.POST.class})
    @Schema(description = "User who modified")
    private String usrModif;

    @JsonView(View.Public.GET.class)
    @Schema(description = "Date modified")
    private String dateModif;
}