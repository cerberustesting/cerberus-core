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

import java.sql.Timestamp;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.cerberus.util.StringUtil;
import org.json.JSONException;
import org.json.JSONObject;

/**
 *
 * @author bcivel
 */
public class RobotExecutor {

    private static final Logger LOG = LogManager.getLogger(RobotExecutor.class);

    private Integer ID;
    private String robot;
    private String executor;
    private String active;
    private Integer rank;
    private String host;
    private String port;
    private String hostUser;
    private String hostPassword;
    private String deviceUdid;
    private String deviceName;
    private Integer devicePort;
    private String deviceLockUnlock;
    private String description;
    private long dateLastExeSubmitted;
    private String executorExtensionHost;
    private Integer executorExtensionPort;
    private String executorProxyHost;
    private Integer executorProxyPort;
    private String executorProxyActive;       
    private String UsrCreated;
    private Timestamp DateCreated;
    private String UsrModif;
    private Timestamp DateModif;

    public Integer getID() {
        return ID;
    }

    public void setID(Integer ID) {
        this.ID = ID;
    }

    public String getRobot() {
        return robot;
    }

    public void setRobot(String robot) {
        this.robot = robot;
    }

    public String getExecutor() {
        return executor;
    }

    public void setExecutor(String executor) {
        this.executor = executor;
    }

    public long getDateLastExeSubmitted() {
        return dateLastExeSubmitted;
    }

    public void setDateLastExeSubmitted(long dateLastExeSubmitted) {
        this.dateLastExeSubmitted = dateLastExeSubmitted;
    }

    public String getActive() {
        return active;
    }

    public void setActive(String active) {
        this.active = active;
    }

    public Integer getRank() {
        return rank;
    }

    public void setRank(Integer rank) {
        this.rank = rank;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public String getPort() {
        return port;
    }

    public void setPort(String port) {
        this.port = port;
    }

    public String getHostUser() {
        return hostUser;
    }

    public void setHostUser(String hostUser) {
        this.hostUser = hostUser;
    }

    public String getHostPassword() {
        return hostPassword;
    }

    public void setHostPassword(String hostPassword) {
        this.hostPassword = hostPassword;
    }

    public String getDeviceUuid() {
        return deviceUdid;
    }

    public void setDeviceUuid(String deviceUdid) {
        this.deviceUdid = deviceUdid;
    }

    public String getDeviceName() {
        return deviceName;
    }

    public void setDeviceName(String deviceName) {
        this.deviceName = deviceName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getUsrCreated() {
        return UsrCreated;
    }

    public void setUsrCreated(String UsrCreated) {
        this.UsrCreated = UsrCreated;
    }

    public Timestamp getDateCreated() {
        return DateCreated;
    }

    public void setDateCreated(Timestamp DateCreated) {
        this.DateCreated = DateCreated;
    }

    public String getUsrModif() {
        return UsrModif;
    }

    public void setUsrModif(String UsrModif) {
        this.UsrModif = UsrModif;
    }

    public Timestamp getDateModif() {
        return DateModif;
    }

    public Integer getDevicePort() {
        return devicePort;
    }

    public void setDevicePort(Integer devicePort) {
        this.devicePort = devicePort;
    }

    public String getExecutorExtensionHost() {
        return executorExtensionHost;
    }

    public void setExecutorExtensionHost(String executorExtensionHost) {
        this.executorExtensionHost = executorExtensionHost;
    }
    
    public Integer getExecutorExtensionPort() {
        return executorExtensionPort;
    }

    public void setExecutorExtensionPort(Integer executorExtensionPort) {
        this.executorExtensionPort = executorExtensionPort;
    }

    public String getExecutorProxyHost() {
        return executorProxyHost;
    }

    public void setExecutorProxyHost(String executorProxyHost) {
        this.executorProxyHost = executorProxyHost;
    }

    public Integer getExecutorProxyPort() {
        return executorProxyPort;
    }

    public void setExecutorProxyPort(Integer executorProxyPort) {
        this.executorProxyPort = executorProxyPort;
    }

    public String getExecutorProxyActive() {
        return executorProxyActive;
    }

    public void setExecutorProxyActive(String executorProxyActive) {
        this.executorProxyActive = executorProxyActive;
    }
    
    

    /**
     * From here are data outside database model.
     */
    public void setDateModif(Timestamp DateModif) {
        this.DateModif = DateModif;
    }

    public String getDeviceLockUnlock() {
        return deviceLockUnlock;
    }

    public void setDeviceLockUnlock(String deviceLockUnlock) {
        this.deviceLockUnlock = deviceLockUnlock;
    }

    public String getHostWithCredential() {
        String credential = "";
        if (!StringUtil.isNullOrEmpty(this.getHostUser())) {
            credential = this.getHostUser() + ":" + this.getHostPassword() + "@";
        }

        return credential + this.getHost();
    }

    public boolean hasSameKey(RobotExecutor obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }

        final RobotExecutor other = (RobotExecutor) obj;
        if ((this.robot == null) ? (other.robot != null) : !this.robot.equals(other.robot)) {
            return false;
        }
        if ((this.executor == null) ? (other.executor != null) : !this.executor.equals(other.executor)) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {

        int hash = 3;
        hash = 67 * hash + (this.robot != null ? this.robot.hashCode() : 0);
        hash = 67 * hash + (this.executor != null ? this.executor.hashCode() : 0);
        hash = 67 * hash + this.rank;
        hash = 67 * hash + (this.host != null ? this.host.hashCode() : 0);
        hash = 67 * hash + (this.port != null ? this.port.hashCode() : 0);
        hash = 67 * hash + (this.hostUser != null ? this.hostUser.hashCode() : 0);
        hash = 67 * hash + (this.hostPassword != null ? this.hostPassword.hashCode() : 0);
        hash = 67 * hash + (this.deviceUdid != null ? this.deviceUdid.hashCode() : 0);
        hash = 67 * hash + (this.deviceName != null ? this.deviceName.hashCode() : 0);
        hash = 67 * hash + (this.active != null ? this.active.hashCode() : 0);
        hash = 67 * hash + (this.description != null ? this.description.hashCode() : 0);
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
        final RobotExecutor other = (RobotExecutor) obj;
        if ((this.robot == null) ? (other.robot != null) : !this.robot.equals(other.robot)) {
            return false;
        }
        if ((this.executor == null) ? (other.executor != null) : !this.executor.equals(other.executor)) {
            return false;
        }
        if (this.rank != other.rank) {
            return false;
        }
        if ((this.host == null) ? (other.host != null) : !this.host.equals(other.host)) {
            return false;
        }
        if ((this.port == null) ? (other.port != null) : !this.port.equals(other.port)) {
            return false;
        }
        if ((this.hostUser == null) ? (other.hostUser != null) : !this.hostUser.equals(other.hostUser)) {
            return false;
        }
        if ((this.hostPassword == null) ? (other.hostPassword != null) : !this.hostPassword.equals(other.hostPassword)) {
            return false;
        }
        if ((this.deviceUdid == null) ? (other.deviceUdid != null) : !this.deviceUdid.equals(other.deviceUdid)) {
            return false;
        }
        if ((this.deviceName == null) ? (other.deviceName != null) : !this.deviceName.equals(other.deviceName)) {
            return false;
        }
        if ((this.devicePort == null) ? (other.devicePort != null) : !this.devicePort.equals(other.devicePort)) {
            return false;
        }
        if ((this.active == null) ? (other.active != null) : !this.active.equals(other.active)) {
            return false;
        }
        if ((this.description == null) ? (other.description != null) : !this.description.equals(other.description)) {
            return false;
        }
        if ((this.deviceLockUnlock == null) ? (other.deviceLockUnlock != null) : !this.deviceLockUnlock.equals(other.deviceLockUnlock)) {
            return false;
        }
        if ((this.executorProxyActive == null) ? (other.executorProxyActive != null) : !this.executorProxyActive.equals(other.executorProxyActive)) {
            return false;
        }
        if ((this.executorExtensionHost == null) ? (other.executorExtensionHost != null) : !this.executorExtensionHost.equals(other.executorExtensionHost)) {
            return false;
        }
        if ((this.executorExtensionPort == null) ? (other.executorExtensionPort != null) : !this.executorExtensionPort.equals(other.executorExtensionPort)) {
            return false;
        }
        if ((this.executorProxyHost == null) ? (other.executorProxyHost != null) : !this.executorProxyHost.equals(other.executorProxyHost)) {
            return false;
        }
        if ((this.executorProxyPort == null) ? (other.executorProxyPort != null) : !this.executorProxyPort.equals(other.executorProxyPort)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return robot + " - " + executor + " - " + host;
    }

    public JSONObject toJson(boolean secured) {
        JSONObject result = new JSONObject();
        try {
            result.put("DateCreated", this.getDateCreated());
            result.put("DateModif", this.getDateModif());
            result.put("ID", this.getID());
            result.put("UsrCreated", this.getUsrCreated());
            result.put("UsrModif", this.getUsrModif());
            result.put("active", this.getActive());
            result.put("description", this.getDescription());
            result.put("deviceName", this.getDeviceName());
            result.put("deviceUdid", this.getDeviceUuid());
            result.put("devicePort", this.getDevicePort());
            result.put("deviceLockUnlock", "Y".equals(this.getDeviceLockUnlock()) ? true : false);
            result.put("executorExtensionHost", this.getExecutorExtensionHost());
            result.put("executorExtensionPort", this.getExecutorExtensionPort());
            result.put("executorProxyHost", this.getExecutorProxyHost());
            result.put("executorProxyPort", this.getExecutorProxyPort());
            result.put("executorProxyActive", "Y".equals(this.getExecutorProxyActive()) ? true : false);
            result.put("executor", this.getExecutor());
            result.put("host", this.getHost());
            if (secured) {
                if (this.getHostPassword() != null && !this.getHostPassword().equals("")) {
                    result.put("hostPassword", "XXXXXXXXXX");
                } else {
                    result.put("hostPassword", "");
                }
            } else {
                result.put("hostPassword", this.getHostPassword());
            }
            result.put("hostUser", this.getHostUser());
            result.put("port", this.getPort());
            result.put("rank", this.getRank());
            result.put("robot", this.getRobot());
        } catch (JSONException ex) {
            LOG.error(ex.toString(), ex);
        } catch (Exception ex) {
            LOG.error(ex.toString(), ex);
        }
        return result;
    }

}
