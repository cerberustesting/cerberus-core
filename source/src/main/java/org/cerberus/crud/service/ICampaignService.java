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
package org.cerberus.crud.service;

import java.util.List;
import java.util.Map;
import org.cerberus.crud.entity.Campaign;
import org.cerberus.crud.entity.CampaignContent;
import org.cerberus.crud.entity.CampaignParameter;
import org.cerberus.exception.CerberusException;
import org.cerberus.util.answer.Answer;
import org.cerberus.util.answer.AnswerItem;
import org.cerberus.util.answer.AnswerList;

/**
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


    List<String> findCountries(String campaignName) throws CerberusException;

    public AnswerList readByCriteria(int start, int amount, String colName, String dir, String searchParameter, String individualSearch);

    /**
     * Get the {@link Campaign} List of the given {@link System} with the given
     * Criteria
     *
     * @param system           the {@link System} To look for
     * @param startPosition    the start index to look for
     * @param length           the number of {@link Campaign} to get
     * @param columnName       the Column name to sort
     * @param sort
     * @param searchParameter  the string to search in the {@link Campaign}
     * @param individualSearch the string to search for each column
     * @return
     */
    AnswerList readByCriteria(int startPosition, int length, String columnName, String sort, String searchParameter, Map<String, List<String>> individualSearch);

    /**
     * Get the {@link Campaign} with the given {@link System} and the given key
     *
     * @param key the key of the {@link Campaign}
     * @return
     */
    AnswerItem readByKey(String key);

    /**
     * @param searchParameter
     * @param individualSearch
     * @param columnName
     * @return
     */
    public AnswerList<String> readDistinctValuesByCriteria(String searchParameter, Map<String, List<String>> individualSearch, String columnName);

    /**
     * @param object the {@link Campaign} to Create
     * @return {@link AnswerItem}
     */
    Answer create(Campaign object);

    /**
     * @param campaign
     * @param object the {@link Campaign} to Update
     * @return {@link AnswerItem}
     */
    Answer update(Campaign object);

    /**
     * @param object the {@link Campaign} to Delete
     * @return {@link AnswerItem}
     */
    Answer delete(Campaign object);
}
