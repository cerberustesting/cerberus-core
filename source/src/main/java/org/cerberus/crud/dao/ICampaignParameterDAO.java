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
import org.cerberus.crud.entity.CampaignParameter;
import org.cerberus.exception.CerberusException;
import org.cerberus.util.answer.Answer;
import org.cerberus.util.answer.AnswerList;

/**
 *
 * @author memiks
 */
public interface ICampaignParameterDAO {

    List<CampaignParameter> findAll() throws CerberusException;

    CampaignParameter findCampaignParameterByKey(Integer campaignparameterID) throws CerberusException;

    List<CampaignParameter> findCampaignParametersByCampaign(String campaign) throws CerberusException;

    boolean updateCampaignParameter(CampaignParameter campaignParameter);

    boolean createCampaignParameter(CampaignParameter campaignParameter);

    boolean deleteCampaignParameter(CampaignParameter campaignParameter);

    List<CampaignParameter> findCampaignParameterByCriteria(Integer campaignparameterID, String campaign, String parameter, String value) throws CerberusException;

    AnswerList readByCampaignByCriteria(String campaign, int startPosition, int length, String columnName, String sort, String searchParameter, String string);

    AnswerList readByCampaign(String campaign);

    Answer deleteByCampaign(String key);

    Answer delete(CampaignParameter object);

    Answer update(CampaignParameter object);

    Answer create(CampaignParameter object);
}
