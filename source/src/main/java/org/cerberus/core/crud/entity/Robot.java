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

import lombok.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 *
 * @author bcivel
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Robot {

    private static final Logger LOG = LogManager.getLogger(Robot.class);

    private Integer robotID;
    private String robot;
    private String type; // Robot Type (GUI / APK / IPA / ...)
    private String platform;
    private String browser;
    private String version;
    private boolean active;
    private String userAgent;
    private String screenSize;
    private String profileFolder;
    private String extraParam;
    private Integer acceptNotifications;
    private boolean acceptInsecureCerts;
    private String preloadScript;
    private String robotDecli;
    private String lbexemethod; // Contain the method used in order to spread the load against all executors of the robot
    private String description;
    private String usrCreated;
    private Timestamp dateCreated;
    private String usrModif;
    private Timestamp dateModif;

    public static final String LOADBALANCINGEXECUTORMETHOD_ROUNDROBIN = "ROUNDROBIN";
    public static final String LOADBALANCINGEXECUTORMETHOD_BYRANKING = "BYRANKING";

    /**
     * From here are data outside database model.
     */
    private List<RobotCapability> capabilities;
    private List<RobotCapability> capabilitiesDecoded;
    private List<RobotExecutor> executors;

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
            result.put("preloadScript", this.getPreloadScript());
            result.put("extraParam", this.getExtraParam());

            if (withChilds) {
                JSONArray arrayCap = new JSONArray();
                if (this.getCapabilities() != null) {
                    for (RobotCapability capability : this.getCapabilities()) {
                        arrayCap.put(capability.toJson());
                    }
                }
                result.put("capabilities", arrayCap);

                JSONArray arrayExecutor = new JSONArray();
                if (this.getExecutors() != null) {
                    for (RobotExecutor executor : this.getExecutors()) {
                        arrayExecutor.put(executor.toJson(secured));
                    }
                }
                result.put("executors", arrayExecutor);
            }

        } catch (Exception ex) {
            LOG.error("Error converting Robot to JSON", ex);
        }
        return result;
    }

}
