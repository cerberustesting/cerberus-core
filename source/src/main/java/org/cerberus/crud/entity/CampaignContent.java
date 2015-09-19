/*
 * Cerberus  Copyright (C) 2013  vertigo17
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

import java.io.Serializable;

/**
 *
 * @author memiks
@Entity
@Table(catalog = "cerberus", schema = "", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"campaign", "testbattery"})})
@XmlRootElement
@NamedQueries({
    @NamedQuery(name = "CampaignContent.findAll", query = "SELECT c FROM campaigncontent c"),
    @NamedQuery(name = "CampaignContent.findByCampaigncontentID", query = "SELECT c FROM campaigncontent c WHERE c.campaigncontentID = :campaigncontentID")})
 */
public class CampaignContent implements Serializable {
    private static final long serialVersionUID = 1L;

    private Integer campaigncontentID;
    private String testbattery;
    private String campaign;

    public CampaignContent() {
    }

    public CampaignContent(Integer campaigncontentID) {
        this.campaigncontentID = campaigncontentID;
    }

    public CampaignContent(Integer campaigncontentID, String testbattery, String campaign) {
        this.campaigncontentID = campaigncontentID;
        this.testbattery = testbattery;
        this.campaign = campaign;
    }

    public Integer getCampaigncontentID() {
        return campaigncontentID;
    }

    public void setCampaigncontentID(Integer campaigncontentID) {
        this.campaigncontentID = campaigncontentID;
    }

    public String getTestbattery() {
        return testbattery;
    }

    public void setTestbattery(String testbattery) {
        this.testbattery = testbattery;
    }

    public String getCampaign() {
        return campaign;
    }

    public void setCampaign(String campaign) {
        this.campaign = campaign;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (campaigncontentID != null ? campaigncontentID.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof CampaignContent)) {
            return false;
        }
        CampaignContent other = (CampaignContent) object;
        if ((this.campaigncontentID == null && other.campaigncontentID != null) || (this.campaigncontentID != null && !this.campaigncontentID.equals(other.campaigncontentID))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "org.cerberus.crud.entity.CampaignContent[ campaigncontentID=" + campaigncontentID + " ]";
    }

}
