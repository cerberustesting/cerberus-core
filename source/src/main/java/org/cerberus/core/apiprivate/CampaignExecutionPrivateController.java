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
package org.cerberus.core.apiprivate;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.cerberus.core.crud.dao.ITagStatisticDAO;
import org.cerberus.core.crud.entity.Application;
import org.cerberus.core.crud.entity.TagStatistic;
import org.cerberus.core.crud.entity.UserSystem;
import org.cerberus.core.crud.service.IApplicationService;
import org.cerberus.core.crud.service.IUserSystemService;
import org.cerberus.core.exception.CerberusException;
import org.cerberus.core.util.ParameterParserUtil;
import org.cerberus.core.util.StringUtil;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.BadRequestException;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/campaignexecutions/")
public class CampaignExecutionPrivateController {

    private static final Logger LOG = LogManager.getLogger(CampaignExecutionPrivateController.class);

    @Autowired
    private IUserSystemService userSystemService;

    @Autowired
    private IApplicationService applicationService;

    @Autowired
    private ITagStatisticDAO tagStatisticDAO;

    @GetMapping(path = "/statistics", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> getTagStatistics(
            HttpServletRequest request,
            @RequestParam(name = "systemsFilter", required = false) String[] systemsParam,
            @RequestParam(name = "applicationsFilter", required = false) String[] applicationsParam,
            @RequestParam(name = "group1Filter", required = false) String[] group1Param,
            @RequestParam(name = "from", required = false) String fromParam,
            @RequestParam(name = "to", required = false) String toParam
    ) throws JSONException {
        fromParam = ParameterParserUtil.parseStringParamAndDecode(fromParam, new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.S'Z'").format(new Date()), "UTF8");
        toParam = ParameterParserUtil.parseStringParamAndDecode(toParam, new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.S'Z'").format(new Date()), "UTF8");
        List<String> systems = ParameterParserUtil.parseListParamAndDecode(systemsParam, new ArrayList<>(), "UTF8");
        List<String> applications = ParameterParserUtil.parseListParamAndDecode(applicationsParam, new ArrayList<>(), "UTF8");
        List<String> groups1 = ParameterParserUtil.parseListParamAndDecode(group1Param, new ArrayList<>(), "UTF8");

        String fromDateFormatted = formatDateForDb(fromParam);
        String toDateFormatted = formatDateForDb(toParam);

        JSONObject response = new JSONObject();

        List<String> systemsAllowed;
        List<String> applicationsAllowed;
        try {
            systemsAllowed = getSystemsAllowedForUser(request.getUserPrincipal().getName());
            applicationsAllowed = getApplicationsSystems(systemsAllowed);
        } catch (CerberusException e) {
            throw new BadRequestException();
        }

        //If user put in filter a system that is has no acces, we delete from the list.
        systems.removeIf(param -> !systemsAllowed.contains(param));
        applications.removeIf(param -> !applicationsAllowed.contains(param));

        List<TagStatistic> tagStatistics = tagStatisticDAO.readByCriteria(systems, applications, groups1, fromDateFormatted, toDateFormatted).getDataList();
        LOG.debug(tagStatistics);

        Map<String, Map<String, JSONObject>> agregateByTag = new HashMap<>();
        Map<String, JSONObject> agregateByCampaign = new HashMap<>();
        int nbExeUsefull;
        int nbExe;
        int nbOK;
        long duration;
        for (TagStatistic tagStatistic : tagStatistics) {
            Timestamp minStartDate;
            Timestamp maxEndDate;
            nbExeUsefull = 0;
            nbExe = 0;
            nbOK = 0;
            duration = 0;
            String key = tagStatistic.getCampaign();

            if (!agregateByTag.containsKey(tagStatistic.getCampaign())) {
                agregateByTag.put(key, new HashMap<>());
            }

            if (!agregateByTag.get(tagStatistic.getCampaign()).containsKey(tagStatistic.getTag())) {
                agregateByTag.get(tagStatistic.getCampaign()).put(tagStatistic.getTag(), new JSONObject());
                agregateByTag.get(tagStatistic.getCampaign()).get(tagStatistic.getTag()).put("maxTagDateEnd", "");
                agregateByTag.get(tagStatistic.getCampaign()).get(tagStatistic.getTag()).put("minTagDateStart", "");
                agregateByTag.get(tagStatistic.getCampaign()).get(tagStatistic.getTag()).put("nbExeUsefull", 0);
                agregateByTag.get(tagStatistic.getCampaign()).get(tagStatistic.getTag()).put("nbExe", 0);
                agregateByTag.get(tagStatistic.getCampaign()).get(tagStatistic.getTag()).put("nbOK", 0);
                agregateByTag.get(tagStatistic.getCampaign()).get(tagStatistic.getTag()).put("duration", 0);
                agregateByTag.get(tagStatistic.getCampaign()).get(tagStatistic.getTag()).put("campaignGroup1", tagStatistic.getCampaignGroup1());
                agregateByTag.get(tagStatistic.getCampaign()).get(tagStatistic.getTag()).put("systemList", new JSONArray().toString());
                agregateByTag.get(tagStatistic.getCampaign()).get(tagStatistic.getTag()).put("applicationList", new JSONArray().toString());
            }

            JSONObject mapTag = agregateByTag.get(tagStatistic.getCampaign()).get(tagStatistic.getTag());
            JSONArray systemsInTagMap = new JSONArray(agregateByTag.get(tagStatistic.getCampaign()).get(tagStatistic.getTag()).getString("systemList"));
            JSONArray applicationsInTagMap = new JSONArray(agregateByTag.get(tagStatistic.getCampaign()).get(tagStatistic.getTag()).getString("applicationList"));

            for (int i = 0; i < new JSONArray(tagStatistic.getSystemList()).length(); i++) {
                String element = new JSONArray(tagStatistic.getSystemList()).getString(i);
                if (!contains(systemsInTagMap, element)) {
                    systemsInTagMap.put(element);
                }
            }

            agregateByTag.get(tagStatistic.getCampaign()).get(tagStatistic.getTag()).put("systemList", systemsInTagMap.toString());

            for (int i = 0; i < new JSONArray(tagStatistic.getApplicationList()).length(); i++) {
                String element = new JSONArray(tagStatistic.getApplicationList()).getString(i);
                if (!contains(applicationsInTagMap, element)) {
                    applicationsInTagMap.put(element);
                }
            }

            agregateByTag.get(tagStatistic.getCampaign()).get(tagStatistic.getTag()).put("applicationList", applicationsInTagMap.toString());

            mapTag.put("campaign", tagStatistic.getCampaign());

            if (StringUtil.isEmpty(mapTag.getString("minTagDateStart")) || tagStatistic.getDateStartExe().getTime() < Timestamp.valueOf(mapTag.getString("minTagDateStart")).getTime()) {
                mapTag.put("minTagDateStart", tagStatistic.getDateStartExe());
            }

            if (StringUtil.isEmpty(mapTag.getString("maxTagDateEnd")) || tagStatistic.getDateEndExe().getTime() > Timestamp.valueOf(mapTag.getString("maxTagDateEnd")).getTime()) {
                mapTag.put("maxTagDateEnd", tagStatistic.getDateEndExe());
            }

            long msMinTagDateStart = Timestamp.valueOf(mapTag.getString("minTagDateStart")).getTime();
            long msMaxTagDateEnd = Timestamp.valueOf(mapTag.getString("maxTagDateEnd")).getTime();
            duration = (msMaxTagDateEnd - msMinTagDateStart) / 1000;
            mapTag.put("duration", duration);

            nbExeUsefull += tagStatistic.getNbExeUsefull() + mapTag.getInt("nbExeUsefull");
            mapTag.put("nbExeUsefull", nbExeUsefull);

            nbExe += tagStatistic.getNbExe() + mapTag.getInt("nbExe");
            mapTag.put("nbExe", nbExe);

            nbOK += tagStatistic.getNbOK() + mapTag.getInt("nbOK");
            mapTag.put("nbOK", nbOK);

            mapTag.put("campaignGroup1", tagStatistic.getCampaignGroup1());
        }

        List<JSONObject> aggregateListByCampaign = new ArrayList<>();
        JSONArray allCampaignGroup1 = new JSONArray();

        for (Map.Entry<String, Map<String, JSONObject>> mapCampaignEntry : agregateByTag.entrySet()) {
            String key = mapCampaignEntry.getKey();
            Map<String, JSONObject> value = mapCampaignEntry.getValue();
            double avgDuration = 0;
            double totalDuration = 0;
            String minDateStart = "";
            String maxDateEnd = "";
            double sumPercOK = 0;
            double sumPercReliability = 0;
            int sumNumberExeUsefull = 0;
            String campaignGroup1 = "";
            String campaign = "";
            JSONArray systemsByCampaign = new JSONArray();
            JSONArray applicationsByCampaign = new JSONArray();

            if (!agregateByCampaign.containsKey(key)) {
                agregateByCampaign.put(key, new JSONObject(new LinkedHashMap<>()));
                agregateByCampaign.get(key).put("campaign", key);
                agregateByCampaign.get(key).put("systemList", new JSONArray().toString());
                agregateByCampaign.get(key).put("applicationList", new JSONArray().toString());
                agregateByCampaign.get(key).put("campaignGroup1", "");
                agregateByCampaign.get(key).put("minDateStart", "");
                agregateByCampaign.get(key).put("maxDateEnd", "");
                agregateByCampaign.get(key).put("avgOK", 0);
                agregateByCampaign.get(key).put("avgDuration", 0);
                agregateByCampaign.get(key).put("avgReliability", 0);
                agregateByCampaign.get(key).put("avgNbExeUsefull", 0);

            }

            for (Map.Entry<String, JSONObject> mapTagEntry : value.entrySet()) {

                totalDuration += mapTagEntry.getValue().getLong("duration");

                if (StringUtil.isEmpty(campaignGroup1) && StringUtil.isNotEmpty(mapTagEntry.getValue().getString("campaignGroup1"))) {
                    campaignGroup1 = mapTagEntry.getValue().getString("campaignGroup1");
                    if (!contains(allCampaignGroup1, campaignGroup1)) {
                        allCampaignGroup1.put(campaignGroup1);
                    }
                }

                if (StringUtil.isEmpty(campaign)) {
                    campaign = key;
                }

                if (StringUtil.isEmpty(minDateStart) || Timestamp.valueOf(mapTagEntry.getValue().getString("minTagDateStart")).getTime() < Timestamp.valueOf(minDateStart).getTime()) {
                    minDateStart = mapTagEntry.getValue().getString("minTagDateStart");
                }

                if (StringUtil.isEmpty(maxDateEnd) || Timestamp.valueOf(mapTagEntry.getValue().getString("maxTagDateEnd")).getTime() > Timestamp.valueOf(maxDateEnd).getTime()) {
                    maxDateEnd = mapTagEntry.getValue().getString("maxTagDateEnd");
                }

                sumPercOK += ((double)mapTagEntry.getValue().getInt("nbOK") / mapTagEntry.getValue().getInt("nbExeUsefull"));
                sumPercReliability += ((double)mapTagEntry.getValue().getInt("nbExeUsefull") / mapTagEntry.getValue().getInt("nbExe"));
                sumNumberExeUsefull += mapTagEntry.getValue().getInt("nbExeUsefull");
                systemsByCampaign = new JSONArray(agregateByCampaign.get(key).getString("systemList"));
                applicationsByCampaign = new JSONArray(agregateByCampaign.get(key).getString("applicationList"));

                for (int i = 0; i < new JSONArray(mapTagEntry.getValue().getString("systemList")).length(); i++) {
                    String element = new JSONArray(mapTagEntry.getValue().getString("systemList")).getString(i);
                    if (!contains(systemsByCampaign, element)) {
                        systemsByCampaign.put(element);
                    }
                }

                for (int i = 0; i < new JSONArray(mapTagEntry.getValue().getString("applicationList")).length(); i++) {
                    String element = new JSONArray(mapTagEntry.getValue().getString("applicationList")).getString(i);
                    if (!contains(applicationsByCampaign, element)) {
                        applicationsByCampaign.put(element);
                    }
                }


            }

            agregateByCampaign.get(key).put("campaign", campaign);
            agregateByCampaign.get(key).put("systemList", systemsByCampaign);
            agregateByCampaign.get(key).put("applicationList", applicationsByCampaign);
            agregateByCampaign.get(key).put("campaignGroup1", campaignGroup1);
            agregateByCampaign.get(key).put("avgDuration", totalDuration / mapCampaignEntry.getValue().size());
            agregateByCampaign.get(key).put("minDateStart", minDateStart);
            agregateByCampaign.get(key).put("maxDateEnd", maxDateEnd);
            agregateByCampaign.get(key).put("avgOK", (sumPercOK * 100.0) / mapCampaignEntry.getValue().size());
            agregateByCampaign.get(key).put("avgReliability", (sumPercReliability * 100) / mapCampaignEntry.getValue().size());
            agregateByCampaign.get(key).put("avgNbExeUsefull", sumNumberExeUsefull / mapCampaignEntry.getValue().size());
        }

        agregateByCampaign.forEach((key, value) -> aggregateListByCampaign.add(value));
        response.put("contentTable", aggregateListByCampaign);
        response.put("allCampaignGroup1", allCampaignGroup1);
        return ResponseEntity.ok(response.toString());
    }

    private List<String> getSystemsAllowedForUser(String user) throws CerberusException {
        List<UserSystem> systemsAllowedForUser = userSystemService.findUserSystemByUser(user);
        return systemsAllowedForUser.stream().map(UserSystem::getSystem).collect(Collectors.toList());
    }

    private List<String> getApplicationsSystems(List<String> systems) {
        List<Application> applicationsAllowedForUser = applicationService.readBySystem(systems).getDataList();
        return applicationsAllowedForUser.stream().map(Application::getApplication).collect(Collectors.toList());
    }

    private boolean contains(JSONArray array, String value) throws JSONException {
        for (int i = 0; i < array.length(); i++) {
            if (array.getString(i).equals(value)) {
                return true;
            }
        }
        return false;
    }

    private String formatDateForDb(String date) {
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
}
