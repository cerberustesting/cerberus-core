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
package org.cerberus.core.crud.service.impl;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.cerberus.core.crud.dao.ITagStatisticDAO;
import org.cerberus.core.crud.entity.Tag;
import org.cerberus.core.crud.entity.TagStatistic;
import org.cerberus.core.crud.entity.TestCaseExecution;
import org.cerberus.core.crud.service.ICampaignService;
import org.cerberus.core.crud.service.ITagStatisticService;
import org.cerberus.core.exception.CerberusException;
import org.cerberus.core.util.answer.AnswerList;
import org.json.JSONArray;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class TagStatisticService implements ITagStatisticService {

    private static final Logger LOG = LogManager.getLogger("TagStatisticService");

    @Autowired
    private ITagStatisticDAO tagStatisticDAO;

    @Autowired
    private ICampaignService campaignService;

    /**
     * @param tag
     * @return AnswerList that contains data from database
     */
    public AnswerList<TagStatistic> readByTag(String tag) {
        return tagStatisticDAO.readByTag(tag);
    }

    /**
     * Initialize TagStatistics objects
     * @param tag
     * @param executions
     * @return Map with TagStatistics initialized
     */
    public HashMap<String, TagStatistic> initTagStatistics(Tag tag, List<TestCaseExecution> executions) {

        String campaignGroup1 = getCampaignGroup1(tag.getCampaign());
        HashMap<String, TagStatistic> tagStatistics = new HashMap<>();

        for (TestCaseExecution execution : executions) {
            String key = String.format("%s_%s", execution.getEnvironment(), execution.getCountry());
            tagStatistics.computeIfAbsent(key, k -> TagStatistic.builder()
                    .tag(tag.getTag())
                    .country(execution.getCountry())
                    .environment(execution.getEnvironment())
                    .campaign(tag.getCampaign())
                    .dateStartExe(new Timestamp(0))
                    .dateEndExe(new Timestamp(0))
                    .campaignGroup1(campaignGroup1)
                    .executions(new ArrayList<>())
                    .build());
            tagStatistics.get(key).getExecutions().add(execution);
        }
        return tagStatistics;
    }

    /**
     * Populate TagStatistics objects with aggregated data
     * @param tagStatistics
     * @param executions
     * @param tag
     */
    public void populateTagStatisticsMap(Map<String, TagStatistic> tagStatistics, List<TestCaseExecution> executions, Tag tag) {
        for (Map.Entry<String, TagStatistic> tagStatisticEntry : tagStatistics.entrySet()) {
            TagStatistic tagStatistic = tagStatisticEntry.getValue();
            List<String> systems = new ArrayList<>();
            List<String> applications = new ArrayList<>();

            for (TestCaseExecution execution : tagStatistic.getExecutions()) {
                calculateExecutionDates(tagStatistic, execution);
                calculateNumberExecutionsByStatus(tagStatistic, execution);
                populateSystemList(systems, execution);
                populateApplicationList(applications, execution);
            }
            tagStatistic.setSystemList(new JSONArray(systems).toString());
            tagStatistic.setApplicationList(new JSONArray(applications).toString());
            tagStatistic.setUsrCreated(tag.getUsrCreated());
            tagStatistic.setExecutions(null);
        }
        tagStatisticDAO.createWithMap(tagStatistics);
    }

    /**
     * Used to calculate the start and end date for each TagStatistic
     * @param tagStatistic
     * @param execution
     */
    private void calculateExecutionDates(TagStatistic tagStatistic, TestCaseExecution execution) {
        if (tagStatistic.getDateStartExe().getTime() == 0 || (execution.getStart() < tagStatistic.getDateStartExe().getTime())) {
            tagStatistic.setDateStartExe(new Timestamp(execution.getStart()));
        }
        if (tagStatistic.getDateEndExe().getTime() == 0 ||(execution.getEnd() > tagStatistic.getDateEndExe().getTime())) {
            tagStatistic.setDateEndExe(new Timestamp(execution.getEnd()));
        }
    }

    /**
     * Used to calculate number of executions by status for each TagStatistics
     * @param tagStatistic
     * @param execution
     */
    private void calculateNumberExecutionsByStatus(TagStatistic tagStatistic, TestCaseExecution execution) {
        int nbRetries = execution.getNbExecutions() - 1;
        tagStatistic.setNbExe(tagStatistic.getNbExe() + execution.getNbExecutions());
        tagStatistic.setNbExeUsefull(tagStatistic.getNbExeUsefull() + (execution.getNbExecutions() - nbRetries));

        switch(execution.getControlStatus()) {
            case "OK":
                tagStatistic.incrementNbOK();
                break;
            case "KO":
                tagStatistic.incrementNbKO();
                break;
            case "FA":
                tagStatistic.incrementNbFA();
                break;
            case "NA":
                tagStatistic.incrementNbNA();
                break;
            case "NE":
                tagStatistic.incrementNbNE();
                break;
            case "WE":
                tagStatistic.incrementNbWE();
                break;
            case "PE":
                tagStatistic.incrementNbPE();
                break;
            case "QU":
                tagStatistic.incrementNbQU();
                break;
            case "QE":
                tagStatistic.incrementNbQE();
                break;
            case "CA":
                tagStatistic.incrementNbCA();
                break;
            default:
                break;
        }
    }

    /**
     * Populate the systems list for each TagStatistic object
     * @param systems
     * @param execution
     */
    private void populateSystemList(List<String> systems, TestCaseExecution execution) {
        if (!systems.contains(execution.getSystem())) {
            systems.add(execution.getSystem());
        }
    }

    /**
     * Populate the applications list for each TagStatistic object
     * @param applications
     * @param execution
     */
    private void populateApplicationList(List<String> applications, TestCaseExecution execution) {
        if (!applications.contains(execution.getApplication())) {
            applications.add(execution.getApplication());
        }
    }

    /**
     * Retrieve campaign group 1
     * @param campaign
     * @return
     */
    private String getCampaignGroup1(String campaign) {
        String campaignGroup1 = "";
        try {
            campaignGroup1 = campaignService.convert(campaignService.readByKey(campaign)).getGroup1();
        } catch (CerberusException exception) {
            LOG.error("Unable to get campaign owner: ", exception);
        }
        return campaignGroup1;
    }
}
