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
import org.json.JSONException;
import org.json.JSONObject;

/**
 * @author bcivel
 */
public class Label {

    private Integer id;
    private String system;
    private String label;
    private String type;
    private String color;
    private Integer parentLabelID;
    private String reqType;
    private String reqStatus;
    private String reqCriticity;
    private String description;
    private String longDesc;
    private String usrCreated;
    private Timestamp dateCreated;
    private String usrModif;
    private Timestamp dateModif;
    // External Database model
    Integer counter1;
    /**
     * Invariant PROPERTY TYPE String.
     */
    public static final String TYPE_STICKER = "STICKER";
    public static final String TYPE_BATTERY = "BATTERY";
    public static final String TYPE_REQUIREMENT = "REQUIREMENT";

    private static final Logger LOG = LogManager.getLogger(Label.class);

    public Integer getCounter1() {
        return counter1;
    }

    public void setCounter1(Integer counter1) {
        this.counter1 = counter1;
    }

    public Integer getParentLabelID() {
        return parentLabelID;
    }

    public void setParentLabelID(Integer parentLabelID) {
        this.parentLabelID = parentLabelID;
    }

    public String getReqType() {
        return reqType;
    }

    public void setReqType(String reqType) {
        this.reqType = reqType;
    }

    public String getReqStatus() {
        return reqStatus;
    }

    public void setReqStatus(String reqStatus) {
        this.reqStatus = reqStatus;
    }

    public String getReqCriticity() {
        return reqCriticity;
    }

    public void setReqCriticity(String reqCriticity) {
        this.reqCriticity = reqCriticity;
    }

    public String getLongDesc() {
        return longDesc;
    }

    public void setLongDesc(String longDesc) {
        this.longDesc = longDesc;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getSystem() {
        return system;
    }

    public void setSystem(String system) {
        this.system = system;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public String getUsrCreated() {
        return usrCreated;
    }

    public void setUsrCreated(String usrCreated) {
        this.usrCreated = usrCreated;
    }

    public String getUsrModif() {
        return usrModif;
    }

    public void setUsrModif(String usrModif) {
        this.usrModif = usrModif;
    }

    public Timestamp getDateCreated() {
        return dateCreated;
    }

    public void setDateCreated(Timestamp dateCreated) {
        this.dateCreated = dateCreated;
    }

    public Timestamp getDateModif() {
        return dateModif;
    }

    public void setDateModif(Timestamp dateModif) {
        this.dateModif = dateModif;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public JSONObject toJson() {
        JSONObject labelJson = new JSONObject();
        try {
            labelJson.put("id", this.getId());
            labelJson.put("system", this.getSystem());
            labelJson.put("label", this.getLabel());
            labelJson.put("type", this.getType());
            labelJson.put("color", this.getColor());
            labelJson.put("parentLabelID", this.getParentLabelID());
            labelJson.put("requirementStatusType", this.getReqType());
            labelJson.put("requirementStatus", this.getReqStatus());
            labelJson.put("requirementStatusCriticity", this.getReqCriticity());
            labelJson.put("description", this.getDescription());
            labelJson.put("longDesc", this.getLongDesc());
            labelJson.put("usrCreated", this.getUsrCreated());
            labelJson.put("dateCreated", this.getDateCreated());
            labelJson.put("usrModif", this.getUsrModif());
            labelJson.put("dateModif", this.getDateModif());
            labelJson.put("counter1", this.getCounter1());
        } catch (JSONException ex) {
            LOG.error(ex.toString(), ex);
        }
        return labelJson;
    }

    public JSONObject toJsonGUI() {
        JSONObject result = new JSONObject();
        try {
            result.put("description", this.getDescription());
            result.put("label", this.getLabel());
            result.put("type", this.getType());
            result.put("color", this.getColor());
        } catch (JSONException ex) {
            LOG.error(ex.toString(), ex);
        }
        return result;
    }

    @Override
    public String toString() {
        return id + ":" + system + "-" + label;
    }

}
