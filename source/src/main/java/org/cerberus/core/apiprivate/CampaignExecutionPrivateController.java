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
import javax.ws.rs.BadRequestException;
import java.text.SimpleDateFormat;
import java.util.*;

@RestController
@RequestMapping("/campaignexecutions/")
public class CampaignExecutionPrivateController {

    private static final Logger LOG = LogManager.getLogger(CampaignExecutionPrivateController.class);
    @Autowired
    private ITagStatisticService tagStatisticService;

    @GetMapping(path = "/statistics", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> getTagStatistics(
            HttpServletRequest request,
            @RequestParam(name = "systemsFilter", required = false) String[] systemsParam,
            @RequestParam(name = "applicationsFilter", required = false) String[] applicationsParam,
            @RequestParam(name = "group1Filter", required = false) String[] group1Param,
            @RequestParam(name = "from", required = false) String fromParam,
            @RequestParam(name = "to", required = false) String toParam
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
            applicationsAllowed = tagStatisticService.getApplicationsSystems(systemsAllowed);
        } catch (CerberusException e) {
            throw new BadRequestException();
        }

        if (systems.isEmpty() && applications.isEmpty()) {
            tagStatistics = new ArrayList<>();
        } else {
            //If user put in filter a system that is has no access, we delete from the list.
            systems.removeIf(param -> !systemsAllowed.contains(param));
            applications.removeIf(param -> !applicationsAllowed.contains(param));
            tagStatistics = tagStatisticService.readByCriteria(systems, applications, group1List, fromDateFormatted, toDateFormatted).getDataList();
        }

        try {
            Map<String, Map<String, JSONObject>> aggregateByTag = tagStatisticService.createMapAggregateByTag(tagStatistics);
            Map<String, JSONObject> aggregateByCampaign = tagStatisticService.createMapAggregateByCampaign(aggregateByTag);
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
            LOG.error("Internal server error: ", ex);
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Internal server error: " + ex.getMessage());
        }

    }
}
