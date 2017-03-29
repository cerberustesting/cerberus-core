/**
 * Cerberus Copyright (C) 2013 - 2017 cerberustesting
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
package org.cerberus.crud.entity;

import java.util.List;

/**
 * @author vertigo
 */
public class User {

    private int userID;
    private String login;
    private String password;
    private String resetPasswordToken;
    private String request;
    private String name;
    private String team;
    private String language;
    private String reportingFavorite;
    private String robotHost;
    private String robotPort;
    private String robotPlatform;
    private String robotBrowser;
    private String robotVersion;
    private String robot;
    private String defaultSystem;
    private String email;
    private String userPreferences;
    private List<UserSystem> userSystems;
    private List<UserGroup> userGroups;

    public String getUserPreferences() {
        return userPreferences;
    }

    public void setUserPreferences(String userPreferences) {
        this.userPreferences = userPreferences;
    }

    public String getResetPasswordToken() {
        return resetPasswordToken;
    }

    public void setResetPasswordToken(String resetPasswordToken) {
        this.resetPasswordToken = resetPasswordToken;
    }

    public List<UserGroup> getUserGroups() {
        return userGroups;
    }

    public void setUserGroups(List<UserGroup> userGroups) {
        this.userGroups = userGroups;
    }

    public List<UserSystem> getUserSystems() {
        return userSystems;
    }

    public void setUserSystems(List<UserSystem> userSystems) {
        this.userSystems = userSystems;
    }

    public String getRobotPort() {
        return robotPort;
    }

    public void setRobotPort(String robotPort) {
        this.robotPort = robotPort;
    }

    public String getRobotPlatform() {
        return robotPlatform;
    }

    public void setRobotPlatform(String robotPlatform) {
        this.robotPlatform = robotPlatform;
    }

    public String getRobotBrowser() {
        return robotBrowser;
    }

    public void setRobotBrowser(String robotBrowser) {
        this.robotBrowser = robotBrowser;
    }

    public String getRobotVersion() {
        return robotVersion;
    }

    public void setRobotVersion(String robotVersion) {
        this.robotVersion = robotVersion;
    }

    public String getRobot() {
        return robot;
    }

    public void setRobot(String robot) {
        this.robot = robot;
    }

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

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public String getReportingFavorite() {
        return reportingFavorite;
    }

    public void setReportingFavorite(String reportingFavorite) {
        this.reportingFavorite = reportingFavorite;
    }

    public String getRobotHost() {
        return robotHost;
    }

    public void setRobotHost(String robotHost) {
        this.robotHost = robotHost;
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
        if ((this.robotHost == null) ? (other.robotHost != null) : !this.robotHost.equals(other.robotHost)) {
            return false;
        }
        if (this.robotPort != other.robotPort && (this.robotPort == null || !this.robotPort.equals(other.robotPort))) {
            return false;
        }
        if ((this.robotPlatform == null) ? (other.robotPlatform != null) : !this.robotPlatform.equals(other.robotPlatform)) {
            return false;
        }
        if ((this.robotBrowser == null) ? (other.robotBrowser != null) : !this.robotBrowser.equals(other.robotBrowser)) {
            return false;
        }
        if ((this.robotVersion == null) ? (other.robotVersion != null) : !this.robotVersion.equals(other.robotVersion)) {
            return false;
        }
        if ((this.robot == null) ? (other.robot != null) : !this.robot.equals(other.robot)) {
            return false;
        }
        if ((this.defaultSystem == null) ? (other.defaultSystem != null) : !this.defaultSystem.equals(other.defaultSystem)) {
            return false;
        }
        if ((this.resetPasswordToken == null) ? (other.resetPasswordToken != null) : !this.resetPasswordToken.equals(other.resetPasswordToken)) {
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
        hash = 97 * hash + (this.resetPasswordToken != null ? this.resetPasswordToken.hashCode() : 0);        
        hash = 97 * hash + (this.request != null ? this.request.hashCode() : 0);
        hash = 97 * hash + (this.name != null ? this.name.hashCode() : 0);
        hash = 97 * hash + (this.team != null ? this.team.hashCode() : 0);
        hash = 97 * hash + (this.reportingFavorite != null ? this.reportingFavorite.hashCode() : 0);
        hash = 97 * hash + (this.robotHost != null ? this.robotHost.hashCode() : 0);
        hash = 97 * hash + (this.robotPort != null ? this.robotPort.hashCode() : 0);
        hash = 97 * hash + (this.robotPlatform != null ? this.robotPlatform.hashCode() : 0);
        hash = 97 * hash + (this.robotBrowser != null ? this.robotBrowser.hashCode() : 0);
        hash = 97 * hash + (this.robotVersion != null ? this.robotVersion.hashCode() : 0);
        hash = 97 * hash + (this.defaultSystem != null ? this.defaultSystem.hashCode() : 0);
        hash = 97 * hash + (this.email != null ? this.email.hashCode() : 0);
        return hash;
    }

    

    
}
