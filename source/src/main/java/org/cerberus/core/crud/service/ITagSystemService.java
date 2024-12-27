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

import org.cerberus.core.crud.entity.TagSystem;
import org.cerberus.core.exception.CerberusException;
import org.cerberus.core.util.answer.Answer;
import org.cerberus.core.util.answer.AnswerItem;
import org.cerberus.core.util.answer.AnswerList;

/**
 *
 * @author vertigo
 */
public interface ITagSystemService {

    List<String> getTagSystemCache();

    /**
     * Remove all cache entries
     */
    void purgeTagSystemCache();

    /**
     *
     * @param tag
     * @param system
     * @return
     */
    AnswerItem<TagSystem> readByKey(String tag, String system);

    /**
     *
     * @return
     */
    AnswerList<TagSystem> readAll();

    /**
     *
     * @param system
     * @return
     */
    AnswerList readBySystem(String system);

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
     * @param system
     * @param startPosition
     * @param length
     * @param columnName
     * @param sort
     * @param searchParameter
     * @param individualSearch
     * @return
     */
    AnswerList readByVariousByCriteria(String system, int startPosition, int length, String columnName, String sort, String searchParameter, Map<String, List<String>> individualSearch);

    /**
     *
     * @param tag
     * @param system
     * @return true is application exist or false is application does not exist
     * in database.
     */
    boolean exist(String tag, String system);

    /**
     *
     * @param tag
     * @param system
     * @param user
     * @return
     */
    Answer createIfNotExist(String tag, String system, String user);

    /**
     *
     * @param object
     * @return
     */
    Answer create(TagSystem object);

    /**
     *
     * @param object
     * @return
     */
    Answer delete(TagSystem object);

    /**
     *
     * @param Tag
     * @param system
     * @param object
     * @return
     */
    Answer update(String Tag, String system, TagSystem object);

    /**
     *
     * @param answerItem
     * @return
     * @throws CerberusException
     */
    TagSystem convert(AnswerItem<TagSystem> answerItem) throws CerberusException;

    /**
     *
     * @param answerList
     * @return
     * @throws CerberusException
     */
    List<TagSystem> convert(AnswerList<TagSystem> answerList) throws CerberusException;

    /**
     *
     * @param answer
     * @throws CerberusException
     */
    void convert(Answer answer) throws CerberusException;

    /**
     *
     * @param searchParameter
     * @param individualSearch
     * @param columnName
     * @return
     */
    AnswerList<String> readDistinctValuesByCriteria(String searchParameter, Map<String, List<String>> individualSearch, String columnName);
}
