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
import jakarta.validation.Valid;
import java.util.List;
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
@JsonPropertyOrder({"id", "name", "system", "environment", "country", "type", "group", "privateData", "description",
        "database", "script", "databaseUrl", "service", "servicePath", "method", "envelope", "databaseCsv", "csvUrl",
        "separator", "ignoreFirstLine", "creator", "created", "lastModifier", "lastModified", "subData"})
@Schema(name = "TestDataLib")
public class TestDataLibDTOV001 {

    @JsonView({View.Public.GET.class})
    @Schema(description = "Technical identifier of the data library")
    private Integer id;

    @JsonView({View.Public.GET.class, View.Public.PUT.class, View.Public.POST.class, View.Public.PATCH.class})
    @Schema(description = "Name of the data library")
    private String name;

    @JsonView({View.Public.GET.class, View.Public.PUT.class, View.Public.POST.class, View.Public.PATCH.class})
    @Schema(description = "System (empty = all systems)")
    private String system;

    @JsonView({View.Public.GET.class, View.Public.PUT.class, View.Public.POST.class, View.Public.PATCH.class})
    @Schema(description = "Environment (empty = all environments)")
    private String environment;

    @JsonView({View.Public.GET.class, View.Public.PUT.class, View.Public.POST.class, View.Public.PATCH.class})
    @Schema(description = "Country (empty = all countries)")
    private String country;

    @JsonView({View.Public.GET.class, View.Public.PUT.class, View.Public.POST.class, View.Public.PATCH.class})
    @Schema(description = "Type of library: INTERNAL, SQL, SERVICE or FILE")
    private String type;

    @JsonView({View.Public.GET.class, View.Public.PUT.class, View.Public.POST.class, View.Public.PATCH.class})
    @Schema(description = "Group")
    private String group;

    @JsonView({View.Public.GET.class, View.Public.PUT.class, View.Public.POST.class, View.Public.PATCH.class})
    @Schema(description = "Y if the data is private, N otherwise")
    private String privateData;

    @JsonView({View.Public.GET.class, View.Public.PUT.class, View.Public.POST.class, View.Public.PATCH.class})
    @Schema(description = "Description")
    private String description;

    @JsonView({View.Public.GET.class, View.Public.PUT.class, View.Public.POST.class, View.Public.PATCH.class})
    @Schema(description = "Database name (SQL type)")
    private String database;

    @JsonView({View.Public.GET.class, View.Public.PUT.class, View.Public.POST.class, View.Public.PATCH.class})
    @Schema(description = "SQL script (SQL type)")
    private String script;

    @JsonView({View.Public.GET.class, View.Public.PUT.class, View.Public.POST.class, View.Public.PATCH.class})
    @Schema(description = "Database url (SQL type)")
    private String databaseUrl;

    @JsonView({View.Public.GET.class, View.Public.PUT.class, View.Public.POST.class, View.Public.PATCH.class})
    @Schema(description = "Application service name (SERVICE type)")
    private String service;

    @JsonView({View.Public.GET.class, View.Public.PUT.class, View.Public.POST.class, View.Public.PATCH.class})
    @Schema(description = "Service path (SERVICE type)")
    private String servicePath;

    @JsonView({View.Public.GET.class, View.Public.PUT.class, View.Public.POST.class, View.Public.PATCH.class})
    @Schema(description = "Service method (SERVICE type)")
    private String method;

    @JsonView({View.Public.GET.class, View.Public.PUT.class, View.Public.POST.class, View.Public.PATCH.class})
    @Schema(description = "Service envelope (SERVICE type)")
    private String envelope;

    @JsonView({View.Public.GET.class, View.Public.PUT.class, View.Public.POST.class, View.Public.PATCH.class})
    @Schema(description = "Database used to read the CSV (FILE type)")
    private String databaseCsv;

    @JsonView({View.Public.GET.class, View.Public.PUT.class, View.Public.POST.class, View.Public.PATCH.class})
    @Schema(description = "CSV url (FILE type)")
    private String csvUrl;

    @JsonView({View.Public.GET.class, View.Public.PUT.class, View.Public.POST.class, View.Public.PATCH.class})
    @Schema(description = "CSV separator (FILE type)")
    private String separator;

    @JsonView({View.Public.GET.class, View.Public.PUT.class, View.Public.POST.class, View.Public.PATCH.class})
    @Schema(description = "Ignore the first line of the CSV (FILE type)")
    private Boolean ignoreFirstLine;

    @JsonView({View.Public.GET.class})
    @Schema(description = "User who created the library")
    private String creator;

    @JsonView({View.Public.GET.class})
    @Schema(description = "Creation date")
    private String created;

    @JsonView({View.Public.GET.class})
    @Schema(description = "User who last modified the library")
    private String lastModifier;

    @JsonView({View.Public.GET.class})
    @Schema(description = "Modification date")
    private String lastModified;

    @Valid
    @JsonView({View.Public.GET.class, View.Public.PUT.class, View.Public.POST.class, View.Public.PATCH.class})
    @Schema(description = "Sub-data of the library. On PUT the list replaces the existing one; on PATCH, when provided, entries are merged by subData name and the others are kept.")
    private List<TestDataLibDataDTOV001> subData;
}
