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
package org.cerberus.core.api.dto.robot;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.annotation.JsonView;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;
import lombok.extern.jackson.Jacksonized;
import org.cerberus.core.api.dto.views.View;

import javax.validation.constraints.NotBlank;

@Data
@Builder
@Jacksonized
@JsonInclude(JsonInclude.Include.NON_EMPTY)
@JsonPropertyOrder({
        "id",
        "robot",
        "capability",
        "value"
})
@Schema(name = "RobotCapability")
public class RobotCapabilityDTOV001 {

    @JsonView(View.Public.GET.class)
    @Schema(description = "Technical identifier", example = "42")
    private Integer id;

    @JsonView(View.Public.GET.class)
    @Schema(description = "Robot name", example = "CHROME_LINUX")
    private String robot;

    @NotBlank
    @JsonView({View.Public.GET.class, View.Public.POST.class})
    @Schema(description = "Capability key", example = "browserName", required = true)
    private String capability;

    @NotBlank
    @JsonView({View.Public.GET.class, View.Public.POST.class})
    @Schema(description = "Capability value", example = "chrome", required = true)
    private String value;
}
