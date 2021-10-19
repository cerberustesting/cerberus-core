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

/**
 *
 * @author mlombard
 */
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

    public InvariantDTOV001() {
    }

    public InvariantDTOV001(String idName, String value, Integer sort, String description,
            String veryShortDesc, String gp1, String gp2,
            String gp3, String gp4, String gp5, String gp6,
            String gp7, String gp8, String gp9) {
        this.idName = idName;
        this.value = value;
        this.sort = sort;
        this.description = description;
        this.shortDescription = veryShortDesc;
        this.attribute1 = gp1;
        this.attribute2 = gp2;
        this.attribute3 = gp3;
        this.attribute4 = gp4;
        this.attribute5 = gp5;
        this.attribute6 = gp6;
        this.attribute7 = gp7;
        this.attribute8 = gp8;
        this.attribute9 = gp9;
    }

    public String getIdName() {
        return idName;
    }

    public void setIdName(String idName) {
        this.idName = idName;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public Integer getSort() {
        return sort;
    }

    public void setSort(Integer sort) {
        this.sort = sort;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getShortDescription() {
        return shortDescription;
    }

    public void setShortDescription(String shortDescription) {
        this.shortDescription = shortDescription;
    }

    public String getAttribute1() {
        return attribute1;
    }

    public void setAttribute1(String attribute1) {
        this.attribute1 = attribute1;
    }

    public String getAttribute2() {
        return attribute2;
    }

    public void setAttribute2(String attribute2) {
        this.attribute2 = attribute2;
    }

    public String getAttribute3() {
        return attribute3;
    }

    public void setAttribute3(String attribute3) {
        this.attribute3 = attribute3;
    }

    public String getAttribute4() {
        return attribute4;
    }

    public void setAttribute4(String attribute4) {
        this.attribute4 = attribute4;
    }

    public String getAttribute5() {
        return attribute5;
    }

    public void setAttribute5(String attribute5) {
        this.attribute5 = attribute5;
    }

    public String getAttribute6() {
        return attribute6;
    }

    public void setAttribute6(String attribute6) {
        this.attribute6 = attribute6;
    }

    public String getAttribute7() {
        return attribute7;
    }

    public void setAttribute7(String attribute7) {
        this.attribute7 = attribute7;
    }

    public String getAttribute8() {
        return attribute8;
    }

    public void setAttribute8(String attribute8) {
        this.attribute8 = attribute8;
    }

    public String getAttribute9() {
        return attribute9;
    }

    public void setAttribute9(String attribute9) {
        this.attribute9 = attribute9;
    }

}
