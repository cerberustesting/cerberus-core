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
package org.cerberus.core.crud.dao;

import java.util.List;

import org.cerberus.core.crud.entity.CampaignParameter;
import org.cerberus.core.exception.CerberusException;
import org.cerberus.core.util.answer.Answer;
import org.cerberus.core.util.answer.AnswerList;

/**
 * @author memiks
 */
public interface ICampaignParameterDAO {

    /**
     *
     * @return
     * @throws CerberusException
     */
    List<CampaignParameter> findAll() throws CerberusException;

    /**
     *
     * @param campaignparameterID
     * @return
     * @throws CerberusException
     */
    CampaignParameter findCampaignParameterByKey(Integer campaignparameterID) throws CerberusException;

    /**
     *
     * @param campaign
     * @return
     * @throws CerberusException
     */
    List<CampaignParameter> findCampaignParametersByCampaign(String campaign) throws CerberusException;

    /**
     *
     * @param campaignParameter
     * @return
     */
    boolean updateCampaignParameter(CampaignParameter campaignParameter);

    /**
     *
     * @param campaignParameter
     * @return
     */
    boolean createCampaignParameter(CampaignParameter campaignParameter);

    /**
     *
     * @param campaignParameter
     * @return
     */
    boolean deleteCampaignParameter(CampaignParameter campaignParameter);

    /**
     *
     * @param campaignparameterID
     * @param campaign
     * @param parameter
     * @param value
     * @return
     * @throws CerberusException
     */
    List<CampaignParameter> findCampaignParameterByCriteria(Integer campaignparameterID, String campaign, String parameter, String value) throws CerberusException;

    /**
     *
     * @param campaign
     * @param startPosition
     * @param length
     * @param columnName
     * @param sort
     * @param searchParameter
     * @param string
     * @return
     */
    AnswerList<CampaignParameter> readByCampaignByCriteria(String campaign, int startPosition, int length, String columnName, String sort, String searchParameter, String string);

    /**
     *
     * @param campaign
     * @return
     */
    AnswerList<CampaignParameter> readByCampaign(String campaign);

    /**
     *
     * @param key
     * @return
     */
    Answer deleteByCampaign(String key);

    /**
     *
     * @param object
     * @return
     */
    Answer delete(CampaignParameter object);

    /**
     *
     * @param object
     * @return
     */
    Answer update(CampaignParameter object);

    /**
     *
     * @param object
     * @return
     */
    Answer create(CampaignParameter object);
}
