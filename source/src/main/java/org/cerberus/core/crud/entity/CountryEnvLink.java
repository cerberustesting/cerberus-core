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
 *
 * @author bcivel
 */
public class CountryEnvLink {

    private String system;
    private String country;
    private String environment;
    private String systemLink;
    private String countryLink;
    private String environmentLink;

    public String getSystem() {
        return system;
    }

    public void setSystem(String system) {
        this.system = system;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getEnvironment() {
        return environment;
    }

    public void setEnvironment(String environment) {
        this.environment = environment;
    }

    public String getSystemLink() {
        return systemLink;
    }

    public void setSystemLink(String systemLink) {
        this.systemLink = systemLink;
    }

    public String getCountryLink() {
        return countryLink;
    }

    public void setCountryLink(String countryLink) {
        this.countryLink = countryLink;
    }

    public String getEnvironmentLink() {
        return environmentLink;
    }

    public void setEnvironmentLink(String environmentLink) {
        this.environmentLink = environmentLink;
    }

    public boolean hasSameKey(CountryEnvLink obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final CountryEnvLink other = obj;
        if ((this.system == null) ? (other.system != null) : !this.system.equals(other.system)) {
            return false;
        }
        if ((this.country == null) ? (other.country != null) : !this.country.equals(other.country)) {
            return false;
        }
        if ((this.environment == null) ? (other.environment != null) : !this.environment.equals(other.environment)) {
            return false;
        }
        if ((this.systemLink == null) ? (other.systemLink != null) : !this.systemLink.equals(other.systemLink)) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 29 * hash + (this.system != null ? this.system.hashCode() : 0);
        hash = 29 * hash + (this.country != null ? this.country.hashCode() : 0);
        hash = 29 * hash + (this.environment != null ? this.environment.hashCode() : 0);
        hash = 29 * hash + (this.systemLink != null ? this.systemLink.hashCode() : 0);
        hash = 29 * hash + (this.countryLink != null ? this.countryLink.hashCode() : 0);
        hash = 29 * hash + (this.environmentLink != null ? this.environmentLink.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final CountryEnvLink other = (CountryEnvLink) obj;
        if ((this.system == null) ? (other.system != null) : !this.system.equals(other.system)) {
            return false;
        }
        if ((this.country == null) ? (other.country != null) : !this.country.equals(other.country)) {
            return false;
        }
        if ((this.environment == null) ? (other.environment != null) : !this.environment.equals(other.environment)) {
            return false;
        }
        if ((this.systemLink == null) ? (other.systemLink != null) : !this.systemLink.equals(other.systemLink)) {
            return false;
        }
        if ((this.countryLink == null) ? (other.countryLink != null) : !this.countryLink.equals(other.countryLink)) {
            return false;
        }
        if ((this.environmentLink == null) ? (other.environmentLink != null) : !this.environmentLink.equals(other.environmentLink)) {
            return false;
        }
        return true;
    }

}
