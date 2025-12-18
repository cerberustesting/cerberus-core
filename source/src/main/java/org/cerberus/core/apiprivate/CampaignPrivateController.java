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
package org.cerberus.core.apiprivate;

import io.swagger.v3.oas.annotations.Operation;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;
import javax.servlet.http.HttpServletRequest;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.cerberus.core.api.dto.campaign.CampaignMonthlyStatsDTOV001;
import org.cerberus.core.api.dto.campaign.CampaignStatsDTOV001;
import org.cerberus.core.crud.entity.stats.CampaignStats;
import org.cerberus.core.crud.factory.IFactoryLogEvent;
import org.cerberus.core.crud.factory.IFactoryTest;
import org.cerberus.core.crud.service.ILogEventService;
import org.cerberus.core.crud.service.IParameterService;
import org.cerberus.core.crud.service.ITestService;
import org.cerberus.core.crud.service.impl.CampaignService;
import org.cerberus.core.crud.service.impl.TestCaseExecutionService;
import org.cerberus.core.engine.scheduler.SchedulerInit;
import org.cerberus.core.util.servlet.ServletUtil;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.owasp.html.PolicyFactory;
import org.owasp.html.Sanitizers;
import org.quartz.Trigger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 *
 * @author bcivel
 */
@RestController
@RequestMapping("/campaigns")
public class CampaignPrivateController {

    private static final Logger LOG = LogManager.getLogger(CampaignPrivateController.class);
    private final PolicyFactory policy = Sanitizers.FORMATTING.and(Sanitizers.LINKS);
    private static final String DATE_FORMAT = "yyyy-MM-dd'T'HH:mm:ss";

    @Autowired
    CampaignService campaignService;
    @Autowired
    SchedulerInit scInit;

    /**
     * Read By Key
     *
     * @param request
     * @param test
     * @return
     */
    @Operation(hidden=true)
    @GetMapping("/scheduled")
    public String readByKey(HttpServletRequest request, String test) {

        JSONObject object = new JSONObject();

        try {
            // Calling Servlet Transversal Util.
            ServletUtil.servletStart(request);

            if (scInit != null) {
                object.put("schedulerInstanceVersion", scInit.getInstanceSchedulerVersion());
                object.put("schedulerReloadIsRunning", scInit.isIsRunning());
                // We get here the list of triggers of Quartz scheduler.
                List<JSONObject> triggerList = new ArrayList<>();
                for (Trigger triggerSet : scInit.getMyTriggersSet()) {

                    Date now = new Date();
                    Date nextTrigger = triggerSet.getFireTimeAfter(new Date());

                    for (int i = 0; i < 10; i++) {

                        JSONObject objectTrig = new JSONObject();
                        objectTrig.put("triggerId", triggerSet.getJobDataMap().getLong("schedulerId"));
                        objectTrig.put("triggerName", triggerSet.getJobDataMap().getString("name"));
                        objectTrig.put("triggerType", triggerSet.getJobDataMap().getString("type"));
                        objectTrig.put("triggerUserCreated", triggerSet.getJobDataMap().getString("user"));
                        objectTrig.put("triggerNextFiretime", nextTrigger);
                        objectTrig.put("triggerNextFiretimeTimestamp", new SimpleDateFormat(DATE_FORMAT).format(nextTrigger));
                        objectTrig.put("triggerNextFiretimeDurationToTriggerInMs", Math.abs(nextTrigger.getTime() - now.getTime()));
                        objectTrig.put("triggerCronDefinition", triggerSet.getJobDataMap().getString("cronDefinition"));
                        triggerList.add(objectTrig);

                        nextTrigger = triggerSet.getFireTimeAfter(nextTrigger);

                    }
                }
                Collections.sort(triggerList, new SortTriggers());
                JSONArray object1 = new JSONArray(triggerList);
                object.put("schedulerTriggers", object1);
                Date now = new Date();
                object.put("serverDate", new SimpleDateFormat(DATE_FORMAT).format(now));
                object.put("serverTimeZone", TimeZone.getDefault().getDisplayName());
            }

        } catch (JSONException ex) {
            LOG.warn(ex);
        }
        return object.toString();

    }

    class SortTriggers implements Comparator<JSONObject> {

        // Used for sorting Triggers 
        @Override
        public int compare(JSONObject a, JSONObject b) {

            if (a != null && b != null) {
                Date dateA;
                Date dateB;
                try {
                    dateA = (Date) a.get("triggerNextFiretime");
                    dateB = (Date) b.get("triggerNextFiretime");
                    if (dateA.equals(dateB)) {

                    } else {
                        return (dateA.compareTo(dateB));
                    }
                } catch (JSONException ex) {
                    LOG.error("Exception on JSON Parse.", ex);
                } catch (ClassCastException ex) {
                    return 1;
                }

            } else {
                return 1;
            }

            return 1;
        }
    }

    @Operation(hidden = true)
    @GetMapping("/monthlyStats")
    public CampaignMonthlyStatsDTOV001 getMonthlyStats(
            @RequestParam(name = "system", required = false) List<String> systems) {

        LocalDate today = LocalDate.now();

        // Périodes
        LocalDate thisStartDate = today.minusDays(30);
        LocalDate thisEndDate   = today;

        // --- Get Global Stats : All dates, All systems --- and build DTO
        CampaignStats statsGlobal = campaignService.readCampaignStats(null, null, null).getItem();
        CampaignStatsDTOV001 statsGlobalDto = CampaignStatsDTOV001.builder()
                .totalCampaigns(statsGlobal.getTotalCampaignsExisting())
                .totalCampaignsLaunched(statsGlobal.getTotalCampaignsLaunched())
                .fromDate(statsGlobal.getFromDate())
                .toDate(statsGlobal.getToDate())
                .build();

        // --- Get last month Stats : Last 30 days, All systems --- and build DTO
        CampaignStats statsGlobalPreviousMonth = campaignService
                .readCampaignStats(thisStartDate.toString(), thisEndDate.toString(), null)
                .getItem();
        CampaignStatsDTOV001 statsGlobalPreviousMonthDto = CampaignStatsDTOV001.builder()
                .totalCampaigns(statsGlobalPreviousMonth.getTotalCampaignsExisting())
                .totalCampaignsLaunched(statsGlobalPreviousMonth.getTotalCampaignsLaunched())
                .fromDate(statsGlobalPreviousMonth.getFromDate())
                .toDate(statsGlobalPreviousMonth.getToDate())
                .build();

        // --- Get Selected System Stats : All dates, selected systems --- and build DTO
        CampaignStats statsSystems = campaignService
                .readCampaignStats(null, null, systems)
                .getItem();
        CampaignStatsDTOV001 statsSystemsDto = CampaignStatsDTOV001.builder()
                .totalCampaigns(statsSystems.getTotalCampaignsExisting())
                .totalCampaignsLaunched(statsSystems.getTotalCampaignsLaunched())
                .fromDate(statsSystems.getFromDate())
                .toDate(statsSystems.getToDate())
                .build();

        // --- Get Selected System Stats : Last 30 days, selected systems --- and build DTO
        CampaignStats statsSystemsPreviousMonth = campaignService
                .readCampaignStats(thisStartDate.toString(), thisEndDate.toString(), systems)
                .getItem();
        CampaignStatsDTOV001 statsSystemsPreviousMonthDto = CampaignStatsDTOV001.builder()
                .totalCampaigns(statsSystemsPreviousMonth.getTotalCampaignsExisting())
                .totalCampaignsLaunched(statsSystemsPreviousMonth.getTotalCampaignsLaunched())
                .fromDate(statsSystemsPreviousMonth.getFromDate())
                .toDate(statsSystemsPreviousMonth.getToDate())
                .build();

        // --- Build DTO final ---
        return CampaignMonthlyStatsDTOV001.builder()
                .global(statsGlobalDto)
                .globalPreviousMonth(statsGlobalPreviousMonthDto)
                .system(statsSystemsDto)
                .systemPreviousMonth(statsSystemsPreviousMonthDto)
                .build();
    }
}
