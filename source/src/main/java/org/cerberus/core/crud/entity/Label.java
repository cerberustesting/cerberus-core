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
    @EqualsAndHashCode.Exclude
    private String fontColor;

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

    /**
     * Label as seen by the GUI tree nodes (dto.TreeNode#toJson embeds this under
     * "label").
     *
     * Carries every field the browser needs to RENDER a tree row itself. It used
     * to expose only description/label/type/color/fontColor, which is why
     * ReadLabel#getTree had to build the row - chip, description, action buttons,
     * requirement pills - as an HTML string server-side and ship it in the node's
     * "text". That string concatenated the label name straight into inline
     * onclick handlers, so a label named  l'ol  broke out of the JS string; and it
     * hardcoded Bootstrap 3 classes the front end no longer uses.
     *
     * js/global/crbLabelTree.js renders from these fields and ignores "text"
     * entirely. "text" is still produced for the V1 rollback pages, which drive
     * bootstrap-treeview and have no other source for a row.
     */
    public JSONObject toJsonGUI() {
        JSONObject result = new JSONObject();
        try {
            result.put("id", this.getId());
            result.put("system", this.getSystem());
            result.put("description", this.getDescription());
            result.put("label", this.getLabel());
            result.put("type", this.getType());
            result.put("color", this.getColor());
            result.put("fontColor", this.guessFontColor());
            result.put("parentLabelID", this.getParentLabelID());
            result.put("requirementType", this.getRequirementType());
            result.put("requirementStatus", this.getRequirementStatus());
            result.put("requirementCriticity", this.getRequirementCriticity());
        } catch (JSONException ex) {
            LOG.error(ex.toString(), ex);
        }
        return result;
    }

    /**
     * Text colour to write on top of this label's background colour.
     *
     * Returns the ink with the best WCAG contrast ratio against the background.
     * This used to compare HSB *brightness*, which is max(R,G,B) and therefore
     * reads every saturated colour as light: #1e49c8 (a dark blue) scored 0.78,
     * was declared "not dark", and got black text that was barely readable.
     * Relative luminance weights the channels the way the eye does, so the same
     * blue now scores 0.09 and correctly gets white text.
     *
     * js/global/global.js#crbFontColorFor applies the identical rule client-side
     * for the chips built in the browser; keep the two in step.
     */
    public String guessFontColor() {
        return (this.isColorDark(this.getColor()) ? "white" : "black");
    }

    /**
     * True when white text reads better than black on the given colour.
     *
     * Kept named isColorDark for its callers, but the question it answers is
     * "does this need light ink", which is what a contrast comparison gives.
     */
    public boolean isColorDark(String colorCode) {

        try {
            int[] rgb = toRgb(colorCode);
            double luminance = relativeLuminance(rgb[0], rgb[1], rgb[2]);
            // Contrast against the two inks; white wins when it is the better read.
            double onWhite = contrastRatio(luminance, 1.0d);
            double onBlack = contrastRatio(luminance, 0.0d);
            return onWhite >= onBlack;

        } catch (Exception e) {
            LOG.warn("Could not guess if color " + colorCode + " is Dark.", e);
        }
        return true;
    }

    /** Parses "#RRGGBB", "RRGGBB" or "rgb/rgba(r,g,b[,a])" into {r, g, b}. */
    private static int[] toRgb(String colorCode) {
        // Check if color is in RGBA format: rgba(r,g,b,a)
        if (colorCode != null && colorCode.toLowerCase().startsWith("rgb")) {
            String rgbaValues = colorCode.substring(colorCode.indexOf('(') + 1, colorCode.indexOf(')'));
            String[] values = rgbaValues.split(",");
            if (values.length >= 3) {
                // values[3], the alpha channel, plays no part in the luminance.
                return new int[]{
                    Integer.parseInt(values[0].trim()),
                    Integer.parseInt(values[1].trim()),
                    Integer.parseInt(values[2].trim())
                };
            }
            throw new IllegalArgumentException("Unparseable colour " + colorCode);
        }

        // Handle hexadecimal color format: #RRGGBB or RRGGBB
        String rawFontColor = colorCode;
        if (rawFontColor.startsWith("#")) {
            rawFontColor = rawFontColor.substring(1);
        }
        Color c = new Color(Integer.parseInt(rawFontColor, 16));
        return new int[]{c.getRed(), c.getGreen(), c.getBlue()};
    }

    /**
     * Full inline style for this label's chip: background, readable ink, and an
     * outline when the colour would blend into the surface behind it.
     *
     * Mirrors crbChipStyle() in js/global/global.js so a chip rendered here (the
     * label trees) and one rendered in the browser look identical.
     */
    public String chipStyle() {
        String style = "background-color:" + this.getColor() + ";color:" + this.guessFontColor() + ";";
        if (this.needsOutline(this.getColor())) {
            style += "box-shadow:inset 0 0 0 1px rgba(148,163,184,.55);";
        }
        return style;
    }

    /**
     * True when the colour sits at either extreme and would disappear into the
     * card behind it: white on the light theme's card, black on the dark one's.
     */
    public boolean needsOutline(String colorCode) {
        try {
            int[] rgb = toRgb(colorCode);
            double luminance = relativeLuminance(rgb[0], rgb[1], rgb[2]);
            return luminance > 0.75d || luminance < 0.05d;
        } catch (Exception e) {
            return true;
        }
    }

    /** WCAG 2.1 relative luminance of an sRGB colour. */
    private static double relativeLuminance(int red, int green, int blue) {
        return 0.2126d * linearise(red) + 0.7152d * linearise(green) + 0.0722d * linearise(blue);
    }

    private static double linearise(int channel) {
        double v = channel / 255.0d;
        return v <= 0.03928d ? v / 12.92d : Math.pow((v + 0.055d) / 1.055d, 2.4d);
    }

    /** WCAG 2.1 contrast ratio between two relative luminances. */
    private static double contrastRatio(double l1, double l2) {
        double hi = Math.max(l1, l2);
        double lo = Math.min(l1, l2);
        return (hi + 0.05d) / (lo + 0.05d);
    }

}
