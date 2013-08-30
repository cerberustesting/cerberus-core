/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.redcats.tst.entity;

import java.sql.Timestamp;

/**
 *
 * @author ip100003
 */
public class LogEvent {

    private long LogEventID;
    private long UserID;
    private String login;
    private Timestamp time;
    private String page;
    private String action;
    private String log;
    private String remoteIP;
    private String localIP;

    public long getUserID() {
        return UserID;
    }

    public void setUserID(long UserID) {
        this.UserID = UserID;
    }

    public String getremoteIP() {
        return remoteIP;
    }

    public void setremoteIP(String remoteIP) {
        this.remoteIP = remoteIP;
    }

    public String getLocalIP() {
        return localIP;
    }

    public void setLocalIP(String localIP) {
        this.localIP = localIP;
    }

    public long getLogEventID() {
        return LogEventID;
    }

    public void setLogEventID(long LogEventID) {
        this.LogEventID = LogEventID;
    }

    public String getLogin() {
        return login;
    }

    public void setLogin(String login) {
        this.login = login;
    }

    public Timestamp getTime() {
        return time;
    }

    public void setTime(Timestamp time) {
        this.time = time;
    }

    public String getPage() {
        return page;
    }

    public void setPage(String page) {
        this.page = page;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public String getLog() {
        return log;
    }

    public void setLog(String log) {
        this.log = log;
    }
}
