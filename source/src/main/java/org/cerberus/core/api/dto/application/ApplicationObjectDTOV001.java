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
        "id", "application", "object", "value",
        "screenshotFilename", "xOffset", "yOffset",
        "usrCreated", "dateCreated", "usrModif", "dateModif"
})
@Schema(name = "ApplicationObject")
public class ApplicationObjectDTOV001 {

    @JsonView({View.Public.GET.class})
    @Schema(description = "Technical identifier of the application object")
    private int id;

    @JsonView({View.Public.GET.class, View.Public.PUT.class, View.Public.POST.class})
    @Schema(description = "Application this object belongs to")
    private String application;

    @JsonView({View.Public.GET.class, View.Public.PUT.class, View.Public.POST.class})
    @Schema(description = "Object name (unique per application)")
    private String object;

    @JsonView({View.Public.GET.class, View.Public.PUT.class, View.Public.POST.class})
    @Schema(description = "Locator value (e.g. XPath, CSS selector, element identifier)")
    private String value;

    @JsonView({View.Public.GET.class, View.Public.PUT.class, View.Public.POST.class})
    @Schema(description = "Screenshot filename associated with this object")
    private String screenshotFilename;

    @JsonView({View.Public.GET.class, View.Public.PUT.class, View.Public.POST.class})
    @Schema(description = "Horizontal offset for element coordinates")
    private String xOffset;

    @JsonView({View.Public.GET.class, View.Public.PUT.class, View.Public.POST.class})
    @Schema(description = "Vertical offset for element coordinates")
    private String yOffset;

    @JsonView({View.Public.GET.class})
    @Schema(description = "User who created this object")
    private String usrCreated;

    @JsonView({View.Public.GET.class})
    @Schema(description = "Creation date")
    private String dateCreated;

    @JsonView({View.Public.GET.class})
    @Schema(description = "User who last modified this object")
    private String usrModif;

    @JsonView({View.Public.GET.class})
    @Schema(description = "Modification date")
    private String dateModif;
}
