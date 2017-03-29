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
package org.cerberus.crud.service.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.cerberus.crud.dao.ICampaignContentDAO;
import org.cerberus.crud.dao.ICampaignDAO;
import org.cerberus.crud.dao.ICampaignParameterDAO;
import org.cerberus.crud.entity.Campaign;
import org.cerberus.crud.entity.CampaignContent;
import org.cerberus.crud.entity.CampaignParameter;
import org.cerberus.exception.CerberusException;
import org.cerberus.crud.service.ICampaignService;
import org.cerberus.util.answer.Answer;
import org.cerberus.util.answer.AnswerItem;
import org.cerberus.util.answer.AnswerList;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
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
    public List<String> findCountries(String campaignName) throws CerberusException {
        List<String> result = new ArrayList<String>();
        List<CampaignParameter> parameters = this.findCampaignParametersByCampaignName(campaignName);

        for (CampaignParameter parameter : parameters) {
            if (parameter.getParameter().equals("COUNTRY")) {
                result.add(parameter.getValue());
            }
        }
        return result;
    }

    @Override
    public AnswerList readByCriteria(int start, int amount, String colName, String dir, String searchParameter, String individualSearch) {
        return campaignDAO.readByCriteria(start, amount, colName, dir, searchParameter, individualSearch);
    }

    @Override
    public AnswerList readByCriteria(int start, int amount, String colName, String dir, String searchParameter, Map<String, List<String>> individualSearch) {
        return campaignDAO.readByCriteria(start, amount, colName, dir, searchParameter, individualSearch);
    }

    @Override
    public AnswerItem readByKey(String key) {
        return campaignDAO.readByKey(key);
    }

    @Override
    public AnswerList<String> readDistinctValuesByCriteria(String searchParameter, Map<String, List<String>> individualSearch, String columnName) {
        return campaignDAO.readDistinctValuesByCriteria(searchParameter, individualSearch, columnName);
    }

    @Override
    public Answer create(Campaign object) {
        return campaignDAO.create(object);
    }

    @Override
    public Answer update(Campaign object) {
        return campaignDAO.update(object);
    }

    @Override
    public Answer delete(Campaign object) {
        return campaignDAO.delete(object);
    }
}
