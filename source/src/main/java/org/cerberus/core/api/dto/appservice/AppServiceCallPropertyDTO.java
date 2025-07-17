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
package org.cerberus.core.api.dto.appservice;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
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
@JsonPropertyOrder({"isActive", "key", "value", "description", "toDelete"})
@Schema(name = "ServiceCallProperty")
public class AppServiceCallPropertyDTO {

    @JsonView({View.Public.GET.class, View.Public.POST.class, View.Public.PUT.class})
    @Schema(description = "Indicates if the property is active")
    private boolean isActive;

    @JsonView({View.Public.GET.class, View.Public.PUT.class, View.Public.POST.class})
    @Schema(description = "Property key")
    private String key;

    @JsonView({View.Public.GET.class, View.Public.PUT.class, View.Public.POST.class})
    @Schema(description = "Property value")
    private String value;

    @JsonView({View.Public.GET.class, View.Public.PUT.class, View.Public.POST.class})
    @Schema(description = "Property description")
    private String description;

    @JsonView({View.Public.GET.class, View.Public.PUT.class, View.Public.POST.class})
    @Schema(description = "Flag to indicate deletion")
    private boolean toDelete;
}