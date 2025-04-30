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
        tagStatistic.setNbExeUseful(tagStatistic.getNbExeUseful() + (execution.getNbExecutions() - nbRetries));

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
            LOG.error("Unable to get campaign group1: ", exception);
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

    @Override
    public AnswerList<TagStatistic> readByCriteria(String campaign, List<String> countries, List<String> environment, String minDate, String maxDate) {
        return tagStatisticDAO.readByCriteria(campaign, countries, environment, minDate, maxDate);
    }

    public Map<String, Map<String, JSONObject>> createMapGroupedByTag(List<TagStatistic> tagStatistics, String aggregateType) throws JSONException {
        Map<String, Map<String, JSONObject>> aggregateByTag = new HashMap<>();
        for (TagStatistic tagStatistic : tagStatistics) {
            int nbExeUseful = 0;
            int nbExe = 0;
            int nbOK = 0;
            long duration = 0;
            long msMinTagDateStart = 0;
            long msMaxTagDateEnd = 0;

            String key = setKeyAccordingToAggregateType(aggregateType, tagStatistic);
            String campaign = tagStatistic.getCampaign();
            String tag = tagStatistic.getTag();
            String group1 = tagStatistic.getCampaignGroup1();

            Timestamp minTagDateStartExe = new Timestamp(0);
            Timestamp maxTagDateEndExe = new Timestamp(0);

            JSONArray systemsInTagMap = new JSONArray();
            JSONArray applicationsInTagMap = new JSONArray();

            JSONObject statistics;

            key = setKeyAccordingToAggregateType(aggregateType, tagStatistic);

            if (!aggregateByTag.containsKey(key)) {
                aggregateByTag.put(key, new HashMap<>());
            }

            if (!aggregateByTag.get(key).containsKey(tag)) {
                statistics = createJsonTagStat(systemsInTagMap, applicationsInTagMap, group1, minTagDateStartExe, maxTagDateEndExe, duration, nbExeUseful, nbExe, nbOK);
                aggregateByTag.get(key).put(tag, statistics);
            }

            JSONUtil.jsonArrayAddUniqueElement(
                    new JSONArray(tagStatistic.getSystemList()),
                    systemsInTagMap
            );

            JSONUtil.jsonArrayAddUniqueElement(
                    new JSONArray(tagStatistic.getApplicationList()),
                    applicationsInTagMap
            );

            if (minTagDateStartExe.equals(new Timestamp(0)) || tagStatistic.getDateStartExe().getTime() < minTagDateStartExe.getTime()) {
                minTagDateStartExe = tagStatistic.getDateStartExe();
            }

            if (maxTagDateEndExe.equals(new Timestamp(0)) || tagStatistic.getDateEndExe().getTime() > maxTagDateEndExe.getTime()) {
                maxTagDateEndExe = tagStatistic.getDateEndExe();
            }

            JSONObject mapTag = aggregateByTag.get(key).get(tag);
            msMinTagDateStart = minTagDateStartExe.getTime();
            msMaxTagDateEnd = maxTagDateEndExe.getTime();
            duration = (msMaxTagDateEnd - msMinTagDateStart) / 1000;
            nbExeUseful += tagStatistic.getNbExeUseful() + mapTag.getInt("nbExeUseful");
            nbExe += tagStatistic.getNbExe() + mapTag.getInt("nbExe");
            nbOK += tagStatistic.getNbOK() + mapTag.getInt("nbOK");

            statistics = createJsonTagStat(systemsInTagMap, applicationsInTagMap, group1, minTagDateStartExe, maxTagDateEndExe, duration, nbExeUseful, nbExe, nbOK);
            aggregateByTag.get(key).put(tag, statistics);
        }
        return aggregateByTag;
    }

    public Map<String, JSONObject> createMapAggregatedStatistics(Map<String, Map<String, JSONObject>> aggregateByTag, String aggregateType, Map<String, String> campaignGroups1) throws JSONException {
        Map<String, JSONObject> aggregatedStatistics = new HashMap<>();
        for (Map.Entry<String, Map<String, JSONObject>> aggregateByTagEntry : aggregateByTag.entrySet()) {
            String key = aggregateByTagEntry.getKey();
            double totalDuration = 0;
            String minDateStart = "";
            String maxDateEnd = "";
            double sumPercOK = 0;
            double sumPercReliability = 0;
            int sumNumberExeUseful = 0;
            int sumNumberExe = 0;
            int nbCampaignExecutions = 0;
            String campaignGroup1 = "";
            JSONArray systemsByCampaign = new JSONArray();
            JSONArray applicationsByCampaign = new JSONArray();

            for (JSONObject mapTagEntry : aggregateByTagEntry.getValue().values()) {
                JSONUtil.jsonArrayAddUniqueElement(
                        mapTagEntry.getJSONArray("systemList"),
                        systemsByCampaign
                );
                JSONUtil.jsonArrayAddUniqueElement(
                        mapTagEntry.getJSONArray("applicationList"),
                        applicationsByCampaign
                );

                minDateStart = updateMinCampaignDateStart(minDateStart, mapTagEntry);
                maxDateEnd = updateMaxCampaignDateEnd(maxDateEnd, mapTagEntry);
                sumPercOK += ((double) mapTagEntry.getInt("nbOK") / mapTagEntry.getInt("nbExeUseful"));
                sumPercReliability += ((double) mapTagEntry.getInt("nbExeUseful") / mapTagEntry.getInt("nbExe"));
                sumNumberExeUseful += mapTagEntry.getInt("nbExeUseful");
                sumNumberExe += mapTagEntry.getInt("nbExe");
                totalDuration += mapTagEntry.getLong("duration");
                nbCampaignExecutions++;

                if (aggregateType.equals("CAMPAIGN")) {
                    if (StringUtil.isNotEmptyOrNull(campaignGroups1.get(key)) && campaignGroups1.get(key).equals(mapTagEntry.getString("campaignGroup1"))) {
                        campaignGroup1 = mapTagEntry.getString("campaignGroup1");
                    }
                }
            }

            JSONObject statistics;
            double avgDuration = totalDuration / aggregateByTagEntry.getValue().size();
            double avgOK = (sumPercOK * 100.0) / aggregateByTagEntry.getValue().size();
            double avgReliability = (sumPercReliability * 100) / aggregateByTagEntry.getValue().size();

            if (aggregateType.equals("ENV_COUNTRY")) {
                String environment = key.split("_")[0];
                String country = key.split("_")[1];
                statistics = createJsonCampaignStatByEnvCountry(systemsByCampaign, applicationsByCampaign, environment, country, avgDuration, minDateStart, maxDateEnd, avgOK, avgReliability, sumNumberExeUseful, sumNumberExe);
            } else {
                statistics = createJsonCampaignStat(key, systemsByCampaign, applicationsByCampaign, campaignGroup1, avgDuration, minDateStart, maxDateEnd, avgOK, avgReliability, nbCampaignExecutions);
            }

            aggregatedStatistics.put(key, statistics);
        }
        return aggregatedStatistics;
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

    public Map<String, String> generateGroup1List(Set<String> campaignsInTagstatistics) {
        List<String> campaigns = new ArrayList<>(campaignsInTagstatistics);
        Map<String, String> campaignGroups1 = new HashMap<>();
        Map<String, List<String>> queryParams = new HashMap<>();
        queryParams.put("campaign", campaigns);
        List<Campaign> globalGroup1List = campaignService.readByCriteria(0, -1, "Group1", "asc", null, queryParams).getDataList();
        for (Campaign campaign : globalGroup1List) {
            if (StringUtil.isNotEmptyOrNull(campaign.getGroup1())) { //No interest to put empty group1
                campaignGroups1.put(campaign.getCampaign(), campaign.getGroup1());
            }
        }
        return campaignGroups1;
    }

    private String updateMaxCampaignDateEnd(String maxDateEnd, JSONObject mapTagEntry) throws JSONException {
        String currentEndDate = mapTagEntry.get("maxTagDateEnd").toString();
        if (StringUtil.isEmptyOrNull(maxDateEnd) || Timestamp.valueOf(currentEndDate).getTime() > Timestamp.valueOf(maxDateEnd).getTime()) {
            return currentEndDate;
        }
        return maxDateEnd;
    }

    public boolean userHasRightSystems(String user, List<TagStatistic> tagStatistics) {
        List<String> systemsAllowedForUser;
        List<String> systemList = new ArrayList<>();
        try {
            systemsAllowedForUser = getSystemsAllowedForUser(user);
        } catch (CerberusException exception) {
            LOG.error("Unable to get systems allowed for user: ", exception);
            return false;
        }
        for (TagStatistic tagStatistic : tagStatistics) {
            String[] elements = tagStatistic.getSystemList().replaceAll("\"", "").replace("[", "").replace("]", "").split(",");
            systemList.addAll(Arrays.asList(elements));
        }
        return systemList.stream()
                .anyMatch(system -> systemsAllowedForUser.contains(system));
    }

    private String updateMinCampaignDateStart(String minDateStart, JSONObject mapTagEntry) throws JSONException {
        String currentStartDate = mapTagEntry.get("minTagDateStart").toString();
        if (StringUtil.isEmptyOrNull(minDateStart) || Timestamp.valueOf(currentStartDate).getTime() < Timestamp.valueOf(minDateStart).getTime()) {
            return currentStartDate;
        }
        return minDateStart;
    }

    private JSONObject createJsonTagStat(JSONArray systems, JSONArray applications, String group1, Timestamp minDateStart, Timestamp maxDateEnd, long duration, int nbExeUseful, int nbExe, int nbOK) throws JSONException {
        return new JSONObject(new LinkedHashMap<>())
                .put("systemList", systems)
                .put("applicationList", applications)
                .put("campaignGroup1", group1)
                .put("minTagDateStart", minDateStart)
                .put("maxTagDateEnd", maxDateEnd)
                .put("duration", duration)
                .put("nbExeUseful", nbExeUseful)
                .put("nbExe", nbExe)
                .put("nbOK",nbOK);
    }

    private JSONObject createJsonCampaignStatByEnvCountry(JSONArray systems, JSONArray applications, String environment, String country, double avgDuration, String minDateStart, String maxDateEnd, double avgOK, double avgReliability, int sumNbExeUseful, int sumNbExe) throws JSONException {
        return new JSONObject(new LinkedHashMap<>())
                .put("systemList", systems)
                .put("applicationList", applications)
                .put("environment", environment)
                .put("country", country)
                .put("avgDuration", avgDuration)
                .put("minDateStart", minDateStart)
                .put("maxDateEnd", maxDateEnd)
                .put("avgOK", avgOK)
                .put("avgReliability",avgReliability)
                .put("nbExeUseful", sumNbExeUseful)
                .put("nbExe", sumNbExe);
    }

    private JSONObject createJsonCampaignStat(String campaign, JSONArray systems, JSONArray applications, String group1, double avgDuration, String minDateStart, String maxDateEnd, double avgOK, double avgReliability, int nbCampaignExecutions) throws JSONException {
        return new JSONObject(new LinkedHashMap<>())
                .put("campaign", campaign)
                .put("systemList", systems)
                .put("applicationList", applications)
                .put("campaignGroup1", group1)
                .put("avgDuration", avgDuration)
                .put("minDateStart", minDateStart)
                .put("maxDateEnd", maxDateEnd)
                .put("avgOK", avgOK)
                .put("avgReliability",avgReliability)
                .put("nbCampaignExecutions", nbCampaignExecutions);
    }

    private String setKeyAccordingToAggregateType(String aggregateType, TagStatistic tagStatistic) {
        String key = "";
        switch (aggregateType) {
            case "CAMPAIGN":
                key = tagStatistic.getCampaign();
                break;
            case "ENV_COUNTRY":
                key = String.format("%s_%s", tagStatistic.getEnvironment(), tagStatistic.getCountry());
                break;
        }
        return key;
    }
}
