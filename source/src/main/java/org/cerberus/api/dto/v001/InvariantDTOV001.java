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

/**
 *
 * @author mlombard
 */
@Data
@Builder
@ApiModel(value = "Invariant") 
public class InvariantDTOV001 {

    @ApiModelProperty(example = "ACTION", position = 0)
    private String idName;
    
    @ApiModelProperty(example = "click", position = 1)
    private String value;
    
    @ApiModelProperty(example = "3000", position = 2)
    private Integer sort;
    
    @ApiModelProperty(example = "click", position = 3)
    private String description;
    
    @ApiModelProperty(example = "null", position = 4)
    private String shortDescription;
    
    @ApiModelProperty(example = "null", position = 5)
    private String attribute1;
    
    @ApiModelProperty(example = "null", position = 6)
    private String attribute2;
    
    @ApiModelProperty(example = "null", position = 7)
    private String attribute3;
    
    @ApiModelProperty(example = "null", position = 8)
    private String attribute4;
    
    @ApiModelProperty(example = "null", position = 9)
    private String attribute5;
    
    @ApiModelProperty(example = "null", position = 10)
    private String attribute6;
    
    @ApiModelProperty(example = "null", position = 11)
    private String attribute7;
    
    @ApiModelProperty(example = "null", position = 12)
    private String attribute8;
    
    @ApiModelProperty(example = "null", position = 13)
    private String attribute9;
}
