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
import org.cerberus.core.crud.entity.EventHook;
import org.cerberus.core.exception.CerberusException;
import org.cerberus.core.util.answer.Answer;
import org.cerberus.core.util.answer.AnswerItem;
import org.cerberus.core.util.answer.AnswerList;

/**
 *
 * @author vertigo17
 */
public interface IEventHookService {

    /**
     *
     * @param id
     * @return
     */
    AnswerItem<EventHook> readByKey(Integer id);

    /**
     *
     * @param eventReference
     * @return
     */
    AnswerList<EventHook> readByEventReference(List<String> eventReference);

    /**
     *
     * @param eventReference
     * @param objectKey1
     * @return
     */
    AnswerList<EventHook> readByEventReference(List<String> eventReference, List<String> objectKey1);

    /**
     *
     * @param campaign
     * @return
     */
    AnswerList<EventHook> readByCampaign(String campaign);

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
    AnswerList<EventHook> readByCriteria(int startPosition, int length, String columnName, String sort, String searchParameter, Map<String, List<String>> individualSearch);

    /**
     *
     * @param eventReference
     * @param strictSystemFilter
     * @param startPosition
     * @param length
     * @param columnName
     * @param sort
     * @param searchParameter
     * @param individualSearch
     * @return
     */
    AnswerList<EventHook> readByEventReferenceByCriteria(List<String> eventReference, boolean strictSystemFilter, int startPosition, int length, String columnName, String sort, String searchParameter, Map<String, List<String>> individualSearch);

    /**
     *
     * @param id
     * @return true is label exist or false is label does not exist in database.
     */
    boolean exist(Integer id);

    /**
     *
     * @param object
     * @return
     */
    Answer create(EventHook object);

    /**
     *
     * @param object
     * @return
     */
    Answer delete(EventHook object);

    /**
     *
     * @param campaign
     * @return
     */
    public Answer deleteBycampaign(String campaign);
    
    /**
     *
     * @param object
     * @return
     */
    Answer update(EventHook object);

    /**
     *
     * @param answerItem
     * @return
     * @throws CerberusException
     */
    EventHook convert(AnswerItem<EventHook> answerItem) throws CerberusException;

    /**
     *
     * @param answerList
     * @return
     * @throws CerberusException
     */
    List<EventHook> convert(AnswerList<EventHook> answerList) throws CerberusException;

    /**
     *
     * @param answer
     * @throws CerberusException
     */
    void convert(Answer answer) throws CerberusException;

    /**
     *
     * @param system
     * @param searchParameter
     * @param individualSearch
     * @param columnName
     * @return
     */
    public AnswerList<String> readDistinctValuesByCriteria(String system, String searchParameter, Map<String, List<String>> individualSearch, String columnName);

    /**
     *
     * @param objectList
     * @return
     */
    public Answer deleteList(List<EventHook> objectList);

    /**
     *
     * @param objectList
     * @return
     */
    public Answer createList(List<EventHook> objectList);

    /**
     *
     * @param campaign
     * @param newList
     * @return
     */
    public Answer compareListAndUpdateInsertDeleteElements(String campaign, List<EventHook> newList);
}
