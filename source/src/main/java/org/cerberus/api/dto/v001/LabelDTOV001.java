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
@ApiModel(value = "Label")
public class LabelDTOV001 {

    @ApiModelProperty(position = 0)
    private Integer id;

    @ApiModelProperty(position = 1)
    private String system;

    @ApiModelProperty(position = 2)
    private String label;

    @ApiModelProperty(position = 3)
    private String type;

    @ApiModelProperty(position = 4)
    private String color;

    @ApiModelProperty(position = 5)
    private Integer parentLabelID;

    @ApiModelProperty(position = 6)
    private String requirementType;

    @ApiModelProperty(position = 7)
    private String requirementStatus;

    @ApiModelProperty(position = 8)
    private String requirementCriticality;

    @ApiModelProperty(position = 9)
    private String description;

    @ApiModelProperty(position = 10)
    private String longDescription;

    @ApiModelProperty(position = 11)
    private String usrCreated;

    @ApiModelProperty(position = 12)
    private String dateCreated;

    @ApiModelProperty(position = 13)
    private String usrModif;

    @ApiModelProperty(position = 14)
    private String dateModif;


}
