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

import java.sql.Timestamp;
import java.util.Date;
import java.util.List;
import java.util.Map;
import org.cerberus.core.crud.entity.Tag;
import org.cerberus.core.exception.CerberusException;
import org.cerberus.core.util.answer.Answer;
import org.cerberus.core.util.answer.AnswerItem;
import org.cerberus.core.util.answer.AnswerList;
import org.json.JSONArray;

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
    AnswerList<Tag> readByCampaign(String campaign);

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
     * @param from
     * @param group2s
     * @param environments
     * @param group3s
     * @param robotDeclis
     * @param countries
     * @param to
     * @return
     */
    AnswerList<Tag> readByVarious(List<String> campaigns, List<String> group1s, List<String> group2s, List<String> group3s, List<String> environments, List<String> countries, List<String> robotDeclis, Date from, Date to);

    /**
     *
     * @param startPosition
     * @param length
     * @param columnName
     * @param sort
     * @param searchParameter
     * @param individualSearch
     * @param system
     * @return
     */
    AnswerList<Tag> readByCriteria(int startPosition, int length, String columnName, String sort, String searchParameter, Map<String, List<String>> individualSearch, List<String> system);

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
    AnswerList<Tag> readByVariousByCriteria(String campaign, int startPosition, int length, String columnName, String sort, String searchParameter, Map<String, List<String>> individualSearch);

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
     * When the end of the tag execution occurs, that method calculate the CI
     * score result and trigger the corresponding event
     *
     * @param tag
     * @return
     */
    Answer updateEndOfQueueData(String tag);

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
     * That method update the XRay reference to the real data coming from JIRA
     * Xray.
     *
     * @param tag
     * @param object
     * @return
     */
    Answer updateXRayTestExecution(String tag, Tag object);

    /**
     * Set Tag to an Execution
     *
     * @param tag :
     * @param falseNegative : value of falseNegative.
     * @param usrModif
     * @throws CerberusException when exception occur during the update.
     */
    void updateFalseNegative(String tag, boolean falseNegative, String usrModif) throws CerberusException;

    /**
     * Set Tag to an Execution
     *
     * @param tag :
     * @param usrModif
     * @return 
     * @throws CerberusException when exception occur during the update.
     */
    AnswerItem<Integer> cancelAllExecutions(String tag, String usrModif) throws CerberusException;

    /**
     * Set Tag to an Execution
     *
     * @param tag :
     * @param usrModif
     * @return 
     * @throws CerberusException when exception occur during the update.
     */
    AnswerItem<Integer> pauseAllExecutions(String tag, String usrModif) throws CerberusException;

    /**
     * Set Tag to an Execution
     *
     * @param tag :
     * @param usrModif
     * @return 
     * @throws CerberusException when exception occur during the update.
     */
    AnswerItem<Integer> resumeAllExecutions(String tag, String usrModif) throws CerberusException;

    /**
     * That method update the XRay reference to PENDING.
     *
     * @param tag
     * @param object
     * @return 1 if the Update occurs and 0 in case the tag was already updated
     * before.
     */
    int lockXRayTestExecution(String tag, Tag object);

    /**
     *
     * @param tag
     * @param campaign
     * @param user
     * @param reqEnvironmentList
     * @param reqCountryList
     * @return
     */
    Answer createAuto(String tag, String campaign, String user, JSONArray reqEnvironmentList, JSONArray reqCountryList);

    /**
     * will enrich the tag with any external cloud service provider buildId.
     *
     * @param provider cloud provider taken from
     * TestCaseExecution.ROBOTPROVIDER_* value
     * @param system
     * @param tagS
     * @param user
     * @param pass
     * @return
     */
    String enrichTagWithCloudProviderBuild(String provider, String system, String tagS, String user, String pass);

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

    /**
     * After every execution finished, <br>
     * if the execution has a tag that has a campaign associated  <br>
     * and no more executions are in the queue, <br>
     * we trigger : <br>
     * 1/ The update of the EndExeQueue of the tag <br>
     * 2/ The calculation of main KPI on the campaign <br>
     *
     * @param tag
     * @throws org.cerberus.core.exception.CerberusException
     */
    void manageCampaignEndOfExecution(String tag) throws CerberusException;

    /**
     * At the beginning of an execution, <br>
     * if the execution has a tag <br>
     * we trigger : <br>
     * 1/ The update of the Execution Start of the tag <br>
     *
     * @param tag
     * @throws org.cerberus.core.exception.CerberusException
     */
    void manageCampaignStartOfExecution(String tag, Timestamp startOfExecution) throws CerberusException;

    /**
     * Format in a nice way the various Control Status values. <br>
     * ex : 10 Executions(s) - 5 KO - 2 FA - 3 NE<br>
     * All 0 control status are omitted.
     *
     * @param tag
     * @return
     */
    String formatResult(Tag tag);
}
