/**
 * Cerberus Copyright (C) 2013 - 2026 cerberustesting
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
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;
import org.cerberus.core.api.dto.monitor.MonitorKpiDTOV001;
import org.cerberus.core.api.dto.monitor.MonitorKpiScopeDTOV001;
import org.cerberus.core.crud.entity.stats.ApplicationStats;
import org.cerberus.core.crud.entity.stats.CampaignStats;
import org.cerberus.core.crud.entity.stats.TestCaseExecutionStats;
import org.cerberus.core.crud.entity.stats.TestCaseStats;
import org.cerberus.core.crud.service.IApplicationService;
import org.cerberus.core.crud.service.ICampaignService;
import org.cerberus.core.crud.service.ITestCaseExecutionService;
import org.cerberus.core.crud.service.ITestCaseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

/**
 * Figures of the KPI widgets of My Monitor. Unlike the monthlyStats of the
 * homepage cards, the period is a parameter, and the figures over the period
 * are the ones created (or launched, or executed) during it.
 *
 * @author bcivel
 */
@RestController
@RequestMapping("/monitor")
public class MonitorPrivateController {

    private static final int MAX_DAYS = 365;

    @Autowired
    private IApplicationService applicationService;
    @Autowired
    private ITestCaseService testCaseService;
    @Autowired
    private ICampaignService campaignService;
    @Autowired
    private ITestCaseExecutionService executionService;

    /**
     * @param source applications, testcases, campaigns or executions
     * @param days length of the period (1 to 365)
     * @param systems systems of the "system" scope, all the systems of the user when empty
     */
    @Operation(hidden = true)
    @GetMapping("/kpi")
    public MonitorKpiDTOV001 getKpi(
            @RequestParam(name = "source") String source,
            @RequestParam(name = "days", defaultValue = "30") int days,
            @RequestParam(name = "system", required = false) List<String> systems) {

        if (days < 1 || days > MAX_DAYS) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "days must be between 1 and " + MAX_DAYS);
        }

        LocalDate today = LocalDate.now();
        // The queries of applications, test cases and executions compare the dates with a date at midnight:
        // the end of the period is tomorrow to include today.
        Period period = new Period(today.minusDays(days), today.plusDays(1), today.minusDays(2L * days), today.minusDays(days));
        // The campaign query takes the whole last day, the previous period stops the day before.
        Period campaignPeriod = new Period(today.minusDays(days), today, today.minusDays(2L * days), today.minusDays(days + 1L));

        BiFunction<Period, List<String>, MonitorKpiScopeDTOV001> scope;
        switch (source) {
            case "applications":
                scope = (p, s) -> applications(p, s);
                break;
            case "testcases":
                scope = (p, s) -> testcases(p, s);
                break;
            case "campaigns":
                scope = (p, s) -> campaigns(campaignPeriod, s);
                break;
            case "executions":
                scope = (p, s) -> executions(p, s);
                break;
            default:
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "unknown source " + source);
        }

        return MonitorKpiDTOV001.builder()
                .source(source)
                .days(days)
                .system(scope.apply(period, systems))
                .global(scope.apply(period, null))
                .build();
    }

    private MonitorKpiScopeDTOV001 applications(Period p, List<String> systems) {
        ApplicationStats all = applicationService.readApplicationStats(null, null, systems).getItem();
        ApplicationStats now = applicationService.readApplicationStats(p.from(), p.to(), systems).getItem();
        ApplicationStats before = applicationService.readApplicationStats(p.previousFrom(), p.previousTo(), systems).getItem();
        Map<String, Integer> current = new HashMap<>();
        Map<String, Integer> previous = new HashMap<>();
        current.put("total", all.getTotalApplications());
        current.put("created", now.getTotalApplications());
        previous.put("created", before.getTotalApplications());
        return scope(current, previous);
    }

    private MonitorKpiScopeDTOV001 testcases(Period p, List<String> systems) {
        TestCaseStats all = testCaseService.readTestCaseStats(null, null, systems).getItem();
        TestCaseStats now = testCaseService.readTestCaseStats(p.from(), p.to(), systems).getItem();
        TestCaseStats before = testCaseService.readTestCaseStats(p.previousFrom(), p.previousTo(), systems).getItem();
        Map<String, Integer> current = new HashMap<>();
        Map<String, Integer> previous = new HashMap<>();
        current.put("total", all.getTotalCount());
        current.put("working", all.getWorkingCount());
        current.put("created", now.getTotalCount());
        previous.put("created", before.getTotalCount());
        return scope(current, previous);
    }

    private MonitorKpiScopeDTOV001 campaigns(Period p, List<String> systems) {
        CampaignStats all = campaignService.readCampaignStats(null, null, systems).getItem();
        CampaignStats now = campaignService.readCampaignStats(p.from(), p.to(), systems).getItem();
        CampaignStats before = campaignService.readCampaignStats(p.previousFrom(), p.previousTo(), systems).getItem();
        Map<String, Integer> current = new HashMap<>();
        Map<String, Integer> previous = new HashMap<>();
        current.put("total", all.getTotalCampaignsExisting());
        current.put("launched", now.getTotalCampaignsLaunched());
        previous.put("launched", before.getTotalCampaignsLaunched());
        return scope(current, previous);
    }

    // No total: counting every execution of the table is not worth it for a figure.
    private MonitorKpiScopeDTOV001 executions(Period p, List<String> systems) {
        TestCaseExecutionStats now = executionService.readTestCaseExecutionStats(p.from(), p.to(), systems).getItem();
        TestCaseExecutionStats before = executionService.readTestCaseExecutionStats(p.previousFrom(), p.previousTo(), systems).getItem();
        Map<String, Integer> current = new HashMap<>();
        Map<String, Integer> previous = new HashMap<>();
        current.put("executed", now.getTotalTestCaseExecutions());
        previous.put("executed", before.getTotalTestCaseExecutions());
        return scope(current, previous);
    }

    private static MonitorKpiScopeDTOV001 scope(Map<String, Integer> current, Map<String, Integer> previous) {
        return MonitorKpiScopeDTOV001.builder().current(current).previous(previous).build();
    }

    /** The period and the one just before, as the dates the stats of the services take. */
    private record Period(LocalDate fromDate, LocalDate toDate, LocalDate previousFromDate, LocalDate previousToDate) {

        String from() { return fromDate.toString(); }
        String to() { return toDate.toString(); }
        String previousFrom() { return previousFromDate.toString(); }
        String previousTo() { return previousToDate.toString(); }
    }
}
