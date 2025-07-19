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
package org.cerberus.core.service.automatescore.impl;

import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;
import javax.servlet.http.HttpServletRequest;
import org.cerberus.core.crud.entity.Parameter;
import org.cerberus.core.crud.entity.Tag;
import org.cerberus.core.crud.entity.TestCaseExecution;
import org.cerberus.core.crud.entity.TestCaseHisto;
import org.cerberus.core.crud.service.IParameterService;
import org.cerberus.core.crud.service.ITagService;
import org.cerberus.core.crud.service.ITestCaseExecutionService;
import org.cerberus.core.crud.service.ITestCaseHistoService;
import org.cerberus.core.engine.entity.MessageEvent;
import org.cerberus.core.enums.MessageEventEnum;
import org.cerberus.core.service.automatescore.IAutomateScoreService;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.cerberus.core.util.StringUtil;
import org.cerberus.core.util.answer.AnswerList;
import org.json.JSONArray;

/**
 *
 * @author vertigo17
 */
@Service
public class AutomateScoreService implements IAutomateScoreService {

    @Autowired
    private ITagService tagService;

    @Autowired
    private ITestCaseExecutionService executionService;

    @Autowired
    private ITestCaseHistoService histoService;

    @Autowired
    private IParameterService parameterService;

    private static final String DATE_FORMAT = "yyyy-MM-dd'T'HH:mm:ss.S'Z'";
    private static final String DATEWEEK_FORMAT = "yyyy-ww";
    private static final String DATEWEEKONLY_FORMAT = "w";

    private static final long CHANGE_HORIZON = 300000; // 5 min

    private static final org.apache.logging.log4j.Logger LOG = org.apache.logging.log4j.LogManager.getLogger(AutomateScoreService.class);

    @Override
    public JSONObject generateAutomateScore(HttpServletRequest request, List<String> systems, List<String> campaigns, String to, int nbWeeks) {

        LOG.debug(systems);

        long changeDuration = parameterService.getParameterLongByKey(Parameter.VALUE_cerberus_automatescore_changehorizon, null, CHANGE_HORIZON);

        Date toD;
        try {
            TimeZone tz = TimeZone.getTimeZone("UTC");
            DateFormat df = new SimpleDateFormat(DATE_FORMAT);
            df.setTimeZone(tz);
            toD = df.parse(to);
        } catch (ParseException ex) {
            toD = Date.from(ZonedDateTime.now().minusMonths(1).toInstant());
            LOG.debug("Exception when parsing date", ex);
        }

        List<Tag> tagsStatistics = new ArrayList<>();
        AnswerList<Tag> daoTagAnswer;

        List<TestCaseExecution> exesStatistics = new ArrayList<>();

        List<TestCaseHisto> histoStatistics = new ArrayList<>();

        JSONObject response = new JSONObject();
        try {

            if (request.getUserPrincipal() == null) {
                MessageEvent message = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_UNAUTHORISED);
                message.setDescription(message.getDescription().replace("%ITEM%", "Automate Score Statistics"));
                message.setDescription(message.getDescription().replace("%OPERATION%", "'Get statistics'"));
                message.setDescription(message.getDescription().replace("%REASON%", "No user provided in the request, please refresh the page or login again."));
                response.put("message", message.getDescription());
                return response;
            }

            nbWeeks--;
            // Calculate from and to securing that full week is considered
            Calendar cFrom = Calendar.getInstance();
            cFrom.setFirstDayOfWeek(Calendar.MONDAY);
            //ensure the method works within current month
            cFrom.setTimeInMillis(toD.getTime() - Duration.ofDays(7 * nbWeeks).toMillis());
            //go to the 1st week of february, in which monday was in january
            cFrom.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
            cFrom.set(Calendar.HOUR_OF_DAY, 0);
            cFrom.set(Calendar.MINUTE, 0);
            cFrom.set(Calendar.SECOND, 0);
            System.out.println("Date from " + cFrom.getTime());

            //same for tuesday
            Calendar cTo = Calendar.getInstance();
            cTo.setFirstDayOfWeek(Calendar.MONDAY);
            //ensure the method works within current month
            cTo.setTime(toD);
            //go to the 1st week of february, in which monday was in january
            cTo.set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY);
            cTo.set(Calendar.HOUR_OF_DAY, 23);
            cTo.set(Calendar.MINUTE, 59);
            cTo.set(Calendar.SECOND, 59);

//            LOG.debug(tagStatistics.size());
            DateFormat dwf = new SimpleDateFormat(DATEWEEK_FORMAT);
            DateFormat dwfWekkOnly = new SimpleDateFormat(DATEWEEKONLY_FORMAT);
            JSONArray weeks = new JSONArray();
            String weekEntry = "";

            // Get week number of current week.
            String todayWeekEntry = dwf.format(new Date());

            JSONObject weekStatTag = new JSONObject();
            JSONObject weekStatExe = new JSONObject();
            JSONObject weekStatUser = new JSONObject();
            JSONObject weekStat = new JSONObject();

            Map<String, JSONObject> weekGlobalTagStats = new HashMap<>();
            Map<String, JSONObject> weekGlobalExeStats = new HashMap<>();
            Map<String, JSONObject> weekGlobalUserStats = new HashMap<>();
            Map<String, JSONObject> weekGlobalStats = new HashMap<>();

            Map<String, JSONObject> campaignMap = new HashMap<>();
            Map<String, JSONObject> campaignWeekMap = new HashMap<>();

            Map<String, JSONObject> applicationMap = new HashMap<>();

            Map<String, JSONObject> testCaseMap = new HashMap<>();
            Map<String, JSONObject> testCaseWeekMap = new HashMap<>();

            Map<String, JSONObject> userMap = new HashMap<>();
            Map<String, JSONArray> userWeekMap = new HashMap<>();

            /**
             * ################################################################
             * ## WEEK LIST ##
             * ################################################################
             * Feed Week list from to parameter and nbWeeks (Trend duration)
             */
            for (int i = nbWeeks; i >= 0; i--) {
                JSONObject week = new JSONObject();
                weekEntry = dwf.format(new Date(toD.getTime() - Duration.ofDays(7 * i).toMillis()));
                week.put("val", weekEntry);
                week.put("label", "W" + dwfWekkOnly.format(new Date(toD.getTime() - Duration.ofDays(7 * i).toMillis())));

                weekStatTag = new JSONObject();
                weekStatTag.put("nbFlaky", 0);
                weekStatTag.put("nbExe", 0);
                weekStatTag.put("nb", 0);
                weekStatTag.put("durationSum", 0);
                weekStatTag.put("durationMax", 0);
                weekStatTag.put("durationMin", 0);
                weekStatTag.put("duration", 0);
                weekGlobalTagStats.put(weekEntry, weekStatTag);

                weekStatExe = new JSONObject();
                weekStatExe.put("nbFlaky", 0);
                weekStatExe.put("nbFN", 0);
                weekStatExe.put("nbExe", 0);
                weekStatExe.put("durationSum", 0);
                weekStatExe.put("durationMax", 0);
                weekStatExe.put("durationMin", 0);
                weekStatExe.put("duration", 0);
                weekGlobalExeStats.put(weekEntry, weekStatExe);

                weekStatUser = new JSONObject();
                weekStatUser.put("nbSave", 0);
                weekStatUser.put("duration", 0);
                weekGlobalUserStats.put(weekEntry, weekStatUser);

                weekStat = new JSONObject();
                weekGlobalStats.put(weekEntry, weekStat);

                weeks.put(week);
            }
            response.put("weeks", weeks);

            /**
             * ################################################################
             * ## CAMPAIGN EXECUTION ##
             * ################################################################
             * Feed Week with counters parsing all campaigns executions
             */
            daoTagAnswer = tagService.readByVarious(campaigns, systems, cFrom.getTime(), cTo.getTime());
            tagsStatistics = daoTagAnswer.getDataList();

            if (tagsStatistics.isEmpty()) {
                response.put("message", daoTagAnswer.getResultMessage().getDescription());
                return response;
//                return ResponseEntity
//                        .status(HttpStatus.NOT_FOUND)
//                        .body(response.toString());
            }

            JSONObject tag = new JSONObject();
            JSONArray tags = new JSONArray();
            JSONObject campaign = new JSONObject();
            String cwKey = "";
            for (Tag myTag : tagsStatistics) {

                if ((myTag.getDurationMs() > 0) && (StringUtil.isNotEmptyOrNull(myTag.getCampaign()))) {

                    weekEntry = dwf.format(new Date(myTag.getDateStartExe().getTime()));
//                    LOG.debug(weekEntry);
                    // Tag
                    tag = new JSONObject();
                    tag.put("week", weekEntry);
                    tag.put("tag", myTag.getTag());
                    tag.put("start", myTag.getDateStartExe());
                    tag.put("nbFlaky", myTag.getNbFlaky());
                    tag.put("nbExe", myTag.getNbExeUsefull());
                    tag.put("duration", myTag.getDurationMs());
                    tag.put("campaign", myTag.getCampaign());
                    tags.put(tag);

                    // Campaigns
                    if (!campaignMap.containsKey(myTag.getCampaign())) {
                        campaign = new JSONObject();
                        campaign.put("campaign", myTag.getCampaign());
                        campaign.put("nb", 1);
                        campaign.put("nbFlaky", myTag.getNbFlaky());
                        campaign.put("nbFlakySum", myTag.getNbFlaky());
                        campaign.put("nbFlakyMax", myTag.getNbFlaky());
                        campaign.put("nbFlakyMin", myTag.getNbFlaky());
                        campaign.put("nbExe", myTag.getNbExeUsefull());
                        campaign.put("nbExeSum", myTag.getNbExeUsefull());
                        campaign.put("nbExeMax", myTag.getNbExeUsefull());
                        campaign.put("nbExeMin", myTag.getNbExeUsefull());
                        campaign.put("duration", myTag.getDurationMs());
                        campaign.put("durationSum", myTag.getDurationMs());
                        campaign.put("durationMax", myTag.getDurationMs());
                        campaign.put("durationMin", myTag.getDurationMs());
                        campaignMap.put(myTag.getCampaign(), campaign);
                    } else {
                        campaign = campaignMap.get(myTag.getCampaign());
                        campaign.put("nb", campaign.getInt("nb") + 1);

                        campaign.put("nbFlakySum", campaign.getLong("nbFlakySum") + myTag.getNbFlaky());
                        if (myTag.getNbFlaky() > campaign.getLong("nbFlakyMax")) {
                            campaign.put("nbFlakyMax", myTag.getNbFlaky());
                        }
                        if (myTag.getNbFlaky() < campaign.getLong("nbFlakyMin")) {
                            campaign.put("nbFlakyMin", myTag.getNbFlaky());
                        }
                        if (campaign.getInt("nb") > 0) {
                            campaign.put("nbFlaky", campaign.getLong("nbFlakySum") / campaign.getInt("nb"));
                        }

                        campaign.put("nbExeSum", campaign.getLong("nbExeSum") + myTag.getNbExeUsefull());
                        if (myTag.getNbExeUsefull() > campaign.getLong("nbExeMax")) {
                            campaign.put("nbExeMax", myTag.getNbExeUsefull());
                        }
                        if (myTag.getNbExeUsefull() != 0 && (myTag.getNbExeUsefull() < campaign.getLong("nbExeMin") || campaign.getLong("nbExeMin") == 0)) {
                            campaign.put("nbExeMin", myTag.getNbExeUsefull());
                        }
                        if (campaign.getInt("nb") > 0) {
                            campaign.put("nbExe", campaign.getLong("nbExeSum") / campaign.getInt("nb"));
                        }

                        campaign.put("durationSum", campaign.getLong("durationSum") + myTag.getDurationMs());
                        if (myTag.getDurationMs() > campaign.getLong("durationMax")) {
                            campaign.put("durationMax", myTag.getDurationMs());
                        }
                        if (myTag.getDurationMs() != 0 && (myTag.getDurationMs() < campaign.getLong("durationMin") || campaign.getLong("durationMin") == 0)) {
                            campaign.put("durationMin", myTag.getDurationMs());
                        }
                        if (campaign.getInt("nb") > 0) {
                            campaign.put("duration", campaign.getLong("durationSum") / campaign.getInt("nb"));
                        }

                        campaignMap.put(myTag.getCampaign(), campaign);
                    }

                    // Campaigns Week
                    cwKey = getCampaignWeekKey(myTag.getCampaign(), weekEntry);
                    if (!campaignWeekMap.containsKey(cwKey)) {
                        campaign = new JSONObject();
                        campaign.put("campaign", myTag.getCampaign());
                        campaign.put("nb", 1);
                        campaignWeekMap.put(cwKey, campaign);
                    } else {
                        campaign = campaignWeekMap.get(cwKey);
                        campaign.put("nb", campaign.getInt("nb") + 1);
                        campaignWeekMap.put(cwKey, campaign);
                    }

                    // Stats
                    weekStatTag = weekGlobalTagStats.get(weekEntry);
                    if (weekStatTag != null) {

                        weekStatTag.put("nbFlaky", weekStatTag.getInt("nbFlaky") + myTag.getNbFlaky());
                        weekStatTag.put("nbExe", weekStatTag.getInt("nbExe") + myTag.getNbExeUsefull());
                        weekStatTag.put("nb", weekStatTag.getInt("nb") + 1);
                        weekStatTag.put("durationSum", weekStatTag.getLong("durationSum") + myTag.getDurationMs());
                        if (myTag.getDurationMs() > weekStatTag.getLong("durationMax")) {
                            weekStatTag.put("durationMax", myTag.getDurationMs());
                        }
                        if (myTag.getDurationMs() != 0 && (myTag.getDurationMs() < weekStatTag.getLong("durationMin") || weekStatTag.getLong("durationMin") == 0)) {
                            weekStatTag.put("durationMin", myTag.getDurationMs());
                        }
                        if (weekStatTag.getInt("nb") > 0) {
                            weekStatTag.put("duration", weekStatTag.getLong("durationSum") / weekStatTag.getInt("nb"));
                        }
//                        LOG.debug("toto {} {} {}", weekStat.getInt("nbExe"), weekStat.getInt("nbFlaky"), weekStat.getInt("nbExe"));
//                        if (weekStatTag.getInt("nbExe") > 0) {
//                            weekStatTag.put("sability", (int) (weekStatTag.getInt("nbFlaky") * 10000 / weekStatTag.getInt("nbExe")));
//                        }
                        weekGlobalTagStats.put(weekEntry, weekStatTag);
                    }

                }
            }

            // Loop agains campaignweek list in order to attach each of them into the correct week.
            String tmpWeekEntry = "";
            JSONArray tmpCmp;
            for (Map.Entry<String, JSONObject> entry : campaignWeekMap.entrySet()) {
                String key = entry.getKey();
                JSONObject val = entry.getValue();
                tmpWeekEntry = key.substring(key.length() - 7);
//                LOG.debug(key);
//                LOG.debug(tmpWeekEntry);
                if (weekGlobalTagStats.containsKey(tmpWeekEntry)) {
                    JSONObject tmpWeek = weekGlobalTagStats.get(tmpWeekEntry);
                    if (tmpWeek.has("campaigns")) {
                        tmpCmp = tmpWeek.getJSONArray("campaigns");
                    } else {
                        tmpCmp = new JSONArray();
                    }
                    tmpCmp.put(val);
                    tmpWeek.put("campaigns", tmpCmp);
                    weekGlobalTagStats.put(tmpWeekEntry, tmpWeek);
                }
            }

            /**
             * ################################################################
             * ## EXECUTION ##
             * ################################################################
             * Feed Week with counters parsing all executions
             */
            List<String> taglist = new ArrayList<>();
            for (Tag tagsStatistic : tagsStatistics) {
                taglist.add(tagsStatistic.getTag());
            }
            exesStatistics = executionService.readByCriteria(systems, taglist, cFrom.getTime(), cTo.getTime());

            JSONObject tcExe = new JSONObject();
            JSONArray tcExes = new JSONArray();
            String tcKey;
            String tcwKey;
            JSONObject testcase = new JSONObject();
            JSONObject testcaseWeek = new JSONObject();
            for (TestCaseExecution myExe : exesStatistics) {

                if ((myExe.getDurationMs() > 0) && (StringUtil.isNotEmptyOrNull(myExe.getTag()))) {

                    weekEntry = dwf.format(new Date(myExe.getStart()));
//                    LOG.debug(weekEntry);
                    // Tag
                    tcExe = new JSONObject();
                    tcExe.put("id", myExe.getId());
                    tcExe.put("week", weekEntry);
                    tcExe.put("tag", myExe.getTag());
                    tcExe.put("start", new Timestamp(myExe.getStart()));
                    tcExe.put("isFlaky", myExe.isFlaky());
                    tcExe.put("isFN", myExe.isFalseNegative());
                    tcExe.put("duration", myExe.getDurationMs());
                    tcExe.put("testFolder", myExe.getTest());
                    tcExe.put("testcaseId", myExe.getTestCase());
                    tcExe.put("application", myExe.getApplication());
                    tcExes.put(tcExe);

                    // Init counters
                    int nbFlaky = myExe.isFlaky() ? 1 : 0;
                    int nbFN = myExe.isFalseNegative() ? 1 : 0;
                    long exeDur = myExe.getDurationMs();

                    // Applications
                    if (!applicationMap.containsKey(myExe.getApplication())) {
                        applicationMap.put(myExe.getApplication(), null);
                    }

                    // Testcases
                    tcKey = getTestCaseKey(myExe.getTest(), myExe.getTestCase());
                    if (!testCaseMap.containsKey(tcKey)) {
                        testcase = new JSONObject();
//                        testcase.put("name", tcKey);
                        testcase.put("testFolder", myExe.getTest());
                        testcase.put("testcaseId", myExe.getTestCase());
                        testcase.put("application", myExe.getApplication());
                        testcase.put("nb", 1);
                        testcase.put("nbFlaky", nbFlaky);
                        testcase.put("nbFN", nbFN);
                        testcase.put("durationSum", myExe.getDurationMs());
                        testcase.put("durationMax", myExe.getDurationMs());
                        testcase.put("durationMin", myExe.getDurationMs());
                        testcase.put("duration", myExe.getDurationMs());
                        testCaseMap.put(tcKey, testcase);
                    } else {
                        testcase = testCaseMap.get(tcKey);
                        testcase.put("nb", testcase.getInt("nb") + 1);
                        testcase.put("nbFlaky", testcase.getLong("nbFlaky") + nbFlaky);
                        testcase.put("nbFN", testcase.getLong("nbFN") + nbFN);
                        testcase.put("durationSum", testcase.getLong("durationSum") + myExe.getDurationMs());
                        if (myExe.getDurationMs() > testcase.getLong("durationMax")) {
                            testcase.put("durationMax", myExe.getDurationMs());
                        }
                        if (myExe.getDurationMs() != 0 && (myExe.getDurationMs() < testcase.getLong("durationMin") || testcase.getLong("durationMin") == 0)) {
                            testcase.put("durationMin", myExe.getDurationMs());
                        }
                        if (testcase.getInt("nb") > 0) {
                            testcase.put("duration", testcase.getLong("durationSum") / testcase.getInt("nb"));
                        }

                        testCaseMap.put(tcKey, testcase);
                    }

                    tcwKey = getTestCaseWeekKey(myExe.getTest(), myExe.getTestCase(), weekEntry);
                    if (!testCaseWeekMap.containsKey(tcwKey)) {
                        testcaseWeek = new JSONObject();
//                        testcaseWeek.put("name", tcwKey);
                        testcaseWeek.put("testFolder", myExe.getTest());
                        testcaseWeek.put("testcaseId", myExe.getTestCase());
                        testcaseWeek.put("application", myExe.getApplication());
                        testcaseWeek.put("nb", 1);
                        testcaseWeek.put("nbFlaky", nbFlaky);
                        testcaseWeek.put("nbFN", nbFN);
                        testcaseWeek.put("durationSum", exeDur);
                        testCaseWeekMap.put(tcwKey, testcaseWeek);
                    } else {
                        testcaseWeek = testCaseWeekMap.get(tcwKey);
                        testcaseWeek.put("nb", testcaseWeek.getInt("nb") + 1);
                        testcaseWeek.put("nbFlaky", testcaseWeek.getInt("nbFlaky") + nbFlaky);
                        testcaseWeek.put("nbFN", testcaseWeek.getInt("nbFN") + nbFN);
                        testcaseWeek.put("durationSum", testcaseWeek.getLong("durationSum") + exeDur);
                        if (testcaseWeek.getInt("nb") > 0) {
                            testcaseWeek.put("duration", testcaseWeek.getLong("durationSum") / testcaseWeek.getInt("nb"));
                        }
                        testCaseWeekMap.put(tcwKey, testcaseWeek);
                    }

                    // Stats
                    weekStatExe = weekGlobalExeStats.get(weekEntry);
//                    LOG.debug("toto " + weekEntry);
//                    LOG.debug("toto " + weekStatExe);
                    if (weekStatExe != null) {
                        weekStatExe.put("nbFlaky", weekStatExe.getInt("nbFlaky") + (myExe.isFlaky() ? 1 : 0));
                        weekStatExe.put("nbFN", weekStatExe.getInt("nbFN") + (myExe.isFalseNegative() ? 1 : 0));
                        weekStatExe.put("nbExe", weekStatExe.getInt("nbExe") + 1);
                        weekStatExe.put("durationSum", weekStatExe.getLong("durationSum") + myExe.getDurationMs());
                        if (myExe.getDurationMs() > weekStatExe.getLong("durationMax")) {
                            weekStatExe.put("durationMax", myExe.getDurationMs());
                        }
                        if (myExe.getDurationMs() != 0 && (myExe.getDurationMs() < weekStatExe.getLong("durationMin") || weekStatExe.getLong("durationMin") == 0)) {
                            weekStatExe.put("durationMin", myExe.getDurationMs());
                        }
                        if (weekStatExe.getInt("nbExe") > 0) {
                            weekStatExe.put("duration", weekStatExe.getLong("durationSum") / weekStatExe.getInt("nbExe"));
                        }
//                        LOG.debug("toto {} {} {}", weekStatExe.getInt("nbExe"), weekStatExe.getInt("nbFlaky"), weekStatExe.getInt("durationSum"));
                        if (weekStatExe.getInt("nbExe") > 0) {
                            weekStatExe.put("stability", (int) ((weekStatExe.getInt("nbFlaky") + weekStatExe.getInt("nbFN")) * 10000 / (float) weekStatExe.getInt("nbExe")));
                        }
                        weekGlobalExeStats.put(weekEntry, weekStatExe);
                    }
                }
            }

            // Loop against testcaseweek map in order to attach each of them into the 2 maps per week and per test.
            tmpWeekEntry = "";
            JSONArray tmpTc;
            Map<String, JSONArray> weekTestCaseMap = new HashMap<>();

            for (Map.Entry<String, JSONObject> entry : testCaseWeekMap.entrySet()) {
                String key = entry.getKey();
                JSONObject val = entry.getValue();
                tmpWeekEntry = key.substring(key.length() - 7);
//                LOG.debug(key);
//                LOG.debug(tmpWeekEntry);
                if (weekGlobalExeStats.containsKey(tmpWeekEntry)) {
                    JSONObject tmpWeek = weekGlobalExeStats.get(tmpWeekEntry);
                    if (tmpWeek.has("tests")) {
                        tmpTc = tmpWeek.getJSONArray("tests");
                    } else {
                        tmpTc = new JSONArray();
                    }
                    tmpTc.put(val);
                    tmpWeek.put("tests", tmpTc);
                    weekGlobalExeStats.put(tmpWeekEntry, tmpWeek);
                }
            }

            /**
             * ################################################################
             * ## Maintenance duration ##
             * ################################################################
             *
             */
            histoStatistics = histoService.readByDate(cFrom.getTime(), cTo.getTime());

            String userWeekKey;
            JSONObject tcChmt = new JSONObject();
            JSONArray tcChmts = new JSONArray();
            JSONObject userWeek = new JSONObject();
            JSONArray userWeeks = new JSONArray();
            String tmpUserEntry = "";
            for (TestCaseHisto myChange : histoStatistics) {

                tcKey = getTestCaseKey(myChange.getTest(), myChange.getTestCase());

                if ((StringUtil.isNotEmptyOrNull(myChange.getTestCase()))
                        && (StringUtil.isNotEmptyOrNull(myChange.getTestCase()))
                        && (testCaseMap.containsKey(tcKey))) {

                    weekEntry = dwf.format(new Date(myChange.getDateVersion().getTime()));
//                    LOG.debug(weekEntry);
                    // Unit Change for debug
                    tcChmt = new JSONObject();
                    tcChmt.put("date", myChange.getDateVersion());
                    tcChmt.put("week", weekEntry);
                    tcChmt.put("user", myChange.getUsrCreated());
                    tcChmt.put("testFolder", myChange.getTest());
                    tcChmt.put("testcaseId", myChange.getTestCase());
                    tcChmts.put(tcChmt);
//                    LOG.debug("---------------------------------------");
//                    LOG.debug(tcChmt);

                    userWeekKey = getUserWeekKey(myChange.getUsrCreated(), weekEntry);

                    if (!userWeekMap.containsKey(userWeekKey)) {
                        userWeeks = new JSONArray();
                        userWeek = new JSONObject();
//                        testcaseWeek.put("name", tcwKey);
                        userWeek.put("user", myChange.getUsrCreated());
                        userWeek.put("dateStart", myChange.getDateVersion());
                        userWeek.put("dateStartl", myChange.getDateVersion().getTime());
                        userWeek.put("dateEnd", myChange.getDateVersion());
                        userWeek.put("dateEndl", myChange.getDateVersion().getTime());
                        userWeek.put("nb", 1);
                        userWeek.put("duration", changeDuration * 2);
                        userWeeks.put(userWeek);
                        userWeekMap.put(userWeekKey, userWeeks);
//                        LOG.debug(userWeeks);
                    } else {
                        JSONArray tmpUserWeeks = new JSONArray();

                        userWeeks = userWeekMap.get(userWeekKey);

                        boolean found = false;
                        for (Object userWeek1 : userWeeks) {
                            userWeek = (JSONObject) userWeek1;
//                            LOG.debug(userWeek);
                            if ((myChange.getDateVersion().getTime() < (userWeek.getLong("dateStartl") - changeDuration))
                                    || (myChange.getDateVersion().getTime() > (userWeek.getLong("dateEndl") + changeDuration))) {
                                tmpUserWeeks.put(userWeek);
                            } else {
                                found = true;
                                userWeek.put("nb", userWeek.getInt("nb") + 1);
                                if (myChange.getDateVersion().getTime() < (userWeek.getLong("dateStartl"))) {
                                    userWeek.put("dateStart", myChange.getDateVersion());
                                    userWeek.put("dateStartl", myChange.getDateVersion().getTime());
                                } else if (myChange.getDateVersion().getTime() > (userWeek.getLong("dateEndl"))) {
                                    userWeek.put("dateEnd", myChange.getDateVersion());
                                    userWeek.put("dateEndl", myChange.getDateVersion().getTime());
                                }
                                userWeek.put("duration", (userWeek.getLong("dateEndl") + changeDuration) - (userWeek.getLong("dateStartl") - changeDuration));
                                tmpUserWeeks.put(userWeek);
//                                LOG.debug("match");
                            }

                        }
                        if (!found) {
                            userWeek = new JSONObject();
                            userWeek.put("user", myChange.getUsrCreated());
                            userWeek.put("dateStart", myChange.getDateVersion());
                            userWeek.put("dateStartl", myChange.getDateVersion().getTime());
                            userWeek.put("dateEnd", myChange.getDateVersion());
                            userWeek.put("dateEndl", myChange.getDateVersion().getTime());
                            userWeek.put("nb", 1);
                            userWeek.put("duration", changeDuration * 2);
                            tmpUserWeeks.put(userWeek);
//                            LOG.debug("not match");
                        }

                        userWeekMap.put(userWeekKey, tmpUserWeeks);
                    }
//                    LOG.debug(userWeekMap);

                }
            }

            // Loop against userweek map in order to attach each of them into the 2 maps per week and per user.
            tmpWeekEntry = "";
            tmpUserEntry = "";
            JSONArray tmpUser;
            Map<String, JSONObject> weekUserTimeMap = new HashMap<>();

            for (Map.Entry<String, JSONArray> entry : userWeekMap.entrySet()) {
                String key = entry.getKey();
                JSONArray val = entry.getValue();
                tmpWeekEntry = key.substring(key.length() - 7);
                tmpUserEntry = key.substring(0, key.length() - 10);
//                LOG.debug(key);
//                LOG.debug(tmpWeekEntry);

                if (weekGlobalUserStats.containsKey(tmpWeekEntry)) {

                    JSONObject tmpWeek = weekGlobalUserStats.get(tmpWeekEntry);

                    for (Object object : val) {
                        JSONObject listOfTimeSpent = (JSONObject) object;
                        tmpWeek.put("nbSave", tmpWeek.getInt("nbSave") + listOfTimeSpent.getInt("nb"));
                        tmpWeek.put("duration", tmpWeek.getLong("duration") + listOfTimeSpent.getLong("duration"));
                    }

                    if (tmpWeek.has("users")) {
                        tmpTc = tmpWeek.getJSONArray("users");
                    } else {
                        tmpTc = new JSONArray();
                    }

                    tmpTc.put(tmpUserEntry);
                    tmpWeek.put("users", tmpTc);
                    weekGlobalUserStats.put(tmpWeekEntry, tmpWeek);
                }
            }

            /**
             * ################################################################
             * ## KPI CALCULATION ##
             * ################################################################
             *
             */
            // Loop against all weeks in order to calculate score + variations vs Week - 1
            long kpi1ValuePrev = 0;
            long kpi2ValuePrev = 0;
            long kpi3ValuePrev = 0;
            long kpi4ValuePrev = 0;
            long kpi1ValuePrevAll = 0;
            long kpi2ValuePrevAll = 0;
            long kpi3ValuePrevAll = 0;
            long kpi4ValuePrevAll = 0;
            int kpi1ValuePrevAllNb = 0;
            int kpi2ValuePrevAllNb = 0;
            int kpi3ValuePrevAllNb = 0;
            int kpi4ValuePrevAllNb = 0;
            Integer score1 = null;
            Integer score2 = null;
            Integer score3 = null;
            Integer score4 = null;
            Integer score = null;

            for (int i = 0; i < weeks.length(); i++) {
                JSONObject myWeek = (JSONObject) weeks.get(i);
                int previousKPI = 0;

                String weekKey = myWeek.getString("val");

                // KPI1 - Frequency
                JSONObject kpiFreq = new JSONObject();
                long kpi1Value = 0;
                if (weekGlobalTagStats.get(weekKey).has("campaigns") && weekGlobalTagStats.get(weekKey).getJSONArray("campaigns").length() > 0) {
                    kpi1Value = weekGlobalTagStats.get(weekKey).getInt("nb") / weekGlobalTagStats.get(weekKey).getJSONArray("campaigns").length();
                }
                kpiFreq.put("value", kpi1Value);
                score1 = getScoreFrequency(kpi1Value, weekKey, todayWeekEntry);
                kpiFreq.put("score", score1);
                kpiFreq.put("scoreL", getScoreFromInt(score1));
                if (kpi1ValuePrev != 0 && i > 0) {
                    kpiFreq.put("varVs1", ((kpi1Value * 10000) - (kpi1ValuePrev * 10000)) / (kpi1ValuePrev));
                }
                if (kpi1ValuePrevAllNb != 0 && kpi1ValuePrevAll != 0 && i > 0) {
                    kpiFreq.put("varVsAll", Math.round(((kpi1Value * 10000) - (kpi1ValuePrevAll * 10000 / (float) kpi1ValuePrevAllNb)) / (kpi1ValuePrevAll / (float) kpi1ValuePrevAllNb)));
                }
                if (kpi1ValuePrevAllNb > 0) {
                    kpiFreq.put("trend", getTrendFrequency(Math.round(kpi1ValuePrevAll / kpi1ValuePrevAllNb), kpi1Value, weekKey, todayWeekEntry));
                } else {
                    kpiFreq.put("trend", getTrendFrequency(kpi1ValuePrev, kpi1Value, weekKey, todayWeekEntry));
                }

                // KPI2 - Duration
                JSONObject kpiDur = new JSONObject();
                long kpi2Value = weekGlobalTagStats.get(weekKey).getLong("duration");
                kpiDur.put("value", kpi2Value);
                score2 = getScoreDuration(kpi2Value);
                kpiDur.put("score", score2);
                kpiDur.put("scoreL", getScoreFromInt(score2));
                if (kpi2ValuePrev != 0 && i > 0) {
                    kpiDur.put("varVs1", ((kpi2Value) - (kpi2ValuePrev)) * 10000 / (kpi2ValuePrev));
                }
                if (kpi2ValuePrevAllNb != 0 && kpi2ValuePrevAll != 0 && i > 0) {
                    kpiDur.put("varVsAll", Math.round(((kpi2Value * 10000) - (kpi2ValuePrevAll * 10000 / (float) kpi2ValuePrevAllNb)) / (kpi2ValuePrevAll / (float) kpi2ValuePrevAllNb)));
//                    kpiDur.put("varVsAll", Math.round(((kpi2Value * 10000) - ((kpi2ValuePrevAll * 10000) / (float) kpi2ValuePrevAllNb)) / ((float) (kpi2ValuePrevAll / (float) kpi2ValuePrevAllNb))));
                    kpiDur.put("varVsAll-nb", kpi2ValuePrevAllNb);
                    kpiDur.put("varVsAll-sum", kpi2ValuePrevAll);
                }
                if (kpi2ValuePrevAllNb > 0) {
                    kpiDur.put("trend", getTrendDuration(Math.round(kpi2ValuePrevAll / (float) kpi2ValuePrevAllNb), kpi2Value));
                } else {
                    kpiDur.put("trend", getTrendDuration(kpi2ValuePrev, kpi2Value));
                }

                // KPI3 - Stability
                JSONObject kpiStability = new JSONObject();
                long kpi3Value = weekGlobalTagStats.get(weekKey).getInt("nbExe") == 0 ? 0
                        : (weekGlobalTagStats.get(weekKey).getInt("nbFlaky") * 10000 + weekGlobalExeStats.get(weekKey).getInt("nbFN") * 10000) / weekGlobalTagStats.get(weekKey).getInt("nbExe");
                kpiStability.put("value", kpi3Value);
                score3 = getScoreStability(kpi3Value, weekGlobalTagStats.get(weekKey).getInt("nbExe"));
                kpiStability.put("score", score3);
                kpiStability.put("scoreL", getScoreFromInt(score3));
                if (kpi3ValuePrev != 0 && i > 0) {
                    kpiStability.put("varVs1", ((kpi3Value) - (kpi3ValuePrev)) * 10000 / (kpi3ValuePrev));
                }
                if (kpi3ValuePrevAllNb != 0 && kpi3ValuePrevAll != 0 && i > 0) {
                    kpiStability.put("varVsAll", Math.round(((kpi3Value * 10000) - (kpi3ValuePrevAll / (float) kpi3ValuePrevAllNb * 10000)) / (kpi3ValuePrevAll / (float) kpi3ValuePrevAllNb)));
                }
                if (kpi3ValuePrevAllNb > 0) {
                    kpiStability.put("trend", getTrendStability(Math.round(kpi3ValuePrevAll / kpi3ValuePrevAllNb), kpi3Value));
                } else {
                    kpiStability.put("trend", getTrendStability(kpi3ValuePrev, kpi3Value));
                }

                // KPI4 - Maintenance
                JSONObject kpiMaintenance = new JSONObject();
                long kpi4Value = weekGlobalUserStats.get(weekKey).getLong("duration");
                kpiMaintenance.put("value", kpi4Value);
                score4 = getScoreMaintenance(kpi4Value);
                kpiMaintenance.put("score", score4);
                kpiMaintenance.put("scoreL", getScoreFromInt(score4));
                if (kpi4ValuePrev != 0 && i > 0) {
                    kpiMaintenance.put("varVs1", ((kpi4Value) - (kpi4ValuePrev)) * 10000 / (kpi4ValuePrev));
                }
                if (kpi4ValuePrevAllNb != 0 && kpi4ValuePrevAll != 0 && i > 0) {
                    kpiMaintenance.put("varVsAll", Math.round(((kpi4Value * 10000) - (kpi4ValuePrevAll / (float) kpi4ValuePrevAllNb * 10000)) / (kpi4ValuePrevAll / (float) kpi4ValuePrevAllNb)));
                }
                if (kpi4ValuePrevAllNb > 0) {
                    kpiMaintenance.put("trend", getTrendMaintenance(Math.round(kpi4ValuePrevAll / kpi4ValuePrevAllNb), kpi4Value));
                } else {
                    kpiMaintenance.put("trend", getTrendMaintenance(kpi4ValuePrev, kpi4Value));
                }
                kpiMaintenance.put("trend", getTrendMaintenance(kpi4ValuePrev, kpi4Value));

                // GLOBAL KPI
                JSONObject kpi = new JSONObject();
                score = getGlobalScore(score1, score2, score3, score4);
                kpi.put("score", score);
                kpi.put("scoreL", getGlobalScoreFromInt(score));

                // Feed all KPI to final week entry
                JSONObject weekVal = weekGlobalStats.get(weekKey);
                weekVal.put("kpiFrequency", kpiFreq);
                weekVal.put("kpiDuration", kpiDur);
                weekVal.put("kpiStability", kpiStability);
                weekVal.put("kpiMaintenance", kpiMaintenance);
                weekVal.put("kpi", kpi);

                weekGlobalStats.put(weekKey, weekVal);

                // Keep previous value for next iteration
                kpi1ValuePrev = kpi1Value;
                kpi2ValuePrev = kpi2Value;
                kpi3ValuePrev = kpi3Value;
                kpi4ValuePrev = kpi4Value;
                kpi1ValuePrevAll += kpi1Value;
                kpi2ValuePrevAll += kpi2Value;
                kpi3ValuePrevAll += kpi3Value;
                kpi4ValuePrevAll += kpi4Value;
                kpi1ValuePrevAllNb++;
                kpi2ValuePrevAllNb++;
                kpi3ValuePrevAllNb++;
                kpi4ValuePrevAllNb++;

            }

//            for (Map.Entry<String, JSONObject> entry : weekStats.entrySet()) {
//                String key = entry.getKey();
//                JSONObject val = entry.getValue();
//
//                JSONObject kpiFreq = new JSONObject();
//                kpiFreq.put("value", val.get("frequency"));
//                kpiFreq.put("score", "A");
//                kpiFreq.put("varVs-1", -100);
//                val.put("kpi1", kpiFreq);
//                weekStats.put(key, val);
//            }
            JSONArray campaignsArray = new JSONArray();
            for (Map.Entry<String, JSONObject> entry : campaignMap.entrySet()) {
                JSONObject val = entry.getValue();
                campaignsArray.put(val);
            }
            response.put("campaigns", campaignsArray);

            response.put("tags", tags);

            JSONArray testcasesArray = new JSONArray();
            for (Map.Entry<String, JSONObject> entry : testCaseMap.entrySet()) {
                JSONObject val = entry.getValue();
                testcasesArray.put(val);
            }
            response.put("testcases", testcasesArray);

            JSONArray applicationsArray = new JSONArray();
            for (Map.Entry<String, JSONObject> entry : applicationMap.entrySet()) {
                String key = entry.getKey();
                JSONObject val = entry.getValue();
                applicationsArray.put(key);
            }
            response.put("applications", applicationsArray);

            response.put("debug-testcaseWeeks", testCaseWeekMap);

            response.put("debug-campaignWeeks", campaignWeekMap);

            response.put("debug-userWeeks", userWeekMap);

            response.put("exes", tcExes);

            response.put("chgmts", tcChmts);

            response.put("weekStatsTag", weekGlobalTagStats);

            response.put("weekStatsExe", weekGlobalExeStats);

            response.put("weekStatsUser", weekGlobalUserStats);

            response.put("weekStats", weekGlobalStats);

            return response;

        } catch (JSONException exception) {
            LOG.error("Error when JSON processing: ", exception);
            return response;

        } catch (Exception exception) {
            LOG.error(exception, exception);
            return response;

        }
    }

    private String getTestCaseKey(String test, String testCase) {
        return test + " | " + testCase;
    }

    private String getTestCaseWeekKey(String test, String testCase, String week) {
        return test + " | " + testCase + " | " + week;
    }

    private String getCampaignWeekKey(String campaign, String week) {
        return campaign + " | " + week;
    }

    private String getUserWeekKey(String user, String week) {
        return user + " | " + week;
    }

    private Integer getGlobalScore(Integer sc1, Integer sc2, Integer sc3, Integer sc4) {
        int nb = 0;
        int total = 0;
        if (sc1 != null) {
            nb++;
            total += sc1;
        }
        if (sc2 != null) {
            nb++;
            total += sc2;
        }
        if (sc3 != null) {
            nb++;
            total += sc3;
        }
        if (sc4 != null) {
            nb++;
            total += sc4;
        }
        if (nb > 0) {
            return total;
        }
        return null;
    }

    private String getScoreFromInt(Integer kpi) {
        if (kpi == null) {
            return "NA";
        }
        if (kpi > 20) {
            return "A";
        } else if (kpi > 15) {
            return "B";
        } else if (kpi > 10) {
            return "C";
        } else if (kpi > 5) {
            return "D";
        } else if (kpi >= 0) {
            return "E";
        } else {
            return "NA";
        }
    }

    private String getGlobalScoreFromInt(Integer kpi) {
        if (kpi == null) {
            return "NA";
        }
        if (kpi > 80) {
            return "A";
        } else if (kpi > 60) {
            return "B";
        } else if (kpi > 40) {
            return "C";
        } else if (kpi > 20) {
            return "D";
        } else if (kpi >= 0) {
            return "E";
        } else {
            return "NA";
        }
    }

    private Integer getScoreFrequency(long kpi, String week, String todayWeek) {
        if (week != null && week.equals(todayWeek)) {
            return null;
        }
        if (kpi >= 7) {
            return 25;
        } else if (kpi >= 5) {
            return 20;
        } else if (kpi >= 3) {
            return 15;
        } else if (kpi >= 1) {
            return 10;
        } else {
            return 0;
        }
    }

    private Integer getScoreStability(long kpi, int nbExe) {
        if (nbExe == 0) {
            return null;
        }
        if (kpi < 100) {
            return 25;
        } else if (kpi < 500) {
            return 20;
        } else if (kpi < 1000) {
            return 15;
        } else if (kpi < 2000) {
            return 10;
        } else if (kpi < 4000) {
            return 5;
        } else {
            return 0;
        }
    }

    private Integer getScoreDuration(long kpi) {
        if (kpi == 0) {
            return null;
        }

        if (kpi < 2700000) { // 45 min
            return 25;
        } else if (kpi < 7200000) { // 2 hours
            return 20;
        } else if (kpi < 14400000) { // 4 hours
            return 15;
        } else if (kpi < 28800000) { // 8 hours
            return 10;
        } else if (kpi < 57600000) { // 16 hours
            return 5;
        } else {
            return 0;
        }
    }

    private Integer getScoreMaintenance(long kpi) {
        if (kpi < 2700000) { // 45 min
            return 25;
        } else if (kpi < 7200000) { // 2 hours
            return 20;
        } else if (kpi < 14400000) { // 4 hours
            return 15;
        } else if (kpi < 28800000) { // 8 hours
            return 10;
        } else if (kpi < 57600000) { // 16 hours
            return 5;
        } else {
            return 0;
        }
    }

    private String getTrendMaintenance(long kpiprev, long kpi) {
        if (kpiprev < kpi) {
            return "KOUP";
        }
        if (kpiprev > kpi) {
            return "OKDOWN";
        }
        return "ISO";
    }

    private String getTrendFrequency(long kpiprev, long kpi, String week, String todayWeek) {
        if (week != null && week.equals(todayWeek)) {
            return "NA";
        }
        if (kpiprev < kpi) {
            return "OKUP";
        }
        if (kpiprev > kpi) {
            return "KODOWN";
        }
        return "ISO";
    }

    private String getTrendStability(long kpiprev, long kpi) {
        if (kpiprev < kpi) {
            return "KOUP";
        }
        if (kpiprev > kpi) {
            return "OKDOWN";
        }
        return "ISO";
    }

    private String getTrendDuration(long kpiprev, long kpi) {
        if (kpiprev > kpi) {
            return "OKDOWN";
        }
        if (kpiprev < kpi) {
            return "KOUP";
        }
        return "ISO";
    }

}
