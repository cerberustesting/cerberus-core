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
package org.cerberus.core.crud.entity;

import lombok.*;

import java.sql.Timestamp;

/**
 * @author bcivel
 */
@Getter
@Setter
@EqualsAndHashCode
@ToString
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TestCaseLabel {

    private Integer id;
    private String test;
    private String testcase;
    private Integer labelId;
    @EqualsAndHashCode.Exclude
    private String usrCreated;
    @EqualsAndHashCode.Exclude
    private Timestamp dateCreated;
    @EqualsAndHashCode.Exclude
    private String usrModif;
    @EqualsAndHashCode.Exclude
    private Timestamp dateModif;

    /**
     * Not included in table.
     */
    @EqualsAndHashCode.Exclude
    private Label label;


    public boolean hasSameKey(TestCaseLabel obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final TestCaseLabel other = obj;
        if ((this.test == null) ? (other.test != null) : !this.test.equals(other.test)) {
            return false;
        }
        if ((this.testcase == null) ? (other.testcase != null) : !this.testcase.equals(other.testcase)) {
            return false;
        }
        if (this.labelId != other.labelId) {
            return false;
        }
        return true;
    }
}
