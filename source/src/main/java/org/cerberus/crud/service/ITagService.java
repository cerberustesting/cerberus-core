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

import java.sql.Timestamp;
import java.util.List;
import java.util.Map;

import org.cerberus.crud.entity.Tag;
import org.cerberus.exception.CerberusException;
import org.cerberus.util.answer.Answer;
import org.cerberus.util.answer.AnswerItem;
import org.cerberus.util.answer.AnswerList;

/**
 *
 * @author vertigo
 */
public interface ITagService {

    /**
     *
     * @param tag
     * @return
     */
    AnswerItem<Tag> readByKey(String tag);

    /**
     *
     * @param id
     * @return
     */
    AnswerItem<Tag> readByKeyTech(long id);

    /**
     *
     * @return
     */
    AnswerList<Tag> readAll();

    /**
     *
     * @param campaign
     * @return
     */
    AnswerList readByCampaign(String campaign);

    /**
     *
     * @param startPosition
     * @param length
     * @param columnName
     * @param sort
     * @param searchParameter
     * @param individualSearch
     * @return
     */
    AnswerList readByCriteria(int startPosition, int length, String columnName, String sort, String searchParameter, Map<String, List<String>> individualSearch);

    /**
     *
     * @param campaign
     * @param startPosition
     * @param length
     * @param columnName
     * @param sort
     * @param searchParameter
     * @param individualSearch
     * @return
     */
    AnswerList readByVariousByCriteria(String campaign, int startPosition, int length, String columnName, String sort, String searchParameter, Map<String, List<String>> individualSearch);

    /**
     *
     * @param tag
     * @return true is application exist or false is application does not exist
     * in database.
     */
    boolean exist(String tag);

    /**
     *
     * @param object
     * @return
     */
    Answer create(Tag object);

    /**
     *
     * @param object
     * @return
     */
    Answer delete(Tag object);

    /**
     *
     * @param tag
     * @param object
     * @return
     */
    Answer update(String tag, Tag object);

    /**
     *
     * @param tag
     * @param newDate
     * @return
     */
    Answer updateDateEndQueue(String tag, Timestamp newDate);

    /**
     *
     * @param tag
     * @param campaign
     * @param user
     * @return
     */
    Answer createAuto(String tag, String campaign, String user);

    /**
     *
     * @param answerItem
     * @return
     * @throws CerberusException
     */
    Tag convert(AnswerItem<Tag> answerItem) throws CerberusException;

    /**
     *
     * @param answerList
     * @return
     * @throws CerberusException
     */
    List<Tag> convert(AnswerList<Tag> answerList) throws CerberusException;

    /**
     *
     * @param answer
     * @throws CerberusException
     */
    void convert(Answer answer) throws CerberusException;

    /**
     *
     * @param campaign
     * @param searchParameter
     * @param individualSearch
     * @param columnName
     * @return
     */
    AnswerList<String> readDistinctValuesByCriteria(String campaign, String searchParameter, Map<String, List<String>> individualSearch, String columnName);
}
