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
import org.cerberus.core.crud.entity.Tag;
import org.cerberus.core.crud.entity.TestCaseExecution;
import org.cerberus.core.crud.service.ITagService;
import org.cerberus.core.crud.service.ITagStatisticService;
import org.cerberus.core.crud.service.ITestCaseExecutionService;
import org.cerberus.core.engine.entity.MessageEvent;
import org.cerberus.core.enums.MessageEventEnum;
import org.cerberus.core.exception.CerberusException;
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

    private static final String DATE_FORMAT = "yyyy-MM-dd'T'HH:mm:ss.S'Z'";
    private static final String DATEWEEK_FORMAT = "yyyy-ww";
    private static final String DATEWEEKONLY_FORMAT = "w";

    private static final org.apache.logging.log4j.Logger LOG = org.apache.logging.log4j.LogManager.getLogger(AutomateScoreService.class);

    @Override
    public JSONObject generateAutomateScore(HttpServletRequest request, List<String> systems, List<String> campaigns, String to, int nbWeeks) {

        LOG.debug(systems);

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
        AnswerList<TestCaseExecution> daoExeAnswer;

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

//            systemsAllowed = tagStatisticService.getSystemsAllowedForUser(request.getUserPrincipal().getName());
//            systems.removeIf(param -> !systemsAllowed.contains(param));
            //If user put in filter a system that is has no access, we delete from the list.
//            LOG.debug(systems);
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
            JSONObject weekStat = new JSONObject();

            Map<String, JSONObject> weekGlobalTagStats = new HashMap<>();
            Map<String, JSONObject> weekGlobalExeStats = new HashMap<>();
            Map<String, JSONObject> weekGlobalStats = new HashMap<>();

            Map<String, JSONObject> campaignMap = new HashMap<>();
            Map<String, JSONObject> campaignWeekMap = new HashMap<>();
            Map<String, JSONObject> testCaseMap = new HashMap<>();
            Map<String, JSONObject> testCaseWeekMap = new HashMap<>();

            /**
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

                weekStat = new JSONObject();
                weekGlobalStats.put(weekEntry, weekStat);

                weeks.put(week);
            }
            response.put("weeks", weeks);

            /**
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

            // Loop agains campaignweek list in order to attach each of the into the correct week.
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
             * Feed Week with counters parsing all executions
             */
            List<String> taglist = new ArrayList<>();
            for (Tag tagsStatistic : tagsStatistics) {
                taglist.add(tagsStatistic.getTag());
            }
            exesStatistics = executionService.readByCriteria(systems, taglist, cFrom.getTime(), cTo.getTime());

//            if (exesStatistics.isEmpty()) {
//                response.put("message", daoTagAnswer.getResultMessage().getDescription());
//                return response;
//                return ResponseEntity
//                        .status(HttpStatus.NOT_FOUND)
//                        .body(response.toString());
//            }
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
                    tcExes.put(tcExe);

                    // Testcases
                    tcKey = getTestCaseKey(myExe.getTest(), myExe.getTestCase());
                    int nbFlaky = myExe.isFlaky() ? 1 : 0;
                    int nbFN = myExe.isFalseNegative() ? 1 : 0;
                    long exeDur = myExe.getDurationMs();
                    if (!testCaseMap.containsKey(tcKey)) {
                        testcase = new JSONObject();
//                        testcase.put("name", tcKey);
                        testcase.put("testFolder", myExe.getTest());
                        testcase.put("testcaseId", myExe.getTestCase());
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

            // Loop agains testcaseweek list in order to attach each of the into the correct week.
            tmpWeekEntry = "";
            JSONArray tmpTc;
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
                kpiFreq.put("score", getScoreFrequency(kpi1Value, weekKey, todayWeekEntry));
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
                kpiDur.put("score", getScoreDuration(kpi2Value));
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
                kpiStability.put("score", getScoreStability(kpi3Value, weekGlobalTagStats.get(weekKey).getInt("nbExe")));
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
                long kpi4Value = weekGlobalTagStats.get(weekKey).getLong("durationMax");
                kpiMaintenance.put("value", kpi4Value);
                kpiMaintenance.put("score", getScoreMaintenance(kpi4Value));
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

                JSONObject weekVal = weekGlobalStats.get(weekKey);
                weekVal.put("kpiFrequency", kpiFreq);
                weekVal.put("kpiDuration", kpiDur);
                weekVal.put("kpiStability", kpiStability);
                weekVal.put("kpiMaintenance", kpiMaintenance);

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

            response.put("debug-testcaseWeeks", testCaseWeekMap);

            response.put("debug-campaignWeeks", campaignWeekMap);

            response.put("exes", tcExes);

            response.put("weekStatsTag", weekGlobalTagStats);

            response.put("weekStatsExe", weekGlobalExeStats);

            response.put("weekStats", weekGlobalStats);

            //            Map<String, Map<String, JSONObject>> aggregateByTag = tagStatisticService.createMapGroupedByTag(tagStatistics, "CAMPAIGN");
            //            Map<String, String> campaignGroups1 = tagStatisticService.generateGroup1List(aggregateByTag.keySet());
            //            Map<String, JSONObject> aggregateByCampaign = tagStatisticService.createMapAggregatedStatistics(aggregateByTag, "CAMPAIGN", campaignGroups1);
            //            List<JSONObject> aggregateListByCampaign = new ArrayList<>();
            //            for (Map.Entry<String, JSONObject> entry : aggregateByCampaign.entrySet()) {
            //                String key = entry.getKey();
            //                JSONObject value = entry.getValue();
            //                group1List.replaceAll(g -> g.replace("%20", " "));
            //                if (group1List.isEmpty()) {
            //                    aggregateListByCampaign.add(value);
            //                } else {
            //                    if (group1List.contains(value.getString("campaignGroup1"))) {
            //                        aggregateListByCampaign.add(value);
            //                    }
            //                }
            //            }
            //            response.put("group1List", new HashSet<>(campaignGroups1.values())); //Hashset has only unique values
            //            response.put("campaignStatistics", aggregateListByCampaign);
            return response;

        } catch (JSONException exception) {
            LOG.error("Error when JSON processing: ", exception);
            return response;

//        } catch (CerberusException exception) {
//            LOG.error("Unable to get allowed systems: ", exception);
//            return response;
//
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

    private String getScoreFrequency(long kpi, String week, String todayWeek) {
        if (week != null && week.equals(todayWeek)) {
            return "NA";
        }
        if (kpi > 10) {
            return "A";
        } else if (kpi > 7) {
            return "B";
        } else if (kpi > 5) {
            return "C";
        } else if (kpi > 3) {
            return "D";
        } else if (kpi >= 0) {
            return "E";
        } else {
            return "NA";
        }
    }

    private String getScoreStability(long kpi, int nbExe) {
        if (nbExe == 0) {
            return "NA";
        }
        if (kpi < 50) {
            return "A";
        } else if (kpi < 100) {
            return "B";
        } else if (kpi < 300) {
            return "C";
        } else if (kpi < 1000) {
            return "D";
        } else {
            return "E";
        }
    }

    private String getScoreDuration(long kpi) {
        if (kpi == 0) {
            return "NA";
        }

        if (kpi < 600000) { // 10 min
            return "A";
        } else if (kpi < 1200000) { // 20 min
            return "B";
        } else if (kpi < 1800000) { // 30 min
            return "C";
        } else if (kpi < 3600000) { // 1 h
            return "D";
        } else {
            return "E";
        }
    }

    private String getScoreMaintenance(long kpi) {
        if (kpi < 600000) { // 10 min
            return "A";
        } else if (kpi < 1200000) { // 20 min
            return "B";
        } else if (kpi < 1800000) { // 30 min
            return "C";
        } else if (kpi > 3600000) { // 1 h
            return "D";
        } else {
            return "E";
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
