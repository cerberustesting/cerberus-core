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

import org.cerberus.core.crud.entity.TagStatistic;
import org.cerberus.core.util.answer.Answer;
import org.cerberus.core.util.answer.AnswerList;

import java.util.List;
import java.util.Map;

/**
 * Interface that defines the public methods to manage Application data on table
 * Create Read Update Delete
 *
 * @author lhimpens
 */
public interface ITagStatisticDAO {

    /**
     * Insert a unique line of TagStatistic in database
     * @param object
     * @return
     */
    Answer create(TagStatistic object);

    /**
     * Insert a list of tagStatistics in only one INSERT statement
     * @param tagStatistics
     * @return
     */
    Answer createWithMap(Map<String, TagStatistic> tagStatistics);

    /**
     * Get a TagStatistic object from database
     * @param object
     * @return
     */
    Answer read(TagStatistic object);

    AnswerList<TagStatistic> readByCriteria(List<String> systems, List<String> applications, List<String> groups1, String minDate, String maxDate);

    AnswerList<TagStatistic> readByCriteria(String campaign, List<String> countries, List<String> environments, String minDate, String maxDate);

    /**
     * Get a TagStatistics list by tag from database
     * @param tag
     * @return
     */
    AnswerList<TagStatistic> readByTag(String tag);

    /**
     * Update a TagStatistic
     * @param object
     * @return
     */
    Answer update(TagStatistic object);

    /**
     * Delete a TagStatistic object in database
     * @param tag
     * @param object
     * @return
     */
    Answer delete(String tag, TagStatistic object);
}
