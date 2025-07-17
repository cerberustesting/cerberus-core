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
@JsonPropertyOrder({
        "service",
        "application",
        "type",
        "method",
        "servicePath",
        "isFollowingRedirection",
        "fileName",
        "operation",
        "attachementURL",
        "serviceRequest",
        "kafkaTopic",
        "kafkaKey",
        "kafkaFilterPath",
        "kafkaFilterValue",
        "group",
        "description",
        "headers",
        "contents",
        "usrCreated",
        "dateCreated",
        "usrModif",
        "dateModif"
})
@Schema(name = "Service")
public class AppServiceDTOV001 {

    @NotEmpty
    @JsonView({View.Public.GET.class, View.Public.POST.class, View.Public.PUT.class})
    @Schema(description = "Service identifier", required = true)
    private String service;

    @JsonView({View.Public.GET.class, View.Public.PUT.class, View.Public.POST.class})
    @Schema(description = "Application name")
    private String application;

    @NotEmpty
    @JsonView({View.Public.GET.class, View.Public.PUT.class, View.Public.POST.class})
    @Schema(description = "Type of service", required = true)
    private String type;

    @NotEmpty
    @JsonView({View.Public.GET.class, View.Public.PUT.class, View.Public.POST.class})
    @Schema(description = "Method name", required = true)
    private String method;

    @JsonView({View.Public.GET.class, View.Public.PUT.class, View.Public.POST.class})
    @Schema(description = "Service path")
    private String servicePath;

    @JsonView({View.Public.GET.class, View.Public.PUT.class, View.Public.POST.class})
    @JsonProperty("isFollowingRedirection")
    @Schema(description = "Whether the service follows redirection")
    private boolean isFollowingRedirection;

    @JsonView({View.Public.GET.class, View.Public.PUT.class, View.Public.POST.class})
    @Schema(description = "File name")
    private String fileName;

    @JsonView({View.Public.GET.class, View.Public.PUT.class, View.Public.POST.class})
    @Schema(description = "Operation name")
    private String operation;

    @JsonView({View.Public.GET.class, View.Public.PUT.class, View.Public.POST.class})
    @Schema(description = "Attachment URL")
    private String attachementURL;

    @JsonView({View.Public.GET.class, View.Public.PUT.class, View.Public.POST.class})
    @Schema(description = "Service request content")
    private String serviceRequest;

    @JsonView({View.Public.GET.class, View.Public.PUT.class, View.Public.POST.class})
    @Schema(description = "Kafka topic")
    private String kafkaTopic;

    @JsonView({View.Public.GET.class, View.Public.PUT.class, View.Public.POST.class})
    @Schema(description = "Kafka key")
    private String kafkaKey;

    @JsonView({View.Public.GET.class, View.Public.PUT.class, View.Public.POST.class})
    @Schema(description = "Kafka filter path")
    private String kafkaFilterPath;

    @JsonView({View.Public.GET.class, View.Public.PUT.class, View.Public.POST.class})
    @Schema(description = "Kafka filter value")
    private String kafkaFilterValue;

    @JsonView({View.Public.GET.class, View.Public.PUT.class, View.Public.POST.class})
    @Schema(description = "Group name")
    private String group;

    @JsonView({View.Public.GET.class, View.Public.PUT.class, View.Public.POST.class})
    @Schema(description = "Description")
    private String description;

    @JsonView({View.Public.GET.class, View.Public.PUT.class, View.Public.POST.class})
    @Schema(description = "List of service headers")
    private List<AppServiceHeaderDTOV001> headers;

    @JsonView({View.Public.GET.class, View.Public.PUT.class, View.Public.POST.class})
    @Schema(description = "List of service contents")
    private List<AppServiceContentDTOV001> contents;

    @JsonView({View.Public.GET.class, View.Public.PUT.class, View.Public.POST.class})
    @Schema(description = "User who created the record")
    private String usrCreated;

    @JsonView({View.Public.GET.class})
    @Schema(description = "Date of creation")
    private String dateCreated;

    @JsonView({View.Public.GET.class, View.Public.PUT.class, View.Public.POST.class})
    @Schema(description = "User who last modified the record")
    private String usrModif;

    @JsonView({View.Public.GET.class})
    @Schema(description = "Date of last modification")
    private String dateModif;
}