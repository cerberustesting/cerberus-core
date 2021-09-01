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
    private String requirementType;
    private String requirementStatus;
    private String requirementCriticity;
    private String description;
    private String longDescription;
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

    public String getRequirementType() {
        return requirementType;
    }

    public void setRequirementType(String requirementType) {
        this.requirementType = requirementType;
    }

    public String getRequirementStatus() {
        return requirementStatus;
    }

    public void setRequirementStatus(String requirementStatus) {
        this.requirementStatus = requirementStatus;
    }

    public String getRequirementCriticity() {
        return requirementCriticity;
    }

    public void setRequirementCriticity(String requirementCriticity) {
        this.requirementCriticity = requirementCriticity;
    }

    public String getLongDescription() {
        return longDescription;
    }

    public void setLongDescription(String longDescription) {
        this.longDescription = longDescription;
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
            labelJson.put("requirementType", this.getRequirementType());
            labelJson.put("requirementStatus", this.getRequirementStatus());
            labelJson.put("requirementCriticity", this.getRequirementCriticity());
            labelJson.put("description", this.getDescription());
            labelJson.put("longDescription", this.getLongDescription());
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

    public JSONObject toJsonV001() {
        JSONObject labelJson = new JSONObject();
        try {
            labelJson.put("JSONVersion", "001");
            labelJson.put("id", this.getId());
            labelJson.put("system", this.getSystem());
            labelJson.put("label", this.getLabel());
            labelJson.put("type", this.getType());
            labelJson.put("color", this.getColor());
            labelJson.put("parentLabelID", this.getParentLabelID());
            labelJson.put("requirementType", this.getRequirementType());
            labelJson.put("requirementStatus", this.getRequirementStatus());
            labelJson.put("requirementCriticity", this.getRequirementCriticity());
            labelJson.put("description", this.getDescription());
            labelJson.put("longDescription", this.getLongDescription());
            labelJson.put("usrCreated", this.getUsrCreated());
            labelJson.put("dateCreated", this.getDateCreated());
            labelJson.put("usrModif", this.getUsrModif());
            labelJson.put("dateModif", this.getDateModif());
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
        return "Label{" + "id=" + id + ", system=" + system + ", label=" + label + ", type=" + type + ", color=" + color + ", parentLabelID=" + parentLabelID + ", requirementType=" + requirementType + ", requirementStatus=" + requirementStatus + ", requirementCriticity=" + requirementCriticity + ", description=" + description + ", longDescription=" + longDescription + ", usrCreated=" + usrCreated + ", dateCreated=" + dateCreated + ", usrModif=" + usrModif + ", dateModif=" + dateModif + ", counter1=" + counter1 + '}';
    }
}
