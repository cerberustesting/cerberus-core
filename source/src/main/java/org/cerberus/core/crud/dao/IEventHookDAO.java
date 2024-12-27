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

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import org.cerberus.core.crud.entity.EventHook;
import org.cerberus.core.util.answer.Answer;
import org.cerberus.core.util.answer.AnswerItem;
import org.cerberus.core.util.answer.AnswerList;

/**
 * Interface that defines the public methods to manage Event Hook table Insert,
 * Delete, Update, Find
 *
 * @author vertigo17
 */
public interface IEventHookDAO {

    /**
     *
     * @param id
     * @return
     */
    AnswerItem<EventHook> readByKey(Integer id);

    /**
     *
     * @param eventReference
     * @param objectKey1
     * @param activeOnly
     * @param startPosition
     * @param length
     * @param columnName
     * @param sort
     * @param searchParameter
     * @param individualSearch
     * @return
     */
    AnswerList<EventHook> readByEventReferenceByCriteria(List<String> eventReference, List<String> objectKey1, boolean activeOnly, int startPosition, int length, String columnName, String sort, String searchParameter, Map<String, List<String>> individualSearch);

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
     * @param object
     * @return
     */
    Answer update(EventHook object);

    /**
     * Uses data of ResultSet to create object {@link Label}
     *
     * @param rs ResultSet relative to select from table Label
     * @return object {@link Label}
     * @throws SQLException when trying to get value from
     * {@link java.sql.ResultSet#getString(String)}
     * @see FactoryLabel
     */
    EventHook loadFromResultSet(ResultSet rs) throws SQLException;

    /**
     *
     * @param system
     * @param searchParameter
     * @param individualSearch
     * @param columnName
     * @return
     */
    public AnswerList<String> readDistinctValuesByCriteria(String system, String searchParameter, Map<String, List<String>> individualSearch, String columnName);

}
