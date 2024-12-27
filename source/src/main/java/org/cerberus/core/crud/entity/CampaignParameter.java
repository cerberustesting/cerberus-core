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

import java.io.Serializable;

/**
 *
 * @author memiks
 * @Entity
 * @Table(catalog = "cerberus", schema = "", uniqueConstraints = {
 * @UniqueConstraint(columnNames = {"campaign", "Parameter"})})
 * @XmlRootElement
 * @NamedQueries({
 * @NamedQuery(name = "CampaignParameter.findAll", query = "SELECT c FROM
 * campaignparameter c"),
 * @NamedQuery(name = "CampaignParameter.findByCampaignparameterID", query =
 * "SELECT c FROM campaignparameter c WHERE c.campaignparameterID =
 * :campaignparameterID"),
 * @NamedQuery(name = "CampaignParameter.findByParameter", query = "SELECT c
 * FROM campaignparameter c WHERE c.parameter = :parameter"),
 * @NamedQuery(name = "CampaignParameter.findByValue", query = "SELECT c FROM
 * campaignparameter c WHERE c.value = :value")})
 */
public class CampaignParameter implements Serializable {

    private static final long serialVersionUID = 1L;

    public static final String COUNTRY_PARAMETER = "COUNTRY";
    public static final String ENVIRONMENT_PARAMETER = "ENVIRONMENT";
    public static final String ROBOT_PARAMETER = "ROBOT";
    
    public static final String PRIORITY_PARAMETER = "PRIORITY";
    public static final String STATUS_PARAMETER = "STATUS";
    public static final String SYSTEM_PARAMETER = "SYSTEM";
    public static final String APPLICATION_PARAMETER = "APPLICATION";
    public static final String TYPE_PARAMETER = "TYPE";
    public static final String TYPE_TESTFOLDER = "TESTFOLDER";
    public static final String BROWSER_PARAMETER = "BROWSER";

    private Integer campaignparameterID;
    private String parameter;
    private String value;
    private String campaign;

    public CampaignParameter() {
    }

    public CampaignParameter(Integer campaignparameterID) {
        this.campaignparameterID = campaignparameterID;
    }

    public CampaignParameter(Integer campaignparameterID, String campaign, String parameter, String value) {
        this.campaignparameterID = campaignparameterID;
        this.campaign = campaign;
        this.parameter = parameter;
        this.value = value;
    }

    public Integer getCampaignparameterID() {
        return campaignparameterID;
    }

    public void setCampaignparameterID(Integer campaignparameterID) {
        this.campaignparameterID = campaignparameterID;
    }

    public String getParameter() {
        return parameter;
    }

    public void setParameter(String parameter) {
        this.parameter = parameter;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
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
        hash += (campaignparameterID != null ? campaignparameterID.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof CampaignParameter)) {
            return false;
        }
        CampaignParameter other = (CampaignParameter) object;
        if ((this.campaignparameterID == null && other.campaignparameterID != null) || (this.campaignparameterID != null && !this.campaignparameterID.equals(other.campaignparameterID))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "org.cerberus.crud.entity.CampaignParameter[ campaignparameterID=" + campaignparameterID + " ]";
    }

    public boolean hasSameKey(CampaignParameter obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final CampaignParameter other = obj;
        if ((this.campaignparameterID == null) ? (other.campaignparameterID != null) : !this.campaignparameterID.equals(other.campaignparameterID)) {
            return false;
        }
        if ((this.parameter == null) ? (other.parameter != null) : !this.parameter.equals(other.parameter)) {
            return false;
        }
        if ((this.campaign == null) ? (other.campaign != null) : !this.campaign.equals(other.campaign)) {
            return false;
        }
        if ((this.value == null) ? (other.value != null) : !this.value.equals(other.value)) {
            return false;
        }
        return true;
    }

}
