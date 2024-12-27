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
import java.sql.Timestamp;
import java.util.List;
import javax.xml.bind.annotation.XmlTransient;
import com.fasterxml.jackson.annotation.JsonIgnore;

public class Campaign implements Serializable {

    private static final long serialVersionUID = 1L;

    private Integer campaignID;
    private String campaign;
    private String CIScoreThreshold;
    private String Tag;
    private String Verbose;
    private String Screenshot;
    private String Video;
    private String PageSource;
    private String RobotLog;
    private String ConsoleLog;
    private String Timeout;
    private String Retries;
    private String Priority;
    private String ManualExecution;
    private String description;
    private String longDescription;
    private String group1;
    private String group2;
    private String group3;
    private String UsrCreated;
    private Timestamp DateCreated;
    private String UsrModif;
    private Timestamp DateModif;

    private List<CampaignParameter> campaignParameterList;


    public Campaign() {
    }

    public Campaign(Integer campaignID) {
        this.campaignID = campaignID;
    }

    public Campaign(Integer campaignID, String campaign, String description) {
        this.campaignID = campaignID;
        this.campaign = campaign;
        this.description = description;
    }

    public String getVideo() {
        return Video;
    }

    public void setVideo(String Video) {
        this.Video = Video;
    }

    public String getConsoleLog() {
        return ConsoleLog;
    }

    public void setConsoleLog(String ConsoleLog) {
        this.ConsoleLog = ConsoleLog;
    }

    public String getGroup1() {
        return group1;
    }

    public void setGroup1(String group1) {
        this.group1 = group1;
    }

    public String getGroup2() {
        return group2;
    }

    public void setGroup2(String group2) {
        this.group2 = group2;
    }

    public String getGroup3() {
        return group3;
    }

    public void setGroup3(String group3) {
        this.group3 = group3;
    }

    public String getCIScoreThreshold() {
        return CIScoreThreshold;
    }

    public void setCIScoreThreshold(String CIScoreThreshold) {
        this.CIScoreThreshold = CIScoreThreshold;
    }

    public String getTag() {
        return Tag;
    }

    public void setTag(String Tag) {
        this.Tag = Tag;
    }

    public String getVerbose() {
        return Verbose;
    }

    public void setVerbose(String Verbose) {
        this.Verbose = Verbose;
    }

    public String getScreenshot() {
        return Screenshot;
    }

    public void setScreenshot(String Screenshot) {
        this.Screenshot = Screenshot;
    }

    public String getPageSource() {
        return PageSource;
    }

    public void setPageSource(String PageSource) {
        this.PageSource = PageSource;
    }

    public String getRobotLog() {
        return RobotLog;
    }

    public void setRobotLog(String RobotLog) {
        this.RobotLog = RobotLog;
    }

    public String getTimeout() {
        return Timeout;
    }

    public void setTimeout(String Timeout) {
        this.Timeout = Timeout;
    }

    public String getRetries() {
        return Retries;
    }

    public void setRetries(String Retries) {
        this.Retries = Retries;
    }

    public String getPriority() {
        return Priority;
    }

    public void setPriority(String Priority) {
        this.Priority = Priority;
    }

    public String getManualExecution() {
        return ManualExecution;
    }

    public void setManualExecution(String ManualExecution) {
        this.ManualExecution = ManualExecution;
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

    public Integer getCampaignID() {
        return campaignID;
    }

    public void setCampaignID(Integer campaignID) {
        this.campaignID = campaignID;
    }

    public String getCampaign() {
        return campaign;
    }

    public void setCampaign(String campaign) {
        this.campaign = campaign;
    }

    public String getLongDescription() {
        return longDescription;
    }

    public void setLongDescription(String longDescription) {
        this.longDescription = longDescription;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @XmlTransient
    @JsonIgnore
    public List<CampaignParameter> getCampaignParameterList() {
        return campaignParameterList;
    }

    public void setCampaignParameterList(List<CampaignParameter> campaignParameterList) {
        this.campaignParameterList = campaignParameterList;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (campaignID != null ? campaignID.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof Campaign)) {
            return false;
        }
        Campaign other = (Campaign) object;
        if ((this.campaignID == null && other.campaignID != null) || (this.campaignID != null && !this.campaignID.equals(other.campaignID))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "org.cerberus.crud.entity.Campaign[ campaignID=" + campaignID + " ]";
    }

}
