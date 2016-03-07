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
package org.cerberus.crud.entity;

/**
 *
 * @author bcivel
 */
public class CountryEnvironmentParameters {

    private String system;
    private String country;
    private String environment;
    private String application;
    private String ip;
    private String domain;
    private String url;
    private String urlLogin;

    public String getApplication() {
        return application;
    }

    public void setApplication(String application) {
        this.application = application;
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

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public String getDomain() {
        return domain;
    }

    public void setDomain(String domain) {
        this.domain = domain;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getUrlLogin() {
        return urlLogin;
    }

    public void setUrlLogin(String urlLogin) {
        this.urlLogin = urlLogin;
    }

    public String getSystem() {
        return system;
    }

    public void setSystem(String system) {
        this.system = system;
    }

    public boolean hasSameKey(CountryEnvironmentParameters obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final CountryEnvironmentParameters other = (CountryEnvironmentParameters) obj;
        if ((this.system == null) ? (other.system != null) : !this.system.equals(other.system)) {
            return false;
        }
        if ((this.country == null) ? (other.country != null) : !this.country.equals(other.country)) {
            return false;
        }
        if ((this.environment == null) ? (other.environment != null) : !this.environment.equals(other.environment)) {
            return false;
        }
        if ((this.application == null) ? (other.application != null) : !this.application.equals(other.application)) {
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
        hash = 29 * hash + (this.application != null ? this.application.hashCode() : 0);
        hash = 29 * hash + (this.ip != null ? this.ip.hashCode() : 0);
        hash = 29 * hash + (this.url != null ? this.url.hashCode() : 0);
        hash = 29 * hash + (this.urlLogin != null ? this.urlLogin.hashCode() : 0);
        hash = 29 * hash + (this.domain != null ? this.domain.hashCode() : 0);
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
        final CountryEnvironmentParameters other = (CountryEnvironmentParameters) obj;
        if ((this.system == null) ? (other.system != null) : !this.system.equals(other.system)) {
            return false;
        }
        if ((this.country == null) ? (other.country != null) : !this.country.equals(other.country)) {
            return false;
        }
        if ((this.environment == null) ? (other.environment != null) : !this.environment.equals(other.environment)) {
            return false;
        }
        if ((this.application == null) ? (other.application != null) : !this.application.equals(other.application)) {
            return false;
        }
        if ((this.ip == null) ? (other.ip != null) : !this.ip.equals(other.ip)) {
            return false;
        }
        if ((this.url == null) ? (other.url != null) : !this.url.equals(other.url)) {
            return false;
        }
        if ((this.urlLogin == null) ? (other.urlLogin != null) : !this.urlLogin.equals(other.urlLogin)) {
            return false;
        }
        if ((this.domain == null) ? (other.domain != null) : !this.domain.equals(other.domain)) {
            return false;
        }
        return true;
    }

}
