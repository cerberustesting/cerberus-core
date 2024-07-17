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
import org.cerberus.core.crud.entity.*;
import org.cerberus.core.crud.service.IApplicationService;
import org.cerberus.core.crud.service.ICampaignService;
import org.cerberus.core.crud.service.ITagStatisticService;
import org.cerberus.core.crud.service.IUserSystemService;
import org.cerberus.core.exception.CerberusException;
import org.cerberus.core.util.JSONUtil;
import org.cerberus.core.util.StringUtil;
import org.cerberus.core.util.answer.Answer;
import org.cerberus.core.util.answer.AnswerList;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class TagStatisticService implements ITagStatisticService {

    private static final Logger LOG = LogManager.getLogger("TagStatisticService");

    @Autowired
    private ITagStatisticDAO tagStatisticDAO;
    @Autowired
    private ICampaignService campaignService;
    @Autowired
    private IUserSystemService userSystemService;
    @Autowired
    private IApplicationService applicationService;

    //Methods used when we create TagStatistics//

    @Override
    public Answer createWithMap(Map<String, TagStatistic> map) {
        return tagStatisticDAO.createWithMap(map);
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
        createWithMap(tagStatistics);
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

    //Methods used when we retrieve and aggregate TagStatistics by campaign//

    /**
     * @param tag
     * @return AnswerList that contains data from database
     */
    public AnswerList<TagStatistic> readByTag(String tag) {
        return tagStatisticDAO.readByTag(tag);
    }

    @Override
    public AnswerList<TagStatistic> readByCriteria(List<String> systems, List<String> applications, List<String> group1List, String minDate, String maxDate) {
        return tagStatisticDAO.readByCriteria(systems, applications, group1List, minDate, maxDate);
    }

    public Map<String, Map<String, JSONObject>> createMapAggregateByTag(List<TagStatistic> tagStatistics) throws JSONException {
        Map<String, Map<String, JSONObject>> aggregateByTag = new HashMap<>();
        for (TagStatistic tagStatistic : tagStatistics) {
            int nbExeUsefull = 0;
            int nbExe = 0;
            int nbOK = 0;
            long duration = 0;
            String campaign = tagStatistic.getCampaign();
            String tag = tagStatistic.getTag();
            Timestamp minTagDateStartExe = new Timestamp(0);
            Timestamp maxTagDateEndExe = new Timestamp(0);
            long msMinTagDateStart = 0;
            long msMaxTagDateEnd = 0;
            JSONArray systemsInTagMap;
            JSONArray applicationsInTagMap;

            if (!aggregateByTag.containsKey(campaign)) {
                aggregateByTag.put(campaign, new HashMap<>());
            }

            if (!aggregateByTag.get(campaign).containsKey(tag)) {
                aggregateByTag.get(campaign).put(tag, new JSONObject());
                aggregateByTag.get(campaign).get(tag)
                        .put("campaign", tagStatistic.getCampaign())
                        .put("campaignGroup1", tagStatistic.getCampaignGroup1())
                        .put("systemList", new JSONArray().toString())
                        .put("applicationList", new JSONArray().toString())
                        .put("maxTagDateEnd", minTagDateStartExe)
                        .put("minTagDateStart", maxTagDateEndExe)
                        .put("nbExeUsefull", 0)
                        .put("nbExe", 0)
                        .put("nbOK", 0)
                        .put("duration", 0);
            }

            JSONObject mapTag = aggregateByTag.get(campaign).get(tag);

            systemsInTagMap = JSONUtil.jsonArrayAddUniqueElement(
                    new JSONArray(tagStatistic.getSystemList()),
                    new JSONArray(mapTag.getString("systemList"))
            );

            applicationsInTagMap = JSONUtil.jsonArrayAddUniqueElement(
                    new JSONArray(tagStatistic.getApplicationList()),
                    new JSONArray(mapTag.getString("applicationList"))
            );

            minTagDateStartExe = Timestamp.valueOf(mapTag.getString("minTagDateStart"));
            if (minTagDateStartExe.equals(new Timestamp(0)) || tagStatistic.getDateStartExe().getTime() < minTagDateStartExe.getTime()) {
                minTagDateStartExe = tagStatistic.getDateStartExe();
            }

            maxTagDateEndExe = Timestamp.valueOf(mapTag.getString("maxTagDateEnd"));
            if (maxTagDateEndExe.equals(new Timestamp(0)) || tagStatistic.getDateEndExe().getTime() > maxTagDateEndExe.getTime()) {
                maxTagDateEndExe = tagStatistic.getDateEndExe();
            }

            msMinTagDateStart = minTagDateStartExe.getTime();
            msMaxTagDateEnd = maxTagDateEndExe.getTime();
            duration = (msMaxTagDateEnd - msMinTagDateStart) / 1000;
            nbExeUsefull += tagStatistic.getNbExeUsefull() + mapTag.getInt("nbExeUsefull");
            nbExe += tagStatistic.getNbExe() + mapTag.getInt("nbExe");
            nbOK += tagStatistic.getNbOK() + mapTag.getInt("nbOK");

            mapTag.put("systemList", systemsInTagMap.toString())
                    .put("applicationList", applicationsInTagMap.toString())
                    .put("minTagDateStart", minTagDateStartExe)
                    .put("maxTagDateEnd", maxTagDateEndExe)
                    .put("duration", duration)
                    .put("nbExeUsefull", nbExeUsefull)
                    .put("nbExe", nbExe)
                    .put("nbOK", nbOK);
        }
        return aggregateByTag;
    }

    public Map<String, JSONObject> createMapAggregateByCampaign(Map<String, Map<String, JSONObject>> aggregateByTag) throws JSONException {
        Map<String, JSONObject> aggregateByCampaign = new HashMap<>();
        JSONArray globalGroup1List = new JSONArray();
        for (Map.Entry<String, Map<String, JSONObject>> aggregateByTagEntry : aggregateByTag.entrySet()) {
            String campaign = aggregateByTagEntry.getKey();
            double totalDuration = 0;
            String minDateStart = "";
            String maxDateEnd = "";
            double sumPercOK = 0;
            double sumPercReliability = 0;
            int sumNumberExeUsefull = 0;
            String campaignGroup1 = "";
            JSONArray systemsByCampaign = new JSONArray();
            JSONArray applicationsByCampaign = new JSONArray();

            //If campaign is not present in the map we create a new entry
            aggregateByCampaign.computeIfAbsent(campaign, key -> {
                JSONObject entry = new JSONObject(new LinkedHashMap<>());
                try {
                    entry.put("campaign", campaign)
                            .put("systemList", new JSONArray().toString())
                            .put("applicationList", new JSONArray().toString());
                } catch (JSONException exception) {
                    LOG.error(exception);
                }
                return entry;
            });

            JSONObject campaignStatistic = aggregateByCampaign.get(campaign);

            for (JSONObject mapTagEntry : aggregateByTagEntry.getValue().values()) {

                campaignGroup1 = mapTagEntry.getString("campaignGroup1");

                systemsByCampaign = JSONUtil.jsonArrayAddUniqueElement(
                        new JSONArray(mapTagEntry.getString("systemList")),
                        new JSONArray(campaignStatistic.getString("systemList"))
                );

                applicationsByCampaign = JSONUtil.jsonArrayAddUniqueElement(
                        new JSONArray(mapTagEntry.getString("applicationList")),
                        new JSONArray(campaignStatistic.getString("applicationList"))
                );

                if (StringUtil.isNotEmpty(campaignGroup1)) {
                    updateGlobalGroup1List(globalGroup1List, campaignGroup1);
                }

                minDateStart = updateMinCampaignDateStart(minDateStart, mapTagEntry);
                maxDateEnd = updateMaxCampaignDateEnd(maxDateEnd, mapTagEntry);
                sumPercOK += ((double) mapTagEntry.getInt("nbOK") / mapTagEntry.getInt("nbExeUsefull"));
                sumPercReliability += ((double) mapTagEntry.getInt("nbExeUsefull") / mapTagEntry.getInt("nbExe"));
                sumNumberExeUsefull += mapTagEntry.getInt("nbExeUsefull");
                totalDuration += mapTagEntry.getLong("duration");
            }

            campaignStatistic
                    .put("systemList", systemsByCampaign)
                    .put("systemList", systemsByCampaign)
                    .put("applicationList", applicationsByCampaign)
                    .put("campaignGroup1", campaignGroup1)
                    .put("avgDuration", totalDuration / aggregateByTagEntry.getValue().size())
                    .put("minDateStart", minDateStart)
                    .put("maxDateEnd", maxDateEnd)
                    .put("avgOK", (sumPercOK * 100.0) / aggregateByTagEntry.getValue().size())
                    .put("avgReliability", (sumPercReliability * 100) / aggregateByTagEntry.getValue().size())
                    .put("avgNbExeUsefull", sumNumberExeUsefull / aggregateByTagEntry.getValue().size());
        }
        aggregateByCampaign.put("globalGroup1List", new JSONObject().put("array", globalGroup1List));
        return aggregateByCampaign;
    }

    public List<String> getSystemsAllowedForUser(String user) throws CerberusException {
        List<UserSystem> systemsAllowedForUser = userSystemService.findUserSystemByUser(user);
        return systemsAllowedForUser.stream().map(UserSystem::getSystem).collect(Collectors.toList());
    }
    public List<String> getApplicationsSystems(List<String> systems) {
        List<Application> applicationsAllowedForUser = applicationService.readBySystem(systems).getDataList();
        return applicationsAllowedForUser.stream().map(Application::getApplication).collect(Collectors.toList());
    }

    public String formatDateForDb(String date) {
        TimeZone tz = TimeZone.getTimeZone("UTC");
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.S'Z'");
        df.setTimeZone(tz);

        String dateFormatted = "";
        try {
            dateFormatted = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(df.parse(date));
        } catch (ParseException exception) {
            LOG.error("Exception when parsing date, ", exception);
        }
        return dateFormatted;
    }

    private JSONArray updateGlobalGroup1List(JSONArray campaignGroup1Array, String campaignGroup1) throws JSONException {
        if (!JSONUtil.jsonArrayContains(campaignGroup1Array, campaignGroup1)) {
            campaignGroup1Array.put(campaignGroup1);
        }
        return campaignGroup1Array;
    }

    private String updateMaxCampaignDateEnd(String maxDateEnd, JSONObject mapTagEntry) throws JSONException {
        String currentEndDate = mapTagEntry.getString("maxTagDateEnd");
        if (StringUtil.isEmpty(maxDateEnd) || Timestamp.valueOf(currentEndDate).getTime() > Timestamp.valueOf(maxDateEnd).getTime()) {
            return currentEndDate;
        }
        return maxDateEnd;
    }

    private String updateMinCampaignDateStart(String minDateStart, JSONObject mapTagEntry) throws JSONException {
        String currentStartDate = mapTagEntry.getString("minTagDateStart");
        if (StringUtil.isEmpty(minDateStart) || Timestamp.valueOf(currentStartDate).getTime() < Timestamp.valueOf(minDateStart).getTime()) {
            return currentStartDate;
        }
        return minDateStart;
    }
}
