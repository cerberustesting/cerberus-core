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
package org.cerberus.crud.dao;

import java.util.List;
import org.cerberus.dto.TestCaseWithExecution;
import org.cerberus.crud.entity.Campaign;
import org.cerberus.exception.CerberusException;
import org.cerberus.util.answer.AnswerList;

/**
 *
 * @author memiks
 */
public interface ICampaignDAO {

    List<Campaign> findAll() throws CerberusException;

    Campaign findCampaignByKey(Integer campaignID) throws CerberusException;

    Campaign findCampaignByCampaignName(String campaign) throws CerberusException;

    boolean updateCampaign(Campaign campaign);

    boolean createCampaign(Campaign campaign);

    boolean deleteCampaign(Campaign campaign);

    List<Campaign> findCampaignByCriteria(Integer campaignID, String campaign, String description) throws CerberusException;

    List<TestCaseWithExecution> getCampaignTestCaseExecutionForEnvCountriesBrowserTag(String tag) throws CerberusException;

    public AnswerList readByCriteria(int start, int amount, String colName, String dir, String searchParameter, String individualSearch);
}
