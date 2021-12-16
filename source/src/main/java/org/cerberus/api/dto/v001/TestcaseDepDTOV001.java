/**
 * Cerberus Copyright (C) 2013 - 2017 cerberustesting
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

package org.cerberus.api.dto.v001;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Builder;
import lombok.Data;
import lombok.extern.jackson.Jacksonized;

/**
 * @author MorganLmd
 */
@Data
@Builder
@Jacksonized
@ApiModel(value = "TestcaseDependency")
public class TestcaseDepDTOV001 {

    @ApiModelProperty(position = 0)
    private long id;

    @ApiModelProperty(position = 1)
    private String dependencyTestFolderId;

    @ApiModelProperty(position = 2)
    private String dependencyTestcaseId;

    @ApiModelProperty(position = 3)
    private String dependencyEvent;

    @ApiModelProperty(position = 4)
    private String type;

    @ApiModelProperty(position = 5)
    private boolean isActive;

    @ApiModelProperty(position = 6)
    private String description;
}
