/* DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
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
package org.cerberus.entity;

/**
 * @author bcivel
 */
public class Invariant {

    private String idName;
    private String value;
    private Integer sort;
    private String description;
    private String veryShortDesc;
    private String gp1;
    private String gp2;
    private String gp3;

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getVeryShortDesc() {
        return veryShortDesc;
    }

    public void setVeryShortDesc(String veryShortDesc) {
        this.veryShortDesc = veryShortDesc;
    }

    public String getGp1() {
        return gp1;
    }

    public void setGp1(String gp1) {
        this.gp1 = gp1;
    }

    public String getGp2() {
        return gp2;
    }

    public void setGp2(String gp2) {
        this.gp2 = gp2;
    }

    public String getGp3() {
        return gp3;
    }

    public void setGp3(String gp3) {
        this.gp3 = gp3;
    }

    public String getIdName() {
        return idName;
    }

    public void setIdName(String idName) {
        this.idName = idName;
    }

    public Integer getSort() {
        return sort;
    }

    public void setSort(Integer sort) {
        this.sort = sort;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
}
