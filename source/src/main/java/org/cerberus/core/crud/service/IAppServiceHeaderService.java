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

import org.cerberus.core.crud.entity.AppServiceHeader;
import org.cerberus.core.exception.CerberusException;
import org.cerberus.core.util.answer.Answer;
import org.cerberus.core.util.answer.AnswerItem;
import org.cerberus.core.util.answer.AnswerList;

import java.util.List;
import java.util.Map;

/**
 * @author vertigo
 */
public interface IAppServiceHeaderService {

    /**
     * @param service
     * @param key
     * @return
     */
    AnswerItem<AppServiceHeader> readByKey(String service, String key);

    /**
     * @return
     */
    AnswerList<AppServiceHeader> readAll();

    /**
     * @param service
     * @return
     */
    AnswerList<AppServiceHeader> readByVarious(String service);

    /**
     * @param service
     * @param isActive
     * @return
     */
    AnswerList<AppServiceHeader> readByVarious(String service, boolean isActive);

    /**
     * @param startPosition
     * @param length
     * @param columnName
     * @param sort
     * @param searchParameter
     * @param individualSearch
     * @return
     */
    AnswerList<AppServiceHeader> readByCriteria(int startPosition, int length, String columnName, String sort, String searchParameter, Map<String, List<String>> individualSearch);

    /**
     * @param service
     * @param withActiveCriteria
     * @param isActive
     * @param startPosition
     * @param length
     * @param columnName
     * @param sort
     * @param searchParameter
     * @param individualSearch
     * @return
     */
    AnswerList<AppServiceHeader> readByVariousByCriteria(String service, boolean withActiveCriteria, boolean isActive, int startPosition, int length, String columnName, String sort, String searchParameter, Map<String, List<String>> individualSearch);

    /**
     * @param service
     * @param key
     * @return true is application exist or false is application does not exist
     * in database.
     */
    boolean exist(String service, String key);

    /**
     * @param object
     * @return
     */
    Answer create(AppServiceHeader object);

    /**
     * @param objectList
     * @return
     */
    Answer createList(List<AppServiceHeader> objectList);

    /**
     * @param object
     * @return
     */
    Answer delete(AppServiceHeader object);

    /**
     * @param objectList
     * @return
     */
    Answer deleteList(List<AppServiceHeader> objectList);

    /**
     * @param service
     * @param key
     * @param object
     * @return
     */
    Answer update(String service, String key, AppServiceHeader object);

    /**
     * @param answerItem
     * @return
     * @throws CerberusException
     */
    AppServiceHeader convert(AnswerItem<AppServiceHeader> answerItem) throws CerberusException;

    /**
     * @param answerList
     * @return
     * @throws CerberusException
     */
    List<AppServiceHeader> convert(AnswerList<AppServiceHeader> answerList) throws CerberusException;

    /**
     * @param answer
     * @throws CerberusException
     */
    void convert(Answer answer) throws CerberusException;

    /**
     * @param service
     * @param newList
     * @return
     */
    Answer compareListAndUpdateInsertDeleteElements(String service, List<AppServiceHeader> newList);

    /**
     * @param service
     * @param searchParameter
     * @param individualSearch
     * @param columnName
     * @return
     */
    AnswerList<String> readDistinctValuesByCriteria(String service, String searchParameter, Map<String, List<String>> individualSearch, String columnName);

    /**
     *
     * @param headerList
     * @param newHeader
     * @return
     */
    List<AppServiceHeader> addIfNotExist(List<AppServiceHeader> headerList, AppServiceHeader newHeader);

}
