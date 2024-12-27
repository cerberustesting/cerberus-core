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
public class CountryEnvironmentDatabase {

    private String system;
    private String country;
    private String environment;
    private String database;
    private String connectionPoolName;
    private String soapUrl;
    private String csvUrl;

    public String getCsvUrl() {
        return csvUrl;
    }

    public void setCsvUrl(String csvUrl) {
        this.csvUrl = csvUrl;
    }

    public String getConnectionPoolName() {
        return connectionPoolName;
    }

    public void setConnectionPoolName(String connectionPoolName) {
        this.connectionPoolName = connectionPoolName;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getDatabase() {
        return database;
    }

    public void setDatabase(String database) {
        this.database = database;
    }

    public String getEnvironment() {
        return environment;
    }

    public void setEnvironment(String environment) {
        this.environment = environment;
    }

    public String getSystem() {
        return system;
    }

    public void setSystem(String system) {
        this.system = system;
    }

    public String getSoapUrl() {
        return soapUrl;
    }

    public void setSoapUrl(String soapUrl) {
        this.soapUrl = soapUrl;
    }

    public boolean hasSameKey(CountryEnvironmentDatabase obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final CountryEnvironmentDatabase other = obj;
        if ((this.system == null) ? (other.system != null) : !this.system.equals(other.system)) {
            return false;
        }
        if ((this.country == null) ? (other.country != null) : !this.country.equals(other.country)) {
            return false;
        }
        if ((this.environment == null) ? (other.environment != null) : !this.environment.equals(other.environment)) {
            return false;
        }
        if ((this.database == null) ? (other.database != null) : !this.database.equals(other.database)) {
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
        hash = 29 * hash + (this.database != null ? this.database.hashCode() : 0);
        hash = 29 * hash + (this.connectionPoolName != null ? this.connectionPoolName.hashCode() : 0);
        hash = 29 * hash + (this.soapUrl != null ? this.soapUrl.hashCode() : 0);
        hash = 29 * hash + (this.csvUrl != null ? this.csvUrl.hashCode() : 0);
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
        final CountryEnvironmentDatabase other = (CountryEnvironmentDatabase) obj;
        if ((this.system == null) ? (other.system != null) : !this.system.equals(other.system)) {
            return false;
        }
        if ((this.country == null) ? (other.country != null) : !this.country.equals(other.country)) {
            return false;
        }
        if ((this.environment == null) ? (other.environment != null) : !this.environment.equals(other.environment)) {
            return false;
        }
        if ((this.database == null) ? (other.database != null) : !this.database.equals(other.database)) {
            return false;
        }
        if ((this.connectionPoolName == null) ? (other.connectionPoolName != null) : !this.connectionPoolName.equals(other.connectionPoolName)) {
            return false;
        }
        if ((this.soapUrl == null) ? (other.soapUrl != null) : !this.soapUrl.equals(other.soapUrl)) {
            return false;
        }
        if ((this.csvUrl == null) ? (other.csvUrl != null) : !this.csvUrl.equals(other.csvUrl)) {
            return false;
        }
        return true;
    }

}
