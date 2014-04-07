/*
 * Here comes the text of your license
 * Each line should be prefixed with  * 
 */

package org.cerberus.entity;

import java.io.Serializable;

/**
 *
 * @author memiks
@Entity
@Table(catalog = "cerberus", schema = "", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"campaign", "testbattery"})})
@XmlRootElement
@NamedQueries({
    @NamedQuery(name = "CampaignContent.findAll", query = "SELECT c FROM CampaignContent c"),
    @NamedQuery(name = "CampaignContent.findByCampaigncontentID", query = "SELECT c FROM CampaignContent c WHERE c.campaigncontentID = :campaigncontentID")})
 */
public class CampaignContent implements Serializable {
    private static final long serialVersionUID = 1L;

    private Integer campaigncontentID;
    private TestBattery testbattery;
    private Campaign campaign;

    public CampaignContent() {
    }

    public CampaignContent(Integer campaigncontentID) {
        this.campaigncontentID = campaigncontentID;
    }

    public Integer getCampaigncontentID() {
        return campaigncontentID;
    }

    public void setCampaigncontentID(Integer campaigncontentID) {
        this.campaigncontentID = campaigncontentID;
    }

    public TestBattery getTestbattery() {
        return testbattery;
    }

    public void setTestbattery(TestBattery testbattery) {
        this.testbattery = testbattery;
    }

    public Campaign getCampaign() {
        return campaign;
    }

    public void setCampaign(Campaign campaign) {
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
        return "org.cerberus.entity.CampaignContent[ campaigncontentID=" + campaigncontentID + " ]";
    }

}
