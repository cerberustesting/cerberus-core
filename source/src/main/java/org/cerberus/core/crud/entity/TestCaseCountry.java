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

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.*;

import java.sql.Timestamp;
import java.util.List;

/**
 * @author bcivel
 */
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
@EqualsAndHashCode
@ToString
public class TestCaseCountry {

    private String test;
    private String testcase;
    private String country;
    @EqualsAndHashCode.Exclude
    private String usrCreated;
    @EqualsAndHashCode.Exclude
    private Timestamp dateCreated;
    @EqualsAndHashCode.Exclude
    private String usrModif;
    @EqualsAndHashCode.Exclude
    private Timestamp dateModif;

    /**
     * From here are data outside database model.
     */
    @EqualsAndHashCode.Exclude
    private List<TestCaseCountryProperties> testCaseCountryProperty;
    @EqualsAndHashCode.Exclude
    private TestCase testCaseObj;


    @JsonIgnore
    public TestCase getTestCaseObj() {
        return testCaseObj;
    }

    public boolean hasSameKey(TestCaseCountry obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final TestCaseCountry other = obj;
        if ((this.test == null) ? (other.test != null) : !this.test.equals(other.test)) {
            return false;
        }
        if ((this.testcase == null) ? (other.testcase != null) : !this.testcase.equals(other.testcase)) {
            return false;
        }
        return !((this.country == null) ? (other.country != null) : !this.country.equals(other.country));
    }
}
