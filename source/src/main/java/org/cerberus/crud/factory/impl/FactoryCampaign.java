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
package org.cerberus.crud.factory.impl;

import java.sql.Timestamp;
import org.cerberus.crud.entity.Campaign;
import org.cerberus.crud.factory.IFactoryCampaign;
import org.springframework.stereotype.Service;

/**
 *
 * @author memiks
 */
@Service
public class FactoryCampaign implements IFactoryCampaign {

    @Override
    public Campaign create(Integer campaignID, String campaign,
            String distribList, String notifyStartTagExecution, String notifyEndTagExecution,
            String SlackNotifyStartTagExecution, String SlackNotifyEndTagExecution, String SlackWebhook, String SlackChannel,
            String CIScoreThreshold,
            String Tag, String Verbose, String Screenshot, String PageSource, String RobotLog, String Timeout, String Retries, String Priority, String ManualExecution,
            String description, String longDescription, String group1, String group2, String group3,
            String UsrCreated, Timestamp DateCreated, String UsrModif, Timestamp DateModif) {
        Campaign newObject = new Campaign();
        newObject.setCampaignID(campaignID);
        newObject.setCampaign(campaign);
        newObject.setDistribList(distribList);
        newObject.setNotifyStartTagExecution(notifyStartTagExecution);
        newObject.setNotifyEndTagExecution(notifyEndTagExecution);
        newObject.setSlackNotifyStartTagExecution(SlackNotifyStartTagExecution);
        newObject.setSlackNotifyEndTagExecution(SlackNotifyEndTagExecution);
        newObject.setSlackWebhook(SlackWebhook);
        newObject.setSlackChannel(SlackChannel);
        newObject.setCIScoreThreshold(CIScoreThreshold);
        newObject.setTag(Tag);
        newObject.setVerbose(Verbose);
        newObject.setScreenshot(Screenshot);
        newObject.setPageSource(PageSource);
        newObject.setRobotLog(RobotLog);
        newObject.setTimeout(Timeout);
        newObject.setRetries(Retries);
        newObject.setPriority(Priority);
        newObject.setManualExecution(ManualExecution);
        newObject.setDescription(description);
        newObject.setLongDescription(longDescription);
        newObject.setGroup1(group1);
        newObject.setGroup2(group2);
        newObject.setGroup3(group3);
        newObject.setUsrCreated(UsrCreated);
        newObject.setDateCreated(DateCreated);
        newObject.setUsrModif(UsrModif);
        newObject.setDateModif(DateModif);

        return newObject;
    }

}
