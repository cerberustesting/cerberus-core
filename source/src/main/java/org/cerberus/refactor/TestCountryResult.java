/*
 * Cerberus  Copyright (C) 2013  vertigo17
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
package org.cerberus.refactor;

public class TestCountryResult {

    private Country country;
    private String date;
    private Integer id;
    private Boolean oK;
    private Test test;

    public Country getCountry() {

        return this.country;
    }

    public String getDate() {

        return this.date;
    }

    public Integer getId() {

        return this.id;
    }

    public Boolean getoK() {

        return this.oK;
    }

    public Test getTest() {

        return this.test;
    }

    public void setCountry(Country country) {

        this.country = country;
    }

    public void setDate(String date) {

        this.date = date;
    }

    public void setId(Integer id) {

        this.id = id;
    }

    public void setoK(Boolean oK) {

        this.oK = oK;
    }

    public void setoK(String oK) {

        if (oK.compareTo("OK") == 0) {
            this.oK = true;
            // System.out.println ( oK + " True " ) ;
        } else {
            // System.out.println ( oK + " False " ) ;
            this.oK = false;
        }
    }

    public void setTest(Test test) {

        this.test = test;
    }

    public void updateCountryStatistics() {

        if (this.country != null && this.test != null) {
            // System.out.println("Adding available for : "
            // + this.country.getName() + " in test -> "
            // + this.getTest().getTest());
            this.country.addAvailableTest();
        }

    }

    public void updateExecutionStatistics() {

        if (this.country != null && this.test != null) {
            this.country.addExecutedTest();

            if (this.oK) {
                this.country.addOKTest();
            } else {
                this.country.addKOTest();
            }
        }
    }
}
