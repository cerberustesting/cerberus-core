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
 * @author bcivel
 */
public class CountryEnvironmentParameters {

    public static class Key {

        public static Key fromCountryEnvironmentParameters(CountryEnvironmentParameters countryEnvironmentParameters) {
            return new Key(
                    countryEnvironmentParameters.getSystem(),
                    countryEnvironmentParameters.getApplication(),
                    countryEnvironmentParameters.getCountry(),
                    countryEnvironmentParameters.getEnvironment()
            );
        }

        private String system;

        private String application;

        private String country;

        private String environment;

        public Key(String system, String application, String country, String environment) {
            this.system = system;
            this.application = application;
            this.country = country;
            this.environment = environment;
        }


        public String getSystem() {
            return system;
        }

        public String getApplication() {
            return application;
        }

        public String getCountry() {
            return country;
        }

        public String getEnvironment() {
            return environment;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            Key key = (Key) o;

            if (system != null ? !system.equals(key.system) : key.system != null) return false;
            if (application != null ? !application.equals(key.application) : key.application != null)
                return false;
            if (country != null ? !country.equals(key.country) : key.country != null) return false;
            return environment != null ? environment.equals(key.environment) : key.environment == null;

        }

        @Override
        public int hashCode() {
            int result = system != null ? system.hashCode() : 0;
            result = 31 * result + (application != null ? application.hashCode() : 0);
            result = 31 * result + (country != null ? country.hashCode() : 0);
            result = 31 * result + (environment != null ? environment.hashCode() : 0);
            return result;
        }

        @Override
        public String toString() {
            return "Key{" +
                    "system='" + system + '\'' +
                    ", application='" + application + '\'' +
                    ", country='" + country + '\'' +
                    ", environment='" + environment + '\'' +
                    '}';
        }

    }

    private String system;
    private String country;
    private String environment;
    private String application;
    private String ip;
    private String domain;
    private String url;
    private String urlLogin;
    private String var1;
    private String var2;
    private String var3;
    private String var4;
    private int poolSize;

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

    public String getVar1() {
        return var1;
    }

    public void setVar1(String var1) {
        this.var1 = var1;
    }

    public String getVar2() {
        return var2;
    }

    public void setVar2(String var2) {
        this.var2 = var2;
    }

    public String getVar3() {
        return var3;
    }

    public void setVar3(String var3) {
        this.var3 = var3;
    }

    public String getVar4() {
        return var4;
    }

    public void setVar4(String var4) {
        this.var4 = var4;
    }

    public int getPoolSize() {
        return poolSize;
    }

    public void setPoolSize(int poolSize) {
        this.poolSize = poolSize;
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
        hash = 29 * hash + (this.var1 != null ? this.var1.hashCode() : 0);
        hash = 29 * hash + (this.var2 != null ? this.var2.hashCode() : 0);
        hash = 29 * hash + (this.var3 != null ? this.var3.hashCode() : 0);
        hash = 29 * hash + (this.var4 != null ? this.var4.hashCode() : 0);
        hash = 29 * hash + this.poolSize;
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
        if ((this.var1 == null) ? (other.var1 != null) : !this.var1.equals(other.var1)) {
            return false;
        }
        if ((this.var2 == null) ? (other.var2 != null) : !this.var2.equals(other.var2)) {
            return false;
        }
        if ((this.var3 == null) ? (other.var3 != null) : !this.var3.equals(other.var3)) {
            return false;
        }
        if ((this.var4 == null) ? (other.var4 != null) : !this.var4.equals(other.var4)) {
            return false;
        }
        if (this.poolSize != other.poolSize) {
            return false;
        }
        return true;
    }

}
