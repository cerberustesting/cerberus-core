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
package org.cerberus.core.crud.service;

import org.cerberus.core.crud.entity.Tag;
import org.cerberus.core.crud.entity.TagStatistic;
import org.cerberus.core.crud.entity.TestCaseExecution;
import org.cerberus.core.exception.CerberusException;
import org.cerberus.core.util.answer.Answer;
import org.cerberus.core.util.answer.AnswerList;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;
import java.util.Map;
public interface ITagStatisticService {

    /**
     * @param tag
     * @return AnswerList that contains data from database
     */
    AnswerList<TagStatistic> readByTag(String tag);
    Answer createWithMap(Map<String, TagStatistic> map);
    AnswerList<TagStatistic> readByCriteria(List<String> systems, List<String> applications, List<String> groups1, String minDate, String maxDate);

    /**
     * Initialize TagStatistics objects
     * @param tag
     * @param executions
     * @return Map with TagStatistics initialized
     */
    Map<String, TagStatistic> initTagStatistics(Tag tag, List<TestCaseExecution> executions);

    /**
     * Populate TagStatistics objects with aggregated data
     * @param tagStatistics
     * @param executions
     * @param tag
     */
    void populateTagStatisticsMap(Map<String, TagStatistic> tagStatistics, List<TestCaseExecution> executions, Tag tag);

    Map<String, Map<String, JSONObject>> createMapAggregateByTag(List<TagStatistic> tagStatistics) throws JSONException;
    Map<String, JSONObject> createMapAggregateByCampaign(Map<String, Map<String, JSONObject>> aggregateByTag) throws JSONException;
    List<String> getSystemsAllowedForUser(String user) throws CerberusException;
    List<String> getApplicationsSystems(List<String> systems);
    String formatDateForDb(String date);

}
