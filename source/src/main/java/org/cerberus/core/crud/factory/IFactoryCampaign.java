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
package org.cerberus.core.crud.factory;

import java.sql.Timestamp;
import org.cerberus.core.crud.entity.Campaign;

/**
 *
 * @author memiks
 */
public interface IFactoryCampaign {

    /**
     * @param campaignID Technical ID of the Campaign.
     * @param campaign Id name of the Campaign
     * @param description Description of the Campaign.
     * @param CIScoreThreshold
     * @param verbose
     * @param tag
     * @param video
     * @param PageSource
     * @param longDescription
     * @param ConsoleLog
     * @param RobotLog
     * @param UsrCreated
     * @param screenshot
     * @param ManualExecution
     * @param UsrModif
     * @param Timeout
     * @param group1
     * @param group2
     * @param group3
     * @param Priority
     * @param Retries
     * @param DateModif
     * @param DateCreated
     * @return Campaign Object
     */
    Campaign create(Integer campaignID, String campaign,
            String CIScoreThreshold,
            String tag, String verbose, String screenshot, String video, String PageSource, String RobotLog, String ConsoleLog, String Timeout, String Retries, String Priority, String ManualExecution,
            String description, String longDescription, String group1, String group2, String group3,
            String UsrCreated, Timestamp DateCreated, String UsrModif, Timestamp DateModif);
}
