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
import org.cerberus.core.crud.entity.TagStatistic;
import org.cerberus.core.crud.service.ITagStatisticService;
import org.cerberus.core.exception.CerberusException;
import org.cerberus.core.util.ParameterParserUtil;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.text.SimpleDateFormat;
import java.util.*;
import org.cerberus.core.crud.service.ITagService;
import org.cerberus.core.util.servlet.ServletUtil;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

@RestController
@RequestMapping("/campaignexecutions/")
public class CampaignExecutionPrivateController {

    private static final Logger LOG = LogManager.getLogger(CampaignExecutionPrivateController.class);
    @Autowired
    private ITagStatisticService tagStatisticService;
    @Autowired
    private ITagService tagService;

    @GetMapping(path = "/statistics", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> getTagStatistics(
            HttpServletRequest request,
            @RequestParam(name = "systems") String[] systemsParam,
            @RequestParam(name = "applications") String[] applicationsParam,
            @RequestParam(name = "group1") String[] group1Param,
            @RequestParam(name = "from") String fromParam,
            @RequestParam(name = "to") String toParam
    ) {
        //Retrieve and format URL parameter
        fromParam = ParameterParserUtil.parseStringParamAndDecode(fromParam, new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.S'Z'").format(new Date()), "UTF8");
        toParam = ParameterParserUtil.parseStringParamAndDecode(toParam, new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.S'Z'").format(new Date()), "UTF8");
        List<String> systems = ParameterParserUtil.parseListParamAndDecode(systemsParam, new ArrayList<>(), "UTF8");
        List<String> applications = ParameterParserUtil.parseListParamAndDecode(applicationsParam, new ArrayList<>(), "UTF8");
        List<String> group1List = ParameterParserUtil.parseListParamAndDecode(group1Param, new ArrayList<>(), "UTF8");
        String fromDateFormatted = tagStatisticService.formatDateForDb(fromParam);
        String toDateFormatted = tagStatisticService.formatDateForDb(toParam);
        List<TagStatistic> tagStatistics;
        List<String> systemsAllowed;
        List<String> applicationsAllowed;
        JSONObject response = new JSONObject();

        try {
            systemsAllowed = tagStatisticService.getSystemsAllowedForUser(request.getUserPrincipal().getName());
        } catch (CerberusException exception) {
            LOG.error("Unable to get allowed systems: ", exception);
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Unable to get allowed systems: " + exception.getMessage());
        }

        applicationsAllowed = tagStatisticService.getApplicationsSystems(systemsAllowed);

        if (systems.isEmpty() && applications.isEmpty()) {
            tagStatistics = new ArrayList<>();
        } else {
            //If user put in filter a system that is has no access, we delete from the list.
            systems.removeIf(param -> !systemsAllowed.contains(param));
            applications.removeIf(param -> !applicationsAllowed.contains(param));
            tagStatistics = tagStatisticService.readByCriteria(systems, applications, group1List, fromDateFormatted, toDateFormatted).getDataList();
        }

        try {
            Map<String, Map<String, JSONObject>> aggregateByTag = tagStatisticService.createMapGroupedByTag(tagStatistics, "CAMPAIGN");
            Map<String, JSONObject> aggregateByCampaign = tagStatisticService.createMapAggregatedStatistics(aggregateByTag, "CAMPAIGN");
            List<JSONObject> aggregateListByCampaign = new ArrayList<>();
            aggregateByCampaign.forEach((key, value) -> {
                if (!Objects.equals(key, "globalGroup1List")) {
                    aggregateListByCampaign.add(value);
                }
            });
            response.put("globalGroup1List", aggregateByCampaign.get("globalGroup1List").get("array"));
            response.put("campaignStatistics", aggregateListByCampaign);
            return ResponseEntity.ok(response.toString());
        } catch (JSONException ex) {
            LOG.error("Error when JSON processing: ", ex);
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error when JSON processing: " + ex.getMessage());
        }
    }

    @GetMapping(path = "/statistics/{campaign}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> getCampaignStatisticsByCountryEnv(
            HttpServletRequest request,
            @PathVariable("campaign") String campaign,
            @RequestParam(name = "countries", required = false) String[] countriesParam,
            @RequestParam(name = "environments", required = false) String[] environmentsParam,
            @RequestParam(name = "from") String fromParam,
            @RequestParam(name = "to") String toParam
    ) throws JSONException {
        fromParam = ParameterParserUtil.parseStringParamAndDecode(fromParam, new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.S'Z'").format(new Date()), "UTF8");
        toParam = ParameterParserUtil.parseStringParamAndDecode(toParam, new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.S'Z'").format(new Date()), "UTF8");
        List<String> countries = ParameterParserUtil.parseListParamAndDecode(countriesParam, new ArrayList<>(), "UTF8");
        List<String> environments = ParameterParserUtil.parseListParamAndDecode(environmentsParam, new ArrayList<>(), "UTF8");
        String fromDateFormatted = tagStatisticService.formatDateForDb(fromParam);
        String toDateFormatted = tagStatisticService.formatDateForDb(toParam);
        List<TagStatistic> tagStatistics;
        JSONObject response = new JSONObject();

        tagStatistics = tagStatisticService.readByCriteria(campaign, countries, environments, fromDateFormatted, toDateFormatted).getDataList();

        boolean userHasRightSystems = tagStatisticService.userHasRightSystems(request.getUserPrincipal().getName(), tagStatistics);
        if (!userHasRightSystems) {
            response.put("message", "User has no access to required systems.");
            return ResponseEntity
                    .status(HttpStatus.FORBIDDEN)
                    .body(response.toString());
        }

        Map<String, Map<String, JSONObject>> aggregateByTag = tagStatisticService.createMapGroupedByTag(tagStatistics, "ENV_COUNTRY");
        Map<String, JSONObject> aggregateByCampaign = tagStatisticService.createMapAggregatedStatistics(aggregateByTag, "ENV_COUNTRY");
        List<JSONObject> aggregateListByCampaign = new ArrayList<>();
        countries.clear();
        environments.clear();
        aggregateByCampaign.forEach((key, value) -> {
            aggregateListByCampaign.add(value);
            String environment = key.split("_")[0];
            String country = key.split("_")[1];
            if (!environments.contains(environment)) environments.add(environment);
            if (!countries.contains(country)) countries.add(country);
        });

        response.put("campaignStatistics", aggregateListByCampaign);
        response.put("environments", environments);
        response.put("countries", countries);
        return ResponseEntity.ok(response.toString());
    }

    @PostMapping("{executionId}/declareFalseNegative")
    public String updateDeclareFalseNegative(
            @PathVariable("executionId") String tag,
            HttpServletRequest request) {

        // Calling Servlet Transversal Util.
        ServletUtil.servletStart(request);
        try {
            tagService.updateFalseNegative(tag, true, request.getUserPrincipal().getName());
        } catch (CerberusException ex) {
            return ex.toString();
        }
        return "";

    }

    @PostMapping("{executionId}/undeclareFalseNegative")
    public String updateUndeclareFalseNegative(
            @PathVariable("executionId") String tag,
            HttpServletRequest request) {

        // Calling Servlet Transversal Util.
        ServletUtil.servletStart(request);
        try {
            tagService.updateFalseNegative(tag, false, request.getUserPrincipal().getName());
        } catch (CerberusException ex) {
            return ex.toString();
        }
        return "";

    }

}
