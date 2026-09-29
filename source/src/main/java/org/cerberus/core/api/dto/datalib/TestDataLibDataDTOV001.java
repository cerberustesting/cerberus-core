/*
 * Cerberus Copyright (C) 2013 - 2026 cerberustesting
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
package org.cerberus.core.api.dto.datalib;

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
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({"id", "subData", "encrypt", "value", "column", "parsingAnswer", "columnPosition", "description"})
@Schema(name = "TestDataLibData")
public class TestDataLibDataDTOV001 {

    @JsonView({View.Public.GET.class})
    @Schema(description = "Technical identifier of the sub-data")
    private Integer id;

    @JsonView({View.Public.GET.class, View.Public.PUT.class, View.Public.POST.class, View.Public.PATCH.class})
    @Schema(description = "Sub-data name (an empty string designates the key entry)")
    private String subData;

    @JsonView({View.Public.GET.class, View.Public.PUT.class, View.Public.POST.class, View.Public.PATCH.class})
    @Schema(description = "Y if the value is encrypted, N otherwise")
    private String encrypt;

    @JsonView({View.Public.GET.class, View.Public.PUT.class, View.Public.POST.class, View.Public.PATCH.class})
    @Schema(description = "Literal value (INTERNAL type)")
    private String value;

    @JsonView({View.Public.GET.class, View.Public.PUT.class, View.Public.POST.class, View.Public.PATCH.class})
    @Schema(description = "Column name (SQL type)")
    private String column;

    @JsonView({View.Public.GET.class, View.Public.PUT.class, View.Public.POST.class, View.Public.PATCH.class})
    @Schema(description = "Parsing expression (SERVICE type with XML / JSON answer)")
    private String parsingAnswer;

    @JsonView({View.Public.GET.class, View.Public.PUT.class, View.Public.POST.class, View.Public.PATCH.class})
    @Schema(description = "Column position (FILE type)")
    private String columnPosition;

    @JsonView({View.Public.GET.class, View.Public.PUT.class, View.Public.POST.class, View.Public.PATCH.class})
    @Schema(description = "Description")
    private String description;
}
