/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.redcats.tst.entity;

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
    private String defaultSystem;

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
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof User)) return false;

        User user = (User) o;

        if (userID != user.userID) return false;
        if (defaultIP != null ? !defaultIP.equals(user.defaultIP) : user.defaultIP != null) return false;
        if (defaultSystem != null ? !defaultSystem.equals(user.defaultSystem) : user.defaultSystem != null)
            return false;
        if (login != null ? !login.equals(user.login) : user.login != null) return false;
        if (name != null ? !name.equals(user.name) : user.name != null) return false;
        if (password != null ? !password.equals(user.password) : user.password != null) return false;
        if (reportingFavorite != null ? !reportingFavorite.equals(user.reportingFavorite) : user.reportingFavorite != null)
            return false;
        if (request != null ? !request.equals(user.request) : user.request != null) return false;
        if (team != null ? !team.equals(user.team) : user.team != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = userID;
        result = 31 * result + (login != null ? login.hashCode() : 0);
        result = 31 * result + (password != null ? password.hashCode() : 0);
        result = 31 * result + (request != null ? request.hashCode() : 0);
        result = 31 * result + (name != null ? name.hashCode() : 0);
        result = 31 * result + (team != null ? team.hashCode() : 0);
        result = 31 * result + (reportingFavorite != null ? reportingFavorite.hashCode() : 0);
        result = 31 * result + (defaultIP != null ? defaultIP.hashCode() : 0);
        result = 31 * result + (defaultSystem != null ? defaultSystem.hashCode() : 0);
        return result;
    }
}
