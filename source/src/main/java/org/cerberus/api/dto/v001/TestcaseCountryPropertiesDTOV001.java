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
import org.cerberus.crud.entity.Invariant;

import java.util.List;

/**
 * @author MorganLmd
 */
@Data
@Builder
@Jacksonized
@ApiModel(value = "TestcaseCountryProperties")
public class TestcaseCountryPropertiesDTOV001 {

    @ApiModelProperty(position = 0)
    private String testFolderId;

    @ApiModelProperty(position = 1)
    private String testcaseId;

    @ApiModelProperty(position = 2)
    private String property;

    @ApiModelProperty(position = 3)
    private String description;

    @ApiModelProperty(position = 4)
    private String type;

    @ApiModelProperty(position = 5)
    private String database;

    @ApiModelProperty(position = 6)
    private String value1;

    @ApiModelProperty(position = 7)
    private String value2;

    @ApiModelProperty(position = 8)
    private String length;

    @ApiModelProperty(position = 9)
    private int rowLimit;

    @ApiModelProperty(position = 10)
    private String nature;

    @ApiModelProperty(position = 11)
    private int rank;

    @ApiModelProperty(position = 12)
    private String usrCreated;

    @ApiModelProperty(position = 13)
    private String dateCreated;

    @ApiModelProperty(position = 14)
    private String usrModif;

    @ApiModelProperty(position = 15)
    private String dateModif;

    @ApiModelProperty(position = 16)
    private List<Invariant> countries;
}
