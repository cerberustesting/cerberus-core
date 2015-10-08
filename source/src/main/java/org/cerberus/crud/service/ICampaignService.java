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
package org.cerberus.crud.service;

import java.util.HashMap;
import java.util.List;
import org.cerberus.dto.TestCaseWithExecution;
import org.cerberus.crud.entity.Campaign;
import org.cerberus.crud.entity.CampaignContent;
import org.cerberus.crud.entity.CampaignParameter;
import org.cerberus.crud.entity.TestCaseExecution;
import org.cerberus.exception.CerberusException;

/**
 *
 * @author memiks
 */
public interface ICampaignService {

    List<Campaign> findAll() throws CerberusException;

    Campaign findCampaignByKey(Integer campaignID) throws CerberusException;

    CampaignParameter findCampaignParameterByKey(Integer campaignParameterID) throws CerberusException;

    CampaignContent findCampaignContentByKey(Integer campaignContentID) throws CerberusException;

    Campaign findCampaignByCampaignName(String campaign) throws CerberusException;

    List<CampaignContent> findCampaignContentsByCampaignName(String campaign) throws CerberusException;

    List<CampaignParameter> findCampaignParametersByCampaignName(String campaign) throws CerberusException;

    boolean updateCampaign(Campaign campaign);

    boolean updateCampaignContent(CampaignContent campaignContent);

    boolean updateCampaignParameter(CampaignParameter campaignParameter);

    boolean createCampaign(Campaign campaign);

    boolean createCampaignContent(CampaignContent campaignContent);

    boolean createCampaignParameter(CampaignParameter campaignParameter);

    boolean deleteCampaign(Campaign campaign);

    boolean deleteCampaignContent(CampaignContent campaignContent);

    boolean deleteCampaignParameter(CampaignParameter campaignParameter);

    List<Campaign> findCampaignByCriteria(Integer campaignID, String campaign, String description) throws CerberusException;

    List<CampaignContent> findCampaignContentByCriteria(String campaign, Integer campaignContentID, String testBattery) throws CerberusException;

    List<CampaignParameter> findCampaignParameterByCriteria(Integer campaignparameterID, String campaign, String parameter, String value) throws CerberusException;

    /**
     *
     * @param tag
     * @return
     * @throws CerberusException
     */
    List<TestCaseWithExecution> getCampaignTestCaseExecutionForEnvCountriesBrowserTag(String tag) throws CerberusException;
    
    List<String> findCountries(String campaignName) throws CerberusException;
}
