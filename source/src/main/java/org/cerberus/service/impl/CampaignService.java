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
package org.cerberus.service.impl;

import java.util.List;
import org.cerberus.dao.ICampaignContentDAO;
import org.cerberus.dao.ICampaignDAO;
import org.cerberus.dao.ICampaignParameterDAO;
import org.cerberus.dto.TestCaseWithExecution;
import org.cerberus.entity.Campaign;
import org.cerberus.entity.CampaignContent;
import org.cerberus.entity.CampaignParameter;
import org.cerberus.exception.CerberusException;
import org.cerberus.service.ICampaignService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 *
 * @author memiks
 */
@Service
public class CampaignService implements ICampaignService {

    @Autowired
    ICampaignDAO campaignDAO;

    @Autowired
    ICampaignContentDAO campaignContentDAO;

    @Autowired
    ICampaignParameterDAO campaignParameterDAO;

    @Override
    public List<Campaign> findAll() throws CerberusException {
        return campaignDAO.findAll();
    }

    @Override
    public Campaign findCampaignByKey(Integer campaignID) throws CerberusException {
        return campaignDAO.findCampaignByKey(campaignID);
    }

    @Override
    public CampaignParameter findCampaignParameterByKey(Integer campaignParameterID) throws CerberusException {
        return campaignParameterDAO.findCampaignParameterByKey(campaignParameterID);
    }

    @Override
    public CampaignContent findCampaignContentByKey(Integer campaignContentID) throws CerberusException {
        return campaignContentDAO.findCampaignContentByKey(campaignContentID);
    }

    @Override
    public Campaign findCampaignByCampaignName(String campaign) throws CerberusException {
        return campaignDAO.findCampaignByCampaignName(campaign);
    }

    @Override
    public List<CampaignContent> findCampaignContentsByCampaignName(String campaign) throws CerberusException {
        return campaignContentDAO.findCampaignContentByCampaignName(campaign);
    }

    @Override
    public List<CampaignParameter> findCampaignParametersByCampaignName(String campaign) throws CerberusException {
        return campaignParameterDAO.findCampaignParametersByCampaign(campaign);
    }

    @Override
    public boolean updateCampaign(Campaign campaign) {
        return campaignDAO.updateCampaign(campaign);
    }

    @Override
    public boolean updateCampaignContent(CampaignContent campaignContent) {
        return campaignContentDAO.updateCampaignContent(campaignContent);
    }

    @Override
    public boolean updateCampaignParameter(CampaignParameter campaignParameter) {
        return campaignParameterDAO.updateCampaignParameter(campaignParameter);
    }

    @Override
    public boolean createCampaign(Campaign campaign) {
        return campaignDAO.createCampaign(campaign);
    }

    @Override
    public boolean createCampaignContent(CampaignContent campaignContent) {
        return campaignContentDAO.createCampaignContent(campaignContent);
    }

    @Override
    public boolean createCampaignParameter(CampaignParameter campaignParameter) {
        return campaignParameterDAO.createCampaignParameter(campaignParameter);
    }

    @Override
    public List<Campaign> findCampaignByCriteria(Integer campaignID, String campaign, String description) throws CerberusException {
        return campaignDAO.findCampaignByCriteria(campaignID, campaign, description);
    }

    @Override
    public List<CampaignContent> findCampaignContentByCriteria(String campaign, Integer campaignContentID, String testBattery) throws CerberusException {
        return campaignContentDAO.findCampaignContentByCriteria(campaign, campaignContentID, testBattery);
    }

    @Override
    public List<CampaignParameter> findCampaignParameterByCriteria(Integer campaignparameterID, String campaign, String parameter, String value) throws CerberusException {
        return campaignParameterDAO.findCampaignParameterByCriteria(campaignparameterID, campaign, parameter, value);
    }

    @Override
    public boolean deleteCampaign(Campaign campaign) {
        return campaignDAO.deleteCampaign(campaign);
    }

    @Override
    public boolean deleteCampaignContent(CampaignContent campaignContent) {
        return campaignContentDAO.deleteCampaignContent(campaignContent);
    }

    @Override
    public boolean deleteCampaignParameter(CampaignParameter campaignParameter) {
        return campaignParameterDAO.deleteCampaignParameter(campaignParameter);
    }

    @Override
    public List<TestCaseWithExecution> getCampaignTestCaseExecutionForEnvCountriesBrowserTag(String campaignName, String tag, String[] env, String[] country, String[] browser) throws CerberusException {
        return campaignDAO.getCampaignTestCaseExecutionForEnvCountriesBrowserTag(campaignName, tag, env, country, browser);
    }
}
