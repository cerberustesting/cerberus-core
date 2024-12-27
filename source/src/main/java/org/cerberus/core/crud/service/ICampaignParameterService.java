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
package org.cerberus.core.crud.service;

import org.cerberus.core.crud.entity.CampaignParameter;
import org.cerberus.core.exception.CerberusException;
import org.cerberus.core.util.answer.Answer;
import org.cerberus.core.util.answer.AnswerItem;
import org.cerberus.core.util.answer.AnswerList;

import java.util.List;
import java.util.Map;

/**
 *
 * @author cerberus
 */
public interface ICampaignParameterService {

    AnswerList<CampaignParameter> readByCampaignByCriteria(String campaign, int startPosition, int length, String columnName, String sort, String searchParameter, String string);

    AnswerList<CampaignParameter> readByCampaign(String campaign);

    AnswerItem<Map<String, List<String>>> parseParametersByCampaign(String campaignName);

    Answer deleteByCampaign (String key);

    Answer delete (CampaignParameter object);

    Answer update (CampaignParameter object);

    Answer create (CampaignParameter object);

    Answer createList(List<CampaignParameter> objectList);

    Answer deleteList(List<CampaignParameter> objectList);

    Answer compareListAndUpdateInsertDeleteElements(String campaign, List<CampaignParameter> newList);

    /**
     *
     * @param answerItem
     * @return
     * @throws CerberusException
     */
    CampaignParameter convert(AnswerItem<CampaignParameter> answerItem) throws CerberusException;

    /**
     *
     * @param answerList
     * @return
     * @throws CerberusException
     */
    List<CampaignParameter> convert(AnswerList<CampaignParameter> answerList) throws CerberusException;

    }
