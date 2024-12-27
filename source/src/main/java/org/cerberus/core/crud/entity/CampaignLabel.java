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

public class CampaignLabel {

    private Integer campaignLabelID;
    private String campaign;
    private Integer LabelId;
    private String UsrCreated;
    private Timestamp DateCreated;
    private String UsrModif;
    private Timestamp DateModif;
    
    /**
     * Not included in table.
     */
    private Label label;

    public Label getLabel() {
        return label;
    }

    public void setLabel(Label label) {
        this.label = label;
    }

    public Integer getCampaignLabelID() {
        return campaignLabelID;
    }

    public void setCampaignLabelID(Integer campaignLabelID) {
        this.campaignLabelID = campaignLabelID;
    }

    public String getCampaign() {
        return campaign;
    }

    public void setCampaign(String campaign) {
        this.campaign = campaign;
    }

    public Integer getLabelId() {
        return LabelId;
    }

    public void setLabelId(Integer LabelId) {
        this.LabelId = LabelId;
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

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (campaignLabelID != null ? campaignLabelID.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof CampaignLabel)) {
            return false;
        }
        CampaignLabel other = (CampaignLabel) object;
        if ((this.campaign == null && other.campaign != null) || (this.campaign != null && !this.campaign.equals(other.campaign))) {
            return false;
        }
        if ((this.LabelId == null && other.LabelId != null) || (this.LabelId != null && !this.LabelId.equals(other.LabelId))) {
            return false;
        }
        return true;
    }

    public boolean hasSameKey(CampaignLabel obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final CampaignLabel other = obj;
        if ((this.LabelId == null) ? (other.LabelId != null) : !this.LabelId.equals(other.LabelId)) {
            return false;
        }
        if ((this.campaign == null) ? (other.campaign != null) : !this.campaign.equals(other.campaign)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "org.cerberus.crud.entity.CampaignLabel[ campaignLabelID=" + campaignLabelID + " ]";
    }

}
