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
package org.cerberus.core.api.dto.queueexecution;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonView;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.util.List;
import lombok.Builder;
import lombok.Data;
import lombok.extern.jackson.Jacksonized;
import org.cerberus.core.api.dto.views.View;

/**
 * @author lucashimpens
 */
@Data
@Builder
@Jacksonized
@JsonInclude(JsonInclude.Include.NON_NULL)
@ApiModel(value = "QueuedExecution")
public class QueuedExecutionDTOV001 {

    @ApiModelProperty(position = 1)
    @JsonView(View.Public.POST.class)
    private List<QueuedExecutionTestcaseDTOV001> testcases;

    @JsonView(View.Public.POST.class)
    @ApiModelProperty(position = 2)
    private List<String> countries;

    @JsonView(View.Public.POST.class)
    @ApiModelProperty(position = 3)
    private List<String> environments;

    @JsonView(View.Public.POST.class)
    @ApiModelProperty(position = 4)
    private List<String> robots;

    @JsonView(View.Public.POST.class)
    @ApiModelProperty(position = 5)
    private String tag;

    @JsonView(View.Public.POST.class)
    @ApiModelProperty(position = 6)
    private Integer screenshot;

    @JsonView(View.Public.POST.class)
    @ApiModelProperty(position = 7)
    private Integer video;

    @JsonView(View.Public.POST.class)
    @ApiModelProperty(position = 8)
    private Integer verbose;

    @JsonView(View.Public.POST.class)
    @ApiModelProperty(position = 9)
    private Integer timeout;

    @JsonView(View.Public.POST.class)
    @ApiModelProperty(position = 10)
    private Integer pageSource;

    @JsonView(View.Public.POST.class)
    @ApiModelProperty(position = 11)
    private Integer robotLog;

    @JsonView(View.Public.POST.class)
    @ApiModelProperty(position = 12)
    private Integer consoleLog;

    @JsonView(View.Public.POST.class)
    @ApiModelProperty(position = 13)
    private String manualExecution;

    @JsonView(View.Public.POST.class)
    @ApiModelProperty(position = 14)
    private Integer retries;

    @JsonView(View.Public.POST.class)
    @ApiModelProperty(position = 15)
    private Integer priority;

    @JsonView(View.Public.POST.class)
    @ApiModelProperty(position = 16)
    private Integer manualUrl;

    @JsonView(View.Public.POST.class)
    @ApiModelProperty(position = 17)
    private ManualUrlParametersDTOV001 manualUrlParameters;
}
