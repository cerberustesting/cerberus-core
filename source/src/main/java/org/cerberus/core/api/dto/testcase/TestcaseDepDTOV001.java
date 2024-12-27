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

package org.cerberus.core.api.dto.testcase;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonView;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.sql.Timestamp;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;
import lombok.extern.jackson.Jacksonized;
import org.cerberus.core.api.dto.views.View;

/**
 * @author MorganLmd
 */
@Data
@Builder
@Jacksonized
@ApiModel(value = "TestcaseDependency")
public class TestcaseDepDTOV001 {

    @JsonView(value = {View.Public.GET.class, View.Public.PUT.class})
    @ApiModelProperty(position = 0)
    private long id;

    @NotBlank(message = "The test folder id this testcase is dependent of is mandatory")
    @JsonView(value = {View.Public.GET.class, View.Public.PUT.class, View.Public.POST.class})
    @ApiModelProperty(position = 1)
    private String dependencyTestFolderId;

    @NotBlank(message = "The testcase id this testcase is dependent of is mandatory")
    @JsonView(value = {View.Public.GET.class, View.Public.PUT.class, View.Public.POST.class})
    @ApiModelProperty(position = 2)
    private String dependencyTestcaseId;

    @JsonView(value = {View.Public.GET.class, View.Public.PUT.class, View.Public.POST.class})
    @ApiModelProperty(position = 3)
    private String dependencyEvent;

    @JsonView(value = {View.Public.GET.class, View.Public.PUT.class, View.Public.POST.class})
    @ApiModelProperty(position = 3)
    private Integer dependencyTCDelay;

    @NotNull(message = "Type is mandatory")
    @JsonView(value = {View.Public.GET.class, View.Public.PUT.class, View.Public.POST.class})
    @ApiModelProperty(position = 4)
    private String type;

    @JsonView(value = {View.Public.GET.class, View.Public.PUT.class, View.Public.POST.class})
    @ApiModelProperty(position = 5)
    @JsonProperty("isActive")
    @Builder.Default
    private boolean isActive = true;

    @JsonView(value = {View.Public.GET.class, View.Public.PUT.class, View.Public.POST.class})
    @ApiModelProperty(position = 6)
    private String description;
    
    @ApiModelProperty(position = 7)
    @JsonView(value = {View.Public.GET.class})
    private String usrCreated;

    @ApiModelProperty(position = 8)
    @JsonView(value = {View.Public.GET.class})
    private Timestamp dateCreated;

    @ApiModelProperty(position = 9)
    @JsonView(value = {View.Public.GET.class})
    private String usrModif;

    @ApiModelProperty(position = 10)
    @JsonView(value = {View.Public.GET.class})
    private Timestamp dateModif;
    
}
