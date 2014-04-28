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
 * @author vertigo
 */
public class User {

    private int userID;
    private String login;
    private String password;
    private String request;
    private String name;
    private String team;
    private String reportingFavorite;
    private String defaultIP;
    private Integer preferenceRobotPort;
    private String preferenceRobotPlatform;
    private String preferenceRobotOS;
    private String preferenceRobotBrowser;
    private String preferenceRobotVersion;
    

    public Integer getPreferenceRobotPort() {
        return preferenceRobotPort;
    }

    public void setPreferenceRobotPort(Integer preferenceRobotPort) {
        this.preferenceRobotPort = preferenceRobotPort;
    }

    public String getPreferenceRobotPlatform() {
        return preferenceRobotPlatform;
    }

    public void setPreferenceRobotPlatform(String preferenceRobotPlatform) {
        this.preferenceRobotPlatform = preferenceRobotPlatform;
    }

    public String getPreferenceRobotOS() {
        return preferenceRobotOS;
    }

    public void setPreferenceRobotOS(String preferenceRobotOS) {
        this.preferenceRobotOS = preferenceRobotOS;
    }

    public String getPreferenceRobotBrowser() {
        return preferenceRobotBrowser;
    }

    public void setPreferenceRobotBrowser(String preferenceRobotBrowser) {
        this.preferenceRobotBrowser = preferenceRobotBrowser;
    }

    public String getPreferenceRobotVersion() {
        return preferenceRobotVersion;
    }

    public void setPreferenceRobotVersion(String preferenceRobotVersion) {
        this.preferenceRobotVersion = preferenceRobotVersion;
    }
    private String defaultSystem;
    private String email;

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public int getUserID() {
        return userID;
    }

    public void setUserID(int userID) {
        this.userID = userID;
    }

    public String getLogin() {
        return login;
    }

    public void setLogin(String login) {
        this.login = login;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getRequest() {
        return request;
    }

    public void setRequest(String request) {
        this.request = request;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getTeam() {
        return team;
    }

    public void setTeam(String team) {
        this.team = team;
    }

    public String getReportingFavorite() {
        return reportingFavorite;
    }

    public void setReportingFavorite(String reportingFavorite) {
        this.reportingFavorite = reportingFavorite;
    }

    public String getDefaultIP() {
        return defaultIP;
    }

    public void setDefaultIP(String defaultIP) {
        this.defaultIP = defaultIP;
    }

    public String getDefaultSystem() {
        return defaultSystem;
    }

    public void setDefaultSystem(String defaultSystem) {
        this.defaultSystem = defaultSystem;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final User other = (User) obj;
        if (this.userID != other.userID) {
            return false;
        }
        if ((this.login == null) ? (other.login != null) : !this.login.equals(other.login)) {
            return false;
        }
        if ((this.password == null) ? (other.password != null) : !this.password.equals(other.password)) {
            return false;
        }
        if ((this.request == null) ? (other.request != null) : !this.request.equals(other.request)) {
            return false;
        }
        if ((this.name == null) ? (other.name != null) : !this.name.equals(other.name)) {
            return false;
        }
        if ((this.team == null) ? (other.team != null) : !this.team.equals(other.team)) {
            return false;
        }
        if ((this.reportingFavorite == null) ? (other.reportingFavorite != null) : !this.reportingFavorite.equals(other.reportingFavorite)) {
            return false;
        }
        if ((this.defaultIP == null) ? (other.defaultIP != null) : !this.defaultIP.equals(other.defaultIP)) {
            return false;
        }
        if (this.preferenceRobotPort != other.preferenceRobotPort && (this.preferenceRobotPort == null || !this.preferenceRobotPort.equals(other.preferenceRobotPort))) {
            return false;
        }
        if ((this.preferenceRobotPlatform == null) ? (other.preferenceRobotPlatform != null) : !this.preferenceRobotPlatform.equals(other.preferenceRobotPlatform)) {
            return false;
        }
        if ((this.preferenceRobotOS == null) ? (other.preferenceRobotOS != null) : !this.preferenceRobotOS.equals(other.preferenceRobotOS)) {
            return false;
        }
        if ((this.preferenceRobotBrowser == null) ? (other.preferenceRobotBrowser != null) : !this.preferenceRobotBrowser.equals(other.preferenceRobotBrowser)) {
            return false;
        }
        if ((this.preferenceRobotVersion == null) ? (other.preferenceRobotVersion != null) : !this.preferenceRobotVersion.equals(other.preferenceRobotVersion)) {
            return false;
        }
        if ((this.defaultSystem == null) ? (other.defaultSystem != null) : !this.defaultSystem.equals(other.defaultSystem)) {
            return false;
        }
        if ((this.email == null) ? (other.email != null) : !this.email.equals(other.email)) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 97 * hash + this.userID;
        hash = 97 * hash + (this.login != null ? this.login.hashCode() : 0);
        hash = 97 * hash + (this.password != null ? this.password.hashCode() : 0);
        hash = 97 * hash + (this.request != null ? this.request.hashCode() : 0);
        hash = 97 * hash + (this.name != null ? this.name.hashCode() : 0);
        hash = 97 * hash + (this.team != null ? this.team.hashCode() : 0);
        hash = 97 * hash + (this.reportingFavorite != null ? this.reportingFavorite.hashCode() : 0);
        hash = 97 * hash + (this.defaultIP != null ? this.defaultIP.hashCode() : 0);
        hash = 97 * hash + (this.preferenceRobotPort != null ? this.preferenceRobotPort.hashCode() : 0);
        hash = 97 * hash + (this.preferenceRobotPlatform != null ? this.preferenceRobotPlatform.hashCode() : 0);
        hash = 97 * hash + (this.preferenceRobotOS != null ? this.preferenceRobotOS.hashCode() : 0);
        hash = 97 * hash + (this.preferenceRobotBrowser != null ? this.preferenceRobotBrowser.hashCode() : 0);
        hash = 97 * hash + (this.preferenceRobotVersion != null ? this.preferenceRobotVersion.hashCode() : 0);
        hash = 97 * hash + (this.defaultSystem != null ? this.defaultSystem.hashCode() : 0);
        hash = 97 * hash + (this.email != null ? this.email.hashCode() : 0);
        return hash;
    }

    

    
}
