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

import java.sql.Timestamp;

import java.util.List;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 *
 * @author bcivel
 */
public class Robot {

    private static final Logger LOG = LogManager.getLogger(Robot.class);

    private Integer robotID;
    private String robot;
    private String type; // Robot Type (GUI / APK / IPA / ...)
    private String platform;
    private String browser;
    private String version;
    private boolean isActive;
    private String userAgent;
    private String screenSize;
    private String profileFolder;
    private String extraParam;
    private Integer acceptNotifications;
    private boolean isAcceptInsecureCerts;
    private String robotDecli;
    private String lbexemethod; // Contain the method used in order to spread the load against all executors of the robot.
    private String description;
    private String UsrCreated;
    private Timestamp DateCreated;
    private String UsrModif;
    private Timestamp DateModif;

    public static final String LOADBALANCINGEXECUTORMETHOD_ROUNDROBIN = "ROUNDROBIN";
    public static final String LOADBALANCINGEXECUTORMETHOD_BYRANKING = "BYRANKING";

    /**
     * From here are data outside database model.
     */
    private List<RobotCapability> capabilities;
    private List<RobotCapability> capabilitiesDecoded;
    private List<RobotExecutor> executors;

    public String getExtraParam() {
        return extraParam;
    }

    public void setExtraParam(String extraParam) {
        this.extraParam = extraParam;
    }

    public boolean isAcceptInsecureCerts() {
        return isAcceptInsecureCerts;
    }

    public void setIsAcceptInsecureCerts(boolean isAcceptInsecureCerts) {
        this.isAcceptInsecureCerts = isAcceptInsecureCerts;
    }

    public String getProfileFolder() {
        return profileFolder;
    }

    public void setProfileFolder(String profileFolder) {
        this.profileFolder = profileFolder;
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

    public void setDateModif(Timestamp DateModif) {
        this.DateModif = DateModif;
    }

    public List<RobotExecutor> getExecutors() {
        return executors;
    }

    public void setExecutors(List<RobotExecutor> executors) {
        this.executors = executors;
    }

    public List<RobotCapability> getCapabilitiesDecoded() {
        return capabilitiesDecoded;
    }

    public void setCapabilitiesDecoded(List<RobotCapability> capabilitiesDecoded) {
        this.capabilitiesDecoded = capabilitiesDecoded;
    }

    public String getLbexemethod() {
        return lbexemethod;
    }

    public void setLbexemethod(String lbexemethod) {
        this.lbexemethod = lbexemethod;
    }

    public String getRobotDecli() {
        return robotDecli;
    }

    public void setRobotDecli(String robotDecli) {
        this.robotDecli = robotDecli;
    }

    public String getScreenSize() {
        return screenSize;
    }

    public void setScreenSize(String screenSize) {
        this.screenSize = screenSize;
    }

    public String getUserAgent() {
        return userAgent;
    }

    public void setUserAgent(String userAgent) {
        this.userAgent = userAgent;
    }

    public Integer getRobotID() {
        return robotID;
    }

    public void setRobotID(Integer robotID) {
        this.robotID = robotID;
    }

    public String getRobot() {
        return robot;
    }

    public void setRobot(String robot) {
        this.robot = robot;
    }

    public boolean isActive() {
        return isActive;
    }

    public void setIsActive(boolean active) {
        this.isActive = active;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getPlatform() {
        return platform;
    }

    public void setPlatform(String platform) {
        this.platform = platform;
    }

    public String getBrowser() {
        return browser;
    }

    public void setBrowser(String browser) {
        this.browser = browser;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public List<RobotCapability> getCapabilities() {
        return capabilities;
    }

    public void setCapabilities(List<RobotCapability> capabilities) {
        this.capabilities = capabilities;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Integer getAcceptNotifications(){return acceptNotifications;}

    public void setAcceptNotifications(Integer acceptNotifications){this.acceptNotifications = acceptNotifications;}

    /**
     * Convert the current TestCaseExecution into JSON format
     *
     * @param withChilds boolean that define if childs should be included
     * @param secured
     * @return TestCaseExecution in JSONObject format
     */
    public JSONObject toJson(boolean withChilds, boolean secured) {
        JSONObject result = new JSONObject();
        try {
            result.put("isActive", this.isActive());
            result.put("description", this.getDescription());
            result.put("userAgent", this.getUserAgent());
            result.put("robotID", this.getRobotID());
            result.put("version", this.getVersion());
            result.put("platform", this.getPlatform());
            result.put("robot", this.getRobot());
            result.put("robotDecli", this.getRobotDecli());
            result.put("screenSize", this.getScreenSize());
            result.put("browser", this.getBrowser());
            result.put("lbexemethod", this.getLbexemethod());
            result.put("type", this.getType());
            result.put("isAcceptInsecureCerts", this.isAcceptInsecureCerts());
            result.put("acceptNotifications", this.getAcceptNotifications());
            result.put("extraParam", this.getExtraParam());

            if (withChilds) {
                // Looping on ** Capabilities **
                JSONArray arrayCap = new JSONArray();
                if (this.getCapabilities() != null) {
                    for (Object capability : this.getCapabilities()) {
                        arrayCap.put(((RobotCapability) capability).toJson());
                    }
                }
                result.put("capabilities", arrayCap);

                // Looping on ** Executors **
                JSONArray arrayExecutor = new JSONArray();
                if (this.getExecutors() != null) {
                    for (Object executor : this.getExecutors()) {
                        arrayExecutor.put(((RobotExecutor) executor).toJson(secured));
                    }
                }
                result.put("executors", arrayExecutor);

            }

        } catch (JSONException ex) {
            LOG.error(ex.toString(), ex);
        } catch (Exception ex) {
            LOG.error(ex.toString(), ex);
        }
        return result;
    }

}
