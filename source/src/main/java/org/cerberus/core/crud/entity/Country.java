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

/**
 * {Insert class description here}
 *
 * @author Tiago Bernardes
 * @version 1.0, 18/Dez/2012
 * @since 2.0.0
 */
public class Country {
    /**
     * Name of Country (ex. Portugal, Belgium, etc.).
     */
    private String country;
    /**
     * Code of Country (ex. PT, BE, etc.).
     */
    private String countryCode;

    public Country(String tempCountryCode) {
        this.countryCode = tempCountryCode;
    }

    public Country(String tempCountry, String tempCountryCode) {
        this.country = tempCountry;
        this.countryCode = tempCountryCode;
    }

    public String getCountry() {
        return this.country;
    }

    public void setCountry(String tempCountry) {
        this.country = tempCountry;
    }

    public String getCountryCode() {
        return this.countryCode;
    }

    public void setCountryCode(String tempCountryCode) {
        this.countryCode = tempCountryCode;
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }
        if (object == null || getClass() != object.getClass()) {
            return false;
        }

        Country country1 = (Country) object;

        if (!this.country.equals(country1.country)) {
            return false;
        }
        if (!this.countryCode.equals(country1.countryCode)) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int result = this.country.hashCode();
        result = 31 * result + this.countryCode.hashCode();
        return result;
    }
}
