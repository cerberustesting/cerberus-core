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

import java.awt.Color;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONException;
import org.json.JSONObject;

import java.sql.Timestamp;

/**
 * @author bcivel
 */
@Getter
@Setter
@EqualsAndHashCode
@ToString
public class Label {

    private Integer id;
    private String system;
    private String label;
    private String type;
    private String color;
    private String fontColor;
    private Integer parentLabelID;
    private String requirementType;
    private String requirementStatus;
    private String requirementCriticity;
    private String description;
    private String longDescription;
    @EqualsAndHashCode.Exclude
    private String usrCreated;
    @EqualsAndHashCode.Exclude
    private Timestamp dateCreated;
    @EqualsAndHashCode.Exclude
    private String usrModif;
    @EqualsAndHashCode.Exclude
    private Timestamp dateModif;

    // External Database model
    @EqualsAndHashCode.Exclude
    Integer counter1;

    /**
     * Invariant PROPERTY TYPE String.
     */
    public static final String TYPE_STICKER = "STICKER";
    public static final String TYPE_BATTERY = "BATTERY";
    public static final String TYPE_REQUIREMENT = "REQUIREMENT";

    private static final Logger LOG = LogManager.getLogger(Label.class);

    public JSONObject toJson() {
        JSONObject labelJson = new JSONObject();
        try {
            labelJson.put("id", this.getId());
            labelJson.put("system", this.getSystem());
            labelJson.put("label", this.getLabel());
            labelJson.put("type", this.getType());
            labelJson.put("color", this.getColor());
            labelJson.put("fontColor", this.guessFontColor());
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
            labelJson.put("fontColor", this.guessFontColor());
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
            result.put("fontColor", this.guessFontColor());
        } catch (JSONException ex) {
            LOG.error(ex.toString(), ex);
        }
        return result;
    }

    public String guessFontColor() {
        return (this.isColorDark(this.getColor()) ? "white" : "black");
    }

    public boolean isColorDark(String colorCode) {

        try {
            int red = 0;
            int green = 0;
            int blue = 0;

            // Check if color is in RGBA format: rgba(r,g,b,a)
            if (colorCode != null && colorCode.toLowerCase().startsWith("rgba")) {
                // Extract RGBA values using regex
                String rgbaValues = colorCode.substring(colorCode.indexOf('(') + 1, colorCode.indexOf(')'));
                String[] values = rgbaValues.split(",");
                if (values.length >= 3) {
                    red = Integer.parseInt(values[0].trim());
                    green = Integer.parseInt(values[1].trim());
                    blue = Integer.parseInt(values[2].trim());
                    // values[3] is the alpha channel, which we don't need for brightness calculation
                }
            } else {
                // Handle hexadecimal color format: #RRGGBB or RRGGBB
                String rawFontColor = colorCode;
                if (rawFontColor.startsWith("#")) {
                    rawFontColor = rawFontColor.substring(1);
                }

                // convert hex string to int
                int rgb = Integer.parseInt(rawFontColor, 16);

                Color c = new Color(rgb);
                red = c.getRed();
                green = c.getGreen();
                blue = c.getBlue();
            }

            // Calculate brightness using RGB values
            float[] hsb = Color.RGBtoHSB(red, green, blue, null);
            float brightness = hsb[2];

            LOG.debug("is the Color Dark ? " + colorCode + " : " + (brightness < 0.5));
            return (brightness < 0.5);
        } catch (Exception e) {
            LOG.warn("Could not guess if color " + colorCode + " is Dark.", e);
        }
        return true;
    }

}
