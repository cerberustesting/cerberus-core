/*
 * Here comes the text of your license
 * Each line should be prefixed with  * 
 */

package org.cerberus.entity;

import java.io.Serializable;
import java.util.List;
import javax.xml.bind.annotation.XmlTransient;
import org.codehaus.jackson.annotate.JsonIgnore;

/**
 *
 * @author memiks
@Entity
@Table(catalog = "cerberus", schema = "", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"testbattery"})})
@XmlRootElement
@NamedQueries({
    @NamedQuery(name = "TestBattery.findAll", query = "SELECT t FROM TestBattery t"),
    @NamedQuery(name = "TestBattery.findByTestbatteryID", query = "SELECT t FROM TestBattery t WHERE t.testbatteryID = :testbatteryID"),
    @NamedQuery(name = "TestBattery.findByTestbattery", query = "SELECT t FROM TestBattery t WHERE t.testbattery = :testbattery"),
    @NamedQuery(name = "TestBattery.findByDescription", query = "SELECT t FROM TestBattery t WHERE t.description = :description")})
 */
public class TestBattery implements Serializable {
    private static final long serialVersionUID = 1L;

    private Integer testbatteryID;
    private String testbattery;
    private String description;
    private List<CampaignContent> campaignContentList;
    private List<TestBatteryContent> testBatteryContentList;

    public TestBattery() {
    }

    public TestBattery(Integer testbatteryID) {
        this.testbatteryID = testbatteryID;
    }

    public TestBattery(Integer testbatteryID, String testbattery, String description) {
        this.testbatteryID = testbatteryID;
        this.testbattery = testbattery;
        this.description = description;
    }

    public Integer getTestbatteryID() {
        return testbatteryID;
    }

    public void setTestbatteryID(Integer testbatteryID) {
        this.testbatteryID = testbatteryID;
    }

    public String getTestbattery() {
        return testbattery;
    }

    public void setTestbattery(String testbattery) {
        this.testbattery = testbattery;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @XmlTransient
    @JsonIgnore
    public List<CampaignContent> getCampaignContentList() {
        return campaignContentList;
    }

    public void setCampaignContentList(List<CampaignContent> campaignContentList) {
        this.campaignContentList = campaignContentList;
    }

    @XmlTransient
    @JsonIgnore
    public List<TestBatteryContent> getTestBatteryContentList() {
        return testBatteryContentList;
    }

    public void setTestBatteryContentList(List<TestBatteryContent> testBatteryContentList) {
        this.testBatteryContentList = testBatteryContentList;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (testbatteryID != null ? testbatteryID.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof TestBattery)) {
            return false;
        }
        TestBattery other = (TestBattery) object;
        if ((this.testbatteryID == null && other.testbatteryID != null) || (this.testbatteryID != null && !this.testbatteryID.equals(other.testbatteryID))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "org.cerberus.entity.TestBattery[ testbatteryID=" + testbatteryID + " ]";
    }

}
