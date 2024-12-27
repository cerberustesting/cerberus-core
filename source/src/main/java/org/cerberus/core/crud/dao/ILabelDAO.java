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

import org.cerberus.core.crud.entity.Label;
import org.cerberus.core.util.answer.Answer;
import org.cerberus.core.util.answer.AnswerItem;
import org.cerberus.core.util.answer.AnswerList;

/**
 * Interface that defines the public methods to manage Label data on table
 * Insert, Delete, Update, Find
 *
 * @author bcivel
 */
public interface ILabelDAO {

    /**
     *
     * @param id
     * @return
     */
    AnswerItem<Label> readByKey(Integer id);

    /**
     *
     * @param system
     * @param strictSystemFilter
     * @param type
     * @param startPosition
     * @param length
     * @param columnName
     * @param sort
     * @param searchParameter
     * @param individualSearch
     * @return
     */
    AnswerList<Label> readBySystemByCriteria(List<String> system, boolean strictSystemFilter, List<String> type, int startPosition, int length, String columnName, String sort, String searchParameter, Map<String, List<String>> individualSearch);

    /**
     *
     * @return
     */
    AnswerList<Label> readAllLinks();

    /**
     *
     * @param object
     * @return
     */
    Answer create(Label object);

    /**
     *
     * @param object
     * @return
     */
    Answer delete(Label object);

    /**
     *
     * @param object
     * @return
     */
    Answer update(Label object);

    /**
     * Uses data of ResultSet to create object {@link Label}
     *
     * @param rs ResultSet relative to select from table Label
     * @return object {@link Label}
     * @throws SQLException when trying to get value from
     * {@link java.sql.ResultSet#getString(String)}
     * @see FactoryLabel
     */
    Label loadFromResultSet(ResultSet rs) throws SQLException;

    /**
     *
     * @param systems
     * @param searchParameter
     * @param individualSearch
     * @param columnName
     * @return
     */
    public AnswerList<String> readDistinctValuesByCriteria(List<String> systems, String searchParameter, Map<String, List<String>> individualSearch, String columnName);

}
