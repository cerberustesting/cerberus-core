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
import org.cerberus.core.crud.service.ITagService;
import org.cerberus.core.crud.service.ITagStatisticService;
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

        List<Tag> tagStatistics = new ArrayList<>();
        AnswerList<Tag> daoAnswer;
        List<String> systemsAllowed;
        List<String> applicationsAllowed;

        Map<String, Object> mandatoryFilters = new HashMap<>();
        mandatoryFilters.put("System", systems);

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

            daoAnswer = tagService.readByVarious(campaigns, systems, cFrom.getTime(), cTo.getTime());
            tagStatistics = daoAnswer.getDataList();

//            LOG.debug(tagStatistics.size());
            DateFormat dwf = new SimpleDateFormat(DATEWEEK_FORMAT);
            DateFormat dwfWekkOnly = new SimpleDateFormat(DATEWEEKONLY_FORMAT);
            JSONArray weeks = new JSONArray();
            String weekEntry = "";

            JSONObject weekStat = new JSONObject();
            Map<String, JSONObject> weekCampaignStats = new HashMap<>();
            Map<String, JSONObject> campaignsMap = new HashMap<>();

            for (int i = nbWeeks; i >= 0; i--) {
                JSONObject week = new JSONObject();
                weekEntry = dwf.format(new Date(toD.getTime() - Duration.ofDays(7 * i).toMillis()));
                week.put("val", weekEntry);
                week.put("label", "W" + dwfWekkOnly.format(new Date(toD.getTime() - Duration.ofDays(7 * i).toMillis())));

                weekStat = new JSONObject();
                weekStat.put("nbFlaky", 0);
                weekStat.put("nbExe", 0);
                weekStat.put("frequency", 0);
                weekStat.put("durationSum", 0);
                weekStat.put("durationMax", 0);
                weekStat.put("durationMin", 0);
                weekStat.put("duration", 0);
                weekStat.put("reliability", 0);
                weekCampaignStats.put(weekEntry, weekStat);

//                tag.put("start", weekEntry);
//                tag.put("end", weekEntry);
                weeks.put(week);
            }
            response.put("weeks", weeks);

            if (tagStatistics.isEmpty()) {
                response.put("message", daoAnswer.getResultMessage().getDescription());
                return response;
//                return ResponseEntity
//                        .status(HttpStatus.NOT_FOUND)
//                        .body(response.toString());
            }

            JSONObject tag = new JSONObject();
            JSONArray tags = new JSONArray();
            JSONObject campaign = new JSONObject();
            for (Tag myTag : tagStatistics) {

                if ((myTag.getDurationMs() > 0) && (StringUtil.isNotEmptyOrNull(myTag.getCampaign()))) {

                    weekEntry = dwf.format(new Date(myTag.getDateStartExe().getTime()));
//                    LOG.debug(weekEntry);
                    // Tag
                    tag = new JSONObject();
                    tag.put("week", weekEntry);
                    tag.put("tag", myTag.getTag());
                    tag.put("start", myTag.getDateStartExe());
                    tag.put("nbFlacky", myTag.getNbFlaky());
                    tag.put("nbExe", myTag.getNbExeUsefull());
                    tag.put("duration", myTag.getDurationMs());
                    tag.put("campaign", myTag.getCampaign());
                    tags.put(tag);

                    // Campaigns
                    if (!campaignsMap.containsKey(myTag.getCampaign())) {
                        campaign = new JSONObject();
                        campaign.put("name", myTag.getCampaign());
                        campaign.put("nb", 1);
                        campaignsMap.put(myTag.getCampaign(), campaign);
                    } else {
                        campaign = campaignsMap.get(myTag.getCampaign());
                        campaign.put("nb", campaign.getInt("nb") + 1);
                        campaignsMap.put(myTag.getCampaign(), campaign);
                    }

                    // Stats
                    weekStat = weekCampaignStats.get(weekEntry);
                    if (weekStat != null) {
                        weekStat.put("nbFlaky", weekStat.getInt("nbFlaky") + myTag.getNbFlaky());
                        weekStat.put("nbExe", weekStat.getInt("nbExe") + myTag.getNbExeUsefull());
                        weekStat.put("frequency", weekStat.getInt("frequency") + 1);
                        weekStat.put("durationSum", weekStat.getInt("durationSum") + myTag.getDurationMs());
                        if (myTag.getDurationMs() > weekStat.getInt("durationMax")) {
                            weekStat.put("durationMax", myTag.getDurationMs());
                        }
                        if (myTag.getDurationMs() != 0 && (myTag.getDurationMs() < weekStat.getInt("durationMin") || weekStat.getInt("durationMin") == 0)) {
                            weekStat.put("durationMin", myTag.getDurationMs());
                        }
                        if (weekStat.getInt("frequency") > 0) {
                            weekStat.put("duration", weekStat.getInt("durationSum") / weekStat.getInt("frequency"));
                        }
//                        LOG.debug("toto {} {} {}", weekStat.getInt("nbExe"), weekStat.getInt("nbFlaky"), weekStat.getInt("nbExe"));
                        if (weekStat.getInt("nbExe") > 0) {
                            weekStat.put("reliability", (int) (weekStat.getInt("nbFlaky") * 10000 / weekStat.getInt("nbExe")));
                        }
                        weekCampaignStats.put(weekEntry, weekStat);
                    }

                }
            }

            // Loop against all weeks in order to calculate score + variations vs Week - 1
            int kpi1ValuePrev = 0;
            int kpi2ValuePrev = 0;
            int kpi3ValuePrev = 0;
            int kpi4ValuePrev = 0;
            for (int i = 0; i < weeks.length(); i++) {
                JSONObject myWeek = (JSONObject) weeks.get(i);
                int previousKPI = 0;

                String weekKey = myWeek.getString("val");

                // KPI1 - Frequency
                JSONObject kpiFreq = new JSONObject();
                int kpi1Value = weekCampaignStats.get(weekKey).getInt("frequency");
                kpiFreq.put("value", kpi1Value);
                kpiFreq.put("score", getScoreFrequency(kpi1Value));
                if (kpi1ValuePrev != 0 && i > 0) {
                    kpiFreq.put("varVs1", ((kpi1Value * 10000) - (kpi1ValuePrev * 10000)) / (kpi1ValuePrev));
                }
                kpiFreq.put("trend", getTrendFrequency(kpi1ValuePrev, kpi1Value));

                // KPI2 - Duration
                JSONObject kpiDur = new JSONObject();
                int kpi2Value = weekCampaignStats.get(weekKey).getInt("duration");
                kpiDur.put("value", kpi2Value);
                kpiDur.put("score", getScoreDuration(kpi2Value));
                if (kpi2ValuePrev != 0 && i > 0) {
                    kpiDur.put("varVs1", ((kpi2Value) - (kpi2ValuePrev)) * 10000 / (kpi2ValuePrev));
                }
                kpiDur.put("trend", getTrendDuration(kpi1ValuePrev, kpi1Value));

                // KPI3 - Reliability
                JSONObject kpiReliability = new JSONObject();
                int kpi3Value = weekCampaignStats.get(weekKey).getInt("nbExe") == 0 ? 0 : weekCampaignStats.get(weekKey).getInt("nbFlaky") * 10000 / weekCampaignStats.get(weekKey).getInt("nbExe");
                kpiReliability.put("value", kpi3Value);
                kpiReliability.put("score", getScoreReliability(kpi3Value, weekCampaignStats.get(weekKey).getInt("nbExe")));
                if (kpi3ValuePrev != 0 && i > 0) {
                    kpiReliability.put("varVs1", ((kpi3Value) - (kpi3ValuePrev)) * 10000 / (kpi3ValuePrev));
                }
                kpiReliability.put("trend", getTrendReliability(kpi1ValuePrev, kpi1Value));

                // KPI4 - Maintenance
                JSONObject kpiMaintenance = new JSONObject();
                int kpi4Value = weekCampaignStats.get(weekKey).getInt("durationMax");
                kpiMaintenance.put("value", kpi4Value);
                kpiMaintenance.put("score", getScoreMaintenance(kpi4Value));
                if (kpi4ValuePrev != 0 && i > 0) {
                    kpiMaintenance.put("varVs1", ((kpi4Value) - (kpi4ValuePrev)) * 10000 / (kpi4ValuePrev));
                }
                kpiMaintenance.put("trend", getTrendMaintenance(kpi1ValuePrev, kpi1Value));

                JSONObject weekVal = weekCampaignStats.get(weekKey);
                weekVal.put("kpiFrequency", kpiFreq);
                weekVal.put("kpiDuration", kpiDur);
                weekVal.put("kpiReliability", kpiReliability);
                weekVal.put("kpiMaintenance", kpiMaintenance);

                weekCampaignStats.put(weekKey, weekVal);

                // Keep previous value for next iteration
                kpi1ValuePrev = kpi1Value;
                kpi2ValuePrev = kpi2Value;
                kpi3ValuePrev = kpi3Value;
                kpi4ValuePrev = kpi4Value;

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
            response.put("tags", tags);

            response.put("weekStats", weekCampaignStats);

            response.put("campaigns", campaignsMap);

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

    private String getScoreFrequency(int kpi) {
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

    private String getScoreReliability(int kpi, int nbExe) {
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

    private String getScoreDuration(int kpi) {
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

    private String getScoreMaintenance(int kpi) {
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

    private String getTrendMaintenance(int kpiprev, int kpi) {
        if (kpiprev < kpi) {
            return "KOUP";
        }
        if (kpiprev > kpi) {
            return "OKDOWN";
        }
        return "ISO";
    }

    private String getTrendFrequency(int kpiprev, int kpi) {
        if (kpiprev < kpi) {
            return "OKUP";
        }
        if (kpiprev > kpi) {
            return "KODOWN";
        }
        return "ISO";
    }

    private String getTrendReliability(int kpiprev, int kpi) {
        if (kpiprev < kpi) {
            return "KOUP";
        }
        if (kpiprev > kpi) {
            return "OKDOWN";
        }
        return "ISO";
    }

    private String getTrendDuration(int kpiprev, int kpi) {
        if (kpiprev < kpi) {
            return "OKDOWN";
        }
        if (kpiprev > kpi) {
            return "KOUP";
        }
        return "ISO";
    }

}
