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

import java.util.List;
import java.util.Map;
import org.cerberus.core.crud.entity.Campaign;
import org.cerberus.core.crud.entity.CampaignParameter;
import org.cerberus.core.exception.CerberusException;
import org.cerberus.core.util.answer.Answer;
import org.cerberus.core.util.answer.AnswerItem;
import org.cerberus.core.util.answer.AnswerList;

/**
 * @author memiks
 */
public interface ICampaignService {

    List<CampaignParameter> findCampaignParametersByCampaignName(String campaign) throws CerberusException;

    /**
     * Get the {@link Campaign} List of the given {@link System} with the given
     * Criteria
     *
     * @param startPosition the start index to look for
     * @param length the number of {@link Campaign} to get
     * @param columnName the Column name to sort
     * @param sort
     * @param searchParameter the string to search in the {@link Campaign}
     * @param individualSearch the string to search for each column
     * @return
     */
    AnswerList<Campaign> readByCriteria(int startPosition, int length, String columnName, String sort, String searchParameter, Map<String, List<String>> individualSearch);

    /**
     * Get the {@link Campaign} with the given {@link System} and the given key
     *
     * @param key the key of the {@link Campaign}
     * @return
     */
    AnswerItem<Campaign> readByKey(String key);

    /**
     * Get the {@link Campaign} with the given {@link System} and the given key
     *
     * @param key the key of the {@link Campaign}
     * @return
     */
    AnswerItem<Campaign> readByKeyTech(int key);

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
     * @param originalCampaign
     * @param object the {@link Campaign} to Update
     * @return {@link AnswerItem}
     */
    Answer update(String originalCampaign, Campaign object);

    /**
     * @param object the {@link Campaign} to Delete
     * @return {@link AnswerItem}
     */
    Answer delete(Campaign object);

    /**
     *
     * @param answerItem
     * @return
     * @throws CerberusException
     */
    Campaign convert(AnswerItem<Campaign> answerItem) throws CerberusException;

    /**
     *
     * @param answerList
     * @return
     * @throws CerberusException
     */
    List<Campaign> convert(AnswerList<Campaign> answerList) throws CerberusException;

    /**
     *
     * @param answer
     * @throws CerberusException
     */
    void convert(Answer answer) throws CerberusException;

}
