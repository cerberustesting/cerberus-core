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
package org.cerberus.core.api.dto.test;

import com.fasterxml.jackson.annotation.*;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;
import lombok.extern.jackson.Jacksonized;
import org.cerberus.core.api.dto.views.View;

import java.sql.Timestamp;

/**
 * @author bcivel
 */
@Data
@Builder
@Jacksonized
@JsonInclude(JsonInclude.Include.ALWAYS)
@JsonIgnoreProperties(ignoreUnknown = true)
@Schema(name = "Test")
public class TestDTOV001 {

    @JsonView({View.Public.GET.class, View.Public.POST.class})
    private String test;

    @JsonView({View.Public.GET.class, View.Public.POST.class})
    private String description;

    @JsonView({View.Public.GET.class, View.Public.POST.class})
    @JsonProperty("isActive")
    private boolean isActive;

    @JsonView({View.Public.GET.class, View.Public.POST.class})
    private String parentTest;

    @JsonView(View.Public.GET.class)
    private String usrCreated;

    @JsonView(View.Public.GET.class)
    private Timestamp dateCreated;

    @JsonView(View.Public.GET.class)
    private String usrModif;

    @JsonView(View.Public.GET.class)
    private Timestamp dateModif;
}