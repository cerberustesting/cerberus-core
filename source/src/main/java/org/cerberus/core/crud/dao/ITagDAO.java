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
import java.util.Date;
import java.util.List;
import java.util.Map;
import org.cerberus.core.crud.entity.Tag;
import org.cerberus.core.exception.CerberusException;
import org.cerberus.core.util.answer.Answer;
import org.cerberus.core.util.answer.AnswerItem;
import org.cerberus.core.util.answer.AnswerList;

/**
 * Interface that defines the public methods to manage Application data on table
 * Insert, Delete, Update, Find
 *
 * @author tbernardes
 * @version 1.0, 15/10/13
 * @since 0.9.0
 */
public interface ITagDAO {

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
     * @param campaign
     * @param startPosition
     * @param length
     * @param columnName
     * @param sort
     * @param searchParameter
     * @param individualSearch
     * @param system
     * @return
     */
    AnswerList<Tag> readByVariousByCriteria(String campaign, int startPosition, int length, String columnName, String sort, String searchParameter, Map<String, List<String>> individualSearch, List<String> system);

    /**
     *
     * @param campaigns
     * @param systems
     * @param from
     * @param to
     * @return
     */
    AnswerList<Tag> readByVarious(List<String> campaigns, List<String> systems, Date from, Date to);

    /**
     *
     * @param campaigns
     * @param group1s
     * @param group2s
     * @param group3s
     * @param environments
     * @param countries
     * @param robotDeclis
     * @param from
     * @param to
     * @return
     */
    AnswerList<Tag> readByVarious(List<String> campaigns, List<String> group1s, List<String> group2s, List<String> group3s, List<String> environments, List<String> countries, List<String> robotDeclis, Date from, Date to);

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
     * @param object
     * @return
     */
    Answer updateBrowserStackBuild(String tag, Tag object);

    /**
     *
     * @param tag
     * @param object
     * @return
     */
    Answer updateLambdatestBuild(String tag, Tag object);

    /**
     *
     * @param tag
     * @param object
     * @return
     */
    Answer updateDescription(String tag, Tag object);

    /**
     *
     * @param tag
     * @param object
     * @return
     */
    Answer updateComment(String tag, Tag object);

    /**
     *
     * @param tag
     * @param object
     * @return
     */
    Answer appendComment(String tag, Tag object);

    /**
     *
     * @param tag
     * @param object
     * @return
     */
    Answer updateXRayTestExecution(String tag, Tag object);

    /**
     *
     * @param tag
     * @param falseNegative
     * @param usrModif
     * @throws CerberusException
     */
    void updateFalseNegative(String tag, boolean falseNegative, String usrModif) throws CerberusException;

    /**
     *
     * @param tag
     * @param object
     * @return
     */
    int lockXRayTestExecution(String tag, Tag object);

    /**
     *
     * @param tag
     * @return
     */
    Answer updateDateEndQueue(Tag tag);

    /**
     *
     * @param tag
     * @return
     */
    public Answer updateDateStartExe(Tag tag);

    /**
     * Uses data of ResultSet to create object {@link Application}
     *
     * @param rs ResultSet relative to select from table Application
     * @return object {@link Application}
     * @throws SQLException when trying to get value from
     * {@link java.sql.ResultSet#getString(String)}
     * @see FactoryApplication
     */
    Tag loadFromResultSet(ResultSet rs) throws SQLException;

    /**
     *
     * @param campaign
     * @param searchParameter
     * @param individualSearch
     * @param columnName
     * @return
     */
    public AnswerList<String> readDistinctValuesByCriteria(String campaign, String searchParameter, Map<String, List<String>> individualSearch, String columnName);

}
